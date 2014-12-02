
/*
it woud be possible to do this outside a SynthDef, then UGen would automatically produce this representation. (UGen.buildSynthDef == nil)
*/

UGenNode : UGen {
	var <>ugenClass, <>selector, <>arguments;
	
	*initClass {
		"UGenNode extension: overriding Meta_UGen:multiNew method, and Meta_Control:names".postln;
	}
	
	*new { |ugenClass, selector, arguments|
		^super.new.ugenClass_(ugenClass).selector_(selector).arguments_(arguments)
	}
	asUGenInput {
		^ugenClass.performList(selector, arguments)
	}
	
	composeUnaryOp { arg aSelector;
		^UnaryOpUGenNode(aSelector, this)
	}
	composeBinaryOp { arg aSelector, something;
		^BinaryOpUGenNode(aSelector, this, something)
	}
	reverseComposeBinaryOp { arg aSelector, something, adverb;
		^BinaryOpUGenNode(aSelector, something, this)
	}
	composeNAryOp { arg aSelector, anArgList;
		^this.notYetImplemented(thisMethod)
	}
		
	reducedArguments {
		var defaultArguments, numArgs = arguments.size;
		var method = ugenClass.class.findRespondingMethodFor(selector);
		method !? {
			defaultArguments = method.prototypeFrame.drop(1).keep(numArgs);
			numArgs.reverseDo { |i| 
					if(arguments[i] != defaultArguments[i]) {
					^arguments.keep(i + 1);
				}
			};
			^[]
		};
		"no method found".error;
	}
	storeOn { arg stream;
		var params = this.reducedArguments;
		stream << ugenClass.name << "." << selector;
		if(params.notEmpty) { stream << "(" <<<* params << ")" }; 
	}
	
}

UnaryOpUGenNode : UGenNode {
	*new { |selector, operand|
		^super.new(UnaryOpUGen, \new, [selector, operand])
	}
	storeOn { arg stream;
		var selector, operand;
		#selector, operand = arguments;
		stream <<< operand << "." << selector
	}
}

BinaryOpUGenNode : UGenNode {
	*new { |selector, op1, op2|
		^super.new(BinaryOpUGen, \new, [selector, op1, op2])
	}
	storeOn { arg stream;
		var selector, op1, op2;
		#selector, op1, op2 = arguments;
		if(selector.isBasicOperator) {
			stream <<< op1 << " " << selector << " " <<< op2
		} {
			stream <<< op1 << "." << selector << "(" <<< op2 << ")"
		}
	}
}

MulAddUGenNode : UGenNode {
	*new { |rate, in, mul, add|
		^super.new(MulAdd, \new, [in, mul, add])
	}
	// here, we don't use .madd, which may be slightly more efficient, but is not so nice to read.
	storeOn { arg stream; 
		var in, mul, add;
		#in, mul, add = arguments;
		stream <<< in;
		if(mul != 1) { stream << " * (" <<< mul << ")" };
		if(add != 0) { stream << " + (" <<< add << ")" };
	}
}

ControlUGenNode : UGenNode {
	var <>names = #[];
	
	kr { arg values;
		arguments = values.asArray;
		selector = \kr;
	}
	ir { arg values;
		arguments = values.asArray;
		selector = \ir;
	}
	*kr { arg ugenClass, values;
		^this.new([], ugenClass, \kr, values.asArray)
	}
	*ir { arg ugenClass, values;
		^this.new([], ugenClass, \ir, values.asArray)
	}
	asUGenInput {
		if(names.notNil) { ugenClass.perform(\names, names) };
		^ugenClass.perform(selector, arguments)
	}
}

NodeProxyUGenNode : UGenNode {}

