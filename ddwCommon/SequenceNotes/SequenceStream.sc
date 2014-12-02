
	// now a subclass of AbstractFunction so that you can do math on them
	// this is the simple variety: no grace notes or chord notes
	// can be played simply by using the vars as voicer parms for example
	// typically you're doing math on the frequencies
SequenceNote : AbstractFunction {
	var	<>freq, <>dur, <>length, <>args;		// values for this note

		// note, newCopyArgs will break if any instance variables are added to AbstractFunction
	*new { arg freq, dur, length, args;
		^super.newCopyArgs(freq, dur, length, args);
	}

	mapMode { |mode|
		^this.copy.freq_(freq.mapMode(mode))
	}

	unmapMode { |mode|
		^this.copy.freq_(freq.unmapMode(mode))
	}

	asString { ^this.asArray.asCompileString }

	storeArgs { ^[freq, dur, length, args] }

	composeUnaryOp { |selector|
		^this.copy.freq_(freq.perform(selector))
	}
	composeBinaryOp { arg aSelector, note;
		^this.copy.freq_(note.numPerformBinaryOpOnNumber(aSelector, freq))
	}

// this needs to be tested
	reverseComposeBinaryOp { arg aSelector, something;
		^something.perform(aSelector, this.asFloat)
	}
	composeNAryOp { arg aSelector, anArgList;
		^this.copy.freq_(freq.performList(aSelector, anArgList))
	}

	// double dispatch for mixed operations
	// this handles the situation of 5 + SequenceNote() -- a note should be returned
	performBinaryOpOnSimpleNumber { arg aSelector, aNumber;
		var result;

		(result = aNumber.perform(aSelector, this.freq)).respondsTo('+').if({
			^this.copy.freq_(result)	// note should be output only if the result is a number
		}, {
			^result
		});
	}
	// this handles note + number, but returns number so composeBinaryOp can wrap it correctly
	numPerformBinaryOpOnNumber { arg aSelector, aNumber;
		^aNumber.perform(aSelector, freq)
	}

		// compares frequency only. To compare the whole note use identity ===
		// these do not work if you do 3 == aNote
	== { |that| ^that.numPerformBinaryOpOnNumber('==', this.freq) }
	!= { |that| ^that.numPerformBinaryOpOnNumber('!=', this.freq) }
	< { |that| ^that.numPerformBinaryOpOnNumber('<', this.freq) }
	> { |that| ^that.numPerformBinaryOpOnNumber('>', this.freq) }
	<= { |that| ^that.numPerformBinaryOpOnNumber('<=', this.freq) }
	>= { |that| ^that.numPerformBinaryOpOnNumber('>=', this.freq) }

		// some freq calculations will require end result to be a number
	asInteger { ^freq.asInteger }
	asFloat { ^freq.asFloat }

	convertGates {
		args.isNumber.if({
			^this.copy.args_([\gate, args])
		});
	}	// else, output this by default

		// act like an instance variable
		// will replace with a real instance variable later
	gate {
		var	index;
		(args.size == 0).if({ ^args }, {
			^(index = args.indexOf(\gate)).notNil.if({ args[index+1] });  // nil otherwise
		});
		^nil
	}

	gate_ { |gate|
		var	index;
		args.isNil.if({ args = gate }, {
			(index = args.indexOf(\gate)).notNil.if({
				args[index+1] = gate;
			}, {
				args = args ++ [\gate, gate];
			});
		});
	}

	// maybe make this a dictionary sometime
	argAt { |key|
		var i;
		if(args.isArray) {
			if((i = args.indexOf(key)).notNil) {
				^args[i+1]
			}
		};
		^nil
	}

	argPut { |key, value|
		var i;
		if(args.isArray) {
			if((i = args.indexOf(key)).notNil) {
				args = args.copy.put(i+1, value);
			} {
				args = args ++ [key, value];
			}
		} {
			if(args.isNumber) {
				args = [gate: args, key, value]
			} {
				args = [key, value];
			}
		};
	}

		// simplify parsing
	isGraceNote { |deltaThresh = 0.1, overlapThresh = 0.1, allowShortNotes = true|
			// gracenotes must be short and have a small overlap
		^(dur < deltaThresh) and: {
			allowShortNotes.if({ (length - dur) < overlapThresh },
				{ (length - dur) >= 0 and: { (length - dur) < overlapThresh } })
		}
	}

		// add chord notes -- this changes the type to SeqChordNote
	add { |note|
		^SeqChordNote(freq, dur, length, args).add(note)
	}
	++ { |notes|
		^SeqChordNote(freq, dur, length, args) ++ notes
	}

	addGraceNotes { |notes|
		^SequenceItem(freq, dur, length, args, notes)
	}

		// support for more complex note types
	asPlayableNote { ^this }
	asSequenceNote { ^this }
	asMelodyNote { ^this }
	asTopNote { ^this }

	asNoteArray { ^[this] }

	isChord { ^false }		// only 1 note

	asArray { ^[freq, dur, length, args] }

		// more complex types have to be read as a stream (to play grace notes)
		// this is for compatibility

		// it is actually more efficient not to implement embedInStream here,
		// since embedInStream should simply do this.yield (as in Object) for a SequenceNote
	asPattern { ^Pn(this, 1) }
	asStream { ^this.asPattern.asStream }

	shortDur { ^dur }	// var is not needed here, but subclasses use it
					// all the different note types should be interchangeable

}

