
// adaptation functions
var saveSubType = AbstractChuckArray.defaultSubType;

// first, evaluate fitness
// these are very basic defaults, user can customize them for certain materials

// write adTest later

#{ |orig, test|
	var	out;
	out = orig.metric.absdif(test.metric);
	(out > (orig.metric*0.25)).if(out);	// otherwise nil
} => Func(\eugTest).subType_(\eugTest);

#{ nil } => Func(\dummyEugTest).subType_(\eugTest);

// first checks range as a hard limit
// BP(\melodicProcess).v.range = NumericRange(lo, hi)
{ |orig, test, passInValue|
	var	range;
	((range = passInValue[\range]).notNil and: {
			test.loNote < range.lo or: { test.hiNote > range.hi } }).if(100);
} => Func(\eugRangeOnlyTest).subType_(\eugTest);

// if in range, returns result of eugTest; otherwise, 100 to guarantee failure
{ |orig, test, passInValue|
	var	out;
	(out = Func(\eugRangeOnlyTest).doAction(orig, test, passInValue)).isNil.if({
		out = Func(\eugTest).doAction(orig, test, passInValue)
	});
	out
} => Func(\eugRangeTest).subType_(\eugTest);


// design may change if I need to pass extra args in

// material should always be in array form -- make those changes later

AbstractChuckArray.defaultSubType = \melAdapt;

#{ |source, cross|	// absolute splice
	var	s1, s2, newSeg, splice, spl2size, spl2;
	s1 = source.notes;		// not worrying about weighting b/c poor adaptations
	s2 = cross.notes;		// will die after a few generations
	splice = ((s1.size-2).rand + 1).max(1);	// splice coordinates
	spl2size = rrand(3, (s2.size * 0.7).roundUp.asInteger);
	spl2 = (s2.size - spl2size).rand;
	newSeg = s1.copyRange(0, splice-1);
	newSeg = newSeg ++ s2.copyRange(spl2, spl2 + spl2size);
	newSeg ++ s1.copyRange(splice, s1.size - 1);
} => Func(\absSplice);

#{ |source, cross|
	// diatonic splice -- take part of s1 as is, insert intervals from part of s2,
	// and finish with s1
	var s1, s2, temp, s1ang, splice, current, new, spl2, spl2size;
	splice = ((source.notes.size-2).rand + 1).max(1);
	spl2size = rrand(3, (cross.notes.size * 0.7).round.asInteger);
	spl2 = (cross.notes.size - spl2size).rand.max(1);
	(spl2size + spl2 >= cross.notes.size).if({ spl2size = cross.notes.size - spl2 });

	(spl2size >= 2).if({
		new = source.notes.copyRange(0, splice-1);
			// last diatonically mapped note + first spliced interval - s2 dia map note
		current = (source.notes[splice-1] + cross.intervals[spl2 - 1] -
			cross.notes[spl2].freq).asFloat;
		new = new ++ (cross.notes.copyRange(spl2, spl2 + spl2size - 1) + current);
			// last note copied + transposition + next interval from source - next note (source)
			// algebraically simplifies to last note(s2) + transposition - last note (source)
		current = cross.notes[spl2 + spl2size - 1].freq + current - source.notes[splice-1].freq;
		new ++ (source.notes.copyRange(splice, source.notes.size - 1) + current);
	});
} => Func(\intSplice);

// delete some notes randomly from the segment
#{ |source|
	var	halfSize, deleteFrom, deleteTo;
	(source.notes.size > 6).if({
		halfSize = source.notes.size >> 1;
		deleteFrom = halfSize.rand;
		deleteTo = min(source.notes.size - 2, halfSize.rand + halfSize);
		source.notes[0..deleteFrom] ++ source.notes[deleteTo..source.notes.size-1]
	});	// else return nil (failed)
} => Func(\delete);

// delete some notes, keeping the segment duration the same
// issue: how to determine whether to sustain the previous note, or leave a rest?
// for now just leave a rest
#{ |source|
	var	lo, hi, numToDrop, newNote;
	source = source.notes.copy;	// must copy array before modifying
	lo = rand(source.size-1)+1;	// can't drop first note
	numToDrop = rand((source.size - 1).min((source.size * 0.25).asInteger));
	hi = (lo + numToDrop).min(source.size-1);
		// copy the note before modifying
	source.put(lo-1, source[lo-1].copy);
		// add the deleted notes' deltas to the new note's delta
	for(lo, hi, { |i|
		source[lo-1].dur = source[lo-1].dur + source[i].dur;
	});
	(source[0..lo-1] ++ source[hi+1..source.size-1])
} => Func(\delHoldDur);

