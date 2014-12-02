//redFrik - adapted from jmc's tank example in LocalIn helpfile

RedEfxTank : RedEffectModule {
	*def {
		^SynthDef(\redEfxTank, {|out= 0, mix= -1, fb= 0.98, dec= 1, damp= 1|
			var dry, wet, in2;
			dry= In.ar(out, 2);
			in2= dry;
			4.do{in2= AllpassN.ar(in2, 0.02, {Rand(0.005, 0.02)}.dup, dec, damp)};
			wet= LocalIn.ar(2)*fb;
			wet= OnePole.ar(wet, 0.5);
			wet= Rotate2.ar(wet[0], wet[1], Rand(0.1, 0.25));
			wet= AllpassN.ar(wet, 0.05, {Rand(0.01, 0.05)}.dup, 2*dec, damp);
			wet= DelayN.ar(wet, 0.26, {Rand(0.1, 0.26)}.dup);
			wet= AllpassN.ar(wet, 0.05, {Rand(0.03, 0.05)}.dup, 2*dec, damp);
			//wet= LeakDC.ar(Limiter.ar(wet));
			wet= LeakDC.ar(wet);
			wet= wet+in2;
			LocalOut.ar(wet);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\fb: ControlSpec(0, 2, 'lin', 0, 0.98),
				\dec: ControlSpec(0, 10, 'lin', 0, 1),
				\damp: ControlSpec(0, 10, 'lin', 0, 1)
			),
			order: [
				\out -> \tankOut,
				\mix -> \tankMix,
				\fb -> \tankFB,
				\dec -> \tankDec,
				\damp -> \tankDamp
			]
		));
	}
}
