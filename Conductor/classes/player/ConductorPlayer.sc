ConductorPlayer {
	var <>conductor;
	var <>name;
	var <>server;
	var <>players;
	var <>currentState;
	
	*new { |conductor|
		^super.newCopyArgs(conductor).init;
	}
	
//	input { ^currentState/4 }
//	input_ { | in| this.perform(#[stop, play, pause].at((in * 4).asInteger)) }
	value {  ^currentState  }
	value_ { |v| this.perform(#[stop, play, pause].at(v)) }
//	
	init {
		server = Server.default;
		players = [];
		currentState = 0;
		name = ">";
	}
		
	stop {
		if (currentState != 0) { 
			this.makeBundles({ conductor.use{ players.do(_.stop) } });
			currentState = 0; 
			CmdPeriod.remove(this); 
			this.changed(\synch, 0);
		}
	}
	
	play { 
		if (currentState == 0 ) {
			this.makeBundles({ conductor.use{ players.do(_.play) } });
			currentState = 1; 
			CmdPeriod.add(this); 
			this.changed(\synch, 1);
		};

	}
	
	pause { 
		this.makeBundles({ conductor.use{ players.do(_.pause) } });
		currentState = 2; 
		this.changed(\synch, 2);
	}
	
	resume { 
		this.makeBundles({ conductor.use{ players.do(_.resume) } });
		currentState = 1; 
		this.changed(\synch, 1);
	}

	cmdPeriod { this.stop; }
	
	makeBundles { arg func;
		var servers;
		servers = server.asArray;
		servers = servers.reject { | s | s.isKindOf(BundleNetAddr) };
		servers.do { |s | s.openBundle };
		try {
			func.value(this);
			servers.do { |s| s.closeBundle(s.latency) };
		}{|error|
			servers.do { |s| s.addr = s.addr.saveAddr}; // on error restore the normal NetAddr
			error.throw
		}
	}

	add { | obj | players = players.add(obj) }
	
	remove { |obj| players.remove(obj) }
	
	action_ { | playFunc, stopFunc, pauseFunc, resumeFunc |
		this.add ( ActionPlayer(playFunc, stopFunc, pauseFunc, resumeFunc ) )
	}

	buffer_ { | ev| 
		ev.parent = CVEvent.bufferEvent;
		this.add(ev);
	}
	
	controlBus_ { | ev, cvs|
		ev[\cvs] = cvs ? [];
		ev.parent = CVEvent.controlBusEvent;
		this.add(ev)
	}

	synth_ { | ev, cvs|
		ev[\cvs] = cvs ? [];
		ev.parent = CVEvent.synthEvent;
		this.add(ev)
	}

	synthDef_ { | function, cvs, ev|
		var name;
		name = function.hash.asString;
		SynthDef(name, function).store;
		ev = ev ? ();
		ev	.put(\instrument, name)
			.put(\cvs, cvs);
		ev.parent_(CVEvent.synthEvent);
		this.add(ev);
		^ev
	}

	group_ { | ev, cvs|
		ev[\cvs] = cvs ? [];
		ev.parent = CVEvent.groupEvent;
		this.add(ev)
	}
	
	task_ { |func, clock, quant|
		this.add(TaskPlayer(func,clock, quant));
	}
	
	pattern_ { | pat, clock, event, quant |
		this.add(PatternPlayer(pat, clock, event, quant) )
	}

	draw { arg win, argName, player; 
//		argName is ignored in favor of name assigned to object
		if (players.size > 0) {
			~playerGUI.value(win, name, player);
		};
	}
}


