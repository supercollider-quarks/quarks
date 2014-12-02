//redFrik - adapted from jmc's tape example in LocalIn helpfile

RedEfxTape : RedEffectModule {
	*def {
		^SynthDef(\redEfxTape, {|out= 0, mix= -1, fb= 1, ff= 1.25, thresh= 0.02, rate= 0.25|
			var dry, wet, in2;
			dry= In.ar(out, 2);
			in2= dry*(Amplitude.kr(Mix(dry))>thresh);
			wet= LocalIn.ar(2)*fb;
			wet= OnePole.ar(OnePole.ar(wet, 0.4), -0.08);
			wet= Rotate2.ar(wet[0], wet[1], Rand(0.1, 0.25));
			wet= DelayL.ar(wet, 2, rate.min(2));
			wet= LeakDC.ar(wet);
			wet= ((wet+dry)*ff).softclip;
			LocalOut.ar(wet);
			ReplaceOut.ar(out, XFade2.ar(dry, wet*0.1, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\fb: ControlSpec(0, 2, 'lin', 0, 1),
				\ff: ControlSpec(0, 2, 'lin', 0, 1.25),
				\thresh: ControlSpec(0, 1, 'lin', 0, 0.02),
				\rate: ControlSpec(0, 2, 'lin', 0, 0.25)
			),
			order: [
				\out -> \tapeOut,
				\mix -> \tapeMix,
				\fb -> \tapeFB,
				\ff -> \tapeFF,
				\thresh -> \tapeThresh,
				\rate -> \tapeRate
			]
		));
	}
}
