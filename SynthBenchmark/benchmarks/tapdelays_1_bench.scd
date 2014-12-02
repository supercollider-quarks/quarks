SynthDef(
	\tapdelays_1,
{
	var src = WhiteNoise.ar;
	Out.ar(0, 250.collect{|i|
		var buf = LocalBuf(SampleRate.ir * 6);
		var delay = Rand(0.01, 3.0);

		var phase = DelTapWr.ar(buf, src);
		var tap =   DelTapRd.ar(buf, phase, delay,
			[1, 2, 4].wrapAt(i),
			1.0
		);
		tap
	}.mean)
})
