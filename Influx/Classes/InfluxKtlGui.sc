InfluxKtlGui : JITGui {

	var <leftTopV, <leftBotV, <leftButtonV, <rightButtonV;
	var <attachButtons, <xpop, <ypop, <xySlider, <loopGui;
	var <xyMapDict;

	*new { |object, numItems = 5, parent, bounds, makeSkip = true, options = #[]|
		^super.new(nil, numItems, parent, bounds, makeSkip, options)
		.object_(object);
	}

	accepts { |obj| ^obj.isNil or: obj.isKindOf(Influx) }

	setDefaults { |options|
		var minHeight = (numItems + 6 * (skin.buttonHeight + 2) + (skin.margin.y * 4));
		defPos = 530@660;
		if (options.includes(\loop)) { minHeight = minHeight + 200 };
		minSize = 330 @ minHeight;
	}

	winName { ^"Influx" + (try { object.key } ? "") }

	makeViews { |options|

		if (hasWindow.not) { this.makeNameView(400, 20); };
		this.name_("Influx ...");

		leftTopV = HLayoutView(zone, Rect(0, 0, zone.bounds.width, 240))
		.background_(Color.green.alpha_(0.2));

		leftButtonV = VLayoutView(leftTopV, Rect(0, 0, zone.bounds.width * 0.375, 240) );
		rightButtonV = VLayoutView(leftTopV, Rect(0, 0, zone.bounds.width * 0.615, 240) );

		this.makeButtons;
		this.makeSlider;

		if (options.includes(\loop)) {
			loopGui = KtlLoopGui(KtlLoop(\testXYZ), 0, parent: zone, bounds: 310 @ 180);
			loopGui.taskGui.name_("KtlLoop for Influx");
		};
	}

	makeNameView { |nameWid, height|
		nameView = StaticText(zone, Rect(0,0, nameWid, height))
			.font_(font).align_(0);
	}

	makeButtons { |options|
		var butWid = leftButtonV.bounds.width;
		var butH = skin.buttonHeight + 2;

		StaticText(leftButtonV, Rect(0, 0, butWid, butH))
		.align_(\center).string_("Change  weights:");
		[
			["set to diagL",{ object.setwPre(\diagL); }],
			["disentangle",{ object.disentangle(0.3); }],
			["entangle",{ object.entangle(0.3); }],
			["RANDOM",{  object.rand(1.0); }]
		].collect { |labFunc|
			Button(leftButtonV, Rect(0, 0, butWid, butH))
			.states_([[labFunc[0]]]).action_(labFunc[1]);
		};

		StaticText(leftButtonV, Rect(0, 0, butWid, butH))
		.align_(\center).string_("Influence objects:");

		attachButtons = numItems.collect { |i|
			var name =i.asString;
			Button(leftButtonV, Rect(0, 0, butWid, butH))
			.states_([[name], [name, Color.white, Color.green(0.62)]])
			.action_({ |b, modif| this.influxFunc(i, b.value, modif); });
		};
	}

	makeSlider {
		var butH = skin.buttonHeight + 2;
		var rightWid = rightButtonV.bounds.width;
		var topLine = HLayoutView(rightButtonV, Rect(0, 0, rightWid, butH));

		xyMapDict = (x: \x, y: \y);

		xpop = EZPopUpMenu(topLine, (rightWid * 0.37)@butH, "x:", labelWidth: 12)
		.items_([\x, \y])
		.globalAction_({ |pop|
			xyMapDict.put(\x, object.inNames[pop.value]).postln
		});

		StaticText(topLine, Rect(0, 0, rightWid * 0.26, butH)).align_(\center)
		.string_("Inputs:");

		ypop = EZPopUpMenu(topLine, (rightWid * 0.37)@butH, "y:", labelWidth: 12)
		.items_([\x, \y]).value_(1).globalAction_({ |pop|
			xyMapDict.put(\y, object.inNames[pop.value]).postln
		});

		xySlider = Slider2D(rightButtonV, Rect(0, 0, rightWid, rightWid))
		.x_(0.5).y_(0.5)
		.background_( Color.new255(200, 100, 0) )
		.action_({|sl| this.slAction(sl); });

		xySlider.keyDownAction_({ |sl, ch, mod| this.slKeydown(ch, mod); });
	}

	setButton { |index, name, func|
		var but = attachButtons[index];
		but.action_(func);
		but.states_(but.states.collect { |state| state[0] = name });
		but.refresh;
	}

	attachToButton { |index, proxy, mapped = true|
		var name = proxy.key;
		this.setButton(index, name, { |bt, modif|
			if (bt.value > 0) {
				if (mapped) {
					object.attachMapped(proxy);
				} {
					object.attachDirect(proxy);
				};
				proxy.play;
			} {
				if (modif.isAlt) { proxy.stop; };
				object.detach(name);
			};
		});
	}

	// could do func lookup here - maybe later
	influxFunc { |index, butVal, modif|
		thisMethod.postln;
		[index, butVal, modif].postln;
	}

	slKeydown { |char, modif|
		thisMethod.postln;
		if (object.notNil) {
			char.switch(
				$o, { object.rec.toggleRec },
				$p, { object.rec.togglePlay },
				$ , { object.rec.togglePlay },
				$l, { object.rec.toggleLooped }
			);
		};
	}

	slAction { |sl|

		if (object.notNil) {
			// how to attach KtlLoop to Influx? an instvar?
			// // recording into KtlLoop here
			// if (object.rec.notNil) {
			// object.rec.recordEvent((type: \set, x: sl.x, y: sl.y)); };
			// // // and this is the normal set function
			// // // bipolar mapping here done by hand

			object.set(xyMapDict[\x], sl.x.unibi, xyMapDict[\y], sl.y.unibi);
		}
	}


	checkUpdate {
		var newState = this.getState;

		if (newState[\object].notNil) {
			if (newState[\object] != prevState[\object]) {
				zone.enabled_(true);
				this.name_(this.getName);
				xyMapDict.put(\x, object.inNames[0]);
				xyMapDict.put(\y, object.inNames[1]);
				[xpop, ypop].do { |pop, i|
					pop.items_(object.inNames).valueAction_(i);
				};
			};

			xpop.value_(object.inNames.indexOf(xyMapDict[\x]));
			ypop.value_(object.inNames.indexOf(xyMapDict[\y]));

			xySlider.setXY(
				object.inValDict[xyMapDict[\x]].biuni,
				object.inValDict[xyMapDict[\y]].biuni
			);
		} {
			zone.enabled_(false);
		};

		prevState = newState;
	}
}
