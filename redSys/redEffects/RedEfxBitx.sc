//redFrik - bitcrusher

RedEfxBitx : RedEffectModule {
	*def {
		^SynthDef(\redEfxBitx, {|out= 0, mix= -1, rate= 0.5, bits= 4|
			var dry, wet, crux;
			dry= In.ar(out, 2);
			crux= (dry*bits+bits).floor-bits/bits;
			wet= Latch.ar(crux, Impulse.ar(SampleRate.ir*0.5*rate));
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\rate: ControlSpec(0, 1, 'lin', 0, 0.5),
				\bits: ControlSpec(0, 12, 'lin', 0, 4)
			),
			order: [
				\out -> \bitxOut,
				\mix -> \bitxMix,
				\rate -> \bitxRate,
				\bits -> \bitxBits
			]
		));
	}
}

/*

{|in, rate= 0.5, bits= 4| Latch.ar((in*bits+bits).floor-bits/bits, Impulse.ar(SampleRate.ir*0.5*rate))}

*/