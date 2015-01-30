/*
\pinkNoise

A pink noise generator. Generates noise whose spectrum falls off in power by 3 dB per octave. This gives equal power over the span of each octave. This version gives 8 octaves of pink noise.
http://en.wikipedia.org/wiki/Pink_noise

amp: amplitude (0-1) of the noise
seed: random seed (positive whole number). The same seed will always result in exactly the same noise on any computer. If you want decorrelated noise from multiple sources, change the seed on each source to a different number.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \pinkNoise, { |amp = 0.1, seed = 12345|
	URandSeed.ir(seed);
	UMixOut.ar( 0, PinkNoise.ar * amp, 0, true )
} ).category_( \noise )
	.setSpec( \seed, PositiveIntegerSpec(12345) )