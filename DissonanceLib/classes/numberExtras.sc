+ SimpleNumber {
//	 The following methods are also defined for SequenceableCollection below.
//	 Methods that can be applied to rational numbers will work with arrays of [p,q]'s

	//ex. 3.cents(2) or 440.cents(60.midicps) or [2,3].cents
//	cents { | frq = 1| 
//		^1200 * ( (this/frq).log / 2.log )
//	}
// used to be the mathematical formula above but was changed for efficiency to:
	cents {|frq=1|
		^( (this/frq).ratiomidi * 100 )
	}
	
	//ex. 440.addCents(Array.series(12, 0, 100)).asNote
	addCents { |cents|
		^this * (2**(cents/1200))
	}
	
	// basically the same but with different semantics: 
	// ex. 833.cents2Frq
	cents2Frq {|frq = 1| ^frq.addCents(this)}

	asNote { var residue, octave, note, roundedNote;
		note = this.cpsmidi; 
		roundedNote = note.round(1);
		octave = ((roundedNote / 12).asInteger) - 1;
		residue  = (note.frac * 100).round(1);
		if (residue > 50) {
			^[NoteNames.flatnames[(roundedNote - 72) % 12].asString ++ octave.asString, 
				(100 - residue).neg]
		}{
			^[NoteNames.names[(roundedNote - 72) % 12].asString ++ octave.asString, 
				residue];
		}
	}
	
	// utility to calculate pitch bends for midi playback of microtones
	// this is in cents, pb is the pitch bend ammount (400 = +- 1 tone, i.e. -200 to +200 cents)
	// ex. 50.asPitchBend - > 16
	asPitchBend{ |pb = 400| ^128 / (pb / this) } 

	asBark { var bk;
		if (this <= 219.5) {
			bk = 13.3 * atan( 3 * this / 4000);	// Terhardt 1979
		}{
			bk = ( (26.81 * this) / (1960 + this) ) - 0.53; // Traunmuller
//			bk = (26.81 / (1 + (1960 / this)) - 0.53); // just slightly different formulation
			if (bk > 20.1) { bk = bk + (0.22 * (bk-20.1)) }
		}
		^bk
	}

	// this method of conversion comes from the definition of the edges of the critical bandwidth
	barkToFreq {var barkEdge = #[0, 100, 200, 300, 400, 510, 630, 770, 920, 1080, 1270, 1480, 
			1720, 2000, 2320, 2700, 3150, 3700, 4400, 5300, 6400, 7700, 9500, 12000, 15500];
			^barkEdge.blendAt(this)
	}
	
	// this one is the inverse of the Traunmuller approximation function used in asBark
	// differs from asBark below 220 hz
	barkToHz { ^1960 / (26.81 / (this + 0.53) - 1) }
	
	// gives the size (in Hz) of the critical bandwidth given in barks
	criticalBW { ^52548 / (this.squared - (52.56 * this) + 690.39)}

	// freqs to ERB (Equivalent Rectangular Bandwidth, another scale based on the CBW)
	// also called ERB-rate of a tone. It is mainly used for masking analysis 
	hzToErb { ^11.17 * log( (this + 312) / (this + 14675)) + 43.0}
	
	hzToMel { ^1127.01048 * log( 1 + (this/700))}
	
	melToHz { ^700 * ((this/1127.01048).exp -1)}

	phonToSone { ^2**((this - 40) / 10)}
	
	soneToPhon { ^10 * (4 + (this.log10 / 2.log10))}
	
	// calibration: should be 0 if loudness is in dB spl; a positive number if the values
	// are in dBFS or negative dB's relative to 0. 
	// This is the case when translating amps to db with ampdb.
	asPhon {|spl, calib = 0| ^LoudnessModel.calc(this, spl + calib)}
	
	asSone {|spl, calib = 0| ^this.asPhon(spl, calib).phonToSone }
	
	// this is an amplitude converted into ...ppp - fff
	asDynamic {|freq = 1000, ref = 100, fff = 3| var num, char;
		num = freq.asSone(this.ampdb, ref).log2.round(1) - fff;
		if (num == 0) 
			{ 
				^\mf
			}{
				if (num.isNegative) { char = 'p' } { char = 'f' };
				^(char!num.abs).join
			};
	}
	
