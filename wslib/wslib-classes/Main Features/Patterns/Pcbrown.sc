// wslib 2009

Pcbrown : Pattern {

	// The closer to the lo or hi, the higher the chance for a step towards the center.
	// never exceeds lo/hi, except when step > (hi - lo)
	
	var <>lo, <>hi, <>step, <>center, <>length, <>startVal;
	
	*new { arg lo=0.0, hi=1.0, step=0.125, center, length=inf, startVal;
		^super.newCopyArgs(lo, hi, step, center, length, startVal)
	}
	
	storeArgs { ^[lo,hi,step,center,length] }
	
	embedInStream { arg inval;
		var cur, dev, rnd;
		var loStr = lo.asStream, loVal;
		var hiStr = hi.asStream, hiVal;
		var centerStr = center !? { center.asStream };
		var centerVal;
		var stepStr = step.asStream, stepVal;
		
		loVal = loStr.next(inval);
		hiVal = hiStr.next(inval);
		stepVal = stepStr.next(inval);
		cur = startVal ?? { rrand(loVal, hiVal); };
		centerVal = (centerStr !? { centerStr.next( inval ) }) ?? { [loVal, hiVal].mean };

		if(loVal.isNil or: { hiVal.isNil } or: { stepVal.isNil }) { ^inval };
		
		length.value.do {
			loVal = loStr.next(inval);
			hiVal = hiStr.next(inval);
			stepVal = stepStr.next(inval);
			if(loVal.isNil or: { hiVal.isNil } or: { stepVal.isNil }) { ^inval };
			
			centerVal = (centerStr !? { centerStr.next( inval ) }) ?? { [loVal, hiVal].mean };
			
			rnd = stepVal.xrand2.abs;
			
			if( cur >= centerVal )
				{ dev = (cur + rnd).linlin( centerVal, hiVal, 0.5,1 ); }
				{ dev = (cur - rnd).linlin( loVal, centerVal, 0,0.5 ); };
	
			cur = cur + ( rnd * [1,-1][ dev.coin.binaryValue ] );
			
			inval = cur.yield;				
		};
		
		^inval;
	}
	
}
