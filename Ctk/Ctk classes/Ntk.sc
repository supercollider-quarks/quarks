// Notation tool kit - abstract wrappers to GUIDO, LilyPond and (eventually) MusicXML
NtkObj {
	classvar rhyToSym, symToRhy, clefs, rhythmToDur, timeToDots, timeToDur;

	*initClass {
		clefs = [\perc, \g, \f, \c];
		rhythmToDur = IdentityDictionary[
			\q -> 0.25,
			\qd -> 0.25,
			\qdd -> 0.25,
			\e -> 0.125,
			\ed -> 0.125,
			\edd -> 0.125,
			\s -> 0.0625,
			\sd -> 0.0625,
			\sdd -> 0.0625,
			\t -> 0.03125,
			\td -> 0.03125,
			\tdd -> 0.03125,
			\x -> 0.015625,
			\xd -> 0.015625,
			\xdd -> 0.015625,
			\o -> 0.0078125,
			\od -> 0.0078125,
			\odd -> 0.0078125,
			\h -> 0.5,
			\hd -> 0.5,
			\hdd -> 0.5,
			\w -> 1.0,
			\wd -> 1.0,
			\wdd -> 1.0,
			\b -> 2.0,
			\bd -> 2.0,
			\bdd -> 2.0,
			\l -> 4.0,
			\ld -> 4.0,
			\ldd -> 4.0
			];
		timeToDots = IdentityDictionary[
			0.25 -> 0,
			0.375 -> 1,
			0.4375 -> 2,
			0.125 -> 0,
			0.1875 -> 1,
			0.21875 -> 2,
			0.0625 -> 0,
			0.09375 -> 1,
			0.109375 -> 2,
			0.03125 -> 0,
			0.046875 -> 1,
			0.0546875 -> 2,
			0.015625 -> 0,
			0.0234375 -> 1,
			0.02734375 -> 2,
			0.0078125 -> 0,
			0.01171875 -> 1,
			0.013671875 -> 2,
			0.00390625 -> 0,
			0.5 -> 0,
			0.75 -> 1,
			0.875 -> 2,
			1.0 -> 0,
			1.5 -> 1,
			1.75 -> 2,
			2.0 -> 0,
			3.0 -> 1,
			3.5 -> 2,
			4.0 -> 0,
			6.0 -> 1,
			7.0 -> 2
			];
		timeToDur = IdentityDictionary[
			0.25 -> 0.25,
			0.375 -> 0.25,
			0.4375 -> 0.25,
			0.125 -> 0.125,
			0.1875 -> 0.125,
			0.21875 -> 0.125,
			0.0625 -> 0.0625,
			0.09375 -> 0.0625,
			0.109375 -> 0.0625,
			0.03125 -> 0.03125,
			0.046875 -> 0.03125,
			0.0546875 -> 0.03125,
			0.015625 -> 0.015625,
			0.0234375 -> 0.015625,
			0.02734375 -> 0.015625,
			0.0078125 -> 0.0078125,
			0.01171875 -> 0.0078125,
			0.013671875 -> 0.0078125,
			0.5 -> 0.5,
			0.75 -> 0.5,
			0.875 -> 0.5,
			1.0 -> 1.0,
			1.5 -> 1.0,
			1.75 -> 1.0,
			2.0 -> 2.0,
			3.0 -> 2.0,
			3.5 -> 2.0,
			4.0 -> 4.0,
			6.0 -> 4.0,
			7.0 -> 4.0
			];
		}
	}

