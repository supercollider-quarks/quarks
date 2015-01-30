UPatDef : FuncUMapDef {
	
	classvar <>defaultCanUseUMapFunc;
	classvar <>currentUnit;
	
	*initClass{
		defaultCanUseUMapFunc = { |unit, key, upatdef|
			unit.getSpec( key ).respondsTo( \asControlSpec ) && {
				unit.getDefault( key ).asControlInput.asCollection.size == upatdef.numChannels
			};
		};
	}
	
	doFunc { |unit|
		var res;
		if( unit.stream.isNil ) { this.makeStream( unit ) };
		this.class.currentUnit = unit;
		res = unit.stream.next;
		this.class.currentUnit = nil;
		if( valueIsMapped ) {
			unit.setArg( \value, unit.getSpec( \value ).map( res ) );
		} {
			unit.setArg( \value, res );
		};
	}
	
	activateUnit { |unit| // called at UMap:asUnitArg
		unit.makeStreamID;
		if( unit.unit.notNil && { unit.unit.synths.size > 0 } ) {
			unit.prepare;
		};
	}
	
	makeStream { |unit|
		unit.stream = func.value( unit, *this.getStreamArgs( unit ) );
	}
	
	getStreamArgs { |unit|
		^unit.argSpecs.collect({ |item| 
			if( this.isMappedArg( item.name ) ) { 
				UPatArg( unit, item.name, item.spec ); 
			} {
				UPatArg( unit, item.name );
			};
		});
	}
	
	setSynth { |unit ...keyValuePairs|
		keyValuePairs.clump(2).do({ |item|
			if( [ \u_spec, \u_prepared ].includes( item[0] ).not ) {
				unit.unitSet;
			};
		});
	}

}

UPatArg {
	var <>unit, <>key, <>spec;
	
	*new { |unit, key, spec|
		^super.newCopyArgs( unit, key, spec );	
	}
	
	next {
		var value;
		if( UPatDef.currentUnit.notNil && { unit !== UPatDef.currentUnit }) {
			value = UPatDef.currentUnit.get( key );
		} {
			value = unit.get( key );
		};
		if( value.isUMap.not && { spec.notNil } ) {
			^spec.unmap( value.next );
		} {
			^value.next;
		};
	}
	
	doesNotUnderstand { |selector ...args|
		^this.next.perform( selector, *args);
	}
}