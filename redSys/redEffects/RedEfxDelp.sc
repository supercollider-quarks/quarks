//redFrik - adapted from sc-users post 'FM Swear Box' by Paul Jones, sc-users 090919

RedEfxDelp : RedEffectModule {
	*def {
		^SynthDef(\redEfxDelp, {|out= 0, mix= -1, fb= 0.98, dly= 0.3, ps= 1.667, fc= 9999, rq= 1, fc2= 3000, rq2= 2|
			var dry, wet, loc, cmp, pch, wet2;
			dry= In.ar(out, 2);
			loc= LocalIn.ar(2);
			wet= DelayN.ar(dry+loc, 1, dly);
			cmp= Compander.ar(wet, wet, 0.5, 1, 0.001);
			pch= PitchShift.ar(cmp, dly, ps, 0, 0.1);
			wet2= BLowPass.ar(cmp+pch, fc2, rq2);
			wet2= LeakDC.ar(wet2);
			LocalOut.ar(wet2*fb);
			wet2= BLowPass4.ar(wet2, fc, rq);
			ReplaceOut.ar(out, XFade2.ar(dry, wet2, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\fb: ControlSpec(0, 2, 'lin', 0, 0.98),
				\dly: \delay.asSpec,
				\ps: ControlSpec(0, 5, 'lin', 0, 1.667),
				\fc: ControlSpec(20, 20000, 'exp', 0, 9999),
				\rq: ControlSpec(0.01, 4, 'exp', 0, 1),
				\fc2: ControlSpec(20, 20000, 'exp', 0, 3000),
				\rq2: ControlSpec(0.01, 4, 'exp', 0, 2)
			),
			order: [
				\out -> \delpOut,
				\mix -> \delpMix,
				\fb -> \delpFB,
				\dly -> \delpDly,
				\ps -> \delpPS,
				\fc -> \delpFC,
				\rq -> \delpRQ,
				\fc2 -> \delpFC2,
				\rq2 -> \delpRQ2
			]
		));
	}
}
/*
a= RedEfxDelp.new
b= {SoundIn.ar([0, 1])}.play
a.cvs.delpMix.input= 0.5
a.gui
*/
