/*
Paper size and margins:
// remove header (and Music engraved note at bottom)

\header {  tagline = ##f  composer = ##f}\paper {  paper-width = 7\cm  paper-height = 7\cm  top-margin = 0.1\cm  left-margin = 0.1\cm}
*/

LPObj {

	classvar <>lilypondPath = "/Applications/LilyPond.app/Contents/Resources/bin/lilypond";
	classvar <>lily2imagePath = "/usr/local/bin/lily2image";
	// these should all be Strings, numbers or booleans (OK for value), Scheme '#' and '#'' will be added for you
	overridePropString {arg layoutObj, layoutProp, value, context;
		var valueStr, contextStr;
		contextStr = context.notNil.if({context++"."}, {""});
		valueStr = this.getValueString(value);
		^"\\override "++contextStr++layoutObj++" #'"++layoutProp++" = "++valueStr;
		}
		
	overridePropOnceString {arg layoutObj, layoutProp, value, context;
		^"\\once " ++ this.overridePropString(layoutObj, layoutProp, value, context);
		}
	
	// should look like this... layoutSubPropArray is an array of values to reach sub property
	// ("Glissando", "bound-details", ["right", "arrow"], true, "Voice", 0)
	// should give \override Voice.Glissando #'bound-details #'right #'arrow = ##t
	overrideSubpropString {arg layoutObj, layoutProp, layoutSubPropArray, value, context;
		var valueStr, contextStr;
		valueStr = this.getValueString(value);
		contextStr = context.notNil.if({context++"."}, {""});
		layoutSubPropArray.do({arg me;
			layoutProp = layoutProp ++ " #'" ++ me;
			});
		^"\\override "++contextStr++layoutObj++" #'"++layoutProp++" = "++valueStr;
		}	

	overrideSubpropOnceString {arg layoutObj, layoutProp, layoutSubPropArray, value, context;
		^"\\once " ++ this.overrideSubpropString(layoutObj, layoutProp, layoutSubPropArray, 
			value, context);
		}	
				
	getValueString {arg value;
		case 
			{value.isKindOf(String)}
			{^"#'"++value}
			{value.isKindOf(SimpleNumber)}
			{^"#"++value.asString}
			{value == true}
			{^"##t"}
			{value == false}
			{^"##f"}
			{value.isKindOf(Symbol)}
			{^"#"++value}
			{true}
			{"It appears a strange value of "++value++" has been passed in.".warn;^""}
		}
		
	}
	
