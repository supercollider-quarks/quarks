/*
\ringMod

A ringmodulator that modulates the soundfile with a sine oscillator, a pink noise generator and/or a second sound file.
sine: level of the sine oscillator.freq: frequency of the sine oscillator.noise: level of the pink noise generator.input: level of the input on the 1st audio bus.mix: balance between the original and ringmodulated sound.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \ringMod, { |sine = 1, freq = 440, noise = 0, input = 0, mix = 1.0|
	var sig, modsig;
	RandSeed.ir(1,12345); // for the noise
	sig = UIn.ar(0,1);
	modsig = (
		SinOsc.ar(freq, 0, sine ) +
		PinkNoise.ar( noise ) +
		( UIn.ar(1,1) * input )
	) * sig;
	
	UOut.ar( 0, (sig * (1-mix)) + (modsig * mix) );
	
})
	.category_( \effect )
	.setSpec( \sine, \amp.asSpec )
	.setSpec( \freq, FreqSpec(2,20000) )
	.setSpec( \noise, \amp.asSpec )
	.setSpec( \input, \amp.asSpec )
	.setSpecMode( \sine, \normal, \noise, \normal, \input, \normal, \mix, \normal );
