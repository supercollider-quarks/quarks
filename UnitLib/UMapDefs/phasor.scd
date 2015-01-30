/*
\phasor

Creates an UMap with a looping linear ramp, that can be reset via a trigger.

speed: the speed of the ramp
up: if true (default) the ramp moves upwards, if false the ramp moves downwards
startPos: the start value of the ramp
range: the output range
trigger: if set, the ramp will jump to 'startPos'

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \phasor, { |speed = 0.1, up = 1, startPos = 0, range = #[0.0,1.0]|
	var sig;
	var trigger;
	trigger = \trigger.utr + Impulse.kr(0);
	sig = Phasor.kr(trigger, speed * up.linlin(0,1,-1,1) / ControlRate.ir, 
		range[0], range[1], startPos );
	UMapOut.kr(sig);
})
	.setSpec( \up, BoolSpec(true) )
	.setSpec( \startPos, [0,1,\lin,0,0] )
	.setSpec( \speed, [0.001, 100, \exp, 0, 0.1 ] )
	.mappedArgs_( [ \range, \startPos ] )
	.category_( 'modulation' )
