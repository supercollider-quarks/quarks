
// MIDI input parsers
// e.g. a melody sequencer needs raw data, but a topnote sequencer doesn't care
// about grace or chord notes; so, detect and trap

// not a subclass of AbstractMIDISocket because this doesn't need to go into the MIDI hierarchy

MelodyParser {
	// pass MIDI as is to sequence
	// destination var should already hold sequencer

	var	<destination, <recSocket;

	*new { |chan, dest, recSocket, clock|
		^super.new.prInit(chan, dest, recSocket, clock)
	}
	
	prInit { |chan, dest, recsocket, clock|
		recSocket = (recsocket ?? { MIDIRecSocket(chan, clock) })
			.addResponder(this);
		destination = dest;
	}

		// this class receives SequenceNotes, not raw midi data
	noteOn { |note|
		destination.melNoteOn(note)
	}
	
	noteOff { |note|
		destination.melNoteOff(note)
	}
	
	midiCleanup { ^destination.midiCleanup }
}

ChordParser : MelodyParser {
	// should not talk to destination until a complete chord is received
	// piggybacks off of a MIDIRecSocket, since it already does the bookkeeping
	// chord notes are assumed to be longer than deltaThresh
	// therefore, chord notes will be in the recsocket's unresolvedNotes array
	// after deltaThresh passes
	
	// fails on staccato chord. I will probably rewrite this.
	
	var	<>deltaThresh, <>lengthThresh,
		<chordNotes, <unresolvedCount, <chordSent = false;

	*new { |chan, dest, recSocket, clock, deltaThr, lengthThr|
		^super.new(chan, dest, recSocket, clock).init(deltaThr, lengthThr)
	}
		
	init { |deltaThr, lengthThr|
		deltaThresh = deltaThr ? 0.2;	// in seconds, not beats
		lengthThresh = lengthThr ? 0.2;
		chordNotes = [];
		unresolvedCount = 0;
	}
	
	noteOn { |note|
			// if a note is played between this one and now + deltaThresh, still building
			// the chord; otherwise (hash unchanged), send it to the destination
		var	comparisonKey;
		chordNotes = chordNotes.add(note);
		unresolvedCount = unresolvedCount + 1;
//		unresolvedNotes = unresolvedNotes.add(note);
		comparisonKey = chordNotes.hash;
//[\noteOn, unresolvedCount, comparisonKey, chordNotes].postcs;
			// or, if the chord was sent and another note comes in while holding,
			// update dest
		(chordSent and: (chordNotes.size > 0)).if({
//"chord sent, still notes holding".postln;
			destination.chordNoteAdd(note)
		}, {
			SystemClock.sched(deltaThresh, {
					// if these are ==, nothing happened so assume the chord is finished
//[\noteOn_checking, unresolvedCount, comparisonKey, chordNotes.hash].postcs;
// need to test chordSent here
				(comparisonKey == chordNotes.hash).if({
//[\sending].postln;
					this.dropShortNotes;
					destination.chordNoteOn(chordNotes);
					chordSent = true;
				});	// end if
			});	// end sched
		});	// end if
	}
	
	noteOff { |note|
		var temp;
		((unresolvedCount = unresolvedCount - 1) == 0).if({
			chordSent.if({
				destination.chordNoteOff(chordNotes);
			}, {
				this.dropShortNotes;
				destination.chordNoteOn(chordNotes);
				temp = chordNotes.copy;
				SystemClock.sched(lengthThresh, {
					destination.chordNoteOff(temp);
				});
			});
				// reset
			chordNotes = [];
			chordSent = false;
		});
	}
	
		// maybe there were grace notes in the chord I shouldn't send to destination
	dropShortNotes {
			// but only if there's at least 1 long note
		chordNotes.detect({ |item| item.length >= lengthThresh }).notNil.if({
			chordNotes = chordNotes.select({ |item| item.length >= lengthThresh });
		});
	}
		
}

