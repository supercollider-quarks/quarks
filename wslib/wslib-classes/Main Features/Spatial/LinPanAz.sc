LinPanAz {
	*ar {  arg numChans, in, pos = 0.0, level = 1.0, width = 2.0, orientation = 0.5; 
			// width not used for now
		pos = ( pos * (numChans / 2) ) + orientation;
		^
			( pos - (-1,0..(numChans-2)) )
				.fold( (numChans.neg/2) + 1, 1 )
				.max(0) 
			*.t in
		}
	
	*kr {  arg numChans, in, pos = 0.0, level = 1.0, width = 2.0, orientation = 0.5;
		pos = ( pos * (numChans / 2) ) + orientation;
		^( pos - (-1,0..(numChans-2)) )
				.fold( (numChans.neg/2) + 1, 1 )
				.max(0)
			*.t in 
		}
	}
	