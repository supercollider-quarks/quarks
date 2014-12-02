SynthDef(
	\many_pulse_1,
{
var freqs = 300.collect{|n| n + 1.0.rand } * 20;
var oscs  = freqs.collect{|f| Pulse.ar(freq: f, width: 1.0.rand) };
Out.ar(0, oscs.mean)
})
