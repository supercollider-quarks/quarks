
Rational : AbstractFunction { 
	var <value; 

	isNumber { ^true }

	*new { |value| ^super.new.value_(value) } 

	*newFrom { |value| ^super.new.value_(value) } 

	value_ { |v| v.isNumber.if({ value = v }, 
		{ MethodError("Value of a Rational must be a number", this).throw }) 
	} 

		// implements all basic math operations
	composeUnaryOp { arg aSelector; 
		var out;
		^(out = value.perform(aSelector)).tryPerform(\asRational)
			?? { out }
	} 
	composeBinaryOp { arg aSelector, operand, adverb; 
		var out;
		^(out = value.perform(aSelector, operand, adverb)).tryPerform(\asRational)
			?? { out }
	} 
	reverseComposeBinaryOp { arg aSelector, something, adverb; 
		var out;
		^(out = something.perform(aSelector, value, adverb)).tryPerform(\asRational)
			?? { out }
	} 
	composeNAryOp { arg aSelector, anArgList; 
		var out;
		^(out = value.perform(aSelector, *anArgList)).tryPerform(\asRational)
			?? { out }
	}
	
	performBinaryOpOnSimpleNumber { |selector, aNumber, adverb|
		var out;
		^(out = aNumber.perform(selector, value, adverb)).tryPerform(\asRational)
			?? { out }
	}

	/% { |that, adverb| 
		^(value.perform('/', that, adverb)).asRational 
	}
	
	fuzzygcd { |that, error = 0.15, tries|
			// why not this.fuzzygcd?
			// 'that' might be a Rational which also should be converted to float
		^that.fuzzygcd(value, error, tries)
	}

	asRational { ^this } 
	asFloat { ^value } 
	asInteger { ^value.asInteger } 
	
	asUGenInput { ^value }
	asControlInput { ^value }
	synthArg { ^value }
	
	numerator {
		var	d = this.denominator;
		^if(d.notNil) { value * d }
	}
	
	denominator {
		var	d = value.fuzzygcd(1, 0.0001, 100);
		^if(d.notNil) { d.reciprocal.round }
	}

	asString { 
		var	denom; 
		((denom = value.fuzzygcd(1, 0.0001, 100)).notNil 
			and: { denom != 1 }) 
		.if({ 
			^(value * (denom = denom.reciprocal.round)).asString ++ "/" ++ denom 
		}, { 
			^value.asString 
		}); 
	} 

	asCompileString { 
		var	denom; 
		((denom = value.fuzzygcd(1, 0.0001, 100)).notNil 
			and: { denom != 1 }) 
		.if({ 
			^"(" ++ (value * (denom = denom.reciprocal.round)).asString ++ " /% " ++ denom ++ ")" 
		}, { 
			^"Rational(" ++ value.asCompileString ++ ")" 
		}); 
	} 

	printOn { |stream| stream << this.asString } 

	storeOn { |stream| stream << this.asCompileString } 

}