/*	
	Bn (k) = 20log10(zeta * |Xn (k)|) where zeta = 184268 (see Nick Collins' PhD thesis p. 66)
	zeta = 8 is usually enough for FFT values
*/	
	bintodb {|zeta = 8| ^( 20 * (zeta * this.abs).log10) } 

	// size of wavelength in meters:
	asWavelength  {|c = 343|  ^c/this } // c is speed of sound in m/s @ 20 celsius	
	factorial { // the highest factorial that can be represented as a Float is 171
		^(2..this.asFloat).product
	}
	
/*	Return a sequence of largest prime powers for a given harmonicity minimum. Pitch range is 
	in octaves, ex, 0.03.minHarmonicityVector(1,13) yields [12, 8, 3, 2, 1, 1]. 
	They correspond to the powers of the harmonic space bases 2,3,5,7,11,and 13 inside an octave.
	"A maximum powers sequence includes intervals, the harmonicities of which may lie
	below the minimum suggested [by this method]...The maximum power sequence guarantees merely
	that all intervals that are more harmonic than a given minimum [harmonicity] value can be 
	expressed by the sequence. [12, 8, 3, 2, 1, 1] results in as many as 3,964 different intervals 
	within one octave (!), of which only 211 are truly more harmonic than 0.03" ("Two Essays on 	Theory", C.Barlow (CMJ, 1987, see formula in highestPower method below).             */ 
	minHarmonicityVector {|pitchRange = 1, maxPrime = 11|      
	     ^Array.primes(maxPrime).collect{|p| p.highestPower(this, pitchRange)} 
	}

	// this is like asFraction but hacked in order to handle rounding errors for
	// harmonic interpretation of periodic decimals (0.333 will be 1/3 and not 333/1000)
		asRatio {|denominator = 100, fasterBetter = true|
		var num = this, str, a, b, f;
		str = this.asString;
//		if ( (str.contains(".")) and: (str.size > 3) ) // only in pertinent cases
		f = str.find(".");
		if (f.notNil) { 
			if (str[f..].size > 2) 
				{
					a = str.wrapAt(-2).digit; 
					b = str.last.digit; // get last 2 digits
					if ( (a == b) or: ((a + 1) == b) ) {	// cases like 1.33 and 1.67
						num = (str.drop(-1) ++ "".catList(a!12)).asFloat 
					}
				}
		};
		^num.asFraction(denominator, fasterBetter)
	}
	
}

