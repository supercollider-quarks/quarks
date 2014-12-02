// by Juan A. Romero
// Based on the Bjorklund Quark
// gives ratios for durations instead of arrays with binaries

Bjorklund2 {
	*new {|k, n|
		var b = Bjorklund(k, n);
		var r = b.indicesOfEqual(1).replace(0, b.size);
		r = r.rotate(-1).differentiate;
		^r;
	}
}