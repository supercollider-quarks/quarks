// hybrid: clock and server at a time.
// maybe should be subclass of server

Renderer : Clock {
	

	var <>lifeTime, <>options, <threadBeats;
	var <>score;
	
	
	var <nodeAllocator; 
	var <controlBusAllocator;
	var <audioBusAllocator;
	var <bufferAllocator;
	
	
	*new { arg lifeTime=60, options;
		^super.newCopyArgs(lifeTime, options ? ServerOptions.new).init
	}
	
	*use { arg func, lifeTime=60, options;
		var renderer = this.new(lifeTime, options);
		var curClock = TempoClock.default, curServer = Server.default;
		TempoClock.default = renderer;
		Server.default = renderer;
		func.value(this);
		TempoClock.default = curClock;
		Server.default = curServer;
		^renderer.score		
	}
	
	init { 
		threadBeats = IdentityDictionary.new;
		score = Score.new;
		this.newAllocators;
	}
	
	/////////////// server /////////////////
	
	// perhaps one needs to emulate some more here
	
	newAllocators {
		nodeAllocator = NodeIDAllocator(0);
		controlBusAllocator = PowerOfTwoAllocator(options.numControlBusChannels);
		audioBusAllocator = PowerOfTwoAllocator(options.numAudioBusChannels, 
				options.numInputBusChannels + options.numOutputBusChannels);
		bufferAllocator = PowerOfTwoAllocator(options.numBuffers);
	}
	latency { ^0 }
	nextNodeID { ^nodeAllocator.alloc }
	serverRunning { ^true }
	asTarget { ^this }
	server { ^this }
	notified { ^true }
	addr { ^this }
	// well ..
	defaultGroup { ^this }
	nodeID { ^1 }
	
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
	sendMsg { arg ... args;
		score.add([this.getElapsed, args]);
	}
	sendBundle { arg time ... messages;
		var t;
		t = this.getElapsed + (time ? 0);
		messages.do {Êarg msg; 
			score.add([t, msg])
		}
	}
	listSendMsg { arg msg;
		this.sendBundle(nil, msg);
	}
 	listSendBundle { arg time, bundle;
		this.performList(\sendBundle, [time] ++ bundle);
	}
	play { arg task;  // quant to do..
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