// fixed-duration splice: output will be exactly as long as source.notes
{ |source, cross|
	var	s1, s2, newSeg, splice, spl2size, spl2, dur1, dur2, i, notetemp;
	s1 = source.notes;
	s2 = cross.notes;
	splice = ((s1.size-2).rand + 1).max(1);
	spl2size = rrand((s2.size * 0.2).round.asInteger, (s2.size * 0.7).round.asInteger);
	spl2 = (s2.size - spl2size).rand.max(0);
	dur2 = s2[spl2 .. spl2 + spl2size].collect(_.dur).sum;

		// calc coordinates in s1 matching that dur
	dur1 = 0;
	i = splice;
	{ (i < s1.size) and: { dur1 < dur2 } }.while({
		dur1 = dur1 + s1[i].dur;
		i = i + 1;
	});
		// if durs are equal, no problem
		// if dur1 is shorter, remove notes from spl2 until dur1 is longer or only one note is left
	(dur1 < dur2).if({
		{ spl2size > 0 and: { dur2 > dur1 } }.while({
			dur2 = dur2 - s2[spl2 + spl2size].dur;
			spl2size = spl2size - 1;
		});
	});

		// if dur1 is longer, adjust length of last splice note
	(dur1 != dur2).if({
		s2 = s2.copy.put(spl2 + spl2size, (notetemp = s2[spl2 + spl2size].copy).dur_(notetemp.dur + dur1 - dur2));
	});

	newSeg = s1.copyRange(0, splice-1);
	newSeg = newSeg ++ s2.copyRange(spl2, spl2 + spl2size);
	(i < s1.size).if({
		newSeg = newSeg ++ s1.copyRange(i, s1.size - 1);
	}, { newSeg });
} => Func(\fixedSplice);

// a few simple ones
{ |source| var size = source.notes.size, i, notes;
	notes = source.notes.copy.swap(i = size.rand, (i + (size-1).rand).wrap(0, size-1));
} => Func(\noteSwap);

// delete 1 note
{ |source| var i = (source.notes.size - 1).rand, out;
	(source.notes.size > 4).if({
		(out = source.notes.copy).put(i, out[i].copy.dur_(out[i].dur + out[i+1].dur));
		out.removeAt(i+1);
		out
	}, { nil })
} => Func(\dropNote);

// pick a note, split it in 2 (choose a note randomly in range of source)
{ |source| var i = source.notes.size.rand, out, dur;
	out = source.notes.copy;
	dur = out[i].dur;
	out[i] = out[i].copy.dur_(rrand(0.25, dur).round(0.25));
	(out[i].dur != dur).if({
		out = out.insert(i+1, SequenceNote(rrand(source.loNote, source.hiNote).round, dur = dur - out[i].dur, dur + rrand(-0.1, 0.1), out[i].args));
		out
	}, { nil });	// nil == no adaptation
} => Func(\splitNote);

// variation: choose note just outside range
{ |source| var i = source.notes.size.rand, out, dur;
	out = source.notes.copy;
	dur = out[i].dur;
	out[i] = out[i].copy.dur_(rrand(0.25, dur).round(0.25));
	(out[i].dur != dur).if({
		out = out.insert(i+1, SequenceNote(rrand(source.loNote - 3, source.hiNote + 3).round, dur = dur - out[i].dur, dur + rrand(-0.1, 0.1), out[i].args));
		out
	}, { nil });	// nil == no adaptation
} => Func(\splitNote2);


// chordal adaptation -- currently only fitToBassAndTop and support funcs

AbstractChuckArray.defaultSubType = \chordFit;

// couple of very simple cases
// return notes, no modification
#{ |source| source[\notes] } => Func(\asis);

// return notes transposed to topnote
#{ |source, inEvent|
	var	sourceTop, inTop, mode, topEvent, top, topMode, root, notes;
	#mode, topEvent, top, topMode, root, notes =
		Func(\getValuesFromEvent).doAction(source, inEvent);
	inEvent[\top].notNil.if({
		sourceTop = source[\notes].maxItem;
		source[\notes] + (top.asFloat - sourceTop.asFloat)
	}, {
		source[\notes]	// no topnote given, return input notes
	});
} => Func(\fitToTop);

