

TestInstrSynthDef : UnitTest {
	
	test_hashEncode {
		["-",0,1,-1,"hello",
			[\kr,1,"yello",IdentityDictionary.new],
			[\kr,1,"yello",IdentityDictionary.new],
			[\kr,1,"yellow",IdentityDictionary.new]
			
		].do { arg thing;
			// can't figure out what to test yet
			// just posting
			"".postln;
			thing.hash.debug(thing);
			InstrSynthDef.hashEncode(thing).debug(thing)
		}
	}
	
}


