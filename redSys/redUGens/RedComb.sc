//redFrik

//a comb that crossfades when delaytime is changed

//--related:
//RedDelay

RedComb {
	*ar {|in= 0, maxdelaytime= 0.2, delaytime= 0.2, decaytime= 1, lagTime= 0.1, mul= 1, add= 0|
		var t= HPZ2.ar(delaytime);
		var d= Latch.ar(delaytime, PulseDivider.ar(t, 2, #[0, 1]));
		var z= CombN.ar(in, maxdelaytime, d, decaytime, mul, add);
		^XFade2.ar(z[0], z[1], Ramp.ar(ToggleFF.ar(t), lagTime, 2, -1));
	}
	*kr {|in= 0, maxdelaytime= 0.2, delaytime= 0.2, decaytime= 1, lagTime= 0.1, mul= 1, add= 0|
		var t= HPZ2.kr(delaytime);
		var d= Latch.kr(delaytime, PulseDivider.kr(t, 2, #[0, 1]));
		var z= CombN.kr(in, maxdelaytime, d, decaytime, mul, add);
		^XFade2.kr(z[0], z[1], Ramp.kr(ToggleFF.kr(t), lagTime, 2, -1));
	}
}