// the next 2 classes will allaw chords and gracenotes to be played into a melodic
// sequence while melodic process calculate off the main note
// the math implementation for all is the same, but the play implementation is different

SeqChordNote : SequenceNote {	// chord, no grace notes
	var	<>chordNotes,		// array of SequenceNotes - should not contain SeqChordNotes
		<>shortDur;		// shortDur = dur as played; dur = dur + dur of chordnotes
						// so that you can play w/o grace notes and keep rhythm
						// chord notes are stored relative to freq for easy transposition

	*new { arg freq, dur, length, args, chordNotes;
		^super.new(freq, dur, length, args)
			.shortDur_(dur)	// initialize shortdur
			++ chordNotes
	}

	storeArgs { ^[freq, shortDur, length, args, chordNotes+freq] }

	copy { ^this.deepCopy }		// must copy everything or there will be trouble

	convertGates {
		chordNotes = chordNotes.collect({ |n| n.convertGates });
		^super.convertGates	// handle the main note
	}

		// works with only one note
	add { |note|
		dur = dur + note.dur;	// chord dur must be the *sum* of the note deltas
		chordNotes = chordNotes.asArray ++ (note.asMelodyNote - freq)
	}

	++ { |notes|
		chordNotes = chordNotes.asArray ++ notes.collect({ |n|
			dur = dur + n.dur;  // faster than .collect({ |n| n.dur }).sum (then another collect)
			n.asMelodyNote - freq
		})
	}

	addGraceNotes { |notes|
		^SequenceItem(freq, dur, length, args, notes, chordNotes)
	}

		// outputs a new object
	mapMode { |mode|
		var	newFreq;
		newFreq = freq.mapMode(mode);
		^SeqChordNote(newFreq, shortDur, length, args,
			chordNotes.isNil.if({ nil },
				{ (chordNotes + freq).mapMode(mode) }))
	}

	unmapMode { |mode|
		var	newFreq;
		newFreq = freq.unmapMode(mode);
		^SeqChordNote(newFreq, shortDur, length, args,
			chordNotes.isNil.if({ nil },
				{ (chordNotes + freq).unmapMode(mode) }))
	}

		// changing duration needs to scale all rhythmic values
	dur_ { |newDur|
		var	ratio;
		ratio = newDur / dur;
		chordNotes.do({ |n|
			n.dur_(n.dur * ratio); // .length_(n.length * ratio)
		});
		shortDur = shortDur * ratio;
		dur = newDur;
	}

	asPlayableNote {
		var	f, d, l, a, newSize;
		(chordNotes.size > 0).if({
			f = Array.new(newSize = chordNotes.size + 1).add(freq);
			l = Array.new(newSize).add(length);
			a = Array.new(newSize).add(args);
			chordNotes.do({ |n|
				f.add(n.freq + freq);
				l.add(n.length);
				a.add(n.args);
			});
			^SequenceNote(f, dur, l, a)
		}, {
			^this
		});
	}

	asNoteArray { ^[this.asMelodyNote] ++ (chordNotes + freq) }

	isChord { ^chordNotes.size > 0 }

		// if this method is used, chord notes are not wanted but grace notes might be
	asMelodyNote { ^this.copy.chordNotes_(nil) }
	asSequenceNote { ^SequenceNote(freq, dur, length, args) }
	asTopNote {
		var	highNote;
			// chordNotes must exist for the test to be valid
		((highNote = chordNotes !? { chordNotes.maxItem }).notNil
			and: { highNote + freq > this }).if({
				^SequenceNote(highNote.freq + freq, dur, length, args)
			}, {
				^this.asSequenceNote;
			});
	}


	asPattern { ^Pseq([SequenceNote(freq, shortDur, length, args)] ++ (chordNotes + freq), 1) }
	embedInStream { |inval| ^this.asPattern.embedInStream(inval) }
}

