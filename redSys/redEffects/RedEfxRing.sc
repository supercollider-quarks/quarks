//redFrik

RedEfxRing : RedEffectModule {
	*def {
		^SynthDef(\redEfxRing, {|out= 0, mix= -1, freq= 440, mul= 1, rate= 6, det= 0|
			var dry, wet, lfo;
			dry= In.ar(out, 2);
			lfo= SinOsc.ar(rate, 0, mul);
			wet= dry*SinOsc.ar(freq*[1, 2.pow(det)]+lfo, 0, 1);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\freq: \freq.asSpec,
				\mul: ControlSpec(0, 100, 'lin', 0, 1),
				\rate: \lofreq.asSpec,
				\det: ControlSpec(-2, 2, \lin, 0, 0)
			),
			order: [
				\out -> \ringOut,
				\mix -> \ringMix,
				\freq -> \ringFreq,
				\mul -> \ringMul,
				\rate -> \ringRate,
				\det -> \ringDet
			]
		));
	}
}

/*

{|in, freq= 440, mul= 1, rate= 6, det= 0| in*SinOsc.ar(freq*[1, 2.pow(det)]+SinOsc.ar(rate, 0, mul))}

*/