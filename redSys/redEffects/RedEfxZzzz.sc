//redFrik - should be either full on or off (mix -1 or mix 1)

RedEfxZzzz : RedEffectModule {
	*def {
		^SynthDef(\redEfxZzzz, {|out= 0, mix= -1, vol= 0|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= dry*vol.dbamp;
			wet= Limiter.ar(
				LeakDC.ar(
					
					//--this from StageLimiter by Batuhan Bozkurt
					Select.ar(CheckBadValues.ar(wet, 0, 2), [wet, DC.ar(0), DC.ar(0), wet])
				)
			);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 1, -1),
				\vol: ControlSpec(-inf, 12, 'db', 0, 0)
			),
			order: [
				\out -> \zzzzOut,
				\mix -> \zzzzMix,
				\vol -> \zzzzVol
			]
		));
	}
}

/*

{|in, vol= 0| in= in*vol.dbamp; Limiter.ar(LeakDC.ar(Select.ar(CheckBadValues.ar(in), [in, DC.ar(0), DC.ar(0), in])))}

*/