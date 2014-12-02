// General-use synthdefs
// I will keep them in a separate SynthDescLib as well as the global one

Library.put(\ddwGlobal, \storeMethod,
	if(SynthDef.findRespondingMethodFor(\add).notNil)
		{ \add } { \memStore }
);

SynthDescLib(\ddwSynths, { Server.default });

~mylib = SynthDescLib.all[\ddwSynths];

SynthDef(\ctlPoint, { |outbus, value, time, curve|
	var	start = In.kr(outbus, 1);
	ReplaceOut.kr(outbus, EnvGen.kr(Env([start, value], [time], curve), doneAction: 2));
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

// this def plays an arbitrary envelope on the bus
// to avoid discontinuity, the envelope's init value is replaced
// with the bus's value if connect>0
SynthDef(\ctlEnv, { |outbus, levelScale = 1, levelBias = 0, time = 1, connect = 1|
	var	env = \env.kr(0 ! 40);	// enough for 10 breakpoints
	env[0] = Select.kr(connect > 0, [env[0], In.kr(outbus, 1)]);
	ReplaceOut.kr(outbus, EnvGen.kr(env, 1, levelScale, levelBias, time, doneAction: 2));
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

// formerly stock synthdefs were written into the synthdefs/ folder
// but it's better to send synthdefs on demand
// so now they are stored in SynthDescLib.global
// and recalled by name when needed

SynthDef(\bufGrain, { |start, time, bufnum, rate = 1, amp = 1,
		attack = 0.001, decay = 0.02, outbus|
	var sig;
	sig = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
		* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
	Out.ar(outbus, sig * amp);
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

SynthDef(\bufGrainPan, { |start, time, bufnum, pan, rate = 1, amp = 1,
		attack = 0.001, decay = 0.02, outbus|
	var sig;
	sig = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
		* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
	Out.ar(outbus, Pan2.ar(sig, pan, amp));
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

SynthDef(\bufGrain2, { |start, time, bufnum, rate = 1, amp = 1,
		attack = 0.001, decay = 0.02, outbus|
	var sig;
	sig = PlayBuf.ar(2, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
		* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
	Out.ar(outbus, sig * amp);
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

SynthDef(\bufGrainPan2, { |start, time, bufnum, rate = 1, pan, amp = 1,
		attack = 0.001, decay = 0.02, outbus|
	var l, r;
	#l, r = PlayBuf.ar(2, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
		* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
	Out.ar(outbus, Balance2.ar(l, r, pan, amp));
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

// use a pair of enveloped PlayBufs to avoid clicks when looping back on the sample

SynthDef(\bufRoll, { |start, time = 1, bufnum, rate = 1, amp = 1, strokesPerSec = 18,
		attack = 0.002, decay = 0.01,	// attack and decay of individual stroke
		outbus|
	var	sig, envDefault, env,
		trig = Impulse.ar(strokesPerSec),
		trigs = PulseDivider.ar(trig, 2, (0..1)),
		strokeEnv = EnvGen.ar(Env.linen(attack, strokesPerSec.reciprocal - attack - decay, decay), trigs);
		// time of this env must add up to 1.0 sec
	envDefault = Env(#[0, 1, 0], #[0.001, 0.999], -4);
		// 40 == 10 possible envelope segments (which is a lot, really)
	env = Control.names(\env).kr((0 ! 40).overWrite(envDefault.asArray, 0));
	sig = (PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), trigs, start, 1) * strokeEnv).sum
		* EnvGen.ar(env, timeScale: time, doneAction:2);
	Out.ar(outbus, sig * amp);
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

SynthDef(\bufRollPan, { |start, time = 1, bufnum, rate = 1, amp = 1, strokesPerSec = 18, pan = 0,
		attack = 0.002, decay = 0.01,	// attack and decay of individual stroke
		outbus|
	var	sig, envDefault, env,
		trig = Impulse.ar(strokesPerSec),
		trigs = PulseDivider.ar(trig, 2, (0..1)),
		strokeEnv = EnvGen.ar(Env.linen(attack, strokesPerSec.reciprocal - attack - decay, decay), trigs);
		// time of this env must add up to 1.0 sec
	envDefault = Env(#[0, 1, 0], #[0.001, 0.999], -4);
		// 40 == 10 possible envelope segments (which is a lot, really)
	env = Control.names(\env).kr((0 ! 40).overWrite(envDefault.asArray, 0));
	sig = (PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), trigs, start, 1) * strokeEnv).sum
		* EnvGen.ar(env, timeScale: time, doneAction:2);
	Out.ar(outbus, Pan2.ar(sig, pan, amp));
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

SynthDef(\bufRollPanMove, { |start, time = 1, bufnum, rate = 1, amp = 1, strokesPerSec = 18,
		attack = 0.002, decay = 0.01,	// attack and decay of individual stroke
		panStart = 0, panEnd = 0,	outbus|
	var	sig, envDefault, env,
		trig = Impulse.ar(strokesPerSec),
		trigs = PulseDivider.ar(trig, 2, (0..1)),
		strokeEnv = EnvGen.ar(Env.linen(attack, strokesPerSec.reciprocal - attack - decay, decay), trigs);
		// time of this env must add up to 1.0 sec
	envDefault = Env(#[0, 1, 0], #[0.001, 0.999], -4);
		// 40 == 10 possible envelope segments (which is a lot, really)
	env = Control.names(\env).kr((0 ! 40).overWrite(envDefault.asArray, 0));
	sig = (PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), trigs, start, 1) * strokeEnv).sum
		* EnvGen.ar(env, timeScale: time, doneAction:2);
	Out.ar(outbus, Pan2.ar(sig, Line.kr(panStart, panEnd, time), amp));
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

SynthDef(\bufRollPanChangeRate, { |start, time = 1, bufnum, rate = 1, amp = 1,
		strokeRateStart = 18, strokeRateEnd = 18,
		attack = 0.002, decay = 0.01,	// attack and decay of individual stroke
		panStart = 0, panEnd = 0,	outbus|
	var	sig, envDefault, env,
		strokesPerSec = XLine.kr(strokeRateStart, strokeRateEnd, time),
		trig = Impulse.ar(strokesPerSec),
		trigs = PulseDivider.ar(trig, 2, (0..1)),
		strokeEnv = EnvGen.ar(Env.linen(attack, strokesPerSec.reciprocal - attack - decay, decay), trigs);
		// time of this env must add up to 1.0 sec
	envDefault = Env(#[0, 1, 0], #[0.001, 0.999], -4);
		// 40 == 10 possible envelope segments (which is a lot, really)
	env = Control.names(\env).kr((0 ! 40).overWrite(envDefault.asArray, 0));
	sig = (PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), trigs, start, 1) * strokeEnv).sum
		* EnvGen.ar(env, timeScale: time, doneAction:2);
	Out.ar(outbus, Pan2.ar(sig, Line.kr(panStart, panEnd, time), amp));
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);


SynthDef(\bufGrainRLPF, { |start, time, bufnum, rate = 1, amp = 1,
		attack = 0.001, decay = 0.02, ffreq = 2000, rq = 1, outbus|
	var sig;
	sig = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
		* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
	sig = RLPF.ar(sig, ffreq, rq);
	Out.ar(outbus, sig * amp);
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

SynthDef(\bufGrainRLPF2, { |start, time, bufnum, rate = 1, amp = 1,
		attack = 0.001, decay = 0.02, ffreq = 2000, rq = 1, outbus|
	var sig;
	sig = PlayBuf.ar(2, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
		* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
	sig = RLPF.ar(sig, ffreq, rq);
	Out.ar(outbus, sig * amp);
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);

SynthDef(\bufGrainRLPFPan, { |start, time, bufnum, rate = 1, amp = 1,
		attack = 0.001, decay = 0.02, pan = 0, ffreq = 2000, rq = 1, outbus|
	var sig;
	sig = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
		* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
	sig = RLPF.ar(sig, ffreq, rq);
	Out.ar(outbus, Pan2.ar(sig, pan, amp));
}).perform(Library.at(\ddwGlobal, \storeMethod), \ddwSynths);


// figure out rollPan and 2-channel versions later

SynthDescLib.all[\ddwSynths].synthDescs.do({ |desc|
	SynthDescLib.global.add(desc);		// copy into the global lib
});


// Initially my idea is that the user would be responsible for sending the defs
// to the server when needed. But this broke too many of my tracks,
// so I'm adding a dependant to servers to send the synthdefs on boot.
// Feel free to comment this out if you don't like it.

// Note also, sending a synthdef from the SynthDescLib does NOT work
// if the SynthDesc was read from a .scsyndef file.
// These are memStore'd so it's OK.

Updater(Server, { |obj, what, newServer|
	if(what == \serverAdded) {
		Updater(newServer, { |obj, what|
			if(what == \serverRunning and: { obj.serverRunning }) {
				SynthDescLib.all[\ddwSynths].synthDescs.do({ |desc|
					desc.def.send(obj);
				});
			};
		});
	};
});

Server.named.do({ |svr| Server.changed(\serverAdded, svr) });
