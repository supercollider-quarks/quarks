//redFrik

RedMixMono : RedAbstractMix {
	*def {
		^SynthDef(\redMixMono, {|inA= 0, inB= 2, out= 0, mix= 0, amp= 1, lag= 0.05|
			var a= In.ar(inA, 1);	//mono
			var b= In.ar(inB, 1);	//mono
			var z= XFade2.ar(a, b, Ramp.kr(mix, lag), Ramp.kr(amp, lag));
			ReplaceOut.ar(out, z.dup);
		}, metadata: (
			specs: (
				\inA: \audiobus.asSpec,
				\inB: \audiobus.asSpec.copy.default_(2),
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\amp: ControlSpec(0, 1, 'lin', 0, 1),	//db here later???
				\lag: ControlSpec(0, 99, 'lin', 0, 0.05)
			),
			info: (
				\inA: \mono,
				\inB: \mono
			)
		));
	}
}
