+ SimpleNumber {
	xlistrand { |listToExclude = 0, maxRecursions = 1000|
		// may still return an item from listToExclude if maxRecursions is exceeded
		// better not set maxRecursons to inf as it may cause infinite loop
		var res;
		res = this.rand;
		if( listToExclude.asCollection.includes( res ) && { maxRecursions > 0 } )
			{ ^this.xlistrand( listToExclude, maxRecursions - 1 ) }
			{ ^res }
		}
	}