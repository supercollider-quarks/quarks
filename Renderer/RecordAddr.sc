RecordAddr : NetAddr {
	var <startTime, <>score, <>filter;
	var <>sendToServer=true;
	
	*new { arg hostname, port=0;
		^super.new(hostname, port).init;
	}
	init {
		this.clear;
		filter = #['/status', '/notify', '/dumpOSC', '/quit', '/n_trace', '/clearSched']; 
		// is this all?
	}
	
		
	start { startTime = Main.elapsedTime }
	stop { startTime = nil }
	clear {Êscore = Score.new; }
	getElapsed { ^Main.elapsedTime - startTime }
	isRecording { ^startTime.notNil }


	bundleToScore { arg bundle, time;
		var dt;
		dt = this.getElapsed  + (time ? 0);
		bundle.do { arg msg;
			var cmd;
			cmd = msg[0].asSymbol;
			if(filter.includes(cmd).not) { 
				score.add([dt, msg]) 
			}; 
		};
	}
	
	sendBundle { arg time ... args;
		if(sendToServer) { this.superPerformList(\sendBundle, [time] ++ args) };
		if(this.isRecording) {Êthis.bundleToScore(args, time) }
	}
	sendMsg { arg ... args;
		this.sendBundle(nil, args);
	}
	
}

RenderAddr : RecordAddr {
	var <>lifeTime, <threadBeats;
	
	*new { arg hostname="127.0.0.1", port=0, lifeTime=60;
		^super.new(hostname, port).lifeTime_(lifeTime)
	}

	init { 
		super.init;
		threadBeats = IdentityDictionary.new;
	}
	
	sendBundle { arg time ... args;
		this.bundleToScore(args, time);
	}
	bundleToScore { arg bundle, time;
		var dt;
		dt = this.getElapsed  + (time ? 0);
		bundle.do { arg msg; score.add([dt, msg]) }; 
	}


	
	////////////// clock //////////////////	
	
	getElapsed {
		^threadBeats[thisThread] ? 0;
	}
	addDelta { arg delta, thread;
		var beats;
		if(delta.isNil) { ^nil };
		beats = threadBeats[thread];
		if(beats.isNil) 
			{ threadBeats[thread] = beats = delta } // add new thread
			{
				beats = beats + delta;
				threadBeats[thread] = beats; 
			};
		^beats
	}
	
	sched { arg delta, item;
		var beats;
		beats = this.addDelta(delta, thisThread);
		^if(beats <= lifeTime) { item.value } { nil }
	}
	schedAbs { arg beat, item; // no tempo for now.
		this.sched(beat, item);
	}
	
	play { arg task; 
		var delta=0, beats=0;
		while { 
			delta.notNil and: { beats.notNil } and: { beats <= lifeTime } 
		} { 
			delta = task.value(this);
			beats = this.addDelta(delta, task);
		};
		threadBeats.removeAt(task);
	}
	

}