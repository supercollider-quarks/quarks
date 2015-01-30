KeyCodesEditor : KeyDownActions {

	classvar all;
	var <window, <eas;

	*initClass {
		all = List.new;
	}

	*new { |parent, bounds|
		^super.new.init(parent, bounds);
	}

	init { |parent, bounds|
		var platform, scrollArea, scrollView, flow;
		var editAreasBg, staticTextColor, staticTextFont, shortCutFont, textFieldFont;
		var makeEditArea, editArea;
		var thisBounds;

		editAreasBg = Color(0.8, 0.8, 0.8);
		staticTextColor = Color(0.1, 0.1, 0.1);
		staticTextFont = Font(Font.available("Arial") ? Font.defaultSansFace, 10);
		shortCutFont = Font(Font.available("Arial") ? Font.defaultSansFace, 12, true);
		textFieldFont = Font(Font.available("Courier New") ? Font.defaultSansFace, 10);

		Platform.case(
			\osx, { platform = "OSX" },
			\linux, { platform = "Linux" },
			\windows, { platform = "Windows" },
			{ platform = "an unknown platform" }
		);

		switch(GUI.id,
			\cocoa, { platform = platform+"[Cocoa]" },
			\qt, { platform = platform+"[Qt]" },
			\swing, { platform = platform+"[SwingOSC]" }
		);

		bounds !? { thisBounds = bounds };

		if(parent.isNil) {
			window = Window("key-codes and modifiers for"+platform, thisBounds ?? { thisBounds = Rect(
				Window.screenBounds.width-600/2,
				Window.screenBounds.height-600/2,
				600, 600
			) });
		} { window = parent; thisBounds = window.bounds };

		window.onClose_({ all.remove(this) });

		scrollArea = ScrollView(window.view, Point(
			thisBounds.width, thisBounds.height
		)).hasHorizontalScroller_(false).hasVerticalScroller_(true).background_(editAreasBg).hasBorder_(false);

		scrollView = CompositeView(scrollArea, Point(
			scrollArea.bounds.width, scrollArea.bounds.height
		));

		scrollView.decorator = flow = FlowLayout(scrollView.bounds, 8@8, 0@0);

		makeEditArea = { |dictName, dict, height|
			var editArea, name, key;

			name = StaticText(scrollView, Point(
				flow.indentedRemaining.width, 17
			)).font_(shortCutFont).stringColor_(staticTextColor).string_(dictName);

			flow.nextLine;
			editArea = TextView(scrollView, Point(
				flow.indentedRemaining.width-15, height
			)).font_(textFieldFont).syntaxColorize.hasVerticalScroller_(true);

			editArea.string = "IdentityDictionary[\n";
			dict.pairsDo({ |k, v|
				switch(k.class,
					Symbol, { key = "'"++k++"'" },
					Char, { key = "$"++k }
				);
				editArea.string_(editArea.string++"    "++key+"->"+v++",\n");
			});
			editArea.string_(editArea.string++"];");

			flow.nextLine.shift(0, 5);
			[name.bounds.height+editArea.bounds.height+16, editArea];
		};

		eas = ();

		eas.keyCodes = makeEditArea.("KeyDownActions.keyCodes", keyCodes, 400);
		// "eas.keyCodes: %\n".postf(eas.keyCodes);
		scrollView.bounds = Rect(0, 0, scrollView.bounds.width, eas.keyCodes[0]);

		if(GUI.id !== \cocoa) {
			eas.modifiersQt = makeEditArea.("KeyDownActions.modifiersQt", modifiersQt, 100);
			scrollView.bounds = Rect(0, 0, scrollView.bounds.width, scrollView.bounds.height+eas.modifiersQt[0]);

			if(arrowsModifiersQt !== modifiersQt) {
				eas.arrowsModifiersQt = makeEditArea.("KeyDownActions.arrowsModifiersQt", arrowsModifiersQt, 100);
				scrollView.bounds = Rect(0, 0, scrollView.bounds.width, scrollView.bounds.height+eas.arrowsModifiersQt[0]);
			}
		} {
			eas.modifiersCocoa = makeEditArea.("KeyDownActions.modifiersCocoa", modifiersCocoa, 100);
			scrollView.bounds = Rect(0, 0, scrollView.bounds.width, scrollView.bounds.height+eas.modifiersCocoa[0]);

			if(arrowsModifiersCocoa !== modifiersCocoa) {
				eas.arrowsModifiersCocoa = makeEditArea.("KeyDownActions.arrowsModifiersCocoa", arrowsModifiersCocoa, 100);
				scrollView.bounds = Rect(0, 0, scrollView.bounds.width, scrollView.bounds.height+eas.arrowsModifiersCocoa[0]);
			}
		};

		parent ?? { window.front };
		all.add(this);
	}

	result { |write=true|
		var res = IdentityDictionary.new, tmp;
		var keyCodesPath, pform;

		Platform.case(
			\osx, { pform = "OSX" },
			\linux, { pform = "Linux" },
			\windows, { pform = "Windows" },
			{ pform = "NN" }
		);

		keyCodesPath = this.class.filenameSymbol.asString.dirname +/+ "keyCodesAndMods"++pform;

		if((tmp = eas.keyCodes[1].string.interpret).size > 0) { res.put(\keyCodes, tmp) };
		if(GUI.id !== \cocoa, {
			if((tmp = eas.modifiersQt[1].string.interpret).size > 0, { res.put(\modifiersQt, tmp) });
			eas.arrowsModifiersQt !? {
				if(eas.arrowsModifiersQt[1].string.interpret.size > 0 and:{
					tmp !== eas.arrowsModifiersQt[1].string.interpret
				}) {
					res.put(\arrowsModifiersQt, eas.arrowsModifiersQt[1].string.interpret)
				} { res.arrowsModifiersQt = res.modifiersQt };
			}
		}, {
			if((tmp = eas.modifiersCocoa[1].string.interpret).size > 0) { res.put(\modifiersCocoa, tmp) };
			eas.arrowsModifiersCocoa !? {
				if(eas.arrowsModifiersCocoa[1].string.interpret.size > 0 and:{
					tmp !== eas.arrowsModifiersCocoa[1].string.interpret
				}) {
					res.put(\arrowsModifiersCocoa, eas.arrowsModifiersCocoa[1].string.interpret)
				} { res.arrowsModifiersCocoa = res.modifiersCocoa }
			}
		});

		// if on OSX build dummies for other GUI-schemes (cocoa or qt)
		if(pform == "OSX") {
			switch(GUI.id,
				\cocoa, {
					res.put(\modifiersQt, this.class.modifiersQt);
					res.put(\arrowsModifiersQt, this.class.arrowsModifiersQt);
				},
				\qt, {
					res.put(\modifiersCocoa, this.class.modifiersCocoa);
					res.put(\arrowsModifiersCocoa, this.class.arrowsModifiersCocoa);
				}
			)
		};


		if(write) { ^res.writeArchive(keyCodesPath) } { ^res }
	}

}
