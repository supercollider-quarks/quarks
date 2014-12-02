(
(
SynthDef(\outlet1, { |out, amp=0.1, sustain=5, freq=5000|
	var energy = EnvGen.kr(Env.perc(Rand(0.01, 0.02), sustain), doneAction:2);
	var son;
	son = Resonz.ar(WhiteNoise.ar(0.5.dup), energy.linlin(1, 0, freq, freq * 0.2), 0.4);
	Out.ar(out, son * energy)

}).share
);

(
SynthDef(\press2, { |out, amp = 0.1, p0 = 1, p1 = 0, sustain = 10, freq = 4000, ratio = 2|
	var pressure, son, map, n = 4;
	pressure = EnvGen.ar(Env.new([p0, p1.max(0.0001)], [sustain], [\exp]), doneAction: 2);
	map = { |i| 
		var a = rrand(0.0, 1.0).postln; 
		InRange.ar(pressure, a, a + 0.1) * SinOsc.ar(rand(9000, 12000), 0, 0.1)
	} ! n;
	son = Crackle.ar(pressure.linlin(1, 0, 1.32, 1.99) + [0, Rand(0, 0.001)]);
	freq = pressure.linlin(1, 0, freq * ratio, freq) * LFNoise1.kr(80,  1-pressure * 0.1, 1);
	son = Ringz.ar(son, freq, 0.01);
	son = son + map.sum;
	Out.ar(out, son * pressure * (amp * 5))
}).share;

);

(
SynthDef(\filling, {|out, amp = 0.1, p0 = 0, p1 = 1, sustain = 10, 
	freq = 300, div = 0.7, decay = 0.3|
	var son, n = 6, freqs;
	var exc = WhiteNoise.ar(0.1.dup);
	var pressure = EnvGen.kr(Env.new([p0, p1, p1], [sustain * div, sustain * (1 - div)], [\lin]));
	amp = EnvGen.kr(
		Env.new([0.001, amp, amp, 0.001], [0.01, sustain, decay], [\exp]), doneAction: 2);
	freqs = {|i| (pressure * 0.2 + 1) ** i * freq } ! n;
	son = DynKlank.ar(`[freqs, 1/n, [2 - pressure]], exc);
	// son = son * ToggleFF.kr(Dust.kr(20), 0.3, 0.5);
	Out.ar(out, son * amp);

}).share
);
)

(instrument: \outlet1, \freq: 3000, sustain: [2, 3, 4, 8], server: r.servers.choose).play;




(instrument: \press2, dur: 10, freq: 5700, server: r.servers.choose).play;
(instrument: \press2, dur: 10, freq: 700).play;
(instrument: \press2, dur: 20, freq: 700, ratio: 4).play;

(
Tdef(\x, {
	100.do { |i|
		(instrument: \press2, freq: [1, 4, 2, 3]@@i * 300, sustain: 0.1, server: r.servers.choose).play;
		(1/8).wait;
	}
}).play
)


(instrument: \filling, dur: 1, freq: rrand(400, 1600), div: 0.5, server: r.s, where: \all).play;
(instrument: \filling, dur: 40, freq: 100, div: 0.5).play;
(instrument: \filling, dur: 0.1, freq: 1700, div: 0.5).play;

//!! a shout message