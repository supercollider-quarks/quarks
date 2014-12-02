XMLMusicObj {
	classvar durToTypes, symToTypes, symToDur, baseDurs, accToAlter, typeToBeam, durToDots;
	var <duration, <type;
	
	*initClass {
		/* incorporate dotted values */
		durToTypes = IdentityDictionary[
			0.25 -> "quarter",			
			0.375 -> "quarter",			
			0.4375 -> "quarter",			
			0.125 -> "eighth",
			0.1875 -> "eighth",
			0.21875 -> "eighth",
			0.0625 -> "16th",
			0.09375 -> "16th",
			0.109375 -> "16th",
			0.03125 -> "32nd",
			0.046875 -> "32nd",
			0.0546875 -> "32nd",
			0.015625 -> "64th",
			0.0234375 -> "64th",
			0.02734375 -> "64th",
			0.0078125 -> "128th",
			0.01171875 -> "128th",
			0.013671875 -> "128th",
			0.00390625 -> "256th",
			0.5 -> "half",
			0.75 -> "half",
			0.875 -> "half",
			1.0 -> "whole",
			1.5 -> "whole",
			1.75 -> "whole",
			2.0 -> "breve",
			3.0 -> "breve",
			3.5 -> "breve",
			4.0 -> "long",
			6.0 -> "long",
			7.0 -> "long"
			];
		durToDots = IdentityDictionary[
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
		symToTypes = IdentityDictionary[
			\q -> "quarter",
			\qd -> "quarter", 		
			\qdd -> "quarter", 
			\e -> "eighth",
			\ed -> "eighth",
			\edd -> "eighth",
			\s -> "16th",
			\sd -> "16th",
			\sdd -> "16th",
			\t -> "32nd",
			\td -> "32nd",
			\tdd -> "32nd",
			\x -> "64th",
			\xd -> "64th",
			\xdd -> "64th",
			\o -> "128th",
			\od -> "128th",
			\odd -> "128th",
			\h -> "half",
			\hd -> "half",
			\hdd -> "half",
			\w -> "whole",
			\wd -> "whole",
			\wdd -> "whole",
			\b -> "breve",
			\bd -> "breve",
			\bdd -> "breve",
			\l -> "long",
			\ld -> "long",
			\ldd -> "long"
			];
		symToDur = IdentityDictionary[
			\q -> 0.25,
			\qd -> 0.375,
			\qdd -> 0.4375,			
			\e -> 0.125,
			\ed -> 0.1875,
			\edd -> 0.21875,
			\s -> 0.0625,
			\sd -> 0.09375,
			\sdd -> 0.109375,
			\t -> 0.03125,
			\td -> 0.046875,
			\tdd -> 0.0546875,
			\x -> 0.015625,
			\xd -> 0.0234375,
			\xdd -> 0.02734375,
			\o -> 0.0078125,
			\od -> 0.01171875,
			\odd -> 0.013671875,
			\h -> 0.5,
			\hd -> 0.75,
			\hdd -> 0.875,
			\w -> 1.0,
			\wd -> 1.5,
			\wdd -> 1.75,
			\b -> 2.0,
			\bd -> 3.0,
			\bdd -> 3.5,
			\l -> 4.0,
			\ld -> 6.0,
			\ldd -> 7.0
			];
		typeToBeam = Dictionary[
			"quarter" -> 0,			
			"eighth" -> 1,
			"16th" -> 2,
			"32nd" -> 3,
			"64th" -> 4,
			"128th" -> 5,
			"256th" -> 6,
			"half" -> 0,
			"whole" -> 0,
			"breve" -> 0,
			"long" -> 0
			];
		baseDurs = 
			[ 0.00390625, 0.0078125, 0.015625, 0.03125, 0.0625, 0.125, 0.25, 0.5, 1.0, 2.0, 4.0 ];
		accToAlter = IdentityDictionary[
			\ffff -> -4,
			\fff -> -3,
			\ff -> -2,
//			\tqf -> -1.5,
			\f -> -1,
//			\qf -> -0.5,
			\n -> 0,
//			\qs -> 0.5,
			\s -> 1,
//			\tqs -> 1.5,
			\ss -> 2,
			\sss -> 3,
			\ssss -> 4]
		}
		
	duration_ {arg newDuration, tuplet;
		var idx, floatDur;
		case
			{newDuration.isKindOf(SimpleNumber)}
			{
				type = durToTypes[newDuration.asFloat.round(0.0001)];
				floatDur = newDuration;
				this.dots_(durToDots[floatDur]);
					
			}
			{newDuration.isKindOf(Symbol)}
			{
				type = symToTypes[newDuration];
				floatDur = symToDur[newDuration];
				this.dots_(newDuration.asString.size - 1);
			}
			{true}
			{nil};
		(type.isNil and: {newDuration.isKindOf(SimpleNumber)}).if({
			// is there a tuplet to undo?
			floatDur = newDuration * (tuplet.reciprocal);
			type = durToTypes[floatDur];
			});
		duration = floatDur * tuplet.notNil.if({tuplet.reciprocal}, {1});
		this.beam = typeToBeam[type];
		(this.beam > 0).if({this.beamType = Array.fill(this.beam, {\none})});
		
		}
	

	numBeats {arg anXMLPart;
		var beatFloor, beatDur, remDur, beat, thisMeter, pctOfBeat, compoundScale, base, 
			tmpDur, newDur, i = 0, test;
		beat = this.beat;
		beatFloor = beat.floor;
		beatDur = anXMLPart.getBeatDurFromBeat(beatFloor);
		// how much duration is left in the beat?
		thisMeter = anXMLPart.getMeterFromBeat(beatFloor);
		pctOfBeat = ((beat - beatFloor) > 0).if({
			1 - (beat - beatFloor)
			}, {
			(beat + 1) - beatFloor
			});
		remDur = beatDur * pctOfBeat;
		(duration > remDur).if({
			tmpDur = duration - remDur;
			base = remDur / beatDur;
			while({
				i = i + 1;
				beatDur = anXMLPart.getBeatDurFromBeat(beatFloor + i);
				(tmpDur > beatDur).if({
					test = true;
					base = base + 1;
					tmpDur = tmpDur - beatDur;
					}, {
					test = false;
					base = base + (tmpDur / beatDur);
					});
				test
				});
			^base;
			}, {
			^duration / beatDur;
			});
			
		}
	}

// holds parts, and outputs the actual MusicXML file

XMLScore {
	var <score, file, <doc;
	*new {
		^super.new.initXMLScore;
		}
		
	initXMLScore {
		score = [];
		doc = DOMDocument.new;
		}
		
	add {arg ... parts;
		parts.do({arg aPart;
			aPart.isKindOf(XMLPart).if({
				score = score.add(aPart);
				}, {
				"XMLScores can only add an XMLPart".warn
				})
			});
		}
			
	output {arg pathname;
		file = File.new(pathname, "w");
		file.putString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE score-partwise PUBLIC \"-/"++"/Recordare/"++"/DTD MusicXML 1.0 Partwise/"++"/EN\"\n\t\t\t\t\"http:/"++"/www.musicxml.org/dtds/partwise.dtd\">\n");
		this.populateScore;
         doc.write(file);
         file.close;
		}
		
	populateScore {
		var scorePartwise, partList, scorePart ;
		doc.appendChild(scorePartwise = doc.createElement("score-partwise")
			.setAttribute("version", "1.0"));
		scorePartwise.appendChild(partList = doc.createElement("part-list"));
		score.sort({arg a, b; (a.id == b.id).if({
				^"AAAAAH!!! you have parts with the same id... very bad, I won't even try it!".warn;});
			a.id < b.id});
		score.do({arg me, i;
			partList.appendChild(scorePart = doc.createElement("score-part")
				.setAttribute("id", "P"++me.id));
			scorePart.appendChild(doc.createElement("part-name")
				.appendChild(doc.createTextNode(me.partName.asString)));
				});
		score.do({arg me;
			me.addToDoc(doc, scorePartwise);
			});
		}


}

