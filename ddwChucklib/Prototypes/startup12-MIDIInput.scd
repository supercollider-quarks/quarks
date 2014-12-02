// this guy interrupts a playing stream to allow midi input
var saveSubType = AbstractChuckArray.defaultSubType;
AbstractChuckArray.defaultSubType = \midiInputWrapper;

PR(\abstractProcess).v.clone({
		// if true, using this as a wrapper will stop the child immediately
		// really what you want it to do is wait for the first MIDI event before overriding
	~event = (eventKey: \dummy);
	~doReplay = false;
	~canWrap = true;
	~replayTimeSpec = BasicTimeSpec(1);
	~parseBuf = true;

	~postRecFunc = #{ |buf, process|
		(buf.properties.tryPerform(\at, \parse) ? ~parseBuf).if({ buf = buf.parse; });
			// output:
		buf.hasQuantizeProperties.if({ buf.quantize },
			{ buf });
	};
}) => PR(\abstractMIDIInput);

PR(\abstractMIDIInput).v.clone({
	~defaultMIDIType = \mel;
	~preparePlay = #{
		~midiNotesOn = IdentitySet.new;
	};
	~getMIDIParser = #{ |socket|
		~parser = MelodyParser(nil, currentEnvironment, socket, ~child.clock)
	};

	~midiCleanup = #{ 
		var	bp = BP(~collIndex), buf, quant;
		bp.unwrap(false);
			// insert your own processing here -- parsing, quantization
		buf = ~postRecFunc.value(~parser.recSocket.buf, bp);
		bp.bindMIDIRecBuf(buf, ~defaultMIDIType);
		quant = ~replayTimeSpec.asTimeSpec;
		bp.clock.schedAbs(
			quant.adjustTimeForLatency(quant.nextTimeOnGrid(bp.clock), bp.leadTime, bp.clock) - 0.05,
			{ bp.stopNow });
		(~doReplay ? false).if({
			bp.clock.schedAbs(quant.adjustTimeForLatency(quant.nextTimeOnGrid(bp.clock),
					bp.leadTime, bp.clock), {
				bp.play(NilTimeSpec.new, doReset:true);
			});
		});
	};
	
	~melNoteOn = #{ |note|
		~child.eventStreamPlayer.stop;
			// save this note -- when a note is released, this goes into that event
			// so that event can check for overlapping notes
		~midiNotesOn.add(note.asFloat);
		~lastMIDINoteOn = note.asFloat;
			// let child process determine how to handle the note
			// by setting length to nil, we tell child process the note will be terminated later
		~preparePlayEvent.value(note);
	};

	~preparePlayEvent = #{ |note|
		~child.event.copy.put(\note, note.copy.length_(nil))
			.put(\midi, true)
			.put(\immediateOSC, true)
			.play;
	};
		// should be able to receive an array of notes
	~melNoteOff = #{ |note|
		~midiNotesOn.remove(note.asFloat);
		~child.event.copy
			.put(\note, note)
			.put(\immediateOSC, true)
			.use({ ~releaseNote.value; });
	};
}, nil, #[\defaultMIDIType]) => PR(\melMIDI);

PR(\melMIDI).v.clone({
	~defaultMIDIType = \adapt;
}, nil, #[\defaultMIDIType]) => PR(\adaptMIDI);

PR(\abstractMIDIInput).v.clone({
	~defaultMIDIType = \ch;
	~preparePlay = #{  currentEnvironment };
	~getMIDIParser = #{ |socket|
		~parser = ChordParser(nil, currentEnvironment, socket, ~child.clock,
			~deltaThresh ? 0.1, ~lengthThresh ? 0.1)
	};
	~chordNoteOn = #{ |note|
		~child.eventStreamPlayer.notNil.if({
			BP(~collIndex).stopNow;
		});
			// let child process determine how to handle the note

			// get additional parameters from child (wrapper) process
			// this assumes child is already playing
		~preparePlayEvent.value(note);
	};
	~preparePlayEvent = #{ |note|
		~child.eventStream.next(~child.event.copy)
			.put(\chNotes, note.mapMode(~child.mode))
			.put(\length, inf)		// child should not cut off on its own
			.put(\midi, false)		// this only works on mapped data
			.play;
	};
		// should be able to receive an array of notes
	~chordNoteOff = #{ |note|
		~child.event.copy.put(\chNotes, note).use({
			~releaseNote.value;
		});
	};
}).import((melMIDI: \midiCleanup)) => PR(\chMIDI);

PR(\melMIDI).v.clone({
	~defaultMIDIType = \mel;	// chord processes place mel into topNote
	~preparePlayEvent = #{ |note|
		var	mode = ~parser.recSocket.buf.properties.tryPerform(\at, \mode) ?? { ~child.mode ? \default };
		~melNoteOff.(note);	// polyphony is not allowed
		~child.eventStream.next(~child.event.copy)
			.put(\delta, 0.1)
			.put(\length, inf)
			.put(\immediateOSC, true)
			.put(\top, (note: note.copy.mapMode(mode), mode: mode))
			.put(\midi, false)
			.play;
	};

		// this one should only stop the stream if releasing the last played note
	~melNoteOff = #{ |note|
		var	currentChord = ~child.child.currentChord, notes;
		currentChord.notNil.if({
			notes = currentChord.lastFitNotes.asFloat
				.unmapMode(currentChord.modeForEvent(~child.lastEvent));
			(~lastMIDINoteOn == note.asFloat).if({
				~child.child.event.copy
					.put(\freq, notes)
					.put(\immediateOSC, true)
					.use({ ~releaseNote.value; });
			});
		});
		~midiNotesOn.remove(note.asFloat);
	};
}) => PR(\topMIDI);

// chord and topnote melody input simultaneously
PR(\chMIDI).v.clone({
	~preparePlayEvent = #{ |note|
		~child.eventStream.next(~child.event.copy)
			.put(\chNotes, note.mapMode(~child.mode))
			.put(\top, note.maxItem.postln.mapMode(~child.mode).postln)
			.put(\length, inf)		// child should not cut off on its own
			.put(\midi, false)		// this only works on mapped data
			.put(\getChord, false)
			.play;
	};

	~midiCleanup = #{ 
		var	bp, buf;
		bp = BP(~collIndex);
		bp.unwrap(false);
			// insert your own processing here -- parsing, quantization
		buf = ~postRecFunc.value(~parser.recSocket.buf, bp);
		bp.bindMIDIRecBuf(buf, \ch);
		bp.bindMIDIRecBuf(buf.copyTopNotes, \mel);
		bp.stopNow;
		bp.play(~replayTimeSpec.applyLatency(bp.latency), doReset:true);
	};
	
}) => PR(\chmelMIDI);

AbstractChuckArray.defaultSubType = saveSubType;
