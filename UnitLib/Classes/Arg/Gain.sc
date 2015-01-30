// a level gain that can be used in a EQdef

Gain : Filter {
	*ar { |in, db = 0|
		^in * db.dbamp;
	}
	
	*coeffs { |sr = 44100, db = 0|
		^[[ db.dbamp ], [0]]
	}
}
