
// methods for estimating number of events in a pattern

// most base patterns are infinite
+ Pattern {
	estimateLength { ^inf }
}

// some have a length argument
+ Pseries {
	estimateLength { ^length }
}

+ Pgeom {
	estimateLength { ^length }
}

+ Pbrown {
	estimateLength { ^length }
}

+ Pwhite {
	estimateLength { ^length }
}

+ Paccum {
	estimateLength { ^length }
}

// list patterns generally have a repeat argument
// we'll assume most of them return "repeats" events
+ ListPattern {
	estimateLength { ^repeats }
	
	estimateLengthOfList { arg argList;
		var len = 0, itemLen;
		argList = argList ? list;
		argList.do({ |item|
			((len === inf).not and: { ((itemLen = item.estimateLength) === inf).not }).if({
				len = len + itemLen;
			}, {
				len = inf
			});
		});
		^len
	}
}

// Pseq and Pshuf repeat the whole list
+ Pseq {
	estimateLength {
		var listLen;
		(repeats === inf).if({ ^inf }, {
			((listLen = this.estimateLengthOfList) === inf).if({ ^inf },
				{ ^repeats * listLen })
		})
	}
}

+ Pshuf {
	estimateLength {
		var listLen;
		(repeats === inf).if({ ^inf }, {
			((listLen = this.estimateLengthOfList) === inf).if({ ^inf },
				{ ^repeats * listLen })
		})
	}
}

// not exact -- works if there are no embedded patterns within the arrays being laced
// otherwise the estimate will be too low
// should be adequate for my purposes
+ Place {
	estimateLength {
		var listLen;
		(repeats === inf).if({ ^inf }, {
			((listLen = this.estimateLengthOfList) === inf).if({ ^inf },
				{ ^repeats * listLen })
		})
	}
}

// this one will also be low if there are embedded patterns
// maybe I'll deal with it later
+ Pslide {
	estimateLength {
		(repeats === inf).if({ ^inf },
			{ (len === inf).if({ ^inf },
				{ ^repeats * len });
		});
	}
}

// again... too low
+ Pwalk {
	estimateLength { ^stepPattern.estimateLength }
}

// this is not exact -- the only way to calculate precisely is to run through the "which"
// pattern, which is an unbounded operation
+ Pswitch {
	estimateLength {
		var listLen, whichLen;
		listLen = this.estimateLengthOfList;
		whichLen = this.estimateLengthOfList(which);
		((listLen === inf) or: (whichLen === inf)).if({
			^inf
		}, {
			^listLen * whichLen
		});
	}
}

// filter patterns
+ FilterPattern {
	estimateLength { ^pattern.estimateLength }
}

+ Pn {
	estimateLength {
		var patLen;
		((repeats === inf).not and: { ((patLen = pattern.estimateLength) === inf).not }).if({
			^repeats * patLen
		}, {
			^inf
		});
	}
}

+ Pfin {
	estimateLength { ^count }
}

+ Pfuncn {
	estimateLength { ^repeats }
}

// no good way to estimate # of events for Pfindur, Pstutter, a few others
// maybe I'll look at them later

// support for other objects
+ Object {
	estimateLength { ^1 }
}

// not related, but ok here
+ Pattern {
	interpolate { |times = 1.0, curves = \lin|
		^Pseg(this, times, curves)
	}
}
