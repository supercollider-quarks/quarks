
// framework for adaptive processes

var saveSubType = AbstractChuckArray.defaultSubType;
AbstractChuckArray.defaultSubType = \melodyStorage;

// first, handler for a single segment -- this will be analyzed modally
Proto({
		// canPauseAfter is deprecated for a more OO solution
	~prepare = #{ |notes, nextNote, storage, parms, rawMIDI = true|
			// notes should be in the format [note, note, note]
		var	mode = parms.atBackup(\mode, storage) ? \default;
		~notes = notes.asArray;
		~nextNote = nextNote;	// final interval should be calculated relative to the next note
		{ 	(rawMIDI ? true).if({ ~mapToMode.value(mode) }, {
				~mode = (mode.size > 0).if({ mode[0] }, { mode });
			});
		}.try({ |error|
			Error("Error translating MIDI to modal. Check to see if default mode is defined.")
				.throw;
		});
		~modeSubstitution = parms.atBackup(\modeSub, storage);
		~analyzeNotes.value(rawMIDI);	// collect stats on chord -- user definable
		currentEnvironment
	};
	
	~mapToMode = #{ |mode|
		var	bestMode, bestFit = -1e10, bestMap, modeFit, notesTemp, modeObj;
		mode = mode ? \default; 	// keep symbol for storage
		modeObj = mode.asMode;
			// is it a single mode, or a mode pool?
		if(modeObj.value.size > 0) {
			modeObj.value.do({ |mode, i|
				notesTemp = ~notes.mapMode(mode);
				((modeFit = Func(\chModeFit).doAction(notesTemp)) > bestFit).if({
					bestMode = mode;
					bestFit = modeFit;
					bestMap = notesTemp;
				});
			});
			~mode = bestMode;
			~notes = bestMap;
			~nextNote = ~nextNote.mapMode(bestMode)
		}{
			~notes = ~notes.mapMode(mode);
			~nextNote = ~nextNote.mapMode(mode);
			~mode = mode;
		};
		currentEnvironment
	};
	
	~mapToMIDI = #{ |mode|
		~notes = ~notes.unmapMode(mode ?? { ~mode ? \default });
		~nextNote = ~nextNote.unmapMode(mode ?? { ~mode ? \default });
		currentEnvironment
	};
	
// subclasses must implement:
//	~analyzeNotes (if needed)
//	~asPattern
}) => PR(\abstractModeSeg);

PR(\abstractModeSeg).v.clone({
	~analyzeNotes = #{ |rawMIDI|
		currentEnvironment
	};

		// rhythm profile must act on grace notes before the notes are embedded in the stream
		// thus, PseqFunc
		
		// change in ~useRh will take effect only at segment boundaries
		
	~asNotePattern = #{ |passInValue|
		var dur, length, rhy, out;
		out = (passInValue[\useRh] and: { (rhy = passInValue[\rhythmStream]).notNil }).if({
			PseqFunc(~notes, 1, func: { |note|
				#dur, length = rhy.next(note);
				note.copy.dur_(dur).length_(length);
			})
		}, { Pseq(~notes, 1) });
	};

		// all modal patterns must return modally-mapped notes
		// conversion to midinotes will be made in the note event
	~asPattern = #{ |passInValue|
		var	pat;
		pat = Pbind([\note, \delta], ~asNotePattern.value(passInValue)
				.collect({ |n| [n, n.dur] }));
		~mode.notNil.if({
			pat.patternpairs = pat.patternpairs ++
				[\mode, ~modeSubstitution.notNil.if({ ~modeSubstitution.value }, { ~mode })];
		});
		pat
	};

		// combine a series of metrics to compare different segments
		// ~qkeys and ~qnormals should be populated in the subclass
		// usage: seg1.qualities.sum.absdif(seg2.qualities.sum) -- lower absdif, more similar
	~qualities = #{
		(~qkeys.collect({ |k, i| k.envirGet * ~qnormals[i] })) ? 0
	};
}) => PR(\melModeSeg);

