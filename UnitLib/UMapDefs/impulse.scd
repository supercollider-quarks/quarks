/*
\impulse

Creates an UMap for producing single-frame impulses

freq: the frequency of the impulses wave
phase: the initial phase (0-1) of the pulse wave
range: the output range

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \impulse, { |freq = 2, phase = 0, range = #[0.0,1.0]|
	var sig;
	sig = Impulse.kr(freq, (phase + (\u_startPos.kr(0) * freq)).wrap(0,1)).range(*range);
	UMapOut.kr(sig);
})
	.setSpec( \phase, [0,1,\lin,0,0] )
	.setSpec( \freq, FreqSpec(0.01,300, default: 2) )
	.mappedArgs_( [ \range ] )
	.category_( 'trigger_utility' )
