/*
\lfo_sine

Creates an UMap with a low frequency sine wave oscillator.

freq: the frequency of the sine wave
phase: (-pi - pi) the start phase of the sine wave (can be modulated)
range: the output range

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \lfo_sine, { |freq = 2, phase = 0, range = #[0.0,1.0]|
	var sig;
	sig = SinOsc.kr(freq, phase + DC.kr(\u_startPos.kr(0) * freq * 2pi).wrap(0,2pi)).range(*range);
	UMapOut.kr(sig);
})
	.setSpec( \phase, AngleSpec() )
	.setSpec( \freq, FreqSpec(0.01,300, default: 2) )
	.mappedArgs_( [ \range ] )
	.category_( 'modulation' )
