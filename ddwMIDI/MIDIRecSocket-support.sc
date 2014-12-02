
// todo: beatsPerBar in quantize defs
// translator classes are deprecated

MIDIRecBuf {
	var	<>name, <notes,	// notes is array of SequenceNotes
						// except, args will be velocity only
						// to play, must translate args into [\gate, Pseq(...)]
		<>absoluteOnsets = false,	// MIDIRecSocket now records as deltas,
								// not absolute times
		<>properties,		// a dict with user-defined characteristics
		
		>stopRecTime;		// a private variable to make sure last dur is correct
						// in convertToDeltas
		
	*new { arg name, notesArray, properties;
		^super.newCopyArgs(name, notesArray ?? { Array.new }).properties_(properties);
	}
	
	storeArgs { ^[name, notes, properties] }
	
	empty {
		notes = Array.new;
	}
	
	convertToDeltas {	// must be done only once!!
		var baseTime, runningTime, i;
		notes = notes.copy.sort({ |a, b| a.dur < b.dur });
		(absoluteOnsets and: (notes.size > 0)).if({
			baseTime = notes.first.dur;
			runningTime = 0;
			i = 0;
			{ i < (notes.size-1) }.while({
				notes[i].dur = notes[i+1].dur - runningTime - baseTime;
				runningTime = runningTime + notes[i].dur;
				i = i+1;
			});
			stopRecTime.notNil.if({
				notes[i].dur = stopRecTime - runningTime - baseTime;
				stopRecTime = nil;		// not needed anymore
			}, {
					// else, this wasn't a recorded buf and you really shouldn't be calling
					// this method. But, we have to fudge something.
				notes[i].dur = notes[i].length;
			});

			absoluteOnsets = false;
		});
	}
	
	convertGates {	// output a new Buf
		^this.class.new(name, notes.collect({ |n| 
			n.convertGates
		})).absoluteOnsets_(absoluteOnsets)
	}
	
	transpose { |steps = 0, newName|
		^(steps == 0).if({ this.copy.name_(newName ?? { name }) }, {
			newName.isNil.if({
				newName = (name ++ ("%%".format(
					steps.isPositive.if("+", ""),	// - will be inserted by rendering the #
					steps
				)));
			});
			this.class.new(newName, notes + steps, properties);
		});
	}
	
	dumpSeq {
		("\n\nMIDIRecBuf(" ++ 34.asAscii ++ name ++ 34.asAscii ++ ")").postln;
		notes.do({ |n, i| i.post; " : ".post; n.postln; })
	}
	
	asMIDIRecBuf { ^this }
	asPattern { ^Pseq(notes, 1) }
	asStream { ^this.asPattern.asStream }
	embedInStream { |inval| ^this.asPattern.embedInStream(inval) }
	
///////////// Some Collection-style methods for easier access
	
	at { arg i; ^notes.at(i) }	// also invoked by buf[index]
	
	first { ^notes.first }
	last { ^notes.last }
	
	add { arg note;
		notes = notes.add(note);
	}
	
	includes { arg item1;
		notes.do({ |item2| (item1 == item2).if({ ^true }); });
		^false
	}

		// useful for arpeggiator-processes
	removeDups { // arg key;	// key: maybe you want dup freqs removed, maybe gates, ...?
						// this is the method to get the value from the SequenceNote
						// SequenceNote supports only freq here
		var result;
		result = this.class.new(name, nil, properties);  // keep old name
		notes.do({ arg item;
			result.includes(item).not.if({ result.add(item) });
		});
		^result
	}
	
	sort { arg func;	// keys?
		^this.class.new(name, notes.copy.sort(func), properties);
	}
	
	++ { arg that;
		^this.class.new(name, notes ++ that.asArray, properties)
	}
	
	reverse { ^this.class.new(name, notes.reverse, properties) }
	
	scramble { ^this.class.new(name, notes.scramble, properties) }
	
	copyRange { arg start, stop;
		^this.class.new(name, notes.copyRange(start, stop).properties_(properties))
	}
	
	size { ^notes.size }
	
	asArray { ^notes }		// generally useful; see this-++ for a specific use
	
	copy { ^this.class.new(name, notes.deepCopy).properties_(properties) }
	
	copySeqNotes {
		^this.class.new(name, notes.collect(_.asSequenceNote)).properties_(properties)
	}
	
	copyMelNotes {
		^this.class.new(name, notes.collect(_.asMelodyNote)).properties_(properties)
	}
	
	copyTopNotes {
		^this.class.new(name, notes.collect(_.asTopNote)).properties_(properties)
	}
	
	mapMode { |mode|
		var	n;
		n = notes.collect({ |n| n.copy.freq_(n.freq.mapMode(mode)) });
		^this.class.new(name, n).properties_(properties)
	}
	
	unmapMode { |mode|
		var	n;
		n = notes.collect({ |n| n.copy.freq_(n.freq.unmapMode(mode)) });
		^this.class.new(name, n).properties_(properties)
	}
	
/////////////	These methods extract arrays from individual SequenceNotes,
///////////// for use in NoteSequences. You wrap the returned arrays in
///////////// whatever pattern you want.

	getArray { arg method = \freq;
		^notes.collect({ arg n; n.perform(method) });
	}

	midiNotes { ^this.getArray(\freq) }
	
	freqs {
		var	f = this.getArray(\freq),
			tuning = 	properties.tryPerform(\at, \tuning)
				?? { properties.tryPerform(\at, \mode).tryPerform(\asMode)
					.tryPerform(\cpsFunc) };
			
		tuning.notNil.if({ ^tuning.value(f) }, { ^f.midicps });
	}
	
	durs { ^this.getArray(\dur) }
	
	lengths { ^this.getArray(\length) }
	
	gates { ^this.getArray(\gate) }
	args { ^this.getArray(\args) }

	hasQuantizeProperties {
		^(properties.size > 0 and: { properties[\factor].notNil })
	}

	quantize { arg factor, beatsPerBar;
		var	onset = 0, lastOnset = 0,	// running onset times for each note
			newnote, prevnote, j;
		var b;
		factor = factor ?? { properties.tryPerform(\at, \factor) ?? { 0.25 } };
		beatsPerBar = beatsPerBar ?? { properties.tryPerform(\at, \beatsPerBar) };
		b = Array.new;
		notes.do({ arg note, i;
			b = b ++ (newnote = note.copy.dur_(note.dur.round(factor)))
				.length_(note.length * newnote.dur / note.dur);
			j = b.size - 2;	// scan backward for 0 length notes
			{ j >= 0 and: { b[j].length == 0 } }.while({
					// take original length and apply ratio to this note's (nonzero) dur
				b[j].length = notes[j].length * newnote.dur / notes[j].dur;
				j = j - 1;
			});
			onset = onset + newnote.dur;
		});
		(beatsPerBar.notNil and: { beatsPerBar > 0 }).if({
				// fill out to end of bar if bar length specified
			b.last.dur_(beatsPerBar - ((onset - b.last.dur) % beatsPerBar));
		});
		^this.class.new(name, b, properties)
	}
	
		// attempt to quantize without requiring material to be exactly in tempo
		// that is, if tempo of buffer material is internally consistent but not
		// exactly in clock's tempo, try to find the tempo and adjust to the clock

		// this works well under some circumstances but fails in others -- unclear why

	baseRhythmicValue { arg factor, clock, error = 0.1;
		var	durs, /*error,*/ base, gcd,
			forwardAvg, backwardAvg, forwardSum, backwardSum, i;  // for outlier searching
		error = error * (clock ? TempoClock.default).tempo;  // at fast tempo, error must be higher
		
			// drop near-zero durs (these are chord notes that shouldn't figure into calcs)
			// sort them so shortest values come first
		durs = this.durs.select({ |item| item > error }).sort
				// clump durs into note values -- b-a is always >= 0 b/c array is sorted
			.separate({ |a, b| (b-a) > error });

			// base, by end, should be gcd of averages of all subarrays
			// if no gcd is found, base remains the average of lowest note value
		base = nil;
		durs.do({ |noteval|
			base.isNil.if({
				base = noteval.median;  // changing mean to median for now
			}, {
					// calculate running averages in both directions to search for outliers
					// this algorithm is more robust; however, if there's an outlier in
					// the smallest note value, it could still fail
				forwardAvg = Array.newClear(noteval.size);  // empty arrays for averages
				backwardAvg = Array.newClear(noteval.size);
				forwardSum = backwardSum = 0;
				noteval.size.reverseDo({ |i, j|
					forwardSum = forwardSum + noteval[j];
					backwardSum = backwardSum + noteval[i];
					forwardAvg.put(i, forwardSum / (j+1));
					backwardAvg.put(i, backwardSum / (j+1));
				});
				gcd = nil;	// test subsequent averages
				i = 0;
				{ gcd.isNil and: { i < forwardAvg.size }}.while({
					(gcd = base.fuzzygcd(forwardAvg[i], error)).notNil.if({
						base = gcd;
					}, {
						(gcd = base.fuzzygcd(backwardAvg[i], error)).notNil.if({
							base = gcd;
						});
					});
					i = i + 1;
				});		// end while
			});		// end base.isNil.if
		});		// end durs.do
		
		(base < ((factor ? 0.25) / 2)).if({ ^nil }, { ^base });	// quantization failed, return nil
	}
	
	flexQuantize { arg factor, clock, error;
			// here, the algorithm will attempt to determine the base value present in the actual
			// notes played, and map that to the nearest "factor"
		var	buf, durs, base, adjustedBase, newdur;

		factor = factor ?? { properties.tryPerform(\at, \factor) ?? { 0.25 } };
		error = error ?? { properties.tryPerform(\at, \error) ?? { 0.1 } };
		
			// fail gracefully, or continue
		(base = this.baseRhythmicValue(factor, clock, error)).isNil.if({ ^nil });
		
		adjustedBase = base.round(factor);	// round base to nearest quant
		buf = this.copy;	// make a new buf
		durs = this.durs;
		buf.notes.do({ |note, i|
				// scale to integer level, round to int, and scale back to adjusted value
			newdur = (durs[i] / base).round * adjustedBase;
			(newdur > 0).if({
				note.length_(note.length * newdur / durs[i]);  // keep articulation as is
			}, {
				note.length_(note.length * adjustedBase / base);  // else scale note length
			});
			note.dur_(newdur);
		});
		
		^buf
	}
	
		// parse into more complex note types.
		// Allows sequences with chords and gracenotes to be quantized.
	parse { |deltaThresh, overlapThresh, allowShortNotes|
		var 	newbuf, notestemp, newNote, newNote2, oldIndex, startIndex, totalChordDelta,
			swaptemp;

			// cannot parse absolute onsets
		absoluteOnsets.if({
			^this.copy.convertToDeltas.parse(deltaThresh, overlapThresh, allowShortNotes);
		});

		deltaThresh = deltaThresh ?? { properties.tryPerform(\at, \deltaThresh) ?? { 0.1 } };
		overlapThresh = overlapThresh ?? { properties.tryPerform(\at, \overlapThresh) ?? { 0.1 } };
		allowShortNotes = allowShortNotes ??
			{ properties.tryPerform(\allowShortNotes) ?? { true } };

		newbuf = this.class.new(name ++ "b", []).properties_(properties);

		notestemp = notes;
		oldIndex = 0;
		{ oldIndex < notestemp.size }.while({
				// locate first note to be "main" (non-grace note)
			startIndex = oldIndex;
			{ oldIndex < notestemp.size and:
				{ notestemp[oldIndex].isGraceNote(deltaThresh, overlapThresh, allowShortNotes) }
			}.while({
					oldIndex = oldIndex + 1;
			});
				// create the note in the newbuf and add gracenotes
			newNote = notestemp[oldIndex] ?? { oldIndex = oldIndex - 1; notestemp.last };
			(oldIndex > startIndex).if({
				newNote = newNote.addGraceNotes(notestemp.copyRange(startIndex, oldIndex-1));
			});
				// while the overlap of previous note is high, they're chord notes
			totalChordDelta = notestemp[oldIndex].dur;
			oldIndex = oldIndex + 1;
			{ oldIndex < notestemp.size and:
					// calculates percentage of this note that overlaps with the last
					// if this note terminates before the main note, result > 1.0
					// which is OK b/c the test is >=
				{ notes[oldIndex-1].dur < deltaThresh and:
				  (((newNote.length - totalChordDelta) / notestemp[oldIndex].length)
				  		>= overlapThresh) } }.while({
						// got a chord note: now look for gracenotes within the chord
					startIndex = oldIndex;
					{ oldIndex < notestemp.size and:
						{ notestemp[oldIndex].isGraceNote(deltaThresh, overlapThresh) } }
					.while({
						oldIndex = oldIndex + 1;
					});
					newNote2 = notestemp[oldIndex];
					(oldIndex > startIndex).if({
						newNote2 = newNote2
							.addGraceNotes(notestemp.copyRange(startIndex, oldIndex-1));
					});
					newNote = newNote.add(newNote2);
					totalChordDelta = totalChordDelta + notestemp[oldIndex].dur;
					oldIndex = oldIndex + 1;
			});
				// done with this note
			newbuf.add(newNote);
		});
		(newbuf.notes.size > 0).if({
			^newbuf	// valid notes were created
		}, {
			^this		// all grace notes, just use the plain buffer
		});
	}

}

