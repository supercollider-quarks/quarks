// requires SimpleMIDIFile.sc from wslib
 
+ SimpleMIDIFile {
	
	playNotesWithFunction { |function, channel, track, clock, doneAction|
		// function is called every time a note is played
		// passed arguments are:
		//    noteNr, velo, dur, upVelo, track, channel, absTime, lastTime, counter
		
		var thisCopy, notes, routine;			
		notes = this.copy.timeMode_( \seconds )
			.asNoteDicts( channel, track )
			.sort({ |a,b| a.absTime <= b.absTime });
		clock = clock ? SystemClock;
		
		function = function ? { |... args|  // monitoring func for default
				"note played:".postln;
				["noteNr", "velo", "dur", "upVelo", "tr", "ch", "absTime", "lastTime", "i"]
					.do({ |item, i| ("\t" ++ item ++ ": " ++ args[i]).postln;});
				"".postln;
			}; 
			
		doneAction = doneAction ? { "ended".postln; };
		routine = Task({
			var lastTime;
			lastTime = 0;
			notes.do({ |event, i|
				var synth;	
				(event.absTime - lastTime).wait;
				function.value( event.note, event.velo, event.dur, event.upVelo, 
						event.track, event.channel, event.absTime, lastTime, i );
				lastTime = event.absTime;
				});
			doneAction.value;
			}, clock);
			
		^routine.start;
		}		
	
	playWithSynth { |defName = "default", maxlevel = 0.5, minlevel = 0, 			addArgs, addAction = \addToHead, channel, track, target|
			// should be self-killing synth
			var function;
			function = { |noteNr, velo, dur, upVelo, tr, ch, absTime, lastTime, i|
				var synth;	
				synth = Synth( defName, [ 
					freq: noteNr.midicps, 
					level: ((velo / 127) * (maxlevel - minlevel)) + minlevel,
					amp: ((velo / 127) * (maxlevel - minlevel)) + minlevel,
					sustain: dur ] ++ addArgs, target, addAction  ); 
				("played event (" ++ noteNr.midiname ++ ")" ).postln;
				};
			^this.playNotesWithFunction( function, channel, track );
		}
	
	}
		