+ Integer {
		
	// Barlow's Indigestibility of an integer 
	// (low vs. high prime factors as a measure of "digestibility"):
	indigestibility { var sum = 0; 
		if ( this <= 1 ) { ^0 };
		this.factors.asBag.contents.pairsDo{|y0, y1| 
			 sum = sum + ((y1 * (y0 - 1).squared) / y0)
		};
		^sum * 2
	}
	// note: at prime 46349, 32-bit integer arithmetic overflows
	// and gives wrong (negative) indigestibilites...

	//  Barlow's Harmonicity formula (for an interval p/q):
	harmonicity {|q| 
		var numer = this.indigestibility, denom = q.indigestibility;
		^(denom - numer).sign / (numer + denom);
	}
	
	// formula N(p) from "Two Essays on Theory", C.Barlow (CMJ, 1987): (see minHarmVector above)
	highestPower {| minHarmonicity = 1, pitchRange = 1|
		if (this.isPrime.not) {"Number has to be prime".warn; ^nil};
		if (this == 2) { 
			^((pitchRange + (minHarmonicity.reciprocal)) / 
				(1 + (256.log / 27.log))).trunc
		}{
			^((pitchRange + (minHarmonicity.reciprocal)) / 
				(this.indigestibility + (this.log / 2.log))).trunc
		}
	}
	
	divisorSet { ^(1..this).select{|i| (this / i).frac == 0} }
			
	multiples {|... primes|
		var factorList, multiples;
		multiples = Array.newClear;
		(2..this).do{|i|
			factorList = i.factors;
			primes.do{|j|
				factorList.occurrencesOf(j).do{
							factorList.remove(j)} };
			if (factorList.isEmpty) {multiples = multiples.add(i)} };
		^multiples;
	}

/* 
 USAGE: a_number.harmonics(highest_harmonic)
 returns an Array with the harmonics of a number up to highest
 Ex: 5.harmonics(48) ->  [ 5, 10, 15, 20, 25, 30, 35, 40, 45 ]

*/
	harmonics {|max|
		var n = 1, h = 1, result;
		result = Array.newClear;
		{ h <= max }.while{ 
			h = this * n;
			result = result.add(h);
			n = n + 1;
			};
		result.pop;	
		h = result[0] ? nil;
		if (h.notNil, {^result}, {^nil});
	}
	
/*
USAGE:  a_prime.primeHarmonics(highest_harmonic)
			returns a nested array with all the harmonics up to highest_harmonic
			of a_prime along with all lower primes.
			Ex.	17.primeHarmonics(20) ->
			[ [ 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 ], [ 3, 6, 9, 12, 15, 18 ], [ 5, 10, 15, 20 ], [ 7, 14 ], [ 11 ], [ 13 ], [ 17 ] ]		
*/	
	primeHarmonics {|maxPartial|
		var primeList, result = Array.newClear;
		if (this.isPrime.not) {"Number has to be prime".warn; ^nil};
		primeList = Array.primes(this);
		result = primeList.collect({|i| i.harmonics(maxPartial)}); 
		result.removeAllSuchThat({|n| n.isNil});
		^result;
	} 

/*	
 USAGE: a_prime.listOfHarmonics(highest_harmonic)
		returns an array with all the harmonics (up to highest) of 
		all the primes below (and including) a_prime
		Ex.  7.listOfHarmonics(50)	
 this is equivalent to: highest_harmonic.multiples(array of primes up to a_prime)
*/	
	listOfHarmonics {|max|
		var result;
		result = this.primeHarmonics(max);
		result = result.flatten;
		result = result.sort;
		result.removeAllSuchThat({|n| result.occurrencesOf(n) > 1});
		^result
	}
	
	vpNumbers {|primeArray| ^primeArray.collect{|n| n.harmonics(n * this) } }
}

