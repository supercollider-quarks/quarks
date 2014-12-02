+ Object {
	drawBounds { 
		
		// future proof version
		^if( this.class.instVarNames.includes( \relativeOrigin )
				&& { this.slotAt( \relativeOrigin ) == false } )
			{ this.absoluteBounds; }
			{ this.bounds.moveTo(0,0) }; // should't sth like this be a primitive?
			
		/*
		^if ( relativeOrigin ) // thanks JostM !
			{ this.bounds.moveTo(0,0) }
			{ this.absoluteBounds; };
		*/
		}
	}