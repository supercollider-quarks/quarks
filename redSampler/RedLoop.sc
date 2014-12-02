//redFrik

RedLoop {
	var sampler, magic;
	*new {|path, amp= 1, out= 0, group|
		^super.new.initRedLoop(path, amp, out, group);
	}
	initRedLoop {|path, amp, out, group|
		magic= 999999.9.rand.asSymbol;
		Server.default.waitForBoot{
			sampler= this.prType.new(Server.default);
			sampler.overlaps= 1;
			Server.default.sync;
			sampler.prepareForPlay(magic, path);
			Server.default.sync;
			sampler.play(magic, amp: amp, out: out, group: group, loop: 1);
		};
	}
	amp_ {|val= 1|
		sampler.amp= val;
	}
	channels {
		^sampler.channels(magic);
	}
	length {
		^sampler.length(magic);
	}
	buffer {
		^sampler.buffers(magic)[0];
	}
	free {
		sampler.stop(magic);
		sampler.free;
	}
	prType {
		^RedSampler;
	}
}
RedLoopDisk : RedLoop {
	prType {
		^RedDiskInSampler;
	}
}

/*
a= RedLoop("sounds/FreeHipHopAppleLoops/FreeHipHopAppleLoops/In Da City break.aif");
a.free
a= RedLoopDisk("sounds/crashingRedXM.aiff");
a.free
a= RedLoop("sounds/amenmono.wav", 1, 1);
a.amp= 0.3
a.channels
a.length
a.buffer
a.free
*/