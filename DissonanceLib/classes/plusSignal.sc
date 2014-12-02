+ Array 
{
// these methods are helpful for combining Dissonance with FFT data

	asFreqMag { |win, sr |
		var resF = [], resM = [], bw;
		sr ?? {Server.default.sampleRate / 2};
		win ?? {this.size};	
		bw = sr/win;
		this.reverseDo{|mag, i|
			resF = resF.add(bw * i); 
			resM = resM.add(mag);
		}
		^[resF, resM.reverse]
	}
	
	asFreqMagPhase {|phases, win, sr|
		sr ?? {Server.default.sampleRate/2};
		win ?? {this.size};
		
		
			
	}

// return the n highest partials
// this should be an array in the form [ freqs, mags ] like that returned from asFreqMag
	findNlargest { | n = 10, sort = false |
		var idx, order, resF = [], resM = [], temp = this.deepCopy;
		n.do{|i|
			idx = temp[1].indexOf(temp[1].maxItem);
			[idx, temp[1][idx], temp[0][idx]].postln;
			resF = resF.add(temp[0].removeAt(idx));
			resM = resM.add(temp[1].removeAt(idx));
		};
			if (sort) {
				order = resF.order; 
				^[resF[order], resM[order]]
			}{
				^[resF, resM]
			}
	}

	maxima { // for working with arrays of [freq, mag] and operating on mags!
		var temp = this.deepCopy, gradient = temp[1].differentiate,
		prev = gradient.first, res = [], max = [[],[]];
		gradient.do{|a| 
			if (a.sign != prev.sign) //changes in sign indicating changes in curvature
			{ 
				res = res.add(gradient.indexOf(a) - 1);				prev = a;
			}
		};
		res.do{|r| // res contains indexes of the inflection points...filter out minima
			if (temp[1][r] > temp[1].wrapAt(r-1)) //check to the left
				{
					max[0] = max[0].add(temp[0][r]);
					max[1] = max[1].add(temp[1][r]); 
				}
			};
		if (max[0].isEmpty) {max = [ [0], [0] ]};
		^max			
	}

	// linear interpolation between 2 arrays; ammount from 0 = first array to 1 = second array
	interpolate2 {|that, ammount = 0.5|  ^((1-ammount) * this) + (ammount * that) }
	
	// interpolation between 3 arrays, low (= this), mid and high
	interpolate3 { |mid, high, ammount| var res; 
			if (ammount < 0)
//				{ res = this.interpolate2(mid, ammount + 1) }
//				{ res = mid.interpolate2(high, ammount) };
//			^res
				{ ^this.interpolate2(mid, ammount + 1) }
				{ ^mid.interpolate2(high, ammount) };
	}

}		

+ Signal {

	analyseN {| max = 10, normalize = true, sort = true,
			 wSize = 512, ovlp = 256, wType = \hammingWindow |
		var spectrum, freqAmps;
		^spectrum = this.stft(wSize, ovlp, wType);

//		if (normalize) {
//				spectrum = spectrum * spectrum.collect(_.maxItem).maxItem.reciprocal;
//		};
		
//		freqAmps  = spectrum.asFreqMag;
//		^freqAmps.findNlargest(max, sort)		
		
	}
	
	
//	analyseFileN {|path = nil, wSize = 128, ovlp = 64, wType = \hammingWindow, 
//				max = 10, normalize = true, sort = true|
//		var spectrum, freqAmps, file, size, signal, array, res;
//		path ?? {
//			CocoaDialog.getPaths({ arg paths;
//			paths.do({ arg p;	path = p;});
//			},{
//			"cancelled".postln });
//		};
//		file  = SoundFile.new;
//		file.openRead(path).if {
//			size = file.numFrames * file.numChannels;
//			array = FloatArray.newClear(size);
//			file.readData(array);
//		};
//		file.close; 
//		signal = Signal.newFrom(array);
//		spectrum = signal.stft(wSize, ovlp, wType);
//		if (normalize) {
//				signal = signal * signal.collect(_.maxItem).maxItem.reciprocal;
//		};
//		
//		freqAmps  = spectrum.asFreqMag;
////		freqAmps[1]  = freqAmps[1].reverse;
//		^freqAmps.findNlargest(max, sort)		
//		
//	}






}