NtkScore : NtkObj {
	var <parts, partIdx;

	*new {
		^super.new.initNtkScore;
		}

	*open {arg path;
		this.readArchive(path);
		}

	initNtkScore {
		parts = IdentityDictionary.new;
		partIdx = 0;
		}

	add {arg ... newParts;
		newParts.do({arg aPart;
			aPart.isKindOf(NtkPart).if({
				parts.add(aPart.id -> [aPart, partIdx])
				});
			partIdx = partIdx + 1;
			});
		}

	// just outputs an archive of parts -> voices -> note data
	output {arg path;
		this.writeArchive(path);
		}

	sortParts {
		var theseParts, sortParts;
		theseParts = [];
		parts.do({arg me;
			theseParts = theseParts.add(me);
			});
		theseParts.sort({arg a, b; a[1] < b[1]});
		sortParts = theseParts.collect({arg me; me[0]});
		^sortParts;
		}

	asGuidoScore {
		var guidoScore, theseParts;
		guidoScore = GuidoScore.new;
		theseParts = this.sortParts;
		theseParts.do({arg aPart;
			var guidoPart;
			aPart.splitAtBarline;
			aPart.fillWithRests;
			guidoPart = aPart.asGuidoPart;
			guidoScore.add(guidoPart);
			});
		^guidoScore;
		}

	asLPScore {
		var lpScore, theseParts;
		lpScore = LPScore.new;
		theseParts = this.sortParts;
		theseParts.do({arg aPart;
			var lpPart;
			aPart.splitAtBarline;
			aPart.fillWithRests;
			lpPart = aPart.asLPPart;
			lpScore.add(lpPart);
			});
		^lpScore;
		}
	}

