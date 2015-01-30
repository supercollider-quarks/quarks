/*
\lfo_noise

Creates an UMap with a low frequency noise oscillator.

freq: the frequency of the noise generator
type: the type of noise:
	0: step or sample-and-hold noise; hard jumps at each value change
	1: linear interpolated noise
	2: cubic interpolated noise
seed: random seed (positive whole number). The same seed will always result in exactly the signal on any computer.
range: the output range

The 'range' arg is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \lfo_noise, { |freq = 2, type = 2, seed = 12345, range = #[0.0,1.0]|
	var sig;
	URandSeed.ir(seed);
	sig = [
		LFDNoise0.kr( freq ),
		LFDNoise1.kr( freq ),
		LFDNoise3.kr( freq )
	];
	sig = Select.kr( type, sig ).linlin(-1,1,*range);
	UMapOut.kr(sig);
})
	.setSpec( \type, [0,2,\lin,1,0].asSpec )
	.setSpec( \freq, FreqSpec(0.01,300, default: 2) )
	.setSpec( \seed, PositiveIntegerSpec(12345), \init )
	.mappedArgs_( [ \range ] )
	.category_( 'modulation' )
