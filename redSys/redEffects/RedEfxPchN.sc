//redFrik - but n is hardcoded

RedEfxPchN : RedEffectModule {
	*def {
		^SynthDef(\redEfxPchN, {|out= 0, mix= -1, ratio= 4|
			var dry, wet, phasors, reads, windows;
			var windowDur= 0.1;
			var n= 3;
			dry= In.ar(out, 2);
			phasors= {|i| Phasor.ar(1, ratio*SampleDur.ir)+(i/n)%1!2}.dup(n).flop;
			reads= DelayC.ar(dry, windowDur, phasors*windowDur);
			windows= sin(phasors*pi);
			wet= [Mix((reads*windows)[0]), Mix((reads*windows)[1])];
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\ratio: ControlSpec(0.01, 10, 'exp', 0, 4)
			),
			order: [
				\out -> \pchNOut,
				\mix -> \pchNMix,
				\ratio -> \pchNRatio
			]
		));
	}
}
