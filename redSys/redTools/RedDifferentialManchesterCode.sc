//redFrik

RedDifferentialManchesterCode {
	var <>numBits= 8, <>phase= 0;
	*new {|numBits= 8, initPhase= 0|
		^super.newCopyArgs(numBits, initPhase);
	}
	reset {
		phase= 0;
	}
	encodeValue {|value|
		^this.encode(value.asBinaryDigits(numBits));
	}
	encodeArray {|array|
		^array.collect{|x| this.encodeValue(x)}.flat;
	}
	//--positive edge clock (1= flip)
	encode {|bits|
		^bits.collect{|x|
			[1-phase, phase= 1-x.bitXor(phase)];
		}.flat;
	}
}

RedDifferentialManchesterCodeNegative : RedDifferentialManchesterCode {
	*new {|numBits= 8, initPhase= 1|
		^super.newCopyArgs(numBits, initPhase);
	}
	reset {
		phase= 1;
	}
	
	//--negative edge clock (0= flip)
	encode {|bits|
		^bits.collect{|x|
			var d= if(x==0, {1-phase}, {phase});
			[d, phase= 1-d];
		}.flat;
	}
}

+Integer {
	differentialManchesterEncode {|numBits= 8, phase= 0|
		^RedDifferentialManchesterCode(numBits, phase).encodeValue(this);
	}
	differentialManchesterEncodeNegative {|numBits= 8, phase= 1|
		^RedDifferentialManchesterCodeNegative(numBits, phase).encodeValue(this);
	}
}
