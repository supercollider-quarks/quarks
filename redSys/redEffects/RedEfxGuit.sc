//redFrik - adapted from sc-users post 'Saturation' by Batuhan Bozkurt, sc-users 090701

RedEfxGuit : RedEffectModule {
	*def {
		^SynthDef(\redEfxGuit, {|out= 0, mix= -1, amount= 0.5, fc= 3500, center= 120, rq= 0.707|
			var dry, wet, k;
			dry= In.ar(out, 2);
			k= 2*amount/(1-amount);
			wet= ((1+k)*dry)/(1+(k*dry.abs));
			wet= MidEQ.ar((LPF.ar(wet, fc*#[1, 1.1])*0.5), center, rq, 8);
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\amount: ControlSpec(0, 0.9999999, 'lin', 0, 0.5),
				\fc: ControlSpec(20, 20000, 'exp', 0, 3500),
				\center: ControlSpec(40, 1000, 'exp', 0, 120),
				\rq: ControlSpec(0.01, 15, 'exp', 0, 0.707)
			),
			order: [
				\out -> \guitOut,
				\mix -> \guitMix,
				\amount -> \guitAmount,
				\fc -> \guitFC,
				\center -> \guitCenter,
				\rq -> \guitRQ
			]
		));
	}
}

/*

{|in, amount= 0.5, fc= 3500, center= 120, rq= 0.707| var k= 2*amount/(1-amount); var wet= ((1+k)*in)/(1+(k*in.abs)); wet= MidEQ.ar((LPF.ar(wet, fc*#[1, 1.1])*0.5), center, rq, 8)}

*/