/* build in some info type things - where is beat x? etc. 
perhaps also a way to insert measures (so - offset what is already there, and shift data) */

XMLPart {
	var <partName, <id, breakAcrossMeasures, <measures, <timeSigs, <keySigs, <clefs;
	var <timeSigMeasures, <timeSigInstances, <keySigMeasures, <keySigInstances, 
		<clefMeasures, <clefInstances, sortNeeded; // <notes
	var curMeter, curClef, curKey, <beatDurArray, <measureBeatArray;
	var beatKeeper;
	var <voices, <numVoices;
	
	*new {arg partName, id = 1, meter, key, clef, breakAcrossMeasures = true;
		^this.newCopyArgs(partName, id, breakAcrossMeasures).initXMLPart(meter, key, clef);
		}
		
	initXMLPart {arg meter, key, clef;
		timeSigs = [];
		keySigs = [];
		clefs = [];
		meter.notNil.if({this.addMeter(1, meter)});
		key.notNil.if({this.addKey(1, key)});
		clef.notNil.if({this.addClef(1, clef)});
		measures = [];
		beatDurArray = [];
		sortNeeded = true;
		beatKeeper = BeatKeeper.new(1.0);
		numVoices = 1;
		voices = [XMLVoice.new(1, this)];
		}
	
	now {
		^beatKeeper.now
		}
		
	now_ {arg newNow;
		beatKeeper.now_(newNow);
		}
		
	incNextBeat {
		beatKeeper.now_(beatKeeper.now.ceil);
		}
		
	/* perhaps add a 'shift' flag. If there is data after this, should it shift? Or, better yet
	make an insert measure function */
	
	addMeter {arg measure, meter;
		meter.isKindOf(XMLMeter).if({
			timeSigs = timeSigs.add([measure, meter]);
			sortNeeded = true;
			}, {
			"Only XMLMeter objects can be added as a meter".warn
			});	
		}

	addKey {arg measure, key;
		key.isKindOf(XMLKey).if({
			keySigs = keySigs.add([measure, key]);
			sortNeeded = true;
			}, {
			"Only XMLkey objects can be added as a key".warn
			});	
		}
		
	addClef {arg measure, clef;
		clef.isKindOf(XMLClef).if({
			clefs = clefs.add([measure, clef]);
			sortNeeded = true;
			}, {
			"Only XMLClef objects can be added as a clef".warn
			});	
		}
		
	addVoice {arg showRests = true;
		var newVoice;
		numVoices = numVoices + 1;
		newVoice = XMLVoice.new(numVoices, this);
		voices = voices.add(newVoice); // add at the end... should have an index in the array of 
								// numVoices - 1
		^newVoice;
		}
	
	voice1 {
		^voices[0]
		}
		
	addToVoice {arg voice ... noteEvents;
		var thisVoice, test, newStart, newEnd, testStart, testEnd;
		// this Voice can be the instance of XMLVoice or the voice number
		thisVoice = voice.isKindOf(XMLVoice).if({
			voice
			}, {
			voices[voice - 1];
			});
		noteEvents.flat.do({arg me;
			newStart = me.beat;
			newEnd = me.endBeat(this);
			thisVoice.add(me);
			(me.beat >= beatKeeper.now).if({
				beatKeeper.now_(me.beat);
				beatKeeper.wait(me.duration, this.getBeatDurFromBeat(me.beat));
				});
			});
		}	
	
	// probably the most general case... add to voice 1
	add {arg ... noteEvents;
		this.addToVoice(1, noteEvents);
		}
		
	setupMeasures {arg endBeat;
		var voiceEnd, tmp, curMeasureNumber, curNumBeats, curMeter, curKey, curClef;
		var thism; // for debug
		endBeat = endBeat ?? {
			voiceEnd = 0;
			voices.do({arg thisVoice;
				tmp = thisVoice.getLastBeat;
				(tmp > voiceEnd).if({voiceEnd = tmp});
				});
			(voiceEnd > this.now).if(voiceEnd, this.now); 
			};
		this.fillMeasureBeatArray;
		// create measures up to endBeat
		tmp = 1;
		curMeasureNumber = 1;
		while({
			#curMeter, curKey, curClef = this.getAttributes(curMeasureNumber);
			curNumBeats = curMeter.numBeats;
			measures = measures.add(
				thism = XMLMeasure(curMeasureNumber, curMeter, curClef, curKey, tmp));
			tmp = tmp + curNumBeats;
			curMeasureNumber = curMeasureNumber + 1;
			tmp <= endBeat;
			});
		}
		
	/* sendToMeasures should go through the voices, and place notes into measures. Then! 
	tell the measures to fill in rests of forwards */
	
	sendToMeasures {
		// fill with rests here... let the other function break them up as needed */
		this.setupMeasures;
		// this fills the measures with note and rest / forward data... 
		// voices can access the measures array
		voices.do({arg thisVoice, i; 
			thisVoice.fillMeasures(this, i)
			});
		}
	
	sortPartAttributes {
		timeSigs.sort({arg a, b; (a[0] == b[0]).if({
				"It appears that measure " ++ a[0] ++ " has two time signatures assigned to it... things probably won't look right!".warn;
				});
			a[0] < b[0]
			});
		#timeSigMeasures, timeSigInstances = timeSigs.flop;
		keySigs.sort({arg a, b; (a[0] == b[0]).if({
				"It appears that measure " ++ a[0] ++ " has two key signatures assigned to it... things probably won't look right!".warn;
				});
			a[0] < b[0]
			});
		#keySigMeasures, keySigInstances = keySigs.flop;
		clefs.sort({arg a, b; (a[0] == b[0]).if({
				"It appears that measure " ++ a[0] ++ " has two clefs assigned to it... things probably won't look right!".warn;
				});
			a[0] < b[0]
			});
		#clefMeasures, clefInstances = clefs.flop;
		sortNeeded = false;
		}
	
	getAttributes {arg curMeasure;
		var thisMeter, thisKey, thisClef, idx;
		/* set up the first measure, get the current attributes */
		thisMeter = (timeSigs.size > 0 and: {timeSigMeasures[0] == 1}).if({
			idx = timeSigMeasures.indexInBetween(curMeasure).floor;
			timeSigInstances[idx];
			}, {
			curMeter ?? {XMLMeter(4, 4)};
			});
		thisKey = (keySigs.size > 0 and: {keySigMeasures[0] == 1}).if({
			idx = keySigMeasures.indexInBetween(curMeasure).floor;
			keySigInstances[idx];
			}, {
			curKey ?? {XMLKey.major(\C)};
			});
		thisClef = (clefs.size > 0 and: {clefMeasures[0] == 1}).if({
			idx = clefMeasures.indexInBetween(curMeasure).floor;
			clefInstances[idx];
			}, {
			curClef ?? {XMLClef.treble}
			});
		^[thisMeter, thisKey, thisClef];
		} 
	
	addToDoc {arg doc, scorePartwise;
		var partElement, attribute, note;
		scorePartwise.appendChild(
			partElement = doc.createElement("part").setAttribute("id", "P"++id));
		this.sendToMeasures;
		measures.do({arg measure;
			measure.appendAttributes(doc, partElement, this)
			});
		}
	
	// fills an array with the number of beats in each measure... e.g:
	// [4, 4, 4, 3, 4, 4] tells you measure 1, 2, 4 and 5 have 4 beats, measure 3 has 3
	// fist element is a 0th measure, that really doesn't exist. makes indexing easier.
	fillMeasureBeatArray {
		var idx;
		sortNeeded.if({this.sortPartAttributes});
		measureBeatArray = (timeSigMeasures.maxItem + 1).collect({arg curMeasure;
			curMeasure = curMeasure;
			idx = timeSigMeasures.indexInBetween(curMeasure).floor;
			timeSigInstances[idx].numBeats;
			});
		^measureBeatArray;
		}
	
	// returns the measure and beat where a beat exists
	getMeasureFromBeat {arg beat;
		var measure = 1, tmpBeat, curBeat, theseBeats, maxSize, idx;
		tmpBeat = beat;
		this.fillMeasureBeatArray;
		maxSize = measureBeatArray.size - 1;
		curBeat = 1 + (theseBeats = measureBeatArray[measure]);
		(curBeat > beat).if({
			^[measure, beat]
			}, {
			while({
				tmpBeat = tmpBeat - theseBeats;
				measure = measure + 1;
				idx = measure.min(maxSize);
				theseBeats = measureBeatArray[idx];
				curBeat = curBeat + measureBeatArray[idx];
				curBeat <= beat;
				});
			^[measure, tmpBeat]
			});
		}
		
	getBeatDurFromBeat {arg beat;
		var thisMeasure, thisBeat, meter, key, clef, beatDurInMeasure;
		#thisMeasure, thisBeat = this.getMeasureFromBeat(beat);
		#meter, key, clef = this.getAttributes(thisMeasure);
		meter.upper.isKindOf(Array).if({
			beatDurInMeasure = meter.upper[thisBeat - 1];
			}, {
			beatDurInMeasure = meter.divisor; // (meter.type == \compound).if({3}, {1}); //meter.upper;
			});
		^meter.lower.reciprocal * beatDurInMeasure;
		}

	getBeatDur {arg beat;
		var beatDurArraySize;
		sortNeeded.if({this.sortPartAttributes});
		^((beat - 1) < beatDurArraySize = beatDurArray.size).if({
			beatDurArray[beat - 1]
			}, {
			beatDurArray[beatDurArraySize - 1]
			})
			
		}
		
	getMeterForMeasure {arg measure;
		var idx;
		^(timeSigs.size > 0 and: {timeSigMeasures[0] == 1}).if({
			idx = timeSigMeasures.indexInBetween(measure).floor;
			timeSigInstances[idx];
			}, {
			curMeter ?? {XMLMeter(4, 4)};
			});	
		}
		
	getMeterFromBeat {arg beat;
		var measure;
		measure = this.getMeasureFromBeat(beat);
		^this.getMeterForMeasure(measure[0]);
		}
		
	getDurationBetweenBeats {arg beat1, beat2;
		var dif, beg, mid, end, b1floor, b2floor, dursum, beatsRem;
		b1floor = beat1.floor;
		b2floor = beat2.floor;
		// does this span more then one beat?
		(((b2floor - b1floor) > 0) and: {b2floor - beat2 > 0}).if({
			beg = b1floor; // the first beat to look for
			dursum = ((beat1 - b1floor) > 0).if({
				this.getBeatDurFromBeat(b1floor) * ((beat1 - b1floor));
				}, {
				this.getBeatDurFromBeat(b1floor)});
			dif = beat2 - b2floor;
			dursum = dursum + (this.getBeatDurFromBeat(b2floor) * dif);
			// complete beats to fill
			beatsRem = Array.series(b2floor-b1floor-1, b1floor, 1);
			beatsRem.do({arg me;
				dursum = dursum + this.getBeatDurFromBeat(me);
				});
			^dursum;
			}, {
			// same beat! just need to figure out the duration based on a single beats duration
			dif = beat2 - beat1;
			^(this.getBeatDurFromBeat(beat1.floor) * dif);
			})
		}
}

