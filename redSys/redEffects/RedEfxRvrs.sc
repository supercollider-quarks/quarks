//redFrik - buffer duration hardcoded.  also work as pitch shifter if rate is positive

RedEfxRvrs : RedEffectModule {
	*def {
		^SynthDef(\redEfxRvrs, {|out= 0, mix= -1, rate= -1|
			var dry, wet;
			var bufferDur= 8;			//in seconds
			var buffer= LocalBuf(SampleRate.ir*bufferDur, 2);
			dry= In.ar(out, 2);
			BufWr.ar(dry, buffer, Phasor.ar(1, 1, 0, BufFrames.ir(buffer)));
			wet= PlayBuf.ar(2, buffer, rate, loop:1);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\rate: ControlSpec(-8, 8, 'lin', 0, -1)
			),
			order: [
				\out -> \rvrsOut,
				\mix -> \rvrsMix,
				\rate -> \rvrsRate
			]
		));
	}
}
