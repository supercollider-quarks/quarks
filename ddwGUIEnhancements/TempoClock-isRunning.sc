
+ TempoClock {
//	stop {
//		all.take(this);
//		this.dependants.do({ arg d; d.remove });  // this line is added
//		this.prStop;
//	}
//
//	tempo_ { arg newTempo;
//		this.setTempoAtBeat(newTempo, this.beats);
//		this.changed(\tempo);  // this line is added
//	}

	isRunning { ^ptr.notNil }
	guiClass { ^TempoClockGui }
}

