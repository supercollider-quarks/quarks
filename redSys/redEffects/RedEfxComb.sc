//redFrik

RedEfxComb : RedEffectModule {
	*def {
		^SynthDef(\redEfxComb, {|out= 0, mix= -1, dly= 0.3, dec= 1|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= CombN.ar(dry, 1, dly.min(1), dec);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\dly: \delay.asSpec,
				\dec: ControlSpec(0, 10, 'lin', 0, 1)
			),
			order: [
				\out -> \combOut,
				\mix -> \combMix,
				\dly -> \combDly,
				\dec -> \combDec
			]
		));
	}
}

/*

{|in, dly= 0.3, dec= 1| CombN.ar(in, 1, dly.min(1), dec)}

*/