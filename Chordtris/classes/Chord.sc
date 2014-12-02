Chord {
	
	// base key of the chord
	var <>baseKey;
	
	// kind (can be \minor or \major)
	var <>kind;
	
	// optional notes
	var <>notes;
	
	// chord language for displaying the chord instance
	var <>chordLanguage;
	
	*new { |baseKey=0, kind=\major, notes| ^super.newCopyArgs(baseKey, kind, notes) }
	
	printOn { |stream|
			if(chordLanguage.notNil && (chordLanguage.asSymbol == \Deutsch)) {
				stream << ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "H"].wrapAt(baseKey);
			} {
				stream << ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"].wrapAt(baseKey);
			};
			
			if(kind == \minor) { stream << "m"};
		}
	
	// returns a set of MIDI notes this chord consists of
	noteSet {
		var noteSet = Set.new;
		// add base key
		noteSet.add(baseKey);
		
		// add third
		if(kind == \major) { noteSet.add((baseKey+4)%12) } { noteSet.add((baseKey+3)%12) };
		
		// add fifth
		noteSet.add((baseKey+7)%12);
		
		// add optional notes
		notes.do { |note| noteSet.add((baseKey+note)%12) };
		^noteSet;
	}
	
}