SequenceItem : SeqChordNote {	// add grace notes
	var	<>graceNotes;

	*new { arg freq, dur, length, args, graceNotes, chordNotes;
		^(super.new(freq, dur, length, args) ++ chordNotes)
			.shortDur_(dur)	// initialize shortdur
			.addGraceNotes(graceNotes)
	}

	storeArgs { ^[freq, shortDur, length, args, graceNotes+freq, chordNotes+freq] }

	mapMode { |mode|
		var	newFreq;
		newFreq = freq.mapMode(mode);
		^SequenceItem(newFreq, shortDur, length, args,
			graceNotes.isNil.if({ nil },
				{ (graceNotes + freq).mapMode(mode) }),
			chordNotes.isNil.if({ nil },
				{ (chordNotes + freq).mapMode(mode) }))
	}

	unmapMode { |mode|
		var	newFreq;
		newFreq = freq.unmapMode(mode);
		^SequenceItem(newFreq, shortDur, length, args,
			graceNotes.isNil.if({ nil },
				{ (graceNotes + freq).unmapMode(mode) }),
			chordNotes.isNil.if({ nil },
				{ (chordNotes + freq).unmapMode(mode) }))
	}

	dur_ { |newDur|
		var	ratio;
		ratio = newDur / dur;
		chordNotes.do({ |n|
			n.dur_(n.dur * ratio); // .length_(n.length * ratio)
		});
			// could use super.dur_ but this saves a dispatch
		graceNotes.do({ |n|
			n.dur_(n.dur * ratio); // .length_(n.length * ratio)
		});
		shortDur = shortDur * ratio;
		dur = newDur;
	}

	convertGates {
		graceNotes = graceNotes.collect({ |n| n.convertGates });
		^super.convertGates	// handle the chord notes and main note
	}

	addGraceNotes { |notes|
		graceNotes = graceNotes.asArray ++ notes.collect({ |n|
			dur = dur + n.dur;
			n.asSequenceNote - freq		// or asMelodyNote?
		});
	}

	// asPlayableNote should drop the grace notes because grace notes can't be represented
	// as one playable note - hence superclass implementation is sufficient

	asNoteArray { ^(graceNotes + freq) ++ super.asNoteArray }

	asPattern { ^Pseq(
			(graceNotes.size > 0).if({ [Pseq(graceNotes + freq, 1)] }, { Array.new })
				++ SequenceNote(freq, shortDur, length, args)
				++ (chordNotes.size > 0).if({ Pseq(chordNotes + freq, 1) })
			, 1)
	}

}
