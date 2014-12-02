
MixerChannelGUI {
	classvar	<>defaultDef;	// scwork/startup.rtf should set this during startup

	var	<mixer,	// the instance of mixerchannel
		<board,	// the instance of mixingboard
		<>origin,	// provided by MixingBoard
		<guidef,
		<>freeOnMixerFree,		// if I was created for a specific MixerChannel,
							// I should free when the channel is freed.
							// if I'm a placeholder, I should persist

		<views,
		>midiControl,
		<menuItems,
		>color1, >color2;		// allow overriding colors per channel
		
	*new { |mixer, board, origin|
		^super.newCopyArgs(mixer, board, origin).init
	}
	
	asMixerChannelGUI { ^this }
	asMixer { ^mixer }
	
	midiControl { ^midiControl ?? { mixer.tryPerform(\midiControl) } }

	init {
		freeOnMixerFree = mixer.notNil;
		guidef = mixer.tryPerform(\def).tryPerform(\guidef) ? defaultDef;
		origin = origin ?? { Point(0, 0) };

		this.setMenuItems.makeViews;

			// save path for mixerchannel to reach these views
			// tryPerform b/c mixer may be nil
		mixer.tryPerform(\mcgui_, this);
		^this
	}
	
	setMenuItems {
		menuItems = mixer.notNil.if({
			BusDict.menuItems(mixer.server, \audio).add("none")
		}, {
			["none"]
		});
	}

	makeViews {
		views = guidef.makeViews(board.w, origin, mixer, this);
	}
	
		// if origin changes, set the inst var before calling this
	refresh {
		this.setMenuItems;
		{	views.do({ |view, i|
				view.mixer_(mixer)
					.refresh(guidef.viewBounds[i].moveBy(origin.x, origin.y));
			});
		}.defer
	}
	
	free { arg freeIt = true;
		freeIt.if({ mixer.free(false); }); 	// free mixer & don't update gui
		mixer.tryPerform(\mcgui_, nil);
		{	views.do(_.free);
		}.defer;
			// update others? board responsibility?
	}

	updateView { |key, newValue|
		{	views.do({ |view, i|
				view.updateKeys.matchItem(key).if({
					views[i].update(newValue);
				});
			});
		}.defer;
	}

		// allow reassignment of mixer guis on the fly
	mixer_ { |mx, updateGUI = true|
		var oldgui, newdef;
		((oldgui = mx.tryPerform(\mcgui)).notNil and: { oldgui !== this }).if({
			mx.mcgui.mixer_(nil);	// clear former gui if assigned
		});
		mixer.tryPerform(\mcgui_, nil);	// old mixer shouldn't keep pointing to mixing board
		mixer = mx;
		mx.tryPerform(\mcgui_, this);
		newdef = mx.tryPerform(\def).tryPerform(\guidef) ? defaultDef;
		(guidef != newdef).if({
			this.guidef_(newdef, updateGUI);	// this will also set the views' mixer variables
		}, {
			views.do(_.mixer_(mixer));
		});
		this.resyncMIDI;
		updateGUI.if({ this.refresh });
	}
	
	guidef_ { |def, refresh = true|
		var	needResize = (guidef.channelSize != def.channelSize);
		(guidef == def).if({
			^this
		}, {
			views.do(_.free);	// kill the old views
			guidef = def;
			this.makeViews;
			refresh.if({
				needResize.if({
					board.sizeWindow;
					{	board.refresh;
					}.defer(0.1);
				}, {
					this.refresh;	// size did not change, refresh my views only
					{	board.w.refresh;	// in case views were removed
					}.defer(0.1);
				});
			});
		})
	}
	
		// hot-swappable midi support -- limited to currently supported midi objects
		// if you want more functionality, add your methods here
	level { ^mixer.tryPerform(\getControl, \level) }
	level_ { |level| mixer.tryPerform(\level_, level) }
	mute { |muteMe, updateGUI = true| mixer.tryPerform(\mute, muteMe, updateGUI) }
	getControl { |name| ^mixer.tryPerform(\getControl, name) }
	setControl { |name, value, updateGUI = true, stopAutomation = true, resync = true|
		^mixer.tryPerform(\setControl, name, value, updateGUI, stopAutomation, resync)
	}
	resyncMIDI {
		midiControl !? { midiControl.resync }
	}
	
		// individual views can ask the mcgui what background colors to use
	color1 { ^color1 ?? { guidef.color1 } }
	color2 { ^color2 ?? { guidef.color2 } }
}

