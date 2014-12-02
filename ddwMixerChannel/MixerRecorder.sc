
MixerRecorder {
		// a reduced version of RecNodeProxy to work better with MixerChannels
	classvar	<dir; // = "sounds/";
	var	<mixer, <buffer, <path, <synth, <running = false;

	*initClass {
		StartUp.add {
			this.dir = thisProcess.platform.recordingsDir;
		}
	}

	*dir_ { |path|
		dir = path;
		(dir.last != thisProcess.platform.pathSeparator).if({
			dir = dir ++ thisProcess.platform.pathSeparator;
		});
	}

	*new { arg mixer, path, headerFormat = "aiff", sampleFormat = "float";
		^super.new.init(mixer, path, headerFormat, sampleFormat);
	}
	
	init { arg mix, p, head, samp;
		mixer = mix;
		mixer.server.serverRunning.not.if({ "server not running".inform; ^this });
		buffer.notNil.if({ "already recording. use pause/unpause".inform; ^this  });
			// if no name provided, name it after the mixer with a timestamp
		path = p ?? { dir ++ mixer.name ++ Main.elapsedTime.trunc
			++ "." ++ head };
		buffer = Buffer.alloc(mixer.server, 65536, mixer.outChannels);
		buffer.write(path, head, samp, 0, 0, true);
		("Prepared for recording: " ++ path).postln;
		CmdPeriod.add(this);
	}
	
	cmdPeriod {
		CmdPeriod.remove(this);
		this.close;
	}
	
	record { arg paused = false;
		var bus;
		buffer.isNil.if({ this.init });
		
			// should not create a synth if already recording
		synth.isNil.if({
			bus = mixer.inbus;

			synth = Synth.basicNew("mixers/Rec" ++ mixer.outChannels, mixer.server,
				mixer.server.nodeAllocator.allocPerm);
			mixer.server.sendBundle(nil, synth.newMsg(mixer.fadergroup,
				[\i_in, bus.index, \i_bufNum, buffer.bufnum], \addToTail));
			paused.if({
				this.pause;
			}, {
				running = true;
				("******** Recording begun: " ++ path).postln;
			});
			mixer.mcgui !? { mixer.mcgui.updateView(\record, mixer.isRecording.binaryValue) };
		});
	}
	
	pause {
		synth.notNil.if({
			synth.run(false);
			("******** Recording paused: " ++ path).postln;
		});
		running = false;
		mixer.mcgui !? { mixer.mcgui.updateView(\record, mixer.isRecording.binaryValue) };
	}
	
	unpause {
		synth.notNil.if({
			synth.run(true);
			("******** Recording resumed: " ++ path).postln;
			running = true;
		});
		mixer.mcgui !? { mixer.mcgui.updateView(\record, mixer.isRecording.binaryValue) };
	}

	close {
		synth.notNil.if({ 
			synth.free;
			synth.server.nodeAllocator.freePerm(synth.nodeID);
			synth = nil;
		});
		buffer.notNil.if({
			("******** Recording ended. File closed: " ++ path).postln;
			buffer.close;
		});
		mixer.mcgui !? { mixer.mcgui.updateView(\record, false.binaryValue) };
		path = nil;
		buffer.free;
		buffer = nil;
		running = false;
		CmdPeriod.remove(this);		// does not need to respond to cmd-. any more
	}
}
