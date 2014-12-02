/*
a synth used to run a very simple dummy 'first run'
*/
SynthDef(\_synthbenchmark_dummyone, {
	var son, buf;
	son = SinOsc.ar;
	buf = LocalBuf(1024);
	son = son + PlayBuf.ar(1, buf);
	Out.ar(0, Pan2.ar(son));
});
