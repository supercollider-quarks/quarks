
MixerControl : GlobalControlBase {
	var <mixerGui, controlKey;
	var regCount = 0;

	mixerGui_ { |mcgui|
		mixerGui = mcgui;
		NotificationCenter.notify(this, \setMixerGui, [mcgui]);
	}

	update { |bus, msg|
		value = msg[0];
		(mixerGui.notNil).if({
			mixerGui.updateView(controlKey, value)
		});
	}

	watch { |key, gui, count = 0|
		this.register(key, gui, count);
		super.watch(count);
	}

	stopWatching { |count = 0|
		this.register(
			nil, nil,
			// potential bug: watchCount > regCount?
			// That should never happen: regCount should always be >= watchCount
			// if count is 0, we want to remove all watches but keep remaining registrations
			if(count == 0) { regCount - watchCount } { count }
		);
		super.stopWatching(count);
	}

	register { |key, gui, count = 1|
		if(key.isNil) {
			if(count == 0) {  // 0 means always unregister
				regCount = watchCount;
			} {
				regCount = max(0, regCount - count);
			};
			if(regCount == 0) {
				mixerGui = controlKey = nil;
			};
		} {
			mixerGui = gui;
			controlKey = key;
			regCount = regCount + max(count, 1);
		};
	}

	makeGUI {}	// MixerChannelGUI class does this
}