// voice numbers should be stored here... index at 1
// can set things like stemDir, rest offsets, etc.
XMLVoice {
	var <voiceNum, <part, <showRests, <notes, <rests, sorted;
	
	*new {arg voiceNum, part, showRests = true;
		^super.newCopyArgs(voiceNum, part, showRests).initXMLVoice;
		}
		
	initXMLVoice {
		notes = [];
		rests = [];
		sorted = false;
		}
		
	add {arg ... newNotes;
		// check for overlapping notes... post a warning and DON'T add the notes
		notes = notes ++ newNotes; // ???
		sorted.if({sorted = false});
		}
	
	sort {
		sorted.not.if({
			notes.sort({arg a, b; a.beat < b.beat});
			});
		sorted = true;
		}
		
	getLastBeat {
		this.sort;
		(notes.size > 0).if({
			^notes[notes.size-1].endBeat(part);
			}, {
			^1.0
			});
		}

	// sort notes in a voice, access the measures in a part, and fill with notes.
	fillMeasures {arg part, voiceIdx;
		var theseNotes, curNote, curMeasure, curMeasureIdx, curBeat, mStart, mEnd, numMeasures;
		var newDur, remain, newNote, restsToAdd;
		theseNotes = notes.copy;
		curMeasureIdx = 0;
		numMeasures = part.measures.size;
		curMeasure = part.measures[curMeasureIdx];
		mStart = curMeasure.firstBeat;
		mEnd = curMeasure.lastBeat;
		restsToAdd = [];
		theseNotes.sort({arg a, b; a.beat < b.beat});
		(theseNotes.size - 1).do({arg i;
			var note1, note2, note1EndBeat, noteDur;
			note1 = theseNotes[i];
			note2 = theseNotes[i + 1];
			note1EndBeat = note1.endBeat(part).round(0.0001);
			(note1EndBeat < note2.beat).if({
				noteDur = part.getDurationBetweenBeats(note1EndBeat, note2.beat).round(0.0001);
				(noteDur > 0.0001).if({
					restsToAdd = restsToAdd.add(XMLRest(note1EndBeat, noteDur.round(0.00001)))
					})
				});
			});
		theseNotes = theseNotes ++ restsToAdd;
		theseNotes.sort({arg a, b; a.beat < b.beat});		
		// this also needs to be a while loop! add to theseNotes as needed
		while({
			curNote = (theseNotes.size > 0).if({
				theseNotes.removeAt(0);
				}, {
				nil
				});
			curNote.notNil;
			}, {	
			while({
				curNote.beat >= mEnd
				}, {
				curMeasureIdx = curMeasureIdx + 1;
				numMeasures = part.measures.size;
				curMeasure = part.measures[curMeasureIdx];
				mStart = curMeasure.firstBeat;
				mEnd = curMeasure.lastBeat;
				});
			// does this note extend past the end of the measure?
			(curNote.endBeat(part) > mEnd).if({
				newDur = part.getDurationBetweenBeats(curNote.beat, curMeasure.lastBeat);
				newDur = newDur.quantize(0.0001);
				remain = curNote.duration - newDur;
				(remain > 0.0001).if({
					remain = remain.round(0.00001);
					newNote = XMLNote.new(curNote.pc, curMeasure.lastBeat, remain, 
							curNote.tuplet)
						.tieStop_(true)
						.tieStart_(curNote.tieStart);
					curNote.duration_(newDur).tieStart_(true);
					theseNotes.addFirst(newNote);
					curMeasure.addNote(voiceIdx, curNote);
					}, {
					curMeasure.addNote(voiceIdx, curNote);
					})
				}, {
				(curMeasure.voices.size >= voiceIdx).if({
					(voiceIdx - (curMeasure.voices.size - 1)).do({
						curMeasure.voices = curMeasure.voices.add([]);
						})
					});
				curMeasure.addNote(voiceIdx, curNote);
				})
			});
		}
}

