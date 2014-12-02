/*********

KeyPlayer example with 9 random synth percussion presets:

Type 1 - 9 to select a preset of sounds;
type Q, W, E, etc to play sounds

type alt-r to toggle/record a loop
type alt-p to play it

click on lgui button to open a KeyLoopGui

*********/

s.boot;
q = q ? ();
q.numChans = q.numChans ? 2; // stereo or multichannel

		// sound is pannable two chan stereo:
(
s.latency = nil;	// faster response

SynthDef(\fmgr, { |out, amp=0.2, pan, freq=440, ratio=2.5, mod=3, modrat=0.5,
	sustain=0.1, rand=0.1, att=0.01|

	var ampComp = AmpComp.ir(freq.max(50), exp: 0.2) * (0.8 ** mod.min(6));
		// make shorties louder?
	// ampComp = ampComp * (sustain.clip(0.01, 0.1) * 10 ** -0.5);

	var sound = SinOsc.ar(
		freq * Line.kr(1, { ExpRand(2 ** rand, 0.5 ** rand) }!2, sustain),
		0.5pi + SinOsc.ar(
			freq * ratio * Line.kr(1, { ExpRand(2 ** rand, 0.5 ** rand) }!2, sustain)
		)
		* Line.kr(mod, mod * modrat, sustain)
	);
	var env = EnvGen.ar(Env.perc(att, 1-att),
		levelScale: amp * ampComp,
		timeScale: sustain,
		doneAction: 2);
	sound = sound * env;
		// stereo
//	sound = Pan2.ar(sound, (pan * 2 + [-1, 1]).clip2(1)).sum;

		// maybe multichan here?
	sound = PanAz.ar(q.numChans, sound, (pan + [0, 1])).sum;

	OffsetOut.ar(out, sound )

}).add
);

(
q = q ? ();	// a dict for keeping things

	// a keyplayer with a recorder
q.fmod = KeyPlayer(\fmod);
q.fmod.makeLoop;

  	// space for fixed events for all keys,
    // times 9 presets
q.fmodEvents = ();

	// global variables that keyplayer will look up
q.fmods = ();
q.fmods.rand = 0.1;
q.fmods.amp = 0.1;

	// a method for making presets
q.makeFmods = { |q, seed = 2008, time=1, range=0|
	thisThread.randSeed = seed;

	"qwertzuiopasdfghjklyxcvbnm".do { |char, i|
		q.fmodEvents.put(char,
		(instrument: \fmgr, amp: 0.6,
			midinote: rrand(0.0, 48).round(1.5) + (range + 2 * 18),
			rand:{ q.fmods.rand },
			amp: { q.fmods.amp },
			out: { 1.rand }, 				// { 6.rand }, // MULTICHANNEL RAND!
			sustain: exprand(0.01, 1) * time,
			att: exprand(0.0001, 0.99),
			ratio: rrand(1, 5),
			mod: exprand(0.1, 10),
			modrat: 0.5,
			pan: {1.0.rand2}
		)
		);

		q.fmod.put(char, {
			q.fmodEvents[char].collect(_.value).play
		});
	};
};

(1..9).do { |i|
	q.fmod.put(i.asDigit, {
		q.makeFmods(
			1000 + i.postln,
			(10 ** (i - 1 % 3 - 1)),
			i - 1 div: 3
		)
	})
};

q.fmodWin = q.fmod.gui.parent;
q.fmodWin.bounds_(q.fmodWin.bounds  + (0@100));
	// slider that write into the envir:
EZSlider(q.fmodWin, 400@20, \vol, \amp, { |ez| q.fmods.amp = ez.value }, q.fmods.amp, false, 50, 50 );
EZSlider(q.fmodWin, 400@20, \rand, \amp, { |ez| q.fmods.rand = ez.value }, q.fmods.rand, false, 50, 50 );
);

