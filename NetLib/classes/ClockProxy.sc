ClockProxy {
	var <clock;
	
	*new { |clock|
		^super.newCopyArgs(clock)
	}
	
	clock_ { |newClock|
		
		var oldClock = clock;
		var queue = oldClock.queue.copy;
		var newTempo = try { newClock.tempo } ? 1.0;
		var oldTempo = try { oldClock.tempo } ? 1.0;
		var tempoRatio = newTempo / oldTempo;
		var elapsed = clock.elapsedBeats;
		
		queue.pairsDo {|delta, item|
			newClock.sched(delta - elapsed * tempoRatio, item)
		};
		
		
		// removed all items from old clock. 
		oldClock.queue.removeAllSuchThat(true);
		// end old clock without sending removedFromScheduler
		SystemClock.sched(0.01,  { oldClock.prClear });

		clock = newClock;
	}
	
	
	// for efficiency: pass on messages to clock.
	
	permanent_ { |flag|
		clock.permanent_(flag)
	}
	permanent { |flag|
		^clock.permanent
	}
	stop { clock.stop }
	
	play { |task, quant = 1|
		clock.play(task, quant)
	}
	sched { |delta, item|
		^clock.sched(delta, item)
	}
	schedAbs { |time, item|
		^clock.sched(time, item)
	}
	playNextBar { |task|
		clock.playNextBar(task)
	}
	elapsedBeats {
		^clock.elapsedBeats;
	}
	beats {
		^clock.beats
	}
	clear { clock.clear }
	
	tempo_ { |newTempo|
		clock.tempo_(newTempo)
	}
	timeToNextBeat { |quant=0.0|
		^clock.timeToNextBeat(quant)
	}
	nextTimeOnGrid { |quant = 1, phase=0|
		^clock.nextTimeOnGrid(quant, phase)
	}
	beats2bars { arg beats;
		^clock.beats2bars(beats)
	}
	bars2beats { arg bars;
		^clock.bars2beats(bars)
	}
	bar {
		^clock.bar
	}
	nextBar { arg beat;
		^clock.nextBar(beat)
	}
	
}
