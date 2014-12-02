
AbstractUGenBend {
	classvar currentBuildSynthDef, ugens;

	*use { |func|
		var res;
		currentBuildSynthDef = UGen.buildSynthDef;
		UGen.buildSynthDef = this;

		res = func.value;

		UGen.buildSynthDef = currentBuildSynthDef;
		^res
	}


	*bendAllUGens { |... args|
		this.subclassResponsibility(thisMethod)
	}

	*extractUGenMethod { |class, selectors|
		selectors.asArray.do { |selector|
			var res = class.findRespondingMethodFor(selector);
			if(res.notNil) { ^res }
		};
		^nil
	}

	*extractUGenArgNames { |ugen|
		^this.extractUGenMethod(ugen.class.class, [\ar, \kr]).argNames.asArray.drop(1);
	}

	*methodSelectorFor { |something|
		^if(something.isKindOf(UGen)) {
			something.methodSelectorForRate ? \kr
		} {
			\kr
		}
	}

	*makeCrossfade { |original, replacement, ratio|
		var min, max;
		max = Amplitude.kr(max(0, original), 0.1, 0.1);
		min = Amplitude.kr(min(0, original), 0.1, 0.1);
		^LinXFade2.perform(
			this.methodSelectorFor(original),
			original,
			replacement * (max + min) - min,
			ratio.linlin(0, 1, -1, 1)
		)
	}


	// forward necessary methods to SynthDef

	*addUGen { |ugen|
		currentBuildSynthDef.addUGen(ugen);
		ugens = ugens.add(ugen);
	}

	*replaceUGen { |a, b|
		currentBuildSynthDef.replaceUGen(a, b);
		ugens !? { ugens = ugens.replace(a, b) };
	}


	*available {
		^currentBuildSynthDef.available
	}

	*available_ { |value|
		currentBuildSynthDef.available_(value)
	}

	*addConstant { |value|
		currentBuildSynthDef.addConstant(value)
	}

	*constants {
		^currentBuildSynthDef.constants
	}

}


Bend : AbstractUGenBend {

	*new { |bendFunc, ugenFunc| // todo multichannel expand bendFunc

		var res = this.use(ugenFunc);
		this.bendAllUGens(bendFunc);
		ugens = nil;

		^res
	}

	*time { |factor, ugenFunc, freqArgNames = #[\freq, \rate], durArgNames = #[\dur, \duration, \delaytime, \decaytime]|
		^this.new({ |original, argName, ugen|
			if(freqArgNames.includes(argName)) {
				original * factor.value(original, argName, ugen)
			} {
				if(durArgNames.includes(argName)) {
					original * factor.value(original, argName, ugen).reciprocal
				} {
					original
				}
			}
		}, ugenFunc)
	}

	*controls { |ugenFunc, applySpecs = false|
		var bent, ctlName, spec, names = Dictionary.new, ugenCount;
		^this.new({ |original, argName, ugen|
			ctlName = "%_%".format(argName,  ugen.class.name);
			ugenCount = names.at(ctlName);
			ugenCount = if(ugenCount.isNil) { 0 } { ugenCount + 1 };
			names.put(ctlName, ugenCount);
			ctlName = ctlName ++ "_" ++ ugenCount;
			spec = argName.asSpec;
			ctlName = ctlName.asSymbol;
			if(applySpecs and: { original.isNumber } and: { spec.notNil }) {
				Spec.add(ctlName, spec.storeArgs);
				NamedControl.new(ctlName, original, \control);
			} {
				original * NamedControl.new(ctlName, 1.0, \control)
			}
		}, ugenFunc)
 	}

	*bendAllUGens { |bendFunc|

		ugens.do { |ugen|
			this.bendUGen(ugen, bendFunc)
		}
	}

	*bendUGen { |ugen, bendFunc|

		var inputs, arguments, argNames;

		argNames = this.extractUGenArgNames(ugen);
		inputs = ugen.inputs;
		if(inputs.isNil or: { argNames.isNil }) { ^this };

		argNames.do { |argName, argIndex|  // drop "this"
				var original, replaceArg;
				original = inputs.at(argIndex);
				if(original.notNil) {
						replaceArg = bendFunc.value(original, argName, ugen);
						if(replaceArg !== original and: { replaceArg.notNil }) {
							ugen.inputs.put(argIndex, replaceArg);
						}
				}
		}
	}



}


