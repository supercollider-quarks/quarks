/*
\crackle

A noise generator based on a chaotic function.

chaosParam: value between 1 and 2; higher values produce more crackling
amp: amplitude (0-1) of the noise
seed: random seed (positive whole number). The same seed will always result in exactly the same noise on any computer. If you want decorrelated noise from multiple sources, change the seed on each source to a different number.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \crackle, { |chaosParam = 1.5, amp = 0.1, seed = 12345|
	URandSeed.ir(seed);
	UMixOut.ar( 0, Crackle.ar( chaosParam, amp ), 0, true )
} ).category_( \noise )
	.setSpec( \seed, PositiveIntegerSpec(12345) )
	.setSpec( \chaosParam, [1,2,\lin].asSpec );