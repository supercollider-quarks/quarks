+ Number {
	asSMPTE { |fps, type| 
		^SMPTE.type( this, (type ? \seconds).asSymbol, fps);
		}
		
	asSizedString { |size = 2, keepWidth = false|
		if( keepWidth.not )
			{ size = size.max( this.asString.size )  };
		if( this.isNegative )
			{ ^this.asString.reverse.extend( size, $  ).reverse }
			{ ^this.asString.reverse.extend( size, $0 ).reverse };
			
		 }
		 
	}
	
+ Collection {
	asSMPTE { |fps| ^SMPTE.array( this, fps ) }
	}
	
+ Nil {
	asSMPTE { |fps| ^SMPTE(0, fps); }
	}