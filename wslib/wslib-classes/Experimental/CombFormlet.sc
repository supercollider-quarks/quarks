// wslib 2007
// experimenting with phase cancellation

CombFormlet : Filter {
	*ar { arg in = 0.0, freq = 440.0, attacktime = 1.0, decaytime = 1.0, mul = 1.0, add = 0.0,
			minFreq = 20;
		^MulAdd( CombC.ar( in, 1/minFreq, 1/freq, decaytime ) - 				CombC.ar( in, 1/minFreq, 1/freq, attacktime ) , mul, add )
		}
	}