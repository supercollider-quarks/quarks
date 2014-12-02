RingList : List {

	var <>readID = 0;
	var <>writeID = 0;
	
	read{
		var val;
		val = this.wrapAt( readID );
		if ( writeID >= readID ){
			// increase readID only if we are still ahead with our writing
			readID = readID + 1;
		};
		^val;
	}

	addData{ |arrayIn|
		this.wrapPutSeries( writeID + 1, arrayIn );
		writeID = writeID + arrayIn.size - 1;
	}

	wrapPutSeries{ |firstindex, arrayIn|
		arrayIn.do{ |it,i|
			this.array.wrapPut( firstindex + i, it );
		}
	}

}