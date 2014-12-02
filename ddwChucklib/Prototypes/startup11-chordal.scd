
var saveSubType = AbstractChuckArray.defaultSubType;
AbstractChuckArray.defaultSubType = \chordStorage;

// chord processes

// chord segment
// no modal stuff, just raw midi notes
PR(\abstractProcess).v.clone({
	~prepare = #{ |notes, modeOverride|
			// notes should be in the format [note, note, note]
			// but they might come in as [SeqChordNote(..., [....]), ...]
		~modeOverride = modeOverride;
		~notes = notes.asArray.collect({ |n| n.asNoteArray }).flat;
		currentEnvironment
	};
	
		// subclasses will use keys in inEvent to adapt to topnote, etc.
	~asPattern = #{ |inEvent|
		ArpegPat(inEvent[\arpegType]).asPattern(~notes, inEvent)
	};
}) => PR(\basicChordSeg);

// modal chord segment that adapts itcurrentEnvironment to bass and topnote

// fitnesses of chord notes, assuming root is 0
// you can set this in a chordsegment instance to override.
// To change the default, put something else in the library.

Library.put(\fitFactors, Dictionary[
	0 -> 10,
	1.0 -> 6,  // 2nd
	2.0 -> 10, // 3rd -- note, privileging triadic notes
	3.0 -> 4,
	4.0 -> 10,
	5.0 -> -10,	// 6th degree makes chord root perception ambiguous; thus, it's punished
	6.0 -> 3,
	0.5 -> -5,	// non-diatonic notes
	1.5 -> -5,
	2.5 -> -5,
	3.5 -> 3,		// #4 or b5 OK
	4.5 -> -1,	// not preferred, but not completely evil either
	5.5 -> 0,		// b7 neutral
	6.5 -> 0,		// #7 neutral
]);

PR(\abstractModeSeg).v.clone({
		// by default, use the mode of the calling event
		// set to true to force the notes to play back using the mode analyzed here
	~useOwnMode = false;
	~prepare = #{ |notes, mode, rawMIDI = true, parms|
			// notes should be in the format [note, note, note]
		~rawNotes = ~notes = notes.asArray;
		~rawMIDI = rawMIDI ? true;
		~rawMIDI.if({ ~mapToMode.value(mode) });
		~analyzeNotes.value(~rawMIDI);	// collect stats on chord -- user definable
		~useOwnMode = parms.atBackup(\useOwnMode, currentEnvironment);
		currentEnvironment
	};
	
	~mapToMode = #{ |mode|
		var	bestMode, bestFit = -1e10, bestMap, modeFit, notesTemp, modeObj;
			// is it a single mode, or a mode pool?
		mode = mode ? \default;
		modeObj = mode.asMode.value;
		if(modeObj.size > 0) {
			modeObj.do({ |mode, i|
				notesTemp = ~rawNotes.mapMode(mode);
				((modeFit = Func(\chModeFit).doAction(notesTemp)) > bestFit).if({
					bestMode = mode;
					bestFit = modeFit;
					bestMap = notesTemp;
				});
			});
			~mode = bestMode;
			~notes = bestMap;
		}{
			~notes = ~notes.mapMode(mode);
			~mode = mode;
		};
		currentEnvironment
	};
	
	~mapToMIDI = #{ |mode|
		~notes = ~notes.unmapMode(mode);
		currentEnvironment
	};
	
	~analyzeNotes = #{ 
		~hiNote = ~notes.maxItem;
		~loNote = ~notes.minItem;
		currentEnvironment
	};
	
	~modeForEvent = { |ev|
		~modeOverride ?? { ~useOwnMode.if({ ~mode }, { ev[\mode] }) };
	};
	
	~modeOverride_ = { |mode|
		(mode !== ~modeOverride).if({
			~modeOverride = mode;
			~mapToMode.(mode ? ~mode);
		});
	};

	~getFit = #{ ~fitFactors ? Library.at(\fitFactors); };

	~asPattern = #{ |inEvent, fitFunc|
		var result = Func(fitFunc).doAction(currentEnvironment, inEvent,
			inEvent[\fitnessOverride] ?? { ~getFit.value });
		result.notNil.if({
			~lastFitNotes = result;
			ArpegPat(inEvent[\arpegType]).asPattern(result, inEvent)
		}, {
			Error("Fatal error arpeggiating chord: Func(%) returned nil."
				.format(fitFunc.asCompileString)).throw
		});
	};
}) => PR(\chordModeSeg);

