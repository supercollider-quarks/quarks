BusDict {
	// stores dictionaries of descriptive names for buses
	// and the bus objects themselves
	// the dictionary key is the bus number, obtained from Bus.alloc
	// this class does not create instances;
	// the class itself is the interface
	
	classvar	<audioNames, <audioObjects,
			<controlNames, <controlObjects;
	
	*initClass {
		audioNames = IdentityDictionary.new;
		audioObjects = IdentityDictionary.new;
		controlNames = IdentityDictionary.new;
		controlObjects = IdentityDictionary.new;
	}
	
	*new {	// note: this doesn't create an instance of BusDict
			// it places an entry for the specified server into
			// the classvar dictionaries
		arg server;
		var outbuses;
			// only add if the server is not already represented
		(audioNames.size == 0).if(
			{ true },		// always add if no servers have been init'ed
					// if servers exist, add only if this server is new
			{ audioNames.includesKey(server).not }
		).if({
			audioNames.put(server, IdentityDictionary.new);
			audioObjects.put(server, IdentityDictionary.new);
			controlNames.put(server, IdentityDictionary.new);
			controlObjects.put(server, IdentityDictionary.new);
			
				// put names for hardware outs and ins
			outbuses = server.options.numOutputBusChannels;
			outbuses.do({ arg i;
				audioNames.at(server).put(i, "Output " ++ i.asString);
			});
			server.options.numInputBusChannels.do({ arg i;
				audioNames.at(server).put(i+outbuses,
					"Input " ++ i.asString);
			});
			^true	// return true if server added
		}, { ^false });	// return false if it was already there
	}
		
	*alloc { arg rate = \audio, server, numChannels = 1, name;
		var b;
		b = Bus.alloc(rate, server, numChannels);  // get a bus

			// if server is not currently in BusDict, put it there
		audioNames.at(server).isNil.if({
			BusDict.new(server);
		});
		
		(rate == \audio).if({	// add to audio dictionary
			audioNames.at(server).put(b.index, name);
			audioObjects.at(server).put(b.index, b);
		}, {		// else put in control dictionary
			controlNames.at(server).put(b.index, name);
			controlObjects.at(server).put(b.index, b);
		});

		^b	// send bus object back
	}
	
	*control { arg server, numChannels = 1, name;
		^BusDict.alloc(\control, server, numChannels, name);
	}
	
	*audio { arg server, numChannels = 1, name;
		^BusDict.alloc(\audio, server, numChannels, name);
	}
	
	*at { arg rate = \audio, server, index;	// access bus object by index
		// BusDict.at(\audio, s, 2) == BusDict.audioObjects.at(s).at(2)
		(rate == \audio).if({
			^audioObjects.at(server).at(index)
		}, {
			^controlObjects.at(server).at(index)
		});
	}
	
	*free { arg bus;	// free bus by bus object
		bus.isNil.not.if({
			BusDict.freeAt(bus.rate, bus.server, bus.index);
		});
	}
	
	*freeAt { arg rate = \audio, server, index;	// free bus by index
		(rate == \audio).if({
			index.notNil.if({
					// deallocate bus object if it's not hardware
				(index >= (server.options.numOutputBusChannels +
						server.options.numInputBusChannels)).if({
					audioObjects.at(server).at(index).free;
					audioNames.at(server).removeAt(index);
					audioObjects.at(server).removeAt(index);
				});
			});
		}, {
			controlObjects.at(server).at(index).free;	// deallocate bus object
			controlNames.at(server).removeAt(index);
			controlObjects.at(server).removeAt(index);
		});
	}
	
	*freeAll {		// free all buses
		arg server;	// can free them on just one server, or all w/ nil here
		server.isNil.if({
			audioObjects.do({ arg serv;
				BusDict.freeAll(serv);	// this is OK b/c freeAll(serv) is
									// non-recursive
			});
			BusDict.initClass;			// just to be safe, clean it all up
		}, {
			audioObjects.at(server).do({	// deallocate each bus
				arg bus;
					// probably an unnecessary trap, but there you are...
				bus.isNil.not.if({ bus.free });
			});
			controlObjects.at(server).do({ 
				arg bus;
				bus.isNil.not.if({ bus.free });
			});
			audioNames.removeAt(server);	// remove items from server dict
			audioObjects.removeAt(server);
			controlNames.removeAt(server);
			controlObjects.removeAt(server);
		});
	}
	
	*menuItems { 
			// for send menus; audio only is relevant here, but control is supported
			// index of output array has to correspond to bus number;
			// therefore, multichannel buses are expanded to "bus", "bus.1", "bus.2" etc.
		arg server, rate = \audio;
		var hiBus, lastBus, lastBusChans;
		
		(rate != \control).if({
			hiBus = audioObjects.at(server).keys.maxItem;
				// allow access to all channels of highest bus
			hiBus = hiBus + (audioObjects@(server)@(hiBus)).numChannels;
			lastBus = 0; 
			lastBusChans = 1;
			^Array.fill(hiBus, { 
				arg i;
					// if this busnum isn't registered, 
				(audioNames@(server)@(i)).isNil.if(
						// could be part of multichan bus
					{	(i > (lastBus + lastBusChans - 1)).if(
							{ "unused" },		// no, it isn't
								// yes, so look up previous bus name and give suffix
							{ ((audioNames@(server)@(lastBus)).asString
								++ "." ++ (i - lastBus)); }
						)
					},	// otherwise, it's registered, so remember which one
					{ lastBus = i;
					  lastBusChans = BusDict.at(\audio, server, lastBus);
						// this is needed b/c hardware ins/outs aren't registered
						// as bus objects here
					  lastBusChans = lastBusChans.isNil.if({ 1 },
					  	{ lastBusChans.numChannels });
					  audioNames@(server)@(i) }
				)
			});
		}, {
			hiBus = controlObjects.at(server).keys.maxItem;
			hiBus = hiBus + (controlObjects@(server)@(hiBus)).numChannels;
			^Array.fill(hiBus, { 
				arg i;
				(controlNames@(server)@(i)).isNil.if(
					{ controlNames@(server)@(lastBus) ?? "unused" },
					{ lastBus = i;
					  controlNames@(server)@(i) }
				)
			});
		});
	}
	
	*postServer {		// print list of all buses and names for a server
		arg server;
		(audioNames.at(server).size > 0).if({ // skip if nothing there
			"AUDIO BUSES:".postln;
			audioNames.at(server).postSorted;
		}, {
			"No audio buses.".postln;
		});
		(controlNames.at(server).size > 0).if({
			"\nCONTROL BUSES:".postln;
			controlNames.at(server).postSorted;
		}, {
			"No control buses.".postln;
		});
		^nil
	}
	
	*post {			// print list of all buses, all servers
		audioNames.keys.do({	// iterate over server objects
			arg serv;
			("Server." ++ serv.name ++ ":").postln;
			BusDict.postServer(serv);
			"".postln;
		});
		^nil
	}
	
	*postln {
		^BusDict.post;
	}
	
}