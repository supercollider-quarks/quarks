//redFrik

RedSequencer {
	*ar {|array, trig, reset= 0|
		^Demand.ar(trig, reset, Dseq(array, inf));
	}
	*kr {|array, trig, reset= 0|
		^Demand.kr(trig, reset, Dseq(array, inf));
	}
}
