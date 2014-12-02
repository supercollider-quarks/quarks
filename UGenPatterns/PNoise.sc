//redFrik

PRand : Pattern {
	var <>lo, <>hi, <>length;
	*new {|lo= 0, hi= 1, length= inf|
		^super.newCopyArgs(lo, hi, length);
	}
	storeArgs {^[lo, hi, length]}
	embedInStream {|inval|
		var loStr= lo.asStream;
		var hiStr= hi.asStream;
		var loVal, hiVal;
		length.value(inval).do{
			loVal= loStr.next(inval);
			hiVal= hiStr.next(inval);
			if(loVal.isNil or:{hiVal.isNil}, {^inval});
			inval= lo.asFloat.rrand(hi).yield;
		};
		^inval;
	}
}

PIRand : Pattern {
	var <>lo, <>hi, <>length;
	*new {|lo= 0, hi= 127, length= inf|
		^super.newCopyArgs(lo, hi, length);
	}
	storeArgs {^[lo, hi, length]}
	embedInStream {|inval|
		var loStr= lo.asStream;
		var hiStr= hi.asStream;
		var loVal, hiVal;
		length.value(inval).do{
			loVal= loStr.next(inval);
			hiVal= hiStr.next(inval);
			if(loVal.isNil or:{hiVal.isNil}, {^inval});
			inval= lo.rrand(hi).asInteger.yield;
		};
		^inval;
	}
}

PExpRand : Pattern {
	var <>lo, <>hi, <>length;
	*new {|lo= 0.01, hi= 1, length= inf|
		^super.newCopyArgs(lo, hi, length);
	}
	storeArgs {^[lo, hi, length]}
	embedInStream {|inval|
		var loStr= lo.asStream;
		var hiStr= hi.asStream;
		var loVal, hiVal;
		length.value(inval).do{
			loVal= loStr.next(inval);
			hiVal= hiStr.next(inval);
			if(loVal.isNil or:{hiVal.isNil}, {^inval});
			inval= lo.exprand(hi).yield;
		};
		^inval;
	}
}

PWhiteNoise : Pattern {
	var <>mul, <>add, <>length;
	*new {|mul= 1, add= 0, length= inf|
		^super.newCopyArgs(mul, add, length);
	}
	storeArgs {^[mul, add, length]}
	embedInStream {|inval|
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var mulVal, addVal;
		length.value(inval).do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil}, {^inval});
			inval= (mulVal.rand+addVal).yield;
		};
		^inval;
	}
}

PClipNoise : PWhiteNoise {
	embedInStream {|inval|
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var mulVal, addVal;
		length.value(inval).do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil}, {^inval});
			inval= ((2.rand*2*mulVal-mulVal)+addVal).yield;
		};
		^inval;
	}
}
