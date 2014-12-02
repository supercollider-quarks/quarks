
// I wrote Pinterp before Pseg got added; keeping the stub for backward compatibility
// current embedInStream implementation is incorrect
// because Envs as streams do not terminate anymore

Pinterp : FilterPattern {
	var	<>times, <>curves;
	*new { |pattern, times = 1.0, curves = \lin|
		^Pseg(pattern, times, curves)
	}
}

// pattern should return patterns, each of which should be embedded in the stream
Pembedn : Pn {
	embedInStream { |event|
		var	stream, result;
		stream = pattern.asStream;
		repeats.value.do({
			(result = stream.next(event)).notNil.if({
				event = result.embedInStream(event);
			}, { "Pembedn got nil, returning".warn; ^event });
		});
		^event
	}
}


// this should be used ONLY for event patterns that will be played on a clock
// streams may terminate prematurely if you use this in other contexts
// this is a workaround until I find a better solution
// and, to make a workaround more workaround-y,
// sometimes you want to allow a couple of zero-length embeds but not let it go infinitely
// so add a maxNull counter

PnNilSafe : Pn {
	var	<>maxNull;
	*new { |pattern, repeats = inf, maxNull = 0|
		^super.new(pattern, repeats).maxNull_(maxNull)
	}
	embedInStream { arg event;
		var	saveLogicalTime, counter = 0;
		repeats.value.do {
			saveLogicalTime = thisThread.clock.beats;
			event = pattern.embedInStream(event);
			if(thisThread.clock.beats == saveLogicalTime) {
				counter = counter + 1;
				if(counter > maxNull) { ^event };
			} {
				// one good embed resets the count
				counter = 0;
			};
		};
		^event;
	}
}


// Pstutter, but based on next value of child stream
// n-stream gets passed not the event, but the next value of the child
// child stream still gets passed the event
Psmartstutter : Pstutter {
	embedInStream { arg event;
		var inevent, nn;

		var stream = pattern.asStream;
		var nstream = n.asStream;

		while ({
			(inevent = stream.next(event)).notNil
		},{
			(nn = nstream.next(inevent)).notNil.if({
				nn.abs.do({
					event = inevent.copy.embedInStream(event);
				});
			}, { ^event });
		});
		^event;
	}
}


Pdelta : FilterPattern {
	var	<>cycle;
	*new { |pattern, cycle = 4|
		^super.newCopyArgs(pattern).cycle_(cycle)
	}

	embedInStream { |inval|
		var	stream = pattern.asStream,
			lastValue, value;
		(lastValue = stream.next(inval)).isNil.if({ ^inval });
		loop {
			while {
				(value = stream.next(inval)).isNil.if({ ^inval });
				value >= lastValue
			} {
				inval = (value - lastValue).yield;	// these must be numbers obviously
				lastValue = value;
			};
			lastValue = lastValue - (lastValue - value).roundUp(cycle);
			inval = (value - lastValue).yield;
			lastValue = value;
		};
		^inval
	}
}


// Sync time-points to the barline
// Provide time-points as \timept
// pattern must be an event pattern!
PTimePoints : FilterPattern {
	var <>tolerance;
	*new { |pattern, tolerance = 0.001|
		^super.new(pattern).tolerance_(tolerance);
	}
	embedInStream { |inval|
		var // now = thisThread.beats,
		beatInBar = thisThread.clock.beatInBar,
		stream = pattern.asStream,
		event = stream.next(inval.copy), oldEvent, timept;
		if(event.isNil) { ^inval };
		if(event[\timept].isNil) {
			^inval
		};
		timept = event[\timept] % thisThread.clock.beatsPerBar;
		if(timept absdif: beatInBar > tolerance) {
			inval = Event.silent(
				(timept - beatInBar).wrap(tolerance, thisThread.clock.beatsPerBar + tolerance),
				inval
			).yield;
			beatInBar = timept;
		};
		while {
			oldEvent = event;
			event = stream.next(inval);
			event.notNil and: { event[\timept].notNil }
		} {
			timept = event[\timept] % thisThread.clock.beatsPerBar;
			oldEvent[\dur] = (timept - beatInBar).wrap(tolerance, thisThread.clock.beatsPerBar + tolerance);
			inval = oldEvent.yield;
			beatInBar = timept;
		};
		if(oldEvent.notNil) {
			// resync to barline
			// there is no 'event' here so we have no idea how long oldEvent should be
			// without assuming a reference point. Barline is the most logical reference.
			oldEvent[\dur] = (tolerance - beatInBar) % thisThread.clock.beatsPerBar - tolerance;
			^oldEvent.yield
		} { ^inval }
	}
}


// record scratching goes forward and backward thru the audio stream
// Pscratch does the same for the output values of a pattern
// memory is finite (can only go backward so far)
// recommend to use Pwrand for stepPattern -- weights can give an overall positive direction