PR(\melModeSeg).v.clone({
	~analyzeNotes = #{ |rawMIDI|
		var notes, temp, tempdur;	// temp variable for calculations
			// some analysis funcs in here
			// start with pitch material

		temp = ~notes
			.reject({ |item| item.isSymbol or: { item.tryPerform(\freq).isSymbol } })
			.collect(_.asFloat)
			.select({ |item| item.isNumber });

		~intervals = Array.new(temp.size);

		temp.doAdjacentPairs({ |note1, note2, i|
			~intervals.add(note2 - note1)
		});
		(temp.size > 1).if({
			~intervals.add(~nextNote.asFloat - temp.last);
			~angularity = ~intervals.abs.mean / (~intervals.mean.abs.nz(1));
		}, {
			~angularity = 0;	// 1 note, can't be angular
		});

		~hiNote = temp.maxItem;
		~loNote = temp.minItem;
		~range = ~hiNote - ~loNote;
			// 0 = lowest, 1 = highest for center (ratio of median to range)
		~center = (temp.copy.median - temp.minItem) / ~range.nz(1);
		~lastNote = temp.last;
		~firstNote = temp.first;
		~direction = ~lastNote - ~firstNote;
		
			// lots of notes shorter than their deltas will drive this metric down
		~rhythmSustain = ~notes.sum(_.length) / ~notes.sum(_.dur);
// add repetition factor? how to compute quickly?
		
		~metric = ~qualities.value.sum;
		
		currentEnvironment
	};

	~qkeys = #[\angularity, \direction, \range, \center, \hiNote, \loNote];
	~qnormals = #[1, 1, 1, 8, 0.25, 0.25];	// factors used for normalizing analysis values

}, nil, #[\qkeys, \qnormals]) => PR(\melAdaptSeg);


// holds a single melodic segment along with its adaptations
PR(\abstractProcess).v.clone({
	~segProto = \melAdaptSeg;
	~adaptable = true;		// can specify some segments never to adapt
	
	~prepare = #{ |notes, nextNote, storage, parms, rawMIDI = true|
		~segs = List.with(PR(~segProto).v.copy
			.prepare(notes, nextNote, storage, parms, rawMIDI));
		~nextNote = nextNote;
		~mode = parms.atBackup(\mode, storage) ? \default;
		currentEnvironment
	};
	
	~main = #{  ~segs[0] };
	
	~addAdapt = #{ |notes, parms, rawMIDI = false|
			// if there is no mode in parms, use the one given at creation
		parms[\mode].isNil.if({ parms[\mode] = ~mode });
		~segs.add(PR(~segProto).v.copy
			.prepare(notes, ~nextNote, nil, parms, rawMIDI));  // not passing in storage object
		currentEnvironment
	};
	
	~removeAt = #{ |index| ~segs.removeAt(index) 
	};
	
	~asPattern = #{ |rhythmProfile|
		~segs.choose.asPattern(rhythmProfile)
	};

	~asNotePattern = #{ |rhythmProfile|
		~segs.choose.asNotePattern(rhythmProfile)
	};
	
	~clearAdapt = #{
		(~segs.size > 0).if({
			~segs = List.with(~segs[0]);
		});
	};
	
}, nil, #[\segProto]) => PR(\melSegWithAdapt);

