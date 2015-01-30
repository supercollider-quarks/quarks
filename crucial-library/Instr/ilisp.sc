+ SequenceableCollection {

	ilisp {
		var operator, subject, arguments;
		if(this.size == 0, { ^this });
		# operator ... arguments = this;
		// operator is instr name
		if(operator.isString, {
			// pseudo-macro for any instr named #name
			// it doesn't evaluate the arguments
			// and returns the result
			if(operator.first == $#, {
				^operator.asInstr.valueArray(arguments).ilisp
			});

			if(arguments.size == 1 and: {
				arguments.first.isKindOf(Dictionary)
			},{
				// accept keyword arguments
				arguments = arguments.first.ilisp
			},{
				arguments = arguments.collect(_.ilisp)
			});
			^operator.asInstr.valueArray(arguments);
		});
		// operator is symbol
		if(operator.isKindOf(Symbol).not, {
			Error("Operator is not a symbol:" + operator + "in form:" + this).throw
		});

		subject = arguments.removeAt(0);
		subject = subject.ilisp; // resolve sexp
		^subject.performList(operator, arguments.collect(_.ilisp));
	}
}

+ Object {

	ilisp {
		^this.dereference
	}
}

+ Dictionary {

	ilisp {
		^this.collect(_.ilisp);
	}
}
