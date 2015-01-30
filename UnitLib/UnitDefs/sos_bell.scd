/*
\sos_bell

A simple bell sound by Dan Stowell. based on a sound-on-sound 'synth secrets' tutorial. Adapted to sound indefinitely.

freq: frequency (Hz) of the bell
decay: decay time (s) of the bell.
amp: amplitude (0-1)
seed: random seed (positive whole number). The same seed will always result in exactly the same signal on any computer. 

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef(\sos_bell,{ |freq = 440, decay = 10,  amp = 0.1, seed = 12345|
	var out, env;	
	var son, strike, hum;
	
	URandSeed.ir( seed ); // always the same sound
	
	// Stretched harmonic series
	son = SinOsc.ar(#[2, 3, 4.1, 5.43, 6.8, 8.21] * freq, 0, 
			 #[1, 0.9, 0.8, 0.7, 0.6, 0.5] * 0.1);
	
	son = son * EnvGen.ar(Env.new([0,1,0.3, 0.2, 0], [0, 0.3, 0.3, 0.3] * decay));
	
	// A bit of FM adds 'warble'
	son = son * LFTri.ar({Rand(1.0, 1.8)}.dup(6), 1, 0.3, 0.7);
	
	// Mix down the partials in the main sound
	son = son.mean;
	
	strike = SinOsc.ar(LFNoise1.ar(freq * 36, 100, freq*8), 1, 0.1) *
				EnvGen.ar(Env.new([0,1,0.2, 0.1, 0], [0, 0.01, 0, 0.04]));
	
	hum = SinOsc.ar([freq*1.01, freq*0.47], 0, 
		EnvGen.ar(Env.new([0,0.05,0.05,0], [decay/2,decay/2,decay]) ) 
	).mean;
	
	UMixOut.ar(0, (son + strike + hum) * 4 * amp, 0, true );
	
}).category_( \synthesis )
	.setSpec( \decay, [0.3,1000,\exp].asSpec )
	.setSpec( \seed, PositiveIntegerSpec(12345) );