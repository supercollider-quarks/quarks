/*
\envir_get

Creates an UMap that gets the value of an environment variable in SuperCollider. The value will only be used if it exists, and if it is valid input.

value: the actual output value (this will change when the variable changes, but can also be changed manually)
active: if true, the UMap will listen to the specified variable and update it regularly. A message with the key and value range is posted in the post window when active becomes true. Hitting cmd-. will make the UMap stop updating it's value, but whenever the Unit is started, it will re-activate.
key: the name of the variable (as String). If the key is not specified (default), it will automatically be set to the name of the parameter to which the UMap is connected when 'active' is set to true.
speed: the speed by which the value is checked and updated, in times per second
mapped: if true, the value will be mapped from 0-1 to the output range of the UMap, if false (default) the value will be used directly.

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

example:

x = UChain([ 'sine', [ 'freq', UMap('envir_get', [ 'active', true, 'key', "freq" ]) ] ], 'output');
x.prepareAndStart;

~freq = 330;
~freq = 440;

x.stop;

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

ValueUMapDef( \envir_get, { |unit, routine|
	var key, spec;
	var update, lastVal;
	routine.stop;
	spec = unit.getSpec( \value );
	key = unit.get( \key );
	if( key.size == 0 ) {
		key = unit.unitArgName;
		unit.set( \key, key.asString );
	};
	key = key.asSymbol;
	"listening to ~%, range %\n".postf( 
		key,
		if( unit.get( \mapped ) ) {
			"0 - 1"
		} {
			"% - %".format( spec.minval, spec.maxval );
		};
	);
	update = {
		var key = unit.get( \key ).asSymbol;
		var val = key.envirGet;
		if( val != lastVal && { spec.class.testObject( val ) }) {
			if( unit.get( \mapped ) ) {
				unit.mapSet( \value, val );
			} {
				unit.set( \value, spec.constrain( val ) );
			};
		};
		lastVal = val;
	};
	update.value; // this seems to make it crash sometimes..
	{
		loop {
			(1/unit.get( \speed )).max(0.01).wait;
			update.value;
		};
	}.fork;
}, { |unit, routine|
	routine.stop;
	nil;
}, [
	[ \key, nil, StringSpec("") ],
	[ \speed, 10, [1,100,\exp,0,10].asSpec ],
	[ \mapped, false, BoolSpec(false) ]
], \control );