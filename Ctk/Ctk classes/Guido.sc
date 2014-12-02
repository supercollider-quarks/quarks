GuidoObj {
	classvar keyConvert;

	*initClass {
		keyConvert = Dictionary[
			\c -> -3,
			\C -> 0,
			\cs -> 4,
			\Cs -> 7,
			\df -> -8,
			\Df -> -5,
			\d -> -1,
			\D -> 2,
			\ds -> 8,
			\e -> 1,
			\E -> 4,
			\f -> -4,
			\F -> -1,
			\fs -> 3,
			\Fs -> 6,
			\Gf -> -6,
			\g -> -2,
			\G -> 1,
			\gs -> 5,
			\Gs -> 8,
			\af -> -7,
			\Af -> -4,
			\a -> 0,
			\A -> 3,
			\bf -> -5,
			\Bf -> -2,
			\b -> 2,
			\B -> 5,
			\Cf -> -7
			]
		}
	}

GuidoScore : GuidoObj {
	// score will be an Dictionary of GuidoPart objects
	var <>score, file;
	*new {
		^super.new.init;
		}

	init {
		score = []; // Dictionary.new;
		}

	// add a voice to the score
	add {arg aVoice;
		aVoice.isKindOf(GuidoPart).if({
			//score = score.add(aVoice.id -> aVoice)
			score = score.add(aVoice);
			}, {
			"GuidoScore can only add GuidoParts".warn;
			})
		}

	remove {arg anID;
		(score.at(anID).notNil).if({
			score.removeAt(anID)}, {
			"No voice with that ID found".warn;
			})
		}

	output {arg pathname, mode = "w";
		var string, eventstring;
		file = File.new(pathname, mode);
		file.write("%% SuperCollider output from " ++ Date.localtime ++ "\n");
		file.write("%% Comments (%) after musical objects denote measure, beat (if supplied) \n");
		file.write("{\n");
		score.do({arg me, i;
			file.write("%%Voice" ++ i ++ "\n");
			me.output(file);
			(i != (score.size - 1)).if({file.write(",")});
			});
		file.write("}");
		file.close;
		}
	}

/*

each GuidoPart must have a unique id. More then one GuidoPart can exist on a staff (in other words, two GuidoParts with different id can share a staffid. To differentiate two lines on one staff, you may want to specify one \stemsUp and the other \stemsDown



key can be an integer. 0 is no sharps or flats, -2 is 2 flats, 3 is 3 sharps OR:
	\ef - E Flat minor - where lowercase indicates minot
	\Fs - F sharp major - where upper case indicates major
*/

GuidoPart : GuidoObj {
	var <>id, <instr, <>events, <>stemdir, <>staffid, <>clef, <>key, <>timeSig;
	// GuidoPart Objects will be an array Array of GuidoEvents
	*new {arg id, instr, events, clef, key, timeSig, stemdir = \stemsAuto, staffid;
		staffid = staffid ?? {id};
		events = events ? [];
		^super.newCopyArgs(id, instr, events, stemdir, staffid).initGuidoPart(clef, key, timeSig);
		}

	initGuidoPart {arg argClef, argKey, argTimeSig;
		timeSig = argTimeSig ?? {GuidoTimeSig(4, 4).measure_(1)};
		key = argKey ?? {GuidoKeySig(0).measure_(1)};
		clef = argClef ?? {GuidoClef(\g).measure_(1)};
		[timeSig, key, clef].do({arg me;
			events = events.addFirst(me);
			})
		}

	// add anEvent or an array of events
	add {arg ... anEvent;
		anEvent.flat.do({arg thisEv;
			thisEv.isKindOf(GuidoEvent).if({
				events = events.add(thisEv)
				}, {
				"It appears you are trying to add a non-GuidoEvent to a GuidoPart or GuidoPart".warn;
				});
			})
		}

	output {arg file;
		var string, eventstring, initMeter, currentMeter, currentMeasure, theseevents;
		file.write("[\n");
		file.write("\\staff<\""++staffid.asString++"\"> ");
		instr.notNil.if({file.write("\\instr<\""++instr.asString++"\"> ")});
		currentMeasure = 1;
		file.write("\\"++stemdir.asString++" \n");
		events.do{arg me; me.output(file)};
		file.write("]\n");
		}
	}