XMLEvent {


}

XMLNote : XMLMusicObj {
	var <>beat, <tuplet, <note, <pc, <thisNote;
	// beam is an array of beam elements, beamType is \begin, \continue, \end or \none
	// beamType is set in XMLMeasure, beam is set in duration_
	// stem is \up, \down, \none, \double
	var <>beam, <>beamType, <>stem, <>backup, <>forward, <>voiceNum, <tieStop, <tieStart, 
		<notations, <>dots = 0, <>chord;
	
	*new {arg aPitchClass, beat = 1.0, duration = 1.0, tuplet;
		^super.newCopyArgs(nil, nil, beat, tuplet).initXMLNote(aPitchClass, duration);
		}
		
	initXMLNote {arg argAPitchClass, argDuration;
		var tmp;
		this.note_(argAPitchClass);
		this.duration_(argDuration, tuplet);
		tieStop = tieStart = false;
		notations = [];
		chord = false;
		}
		
	note_ {arg aPitchClass;
		pc = aPitchClass.isKindOf(Number).if({PitchClass(aPitchClass)}, {aPitchClass});
		}
	
	beatsInMeter {arg beat = 1, anXMLMeter;
		anXMLMeter = anXMLMeter ?? {XMLMeter(4, 4)};
		^(duration / anXMLMeter.beatDur(beat));
		}
		
	endBeat {arg anXMLPart;
		^(beat + this.numBeats(anXMLPart)).round(0.00001);
		}
	
	tieStop_ {arg bool;
		tieStop = bool;
		tieStop.if({
			notations = notations.add(\tieStop)
			}, {
			notations.remove(\tieStop);
			})
		}

	tieStart_ {arg bool;
		tieStart = bool;
		tieStart.if({
			notations = notations.add(\tieStart)
			}, {
			notations.remove(\tieStart);
			})
		}

	appendToMeasure {arg tree, doc, divisions = 1;
		//var thisNote, 
		var thisType, idx, leftover, bu, budur, for, fordur, thisVoice;
		var pitch, step, alter, octave, thisDuration, tmpDur, tupletTop, tupletBottom, timeMod,
			actual, normal, thisNotations;
		backup.notNil.if({
			bu = doc.createElement("backup");
			budur = doc.createElement("duration").appendChild(doc.createTextNode(
				((backup * divisions).round * 4).asString));
			bu.appendChild(budur);
			tree.appendChild(bu);
			});
		forward.notNil.if({
			for = doc.createElement("forward");
			fordur = doc.createElement("duration").appendChild(doc.createTextNode(
				((forward * divisions).round * 4).asString));
			for.appendChild(fordur);
			tree.appendChild(for);
			});
		thisNote = doc.createElement("note");
		pitch = doc.createElement("pitch");
		step = doc.createElement("step");
		step.appendChild(doc.createTextNode((pc.note.asString)[0].toUpper.asString));
		alter = doc.createElement("alter");	
		alter.appendChild(doc.createTextNode(accToAlter[pc.acc].asString));
		octave = doc.createElement("octave");
		octave.appendChild(doc.createTextNode(pc.octave.asInteger.asString));
		pitch.appendChild(step);
		pitch.appendChild(alter);
		pitch.appendChild(octave);
		thisNote.appendChild(pitch);
		tmpDur = duration.isKindOf(Symbol).if({symToDur[duration]}, {duration});
		thisDuration = doc.createElement("duration").appendChild(
				doc.createTextNode(
					((tmpDur * divisions).round * 4).asString));
		thisNote.appendChild(thisDuration);
		tieStop.if({
			thisNote.appendChild(
				doc.createElement("tie").setAttribute("type", "stop")
				)
			});
		tieStart.if({
			thisNote.appendChild(
				doc.createElement("tie").setAttribute("type", "start"))
			});
		voiceNum.notNil.if({
			thisVoice = doc.createElement("voice")
				.appendChild(doc.createTextNode(voiceNum.asString));
			thisNote.appendChild(thisVoice);
			});
		thisType = doc.createElement("type").appendChild(doc.createTextNode(type));
		thisNote.appendChild(thisType);
		dots.do({
			thisNote.appendChild(doc.createElement("dot"));
			});
		stem.notNil.if({
			thisNote.appendChild(
				doc.createElement("stem").appendChild(doc.createTextNode(stem.asString));
				)
			});
		beam.do({arg i;
			thisNote.appendChild(
				doc.createElement("beam").setAttribute("number", (i+1).asString)
					.appendChild(doc.createTextNode(beamType[i].asString)));
			});
		(tuplet.notNil and: {tuplet != 1.0}).if({
			#tupletTop, tupletBottom = tuplet.asFraction(768, false);
			timeMod = doc.createElement("time-modification");
			thisNote.appendChild(timeMod);
			actual = doc.createElement("actual-notes");
			timeMod.appendChild(actual);
			actual.appendChild(doc.createTextNode(tupletTop.asString));
			normal = doc.createElement("normal-notes");
			timeMod.appendChild(normal);
			normal.appendChild(doc.createTextNode(tupletBottom.asString));
			});
		(notations.size > 0).if({
			thisNote.appendChild(thisNotations = doc.createElement("notations"));
			notations.do({arg me;
				case
					{me == \tieStop}
					{thisNotations.appendChild(
						doc.createElement("tied").setAttribute("type", "stop"))}
					{me == \tieStart}
					{thisNotations.appendChild(
						doc.createElement("tied").setAttribute("type", "start"))}
					{true}
					{me.postln}
				});
			});
		tree.appendChild(thisNote);
		}
	
}

