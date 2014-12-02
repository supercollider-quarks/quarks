+ TempoClock {
	// when you want schedule to play lots of events at once
	
	*newBig { arg tempo, beats, seconds, size = 1024;
		^super.new.initBig(tempo, beats, seconds, size)
		}
		
	initBig { arg tempo, beats, seconds, size;
		queue = Array.new(size.nextPowerOfTwo);
		this.prStart(tempo, beats, seconds);
		all = all.add(this);
	}
	
}