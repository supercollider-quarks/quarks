//redFrik

//--related:
//RedStereo2

RedStereo {
	*ar {|in= 0, rate= 0.2, depth= 0.1|
		var bpfs= MidEQ.ar(in, {LFNoise1.kr(rate, 3000, 3040)}.dup(4), 0.25, 8);
		var dels= DelayC.ar(bpfs, 0.0075, {LFNoise1.kr(rate, 0.00075*depth).abs}.dup(4));
		^Pan2.ar(dels[0], -0.9, 0.35)
		+Pan2.ar(dels[1], -0.3, 0.35)
		+Pan2.ar(dels[2], 0.3, 0.35)
		+Pan2.ar(dels[3], 0.9, 0.35)
	}
}
