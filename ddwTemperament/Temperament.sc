
// set of temperament classes
// for custom tuning methodologies, you will need to override:
//	calibrate	-- compute base frequency given n (noteindex) and f(n) (freq for that note)
//	stepsPerOctave_  -- change steps per octave, no adjustment to base frequency
//	setStepsPerOctave  -- change steps per octave, hold given note index to its current frequency
//	cps -- convert noteindex to frequency (Hz)

// h. james harkins -- jamshark70@dewdrop-world.net

EqualTemperament {
	var	<stepsPerOctave = 12,
		<basefreq, <ratio, <>root = 0;
	var <octaveRatio = 2;

	*new { |stepsPerOctave = 12, calibratefreq = 440, calibratenote = 69 ... args|
		^super.new.init(stepsPerOctave, calibratefreq, calibratenote, *args)
	}

	init { |stepsPerOctave = 12, calibratefreq = 440, calibratenote = 69|
		this.stepsPerOctave_(stepsPerOctave).calibrate(calibratefreq, calibratenote)
	}

		// set base frequency directly
	basefreq_ { |baseHz (0.midicps)|
		basefreq = baseHz;
	}

		// set frequency of arbitrary note (typically, tune a' to a given frequency)
	calibrate { |freq = 440, noteindex = 69|
		basefreq = freq / (octaveRatio ** (noteindex / stepsPerOctave));
	}

		// change steps per octave, keep base frequency the same (upper frequencies will change)
	stepsPerOctave_ { |steps = 12|
		stepsPerOctave = steps;
		ratio = octaveRatio ** (stepsPerOctave.reciprocal);
	}

		// change steps per octave, hold arbitrary note (e.g. A=440) to same frequency as before
	setStepsPerOctave { |steps = 12, noteindex = 69|
		var	base440 = this.cps(noteindex);
		this.stepsPerOctave = steps;
		this.calibrate(base440, noteindex);
	}

	octaveRatio_ { |ratio = 2|
		octaveRatio = ratio;
		this.stepsPerOctave = stepsPerOctave;
	}

	setOctaveRatio { |ratio = 2, noteindex = 69|
		var	base440 = this.cps(noteindex);
		octaveRatio = ratio;
		this.stepsPerOctave = stepsPerOctave;
		this.calibrate(base440, noteindex);
	}

	cps { |noteindex|
			// base * (2 ^ octave) * (ratio ^ scaleDegree)
			// 2 ^ octave factor reduces rounding errors from simpler formula
		^basefreq * (octaveRatio ** ((noteindex / stepsPerOctave).trunc))
			* (ratio ** (noteindex % stepsPerOctave));
	}

		// for compatibility with midi-to-cps functions
	value { |noteindex|
		^this.cps(noteindex)
	}

}

// apply arbitrary tuning adjustment to every note of a scale
// tunings are given in noteindex units, e.g., 0.01 is one cent
// fractional note indices blend adjacent offsets

TuningOffset : EqualTemperament {
	var	<tunings, prTunings;

	*new { |stepsPerOctave = 12, calibratefreq = 440, calibratenote = 69, tunings = 0|
		^super.new(stepsPerOctave, calibratefreq, calibratenote, tunings)
	}

	init { |stepsPerOctave = 12, calibratefreq = 440, calibratenote = 69, tunings = 0|
		this.stepsPerOctave_(stepsPerOctave)
			.tunings_(tunings)
			.calibrate(calibratefreq, calibratenote)
	}

	tunings_ { |tuning|
		tunings = tuning.asArray.collect({ |item| item ? 0.0 }).wrapExtend(stepsPerOctave);
		this.root_(root);
	}

	calibrate { |freq = 440, noteindex = 69|
		(noteindex != noteindex.trunc).if({
			MethodError("noteindex should be an integer when calibrating", this).throw;
		});
		basefreq = freq / (octaveRatio ** ((noteindex - prTunings.wrapAt(noteindex)) / stepsPerOctave));
	}

	cps { |noteindex|
		noteindex = noteindex + prTunings.blendAt(noteindex, \wrapAt);
		^basefreq * (octaveRatio ** ((noteindex / stepsPerOctave).trunc))
			* (ratio ** (noteindex % stepsPerOctave));
	}

	root_ { |newroot = 0|
		root = newroot % stepsPerOctave;
		prTunings = tunings.rotate(root);
	}
}

// specify a ratio above the base note for each note index
// especially good for just intonations or pythagorean

TuningRatios : TuningOffset {

	tunings_ { |tuning, calibratenote|
		(tuning.size != stepsPerOctave).if({
			MethodError("You must supply % ratios.".format(stepsPerOctave), this).throw;
		});
		tuning.detect({ |item| item.isNumber.not }).notNil.if({
			MethodError("Ratios array must not have non-numeric values.", this).throw;
		});
		tunings = tuning ++ [octaveRatio];	// array must end with octaveRatio for blendAt
		this.root_(root);
		calibratenote.notNil.if({ this.setStepsPerOctave(stepsPerOctave, calibratenote) });
	}

	calibrate { |freq = 440, noteindex = 69|
		(noteindex != noteindex.trunc).if({
			MethodError("noteindex should be an integer when calibrating", this).throw;
		});
		basefreq = freq / ((octaveRatio ** (noteindex / stepsPerOctave).trunc)
			* prTunings[noteindex % stepsPerOctave]);
	}

	cps { |noteindex|
		^basefreq * (octaveRatio ** ((noteindex / stepsPerOctave).trunc))
			* (prTunings.blendAt(noteindex % stepsPerOctave));
	}

	root_ { |newroot = 0|
		root = newroot % stepsPerOctave;
		prTunings = tunings.drop(-1).rotate(root);
		prTunings = prTunings.collect { |item, i|
			item / prTunings[0] * (if(root > 0 and: { i >= root }) { octaveRatio } { 1 })
		};
	}
}

// in just intonation, you might need several varieties of the tuning to get all the right ratios
// this is a container for them, and uses the fractional part of the note to choose the variety
// x.0 --> tuning 0, x.01 --> tuning 1, etc.
// any subclass of EqualTemperament is valid

CompositeTuning {
	var	<tunings, <size;

	*new { |tunings|
		^super.new.tunings_(tunings)
	}

	tunings_ { |tuningList|
		tunings = tuningList;
		size = tunings.size;
	}

	at { |i| ^tunings[i] }
	wrapAt { |i| ^tunings.wrapAt(i) }
	clipAt { |i| ^tunings.clipAt(i) }
	foldAt { |i| ^tunings.foldAt(i) }

	put { |i, tuning| tunings[i] = tuning }
	wrapPut { |i, tuning| tunings.wrapPut(i, tuning) }
	clipPut { |i, tuning| tunings.clipPut(i, tuning) }
	foldPut { |i, tuning| tunings.foldPut(i, tuning) }

	do { |func| tunings.do(func) }
	collect { |func| ^tunings.collect(func) }

	calibrate { |freq = 440, noteindex = 69|
		tunings.do(_.tryPerform(\calibrate, freq, noteindex))
	}
	root_ { |newroot = 0|
		tunings.do(_.tryPerform(\root_, newroot))
	}

	cps { |noteindex|
		(noteindex.size == 0).if({
			^tunings.wrapAt(((noteindex - noteindex.asInteger) * 100).round)
				.value(noteindex.asInteger)
		}, {
			^noteindex.asArray.collect({ |i|
				tunings.wrapAt(((i - i.asInteger) * 100).round).value(i.asInteger)
			});
		});
	}

		// for compatibility with midi-to-cps functions
	value { |noteindex|
		^this.cps(noteindex)
	}
}
