
SharedServerOptions : ServerOptions {
	
	var <>numReservedControlBuses = 0;
	var <>numReservedAudioBuses = 0;
	var <>numReservedBuffers = 0;
	var <>numClients;	
	
	*configArgs {
		^[
			\numOutputBusChannels, \numInputBusChannels, 
			\numReservedControlBuses, \numReservedAudioBuses, 
			\numReservedBuffers, \numClients
			]
	}
	
	*fromConfig {|... args|
		
		var res = this.new;
		this.configArgs.do { |key, i| 
			var val = args.at(i);
			val !? { res.instVarPut(key, val) }
		};
		
		^res
	}
	
	asConfig {
		^this.class.configArgs.collect { |key| this.instVarAt(key) }
	}
	
}

SharedServer : Server {
	
	var <myGroup;
	var <buffers;

	init { | argName, argAddr, argOptions, argClientID |
		super.init(argName, argAddr, argOptions, argClientID); 
		myGroup = Group.basicNew(this, 100 + argClientID);
	}
	
	initTree {
		nodeAllocator = NodeIDAllocator(clientID, options.initialNodeID);
		this.bind {
			"initTree % : myGroup should come back. 
			Others have to call initTree as well, e.g. by hitting Cmd-Period.\n".postf(name);
			this.sendMsg("/g_new", 1, 0, 0);
			this.sendMsg("/g_new", myGroup.nodeID, 1, 1);
		};
		tree.value(this);
		ServerTree.run(this);
	}
	
	asTarget { ^myGroup }
	asGroup { ^myGroup }
	asNodeID { ^myGroup.nodeID }

	
	// for now
	numClients_ { |numClients| options.numClients = numClients }
	numClients { ^options.numClients }

	
	newBusAllocators {
		var numControl, numAudio;
		var controlBusOffset, audioBusOffset;
		var offset = this.calcOffset;
		var n = options.numClients ? 1;
		
		numControl = options.numControlBusChannels div: n;
		numAudio = options.numAudioBusChannels div: n;
		
		controlBusOffset = options.numReservedControlBuses + (numControl * offset);
		audioBusOffset = options.firstPrivateBus + options.numReservedAudioBuses 
					+ (numAudio * offset);
					
		controlBusAllocator = 
				ContiguousBlockAllocator.new(
					numControl + controlBusOffset,
					controlBusOffset
				);
		audioBusAllocator = 
				ContiguousBlockAllocator.new(
					numAudio + audioBusOffset,
					audioBusOffset
				);
		"SharedServer % audio buses: % control buses %\n"
			.postf(name, numAudio, numControl);
	}
	
	newBufferAllocators {
		var bufferOffset;
		var offset = this.calcOffset;
		var n = options.numClients ? 1;
		var numBuffers = options.numBuffers div: n;
		bufferOffset = options.numReservedBuffers + (numBuffers * offset);
		bufferAllocator = 
				ContiguousBlockAllocator.new(
					numBuffers + bufferOffset, 
					bufferOffset
				);
		"SharedServer % buffers: %\n".postf(name, numBuffers);
	}
	
	calcOffset {
			if(options.numClients.isNil) { ^0 };
			if(clientID > options.numClients) {
					"Some buses and buffers may overlap for remote server: %".format(this).warn;
			};
			^clientID % options.numClients;		
	}

	myOuts {
		var numEach = options.numOutputBusChannels div: options.numClients;
		^(0 .. (numEach - 1)) + (numEach * clientID);
	}
	
	freeAll { |hardFree = false| 
		if (hardFree) { 
			super.freeAll(false);
		} { 
			myGroup.freeAll;
		}
	}
	
	getBuffers { |action| 
		var dur = (options.numBuffers * options.blockSize / sampleRate * 5);
		var newbuffers = Array(32);
		var resp = OSCresponder(nil, 'bufscan', { |time, resp, msg| 
						var bufnum, frames, chans, srate; 
						#bufnum, frames, chans, srate = msg.keep(-4); 
						if (chans > 0) { 
							newbuffers = newbuffers.add(
								Buffer(this, frames, chans, srate, bufnum: bufnum)) 
						};
					}).add;

		{
		var bufnum = Line.kr(0, options.numBuffers, dur, doneAction: 2).round(1);
		var trig = HPZ1.kr(bufnum);
		SendReply.kr(trig, 'bufscan', 
			[
				bufnum, 
				BufFrames.kr(bufnum), 
				BufChannels.kr(bufnum), 
				BufSampleRate.kr(bufnum)
			]);
		}.play(this);
		
		fork { 
			(dur + 0.5).wait; 
			resp.remove; 
			buffers = newbuffers;
			(action ? { |bufs| 
				"\t SharedServer - found these buffers: ".postln; bufs.printAll 
			}).value(buffers);
		}
	}
}




+ ServerOptions {
	
	numReservedControlBuses { ^0 }
	numReservedAudioBuses { ^0 }
	numReservedBuffers { ^0 }
	numClients { ^nil }
	
}



+ Server {

	remove {
		Server.all.remove(this);
		Server.named.removeAt(name);
		SynthDescLib.global.removeServer(this);
		try { this.window.close };
	}
	
	nodeAllocator_ { |allocator|
		nodeAllocator = allocator
	}
	
	controlBusAllocator_ { |allocator|
		controlBusAllocator = allocator
	}
	
	audioBusAllocator_ { |allocator|
		audioBusAllocator = allocator
	}
	
	bufferAllocator_ { |allocator|
		bufferAllocator = allocator
	}
	
}