NtkPart : NtkObj {
	// events get priority over notes. They are things like clef changes, meter, etc.
	// notes are notes and rests
	var <id, <measures, <voices, <notes, <clef, <key, <timeSig, <tempo, <tempoEnv;
	var <timeSigs, <>instrumentName;

	*new {arg id, clef, key, timeSig, tempo;
		^super.new.initNtkPart(id, clef, key, timeSig, tempo);
		}

	// init the part, and create the first measure

	initNtkPart {arg argId, argClef, argKey, argTimeSig, argTempo;
		id = argId;
		// create the default voice, add any values above to the events array
		voices = [[]];
		// check if the clef, key and timeSigs are NtkObjects
		clef = (argClef ?? {NtkClef.treble(1, 1)});
		key = (argKey ?? {NtkKeySig.major(1, 1, \c)});
		timeSig = (argTimeSig ?? {NtkTimeSig(1, 4, 4)});
		timeSigs = Array.with(timeSig);
		// tempo is an Env that can be accessed by index?s		// users add to it with NtkTempos, but internally, all goes through the Env
		}

	timeSig_ {arg anNtkTimeSig;
		var measure;
		var lastTimeSig;
		lastTimeSig = timeSigs[timeSigs.size - 1];
		anNtkTimeSig = anNtkTimeSig ?? {
			NtkTimeSig(lastTimeSig.measure + 1,lastTimeSig.upper,
				lastTimeSig.lower, lastTimeSig.compound)};
		measure = anNtkTimeSig.measure;
		(measure >= timeSigs.size).if({
			(measure - 1 - timeSigs.size).do({arg i;
				timeSigs = timeSigs.add(
					NtkTimeSig.new(lastTimeSig.measure + i + 1,
						lastTimeSig.upper, lastTimeSig.lower, lastTimeSig.compound)
					);
				});
			});
		timeSigs = timeSigs.add(anNtkTimeSig);
		}

	timeSigAt {arg measure;
		^timeSigs[measure];
		}

	fillWithRests {
		var thisMeasure, rests;
		rests = Array.fill(voices.size, {[]});
		timeSigs.do({arg thisTimeSig, i;
			var thisMeasure, first, lower, rlower, rem, endBeat;
			lower = thisTimeSig.lower;
			rlower = lower.reciprocal;
			thisMeasure = this.notesForMeasure(i);
			thisMeasure.do({arg thisVoice, v;
				// are there notes?
				(thisVoice.size > 0).if({
					// is the first note on beat 0.0? If not -
					//	fill in the rest according to the beat
					((first = thisVoice[0].beat) != 0.0).if({
						// fill in full beats
						first.floor.do({arg beat;
							rests[v] = rests[v].add(NtkNote(\r, 1*rlower, i, beat))
							});
						// then any remainder
						((rem = (first % 1.0)) != 0.0).if({
							rests[v] = rests[v].add(NtkNote(\r, rem*rlower, i, first.floor));
							});
						});
					thisVoice.doAdjacentPairs({arg note1, note2;
						((first = note1.beat + (note1.duration * lower)) < note2.beat).if({
							// if the notes are in the same beat - just fill in the space
							(first.floor == note2.beat.floor).if({
								rests[v] = rests[v].add(
									NtkNote(\r, (note2.beat - first) * rlower, i, first));
								// otherwise - fill in the rest of this beat -
								// all complete beats, and the begining of the next
								}, {
								// is there some beat left over?
								((first % 1.0) != 0.0).if({
									rests[v] = rests[v].add(
										NtkNote(\r, (1 - (first % 1.0)) * rlower, i, first))
									});
								// are there whole beats in between?
								(note2.beat.floor - first.ceil).do({arg offset;
									rests[v] = rests[v].add(
										NtkNote(\r, rlower, i, first.ceil + offset))
									});
								// and any space IN a beat before the next note?
								((note2.beat % 1.0) != 0.0).if({
									rests[v] = rests[v].add(
										NtkNote(\r, note2.beat % 1.0, i, note2.beat.floor))
									});
								})
							})
						});
					// finally - find if the last note ends before the end of the measure
					((first = (thisVoice.last.beat + (thisVoice.last.duration * lower))) <
						thisTimeSig.upper).if({
							// fill in the rest of the current beat
							(((rem = (first.ceil - first)) % 1.0) != 0.0).if({
								rests[v] = rests[v].add(NtkNote(\r, rem * rlower, i, first));
								}, {
								rests[v] = rests[v].add(NtkNote(\r, rlower, i, first));
								});
							// any more full beats?
							(thisTimeSig.upper - first.ceil - 1).do({arg offset;
								rests[v] = rests[v].add(
									NtkNote(\r, rlower, i, first.ceil + offset + 1))
								})
							})
					}, {
					rests[v] = rests[v].add(
						NtkNote(\r, thisTimeSig.upper / thisTimeSig.lower, i, 0))
					})
				});
			});
		voices.do({arg thisVoice, i;
			voices[i] = voices[i] ++ rests[i];
			this.sortVoice(i)
			});
		}

	splitAtBarline {
		voices.do({arg thisVoice, i;
			var thisTimeSig, endBeat, oldDur, diff, lower, upper, adds;
			adds = [];
			thisVoice.do({arg note;
				thisTimeSig = timeSigs[note.measure];
				lower = thisTimeSig.lower;
				upper = thisTimeSig.upper;
				((endBeat = note.beat + (note.duration * lower)) > (upper)).if({
					diff = endBeat - upper;
					//[diff, upper - note.beat / lower].postln;
					note.rhythm_((upper - note.beat) / lower);
					note.tie_(\start);
					adds = adds.add(
						NtkNote(note.pitch, diff / lower, note.measure + 1, 0).tie_(\end));
					});
				});
			voices[i] = voices[i] ++ adds;
			this.sortVoice(i)
			});
		}

	// there has to be a more efficient way to do this
	// but this should work for now;
	eventsForMeasure {arg measure, voice;
		var theseVoices, thisVoice, test;
		theseVoices = [];
		voices.do({arg thisVoice;
			thisVoice = thisVoice.select({arg thisNote;
				thisNote.measure == measure;
				});
			theseVoices = theseVoices.add(thisVoice);
			});
		(voice.notNil and: {
			test = voice < theseVoices.size;
			test.not.if({"No voice exists with that given index".warn; ^this});
			test;
			}).if({
				^theseVoices[voice]
				}, {
				^theseVoices
				});
		}

	notesForMeasure {arg measure, voice;
		var theseVoices, theseNotes, tmp;
		theseVoices = this.eventsForMeasure(measure, voice);
		theseNotes = [];
		voice.notNil.if({
			theseNotes = theseVoices.select({arg thisEvent; thisEvent.isKindOf(NtkNote)});
			}, {
			theseVoices.do({arg thisVoice;
				tmp = thisVoice.select({arg thisEvent; thisEvent.isKindOf(NtkNote)});
				theseNotes = theseNotes.sort({arg a, b; a.index < b.index}).add(tmp);
				});
			});
		^theseNotes;
		}

	// accessing events
	at {arg measure, beat, voice;
		var testIdx;
		testIdx = measure + (beat * 0.001);

		}

	addVoice {arg voiceNum;
		(voices.size < voiceNum).if({
			(voiceNum - voices.size).do({arg i;
				voices = voices.add([]);
				});
			})
		}

	add {arg ... events;
		this.addToVoice(0, *events);
		}

	addToVoice {arg voice ... events;
		voices[voice] = voices[voice] ++ events;
		events.do({arg me;
			(me.measure > (timeSigs.size - 1)).if({
				(me.measure - (timeSigs.size - 1)).do({
					this.timeSig_()
					})
				})
			})
		}

	sortVoice {arg voice = 0;
		voices[voice].sort({arg a, b; a.isKindOf(NtkPriorityOneEvent)});
		voices[voice].sort({arg a, b; a.index <= b.index});
		}

	buildTempoEnv {

		}

	// figure out how to pass in all the 'part' based components
	// should be done here

	asGuidoPart {
		var part;
		part = GuidoPart.new(id, clef: clef.asGuidoEvent, key: key.asGuidoEvent, timeSig: timeSig.asGuidoEvent);
		voices.do({arg thisVoice, i;
			this.sortVoice(i);
			thisVoice.do({arg thisEv;
				part.add(thisEv.asGuidoEvent)
				})
			});
		^part;
		}

	asLPPart {
		var part;
		part = LPPart.new(id, clef: clef.asLPEvent, keySig: key.asLPEvent, timeSig: timeSig.asLPEvent);
		instrumentName.notNil.if({
			part.addInstrumentName(instrumentName);
			});
		voices.do({arg thisVoice, i;
			thisVoice.do({arg thisEv;
				this.sortVoice(i);
				part.addToVoice(i, thisEv.asLPEvent)
				});
			});
		^part;
		}

	}

