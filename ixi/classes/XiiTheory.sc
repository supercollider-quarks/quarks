XiiTheory {

	*new {
		^()
		.ionian_([2,2,1,2,2,2,1])
		.augmented_([3,1,2,1,3,1]); // etc.
		
		//^super.new; // needed for methods below
	}

// need to fill in the scales
	iwato { ^[0,1,5,6,10] }
	chinese { ^[0,4,6,7,11] } // mode of hirajoshi
// and chords
	major { ^[0, 4, 7] }
	

/*
ww.maqamworld.com/maqamat/bayati.html#bayati
a = [1]++Scale().bayati

(
Task({
	a.do({arg rel;
		Synth(\midikeyboardsine, [\freq, 440*rel]);
		0.5.wait;
	})
}).play
)
*/

	*chords {
		^[["Major", [0, 4, 7]], ["Minor", [0, 3, 7]],["5", [0, 7]], ["Dominant 7th", [0, 4, 7, 10]], ["Major 7th", [0, 4, 7, 11]], ["Minor 7th", [0, 3, 7, 10]], ["Minor Major 7th", [0, 3, 7, 11]], ["Sus 4", [0, 5, 7]], ["Sus 2",  [0, 2, 7]], ["6", [0, 4, 7, 9]], ["Minor 6", [0, 3, 7, 9]], ["9", [0, 2, 4, 7, 10]], ["Minor 9", [0, 2, 3, 7, 10]], ["Major 9", [0, 2, 4, 7, 11]], ["Minor Major 9", [0, 2, 3, 7, 11]], ["11", [0, 2, 4, 5, 7, 11]], ["Minor 11", [0, 2, 3, 5, 7, 10]], ["Major 11", [0, 2, 4, 5, 7, 11]], ["Minor Major 11", [0, 2, 3, 5, 7, 11]], ["13", [0, 2, 4, 7, 9, 10]], ["Minor 13", [0, 2, 3, 7, 9, 10]], ["Major 13", [0, 2, 4, 7, 9, 11]], ["Minor Major 13", [0, 2, 3, 7, 9, 11]], ["add 9", [0, 2, 4, 7]], ["Minor add 9", [0, 2, 3, 7]], ["6 add 9", [0, 2, 4, 7, 9]], ["Minor 6 add 9", [0, 2, 3, 7, 9]], ["Dominant 7th add 11", [0, 4, 5, 7, 10]], ["Major 7th add 11", [0, 4, 5, 7, 11]], ["Minor 7th add 11", [0, 3, 5, 7, 10]], ["Minor Major 7th add 11", [0, 3, 5, 7, 11]], ["Dominant 7th add 13", [0, 4, 7, 9, 10]], ["Major 7th add 13", [0, 4, 7, 9, 11]], ["Minor 7th add 13", [0, 3, 7, 9, 10]], ["Minor Major 7th add 13", [0, 3, 7, 9, 11]], ["7b5", [0, 4, 6, 10]], ["7#5", [0, 4, 8, 10]], ["7b9", [0, 1, 4, 7, 10]], ["7#9", [0, 3, 4, 7, 10]], ["7#5b9", [0, 1, 4, 8, 10]], ["m7b5", [0, 3, 6, 10]], ["m7#5", [0, 3, 8, 10]], ["m7b9", [0, 1, 3, 7, 10]], ["9#11", [0, 2, 4, 6, 7, 10]], ["9b13", [0, 2, 4, 7, 8, 10]], ["6sus4", [0, 5, 7, 9]], ["7sus4", [0, 5, 7, 10]], ["Major 7th Sus4", [0, 5, 7, 11]], ["9sus4", [0, 2, 5, 7, 10]], ["Major 9 Sus4", [0, 2, 5, 7, 11]]]
	}
	
	*scales{ 
		^[
		// 5 note scales
			["minorPentatonic", [0,3,5,7,10]],
			["majorPentatonic", [0,2,4,7,9]],
			["ritusen", [0,2,5,7,9]], // another mode of major pentatonic
			["egyptian", [0,2,5,7,10]], // another mode of major pentatonic
			
			["kumoi", [0,2,3,7,9]],
			["hirajoshi", [0,2,3,7,8]],
			["iwato", [0,1,5,6,10]], // mode of hirajoshi
			["chinese", [0,4,6,7,11]], // mode of hirajoshi
			["indian", [0,4,5,7,10]],
			["pelog", [0,1,3,7,8]],
			
			["prometheus", [0,2,4,6,11]],
			["scriabin", [0,1,4,7,9]],
			
		// 6 note scales
			["whole", (0,2..10)],
			["augmented", [0,3,4,7,8,11]],
			["augmented2", [0,1,4,5,8,9]],
			
			// hexatonic modes with no tritone
			["hexMajor7", [0,2,4,7,9,11]],
			["hexDorian", [0,2,3,5,7,10]],
			["hexPhrygian", [0,1,3,5,8,10]],
			["hexSus", [0,2,5,7,9,10]],
			["hexMajor6", [0,2,4,5,7,9]],
			["hexAeolian", [0,3,5,7,8,10]],
			
		// 7 note scales
			["ionian", [0,2,4,5,7,9,11]],
			["dorian", [0,2,3,5,7,9,10]],
			["phrygian", [0,1,3,5,7,8,10]],
			["lydian", [0,2,4,6,7,9,11]],
			["mixolydian", [0,2,4,5,7,9,10]],
			["aeolian", [0,2,3,5,7,8,10]],
			["locrian", [0,1,3,5,6,8,10]],
			
			["harmonicMinor", [0,2,3,5,7,8,11]],
			["harmonicMajor", [0,2,4,5,7,8,11]],
			
			["melodicMinor", [0,2,3,5,7,9,11]],
			["bartok", [0,2,4,5,7,8,10]], // jazzers call this the hindu scale
			
			// raga modes
			["todi", [0,1,3,6,7,8,11]], // maqam ahar kurd
			["purvi", [0,1,4,6,7,8,11]],
			["marva", [0,1,4,6,7,9,11]],
			["bhairav", [0,1,4,5,7,8,11]],
			["ahirbhairav", [0,1,4,5,7,9,10]],
			
			["superLocrian", [0,1,3,4,6,8,10]],
			["romanianMinor", [0,2,3,6,7,9,10]], // maqam nakriz
			["hungarianMinor", [0,2,3,6,7,8,11]],	
			["neapolitanMinor", [0,1,3,5,7,8,11]],
			["enigmatic", [0,1,4,6,8,10,11]],
			["spanish", [0,1,4,5,7,8,10]],
			
			// modes of whole tones with added note:
			["leadingWhole", [0,2,4,6,8,10,11]],
			["lydianMinor", [0,2,4,6,7,8,10]],
			["neapolitanMajor", [0,1,3,5,7,9,11]],
			["locrianMajor", [0,2,4,5,6,8,10]],
			
		// 8 note scales
			["diminished", [0,1,3,4,6,7,9,10]],
			["diminished2", [0,2,3,5,6,8,9,11]],
			
		// 12 note scales
			["chromatic", (0..11)]
		]
	}
	
	*tunings {
		^[
			["equal", Tuning.et],
			["pythagorean", Tuning.pythagorean],
			["just", Tuning.just],
			["sept 1", Tuning.sept1],
			["sept 2", Tuning.sept2],
			["mean 4", Tuning.mean4],
			["mean 5", Tuning.mean5],
			["mean 6", Tuning.mean6],
			["kirnberger", Tuning.kirnberger],
			["werckmeister", Tuning.werckmeister],
			["vallotti", Tuning.vallotti],
			["young", Tuning.young],
			["reinhard", Tuning.reinhard],
			["wcHarm", Tuning.wcHarm],
			["wcSJ", Tuning.wcSJ]

		]	
	}
	
	
}


/*
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
				

*/
