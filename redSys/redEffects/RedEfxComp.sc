//redFrik
//todo: make into stereo linked

RedEfxComp : RedEffectModule {
	*def {
		^SynthDef(\redEfxComp, {|out= 0, mix= -1, amp= 1, thresh= 0.5, ratio= 1|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= CompanderD.ar(ratio*0.5*dry, thresh, 1, ratio.reciprocal, 0.01, 0.01, amp);
			ReplaceOut.ar(out, XFade2.ar(dry, wet.clip2, mix));
		}, metadata: (
			specs: (			//all these - fix better compressor
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\amp: ControlSpec(0, 1, 'lin', 0, 1),
				\thresh: ControlSpec(0, 1, 'lin', 0, 0.5),
				\ratio: ControlSpec(0, 1.5, 'lin', 0, 1)
			),
			order: [
				\out -> \compOut,
				\mix -> \compMix,
				\amp -> \compAmp,
				\thresh -> \compThresh,
				\ratio -> \compRatio
			]
		));
	}
}
