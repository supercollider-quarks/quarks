
// returns the next beat with a given offset 
// in relation to the current beat
// E.G. if beats is 10.2, and the offset is 0.25, => 10.25
// if beats is 10.3, and the offset is 0.25, => 11.25


+ TempoClock {

	nextBeatOffset { |offset = 0.25|
		^if(1-this.timeToNextBeat<= offset, {0}, {1}) + offset + this.beats.floor;
	}
	
}