/*
wslib 2005 v#1.0
midi to name toolkit for SC3

notenumber to notename and backwards conversion
also includes convenience methods for frequency (cps)

midivoicetype can be used to test if a certain note 
or frequency is included in a certain voice type

voice types are currently in english and dutch

note names are as found in Logic Pro. Sibelius and some other brands
use other:  C4 in Logic = C3 in Sibelius

methods included:

SimpleNumber-midiname
	convert a midi notenumber to a notename
	adds a 'cents' method
SimpleNumber-midivoicetype
	get the voicetypes who can sing this midi notenumber
SimpleNumber-cpsname
	convert frequency to a notename
SimpleNumber-cpsvoicetype
	get the voicetypes who can sing this frequency
String-namemidi
	convert a notename to a midi notenumber
String-namename
	convert a notename to a standardized notename
String-namecps
	convert a notename to a frequency
String-namevoicetype
	get the voicetypes who can sing this notename
Array-makeScale
	make a scale from start- to endnote based on groundnote
	including a number of tuning tables and combinations
	notenames are always enharmonically transposed to the closest one

Convenience methods for Symbol and Array classes were added.

W. Snoei 2005

*/

+ SimpleNumber {
	midiname { arg sign;
		// format "[notename][b/#][octave]"
		// arg sign can be String, Symbol or Char containing # or b
		// # forces altered notes to # sign
		// b forces altered notes to b sign
		// nil or n chooses most common alterations (F#, C#, G#, Bb, Eb)
		// examples: "C#-2" , "Gb7"
		// adds 'cents' method for deviation in cents
		var out;
		if(sign.isNil) {sign = $n};
		if(sign.class == Symbol) {sign = sign.asString};
		if(sign.class == String) {sign = sign[0]};
		out = IdentityDictionary[
		$# -> ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"],
		$b -> ["C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"],
		$n -> ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"]
		].at(sign)[this.round(1.0) % 12] ++ ((this.round(1.0) / 12).floor - 2).asInt;
		out.addUniqueMethod('cents', { ((this - this.round(1.0)) * 100).round(10e-8) });
		out.addUniqueMethod('alt', { IdentityDictionary[$# -> 1, $b -> -1].at(out[1]) ? 0 })
		//out.cents_( ((this - this.round(1.0)) * 100).round(10e-8)  );
		^out;
		}
		
	midivoicetype { arg type, language = 'en', filter; //type: testing / filter: 'm' or 'f'
		// midi nn -> type of voice
		// outputs an array of voicetypes
		// +/- are added behind voicetype if the note
		// is in the outer regions of the voicetype
		var voiceDict, out;
		
		voiceDict = IdentityDictionary[
		
		'nl' -> IdentityDictionary[ //min -, min, max, max +, male/female
			'bas' 		-> ["D1","E1","E3","G3", 'm'],
			'bariton' 	-> ["F1","G1","G3","B3", 'm'],
			'tenor' 		-> ["A1","B1","B3","D4", 'm'],
			'alt' 		-> ["E2","F2","F4","A4", 'f'],
			'mezzosopraan' -> ["G2","A2","A4","C5", 'f'],
			'sopraan' 	-> ["B2","C3","C5","E5", 'f']
			],
			
		'en' -> IdentityDictionary[ //min -, min, max, max +, male/female
			'bass' 		-> ["D1","E1","E3","G3", 'm'],
			'baritone' 	-> ["F1","G1","G3","B3", 'm'],
			'tenor' 		-> ["A1","B1","B3","D4", 'm'],
			'contralto' 		-> ["E2","F2","F4","A4", 'f'],
			'mezzo-soprano' -> ["G2","A2","A4","C5", 'f'],
			'soprano' 	-> ["B2","C3","C5","E5", 'f']
			]
		][language];
				
		out = [];
		filter = filter ? ['m','f'];
		filter = [filter].flat;
			
		if(type.isNil)
			{
			voiceDict.keys.do({
				|item|
				var reach;
				reach = voiceDict.at(item);
				if( reach.includesAny(filter) )
				{
				if(	(this >= reach.at(0).namemidi) && 
					(this < reach.at(1).namemidi)  )
					{out = out.add(item.asString ++ " -") };
				if(	(this >= reach.at(1).namemidi) && 
					(this <= reach.at(2).namemidi)  )
					{out = out.add(item.asString) };
				if(	(this > reach.at(2).namemidi) && 
					(this <= reach.at(3).namemidi)  )
					{out = out.add(item.asString ++ " +") };
				};
				});
			} {
			out = false;
			type = type.asSymbol;
			if(	(this >= voiceDict.at(type).at(0).namemidi) && 
				(this < voiceDict.at(type).at(1).namemidi)  )
				{out = "-"; };
			if(	(this >= voiceDict.at(type).at(1).namemidi) && 
				(this <= voiceDict.at(type).at(2).namemidi)  )
				{out = true; };
			if(	(this > voiceDict.at(type).at(2).namemidi) && 
				(this <= voiceDict.at(type).at(3).namemidi)  )
				{out = "+"; };
			}
		^out;
		}
	
	makeScale {arg type = 'major', startNote, endNote; 
		// startnote and endnote are note names
		// input is midi note number
		// output is midi note numbers
		^Array.makeScaleMidi(this, type, 
			{startNote.namemidi}.try, {endNote.namemidi}.try);
		}
		
	makeScaleMidi {arg type = 'major', startNote, endNote;
		// all args/input are midi note number
		^Array.makeScaleMidi(this, type, startNote, endNote);
		}
	
	makeScaleCps {arg type = 'major', startNote, endNote;
		// all args/input are midi note number
		^Array.makeScaleCps(this, type, startNote, endNote);
		}
	
	makeScaleName {arg type = 'major', startNote, endNote;
		// startnote and endnote are note names
		// input is midi note number
		// output is note names
		^Array.makeScaleName(this.midiname, type, startNote, endNote);
		}
		
	cpsname { arg sign;
		^this.cpsmidi.midiname(sign);
		}
		
	cpsvoicetype {
		^this.cpsmidi.midivoicetype;
		}
	cpstransp { arg trans;
		^(this.cpsmidi + trans).midicps;
		}
	}

/*
+ Nil {
	midiname {^this}
	namemidi {^this}
	namename {^this}
	cpsname {^this}
	namecps {^this}
	cpsmidi {^this}
	midicps {^this}
	cpsmidi {^this}
	midiratio {^this}
	ratiomidi {^this}
	}
*/

+ String {
	//var <>cents; -- won't work, why?
	
	*notesDict { ^IdentityDictionary[ $C -> 0, $D -> 2, $E -> 4, $F -> 5, $G -> 7, $A -> 9, $B -> 11 ]; }
	notesDict { ^String.notesDict; }
	
	namemidi { arg cents; 	
		// format "[notename][bb/b/#/x][octave(-9/9)]"
		// examples: "C#-2" , "Gbb7"
		// min notenr = -84 (C-9)
		// max notenr = 145 (Bx9)
		var notename, addition, octave = 0;
		if(cents.isNil)
			{ cents = {this.cents}.try ? 0 }; //override when available as method of input string
		^(this.notesDict.at(this.getNote) + 24
			 + this.getAlt + (this.getOctave * 12) + (cents * 0.01) );
		}
	
	getAlt {
		var addition = 0;
		if ({this.alt}.try.isNil) {
			this.do({ |item|
			if (item == $#) {addition = addition + 1};
			if (item == $b) {addition = addition - 1};
			if (item == $x) {addition = addition + 2};
				});
			this.addUniqueMethod('alt', {addition}) };
		^this.alt;
		}
	
	getOctave {
		//var octave = 0;
		//octave = 
		^this.extractNumbers[0] ? 3;
		//.asString;
		//if(this.reverse[1] == $-) {octave = "-" ++ octave};
		//^octave.interpret;
		}
		
	getNote { ^this[0].toUpper; }
	
	*makeNoteName {arg name = $C, alt = 0, octave = 3, cents = 0, maxAlt=3;
		var out, altString = "";
		
		if(name.isNumber) {name = name.midiname.first};
		if(name.class != Char) {name = name.asString.first};
		
		cents = cents + ((alt.excess(maxAlt)) * 100);
		alt = alt.clip2(maxAlt);
		alt.abs.do({ altString = altString ++ 
			(if(alt.isPositive) {"#"} {"b"}); });
		if(alt==2) {altString="x"};
		out = name.asString ++ altString ++ octave.round(1).asInt;
		out.addUniqueMethod('cents', { cents });
		out.addUniqueMethod('alt', { alt });
		^out;
	}
	
	
	forceNoteName {arg toName = $C, maxAlt=12;
		var newAlt, inNoteNr, toNoteNr, diff, octave;
		if(toName.isNumber) {toName = toName.midiname.first};
		if(toName.class != Char) {toName = toName.asString.first};
		inNoteNr = this.notesDict[this.getNote];
		diff = (inNoteNr - this.notesDict[toName];).wrap(-5, 6);
		octave = this.getOctave;
		
		case { (diff.neg + inNoteNr) > 11 }
				{octave = octave + 1}
			{ (diff.neg + inNoteNr) < 0 }
				{octave = octave - 1};
		
		newAlt = this.getAlt + diff;
		^String.makeNoteName(toName, newAlt, octave,  {this.cents}.try ? 0, maxAlt:maxAlt)
		}
	
	/*
	
	forceSign {arg toSign = "", maxAlt=12; // will force to sign if possible, or else one less
		// NOT FINISHED YET!!
		var inAlt, toAlt, toAltIn, inNote, inNoteNr, outName, nearestNext;
		toAltIn = toSign.getAlt;
		toAlt = [toAltIn.excess(-1), toAltIn.excess(1)].sort;
		inNote = this.namename;
		inAlt = inNote.getAlt;
		^if(toAltIn !== 0) {
			inNoteNr = this.namename.notesDict[this.getNote] + inAlt;
			if(toAltIn.isPositive)
				{nearestNext = this.notesDict.values.sort.select({ |item| 
					(item >= (inNoteNr - toAltIn).wrap(0,11)) && (item <= (inNoteNr - (toAltIn - 1)).wrap(0,11)); });
				if(nearestNext.first.isNil)
					{nearestNext = [inNoteNr]
				outName = this.notesDict.findKeyForValue(nearestNext.first) }
			
				{nearestNext = this.notesDict.values.sort.select({ |item| 
					(item >= (inNoteNr - (toAltIn + 1)).wrap(0,11)) && (item <= (inNoteNr - toAltIn).wrap(0,11)); });
				nearestNext.postln;
				outName = this.notesDict.findKeyForValue(nearestNext.last) };
			
			inNote.forceNoteName(outName, maxAlt);
			} { inNote };
		}
	*/
	
	makeScale {arg type = 'major', startNote, endNote;
		^Array.makeScale(this, type, startNote, endNote);
		}
	
	makeScaleName {arg type = 'major', startNote, endNote;
		^Array.makeScaleName(this, type, startNote, endNote);
		}

	makeScaleCps {arg type = 'major', startNote, endNote;
		^Array.makeScaleCps(this.namecps, type, startNote, endNote);
		}
	
	makeScaleMidi {arg type = 'major', startNote, endNote;
		^Array.makeScaleMidi(this.namemidi, type, startNote, endNote);
		}
	
	namename {arg cents, sign;
		// convert to standardized format and add 'cents' method
		// with enharmonic transposition
		^this.namemidi(cents).midiname(sign);
		}
		
	namecps { arg cents;
		^this.namemidi(cents).midicps;
		}
		
	// testing
	testnamemidi { arg ifTrue=true, ifFalse=false, cents = 0;
		// if ifTrue and ifFalse are functions they will
		// be called with this and cents as arguments
		// and the result returned (a.k.a if statement included)
		^if({this.namemidi}.try.notNil)
			{ifTrue.value(this, cents)}
			{ifFalse.value(this, cents)};
		}
	
	// try and always return default if fails
	trynamemidi { arg cents, default = "C3";
		^({this.namemidi(cents)}.try ? {default.namemidi(cents)}.try) ? default;
		}
	
	trynamename {arg cents, sign, default;
		^this.testnamemidi(_.namename(_, sign), default ? this, cents);
		}
		
	trynamecps { arg cents, default = "C3";
		^this.testnamemidi(_.namecps(_), {default.trynamecps(cents)}, cents);
		}
	
	
	namevoicetype {arg type, language='en', filter;
		^this.namemidi.midivoicetype(type, language, filter)
		}
		
	nametransp { arg trans;
		^(this.namemidi + trans).midiname;
		}
	}
	
+ Symbol {
	namemidi { arg cents;
		^this.asString.namemidi(cents);
		}
	
	makeScale {arg type = 'major', startNote, endNote;
		^Array.makeScale(this, type, startNote, endNote);
		}
		
	makeScaleName {arg type = 'major', startNote, endNote;
		^Array.makeScaleName(this, type, startNote, endNote);
		}	
		
	makeScaleCps {arg type = 'major', startNote, endNote;
		^Array.makeScaleCps(this.namecps, type, startNote, endNote);
		}
	
	makeScaleMidi {arg type = 'major', startNote, endNote;
		^Array.makeScaleMidi(this.namemidi, type, startNote, endNote);
		}
		
	namename {arg cents, sign;
		^this.namemidi(cents).midiname(sign);
		}
			
	namecps { arg cents;
		^this.namemidi(cents).midicps;
		}
		
	testnamemidi { arg ifTrue=true, ifFalse=false, cents = 0;
		^if({this.namemidi}.try.notNil)
			{ifTrue.value(this, cents)}
			{ifFalse.value(this, cents)};
		}

	trynamemidi { arg cents, default = 'C3';
		^this.asString.trynamemidi(cents, default);
		}
	
	trynamename {arg cents, sign, default;
		^this.testnamemidi(_.namename(_, sign), default ? this, cents);
		}
		
	trynamecps { arg cents, default = 'C3';
		^this.testnamemidi(_.namecps(_), {default.trynamecps(cents)}, cents);
		}
		
	namevoicetype {arg type, language='en', filter;
		^this.namemidi.midivoicetype(type, language, filter)
		}
	}
	
+ Array {
		
	*makeScaleMidi { arg groundNote = 60, type= 'major', startNote, endNote;
		var outArray = [0];
		var finalScale = [], i=0;
		var scale = (
			//scales:
			'major': [0,2,4,5,7,9,11],
			'minor': [0,2,3,5,7,8,10],
			'harm-minor': [0,2,3,5,7,8,11],
			'melo-minor': [0,2,3,5,7,9,11], //only up, use 'minor' for down
			'blues': [0,3,5,6,7,10],
			'blues-major': [0,2,3,4,7,9],
			'pentatonic': [0,2,4,6,8,10],
			'chromatic': (0,1..11),
			'quartertones': (0,0.5..11.5),
			
			//tuning tables: 
			'just':// 7-limit tritone basic
				[1, 16/15, 9/8, 6/5, 5/4, 4/3, 7/5, 3/2, 8/5, 5/3, 9/5, 15/8].ratiomidi, 
			'fifth': // based on pure fifths from goundnote
				Array.fill(12, {|i| ((3/2)**(i-6)).ratiomidi.wrap(0, 12) }).sort,
			
			//tuning tables from Apple Logic Pro:
			'pythagorean': [ 0, 2187/2048, 9/8, 32/27, 81/64, 4/3, 729/512, 3/2, 
				6561/4096, 27/16, 16/9, 243/128].ratiomidi,
			'werckmeister': //Andreas Werckmeister III (1681), the most famous one
				([0, 1.9218, 3.90225, 6.9609, 8.8827, 10.9218] ++
					[256/243, 32/27, 4/3, 1024/729, 128/81, 16/9 ].ratiomidi).sort,
			'indian': //North Indian Gamut, modern Hindustani Gamut out of 22 or more Shrutis 
				[1, 16/15, 9/8, 6/5, 5/4, 4/3, 45/32, 3/2, 8/5, 27/16, 9/5, 15/8].ratiomidi,
			'arabic': // empirical..
				[ 0, 1.3, 1.8, 2.5, 3.55, 5.02, 6.23, 7.06, 7.86, 8.57, 9.3, 11.1],
			
			// tuned scales:
			'just-major': [1, 9/8, 5/4, 4/3, 3/2, 5/3, 15/8].ratiomidi,
			'just-minor': [1, 9/8, 6/5, 4/3, 3/2, 8/5, 9/5].ratiomidi,
			'fifth-major': [1, 9/8, 81/64, 4/3, 3/2, 27/16, 243/128].ratiomidi,
			'fifth-minor': [1, 9/8, 32/27, 4/3, 3/2, 128/81, 16/9].ratiomidi
				
			).at(type.asSymbol);
		
		if(scale.isNil)
			{if(type.isArray)
				{scale = type}
				{("Array-makeScale: input 'type' not valid: " ++ type).warn}
			};
			
		startNote = startNote ? groundNote;
		endNote = endNote ? (startNote + 11);
		i = ((startNote - groundNote) * (scale.size/12)).floor;
		
		// recursive calculation
		while {outArray.last < endNote}
		{outArray = outArray.add(groundNote + scale.wrapAt(i) + ((i/scale.size).floor * 12)); 
			i=i+1};
			
		// corrections:
		/* if(outArray.last > endNote)
			{outArray.pop}; 
			
		if(outArray[1] < startNote)
			{outArray.removeAt(1);}; */
			
		^outArray.select({ |item| (item >= (startNote -0.5)) && (item <= (endNote + 0.5)) });
		
		}
		
	*makeScale { arg groundNote = "C3", type= 'major', startNote, endNote;
		^Array.makeScaleMidi(groundNote.namemidi, type, 
			{startNote.namemidi}.try, {endNote.namemidi}.try).midiname; 
		}
		
	*makeScaleName { arg groundNote = "C3", type= 'major', startNote, endNote;
		// same as above - for convenience and clarity if needed
		^Array.makeScaleMidi(groundNote.namemidi, type, 
			{startNote.namemidi}.try, {endNote.namemidi}.try).midiname; 
		}
			
	*makeScaleCps { arg groundNote = 261.62556530114, type= 'major', startNote, endNote;
		^Array.makeScaleMidi(groundNote.cpsmidi, type, 
			{startNote.cpsmidi}.try, {endNote.cpsmidi}.try).midicps; 
		}
	
	*fillNoteNames { arg startNote = "C3", endNote = "B3", step=1;
		// equal to Array.makeScale(type:'chromatic') but probably faster..
		var startNoteMidi = startNote.namemidi;
		^(startNoteMidi, (startNoteMidi + step) .. endNote.namemidi).midiname;
		}
	
	namemidi { arg cents;
		^this.collect(_.namemidi(cents)); 
		}
	namecps { arg cents;
		^this.collect(_.namecps(cents)); 
		}
	namename {arg cents, sign;
		^this.collect(_.namemidi(cents).midiname(sign));
		}
	midiname { arg sign;
		^this.collect(_.midiname(sign));
		}
	cpsname { arg sign;
		^this.collect(_.cpsname(sign));
		}
	}


	