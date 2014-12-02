// wslib 2005
//
// writing functions to buffers and synths to files

+ Function {
	// directly write functions to soundfiles (realtime)
	// args is am aray, a msg style array of arguments for the function or a dict:
	//   [440, 1] or [\freq, 440, \gate 1] or (freq: 440, gate: 1)
	// 
	// based on Function-loadToFloatArray
	
	loadToBuffer { arg duration = 1, server, action, bufnum, args;
		var buffer, def, synth, value, name, numChannels;
		server = server ? Server.default;
		server.isLocal.not.if({"Function-loadToBuffer only works with a localhost server".warn; 
			^nil });
		server.serverRunning.not.if({"Server not running!".warn; ^nil });
		if(args.notNil) 
			{ value = this.value(*args)}
			{ value = this.value };
		if(value.size == 0, { numChannels = 1 }, { numChannels =  value.size });
		buffer = Buffer.new(server, duration * server.sampleRate, numChannels, bufnum);
		// no need to check for rate as RecordBuf is ar only
		name = name ? this.hash.asString;
		def = SynthDef(name, { 
			RecordBuf.ar(
				( if(args.notNil) 
			{ this.valueArgsArray(args)}
			{ this.value } ),  buffer.bufnum, loop:0);
			Line.ar(dur: duration, doneAction: 2);
		});
		Routine.run({
			var c;
			c = Condition.new;
			server.sendMsgSync(c, *buffer.allocMsg);
			server.sendMsgSync(c, "/d_recv", def.asBytes);
			synth = Synth.basicNew(name, server);
			if(action.notNil)
				{ OSCpathResponder(server.addr, ['/n_end', synth.nodeID], { 
				 action.value(buffer);
			}).add.removeWhenDone; };
			server.listSendMsg(synth.newMsg);
		});
		^buffer;
	}
	
	write { arg duration = 1, path, headerFormat="aiff",sampleFormat="int24", args;
		if( {path.splitext.last[..2]}.try != headerFormat[..2])
				{ path = [path.splitext.first, headerFormat].join(".");
				("Extension mismatch:\nchanged filename to: " ++ path.basename).postln };
		this.loadToBuffer(duration, args: args,
			action: { |buffer| buffer.write(path, headerFormat, sampleFormat, 
				completionMessage: {
					("done writing :\n" ++ path.asCompileString).postln; buffer.freeMsg;  }) });
		^this;
		}
		
	writeDialog { arg duration = 1, headerFormat="aiff",sampleFormat="int24", args;
		CocoaDialog.savePanel( { |path|
			this.write(duration, path, headerFormat, sampleFormat, args);
		});
		^this;
		}
}

+ Buffer {
	*loadFunction { |function, duration, bufnum|
		^function.loadToBuffer(duration, nil, bufnum) }
}

+ SynthDef {
	// just a wrapper for Function-write
	// resembles the SC2 "Synth.record"
	// creates a standard synthdef, which is NOT the one used for writing
	// name is used for filename
	// make sure that the function returns the audiorate output, not an Out, 
	// or it will not record anything.
	*record { arg name, ugenGraphFunc, duration = 1, addPath = "sounds/", 
				headerFormat="aiff",sampleFormat="int24";
		ugenGraphFunc.write(duration, 
			(addPath ++ name ++ "." ++ headerFormat).standardizePath,
				 headerFormat, sampleFormat);
		^SynthDef(name, ugenGraphFunc);
		}
		
	*recordDialog { arg name, ugenGraphFunc, duration = 1, 
			headerFormat="aiff", sampleFormat="int24";
		ugenGraphFunc.writeDialog(duration, headerFormat, sampleFormat);
		^SynthDef(name, ugenGraphFunc);
		}
	}
