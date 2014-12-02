//redFrik 041207

//gate= 1 record, gate= 0 looped playback

RedTapeRecorder {
	
	*ar {|buffer, in, gate, interpol= 1|	//1=no, 2=linear, 4=cubic
		var recPhasor, playPhasor, playTrigger;
		playTrigger= 1-gate;
		recPhasor= Gate.ar(
			EnvGen.ar(
				Env(#[0, 1, 0], #[1, 0], 'lin', 1),
				gate,
				BufFrames.kr(buffer.bufnum),
				0,
				BufDur.kr(buffer.bufnum)
			),
			gate
		);
		playPhasor= Phasor.ar(playTrigger, 1, 0, Latch.ar(recPhasor, playTrigger));
		^Select.ar(playTrigger, [
			BufWr.ar(in, buffer.bufnum, recPhasor, 0),
			BufRd.ar(buffer.numChannels, buffer.bufnum, playPhasor, 1, interpol)
		])
	}
	
	*kr {|buffer, in, gate, interpol= 1|	//1=no, 2=linear, 4=cubic
		var recPhasor, playPhasor, playTrigger;
		playTrigger= 1-gate;
		recPhasor= Gate.kr(
			EnvGen.kr(
				Env(#[0, 1, 0], #[1, 0], 'lin', 1),
				gate,
				BufFrames.kr(buffer.bufnum),
				0,
				BufDur.kr(buffer.bufnum)*buffer.server.options.blockSize
			),
			gate
		);
		playPhasor= Phasor.kr(playTrigger, 1, 0, Latch.kr(recPhasor, playTrigger));
		^Select.kr(playTrigger, [
			BufWr.kr(in, buffer.bufnum, recPhasor, 0),
			BufRd.kr(buffer.numChannels, buffer.bufnum, playPhasor, 1, interpol)
		])
	}
}
