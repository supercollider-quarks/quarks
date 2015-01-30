KeyDownActionsEditor : KeyDownActions {

	classvar all, <cachedScrollViewSC;
	var <window, <tmpShortcuts, <shortcutFields, <shortcutTexts, <funcFields, <editAreas, <editButs;

	*initClass {
		all = List.new;
	}

	*new { |parent, name, bounds, shortcutsDict, showMods=true|
		^super.new.init(parent, name, bounds, shortcutsDict, showMods);
	}

	init { |parent, name, bounds, shortcutsDict, showMods|
		var scrollArea, scrollView, butArea, newBut, saveBut;
		var removeButs, makeEditArea;
		var scrollFlow, editFlows, butFlow;
		var editAreasBg, staticTextColor, staticTextFont, shortCutFont, textFieldFont;
		var order, orderedShortcuts;
		var tmpEditFlow, tmpIndex, join = " + ", mods;
		// vars for makeEditArea
		var count, rmBounds;
		var thisBounds;
		var thisArrowsModifiers, thisModifiers;

		Platform.case(
			\osx, {
				switch(GUI.id,
					\qt, {
						thisModifiers = modifiersQt;
						thisArrowsModifiers = arrowsModifiersQt;
					},
					\cocoa, {
						thisModifiers = modifiersCocoa;
						thisArrowsModifiers = arrowsModifiersCocoa;
					}
				)
			},
			{
				thisModifiers = modifiersQt;
				thisArrowsModifiers = arrowsModifiersQt;
			}
		);

		editAreasBg = Color(0.8, 0.8, 0.8);
		staticTextColor = Color(0.1, 0.1, 0.1);
		staticTextFont = Font(Font.available("Arial") ? Font.defaultSansFace, 10);
		shortCutFont = Font(Font.available("Arial") ? Font.defaultSansFace, 12, true);
		textFieldFont = Font(Font.available("Courier New") ? Font.defaultSansFace, 10);

		bounds !? { thisBounds = bounds.asRect };

		if(parent.isNil) {
			window = Window("shortcut editor:"+name, thisBounds ?? { thisBounds = Rect(
				Window.screenBounds.width-600/2,
				Window.screenBounds.height-600/2,
				600, 600
			) });
		} { window = parent };

		window.onClose_({
			all.remove(this);
			cachedScrollViewSC !? {
				ScrollView.globalKeyDownAction_(cachedScrollViewSC);
			}
		});

		#editAreas, editFlows, shortcutTexts, shortcutFields, editButs, removeButs, funcFields, tmpShortcuts = []!8;

		// "thisBounds: %\n".postf(thisBounds);

		scrollArea = ScrollView(window.asView, Rect(
			0, 0, thisBounds.width, thisBounds.height-23
		)).hasHorizontalScroller_(false).background_(editAreasBg).hasBorder_(false);
		butArea = CompositeView(window.asView, Rect(
			0, thisBounds.height-23, thisBounds.width, 23
		)).background_(editAreasBg);

		scrollView = CompositeView(scrollArea, Rect(
			0, 0, scrollArea.bounds.width, scrollArea.bounds.height
		));

		scrollView.decorator = scrollFlow = FlowLayout(scrollView.bounds, 0@0, 1@0);

		makeEditArea = { |shortcut, funcString|
			var editArea, shortcutText, shortcutField, editBut, removeBut, funcField, myCount;

			editAreas = editAreas.add(
				editArea = CompositeView(
					scrollView, Point(scrollFlow.bounds.width-16, 100)
				).background_(editAreasBg)
			);

			count = editAreas.size-1;
			myCount = count;

			scrollView.bounds_(Rect(
				scrollView.bounds.left,
				scrollView.bounds.top,
				scrollView.bounds.width,
				myCount+1 * 100
			));

			editArea.decorator = tmpEditFlow = FlowLayout(editArea.bounds, 7@7, 2@2);

			tmpShortcuts = tmpShortcuts.add(nil);

			shortcutTexts = shortcutTexts.add(
				shortcutText = StaticText(editArea, 40@15)
					.string_("shortcut:")
					.font_(staticTextFont)
					.stringColor_(staticTextColor)
					.canFocus_(false)
				;
			);

			shortcutFields = shortcutFields.add(
				shortcutField = StaticText(editArea, tmpEditFlow.indentedRemaining.width-125@15)
					.background_(Color.white)
					.font_(shortCutFont)
					.stringColor_(staticTextColor)
					.canFocus_(false)
				;
			);

			shortcut !? {
				shortcutField.string_(" "++shortcut);
			};

			editButs = editButs.add(
				editBut = Button(editArea, 60@15)
					.states_([
						["edit", staticTextColor],
						["end edit", staticTextColor]
					])
					.font_(staticTextFont)
					.action_({ |bt|
						switch(bt.value,
							1, {
								all.do({ |ed|
									ed.editAreas.do(_.background_(editAreasBg));
									ed.editButs.do(_.value_(0));
									ed.shortcutTexts.do(_.stringColor_(staticTextColor));
									ed.funcFields.do(_.enabled_(false));
								});
								editBut.value_(1);

								cachedScrollViewSC = ScrollView.globalKeyDownAction;

								ScrollView.globalKeyDownAction_({ |view, char, mod, unicode, keycode, key|
									if(keyCodes.findKeyForEqualValue(keycode).notNil) {
										char !? {
											if(thisModifiers.includes(mod) and:{
												thisModifiers.findKeyForValue(mod) != \none
											}, {
												mods = thisModifiers.findKeyForValue(mod);
											}) {
												if(thisArrowsModifiers.includes(mod) and:{
													thisArrowsModifiers.findKeyForValue(mod) != \none
												}) {
													mods = thisArrowsModifiers.findKeyForValue(mod);
												}
											};
											if(showMods.asBoolean and:{
												mod.notNil and:{
													mod != thisModifiers[\none] and:{
														mod != thisArrowsModifiers[\none]
													}
												}
											}) {
											// "mods should be considered".postln;
												shortcutField.string_(
													" "++ mods ++ join ++
													keyCodes.findKeyForValue(keycode)
												);
												if(thisArrowsModifiers.includes(mod) and:{ thisModifiers.includes(mod).not }) {
													if(GUI.id !== \cocoa) {
														tmpShortcuts[myCount] = (mods ++ join ++ keyCodes.findKeyForValue(keycode)).asSymbol -> (
															func: funcField.string,
															keyCode: keycode,
															arrowModifierCocoa: nil,
															arrowModifierQt: mod,
															modifierCocoa: nil,
															modifierQt: nil
														)
													} {
														tmpShortcuts[myCount] = (mods ++ join ++ keyCodes.findKeyForValue(keycode)).asSymbol -> (
															func: funcField.string,
															keyCode: keycode,
															arrowModifierCocoa: mod,
															arrowModifierQt: nil,
															modifierCocoa: nil,
															modifierQt: nil
														)
													};
												} {
													if(GUI.id !== \cocoa) {
														tmpShortcuts[myCount] = (mods ++ join ++ keyCodes.findKeyForValue(keycode)).asSymbol -> (
															func: funcField.string,
															keyCode: keycode,
															arrowModifierCocoa: nil,
															arrowModifierQt: nil,
															modifierCocoa: nil,
															modifierQt: mod
														)
													} {
														tmpShortcuts[myCount] = (mods ++ join ++ keyCodes.findKeyForValue(keycode)).asSymbol -> (
															func: funcField.string,
															keyCode: keycode,
															arrowModifierCocoa: nil,
															arrowModifierQt: nil,
															modifierCocoa: mod,
															modifierQt: nil
														)
													}
												}
											} {
												shortcutField.string_(
													" "++
													keyCodes.findKeyForValue(keycode)
												);
												tmpShortcuts[myCount] = (keyCodes.findKeyForValue(keycode)).asSymbol -> (
													func: funcField.string,
													keyCode: keycode,
													arrowModifierCocoa: nil,
													arrowModifierQt: nil,
													modifierCocoa: nil,
													modifierQt: nil
												);
											}
										}
									}
								});
								funcField.enabled_(true);
								editArea.background_(Color.red);
								shortcutText.stringColor_(Color.white);
							},
							0, {
								ScrollView.globalKeyDownAction_(cachedScrollViewSC);

								funcField.enabled_(false);
								editArea.background_(editAreasBg);
								shortcutText.stringColor_(staticTextColor);
							}
						)
					})
				;
			);

			removeButs = removeButs.add(
				removeBut = Button(editArea, 60@15)
					.states_([["remove", staticTextColor]])
					.action_({ |bt|
						tmpIndex = editAreas.detectIndex({ |it| it == bt.parent });
						rmBounds = bt.parent.bounds;
						bt.parent.remove;
						editAreas.removeAt(tmpIndex);
						tmpShortcuts.remove(tmpShortcuts[tmpIndex]);
						editAreas.do({ |it|
							if(it.bounds.top > rmBounds.top, {
								it.bounds_(Rect(
									it.bounds.left,
									it.bounds.top-100,
									it.bounds.width,
									it.bounds.height
								))
							})
						});
						scrollView.bounds_(Rect(
							scrollView.bounds.left,
							scrollView.bounds.top,
							scrollView.bounds.width,
							scrollView.bounds.height
						))
					})
					.font_(staticTextFont)
				;
			);
			tmpEditFlow.nextLine;
			funcFields = funcFields.add(
				funcField = TextView(
					editArea,
					Point(tmpEditFlow.indentedRemaining.width, tmpEditFlow.indentedRemaining.height)
				).font_(textFieldFont).enabled_(false).syntaxColorize.action_({ |ffield|
					tmpShortcuts[myCount].value.func = funcField.string;
				});
				if(GUI.id !== \cocoa) { funcField.tabWidth_(20) };
			);
			funcString !? { funcFields[myCount].string_(funcString) };
		};

		shortcutsDict !? {
			order = shortcutsDict.order;

			order.do({ |shortcut, i|
				makeEditArea.(shortcut, shortcutsDict[shortcut][\func].replace("\t", " "));
				tmpShortcuts[i] = shortcut -> (
					func: shortcutsDict[shortcut][\func],
					keyCode: shortcutsDict[shortcut][\keyCode],
					modifierQt: shortcutsDict[shortcut][\modifierQt],
					modifierCocoa: shortcutsDict[shortcut][\modifierCocoa],
					arrowModifierQt: shortcutsDict[shortcut][\arrowModifierQt],
					arrowModifierCocoa: shortcutsDict[shortcut][\arrowModifierCocoa]
				)
			})
		};

		butArea.decorator = butFlow = FlowLayout(butArea.bounds, 7@4, 3@0);

		newBut = Button(butArea, 70@15)
			.font_(staticTextFont)
			.states_([["new action", staticTextColor]])
			.action_({ |bt|
				editAreas.do({ |cview|
					cview.bounds_(Rect(
						cview.bounds.left,
						cview.bounds.top+100,
						cview.bounds.width,
						cview.bounds.height
					));
				});
				scrollFlow.reset;
				makeEditArea.(funcString: "{ |view| /*do something */ }");
			})
		;
		parent ?? { window.front };
		all.add(this);
	}

	result {
		var res;
		res = IdentityDictionary.new;
		tmpShortcuts.do({ |it| res.put(it.key, it.value) });
		// "res: %\n".postf(res.cs);
		^res;
	}

	setShortcuts { |view|
		var thisMod, thisArrMod;
		var modsDict, arrModsDict, arrowKeys;

		switch(GUI.id,
			\cocoa, {
				modsDict = KeyDownActions.modifiersCocoa;
				arrModsDict = KeyDownActions.arrowsModifiersCocoa;
			},
			\qt, {
				modsDict = KeyDownActions.modifiersQt;
				arrModsDict = KeyDownActions.arrowsModifiersQt;
			}
		);

		arrowKeys = [
			KeyDownActions.keyCodes['arrow up'],
			KeyDownActions.keyCodes['arrow down'],
			KeyDownActions.keyCodes['arrow left'],
			KeyDownActions.keyCodes['arrow right']
		];

		view.keyDownAction_(nil);

		this.result.do({ |keyDowns|

			view.keyDownAction_(
				view.keyDownAction.addFunc({ |view, char, modifiers, unicode, keycode|
					switch(GUI.id,
						\cocoa, {
							thisMod = keyDowns.modifierCocoa;
							thisArrMod = keyDowns.arrowsModifierCocoa;
						},
						\qt, {
							thisMod = keyDowns.modifierQt;
							thisArrMod = keyDowns.arrowsModifierQt;
						}
					);

					case
						{ modifiers == modsDict[\none] or:{ modifiers == arrModsDict[\none] }} {
							if(keycode == keyDowns.keyCode and:{
								thisMod.isNil and:{ thisArrMod.isNil }
							}) {
								keyDowns.func.interpret.value(view, char, modifiers, unicode, keycode)
							};
						}
						{
							(char !== 0.asAscii).or(arrowKeys.includes(keycode)) and:{
								modifiers != modsDict[\none] and:{
									modifiers != arrModsDict[\none]
								}
							}
						} {
							if(keycode == keyDowns.keyCode and:{
								(modifiers == thisArrMod).or(modifiers == thisMod)
							}) {
								keyDowns.func.interpret.value(view, char, modifiers, unicode, keycode)
							}
						}
					;
				})
			)

		})
	}

}
