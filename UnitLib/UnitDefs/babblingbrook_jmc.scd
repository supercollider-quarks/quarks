/*
\babblingbrook_jmc

The famous James Mc.Cartney babbling brook. based on code posted to sc-users 2007-04-07 by james mccartney

amp: amplitude (0-1)
seed: random seed (positive whole number). The same seed will always result in exactly the same signal on any computer.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \babblingbrook_jmc, { |amp = 0.1, seed = 12345|
	var son;
	
	URandSeed.ir(seed);
	son = ({RHPF.ar(OnePole.ar(BrownNoise.ar, 0.99), LPF.ar(BrownNoise.ar, 14)
		* 400 + 500, 0.03, 0.003)}! Udef.numChannels )
		+ ({RHPF.ar(OnePole.ar(BrownNoise.ar, 0.99), LPF.ar(BrownNoise.ar, 20)
		* 800 + 1000, 0.03, 0.005)}! Udef.numChannels )  * 4;
	
	UMixOut.ar( 0, son * (amp * 100), 0, true )
}, [ 
	[ \amp, 0.1, \amp.asSpec ], 
	[ \seed, 12345, PositiveIntegerSpec(12345)  ] 
], channels: [1,2,3,4,5,6,7,8] ).category_( \synthesis )