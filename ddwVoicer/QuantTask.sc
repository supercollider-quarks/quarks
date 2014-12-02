
// DEPRECATED.
// Retained for the odd backward-compatibility purpose.

QuantTask : Task {
	// works like Task but with more flexible quantizing
	// starting / stopping etc. can be quantized using a quantize factor AND an offset
	
	play { arg quant, argClock, doReset;
		var schedTime;
		if (stream.notNil, { "already playing".postln; ^this });
		if (doReset ? false, { this.reset });
		clock = argClock ? clock ? TempoClock.default;
//clock.dump;
		stream = originalStream; 
		(quant == 0 || quant.isNil).if({	// no quantize, do it now
			clock.play(this, quant)
		}, {
			schedTime = quant.asTimeSpec.nextTimeOnGrid(clock);
				// is it too late to schedule? this can happen b/c of a negative offset
//["QuantTask-play", clock.elapsedBeats, clock.elapsedBeats.roundUp(quant), schedTime].postln;
			(clock.elapsedBeats > schedTime).if({ ^stream = nil });
			clock.schedAbs(schedTime, this);
		});
	}

	reset { arg quant;
		(quant == 0 || quant.isNil).if({	// no quantize, do it now
			originalStream.reset
		}, {
//["QuantTask-reset", clock.elapsedBeats.roundUp(quant) + offset - (0.01*clock.tempo)].postln;
			clock.schedAbs(quant.asTimeSpec.nextTimeOnGrid(clock) - (0.01*clock.tempo), {
				originalStream.reset;
				nil
			});
		});
	}
	
	stop { arg quant;
		(quant == 0 || quant.isNil).if({	// no quantize, do it now
			stream = nil
		}, {
			clock.schedAbs(quant.asTimeSpec.nextTimeOnGrid(clock), {
				stream = nil 
			});
		});
	}

	pause { arg quant;
		(quant == 0 || quant.isNil).if({	// no quantize, do it now
			stream = nil
		}, {
			clock.schedAbs(quant.asTimeSpec.nextTimeOnGrid(clock), {
				stream = nil 
			});
		});
	}
	resume { arg quant;
		^this.play(quant, clock, false)
	}
	
	start { arg quant;
		^this.play(quant, clock, true)
	}
	
	schedTime { arg quant;
		^quant.asTimeSpec.nextTimeOnGrid(clock)
	}
		
	stream_ { arg argStream, quant;
		(quant == 0 || quant.isNil).if({	// no quantize, do it now
			originalStream = argStream; 
			if (stream.notNil, { stream = argStream });
		}, {
			clock.schedAbs(quant.asTimeSpec.nextTimeOnGrid(clock), {
				originalStream = argStream; 
				if (stream.notNil, { stream = argStream });
			});
		});
	}
	
	clock_ { arg cl;
		stream.isNil.if({	// must never change the clock while the task is playing
			clock = cl;
		}, {
			"QuantTask-clock_ : can't change the clock while the task is playing.".warn;
		});
	}
}
