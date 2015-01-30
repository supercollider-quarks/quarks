EnvM : Env {
	// a mapped Env. level values are entered raw, but stored internally unmapped
	// note: when entering a literal array (i.e. #[0,1,0]) it will not be unmapped
	
	var <spec;
	var <>mapped = true;
	
	*new { arg levels = #[0,1,0], times = #[1,1], curve = 'lin', releaseNode, loopNode;
		^super.newCopyArgs(levels, times, curve)
		.releaseNode_(releaseNode).loopNode_(loopNode).offset_(0).mapped_(levels.mutable);
	}
	
	offset_ { |value = 0|
		if( this.class.instVarNames.includes( \offset ) ) {
			super.offset_( value );
		};
	}
	
	unmappedLevels { ^levels }
	unmappedLevels_ { |newLevels| levels = newLevels; mapped = false; array = nil; }
	
	levels { ^if( spec.notNil ) { spec.map( levels ) } { levels } }
	levels_ { |newLevels|
		if( spec.notNil && newLevels.mutable ) { 
			levels = spec.unmap( newLevels ) 
		} { 
			levels = newLevels;
			mapped = newLevels.mutable;
		};
		array = nil; 
	}
	
	storeArgs { ^[this.levels, times, curves, releaseNode, loopNode] }
	
	spec_ { |newSpec|
		if( newSpec.notNil ) {
			if( spec.notNil ) {
				spec = newSpec.asSpec;
			} {
				spec = newSpec.asSpec;
				if( mapped ) { 
					levels = spec.unmap( levels );
					mapped = false;
					this.changed( \levels );
				};
			};
		} {
			if( spec.notNil ) {
				levels = spec.map( levels );
				this.changed( \levels );
			};
			spec = nil;
		};
	}
	
	asUnitArg { |unit, key|
		if( key.notNil ) {
			if( unit.getSpec( key ).isKindOf( UEnvSpec ) ) {
				if( unit.isUMap && { unit.def.isMappedArg( key ) } ) {
					if( unit.spec.notNil ) {
						this.spec = unit.getSpec( key ).asControlSpec.copy;
					};
				} {
					this.spec = unit.getSpec( key ).asControlSpec.copy;
				};
			} {
				^UMap( 'envelope', [ \env, this ] ).asUnitArg( unit, key );
			};
		};
		^this;
	}
	
}

+ Env {
	unmappedLevels { ^levels }
	unmappedLevels_ { |newLevels| this.levels = newLevels; }
}