XMLMelody {


}

XMLRest : XMLMusicObj {
	var <>beat, <tuplet, <thisNote;
	var <>beam, <>beamType, <>dots, <>pc, <>voiceNum, <>backup, <>forward, <>stem;
	
	*new {arg beat = 1.0, duration = 1.0, tuplet = 1.0;
		^super.newCopyArgs(nil, nil, beat, tuplet).initXMLRest(duration);
		}
		
	initXMLRest {arg argDuration;
		var tmp;
		this.duration_(argDuration, tuplet);
		pc = PitchClass(0);
		}

	endBeat {arg anXMLPart;
		^(beat + this.numBeats(anXMLPart)).round(0.00001);
		}
		
	appendToMeasure {arg tree, doc, divisions = 1;
		var pitch, step, alter, octave, thisDuration, tmpDur, bu, budur, for, fordur, 
			thisVoice;
		backup.notNil.if({
			bu = doc.createElement("backup");
			budur = doc.createElement("duration").appendChild(doc.createTextNode(
				((backup * divisions).round * 4).asString));
			bu.appendChild(budur);
			tree.appendChild(bu);
			});
		forward.notNil.if({
			for = doc.createElement("forward");
			fordur = doc.createElement("duration").appendChild(doc.createTextNode(
				((forward * divisions).round * 4).asString));
			for.appendChild(fordur);
			tree.appendChild(for);
			});
		thisNote = doc.createElement("note");
		tree.appendChild(thisNote);
		thisNote.appendChild(doc.createElement("rest"));
		tmpDur = duration.isKindOf(Symbol).if({symToDur[duration]}, {duration});
		thisDuration = doc.createElement("duration").appendChild(
				doc.createTextNode(
					((tmpDur * divisions).round * 4).asString));
		thisNote.appendChild(thisDuration);
		voiceNum.notNil.if({
			thisVoice = doc.createElement("voice")
				.appendChild(doc.createTextNode(voiceNum.asString));
			thisNote.appendChild(thisVoice);
			});
		tree.appendChild(thisNote);
		}

}

