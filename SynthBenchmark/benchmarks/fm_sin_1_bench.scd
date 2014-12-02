SynthDef(
	\fm_sin_1,
{
var freqs = 250.collect{|n| SinOsc.ar(n + 1.0.rand).range(rrand(10, 10000), rrand(10, 10000)) } * 10;
var oscs  = freqs.collect{|f| SinOsc.ar(freq: f) };
Out.ar(0, oscs.mean)
})
