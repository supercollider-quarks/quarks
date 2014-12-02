
AbstractMIDIControl {
	classvar	<>syncByDefault = true;

	var	<parent, <ccnum, <spec, <destination;
		// syncSign == 0 means no sync needed
		// nil means sync needed, don't know which direction
		// +1 means incoming value is higher than destValue
	var	<syncSign = nil;
	
	controlType { ^nil }	// what kind of control should I prefer?
						// basic socket doesn't care, so it's nil
						// if your class cares, it should have
						// controlType { ^\myType }
						// this depends on your controls as programmed
						// into CCAllocator

	*new { arg chan, num, destination ... args;
		^super.new.prInit(chan.asChannelIndex, num, destination).performList(\init, args)
	}
	
	init { }		// your class should override this
	
	clear { }		// and this

	prInit { arg ch, n, dest;
		destination = dest;
		this.resync;
		parent = MIDIPort.at(ch) ?? { MIDIChannel.new(ch) };
			// did we ask for control by name?
		n.isSymbol.if({ 
			n = parent.ccAllocator.get(n)
		});
			// is it still nil? ask for preferred type?
		n.isNil.if({
			n = parent.ccAllocator.get(this.controlType)
		});
			// if not, settle for anything
		ccnum = n ?? {
			parent.ccAllocator.get
		};
		spec = #[0, 1].asSpec;
		parent.addControl(this);
	}

	free {
		parent.removeControl(this);
		this.clear;
	}

	name { ^this.asString }		// you may override this

	spec_ { arg sp; spec = sp.asSpec }

		// this is for felix, for compatibility with the crucial Interface classes
	value { arg value, divisor;
		this.set(value, divisor);
	}
	
		// if my destination keeps track of its value, get and return it in this method
		// otherwise give nil
	destValue { ^nil }
	
		// for midi controllers pointing to a proxy - need to be notified if the model changed
	destinationChanged {
	}
	
		// this is the default set method for incoming midi messages
	setSync { |value, divisor = 127, num|
		var sync01, testValue;
		((syncSign != 0) and: { (sync01 = this.destValue).notNil }).if({
			sync01 = this.spec.unmap(sync01);
			testValue = value/divisor - sync01;
				// too big, don't send value to dest
			(testValue.abs <= 0.02 or:
			{ syncSign.notNil and: (syncSign != (syncSign = testValue.sign)) }).if({
				syncSign = 0;
				this.set(value, divisor, num);
			});
		}, {
			this.set(value, divisor, num);
		});
	}
	
	resync { syncSign = syncByDefault.if(nil, 0); }
	
	update { |theChanger, args|
		(args.respondsTo(\keysValuesDo) and: { args[\resync] ? true }).if({
			this.resync;
		});
	}
	
	// your class must also implement:
	// active     (if this controller isn't playing anything else specifically, how do I know
	//			I'm still on? See BasicMIDIControl)
	// set		(what I do to respond to the message)
	
		// for CC storage in chucklib
	bindClassName { ^AbstractMIDIControl }
}

BasicMIDIControl : AbstractMIDIControl {
	var <>func;
	
	init { 
		func = destination;	// see BasicMIDISocket for an explanation of this trick
		destination = this;
	}
	
	clear {
		func = nil;
	}
	
	active { ^func.notNil }	// since I'm the destination, I must say if I'm active or not
	
	name { ^"BasicMIDIControl" }
	
	set { arg value, divisor, ccnum; func.value(value, divisor, ccnum) }
}


MIDI2OSCControl : AbstractMIDIControl {
	var	<addr, <>latency, <port, <channel, <>parentSocket;
	
	var	<>baseTime;
	
	init { |argPort, argChannel, argLatency, sock|
		addr = destination;
		port = argPort;	// port should be given as sequential index into MIDIPort.sources
		channel = argChannel;
		latency = argLatency;
		(parentSocket = sock).notNil.if({
			baseTime = parentSocket.baseTime;
		}, {
			"Confirm that the OSCMidiResponder is running on the other machine and .sync this object."
				.warn;
		});
	}
	
	sync {
		parentSocket.notNil.if({
			parentSocket.sync
		}, {
			addr.sendMsg('/OSCMidiSync'/*, port, channel*/);
			baseTime = Main.elapsedTime;
		});
	}
	
	set { |val, divisor, num|
		switch (num)
			{ \wheel } { 
				addr.sendMsg('/OSCMidiBend', Main.elapsedTime - baseTime, latency,
					port, channel, val); }
			{ \kbprs } {
				addr.sendMsg('/OSCMidiTouch', Main.elapsedTime - baseTime, latency,
					port, channel, val); }
			{	
			addr.sendMsg('/OSCMidiControl', Main.elapsedTime - baseTime, latency,
					port, channel, num, val); };
	}
	
	clear {
			// tell the remote responder I'm gone
			// but, if I'm linked to a socket, leave it to the socket to send .free
		parentSocket.isNil.if({
			addr.sendMsg('/OSCMidiFree', port, channel);
		}, {
			parentSocket.removeControl(this);
		});
	}
}

VoicerSusPedal : AbstractMIDIControl {	
	*new { arg chan, num = 64, destination;
		^super.new(chan.asChannelIndex, num, destination)
	}

	set { |val|
		destination.tryPerform(\sustainPedal, val > 0);
	}
	
	active { ^destination.active }
}

MIDIThruControl : AbstractMIDIControl {
	init {
		destination = destination.asChannelIndex;
	}

	set { |val, divisor, num|
		MIDIIn.doControlAction(destination.port, destination.channel, num, val)
	}
	
	active { ^(destination.respondsTo(\port) and: { destination.respondsTo(\channel) }) }
}
