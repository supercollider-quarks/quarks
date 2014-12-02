//redFrik

//resettable and pausable line

RedLine {
	*ar {|start= 0, end= 1, dur= 1, curve= 0, run= 1, reset= 0, mul= 1, add= 0|
		var line= Sweep.ar(reset, 1/dur*run).lincurve(0, 1, start, end, curve, \minmax);
		^MulAdd(line, mul, add);
	}
	*kr {|start= 1, end= 2, dur= 1, curve= 0, run= 1, reset= 0, mul= 1, add= 0|
		var line= Sweep.kr(reset, 1/dur*run).lincurve(0, 1, start, end, curve, \minmax);
		^MulAdd(line, mul, add);
	}
}
