//redFrik

RedEfxBoom : RedEffectModule {
	*def {
		^SynthDef(\redEfxBoom, {|out= 0, mix= -1, gain= 1|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= DelayN.ar(dry, 0.283, 0.283, gain);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\gain: ControlSpec(0, 10, 'lin', 0, 1)
			),
			order: [
				\out -> \boomOut,
				\mix -> \boomMix,
				\gain -> \boomGain
			]
		));
	}
}

/*

//http://www.youtube.com/watch?v=m5S3_dmj8BU
Ndef(\boom).play
Ndef(\boom,
{|dec= 2| var in= SoundIn.ar; DelayN.ar(in, 0.283, 0.283, dec, in*0.5)}
)
*/