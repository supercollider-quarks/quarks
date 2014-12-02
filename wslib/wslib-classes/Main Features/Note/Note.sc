Note {  // based on midiname.sc - part of wslib
	
	var <name, <cents, <midi, <freq;
	var <changeName; // the name will be enharmonic changed to the nearest whole or half note when this is true
	var <forceSign;	
	
	classvar defaultname = "C3";

	*new { |name, midi, freq, cents = 0|
		this.deprecated( thisMethod );
		#name, midi, freq, cents = Note.calculate(name, midi, freq, cents);
		^super.new.init(name, midi, freq, cents);
		}
		
	init { arg thisname, thismidi, thisfreq, thiscents;
		name = thisname; midi = thismidi; freq = thisfreq; cents = thiscents;
		changeName = false; forceSign = nil;
		}
	
	set { arg thisname, thismidi, thisfreq, thiscents;
		#name, midi, freq, cents = Note.calculate(thisname, thismidi, thisfreq, thiscents, changeName, forceSign);
		}
		
	*calculate { |name, midi, freq, cents, changeName, forceSign|
		changeName = changeName ? false; 
		forceSign = forceSign ? nil;
		case { name.notNil }
				{	if(changeName) {name = name.namename(cents, forceSign); };
					cents = {name.cents}.try ? cents;
					midi = name.namemidi(cents);
					freq = midi.midicps; }
			{midi.notNil}
				{ 	name = midi.midiname(forceSign);
				  	freq = midi.midicps; 
				  	cents = name.cents; } 
			{freq.notNil}
				{	name = freq.cpsname(forceSign);
					midi = freq.cpsmidi; 
					cents = name.cents; } 
			{ true }
				{ 	name = defaultname;
					midi = name.namemidi(cents);
					freq = midi.midicps; };
		^[name, midi, freq, cents];
		}
	
	

	cents_ { |inCents| this.set(name, nil, nil, inCents);
		}
	
	name_ { |inName| this.set(inName, nil, nil, cents);
		}	
	
	midi_ { |inMidi| // cents are recalculated too
			this.set(nil, inMidi, nil, nil); }

	freq_ { |inFreq| // cents are recalculated too
		this.set(nil, nil, inFreq, nil);
		}
	
	addMidi {  |aNumber = 0| this.midi = midi + aNumber; ^this; }
	addFreq { |aNumber = 0| this.freq = freq + aNumber; ^this; }
	addOct { |aNumber = 0| this.midi = midi + (aNumber * 12); ^this; } // can be float
	addRatio { |aNumber = 1| this.freq = freq * aNumber; ^this; } // add an interval ratio like 2/3 (fourth down)
	
	transpose { |aNumber = 0, type=\midi| 
		(	midi: {this.addMidi(aNumber) },
			freq: {this.addFreq(aNumber) },
			oct: {this.addOct(aNumber) },
			ratio: {this.addRatio(aNumber) } ).at(type).value;
		^this }
					
		
	
	forceSign_ { |inChar| forceSign = inChar; this.set(name, nil, nil, cents);  }
	changeName_ { |inBool| changeName = inBool; this.set(name, nil, nil, cents); }
	simplifyName { if (changeName.not) // change name once
		{ changeName = true; this.set(name, nil, nil, cents, true); changeName = false };
		^this; } 
	
	value { ^midi }
	value_ { |inMidi| midi = inMidi }
	
	// 'virtual' variables oct, note, alt and sign
	oct { ^name.getOctave; }
	note { ^name.getNote; } // name of note without alterations
	alt { ^name.getAlt; } // -1 = b, 1 = # etc.
	
	oct_ { |oct| this.set( 
			String.makeNoteName(name.getNote, name.getAlt, oct, cents), nil, nil, cents); }
			
	note_ { |note| this.set( 
			String.makeNoteName(note, name.getAlt, name.getOctave, cents), nil, nil, cents); }
			
	alt_ { |alt| this.set( 
			String.makeNoteName(name.getNote, alt, name.getOctave), nil, nil, cents); }
			
	sign { var altString = "", alt = this.alt;
		alt.abs.do({ altString = altString ++ 
			(if(alt.isPositive) {"#"} {"b"}); });	
		if(alt==2) {altString="x"};
		^altString;
		}
	
	sign_ { |sign| this.alt_(sign.asString.getAlt); }
	
	asNote { ^this }
	asString { ^name }
	asInteger { ^midi.round(1).asInteger; }
	asFloat { ^midi }
	asSymbol { ^name.asSymbol }
	
	printOn { arg stream;
		var title;
		stream << this.class.name;
		this.storeParamsOn(stream);
	}
	
	storeArgs { ^[name, midi, freq, cents] }
	
	// simple calculations are done on the midi value
	== { |aNote| ^(this.midi == aNote.asNote.midi.round(1e-8) ); } // close enough for jazz..
	+ { |aNote| ^Note(midi: aNote.asNote.midi + midi); }
	- { |aNote| ^Note(midi: midi - aNote.asNote.midi); }
	* { |aNumber| ^Note(midi: midi * aNumber); }
	/ { |aNumber| ^Note(midi: midi / aNumber); }
	min { |aNote| ^Note(midi: midi.min(aNote.asNote.midi) ); }
	max { |aNote| ^Note(midi: midi.max(aNote.asNote.midi) ); }
	wrap { arg lo = "C-2", hi = "G8"; ^Note(midi: midi.wrap(lo.asNote.midi, hi.asNote.midi) ) }
	clip { arg lo = "C-2", hi = "G8"; ^Note(midi: midi.clip(lo.asNote.midi, hi.asNote.midi) ) }
	rand { arg lo = "C-2", step = 1; lo = lo.asNote.midi; ^Note(midi: (midi - lo).rand.round(step) + lo); }
	*rand { arg lo = "C-2", hi = "G8", step = 1; ^hi.asNote.rand(lo, step); }
	round { |aNumber = 1| this.midi = midi.round(aNumber); ^this; }
	
	*makeScale { arg groundNote = "C3", type= 'major', startNote, endNote;
		^Array.makeScaleMidi(groundNote.asNote.midi, type, 
			{startNote.asNote.midi}.try, {endNote.asNote.midi}.try).asNote; 
		}
		
	makeScale { arg type= 'major', startNote, endNote;
		^Note.makeScale(midi, type, startNote, endNote) }
	
}