LPScore : LPObj {	
	var <>score, file, <header, <paper, <paperSize, <landscape, <layoutOverrides;
	// for spatial scores - minDuration is the duration cutoff for gliss's
	var <>spatial = false, <minDuration, <lyVars;
	var <baseMeasurementUnit, <includeMIDI;
		
	*new {arg title, composer, opus;
		^super.new.initLPScore;
		}
	
	initLPScore {
		var headers, papers;
		score = [];
		layoutOverrides = [];
		includeMIDI = false;
		lyVars = IdentityDictionary.new;
		paper = Dictionary.new;
		header = Dictionary.new;
		baseMeasurementUnit = "cm";
		// set an initial value
		this.paperSize_("letter", false);
		}
	
	checkStr {arg test;
		test.isKindOf(String).if({^true}, {"This parameter should be a String".warn; ^false});
		}
	
	// should be "cm" or "mm" or ???
	baseMeasuremntUnit_ {arg str; 
		this.checkStr(str).if({
			baseMeasurementUnit = str
			})
		}	
	// the following args should all be strings
	
	title_ {arg str; this.checkStr(str).if({header.add("title" -> str)}) }
	title {^header["title"]}
	subtitle_ {arg str; this.checkStr(str).if({header.add("subtitle" -> str)}) }
	subtitle {^header["subtitle"]}
	dedication_ {arg str; this.checkStr(str).if({header.add("dedication" -> str)}) }
	dedication {^header["dedication"]}
	poet_ {arg str; this.checkStr(str).if({header.add("poet" -> str)}) }
	poet {^header["poet"]}
	composer_ {arg str; this.checkStr(str).if({header.add("composer" -> str)}) }
	composer {^header["composer"]}
	meter_ {arg str; this.checkStr(str).if({header.add("meter" -> str)}) }
	meter {^header["meter"]}
	opus_ {arg str; 
		str.isKindOf(SimpleNumber).if({str = str.asString});
		 this.checkStr(str).if({header.add("opus" -> str)}) }
	opus {^header["opus"]}
	arranger_ {arg str; this.checkStr(str).if({header.add("arranger" -> str)}) }
	arranger {^header["arranger"]}
	instrument_ {arg str; this.checkStr(str).if({header.add("instrument" -> str)}) }
	instrument {^header["instrument"]}
	piece_ {arg str; this.checkStr(str).if({header.add("piece" -> str)}) }
	piece {^header["piece"]}
	copyright_ {arg str; 
		str.isKindOf(SimpleNumber).if({str = str.asString});
		this.checkStr(str).if({header.add(\copyright -> str)}) }
	copyright {^header["copyright"]}
	// can be a string or Boolean
	tagline_ {arg str; header.add("tagline" -> str) }
	tagline {^header["tagline"]}
	
	// size is one of a6, a5, a4, a3, legal, letter, 11x17, as a String
	paperSize_ {arg size, landscapeSetting = false;
		paperSize = size;
		landscape = landscapeSetting;
		}
	
	annotateSpacing_ {arg bool; paper.add("annotate-spacing" -> bool)}
	annotateSpacing {^paper["annotate-spacing"]}
	firstPageNumber_ {arg num; paper.add("first-page-number" -> num)}
	firstPageNumber {^paper["first-page-number"]}
	printFirstPageNumber_ {arg bool; 
		bool.isKindOf(Boolean).if({paper.add("print-first-page-number" -> bool)})}
	printFirstPageNumber {^paper["print-first-page-number"]}
	printPageNumbers_ {arg bool; 
		bool.isKindOf(Boolean).if({paper.add("print-page-number" -> bool)})}
	printPgaeNumbers {^paper["print-page-number"]}
	// size should be a string, with the measurement double \\
	// as in 4\\cm \cm, \mm and \in are OK
	paperWidth_ {arg size; paper.add("paper-width" -> size)}
	paperWidth {^paper["paper-width"]}
	paperHeight_ {arg size; paper.add("paper-height" -> size)}
	paperHeight {^paper["paper-height"]}
	topMargin_ {arg size; paper.add("top-margin" -> size)}
	topMargin {^paper["top-margin"]}
	bottomMargin_ {arg size; paper.add("bottom-margin" -> size)}
	bottomMargin {^paper["bottom-margin"]}
	leftMargin_ {arg size; paper.add("left-margin" -> size)}
	leftMargin {^paper["left-margin"]}
	lineWidth_ {arg size; paper.add("line-width" -> size)}
	lineWidth {^paper["line-width"]}	
	raggedRight_ {arg bool; paper.add("ragged-right" -> bool)}
	raggedRIght {^paper["ragged-right"]}
	raggedLast_ {arg bool; paper.add("ragged-last" -> bool)}
	raggedLast {^paper["ragged-last"]}
	raggedBottom_ {arg bool; paper.add("ragged-bottom" -> bool)}
	raggedBottom {^paper["ragged-bottom"]}
	raggedLastBottom_ {arg bool; paper.add("ragged-last-bottom" -> bool)}
	raggedLastBottom {^paper["ragged-last-bottom"]}
	systemCount_ {arg num; paper.add("system-count" -> num)}
	systemCount {^paper["system-count"]}
	headSeparation_ {arg size; paper.add("head-separation" -> size)}
	headSeparation {^paper["head-separation"]}
	footSeparation_ {arg size; paper.add("foot-separation" -> size)}
	footSeparation {^paper["foot-spearation"]}
	pageTopSpace_ {arg size; paper.add("page-top-space" -> size)}
	pageTopSpace {^paper["page-top-space"]}
	betweenSystemSpace_ {arg size; paper.add("between-system-space" -> size)}
	betweenSystemSpace {^paper["between-system-space"]}
	betweenSystemPadding_ {arg size; paper.add("between-system-padding" -> size)}
	betweenSystemPadding {^paper["between-system-padding"]}
	pageBreakingBetweenSystemPadding_ {arg size; 
		paper.add("page-breaking-between-system-padding" -> size)}
	pageBreakingBetweenSystemPadding {^paper["page-breaking-between-system-padding"]}
	horizontalShift_ {arg size; paper.add("horizontal-shift" -> size)}
	horizontalShift {^paper["horizontal-shift"]}
	afterTitleSpace_ {arg size; paper.add("after-title-space" -> size)}
	afterTitleSpace {^paper["after-title-space"]}
	beforeTitleSpace_ {arg size; paper.add("before-title-space" -> size)}
	beforeTitleSpace {^paper["before-title-space"]}
	betweenTitleSpace_ {arg size; paper.add("between-title-space" -> size)}
	betweenTitleSpace {^paper["between-title-space"]}
	printAllHeaders_ {arg bool; paper.add("printallheaders" -> bool)}
	printAllHeaders {^paper["printallheaders"]}
	systemSeparatorMarkup_ {arg markup; paper.add("systemSeparatorMarkup" -> markup)}
	systemSeparatorMarkup {^paper["systemSeparatorMarkup"]}
	blankPageForce_ {arg num; paper.add("blank-page-force" -> num)}
	blankPageForce {^paper["blank-page-force"]}
	blankLastPageForce_ {arg num; paper.add("blank-last-page-force" -> num)}
	blankLastPageForce {^paper["blank-last-page-force"]}
	pageSpacingWeight_ {arg num; paper.add("blank-last-page-force" -> num)}
	pageSpacingWeight {^paper["blank-last-page-force"]}
	autoFirstPageNumber_ {arg bool; paper.add("auto-first-page-number" -> bool)}
	autoFirstPageNumber {^paper["auto-first-page-number"]}
	
	// ly vars are added are available to the entire context when placed in the score. 
	// these are placed in an Identity Dictionary, and can be accessed later with
	// aScore.lyVars.at(key)
	// string should be a valid LilyPond expression. Tabs and new lines are added in magicaly :)
	// you MUST escape slashes.
	// e.g. if you want this in your lilypond file:
	// 	"useSpace = {\override SpacingSpanner #'uniform-stretching = ##t}
	// do this in SC (the key is the name of the lilyvar in this case):
	// aScore.addLyVar(\useSpace, 
	//	"useSpace = {\\override SpacingSpanner #'uniform-stretching = ##t});
	
//	// or: "useSpace = {"++this.overridePropString("SpacingSpanner", "uniform-stretching", true) ++ "}" 
	
	addLyVar {arg key, string;
		lyVars.add(key -> string);
		}
		
	addLayoutOverrides {arg context ... overrides;
		layoutOverrides = layoutOverrides.add([context, overrides]);
		}
		
	add {arg aLPPart;
		aLPPart.isKindOf(LPPart).if({
			score = score.add(aLPPart);
			}, {
			"LPScore can only add LPParts".warn;
			})
		}
		
	output {arg pathname, mode = "w";
		var string, eventstring, tmp;
		file = File.new(pathname, mode);
		file.write("%% SuperCollider output from " ++ Date.localtime ++ "\n");
		file.write("\n");
//		file.write("\\paper {\n");
//		// paper size is a bit special - it can be overwritten, and has two settings, so set it here... 
//		// then do the rest of the paper settings
//		file.write("}\n");
		file.write("\\include \"english.ly\"\n");
		(header.size > 0).if({
			file.write("\\header {\n");
			header.keysValuesDo({arg key, value, inc;
				case 
					{value.isKindOf(String)}
					{file.write("\t" ++ key ++ " = " ++ "\"" ++ value ++ "\"\n");}
					{value == false}
					{file.write("\t" ++ key ++ " = ##f\n");}
					{value == true}
					{file.write("\t" ++ key ++ " = ##t\n");}
					{true}
					{"Hmm... do you want something in this space".warn}
				});
			file.write("}\n");
			});
			
		/* layout settings */
		(layoutOverrides.size > 0).if({
			file.write("\\layout {\n");
			});
		layoutOverrides.do({arg thisLayoutOverrides;
			var context, overrides;
			#context, overrides = thisLayoutOverrides;
			file.write("\t\\context {\n");
			file.write("\t\t\\"++context++"\n");
			overrides.do({arg thisOverride;
				file.write("\t\t"++thisOverride++"\n");
				});
			file.write("\t\t}\n");
			});
		(layoutOverrides.size > 0).if({
			file.write("\t}\n")
			});	
					
		/* paper settings */

		((paper.size > 0)  or: {paperSize.notNil}).if({
			file.write("\\paper {\n");
			file.write("\t#(set-paper-size \""++paperSize++"\"" ++ landscape.if({" 'landscape)\n"}, {")\n"})); // '

			paper.keysValuesDo({arg key, value, inc;
				case
					{value.isKindOf(String)}
					{file.write("\t" ++ key ++ " = " ++ value ++ "\n");}
					{value == false}
					{file.write("\t" ++ key ++ " = ##f\n");}
					{value == true}
					{file.write("\t" ++ key ++ " = ##t\n");}
					{value.isKindOf(SimpleNumber)}
					{file.write("\t" ++ key ++ " = " ++ value ++ "\n")}
					{true}
					{"Hmm... do you want something in this space".warn}
				});
			file.write("}\n");
			});
		lyVars.do({arg me;
			file.write(me);
			file.write("\n");
			});
		score.do({arg part, i;
			file.write("%Voice" ++ i ++"\n");		
			part.output(file, this);
			});
		file.write("\\score {\n");
		file.write("\t<<\n");
		score.do({arg part, i;
			// score contains parts - each part gets an opening '<<' and closing '>>'
			file.write("\t\\new Staff = \""++ part.id.asString ++ "\" <<\n");
			part.partFunctions.do({arg me;
				file.write("\t\t"++me++"\n");
			});
			part.headOutput(file, this);
			part.voices.do({arg voice;
				file.write("\t\t\\"++voice.id++"\n");
				});
			file.write("\t\t>>\n");				
			});
		file.write("\t>>\n");
		includeMIDI.if({
			file.write("\t\\layout{}\n");
			file.write("\t\\midi{}\n");
		});		
		file.write("}\n");
		file.close;
		}

	render {arg basepath, format = \pdf, midiflag = false;
		midiflag.if({includeMIDI = true});
		this.output(basepath++".ly");
		(lilypondPath ++" --formats="++format++" -o "++basepath++". "++basepath++".ly")
			.unixCmd;
		}
				
	renderAndDisplay {arg basepath, format = \pdf, midiflag = false;
		midiflag.if({includeMIDI = true});
		this.output(basepath++".ly");
		(lilypondPath ++" --formats="++format++" -o "++basepath++". "++basepath++".ly")
			.unixCmdThen({("open "++basepath++"."++format).unixCmd})
		}
	
	renderCropAndDisplay {arg basepath, format = \png, midiflag = false;
		midiflag.if({includeMIDI = true});
		this.output(basepath++".ly");
		(lily2imagePath ++ " -f="++format++" "++basepath++". "++basepath++"")
			.unixCmdThen({("open "++basepath++"."++format).unixCmd})		}
			
	// only add gliss's to notes longer then 1/ 64
	useSpatial {arg minDur = 0.015625; 
		spatial = true;
		minDuration = minDur;
		this.addSpatialFuncs;
		}
	
	// some vars to print for spatial notation
	addSpatialFuncs {
		this.addLyVar(\useSpatialNotation, 
			"useSpatialNotation = {	\\override Score.SpacingSpanner #'uniform-stretching = ##t	\\override Score.SpacingSpanner #'strict-note-spacing = ##t
	\\set Score.proportionalNotationDuration = #(ly:make-moment 1 64)
	\\override Staff.Glissando #'breakable = ##t
	}");		this.addLyVar(\hideNote, 
			"hideNote = {\\once \\override NoteHead #'transparent = ##t	\\once \\override NoteHead #'no-ledgers = ##t	\\once \\override Stem #'transparent = ##t	\\once \\override Beam #'transparent = ##t
	\\once \\override Accidental #'transparent = ##t	}");		this.addLyVar(\timeNotation, 
			"timeNotation = {\\once \\override Stem #'transparent = ##t	\\once \\override Glissando #'thickness = 5	\\once \\override Beam #'transparent = ##t	}");		}
	}

