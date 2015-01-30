/*
\lfo_pulse

Creates an UMap with a low frequency pulse wave oscillator.

freq: the frequency of the pulse wave
phase: the initial phase (0-1) of the pulse wave
width: the width of the pulse. 0.5 (default) creates a square wave
range: the output range

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \lfo_pulse, { |freq = 2, phase = 0, width = 0.5, range = #[0.0,1.0]|
	var sig;
	sig = LFPulse.kr(freq, phase + (\u_startPos.kr(0) * freq), width).range(*range);
	UMapOut.kr(sig);
})
	.setSpec( \phase, [0,1,\lin,0,0], \init )
	.setSpec( \freq, FreqSpec(0.01,300, default: 2) )
	.mappedArgs_( [ \range ] )
	.category_( 'modulation' )
