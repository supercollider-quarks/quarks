ChordtrisSoundModule {
	
	// transposition base key
	var currentBaseKey = -2;
	
	// lower bound for the base key to transpose
	var transposeLowerBound = -11;
	
	// upper transpoe bound
	var transposeUpperBound = 4;
	
	*initClass { StartUp.add { 	this.loadSynthDefs; this.loadPdefs; } }
		
	*loadSynthDefs {
		
		// SynthDefs
		SynthDef(\tetris, { |out, amp=0.3, freq=440, sustain=0.5|
			var snd;
			var sustainLevel = 0.5;
			snd = LFPulse.ar(freq)!2;
			//snd = Latch.ar(snd, Impulse.ar(8000));
			snd = snd * EnvGen.ar(Env([0, 1, sustainLevel, (sustainLevel/1.7), 0], [0.001, sustain/4, sustain*(3/4), 0.1], [-4, -4, 0, 8]), doneAction:2);
			OffsetOut.ar(out, snd*amp);
		}).add;
		
		// SynthDef for the bass
		SynthDef(\bass, { |out, amp=0.5, freq=440, sustain=0.5|
			var snd;
			snd = LFTri.ar(freq)!2;
			snd = Latch.ar(snd, Impulse.ar(16000));
			snd = snd * EnvGen.ar(Env.linen(0.001, sustain, 0.03), doneAction:2);
			OffsetOut.ar(out, snd*amp);
		}).add;
		
		// SynthDef for the percussion track
		SynthDef(\percussion, { |out, amp=0.2, sustain=0.05|
			var snd;
			snd = WhiteNoise.ar!2;
			snd = snd * EnvGen.ar(Env.linen(0.001, sustain, 0.03), doneAction:2);
			OffsetOut.ar(out, snd*amp);
		}).add;
		
		// explosion sound
		SynthDef(\explosion, {|out, amp=0.7|
	
			var snd = Decay2.ar(Impulse.ar(0)) * LFNoise0.ar(Line.kr(8000, 100, 1))!2;
			DetectSilence.ar(snd, doneAction:2);
			Out.ar(out, snd*amp);
			
		}).add;
		
		// SynthDef for the keyboard
		SynthDef(\moog, { |out, amp=0.8, freq=440, sustain=1, fattack=0.1, famp=4|
			var sound = Pulse.ar([freq, freq*Rand(0.999, 1.001)], [0.2, 0.8]);
			var fenv = EnvGen.kr(Env.perc(fattack, sustain), 1, freq*famp, freq);
			sound = MoogFF.ar(sound, fenv, 2);
			sound = sound * EnvGen.kr(Env.perc(0.01, sustain), doneAction: 2);
			Out.ar(out, sound * amp);
		}).add;
		
		// Game Over Sound
		SynthDef(\gameOver, { |out, amp=0.5|
			var dur = 0.5;
			var trig = TDuty.kr(Dseq([dur], 3));
			var gate = abs(1 - (PulseCount.kr(trig) > 3)); 
			var freq = Demand.kr(trig, 0, Dseq([72, 71, 70, 69].midicps)) + SinOsc.kr(6).range(-10, 10);
			var sound = MoogFF.ar(Saw.ar(freq), EnvGen.kr(Env.sine(dur), trig, 4000, 800), 0.2);
			
			sound = sound * EnvGen.kr(Env.asr(0, 1, 4), gate, doneAction: 2);
			
			Out.ar(out, sound * amp);
		}).add;
		
		SynthDef(\pauseSound, {|out, amp=0.7|
	
			var snd = Pulse.ar([57, 64, 69].midicps);
			snd = BPF.ar(snd, Line.kr(7000, 2000, 0.4), 0.6);
			snd = snd * EnvGen.ar(Env.perc(0.01, 0.5), doneAction: 2);
			snd = Splay.ar(snd);
			Out.ar(out, snd*amp);
			
		}).add;

		
	}
	
	*loadPdefs {
		
		// global time divisor, can be used to make the tune faster or slower
		var timeDivisor = 5;
		var repeatsPart1 = 2;
		var repeatsPart2 = 1;
		
		// ***** Pattern definitions for the first voice *****
		
		// part 1
		
		var cond6b = Pif( Pfunc { Pdefn(\scale).source == Scale.harmonicMinor }, 6b, 6);
		
		var notes_voice1_part1 = Ppatlace([4, 1, 2, 3, 2, 1, 0, 0, 2, 4, 3, 2, 1, 2, 3, 4, 2, 0, 0, \p, 3, 5, 7, cond6b, 5, 4, 2, 4, 3, 2, 1, 1, 2, 3, 4, 2, 0, 0], repeatsPart1);
		
		var times_voice1_part1 = Pseq([2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 3, 1, 2, 2, 2, 2, 4, 1, 2, 1, 2, 1, 1, 3, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 2, 4], repeatsPart1);
		
		//var legato_voice1_part1 = Pseq([Pn(1.5, 39)], repeatsPart1);
		
		// part 2
		
		var notes_voice1_part2 = Pseq([4,2,3,1,2,0,-1, 4,2,3,1,2,4,7,6]-7, repeatsPart2);
		var times_voice1_part2 = Pseq([Pn(4,6), 8, Pn(4,4), 2, 2, 4, 8], repeatsPart2);
		//var legato_voice1_part2 = Pseq([Pn(4, 15)], repeatsPart2);
		
		// ***** Pattern definitions for the second voice *****
		
		// part 1
		var notes_voice2_part1 = Ppatlace([8, 6, 7, 8, 7, 6, 4, 4, 7, 9, 8, 7, 6, 4, 6, 7, 8, 9, 7, 4, 4, \p, 5, 7, 9, 9,9, 8, 7, cond6b, 4, cond6b, 7, cond6b, 5, 4, 6, 4, 6, 7, 8, 6, 8, 6, 7, 4, 4, 4], repeatsPart1);
		
		var times_voice2_part1 = Pseq([2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1,1,1, 1, 2, 2, 2, 2, 4, 1, 2, 1, 1, 0.5, 0.5, 1, 1, 3, 1, 1, 0.5,0.5 , 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 4], repeatsPart1);
		//var legato_voice2_part1 = Pseq([Pn(1.2, 50)], repeatsPart1);
		
		// part 2
		
		var seq1 = Pseq([Pseq([0, 4], 4), Pseq([-1, 4], 4), Pseq([0, 4], 4)]);
		var notes_voice2_part2 = Pseq([seq1, Pseq([-3, 4], 2), -1, seq1, Pseq([-1, 4], 2), -1], repeatsPart2);
		var times_voice2_part2 = Pseq([Pn(1,28), 4, Pn(1,28), 4], repeatsPart2);
		//var legato_voice2_part2 = Pseq([Pn(4, 15)], repeatsPart2);
		
		// ***** Pattern definitions for the bass *****
		
		// part 1
		
		var notes_bass_part1 = Pseq([Pseq([4,11], 4), Pseq([7,14], 4), Pseq([6,13], 2), Pseq([4,11], 2), Pseq([7,14], 3), 8, 9, 10, 3, 3, 3, 7, 5, 2, 9, 9, 2, 9, 9, 8, 15, 15, 4, 11, 6, 13, 7, 11, 7, 11, 7], repeatsPart1);
		
		var times_bass_part1 = Pseq([Pn(1,32), 1, 2, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, Pn(1, 8), 4], repeatsPart1);
		
		// part 2
		
		var notes_bass_part2 = Pseq([16, 14, 15, 13, 14, 11, 11, 13, 16, 14, 15, 13, 14, 16, 18, 17, \p], repeatsPart2);
		var times_bass_part2 = Pseq([Pn(4,8), Pn(4,4), 2, 2, 4, 4, 4], repeatsPart2);
		
		// ***** Pattern definitions for the percussion track *****
		
		// part 1
		var uzuz = Pseq([\p, 1], 2);
		var notes_perc_part1 = Pn(Pseq([uzuz, \p, 1,1, \p, 1, uzuz, \p, 1, 1, 1], 4), repeatsPart1);
		
		var uzuzt = Pn(1, 4);
		var times_perc_part1 = Pn(Pseq([uzuzt, 1, 0.5, 0.5, 1, 1, uzuzt, uzuzt], 4), repeatsPart1);
		
		// part 2 is the same as part 1 for the percussion track
		// here is redundant code because of the repetition number
		
		var notes_perc_part2 = Pn(Pseq([uzuz, \p, 1,1, \p, 1, uzuz, \p, 1, 1, 1], 4), repeatsPart2);
		var times_perc_part2 = Pn(Pseq([uzuzt, 1, 0.5, 0.5, 1, 1, uzuzt, uzuzt], 4), repeatsPart2);
	
		
		Pdefn(\scale, Scale.harmonicMinor);
		Pdefn(\baseKey, -3);
		Pdefn(\musicVolume, ChordtrisPreferences.getPreferences.at(\musicVolume).value);
		
		Pdef(\voice1, Pbind(
			\instrument, \tetris,
			\degree, Pseq([notes_voice1_part1, notes_voice1_part2], inf),
			\dur, Pseq([times_voice1_part1, times_voice1_part2]/timeDivisor, inf),
			\scale, Pdefn(\scale),
			\octave, 6,
			\amp, 1 * Pdefn(\musicVolume),
			\legato, 0.9,
			//\legato, Pseq([legato_voice1_part1, legato_voice1_part2], inf),
			\ctranspose, Pdefn(\baseKey),
			
		));
		
		Pdef(\voice2, Pbind(
			\instrument, \tetris,
			\degree, Pseq([notes_voice2_part1, notes_voice2_part2], inf),
			\dur, Pseq([times_voice2_part1, times_voice2_part2]/timeDivisor, inf),
			\scale, Pdefn(\scale),
			\octave, 5,
			\amp, 0.6 * Pdefn(\musicVolume),
			\legato, 0.7,
			//\legato, Pseq([legato_voice2_part1, legato_voice2_part2], inf),
			\ctranspose, Pdefn(\baseKey),
			
		));
		
		Pdef(\bass, Pbind(
			\instrument, \bass,
			\degree, Pseq([notes_bass_part1, notes_bass_part2], inf),
			\dur, Pseq([times_bass_part1, times_bass_part2]/timeDivisor, inf),
			\scale, Pdefn(\scale),
			\octave, 3,
			\amp, 0.6 * Pdefn(\musicVolume),
			\legato, 0.9,
			\ctranspose, Pdefn(\baseKey),
			
		));
		
		Pdef(\percussion, Pbind(
			\instrument, \percussion,
			\degree, Pseq([notes_perc_part1, notes_perc_part2], inf),
			\dur, Pseq([times_perc_part1, times_perc_part2]/timeDivisor, inf),
			\legato, 0.15,
			\amp, 0.3 * Pdefn(\musicVolume),
		));

	}
	
	playMidiNote { |note, vel|
		var keyboardVolume = ChordtrisPreferences.getPreferences.at(\keyboardVolume).value;
		("playing note" + note + "at volume" + keyboardVolume).postln;
		(instrument: \moog, midinote: note, amp: keyboardVolume, fattack: vel.linlin(0, 127, 0.3, 0.01)).play;
	}
	
	playExplosion {
		var soundVolume = ChordtrisPreferences.getPreferences.at(\soundVolume).value;
		(instrument: \explosion, amp: soundVolume).play;
	}
	
	playPauseSound {
		var soundVolume = ChordtrisPreferences.getPreferences.at(\soundVolume).value;
		(instrument: \pauseSound, amp: soundVolume).play;
	}
	
	playGameOverSound {
		var soundVolume = ChordtrisPreferences.getPreferences.at(\soundVolume).value;
		(instrument: \gameOver, amp: soundVolume).play;
	}
	
	startMusic {
		Pdef(\voice1).play(quant:1);
	  	Pdef(\voice2).play(quant:1);
	  	Pdef(\bass).play(quant:1);
	  	Pdef(\percussion).play(quant:1);
	}
	
	stopMusic {
		Pdef(\voice1).stop;
	  	Pdef(\voice2).stop;
	  	Pdef(\bass).stop;
	  	Pdef(\percussion).stop;
	}
	
	pauseMusic {
		Pdefn(\musicVolume, 0);
		
	}
	
	resumeMusic {
		Pdefn(\musicVolume, ChordtrisPreferences.getPreferences.at(\musicVolume).value);
	}
	
	setContextChord { |chord|
		var scale;
		
		Pdefn(\baseKey, this.chooseBaseKey(chord.baseKey));		
		if(chord.kind == \major) { scale = Scale.major } { scale = Scale.harmonicMinor };
		Pdefn(\scale, scale);
		
	}
	
	chooseBaseKey { |baseKey|
		var newBaseKey;
		
		if(currentBaseKey.notNil)
		{
			var differenceUp = (currentBaseKey - baseKey).abs;
			var differenceDown = (currentBaseKey - (baseKey - 12)).abs;
			
			if(differenceUp < differenceDown)
			{
				newBaseKey = baseKey;
			} {
				newBaseKey = baseKey - 12;
			}
		} {
			newBaseKey = baseKey;
		};
		
		// transpose one octave up/down if we are not within the defined key range
		if(newBaseKey < transposeLowerBound) { newBaseKey = newBaseKey + 12 };
		if(newBaseKey > transposeUpperBound) { newBaseKey = newBaseKey - 12 };		
		//if(currentBaseKey.notNil) {
		//(currentBaseKey + " -> " + newBaseKey).postln; };
		
		currentBaseKey = newBaseKey;
		
		^newBaseKey;
	}
	
}