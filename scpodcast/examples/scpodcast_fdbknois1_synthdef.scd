/*
a SynthDef used as an example of scpodcast. by Dan Stowell.
it takes some dust as a source and simply pushes it through 
a bank of drifting delays, with some distortion and a bit of postfx.
*/
SynthDef(\scpodcast_fdbknois1, { |fdbk=1, amp=1|
var son, ekko, slewrate;

son = Dust2.ar(1, 0.2) * LFTri.ar(0.01, 1) - LocalIn.ar(1);// + PinkNoise.ar(1);

// Add a whole set of drifting echoes
ekko = {Pan2.ar(
			softclip(LeakDC.ar(DelayC.ar(son, 2, LFNoise1.kr(0.01).range(0.01, 2))) * 5 * fdbk), 
		LFNoise1.kr(0.01).range(-1, 1))}.dup(10);

son = ekko.mean;

// Feedback loop tap from here
LocalOut.ar(son.sum);

// A little filtering so as not to knacker ears
son = LPF.ar(son, 5000, 0.9) + (son * 0.1);
//slewrate = MouseX.kr(200, 2000);
son = Slew.ar(son, 700, 700);

son = LeakDC.ar(son);

Out.ar(0, son * amp);

}, [1, 10]).load(s);
/*
s.boot;
x = Synth(\scpodcast_fdbknois1);
x.set(\fdbk, 0.42);
x.set(\fdbk, 1);
x.set(\amp, 0);
x.set(\amp, 1);
x.free;
*/