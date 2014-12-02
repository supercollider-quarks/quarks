+ UGenNode {

	asLaTeX {
		var res, args;
		if(ugenClass == Integrator) { ^this.integLaTeX };
		args = this.reducedArguments;
		res = ugenClass.name ++ "_{" ++ selector ++ "}";
		if(args.notEmpty) {
			res = res ++ this.bracketLaTeX(args.collect(_.asLaTeX).join(", "))
		};
		^res
	}
	
	integLaTeX {
		^"\\int_{" ++ selector ++ "}^{ leak = " ++ arguments[1].asLaTeX 
			++ "} {" ++ arguments[0].asLaTeX ++ "} dt"
	}
}

+ UnaryOpUGenNode {

	asLaTeX {
		var op = arguments[0];
		var x = arguments[1].asLaTeX;
		if(op == 'sqrt') { ^"\\sqrt{" ++ x ++ "}"};
		if(op == 'squared') { ^x ++ "^2 "};
		if(op == 'cubed') { ^x ++ "^3 "};
		if(op == 'reciprocal') { ^"\\frac{1}{" ++ x ++ "}"};
		if(op == 'abs') { ^"\\left|{" ++ x ++ "}\\right|"};
		if(op == 'exp') { ^"e^{" ++ x ++ "}"};
		if(op == 'log10') { ^"\\log_{10}{" ++ x ++ "}"};
		if(op == 'log2') { ^"\\log_{2}{" ++ x ++ "}"};
		if(op == 'log') { ^"\\log_{e}{" ++ x ++ "}"};
		if(op == 'floor') { ^"\\lfloor{" ++ x ++ "}\\rfloor"};
		if(op == 'ceil') { ^"\\lceil{" ++ x ++ "}\\rceil"};
		if(op == 'neg') { ^"- " ++ x };
		
		^op  ++ this.bracketLaTeX(x)
	}
	
}

+ BinaryOpUGenNode {

	asLaTeX {
		var selector, op1, op2, op2orig;
		#selector, op1, op2 = arguments;
		op2orig = op2;
		op1 = op1.asLaTeX;
		op2 = op2.asLaTeX;
		
		if(selector == '/') {
			if(op2orig.isNumber) {
				^"\\frac{1}{" ++ op2 ++ "} " + op1 
			};
			^"\\frac {" ++ op1 ++ "}{" ++ op2 ++ "}"
		};
		if(selector == '*') {
			^op1 ++ " \\cdot " ++ op2 
		};
		if(selector == 'pow') {
			^"{" ++ op1 ++ "}^{" ++ op2 ++ "}"
		};
		if(selector == 'absdif') {
			^this.bracketLaTeX(op1 ++ "-" ++  op2, "|", "|");
		};
		if(selector.isBasicOperator) {
			^this.bracketLaTeX(op1 + selector + op2)
		}; 
		^selector.asLaTeX + this.bracketLaTeX(op1 + "," + op2)
		
	}
}

+ MulAddUGenNode {

	asLaTeX {
		var res, in, mul, add;
		#in, mul, add = arguments;
		res = in.asLaTeX;
		
		if(mul != 1) {
			res = res ++ " \\cdot " ++ mul.asLaTeX;
		};
		if(add != 0) {
			res = res ++ " + " ++ add.asLaTeX;
		};
		^res
	}
}

+ ControlUGenNode {
	asLaTeX {
		var nm = names;
		nm = (nm ?? ["\\dots"]).asArray.extend(arguments.size.max(1), "\\dots");
		nm = nm.collect { |x, i| 
					if(arguments[i].notNil) {
						x = x ++  "^{" ++ arguments[i] ++ "}";
					};
				x
		};
		^nm.asLaTeX
	}
}

+ Collection {
	asLaTeX {
		var items = this.collect(_.asLaTeX);
		var res = "\\begin{array}{ll}\n";
		res = res ++ items.join("\\\\\n");
		res = res ++ "\\end{array}";
		res = this.bracketLaTeX(res, "\n[", "]\n");
		^res
	}
	asFracLaTeX {
		^"\\frac{" ++ this[0] ++  "}{" ++ this[1] ++ "}"
	}
}

+ Object {
	asLaTeX {
		^this.asCompileString
	}
	asLaTexNodeDoc {
		^"\\documentclass[12pt,a4paper]{article}\n"
		"\\begin{document}\n"
		"\\begin{displaymath}\n"
		++ this.asLaTeX ++
		"\n\\end{displaymath}\n"
		"\\end{document}\n"
	}
	bracketLaTeX { arg content, lbracket = "(", rbracket = ")";
		^"\\left" ++ lbracket ++ "{" + content + "} \\right" ++ rbracket
	}  
}

+ Symbol {
	asLaTeX {
		^this.asString
	}
}

+ String {
	asLaTeX {
		^this
	}
}

+ Float {
	asLaTeX { // do some basic analysis here
		var frac, res;
		
		frac = this.extractFraction;
		frac !? { ^this.simplifyFractionAsLaTex(frac) };
		
		frac = this.extractFraction({ |x| x / pi });
		frac !? {  res = this.simplifyFractionAsLaTex(frac);
			^if(res == 1) {Ê"" } { res } ++ " \\pi" 
		};

		frac = this.extractFraction({ |x| exp(x) });
		frac !? { ^"e^{" ++ this.simplifyFractionAsLaTex(frac) ++ "}" };
		
		frac = this.extractFraction({ |x| x.squared });
		frac !? { ^"\\sqrt{" ++ this.simplifyFractionAsLaTex(frac) ++ "}" };
		
		^this.asString
	}
	
	extractFraction { arg func, maxden = 100;
		var num = func.value(this) ? this;
		var frac = num.asFraction;
		frac.postln;
		if(frac[0] > maxden or: { frac[1] > maxden }) { ^nil };
		if(absdif(frac[0] / frac[1], num).postln > 1e-10) { ^nil };
		^frac
	}
	
	simplifyFractionAsLaTex { arg frac;
		if(frac[0] == frac[1]) { ^"1" };
		if(frac[1] == 1) { ^frac[0].asString };
		^frac.asFracLaTeX
	}
}

+ NodeProxyUGenNode {
	asLaTeX {
		^ugenClass.keyAsLaTeX
	}
}

+ NodeProxy {
	sourceAsLaTeX {
		var ctl, defArgs, argNames, obj = this.source; // for now only return first object
		^if(obj.isFunction) { 
			argNames = obj.def.argNames;
			defArgs = obj.def.prototypeFrame;
			ctl = Control.names(argNames).kr(defArgs);
			obj.valueArray(ctl).asLaTeX
		} {
			obj.asLaTeX
		}
	}
	
	keyAsLaTeX { |key|
		var rateSym = "", chanSym = "";
		if(this.isNeutral.not) {
				chanSym = this.numChannels;
				rateSym = if(this.rate == \audio) { "ar" } { "kr" };
		};
		key = key ?? { this.key } ? "{}";
		^key ++ "^{" ++ rateSym  ++ "}_{" ++ chanSym ++ "}"
	}
}

+ ProxySpace {
	asLaTeX {
		var keys = envir.keys.asArray.sort;
		var res = "";
		res = res ++ "\\begin{array}{ll}\n";
		keys.do { |key|
			var proxy = this.envir.at(key);
			if(proxy.source.notNil) {
				res = res ++ proxy.keyAsLaTeX(key) + "& = " + proxy.sourceAsLaTeX + "\\\\\n"
			};
		};
		res = res ++ "\\end{array}\n";
		^res
	}
}
