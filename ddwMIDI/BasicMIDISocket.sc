
AbstractMIDISocket {
	var <parent, <destination;
	
	*new { arg chan, destination ... args;
		^super.new.prInit(chan.asChannelIndex, destination).performList(\init, args)
	}
	
	init { }		// your class should override this
	
	clear { }		// and this

	prInit { arg ch, dest;
		destination = dest;
		parent = MIDIPort.at(ch) ?? { MIDIChannel.new(ch) };
		parent.add(this);
	}

	free {
		parent.remove(this);
		this.clear;
	}
	
	enable {
		parent.enableSocket(this);
	}
	
	disable {
		parent.disableSocket(this);
	}
	
	// your class must also implement:
	// destination  (returns the thing being played by this socket, or this if appropriate)
	// active  (if destination is this: return true if the responder is still valid)
	// noteOn  (take action)
	// noteOff (take action)
	
}

BasicMIDISocket : AbstractMIDISocket {
	var <>onPlayer, <>offPlayer;
	
	init { arg offfunc;
		onPlayer = destination;  // since destination is this, I simplify the usual syntax:
		destination = this;	  // new(channel, dest, args)  -- so you can say instead:
		offPlayer = offfunc;	  // new(channel, on_func, off_func)
	}

	clear {
		onPlayer = offPlayer = nil;
	}
	
	active { ^onPlayer.notNil }	// since I'm the destination, I must say if I'm active or not
	
	noteOn { arg note, vel; onPlayer.value(note, vel) }
	
	noteOff { arg note, vel; offPlayer.value(note, vel) }
	
}

MIDIRecSocket : AbstractMIDISocket {
	// allows you to record midi note streams into a buffer (client-side)
	// controllers are ignored at present, maybe I'll add that later
	// this object should exist in the midi hierarchy only while it's recording
	// when you create it, it's on
	// when you do .stopRecord, the socket is freed

	var	<active,	// am I recording?
		<buf,
		<unresolvedNotes,	// noteOn places the note here; noteOff resolves it and puts it into buf
		<clock,
		<>startTime,	// time when recording was commenced
		<>lastNoteTime,
		<moreResponders;	// other objects may want to use the timing data gathered here
	
	init { |properties|
		clock = destination ? TempoClock.default;
		destination = this;
		active = true;
		moreResponders = [];
		this.initRecord(properties);
	}
	
	clear {
		active = false;
		moreResponders.do({ |resp|
			resp.midiCleanup;
		});
	}
	
	addResponder { |... responders|
		moreResponders = moreResponders.addAll(responders);
	}
	
	removeResponder { |... responders|
		moreResponders.removeAll(responders);
	}
	
	removeAllResponders { moreResponders = []; }
	
	initRecord { arg properties;
		buf = MIDIRecBuf("buf" ++ Main.elapsedTime.round, properties: properties ?? { () });
		unresolvedNotes = Array.new;
	}
	
	stopRecord {
		var timeTemp, noteToStop;
		(buf.size > 0).if({
			buf.stopRecTime = (timeTemp = clock.elapsedBeats) - startTime;
				// if stop was received before last noteOff, must fill in note lengths
				// for any notes not yet cut off
			buf.notes.last.dur = timeTemp - lastNoteTime;
				// noteToStop only needs freq to release
			noteToStop = SequenceNote(unresolvedNotes.collect({ |n| n.freq }));
			moreResponders.do({ |resp|
				resp.noteOff(noteToStop);
			});
			unresolvedNotes.do({ |note|
				note.length = timeTemp - note.length;
			});
			startTime = nil;
				// need to do this before freeing
				// in case there's sth in moreResponders
			(buf.properties.tryPerform(\at, \parse) ? false).if({ buf = buf.parse }, { buf });		});
		this.free;
		^buf
	}
	
	noteOn { arg num, vel;
		var timeTemp, new;
		timeTemp = clock.elapsedBeats;
		startTime.isNil.if({ startTime = timeTemp });  // 1st note should be at 0
			// fill in previous note's delta
		(buf.notes.size > 0).if({
			buf.notes.last.dur = timeTemp - lastNoteTime;
		});
		unresolvedNotes = unresolvedNotes.add(new = SequenceNote(num,
				// cannot assign new lastNoteTime until after previous delta is recorded
			0, lastNoteTime = timeTemp, vel/127));
		buf.add(new);
			// use the SequenceNote object, not the raw MIDI data
		moreResponders.do({ |resp| resp.noteOn(new) });
	}
	
	noteOff { arg num, vel;
		var note;
			// search for first unresolved note, with this note num
		(unresolvedNotes.size > 0).if({
			note = unresolvedNotes.detect({ arg x; x.freq == num });
		});
		note.notNil.if({
			unresolvedNotes.remove(note);
				// length now holds the note on time, so subtraction gives the length
			note.length = clock.elapsedBeats - note.length;
			moreResponders.do({ |resp| resp.noteOff(note) });
		});
	}
}

MIDIThruSocket : AbstractMIDISocket {
		// preprocessing before passing thru
	var	<>noteOnProc = nil, <>noteOffProc = nil;
	init { |noteOnFunc, noteOffFunc|
		destination = destination.asChannelIndex;
		noteOnProc = noteOnFunc;
		noteOffProc = noteOffFunc;
	}
	
	noteOn { |num, vel|
		var	num2, vel2;
		noteOnProc !? { #num2, vel2 = noteOnProc.value(num, vel).asArray };
		MIDIIn.doNoteOnAction(destination.port, destination.channel, num2 ? num, vel2 ? vel)
	}
	
	noteOff { |num, vel|
		var	num2, vel2;
		noteOffProc !? { #num2, vel2 = noteOffProc.value(num, vel).asArray };
		MIDIIn.doNoteOffAction(destination.port, destination.channel, num2 ? num, vel2 ? vel)
	}
	
	active { ^(destination.respondsTo(\port) and: { destination.respondsTo(\channel) }) }
}
