ProxyChainGui : JITGui {
	var <guiFuncs; // move to classvar!
	var <butZone, <buttonSpecs, <buttons, <namedButtons, <editGui;

	*new { |chain, numItems = 16, parent, bounds, makeSkip = true, options|

		options = options ?? { if (chain.notNil) { chain.slotNames.asArray } };
		^super.new(nil, numItems, parent, bounds, makeSkip, options)
			.chain_(chain)
	}

	accepts { |obj| ^(obj.isNil or: { obj.isKindOf(ProxyChain) }) }

	chain_ { |chain| ^this.object_(chain) }
	chain { ^object }

	setDefaults { |options|
		if (parent.isNil) {
			defPos = 610@260
		} {
			defPos = skin.margin;
		};
		minSize = 510 @ (numItems * skin.buttonHeight + (skin.headHeight * 2));
	//	"minSize: %\n".postf(minSize);
	}

	makeViews { |options|

		namedButtons = ();

		// "PCGui:makeViews: options are %\n\n".postf(options);

		options = options ?? { if(object.notNil) { object.slotNames.asArray } };

		guiFuncs =  (
			btlabel: { |but, name| but.states_([[name, Color.black, Color(1, 0.5, 0)]]) },
			label: { |but, name| but.states_([[name, Color.white, Color(1, 0.5, 0)]]) },
			slotCtl: { | but, name, level=0|
				but.states_([["[" + name + "]"], [name, Color.black, Color.green(5/7)], ]);
				but.action_({ |but|
					[ { this.chain.remove(name) }, { this.chain.add(name, level) } ][but.value].value
				});
			},

			extra: { |but, name, func|
				but.states_([[name, Color.black, Color(1, 0.7, 0)]]);
				but.action_(func);
			}
		);

		butZone = CompositeView(zone, Rect(0,0, 110, bounds.height - (skin.margin.y * 2)));
		butZone.addFlowLayout;
		buttons = numItems.collect { Button.new(butZone, Rect(0,0, 100, skin.buttonHeight)).states_([["-"]]); };

		this.buttons_(options.asArray);

		this.makeEditGui;
	}

	makeEditGui { editGui = NdefGui(nil, numItems, zone); }

	buttons_ { |specs|

		var objSlotNames = if (object.notNil) { object.slotNames.asArray } { [] };

		specs = (specs ? []);
		if (specs.size > buttons.size) {
			"ProxyChainGui: out of buttons... fix later".postln;
		};

		buttons.do { |but, i|
			var name, kind, func, setup;
			var list = specs[i];
			but.visible_(list.notNil);

			if (list.notNil) {
				#name, kind, func, setup = list.asArray;
				kind = kind ? \slotCtl;
				if (name.notNil) {
					guiFuncs[kind].value(but, name, func);
					setup.value(this, but);
				};
				but.enabled_(name.notNil);
			}
		};

		buttonSpecs = specs;
	}

	getState {
		var state = (object: object, slotsInUse: [], slotNames: []);
		if (object.notNil) {
			state
				.put(\slotsInUse, object.slotsInUse.asArray)
				.put(\slotNames, object.slotNames.asArray)
		};
		^state
	}

	checkUpdate {
		var newState = this.getState;

		if (newState[\object].isNil) {
			this.name_('none');
			editGui.object_(object);
			butZone.enabled_(false);

			prevState = newState;
			^this
		};

		if (newState == prevState) { ^this };

		if (newState[\object] != prevState[\object]) {
			this.name_(object.key);
			butZone.enabled_(true);
			editGui.object_(object.proxy);
			editGui.name_(object.key);
		} {
			editGui.checkUpdate;
		};

		if (newState[\slotNames] != prevState[\slotNames]) {
		//	"new slotnames: ".post; newState[\slotNames].postcs;

			namedButtons = ();
			buttons.do { |but|
				var butname = but.states[0][0].asString.drop(2).drop(-2).asSymbol;
			//	[\butname, butname].postcs;
				if (newState[\slotNames].includes(butname)) {
					namedButtons.put(butname, but);
				};
			};

			object.slotNames.do { |name, i|
				editGui.addReplaceKey(("wet" ++ i).asSymbol, name, \amp.asSpec);
				editGui.addReplaceKey(("mix" ++ i).asSymbol, name, \amp.asSpec);
			};
		};

		if (newState[\slotsInUse] != prevState[\slotsInUse]) {
			namedButtons.keysValuesDo { |name, but|
				but.value_(newState[\slotsInUse].includes(name).binaryValue);
			}
		};

		prevState = newState;
	}
}

MasterFXGui : ProxyChainGui {

	name_ { |name|
		if (hasWindow) { parent.name_(name.asString) };
	}

	makeEditGui {
		var editGuiOptions = [ 'CLR', 'reset', 'doc', 'fade', 'wake', 'end', 'pausR', 'sendR' ];
		editGui = NdefGui(nil, numItems, zone, bounds: 400@0, options: editGuiOptions);
	}
}

