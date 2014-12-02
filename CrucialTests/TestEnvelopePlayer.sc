

TestEnvelopedPlayer : TestAbstractPlayer {

	makePlayer {
		^EnvelopedPlayer(PlayerInputProxy(AudioSpec(2)),Env.adsr,2);
	}
	makeBus {
		^Bus.audio(s,2)
	}
	test_play {
		this.startPlayer;
		0.5.wait;
		this.stopPlayer;
	}
	
	test_prepareToBundle {
		player.prepareToBundle(group,bundle,bus: bus);
		this.assert(bundle.preparationMessages.notNil,"should have synth defs of envdSource");
	}
	test_release {
		var p;
		p = EnvelopedPlayer(
			Patch({
				SinOsc.ar * 0.1
			}),
			Env.adsr(releaseTime: 1.0),
			1
		);
		p.play;
		0.5.wait;
		p.release;
		2.0.wait;
		this.assertEquals(s.numSynths,0,"should be no synths playing");
		p.free;
		0.5.wait;
		this.assertEquals( s.audioBusAllocator.blocks.size,0,"should be no busses allocated now");
	}
}

