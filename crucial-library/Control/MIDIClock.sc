
MIDIClockOut {

	var <>sched,<>port,click,<isPlaying = false;

	*new { arg deviceName,portName,tempoClock;
		var port;
		if(deviceName.notNil and: portName.notNil,{
			port = MIDIOut.newByName(deviceName,portName);
		},{
			port = MIDIOut.new(0,MIDIClient.destinations[0].uid);
		});
		if(port.isNil,{
			Error("Device not found " + deviceName + portName).throw;
		});
		^super.newCopyArgs(BeatSched.new(tempoClock:tempoClock),port)
	}

	play {
		var delta,songBeat = 0.0,quantize=4.0;
		if(isPlaying,{
			"MIDIClockOut is already playing".inform;
			^this
		});
		sched.xqsched(quantize,{
			sched.beat = songBeat;
			this.start;
		});
	}
	start {
		CmdPeriod.add(this);
		click = 0;
		sched.beat = 0.0;
		port.songPtr(sched.beat);
		port.start;
		isPlaying = true;
		this.next;
	}
	next {
		// calculate the beat delta to the logical time we should be at for the
		// number of clocks we have sent.  this means we don't slip due to rounding errors
		// or scheduling slippage.
		var currentBeat,currentClick,delta,beat;
		currentBeat = (click / 24).floor;
		currentClick = click - (currentBeat * 24);

		beat = sched.beat;
		delta = (currentBeat + ((currentClick + 1) / 24.0)) - beat;

		/*(
			currentBeat:  currentBeat,
			currentClick:  currentClick,
			tempo: Tempo.default.bpm,
			beat:  beat, // current actual beat
			delta: delta
		).debug;*/

		port.midiClock;

		sched.xsched( delta,{
			click = click + 1;
			this.next;
		});
	}
	stop {
		sched.clear;
		port.stop;
		isPlaying = false;
		CmdPeriod.remove(this);
	}

	// player support
	// if in a player structure, we will receive didStop
	prepareToBundle { arg group,b;
		b.addMessage( this,\start);
	}
	stopToBundle { arg b;
		b.addMessage( this,\stop);
	}
	synthArg { ^nil }
	/*songPtr { arg gotoBeat,atTime;

	}*/
	gotoBeatAtBeat { arg beat,atBeat;
		var delta = atBeat - sched.beat;
		sched.xsched(delta,{
			sched.beat = beat;
			port.songPtr(beat);
			click = (beat * 24).asInteger; // not tested
			this.next;
		})
	}

	cmdPeriod {
		this.stop;
	}
	destroy {
		CmdPeriod.remove(this);
	}
}