// ach part represents a Staff - can contain many voices
LPPart : LPObj {
	var <voices, <>clef, <>timeSig, <>keySig, <>id, <>spatial = false;
	var showClef, showTimeSig, showKeySig, showBarLine, <>staffLines = 5;
	var <instrumentName, <shortInstrumentName, <partFunctions;
	
	*new {arg id, clef, timeSig, keySig, voice;
		^super.new.initLPPart(id, voice, clef, timeSig, keySig);
		}
		
	initLPPart {arg argID, argVoice, argClef, argTimeSig, argKeySig;
		id = argID;
		voices = [];
		partFunctions = [];
		argVoice.isNil.if({
			this.addVoice(LPVoice(id))
			}, {
			this.addVoice(argVoice)
			});
		argClef = argClef ?? {LPClef(\treble)};
		argTimeSig = argTimeSig ?? {LPTimeSig(4, 4)};
		argKeySig = argKeySig ?? {LPKeySig(\c, \major)};
		argClef.isKindOf(LPClef).if({
			clef = argClef;
			this.add(clef);
			}, {
			"clef must be an instance of LPClef".warn
			});
		argTimeSig.isKindOf(LPTimeSig).if({
			timeSig = argTimeSig;
			this.add(timeSig);
			}, {
			"timeSig must be an instance of LPTimeSig".warn;
			});
		argKeySig.isKindOf(LPKeySig).if({
			keySig = argKeySig;
			this.add(keySig);
			}, {
			"keySig must be an instance of LPKeySig".warn;
			});
		showClef = showTimeSig = showKeySig = showBarLine = true;
		}
	
	useSpatial {arg showTime = false, showBars = false; 
		this.showTimeSig_(showTime);
		this.showBarLine_(showBars);
		spatial = true
		}
	
	addFunction {arg functionString ... args;
		var str;
		str = "#("++functionString;
		args.do({arg me;
			str = str + "'"++me;
			});
		str = str +")";
		partFunctions = partFunctions.add(str);
		}
		
	showClef_ {arg bool = true; showClef = bool}
	showKeySig_ {arg bool = true; showKeySig = bool}
	showTimeSig_ {arg bool = true; showTimeSig = bool}
	showBarLine_ {arg bool = true; showBarLine = bool}
		
	setNumStaffLines {arg numLines; staffLines = numLines}
	
	addVoice {arg aVoice;
		voices = voices.add(aVoice);
		}
	
	addInstrumentName {arg aNameStr;
		instrumentName = aNameStr;
		}
		
	addShortInstrumentName {arg aNameStr;
		shortInstrumentName = aNameStr;
		}
		
	add {arg ... anEvent;
		this.addToVoice(0, *anEvent);
//		voices[0].add(anEvent);
		}
		
	addToVoice {arg voice ... events;
		voices[voice].add(*events);
		}

	output {arg file, score;
		voices.do({arg me;
			me.output(file, score)
			});
		}
		
	headOutput {arg file, score;
		instrumentName.notNil.if({
			file.write("\t\t\\set Staff.instrumentName =#\"" ++ instrumentName ++ "\"\n");
			});
		shortInstrumentName.notNil.if({
			file.write("\t\t\\set Staff.shortInstrumentName =#\"" ++ shortInstrumentName ++ "\"\n");
			});
		score.includeMIDI.if({
			file.write("\t\t\\set Staff.midiInstrument =#\"" ++ instrumentName ++ "\"\n");
			});
//		file.write("\t\t\\clef " ++ clef.type ++ "\n");
//		file.write("\t\t\\time " ++ timeSig.sig ++ "\n");
//		file.write("\t\t\\key " ++ keySig.sig ++ "\n");
		showClef.not.if({file.write("\t\t\\override Staff.Clef #'transparent = ##t\n")});
		showTimeSig.not.if({file.write("\t\t\\override Staff.TimeSignature #'transparent = ##t\n")});
		showKeySig.not.if({file.write("\t\t\\override Staff.KeySignature #'transparent = ##t\n")});
		showBarLine.not.if({file.write("\t\t\\override Staff.BarLine #'transparent = ##t\n")});
		(staffLines != 5).if({file.write("\t\t\\override Staff.StaffSymbol #'line-count = "++staffLines++"\n")});
		spatial.if({
			file.write("\t\t\\useSpatialNotation\n");
			});
		}
		
	addVoiceOverride {arg layoutObj, layoutProp, value, context;
		voices.do({arg me;
			me.overrides = me.overrides.add(this.overridePropString(layoutObj, layoutProp, value, context));
			})
		}

	}

