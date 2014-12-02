//redFrik

//resettable phasor with a per period rate control

RedRamp {
	*ar {|dur= 1, reset= 0, mul= 1, add= 0|
		var phasor= EnvGen.ar(Env(#[0, 1], [dur]).circle, 1-Trig1.kr(reset, ControlDur.ir));
		^MulAdd(phasor, mul, add);
	}
	*kr {|dur= 1, reset= 0, mul= 1, add= 0|
		var phasor= EnvGen.kr(Env(#[0, 1], [dur]).circle, 1-Trig1.kr(reset, ControlDur.ir));
		^MulAdd(phasor, mul, add);
	}
}