// for more flexible analysis: user provides a "modepool" (array of ModalSpecs)
// each individual chord tries each option and chooses the one that fits best
// allows sensible chromatic inflections of minor modes, for instance

// deprecated -- chordModeSeg handles this on its own
// stub retained for backward compatibility

PR(\chordModeSeg).v.clone(nil) => PR(\chordModePoolSeg);

// first, basic arpeggiator (child process)

AbstractChuckArray.defaultSubType = \chordChildProc;

PR(\abstractProcess).v.clone({
		// give keys for prototypes
	~chordProto = \chordModeSeg;
	~mode = \default;
	
	~event = (eventKey: \voicerNote);
	
	~preparePlay = #{ 
		~chordStream = ~chordStream ?? { ~makeChordPattern.value.asStream };
		currentEnvironment
	};
	
	~reset = #{ 
		~chordStream = nil;
		~preparePlay.value;	// should return currentEnvironment
	};

	~getFit = #{ ~fitFactors ? Library.at(\fitFactors); };

	~makeChordPattern = #{ Pseq(~chords, inf) };
	~makeChordPattern_ = { |func|
		~makeChordPattern = func;
		~reset.value;
		currentEnvironment
	};

		// you can change fitToBassAndTop behavior by changing this symbol
		// funcs are defined in Func()
		// this may also be a function, which is passed the chord Proto
		// ~fitFunc = { |chordProto| ... return fitFunc symbol ... };
	~fitFunc = \asis;

// what if this is in response to a bass note (no inEvent)? address in subclass
// update method should modify the value of ~noteStream without updating anything else
	~nextNotePattern = #{ |inEvent|
			// if there is an event, we need to move to the next chord
		(inEvent.size > 0).if({
			~currentChord = ~chordStream.next(inEvent);
		});
	};
	
	~updateNoteStream = #{ |inEvent, bassID|
		bassID !? { inEvent.put(\bassID, bassID) };
		~noteStream = (~notePattern = ~currentChord.asPattern(inEvent, ~fitFunc.value(~currentChord)))
			.asStream;
	};
	
	~updateBass = #{ |lastEvent, bassID|
			// maybe you don't want the process to update every bass note
		(lastEvent.tryPerform(\at, \updateOnBass) ? true).if({
			~updateNoteStream.value(lastEvent, bassID)
		});
	};
	
	~nextNoteStream = #{ |inEvent|
		~nextNotePattern.value(inEvent);
		~updateNoteStream.value(inEvent);
		Prout({ |event|
			{ event = ~noteStream.next(event).yield }.loop
		});
	};

	~asPattern = #{ |inEvent|
		var	out, argPairs;
		if(~fitFactors.notNil) {
			inEvent = inEvent.copy.put(\fitnessOverride, ~fitFactors);
		};
		out = Pbind(
			\note, inEvent[\chNotes].isNil.if({
				(~chords.size == 0).if({
					Error("Cannot play -- no chords have been provided to arpeg process").throw;
				}, {
					~nextNoteStream.value(inEvent)
				});
			}, {
					// fall back for midi input
				out = (notes:~notes);
				ArpegPat(inEvent[\arpegType]).asPattern(
					Func(~fitFunc.value(out)).doAction(out, inEvent, ~getFit.value),
					inEvent)
			}),
			#[\dur, \length, \gate], MicRh(inEvent[\microRhythm])
				.asPattern(~notePattern, inEvent),
			\mode, (Pfunc({
				~currentChord.modeForEvent(inEvent)
			}))
		);
			// note, argpairs will reset every chord
			// if you want a stream not to reset, use BPStream in the argPairs array
		argPairs = ~argPairs.(inEvent);		// if simple array, this will have no effect
		// argPairs.debug("argPairs");
		case
		{ argPairs.isPattern } {
			out = argPairs <> out
		}
		{ argPairs.size > 0 } {
// 			out = Pbindf(out, \argKeys, argPairs[0, 2..], *argPairs);
			out = Pbind(\argKeys, argPairs[0, 2..], *argPairs) <> out;
		};
		~asPatternHook.(out) ?? { out };
	};
	
	~acceptMIDIBuf = #{ |buf, adverb, parms|
		~chords = ~prepareSequence.value(buf, parms);
		~chordStream = ~makeChordPattern.value.asStream;   // reset the chord stream
	};
	
	~prepareSequence = #{ |buf, parms|
		var 	notes, avgDur;
			// why I'm using a parms dictionary: grab more parameters without changing method args
		parms.tryPerform(\at, \fitFunc).notNil.if({ ~fitFunc = parms[\fitFunc] });
			// partition on rhythm - in general, notes belonging to the same chord will have
			// a duration less than the mean
		avgDur = buf.durs.mean;
			// if note is a SeqChordNote, it's a chord unto itself
			// otherwise, use average duration to determine when to break
			// should be possible to play slowly arpeggiated chords with some gracenotes
			// and have them preserved
		notes = buf.notes.separate({ |a, b| a.isChord or: { a.dur > avgDur } });
			// now make a chord object for each set of notes - this is function output
		notes.collect({ |ch|
				// true = unmap notes to mode
			PR(~chordProto).v.copy.prepare(ch.asNoteArray,
				parms.atBackup(\mode, buf.properties, currentEnvironment),
				parms.tryPerform(\at, \rawMIDI) ? true, parms)
		})
	};
}, nil, #[\chordProto]) => PR(\arpeg1);

