HarmonicVector{
	classvar base;
	var <>vector, <>ratio, <>reduced, <>reducedRatio, <>functions, <>pow2; 
	const simbols = #["", \D, \M, \S, \s, \m, \d];
	
	*initClass { 
			base = Array.primes(43); // (way beyond human harmonic perception)
	}

	*new { ^super.new.init }
	
	init { 
		vector = ([0]).asVector;
		ratio = [1,1]; 
		reduced = ([0]).asVector;
		reducedRatio = [1,1];
	}
	
	printOn {|stream|
		stream << this.class.name <<"( " << 
		this.vector.as(Array) << ", " << 
		this.ratio.ratioPost << ", " << 
		this.reduced.as(Array)  << ", " << 
		this.reducedRatio.ratioPost << ", " <<
		this.cents.round(0.01) << "¢";
		if (this.functions != "") {
			stream << ", " << this.functions << ")\n"
		}{ stream << ")"}
	}
	
	*with {|reduced| var hv = this.new; 
		hv.reduced = reduced;
		hv.complete;  
		^hv.getFunctions;
	}
	
	*from {|ratio| var hv = this.new;
		hv.ratio = ratio.reduceRatio;
		hv.toVector;
		hv.reduced = hv.vector[1..];
		hv.adjustOctave;
		^hv.getFunctions;
	}
	
	// harmonic measures: 
	magnitude { ^this.vector.norm }
	harmonicDistance {^this.ratio.harmonicDistance}
	harmonicity {^this.ratio.harmonicity}
	gradusSuavitatis {^this.ratio.gradusSuavitatis}
			
	// complete a reduced octave vector by calculating the number of octaves it contains
	complete { 
		var denom = 1, num = 1, series;
		series = this.reduced.collect{|x,i| base[i+1]**x.abs * x.sign};
		series.do{|x|
			if (x != 0) {
				if (x.isNegative) {denom = denom * x.abs}{num = num * x}
			}
		};
		this.ratio = [num.asInteger, denom.asInteger];
		this.adjustOctave;		
		^this.vector = RealVector.newFrom([this.pow2] ++ this.reduced);
	}

	// convert from vector to ratio representation
	toRatio {var num = 1, denom = 1, series;
		series = this.vector.collect{|x,i| base[i]**x.abs * x.sign};
		series.do{|x|
			if (x != 0) {
				if (x.isNegative) {denom = denom * x.abs} {num = num * x}
			}
		};
		^this.ratio = [num, denom]
	}

	// convert from ratio to vector
	toVector {var res, maxP, p = this.ratio[0].factors, q = this.ratio[1].factors, pow2;
		
		// mathematical correction since 1.factors changed to [] instead of [1]:
		if (p.isEmpty) {p = [1]};
		if (q.isEmpty) {q = [1]};
		
		maxP = max(p.maxItem, q.maxItem);
		if ((maxP == 1) || (maxP.even)) { // 1/1 and its (sub)octaves
			if (maxP == 1) { 
				^this.vector = RealVector.newFrom([0, 0]) 
				
			}{ 
				if (q.includes(2)) { pow2 = q.size.neg }{ pow2 = p.size }; 
				^this.vector = RealVector.newFrom([pow2, 0]) 
			};
		}{	
			res = RealVector.newFrom({0} ! (maxP.indexOfPrime + 1));
		};
		p.collect{|x| 
			var idx = base.indexOf(x);
			if (idx.notNil) // in case there is a 1
				{res[idx] = res[idx] + 1}; 
		};
		q.collect{|x| 
			var idx = base.indexOf(x); 
			if (idx.notNil) // in case there is a 1
				{res[idx] = res[idx] - 1}; 
		};
		^this.vector = res;
	}
	
	adjustOctave {var fratio = this.ratio[0]/this.ratio[1], pow2 = 0, res = this.ratio.copy;
		{ fratio < 1 }.while{ fratio = fratio * 2; pow2 = pow2 + 1 };
		{ fratio >= 2 }.while{ fratio = fratio / 2; pow2 = pow2 - 1 };
		if (pow2.isNegative) { 
			res[1] = (res[1] * ( 2**pow2.abs )).asInteger
		}{
			res[0] = (res[0] * ( 2**pow2 )).asInteger
		}; 
		this.pow2 = pow2; // useful in method 'complete'
		^this.reducedRatio = res.reduceRatio; 
	}
	
	// prepare for arithmetic operations by making the dimension of the two vectors the same
	makeOperands {|tis, tat| var n1 = tis, n2 = tat, dif = tis.size - tat.size; 
		if (dif == 0) {^[n1, n2]}; 
		if (dif.isNegative) { 
			n1 = RealVector.newFrom(tis ++ ({0}!dif.abs))
		}{  
			n2 = RealVector.newFrom(tat ++ ({0}!dif))
		};
		^[n1,n2]
	}
	
	+ {|that| var tis, tat;  			
		#tis, tat = this.makeOperands(this.reduced, that.reduced); 
		^HarmonicVector.with(tis + tat)
	}

	- {|that| var tis, tat; 
		#tis, tat = this.makeOperands(this.reduced, that.reduced); 
		^HarmonicVector.with(tis - tat)
	}

	* {|that| var tis, tat; 
		#tis, tat = this.makeOperands(this.reduced, that.reduced);  
		^HarmonicVector.with(tis * tat)
	}

	/ {|that| var tis, tat; 
		#tis, tat = this.makeOperands(this.reduced, that.reduced); 
		if (tat.indexOfEqual(0).notNil) { "Division by Zero".throw}; // prevent crash
		^HarmonicVector.with(tis / tat)
	}
	
	** {|scalar| if (scalar.isNumber) {
			^HarmonicVector.with(this.reduced * scalar) 
		}{
			"Only scalars allowed".throw;// An hvector can only be raised to the power of a scalar
		}
	}
	
	cents{|reduce = true| if (reduce) {^this.reducedRatio.cents} {^this.ratio.cents} }
	
	
	// katapyknosis, implemented from John Chalmers' "Divisions of the Tetrachord" chapter 2
	katapykne{|n = 1, reduce = false| 
		if (reduce) {
			^this.reducedRatio.katapykne(n)
		}{
			^this.ratio.katapykne(n)
		}
	}
	
	katapykne_ab{|a, b, reduce = false| 
		if (reduce) {
			^this.reducedRatio.katapykne_ab(a,b)
		}{
			^this.ratio.katapykne(a,b)
		}
	}
	
	getFunctions{ if (this.reduced[0..2] == [0,0,0]) {
				^this.functions = \T;
			}{ ^this.functions = this.reduced[0..2].collect{|x,i| var r = "";
					if (x.abs > 1) {r = x.abs.asSymbol};
					r++simbols.wrapAt((i+1) * x.sign)
				}.flat.join
			}
	}
	
	// TODO an inverse of getFunctions: from a function to an Hvector
	
	
	// Returns a boolean answering whether a vector lies inside a periodicity block 
	// (as defined in the unisonmatrix). It is used in PitchSet to separate into harmonic
	// and timbral subsets
	isInIsland{ |unisonmatrix| 	var	max, min, truth = true;
		unisonmatrix = unisonmatrix ? #[ [4, 2, 0], [4, -3, 2], [2, 2, -1] ];
		if (this.reduced.size > unisonmatrix[0].size) {
			truth = false
		}{			
			max = unisonmatrix.collect{|x| max(0,x)};
			min = unisonmatrix.collect{|x| min(x,0)};
			max = max.flop.collect{|x| x.maxItem};
			min = min.flop.collect{|x| x.minItem};
			this.reduced.as(Array).collect{|v,i| 
				v.inclusivelyBetween(min[i], max[i])
			}.do{|x| 
				if (x != true) {truth = false}
			};
//			[max,min,truth].postln;
		};
		^truth
	}
	
	play {|ref = 440, octavereduced = false, withunison = false, melodic = false, dur = 5| 
		var note, strum, freq;
		if (octavereduced) {note = this.reducedRatio} {note = this.ratio};
		if (withunison) { note = [note, [1,1]] };
		strum = if (melodic) {dur / 2 }Ê{ 0 };
		freq = note.ratioToFreq * ref; 
		[freq, freq.asNote, note].postln; 
		^Pbind(\freq, freq, \dur, 5, \strum, strum).play;
	}


}



+ Array {

	asHvector{ ^this.collect{|x| HarmonicVector.from(x) } }
	
	ratioToVector{ ^HarmonicVector.from(this) }
	
	toHvector{ ^this.collect{|x| HarmonicVector.with(x) } }
	
	harmonicVproduct {var product = HarmonicVector.with([1,1,1]);
		this.do{|elem| product = product * elem }
		^product
	}
	
//	ratio {^Ratio(this) }
	
	asVector {
		^RealVector.newFrom(this) 
	}

}

+ RealVector {
	
		*newFrom {|array|  ^super.new(array.size).addAll(array)}
}



/*
cc 2008 jsl
*/