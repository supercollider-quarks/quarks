//redFrik - adapted from example by Bram de Jong, musicdsp.org

RedEfxWave : RedEffectModule {
	*def {
		^SynthDef(\redEfxWave, {|out= 0, mix= -1, amount= 25|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= dry*(dry.abs+amount)/(dry*dry+(amount-1)*dry.abs+1);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\amount: ControlSpec(0, 999, 'lin', 0, 25)
			),
			order: [
				\out -> \waveOut,
				\mix -> \waveMix,
				\amount -> \waveAmount
			]
		));
	}
}

/*

{|in, amount= 25| in*(in.abs+amount)/(in*in+(amount-1)*in.abs+1)}

*/