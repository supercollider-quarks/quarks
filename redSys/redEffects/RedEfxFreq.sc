//redFrik - frequency shifter

RedEfxFreq : RedEffectModule {
	*def {
		^SynthDef(\redEfxFreq, {|out= 0, mix= -1, shift= -50|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= FreqShift.ar(dry, shift);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\shift: ControlSpec(-1000, 1000, 'lin', 0, -50)
			),
			order: [
				\out -> \freqOut,
				\mix -> \freqMix,
				\shift -> \freqShift
			]
		));
	}
}