+ SequenceableCollection {
//  instead make a Ratio class!!
	
//	The following three methods deal with rational numbers, expressed as [p,q] arrays: 

	
	// rational division: [p,q] / [r,s]. ex: [5,9].ratioDiv([2,6]) -> [5,3]
	ratioDiv {|that, reduce = true| 
		var div = [ this[0] * that[1], this[1] * that[0] ];
		if (reduce) {^div.reduceRatio}{^div}
	}
	
	// express a rational as [p,q] where p and q are coprime:
	reduceRatio { ^this div: (this[0] gcd: this[1]) }

	reduceOctave { ^this.asHvector.collect{|x|  x.reducedRatio.as(Array) } }		
		
//			var fratio = x[0]/x[1], pow2 = 0, res = x.copy;
//			{ fratio < 1 }.while{ fratio = fratio * 2; pow2 = pow2 + 1 };
//			{ fratio >= 2 }.while{ fratio = fratio / 2; pow2 = pow2 - 1 };
//			if (pow2.isNegative) { 
//				res[1] = (res[1] * ( 2**pow2.abs )).asInteger
//			}{
//				res[0] = (res[0] * ( 2**pow2 )).asInteger
//			}; 
//			res
//		}
//	}


	ratioPost {|char = ", "| 
		if ( (this.size == 2) and: (this[0].isNumber) ) {
			^this[0].asString ++ "/" ++ this[1].asString
		}{	
			^this.collect{|d| d[0].asString ++ "/" ++ d[1].asString}.join(char)
		}
	}	
	
	
	// sort an array of [p,q] ratios
	ratioSort { ^this[this.ratioToFreq.order] }
	
	
	// lowest common denominator
	lcd { ^this.collect{|x| x[1] }.reduce(\lcm) }
	
	
	// adapted from John Chalmers' "Divisions of the Tetrachord", chapter 2
	katapykne {|n = 1| var ints = ((this[0] * n)..(this[1] * n)).reverse;
		^(ints.size - 1).collect{|i|
			[ ints[i+1], ints[i] ]
		}.reverse
	}
	
	katapykne_ab {|a = 1, b = 2| 
		^[
			[(a+b) * this[0], (b*this[0]) + (a*this[1])].reduceRatio,
			[(b*this[0]) + (a*this[1]), (a+b) * this[1]].reduceRatio
		]
	}
	
	ratioToFreq { 
		if ( (this.size == 2) and: (this[0].isNumber) )
			{
				^this[0]/this[1]
			}{
				^this.collect{|x| x.ratioToFreq } 
			}
	}		

	// converting pitch sets from absoulte intervals to adjacency intervals
	ratioDifferentiate { 
		var list, prev = [1,1];
		list = this.class.new(this.size);
		this.do{|x|
			list.add(x.ratioDiv(prev));
			prev = x;
		};
		^list
	}
	
	// from adjecency to absolute intervals: 
	ratioIntegrate { var res, list, prev = [1,1];
		list = this.class.new(this.size);
		this.do{|x| 
			res = (x * prev).reduceRatio;
			list.add(res);
			prev = res;
		};
		^list
	}
	
	ratioSum { ^this.product.reduceRatio }
	
	ratioDifference { var prev = [1,1];
		^this.collect{|x| x.ratioDiv(prev); prev = x}.sum.reduceRatio;
	}
	
	// must be an array of ratios. The collection of ratios will be converted as a group to harms.
	//	Ex. [[1,1],[16,15],[6,5],[4,3],[3,2],[8,5],[9,5],[2,1]].ratioToHarmonics ->
	//	[ 30, 32, 36, 40, 45, 48, 54, 60 ]		
	ratioToHarmonics { var numerator, denominator;
		if ( (this.size == 2) and: (this[0].isNumber) ) {
			numerator = this[0]; 
			denominator = this[1];
			^[numerator.lcm(denominator).div(denominator)]
		}{
			numerator = this.collect{|x| x[0]}; 
			denominator = this.collect{|x| x[1]};
			^(numerator * denominator.reduce(\lcm)).div(denominator);
		};
		
	}
	
	// must be an array of whole numbers
	// ex. [24,27,30,32,36,40,45,48].harmonicsToRatios ->
	//					 [[1,1],[9,8],[5,4],[4,3],[3,2],[5,3],[15,8],[2,1]]
/*	harmonicsToRatios	{ var ratios = this.collect{|x| [x,1]};
		^ratios.collect{|x| x.ratioDiv([this.minItem, 1])}
	}
*/
	
//	new version: make ratios with the smallest harmonic and reduce
	harmonicsToRatios { ^[this,this.minItem].flop.collect(_.reduceRatio) }
	
/*	subharmonicsToRatios {var ratios = this.collect{|x| [1,x]};
		^ratios.collect{|x| x.ratioDiv([1,this.maxItem])}
	}
*/
	subharmonicsToRatios { ^[this.maxItem, this].flop.collect(_.reduceRatio)}
	
	arithmeticMean { ^(this.sum / this.size) }
	
	harmonicMean { ^(this.size / this.reciprocal.sum) }
	
	geometricMean { ^( this.reduce('*')**(1/this.size)) }
	
	// integer means: 
	intArithmeticMean {
		var ratio = [this[0] * 2, this[1] * 2], 
		res = [ ratio[0], ratio.mean.asInteger, ratio[1] ].reverse.harmonicsToRatios;
		^res[1..].ratioDifferentiate
	}
	
	intHarmonicMean { ^this.intArithmeticMean.reverse }

// Novaro arithmetic progression	
	novaroSeries {|n = 1|var p,q;
		#p,q  = this;
		^Array.series(n+2, min(p,q) * (n+1), (p-q).abs)
	}
	
// Novaro fundamental scale. 'this' is a ratio [p,q]
	novaroF {|n = 1| ^this.novaroSeries(n).harmonicsToRatios }
	
// Novaro reciprocal scale
	novaroR {|n = 1| 
		^this.novaroF(n).collect{|x| 
			[x[1] * this[0], x[0] * this[1]].reduceRatio 
		}.ratioSort
	}
	
//Novaro gradual scale (with a cofundamental)
	novaroG{|n = 1, cofund = #[3,2]|
		^this.novaroF(n).collect{|x|
			[ x[1] * cofund[0], x[0] * cofund[1] ].reduceRatio
		}.ratioSort
	}
	
// Novaro complex scale
	novaroC{|n = 1, cofund = #[3,2]|
		^(this.novaroF(n) ++ this.novaroR(n)).removeDuplicates.ratioSort
	}
	
	novaroPosition {|pos = 2|
		^(this[(pos-1)..] 
		++ (pos -1).collect{|x| (this.last * this[x+1]).reduceRatio})
	}
	
	novaroPositionTransp {|pos = 2| var posicion, first;
		posicion = this.novaroPosition(pos);
		first = posicion.first;
		^posicion.collect{|x| x.ratioDiv(first)}
	}
	
	novaroChordPositions {
		^(this.size-1).collect{|x| this.novaroPositionTransp(x+1)}
	}
	
	// see Novaro, p. 43
	novaroChordSystem {|n = 1| var fund, recip;
		fund = this.novaroF(n).novaroChordPositions;
		recip = this.novaroR(n).novaroChordPositions;
		recip = [recip[0]] ++ recip[1..].reverse;
		^fund.collect{|x,i| [x,recip[i]]}.flatten(1);
	}		

				
//	several methods designed to work with arrays of pairs (either rationals or [', spl]):

	// phon values for [freq, spl] pairs: 
	asPhon {|calib = 0| var res;
		if ( (this.size == 2) and: (this[0].isSequenceableCollection.not))
			{
				res = this[0].asPhon(this[1], calib);
			}{
				res = this.collect{|x| x.asPhon(calib) }
			};
		^res
	}

	// just a convenience for not writing asPhon.phonToSone:
	asSone { |calib = 0| var res;
		if ( (this.size == 2) and: (this[0].isSequenceableCollection.not))
			{
				res = this[0].asSone(this[1], calib);
			}{
				res = this.collect{|x| x.asSone(calib) }
			};
		^res
	}
		
	// Convenience method from LoudnessModel. Returns the amplitudes of partials after masking, 
	// should be in the form of [freq, spl] pairs:
	compensateMasking { |gradient = 12| var f, res;
		f = this.flop;
		^LoudnessModel.compensateMasking(f[0], f[1], gradient);
	}		

	// cents value for rationals [p,q]:
	cents {
		if ( (this.size == 2) and: (this[0].isNumber) ) 
			{
				^this[0].cents(this[1]);
			}{
				^this.collect({|x| x.cents })
			};
	}
	
	// Barlow's harmonicity (see above for formula) for arrays of rational pairs [p,q]:
	harmonicity {|clean = true| var res; 
		if ( (this.size == 2) and: (this[0].isNumber) ) 
			{
				res = this[1].harmonicity(this[0]);
				if (clean) { if (res.isNaN) { res = 2 }}; // replace harmonicity of 1/1 with 2
												   // instead of nan
			}{
				res = this.collect({|x| x.harmonicity(clean) })
			};
		^res
	}	

	// James Tenney's harmonic distance (city-block metric of harmonic lattices)
	// for arrays of rational pairs [p,q] (see Tenney[1983]):

	harmonicDistance { 
		if ( (this.size == 2) and: (this[0].isNumber) ) {
				^(this[1] * this[0]).log2;
			}{
				^this.collect{|x| x.harmonicDistance }
			};
	}
	
	pitchDistance {
		if ( (this.size == 2) and: (this[0].isNumber) ) {
				^(max(this[0], this[1]) / min(this[0], this[1])).log2;
			}{
				^this.collect{|x| x.pitchDistance }
			};
	}
	
	// L. Euler's Gradus Suavitatis (1739, 'degree of sweetness'), arrays of rational pairs [p,q]:
	// G(a) = 1 + k1*(p1-1) + k2*(p2-1) +.... + kn * (pn-1)
	// where a = (p1^k1) *(p2^k2)*...*(pn^kn) and p1, p2, ... pn are its prime factors
	gradusSuavitatis { 
		if ( (this.size == 2) and: (this[0].isNumber) ) {
				^(this[0] * this[1]).factors.collect{|x| x - 1}.sum + 1
			}{
				^this.collect{|x| x.gradusSuavitatis }
			};
	}
	
	// returns the gradus suavitatis of a scale or chord
	gradusSuavitatisN { ^[this.ratioToHarmonics.reduce(\lcm).abs, 1].gradusSuavitatis}


	harmonicityBetween{}
	
// the following are methods for SimpleNumber made to work in SequenceableCollection:

	asNote {^this.collect({|x| x.asNote})}
	
	asMidi {^NoteNames.table.atAll(this) }

	addCents {|cents| ^this.collect({|x| x.addCents(cents)})}
	
	asBark {^this.collect({|x| x.asBark})}
	
	barkToFreq {^this.collect{|x| x.barkToFreq } }
	
	barkToHz {^this.collect{|x| x.barkToHz } }
	
	criticalBW {^this.collect{|x| x.criticalBW } }
	
	hzToErb {^this.collect{|x| x.hzToErb } }
	
	hzToMel {^this.collecy{|x| x.hzToMel} }
	
	melToHz {^this.collecy{|x| x.melToHz} }
	
	phonToSone {^this.collect{|x| x.phonToSone } }
	
	soneToPhon {^this.collect{|x| x.soneToPhon } }
	
	asDynamic{|freq = 1000, ref = 100, fff = 3| 
		if (freq.isSequenceableCollection) {
				^this.collect{|x, i| x.asDynamic(freq[i], ref, fff) }
		}{
			^this.collect{|x| x.asDynamic(freq, ref, fff) } 
		}
	}
	
	asRatio {|denom = 100, fasterBetter = false| 
		^this.collect{|x| x.asRatio(denom, fasterBetter)}
	}
	
	vpChord {|aprox = 1, prime = 7, max = 24| 
		^this.collect({|x| x.vpChord(aprox, prime, max)}).flatten
	}

	vpChordClosed {|aprox = 1, prime = 7, max = 24|
			^this.collect({|x| x.vpChordClosed(aprox, prime, max)}).flatten
	}

	vpChordOpen {|aprox = 1, prime = 7, max = 24|
			^this.collect({|x| x.vpChordOpen(aprox, prime, max)}).flatten
	}
	
	cents2Frq {|frq = 1| ^this.collect{|x| x.cents2Frq(frq)}  }
	
	centsToName {|tolerance = 12, restore = true|
		var g,table = IntervalTable.tableType, 
		min = IntervalTable.tableMin,
		change = false;
		if (table != \huygens) {IntervalTable.loadTable(\huygens); change = true};
		g = IntervalTable.classify(this, tolerance);
		this.do{|int, indx|
		  postf("The following intervals are close to % cents by +/- % cents:\n", int, tolerance);
		  g[indx].do{|x|
		  	postf("\t%/%,\t% cents,\t%\n",x[0][0],x[0][1],x[1].round(0.001),x[2]);
		  };
		  "".postln;
		 };
		if (restore) {
			if (change) {IntervalTable.loadTable(table, min)};
		}
	} 
	
	// falta que funcione para cents y ratios simples, no solo en arrays...
	ratioToName {|tolerance = 16, restore = true| ^this.cents.centsToName(tolerance, restore)}
	
	// a shortcut for making arrays of primes:	
	// ex. Array.primes(11) -> [2, 3, 5, 7, 11]
	*primes {|maxPrime = 11| var obj = this.newClear, i = 0;
		{ i < maxPrime}.while{ 
	     	i = i + 1; 
	     	if (i.isPrime) {obj = obj.add(i)}
     	};
		^obj
	}

		
	analyseScale {|tolerance = 16, type = \size, maxNum = 729, maxDenom = 512, maxPrime = 31, post = true|
		var classification, res, cents = this.cents;
		classification = IntervalTable.classify(cents, tolerance);
		type.switch(
			\size, { // reduce by size of num and denom
				res = classification.collect{|x,i|
					x.reject{|y| (y[0][1] > maxDenom) or: (y[0][0] > maxNum) };
				}
			},
			\prime, { // reduce by max prime factor
				res = classification.collect{|x,i|
						x.reject{|y| 
							var p = y[0][0].factorsHarmonic, q = y[0][1].factorsHarmonic;
/*							((p.maxItem.isNil) or: (q.maxItem.isNil)) or: {*/
								(p.maxItem > maxPrime) or: (q.maxItem > maxPrime)  
/*							}*/
						}
				}
			},
			{^"Invalid type!"}
		);			
		res = res.collect{|x, i| if( (x == []) or: (x.isNil) )
					{[[this[i], cents[i], "NO MATCH"]]} {x} };
		if (post) {
			postf("Each scale degree is close to the following intervals by +/-% cents.\n", tolerance);
			type.switch(\size, {postf("Max denominator: %, max numerator: %\n", maxDenom, maxNum)},
						\prime, {postf("Max prime: %\n", maxPrime)});
			res.do{|x, i|
				postf("%> % ( % cents)  ------------------------------\n", 
					i+1, this[i], cents[i].round(0.001) );
				x.do{|y| postf("\t\t\t\t %/%, % cents, %\n", y[0][0], y[0][1], y[1].round(0.001), y[2])};
				};
			^""	
		}
		^res;
	 }		
	 

// search for a way to favor things like 12/11 instead of 35/32 (answer: harmonicDistance)
	 rationalize {|tolerance = 16, metric, type = \size, max|
	 	var candidates, res, harms, maxNum, maxDenom, maxPrime;
	 	if (metric.class != HarmonicMetric) {metric = HarmonicMetric(metric)};
	 	type.switch(
	 		\size, {
		 		max = max ? [729, 512]; 
		 		maxNum = max[0]; 
		 		maxDenom = max[1]
		 	},
	 		\prime, {
		 		max = max ? 19; 
		 		maxPrime = max;
		 	}
	 	);
	 	candidates = this.analyseScale(tolerance, type, maxNum, maxDenom, maxPrime, false);
	 	res = candidates.collect{|x|
			if (x.size == 1) {	
				x[0][0]
			}{
				harms = x.collect{|y| y[0]};	
				metric.mostHarmonic(harms)
			};
		};
		^res;
	 }
	 
}

