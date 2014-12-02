//these classes are part of the RedSample package / redFrik, gnu gpl v2
//preloads buffer when .stop - files kept open

RedDiskInSamplerGiga : RedDiskInSampler {
	//fast trigger version that re-cue files on .stop
	//note: this can only handle ca 244 files due to unix system limitations of how many files allwed to be kept open at the same time! ("ulimit -u".unixCmd)
	//can be overridden with "launchctl limit maxfiles 2048 unlimited".unixCmd
	prCreateVoice {|sf, startFrame, argNumFrames|
		^RedDiskInGigaSamplerVoice(server, sf.path, sf.numChannels, startFrame, argNumFrames ? numFrames, sf.duration);
	}
}

RedDiskInGigaSamplerVoice : RedDiskInSamplerVoice {
	play {|attack, sustain, release, amp, out, group, loop|
		var name= this.defName;
		switch(loop,
			1, {name= name++"loop"},
			2, {name= name++"loopEnv"}
		);
		isPlaying= true;
		synth= Synth.head(group ?? {server.defaultGroup}, name, [
			\i_out, out,
			\bufnum, buffer.bufnum,
			\amp, amp,
			\attack, attack,
			\sustain, sustain ?? {(length-attack-release).max(0)},
			\release, release
		]);
		OSCresponderNode(server.addr, '/n_end', {|t, r, m|
			if(m[1]==synth.nodeID, {
				buffer.close;
				buffer.cueSoundFile(path, startFrame, {
					isPlaying= false;
					isReleased= false;
				});
				r.remove;
			});
		}).add;
	}
	prAllocBuffer {|action|
		buffer= Buffer.cueSoundFile(server, path, startFrame, channels, numFrames, action)
	}
}
