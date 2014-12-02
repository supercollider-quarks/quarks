// example for granulating a soundfile using gui built in MaxMSP.
// see attached file 'oo_soundfiles.maxpat'.
// SuperCollider listens on port 57120, so messages must be sent to this port.

(
s.waitForBoot{
	var func, buffers, synth;
	SynthDef(\granulate, {|buf, out= 0, gate= 1,
		vol= 0,//overal volume in db (0 = unity gain)
		atk= 0.01, rel= 1,//cross fade times when changing buffer
		posMin= 0, posMax= 1, posRate= 1,//buffer offset boundaries and rate of chage
		panMin= -1, panMax= 1, panRate= 1,//panning boundaries and rate of change
		pchMin= 0.9, pchMax= 1.1, pchRate= 1,//playback speed boundaries and rate of chage
		trgMin= 0.005, trgMax= 0.5, trgRate= 1,//trigger density
		durMin= 0.005, durMax= 0.5, durRate= 1//duration boundaries and rate of change
	|
		var env= EnvGen.kr(Env.asr(atk, 1, rel), gate, doneAction:2);
		var pos= LFDNoise1.kr(posRate).range(posMin, posMax);
		var pan= LFDNoise1.kr(panRate).range(panMin, panMax);
		var pch= LFDNoise1.kr(pchRate).range(pchMin, pchMax);
		var trg= Impulse.kr(1/LFDNoise1.kr(trgRate).exprange(trgMin.max(0.001), trgMax.max(0.001)));
		var dur= LFDNoise1.kr(durRate).exprange(durMin.max(0.001), durMax);
		var snd= GrainBuf.ar(1, trg, dur, buf, pch, pos, 4);
		Out.ar(out, Pan2.ar(snd, pan, env*vol.dbamp));
	}).add;
	s.sync;
	buffers= List.newClear(5);//hold buffers and free later
	OpenObject.start;
	func= {|path, releaseTime= 1|
		var b;
		if(File.exists(path), {
			buffers.pop.free;//free previous
			("loading file"+path.asString.quote).postln;
			Routine.run({
				b= Buffer.readChannel(s, path, channels:[0]);
				buffers.addFirst(b);
				s.sync;
				synth.release;//stop and fade out previous
				synth= Synth(\granulate, [\buf, b, \rel, releaseTime]);
				s.sync;
				OpenObject.put(\synth, synth);//oo access
			});
		}, {
			("file"+path.asString.quote+"not found").warn;
		});
	};
	OpenObject.put(\func, func);//oo access
};
)