XMLMeter {
	var <>upper, <>lower, <type, <division, <divisor;
	// numBeats is going to be the important one here!
	var <numBeats, <beatLength, <measureLength;
	
	*new {arg upper, lower, type = \simple;
		^super.newCopyArgs(upper, lower, type).initXMLMeter
		}
	
	initXMLMeter {
		upper.isKindOf(Array).if({
			numBeats = upper.size;
			divisor = 1;
			}, {
			(type == \compound).if({
				numBeats = upper / 3;
				divisor = 3;
				}, {
				numBeats = upper;
				divisor = 1;
				});
			});
		}
	
	beatDur {arg beat = 1;
		upper.isKindOf(Array).if({
			(beat <= upper.size).if({
				^upper[beat - 1] * lower.reciprocal;
				}, {
				("This meter has "++upper.size++" beats, you asked for "++beat).warn;
				^nil;
				})
			}, {
			^lower.reciprocal * divisor;
			});
		}
		
	measureDur {
		var lowerdur = lower.reciprocal;
		(upper.isKindOf(Array)).if({
			^upper.sum * lowerdur;
			}, {
			^upper * lowerdur
			});
		}
		
	// need to see how to do complex uppers
	appendAttributes {arg tree, doc;
		var upperString = "";
		(upper.isKindOf(Array)).if({
			upper.do({arg me, i;
				(i == 0).if({
					upperString = upperString ++ me;
					}, {
					upperString = upperString ++ "+" ++ me;
					})
				})
			}, {
			upperString = upper.asString;
			});
		tree.appendChild(
			doc.createElement("time").appendChild(
				doc.createElement("beats").appendChild(
					doc.createTextNode(upperString))).appendChild(
				doc.createElement("beat-type").appendChild(
					doc.createTextNode(lower.asString))));
		}



}

