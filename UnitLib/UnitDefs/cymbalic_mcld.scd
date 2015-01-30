/*
\cymbalic_mcld

A cymbal sound by Dan Stowell. Based on the example at http://www.mcld.co.uk/cymbalsynthesis/ published 2008 by Dan Stowell

decay: decay time (s) of the cymbal
ratio: scale factor for the frequencies (0.25-4)
amp: amplitude (0-1)
seed: random seed (positive whole number). The same seed will always result in exactly the same signal on any computer.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef(\cymbalic_mcld,{ |decay = 10, ratio = 1, amp = 0.1, seed = 12345|
	var lodriver, locutoffenv, hidriver, hicutoffenv, freqs, res, thwack;
	
	URandSeed.ir( seed ); // always the same sound
	
	locutoffenv = EnvGen.ar(Env.perc(0.5, decay)) * 20000 + 10;
	lodriver = LPF.ar(WhiteNoise.ar(0.1), locutoffenv);
	
	hicutoffenv = 10001 - (EnvGen.ar(Env.perc(1, 3)) * 10000);
	hidriver = HPF.ar(WhiteNoise.ar(0.1), hicutoffenv);
	hidriver = hidriver * EnvGen.ar(Env.perc(1, 2, 0.25));
	
	thwack = EnvGen.ar(Env.perc(0.001,0.001,1));
	
	freqs  = ExpRand( 300.dup(100), 20000 ) * ratio;
	
	res = Ringz.ar(lodriver + hidriver + thwack, freqs).mean;
		
	UMixOut.ar(0, ((res * 1) + (lodriver * 2) + thwack) * amp * 0.5, 0, true );
	
}).category_( \synthesis )
	.setSpec( \decay, [2.5,100,\exp, 0, 10].asSpec, \init )
	.setSpec( \ratio, [0.25, 4, \exp, 0, 1].asSpec )
	.setSpec( \seed, PositiveIntegerSpec(12345) );