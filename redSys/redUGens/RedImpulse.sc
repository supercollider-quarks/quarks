//redFrik

//resettable impulse train

RedImpulse {
	*ar {|freq= 440, reset= 0, mul= 1, add= 0|
		var osc= Phasor.ar(reset, freq/SampleRate.ir);
		^MulAdd((osc-Delay1.ar(osc))<0+Impulse.ar(0), mul, add);
	}
	*kr {|freq= 440, reset= 0, mul= 1, add= 0|
		var osc= Phasor.kr(reset, freq/ControlRate.ir);
		^MulAdd((osc-Delay1.kr(osc))<0+Impulse.kr(0), mul, add);
	}
}