// clef type is \c, \g, \f, \p or \none???
XMLClef {	
	var <clefType, line, name;
	
	*new {arg clefType, line;
		^super.newCopyArgs(clefType, line).initXMLClef;
		}
		
	initXMLClef {
	
		}
		
	*treble {
		^this.new(\G, 2);
		}
		
	*bass {
		^this.new(\F, 4);
		}
		
	*alto {
		^this.new(\C, 3);
		}
		
	*soprano {
		^this.new(\C, 1);
		}
		
	*tenor {
		^this.new(\C, 4);
		}
		
	appendAttributes {arg tree, doc;
		tree.appendChild(
			doc.createElement("clef").appendChild(
				doc.createElement("sign").appendChild(
					doc.createTextNode(clefType.asString))).appendChild(
				doc.createElement("line").appendChild(
					doc.createTextNode(line.asString))));	
		}
	}

XMLKey {
	classvar majorToAcc, minorToAcc;
	var <accidentals, <mode;
	
	*new {arg accidentals = 0, mode = \major;
		^super.newCopyArgs(accidentals, mode).initXMLKey;
		}
		
	initXMLKey {
	
		}
	
	*major {arg key;
		^this.new(majorToAcc[key], \major);
		}
		
	*minor {arg key;
		^this.new(minorToAcc[key], \minor);
		}
			
	appendAttributes {arg tree, doc;
		tree.appendChild(
			doc.createElement("key").appendChild(
				doc.createElement("fifths").appendChild(
					doc.createTextNode(accidentals.asString))).appendChild(
				doc.createElement("mode").appendChild(
					doc.createTextNode(mode.asString))))
	
		}
		
	*initClass {
		majorToAcc = IdentityDictionary[
			\C -> 0,
			\G -> 1,
			\D -> 2,
			\A -> 3,
			\E -> 4,
			\B -> 5,
			\Fs -> 6,
			\Cs -> 7,
			\F -> -1,
			\Bf -> -2,
			\Ef -> -3,
			\Af -> -4,
			\Df -> -5,
			\Gf -> -6,
			\Cf -> -7];
		minorToAcc = IdentityDictionary[
			\a -> 0,
			\e -> 1,
			\b -> 2,
			\fs -> 3,
			\cs -> 4,
			\gs -> 5,
			\ds -> 6,
			\as -> 7,
			\d -> -1,
			\g -> -2,
			\c -> -3,
			\f -> -4,
			\bf -> -5,
			\ef -> -6,
			\af -> -7
			]
		}

}

