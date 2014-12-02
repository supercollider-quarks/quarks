// part of wslib 2005
//
// extension of range functionality

+ SimpleNumber { 
	// to be used on scalars as well as UGens
	
	range { arg lo, hi, range=\unipolar; 
		// useful for SCSlider .value output
		//^this.linlin(inMin, inMax, lo, hi);
		var mul, add;	
		if (range == \bipolar, {
			mul = (hi - lo) * 0.5;
			add = mul + lo;
		},{
			mul = (hi - lo) ;
			add = lo;
		});
 		^(this * mul) + add;
		}
		
	forceRange { arg lo, hi, range=\unipolar; // same as above
		^this.range(lo,hi,range); }
		
	rangeExp {  arg lo, hi, range=\unipolar; 
		//^this.linexp(inMin, inMax, lo, hi);
		var mul, add;	
		if (range == \bipolar, 
			{^this.linexp(-1, 1, lo.min(1e-13), hi);},
			{^this.linexp(0, 1, lo.min(1e-13), hi); });
		}
	
	bi2uni { |lo = 0, hi = 1, clip = \none| // regardless of input range
		^this.linlin( -1, 1, lo, hi, clip );
		}
		
	uni2bi { |lo = -1, hi = 1, clip = \none|
		^this.linlin( 0, 1, lo, hi, clip );
		}
	
	
}

+ UGen {
	rangeExp { arg lo, hi;
		//cheating with midicps because linexp is not implemented yet..
		^this.range(lo.cpsmidi, hi.cpsmidi).midicps;
		}
		
	forceRange { arg lo, hi, signalRange = \unipolar;
 		var mul, add;	
		if (signalRange == \bipolar, {
			mul = (hi - lo) * 0.5;
			add = mul + lo;
		},{
			mul = (hi - lo) ;
			add = lo;
		});
 		^MulAdd(this, mul, add);
 		}
 		
 	bi2uni { |lo = 0, hi = 1, clip = \none|
		^this.linlin( -1, 1, lo, hi, clip );
		}
		
	uni2bi { |lo = -1, hi = 1, clip = \none|
		^this.linlin( 0, 1, lo, hi, clip );
		}
 	}	

+ SequenceableCollection {
	rangeExp { arg lo, hi;  ^this.collect( _.rangeExp(lo,hi)) }
	forceRange { arg lo, hi, signalRange = \unipolar;
				^this.collect( _.forceRange(lo,hi,signalRange)); } 
				
	bi2uni { |lo = 0, hi = 1, clip = \none|
		^this.linlin( -1, 1, lo, hi, clip );
		}
		
	uni2bi { |lo = -1, hi = 1, clip = \none|
		^this.linlin( 0, 1, lo, hi, clip );
		}
	}

	
				
		
		
		

