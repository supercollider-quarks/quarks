// wslib 2005
// simple + fast audition of audiofile

+ Buffer {
	*playDialog { // convenience method
		arg  mul = 1, rate=1, out=0, server, free=true, startFrame = 0, numFrames, bufnum;
		^Buffer.readPlay(server, nil, free, startFrame, numFrames, bufnum, mul, rate, out);
	}
	
	*readPlay { arg server, path, free=false, startFrame = 0, numFrames, 
		bufnum, mul = 1, rate=1, out=0;
		// free=true will free the buffer right after playback
		// if path=nil a dialog will open to select a path
		var buffer, action, readPlayFunc;
		server = server ? Server.default;
		
		action = { buffer.playOnce(mul, rate, out);
			if(free)
				{ {buffer.free}
					.defer(((buffer.numFrames / buffer.sampleRate) / rate.abs)
						+ server.latency); }
			};
		buffer = super.newCopyArgs(server, bufnum ?? { server.bufferAllocator.alloc(1) })
					.addToServerArray;
		readPlayFunc = { arg inPath;
			buffer.doOnInfo_(action).waitForBufInfo
				.allocRead(inPath,startFrame,numFrames, {["/b_query",buffer.bufnum]});
			};
		if(path.isNil)
			{ File.openDialog("Select a file...", readPlayFunc); }
			{ readPlayFunc.(path.standardizePath); };
		^buffer;
	}
	
	playOnce { arg mul = 1, rate = 1, out=0; 
		// improved version of -play:
		// + no synthdef count buildup
		// + rate argument
		// + mono files will play on 2 channels
		// - no looping with this one..	
		// - no backwards playing (rate must be positive)
		^SynthDef("Buffer-playOnce", { var player;
			player = PlayBuf.ar(numChannels, bufnum, BufRateScale.kr(bufnum) * rate); 
			FreeSelfWhenDone.kr(player);
			if(numChannels == 1)
				{ Out.ar(out, player.dup * mul); } // mono to 2 channels
				{ Out.ar(out, player * mul); }
		}).play;
	}
		
}

