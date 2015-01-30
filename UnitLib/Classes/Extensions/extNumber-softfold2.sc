+ Number {
	softfold2 { |aNumber = 1.0, range|
		^this.fold2( aNumber ).sineclip2( aNumber, range, false );
	}
	
	softclip2 { |aNumber = 1.0, range|
		range = range ?? { aNumber / 2 };
		range = range.clip(1.0e-12,aNumber);
		^this.clip2( aNumber-range ) + ( 
			(this.excess( aNumber-range ) / range).distort * range
		);
	}
	
	sineclip2 { |aNumber = 1.0, range, normalize = true|
		var div, me;
		range = range ?? { aNumber / 2 };
		range = range.clip(1.0e-12, aNumber);
		aNumber = aNumber.max(1.0e-12);
		if( normalize.isKindOf( Boolean ) ) { normalize = normalize.binaryValue };
		div = 1 - ( 0.35971998019677 * ( (range * normalize ) / aNumber ) );
		me = this * div;
		^(
			me.clip2( aNumber-range ) + ( 
				me.excess( aNumber-range )
					.linlin( range.neg, range, -0.5pi, 0.5pi  )
					.sin * 0.64028001980323 * range
			)
		)  / div;
	}
	
	softexcess2 { |aNumber = 0.1, range, steepness = 1|
		var a,b;
		steepness = steepness * aNumber;
		a = this + steepness;
		b = this - steepness;
		^a.blend( b, this.sineclip2(aNumber, range, false).linlin(aNumber.neg,aNumber,0,1,\minmax) );
	}
	
	softwrap2 { |aNumber = 1, amt = 0.5|
		var wrapped;
		amt = amt.clip(0, 1) * aNumber;
		wrapped = (this - (amt * 0.5)).wrap2( aNumber );
		^wrapped.blend( 
			wrapped - (2*aNumber), 
			wrapped.linlin( aNumber-amt, aNumber,-0.5pi,0.5pi).sin.linlin(-1,1,0,1) 
		) + (amt * 0.5)
	}
}

+ UGen {
	softfold2 { |aNumber = 1.0, range|
		^this.fold2( aNumber ).sineclip2( aNumber, range, false );
	}
	
	softclip2 { |aNumber = 1.0, range|
		range = range ?? { aNumber / 2 };
		range = range.clip(1.0e-12, aNumber);
		^this.clip2( aNumber-range ) + ( 
			(this.excess( aNumber-range ) / range).distort * range
		);
	}
	
	sineclip2 { |aNumber = 1.0, range, normalize = 1|
		var div, me;
		range = range ?? { aNumber / 2 };
		range = range.clip(1.0e-12, aNumber);
		aNumber = aNumber.max(1.0e-12);
		if( normalize.isKindOf( Boolean ) ) { normalize = normalize.binaryValue };
		div = 1 - ( 0.35971998019677 * ( (range * normalize ) / aNumber ) );
		me = this * div;
		^(
			me.clip2( aNumber-range ) + ( 
				me.excess( aNumber-range )
					.linlin( range.neg, range, -0.5pi, 0.5pi  )
					.sin * 0.64028001980323 * range
			)
		)  / div;
	}
	
	softexcess2 { |aNumber = 0.1, range, steepness = 1|
		var a,b,mix;
		steepness = steepness * aNumber;
		a = this + steepness;
		b = this - steepness;
		mix = this.sineclip2(aNumber, range, false).linlin(aNumber.neg,aNumber,0,1,\minmax);
		^( mix * b ) + ( (1-mix) * a );
	}
	
	softwrap2 { |aNumber = 1, amt = 0.5|
		var wrapped, mix;
		amt = amt.clip(0, 1) * aNumber;
		wrapped = (this - (amt * 0.5)).wrap2( aNumber );
		mix = wrapped.linlin( aNumber-amt, aNumber,-0.5pi,0.5pi).sin.linlin(-1,1,0,1);
		^(( mix * (wrapped - (2*aNumber)) ) + ((1-mix) * wrapped )) + (amt * 0.5);
	}
}

+ SequenceableCollection {
	softfold2 { arg aNumber = 1.0, range; ^this.collect(_.softfold2(aNumber,range)); }
	softclip2 { arg aNumber = 1.0, range; ^this.collect(_.softclip2(aNumber,range)); }
	sineclip2 { arg aNumber = 1.0, range, normalize = true; 
		^this.collect(_.sineclip2(aNumber,range, normalize)); 
	}
	softexcess2 { arg aNumber = 1.0, range, steepness = 1; 
		^this.collect(_.softexcess2(aNumber,range, steepness)); 
	}
	softwrap2 { arg aNumber = 1.0, amt = 0.5; ^this.collect(_.softwrap2(aNumber,amt)); }
}

+ Point {
	softfold2 { arg aPoint = 1.0, range; 
		aPoint = aPoint.asPoint;
		range = (range ?? { aPoint * 0.5 }).asPoint;
		^Point( 
			x.softfold2( aPoint.x, range.x ), 
			y.softfold2( aPoint.y, range.y ) 
		);
	}
	
	softclip2 { arg aPoint = 1.0, range; 
		aPoint = aPoint.asPoint;
		range = (range ?? { aPoint * 0.5 }).asPoint;
		^Point( x.softclip2( aPoint.x, range.x ), y.softclip2( aPoint.y, range.y ) );
	}
	
	sineclip2 { arg aPoint = 1.0, range, normalize = true; 
		aPoint = aPoint.asPoint;
		range = (range ?? { aPoint * 0.5 }).asPoint;
		^Point( 
			x.sineclip2( aPoint.x, range.x, normalize ), 
			y.sineclip2( aPoint.y, range.y, normalize ) 
		);
	}
	
	softexcess2 { arg aPoint = 1.0, range, steepness = 1; 
		aPoint = aPoint.asPoint;
		range = (range ?? { aPoint * 0.5 }).asPoint;
		steepness = steepness.asPoint;
		^Point( 
			x.softexcess2( aPoint.x, range.x, steepness.x ), 
			y.softexcess2( aPoint.y, range.y, steepness.y ) 
		);
	}
	
	softwrap2 { arg aPoint = 1.0, amt = 0.5; 
		aPoint = aPoint.asPoint;
		amt = (amt ?? { aPoint * 0.5 }).asPoint;
		^Point( 
			x.softwrap2( aPoint.x, amt.x ), 
			y.softwrap2( aPoint.y, amt.y ) 
		);
	}
	
}
