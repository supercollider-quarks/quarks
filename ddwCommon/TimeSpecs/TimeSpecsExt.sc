
// extensions required for flexible scheduling

+SimpleNumber {
	asTimeSpec {
			// why NilTimeSpec if this is 0? NilTimeSpec includes an offset to be sure
			// the schedTime is always (just slightly) in the future
		(this == 0).if({ ^NilTimeSpec.new }, {^BasicTimeSpec(this) });
	}
}

+ArrayedCollection {
	asTimeSpec { ^BasicTimeSpec(*this) }
}

+Nil {
	asTimeSpec { ^NilTimeSpec.new }
}

+Quant {
	asTimeSpec { ^BasicTimeSpec(this.quant, this.phase, this.offset) }
}