// can I get rid of LPVoice? make it an array of array of notes in a voice?
LPVoice : LPObj {
	var <>id, <>voiceNum, <>notes, <>overrides;
	*new {arg id, voiceNum;
		^super.newCopyArgs(id).initLPVoice(voiceNum);
		}
		
	initLPVoice {arg argVoiceNum;
		// just to get going... only allow initial clef, timesig, etc.
		voiceNum = argVoiceNum.notNil.if({
			case {argVoiceNum == 1} {\voiceOne}
				{argVoiceNum == 2} {\voiceTwo}
				{argVoiceNum == 3} {\voiceThree}
				{argVoiceNum == 4} {\voiceFour}
				{true}{"voiceNum must be either 1, 2, 3 or 4".warn; nil};
			});
		notes = [];
		overrides = [];
		}
		
	output {arg file, score;
		var tuplet, tupletValue, tupletString;
		file.write(id.asString ++ " = \\new Voice = \"" ++ id.asString ++ "\" {\n");
		overrides.do({arg me;
			file.write("\t"+me++"\n")
			});
		voiceNum.notNil.if({
			file.write("\t\\" ++ voiceNum.asString ++ "\n");
			});
		notes.do({arg me;
			(tuplet != me.tuplet).if({
				tuplet.notNil.if({
					file.write("} ");
					tuplet = nil;
					}); // if there is already a tuplet, close it off!
				tuplet = me.tuplet;
				tuplet.notNil.if({
					tupletValue = tuplet.reciprocal.asFraction(50, false);
					file.write("\\times "++ tupletValue[0] ++ "/" ++ tupletValue[1] ++ " { ");
					});
				});
			me.output(file, score)
			});
		file.write("\n\t}\n");
		}
		
	add {arg ... anEvent;
		anEvent.flat.do({arg thisEvent;
			thisEvent.isKindOf(LPEvent).if({
				notes = notes.add(thisEvent);
				})
			});
		}
	
	// all should be formatted strings or numbers. # and #' are added for you
	// e.g this: addVoiceOverride("Beam", "auto-knee-gap" 25) =>
	//		\override Beam #'auto-knee-gap = #25
	addVoiceOverride {arg layoutObj, layoutProp, value, context;
			overrides = overrides.add(this.overridePropString(layoutObj, layoutProp, value, context));
		}
	}