// users do not see this
NtkEvent : NtkObj {
	var <measure, <beat, <rhythm, <tuplet, <dots, <event, <index, <duration;

	*new {arg measure = 1, beat = 1, rhythm, tuplet;
		^super.new.initNtkEvent(measure, beat, rhythm, tuplet);
		}

	initNtkEvent {arg argMeasure, argBeat, argRhythm, argTuplet;
		measure = argMeasure;
		beat = argBeat;
//		this.calcRhythm(argRhythm);
		this.rhythm_(argRhythm);
		tuplet = argTuplet;
//		duration = rhythm.isKindOf(Symbol).if({
//			rhythmToDur[rhythm];
//			}, {
//			rhythm
//			});
		this.calcIndex;
		}

	calcIndex {
		index = measure + (beat * 0.001);
		}

	rhythm_ {arg aRhythm;
		rhythm = aRhythm;
		duration = rhythm.isKindOf(Symbol).if({
			rhythmToDur[rhythm]
			}, {
			rhythm
			})
		}

	measure_ {arg newMeasure;
		measure = newMeasure;
		this.calcIndex;
		}

	beat_ {arg newBeat;
		beat = newBeat;
		this.calcIndex;
		}

	asGuidoEvent { }
	asLPEvent { }
	}

NtkNote : NtkEvent {
	var <pitch, <dynamic, <articulation, <>tie;
	*new {arg pitch, rhythm, measure, beat;
		^super.new(measure, beat, rhythm).initNtkNote(pitch);
		}

	initNtkNote {arg argPitch;
		pitch = argPitch;
		}

	addDynamic {arg dynamicSym, above = false;
		dynamic = [dynamicSym, above];
		}

	addArticulation {arg articulationName;
		articulation = articulationName;
		}

	adjustPitch {arg newPitch;
		pitch = newPitch;
	}

	asGuidoEvent {
		var note;
		note = GuidoNote.new(pitch, rhythm).beat_(beat).measure_(measure);
		dynamic.notNil.if({
			note.addDynamic(dynamic[0], dynamic[1])
			});
		articulation.notNil.if({
			note.addArticulation(articulation)
			});
		tie.notNil.if({
			note.tie_(tie)
			});
		^note;
		}

	asLPEvent {
		var note;
		note = LPNote.new(pitch, rhythm).beat_(beat).measure_(measure);
		dynamic.notNil.if({
			note.addDynamic(dynamic[0], dynamic[1])
			});
		articulation.notNil.if({
			note.addArticulation(articulation)
			});
		^note;
		}
	}

