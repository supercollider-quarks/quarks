//redFrik - bitcrusher adapted from adc's bit reduction example found in examples folder

RedEfxBitc : RedEffectModule {
	*def {
		^SynthDef(\redEfxBitc, {|out= 0, mix= -1, rate= 0.5, bits= 4|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= Latch.ar(dry, Impulse.ar(SampleRate.ir*0.5*rate));
			wet= wet.round(0.5**bits);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\rate: ControlSpec(0, 1, 'lin', 0, 0.5),
				\bits: ControlSpec(0, 12, 'lin', 0, 4)
			),
			order: [
				\out -> \bitcOut,
				\mix -> \bitcMix,
				\rate -> \bitcRate,
				\bits -> \bitcBits
			]
		));
	}
}

/*

{|in, rate= 0.5, bits= 4| Latch.ar(in, Impulse.ar(SampleRate.ir*0.5*rate)).round(0.5**bits)}

*/