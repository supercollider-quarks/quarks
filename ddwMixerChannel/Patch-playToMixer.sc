
+ Patch {
	playToMixer { arg m, atTime = nil, callback;
		var	bundle = MixedBundle.new,
			timeOfRequest = Main.elapsedTime;
		this.group = m.synthgroup;
			// generate synthdef to check numChannels
		this.loadDefFileToBundle(bundle, m.server);
		if(synthDef.numChannels > m.inChannels) {
			"Playing a %-channel patch on a %-input mixer. Output may be incorrect."
				.format(synthDef.numChannels, m.inChannels).warn;
		};
		this.prPlayToBundle(atTime,
			SharedBus(this.rate, m.inbus.index, synthDef.numChannels, m.server),
			timeOfRequest, bundle);
		callback !? { bundle.addFunction(callback) };
		bundle.sendAtTime(this.server, atTime, timeOfRequest);
	}
}

+ AbstractPlayer {
	prPlayToBundle { arg atTime, bus, timeOfRequest, bundle;
		if(status !== \readyForPlay,{ this.prepareToBundle(group, bundle, false, bus) });
		this.makePatchOut(group,false,bus,bundle);
		this.spawnToBundle(bundle);
	}
}

+ Nil {
	draggedIntoMixerGUI { |gui|
		gui.mixer = nil;
	}
	
	asMixerChannelGUI { |board|
		^MixerChannelGUI(nil, board)
	}
}

// needed to allow collections as mixer arguments in MixingBoard-new
+ Collection {
	asMixerChannelGUI { |board|
		^this.collect({ |mixer| MixerChannelGUI(mixer, board) })
	}
}

+ MixerChannel {
	draggedIntoMixerGUI { |gui|
		gui.mixer = this;
	}
}

+ String {
	draggedIntoMixerGUI { |gui|
		this.interpret.draggedIntoMixerGUI(gui)
	}
}
