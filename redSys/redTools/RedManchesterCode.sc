//redFrik

RedManchesterCode {
	var <>numBits= 8;
	*new {|numBits= 8|
		^super.newCopyArgs(numBits);
	}
	encode {|bits|
		^bits.collect{|x, i| [x.bitXor(i*2+1%2), x.bitXor(i*2%2)]}.flat;
	}
	encodeValue {|value|
		^this.encode(value.asBinaryDigits(numBits));
	}
	encodeArray {|array|
		^array.collect{|x| this.encodeValue(x)}.flat;
	}
}

+Integer {
	manchesterEncode {|numBits= 8|
		^RedManchesterCode(numBits).encodeValue(this);
	}
}
