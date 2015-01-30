/*
\dust

A random impulse generator. Generates impulses of random amplitude (positive and negative) with random time intervals in between.

density: the average amount of impulses per second
amp: maximum amplitude (0-1) of the pulses
seed: random seed (positive whole number). The same seed will always result in exactly the signal on any computer. If you want decorrelated noise from multiple sources, change the seed on each source to a different number.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \dust, { |density = 100, amp = 0.1, seed = 12345|
	URandSeed.ir(seed);
	UMixOut.ar( 0, Dust2.ar(density) * amp, 0, true )
} ).category_( \noise )
	.setSpec( \density, [ 1, 10000, \exp, 1, 100 ].asSpec )
	.setSpec( \seed, PositiveIntegerSpec(12345) )