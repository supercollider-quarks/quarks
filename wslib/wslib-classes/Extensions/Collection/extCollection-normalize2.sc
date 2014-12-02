+ ArrayedCollection {
	normalize2 { arg max=1.0;
		// normalization as done on audio signals
		var maxItem = this.maxItem.max( this.minItem.neg );
		if( max == 1.0 )
			{ ^this.collect { |el| (el / maxItem) }; }
			{ ^this.collect { |el| (el / maxItem) * max }; };
			
		}
	}
