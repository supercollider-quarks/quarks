// Pseudo Ugens to be used together with Vowel
// 2010 - 2012, Florian Grond and Till Bovermann, 
//
// Implementation is supported by:
// + Ambient Intelligence Group, CITEC, Bielefeld University
//		http://www.techfak.uni-bielefeld.de/ags/ami  
// + MediaLab Helsinki, Department of Media, Aalto University, 
//		http://tai-studio.org
//
// thanks go to Alberto deCampop and and Julian Rohrhuber


// Changelog: see below



Formants {
	*ar1 {|baseFreq = 100, vowel, freqMods = 1, ampMods = 1, widthMods = 1, unfold = false|
		var freqs, dBs, amps, widths;
		var out; 
		
		#freqs, dBs, widths = vowel.asArray;
	
		freqs = freqs * freqMods;
		amps = dBs.dbamp * ampMods;
		widths = widths * widthMods;
	
		out = [freqs, widths, amps].flop.collect{ |args| 
			Formant.ar(baseFreq, *args); 
		}.flop;
		
		// don't mix if unfold is true
		^(unfold.if({ out; },{ out.collect(_.sum); })).unbubble; 
		// unbubble for correct channel expansion behaviour, if only one baseFreq is given
	}
	
	*ar {|baseFreq = 100, vowel, freqMods = 1, ampMods = 1, widthMods = 1, unfold = false|
		^[baseFreq, vowel, [freqMods], [ampMods], [widthMods]].flop.collect({ |inputs|
			this.ar1(*(inputs ++ unfold));
		}).unbubble;		
	}
}



BPFStack {
	*ar1 {|in, vowel, freqMods = 1, ampMods = 1, widthMods = 1, unfold = false|
		var freqs, dBs, amps, widths;
		var out; 
		
		#freqs, dBs, widths = vowel.asArray;
	
		freqs = freqs * freqMods;
		amps = dBs.dbamp * ampMods;
		widths = (widths * widthMods).reciprocal;
	
		out = [freqs, widths, amps].flop.collect{ |args| 
			BPF.ar(in, *args); 
		}.flop;
		
		// don't mix if unfold is true
		^(unfold.if({ out; },{ out.collect(_.sum); })).unbubble; 
		// unbubble for correct channel expansion behaviour, 
		// if only one baseFreq is given
	}
	
	*ar {|in, vowel, freqMods = 1, ampMods = 1, widthMods = 1, unfold = false|
		^[in, vowel, [freqMods], [ampMods], [widthMods]].flop.collect({ |inputs|
			this.ar1(*(inputs ++ unfold));
		}).unbubble;		
	}
}
