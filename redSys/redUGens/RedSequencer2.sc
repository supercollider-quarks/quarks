//redFrik

RedSequencer2 {
	*ar {|array, dur, reset= 0|
		^Duty.ar(dur, reset, Dseq(array, inf));
	}
	
	*kr {|array, dur, reset= 0|
		^Duty.kr(dur, reset, Dseq(array, inf));
	}
}