PR(\abstractProcess).v.clone({
	~segProto = \melSegWithAdapt;
	~segFunc = \defaultMelSegmenter;
	~minSegSize = 4;
	
	~prepare = #{ |notes, nextNote, rawMIDI = true,
			intervals, avgInterval, avgDelta, storage, parms|
		var subsegs;
		
		~parms = parms;	// save for adding variations

			// these are needed to analyze future input materials
		~avgInterval = avgInterval;
		~avgDelta = avgDelta;

		notes = Func(parms.atBackup(\segFunc, currentEnvironment))
			.doAction(notes, /*currentEnvironment,*/ nextNote, intervals, 
				parms.atBackup(\minSegSize, storage, currentEnvironment), parms);

		subsegs = Array.new(notes.size);
		notes.do({ |seg, i|
			subsegs.add(PR(~segProto).v.copy.prepare(seg,
					// if there's a later segment, give first note of next segment
					// if this is the last, give first for wraparound interval
				(i < (notes.size-1)).if({ notes[i+1][0] }, { notes[0][0] }),
				storage, parms
			));
		});
		~mel = subsegs;
		currentEnvironment
	};
	
	~patternBase = #{ 
		Pseq(~mel, 1)
	};
	
	~asPattern = #{ |passInValue|
		~addAdaptCollect.value(~patternBase.value, passInValue, \asPattern)
	};

	~asNotePattern = #{ |passInValue|
		~addAdaptCollect.value(~patternBase.value, passInValue, \asNotePattern)
	};
	
	~addAdaptCollect = #{ |pattern, passInValue, asPatSelector = \asPattern|
		pattern.collect(e { |seg|
			passInValue[\adaptProb].coin.if({
				~doAdapt.value(seg, passInValue);
				(seg.segs.size > passInValue[\variantThreshold]).if({
					~eugenicize.value(seg, passInValue);
				});
			});
			seg.perform(asPatSelector, passInValue);
		})
	};

		// source and cross are Lists ([0] is original, [1..x] are variants)
	~doAdapt = #{ |source, passInValue|
		var srcSeg, crsSeg, fitness, fitnessmean, qtemp, result, adaptTemp;
			// choose source segment -- if too different from original, don't adapt further
			// source is melSegWithAdapt
		srcSeg = source.segs.choose;
		(~adaptMel.size > 0 and: { source.adaptable ? true }).if({
			crsSeg = ~adaptMel.choose;
			fitness = passInValue[\adaptStream].next(srcSeg);
			Func.exists(fitness).if({
				(result = Func(fitness).doAction(srcSeg, crsSeg, passInValue)).notNil.if({
					source.addAdapt(
						result,
						~parms,	// uses parms from creation time (no subsequent updates)
						false  // rawmidi should now be false
					);
						// it may be a bad adaptation -- kill it preemptively if so
					Func(passInValue[\eugTest])
						.doAction(source.segs[0], source.segs.last, passInValue).notNil.if({
							source.removeAt(source.segs.size-1)
					});
				});
			}, {
				fitness.notNil.if({
					format("Func(%) does not exist. No adaptation.",
						fitness.asCompileString).warn;
				});
			});
		});
	};
	
	~tryEugenicize = #{ |variantThreshold|
		var	seg;
		(seg = ~mel.select({ |seg| seg.mel.size > variantThreshold }).choose)
			!? { ~eugenicize.value(seg); };
	};
	
		// choose worst fit (or oldest) out of source and kill it
		// ok to have this in phrase b/c it doesn't involve adapt material
	~eugenicize = #{ |source, passInValue|
		var	fits, orig;

		orig = source.segs[0];
		fits = Array.newClear(source.segs.size-1);
		for(1, source.segs.size-1, { |i|
			fits.put(i-1, [Func(passInValue[\eugTest])
				.doAction(orig, source.segs[i], passInValue), i]);
		});
		fits = fits.reject({ |fit| fit[0].isNil });	// nil values are OK by test func
			// if any segment is left, kill the worst of them
		(fits.size > 0).if({
				// sort descending order of fit
			fits = fits.sort({ |a, b| a[0] > b[0] });
			source.removeAt(fits[0][1])
		}, {		// otherwise use age to determine which to drop
				// nature of list is that earliest items should be older
				// keep the original [0] and drop the oldest
			source.removeAt(1)
		});
	};
	
		// returns flat array of original segments -- used for adaptation
	~asSegArray = #{  ~mel.collect({ |seg| seg.segs[0] }) };
	
	~clearAdapt = #{
		~mel.do(_.clearAdapt);
		~adaptMel = nil;
	};

}, nil, #[\segProto]) => PR(\melAdaptPhrase);

// container to hold a number of melodic segments
// this maintains segments, adaptation, and holds current playback state (which seg)
// pattern to retrieve segments will be user-definable
// no need to hold index anymore -- if you want to change melodies,
// save this object and pop in a new \melodyStorage object

