
	// some new patterns - jamshark70@dewdrop-world.net

Pwxrand : Pwrand {
	embedInStream { |inval|
		var item,
			weightStream = weights.asStream,
			currentWeights,
			index = weightStream.next(inval).windex,
			totalweight, rnd, runningsum;
		repeats.value(inval).do({ |i|
			item = list.at(index);
			inval = item.embedInStream(inval);

			currentWeights = weightStream.next(inval);
			if(currentWeights.isNil) { ^inval };

			totalweight = 1.0 - currentWeights[index];
			rnd = totalweight.rand;
			runningsum = 0;
			while {
				index = (index + 1) % currentWeights.size;
				runningsum = runningsum + currentWeights[index];
				runningsum < rnd
			};
		});
	}
}

// deprecated; Pslide now has a wrap flag
PslideNoWrap : Pslide {
		// false = do not wrap at end
	*new { |list, repeats, len, step, start|
		^super.new(list, repeats, len, step, start, false)
	}
}

Pslide1 : Pslide {
    embedInStream { arg inval;
    	var pos, item, lenStream, stepStream, lenn, stepp;
    	pos = start;
    	lenStream = len.asStream;
    	stepStream = step.asStream;
    	repeats.value(inval).do({
    			// nil protection -- stop immediately if lenStream or stepStream return nil
    		(lenn = lenStream.next(inval)).notNil.if({
	    		lenn.do({ arg j;
	    			item = list.wrapAt(pos + j);
	    			inval = item.embedInStream(inval);
	    		});
	    		(stepp = stepStream.next(inval)).notNil.if({ pos = pos + stepp },
	    			{ ^inval });
	    	}, { ^inval });
    	});
	     ^inval;
    }
}

PseqFunc : Pseq {
		// executes a function on the list item before embedding in stream
		// if func is nil, the item is used as is
	var	<func;

	*new { |list, repeats = 1, offset = 0, func|
		^super.new(list, repeats, offset).func_(func)
	}

	func_ { |f|
		f.isNil.if({ func = { |x| x } }, { func = f });
	}

	embedInStream {  arg inval;
		var item, offsetValue;
		offsetValue = offset.value;
		if (inval.eventAt('reverse') == true, {
			repeats.value(inval).do({ arg j;
				list.size.reverseDo({ arg i;
					item = func.value(list.wrapAt(i + offsetValue));
					inval = item.embedInStream(inval);
				});
			});
		},{
			repeats.value(inval).do({ arg j;
				list.size.do({ arg i;
					item = func.value(list.wrapAt(i + offsetValue));
					inval = item.embedInStream(inval);
				});
			});
		});
		^inval;
	}
}

PserFunc : PseqFunc {
	embedInStream {  arg inval;
		var item, offsetValue;
		offsetValue = offset.value;
		if (inval.eventAt('reverse') == true, {
			repeats.value(inval).reverseDo({ arg i;
				item = func.value(list.wrapAt(i + offsetValue));
				inval = item.embedInStream(inval);
			});
		},{
			repeats.value(inval).do({ arg i;
				item = func.value(list.wrapAt(i + offsetValue));
				inval = item.embedInStream(inval);
			});
		});
		^inval;
	}
}


// 1/f noise

Pvoss : Pattern {
	var	<>lo, <>hi, <>generators, <>length;
	*new { |lo = 0, hi = 1, generators = 8, length = inf|
		^super.newCopyArgs(lo, hi, generators, length)
	}
	embedInStream { |inval|
		var	localGenerators = generators.value;
		^Pfin(length.value,
			Pn(PstepNadd(*(Pwhite(0.0, 1.0, 2) ! localGenerators)), inf)
				/ localGenerators * (hi - lo) + lo
		).embedInStream(inval);
	}
}

Pmcvoss : Pvoss {
	embedInStream { |inval|
		var	counter = 1,
			localGenerators = generators.value,
			maxCounter = 1 << (localGenerators-1),
			gens = { 1.0.rand } ! localGenerators,
			total = gens.sum,
			i, new;

		length.value(inval).do {
			inval = ((total / localGenerators) * (hi - lo) + lo).yield;

			i = counter.trailingZeroes;
			new = 1.0.rand;
			total = total - gens[i] + new;
			gens[i] = new;

			counter = (counter + 1).wrap(1, maxCounter);
		};
		^inval
	}
}

Ptempo : Pattern {
	asStream { ^FuncStream({ thisThread.clock.tryPerform(\tempo) ?? { 1 } }) }
}
