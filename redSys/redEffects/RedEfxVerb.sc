//redFrik

RedEfxVerb : RedEffectModule {
	*def {
		^SynthDef(\redEfxVerb, {|out= 0, mix= -1, room= 0.5, damp= 0.5|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= FreeVerb2.ar(dry[0], dry[1], mix*0.5+0.5, room, damp);
			ReplaceOut.ar(out, wet);
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\room: ControlSpec(0, 1, 'lin', 0, 0.5),
				\damp: ControlSpec(0, 1, 'lin', 0, 0.5)
			),
			order: [
				\out -> \verbOut,
				\mix -> \verbMix,
				\room -> \verbRoom,
				\damp -> \verbDamp
			]
		));
	}
}
