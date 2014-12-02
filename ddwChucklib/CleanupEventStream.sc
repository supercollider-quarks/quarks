
// these are near-identical copies of some of the stream subclasses
// people keep making changes to streams that break compatibility,
// so this is to make sure my stuff keeps working

// most of these are deprecated now, though I still use HJHCleanupStream

PauseStreamHJH : Stream
{
	var <stream, <originalStream, <clock, <nextBeat, <streamHasEnded=false;
	var <isWaiting = false;
	
	*new { arg argStream, clock; 
		^super.newCopyArgs(nil, argStream, clock ? TempoClock.default) 
	}
	
	isPlaying { ^stream.notNil }
	
	play { arg argClock, doReset = (false), quant=0.0;
		if (stream.notNil, { "already playing".postln; ^this });
		if (doReset, { this.reset });
		clock = argClock ? clock ? TempoClock.default;
		streamHasEnded = false;
		stream = originalStream; 
		isWaiting = true;	// make sure that accidental play/stop/play sequences
						// don't cause memory leaks
		clock.play({
			if(isWaiting and: { nextBeat.isNil }) {
				clock.sched(0, this);
				isWaiting = false;
			};
			nil
		}, quant);
		^this
	}
	reset { ^originalStream.reset }
	stop {
		stream = nil;
		isWaiting = false;
	}
	removedFromScheduler { this.stop }
	wasStopped { ^streamHasEnded.not and: { stream.isNil } }
	
	pause {
		stream = nil;
		isWaiting = false;
	}
	resume { arg argClock, quant=1.0; 
		^this.play(clock ? argClock, false, quant) 
	}
	
	refresh {
		stream = originalStream
	}

	start { ^this.play(clock, true) }
		
	stream_ { arg argStream; 
		originalStream = argStream; 
		if (stream.notNil, { stream = argStream; streamHasEnded = argStream.isNil; });
	}

	next { arg inval; 
		var nextTime = stream.next(inval);
		if (nextTime.isNil) { 
			streamHasEnded = stream.notNil; stream = nextBeat = nil }
			{ nextBeat = inval + nextTime };	// inval is current logical beat
		^nextTime
	}
	awake { arg beats, seconds, inClock;
		stream.beats = beats;
		^this.next(beats)
	}
}

HJHCleanupStream : Stream {
	var <stream, <>cleanup, <>reuseCleanupFunc;
	
	*new { arg stream, cleanup, reuseCleanupFunc = false;
		^super.newCopyArgs(stream, cleanup, reuseCleanupFunc)
	}
	next { arg inval;
		var outval;
		inval.notNil.if({
			outval = stream.next(inval);
		});
		if (outval.isNil) {
			try {
				cleanup.value(this, inval);
				reuseCleanupFunc.not.if({ cleanup = nil });
			} { |error|
				error.reportError;
				"CleanupStream cleanup failed. Continuing to stop stream.".warn;
			}
		}
		^outval
	}
	reset {
		stream.reset
	}
}

BlockableEventStreamPlayer : EventStreamPlayer {
	var	<>status = \stopped;
	
	removedFromScheduler { | freeNodes = true |
		if(status != \blocked) {
			super.removedFromScheduler(freeNodes)
		} {
			nextBeat = nil;
			isWaiting = false;
			cleanup.terminate(freeNodes);	
			this.changed(\blocked);
		};
	}
	
	prStop {
		status = \stopped;
		super.prStop;
	}
	
	play { |argClock, doReset = false, quant|
		status = \playing;
		super.play(argClock, doReset, quant);
	}
	
	pause {
		status = \blocked;
		cleanup.terminate;
		stream = nextBeat = nil;
		isWaiting = false;
		this.changed(\userBlocked);
	}
	
	block { this.pause }
}

PausableEventStreamPlayer : PauseStreamHJH {
	var <>event, <>muteCount = 0, <>cleanup;

	*new { arg stream, event;
		^super.new(stream).event_(event ? Event.default);
	}

	stop { 
		stream = nextBeat = nil; isWaiting = false; 
		cleanup.do { | c | c.value(event) }; 
		cleanup.clear;
	 }

	mute { muteCount = muteCount + 1; }
	unmute { muteCount = muteCount - 1; }

	next { arg inTime;
		var nextTime;
		var outEvent;
		outEvent = stream.next(event);
		case { outEvent.isNil } {
			streamHasEnded = stream.notNil;
			this.stop;
			^nil
		}
		{ outEvent.respondsTo(\keysValuesDo) } {
			outEvent[\addToCleanup].do { | f | cleanup.add(f) };
			outEvent[\removeFromCleanup].do { | f | cleanup.remove(f) };
			
			if (muteCount > 0) { outEvent.put(\freq, \rest) };
			outEvent.play;
			if ((nextTime = outEvent.delta).isNil) { 
			stream = nil };
		}
		{ outEvent.isNumber } {
			nextTime = outEvent
		} // default:
		{ stream = nextBeat = nil; ^nil };

		nextBeat = inTime + nextTime;	// inval is current logical beat
		^nextTime
	}
	
	asEventStreamPlayer { ^this }
}
