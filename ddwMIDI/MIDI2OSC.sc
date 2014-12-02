
// for networking MIDI

MIDI2OSCSocket : AbstractMIDISocket {
	var	<addr, <latency, <port, <channel, <controllers;
	
	var	<baseTime;
	
	init { |argPort, argChannel, argLatency, doControls = true|
		addr = destination;
		port = argPort;	// port should be given as sequential index into MIDIPort.sources
		channel = argChannel;
		latency = argLatency;
		controllers = List.new;
		doControls.if({
			this.addControl(MIDI2OSCControl(channel, \omni, addr, port, channel,
				latency, this));
		});
		"Confirm that the OSCMidiResponder is running on the other machine and .sync this object."
			.warn;
	}
	
	sync {
		addr.sendMsg('/OSCMidiSync'/*, port, channel*/);
		baseTime = Main.elapsedTime;
		controllers !? { controllers.do({ |c| c.baseTime = baseTime }) };
	}
	
	noteOn { |num, vel|
		addr.sendMsg('/OSCMidiOn', Main.elapsedTime - baseTime, latency,
			port, channel, num, vel);
	}
	
	noteOff { |num, vel|
		addr.sendMsg('/OSCMidiOff', Main.elapsedTime - baseTime, latency,
			port, channel, num, vel);
	}
	
	clear {
			// tell the remote responder I'm gone
		addr.sendMsg('/OSCMidiFree', port, channel);
		controllers.do(_.free);
	}
	
	latency_ { |lat|
		latency = lat;
		controllers.do({ |c| c.latency = lat });
	}
	
	addControl { |cc|
		cc.baseTime = baseTime;		// will crash if it's not a MIDI2OSCControl
		cc.parentSocket = this;
		controllers.add(cc);
	}
	
	removeControl { |cc|
		cc.baseTime = nil;		// will crash if it's not a MIDI2OSCControl
		cc.parentSocket = nil;
		controllers.remove(cc);
	}
}

OSCMidiResponder {
	var	syncResp, noteOnResp, noteOffResp, controlResp, bendResp, touchResp, freeResp,
		baseTime;
	
	*new { |addr| ^super.new.init(addr) }
	
	init { |addr|
		syncResp = OSCresponderNode(addr, '/OSCMidiSync', { |time, resp, msg|
			baseTime = time;
			Post << "MIDI OSC synchronization from " << addr.ip << " is complete.\n";
		}).add;
		
		noteOnResp = OSCresponderNode(addr, '/OSCMidiOn', { |time, resp, msg|
			var	m, t, latency, port, channel, num, vel;
			#m, t, latency, port, channel, num, vel = msg;
			SystemClock.schedAbs(baseTime + t + latency, {
				MIDIIn.doNoteOnAction(MIDIPort.sources[port].uid, channel, num, vel);
			});
		}).add;

		noteOffResp = OSCresponderNode(addr, '/OSCMidiOff', { |time, resp, msg|
			var	m, t, latency, port, channel, num, vel;
			#m, t, latency, port, channel, num, vel = msg;
			SystemClock.schedAbs(baseTime + t + latency, {
				MIDIIn.doNoteOffAction(MIDIPort.sources[port].uid, channel, num, vel);
			});
		}).add;
		
		controlResp = OSCresponderNode(addr, '/OSCMidiControl', { |time, resp, msg|
			var	m, t, latency, port, channel, num, val;
			#m, t, latency, port, channel, num, val = msg;
			SystemClock.schedAbs(baseTime + t + latency, {
				MIDIIn.doControlAction(MIDIPort.sources[port].uid, channel, num, val);
			});
		}).add;
		
		bendResp = OSCresponderNode(addr, '/OSCMidiBend', { |time, resp, msg|
			var	m, t, latency, port, channel, val;
			#m, t, latency, port, channel, val = msg;
			SystemClock.schedAbs(baseTime + t + latency, {
				MIDIIn.doBendAction(MIDIPort.sources[port].uid, channel, val);
			});
		}).add;
		
		touchResp = OSCresponderNode(addr, '/OSCMidiTouch', { |time, resp, msg|
			var	m, t, latency, port, channel, val;
			#m, t, latency, port, channel, val = msg;
			SystemClock.schedAbs(baseTime + t + latency, {
				MIDIIn.doTouchAction(MIDIPort.sources[port].uid, channel, val);
			});
		}).add;
		
		freeResp = OSCresponderNode(addr, '/OSCMidiFree', { |time, resp, msg|
			this.free;
			Post << "MIDI OSC free message received from " << addr.ip << ". Connection terminated.\n";
		}).add;
	}
	
	free {
		[syncResp, noteOnResp, noteOffResp, controlResp, bendResp, touchResp, freeResp]
			.do(_.remove);
		syncResp = noteOnResp = noteOffResp = controlResp = bendResp = touchResp = freeResp
			= nil;
	}
}
