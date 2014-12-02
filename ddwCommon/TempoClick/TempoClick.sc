
TempoClick {
	// takes a tempoclock, plays impulses on a kr bus based on the clock
	
	classvar	<>latencyFudge = 0;
	
	var	<server, <clock, <bus, <subdiv, <nodeID, nodeIDBounce;
	var	aliveThread, serverWatcher;
	
	*new { arg server, clock, bus, subdiv = 1;
		^super.newCopyArgs(server ?? { Server.default }, clock ? TempoClock.default,
			bus, subdiv).init;
	}
	
	init {
		var	id2;
		clock.isKindOf(TempoClock).not.if({
			("Clock must be a TempoClock. This is " ++ clock.asString).die;
		});
		clock.addDependant(this);
		serverWatcher = Updater(server, { |server, changed|
			if(changed == \serverRunning and: { server.serverRunning.not }) {
				AppClock.sched(5, {
						// really dead, stop the tempoclick
					if(server.serverRunning.not) {
						"Server died, TempoClick is stopped.".postln;
						this.free
					}
				})
			}
		});
		nodeID = server.nodeAllocator.allocPerm;
		id2 = server.nodeAllocator.allocPerm;
		nodeIDBounce = nodeID + id2;
		this.play;
	}
	
	play {
		server.waitForBoot({	// when server is booted
			bus.isNil.if({ bus = Bus.control(server, 1) });
			this.startAliveThread;
		});
	}
	
	startAliveThread {
		var	time;
			// synthdef sending is asynchronous
			// however s_new messages are sent with latency,
			// which in most cases should avoid synthdef not found errors
			// even if the clock is superfast you should get only one failure
		SynthDef("TempoClick", { arg tempo = 1, subd = 1, bus = 0;
				// kill the synth every beat - this helps the client enforce sync
			Line.kr(0, 1, tempo.reciprocal, doneAction:2);
			Out.kr(bus, Impulse.kr(tempo * subd));
		}).send(server);
		
		aliveThread = Routine({
			{	nodeID = nodeIDBounce - nodeID;
				server.sendBundle(server.latency, [\s_new, \TempoClick, nodeID,
					0, 0,	// at head of group 0
					\tempo, clock.tempo, \subd, subdiv, \bus, bus.index]);
				1.0.wait;
			}.loop;
		});
		clock.schedAbs(clock.elapsedBeats.roundUp, aliveThread);
	}
	
	stop {
		aliveThread.stop; aliveThread = nil;
	}
	
	remove { this.free }
	
	free {
		this.stop;
		bus.free;
		clock.removeDependant(this);
		serverWatcher.remove;
		if(nodeID.notNil) {
			server.nodeAllocator.freePerm(nodeID);
			server.nodeAllocator.freePerm(nodeIDBounce - nodeID);
			nodeID = nil
		};
	}
	
	update { |obj, what|
		switch(what)
			{ \tempo } {
				aliveThread.notNil.if({
					server.sendMsg(\n_set, nodeID, \tempo, clock.tempo)
				});
			}
			{ \stop } { this.free };
	}
	
	tempo_ { arg tempo;
		clock.tempo = tempo;	// TempoClock calls changed, which calls update
	}
	
	subdiv_ { arg s;
		subdiv = s;
		aliveThread.notNil.if({ server.sendMsg(\n_set, nodeID, \subd, subdiv) });
	}
	
	index { ^bus.index }
	asMapArg { ^"c" ++ bus.index }	
	asMap { ^this.asMapArg }
	asUGenInput { ^In.kr(bus, 1) }
	asControlInput { ^this.asMapArg }
}
