//redFrik

//related: RedSFPlayer, RedDiskInPlayer

//todo: makeWindow

RedSFPlayerDisk : RedSFPlayer {
	prRead {|path|
		buffer= Buffer.cueSoundFile(server, path, 0, channels);
	}
	prSendDefs {
		8.do{|i|
			SynthDef("redSFPlayerDisk"++(i+1), {|out= 0, rate= 1, atk= 0, rel= 0, loop= 1, amp= 1, buf, gate= 1|
				var env= EnvGen.kr(Env.asr(atk, 1, rel), gate, doneAction:2);
				var src= VDiskIn.ar(i+1, buf, rate*BufRateScale.ir(buf), loop);
				Out.ar(out, src*env*amp);
			}).send(server);
		};
	}
	prPlay {|out, rate, fadeTime, loop|
		var attackTime, cond= Condition(true);
		fork{
			if(this.isPlaying, {
				attackTime= 0;
				cond.test= false;
				this.prStop(0, cond);
			}, {
				attackTime= fadeTime;
			});
			cond.wait;
			buffer.cueSoundFile(buffer.path, 0, {
				synth= Synth("redSFPlayerDisk"++channels, [
				\out, out,
				\rate, rate.max(0),				//need to block negative rates
				\atk, attackTime,
				\rel, fadeTime,
				\loop, loop,
				\amp, amp,
				\buf, buffer
			]);
			NodeWatcher.register(synth);
			if(loop.not, {
				SystemClock.sched(duration, {
					if(this.isPlaying, {
						this.stop(fadeTime);
					});
					nil;
				});
			});});
		};
	}
	prStop {|fadeTime, cond|
		synth.set(\rel, fadeTime, \gate, 0);
		SystemClock.sched(fadeTime, {
			buffer.close({if(cond.notNil, {cond.unhang})});
			nil;
		});
	}
	prFree {
		if(this.isPlaying, {
			synth.free;
		});
		buffer.close({buffer.free});
	}
}
