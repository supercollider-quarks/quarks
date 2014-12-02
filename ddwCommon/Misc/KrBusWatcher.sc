
// polls a bus periodically and updates the bus's dependants
// useful for updating guis representing a kr bus for server-side changes

// does for kr buses what nodewatcher does for synths/groups
// -- responds to c_set and c_setn server messages
// -- also runs an alive thread to send c_get for every registered bus
//		-- user must start the alive thread

// h. james harkins -- jamshark70@dewdrop-world.net

KrBusWatcher : NodeWatcher {
	classvar	<>allBusWatchers;
	
	var	<aliveThread, <>updateFreq = 2;

	*initClass {
		allBusWatchers = IdentityDictionary.new;
		CmdPeriod.add(this);
	}
	
	*cmdPeriod { allBusWatchers.do(_.clear) }

	*newFrom { arg server;
		var res;
		res = allBusWatchers.at(server.name);
		if(res.isNil, {
			res = this.new(server).server_(server);  // better OOP style anyway
			res.start;
			allBusWatchers.put(server.name, res) 
		});
		^res
	}
	
	clear {
		nodes = IdentityDictionary.new;
		this.stopAliveThread;
	}
	
	register { |bus|
		if(server.serverRunning.not, { nodes.removeAll; ^this });
		if(isWatching, {
			bus = bus.asBus;
			nodes.put(bus.index, bus);
			this.startAliveThread;
		});
	}

	unregister { |bus|
		bus = bus.asBus;
		nodes.removeAt(bus.index);
		(nodes.size == 0).if({ this.stopAliveThread });
	}

	*register { |bus|
		var watcher;
		watcher = this.newFrom(bus.asBus.server);
		watcher.register(bus);
	}
	
	*unregister { |bus|
		var watcher;
		watcher = this.newFrom(bus.asBus.server);
		watcher.unregister(bus);
	}

	cmds { ^#["/c_setn"] }
	
	respond { arg method, msg;
		this.perform(method, msg)
	}
	
	startAliveThread {
		aliveThread = aliveThread ?? {
			Routine({
				inf.do({
					(nodes.size > 0).if({
						nodes.values.asArray.clump(10).do({ |busgroup|
							server.sendBundle(nil, [\c_getn]
								++ (busgroup.collect({ |bus| [bus.index, bus.numChannels] }))
								.flat);
						});
					});
					updateFreq.reciprocal.yield;
				});
			});
		};
			// do not re-start aliveThread if it's running already
		aliveThread.nextBeat.isNil.if({ aliveThread.play(SystemClock) });
	}
	
	stopAliveThread {
		aliveThread.stop;
		aliveThread = nil;
	}

	c_setn { |msg|
		var bus, i;
		i = 1;
		{ i < msg.size }.while({
			bus = nodes.at(msg.at(i));
			bus.notNil.if({
				bus.changed(msg[i+2 .. i + 1 + msg[i+1]])
			});
			i = i + msg[i+1] + 2;
		});
	}

	server_ { |newServer| server = newServer }
}
