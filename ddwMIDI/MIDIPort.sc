
MIDIPort {
	classvar	<>autoFreeSockets = true;
	
	classvar <initialized = false,
			<>numPorts = 2;	// user should change this to reflect her setup
	
	classvar <ports, <sources;	// sources is array of uids to access ports
							// I need a separate ref b/c I might be spoofing MIDI sources
	
	classvar	<protoCCAllocators;
	classvar	<>onInitAll;		// user hook to initialize CCAllocator prototypes
	
	var		<channels, <src;
	
	*new { arg src;
		^super.new.init(src)
	}
	
	init { arg s;	// initialize a single MIDIPort
		src = s;
		channels = Array.newClear(17);	// space for midi channels--last entry is an omni respdr
	}
	
	at { arg chan;
		^channels.at(chan.asMIDIChannelNum)
	}
	
	uid { ^src.uid }
	
	removeAt { arg chan;
		var c;
		chan = chan.asMIDIChannelNum;
		(c = channels.at(chan)).notNil.if({
			channels.put(chan, nil);
			c.free(false);
		});
	}

	search { arg v;
		var temp;
		v.isNil.if({ ^nil });
		channels.select({ arg c; c.notNil }).do({ arg c;
			temp = c.search(v);	// returns socket object or nil
			temp.notNil.if({ ^temp });
		});
		^nil
	}

	resetAll {
		this.init(src);	// clear all channel objects so new ones can be created as needed
	}
	
	*resetAll {
		ports.do({ arg p; p.resetAll });
	}

		// prepare MIDIIn for use
		// sourceInports: in MIDIIn.connect(inport, device), inport need not be the same
		// index as sources[i] -- this lets you specify how you want them
		// sources[0..2] -- you can do MIDIPort.init([2, 0, 1]) and you'll get
		// MIDIIn.connect(0, MIDIClient.sources[2])
		// MIDIIn.connect(1, MIDIClient.sources[0])
		// MIDIIn.connect(2, MIDIClient.sources[1])
	*init { |sourceInports|
		var srctemp;
		var randsrc;
			// now set up MIDIIn
			// if a MIDI message is on a channel not defined, it will be passed
			// to whatever function was in MIDIIn before initialization
		initialized.not.if({
			MIDIClient.initialized.not.if({
				MIDIClient.init;	// open the ports
			});

			numPorts = max(numPorts, MIDIClient.sources.size + 1);

				// not enough inports specified, fill with consecutive integers
				// if 3 sources and you supply [1], result is [1, 0, 2]
			sourceInports = sourceInports ? Array.new;
			(sourceInports.size < numPorts).if({
				sourceInports = sourceInports ++
					((0..(numPorts-1)).reject({ |x| sourceInports.includes(x) }));
			});

			sources = Array.new(numPorts);
			sourceInports.do({ arg sourceIndex, i;
				var	port = this.portForSource(MIDIClient.sources.tryPerform(\at, sourceIndex).tryPerform(\uid));
				if(port.src.device != "fake") {
					MIDIIn.connect(i, port.src);  // connect it
				};
			});
			srctemp = MIDIEndPoint("All devices", "All devices", 0x80000001);
			sources = sources.add(srctemp);
			ports.put(0x80000001, MIDIPort(srctemp));
			numPorts = max(numPorts, ports.size);
			
			NoteOnResponder({ arg src, chan, note, vel;
				if(ports[src].isNil) {
					// true == post warning
					// warning should not post repeatedly
					// unless the MIDI subsystem replies with random uids for each message!
					this.portForSource(src, true);
				};
				[0x80000001, src].do { |src|
					ports.at(src).at(chan).notNil.if({ ports.at(src).at(chan).noteOn(note, vel); });
					ports.at(src).at(16).notNil.if({ ports.at(src).at(16).noteOn(note, vel); });
				};
			});
			NoteOffResponder({ arg src, chan, note, vel;
				if(ports[src].isNil) {
					this.portForSource(src, true);
				};
				[0x80000001, src].do { |src|
					ports.at(src).at(chan).notNil.if({ ports.at(src).at(chan).noteOff(note, vel); });
					ports.at(src).at(16).notNil.if({ ports.at(src).at(16).noteOff(note, vel); });
				};
			});
			CCResponder({ arg src, chan, num, value;
				if(ports[src].isNil) {
					this.portForSource(src, true);
				};
				[0x80000001, src].do { |src|
					ports.at(src).at(chan).notNil.if({ 
						ports.at(src).at(chan).control(num, value);
					});
					ports.at(src).at(16).notNil.if({ ports.at(src).at(16).control(num, value); });
				};
			});
			BendResponder({ arg src, chan, bend;
				if(ports[src].isNil) {
					this.portForSource(src, true);
				};
				[0x80000001, src].do { |src|
					ports.at(src).at(chan).notNil.if({ ports.at(src).at(chan).bend(bend); });
					ports.at(src).at(16).notNil.if({ ports.at(src).at(16).bend(bend); });
				};
			});
			TouchResponder({ arg src, chan, pressure;
				if(ports[src].isNil) {
					this.portForSource(src, true);
				};
				[0x80000001, src].do { |src|
					ports.at(src).at(chan).notNil.if({ ports.at(src).at(chan).touch(pressure); });
					ports.at(src).at(16).notNil.if({ ports.at(src).at(16).touch(pressure); });
				};
			});
			initialized = true;
			onInitAll.value(this);
		});
		
	}
	
	*install { arg mchan;
		// places a voicerMIDIChannel on the midi channel
		
		initialized.not.if({ this.init });
		
		mchan = mchan.asMIDIChannelNum;

			// first remove old voicer in this slot
		ports.at(mchan.channel.port).free;	// kill sockets & synths
		
			// replace it
		ports.at(mchan.channel.port).channels.put(mchan.channel.channel, mchan);
		
	}
	
	*at { arg chan;
		this.init;	// must initialize ports dictionary before doing this
		chan = chan.asChannelIndex;
		^ports.at(chan.port).tryPerform(\at, chan.channel)
	}
	
	*removeAt { arg chan;
		var c;
		chan = chan.asChannelIndex;
		ports.at(chan.port).removeAt(chan.channel);
	}
	
		

	*initClass {	// ports dict must exist before first use
		ports = IdentityDictionary.new;
		protoCCAllocators = IdentityDictionary.new;
	}
	
	*search { arg v;
		var temp;
		v.isNil.if({ ^nil });
		ports.do({ arg p;
			temp = p.search(v);	// returns socket object or nil
			temp.notNil.if({ ^temp });
		});
		^nil
	}
	
	*update {		// channel updating clears sockets connected to inactive objects
		autoFreeSockets.if({
			ports.do({ arg p;
				p.channels.do({ arg c;
					c.notNil.if({ c.update });
				});
			});
		});
	}
	
	*putProtoCCAlloc { |port, alloc|
		this.init;
		sources[port].notNil.if({
			port = sources[port].uid;
		});
		protoCCAllocators[port] = alloc;
	}

	*newccAllocForPort { |port|
		this.init;
		sources[port].notNil.if({
			port = sources[port].uid;
		});
		^protoCCAllocators[port].deepCopy
	}

	*portForSource { |uid, warn = false|
		var	srctemp, port;
		if(ports[uid].isNil) {
			srctemp = MIDIClient.sources.tryPerform(\detect, { |src| src.uid == uid })
				?? { MIDIEndPoint.new("fake", "midiport", uid ?? { 1000000000.rand }) };
			sources = sources.add(srctemp);
			port = MIDIPort.new(srctemp);
			ports.put(srctemp.uid, port);
			numPorts = max(numPorts, ports.size);
			if(warn ? false) {
				"Could not locate MIDIPort with uid %. Created a dummy instance at port index %."
				.format(uid, sources.size - 1).warn;
			};
			^port
		} { ^ports[uid] }
	}
}

