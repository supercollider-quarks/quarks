LoudnessModel { 

	classvar data, phons, freqs, logfreqs;
	
	*initClass {
			data = [ 
				[ 76.6, 83.1, 89.3, 94.6, 99.5, 104.3, 108.9, 113.5, 118, 122.5, 127.1 ], 
				[ 67.3, 75.7, 82.9, 88.8, 94.1, 99.1, 104.1, 108.9, 113.7, 118.5, 123.3 ], 
				[ 58.4, 68.6, 76.6, 83, 88.7, 94.1, 99.3, 104.5, 109.6, 114.7, 119.9 ], 
				[ 50.2, 61.7, 70.4, 77.2, 83.3, 89, 94.6, 100.1, 105.6, 111, 116.2 ], 
				[ 43.2, 55.6, 64.8, 72, 78.5, 84.6, 90.5, 96.3, 102.1, 107.8, 113.5 ], 
				[ 36.7, 49.5, 59.2, 66.9, 73.7, 80.2, 86.4, 92.6, 98.7, 104.8, 110.9 ], 
				[ 30.7, 43.5, 53.6, 61.7, 68.9, 75.8, 82.5, 89, 95.5, 102, 108.4 ], 
				[ 25.7, 38.3, 48.7, 57.1, 64.7, 72, 79, 85.9, 92.8, 99.6, 106.4 ], 
				[ 21.3, 33.5, 44.1, 52.8, 60.8, 68.4, 75.8, 83.1, 90.3, 97.5, 104.7 ], 
				[ 17, 28.5, 39.2, 48.2, 56.6, 64.6, 72.4, 80.1, 87.8, 95.5, 103.1 ], 
				[ 13.6, 24.4, 35.1, 44.4, 53.1, 61.5, 69.7, 77.8, 85.8, 93.9, 100.9 ], 
				[ 10.7, 20.9, 31.5, 41, 50, 58.7, 67.2, 75.7, 84.1, 92.5, 100.9 ], 
				[ 8, 17.6, 28.2, 37.8, 47.1, 56.1, 65, 73.8, 82.6, 91.4, 100.2 ], 
				[ 5.7, 14.8, 25.3, 35.1, 44.6, 53.9, 63.1, 72.3, 81.4, 90.5, 99.6 ], 
				[ 3.9, 12.6, 23, 32.9, 42.6, 52.2, 61.6, 71.1, 80.5, 89.9, 99.3 ], 
				[ 2.5, 10.8, 21.1, 31.1, 40.9, 50.7, 60.5, 70.2, 79.9, 89.6, 99.3 ], 
				[ 1.9, 9.9, 20, 30.1, 40, 49.9, 59.8, 69.7, 79.6, 89.5, 99.4 ], 
				[ 2.2, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 ], 
				[ 3.2, 11.1, 21.2, 31.4, 41.5, 51.6, 61.7, 71.9, 82, 92.1, 102.3 ], 
				[ 1.8, 10.4, 21.1, 31.6, 41.9, 52.2, 62.4, 72.6, 82.8, 92.9, 102.8 ], 
				[ -1.2, 7.5, 18.3, 28.8, 39.1, 49.4, 59.6, 69.8, 80, 90.2, 100.4 ], 
				[ -4.3, 4.7, 15.6, 26.2, 36.6, 46.9, 57.2, 67.4, 77.6, 87.7, 97.6 ], 
				[ -6.3, 3.2, 14.4, 25.2, 35.6, 46, 56.3, 66.5, 76.7, 86.8, 96.7 ], 
				[ -6, 3.8, 15.3, 26.1, 36.7, 47, 57.3, 67.5, 77.7, 87.9, 98.1 ], 
				[ -2.5, 7.1, 18.4, 29.2, 39.7, 50.1, 60.4, 70.6, 80.8, 91.1, 101.6 ], 
				[ 5.4, 14, 24.6, 35, 45.3, 55.4, 65.6, 75.7, 85.8, 96, 106.4 ], 
				[ 13.4, 21.4, 31.5, 41.4, 51.2, 61.1, 70.9, 80.6, 90.4, 100.5, 111.1 ], 
				[ 15, 23.9, 34.3, 44.2, 53.7, 63.2, 72.6, 81.9, 91.2, 100.6, 110.2 ], 
				[ 15.6, 23.9, 33.6, 42.7, 51.5, 60.2, 68.7, 77.3, 85.8, 94, 101.7 ] 
			];
			phons = [2,10,20,30,40,50,60,70,80,90,100];
			freqs = [ 20, 25, 31.5, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 
					500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 
					6300, 8000, 10000, 12500 ];
			logfreqs = ((freqs.log10) - (20.log10)) / ((12500.log10) - (20.log10)); 
	}

	*freqmap {|freq| 
			if (freq > 12500) {freq = 12500};
			if (freq < 20) {freq = 20;};
			^((freq.log10) - (20.log10)) / ((12500.log10) - (20.log10));
	}
	
//// Usage: LoudnessModel.calc(freq, spl) 
//// returns the phon value for a certain frequency in Hz and intensity in dB SPL
//// Interpolation code and data for equal loudness contours shared by Nick Collins
	*calc {|freq, spl| var findex, fprop, lastfreq, contour, result;
		freq = LoudnessModel.freqmap(freq);
		findex = 0;
		lastfreq = logfreqs[0];				
		logfreqs.do{|val,i|
			if( i > 0) {
				if(freq.inclusivelyBetween(lastfreq, val))
					{findex = i; fprop = (freq - lastfreq) / (val - lastfreq)};
			};
			lastfreq = val;
		};
		contour = ((1 - fprop) * data[findex-1]) + ((fprop) * data[findex]); 
		result = if(spl < contour[0]) 
			{ 0 } {
			if(spl > contour[10]) 
				{ 100 }  
				{
					findex = 0;
					10.do{|i| 
						if (spl.inclusivelyBetween(contour[i], contour[i+1]))
						{
							findex = i; 
							fprop = (spl - contour[i]) / (contour[i+1] - contour[i])
						}
					};
					((1.0 - fprop) * phons[findex]) + (fprop * phons[findex+1]);
				};
			};
		^result

	}

	*partialMasking {|p, p2, a2, grad = 12| ^a2 - (grad * absdif(p,p2)) }
	
	*maskingSum {|partials, levels, grad = 12| var pSum = []!levels.size, hp = partials.hzToErb;
		hp.do{|p1, i|
		  hp.do{|p2, j|
			if (i != j) {
				pSum[j] = pSum[j].add(LoudnessModel.partialMasking(p1,p2,levels[i], grad).dbamp)
			}
		  }
		};
		^pSum.collect{|p| max(p.sum.ampdb, 0)};
	}
	
	*audibleLevel {|partials, levels, grad = 12| 
		^max(0, levels - LoudnessModel.maskingSum(partials, levels, grad)) 
	}
	
	*audibility {|partials, levels, grad = 12| 
		^1-exp(LoudnessModel.audibleLevel(partials, levels, grad).neg / 15)
	}
	
	*compensateMasking {|partials, levels, grad = 12| 
		var al = LoudnessModel.audibility(partials, levels, grad);
		^((al / al.maxItem) * levels.dbamp).ampdb.clip(0,inf);
	}

}

/* 
(c) 2007 jsl
*/