// these can be (or, will be) any kind of event that happens during a voice part - 
// clef changes, time chagnes, barlines, etc.
LPEvent : LPObj {
	classvar rhythmToDur, timeToDur, timeToDots;
	var <note, <prependStrings, <appendStrings, <tuplet, <events;
	var <>beat, <>measure;
	
	*new {
		^super.new.initLPEvent;
		}
		
	initLPEvent {
		prependStrings = [];
		appendStrings = [];
		events = []
		}
	
	addPrependString {arg str;
		prependStrings = prependStrings.add(str);
		}
		
	addAppendString {arg str, first = false;
		appendStrings = first.if({
			appendStrings.addFirst(str);
			}, {
			appendStrings.add(str);	
			})
		}
	
	addPropOverride {arg layoutObj, layoutProp, value, context, once = true;
		once.if({
			events = events.add(this.overridePropOnceString(layoutObj, layoutProp, value, context));
			}, {	
			events = events.add(this.overridePropString(layoutObj, layoutProp, value, context));
			})
		}

	addSubpropOverride {arg layoutObj, layoutProp, layoutSubPropArray, value, context, once = true;
		once.if({
			events = events.add(this.overrideSubpropOnceString(layoutObj, layoutProp, 
				layoutSubPropArray, value, context))
			}, {
			events = events.add(this.overrideSubpropString(layoutObj, layoutProp, 
				layoutSubPropArray, value, context))
			})
		}
	
	setTimeSig {arg aLPTimeSig;
		events.add([\time, aLPTimeSig.sig]);
		}
	
	setClef {arg aLPClef;
		events.add([\clef, aLPClef.type])
		}
		
	setKeySig {arg aLPKeySig;
		events.add([\key, aLPKeySig.sig]);
		}
		
//	setTempo {arg beat, tempo, str;
//		var tempStr;
//		tempStr = str ?? {""};
//		tempStr = tempStr ++ (beat ?? {""});
//		(beat.notNil and: {tempo.notNil}).if({
//			tempStr = tempStr ++ " = ";
//			});
//		tempStr = tempStr ++ (tempo ?? {""});
//		tempStr = tempStr ++ beat ++ " = " ++ tempo;
//		events.add([\tempo, tempStr]);
//		}
		
	outputString {arg score;
		var output;
		output = "";
		events.do({arg me;
			output = output ++ "\t" ++ me ++ "\n";
			});
		^output;
		}
			
	output {arg file, score;
		var string;
		string = "";
		prependStrings.do({arg me;
			file.write("\t"++me++"\n");
			});
		file.write(this.outputString(score));
		appendStrings.do({arg me;
			file.write("\t"++me++"\n");
			});
		}

	
	rhythmToDot {arg aRhythm;
		^aRhythm.asString.size - 1;
		}
		
	// this should look for dots, etc. later
	checkDuration {arg aDuration;
		var dur, dot;
		case {
			aDuration.isKindOf(Symbol)
			} {
			dur = rhythmToDur[aDuration];
			dot = this.rhythmToDot(aDuration);
			} {
			aDuration.isKindOf(SimpleNumber)
			} {
			dur = timeToDur[timeToDur.indexInBetween(aDuration)];
			dot = timeToDots[aDuration.asFloat] ?? {0.0};
			} {
			true
			} {
			"A duration and number of dots couldn't be determined".warn;
			dur = aDuration;
//			dur = 0.25;
			dot = 0.0;
			};
		^[dur, dot]; //aDuration;
		}
	
	*initClass {
		// 
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
		timeToDur = [0.0078125, 0.015625, 0.03125, 0.0625, 0.125, 0.25, 0.5, 1.0, 2.0, 4.0];
		//IdentityDictionary[
//			0.25 -> 0.25,			
//			0.375 -> 0.25,			
//			0.4375 -> 0.25,			
//			0.125 -> 0.125,
//			0.1875 -> 0.125,
//			0.21875 -> 0.125,
//			0.0625 -> 0.0625,
//			0.09375 -> 0.0625,
//			0.109375 -> 0.0625,
//			0.03125 -> 0.03125,
//			0.046875 -> 0.03125,
//			0.0546875 -> 0.03125,
//			0.015625 -> 0.015625,
//			0.0234375 -> 0.015625,
//			0.02734375 -> 0.015625,
//			0.0078125 -> 0.0078125,
//			0.01171875 -> 0.0078125,
//			0.013671875 -> 0.0078125,
//			0.5 -> 0.5,
//			0.75 -> 0.5,
//			0.875 -> 0.5,
//			1.0 -> 1.0,
//			1.5 -> 1.0,
//			1.75 -> 1.0,
//			2.0 -> 2.0,
//			3.0 -> 2.0,
//			3.5 -> 2.0,
//			4.0 -> 4.0,
//			6.0 -> 4.0,
//			7.0 -> 4.0
//			];
		}

}
	
// make durations based on names / values of notes
// \q = quarter
// \qdd = double dotted quarter
// 0.25 = 1/4 = quarter
// 0.375 = 1/4 + 1/8 = \qd

// 0.25 becomes 4

// beat is in terms of the current TimeSIg

/* tweaking appearance:
place this before a note to hide a single stem
\once \override Stem #'transparent = ##t

Hide NoteHeads:
\override NoteHead #'transparent = ##t

Glissando thickness:
\once \override Glissando #'thickness = 5 

OH! a spatial notation! s is a spacer rest (invisible)
the transparent NoteHead cuts off the glissando

lowerLower = \new Voice = "lowerLower" {  \voiceTwo  \override Glissando #'thickness = 5   \override Stem #'transparent = ##t   \override Glissando #'bound-details #'right #'Y = #2  a4 \glissando s s \once \override NoteHead #'transparent = ##t a4 s s bf4 \glissando c4}

// more spatial stuff!

\header {  tagline = ##f  composer = ##f}\paper {  paper-width = 14\cm  paper-height = 7\cm  top-margin = 0.1\cm  left-margin = 0.1\cm}\include "english.ly"lowerLower = \new Voice = "lowerLower" {  \voiceTwo  \override Glissando #'thickness = 5   \override Stem #'transparent = ##t   \override Glissando #'bound-details #'right #'Y = #2  a4 \glissando s s \once \override NoteHead #'transparent = ##t a4 s s   \once   \override Glissando #'bound-details #'right #'Y = #2.5  bf4 \glissando   \once   \override Glissando #'bound-details #'right #'Y = #3  c'4 \glissando s s   bf8  a4 \glissando s s   \once \override NoteHead #'transparent = ##t a4}

if there are conficts:

  \once   \override Glissando #'bound-details #'right #'padding = #2.5   \score {	\new Staff = "bass" <<	  \override Staff.TimeSignature #'transparent = ##t	  \override Staff.BarLine #'transparent = ##t	  \clef bass	  \time 4/4	  \lowerLower	>>}
*/

