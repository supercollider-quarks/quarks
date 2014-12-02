SynthDef(
	\many_saw_1,
{
var freqs = 700.collect{|n| n + 1.0.rand } * 10;
var oscs  = freqs.collect{|f| Saw.ar(freq: f) };
Out.ar(0, oscs.mean)
})
