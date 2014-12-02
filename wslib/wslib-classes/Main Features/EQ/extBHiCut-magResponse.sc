+ BHiCut {
	
	*magResponse { arg freqs = 1000, sr = 44100, freq = 1200, order = 2;
		var rqs, in; 
		rqs = allRQs.clipAt(order); 
		
		if( freqs.isNumber ) // autoscale 20-22000
			{ freqs = (..freqs).linexp(0,freqs-1, 20, 22000); };
		in = 1!freqs.size;
		rqs.do {Ê|rq|Êin = in * this.filterClass.magResponse( freqs, sr, freq, rq ) };
		^in
	}
	
}

+ LRHiCut {
	
	*magResponse { arg freqs = 1000, sr = 44100, freq = 1200, order = 2;
		^BHiCut.magResponse( freqs, sr, freq, order ).squared;
	}
	
}

+ LRLowCut {
	
	*magResponse { arg freqs = 1000, sr = 44100, freq = 1200, order = 2;
		^BLowCut.magResponse( freqs, sr, freq, order ).squared;
	}
	
}