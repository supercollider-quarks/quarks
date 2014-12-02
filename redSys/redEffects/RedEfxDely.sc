//redFrik

RedEfxDely : RedEffectModule {
	*def {
		^SynthDef(\redEfxDely, {|out= 0, mix= -1, del= 1|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= DelayC.ar(dry, 5, del);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\del: ControlSpec(0, 5, 'lin', 0, 1)
			),
			order: [
				\out -> \delyOut,
				\mix -> \delyMix,
				\del -> \delyDel
			]
		));
	}
}
