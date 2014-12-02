
BufEnvir : EnvironmentRedirect {
	var <server;
	var <>fftsize = 2048;

	*new { arg server;
		^super.new.initServer(server)
	}

	initServer { arg s; server = s }

	makeBuffer { ^Buffer.new(server, 0, 1) }
	updateInfo { envir.do(_.updateInfo) }

	clear { arg removeReferences = false;
		this.do { |buf| buf.free };
		if(removeReferences) { super.clear };
	}

	put { arg key, obj;
		var buf, bufnum;
		if(server.serverRunning.not) { Error("server not running").throw };
		if(key.isNil) { Error("BufEnvir:put() key is nil").throw };
		if(key.isSequenceableCollection) { ^this.putAll(key, obj.asArray) };
		server.makeBundle(nil, {
			buf = this.at(key);
			bufnum = buf.bufnum;
			obj.toBuffer(buf);
			server.sync;
			buf.updateInfo;
		});
		super.put(key, buf)
	}

	at { arg key;
		var res;
		if(key.isNil) { Error("BufEnvir:at() key is nil").throw };
		if(key.isSequenceableCollection) { ^this.atAll(key) };
		res = envir.at(key);
		if(server.serverRunning.not) { Error("server not running").throw };
		if(res.isNil) {
			res = this.makeBuffer;
			envir.put(key, res);
		};
		^res
	}

	putAll { arg keys, objects;
		keys.do { |key, i|
			this.put(key, objects.wrapAt(i))
		}
	}

	atAll { arg keys;
		^keys.collect { |key|
			this.at(key)
		}
	}

	putSeries { arg first, second, last, value;
		this.putAll(value.asArray, (first, second..last))
	}

	getAll { arg keys;
		^this.atAll(keys)  // backward compatibility
	}

	bufnum { arg key;
		var res = this.at(key);
		^if(res.isSequenceableCollection) { res.collect(_.bufnum) } { res.bufnum }
	}

	doWithUpdate { |key, func|
		server.bind { var buf = this.at(key); func.value(buf); server.sync; buf.updateInfo }
	}

	alloc { arg key, numFrames = 2048, numChannels = 1;
		var buf = this.at(key);
		if(buf.numFrames != numFrames or: {buf.numChannels != numChannels}) {
			buf.numFrames_(numFrames).numChannels_(numChannels).alloc;
		};
		^buf;
	}

	read { arg key, path, startFrame = 0, numFrames = -1, completionMessage;
		if(server.serverRunning.not) { Error("server not running").throw };
		this.doWithUpdate(key, { |buf|
			buf.allocRead(path, startFrame, numFrames, completionMessage);
		});
	}

	zero { arg key;
		var buf = envir.at(key);
		buf !? { buf.zero }
	}

	fft { arg key, size, numChannels=1;
		^this.alloc(key, size ? fftsize, numChannels).bufnum;
	}

	cue { arg key, path, startFrame=0, numChannels, bufferSize, completionMessage;
		var buf = this.at(key);
		if(path.isNil) { path = buf.path };
		if(buf.numFrames == 1 and: { bufferSize.isNil }) { bufferSize = 32768 };
		buf = this.alloc(key, bufferSize, numChannels);
		^buf.cueSoundFile(path, startFrame, completionMessage)
	}

	bufFrames { arg key;
		^BufFrames.kr(this.at(key).bufnum)
	}

	bufDur { arg key;
		^BufDur.kr(this.at(key).bufnum)
	}

	rateScale { arg key;
		^BufRateScale.kr(this.at(key).bufnum)
	}

	readAll { arg commonPath, fileExtension, startFrame = 0, numFrames = -1, completionMessage;
		this.doPathKeys({ |key, path|
			this.read(key, path, startFrame, numFrames, completionMessage)
		}, commonPath, fileExtension)
	}

	cueAll { arg commonPath, fileExtension, startFrame=0, numChannels, bufferSize;
		this.doPathKeys({ |key, path|
			this.cue(key, path, startFrame, numChannels, bufferSize)
		}, commonPath, fileExtension)
	}

	doPathKeys { arg func, commonPath, fileExtension;
		var searchPath, paths;
		searchPath= commonPath +/+ "*";
		if(fileExtension.notNil) { searchPath = searchPath ++ "." ++ fileExtension };
		paths = pathMatch(searchPath);
		paths.do { |path|
			var key = difference(path, commonPath);
			key = key.splitext.first.reject(_ == $/).asSymbol;
			func.value(key, path);
		}
	}

	rec { arg in, key, dur = (1.0), offset = 0.0, recLevel=1.0, preLevel=0.0,
						run=1.0, loop=1.0, trigger=1.0, doneAction=0;
		var numChannels = in.asArray.size;
		if(dur.notNil or: { numChannels != this.at(key).numChannels }) {
				this.alloc(key, dur * server.sampleRate, numChannels)
		};
		^RecordBuf.ar(in, this.at(key), offset, run, recLevel, preLevel, loop, trigger, doneAction)
	}

	playBuf { arg key, rate = 1, trigger = 0, startPos = 0, loop = 0, doneAction = 0;
		var buf = this.at(key);
		^PlayBuf.ar(buf.numChannels, buf, rate * BufRateScale.kr(buf), trigger, startPos, loop, doneAction)
	}

}


// to do:


/*
	maybe make it work even if the server is not booted
	consecutive buffers (allocConsecutive returns an array. store array in dict as it is?)

	release info when buffer is freed (shouldn't Buffer do this anyway?)

*/

