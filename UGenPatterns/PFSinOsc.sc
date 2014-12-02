//redFrik


PFSinOsc : Pattern {
	var <>freq, <>iphase, <>mul, <>add, <>length;
	*new {|freq= 440, iphase= 0, mul= 1, add= 0, length= inf|
		^super.newCopyArgs(freq, iphase, mul, add, length);
	}
	storeArgs {^[freq, iphase, mul, add, length]}
	embedInStream {|inval|
		var freqStr= freq.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var freqVal, mulVal, addVal;
		var theta= 0;
		length.value(inval).do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			freqVal= freqStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil or:{freqVal.isNil}}, {^inval});
			inval= (sin(theta/freqVal*2pi+iphase)*mulVal+addVal).yield;
			theta= theta+1;
		};
		^inval;
	}
}

PSaw : Pattern {
	var <>freq, <>mul, <>add, <>length;
	*new {|freq= 440, mul= 1, add= 0, length= inf|
		^super.newCopyArgs(freq, mul, add, length);
	}
	storeArgs {^[freq, mul, add, length]}
	embedInStream {|inval|
		var freqStr= freq.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var freqVal, mulVal, addVal;
		var counter= 0;
		length.value(inval).do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			freqVal= freqStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil or:{freqVal.isNil}}, {^inval});
			inval= (counter*2-1*mulVal+addVal).yield;
			counter= counter+freqVal.reciprocal%1;
		};
		^inval;
	}
}

PPulse : Pattern {
	var <>freq, <>width, <>mul, <>add, <>length;
	*new {|freq= 440, width= 0.5, mul= 1, add= 0, length= inf|
		^super.newCopyArgs(freq, width, mul, add, length);
	}
	storeArgs {^[freq, width, mul, add, length]}
	embedInStream {|inval|
		var freqStr= freq.asStream;
		var widthStr= width.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var freqVal, widthVal, mulVal, addVal;
		var counter= 0;
		length.value(inval).do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			widthVal= widthStr.next(inval);
			freqVal= freqStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil or:{widthVal.isNil or:{freqVal.isNil}}}, {^inval});
			if(counter<widthVal, {
				inval= (mulVal+addVal).yield;
			}, {
				inval= addVal.yield;
			});
			counter= counter+freqVal.reciprocal%1;
		};
		^inval;
	}
}
