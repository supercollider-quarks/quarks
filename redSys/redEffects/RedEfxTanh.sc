//redFrik

RedEfxTanh : RedEffectModule {
	*def {
		^SynthDef(\redEfxTanh, {|out= 0, mix= -1, mul= 1, add= 0.05|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= (dry*mul+add).tanh;
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\mul: ControlSpec(0, 999, 'lin', 0, 25),
				\add: ControlSpec(0, 1, 'lin', 0, 0.05)
				
			),
			order: [
				\out -> \tanhOut,
				\mix -> \tanhMix,
				\mul -> \tanhMul,
				\add -> \tanhAdd
			]
		));
	}
}