// behaves the same as \arpeg1 but uses the synthNote event instead of voicerNote
PR(\arpeg1).v.clone({
	~event = ~event.copy.put(\eventKey, \synthNote);
}) => PR(\arpegSynth);

// uses modepool
PR(\arpeg1).v.clone({
	~chordProto = \chordModePoolSeg;
}, nil, #[\chordProto]) => PR(\arpeg2);

PR(\arpegSynth).v.clone({
	~chordProto = \chordModePoolSeg;
}, nil, #[\chordProto]) => PR(\arpeg2Synth);

// macrorhythm process - no topnote

AbstractChuckArray.defaultSubType = \chordPlayer;

PR(\abstractProcess).v.clone({
	~canWrap = true;
	~respondsToBass = false;
	~updaters = ();		// see ~update below -- for user-extensible notifications

	~acceptMIDIBuf = #{ |buf, adverb, parms|
		(#[\ch, nil].includes(adverb)).if({
				// true == rawMIDI
			~child.acceptMIDIBuf(buf, adverb, parms);
		}, {
			"This process does not accept a melody sequence.".warn;
		});
		currentEnvironment
	};
	
	~update = #{ |obj, changer ... args|
		if(~updaters.tryPerform(\at, changer).notNil) {
			~updaters[changer].value(obj, changer, *args)
		};
		currentEnvironment
	};

	~updateBassID = { |obj, changer|
		(~isPlaying and: { changer == ~bassID and: { ~lastEvent.notNil } })
		.if({
			~child.updateBass(~lastEvent, changer);
		});
	};
	
	~bassID_ = { |bassID|
		if(~updaters.tryPerform(\at, ~bassID).notNil) {
			~updaters.removeAt(~bassID);
		};
		~bassID = bassID;
		if(bassID.notNil) {
			~updaters[bassID] = ~updateBassID;
		};
		currentEnvironment
	};
	
	~bindSimpleNumber = #{ |num, adverb|
		adverb.envirPut(num);
	};
	
	~makeStreamForKey = #{ |key, streamKey, envir|
		var	stream = key.envirGet;
		envir.notNil.if({
			stream = envir.use({ stream = stream.asStream });
		}, {
			stream = stream.asStream
		});
			// output, and stream gets replaced so that playing stream picks it up:
		(streamKey = streamKey ?? { key ++ "Stream" }).asSymbol.envirPut(stream)
	};

		// to allow streams to be changed behind the scenes
	~makeProut = #{ |key, protoEvent, envir|
		var	streamKey;
			// create stream if it doesn't exist
		~makeStreamForKey.value(key, streamKey = (key ++ "Stream").asSymbol, envir);
			// if a protoevent is supplied, use it in next()
			// that's for topNote (not in this process, but subclasses)
			// otherwise use the event passed at evaluation time
		protoEvent.notNil.if({
			Prout({
				{ streamKey.envirGet.next(protoEvent).yield }.loop
			});
		}, {
			Prout({ |inEvent|
				{ inEvent = streamKey.envirGet.next(inEvent).yield }.loop
			});
		});
	};
	
	~preparePlay = #{ 
		~child.tryPerform(\preparePlay);
		currentEnvironment
	};
	~reset = #{ 
		~child.reset;
		currentEnvironment
	};
	
	~event = (eventKey: \macroRh);

	~requiredKeys = #[\macro, \micro, \arpeg];
	~rewrapKeys = ~requiredKeys;
	
	~asPattern = #{ |inEvent|
		var	out;
			// if length is nil, it picks up delta; sim. for arpegType
		PbindMultiChan(#[\delta, \length], ~makeProut.value(\macro),
			#[\microRhythm, \arpegType], Ptuple([
				~makeProut.value(\micro),
				~makeProut.value(\arpeg)], inf))
		.collect(e { |ev| currentEnvironment.put(\lastEvent, ev); ev });
		(~argPairs.size > 0).if({
			Pbindf(out, \argKeys, ~argPairs[0, 2..], * ~argPairs)
		}, { out });
	};
	
	~freeCleanup = {
		~userfree.();
	};
}, nil, #[\rewrapKeys, \requiredKeys]) => PR(\basicMacroRh);

	// macrorhythm with topnote and bass