// hmm... perhaps, if aPitchlass is an array, make it a chord?
// make rests LPNotes with aPitchClass of \r
LPNote : LPEvent {
	var <>duration, <>formatDur, <>dots, <>spatial = false;
	var <tremolo, <articulations, <dynamic, <dynamicChange, grace;
	
	*new {arg aPitchClass = 60, duration = 1.0, tuplet;
		^super.newCopyArgs(aPitchClass).initLPNote(duration, tuplet);
	}
	
	initLPNote {arg argDuration, argTuplet;
		this.note_(note);
//		dots = 0;
		// returns the needed symbol - this is converted later into a dur + dots
		#duration, dots = this.checkDuration(argDuration);
		[duration, dots].postln;
		formatDur = duration.reciprocal;
		argTuplet.notNil.if({tuplet = argTuplet});
		articulations = [];
		grace = false;
		}
	
	useSpatial { 
		spatial = true;
		this.addPrependString("\\timeNotation");
		}
		
	hideNotehead {
		this.addPrependString(
			this.overridePropOnceString("NoteHead", "transparent", true, "Voice")
			)
		}
		
	hideLedgers {
		this.addPrependString(
			this.overridePropOnceString("NoteHead", "no-ledgers", true, "Voice")
			)
		}
				
	hideStem {
		this.addPrependString(
			this.overridePropOnceString("Stem", "transparent", true, "Voice")
			)
		}

	hideBeam {
		this.addPrependString(
			this.overridePropOnceString("Beam", "transparent", true, "Voice")
			)
		}

	hideAccidental {
		this.addPrependString(
			this.overridePropOnceString("Accidental", "transparent", true, "Voice")
			)
		}
		
	hideNote {
		this.hideNotehead;
		this.hideLedgers;
		this.hideStem;
		this.hideBeam;
		this.hideAccidental;
		}
				
	// this needs to allow chords
	note_ {arg ... aPitchClass;
		aPitchClass = aPitchClass.flat;
		note = Array.newClear(aPitchClass.size);
		// then, fill note... need to parse note correclt later
		aPitchClass.do({arg thisPC, i;
			case 
				{thisPC.isKindOf(SimpleNumber)}
				{
				// check if there is an alteration... round to quarter-tones for now?
				note[i] = PitchClass(thisPC);
				}
				{thisPC.isKindOf(Symbol)}
				{note[i] = PitchClass(thisPC)}
				{true}
				{note[i] = thisPC};
			});
		}
	
	addTremolo {arg value;
		value = value.asInteger;
		[8, 16, 32, 64, 128].indexOf(value).notNil.if({
			tremolo = value;
			})
		}
	
	addGliss {arg style, thickness, rightArrow = false, leftArrow = false;
		style.notNil.if({
			this.addPrependString(
				this.overridePropOnceString("Glissando", "style", style, "Voice")
				)
			});
		thickness.notNil.if({
			this.addPrependString(
				this.overridePropOnceString("Glissando", "thickness", thickness, "Voice")
				)
			});
		leftArrow.if({
			this.addPrependString(
				this.overrideSubpropOnceString("Glissando", "bound-details", 
					["left", "arrow"], true, "Voice");
				)
			});
		rightArrow.if({
			this.addPrependString(
				this.overrideSubpropOnceString("Glissando", "bound-details", 
					["right", "arrow"], true, "Voice");
				)
			});
		this.addAppendString("\\glissando")
		}
	
	/* "accent", "marcato", "staccatissimo", "espressivo", "staccato", "tenuto", "portato", 
	"upbow", "downbow", "flageolet", "thumb", "lheel", "rheel", "ltoe", "rtoe", "open", "stopped", 
	"turn", "reverseturn", "trill", "prall", "mordent", "prallprall", "prallmordent", "upprall", 
	"downprall", "upmordent", "downmordent", "pralldown", "prallup", "lineprall", 
	"signumcongruentiae", "shortfermata", "fermata", "longfermata", "verylongfermata", "segno", 
	"coda", "varcoda" */
	
	addArticulation {arg articulation;
		articulations = articulations.add(articulation.asString);
		}

	addArticulationStrings {
		articulations.do({arg me;
			this.addAppendString("\\" ++ me)
			})	
		}
	/* This is a lot of methods to add... is it needed?	
	accent {this.addArticulation(\accent)}
	marcato {this.addArticulation(\marcato)}
	staccatissimo {this.addArticulation(\staccatissimo)}
	espressivo {this.addArticulation(\espressivo)}
	staccato {this.addArticulation(\staccato)}
	tenuto {this.addArticulation(\tenuto)}
	portato {this.addArticulation(\portato)}
	upbow {this.addArticulation(\upbow)}
	downbow {this.addArticulation(\downbow)}
	flageolet {this.addArticulation(\flageolet)}
	thumb {this.addArticulation(\thumb)}
	lheel {this.addArticulation(\lheel)}
	rheel {this.addArticulation(\rheel)}
	ltoe {this.addArticulation(\ltoe)}
	rtoe {this.addArticulation(\rtoe)}
	open {this.addArticulation(\open)}
	stopped {this.addArticulation(\stopped)}
	turn {this.addArticulation(\turn)}
	reverseturn {this.addArticulation(\reverseturn)}
	trill {this.addArticulation(\trill)}
	prall {this.addArticulation(\prall)}
	mordent {this.addArticulation(\mordent)}
	prallprall {this.addArticulation(\prallprall)}
	prallmordent {this.addArticulation(\prallmordent)}
	upprall {this.addArticulation(\upprall)}
	downprall {this.addArticulation(\downprall)}
	upmordent {this.addArticulation(\upmordent)}
	downmordent {this.addArticulation(\downmordent)}
	pralldown {this.addArticulation(\pralldown)}
	prallup {this.addArticulation(\prallup)}
	lineprall {this.addArticulation(\lineprall)}
	signumcongruentiae {this.addArticulation(\signumcongruentiae)}
	shortfermata {this.addArticulation(\shortfremata)}
	fermata {this.addArticulation(\fermata)}
	longfermata {this.addArticulation(\longfermata)}
	verylongfermata {this.addArticulation(\verylongfermata)}
	segno {this.addArticulation(\segno)}
	coda {this.addArticulation(\code)}
	varcoda {this.addArticulation(\varcoda)}
	*/
	// available dynamics are:
	// \ppppp, \pppp, \ppp, \pp, \p, \mp, \mf, \f, \ff, \fff, \ffff, \fp, \sf, \sff, \sp, \spp, \sfz, and \rfz
	
	addDynamic {arg dynamicMark, above = false;
		dynamic = [dynamicMark, above];
		}
		
	dynamicStr {
		var pos;
		dynamic.notNil.if({
			pos = dynamic[1].if({"^"}, {"_"});
			^pos ++ "\\" ++ dynamic[0]
			}, {
			^""
			})
		}
		
	startCresc {arg text = false, niente = false, above = false;
		above.if({
			this.addPrependString("\\dynamicUp");
			}, {
			this.addPrependString("\\dynamicDown");
			});
		text.if({
			this.addPrependString("\\crescTextCresc");
			}, {
			this.addPrependString("\\crescHairpin");
			niente.if({
				this.addPrependString(
					this.overridePropOnceString("Hairpin", "circled-tip", true)
					)
				})
			});
		this.addAppendString("\\<");
		}

	endCresc {arg dynamic, above = false;
		dynamic.notNil.if({
			this.addDynamic(dynamic, above)
			}, {
			this.addAppendString("\\!");
			})
		}
			
	startDim {arg text = false, niente = false, above = false;
		above.if({
			this.addPrependString("\\dynamicUp");
			}, {
			this.addPrependString("\\dynamicDown");
			});
		text.if({
			this.addPrependString("\\dimTextDeresc");
			}, {
			this.addPrependString("\\dimHairpin");
			niente.if({
				this.addPrependString(
					this.overridePropOnceString("Hairpin", "circled-tip", true)
					)
				})
			});
		this.addAppendString("\\>");
		}			

	endDim { arg dynamic, above = false;
		this.endCresc(dynamic, false)
		}

	startSlur {
		this.addAppendString("(")
		}
		
	endSlur {
		this.addAppendString(")")
		}
	
	startPhrasingSlur {
		this.addAppendString("\\(");
		}
		
	endPhrasingSlur {
		this.addAppendString("\\)");
		}
		
	addFall {arg distance = 2;
		this.addAppendString("-\\bendAfter #-" ++ distance);
		}

	addDoit {arg distance = 2;
		this.addAppendString("-\\bendAfter #+" ++ distance);
		}
				
	startBeam {this.addAppendString(" [ ")}
	endBeam {this.addAppendString(" ] ", grace)}
	
	startGrace {this.addPrependString(" \\grace {"); grace = true}
	stopGrace {this.addAppendString(" }"); grace = true}
	grace {this.startGrace; this.stopGrace}
	
	startAppoggiatura {this.addPrependString(" \\appoggiatura {"); grace = true}
	stopAppoggiatura {this.stopGrace}
	appoggiatura {this.startAppoggiatura; this.stopGrace}
	
	
	startAcciaccatura {this.addPrependString(" \\acciaccatura {"); grace = true}
	stopAcciaccatura {this.stopGrace}
	acciaccatura {this.startAcciaccatura; this.stopGrace}
	
	/* one of "default", "baroque", "neomensural", "mensural", "petrucci", "harmonic", 
	"harmonic-black", "harmonic-mixed", "diamond", "cross", "xcircle", "triangle", "slash"
	*/
	
	noteHead_ {arg noteHeadType;
		this.addPrependString(
			"\\once \\override Staff.NoteHead #'style = #'" ++ noteHeadType ++ " ");
		}
	
	/* same as above... just give choices in helpfile
	defaultNote {this.setNoteHead("default")}
	baroque {this.setNoteHead("baroque")}
	neomensural {this.setNoteHead("neomensural")}
	mensural {this.setNoteHead("mensural")}
	petrucci {this.setNoteHead("petrucci")}
	harmonic {this.setNoteHead("harmonic")}
	harmonicBack {this.setNoteHead("harmonic-black")}
	harmonicMixed {this.setNoteHead("harmonic-mixed")}
	diamond {this.setNoteHead("diamond")}
	cross {this.setNoteHead("cross")}
	xcircle {this.setNoteHead("xcircle")}
	triangle {this.setNoteHead("triangle")}
	slash {this.setNoteHead("slash")}
	*/
	
	stemUp {this.addPrependString("\\stemUp")}
	stemDown {this.addPrependString("\\stemDown")}

	changeStaff {arg part;
		part = part.isKindOf(LPPart).if({part.id});
		this.addPrependString("\\change Staff = \""++part++"\"\n");
		}
	
	outputString {arg score;
		var rem, minDur, str, dotStr, tmpDur, tremStr, noteStr;
		this.addArticulationStrings;
		dotStr = "";
		tremStr = tremolo.notNil.if({":"++tremolo}, {""});
		dots.do({dotStr = dotStr ++ ".";});
		spatial.if({
			minDur = score.minDuration;
			(duration > minDur).if({
				tmpDur = duration;
				dots.do({arg i; tmpDur = tmpDur + (duration / (i + 1 * 2))});
				rem = minDur.reciprocal / tmpDur.reciprocal;
				formatDur = minDur.reciprocal;
				// note[0] works... for now!. fix to work with chords
				this.addAppendString("\\glissando s"++formatDur++"*"++(rem-2)++" \\hideNote "++note[0].lilyString++formatDur);
				// remove the dotStr
				dotStr = "";
				})
			});
		noteStr = "";
		note.do({arg me;
			noteStr = noteStr ++ " " ++ me.lilyString;
			});
		(note.size > 1).if({noteStr = "<" ++ noteStr ++ ">"});
		^"\t"++noteStr++formatDur.asInteger ++ dotStr ++ this.dynamicStr ++ tremStr ++ " \n";
		}


	}
	
