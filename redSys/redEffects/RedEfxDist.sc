//redFrik - adapted from example in Tour_of_UGens helpfile

RedEfxDist : RedEffectModule {
	*def {
		^SynthDef(\redEfxDist, {|out= 0, mix= -1, depth= 0.8, freq= 0.3|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= SinOsc.ar(freq, dry*(1+(depth*(8pi-1))))*0.25;
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\depth: ControlSpec(0, 1, 'lin', 0, 0.8),
				\freq: ControlSpec(0.001, 1000, 'exp', 0, 0.3)
			),
			order: [
				\out -> \distOut,
				\mix -> \distMix,
				\depth -> \distDepth,
				\freq -> \distFreq
			]
		));
	}
}
