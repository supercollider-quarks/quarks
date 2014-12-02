

// keyword argument support for Object:perform

+ Object {

	performKeyValuePairs { |selector, pairs|
		var envir, argNames, args;
		var method = this.class.findRespondingMethodFor(selector);
		if(method.isNil) { ^this.doesNotUnderstand(selector) };
		
		envir = method.makeEnvirFromArgs;
		envir.putPairs(pairs);
		
		argNames = method.argNames.drop(1);
		args = envir.atAll(argNames);
		
		^this.performList(selector, args)
	}
}

+ FunctionDef {

	makeEnvirFromArgs {
		var argNames, argVals;
		argNames = this.argNames;
		argVals = this.prototypeFrame;
		^().putPairs([argNames, argVals].flop.flat)
	}
}

+ Function {

	performKeyValuePairs { |selector, pairs|
		var envir;
		if(selector !== \value) { ^this.superPerform(\performKeyValuePairs, pairs) };
		
		envir = this.def.makeEnvirFromArgs;
		envir.putPairs(pairs);
		
		^envir.use { this.valueArrayEnvir }
	}

}
