
MIDIRecGUI : HJHObjectGui {
	var	<mainView,		// holds container for all views in this object
		<seqMenu,
		<nameSet,
		<statusButton,
		<quant;		// record-quantize control

	guiBody { arg lay;
		layout = lay;
		mainView.isNil.if({	// if this gui is already open, don't recreate
			mainView = FlowView(layout, argBounds ?? { Rect(0, 0, 700, 300) }, margin: 2@2);

			seqMenu = GUI.popUpMenu.new(mainView, Rect(0, 0, 120, 20))
				.items_(model.menuItems)  //.align(\center);
				.action_({ arg m;
					model.recorder.isNil.if({	// not recording, so...
						model.value_(m.value)	// update current buffer
					});	// otherwise, ignore (can't change buffer while recording)
				});

			nameSet = ToggleTextField(mainView, Rect(0, 0, 120, 20))
				.action_({ arg t;
					model.current.name_(t.string);	// set the name
					this.refreshMenu;				// fix the menu
					{ t.string_(""); nil }.defer;	// reset string to nothing
				});

			statusButton = GUI.button.new(mainView, Rect(0, 0, 80, 20)).states_([
				["idle", Color.new255(12, 9, 96), Color.new255(255, 178, 203)],
				["RECORD", Color.new255(12, 9, 96), Color.new255(255, 39, 19)]
			])	.value_(0)
				.action_({ |b|
					(b.value > 0).if({
						model.initRecord;
					}, {
						model.stopRecord;
					});
				});

			// implement quantize later -- actually, apply quantize at translation time
			model.view = this;		// so it can find me
			this.refresh(model);
			if(iMadeMasterLayout) { masterLayout.recursiveResize };
		});
	}

	refresh { arg changer;
		{ statusButton.value_(model.recorder.notNil.binaryValue);
		  seqMenu.items_(model.menuItems);
		  nil }.defer;
		model.current.isNil.if({		// pointing to empty space at end?
			{ seqMenu.value_(model.bufs.size);
			  nil }.defer;
		}, {
			{ seqMenu.value_(model.value);
			  nil }.defer;
		});
	}

	refreshMenu {
		{
			seqMenu.items_(model.menuItems);
		}.defer;
	}

	setName {		// give focus to nameSet so user can type name
		{ nameSet.focus(true); }.defer;
	}
}
