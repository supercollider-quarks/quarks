//redFrik - freq is target frequency
//maybe a slope limiter intead of lag?

RedEfxAuto : RedEffectModule {
	*def {
		^SynthDef(\redEfxAuto, {|out= 0, mix= -1, freq= 400, lag= 0.01|
			var dry, wet, phasor1, phasor2, read1, read2, window1, window2, pitch, ratio;
			var windowDur= 0.1;
			dry= In.ar(out, 2);
			pitch= Pitch.kr(dry, freq).flop[0].lag(lag);
			ratio= (1-(freq/pitch))/windowDur;
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
				\freq: \freq.asSpec,
				\lag: ControlSpec(0, 4, 'lin', 0, 0.01)
			),
			order: [
				\out -> \autoOut,
				\mix -> \autoMix,
				\freq -> \autoFreq,
				\lag -> \autoLag
			]
		));
	}
}
