//redFrik

RedEfxKomp : RedEffectModule {
	*def {
		^SynthDef(\redEfxKomp, {|out= 0, mix= -1, preGain= 1, postGain= 1, thresh= 0.1, ratio= 5, atk= 0.05, rel= 0.05|
			var dry, wet, ana, flg, cmp;
			dry= In.ar(out, 2)*preGain;
			ana= Amplitude.ar(dry, atk, rel);
			flg= ana>thresh;
			cmp= (((flg*ana)-(flg*thresh))*ratio).max(0);
			wet= dry*(1-cmp);
			ReplaceOut.ar(out, XFade2.ar(dry, wet*postGain, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\preGain: ControlSpec(0, 4, 'lin', 0, 1),
				\postGain: ControlSpec(0, 4, 'lin', 0, 1),
				\thresh: ControlSpec(0, 1, 'lin', 0, 0.1),
				\ratio: ControlSpec(0, 100, 'lin', 0, 5),
				\atk: ControlSpec(0, 5, 'lin', 0, 0.05),
				\rel: ControlSpec(0, 5, 'lin', 0, 0.05)
			),
			order: [
				\out -> \kompOut,
				\mix -> \kompMix,
				\preGain -> \kompPreGain,
				\postGain -> \kompPostGain,
				\thresh -> \kompThresh,
				\ratio -> \kompRatio,
				\atk -> \kompAtk,
				\rel -> \kompRel
			]
		));
	}
}
