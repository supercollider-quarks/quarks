/*
\sequencer_8

Creates an UMap that can sequence a number (maximum 8) of values (or UMaps) over time. If you need to sequence > 8 values, use \sequencer_16 instead.

speed: number of steps per second
reverse: if true, move backwards through the steps
range: the range (0-7) of used steps.
smooth: smoothening parameter (0: hard steps, 1: linear interpolation)
value0 - value7: the values of the steps (can be UMaps)

The 'value[0-7]' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \sequencer_8, {
	var values, speed, reverse, range, smooth;
	var phase, step, resetPos, sig;
	speed = \speed.kr(10);
	reverse = \reverse.kr(0);
	range =\range.kr([0,7]);
	smooth = \smooth.kr(0.0);
	values = 8.collect({ |i|
		("value"++i).asSymbol.ukr(0.5);
	});
	step = ((ControlDur.ir + SampleDur.ir) * speed) * reverse.linlin(0,1,1,-1);
	range = range + [0,1];
	resetPos = (range[0] - step) + 
		((\u_startPos.ir(0.0) * ControlRate.ir) * step).wrap( 0, range[1] - range[0] );
	phase = Phasor.kr( Impulse.kr(0), step, range[0], range[1], resetPos );
	phase = (phase.floor * (1-smooth)) + (phase * smooth);
	sig = LinSelectX.kr( phase, values );
	UMapOut.kr( sig );
}).category_( \modulation )
	.setSpec( \speed, [0,300, 4,0,10].asSpec )
	.setSpec( \reverse, BoolSpec(false) )
	.setSpec( \range, [0,7,\lin,1].asSpec.asRangeSpec )
	.mappedArgs_( 8.collect({ |i| ("value"++i).asSymbol }) );