CircuitBend : AbstractUGenBend {

	classvar <>excludedUGens = #[\CombN, \AllpassN, \Filter];  // exclude risky UGens
	classvar arugens, krugens, arouts, krouts;

	*new { |bendFunc, ugenFunc, maxSize = 16| // todo multichannel expand bendFunc

		var res;
		maxSize = maxSize ? 16;
		this.readOutputs(maxSize);

		res = this.use(ugenFunc);

		this.initUGens;
		this.bendAllUGens(bendFunc);
		this.processOutputs;
		this.writeOutputs;

		this.finish;

		^res

	}

	*readOutputs { |size|
		arouts = LocalIn.ar(size);
		krouts = LocalIn.kr(size);
	}

	*initUGens {
		arugens = ugens.select { |x| x.rate == \audio };
		krugens = ugens.select { |x| x.rate == \control };
	}

	*processOutputs {
		arugens = Normalizer.ar(arugens);
		arugens = this.shapeOutputs(arugens.asArray, arouts.size);

		krugens = krugens.collect { |x| x / Amplitude.kr(x) };
		krugens = this.shapeOutputs(krugens.asArray, krouts.size);
	}

	*shapeOutputs { |array, size|
		if(array.size >= size) { ^array.keep(size) };
		^if(array.first.rate == \audio) {
			array.extend(size, Silent.ar)
		} {
			array.extend(size, 0.0)
		}
	}

	*writeOutputs {
		if(arugens.notEmpty) { LocalOut.ar(arugens) };
		if(krugens.notEmpty) { LocalOut.kr(krugens) };
	}

	*finish {
		arugens = krugens = arouts = krouts = ugens = nil;
	}

	*bendAllUGens { |bendFunc|

		ugens.do { |ugen, i|

			if(ugen.rate == \audio) {
				this.bendUGen(ugen, bendFunc, arouts.keep(arugens.size), i)
			};
			if(ugen.rate == \control) {
				this.bendUGen(ugen, bendFunc, krouts.keep(krugens.size), i)
			};
		};

	}

	*bendUGen { |ugen, bendFunc, others, ugenIndex|

			var inputs, controls, argNames;
			excludedUGens.do { |class|
				if(ugen.isKindOf(class.asClass)) { ^this }
			};
			inputs = ugen.inputs;
			if(inputs.isEmpty) { ^this };
			argNames = this.extractUGenArgNames(ugen);

			inputs.size.do { |inputIndex|
				// original, others, argName, ugenIndex, inputIndex
				var res = bendFunc.value(
								inputs.at(inputIndex), // original input
								others, // all ugens
								argNames.at(inputIndex), // argument name
								ugenIndex, // nth ugen
								inputIndex // nth input
						);
				if(res.notNil) { inputs.put(inputIndex, res) };
			}
	}


	// specific uses

	*central { |ugenFunc, maxSize, factor, blend = false|

		^this.new({ |in, others, argName, i, j|
			var mix = others.scramble.keep(rrand(1, others.size)).mean;
			if(blend.not) {
				mix * factor + in
			} {
				this.makeCrossfade(in, mix, factor)
			}
		}, ugenFunc, maxSize)
	}

	*drift { |ugenFunc, maxSize, factor = 0.1, rate = 0.1, blend = false|

		^this.new({ |in, others, argName, i, j|
			var mix = (others * ({ |i| LFDNoise1.kr(rate.value(i)).max(0) } ! others.size)).mean;
			if(blend.not) {
				mix * factor + in
			} {
				this.makeCrossfade(in, mix, factor)
			}
		}, ugenFunc, maxSize)
	}

	*controls { |ugenFunc, maxSize, factor = 0.1, blend = false,
				default = ({ 0.001.rand }), defaultLag, controlPrefix = "bend"|

		^this.new({ |in, others, argName, i, j|
			var control, lagControl, mix, selector;
			var controlName, lagControlName;
			if(defaultLag.notNil) {
				lagControlName = "%_lag_%_%".format(controlPrefix, i, j).asSymbol;
				lagControl = 	NamedControl.kr(lagControlName, defaultLag ! others.size);
				lagControl = lagControl.linexp(0, 1, 0.001, 10);
				//Spec.add(lagControlName, [0.001, 10, \exp]);
			};
			controlName = "%_%_%".format(controlPrefix, i, j).asSymbol;
			control = NamedControl.kr(controlName, default ! others.size, lagControl);
			//Spec.add(controlName, [0, 1, \lin]);

			mix = (others * control).sum / control.sum.max(1);

			if(blend) {
				this.makeCrossfade(in, mix, factor)
			} {
				mix * factor + in
			}

		}, ugenFunc, maxSize)
	}

	*controls1 { |ugenFunc, maxSize, blend = false,
					default = 0.05, defaultLag, controlPrefix = "bend"|

		^this.new({ |in, others, argName, i, j|

			var control, indexControl, lagControl, mix;
			var controlName, lagControlName, indexControlName;

			if(defaultLag.notNil) {
				lagControlName = "%_lag_%_%".format(controlPrefix, i, j).asSymbol;
				lagControl = 	NamedControl.kr(lagControlName, defaultLag);
				lagControl = lagControl.linexp(0, 1, 0.001, 10);
				//Spec.add(lagControlName, [0.001, 10, \exp]);
			};
			controlName = "%_%_%".format(controlPrefix, i, j).asSymbol;
			control = NamedControl.kr(controlName, default, lagControl);
			//Spec.add(controlName, [0, 1, \lin]);

			indexControlName = "%_index_%_%".format(controlPrefix, i, j).asSymbol;
			indexControl = NamedControl.kr(indexControlName, others.size.rand);
			indexControl = indexControl * (others.size - 1);
			//Spec.add(indexControlName, [0, others.size - 1, \lin, 1]);

			mix = Select.perform(this.methodSelectorFor(in), indexControl, others);
			if(blend) {
				this.makeCrossfade(in, mix, control)
			} {
				mix * control + in
			}
		}, ugenFunc, maxSize)

	}

}

