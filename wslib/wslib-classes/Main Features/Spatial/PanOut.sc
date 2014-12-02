// wslib 2006
// Pans a mono signal over multiple speakers.
// like PanAz, but with unlimited outputs and never more that 2 busses used.
// use instead of Out.ar .
// CPU and load time benefits at high numbers of outputs ( > 16 )

PanOut {

	*pan { |in, pos = 0| ^Pan2.ar( in, ( pos.fold(0.0,1.0) - 0.5) * 2.0) }
	
	*switchBus { |bus = 0, in, wrap|  // in should be 2 channels
		
		if( wrap.isNil )
			{ ^[ Out.ar( bus.round(2), in[0] * (bus >= -1 ).binaryValue  ),
				Out.ar(  ( (bus+1).round(2) - 1), in[1] * (bus >= 0 ).binaryValue ) ] }
			{ ^[ Out.ar( bus.round(2).wrap(0,wrap), in[0] ),
				Out.ar(  ( (bus+1).round(2) - 1).wrap(0,wrap), in[1] ) ]  };
		}
	
	*ar { |bus = 0, channel, wrap| 
		^this.switchBus( bus, this.pan( channel, bus ), wrap ); }
	
	}