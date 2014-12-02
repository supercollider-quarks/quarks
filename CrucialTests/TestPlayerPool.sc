
// newer system

TestPlayerPool : TestAbstractPlayer {
	var players;
	makePlayer {
		players = Array.fill(3,{ Patch({ SinOsc.ar(rrand(100,200).dup) }) });
		^PlayerPool.new(players);
	}
	makeBus {
		^Bus.audio(s,2);
	}
	test_startStopStart {
		this.startStopStart;
	}
	
	test_play {
		this.startPlayer;
		1.5.wait;
		this.stopPlayer;
	}

	test_select {
		this.startPlayer;
		1.0.wait;
		player.select(1);
		0.5.wait;
		this.assertEquals(player.selected,1);
		
		this.stopPlayer;
	}

	test_prepareToBundle {
		this.makePlayer;

		this.bootServer;
		0.1.wait;
		group = Group.basicNew(s);

		// group exists?
		bus = this.makeBus;
		player.prepareToBundle(group,bundle,true,bus);

		this.assertEquals(player.group,group);
		this.assertEquals(player.bus,bus);
		this.assert(player.envdSource.notNil,"envdSource should exist");

		this.assert(bundle.preparationMessages.notNil,"should have synth defs of envdSource");
		// no, it does it on load now
		//this.assert(players.first.defName.notNil ,"patches should have def names , built");
		// this.assert(bundle.includesDefName( players.first.defName ) ,"should have def name of player");
	}

	// make your own bus and group
	test_prepareToBundle2 {
		this.bootServer;
		0.1.wait;

		this.makePlayer;

		bus = this.makeBus;
		player.prepareToBundle(s,bundle,true,nil);

		this.assert(player.envdSource.notNil,"envdSource should exist");
	}


}