{ |source, inEvent|
		// see Func(\getValuesFromEvent) - I don't need all the values so I take only this one
	var	root = (Library.[inEvent[\bassID] ? \currentBassNote].value(inEvent) ? 0).asFloat,
		notes = source[\notes],
		lowNote = notes.minItem.asFloat;
	notes + (root - lowNote);
} => Func(\fitToBass);

	// caller should supply the correct mode, for speed
#{ |noteArray, top, root, mode, fitFactors|
	var	degreesPerOctave, lowNote, fitIndex;
	mode = mode.asMode;
	degreesPerOctave = mode.scale.size;
	lowNote = noteArray.minItem.asFloat;
	noteArray.collect({ |n, i|
			// this should handle all floats
			// if not, the note will be sorely punished (-20)
			// multiply by a scaling factor to reduce the influence of higher notes
		fitIndex = ((n = n.asFloat) - root).round(0.1) % degreesPerOctave;
			// needed b/c you may get fitIndex === -0.0
		(fitIndex == 0).if({ fitFactors[0] }, {
			(fitFactors[fitIndex] ? -20)
		}) * ((1 - ((n-lowNote) / 14)).clip(0, 1));
	}).mean;	// use mean so that chords with lots of notes won't overwhelm thinner chords
} => Func(\chPitchFit).subType_(\chFitAnalysis);

// measures whether a set of notes belong to the mode into which they were mapped
// chromatic notes are punished
#{ |noteArray|
	noteArray.collect({ |n, i|
		((n = n.asFloat) == n.asInteger).if({ 1 }, { -1 });
	}).mean;	// use mean so that chords with lots of notes won't overwhelm thinner chords
} => Func(\chModeFit).subType_(\chFitAnalysis);

#{ |notes, top, mode|
	var degreesPerOctave, numBelowTop, numAboveTop, transposeBelow;
	mode = mode.asMode;
	degreesPerOctave = mode.scale.size;
	numAboveTop = numBelowTop = 0;
	notes.do({ |n|
		(n > top).if({ numAboveTop = numAboveTop + 1 });
		(n < top).if({ numBelowTop = numBelowTop + 1 });
	});
		// if more notes are above than below top, notes below top need to be dropped 1 oct.
	transposeBelow = (numAboveTop > numBelowTop).if(degreesPerOctave, 0);
	notes.collect({ |n|
		(n > top).if({
			n - (degreesPerOctave *   // notes in an octave *
				(((n - top) / degreesPerOctave).roundUp))  // octaves to transpose
		}, {
			(n < top).if({ n - transposeBelow }, { n });
		});
	});
} => Func(\fixNotesAboveTop).subType_(\chFitAnalysis);

// convert a note from mode1 to mode2
#{ |note, mode1, mode2|
	(note.unmapMode(mode1) - mode1.asMode.tuning).mapMode(mode2)
} => Func(\convertMode).subType_(\chFitAnalysis);

#{ |note, mode1, mode2|
	(Func(\convertMode).doAction(note, mode1, mode2) /*+ 0.1.rand2*/).round
} => Func(\convertNearestInMode).subType_(\chFitAnalysis);

// returns an array with important values for chord fitting
#{ |source, inEvent|
	var	mode, topEvent, top, topMode, root, notes, chordMode;

	mode = (inEvent[\mode] ? \default)/*.asMode*/;

		// mode might be a mode pool (array of Mode IDs) -- if so, revert to the mode stored in the chord
	(mode.asMode.value.size > 0).if({
		mode = source[\mode];
	});

		// midi input will be in the event - use it if present
	notes = inEvent[\chNotes] ? source[\notes];
	(topEvent = inEvent[\top]).notNil.if({
		top = topEvent[\freq] ?? { topEvent[\note].asFloat };
		topMode = topEvent[\mode]/*.asMode*/;
	}, {
		top = notes.asFloat.maxItem;
		topMode = mode;
	});
	(mode != topMode).if({
		top = Func(inEvent[\convertTopFunc] ? \convertMode).doAction(top, topMode, mode, topEvent);
	});

		// .value(inEvent) means that the Library item can generate a new root when called
	root = (Library.at(inEvent[\bassID] ? \currentBassNote).value(inEvent) ? 0).asFloat;

	[mode, topEvent, top, topMode, root, notes]	// return val
} => Func(\getValuesFromEvent).subType_(\chFitAnalysis);

		// should return notes array
		// "match by notes" strategy: calculate all possible transpositions of this chord
		// containing the top note; measure the fitness of each, and choose one of the
		// best fitting chords
