// temporary solution for postfader submixing
// when/if proxies use volume busses, this will change.

ProxySubmix : Ndef {

	var <skipjack, <proxies, <sendNames, <volBusses;

	addLevel { |lev_ALL = 1, masterNodeID = 1001|
		// if needed, init with default numChannels from NodeProxy
		if (this.isNeutral) { this.ar };

		this.put(masterNodeID, {
			ReplaceOut.ar(bus,
				bus.ar * \lev_ALL.kr(lev_ALL).lag(0.05)
			);
		});
	}

	addMix { |proxy, sendLevel = 0.25, postVol = true, mono = false|

		var indexInMix, sendName, volBus;
		this.checkInit(proxy);

		if (proxies.includes(proxy)) { ^this };

		indexInMix = proxies.size + 1;
		sendName = ("snd_" ++ proxy.key).asSymbol;
		proxies = proxies.add(proxy);
		sendNames = sendNames.add(sendName);
		this.addSpec(sendName, \amp);

		if (postVol) {
			if (skipjack.isNil) { this.makeSkip };
			volBus = Bus.control(server, 1);
			volBusses = volBusses.add(volBus);
		};

		this.put(indexInMix, {
			var source, levelCtl;
			source = NumChannels.ar(proxy.ar,
				if(mono) { 1 } { this.numChannels }
			);
			levelCtl = sendName.kr(sendLevel);
			if (postVol) {
				levelCtl = levelCtl * volBus.kr;
			};
			source * levelCtl.lag(0.05);
		});
	}

	checkInit { |proxy|
		if (this.isNeutral) { this.ar(proxy.numChannels) };

		if (proxies.isNil) {
			proxies = [];
			sendNames = [];
			volBusses = [];
			this.addSpec(\lev_ALL, [0, 4, \amp]);
		};
	}

	makeSkip {
		skipjack = SkipJack({ this.updateVols; }, 0.05);
	}

	updateVols {
		// collect all setmessages and send as one bundle
		// to reduce osc traffic
		server.bind {
			proxies.do { |proxy, i|
				var volBus = volBusses[i];
				if (volBus.notNil) { volBus.set(proxy.vol) }
			};
		};
	}

	clear {
		proxies.clear;
		sendNames.clear;
		volBusses.do(_.free);
		volBusses.clear;

		skipjack.stop;
		skipjack = nil;
		^super.clear;
	}
}