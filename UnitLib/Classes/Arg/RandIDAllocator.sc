RandIDAllocator {
	classvar <>dict;
	
	*value { |server|
		var count;
		server = server ? Server.default;
		dict = dict ?? { IdentityDictionary() };
		count = dict[ server ] ? -1;
		count = (count + 1).wrap(0, server.options.numRGens);
		dict[ server ] = count;
		^count;
	}
	
	*asControlInput { ^this.value }
	
	*asControlInputFor { |server| ^this.value( server ) }
	
	*reset { |server|
		if( server.notNil ) {
			dict[ server ] = nil;
		} {
			dict.clear; // remove all
		};
	}
	
	// double as Spec
	*new { ^this } // only use as class
	
	*asSpec { ^this }
	
	*constrain { ^this } // whatever comes in; UGlobalEQ comes out
	
	*default { ^this }
	
	*massEditSpec { ^nil }
	
	*findKey {
		^Spec.specs.findKeyForValue(this);
	}

}

URandSeed {
	
	*getRandID {
		var id = \u_randID.ir( 0 );
		Udef.addBuildSpec( ArgSpec( \u_randID, RandIDAllocator, RandIDAllocator, true, \init ) );
		RandID.ir( id );
	}
	
	*ir { |seed = 12345|
		this.getRandID;
		RandSeed.ir( 1, seed );
	}
	
	*kr { |trig = 1, seed = 12345|
		this.getRandID;
		RandSeed.kr( trig, seed );
	}
}