#{ |source, inEvent, fitFactors|
	var	transposeStats,  // [[xpose1, fitness1], [xpose2, fitness2]...]
		xposeBy, mode, topEvent, top, topMode, root, notes;

	#mode, topEvent, top, topMode, root, notes =
		Func(\getValuesFromEvent).doAction(source, inEvent);

		// transpose and evaluate for each note
	transposeStats = notes.collect({ |n|
		[xposeBy = (top - n).asFloat,
		 Func(\chPitchFit).doAction(notes + xposeBy, top, root, mode, fitFactors)];
	}).sort({ |a, b| a[1] > b[1] });  // sort fitnesses descending
		// transpose the whole chord
	Func(\fixNotesAboveTop).doAction(notes + transposeStats[0][0], top, mode);
} => Func(\chordFitNotes);

// interval strategy -- produce final chord forms by traversing the intervals in a tree structure
#{ |source, inEvent, fitFactors|
	var resultList = Func(\collectChordsByInt).doAction(source, inEvent, fitFactors),
		fitness,		// [[0, fitness0], [1, fitness1]]
		mode, topEvent, top, topMode, root, notes;

	#mode, topEvent, top, topMode, root, notes =
		Func(\getValuesFromEvent).doAction(source, inEvent);

	fitness = resultList.collect({ |ch, i|
		[i, Func(\chPitchFit).doAction(ch, top, root, mode, fitFactors)]
	}).sort({ |a, b| a[1] > b[1] });

		// output -- fitness[0] is best fit, second [0] gets index into resultList
		// dur and length should be replaced by microrhythm
	resultList[fitness[0][0]].collect({ |freq| SequenceNote(freq, 1, 1) })
} => Func(\chordFitInt);

// more code, but faster...
#{ |source, inEvent, fitFactors|
	var	parms = Func(\getValuesFromEvent).doAction(source, inEvent),
		sortedNotes = parms[5].copy.sort({ |a, b| a > b }),	// descending order by pitch
		intervals = Array.new(sortedNotes.size-1),
		result,
		current = parms[2];

	sortedNotes.asFloat.doAdjacentPairs({ |a, b| intervals.add((b - a)) });
	intervals = intervals.scramble;

	result = Array(sortedNotes.size).add(SequenceNote(current, 1, 1));
	intervals.do({ |int|
		current = current + int;  // intervals are negative so this is actually descending
		result.add(SequenceNote(current, 1, 1));
	});
	result
} => Func(\chordRandInt);

{ |source, inEvent, fitFactors|
	var	resultList,	// chords resulting from tree traversal
		notes, sortedNotes, intervals,
		mode, topEvent, top, topMode, root;

	#mode, topEvent, top, topMode, root, notes =
		Func(\getValuesFromEvent).doAction(source, inEvent);

		// define traverse function
	resultList = List.new;
	sortedNotes = notes.copy.sort({ |a, b| a > b });	// descending order by pitch
	intervals = Array.new(sortedNotes.size-1);
		// .asFloat is needed to be sure removeDups in traverse func works
	sortedNotes.asFloat.doAdjacentPairs({ |a, b| intervals.add((b - a)) });
	Func(\traverseIntervalTree).doAction([top], intervals, resultList);
	resultList
} => Func(\collectChordsByInt);

{ |current, intervals, resultList|
	var	tempIntervals;
	(intervals.size > 0).if({
			// removeDups because it isn't necessary to process the same interval
			// multiple times in the same recursion level
		intervals.removeDups.do({ |interval|
			(tempIntervals = intervals.copy).remove(interval);
			Func(\traverseIntervalTree).doAction(current.copy.add(current.last + interval),
				tempIntervals, resultList);
		});
	}, {
		resultList.add(current)
	});
} => Func(\traverseIntervalTree);



// convenience stuff

// making a chord process is too hard, requiring 7 or more chucks
// this sets the basic parameters in one go, puts in a BP, and returns the BP
#{ |newBPname, childName, parentName, chordMIDIBuf, topMelodyMIDIBuf, macrorhythm, microrhythmSelector, arpegPatSelector, adaptKeysForTopMelody, mode, parms|
	var	new;
	BP.exists(newBPname).not.if({
		new = PR(parentName).chuck(PR(childName).chuck(BP(newBPname), nil, parms), nil, parms);
			// short form for ch.notNil.if({ ... })
		mode.asMode !? { mode.asMode => new };
		chordMIDIBuf !? { chordMIDIBuf =>.ch new };
		topMelodyMIDIBuf !? { topMelodyMIDIBuf =>.mel new };
		macrorhythm !? { macrorhythm =>.macro new };
		microrhythmSelector !? { microrhythmSelector =>.micro new };
		arpegPatSelector !? { arpegPatSelector =>.arpeg new };
		adaptKeysForTopMelody !? { adaptKeysForTopMelody =>.adapt new };
		new
	}, {
		("BP(" ++ newBPname.asCompileString ++ ") already exists. Using existing BP.").warn;
	});
	BP(newBPname)
} => Func(\makeCh).subType_(\factory);

