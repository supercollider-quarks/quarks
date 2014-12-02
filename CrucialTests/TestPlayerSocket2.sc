
// newer system

TestPlayerSocket2 : TestAbstractPlayer {
	
	makePlayer {
		^PlayerSocket.new(\audio,1);
	}
	makeBus {
		^Bus.audio(s,1);
	}
	test_play {
		this.startPlayer;
		1.5.wait;
		this.stopPlayer;
	}

	test_setSource {
		var q,r;
		
		
		q = Patch({ Saw.ar(40.midicps) * 0.05 });
		r = Patch({ Saw.ar(52.midicps) * 0.05 });

		this.startPlayer;

		player.preparePlayer(q);
		this.wait({ q.isPrepared },"waiting for player socket to prepare patch for play");
		
		player.setSource(q);
		this.wait({ q.isPlaying },"waiting for the Patch q to play inside the socket");
		this.assertEquals(player.socketStatus,\isPlaying,"socket status");
	}

	test_setSourceToBundle {
		var q,r;
		
		
		q = Patch({ Saw.ar(40.midicps) * 0.05 });
		r = Patch({ Saw.ar(52.midicps) * 0.05 });

		this.startPlayer;

		player.preparePlayer(q);
		this.wait({ q.isPrepared },"waiting for player socket to prepare patch for play");
		
		player.setSourceToBundle(q,bundle,0.1);

		this.stopPlayer;

	}
	test_prepareToBundle {
		group = Group.basicNew(s);
		bus = this.makeBus;
		player.prepareToBundle(group,bundle,true,bus);
		this.assertEquals(player.group,group);
		this.assertEquals(player.bus,bus);
		this.assert(player.envdSource.notNil,"envdSource should exist");

		this.assert(bundle.preparationMessages.notNil,"should have synth defs of envdSource");

	}
	// make your own bus and group
	test_prepareToBundle2 {
		this.bootServer; // zero the instr synth def cache !
		InstrSynthDef.clearCache(s);

		// group exists?
		bus = this.makeBus;
		player.prepareToBundle(s,bundle,true,nil,false);

		this.assert(player.envdSource.notNil,"envdSource should exist");
	}


}