// simply allows for a check for events that should be placed before notes!
NtkPriorityOneEvent : NtkEvent {

	}

NtkClef : NtkPriorityOneEvent {
	var <clef, <line, <name;

	*new {arg measure, beat, clef, line, name;
		^super.new(measure, beat).initNtkClef(clef, line, name);
		}

	*treble {arg measure, beat;
		^this.new(measure, beat, \g, 2, \treble)
		}

	*bass {arg measure, beat;
		^this.new(measure, beat, \f, 4, \bass)
		}

	*alto {arg measure, beat;
		^this.new(measure, beat, \c, 3, \alto)
		}

	*tenor {arg measure, beat;
		^this.new(measure, beat, \c, 4, \tenor)
		}

	*soprano {arg measure, beat;
		^this.new(measure, beat, \c, 1, \soprano)
		}

	initNtkClef {arg argClef, argLine, argName;
		clef = argClef;
		line = argLine;
		name = argName;
		}

	asGuidoEvent { ^GuidoClef(name).measure_(measure).beat_(beat) }
	asLPEvent { ^LPClef(name).measure_(measure).beat_(beat) }
	}

NtkTimeSig : NtkPriorityOneEvent {
	var <upper, <lower, <compound, <totalDur;

	*new {arg measure, upper, lower, compound = false;
		^super.new(measure, 1).initNtkTimeSig(upper, lower, compound);
		}

	initNtkTimeSig {arg argUpper, argLower, argCompound;
		upper = argUpper;
		lower = argLower;
		compound = argCompound;
		totalDur = upper / lower;
		compound.if({
			((upper % 3) != 0).if({
				"Compound time signature should have a numerator that is divisible by 3".warn;
				compound = false;
				})
			})
		}

	numBeats {
		case
			{upper.isKindOf(Array)}
			{^upper.size}
			{compound}
			{^upper / 3}
			{true}
			{^upper};
		}

	beatDur {arg beat;
		case
			{upper.isKindOf(Array)}
			{(upper.size < (beat.size - 1)).if({
				^(upper[beat] / lower)
				})}
			{compound}
			{^3/lower}
			{true}
			{^1/lower};
		}

	beatMul {arg beat;
		case
			{compound}
			{^3 * lower}
			{true}
			{^lower};
		}

	asGuidoEvent { ^GuidoTimeSig(upper, lower).measure_(measure) }
	asLPEvent { ^LPTimeSig(upper, lower).measure_(measure) }
	}

NtkKeySig : NtkPriorityOneEvent {
	var <tonic, <mode;

	*new {arg measure, beat, tonic, mode;
		^super.new(measure, beat).initNtkKeySig(tonic, mode);
		}

	*major {arg measure, beat, tonic;
		^this.new(measure, beat, tonic, \major);
		}

	*minor {arg measure, beat, tonic;
		^this.new(measure, beat, tonic, \minor);
		}

	initNtkKeySig {arg argTonic, argMode;
		tonic = argTonic;
		mode = argMode;
		}

	asGuidoEvent { var gTonic;
		gTonic = (mode == \major).if({
			tonic.asString.toUpper.asSymbol;
			}, {
			tonic.asString.toLower.asSymbol;
			});
		^GuidoKeySig(gTonic).measure_(measure).beat_(beat);
		}

	asLPEvent { ^LPKeySig(tonic, mode).measure_(measure).beat_(beat) }
	}
	