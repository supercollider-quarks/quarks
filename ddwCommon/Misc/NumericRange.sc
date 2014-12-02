
NumericRange {
	var	<>lo, <>hi, <>spec;
	
	*new { arg lo, hi, spec;
		^super.newCopyArgs(lo, hi, spec.asSpec)
	}
	
	rrand { ^rrand(lo, hi) }
	inrange { |num| ^num.inclusivelyBetween(lo, hi) }
	
	range01_ { arg l, h;
		lo = spec.map(l);
		hi = spec.map(h);
	}

	range_ { arg l, h;
		lo = l;
		hi = h;
	}
	
	lo01 { ^spec.unmap(lo) }
	hi01 { ^spec.unmap(hi) }
	
	guiClass { ^NumericRangeGui }
}