LPRest : LPEvent {
	var <>duration, <>formatDur, <>spatial = false, <>dots;
	*new {arg duration, tuplet;
		^super.new.initLPRest(duration, tuplet);
		}
		
	initLPRest {arg argDuration, argTuplet;
		dots = 0;
		// returns the needed duration for setting - adds dots to the 'dots' var
		#duration, dots = this.checkDuration(argDuration);
		formatDur = duration.reciprocal;
		argTuplet.notNil.if({tuplet = argTuplet});
		}
		
	outputString {arg score;
		var sym, dotStr;
		sym = spatial.if({"s"}, {"r"});
		dotStr = "";
		dots.do({dotStr = dotStr ++ ".";});
		^"\t"++sym++formatDur.asInteger ++ dotStr ++ " \n";
		}

	startBeam {this.addAppendString(" [ ")}
	endBeam {this.addAppendString(" ] ")}
		
	hide {spatial = true}
	useSpatial {spatial = true}

	}

/*	
LPClef : LPEvent {
	var type;
	
	*new {arg type;
		^super.newCopyArgs(type).initLPClef;
		}
	
	initLPClef { }
	
	type {
		^type.asString
		}
	}
	
LPTimeSig : LPEvent {
	var upper, lower;
	
	*new {arg upper, lower;
		^super.newCopyArgs(upper, lower).initLPTimeSig;
		}
		
	initLPTimeSig { }
	
	sig {
		^upper.asString ++ "/" ++lower.asString;
		}

	}

LPKeySig : LPEvent {
	var <>tonic, <>mode;
	
	*new {arg tonic = \c, mode = \major;
		^super.newCopyArgs(tonic, mode).initLPKeySig;
		}
		
	initLPKeySig { }
	
	sig {
		^tonic.asString ++ " \\"++mode;
		}
	}
*/
/*
LPChord : LPEvent {
	var <>duration, <>marks, <tuplet;
	*new {arg aPitchClassArray = 60, duration = 1.0, tuplet, marks;
		^super.newCopyArgs(aPitchClassArray.asArray).initLPChord(duration, tuplet, marks);
	}
	
	initLPChord {arg argDuration, argTuplet, argMarks;
		this.note_(note);
		// returns the needed symbol - this is converted later into a dur + dots
		duration = this.checkDuration(argDuration);
		argTuplet.notNil.if({tuplet = argTuplet});
		marks = argMarks.asArray ?? {[]};
		}
		
	note_ {arg aPitchClass;
		var rem;
		aPitchClass.do({arg thisPC, i;
			thisPC.isKindOf(Number).if({
				// check if there is an alteration... round to quarter-tones for now?
				rem = (aPitchClass % 1.0).round(0.5);
				aPitchClass = aPitchClass.trunc;
				note[i] = PitchClass(thisPC, alter: rem);
				}, {
				note[i] = aPitchClass
				});
			})
		}
		
	outputString {arg score;
		var noteString;
		noteString = "< ";
		note.do({arg me;
			noteString = noteString ++ me.lilyString ++ " ";
			});
		^noteString++">"++duration.reciprocal.asInteger ++ " ";
		}

	}
*/
/* These layout objects are MOSTLY for entering overrides as LPEvents. Mostly formats settings */
/*
LPLayoutObjects {
	classvar <>layoutObjects;
	
	*initClass {
		layoutObjects = Dictionary.new;
		}
	}
*/

