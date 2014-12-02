//redFrik

//--related:
//RedStereo

RedStereo2 {
	//different lfos
	*ar {|in= 0, rate= 0.2, depth= 0.1|
		var bpfs= MidEQ.ar(in, {SinOsc.kr(rate, {Rand(0, 2pi)}.dup(4), 2800, 3100)}.dup(4), 0.2, 0);
		var dels= DelayC.ar(bpfs, 0.0075, {SinOsc.kr(rate, {Rand(0, 2pi)}.dup(4), 0.00075*depth).abs}.dup(4));
		^Pan2.ar(dels[0], -0.9, 0.25)
		+Pan2.ar(dels[1], -0.3, 0.25)
		+Pan2.ar(dels[2], 0.3, 0.25)
		+Pan2.ar(dels[3], 0.9, 0.25)
	}
}