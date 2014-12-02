// wslib 2010
// unwrapping a wrapped array
// useful for detecting phase slope etc.

+ SequenceableCollection {
	
	/* example (proof of concept):
	
	((..100) * 0.125).wrap2(1).unwrap2(1).plot;
	
	*/
	
	unwrap2 { |aNumber = 1|
		^this.unwrap( aNumber.neg, aNumber );
	}
	
	unwrap { |lo = -1, hi = 1|
		var last;
		last = this[0];
		^[last] ++ this[1..].collect({ |item|
			var out;
			out = item.wrap( last + lo, last + hi );
			last = out;
			out;
		});
	}
	
}