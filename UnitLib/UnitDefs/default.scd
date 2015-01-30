/*
\default

The SuperCollider default sound.

freq: frequency (Hz) of the bell
amp: amplitude (0-1)
seed: random seed (positive whole number). The same seed will always result in exactly the same signal on any computer. 

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef(\default,{ |freq = 440, amp = 0.1, seed = 12345|
	var z;
	URandSeed.ir( seed ); // always the same sound
	z = LPF.ar(
		Mix(VarSaw.ar(freq + [0, Rand(-0.4,0.0), Rand(0.0,0.4)], 0, 0.3, 0.3)),
		XLine.kr(Rand(4000,5000), Rand(2500,3200), 1)
	) * Linen.kr(1, 0.01, 0.7, 0.3, 0);
				
	UMixOut.ar(0, z * amp, 0, true );
	
}).category_( \synthesis )
	.setSpec( \freq, FreqSpec() )
	.setSpec( \seed, PositiveIntegerSpec(12345) );