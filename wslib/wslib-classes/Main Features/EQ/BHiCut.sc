// wslib 2010-2011
// cascaded butterworth filters
// these filters provide linear slope of -12 to -60 dB per octave
/*

(
6.collect({ |i| // order: 0-5
	BLowCut.magResponse( 1000, 44100, 1000, i ).ampdb
}).plot2( minval: -350, maxval: 10 ).superpose_(true);
)

*/

BHiCut : BEQSuite {
	
	classvar <allRQs;
	
	*initClass { 
		
		/*
		allRQs = [ 	// Tietze/Schenk, 1999 issue page 856, pdf page #882.
			[],
			[ 2.sqrt ],					 // 2nd order - 12dB per octave
			[1.8478, 0.7654 ],						// 4 - 24dB
			[1.9319, 1.4142, 0.5176 ],				// 6 - 36dB
			[1.9616, 1.6629, 1.1111, 0.3902 ],   		// 8 - 48dB
			[1.9754, 1.7820, 1.4142, 0.9080, 0.3129 ]	// 10 - 60dB
		];
		*/
		
		allRQs = [ 	// Tietze/Schenk, 1999 issue page 856, pdf page #882.
			[],									// 0: bypass
			[ 2 ].sqrt,					 		// 1: 2nd order - 12dB per octave
			[ 2 + 2.sqrt, 2 - 2.sqrt ].sqrt,	 		// 2: 4th order - 24dB
			[ 2 + 3.sqrt, 2, 2 - 3.sqrt ].sqrt,		// 3: 6th order - 36dB
			[ 2 + (2 + 2.sqrt).sqrt, 
			  2 + (2 - 2.sqrt).sqrt, 
			  2 - (2 - 2.sqrt).sqrt, 
			  2 - (2 + 2.sqrt).sqrt ].sqrt,           // 4: 8th order - 48dB
			[1.9754, 1.7820, 2.sqrt, 0.9080, 0.3129 ]	// 5: 10th order - 60dB
		];
		
		
	}
	
	*filterClass { ^BLowPass }
	
	*coeffs { |sr, freq = 1200, order = 2|
		^[]; // not us ed, only here for EQdef to know the args
	}
	
	*new1 { |rate = 'audio', in, freq, order=2, maxOrder=5| 
		if( order.isNumber ) {
			// fixed order: less cpu
			^this.newFixed( rate, in, freq, order );
		} { 	// variable order
			// assume control input
			// use lower maxOrder for less cpu use
			^this.newVari( rate, in, freq, order, maxOrder );
		};
	}
	
	*newFixed { |rate = 'audio', in, freq, order=2| // maxOrder doesn't apply here
		var rqs, selector; 
		rqs = allRQs.clipAt(order);
		selector = this.methodSelectorForRate( rate );
		rqs.do {Ê|rq|Êin = this.filterClass.perform( selector, in, freq, rq ) };
		^in;
	}
	
	*newVari { |rate = 'audio', in, freq, order=2, maxOrder=5|
		var rqs, selector;
		rqs = Select.kr(
			order.clip(0, maxOrder), 
			allRQs.collect(_.extend(maxOrder, 2.sqrt) ) 
		);
		selector = this.methodSelectorForRate( rate );
		rqs.do { |rq, i| 
			in = Select.ar( order > i, [ 
				in, 
				this.filterClass.perform( selector, in, freq, rq ) 
			]);
		};
		^in;	
	}

	*ar { |in, freq, order=2, maxOrder = 5|Ê
		^this.new1( 'audio', in, freq, order, maxOrder );
	}
	
	*kr { |in, freq, order=2, maxOrder = 5|Ê
		^this.new1( 'control', in, freq, order, maxOrder );
	}

}

BLowCut : BHiCut {
	*filterClass { ^BHiPass }
}

LRLowCut : BLowCut { // dual cascaded butterworth - use for crossovers
	
	*ar { |in, freq, order=2, maxOrder=5|
		in = this.new1( 'audio', in, freq, order, maxOrder );
		^this.new1( 'audio', in, freq, order, maxOrder );
	}
	
	*kr { |in, freq, order=2, maxOrder=5|
		in = this.new1( 'control', in, freq, order, maxOrder );
		^this.new1( 'control', in, freq, order, maxOrder );
	}
	
}

LRHiCut : BHiCut {
	
	*ar { |in, freq, order=2, maxOrder=5|
		in = this.new1( 'audio', in, freq, order, maxOrder );
		^this.new1( 'audio', in, freq, order, maxOrder );
	}
	
	*kr { |in, freq, order=2, maxOrder=5|
		in = this.new1( 'control', in, freq, order, maxOrder );
		^this.new1( 'control', in, freq, order, maxOrder );
	}
}