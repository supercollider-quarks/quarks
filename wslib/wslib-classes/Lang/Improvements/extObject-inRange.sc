// wslib 2006 :: InRange extension

+ Object { // switch to correct InRange version
	
	inRange { |lo= 0.0, hi = 1.0|
		case { this.rate == 'scalar' }
			{ ^( this >= lo ) && (this <= hi ) }
			{ this.rate == 'audio' }
			{ ^InRange.ar( this, lo, hi ) }
			{ this.rate == 'control' }
			{ ^InRange.kr( this, lo, hi ) }
		}
		
	binaryValue { ^this }
	// so now binaryValue on True/False can also be called on anything else
	// call this on < > or inRange to ensure binary output
	}
	
	