GuidoEvent : GuidoObj {
	var <>measure, <>beat;
	classvar rhythmToDur, timeToDots, timeToDur;

	output {arg file, beatComment = 0.0;
		var string, append;
		append = (measure.notNil or: {beat.notNil}).if({
			"\t%"++measure++" ,"++beat;
			}, {
			""
			});
		string = this.outputString ++ append ++"\n";
		file.write(string);
		//string.postln;
		}

	calcRhyDur {arg duration;
		duration.isKindOf(Symbol).if({
			duration = rhythmToDur[duration]
			});
		^(duration).asFraction(50, false);
		}

	*initClass {
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

// aPitchClass should be an instance of PitchClass or an integer keynum
GuidoNote : GuidoEvent {
	var <note, <>duration, <>marks, chord, <>tie;
	*new {arg aPitchClass = 60, duration = 0.25, marks;
		^super.new.initGuidoNote(aPitchClass, duration, marks.asArray);
		}

	initGuidoNote {arg argNote, argDur, argMarks;
		duration = argDur;
		marks = argMarks;
		chord = false;
		this.note_(argNote);
		}

	note_ {arg aPitchClass;
		aPitchClass.isKindOf(Array).if({
			chord = true
			});
		note = aPitchClass.asArray.collect({arg me;
			this.convertToPC(me);
			});
		}

	addDynamic {arg dynamic;
		marks = marks.add(GuidoDynamic(\i, dynamic))
		}

	addArticulation {arg articulation;
		marks = marks.add(GuidoArticulation(articulation))
		}

	convertToPC {arg aPitchClass;
		var rem;
		case
			{aPitchClass.isKindOf(Number)}
			{
			// check if there is an alteration... round to quarter-tones for now?
			rem = (aPitchClass % 1.0).round(0.5);
			aPitchClass = aPitchClass.trunc;
			^PitchClass(aPitchClass, alter: rem);
			}
			{aPitchClass == \r}
			{^PitchClass(\r)}
			{true}
			{^aPitchClass};
		}

	outputString {
		var string, markstring, articulation = 0, noteStr, rhyString;
		var rhythm = this.calcRhyDur(duration);
		rhyString = "*"++rhythm[0]++"/"++rhythm[1];
		markstring = "";
		marks.do({arg me; me.isKindOf(GuidoArticulation).if({articulation = articulation + 1})});
		(marks.size > 0).if({
			marks.do({arg me;
					markstring = markstring ++ me.outputString
				})
			});
		noteStr = "";
		note.do({arg me, i;
			(i > 0).if({
				noteStr = noteStr ++ ", ";
				});
			noteStr = noteStr ++ me.alt1 ++ me.guidoString ++ rhyString ++ me.alt2;
			});
		chord.if({
			noteStr = "{"++noteStr++"}";
			});

		string = markstring ++ noteStr;
		articulation.do({string = string ++" )"});
		tie.notNil.if({
			// tie can equal \start or \end
			(tie == \start).if({
				string = "\\tie(" ++ string;
				}, {
				string = string ++ ")";
				});
			});
		^"\t"++string;
		}
	}

// Use GuidoTimeSig to set up Time signatures. GuidoTimeSig is used internally for reading
// GuidoTimeSigs

GuidoTimeSig : GuidoEvent {
	var <>upper, <>lower;

	*new {arg upper, lower;
		^super.new.initGuidoTimeSig(upper, lower);
		}

	initGuidoTimeSig {arg argUpper, argLower;
		upper = argUpper;
		lower = argLower;
		}

	outputString {arg file;
		^"\t\\meter<\""++upper++"/"++lower++"\">";
		}
	}

GuidoKeySig : GuidoEvent {
	var key;

	*new {arg key;
		^super.new.initGuidoKeySig(key);
		}

	initGuidoKeySig {arg argKey;
		key = argKey.isKindOf(Integer).if({
			argKey
			}, {
			keyConvert[argKey]
			});
		}

	outputString {
		^"\t\\key<"++key++">"
		}
}

/*
clef is \g, \f or \c
optionally, you can specify a line to place the note indication of the clef on (1 bottom, 5 top)
\g2 is standard treble
\c3 is standard alto
\c4 is tenor

double clefs are allowed
\gg

and these are also valid: \treble, \bass, \alto and \tenor
*/

GuidoClef : GuidoEvent {
	var clef;

	*new {arg clef;
		^super.new.initGuidoClef(clef);
		}

	initGuidoClef	{arg argClef;
		clef = argClef;
		}

	outputString {
		^"\t\\clef<\""++clef.asString++"\">";
		}

}

// A Spanner is a collection of GuidoNotes that are linked through some aspect (for instance, all
// notes in a Spanner may be tied together). These act like chords, but in a horizontal sense
// Spanners will be used internally to create multiple notes out of durations that extend past
// a barline, and possibly for other markings. They will substitute for a GuidoNote call, and should
// be a collection of GuidoNotes (all of the same pitch class).

// arrGuidoNotes = array of GuidoNotes, already sorted in time
// the spanner symbol (e.g. \tie)

GuidoSpanner : GuidoEvent {
	var <>arrGuidoNotes, <>beat, <>spanner;

	*new {arg arrGuidoNotes, spanner;
		^super.newCopyArgs(arrGuidoNotes, spanner);
		}

	outputString {
		var string;
		string = "\\"++spanner.asString++"(\n";
		arrGuidoNotes.do({arg me;
			string = string ++ "\t"++ me.outputString;
			});
		string = string ++"\t)"
		^"\t"++string;
	}
}

GuidoMark {
	*new {
		^super.new;
		}
	}

/*
GuidoDynamic marks are:
\intens or \i
\crescBegin
\crescEnd
\dimBegin
\dimEnd

GuidoDynamic dynamics are:
"fff" .. "f"
"mf" and "mp"
"p" .. "ppp"

*/


GuidoDynamic : GuidoMark {
	var tag, dynamic;
	*new {arg tag, dynamic;
		([\intens, \i, \crescBegin, \crescEnd, \dimBegin, \dimEnd].indexOf(tag).notNil).if({
			^super.newCopyArgs(tag, dynamic).init;
			}, {
			"Tag not recognized as a GuidoDynamic".warn;
			^nil;
			})
		}

	init {
		dynamic = dynamic.isNil.if({
			" "
			}, {
			"<\""++dynamic++"\"> "
			});
		}

	outputString {
		^"\\"++tag.asString++dynamic;
		}
	}

/*
Tempo tags are
\tempo
\accelBegin
\accelEnd
\ritBegin
\ritEnd

tempoString is a direction i.e. "Moderato"
absTempoString is in the form of x/y=n i.e 1/4=120
*/

GuidoTempo : GuidoMark {
	var tag, tempoString, absTempoString, string;
	*new {arg tag, tempoString, absTempoString;
		([\tempo, \accelBegin, \accelEnd, \ritBegin, \ritEnd].indexOf(tag).notNil).if({
			^super.newCopyArgs(tag, tempoString, absTempoString).init;
			}, {
			"Tag not recognized as a GuidoTempo".warn;
			^nil;
			})
		}

	init {
		string = (tempoString.isNil && absTempoString.isNil).if({
				"\\"++ tag++" ";
				}, {
				(tempoString.notNil && absTempoString.notNil).if({
					"\\"++ tag++"<\"" ++ tempoString ++ "\", \"" ++ absTempoString ++ "\"> \n"
					}, {
					"\\"++ tag++"<\"" ++ tempoString ++ "\"> \n";
					})
				})
		}

	outputString {
		^string;
		}
	}

// articulations
/*

\stacc
\accent
\ten
\marcato
\trem
\grace
\alter for quarter-tones
for \trem and \grace, a note value can be passed in indicating that kind of note to draw:

e.g. 32 = 32nd notes

*/

GuidoArticulation : GuidoMark {
	var tag, val;
	*new {arg tag, val;
		([\stacc, \accent, \ten, \marcato, \trem, \grace, \alter].indexOf(tag).notNil).if({
			^super.newCopyArgs(tag, val);
			}, {
			"Tag not recognized as a GuidoArticulation".warn;
			^nil;
			})
		}

	outputString {
		([\trem, \grace, \alter].indexOf(tag).notNil && val.notNil).if({
			^"\\"++tag.asString++"<"++val++">( ";
			}, {
			^"\\"++tag.asString++"( ";
			})
		}
	}

GuidoArt {
	*new {arg tag, val;
		^GuidoArticulation.new(tag, val);
		}
	}

// maps out timesigs over measures: pass in an array of arrays:
// [ [measure, [beats, div]], [measure, [beats, div]] ]
// if a measure is left out, the previous measures timesig will be used
//GuidoTimeSig {
//	var <>timesigArray, <timesigDict;
//
//	*new {arg timesigArray;
//		^super.newCopyArgs(timesigArray).init;
//		}
//
//	init {
//		var measure, meter;
//		timesigDict = Dictionary.new;
//		timesigArray.do{arg me;
//			#measure, meter = me;
//			timesigDict.add(measure -> meter);
//			}
//		}
//
//	// create an array of meters for use in parsing voices... remember, measure one is at index 0!
//	fillMeters {arg numMeasures;
//		var curmeter, newmeter;
//		newmeter = this.timesigDict.at(1);
//		^Array.fill(numMeasures, {arg i;
//			curmeter = newmeter;
//			newmeter = this.timesigDict[i + 2] ? curmeter;
//			curmeter;
//			})
//		}
//
//	meterAt {arg measure = 1;
//		var measures;
//		measures = this.fillMeters(measure);
//		^measures.last;
//		}
//
//	numBeatsAt {arg measure = 1;
//		^this.meterAt(measure)[0]
//		}
//
//	divAt {arg measure = 1;
//		^this.meterAt(measure)[1]
//		}
//
//	// returns which measure a beat occurs in, and the beat in that measure.
//	// beats assumed to be quarter for now
//	measureFromBeat {arg beat;
//		var measure = 1, thisbeat = 1, meter;
//		meter = this.meterAt(measure);
//		thisbeat = thisbeat + ((meter[0] * 4) / meter[1]);
//		while({thisbeat <= beat}, {
//			measure = measure + 1;
//			meter = this.meterAt(measure);
//			thisbeat = thisbeat + ((meter[0] * 4) / meter[1]);
//			});
//		^[measure, (beat - thisbeat) % meter[0] + 1];
//		}
//	}
//
//GuidoTime {
//	var <>now, <>tollerance;
//
//	*new {arg curtime = 1.0, tollerance = 0.98;
//		^this.newCopyArgs(curtime, tollerance)
//		}
//
//	add {arg timeval;
//		var temp;
//		temp = now + timeval;
//		now = this.check(temp);
//		}
//
//	// this.check should be run everytime 'now' is updated
//	check {arg timeval;
//		^(((timeval % 1) >= tollerance).if({timeval.round}, {timeval}));
//		}
//
//	value {
//		^now;
//		}
//
//	+ {arg aVal;
//		aVal = (aVal.isKindOf(GuidoTime)).if({aVal.now}, {aVal});
//		^now + aVal;
//		}
//
//	- {arg aVal;
//		^now - aVal;
//		}
//
//	* {arg aVal;
//		^now * aVal;
//		}
//	/ {arg aVal;
//		^now / aVal
//		}
//
//	+= {arg aVal;
//		var temp;
//		temp = this.check(now + aVal);
//		now = temp;
//		^temp
//		}
//

//}

