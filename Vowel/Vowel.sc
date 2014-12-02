// Vowel, a convenience class
// 2010 - 2012, Florian Grond and Till Bovermann, 
//
// Implementation is supported by:
// + Ambient Intelligence Group, CITEC, Bielefeld University
//		http://www.techfak.uni-bielefeld.de/ags/ami  
// + MediaLab Helsinki, Department of Media, Aalto University, 
//		http://tai-studio.org
//
// thanks go to Alberto deCampop and and Julian Rohrhuber
//"/Users/tboverma/Library/Application Support/SuperCollider/Extensions/quarks/Vowel/Vowel.sc"
// the used formant data is taken from the Csound manual:
// http://ecmc.rochester.edu/onlinedocs/Csound/Appendices/table3.html
// vowel, register, formant frequencies, formant dB, formant width

// Changelog: see below


Vowel {
	classvar formLib;
	var <>freqs, <>dBs, <>widths;

	*initLib{
	// this method is evaluated the first time formLib is used
		formLib = Library.new;
		formLib	
			.put( 'a', 'soprano', 'freq',[ 800, 1150, 2900, 3900, 4950 ])
			.put( 'a', 'soprano', 'db', [ 0, -6, -32, -20, -50 ])
			.put( 'a', 'soprano', 'bw',	 [ 80, 90, 120, 130, 140 ])
	
			.put( 'e', 'soprano', 'freq',[ 350, 2000, 2800, 3600, 4950 ])
			.put( 'e', 'soprano', 'db', [ 0, -20, -15, -40, -56 ])
			.put( 'e', 'soprano', 'bw',	 [ 60, 100, 120, 150, 200 ])
	
			.put( 'i', 'soprano', 'freq',[270, 2140, 2950, 3900, 4950])
			.put( 'i', 'soprano', 'db', [0, -12, -26, -26, -44])
			.put( 'i', 'soprano', 'bw',	 [60, 90, 100, 120, 120])
	
			.put( 'o', 'soprano', 'freq',[450, 800, 2830, 3800, 4950])
			.put( 'o', 'soprano', 'db', [0, -11, -22, -22, -50])
			.put( 'o', 'soprano', 'bw',	 [70, 80, 100, 130, 135])
	
			.put( 'u', 'soprano', 'freq',[325, 700, 2700, 3800, 4950])
			.put( 'u', 'soprano', 'db', [0, -16, -35, -40, -60])
			.put( 'u', 'soprano', 'bw',	 [50, 60, 170, 180, 200])
	
	
			.put( 'a', 'alto', 'freq',	[800, 1150, 2800, 3500, 4950])
			.put( 'a', 'alto', 'db',	[0, -4, -20, -36, -60])
			.put( 'a', 'alto', 'bw',	[80, 90, 120, 130, 140])
	
			.put( 'e', 'alto', 'freq',	[400, 1600, 2700, 3300, 4950])
			.put( 'e', 'alto', 'db',	[0, -24, -30, -35, -60])
			.put( 'e', 'alto', 'bw',	[60, 80, 120, 150, 200])
	
			.put( 'i', 'alto', 'freq',	[350, 1700, 2700, 3700, 4950])
			.put( 'i', 'alto', 'db',	[0, -20, -30, -36, -60])
			.put( 'i', 'alto', 'bw',	[50, 100, 120, 150, 200])
	
			.put( 'o', 'alto', 'freq',	[450, 800, 2830, 3500, 4950])
			.put( 'o', 'alto', 'db',	[0, -9, -16, -28, -55])
			.put( 'o', 'alto', 'bw',	[70, 80, 100, 130, 135])
	
			.put( 'u', 'alto', 'freq',	[325, 700, 2530, 3500, 4950])
			.put( 'u', 'alto', 'db',	[0, -12, -30, -40, -64])
			.put( 'u', 'alto', 'bw',	[50, 60, 170, 180, 200])
	
	
			.put( 'a', 'tenor', 'freq',	[650, 1080, 2650, 2900, 3250])
			.put( 'a', 'tenor', 'db',	[0, -6, -7, -8, -22])
			.put( 'a', 'tenor', 'bw',	[80, 90, 120, 130, 140])
	
			.put( 'e', 'tenor', 'freq',	[400, 1700, 2600, 3200, 3580])
			.put( 'e', 'tenor', 'db',	[0, -14, -12, -14, -20])
			.put( 'e', 'tenor', 'bw',	[70, 80, 100, 120, 120])
	
			.put( 'i', 'tenor', 'freq',	[290, 1870, 2800, 3250, 3540])
			.put( 'i', 'tenor', 'db',	[0, -15, -18, -20, -30])
			.put( 'i', 'tenor', 'bw',	[40, 90, 100, 120, 120])
	
			.put( 'o', 'tenor', 'freq',	[400, 800, 2600, 2800, 3000])
			.put( 'o', 'tenor', 'db',	[0, -10, -12, -12, -26])
			.put( 'o', 'tenor', 'bw',	[40, 80, 100, 120, 120])
	
			.put( 'u', 'tenor', 'freq',	[350, 600, 2700, 2900, 3300])
			.put( 'u', 'tenor', 'db',	[0, -20, -17, -14, -26])
			.put( 'u', 'tenor', 'bw',	[40, 60, 100, 120, 120])
		
	
			.put( 'a', 'bass', 'freq',	[600, 1040, 2250, 2450, 2750])
			.put( 'a', 'bass', 'db',	[0, -7, -9, -9, -20])
			.put( 'a', 'bass', 'bw',	[60, 70, 110, 120, 130])
	
			.put( 'e', 'bass', 'freq',	[400, 1620, 2400, 2800, 3100])
			.put( 'e', 'bass', 'db',	[0, -12, -9, -12, -18])
			.put( 'e', 'bass', 'bw',	[40, 80, 100, 120, 120])
	
			.put( 'i', 'bass', 'freq',	[250, 1750, 2600, 3050, 3340])
			.put( 'i', 'bass', 'db',	[0, -30, -16, -22, -28])
			.put( 'i', 'bass', 'bw',	[60, 90, 100, 120, 120])
	
			.put( 'o', 'bass', 'freq',	[400, 750, 2400, 2600, 2900])
			.put( 'o', 'bass', 'db',	[0, -11, -21, -20, -40])
			.put( 'o', 'bass', 'bw',	[40, 80, 100, 120, 120])
	
			.put( 'u', 'bass', 'freq',	[350, 600, 2400, 2675, 2950])
			.put( 'u', 'bass', 'db',	[0, -20, -32, -28, -36])
			.put( 'u', 'bass', 'bw',	[40, 80, 100, 120, 120])
	
	
			.put( 'a', 'counterTenor', 'freq',	[660, 1120, 2750, 3000, 3350])
			.put( 'a', 'counterTenor', 'db',		[0, -6, -23, -24, -38])
			.put( 'a', 'counterTenor', 'bw',		[80, 90, 120, 130, 140])
	
			.put( 'e', 'counterTenor', 'freq',	[440, 1800, 2700, 3000, 3300])
			.put( 'e', 'counterTenor', 'db',		[0, -14, -18, -20, -20])
			.put( 'e', 'counterTenor', 'bw',		[70, 80, 100, 120, 120])
	
			.put( 'i', 'counterTenor', 'freq',	[270, 1850, 2900, 3350, 3590])
			.put( 'i', 'counterTenor', 'db',		[0, -24, -24, -36, -36])
			.put( 'i', 'counterTenor', 'bw',		[40, 90, 100, 120, 120])
	
			.put( 'o', 'counterTenor', 'freq',	[430, 820, 2700, 3000, 3300])
			.put( 'o', 'counterTenor', 'db',		[0, -10, -26, -22, -34])
			.put( 'o', 'counterTenor', 'bw',		[40, 80, 100, 120, 120])
	
			.put( 'u', 'counterTenor', 'freq',	[370, 630, 2750, 3000, 3400])
			.put( 'u', 'counterTenor', 'db',		[0, -20, -23, -30, -34])
			.put( 'u', 'counterTenor', 'bw',		[40, 60, 100, 120, 120]);
	}
	
	save { |path = nil, timbre = \mytimbre, register = \myregister|
		var file;
		file = File.new(path, "a+");
		
		file.write("Vowel.formLib.put("++" \\"++timbre++","++" \\"++register++", \\freq ,"++this.freqs.asString++" );\n");
		file.write("Vowel.formLib.put("++" \\"++timbre++","++" \\"++register++", \\db ,"++this.dBs.asString++" );\n");
		file.write("Vowel.formLib.put("++" \\"++timbre++","++" \\"++register++", \\bw ,"++this.widths.asString++" );\n");
		file.write("\"loaded "++timbre++", "++register++", saved on"+Date.localtime.format("%Yy%mm%dd%Hh%Mm%Ss").asString+"\".postln;\n");
		file.write("\n\n");
		
		file.close;		
	}

	*load {|path|
	var file;
	file = File.open(path, "r");
	file.readAllString.interpret;
	file.close;
	}

	*formLib {
		formLib.isNil.if{this.initLib};
		^formLib;	
	}

	*new1 {|vowel = \a, register= \bass|
		var data;
		
		// is there a formlib already? otherwise create it
		formLib.isNil.if{this.initLib};

		data = Vowel.formLib[vowel, register];
		data = [\freq, \db, \bw].collect{|id| data[id]};
		^this.basicNew(*data)
	}

	*new {|vowel = \a, register= \bass|
		^[vowel, register].flop.collect{|args|
			this.new1(*args);
		}.unbubble
	}

	*basicNew {|freqs, dBs, widths|
		^super.new.init(freqs, dBs, widths)	
	}

	init {|argFreqs, argDBs, argWidths|
		freqs = argFreqs.copy;
		dBs = argDBs.copy;
		widths = argWidths.copy;
	}
	
	midinotes {
		^this.freqs.cpsmidi
	}
	
	amps {
		^this.dBs.dbamp
	}
	
	rqs {
		^this.widths.reciprocal
	}
	
	addFormants { |freq = 0, db = 1, width = -100|
		[freq,db,width].flop.collect({|form| this.freqs.add(form[0]); this.dBs.add(form[1]); this.widths.add(form[2]);})
	}
	
	removeFormants { |index|
	index.reverseDo({|item,i|  this.freqs.removeAt(item); this.dBs.removeAt(item); this.widths.removeAt(item); })
	}	
		
	ampAt {|freq, filterOrder = 1|
		// var half = 0.5.ampdb;
		var half = -6.0205999132796;

		^[this.freqs,this.dBs,this.widths, filterOrder].flop.collect({|form|  
			( 
				form[1] 
				+ ( (( (freq - form[0]).abs / (form[2] * 0.5) ).pow(form[3]) ) * half
				) 
			).dbamp }).sum
	}
	
	
	plot {|fmin = 0, fmax = 6000, fstep = 2, order = 1|
	var range = {|i| (i*fstep) + fmin}!((fmax - fmin) / fstep);
	
	^this.ampAt(range, order)
		.ampdb
		.plot("dB (-100 to 0) / frequency (% to % Hz)".format(fmin, fmax), minval: -100, maxval: 0)
	}
	
	asArray { 
		^[freqs, dBs, widths]
	}
	
	asEvent {
		^(
			freqs: freqs,
			dBs: dBs,
			widths: widths
		)	
	}
	
	asKeyValuePairs {|id = ""|
		^this.asEvent.asKeyValuePairs.clump(2).collect{|pair|
			[(pair[0] ++ id).asSymbol,pair[1]]	
		}.flatten
	}
	
	asFlatArray { 
"Vowel:asFlatArray is deprecated.".inform;
		^this.asArray.flat
	}

	addControls {|id = "", rate = \kr, lag|
		var pairs, result;
		 
		result = this.class.basicNew;
		lag = lag.asArray;

		pairs = this.asKeyValuePairs.clump(2);
		pairs.collect{|pair, i|
			//pair.postln;
			result.perform(pair[0].asSetter, (pair[0] ++ id).asSymbol.perform(rate, pair[1], lag.wrapAt(i)));
		};
		
		^result
	}

	// math support
	+ { arg that, adverb; ^that.performBinaryOpOnVowel('+', this, adverb) }
	- { arg that, adverb; ^that.performBinaryOpOnVowel('-', this, adverb) }
	* { arg that, adverb; ^that.performBinaryOpOnVowel('*', this, adverb) }
	/ { arg that, adverb; ^that.performBinaryOpOnVowel('/', this, adverb) }


	performBinaryOpOnVowel {|aSelector, aVowel|
		var that = this.class.new;
		
		that.freqs 	= this.freqs		.perform(aSelector, that.freqs);
		that.dBs 		= this.dBs	 	.perform(aSelector, that.dBs);
		that.widths 	= this.widths		.perform(aSelector, that.widths);
		^that		
	}

	performBinaryOpOnSimpleNumber { arg aSelector, aNumber; 
		^this.class.basicNew(*this.asArray.collect{ arg item; 
			aNumber.perform(aSelector, item)
		}) 
	}
	
	
	// blendFraq should be either a number, or an array [freqFrac, dbFrac, widthFrac]
	blend{|that, blendFrac=0.5| 
		var fracs = blendFrac.asArray; // make sure it's an array;
		
		//  use wrapAt to access correct frac-value
		^this.class.basicNew(
			blend(this.midinotes,  that.midinotes, fracs.wrapAt(0)).midicps,
			blend(this.dBs,    that.dBs, fracs.wrapAt(1)),
			blend(this.widths, that.widths, fracs.wrapAt(2))
		)
	}

	brightenRel {|bright = 1, refFormant = 0|
		var refFormat_dB = this.dBs[refFormant];
		this.dBs =  (this.dBs * bright);
		this.dBs = this.dBs - (this.dBs[refFormant] - refFormat_dB);
		^this
	}
		
	brightenLin {|bright = 1, refFormant = 0|
		var refFormat_dB = this.dBs[refFormant];
		this.dBs =  this.dBs + (bright * this.freqs.log);
		this.dBs = this.dBs - (this.dBs[refFormant] - refFormat_dB);
		^this
	}
	
	brightenExp {|bright = 1|
		var ampssum = (this.amps).sum;
		this.dBs =  ( this.amps).pow(bright) ;
		this.dBs =  (this.dBs * (ampssum / this.dBs.sum)).ampdb;
		^this
	}

	*compose {|vowelNames, registers, weights|
		var all;
		var vowel, vowelName, register;
		var midinotes = 0, dBs = 0, widths = 0;
		
		weights = weights.asArray;
		
		// create individual vowels convert freqs to midinotes
		all = [vowelNames, registers].flop.collect{|args, i|
			# vowelName, register = args;
			vowel = Vowel(vowelName, register);

			midinotes = midinotes + (vowel.midinotes * weights.wrapAt(i));
			dBs = dBs + (vowel.dBs * weights.wrapAt(i));
			widths = widths + (vowel.widths * weights.wrapAt(i));		};
		
		^Vowel.basicNew(
			midinotes.midicps, 
			dBs,
			widths
		);	
	}
		
	asKlankRef {|baseFreq = 440, numPartials = 13, ring =  1|
		var pList, gList;
				
	"Vowel:asKlankRef : deprecated, please use ampAt or dbAt to construct your own KlankRef Argument".inform;
				
		pList = {|i| baseFreq * (i+1)    }!numPartials;
		gList = freqs.collect{|freq, i| 
			((pList - freq).abs * (-1 / widths[i])).exp 
				* this.dBs[i].dbamp
		}.sum;

		^(`([pList, gList, gList * [ring]]))
	}


	printOn { arg stream;
		var title;
		stream << this.class.name << ".basicNew";
		this.storeParamsOn(stream);
	}
	
	storeArgs { ^this.asArray }
}


+ Object {
	
	performBinaryOpOnVowel{|aSelector, aVowel|

		^aVowel.class.basicNew(*aVowel.asArray.collect({ arg item; 
			this.perform(aSelector, item)
		})) 
}
	
}




// Changelog

// Till please look at compose, did not get it to work with midinotes for linear combination


// changes to Vowel
// 02.03.2012 tb: adoption of the helpfile to the new schelp system
// 02.03.2012 tb: removed commented code that stayed in the file for too long already.
// 18.02.2010 fg: added method save and load
// 18.02.2010 fg: added method addFormant and removeFormant
// 18.02.2010 fg: added method midinote rqs amps
// 18.02.2010 fg: added method plot
// 18.02.2010 fg: added method ampAt
// 18.02.2010 fg: added method brightenCAmpSum
// 06.2.2010 tb: added asEvent
// 06.2.2010 tb: moved initialization of formLib from iniClass to the actual place where it is used first
// 06.2.2010 tb: added channel expansion, so you now can write Vowel([\a, \e], [\bass, \soprano])

// changes to Formants
// 6.2.2010 tb: added a new implementation that should be preferred to Formants:
//	+ got rid of explicit use of freqs/amps/widths in Formants as it is not really used
//	+ added channel expansion
//	+ added unfold to change between summing or not summing the formants together...
//	+ added mods parameter for freqs/widths/amps
