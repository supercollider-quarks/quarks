// Used especially by miditest()
// but also by FxPatch, so these need to be in common

+ Instr {
	argsAndIndices {
		var out;	
		out = IdentityDictionary.new;
		func.def.argNames.do({ |name, i|
			out.put(name, i);
		});
		^out
	}

	listArgs { |inputs|
		var names = this.getWrappedArgs(inputs);
		("\n\n" ++ this.asString).postln;
		names.do({ arg assn;
			assn.key.post;
			" -> ".post;
			assn.value.asCompileString.postln;
		});
	}
	getWrappedArgs { |inputs|
		var	dummyPatch,	// Instrs that use Instr-wrap must be patched before revealing all args
			names, spc;
		try {	// this might fail, so set up a fallback position
			dummyPatch = this.patchClass.new(this, inputs);
			dummyPatch.asSynthDef;
			names = dummyPatch.argNames;
			spc = dummyPatch.argSpecs;
		} {		// on failure
			names = this.argNames;
			spc = specs;
		};
		^names.collect { |name, i| (name -> spc[i]) }
	}
}