RedFrik {
	*new {|year|
		Server.default.waitForBoot{{RedFrik.ar(year ? 2008)}.play}
	}
	*ar {|year= 2008|
		^GlitchRHPF.ar(
			BrownNoise.ar(1.dup),
			year.fold(0.3, 3.1),
			FSinOsc.kr(FSinOsc.kr(year%3.1,0,year%1.51,0.5),year.reciprocal.fold(0.3,0.61),year.reciprocal.wrap(0.3,0.51))
		);
	}
}
/*
RedFrik.new(2008)
{RedFrik.ar(2008)}.play
*/
