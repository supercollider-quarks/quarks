FuncUMapDef : UMapDef {
	
	// example function for random value
	/*
	{ |unit, range = #[0.0,1.0]|
		range[0] rrand: range[1]; // result sets \value arg
	} 
	*/
	
	var <>valueIsMapped = true;
	var <>dontStoreValue = false;
	
	*new { |name, func, args, valueIsPrivate = false, category, addToAll=true|
		^this.basicNew( name, args ? [], addToAll )
			.initFunc( func, valueIsPrivate ).category_( category ? \default ); 
	}
	
	initFunc { |inFunc, valueIsPrivate|
		func = inFunc;
		argSpecs = ArgSpec.fromFunc( func, argSpecs )[1..];
		argSpecs = argSpecs ++ [
			[ \value, 0, DisplaySpec(), valueIsPrivate ], 
			[ \u_spec, [0,1].asSpec, ControlSpecSpec(), true ],
			[ \u_prepared, false, BoolSpec(false), true ]
		].collect(_.asArgSpec);
		argSpecs.do(_.mode_( \init ));
		this.setSpecMode( \value, \nonsynth );
		mappedArgs = [ \value ];
		allowedModes = [ \init, \sync, \normal ];
		this.changed( \init );
	}
		
	isMappedArg { |name|
		if( name == \value ) {
			^valueIsMapped;
		} {
			^mappedArgs.notNil && { mappedArgs.includes( name ) };
		};
	}
	
	asUnmappedArgsArray { |unit, argPairs|
		argPairs = argPairs ? #[];
		^unit.argSpecs.collect({ |item| 
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			if( this.isMappedArg( item.name ) ) { 
				val = item.spec.unmap( val.value ); 
			} {
				val = val.value;
			};
			[ item.name,  val ] 
		}).flatten(1);
	}
	
	doFunc { |unit|
		var res;
		res = func.value( unit, 
			*this.asUnmappedArgsArray( unit, unit.args ).clump(2).flop[1]
		);
		if( valueIsMapped ) {
			unit.setArg( \value, unit.getSpec( \value ).map( res ) );
		} {
			unit.setArg( \value, res );
		};
	}
	
	uPrepareValue { |unit| // can prepare value earlier if needed
		this.prepare( nil, unit );
		unit.setArg( \u_prepared, true );
		^unit.value;
	}
	
	prepare { |servers, unit, action|
		if( unit.get( \u_prepared ) == false ) {
			this.doFunc( unit );
			unit.setArg( \u_prepared, true );
		};
		action.value;
	}
	
	needsPrepare { ^true }
	
	activateUnit { |unit| // called at UMap:asUnitArg
		if( unit.unit.notNil && { unit.unit.synths.size > 0 } ) {
			unit.prepare;
		};
	}
	
	makeSynth { |unit, target, startPos = 0, synthAction|
		unit.set( \u_prepared, false );
	}
	
	hasBus { ^false }
	
	value { |unit|
		if( valueIsMapped ) {
			^(unit.get( \u_spec ) ?? { [0,1].asSpec }).map( 
				unit.getSpec( \value ).unmap( unit.value )
			);
		} {
			^unit.value
		};
	}
	
	setSynth { |unit ...keyValuePairs|
		keyValuePairs.clump(2).do({ |item|
			if( [ \u_spec, \u_prepared ].includes( item[0] ).not ) {
				this.doFunc( unit );
				unit.unitSet;
			};
		});
	}
	
	canInsert { ^false }

}

+ Object {
	uPrepareValue { }
}

+ U {
	uPrepareValue { |target, startPos = 0, action| 
			this.prepare( target, startPos, action ); 
			^this.value;
	}
}
