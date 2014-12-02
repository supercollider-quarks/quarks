
// extensions for SequenceNote math

// math ops in numeric classes simply return a number
// the math methods in SequenceNote wrap it in a new note object

+ Number {
	numPerformBinaryOpOnNumber { |selector, aNumber|
		^aNumber.perform(selector, this)
	}
}

+ SequenceableCollection {
	numPerformBinaryOpOnNumber { |selector, aNumber|
		^aNumber.perform(selector, this)
	}
}
		
+ Object {
	numPerformBinaryOpOnNumber { |selector|
		Error("FATAL ERROR:\nBinary op " ++ selector.asCompileString
			++ " on SequenceNote failed.").throw;
	}
}

// conversions - shortcut syntax [60, 1, 1, 1].asSequenceNote === SequenceNote(60, 1, 1, 1)

+ Array {
	asSequenceNote { ^SequenceNote(*this) }
}


// other stuff for patterns

+ SimpleNumber {
	isValidVoicerArg { ^true }
	isValidSynthArg { ^true }
}

+ Rational {
	isValidVoicerArg { ^true }
	isValidSynthArg { ^true }
}

+ SequenceableCollection {
	isValidVoicerArg {
		this.do({ |item|
			item.isValidVoicerArg.not.if({ ^false });
		});
		^true
	}
	isValidSynthArg {
		this.do({ |item|
			item.isValidSynthArg.not.if({ ^false });
		});
		^true
	}
}

+ Object {
	isValidVoicerArg { ^false }
	isValidSynthArg { ^false }
}

+ String {
	isValidSynthArg {
		^(this[0] == $c and: {
			(1..this.size-1).do({ |i|
				this[i].isDecDigit.not.if({ ^false });
			});
			^true
		})
	}
	isValidVoicerArg { ^this.isValidSynthArg }
}

+ Symbol {
	isValidSynthArg { ^this.asString.isValidSynthArg }
	isValidVoicerArg { ^this.asString.isValidSynthArg }
}
