// wslib 2012
// variant of SelectX where a lagtime is added to smoothen sudden jumps
//
// uses a square root function for equal power crossfading (instead of the more widely used cosine)

LagSelectX {
	
	*new { |which = 0, array, lag = 0.1|
		if( (which.rate === \scalar) or: { lag == 0 } ) { // no lag (can also be in language)
			^( array * this.getAmps( which, array.size ).sqrt ).sum;
		} {
			lag = 1/lag.asArray.wrapExtend(2); // can have different up/down lags
			^( array * 
				Slew.perform( 
					UGen.methodSelectorForRate(which.rate),
					this.getAmps( which, array.size ), 
					lag[0], lag[1]
				).sqrt 
			).sum
		};
	}
	
	*getAmps { |which = 0, size = 10|
		which = which + 1;
		^size.collect({ |i| (which - i).wrap2(size/2).clip(0,2).fold2(1); });
	}
	
	// convenience methods (no different from *new)
	*kr { |which = 0, array, lag = 0.1|
		^this.new( which, array, lag );
	}
	
	*ar { |which = 0, array, lag = 0.1|
		^this.new( which, array, lag );
	}
	
}

TScramble {
	
	*kr { |array, trigger, lag = 0|
		^this.new1( \control, array, trigger, lag );
	}
	
	*ar { |array, trigger, lag = 0|
		^this.new1( \audio, array, trigger, lag );
	}
	
	*new1 { |rate = \audio, array, trigger, lag = 0|
		if( trigger.rate === \demand ) {
			trigger = TDuty.ar( trigger );
		};
		if( lag == 0 ) {
			^Select.perform( 
				UGen.methodSelectorForRate(rate), 
				this.getIndices( trigger, array.size ), 
				array 
			);
		} {
			^LagSelectX( this.getIndices( trigger, array.size ), array, lag );
		};
	}
	
	*getIndices { |trigger, size = 10|
		var shuffler;
		shuffler = Dshuf((..size-1), 1);
		if( trigger.isNil ) {
			^{ |i|
				Duty.kr( inf, 0, shuffler);
			} ! size;
		} {
			^{ |i|
				Demand.perform(
					UGen.methodSelectorForRate(trigger.rate), 
					trigger, 
					if( i == 0 ) { trigger } { 0 },
					shuffler
				) 
			} ! size;
		};
	}
	
}