// BeatKeeper is a model for dealing with sensible musical time or things like notation. 
// it basically introduces some rounding for starttimes, so things like triplets within a beat
// don't introduce a constant drift away from whole numbers.

BeatKeeper {
	var <now, tempo, rTempo;
	
	*new {arg now = 0, tempo = 60;
		^super.newCopyArgs(now).initBeatKeeper(tempo);
		}

	initBeatKeeper {arg argTempo;
		this.tempo_(argTempo);
	}
	
	tempo_ {arg argTempo;
		(argTempo > 0).if({
			tempo = argTempo;
			rTempo = argTempo.reciprocal;
		}, {
			tempo = rTempo = 0;
		})	
	}
	
	// wait advances and rounds now according to the duration of a beat
	wait {arg waittime, beatDur = 1, tolerance = 0.0001, base = 1;
		this.now_(now + (((waittime / beatDur) * 60) * rTempo), tolerance, base);
		}
	
	// by default, round to the nearest beat
	roundNow {arg tolerance = 0.001, base = 1;
		var tmp, diff;
		tmp = now.round(base);
		diff = (now - tmp).abs;
		(diff < tolerance).if({now = tmp});
		}
		
	now_ {arg newNow, tolerance = 0.0001, base = 1;
		now = newNow;
		this.roundNow(tolerance, base);
		}	
}