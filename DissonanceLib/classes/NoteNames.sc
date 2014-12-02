NoteNames {

// This code is taken from a post by James McCartney:
classvar <semitones, <naturalNoteNames, <table, <names, <flatnames;

	*initClass {
		names = #['C','C#','D','D#','E','F','F#','G','G#','A','A#','B'];
		flatnames = #['C','Db','D','Eb','E','F','Gb','G','Ab','A','Bb','B'];
		table = ();
	 	semitones = [0, 2, 4, 5, 7, 9, 11];
	 	naturalNoteNames  = ["C", "D", "E", "F", "G", "A", "B"];
		(0..9).do{|o|
			naturalNoteNames.do{|c,i| 
				var n = (o + 1) * 12 + semitones[i];
				table[(c ++ o).asSymbol] = n;
				table[(c ++ "s"  ++ o).asSymbol] = n + 1;
				table[(c ++ "ss" ++ o).asSymbol] = n + 2;
				table[(c ++ "b"  ++ o).asSymbol] = n - 1;
				table[(c ++ "bb" ++ o).asSymbol] = n - 2;
			};
		};
	}
	
}
