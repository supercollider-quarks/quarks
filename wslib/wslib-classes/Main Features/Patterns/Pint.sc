// wslib 2009

// interpolates a Pattern to a different rate
// - negative rates will cause unpredictable results

Pint { // redirect to PintN, PintL, PintC
	classvar <dict;
	
	*initClass { dict = ( 
			\step: PintN, \N: PintN, 1: PintN,
			\lin: PintL, \linear: PintL, \L: PintL, 2: PintL,
			\cub: PintC, \cubic: PintC, \C: PintC, 4: PintC )
		}
			
	*new {  arg pattern=0.0, rate=1, length=inf, interpolation = 2;
		^(dict[ interpolation ] ? PintL).new( pattern, rate, length ) 
		}
	}


PintN : Pattern { // better name?
	 var <>pattern, <>rate, <>length;
	
	*new { arg pattern=0.0, rate=1, length=inf;
		^super.newCopyArgs(pattern, rate, length);
	}
	
	storeArgs { ^[pattern, rate, length] }
	
	embedInStream { arg inval;
		var cur, lastIndex = 0, index = 0;
		var patStr = pattern.asStream, patVal;
		var rateStr = rate.asStream, rateVal;
				
		
		patVal = patStr.next(inval);
		rateVal = rateStr.next( inval );
		cur = patVal;
		
		if(patVal.isNil or: { rateVal.isNil }) { ^inval };
				
		length.value.do {
			
			rateVal = rateStr.next( inval );
			
			if( rateVal.isNil ) { ^inval };
			
			lastIndex = index;
			index = index + rateVal;
			
			(index.floor - lastIndex.floor).do({
				patVal = patStr.next(inval);
				});
				
			if(patVal.isNil or: { rateVal.isNil }) { ^inval };
				
			cur = patVal;
			
			inval = cur.yield;				
		};
		
		^inval;
	}
	}

PintL : Pattern {
	 var <>pattern, <>rate, <>length;
	
	*new { arg pattern=0.0, rate=1, length=inf;
		^super.newCopyArgs(pattern, rate, length);
	}
	
	storeArgs { ^[pattern, rate, length] }
	
	embedInStream { arg inval;
		var cur, lastIndex = 0, index = 0;
		var patStr = pattern.asStream, patCurVal, patNxtVal;
		var rateStr = rate.asStream, rateVal;
				
		
		patCurVal = patStr.next(inval);
		patNxtVal = patStr.next(inval);
		rateVal = rateStr.next( inval );
		cur = patCurVal;
		if(patCurVal.isNil or: { rateVal.isNil }) { ^inval };
				
		length.value.do {
			
			rateVal = rateStr.next( inval );
			
			if( rateVal.isNil ) { ^inval };
			
			lastIndex = index;
			index = index + rateVal;
			
			(index.floor - lastIndex.floor).do({
				patCurVal = patNxtVal;
				patNxtVal = patStr.next(inval);
				});
				
			if(patCurVal.isNil or: { rateVal.isNil }) { ^inval };
				
			cur = patCurVal.blend( patNxtVal ? patCurVal, index.frac );
			
			inval = cur.yield;				
		};
		
		^inval;
	}

	}
	
PintC : Pattern {
	 var <>pattern, <>rate, <>length;
	
	*new { arg pattern=0.0, rate=1, length=inf;
		^super.newCopyArgs(pattern, rate, length);
	}
	
	storeArgs { ^[pattern, rate, length] }
	
	embedInStream { arg inval;
		var cur, lastIndex = 0, index = 0;
		var patStr = pattern.asStream;
		var patVal0, patVal1, patVal2, patVal3;
		var rateStr = rate.asStream, rateVal;
	
		#patVal1, patVal2, patVal3 = patStr.nextN( 3, inval);
		patVal0 = patVal1;
		
		rateVal = rateStr.next( inval );
		
		cur = patVal1;
		
		if( [patVal0, patVal1, rateVal].any(_.isNil) ) { ^inval };
				
		length.value.do {
			
			rateVal = rateStr.next( inval );
			
			if( rateVal.isNil ) { ^inval };
			
			lastIndex = index;
			index = index + rateVal;
			
			(index.floor - lastIndex.floor).do({
				#patVal0, patVal1, patVal2 = [ patVal1, patVal2, patVal3 ];
				patVal3 = patStr.next(inval);
				});
				
			if( [patVal1, rateVal].any(_.isNil) ) { ^inval };
				
			cur = this.interpolate( index.frac, patVal0, patVal1, 
					patVal2 ? patVal1, patVal3 ? patVal2 ? patVal1 );
			
			inval = cur.yield;				
		};
		
		^inval;
	}

	interpolate { |i, y0, y1, y2, y3|
		// this is used by many UGens as "cubic"
		var c0, c1, c2, c3;
		c0 = y1;
		c1 = 0.5 * (y2 - y0);
		c2 = y0 - (2.5 * y1) + (2.0 * y2) - (0.5 * y3);
		c3 = (0.5 * (y3 - y0)) + (1.5 * (y1 - y2));
		^((c3 * i + c2) * i + c1) * i + c0;
		}
	
	}
