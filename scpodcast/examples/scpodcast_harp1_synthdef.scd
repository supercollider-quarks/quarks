(
var numsimult = 25; // 15 is OK for monophonic, but not quite enough for stereo perception, esp during rapid sweeps
SynthDef(\scpodcast_harp1, 
{ |amp=0.7, pan=0, pluckratefactor=1|
var trig, index, indextrigs, indexfreqs, freq, oscs, freqquant, x, y, z, sweeptrig, sweep, panpos;


// Add upwards sweep occasionally
sweeptrig = Dust.kr(0.01);
sweep = EnvGen.kr(Env.new([1,4,1], [3, 0.0001]), sweeptrig);


trig = Dust.kr(pluckratefactor * (LFNoise2.kr(ExpRand(0.1, 0.4)).range(1, LFNoise2.kr(ExpRand(0.02, 0.08)).exprange(0.3, 20)) 
	*
	// Boost the amount of triggers when we're doing a "sweep"
	(EnvGen.kr(Env.new([1,Rand(2,5),1], [3, 0.0001]), sweeptrig)) 
));

# x,y,z = RosslerL.ar(ExpRand(0.1, 0.4)); //(MouseX.kr(0.1, 10));


freqquant = A2K.kr(x).exprange(0.01, 1).max(0.01).min(2); //.poll(1, "freqquant");

freq = LFNoise2.kr(ExpRand(3,7)).round(freqquant).exprange(400, 1000);
// Modify octave using a Rossler param
freq = freq * (A2K.kr(y).exprange(1, 2) * 0.5).max(0.5).min(3);//.poll(1, "freqmul");

freq = freq * sweep;

// Here we convert single input to polyphonic
index = Stepper.kr(trig, 0, 0, numsimult);
indextrigs = ((index - (0..numsimult)).abs < 1) * trig;
indexfreqs = Latch.kr(freq, indextrigs);

// Karplus-Strong string(s)
oscs = Pluck.ar(HPF.ar(PinkNoise.ar, 500), indextrigs, 0.1, indexfreqs.reciprocal,
		5, coef:SinOsc.kr(0.1).range(0.3, 0.4));

// Pan each string a little according to pitch
panpos = indexfreqs.collect(_.explin(100, 3000, -0.5, 0.5));
//indexfreqs.poll(10, "indexfreqs");
//panpos.poll(10, "panpos");
oscs = Pan2.ar(oscs, panpos);

//Out.kr(0, indextrigs);
//Out.ar(0, Pan2.ar(LeakDC.ar(oscs.mean) * 20 * amp, pan));
Out.ar(0, LeakDC.ar([oscs.flop[0].mean, oscs.flop[1].mean]) * 20 * amp);
}, [4, 0.5, 10]).load(s);
)
/*
x = Synth(\scpodcast_harp1);

x.set(\amp, 0);
x.set(\amp, 0.7);
x.set(\amp, 0.9);
x.set(\pan, 0.7);
x.set(\pan, -0.7);
x.set(\pan, 0);
x.set(\pluckratefactor, 0);
x.set(\pluckratefactor, 0.1);
x.set(\pluckratefactor, 1);
x.set(\pluckratefactor, 3);

x.free;
*/