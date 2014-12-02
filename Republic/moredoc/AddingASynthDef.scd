	// add a new synthdef so everyone gets it:
(
SynthDef(\kabelScahden, { |out, freq=50, sustain=1, amp = 1|
		var  h, n, m, k, trig, hum; 
		hum = { 
			trig = Dust.kr( 7 ** LFNoise1.kr(0.3) );
			h = ToggleFF.kr(CoinGate.kr(0.4,trig)) *
			RLPF.ar(LFPulse.ar(freq, 0, 0.5, 1, LFNoise1.kr(2, 0.5, -0.5)), 6000, 0.15);
			n =  Trig.ar(trig, TRand.kr(0,0.01,CoinGate.kr(0.4,trig))) * WhiteNoise.ar;
			m =  Trig.ar(trig, TRand.kr(0,0.01,CoinGate.kr(0.4,trig))) * BrownNoise.ar;
			k = Trig.ar(trig, LFNoise1.kr([ 4, 4.2 ], 0.1, 0.11)) *			LFClipNoise.ar(LFNoise0.kr(7, 30, 40));
			LeakDC.ar(h + n + k + m * 10).distort;
		} ! 2;
		Out.ar(out, hum.clip2 * EnvGen.kr(Env([amp,amp,0] * 0.5,[sustain, 0]), doneAction: 2));
		
}).share;
)

	// play it on my own server
(instrument: \kabelScahden, freq: [50, 50], sustain: 3).play;

	// on a randomly chosen server
(instrument: \kabelScahden, freq: [50, 75] * 3, sustain: 5, where: r.nameList.choose).play;

	// on a specific server - plays on mine if not there.
(instrument: \kabelScahden, freq: [50, 50], sustain: 5, where:  \petra).play;

	// or use 'where' - also to play on all
(instrument: \kabelScahden, freq: [50, 60] * 5, sustain: 4, amp: 2, where: \all).play;

	// or use 'where' - maybe clearer, plays nowhere if server not present
(instrument: \kabelScahden, freq: [50, 60] * 5, sustain: 4, amp: 2, where: [\abc, \def]).play;



	// mimic skype's new message sound
	// with strum 
(instrument: \glisson, freq: [800, 1200], freq2: [800, 1300], strum: 0.08, sustain: 0.1).play;
	// different sound, different server
(instrument: \grainXLine, freq: [800, 1250], strum: 0.08, sustain: 0.2, server: r.servers[\petra]).play

	// with a new synth function, played on specific server directly.
{ (BPF.ar(GrayNoise.ar, EnvGen.kr(Env([800, 1250, 1300], [0.1, 0.1] * 1.2, 4)), 0.02) 
	* XLine.kr(10, 0.01, 0.4).min(4)).distort }.play(r.servers[\petra])

