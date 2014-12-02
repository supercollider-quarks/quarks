
// miscellaneous Collection enhancements

+ SequenceableCollection {
		// find indices for every item found in another array
		// nils are ignored
	collectIndicesFromArray { arg sourceArray;
		var i, indices, me;
		me = this.flat;	// allow user to enable and disable many sockets
		indices = Array.new;
		me.do({ arg s;
			(i = sourceArray.indexOf(s)).notNil.if({
				indices = indices.add(i);
			});
		});
		^indices
	}
	
	collectIndicesOfItem { arg that;
		var	indices;
		indices = Array.new;
		this.do({ |item, i|
			(item === that).if({ indices = indices.add(i) })
		});
		^indices
	}
	
	collectIndices { |func|
		var	indices = Array(this.size);
		this.do({ |item, i|
			func.value(item, i).if({ indices = indices.add(i) });
		});
		^indices
	}
	
	detectIndexFrom { |func, start = 0, step = 1|
		var	index = start;
		{	index.inclusivelyBetween(0, this.size-1)
			and: { func.(this[index]).not } }
		.while {
			index = index + step;
		};
		index.inclusivelyBetween(0, this.size-1).if { ^index } { ^nil }
	}
	
	detectFrom { |func, start = 0, step = 1|
		var	index;
		(index = this.detectIndexFrom(func, start, step)).notNil.if
			{ ^this[index] } { ^nil }
	}

	oneOfTop { arg num;
		^this.at(num.min(this.size).rand)
	}
	
	mapMode { arg mode;
		^this.collect({ |item| item.mapMode(mode) });
	}
	
	unmapMode { arg mode;
		^this.collect({ |item| item.unmapMode(mode) });
	}
	
		// change an array of notes into a SequenceNote with an array of frequencies etc.
	asChord {
		var	freqs, lengths, args;
		freqs = Array.new(this.size);
		lengths = Array.new(this.size);
		args = Array.new(this.size);
		this.do({ |note|
			freqs.add(note.freq);
			lengths.add(note.length);
			args.add(note.args);
		});
		^SequenceNote(freqs, this[0].dur, lengths, args)
	}
	
	avgsmooth { |avgpts = 5|
		var	runsum = this[..avgpts-1].sum,
			out = this.class.new(this.size - avgpts + 1);
		(this.size - avgpts + 1).do({ |i|
			out.add(runsum / avgpts);
			runsum = runsum - this[i];
			this[i+avgpts].notNil.if({ runsum = runsum + this[i+avgpts]; });
		});
		^out
	}		
}

+ SimpleNumber {
	mapMode { arg mode;
		^mode.prMap(this)	// save a dispatch by going right to the pseudo-private method
	}
	
	unmapMode { arg mode;
		^mode.prUnmap(this)
	}
}

+ Pattern {
	mapMode { |mode|
		^Pcollect({ |item| item.mapMode(mode) }, this)
	}
	unmapMode { |mode|
		^Pcollect({ |item| item.unmapMode(mode) }, this)
	}
}

+ Stream {
	mapMode { |mode|
		^this.collect { |item| item.mapMode(mode) }
	}
	unmapMode { |mode|
		^this.collect { |item| item.unmapMode(mode) }
	}
}

+ Symbol {
		// no-op, just like \rest + 0
	mapMode {}
	unmapMode {}
}

// need a separate version of removeDups for Dictionary

+ Collection {
	removeDups {	// output a new collection without any duplicate values
		var result;
		result = this.species.new(this.size);
		this.do({ arg item;
			result.includes(item).not.if({ result.add(item) });
		});
		^result
	}	
}


+ Array {
		// make a new copy of an array with items weighted as given in weights
		// weights should all be integers; non-integers will be rounded
	weight { arg weights;
		var res;
		weights = weights.round(1);
		res = Array.new;
		this.do({ arg item, i;
			res = res ++ Array.fill(weights.wrapAt(i) ? 1, item);
		});
		^res
	}
}

