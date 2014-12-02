	// some additional methods required for MixerChannel/MixingBoard

+ Dictionary {		// add a couple of methods to Dictionary
	postSorted {	// post the dictionary in sorted order
		var sorted;
		sorted = this.asSortedArray;
		sorted.do({ arg pair;
			(pair.at(0).asString ++ "\t" ++ pair.at(1)).postln;
		});
			// for command-line use
			// in inline code, this output should be discarded
		^(sorted.size.asString ++ " entries.")
	}
}