// same as makeCh, but frees the BP first
#{ |newBPname, childName, parentName, chordMIDIBuf, topMelodyMIDIBuf, macrorhythm, microrhythmSelector, arpegPatSelector, adaptKeysForTopMelody, mode, parms|
	var	new;
	BP.exists(newBPname).if({ BP(newBPname).free });
	Func(\makeCh).doAction(newBPname, childName, parentName, chordMIDIBuf, topMelodyMIDIBuf, macrorhythm, microrhythmSelector, arpegPatSelector, adaptKeysForTopMelody, mode, parms);
} => Func(\newCh).subType_(\factory);


// for melodic processes
// intra-phrase segmenter
// notes, currentEnvironment is a bit odd, but necessary for error protection
#{ |notes, nextNote, intervals, minSegSize, parms|
		var	durs;
		var	lastTrueIndex, largeInterval;

		minSegSize = minSegSize ? 4;
		durs = notes.collect(_.dur);

			// split phrase into segments
		lastTrueIndex = -1;		// to ensure that true isn't returned too often
								// has to be -1 because separate splits AFTER it sees true
		largeInterval = { |i|		// determine if this is a large interval
			(i < intervals.size).if({
				(intervals[i].abs > ~avgInterval) or: { durs[i] > ~avgDelta }
			}, {
				true		// maybe this should be false?
			});
		};

		notes = notes.separate({ |note1, note2, i|
				// do not start a new segment if less than minsegsize
			(i - lastTrueIndex < minSegSize).if({ false }, {
					// start a new seg if this is a big interval and next is not
					// otherwise, keep rollin'
				(largeInterval.value(i) and: largeInterval.value(i+1).not).if({
					lastTrueIndex = i;
					true
				}, { false });
			});
		});
} => Func(\defaultMelSegmenter).subType_(\melPartition);

// phrase splitter
{ |notes|
	var subsegs, phraseScores, avgScore, splitIndices;
			// partition the melody into phrases
			// metric is (delta - avgDelta) * (rest / delta)
		phraseScores = notes.collect({ |n|
			(n.dur < ~avgDelta or: { n.length > n.dur }).if({ -inf },
				{ (n.dur - ~avgDelta) * ((n.dur - n.length) / n.dur) });
		});
		avgScore = phraseScores.reject(_ < 0).mean;
		splitIndices = Array.new(notes.size).add(0);
		phraseScores.do({ |score, i|
			(score > avgScore).if({ splitIndices.add(i) });
		});
		splitIndices.add(notes.size-1);
			// do you want to join with the preceding or following phrase? dunno, assuming earlier
		splitIndices.doAdjacentPairs({ |a, b, i|
			(b-a < 3).if({ phraseScores[a] = -inf });
		});
		notes = notes.separate({ |a, b, i| phraseScores[i] > avgScore });
} => Func(\defaultMelSplit).subType_(\melPartition);

// split notes into equal-length phrases
// corner case not covered: if last phrase is too short, what do we do?
// or, if last note exceeds bar length, there will be no compensation
{ |notes, parms|
	var	elapsed = 0, result = List.with(List.new),
		barlength = parms.atBackup(\barLength, currentEnvironment) ? 4.0,
		overflow;
	notes.do({ |note, i|
		result.last.add(note);
		elapsed = elapsed + note.dur;
		(elapsed >= barlength and: { i < (notes.size - 1) }).if({
			result.add(List.new);
			elapsed = elapsed - barlength;
		});
	});
	result.doAdjacentPairs({ |a, b|
		((overflow = a.collect(_.dur).sum - barlength) > 0).if({
			a[a.size-1] = a.last.copy.dur_(a.last.dur - overflow);
				// empty array is how to indicate a rest to a voicer
				// will update later for symbols e.g. \rest
			b.insert(0, SequenceNote(\rest, overflow, 0.1, 0.5));
		});
	});
		// more efficient to call asArray first on the outer list, saves an object
	result.asArray.collect(_.asArray);
} => Func(\barMelSplit).subType_(\melPartition);

