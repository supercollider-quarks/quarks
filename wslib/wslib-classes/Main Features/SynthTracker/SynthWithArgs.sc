// wslib 2007

// used by SynthTracker
// keeps args of a synth with the instance

SynthWithArgs : Synth {
	var <>args;
	
	*newKeepArgs {  arg defName, args, target, addAction=\addToHead; 
		var synth;
		synth = SynthWithArgs.new( defName, args, target, addAction);
		synth.args = args.asArgsDict;
		^synth;
		}
	}

		