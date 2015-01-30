/*
\blip

A test signal generator, including a "blip" oscillator and pink noise generator, modulated with a pulse lfo. This generator is equal to the "blip" audioType in WFSCollider version 1.

rate: frequency (Hz) of pulse lfo
freq: frequency (Hz) of blip oscillator
noiseLevel: amplitude (0-1) of pink noise generator
blipLevel: amplitude (0-1) of blip oscillator


--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef(\blip,{ |rate = 1, freq = 100, noiseLevel = 0.125, blipLevel = 1|
	var out, env;	
	URandSeed.ir( 12345 ); // always the same noise
	out = ( Blip.ar( freq, 100, blipLevel * 0.125) + 
		PinkNoise.ar( noiseLevel )) * 
		LFPulse.kr(10 * rate); 
	UMixOut.ar(0, out, 0, true )
}).category_( \synthesis )
	.setSpec( \noiseLevel, \amp.asSpec )
	.setSpec( \blipLevel, \amp.asSpec )
	.setSpecMode( \noiseLevel, \normal )
	.setSpecMode( \blipLevel, \normal )
	