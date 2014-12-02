
ProxyPresetGui : JITGui {

	var <setLBox, <setLPop, <storeBtn, <delBtn, <setRPop, <setRBox, <xfader;
	var <proxyGui;


	object_ { |obj|
		if (this.accepts(obj)) {
			object = obj;
			this.setProxyGui(object);
			this.checkUpdate;
		} {
			"% : object % not accepted!".format(this.class, obj).warn;
		}
	}

	setProxyGui { |obj|
		if (proxyGui.isNil) { ^this };
		if (obj.notNil) {
			proxyGui.object_(obj.proxy);
		} {
			proxyGui.object_(nil);
		};
		proxyGui.checkUpdate
	}


	setDefaults { |options|
		var minHeight;

		if (numItems > 0) {
			minHeight = (skin.headHeight * 2)
			+ (numItems + 2 * skin.buttonHeight);
		} {
			minHeight = (skin.headHeight * 2 + 8);
		};

		defPos = 10@10;
		minSize = 380 @ minHeight;
	}

	makeViews { |options|
		var butHeight = skin.headHeight;
		var flow = zone.decorator;

			// top line

		StaticText(zone, Rect(0,0, 30, butHeight)).string_("curr")
		.background_(skin.foreground)
		.font_(font).align_(\center);

		setLPop = PopUpMenu(zone, Rect(0,0, 80, butHeight))
		.items_([]).font_(font)
		.background_(skin.foreground)
		.action_({ |pop| var name;
			name = pop.items[pop.value].asSymbol;
			object.setProxy(name); 		// sets proxy too.
			object.setCurr(name);
			object.morphVal_(0);
		});

		storeBtn = Button(zone, Rect(0, 0, 40, butHeight))
		.states_([["sto", skin.fontColor, skin.foreground]])
		.font_(font)
		.action_({ object.storeDialog(loc:
			(parent.bounds.left @ parent.bounds.top))
		});

		delBtn =  Button(zone, Rect(0,0, 40, butHeight))
		.states_([["del", skin.fontColor, skin.foreground]])
		.font_(font)
		.action_({ object.deleteDialog(loc:
			(parent.bounds.left - 100 @ parent.bounds.bottom))
		});

		Button(zone, Rect(0,0,40, butHeight))
		.states_([["rand", skin.fontColor, skin.foreground]])
		.font_(font)
		.action_({ |but, modif|
			// cocoa and swingosc -alt mod.
			var rand = if ([524576, 24].includes(modif)) {
				object.setRand(1.0) } { object.setRand  };

		});

		Button(zone, Rect(0,0, 40, butHeight))
		.states_([["doc", skin.fontColor, skin.foreground]])
		.font_(font)
		.action_({ object.postSettings });

		setRPop = PopUpMenu(zone, Rect(0,0, 80, butHeight))
		.items_([]).font_(font)
		.background_(skin.foreground)
		.action_({ |pop|
			object.setTarg(pop.items[pop.value].asSymbol);
		});

		StaticText(zone, Rect(0,0, 30, butHeight))
		.background_(skin.foreground)
		.string_("targ").font_(font).align_(\center);

		flow.nextLine;

			// lower line

		setLBox = NumberBox(zone, Rect(0,0, 30, butHeight))
		.background_(skin.foreground)
		.font_(font).align_(\center)
		.value_(-1).step_(1)
		.action_({ |box|
			var val = box.value % setLPop.items.size;
			setLPop.valueAction_(val);
			box.value_(val)
		});

		xfader = Slider(zone, Rect(0,0, 320, butHeight))
		.action_({ |sl|
			object.morph(sl.value,
				object.currSet.key,
				object.targSet.key
			);
		});

		setRBox = NumberBox(zone, Rect(0,0, 30, butHeight))
		.background_(skin.foreground)
		.font_(font).align_(\center)
		.value_(-1).step_(1)
		.action_({ |box|
			var val = box.value % setRPop.items.size;
			setRPop.valueAction_(val);
			box.value_(val)
		});

		if (numItems > 0) {
			flow.nextLine.shift(0, 8);
			proxyGui = this.proxyGuiClass.new(nil, numItems,
				zone, bounds: minSize - (0@52), makeSkip: false);
		};
	}

	proxyGuiClass { ^TaskProxyGui }

	getState {
		if (object.isNil) {
			^(setNames: [], morphVal: 0);
		};

		^(	object: object,
			setNames: object.settings.collect(_.key),
			currSet: object.currSet,
			currIndex: object.currIndex,
			targSet: object.targSet,
			targIndex: object.targIndex,
			morphVal: object.morphVal
		)
	}

	checkUpdate {
		var newState = this.getState;

		if (prevState[\object] != newState[\object]) {
			zone.enabled_(object.notNil);
		};

		if (prevState[\setNames] != newState[\setNames]) {
			setLPop.items = newState[\setNames];
			setRPop.items = newState[\setNames];
		};
		if (prevState[\currIndex] != newState[\currIndex]) {
			setLPop.value = newState[\currIndex];
			setLBox.value = newState[\currIndex];

		};
		if (prevState[\targIndex] != newState[\targIndex]) {
			setRPop.value = newState[\targIndex];
			setRBox.value = newState[\targIndex];
		};

		if (prevState[\morphVal] != newState[\morphVal]) {
			xfader.value_(newState[\morphVal])
		};

		if (proxyGui.notNil) { proxyGui.checkUpdate; };
	}
}

NdefPresetGui : ProxyPresetGui {
	proxyGuiClass { ^NdefGui }
}

PdefPresetGui : ProxyPresetGui {
	proxyGuiClass { ^PdefGui }
}

TdefPresetGui : ProxyPresetGui {
	proxyGuiClass { ^TdefGui }
}