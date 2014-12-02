
+ SimpleNumber {
	asRational { ^Rational.new(this) } 

	/% { |that| ^Rational(this.asFloat / that) } 

	fuzzygcd { |that, error = 0.15, tries|
			// error = acceptable amount of error in deciding "equality"
			// tries = how many iterations before giving up & returning nil
		^this.asFloat.fuzzygcd(that.asFloat, error, tries)
	}
}

+ Float {
	/% { |that| ^Rational(this / that) }

	fuzzygcd { |that, error = 0.015, tries|
		var lo, hi, margin;
		lo = this.abs.min(that.abs);
		hi = this.abs.max(that.abs);
		tries = (tries ? 20).abs;
		(((margin = (hi % lo) / lo) < error) or: (1-margin < error)).if({
			(lo >= error).if({ ^lo }, { ^nil })	// if lo divides hi acceptably, return lo as gcd
											// unless lo < error, then fail
		}, {
			(tries > 0).if({
				^lo.fuzzygcd(hi % lo, error, tries - 1)  // recursive
			}, {
				^nil		// failed
			});
		});
	}
}

+ Collection {
	asRational { 
		^this.collect({ |item| item.asRational }) 
	}

		// "that" should really be a number here
	/% { |that| ^this.collect(_ /% that) }
}

+ SequenceableCollection {
	/% { |that, adverb| ^this.performBinaryOp('/%', that, adverb) }
}
