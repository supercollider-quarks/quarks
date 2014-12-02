	// share a synthdef
(
SynthDef(\xxx, {|out, zzf = 440, sustain = 1, mod = 0.5, amp = 0.1, pan|
	var env = Line.kr(1, 0, sustain, doneAction: 2);
	Out.ar(out,
		Pan2.ar(
			RLPF.ar(
				LFPulse.ar(zzf, 0, SinOsc.kr(mod * zzf)),
				zzf * (env.cubed + mod + 0.5),
				0.2
			),
		pan, env * amp
	))
}).share;
)

	// post all synthdefs, snd examples for events and patterns that use it
r.postSynthDefs;	
r.postEvents;
r.postPdefs;


	// play a pattern that goes around all members

(
Pdef(\x,
	Pbind(
		\instrument, \xxy,
		\server, r.s,
		\where, Prand(r.nameList, 8), // 
		\dur, 1,
		\legato, 0.3, 
		\freq, Pwhite().linexp(0, 1, 500, 2000)
	).trace
).play;
)

	// getting to the different servers: 

r.servers;
r.nameList;

	// play an event with that synthdef on your computer: 
(server: r.myServer, instrument: \xxy, freq: exprand(300, 1000)).play;


	// or choose random member:
(server: r.servers.choose, instrument: \xxy, freq: exprand(300, 1000)).play;


	//	using where: // 
	
	// send a synth to everyone:
(server: r.s, where: \all, instrument: \xxy, freq: exprand(300, 1000)).play;


	// or to a specific member (replace \XXX with someone's nickname)
(server: r.s, instrument: \xxy, where: \XXX, freq: exprand(300, 1000)).play;

	// or to a subset of the members
(server: r.s, where: [\jrhbr, \adc], instrument: \xxy, freq: [400, 1600]).play;
	
	// by an index - same as r.nameList.wrapAt(index)
(server: r.s, where: 1, instrument: \xxy, freq: [400, 1600]).play;

	// by a list of indices - same as r.nameList.wrapAt(indices)
(server: r.s, where: [1, 2], instrument: \xxy, freq: [400, 1600]).play;


	// test if pattern works here:
	
(
Pdef(\x, 
	Pbind(
		\instrument, \xxx,
		\server, r.servers[r.nickname],
		\zzf, Pseq([8700, 800, 720], inf),
		\mod, Pwhite(0.1, 0.7, inf),
		\dur, Prand([0.3, 0.5, Pn(0.04, 5)], inf)
	)
).play
)

// send synth messages to random participants:

(
Pdef(\x, 
	Pbind(
		\instrument, \xxx,
		\server, r.s,
		\where, r.nameList.scramble.keep(3), 
		\zzf, Pseq([8700, 800, 720], inf) * [ 1, 1.5, 2.5],
		\strum, 0.03, 
		\mod, Pwhite(0.1, 0.7, inf),
		\dur, Prand([0.3, 0.5, Pn(0.04, 5)], inf)
	)
).play
)

(
Pdef(\x, 
	Pbind(
		\instrument, \xxx,
		\server, Pfunc { r.servers.choose } .trace,
		\zzf, Pseq([8700, 800, 720], inf),
		\mod, Pwhite(0.1, 0.7, inf),
		\dur, Prand([0.3, 0.5, Pn(0.04, 5)], inf)
	)
).play
)