/* user probably won't see XMLMeasures. XMLPart will create these */
XMLMeasure : XMLMusicObj {
	// no need for notes anymore... they are stored in voices' arrays
	var <measureNumber, <meter, <clef, <key, <firstBeat,/*<notes,*/<>divisions = 240, <key, <time,
		<lastBeat, <numBeats, <>voiceNum = 1, <>voices;
	
	*new {arg measureNumber, meter, clef, key, firstBeat = 1 ... notes;
		^super.newCopyArgs(nil, nil, measureNumber, meter, clef, key, firstBeat, 
			notes).initXMLMeasure;
		}
		
	initXMLMeasure {
		numBeats = meter.numBeats;
		// this beat doesn't exist in this measure .. it is the first beat of the next measure
		lastBeat = firstBeat + numBeats;
		voices = [[]]; 
		}
	
	addNote {arg voiceIdx ... newNotes;
		(voices.size <= voiceIdx).if({
			(voices.size - (voiceIdx - 1)).do({
				voices = voices.add([])
				})
			});
		voices[voiceIdx] = voices[voiceIdx] ++ newNotes;
		}

	appendAttributes {arg doc, partElement, part;
		var measure, tree, thesedurs, notesFlat, notesFlatSize, tmpNotes, myBeats, beatSort, tmp, 
			myDur, dif, lastPct, fillBeats, curNote, lastNoteEnd, tmpRest;
		myDur = 0;
		notesFlat = voices.flat;
		thesedurs = notesFlat.collect({arg me; me.duration});
		thesedurs = thesedurs.select({arg me; me > 0});
		thesedurs = thesedurs.collect({arg me; tmp = me.asFraction(768, false); tmp[1]});
		thesedurs.removeAllSuchThat({arg me, i; me < 0});
		divisions = (thesedurs.size > 1).if({thesedurs.reduce(\lcm)}, {thesedurs[0] ?? {1}});
		partElement.appendChild(doc.createComment("========== Data for Measure "++measureNumber ));
		measure = doc.createElement("measure").setAttribute("number", measureNumber.asString);
		measure.appendChild(tree = doc.createElement("attributes"));
		tree.appendChild(
			doc.createElement("divisions").appendChild(
					doc.createTextNode(divisions.asString)));
		key.appendAttributes(tree, doc);
		meter.appendAttributes(tree, doc);
		clef.appendAttributes(tree, doc);
		measure.appendChild(tree);
		notesFlatSize = notesFlat.size;
		myBeats = Array.series(numBeats, lastBeat - numBeats, 1.0);
		myBeats.do({arg thisBeat; myDur = myDur + part.getBeatDurFromBeat(thisBeat)});
		beatSort = Array.fill(numBeats, {[]});
		notesFlat.do({arg me;
			var thisBeat;
			thisBeat = me.beat.floor.asFloat;
			myBeats.indexOf(thisBeat);
			beatSort[myBeats.indexOf(thisBeat)] = 
				beatSort[myBeats.indexOf(thisBeat)].add(me);
			});
		voices.do({arg me, i;
			(me.size > 0).if({	
				(me[0].beat != myBeats[0]).if({
					((dif = me[0].beat - myBeats[0]) > 1.0).if({
						lastPct = dif - dif.floor;
						// figure out which beats are needed
						dif.floor.do({arg i;
							tmp = myBeats[i];
							me = me.add(XMLRest(tmp, part.getBeatDurFromBeat(tmp)));
							});
						(lastPct > 0).if({
							tmp = myBeats[i];
							me = me.add(XMLRest(tmp, 
								part.getBeatDurFromBeat(tmp) * lastPct));
							});
						}, {
						me = me.addFirst(
							XMLRest(myBeats[0], part.getBeatDurFromBeat(myBeats[0]) * dif,
								me[0].tuplet));
						})
					});
				});
			me.sort({arg a, b; a.beat < b.beat});
			voices[i] = me;
			});
		(voices.size > 0).if({
			voices.do({arg thisVoice, i;
				thisVoice.do({arg thisNote, j;
					thisNote.voiceNum_(i+1);
					((i != 0) and: {j == 0}).if({
						thisNote.backup_(myDur);
						})
					})
				})
			});
		this.beamToBeat;
		voices.do({arg thisVoice, i;
			var test = 0;
			thisVoice.do({arg thisNote;
				curNote = thisNote;
				(thisNote.duration > 0).if({
					thisNote.appendToMeasure(measure, doc, thesedurs.maxItem);
					})
				});
			/*
			(curNote.notNil and: {(lastNoteEnd = curNote.endBeat(part)) < lastBeat}).if({
				tmp = lastNoteEnd;
				// check first if a partial beat needs to be filled
				((lastNoteEnd - lastNoteEnd.floor) > 0).if({
					/* THIS ROUND IS A KLUDGE!!! */
					tmpRest = XMLRest(lastNoteEnd, 
						(part.getBeatDurFromBeat(lastNoteEnd.floor) *
							(lastNoteEnd - lastNoteEnd.floor)).round(0.00001),
						curNote.tuplet
						).voiceNum_(i + 1);
					tmpRest.appendToMeasure(measure, doc, thesedurs.maxItem);
					tmp = tmpRest.endBeat(part);
					});
				while({
					((tmp < (lastBeat - 0.001)) and: {test < 100});
					}, {
					tmpRest = XMLRest(tmp, part.getBeatDurFromBeat(tmp)).voiceNum_(i+1);
					tmpRest.appendToMeasure(measure, doc, thesedurs.maxItem);
					tmp = tmpRest.endBeat(part);
					test = test + 1;
					}); 
				})
			*/ 
			});
		// append to the part
		partElement.appendChild(measure);
		}

	beamToBeat {
		var beats, curBeat, curBeams, thisBeam, voiceSize, lastNote, extras;
		// work through each beat of the measure, and connect the appropriate beams
		voices.do({arg thisVoice;
			voiceSize = thisVoice.size;
			(voiceSize > 1).if({
				curBeams = 0;
				thisVoice.do({arg thisNote, i;
					// are they in the same beat???
					(curBeat == thisNote.beat.floor).if({
						thisBeam = thisNote.beam;
						// check on the situation!
						(thisBeam > 0).if({
							// if curBeams is > 0, then beams have already started
							case
								{curBeams == 0}
								{thisNote.beamType_(Array.fill(thisBeam, {\begin}))}
								{curBeams < thisBeam}
								// need to begin some, continue others
								{thisNote.beamType_(Array.fill(thisBeam, {\continue}));
								extras = thisBeam - lastNote.beam;
								extras.do({arg i; 
									thisNote.beamType[thisBeam + i - 1] = \begin
									})}
								{curBeams > thisBeam}
								// need to end some, continue others
								{thisNote.beamType_(Array.fill(thisBeam, {\continue}));
								extras = lastNote.beam - thisBeam;
								extras.do({arg i; lastNote.beamType[thisBeam + i] = \end})
								}
								{curBeams == thisBeam}
								{thisNote.beamType_(Array.fill(thisBeam, {\continue}))}
								{true}
								{thisNote.beamType_(Array.fill(thisBeam, {\wtf}))};
							curBeams = thisBeam;
							}, {
							(lastNote.notNil and: {lastNote.beam > 0}).if({
								lastNote.beamType_(Array.fill(lastNote.beam, {\end}))
								});
							})
						}, {
						curBeams = 0;
						(thisNote.beam > 0).if({
							curBeams = thisNote.beam;
							thisNote.beamType_(Array.fill(curBeams, {\begin}))
							});
						(lastNote.notNil and: {lastNote.beam > 0}).if({
							lastNote.beamType.do({arg me, i;
								case
									{me == \begin}
									{lastNote.beamType[i] = \none}
									{me == \continue}
									{lastNote.beamType[i] = \end}
									{true}
									{"no idea".postln;}
								})
							});	
						});				
					curBeat = thisNote.beat.floor;
					lastNote = thisNote;
					});
				});
			(lastNote.notNil and: {lastNote.beam > 0}).if({
				lastNote.beamType_(Array.fill(lastNote.beam, {\end}))
				});
			})

		}

}

// just a wrapper for multiple XMLNotes with the same beat and duration
XMLChord {


}

XMLTempo {
	var <>bpm;
	
	*new {arg bpm = 60;
		^super.newCopyArgs(bpm);
		}
}