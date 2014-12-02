+ Synth {
	*newKeepArgs { arg defName, args, target, addAction=\addToHead; 
		^SynthWithArgs.newKeepArgs( defName, args, target, addAction );
		}
		
	args { ^nil }
	args_ { "Synth should be SynthWithArgs to store args".warn }
		
	}