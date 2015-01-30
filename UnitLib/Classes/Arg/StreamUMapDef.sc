StreamUMapDef : FuncUMapDef {
	
	var <>dict;
	
	// func should create stream
	initFunc { |inFunc, valueIsPrivate|
		func = inFunc;
		dict = IdentityDictionary();
		argSpecs = ArgSpec.fromFunc( func, argSpecs )[1..];
		argSpecs = argSpecs ++ [
			[ \value, 0, DisplaySpec(), valueIsPrivate ], 
			[ \reset, 1, TriggerSpec("reset") ], 
			[ \next, 1, TriggerSpec("next") ], 
			[ \id, nil, StringSpec(), false ],
			[ \u_spec, [0,1].asSpec, ControlSpecSpec(), true ],
			[ \u_prepared, false, BoolSpec(false), true ]
		].collect(_.asArgSpec);
		argSpecs.do(_.mode_( \init ));
		this.setSpecMode( \value, \nonsynth );
		this.setSpecMode( \id, \nonsynth );
		this.setSpecMode( \restart, \nonsynth );
		mappedArgs = [ \value ];
		allowedModes = [ \init, \sync, \normal ];
		this.changed( \init );
	}
	
	makeID {
		var string = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
		^12.collect({ string.choose }).join;
	}
	
	getStream { |id|
		if( id.notNil ) {
			^this.dict[ id.asSymbol ];
		} {
			^nil;
		};
	}
	
	setStream { |id, stream|
		if( id.notNil ) {
			this.dict[ id.asSymbol ] = stream;
		};
	}
		
	doFunc { |unit|
		var res, id, stream, range;
		id = unit.get( \id );
		if( id.size == 0 ) {
			id = this.makeID;
			unit.set( \id, id );
		};	
		stream = this.getStream( id );
		if( stream.isNil ) {
			stream = func.value( unit, 
				*this.asUnmappedArgsArray( unit, unit.args ).clump(2).flop[1]
			);
			this.setStream( id, stream );
		};
		res = stream.next;
		if( res.notNil ) {
			if( valueIsMapped ) {
				unit.setArg( \value, unit.getSpec( \value ).map( res ) );
			} {
				unit.setArg( \value, res );
			};
		}
	}
	
	setSynth { |unit ...keyValuePairs|
		keyValuePairs.clump(2).do({ |item|
			case { item[0] === \reset } {
				this.setStream( unit.get( \id ), nil );
			}  { item[0] === \next } {
				this.doFunc( unit );
				unit.unitSet;
			};
		});
	}
	
}