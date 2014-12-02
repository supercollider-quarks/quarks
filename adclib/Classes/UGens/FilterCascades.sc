Filter2Cascade { 		// so far, second order cascades only, Butterworth only. 
					// first order could be done also, as could tcheby and bessel filters. 
					// did not need them yet. 
					// could be moved to ugens for more efficiency if needed. 
					// adc 2006, with lots of help from Thomas Musil. 
					
	classvar <allRQs;
	*initClass { 
		allRQs = [ 	// Tietze/Schenk, 1999 issue page 856, pdf page #882.
			[],
			[1.4142],								// 2nd order
			[1.8478, 0.7654 ],						// 4
			[1.9319, 1.4142, 0.5176 ],				// 6
			[1.9616, 1.6629, 1.1111, 0.3902 ],   		// 8
			[1.9754, 1.7820, 1.4142, 0.9080, 0.3129 ]	// 10
		];
	}

	*ar { |in, freq, order=2|Ê
		var out, sr, rqs; 
		sr = SampleRate.ir; 
		rqs = allRQs.clipAt(order); 
		freq = freq.max(0.01); 
		
		out = in; 
		rqs.do {Ê|rq|Êout = SOS.ar(out, *this.freqQ2Coeffs(freq, rq, 1, sr)) };
		^out;
	}
	
	*kr { |in, freq, order=2|Ê
		var out, sr, rqs; 
		sr = ControlRate.ir; 
		rqs = allRQs.clipAt(order); 
		freq = freq.max(0.01); 
		
		out = in; 
		rqs.do {Ê|rq|Êout = SOS.kr(out, *this.freqQ2Coeffs(freq, rq, 1, sr)) };
		^out;
	}

}

LPF2Casc : Filter2Cascade { 
	
	*freqQ2Coeffs { |freq=440, rq=0.7, freqScale=1, sr=44100| 
	
		// generates [ a0, a1, a2, b1, b2 ]
		var a0, a1, a2, b1, b2;
		var l, al, bl2, rcp; 
				
			// Thomas Musils formula:
		l = 1.0 / tan(pi * freq / sr);
		al = rq * l; 
		bl2 = freqScale * l * l + 1.0; 
		rcp = 1.0 / (al + bl2);
		
		a0 = rcp;
		a1 = rcp * 2.0;
		a2 = a0; 
		b1 = rcp * 2.0 * (bl2 - 2.0);
		b2 = rcp * (al - bl2); 
		
		^[ a0, a1, a2, b1, b2 ];
	}
}

HPF2Casc : Filter2Cascade { 
	*freqQ2Coeffs { |freq=440, rq=0.7, freqScale=1, sr=44100| 
	
		// generates [ a0, a1, a2, b1, b2 ]
		var a0, a1, a2, b1, b2;
		var l, al, bl2, rcp; 
				
			// Thomas Musils formula:
		l = 1.0 / tan(pi * freq / sr);
		al = rq * l; 
		bl2 = freqScale * l * l + 1.0; 
		rcp = 1.0 / (al + bl2);
			
				// only this bit is different:
		a0 = rcp * (bl2 - 1.0);
		a1 = a0 * -2.0;
		a2 = a0; 
		
		b1 = rcp * 2.0 * (bl2 - 2.0);
		b2 = rcp * (al - bl2); 
		
		^[ a0, a1, a2, b1, b2 ]
	}
}

/******
	// for comparison, freq formula in RLPF_next 
		float qres = sc_max(0.001, reson);
		float pfreq = freq * unit->mRate->mRadiansPerSample;
		
		float D = tan(pfreq * qres * 0.5);
		float C = ((1.f-D)/(1.f+D));
		float cosf = cos(pfreq);
		
******/