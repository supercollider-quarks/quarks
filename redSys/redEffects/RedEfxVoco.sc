//redFrik - with built in sawtooth and whitenoise consonant xfader

//phase problem when xfading? - keep either full on or full off

RedEfxVoco : RedEffectModule {
	*def {
		^SynthDef(\redEfxVoco, {|out= 0, mix= -1, freq= 400, cons= 1|
			var dry, wet, exc, chainA0, chainA1, chainB, x;
			dry= In.ar(out, 2);
			x= (1-Pitch.kr(Mix(dry), clar:1)[1])*cons;
			exc= SelectX.ar(x, [LFSaw.ar(freq), WhiteNoise.ar]);
			chainA0= FFT(LocalBuf(2048), dry[0]);
			chainA1= FFT(LocalBuf(2048), dry[1]);
			chainB= FFT(LocalBuf(2048), exc);
			wet= IFFT([PV_MagMul(chainA0, chainB), PV_MagMul(chainA1, chainB)])*0.1;
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\freq: \freq.asSpec,
				\cons: ControlSpec(0, 1, 'lin', 0, 1)
			),
			order: [
				\out -> \vocoOut,
				\mix -> \vocoMix,
				\freq -> \vocoFreq,
				\cons -> \vocoCons
			]
		));
	}
}