+ Collection {

	removeDuplicates { ^this.asSet.perform( ('as' ++ this.class).asSymbol ) }
	
	asFloatArray {^FloatArray.new(this.size).addAll(this) }
}

+ SimpleNumber {

// variations on Virtual Pitch chords
	vpChord {|aprox = 1, primeArray, maxMultiple = 3| 
		var t, new = Array.newClear;
		t = maxMultiple.vpNumbers(primeArray);
		new = new.add(t[0].choose);
		t.remove(t[0]);
		t.do{|x| 
			var temp; temp = x.choose;
			while {new.includes(temp)} {temp = x.choose};
			new = new.add(temp);
		};
		^(new.sort * this.midicps).cpsmidi.round(aprox);
	}
	
	vpChordClosed {|aprox = 1, primeArray, maxMultiple = 3|
		var t, new = Array.newClear;
		t = maxMultiple.vpNumbers(primeArray);
		new = new.add(t[0].choose);
		t.remove(t[0]);
		(t.size).do {|i|
			var temp; temp = new[i].nearestInList(t[i]);
			while {new.includes(temp)} {temp = t[i].choose};
			new = new.add(temp);
		};
		^(new.sort * this.midicps).cpsmidi.round(aprox);
	}
	
	vpChordOpen {|aprox = 1, primeArray, maxMultiple = 3|
		var t, new = Array.newClear;
		t = maxMultiple.vpNumbers(primeArray);
		new = new.add(t[0].choose);
		t.remove(t[0]);
		(t.size).do{|i|
			var temp; 
			temp = t[i].maxItem({|x| (x-new[i]).abs});
			while {new.includes(temp)} {temp = t[i].choose};
			new = new.add(temp);
		};
		^(new.sort * this.midicps).cpsmidi.round(aprox);
	}

}

// jsl: 2005-2008	
/*	
	TO DO: indispensibility for meters
*/
+ Integer {
	factorsHarmonic {
		var num, array, prime;
// the reason to hack this into a mathematically incorrect factorization is beacuse it 
// saves a ton of work dealing with prime filtering in harmonic ratios:
//		if(this <= 1) { ^[] }; // no prime factors exist below the first prime
		num = this.abs;
		// there are 6542 16 bit primes from 2 to 65521
		6542.do {|i|
			prime = i.nthPrime;
			while { (num mod: prime) == 0 }{
				array = array.add(prime);
				num = num div: prime;
				if (num == 1) {^array}
			};
			if (prime.squared > num) {
				array = array.add(num);
				^array
			};
		};
		// because Integer is 32 bit, and we have tested all 16 bit primes,
		// any remaining number must be a prime.
		array = array.add(num);
		^array
	}
}