//redFrik


PSinOsc : Pattern {
	var <>freq, <>phase, <>mul, <>add, <>length;
	*new {|freq= 440, phase= 0, mul= 1, add= 0, length= inf|
		^super.newCopyArgs(freq, phase, mul, add, length);
	}
	storeArgs {^[freq, phase, mul, add, length]}
	embedInStream {|inval|
		var freqStr= freq.asStream;
		var phaseStr= phase.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var freqVal, phaseVal, mulVal, addVal;
		var theta= 0;
		length.value(inval).do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			phaseVal= phaseStr.next(inval);
			freqVal= freqStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil or:{phaseVal.isNil or:{freqVal.isNil}}}, {^inval});
			inval= (sin(theta/freqVal*2pi+phaseVal)*mulVal+addVal).yield;
			theta= theta+1;
		};
		^inval;
	}
}

PLFSaw : Pattern {
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
		var counter= iphase;
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

PLFTri : PLFSaw {
	embedInStream {|inval|
		var freqStr= freq.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var freqVal, mulVal, addVal;
		var counter= iphase;
		length.value(inval).do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			freqVal= freqStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil or:{freqVal.isNil}}, {^inval});
			inval= ((counter*2).fold(-1, 1)*2-1*mulVal+addVal).yield;
			counter= counter+freqVal.reciprocal%1;
		};
		^inval;
	}
}

PLFPulse : Pattern {
	var <>freq, <>iphase, <>width, <>mul, <>add, <>length;
	*new {|freq= 440, iphase= 0, width= 0.5, mul= 1, add= 0, length= inf|
		^super.newCopyArgs(freq, iphase, width, mul, add, length);
	}
	storeArgs {^[freq, iphase, width, mul, add, length]}
	embedInStream {|inval|
		var freqStr= freq.asStream;
		var widthStr= width.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var freqVal, widthVal, mulVal, addVal;
		var counter= iphase;
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

PImpulse : Pattern {
	var <>freq, <>phase, <>mul, <>add, <>length;
	*new {|freq= 440, phase= 0, mul= 1, add= 0, length= inf|
		^super.newCopyArgs(freq, phase, mul, add, length);
	}
	storeArgs {^[freq, phase, mul, add, length]}
	embedInStream {|inval|
		var freqStr= freq.asStream;
		var phaseStr= phase.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var freqVal, phaseVal, mulVal, addVal;
		var counter= 0, flag= false;
		length.value(inval).do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			phaseVal= phaseStr.next(inval);
			freqVal= freqStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil or:{phaseVal.isNil or:{freqVal.isNil}}}, {^inval});
			if(counter>=phaseVal and:{flag.not}, {
				inval= (mulVal+addVal).yield;
				flag= true;
			}, {
				inval= addVal.yield;
			});
			counter= counter+freqVal.reciprocal;
			if(counter>=1, {
				counter= counter%1;
				flag= false;
			});
		};
		^inval;
	}
}