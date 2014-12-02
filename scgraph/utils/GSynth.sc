GSynth : Synth {

	*new { arg defName, args, target, addAction=\addToHead;
		var synth, server, addNum, inTarget;
		inTarget = target.asTarget;
		server = inTarget.server;
		addNum = addActions[addAction];
		synth = this.basicNew(defName, server);

		if((addNum < 2), { synth.group = inTarget; }, { synth.group = inTarget.group; });
		server.sendMsg(9,
			defName, synth.nodeID, addNum, inTarget.nodeID,
			*(args.asControlInput));
		^synth
	}

}