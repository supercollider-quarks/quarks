CVCenterShortcutsEditor {
	classvar <window;

	*dialog {
		var tabs, cvCenterFlow, cvWidgetFlow, cvWidgetEditorFlow, globalShortcutsFlow, keyCodesAndModsFlow;
		var cvCenterTab, cvWidgetTab, cvWidgetEditorTab, globalShortcutsTab, keyCodesAndModsTab;
		// var cvCenterFlow, cvWidgetFlow, cvWidgetEditorFlow, globalShortcutsFlow;
		var cvCenterEditor, cvWidgetEditor, cvWidgetEditorEditor, globalShortcutsEditor, keyCodesAndModsEditor;
		var tabFont, staticTextFont, staticTextColor, textFieldFont, textFieldFontColor, textFieldBg, tabsBg;
		var saveCancel, saveCancelFlow;
		var saveBut;
		var buildCheckbox, copyToPrefs;
		var fFact, shortcuts;

		if(GUI.id === \cocoa, { fFact = 0.9 }, { fFact = 1 });

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

		tabFont = Font(Font.available("Arial") ? Font.defaultSansFace, 12, true);
		staticTextFont = Font(Font.available("Arial") ? Font.defaultSansFace, 12 * fFact);
		staticTextColor = Color(0.2, 0.2, 0.2);
		textFieldFont = Font(Font.available("Courier New") ? Font.defaultSansFace, 12);
		textFieldFontColor = Color.black;
		textFieldBg = Color.white;
		tabsBg = Color(0.8, 0.8, 0.8);

		if(window.isNil or:{ window.isClosed }) {
			window = Window("edit temporary shortcuts", Rect(
				Window.screenBounds.width/2-249,
				Window.screenBounds.height/2-193,
				498, 385
			));

			tabs = TabbedView2(window, Rect(0, 1, window.bounds.width, window.bounds.height-50))
				.tabHeight_(17)
				.tabCurve_(3)
				.labelColors_(Color.white!2)
				.unfocusedColors_(Color.red!2)
				.stringColors_(Color.white!2)
				.stringFocusedColors_(Color.red!2)
				.dragTabs_(false)
				.font_(tabFont)
			;

			cvCenterTab = tabs.add("CVCenter", scroll: false);
			cvWidgetTab = tabs.add("CVWidget", scroll: false);
			cvWidgetEditorTab = tabs.add("CVWidget(MS)Editor", scroll: false);
			globalShortcutsTab = tabs.add("global shortcuts", scroll: false);
			// keyCodesAndModsTab = tabs.add("keycodes and modifiers", scroll: false);

			cvCenterEditor = KeyDownActionsEditor(
				cvCenterTab, nil, cvCenterTab.bounds, CVCenter.shortcuts, true
			);
			cvWidgetEditor = KeyDownActionsEditor(
				cvWidgetTab, nil, cvWidgetTab.bounds, CVWidget.shortcuts, true
			);
			cvWidgetEditorEditor = KeyDownActionsEditor(
				cvWidgetEditorTab, nil, cvWidgetEditorTab.bounds, AbstractCVWidgetEditor.shortcuts, true
			);
			globalShortcutsEditor = KeyDownActionsEditor(
				globalShortcutsTab, nil, globalShortcutsTab.bounds, KeyDownActions.globalShortcuts, false, false, false
			);

			saveCancel = CompositeView(window, Rect(0, window.bounds.height-49, window.bounds.width, 49))
				.background_(tabsBg)
			;

			saveCancel.decorator = saveCancelFlow = FlowLayout(saveCancel.bounds, 7@2, 1@0);

			Button(saveCancel, saveCancelFlow.bounds.width/2-10@23)
				.states_([["Cancel", Color.black, Color.white]])
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 14, true))
				.action_({ window.close })
			;

			saveBut = Button(saveCancel, saveCancelFlow.indentedRemaining.width@23)
				.states_([["set shortcuts", Color.white, Color.red]])
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 14, true))
				.action_({
					KeyDownActionsEditor.cachedScrollViewSC !? {
						ScrollView.globalKeyDownAction_(KeyDownActionsEditor.cachedScrollViewSC);
					};
					CVCenter.shortcuts_(cvCenterEditor.result);
					CVWidget.shortcuts_(cvWidgetEditor.result);
					AbstractCVWidgetEditor.shortcuts_(cvWidgetEditorEditor.result);
					KeyDownActions.globalShortcuts_(globalShortcutsEditor.result);
					// CVCenter.setShortcuts;
					// CVCenter.cvWidgets.do(_.setShortcuts);
					if(CVCenter.window.notNil and:{ CVCenter.window.isClosed.not }, {
						[CVCenter.tabs.views, CVCenter.prefPane].flat.do({ |view|
							cvCenterEditor.setShortcuts(view);
						});
						CVCenter.cvWidgets.do({ |wdgt|
							wdgt.focusElements.do({ |el|
								cvWidgetEditor.setShortcuts(el)
							})
						})
					});
					// AbstractCVWidgetEditor.allEditors.collect(_.editor).do(_.setShortcuts);
					AbstractCVWidgetEditor.allEditors.collect(_.editor).do({ |ed|
						cvWidgetEditorEditor.setShortcuts(ed.tabs.view)
					});
					if(Server.default.serverRunning and:{
						KeyDownActions.globalShortcutsEnabled;
					}, {
						KeyDownActions.globalShortcutsSync;
					});
					if(copyToPrefs.value.asBoolean, {
						shortcuts = (cvcenter:  cvCenterEditor.result, cvwidget: cvWidgetEditor.result, cvwidgeteditor: cvWidgetEditorEditor.result);
						CVCenterPreferences.writePreferences(
							shortcuts: shortcuts,
							globalShortcuts: globalShortcutsEditor.result,
							informString: "shortcuts have successfully been written to preferences"
						)
					});
					window.close;
				})
			;

			saveCancelFlow.nextLine.shift(saveCancelFlow.bounds.width/2-8, 4);
			copyToPrefs = buildCheckbox.(saveCancel, false);
			saveCancelFlow.shift(4, -2);
			StaticText(saveCancel, Point(saveCancelFlow.indentedRemaining.width, 20))
				.string_("copy to preferences")
				.font_(staticTextFont)
			;

			window.view.keyDownAction_({ |view, char, modifiers, unicode, keycode, key|
				if(keycode == KeyDownActions.keyCodes[\return]) { saveBut.doAction };
				// if(keycode == KeyDownActions.keyCodes[\esc]) { window.close };
			})
		};
		window.front;
	}

}