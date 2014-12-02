SynthDef(
	\tapdelays_3_1,
{
	var src = WhiteNoise.ar;
	var buf = LocalBuf(SampleRate.ir * 6);
	var phase = DelTapWr.ar(buf, src);
	Out.ar(0, 250.collect{|i|
		var delay = Rand(0.01, 3.0);
		var tap =   DelTapRd.ar(buf, phase, delay,
			1,
			1.0
		);
		tap
	}.mean)
})
