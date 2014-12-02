WarpIn {
	*ar {arg in, buffer = 0, warpFactor = 1, freqScale=1, windowSize=0.2, envbufnum = -1,
			overlaps=10, windowRandRatio = 0.1, preLevel = 0, interp = 1, mul=1, add=0; 
		var out, trig, grainrate, start, pointer, record;
		record = RecordBuf.ar(in, buffer, preLevel: preLevel, loop: 0);
		out = Warp0.ar(in.asArray.size, buffer, warpFactor, freqScale, windowSize, 
			envbufnum, overlaps, windowRandRatio, interp, mul, add);
		^out;
	}
}

Warp0 {
	*ar {arg numChannels = 1, buffer = 0, warpFactor = 1, freqScale=1.0, windowSize=0.2,
			envbufnum = -1, overlaps=10, windowRandRatio = 0.1, interp = 1, mul=1, add=0; 
		var pointer;
		pointer = LFSaw.kr( warpFactor * BufDur.kr(buffer).reciprocal, 1, 0.5, 0.5 );
		^Warp1.ar(numChannels, buffer, pointer, freqScale, windowSize, envbufnum, overlaps, 
			windowRandRatio, interp, mul, add);
		
	}
}