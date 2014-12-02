// rateNumber.sc - (c) rohan drape, 2004-2007

// Return the <Integer> rate number of a named rate value.

+ String {
	rateNumber {
		if(this == "audio", { ^2; });
		if(this == "control", { ^1; });
		if(this == "demand", { ^3; });
		if(this == "scalar", { ^0; });
		if(this == "trigger", { ^4; });
		reportError("rateNumber: unknown rate: " ++ this);
	}
}

+ Symbol {
	rateNumber {
		^this.asString.rateNumber;
	}
}
