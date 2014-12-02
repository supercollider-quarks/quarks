//redFrik


POnePole : FilterPattern {
	var <>coef, <>mul, <>add;
	*new {|pattern, coef= 0.5, mul= 1, add= 0|
		^super.newCopyArgs(pattern, coef, mul, add);
	}
	storeArgs {^[pattern, coef, mul, add]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var cofStr= coef.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, cofVal, mulVal, addVal;
		var prev= 0, out;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			cofVal= cofStr.next(outVal);
			if(cofVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			out= (1-cofVal.abs*outVal+(cofVal*prev));
			inval= (out*mulVal+addVal).yield;
			prev= out;
		};
	}
}

POneZero : POnePole {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var cofStr= coef.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, cofVal, mulVal, addVal;
		var prev= 0, out;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			cofVal= cofStr.next(outVal);
			if(cofVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			out= (1-cofVal.abs*outVal+(cofVal*prev));
			inval= (out*mulVal+addVal).yield;
			prev= outVal;
		};
	}
}

PIntegrator : FilterPattern {
	var <>coef, <>mul, <>add;
	*new {|pattern, coef= 1.0, mul= 1, add= 0|
		^super.newCopyArgs(pattern, coef, mul, add);
	}
	storeArgs {^[pattern, coef, mul, add]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var cofStr= coef.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, cofVal, mulVal, addVal;
		var prev= 0, out;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			cofVal= cofStr.next(outVal);
			if(cofVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			out= outVal+(cofVal*prev);
			inval= (out*mulVal+addVal).yield;
			prev= out;
		};
	}
}

PLPZ1 : FilterPattern {
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
		var prev= 0, out;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			out= 0.5*(outVal+prev);
			inval= (out*mulVal+addVal).yield;
			prev= outVal;
		};
	}
}

PHPZ1 : PLPZ1 {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, mulVal, addVal;
		var prev= 0, out;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			out= 0.5*(outVal-prev);
			inval= (out*mulVal+addVal).yield;
			prev= outVal;
		};
	}
}

PChanged : FilterPattern {
	*new {|pattern, threshold= 0, mul= 1, add= 0|
		^Pif(PHPZ1(pattern, mul, add).abs>threshold, 1, 0);
	}
}

PLPZ2 : FilterPattern {
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
		var prev= 0, prev2= 0, out;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			out= 0.25*(outVal+(2*prev)+prev2);
			inval= (out*mulVal+addVal).yield;
			prev2= prev;
			prev= outVal;
		};
	}
}

PHPZ2 : PLPZ2 {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, mulVal, addVal;
		var prev= 0, prev2= 0, out;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			out= 0.25*(outVal-(2*prev)+prev2);
			inval= (out*mulVal+addVal).yield;
			prev2= prev;
			prev= outVal;
		};
	}
}

PBPZ2 : PLPZ2 {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, mulVal, addVal;
		var prev= 0, prev2= 0, out;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			out= 0.5*(outVal-prev2);
			inval= (out*mulVal+addVal).yield;
			prev2= prev;
			prev= outVal;
		};
	}
}

PBRZ2 : PLPZ2 {
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var outVal, mulVal, addVal;
		var prev= 0, prev2= 0, out;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			mulVal= mulStr.next(outVal);
			if(mulVal.isNil, {^inval});
			addVal= addStr.next(outVal);
			if(addVal.isNil, {^inval});
			
			out= 0.5*(outVal+prev2);
			inval= (out*mulVal+addVal).yield;
			prev2= prev;
			prev= outVal;
		};
	}
}
