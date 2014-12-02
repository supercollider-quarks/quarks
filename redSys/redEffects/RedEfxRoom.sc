//redFrik
//todo: add more gverb parameters

RedEfxRoom : RedEffectModule {
	*def {
		^SynthDef(\redEfxRoom, {|out= 0, mix= -1, room= 50, time= 1, damp= 0.5|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= GVerb.ar(dry, room, time, damp);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\room: ControlSpec(0, 300, 'lin', 0, 50),
				\time: ControlSpec(0, 10, 'lin', 0, 1),
				\damp: ControlSpec(0, 1, 'lin', 0, 0.5)
			),
			order: [
				\out -> \roomOut,
				\mix -> \roomMix,
				\room -> \roomRoom,
				\time -> \roomTime,
				\damp -> \roomDamp
			]
		));
	}
}

/*

{|in, room= 50, time= 1, damp= 0.5| GVerb.ar(in, room, time, damp)}

*/
