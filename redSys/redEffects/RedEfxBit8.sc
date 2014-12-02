//redFrik - bitcrusher with ringmodulation

RedEfxBit8 : RedEffectModule {
	*def {
		^SynthDef(\redEfxBit8, {|out= 0, mix= -1, rate= 0.5, bits= 4, freq= 150|
			var dry, wet, ring, crux;
			dry= In.ar(out, 2);
			ring= dry*FSinOsc.ar(freq);
			crux= (ring*bits+bits).floor-bits/bits;
			wet= Latch.ar(crux, Impulse.ar(SampleRate.ir*0.5*rate));
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\rate: ControlSpec(0, 1, 'lin', 0, 0.5),
				\bits: ControlSpec(0, 12, 'lin', 0, 4),
				\freq: ControlSpec(20, 20000, 'exp', 0, 150)
			),
			order: [
				\out -> \bit8Out,
				\mix -> \bit8Mix,
				\rate -> \bit8Rate,
				\bits -> \bit8Bits,
				\freq -> \bit8Freq
			]
		));
	}
}

/*

{|in, rate= 0.5, bits= 4, freq= 150| Latch.ar((in*FSinOsc.ar(freq)*bits+bits).floor-bits/bits, Impulse.ar(SampleRate.ir*0.5*rate))}

*/