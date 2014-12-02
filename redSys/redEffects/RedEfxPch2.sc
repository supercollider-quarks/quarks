//redFrik - only two overlapping windows

RedEfxPch2 : RedEffectModule {
	*def {
		^SynthDef(\redEfxPch2, {|out= 0, mix= -1, ratio= 4|
			var dry, wet, phasor1, phasor2, read1, read2, window1, window2;
			var windowDur= 0.1;
			dry= In.ar(out, 2);
			phasor1= Phasor.ar(1, ratio*SampleDur.ir);
			phasor2= phasor1+0.5%1;
			read1= DelayC.ar(dry, windowDur, phasor1*windowDur);
			read2= DelayC.ar(dry, windowDur, phasor2*windowDur);
			window1= sin(phasor1*pi);
			window2= sin(phasor2*pi);
			wet= (read1*window1)+(read2*window2);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\ratio: ControlSpec(0.01, 10, 'exp', 0, 4)
			),
			order: [
				\out -> \pch2Out,
				\mix -> \pch2Mix,
				\ratio -> \pch2Ratio
			]
		));
	}
}
