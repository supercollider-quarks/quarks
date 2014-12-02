Plfnoise0 : Pattern {
	var <>nSamps, <>lo, <>hi, <>length;
	*new { arg nSamps = 10, lo=0.0, hi=1.0, length=inf;
		^super.newCopyArgs(nSamps, lo, hi, length)
	}
	storeArgs { ^[nSamps,lo,hi,length] }
	embedInStream { arg inval;
		var counter, level, nextValue, curLevel;
		var loStr = lo.asStream;
		var hiStr = hi.asStream;
		var nSampsStr = nSamps.asStream;
		var hiVal, loVal;
		counter = 0;
		hiVal = hiStr.next(inval);
		loVal = loStr.next(inval);
		if(hiVal.isNil or: { loVal.isNil or: {counter.isNil}}) { ^inval };
		level = hiVal.rrand(loVal);
		length.value.do({
			(counter <= 0).if({
				hiVal = hiStr.next(inval);
				loVal = loStr.next(inval);
				counter = nSampsStr.next(inval);
				if(hiVal.isNil or: { loVal.isNil or: {counter.isNil}}) { ^inval };
				level = hiVal.rrand(loVal);
				});
			counter = counter - 1;
			inval = level.yield;
		});
		^inval;
	}
}

Plfnoise1 : Pattern {
	var <>nSamps, <>lo, <>hi, <>length;
	*new { arg nSamps = 10, lo=0.0, hi=1.0, length=inf;
		^super.newCopyArgs(nSamps, lo, hi, length)
	}
	storeArgs { ^[nSamps,lo,hi,length] }
	embedInStream { arg inval;
		var counter, slope, level, nextValue, curLevel;
		var loStr = lo.asStream;
		var hiStr = hi.asStream;
		var nSampsStr = nSamps.asStream;
		var hiVal, loVal;
		counter = 0;
		slope = 0;
		hiVal = hiStr.next(inval);
		loVal = loStr.next(inval);
		if(hiVal.isNil or: { loVal.isNil or: {counter.isNil}}) { ^inval };
		level = hiVal.rrand(loVal);
		length.value.do({
			(counter <= 0).if({
				hiVal = hiStr.next(inval);
				loVal = loStr.next(inval);
				counter = nSampsStr.next(inval);
				if(hiVal.isNil or: { loVal.isNil or: {counter.isNil}}) { ^inval };
				nextValue = hiVal.rrand(loVal);
				slope = (nextValue - level) / counter;
				});
			curLevel = level;
			level = level + slope;
			counter = counter - 1;
			inval = curLevel.yield;
		});
		^inval;
	}
}

Plfnoise2 : Pattern {
	var <>nSamps, <>lo, <>hi, <>length;
	*new { arg nSamps = 10, lo=0.0, hi=1.0, length=inf;
		^super.newCopyArgs(nSamps, lo, hi, length)
	}
	storeArgs { ^[nSamps,lo,hi,length] }
	embedInStream { arg inval;
		var counter, slope, level, nextValue, nextMidPt, fseglen, curve;
		var val, curLevel;
		var loStr = lo.asStream;
		var hiStr = hi.asStream;
		var nSampsStr = nSamps.asStream;
		var hiVal, loVal;
		counter = 0;
		slope = 0;
		hiVal = hiStr.next(inval);
		loVal = loStr.next(inval);
		if(hiVal.isNil or: { loVal.isNil or: {counter.isNil}}) { ^inval };
		nextValue = hiVal.rrand(loVal);
		nextMidPt = nextValue * 0.5;
		length.value.do({
			(counter <= 0).if({
				val = nextValue;
				hiVal = hiStr.next(inval);
				loVal = loStr.next(inval);
				if(hiVal.isNil or: { loVal.isNil or: {counter.isNil}}) { ^inval };
				nextValue = hiVal.rrand(loVal);
				level = nextMidPt;
				nextMidPt = (nextValue + val) * 0.5;
				counter = nSampsStr.next(inval);
				fseglen = counter;
				curve = 2 * 
					(nextMidPt - level - (fseglen * slope) / ((fseglen * fseglen) + fseglen));
				});
			curLevel = level;
			slope = slope + curve;
			level = level + slope;
			counter = counter - 1;
			inval = curLevel.yield;
		});
		^inval;
	}
}