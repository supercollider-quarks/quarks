// (1) distributed sounds

// for testing on a single machine, make a second server:
(
if (r.nameList.includes(\otto).not) { 
	y = SharedServer(\otto, NetAddr("127.0.0.1", 57108), r.defaultServerOptions, r.nextFreeID);
	y.boot; y.makeWindow;
	r.servers.put(\otto, y);
};
)

	// make a synthdef
(
SynthDef(\zzz, {|out, amp = 0.1, sustain = 1, freq = 440, noise = 0.3, pan|
	var env = EnvGen.ar(Env.perc(0.01, sustain), doneAction: 2) * amp;
	var son = SinOsc.ar(freq * [1, 1.2], BrownNoise.ar * noise).sum;
	Out.ar(out, Pan2.ar(son * env, pan));
}).share;
)

	// play it on my own server, 
	// change the parameters :  
(instrument: \zzz).play
(instrument: \zzz, amp: 0.5).play
(instrument: \zzz, amp: 0.1).play

(instrument: \zzz, freq: 500).play
(instrument: \zzz, freq: 2500).play
(instrument: \zzz, freq: 1500, amp: 0.5).play

(instrument: \zzz, sustain: 10).play
(instrument: \zzz, sustain: 1).play
(instrument: \zzz, sustain: 0.3).play
(instrument: \zzz, sustain: 0.1, amp: 0.5).play
(instrument: \zzz, sustain: 0.1, amp: 0.5, noise: 0.8).play



	// r.s is the republic server - should also play locally
(instrument: \zzz, freq: 500, server: r.s).play

	// r.s is the republic server - where: \all goes to all members
(instrument: \zzz, freq: 500, server: r.s, where: \all).play


	// choose a random server: 
(instrument: \zzz, freq: 500, server: r.servers.choose).play

	// choose a specific server - if not found, plays locally
(instrument: \zzz, freq: 500, dur: 3,server: r.servers[\abc]).play
(instrument: \zzz, freq: 500, dur: 3,server: r.servers[\otto]).play
(instrument: \zzz, freq: 500, dur: 3,server: r.servers[\absentNoName]).play

	// expansion is not working yet! 
(instrument: \zzz, freq: 500, dur: 10, server: r.s, where: \all).play
(instrument: \zzz, freq: 500, server: r.s, where: [\otto, \abc]).play

	// and another sound
(
SynthDef(\xxy, {|out, sustain = 1, freq = 440|
	var env = Line.kr(0.1, 0, sustain, doneAction: 2);
	var son = SinOsc.ar(freq * [1, 1.2, 1.5, 1.7]).sum;
	Out.ar(out, son * env);
}).share;
)

(instrument: \xxy, freq: 500, server: t).play

	// make a gui where you see all Tdefs (task proxies):
TdefAllGui(12);

	// put together a little task that combines events, and 
	// plays them on some servers:
(
Tdef(\x2, {
	var servers = r.servers.values.scramble.keep(7).postln;
	inf.do { |i|
	servers.do { |x, j|
		(
			instrument: \xxy, 
			server: x, 
			freq: 1000 + (((i % 13) + (i % 8)) * 100), 
			sustain: 0.5
			
		).play;
		
		0.125.wait;
	}; };
}).play;
);


	// if one server has a multichannel system, 
	// one can play events on all 8 chans of big system:
(
Tdef(\y, {
	inf.do { 
	8.do { |i|
		(
			instrument: \xxy, 
			server: r.servers[\big], 
			freq: 1000 + ((i % 13) * 100), 
			sustain: 0.2, 
			amp: 0.02, 
			out: i
		).play;
		
		0.25.wait;
	}; };
}).play;
);


(
Tdef(\x, {
	var servers = r.servers.values.scramble.keep(4).postln;
	inf.do { |i|
	servers.do { |x, j|
		(
			instrument: [\xxy, \zzz].choose, 
			server: x.postln, 
			freq: 1000 + (100 * (i % 4)), 
			sustain: 0.2
		).play;
		
		0.1.wait;
	}; };
}).play;
);
