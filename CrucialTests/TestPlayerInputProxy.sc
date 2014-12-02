
TestPlayerInputProxy : TestAbstractPlayer {
	
	makePlayer {
		^PlayerInputProxy.new(\stereo);
	}
	makeBus {
		^Bus.audio(s,2);
	}
	test_play {
		this.startPlayer;
		1.5.wait;
		this.stopPlayer;
	}
	
}

