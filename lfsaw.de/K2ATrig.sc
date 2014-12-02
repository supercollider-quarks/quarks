K2ATrig {
	*ar {|trig|
		^(Trig.ar(K2A.ar(trig)>0, (SampleRate.ir).reciprocal) * Latch.ar(trig, trig))
	}
}


K2ATrig1 {
	*ar {|trig|
		^(Trig.ar(K2A.ar(trig)>0, (SampleRate.ir).reciprocal))
	}
}