MIDIChannelIndex {
	classvar	<>defaultPort = 0;
	var	<port, <>channel;
	
		// expects MIDIPort, MIDIEndPoint or integer uid for "port" argument
		// other classes can work too if they support the uid method
	*new { arg port, channel;
		^super.newCopyArgs(port.tryPerform(\uid) ? port, channel ? 0)
	}
	
	asChannelIndex { ^this }
	
	asString {
		var src;
			// search for MIDIEndPoint with this uid
			// my 'port' variable might not be a macosx uid
			// so it isn't enough to do MIDIPort.ports[port]
		src = MIDIPort.ports.values.collect({ arg p; p.src })
			.select({ arg src; src.uid == port }).at(0);

		^"MIDIChannelIndex("
			++ src.isNil.if(
				{ port.asString }, 
				{ "'" ++ src.device ++ "' : '" ++ src.name ++ "'" })
			++ ", " ++ channel ++ ")"
	}
	
	asShortString {
		^"[" ++ (MIDIPort.sources.collect(_.uid).indexOf(port)) ++ ", " ++ channel ++ "]"
	}
	
	storeOn { |stream|
		stream << "MIDIChannelIndex(" << port << ", " << channel << ")"
	}
	
	== { |that|
		^(that.class == this.class) and:
			{ this.port == that.port and: (this.channel == that.channel) }
	}
	
		// I need to use this as a key for a dictionary; regular hash doesn't work for this
		// simpler algorithm, will still produce unique values (??)
	hash { ^(port%1024) * 16 + channel }
}

