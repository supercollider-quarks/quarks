/* (c) 2010-2013 Stefan Nussbaumer */
/*
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*/

CVCenterPreferences {

	classvar <window;

	// *initClass {
	// 	Class.initClassTree(KeyDownActions);
	// }

	*dialog {
		var labelColors, labelStringColors, tabs, scTab, prefsTab, scTabs, flow1, flow2, saveCancel, saveCancelFlow;
		var guiView, guiFlow, midiView, midiFlow, responderView, responderFlow;
		var tabFont, staticTextFont, staticTextColor, textFieldFont, textFieldFontColor, textFieldBg, tabsBg;
		// shortcut-tabs
		var prefShortcutsPath, prefShortCuts;
		var cvCenterTab, cvWidgetTab, cvWidgetEditorTab, globalShortcutsTab, cvKeyCodesEditorTab;
		var cvCenterEditor, cvWidgetEditor, cvWidgetEditorEditor, globalShortcutsEditorTab, cvCenterKeyCodesEditor;
		var saveGuiPosition, leftText, left, topText, top, widthText, width, heightText, height;
		var saveClassVars, removeResponders;
		var initMidiOnStartUp, initMidiText;
		var saveMidiMode, saveMidiResolution, saveCtrlButtonBank, saveMidiMean, saveSoftWithin;
		var textMidiMode, textMidiResolution, textCtrlButtonBank, textMidiMean, textSoftWithin;
		var prefSaveGuiProps, buildCheckbox, buildNumTextBox, vHeight;
		var cvcBounds, propsText, classVarsText;
		var saveBut;
		var fFact, specialHeight;
		var prefs, rect, shortcuts;

		prefs = this.readPreferences;

		if(GUI.id === \cocoa, { fFact = 0.9 }, { fFact = 1 });

		tabFont = Font(Font.available("Arial") ? Font.defaultSansFace, 12, true);
		staticTextFont = Font(Font.available("Arial") ? Font.defaultSansFace, 12 * fFact);
		staticTextColor = Color(0.2, 0.2, 0.2);
		textFieldFont = Font(Font.available("Courier New") ? Font.defaultSansFace, 12);
		textFieldFontColor = Color.black;
		textFieldBg = Color.white;
		tabsBg = Color(0.8, 0.8, 0.8);

		buildCheckbox = { |view, active|
			var cBox;
			if(GUI.id === \cocoa, {
				cBox = Button(view, 15@15)
					.states_([
						["", Color.white, Color.white],
						["X", Color.black, Color.white],
					])
					.font_(Font("Arial Black", 10, true))
				;
				if(active, { cBox.value_(1) }, { cBox.value_(0) });
			}, {
				cBox = \CheckBox.asClass.new(view, 15@15).value_(active);
			});
			cBox;
		};

		buildNumTextBox = { |view, val, kind, width, height, clip|
			var ntBox;
			case
				{ kind === \text } {
					ntBox = TextField(view, (width ?? { 30 }) @ (height ?? { 20 })).string_(val);
				}
				{ kind === \num } {
					ntBox = NumberBox(view, (width ?? { 30 }) @ ( height ?? { 20 }))
						.value_(val)
						.clipLo_(clip[0] ?? { -100 })
						.clipHi_(clip[1] ?? { 100 })
					;
				}
			;
			ntBox.font_(textFieldFont);
		};

		if(CVCenter.window.notNil and:{ CVCenter.window.isClosed.not }, { cvcBounds = CVCenter.bounds });

		if(window.isNil or:{ window.isClosed }, {
			window = Window("CVCenter: preferences", Rect(
				Window.screenBounds.width/2-249,
				Window.screenBounds.height/2-193,
				498, 385
			)).front;

			tabs = TabbedView2(window, Rect(0, 1, window.bounds.width, window.bounds.height-33))
				.tabHeight_(17)
				.tabCurve_(3)
				.labelColors_(Color.white!2)
				.unfocusedColors_(Color.red!2)
				.stringColors_(Color.white!2)
				.stringFocusedColors_(Color.red!2)
				.dragTabs_(false)
				.font_(tabFont)
			;

			// button-area at the bottom ('cancel', 'save')

			saveCancel = CompositeView(window, Rect(0, window.bounds.height-32, window.bounds.width, 32))
				.background_(tabsBg)
			;
			saveCancel.decorator = saveCancelFlow = FlowLayout(saveCancel.bounds, 7@2, 1@0);

			// common preferences

			prefsTab = tabs.add("common preferences", scroll: false).background_(tabsBg);
			scTab = tabs.add("shortcuts", scroll: false).background_(tabsBg);
			cvKeyCodesEditorTab = tabs.add("keycodes & modifiers", scroll: false);

			prefsTab.decorator = flow1 = FlowLayout(prefsTab.bounds, 7@7, 0@1);

			guiView = CompositeView(prefsTab, flow1.indentedRemaining.width@57);

			guiView.decorator = guiFlow = FlowLayout(guiView.bounds, 7@7, 0@3);

			saveGuiPosition = PopUpMenu(guiView, guiFlow.indentedRemaining.width@20)
				.items_([
					"No specific settings for CVCenter window-bounds",
					"Remember CVCenter window-bounds on shutdown / window-close",
					"Remember CVCenter window-bounds as set below"
				])
				.value_(prefs !? { prefs[\saveGuiProperties] } ?? { 0 })
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
			;

			leftText = StaticText(guiView, guiFlow.bounds.width/10@20)
				.string_("left: ")
				.font_(staticTextFont)
				.align_(\right)
			;

			left = buildNumTextBox.(guiView, prefs !? {
				prefs[\guiProperties] !? {
					prefs[\guiProperties].left ?? { cvcBounds.left }
				}
			}, kind: \text, width: 60);

			flow1.shift(0, 0);

			topText = StaticText(guiView, guiFlow.bounds.width/10@20)
				.string_("top: ")
				.font_(staticTextFont)
				.align_(\right)
			;

			top = buildNumTextBox.(guiView, prefs !? {
				prefs[\guiProperties] !? {
					prefs[\guiProperties].top ?? { cvcBounds.top }
				}
			}, kind: \text, width: 60);

			widthText = StaticText(guiView, guiFlow.bounds.width/10@20)
				.string_("width: ")
				.font_(staticTextFont)
				.align_(\right)
			;

			width = buildNumTextBox.(guiView, prefs !? {
				prefs[\guiProperties] !? {
					prefs[\guiProperties].width ?? { cvcBounds.width }
				}
			}, kind: \text, width: 60);

			heightText = StaticText(guiView, guiFlow.bounds.width/10@20)
				.string_("height: ")
				.font_(staticTextFont)
				.align_(\right)
			;

			height = buildNumTextBox.(guiView, prefs !? {
				prefs[\guiProperties] !? {
					prefs[\guiProperties].height ?? { cvcBounds.height }
				}
			}, kind: \text, width: 60);

			if(saveGuiPosition.value == 0 or:{ saveGuiPosition.value == 1 }, {
				[leftText, topText, widthText, heightText].do(_.stringColor_(Color(0.7, 0.7, 0.7, 0.7)));
				[left, top, width, height].do(_.enabled_(false));
				guiView.background_(Color(0.95, 0.95, 0.95));
			}, {
				[leftText, topText, widthText, heightText].do(_.stringColor_(staticTextColor));
				[left, top, width, height].do(_.enabled_(true));
			});

			saveGuiPosition.action_({ |dd|
				if(dd.value == 0 or:{ dd.value == 1 }, {
					[leftText, topText, widthText, heightText].do(_.stringColor_(Color(0.7, 0.7, 0.7, 0.7)));
					[left, top, width, height].do(_.enabled_(false));
					guiView.background_(Color(0.95, 0.95, 0.95));
				}, {
					[leftText, topText, widthText, heightText].do(_.stringColor_(Color.black));
					[left, top, width, height].do(_.enabled_(true));
				})
			});

			if(GUI.id ===\cocoa, { vHeight = 240 }, { vHeight = 236 });

			flow1.nextLine;

			midiView = CompositeView(prefsTab, flow1.indentedRemaining.width@vHeight)
				.background_(Color(0.95, 0.95, 0.95))
			;

			midiView.decorator = midiFlow = FlowLayout(midiView.bounds, 7@7, 0@1);

			if(prefs.notNil, {
				initMidiOnStartUp = buildCheckbox.(midiView, prefs[\initMidiOnStartUp]);
			}, {
				initMidiOnStartUp = buildCheckbox.(midiView, false);
			});

			midiFlow.shift(5, -2);

			initMidiText = StaticText(midiView, midiFlow.indentedRemaining.width@20)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Initialize MIDI on startup.")
			;

			if(GUI.id !== \cocoa, {
				[initMidiOnStartUp, initMidiText].do(_.toolTip_(
					"Select this option to initialize\nMIDI upon SuperCollider-startup."
				));
			});

			midiFlow.nextLine;

			if(prefs.notNil, {
				saveClassVars = buildCheckbox.(midiView, prefs[\saveClassVars]);
			}, {
				saveClassVars = buildCheckbox.(midiView, false);
			});

			midiFlow.shift(5, -2);

			classVarsText = StaticText(midiView, midiFlow.indentedRemaining.width-100@20)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Remember CVCenter's MIDI-classvar-values on shutdown:")
			;

			if(GUI.id !== \cocoa, {
				classVarsText.toolTip_(
					"Selecting this option will make CVCenter remember the current\nvalues for midiMode, midiResolution, midiMean, softWithin and\nctrlButtonBank. These can also be set by entering appropriate\nvalues in the following text-boxes. If this option is not selected\nany of the following values will only be remembered until the\nnext library recompilation.\nNote also that these values get overridden by the corresponding\nsettings in a CVWidget.\nFor more information please have a look at the regarding\nsections in CVCenter's helpfile."
				);
				saveClassVars.toolTip_("Select this option to make CVCenter remember the current values\nof its classvars midiMode, midiResolution, midiMean, softWithin,\nctrlButtonBank on shutdown resp. startup.")
			});

			midiFlow.nextLine.shift(20, 2);

			saveMidiMode = buildNumTextBox.(midiView, prefs !? {
				prefs[\midiMode] } ?? { CVCenter.midiMode }, \text
			);

			midiFlow.shift(5, 2);

			textMidiMode = StaticText(midiView, midiFlow.indentedRemaining.width@20)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set CVCenter's midi-mode (0 or 1).")
			;

			if(GUI.id !== \cocoa, {
				saveMidiMode.toolTip_(
					"Either 0 (hardware-slider output 0-127)\nor 1 (in-/decremental output)."
				)
			});

			midiFlow.nextLine.shift(20, 0);

			saveMidiResolution = buildNumTextBox.(midiView, prefs !? {
				prefs[\midiResolution] } ?? { CVCenter.midiResolution }, \text
			);

			midiFlow.shift(5, 2);

			textMidiResolution = StaticText(midiView, midiFlow.indentedRemaining.width@20)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set CVCenter's midi-resolution. Applies only if midi-mode is 1.")
			;

			if(GUI.id !== \cocoa, {
				saveMidiResolution.toolTip_(
					"A floating point value representing the slider's resolution.\n1 has proven to be a sensible default. Smaller values mean\na higher resolution. Applies only if midiMode is set to 1."
				)
			});

			midiFlow.nextLine.shift(20, 0);

			saveMidiMean = buildNumTextBox.(midiView, prefs !? {
				prefs[\midiMean] } ?? { CVCenter.midiMean }, \text
			);

			midiFlow.shift(5, 2);

			textMidiMean = StaticText(midiView, midiFlow.indentedRemaining.width@30)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set CVCenter's midi-mean: the default-output of your MIDI-device's\nsliders in neutral position. Applies only if midi-mode is 1.")
			;

			if(GUI.id !== \cocoa, {
				saveMidiMean.toolTip_(
					"The output of your device's sliders in neutral position.\nOnly needed if output != 0 and midiMode is set to 1."
				)
			});

			midiFlow.nextLine.shift(20, 0);

			saveSoftWithin = buildNumTextBox.(midiView, prefs !? {
				prefs[\softWithin] } ?? { CVCenter.softWithin }, \text
			);

			midiFlow.shift(5, 2);

			textSoftWithin = StaticText(midiView, midiFlow.bounds.width-100@42)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set the soft-within threshold: the widget will only respond if the\ncurrent MIDI-output is within the widget's current value +/- threshold.\nApplies only if midi-mode is 0.");

			if(GUI.id !== \cocoa, {
				saveSoftWithin.toolTip_(
					"Set an arbitrary floating point value. Recomended: 0.1.\nApplies only if midiMode is set to 0."
				)
			});

			midiFlow.nextLine.shift(20, 0);

			saveCtrlButtonBank = buildNumTextBox.(midiView, prefs !? {
				prefs[\ctrlButtonBank] } ?? { CVCenter.ctrlButtonBank }, \text
			);

			midiFlow.shift(5, 4);

			if(GUI.id === \cocoa, { specialHeight = 60 }, { specialHeight = 54 });

			textCtrlButtonBank = StaticText(midiView, midiFlow.indentedRemaining.width@specialHeight)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set the number of sliders on in one bank of your MIDI-device.\nSetting this number will display the selected slider in a widget not as\na single number but rather as combination of the selected bank and\nthe slider number (e.g.: 4:3 means bank nr. 4, slider nr. 3)")
			;

			saveClassVars.action_({ |b|
				if(b.value.asBoolean.not, {
					[saveMidiMode, saveMidiMean, saveMidiResolution, saveSoftWithin, saveCtrlButtonBank].do(
						_.enabled_(false)
					);
					[textMidiMode, textMidiMean, textMidiResolution, textSoftWithin, textCtrlButtonBank].do(
						_.stringColor_(Color(0.7, 0.7, 0.7))
					)
				});
				if(b.value.asBoolean, {
					[saveMidiMode, saveMidiMean, saveMidiResolution, saveSoftWithin, saveCtrlButtonBank].do(
						_.enabled_(true)
					);
					[textMidiMode, textMidiMean, textMidiResolution, textSoftWithin, textCtrlButtonBank].do(
						_.stringColor_(staticTextColor)
					)
				})
			});

			if(saveClassVars.value.asBoolean.not, {
				[saveMidiMode, saveMidiMean, saveMidiResolution, saveSoftWithin, saveCtrlButtonBank].do(
					_.enabled_(false)
				);
				[textMidiMode, textMidiMean, textMidiResolution, textSoftWithin, textCtrlButtonBank].do(
					_.stringColor_(Color(0.7, 0.7, 0.7))
				)
			});
			if(saveClassVars.value.asBoolean, {
				[saveMidiMode, saveMidiMean, saveMidiResolution, saveSoftWithin, saveCtrlButtonBank].do(
					_.enabled_(true)
				);
				[textMidiMode, textMidiMean, textMidiResolution, textSoftWithin, textCtrlButtonBank].do(
					_.stringColor_(staticTextColor)
				)
			});

			if(GUI.id !== \cocoa, {
				saveCtrlButtonBank.toolTip_(
					"Set an arbitrary integer number, corresponding\nto the number of sliders on your device."
				)
			});

			flow1.nextLine;

			responderView = CompositeView(prefsTab, flow1.indentedRemaining.width@29)
				.background_(Color(0.95, 0.95, 0.95))
			;

			responderView.decorator = responderFlow = FlowLayout(responderView.bounds, 7@7, 0@1);

			if(prefs.notNil and:{ prefs[\removeResponders].notNil }, {
				removeResponders = buildCheckbox.(responderView, prefs[\removeResponders])
			}, {
				removeResponders = buildCheckbox.(responderView, CVWidget.removeResponders)
			});

			responderFlow.shift(5, -2);

			StaticText(responderView, responderFlow.indentedRemaining.width@20)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Remove all OSC-/MIDI-responders on cmd/ctrl-period.")
			;

			scTabs = TabbedView2(scTab, Rect(0, 1, scTab.bounds.width, scTab.bounds.height))
				.tabHeight_(17)
				.tabCurve_(3)
				.labelColors_(Color.white!3)
				.unfocusedColors_(Color.red!3)
				.stringColors_(Color.white!3)
				.stringFocusedColors_(Color.red!3)
				.dragTabs_(false)
				.font_(tabFont)
			;

			cvCenterTab = scTabs.add("CVCenter", scroll: false);
			cvWidgetTab = scTabs.add("CVWidget", scroll: false);
			cvWidgetEditorTab = scTabs.add("CVWidget(MS)Editor", scroll: false);
			globalShortcutsTab = scTabs.add("global shortcuts", scroll: false);

			cvCenterEditor = KeyDownActionsEditor(
				cvCenterTab, nil, cvCenterTab.bounds,
				if(prefs.notNil and:{
					prefs[\shortcuts].notNil and:{
						prefs[\shortcuts][\cvcenter].notNil
					}
				}, {
					prefs[\shortcuts][\cvcenter]
				}, { CVCenter.shortcuts }),
				true
			);
			cvWidgetEditor = KeyDownActionsEditor(
				cvWidgetTab, nil, cvWidgetTab.bounds,
				if(prefs.notNil and:{
					prefs[\shortcuts].notNil and:{
						prefs[\shortcuts][\cvwidget].notNil
					}
				}, {
					prefs[\shortcuts][\cvwidget]
				}, { CVWidget.shortcuts }),
				true
			);
			cvWidgetEditorEditor = KeyDownActionsEditor(
				cvWidgetEditorTab, nil, cvWidgetEditorTab.bounds,
				if(prefs.notNil and:{
					prefs[\shortcuts].notNil and:{
						prefs[\shortcuts][\cvwidgeteditor].notNil
					}
				}, {
					prefs[\shortcuts][\cvwidgeteditor]
				}, { AbstractCVWidgetEditor.shortcuts }),
				true
			);
			// "prefs.globalShortcuts: %\n".postf(prefs.globalShortcuts);
			globalShortcutsEditorTab = KeyDownActionsEditor(
				globalShortcutsTab, nil, globalShortcutsTab.bounds,
				if(prefs.notNil and:{
					prefs[\globalShortcuts].notNil
				}, {
					prefs[\globalShortcuts]
				}, { KeyDownActions.globalShortcuts }),
				false, false, false
			);

			cvCenterKeyCodesEditor = KeyCodesEditor(
				cvKeyCodesEditorTab, nil, false
			);

			Button(saveCancel, saveCancelFlow.bounds.width/2-10@23)
				.states_([["Cancel", Color.black, Color.white]])
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 14, true))
				.action_({ window.close })
			;

			saveBut = Button(saveCancel, saveCancelFlow.indentedRemaining.width@23)
				.states_([["Save", Color.white, Color.red]])
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 14, true))
				.action_({
					if(saveGuiPosition.value == 2 and:{
						[left, top, width, height].select({ |field|
							field.string.interpret.isInteger
						}).size < 4
					}, {
						[leftText, topText, widthText, heightText].do(_.stringColor_(Color.red));
						guiView.background_(Color.yellow);
						"Please supply valid values (integer numbers) for 'left', 'top', 'width', 'height'".warn;
					}, {
						if([left, top, width, height].select({ |f| f.string.interpret.notNil }).size == 4, {
							rect = Rect(
								left.string.interpret.asInteger,
								top.string.interpret.asInteger,
								width.string.interpret.asInteger,
								height.string.interpret.asInteger
							)
						});
						KeyDownActionsEditor.cachedScrollViewSC !? {
							ScrollView.globalKeyDownAction_(KeyDownActionsEditor.cachedScrollViewSC);
						};
						shortcuts = (cvcenter:  cvCenterEditor.result, cvwidget: cvWidgetEditor.result, cvwidgeteditor: cvWidgetEditorEditor.result);
						// "shortcuts.cvcenter['fn + F1']: %\n".postf(shortcuts.cvcenter['fn + F1']);
						// "cvCenterEditor.result: %\n".postf(cvCenterEditor.result);
						// "cvCenterKeyCodesEditor.result: %\n".postf(cvCenterKeyCodesEditor.result);
						this.writePreferences(
							saveGuiPosition.value,
							rect,
							saveClassVars.value,
							saveMidiMode.string.interpret,
							saveMidiResolution.string.interpret,
							saveMidiMean.string.interpret,
							saveSoftWithin.string.interpret,
							saveCtrlButtonBank.string.interpret,
							removeResponders.value,
							initMidiOnStartUp.value,
							shortcuts,
							globalShortcutsEditorTab.result,
							cvCenterKeyCodesEditor.result(false)
						);
						window.close;
					})
				})
			;

			window.view.keyDownAction_({ |view, char, modifiers, unicode, keycode, key|
				if(keycode == KeyDownActions.keyCodes[\return]) { saveBut.doAction };
				// if(keycode == KeyDownActions.keyCodes[\esc]) { window.close };
			})
		});
		window.front;
	}

	*writePreferences { |saveGuiProperties, guiProperties, saveClassVars, midiMode, midiResolution, midiMean, softWithin, ctrlButtonBank, removeResponders, initMidiOnStartUp, shortcuts, globalShortcuts, keyCodesAndMods, informString|
		var prefsPath, prefs, thisGuiProperties, thisSaveClassVars, thisRemoveResponders, thisInformString, thisInitMidi;
		var shortcutsPath, globalShortcutsPath, keyCodesPath;
		var platform;

		Platform.case(
			\linux, { platform = "Linux" },
			\osx, { platform = "OSX" },
			\windows, { platform = "Windows" },
			{ platform = "NN" }
		);

		thisSaveClassVars = saveClassVars.asBoolean;
		thisRemoveResponders = removeResponders.asBoolean;
		thisInitMidi = initMidiOnStartUp.asBoolean;

		guiProperties !? {
			if(guiProperties.isArray, {
				thisGuiProperties = guiProperties.asRect;
			}, {
				thisGuiProperties = guiProperties;
			})
		};

		if(saveGuiProperties == 2 and:{
			guiProperties.isNil
		}, {
			Error("Please provide either a Rect or an Array for your desired GUI-bounds").throw;
		});

		prefsPath = this.filenameSymbol.asString.dirname +/+ "CVCenterPreferences";
		if(File.exists(prefsPath), {
			prefs = Object.readArchive(prefsPath);
		}, {
			prefs = ();
		});

		shortcutsPath = this.filenameSymbol.asString.dirname +/+ "CVCenterShortcuts";
		shortcuts !? { shortcuts.writeArchive(shortcutsPath) };

		globalShortcutsPath = this.filenameSymbol.asString.dirname +/+ "globalShortcuts";
		globalShortcuts !? { globalShortcuts.writeArchive(globalShortcutsPath) };

		keyCodesPath = KeyDownActions.filenameSymbol.asString.dirname +/+ "keyCodesAndMods"++platform;
		keyCodesAndMods !? { keyCodesAndMods.writeArchive(keyCodesPath) };

		prefs.put(\saveGuiProperties, saveGuiProperties);
		if(saveGuiProperties == 2 or:{ saveGuiProperties == 1 }, {
			prefs.put(\guiProperties, thisGuiProperties)
		}, { prefs.removeAt(\guiProperties) });
		prefs.put(\initMidiOnStartUp, thisInitMidi);
		if(thisSaveClassVars, {
			prefs.put(\saveClassVars, true);
			midiMode.notNil.if({ prefs.put(\midiMode, midiMode.asInteger) }, { prefs.removeAt(\midiMode) });
			midiResolution.notNil.if({ prefs.put(\midiResolution, midiResolution.asFloat) }, { prefs.removeAt(\midiResolution) });
			midiMean.notNil.if({ prefs.put(\midiMean, midiMean.asInteger) }, { prefs.removeAt(\midiMean) });
			softWithin.notNil.if({ prefs.put(\softWithin, softWithin.asFloat) }, { prefs.removeAt(\softWithin) });
			ctrlButtonBank.notNil.if({ prefs.put(\ctrlButtonBank, ctrlButtonBank.asInteger) }, { prefs.removeAt(\ctrlButtonBank) });
		}, {
			prefs.put(\saveClassVars, false);
			#[midiMode, midiResolution, midiMean, softWithin, ctrlButtonBank].do(prefs.removeAt(_));
		});
		prefs.put(\removeResponders, thisRemoveResponders);
		prefs.writeArchive(prefsPath);

		if(informString.isNil, {
			thisInformString = "Your CVCenter-preferences have successfully been written to disk and will become active after library-recompilation.";
		}, { thisInformString = informString });

		thisInformString.inform;
	}

	*readPreferences { |...args|
		var prefsPath, prefs, res;
		var shortcutsPath, shortcuts, globalShortcutsPath, keyCodesAndModsPath, keyCodesAndMods;
		var platform;

		Platform.case(
			\osx, { platform = "OSX" },
			\linux, { platform = "Linux" },
			\windows, { platform = "Windows" },
			{ platform = "NN" }
		);

		prefsPath = this.filenameSymbol.asString.dirname +/+ "CVCenterPreferences";
		shortcutsPath = this.filenameSymbol.asString.dirname +/+ "CVCenterShortcuts";
		globalShortcutsPath = KeyCodesEditor.filenameSymbol.asString.dirname +/+ "globalShortcuts";
		keyCodesAndModsPath = KeyCodesEditor.filenameSymbol.asString.dirname +/+ "keyCodesAndMods"++platform;

		if(File.exists(prefsPath), {
			prefs = Object.readArchive(prefsPath);
			if(args.size > 0, {
				res = ();
				args.do({ |val| res.put(val.asSymbol, prefs[val.asSymbol]) });
			});
			if(File.exists(shortcutsPath), {
				// "shortcutsPath exists".postln;
				prefs ?? { prefs = () };
				prefs.put(\shortcuts, Object.readArchive(shortcutsPath));
				if(args.size > 0, {
					if(args.collect(_.asSymbol).includes(\shortcuts), {
						res ?? { res = () };
						res.shortcuts = Object.readArchive(shortcutsPath)
					})
				})
			});
			if(File.exists(globalShortcutsPath), {
				// "globalShortcutsPath exists".postln;
				prefs ?? { prefs = () };
				prefs.put(\globalShortcuts, Object.readArchive(globalShortcutsPath));
				if(args.size > 0, {
					if(args.collect(_.asSymbol).includes(\globalShortcuts), {
						res ?? { res = () };
						res.globalShortcuts = Object.readArchive(globalShortcutsPath) ?? {
							KeyDownActions.globalShortcuts
						}
					})
				})
			});
			if(File.exists(keyCodesAndModsPath), {
				prefs ?? { prefs = () };
				prefs.put(\keyCodesAndMods, Object.readArchive(keyCodesAndModsPath));
				if(args.size > 0, {
					if(args.collect(_.asSymbol).includes(\keyCodesAndMods), {
						res ?? { res = () };
						res.keyCodesAndMods = Object.readArchive(keyCodesAndModsPath) ?? {
							IdentityDictionary[
								\keyCodes -> KeyDownActions.keyCodes,
								\modifiersQt -> KeyDownActions.modifiersQt,
								\modifiersCocoa -> KeyDownActions.modifiersCocoa,
								\arrowsModifiersQt -> KeyDownActions.arrowsModifiersQt,
								\arrowsModifiersCocoa -> KeyDownActions.arrowsModifiersCocoa
							]
						}
					})
				})
			})
		});

		// "res: %, prefs: %\n".postf(res, prefs);

		// "prefs.shortcuts: %\n".postf(prefs.shortcuts.cs);
		// "prefs.keyCodesAndMods: %\n".postf(prefs.keyCodesAndMods);

		^if(res.notNil, { res }, { if(prefs.notNil and:{ prefs.notEmpty }, { prefs }, { nil }) });
	}
}