PR(\basicMacroRh).v.clone({
	~melodyProto = \aiMel;
	~respondsToBass = true;
	~bassUpdate = true;

	~acceptMIDIBuf = #{ |buf, adverb, parms|
		var	parmsplus;
			// buffer properties are base parms; can override with chuck parms
		buf.properties.respondsTo(\putAll).if({
			parmsplus = buf.properties.copy.tryPerform(\putAll, parms ?? { () });
		}, {
			parmsplus = parms;
		});
		adverb = adverb ?? { parmsplus.tryPerform(\at, \type) ? \ch };
		case { adverb.isNil or: (adverb == \ch) }
				{ ~child.acceptMIDIBuf(buf, adverb, parmsplus) }
			{ #[\mel, \adapt].includes(adverb) }
				{	~topNote.isNil.if({ ~topNote = PR(~melodyProto).v.copy });
					~topNote.mode = parms.atBackup(\mode, currentEnvironment);
					~topNote.acceptMIDIBuf(buf, adverb, parmsplus)
				}
			{ "Invalid adverb.".warn; };
		currentEnvironment
	};

	~bindPattern = #{ |pattern, adverb|
		case { adverb == \adapt } { ~topNote.notNil.if({ ~topNote.bindPattern(pattern, adverb) }) }
			{	adverb.envirPut(pattern.asPattern);
				~makeStreamForKey.value(adverb);
			};
		currentEnvironment		// return
	};
	
	~getMode = PR(\aiMel).v[\getMode];
	~mode_ = #{ |mode|
		mode = mode.tryPerform(\collIndex) ? mode ? \default;  // pass a symbol in as the mode
		~child !? { ~child.mode = mode };
		currentEnvironment.put(\mode, mode);
	};
	
	~rewrapKeys = ~requiredKeys ++ [\topNote];
	
	~clearAdapt = #{ ~topNote.clearAdapt };

		// more modularization
		// sans top is to allow MIDI input when top melody is not already given
	~patternSansTop = #{
		PbindMultiChan(#[\delta, \length], ~makeProut.value(\macro),
			\arpegType, ~makeProut.value(\arpeg),
			\microRhythm,	~makeProut.value(\micro),
			\bassID, Pfunc({ ~bassID ? \currentBassNote }),
			\updateOnBass, Pfunc({ ~bassUpdate }),
			\convertTopFunc, Pfunc({ ~convertTopFunc ? \convertMode }),
			\mode, Pfunc({ ~getMode.value }))
		.collect(e { |ev| currentEnvironment.put(\lastEvent, ev); ev });
	};
	
	~patternAvecTop = #{
		~topNotePattern = ~topNote.asPattern;
		PbindMultiChan(#[\delta, \length], ~makeProut.value(\macro),
			\arpegType, ~makeProut.value(\arpeg),
			\microRhythm,	~makeProut.value(\micro),
			\top, ~makeProut.value(\topNotePattern, ~topNote[\event], ~topNote),
			\bassID, Pfunc({ ~bassID ? \currentBassNote }),
			\updateOnBass, Pfunc({ ~bassUpdate }),
			\convertTopFunc, Pfunc({ ~convertTopFunc ? \convertMode }),
			\mode, Pfunc({ ~getMode.value }))
		.collect(e { |ev|
			~lastEvent = ev;
			ev
		});
	};

	~asPattern = #{
		var	out;
			// topNote may be supplied by MIDI; in that case, don't include in Pbind
		~topNote.isNil.if({
			out = ~patternSansTop.value
		}, {
			out = ~patternAvecTop.value
		});
		(~argPairs.size > 0).if({
				// ~argPairs[0, 2..] gets every even index (e.g. symbolic keys)
			Pbindf(out, \argKeys, ~argPairs[0, 2..], * ~argPairs)
		}, { out })
	};
	
		// normally this process should not create resources that need to be freed
		// (GC takes care of data structures) but just in case...
	~freeCleanup = {
		~topNote.tryPerform(\freeCleanup);
		~userfree.();
	};
}, nil, #[\rewrapKeys, \requiredKeys]) => PR(\macroRh);