LPClef : LPEvent {
	var type;
	
	*new {arg type;
		^super.new.initLPClef(type);
		}
	
	initLPClef {arg argType;
		type = argType;
		}
	
	outputString {
		^"\t\t\\clef " ++ type.asString ++ "\n"
		}
	}
	
LPTimeSig : LPEvent {
	var upper, lower;
	
	*new {arg upper, lower;
		^super.new.initLPTimeSig(upper, lower);
		}
		
	initLPTimeSig {arg argUpper, argLower;
		upper = argUpper;
		lower = argLower;
	 	}
	
	outputString {
		^"\t\t\\time " ++ upper.asString ++ "/" ++lower.asString ++ "\n";
		}

	}

LPKeySig : LPEvent {
	var <>tonic, <>mode;
	
	*new {arg tonic = \c, mode = \major;
		^super.new.initLPKeySig(tonic, mode);
		}
		
	initLPKeySig {arg argTonic, argMode;
		tonic = argTonic;
		mode = argMode;
		}
	
	outputString {
		^"\t\t\\key " ++ tonic.asString ++ " \\"++mode ++ "\n";
		}
	}
	
LPTempo : LPEvent {
	var <>beat, <>tempo, <>string;
	
	*new {arg beat, tempo, string;
		^super.new.initLPTempo(beat, tempo, string);
		}
		
	initLPTempo {arg ... args;
		#beat, tempo, string = args;
		}
		
	outputString {
		var tempoStr;
		tempoStr = string.notNil.if({
			"\""++string++"\""
			}, {
			""
			});
//		events.do({arg me;
//			tempoStr = tempoStr ++ "\t" ++ me ++ "\n";
//			});
		tempoStr = "\\tempo " ++ tempoStr;
		(beat.notNil and: {tempo.notNil}).if({
			tempoStr = tempoStr ++ " "++beat++" = "++tempo;
			});
		^"\t\t " ++ tempoStr ++ "\n";
		}
		
	}
	
//LPAccidental { }
//LPAccidentalCautionary { }
//LPAccidentalSuggestion { }
//LPAmbitus { }
//LPAmbitusAccidental { }
//LPAmbitusLine { }
//LPAmbitusNotehead { }