Proto({
	~segProto = \melAdaptPhrase;
	~midiParse = true;
	~splitFunc = \noSplit;
	~rhythmQuant = 0.25;	// default, quantize to 16th
	~adaptProb = 1; //0.4;
	~variantThreshold = 4;	// when more than 4 variants, drop some; see ~eugenicize
	~useRh = false;
	
		// these are keys that will be chucked into BP but need to be passed into mel
		// range is used for eug testing: NumericRange(lo, hi)
	~keysFromParent = #[\mode, \rhythmStream, \adaptStream, \adaptProb,
		\variantThreshold, \repeats, \preSplitFunc, \useRh, \adTest, \eugTest, \splitFunc,
		\resetSeg, \resetPhr, \range];
	
	~repeats = inf;
	~adaptPattern = \absSplice;		// this may be a pattern

	~preSplitFunc = #{ |notes| notes };

	~clearParms = { ~parms = nil; };
	
	~prepareSequence = #{ |key, buf, storage, parms|
			// maintain an aggregate of passed-in parameters
		~parms = (~parms ?? { () }).putAll(parms);
		key.envirPut((key ++ "PrepareSequence").asSymbol.envirGet
			.value(buf, storage, ~parms));
		currentEnvironment
	};

		// generate metrics for adaptation segments
	~melPrepareSequence = #{ |buf, storage, parms|
		var	notes, temp, intervals;
		notes = buf.notes;
			// calc this for splitting the phrases into subsegments
		intervals = Array.new(notes.size - 1);
		notes.doAdjacentPairs({ |note1, note2|
			intervals = intervals.add(note2 - note1);
		});

		~avgInterval = intervals.abs.mean;
		~avgDelta = buf.durs.mean;	// also need note deltas

			// function composition: maybe you want to do mode or some other conversion here
		temp = ~preSplitFunc.value(buf.notes);

		notes = Func(parms.atBackup(\splitFunc, storage, currentEnvironment).debug("splitfunc"))
			.doAction(temp, /*currentEnvironment,*/ parms);

			// must be array now because eventually, this will be a collection of phrases
		(temp == notes).if({ notes = [notes] });

			// return value
		notes.collect({ |phrNotes, i|
			PR(~segProto).v.copy.prepare(phrNotes, 
				(i == (notes.size-1)).if({ notes[0][0] }, { notes[i+1][0] }), 
				true, intervals, ~avgInterval, ~avgDelta, storage, parms)
		});
	};

	~adaptPrepareSequence = #{ |buf, storage, parms|
		var temp;
			// adaptation should just be a set of segments, so change split type
		parms[\splitFunc].isNil.if({
			parms = parms.copy.put(\splitFunc, \noSplit)
		});
		~melPrepareSequence.value(buf, storage, parms)
			.collect({ |phr, i|
				phr.mel.collect({ |seg, i| seg.main })
			}).flat;
	};
	
	~clearAdapt = #{
		~adapt = nil;
		~mel.do(_.clearAdapt);
	};

		// pattern maker needs to be modularized so you can override
	~makePhrPattern = #{ |passInValue|
		passInValue = passInValue ?? { ~passInValue };
		~phrPattern = Pseq(~mel, passInValue[\repeats] ? inf)
	};
	
		// run each melody segment successively
	~asSegPattern = #{ |asPatSelector, passInValue|
		passInValue = passInValue ?? { ~passInValue };
		~makePhrPattern.value(passInValue).collect(e { |phr|
			phr.notNil.if({
						// populate adaptation values in phrase, to simplify API
				phr.putAll((adaptMel: ~adapt));
			}, {
				nil
			});
		});
	};
	
	~asSegStream = #{ |asPatSelector, passInValue|
		passInValue = passInValue ?? { ~passInValue };
		(~segStream.isNil or: { passInValue[\resetPhr] ? true }).if({
			~segStream = CleanupStream(~asSegPattern.value(asPatSelector, passInValue)
				.asStream, e {
					currentEnvironment.put(\segStream, nil);  // allow reset next time
				});
		});
		~segStream
	};

}, nil, #[\segProto]) => PR(\melodyStorage);

// subclass that plays phrases in random order
PR(\melodyStorage).v.clone({
	~makePhrPattern = #{ |passInValue|
		~phrPattern = Pxrand(~mel, passInValue[\repeats] ? ~repeats ? inf)
//.collect({ |val| "\nmelstore-phrase pattern".postln; val.postcs })
	};
}) => PR(\melRandStorage);

AbstractChuckArray.defaultSubType = saveSubType;
