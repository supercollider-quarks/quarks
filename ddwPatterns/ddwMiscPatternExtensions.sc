
// misc pattern extensions

+ Pattern {
	if { |truepattern, falsepattern, default = 0|
		^Pif(this, truepattern, falsepattern, default)
	}
	
	oneValuePerBeat { |minDelta = 0|
		^Penvir((delta: Pdiff(Ptime(inf)).asStream),
			Pclutch(this, Pfunc { ~delta.next > minDelta }), true)
	}
}


+ Pseries {
	*fromEndpoints { |start, end, length = 2|
		(length >= 2 and: { length != inf }).if({
			length = length.asInteger;
			^this.new(start, (end - start) / (length - 1), length)
		}, {
			Error("Pseries:fromEndpoints - length must be finite and >= 2, received %."
				.format(length)).throw;
		});
	}
}

+ Pgeom {
	*fromEndpoints { |start, end, length = 2|
		(length >= 2 and: { length != inf }).if({
			length = length.asInteger;
			^this.new(start, pow(end / start, (length - 1).reciprocal), length)
		}, {
			Error("Pgeom:fromEndpoints - length must be finite and >= 2, received %."
				.format(length)).throw;
		});
	}
}