MIDIChannel {
	// handles all midi for one channel
	// multiple Sockets can be registered on the channel
	// eventually sockets will be written other than VoicerMIDISocket
	// to handle many situations. You can write your own socket to handle your specific needs.
	
	var	<channel,
		<sockets, 	// for keysplits
		<disabledSockets,
		<ccAllocator,
		<ccResponders;
	
	var	<>controlNums, <>controlTypes, <>reservedTypes;  // saved for re-initing the channel

		// should only be called by VoicerMIDISocket or other socket
	*new { arg chan, vsocket, controlNums, controlTypes, reservedTypes;			// channel, voicerSocket, initial values for CCAllocator
		^super.new.init(chan, vsocket, controlNums, controlTypes, reservedTypes);
	}
	
	init { arg chan, v, cNums, cTypes, rTypes;
		sockets = v.notNil.if({ Array.with(v) }, { Array.new });  // array for sockets
		channel = (chan ? channel).asChannelIndex;
			// 128 reserved for \pb, 129 for \touch, 130 for \omni
		ccResponders = Array.newClear(131);
		controlNums = cNums ? controlNums;
		controlTypes = cTypes ? controlTypes;
		reservedTypes = rTypes ? reservedTypes;
		controlNums.notNil.if({
			ccAllocator = CCAllocator.new(controlNums, controlTypes, reservedTypes)
		}, {
			ccAllocator = MIDIPort.newccAllocForPort(channel.port)
				?? { CCAllocator.new(controlNums, controlTypes, reservedTypes) };
		});
		MIDIPort.install(this)
	}
	
	add { arg vsocket;
		var lowtemp, hitemp, i;
		sockets = sockets.add(vsocket);
	}
	
	remove { arg vsocket;
		^sockets.remove(vsocket);
	}
	
	removeAt { arg i;
		^sockets.removeAt(i);
	}
	
	addControl { arg cc;	// a MIDIControllerSocket
		var i;
		switch(cc.ccnum.tryPerform(\type))
			{ \pb } { i = 128 }
			{ \touch } { i = 129 }
			{ \omni } { i = 130 }
			{ i = cc.ccnum.value };
			
		ccResponders.at(i).isNil.if({
			ccResponders.put(i, CCRespGroup.new(this, cc))
		}, {
			ccResponders.at(i).add(cc)
		});
	}
	
	removeControl { arg cc;
		var r, i;
		switch(cc.ccnum.tryPerform(\type))
			{ \pb } { i = 128 }
			{ \touch } { i = 129 }
			{ \omni } { i = 130 }
			{ i = cc.ccnum.value };

		((r = ccResponders.at(i)).notNil).if({
			r.remove(cc);
				// if last socket was freed for this controller, make it available in the alloc'r
			(r.size == 0).if({
				ccAllocator.free(cc.ccnum)
			});
		});
	}
	
	searchControl { arg dest;
		var cc, temp;
		dest.isNil.if({ ^nil });
		ccResponders.do({ arg r;
			temp = r.search(dest);
			temp.notNil.if({ cc = temp });
		});
		^cc
	}

	free { arg callPortRemove = true;
		sockets.do({ arg v; v.free });
		ccResponders.do({ arg r; r.free });
		callPortRemove.if({
			MIDIPort.removeAt(channel);
			sockets = nil;
			ccResponders = nil;
			ccAllocator = nil;		// this obj is now dead
		}, {
			this.init(channel)		// reinit for further use
		});
	}
	
	search { arg player;
		var temp;
		temp = sockets.collect({ arg v; v.destination }).indexOf(player);
		temp.isNil.if({ ^nil }, { ^sockets.at(temp) });
	}
	
	update {
		sockets.copy.do({ arg sock;
			(sock.destination.tryPerform(\active) ? true).not.if({
				sock.free;
				this.remove(sock);
			});
		});
		ccResponders.do({ arg r;
			r.notNil.if({
				r.update;
			});
		});
	}			

	noteOn { arg note, vel;
				// each voicerSocket knows whether to respond (keysplit)
		sockets.do({ arg v; v.noteOn(note ? 69, vel ? 0) });  // default = A440
	}
	
	noteOff { arg note, vel;
		sockets.do({ arg v; v.noteOff(note, vel) });
	}
	
	control { arg num, value;
		var cc;
		(cc = ccResponders.at(num)).notNil.if({
			cc.setSync(value, 127, num);
		});
			// is there an omni control responder?
		(cc = ccResponders.at(130)).notNil.if({
			cc.setSync(value, 127, num);
		});
	}
	
	bend { arg b;
		ccResponders.at(128).notNil.if({ ccResponders.at(128).setSync(b, 16384) });
		ccResponders[130].notNil.if({ ccResponders[130].setSync(b, 16384, \wheel) });
	}
	
	touch { arg pr;
		ccResponders[129].notNil.if({ ccResponders[129].setSync(pr); });
		ccResponders[130].notNil.if({ ccResponders[130].setSync(pr, 127, \kbprs) });
	}
	
	asChannelIndex { ^channel }
	
	enableSocket { arg ... sock;
		var indices;
			// get indices in reverse order
		indices = sock.flat.collectIndicesFromArray(disabledSockets.asArray)
			.sort({ arg a, b; a > b });
		indices.do({ arg i;
			sockets = sockets.add(disabledSockets.removeAt(i));
		});
	}
	
	disableSocket { arg ... sock;
		var indices;
			// get indices in reverse order
		indices = sock.flat.collectIndicesFromArray(sockets).sort({ arg a, b; a > b });
		indices.do({ arg i;
			disabledSockets = disabledSockets.add(sockets.removeAt(i));
		});
	}
	
	soloSocket { arg sock;
		this.disableSocket(sockets.copy.reject({ arg x; x != sock }));
		this.enableSocket(sock);
	}
	
	enableAllBut { arg sock;
		this.enableSocket(disabledSockets.copy.reject({ arg x; x != sock }));
		this.disableSocket(sock);
	}

}

