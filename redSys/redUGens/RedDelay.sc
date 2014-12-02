//redFrik

//a delay that crossfades when delaytime is changed

//--related:
//RedComb

RedDelay {
	*ar {|in= 0, maxdelaytime= 0.2, delaytime= 0.2, lagTime= 0.1, mul= 1, add= 0|
		var t= HPZ2.ar(delaytime);
		var d= Latch.ar(delaytime, PulseDivider.ar(t, 2, #[0, 1]));
		var z= DelayN.ar(in, maxdelaytime, d, mul, add);
		^XFade2.ar(z[0], z[1], Ramp.ar(ToggleFF.ar(t), lagTime, 2, -1));
	}
	*kr {|in= 0, maxdelaytime= 0.2, delaytime= 0.2, lagTime= 0.1, mul= 1, add= 0|
		var t= HPZ2.kr(delaytime);
		var d= Latch.kr(delaytime, PulseDivider.kr(t, 2, #[0, 1]));
		var z= DelayN.kr(in, maxdelaytime, d, mul, add);
		^XFade2.kr(z[0], z[1], Ramp.kr(ToggleFF.kr(t), lagTime, 2, -1));
	}
}
