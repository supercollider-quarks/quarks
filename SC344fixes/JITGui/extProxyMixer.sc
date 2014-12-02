// support an option \small for 800x600 screens. 

+ NdefGui { 
		// smaller fader
	*audioSm { 
			// one line, for small ProxyMixer arZone
		^[\monitor, \playN, \name, \pausR, \sendR, \ed]
	}
}

+ ProxyMixer { 
	
	* small { |obj, numItems = 16, parent, bounds, makeSkip = true| 
		^this.new(obj, numItems, parent, bounds, makeSkip, [\small]);
	}

	setDefaults { |options| 
		var width = 600; 
		var height = numItems * skin.buttonHeight + skin.headHeight + 25;
		
		skin = GUI.skins.jit;
		font = Font(*skin.fontSpecs);

		defPos = 10@260;
		
		if (options.notNil and: { options.includes(\small) }) { 
			sizes = (
				small: (396 @ height), 
				mid: (626 @ height),
				big: (800 @ height)
			);
		} {
			sizes = (
				small: (446 @ height), 
				mid: (676 @ height),
				big: (1080 @ height)
			);
		};
		
		minSize = sizes[\big];
	}

	makeViews { |options| 
		var isSmall = options.notNil and: { options.includes(\small) };
		var openEditBut;
		var arZoneWidth = if (isSmall, 444 - 50, 444);

		parent.bounds_(parent.bounds.extent_(sizes[\mid] + (8@8)));
		
		zone.decorator.gap_(4@4);
		zone.resize_(1).background_(Color.grey(0.7));
		arZone = CompositeView(zone, Rect(0, 0, arZoneWidth, sizes[\mid].y ))
			.background_(skin.foreground);
		arZone.addFlowLayout(skin.margin, skin.gap);
			
		krZone = CompositeView(zone, Rect(0, 0, 225, sizes[\mid].y ))
			.background_(skin.foreground);
		krZone.addFlowLayout(skin.margin, skin.gap);
		
		this.makeTopLine;
		openEditBut = arZone.children[4];

		arZone.decorator.nextLine.shift(0, 10); 
		this.makeArZone(isSmall);
		
		this.makeKrZone; 
		this.setEdButs(isSmall);
		
		if (isSmall) { 
			// put editGui in the same place as krZone
			zone.decorator.left_(krZone.bounds.left).top_(krZone.bounds.top);
				// change openEditButton action:
			openEditBut.action = { |but| this.switchSize(but.value, true) };
		};

		editZone = CompositeView(zone, Rect(0, 0, 400, sizes[\mid].y ))
			.background_(skin.foreground);
		editZone.addFlowLayout(0@0, 0@0);
		
		if (isSmall) { editZone.visible_(false) };

		this.makeEditZone;
		
	}

	makeArZone { |isSmall = false| 
		var ndefOptions = if (isSmall) { NdefGui.audioSm } { NdefGui.audio };
		var arLayout = arZone.decorator;

		var dim = ((arZone.bounds.width - 20)@skin.buttonHeight);
		
		arLayout.nextLine;
		arLayout.shift(0,4);
		
		arGuis = numItems.collect { 
			NdefGui(nil, 0, arZone, dim, makeSkip: false, options: ndefOptions) 
		};
				
		arLayout.top_(40).left_(arZone.bounds.width - 15);
		arScroller = EZScroller.new(arZone,
			Rect(0, 0, 12, numItems * skin.buttonHeight),
			numItems, numItems,
			{ |sc| arKeysRotation = sc.value.asInteger.max(0); this.checkUpdate }
		).value_(0).visible_(true);
	}
	
	switchSize { |index, hideZones = false| 
		parent.bounds_(parent.bounds.extent_(sizes[[\small, \mid, \big][index]] + (6@10)));
		if (hideZones) { 
			index.asInteger.switch(
				0, { krZone.visible_(false); editZone.visible_(false) }, 
				1, { krZone.visible_(true);  editZone.visible_(false) }, 
				2, { krZone.visible_(false); editZone.visible_(true)  } 
			);
		};
	}

	setEdButs { |isSmall = false| 
		(arGuis ++ krGuis).do { |pxgui|
			pxgui.edBut.states_([
					["ed", Color.black, Color.grey(0.75)],
					["ed", Color.black, Color.white]])

				.action_({ arg btn, mod; 
					if (mod.notNil and: { mod.isAlt }) { 
						NdefGui(pxgui.object);
					} { 
						this.switchSize(2, isSmall);
						editGui.object_(pxgui.object);
						arGuis.do { |gui| gui.edBut.value_(0) };
						krGuis.do { |gui| gui.edBut.value_(0) };
						btn.value_(1);
					};
				});
		};
	}
}