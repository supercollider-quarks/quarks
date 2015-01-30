
PApplicativeFunctor : Pattern {
	var <pf, <pa;

	*new { |pf, pa|
		^super.newCopyArgs(pf, pa)
	}

	embedInStream { arg inval;
		var f, a;
		var streamf = pf.asStream;
		var streama = pa.asStream;
		loop {
			f = streamf.next(inval);
			a = streama.next(inval);
			if (f.isNil || a.isNil) { ^inval };
			// NOTE: Normally we would expect 'stream' to do processRest.
			// But the 'collect' func is not under control of the stream,
			// so that's not a safe assumption here. The func may return
			// a rest, so we have to 'processRest' the collect value.
			inval = yield(f.(a).processRest(inval));
		}
	}
}

+ Pattern {

	<*> { |otherPattern|
		^PApplicativeFunctor(this, otherPattern)
	}

}