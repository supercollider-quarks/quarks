//redFrik - adapted from Sean Costello's old-skool emulation of the Boss OC-2 pedal, sc-users 060723

RedEfxOcta : RedEffectModule {
	*def {
		^SynthDef(\redEfxOcta, {|out= 0, mix= -1, fc= 440, oct1= 1, oct2= 0|
			var dry, wet, o1, o2;
			dry= In.ar(out, 2);
			wet= LPF.ar(dry, fc);
			o1= ToggleFF.ar(wet);
			o2= ToggleFF.ar(o1);
			wet= (wet*o1*oct1)+(wet*o2*oct2);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\fc: \freq.asSpec,
				\oct1: ControlSpec(0, 1, 'lin', 0, 1),
				\oct2: ControlSpec(0, 1, 'lin', 0, 0)
			),
			order: [
				\out -> \octaOut,
				\mix -> \octaMix,
				\fc -> \octaFC,
				\oct1 -> \octaOct1,
				\oct2 -> \octaOct2
			]
		));
	}
}