+ String {
	left { arg len; ^this.copyRange(0, len-1) }
	mid { arg start, len; ^this.copyRange(start, start + len - 1) }
	right { arg len; ^this.copyRange(this.size - len, this.size-1) }

	hexToInt {
		// assumes each character of string is a valid hex digit (0-9, A-F)
		// doesn't handle negatives
		var result = 0, i, size, digit;

		this.do { |char|
			digit = char.digit;
			if(digit.notNil) {
				result = (result << 4) | digit;
			} {
				"Invalid hex digit found at % in %".format(char, this).warn;
				result = (result << 4);
			};
		};

		// size = this.size - 1;
		// i = size;		// start with rightmost digit
		// { (i >= 0) && result.notNil }.while({
		// 	(digit = this.at(i).digit).notNil.if({
		// 		result = result + (digit * (16 ** (size-i)));
		// 	}, {
		// 		result = nil;
		// 	});
		// 	i = i-1;
		// });
		^result
	}
}

+ Symbol {
	hexToInt { ^this.asString.hexToInt }
}

+ Collection {
	hexToInt {
		^this.collect({ arg x; x.hexToInt })
	}
	
	asHexString {
		^this.collect({ arg x; x.asInteger.asHexString })
	}
		
}

+ Integer {
		// unrelated but I'm sticking it here anyway
		// return a string showing midi note
	asMIDINote { arg midcOct = 4;	// middle c = c4
		^(#["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"]
			.at(this % 12))
			++ ((this / 12).asInteger - 5 + midcOct).asString
	}
}


+ SequenceableCollection {
		// lay this over that; where there are nils in this, substitute the
		// corresponding value in that
		// [0, nil, 2].maskOver([10, 1, 72]) --> [0, 1, 2]
	maskOver { arg that;
		^this.collect({ arg v, i; v ? that.wrapAt(i) });
	}

}

+ Nil {
	maskOver { arg that;
		^that
	}
}

+ Environment {
	*popAll {
		stack.size.do({ this.pop });
	}
}


+ Nil {
	nz { arg that = 1; ^that }	// so you don't have to nil-check before using nz
}

+ SimpleNumber {
	nz { arg that = 1; (this == 0).if({ ^that }); }	// this is returned by default
}


+ Symbol { isSymbol { ^true }}
+ Object {
	isSymbol { ^false }
	shallowCopyItems { ^this.shallowCopy }
	<!! { ^this }
}

+ Dictionary {
		// shallowCopy copies everything except kernel objects like funcs
		// use this on each item of the dict -- do recursion on dictionaries within
	shallowCopyItems {
		var	new;
		new = this.class.new(this.size);
		this.keysValuesDo({ |k, v|
			new.put(k, v.shallowCopyItems);
		});
		^new
	}
}

+ IdentityDictionary {
	shallowCopyItems {
		^super.shallowCopyItems
			.proto_(proto)	// child object should not be modifying parent/proto
			.parent_(parent)	// so we don't copy them
			.know_(know)
	}
}

+ Spec {
	*listAll {
		specs.asSortedArray.do(_.postcs)
	}
}

+ Server {
	clumpListSendBundle { |time, bundle, delta = 0.05, clumpSize = 5|
		bundle.clump(clumpSize).do({ |segment, i|
			this.listSendBundle((time ? 0.1) + (i * delta), segment);
		});
	}
}

+ Event {
	*makeProto { |parentKey = \default|
		^Event.new(parent: parentEvents[parentKey])
	}
}

// flattening all collections (not just arrays)

+ Collection {
	asFlatArray { ^this.asArray.asFlatArray }
}

+ Object {
	asFlatArray { ^this }	// non-collections should remain as is - name is slightly misleading
}

+ SequenceableCollection {
	asFlatArray { ^this.collect(_.asFlatArray).flat }
}

+ Object {
	releaseFromDependencies {
			// keysValuesDo is not OK
			// because Model and subclasses implement dependants differently
		dependantsDictionary.keysDo({ |obj|
			obj.dependants.includes(this).if({
				obj.removeDependant(this)
			});
		});
	}
}


// some general purpose default envelopes

+ Env {
	*zero { |time = 1| ^Env.new([0, 0], [time]) }
	
	*one { |time = 1| ^Env.new([1, 1], [time]) }
}



// wavetable buffer plotter

+ Buffer {
	plotWavetable { |name, bounds|
		this.getToFloatArray(wait: 0.05, action: { |v|
			defer {
				var	result = FloatArray.new(v.size div: 2);
				v.pairsDo({ |a, b| result.add(a+b) });
				result.plot;
			}
		});
	}
}


+ Object {
	isKindOfByName { |className|
		var	class;
		(class = className.asClass).notNil.if({
			^this.isKindOf(class)
		}, {
			^false
		});
	}
}