MIDIBufManager {
	// allows you to record midi note streams into multiple buffers (client-side)
	// controllers are ignored at present, maybe I'll add that later

		// when adding a buf, if I already have one of the same name, overwrite or throw error?
	classvar	<>overwrite = true;

	var	<active,	// am I on?
		<bufs,	// array of MIDIRecBufs
		<midiControl,	// MIDIController to choose a buf
		<midiChannel,
		<recorder,
		<clock,
		<value = 0,		// which buf am I looking at now?
		<>postRecFunc,	// executed after recording; user-definable, can set properties for buf
						// receives buf as arg, should return new buf
		<>view,		// will be guiable
		<>name,		// see asString
		<>properties;	// to assign to new buffers
	
	*new { arg clock, chan, ccnum;
		^super.new.init(clock, chan, ccnum)
	}

	init { arg cl, chan, ccnum;
		midiChannel = chan !? { chan.asChannelIndex };
		clock = cl ?? { TempoClock.default };
			// make a midi controller if asked, otherwise 
		chan.notNil.if({
			midiControl = BufManagerMIDIControl(midiChannel, ccnum, this);
		});
		bufs = List.new;
		active = true;
	}
	
	free {
		recorder.notNil.if({ recorder.stopRecord; });	// no need to keep going
		view.notNil.if({ view.remove });
		midiControl.free;
		bufs = midiControl = midiChannel = recorder = clock = value = view = nil;  // garbage
		active = false;
		MIDIPort.update;
	}
	
	add { arg buf;	// bufs can be added programmatically
		var index, nameTemp;
		bufs.includes(buf).not.if({
				// is there already a buf with this name?
			nameTemp = buf.name.asSymbol;
			(index = this.indexOf(nameTemp)).notNil.if({
				overwrite.if({ bufs[index] = buf; },
					{ MethodError("MIDIRecSocket-add: already have a buf with name "
						++ nameTemp, this).throw });
			}, {
				bufs.add(buf);
			});
			view.notNil.if({ view.refresh })
		})
	}
	
	remove { arg buf;
		var res;
		(res = bufs.remove(buf)).notNil.if({
			view.notNil.if({ view.refresh });
		});
		^res
	}
	
	removeAt { |index|
		var res;
		(res = bufs.removeAt(index)).notNil.if({
			view.notNil.if({ view.refresh });
		});
		^res
	}
	
	removeCurrent {
		^this.removeAt(value)
	}
	
	value_ { arg v, updateGUI = true;
		v.isNumber.not.if({ v = this.indexOf(v) });
		value = v.wrap(0, bufs.size);	// ensure value is in array range
		(updateGUI and: { view.notNil }).if({ view.refresh(this); });
	}
	
	current { ^bufs.at(value) }
	
	at { |index|
		index.isInteger.if({ ^bufs[index] }, {
				// index may be buf name also -- index -1 if not found, returns nil
			^bufs[this.indexOf(index) ? -1]
		});
	}
	
	indexOf { |name|
		name = name.asSymbol;
		^bufs.detectIndex({ |b| b.name.asSymbol == name })
	}
	
	initRecord { |properties, new = true|
		(new ? true).if({ this.value_(bufs.size) });
		recorder = MIDIRecSocket(midiChannel, clock, properties);
		view.notNil.if({ view.refresh });
	}
	
	stopRecord {
		var newBuf;
		newBuf = recorder.stopRecord;
		protect {
			(newBuf.size > 0).if({
					  // if postRecFunc is nil, don't change value
				newBuf = postRecFunc.value(newBuf) ? newBuf;
	
// how do I know if postRecFunc set the name (to avoid resetting by user?)
	
				(value == bufs.size).if({	// creating a new buffer
					bufs.add(newBuf);
					view.tryPerform(\setName);	// focus namesetter field
				}, {
					bufs.put(value, newBuf);
					newBuf.name = bufs[value].name;	// updating old one, keep name
				});
			});
			this.changed(\finishedMIDIRecord, newBuf);
		} {		// must do this before throwing error
			recorder = nil;	// must garbage to be ready for next record call
			view.notNil.if({ view.refresh });
		};
		^newBuf
	}
	
	guiClass { ^MIDIRecGUI }
	
	menuItems {
		^bufs.asArray.collect({ arg b; b.name }) ++ ["Create new"]
	}
	
	asMIDIRecBuf { ^this.current }
	
	asString { ^"MIDIBufManager : " ++ name }

		// MTGui is defined in chucklib - technically a dependency
		// but this method should never be invoked except by dragging into a chucklib object
	draggedIntoMTGui { |gui, index|
		var	mt;
		mt = gui.model;
		this.current => mt.v[index].bp;
	}
}
