//redFrik

//related: RedSFPlayerDisk, RedSampler

//todo: makeWindow

RedSFPlayer {
	var	<>server, <buffer, <synth, <duration, <channels,
		amp= 1, sendDefs= true;
	*new {|server|
		^super.new.server_(server ? Server.default);
	}
	*read {|path, server|
		^this.new(server).read(path);
	}
	read {|path|
		var file;
		if(File.exists(path), {
			file= SoundFile.openRead(path);
			channels= file.numChannels;
			duration= file.duration;
			file.close;
			buffer.free;
			this.prRead(path);
			if(sendDefs, {
				this.prSendDefs;
				sendDefs= false;
			});
		}, {
			(this.class.name++": file"+path+"not found").warn;
		});
		
	}
	loop {|out= 0, rate= 1, fadeTime= 0|
		this.prPlay(out, rate, fadeTime, true);
	}
	play {|out= 0, rate= 1, fadeTime= 0|
		this.prPlay(out, rate, fadeTime, false);
	}
	stop {|fadeTime= 0|
		if(this.isPlaying, {
			this.prStop(fadeTime);
		});
	}
	free {
		this.prFree;
	}
	amp_ {|val|
		amp= val;
		if(this.isPlaying, {
			synth.set(\amp, amp);
		});
	}
	isPlaying {
		^synth.isPlaying;
	}
	makeWindow {
		
	}
	
	//--private
	prRead {|path|
		buffer= Buffer.read(server, path);
	}
	prSendDefs {
		8.do{|i|
			SynthDef("redSFPlayer"++(i+1), {|out= 0, rate= 1, atk= 0, rel= 0, loop= 1, amp= 1, buf, gate= 1|
				var env= EnvGen.kr(Env.asr(atk, 1, rel), gate, doneAction:2);
				var src= PlayBuf.ar(i+1, buf, rate*BufRateScale.ir(buf), 1, 0, loop);
				Out.ar(out, src*env*amp);
			}).send(server);
		};
	}
	prPlay {|out, rate, fadeTime, loop|
		if(this.isPlaying, {
			this.stop(fadeTime);
		});
		synth= Synth("redSFPlayer"++channels, [
			\out, out,
			\rate, rate,
			\atk, fadeTime,
			\rel, fadeTime,
			\loop, loop,
			\amp, amp,
			\buf, buffer
		]);
		NodeWatcher.register(synth);
		if(loop.not, {
			SystemClock.sched(duration, {
				this.stop(fadeTime);
				nil;
			});
		});
	}
	prStop {|fadeTime|
		synth.set(\rel, fadeTime, \gate, 0);
	}
	prFree {
		if(this.isPlaying, {
			synth.free;
		});
		buffer.free;
	}
}

