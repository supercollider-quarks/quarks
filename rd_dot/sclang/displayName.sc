// displayName.sc - (c) rohan drape, 2004-2007

// The unary and binary operator UGens are displayed using the name of
// operator, which is specified by the specialIndex value of the UGen.

+ UGen { 
	displayName { 
		^this.name; 
	} 
}

+ BasicOpUGen {
	displayName {
		^this.name;
	}
}

+ UnaryOpUGen {
	displayName {
		^this.specialIndex.nameOfUnaryOpSpecialIndex;
	}
}

+ BinaryOpUGen {
	displayName {
		^this.specialIndex.nameOfBinaryOpSpecialIndex;
	}
}
