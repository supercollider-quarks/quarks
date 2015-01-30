/*
\osc_set

Creates an UMap that listens to OSC messages. Works similar to all 'midi_xxx' UMapDefs.

value: the actual output value (this will change when a the OSC message is received, but can also be changed manually)
active: if true, the UMap will listen to the specified OSC message. Hitting cmd-. will make the UMap stop listening, but whenever the Unit is started, it will re-activate. The OSC port is always that of the SuperCollider or WFSCollider application, normally 57120 but that may change sometimes. The current port number is posted together with the message name and range in the post window when active is set to true.
message: the OSC message (String) to which the UMap is listening. This will be set to the name of the parameter the UMap is connected to if not specified (default). The actual message the UMap listens to is always preceeded by a "/" char.
mapped: if true, the received values will be mapped from 0-1 to the range of the output value. If false (default) no mapping is applied and values will be set directly.

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

ValueUMapDef( \osc_set, { |unit, oscfunc|
	var message, spec;
	oscfunc.free;
	spec = unit.getSpec( \value );
	message = unit.get( \message );
	if( message.size == 0 ) {
		message = unit.unitArgName;
		unit.set( \message, message.asString );
	};
	message = message.asSymbol;
	"setting up OSCFunc on port % for message \"%\", range %\n".postf( 
		NetAddr.langPort, 
		if( message.asString[0] == $/ ) { message } { "/" ++ message },
		if( unit.get( \mapped ) ) {
			"0 - 1"
		} {
			"% - %".format( spec.minval, spec.maxval );
		};
	);
	OSCFunc({ |msg, time, addr, recvPort|
		if( unit.get( \mapped ) ) {
			unit.mapSet( \value, msg[1] );
		} {
			unit.set( \value, spec.constrain( msg[1] ) );
		};
	}, message)
}, { |unit, oscfunc|
	oscfunc.free;
	nil;
}, [
	[ \message, nil, StringSpec("") ],
	[ \mapped, false, BoolSpec(false) ]
], \control );