Pscratch : FilterPattern {
	var	<>stepPattern, <>memorySize;
	*new { |pattern, stepPattern, memorySize = 100|
		^super.newCopyArgs(pattern).stepPattern_(stepPattern).memorySize_(memorySize)
	}

	embedInStream { |inval|
		var	memSize = memorySize,	// protect against the instance variable changing
			memory = Array.newClear(memSize),	// a circular buffer
			origin = 0,
			bottomIndex = 0,	// memory.wrapAt(bottomIndex) will always be the OLDEST element
			outIndex = 0,
			stream = pattern.asStream,
			stepStream = stepPattern.asStream,
			value, step;

		while {
			(step = stepStream.next(inval)).notNil
		} {
			(step.isStrictlyPositive or: { value.isNil }).if({
				step = max(step, 1);	// step might be negative or 0 on first iteration
					// have I climbed out of the memory hole?
				(outIndex + step < bottomIndex).if({
						// no, so advance toward the top and return a previous value
					outIndex = outIndex + step;
					inval = memory.wrapAt(outIndex).embedInStream(inval);
				}, {
					(outIndex < (bottomIndex - 1)).if({
							// recover exactly up to bottomIndex
						step = step - bottomIndex + outIndex + 1;
						outIndex = bottomIndex - 1;
					});

						// advance the primary stream and record values in memory
						// output the last value obtained
					step.do({
						(value = stream.next(inval)).isNil.if({
							^inval
						});
						memory.wrapPut(bottomIndex, value);
						bottomIndex = bottomIndex + 1;
					});
					outIndex = bottomIndex - 1;
					inval = memory.wrapAt(outIndex).embedInStream(inval);
				});
			}, {
					// if negative or 0, decrease outIndex (only as far as legal)
					// and return prior value
				outIndex = max(outIndex + step, bottomIndex - memSize).max(0);
				inval = memory.wrapAt(outIndex).embedInStream(inval);
			});
		};
		^inval
	}
}


// Pconst doesn't really "constrain" -- it FITS the last value to match the desired sum
// This is what I really think "constrain" means... go as far as you can, then stop before hitting the limit

Plimitsum : Pconst {
	embedInStream { arg inval;
		var delta, elapsed = 0.0, nextElapsed, str=pattern.asStream,
			localSum = sum.value(inval);
		loop ({
			delta = str.next(inval);
			if(delta.isNil) {
				^inval
			};
			nextElapsed = elapsed + delta;
			if (nextElapsed.round(tolerance) >= localSum) {
				^inval
			}{
				elapsed = nextElapsed;
				inval = delta.yield;
			};
		});
	}
}


// Pswitch embeds list items in the stream; Pswitch1 embeds stream values singly
// Pwhile embeds its pattern in the stream; Pwhile1 embeds stream values singly

Pwhile1 : Pwhile {
	asStream { |cleanup| ^Routine({ arg inval; this.embedInStream(inval, cleanup) }) }
	embedInStream { |event, cleanup|
		var	stream = pattern.asStream, next;
		cleanup ?? { cleanup = EventStreamCleanup.new };
		while { (next = stream.next(event)).notNil } {
			if(func.value(event, next)) {
				cleanup.update(next);
				event = next.yield;
			} { ^cleanup.exit(event) }
		}
		^cleanup.exit(event)
	}
}


// delays a value by 'n' events
// 'n' can be set by a func but can't change while the Pdelay runs
// 'cuz I don't want to deal with interpolation in this version
Pdelay : FilterPattern {
	var	<>delay, <>maxDelay, <>default;
	*new { |pattern, delay = 1, maxDelay = 1, default|
		^super.new(pattern).delay_(delay).maxDelay_(maxDelay).default_(default)
	}

	embedInStream { |inval|
		var	dly = delay.value(inval),
			bsize = max(maxDelay, dly) + 1,
			buffer = Array.fill(bsize, default),
			stream = pattern.asStream,
			writeI = 0, readI = dly.neg,
			item;
		while { (item = stream.next(inval)).notNil } {
			buffer.wrapPut(writeI, item);
			inval = (buffer.wrapAt(readI) ?? { buffer[0] }).yield;
			writeI = writeI + 1;
			readI = readI + 1;
		};
			// input stream ended but the last 'dly' items haven't been yielded yet
		dly.do { |x|
			inval = buffer.wrapAt(max(readI + x, 0)).yield;
		};
		^inval
	}
}


PpatRewrite : FilterPattern {
	var	<>levelPattern, <>rules, <>defaultRule,
		<>autoStreamArrays = true,
		<>reuseLevelResults = false;

	*new { |pattern, levelPattern, rules, defaultRule,
		autoStreamArrays = true, reuseLevelResults = false|
		^super.new(pattern).levelPattern_(levelPattern)
			.rules_(rules)
			.defaultRule_(defaultRule ?? { nil -> { |in| in } })
			.autoStreamArrays_(autoStreamArrays).reuseLevelResults_(reuseLevelResults)
	}

	embedInStream { |inval|
		var	levelStream = levelPattern.asStream,
			level, outputs = List.new;
		while { (level = levelStream.next(inval)).notNil } {
			inval = this.recurse(inval, pattern.asStream, level, outputs);
		};
		^inval
	}

	recurse { |inval, inStream, level, outputs|
		var	rule;
		if(reuseLevelResults and: { outputs[level].notNil }) {
			^Pseq(outputs[level], 1).embedInStream(inval)
		} {
			// mondo sucko that I have to hack into the List
			outputs.array = outputs.array.extend(max(level+1, outputs.size));
			outputs[level] = List.new;
			if(level > 0) {
				r { |inval| this.recurse(inval, inStream, level-1, outputs) }
				.do { |item|
					case
					// matched a rule, use it
					{ (rule = rules.detect { |assn| assn.key.matchItem(item) }).notNil }
						{ inval = this.rewrite(item, rule, inval, level, outputs) }
					// matched the default rule
					{ defaultRule.key.matchItem(item) }
						{ inval = this.rewrite(item, defaultRule, inval, level, outputs) }
					// no match, just spit out the item unchanged
					{ outputs[level].add(item); inval = item.embedInStream(inval) };
				};
			} {
				inval = inStream.collect { |item|
					outputs[level].add(item);
					item
				}.embedInStream(inval);
			};
		};
		^inval
	}

	rewrite { |item, rule, inval, level, outputs|
		var	result = rule.value.value(item, level, inval);
		if(autoStreamArrays and: { result.isSequenceableCollection }) {
			result = Pseq(result, 1);
		};
		^result.asStream.collect { |item| outputs[level].add(item); item }.embedInStream(inval);
	}
}
