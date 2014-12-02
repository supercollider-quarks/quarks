// Andrea Valle jan 2010 
// just adding a storage utility to build 
+ SynthDef {

	build { arg ugenGraphFunc, rates, prependArgs;
		protect {
			this.initBuild;
			this.buildUgenGraph(ugenGraphFunc, rates, prependArgs);
			this.finishBuild;
		} {
			UGen.buildSynthDef = nil;
		} ;
		// Just adding this
		SynthDefStorage.synthDefDict.add(name->[this, ugenGraphFunc.asCompileString]) ;
	}
}