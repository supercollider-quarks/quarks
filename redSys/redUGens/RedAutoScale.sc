//redFrik

RedAutoScale {
	
	//--lang
	var <>lo, <>hi, <>inLo, <>inHi, <>min, <>max;
	*new {|lo= 0, hi= 1, inLo= inf, inHi= -inf|
		^super.newCopyArgs(lo, hi, inLo, inHi).reset;
	}
	reset {
		min= inLo;
		max= inHi;
	}
	autoScale {|val|
		if(val<min, {min= val}, {if(val>max, {max= val})});
		^val-min/(max-min)*(hi-lo)+lo
	}
	
	//--ugen
	*ar {|in, lo= 0, hi= 1, inLo= inf, inHi= -inf, reset= 0|
		var min, max;
		min= RunningMin.ar(in.min(inLo), reset);
		max= RunningMax.ar(in.max(inHi), reset);
		^in-min/(max-min)*(hi-lo)+lo
	}
	*kr {|in, lo= 0, hi= 1, inLo= inf, inHi= -inf, reset= 0|
		var min, max;
		min= RunningMin.kr(in.min(inLo), reset);
		max= RunningMax.kr(in.max(inHi), reset);
		^in-min/(max-min)*(hi-lo)+lo
	}
}

/*
//with slow adapt - TODO
RedAutoScale2 {
	*ar {|in, lo= 0, hi= 1, adaptLo= 0.4, adaptHi= 0.6|
		var min, max;
		min= Decay.kr(RunningMin.ar(in, reset);
		max= Decay.kr(RunningMax.ar(in, reset);
		^LinLin.ar(in-min/(max-min).max(0.0001), 0, 1, lo, hi);
	}
	*kr {|in, lo= 0, hi= 1, reset= 0|
		var min, max;
		min= RunningMin.kr(in, reset);
		max= RunningMax.kr(in, reset);
		^LinLin.kr(in-min/(max-min).max(0.0001), 0, 1, lo, hi);
	}
}
*/