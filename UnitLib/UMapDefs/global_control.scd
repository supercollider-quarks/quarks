/*
\global_control

Creates an UMap that listens to a control in UGlobalControl

value: the actual output value (this will change when the variable changes, but can also be changed manually)
active: if true, the UMap will listen to the specified global control and update it whenever it changes. If a non-existing key is used, it will be added to UGlobalControl automatically when 'active' == true.
controlKey: the name of the globalControl (a Symbol)

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

ValueUMapDef( \global_control, { |unit, ctrl|
	var key, spec;
	UGlobalControl.removeDependant( ctrl );
	spec = unit.getSpec( \value );
	key = unit.get( \controlKey );
	if( key.isNil ) {
		key = 'global_0';
		unit.set( \controlKey, key );
	};
	key = key.asSymbol;
	"listening to UGlobalControl '%'\n".postf( key );
	ctrl = { |obj, key|
		var val;
		if( unit.get( \learn ) ) {
			unit.set( \controlKey, key );
		};
		if( key === unit.get( \controlKey ) ) {
			val = UGlobalControl.current.get( key );
			if( val.notNil ) {
				unit.mapSet( \value, val );
			};
		};
	};
	UGlobalControl.addDependant( ctrl );
	UGlobalControl.current.put( key, UGlobalControl.current.at( key ) );
	ctrl;
}, { |unit, ctrl|
	UGlobalControl.removeDependant( ctrl );
	nil;
}, [
	[ \controlKey, nil, UGlobalControlKeySpec() ],
	[ \learn, false, BoolSpec() ]
], \control );