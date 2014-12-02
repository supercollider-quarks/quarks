+ SimpleNumber{

	// receiver is a time in seconds
	// returns string "hh:mm:ss:ff" where f is frames
	
	// uses one global SMPTE object to prevent memory buildup when called lots of times
	
	asSMPTEString { |fps = 25| ^SMPTE.global.initSeconds( this, fps ).toString }
}

+ String {
	asSeconds { |fps = 25|
		^SMPTE.global.string_( this, fps ).asSeconds;
		}
	}