PR(\macroRh).v.clone({
	~requiredKeys = ~requiredKeys.copy;
	~requiredKeys.remove(\macro);
	~patternSansTop = #{
		PbindMultiChan(#[\delta, \length], [0.1, inf],	// for midi input, we don't know the length yet
			\arpegType, ~makeProut.value(\arpeg),
			\microRhythm,	~makeProut.value(\micro),
			\bassID, Pfunc({ ~bassID ? \currentBassNote }),
			\updateOnBass, Pfunc({ ~bassUpdate }),
			\convertTopFunc, Pfunc({ ~convertTopFunc ? \convertMode }),
			\mode, Pfunc({ ~getMode.value }))
		.collect(e { |ev| currentEnvironment.put(\lastEvent, ev); ev });
	};
	
	~patternAvecTop = #{
		~topNotePattern = ~topNote.asPattern;
		PbindMultiChan(
			#[\top, \delta, \length], ~makeProut.value(\topNotePattern,
				~topNote[\event], ~topNote)
				.collect({ |ev|	// from topEvent, make an array with delta and length
					[ev, ev[\delta], ev[\length] ?? { ev[\note].length }]
				}),
			\arpegType, ~makeProut.value(\arpeg),
			\microRhythm,	~makeProut.value(\micro),
			\bassID, Pfunc({ ~bassID ? \currentBassNote }),
			\updateOnBass, Pfunc({ ~bassUpdate }),
			\convertTopFunc, Pfunc({ ~convertTopFunc ? \convertMode }),
			\mode, Pfunc({ ~getMode.value }))
		.collect(e { |ev| currentEnvironment.put(\lastEvent, ev); ev });
	};			
}) => PR(\chTop);

AbstractChuckArray.defaultSubType = saveSubType;