{ |notes| [notes] } => Func(\noSplit).subType_(\melPartition);

// user-defined phrase splits
// give the size of each clump of notes
{ |notes, parms|
	var	splitsizes = parms.atBackup(\phrSplits, currentEnvironment);
	(splitsizes.size > 0).if({
		notes.clumps(splitsizes)
	});	// ok to return nil on failure; Func will substitute the original note array
} => Func(\userSplit).subType_(\melPartition);


// helper functions to use drum sequencers with a coordinator
// that produces keys to choose generators

AbstractChuckArray.defaultSubType = \drumgenhelpers;

{	var	result, shortIndex;
	~usedKeys.do({ |key|
		(key != \amps).if({
			result = Array.fill(~amps.size, -1);
			shortIndex = 0;
			~amps.do({ |amp, longIndex|
				(amp > 0).if({
					result[longIndex] = key.envirGet.wrapAt(shortIndex);
					shortIndex = shortIndex + 1;
				});
			});
			key.envirPut(result);
		});
	});
} => Func(\expandKeys);

{	var	nonrests = ~amps.collectIndices(_ > 0);
	~usedKeys.do({ |key|
		(key != \amps).if({
			key.envirPut(key.envirGet.asArray.wrapAt(nonrests));
			(key.envirGet.size == 0).if({
				key.envirPut(#[0]);
			});
		});
	});
} => Func(\shrinkKeys);

{ |i, override|
	var	temp;
	~usedKeys.do({ |key|
		(temp = (override ?? { (key ++ "genStream").asSymbol.envirGet }).value(i)).notNil.if({
			key.envirGet[i] = temp;
		});
	});
} => Func(\insertIntoKeys);

{	var	basekey;
	~usedKeys.do({ |key|
		(basekey = (key ++ "base").asSymbol).envirGet.notNil.if({
			key.envirPut(basekey.envirGet.copy)
		}, {
			~base.notNil.if({
				key.envirPut(~base.copy)
			}, {
				key.envirPut(Array.fill(~ampsSize.value, 0))
			});
		});
	});
} => Func(\initKeys);

{ ~amps.collectIndicesOfItem(0) } => Func(\getRests);

{  ~genBase.value; } => Func(\drumBasicPre);

{
	var	action = ~driverEvent.tryPerform(\at, ~collIndex), func;
	~genBase.value;
	Func.exists(action).if({
		Func(action).doAction;
	});
	~driverEvent = nil;
} => Func(\drumRespDriver);



// for a rhythmic-adaptation prototype
// this func assumes your process is modeled after flutefx and has the required variables

{ |source, cross|
	var	new, adaptKey;
	(adaptKey = ~adaptStream.next(source)).notNil.if({
		(new = Func(adaptKey).doAction(source, cross)).notNil.if({
			~rhythms.add(new);
			(~rhythms.size > (3 * ~originalRhythms)).if({
				~rhythms.removeAt(((~rhythms.size * 0.25).asInteger.rand
					+ ~originalRhythms).clip(~originalRhythms, ~rhythms.size-1));
			});
		});
	});
	new
} => Func(\adaptRhythmArray);

{ |source, cross|
	var	new, spliceIndex, spliceStart, spliceEnd, temp;
	source = source;
	cross = cross;
	spliceIndex = source.size.rand;
	spliceStart = cross.size.rand;
	spliceEnd = (spliceStart + (spliceStart * 0.3).asInteger.rand2).clip(0, cross.size-1);
	(spliceIndex > 0).if({
		new = source[0..spliceIndex-1];
	});
	new = new ++ cross[spliceStart .. spliceEnd] ++ source[spliceIndex..];
	new
} => Func(\spliceRhythmArray);

{ |source|
	var	new, spliceStart, spliceEnd, temp;
	(source.size > 4).if({
		source = source;
		spliceStart = source.size.rand;
		temp = source.size.rand;
		spliceEnd = max(spliceStart, temp);
		spliceStart = min(spliceStart, temp);
		(spliceStart > 0).if({
			new = source[0..spliceStart-1];
		});
		(spliceEnd < (source.size - 1)).if({
			new = new ++ source[spliceEnd+1..];
		});
		new
	});
} => Func(\deleteRhythmArray);


AbstractChuckArray.defaultSubType = saveSubType;
