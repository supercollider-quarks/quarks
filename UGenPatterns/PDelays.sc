//redFrik


PDelay1 : FilterPattern {
	var <>mul, <>add;
	*new {|pattern, mul= 1, add= 0|
		^super.newCopyArgs(pattern, mul, add);
	}
	storeArgs {^[pattern, mul, add]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, mulVal, addVal;
		var prev= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			inval= (prev*mulVal+addVal).yield;
			prev= outVal;
		};
	}
}

PDelay2 : PDelay1 {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, mulVal, addVal;
		var prev= 0, prev2= 0;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			inval= (prev2*mulVal+addVal).yield;
			prev2= prev;
			prev= outVal;
		};
	}
}

PDelayN : FilterPattern {
	var <>maxdelaytime, <>delaytime, <>mul, <>add;
	*new {|pattern, maxdelaytime= 2, delaytime= 2, mul= 1, add= 0|
		^super.newCopyArgs(pattern, maxdelaytime, delaytime, mul, add);
	}
	storeArgs {^[pattern, maxdelaytime, delaytime, mul, add]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var delStr= delaytime.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, delVal, mulVal, addVal;
		var delay= Array.fill(maxdelaytime.round, {0});
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			delVal= delStr.next(outVal);
			if(delVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			inval= (delay[0]*mulVal+addVal).yield;
			delay= delay.drop(1).add(0).put(delaytime.round.min(maxdelaytime.round-1), outVal);
		};
	}
}

PCombN : FilterPattern {
	var <>maxdelaytime, <>delaytime, <>decaytime, <>mul, <>add;
	*new {|pattern, maxdelaytime= 2, delaytime= 2, decaytime= 0.5, mul= 1, add= 0|
		^super.newCopyArgs(pattern, maxdelaytime, delaytime, decaytime, mul, add);
	}
	storeArgs {^[pattern, maxdelaytime, delaytime, decaytime, mul, add]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var delStr= delaytime.asStream;
		var decStr= decaytime.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, delVal, decVal, mulVal, addVal;
		var now, delay= Array.fill(maxdelaytime.round, {0});
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			delVal= delStr.next(outVal);
			if(delVal.isNil, {^inval});
			decVal= decStr.next(outVal);
			if(decVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			now= delay[0];
			inval= (now*mulVal+addVal).yield;
			delay= delay.drop(1).add(0).put(delaytime.round.min(maxdelaytime.round-1), outVal+(now*decVal));
		};
	}
}