/*

// needs a refactoring: constructor functions from above should apply

IOBend : CircuitBend {
	*new { |bendFunc, ugenFunc, arbus, krbus|

		var res;

		this.readOutputs(arbus, krbus);

		res = this.use(ugenFunc);

		this.initUGens;
		this.bendAllUGens(bendFunc);
		this.processOutputs;

		this.writeOutputs(arbus, krbus);

		this.finish;

		^res

	}

	*readOutputs { |arbus, krbus|
		arouts = InFeedback.ar(arbus, arbus.numChannels);
		krouts = In.kr(krbus, krbus.numChannels);
	}

	*writeOutputs { |arbus, krbus|
		Out.ar(arbus, arugens);
		Out.kr(krbus, krugens);
	}

}

BendOut : IOBend {
	*new { |ugenFunc, arbus, krbus|

		var res;

		res = this.use(ugenFunc);

		this.initUGens;
		this.processOutputs;
		this.writeOutputs(arbus, krbus);

		this.finish;

		^res

	}
}

BendIn : IOBend {
	*new { |bendFunc, ugenFunc, arbus, krbus|

		var res;

		this.readOutputs(arbus, krbus);

		res = this.use(ugenFunc);

		this.initUGens;
		this.bendAllUGens(bendFunc);

		this.finish;

		^res

	}
}

*/

