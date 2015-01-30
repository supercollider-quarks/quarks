CVWidgetMS : CVWidget {
	var <mSlider, <calibViews, <numVal, <midiBut, <oscBut, <specBut, <actionsBut;
	var numOscResponders, numMidiResponders;
	// persistent widgets
	var isPersistent, oldBounds, oldName;

	*new { |parent, widgetCV, name, connectMSlider, connectTextField, bounds, defaultAction, setup, controllersAndModels, cvcGui, persistent, numSliders=5, server|
		^super.newCopyArgs(parent, widgetCV, name, connectMSlider, connectTextField).init(
			bounds,
			defaultAction,
			setup,
			controllersAndModels,
			cvcGui,
			persistent,
			numSliders,
			server // swing compatibility. well, ...
		)
	}

	init { |bounds, action, setupArgs, controllersAndModels, cvcGui, persistent, numSliders, server|
		var thisXY, thisX, thisY, thisWidth, thisHeight, knobsize;
		// hmmm...
		var msrc = "source", mchan = "chan", mctrl = "ctrl", margs;
		var nextX, nextY, knobX, knobY;
		var calibViewsWidth, calibViewsNextX;
		var text, tActions;

		background ?? { background = Color.white };
		stringColor ?? { stringColor = Color.black };

		synchKeys ?? { synchKeys = [\default] };
		#numOscResponders, numMidiResponders = 0!2;

		calibViews = List();

		widgetCV ?? { widgetCV = CV([0 ! numSliders, 1 ! numSliders]) };

		if(widgetCV.spec.minval.isArray.not and:{
			widgetCV.spec.maxval.isArray.not and:{
				widgetCV.spec.step.isArray.not and:{
					widgetCV.spec.default.isArray.not
				}
			}
		}, {
			widgetCV.spec_(ControlSpec(
				widgetCV.spec.minval ! numSliders,
				widgetCV.spec.maxval ! numSliders,
				widgetCV.spec.warp,
				widgetCV.spec.step ! numSliders,
				widgetCV.spec.default ! numSliders,
				widgetCV.spec.units
			))
		});

		msSize = [
			widgetCV.spec.minval.size,
			widgetCV.spec.maxval.size,
			widgetCV.spec.step.size,
			widgetCV.spec.default.size
		].maxItem;

//		"msSize: %\n".postf(msSize);

		prCalibrate = true ! msSize;
		prMidiMode = 0 ! msSize;
		prMidiMean = 64 ! msSize;
		prMidiResolution = 1 ! msSize;
		prSoftWithin = 0.1 ! msSize;
		prCtrlButtonBank = nil ! msSize;

//		"setup: %\n".postf([prCalibrate, prMidiMode, prMidiMean, prMidiResolution, prSoftWithin]);

		guiEnv = ();
		cvcGui !? { isCVCWidget = true };

		if(cvcGui.class == Event and:{ cvcGui.midiOscEnv.notNil }, {
			midiOscEnv = cvcGui.midiOscEnv
		}, {
			midiOscEnv = ()!msSize
		});

		msSize.do({ |i|
			midiOscEnv[i].oscMapping ?? { midiOscEnv[i].oscMapping = \linlin };
		});

		name ?? { name = "multislider" };

//		what's the editor gonna look like?
		editor = (editors: Array.newClear(msSize));

		// "wdgtControllersAndModels before init: %\n".postf(wdgtControllersAndModels.slots.collect(_.oscConnection));
		// controllersAndModels.pairsDo({ |k, v|
		// 	if(k == \slots, { v.pairsDo({ |kk, vv| [kk, vv].postcs }) }, { [k, v].postln })
		// });
		msSize.do({ |sl| this.initControllersAndModels(controllersAndModels, sl) });
		// "wdgtControllersAndModels after init: %\n".postf(wdgtControllersAndModels.slots.collect(_.oscConnection));

		setupArgs !? {
			msSize.do({ |slot|
				setupArgs[slot] !? { setupArgs[slot][\midiMode] !? { this.setMidiMode(setupArgs[slot][\midiMode], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\midiResolution] !? { this.setMidiResolution(setupArgs[slot][\midiResolution], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\midiMean] !? { this.setMidiMean(setupArgs[slot][\midiMean], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\ctrlButtonBank] !? { this.setCtrlButtonBank(setupArgs[slot][\ctrlButtonBank], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\softWithin] !? { this.setSoftWithin(setupArgs[slot][\softWithin], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\calibrate] !? { this.setCalibrate(setupArgs[slot][\calibrate], slot) }};
			})
		};

		action !? { this.addAction(\default, action) };

		if(bounds.isNil, {
			thisXY = 7@0;
			thisX = 50; thisY = 50;
			thisWidth = 122;
			thisHeight = 196;
		}, {
			if(parent.isNil, { thisXY = 7@0 }, { thisXY = bounds.left@bounds.top });
			thisX = bounds.left; thisY = bounds.top;
			thisWidth = bounds.width;
			thisHeight = bounds.height;
		});

		parent ?? { parent = Window(name, Rect(thisX, thisY, thisWidth+14, thisHeight+7), server: server) };
		// parent.acceptsMouseOver_(true);

		cvcGui ?? {
			parent.onClose_({
				msSize.do({ |slot|
					if(editor.editors[slot].notNil, {
						if(editor.editors[slot].isClosed.not, {
							editor.editors[slot].close(slot);
						}, {
							if(AbstractCVWidgetEditor.allEditors.notNil and:{
								AbstractCVWidgetEditor.allEditors[name.asSymbol].notNil
							}, {
								AbstractCVWidgetEditor.allEditors[name.asSymbol].removeAt(slot);
								if(AbstractCVWidgetEditor.allEditors[name.asSymbol].isEmpty, {
									AbstractCVWidgetEditor.allEditors.removeAt(name.asSymbol);
								})
							})
						})
					})
				});
				editor.msEditor !? {
					if(editor.msEditor.isClosed.not, {
						editor.msEditor.close;
					}, {
						if(AbstractCVWidgetEditor.allEditors.notNil and:{
							AbstractCVWidgetEditor.allEditors[(name.asString++"MS").asSymbol].notNil
						}, {
							if(AbstractCVWidgetEditor.allEditors[(name.asString++"MS").asSymbol].isEmpty, {
								AbstractCVWidgetEditor.allEditors.removeAt((name.asString++"MS").asSymbol);
							})
						})
					})
				}
			});
			if(persistent == false or:{ persistent.isNil }, {
				parent.onClose_(parent.onClose.addFunc({
					msSize.do({ |slot|
						this.oscDisconnect(slot);
						this.midiDisconnect(slot);
						// midiOscEnv[slot].oscResponder !? { midiOscEnv[slot].oscResponder.remove };
						// midiOscEnv[slot].cc !? { midiOscEnv[slot].cc.remove };
						wdgtControllersAndModels[slot].do({ |mc| mc.isKindOf(SimpleController).if{ mc.controller.remove } });
					})
				}))
			}, {
				isPersistent = true;
			})
		};

		persistent !? { if(persistent, { isPersistent = true }) };

		widgetBg = CompositeView(parent, Rect(thisXY.x, thisXY.y, thisWidth, thisHeight))
			.background_(background)
		;
		label = Button(parent, Rect(thisXY.x+1, thisXY.y+1, thisWidth-2, 15))
			.states_([
				[name.asString, Color.white, Color.blue],
				[name.asString, Color.black, Color.yellow],
			])
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.focusColor_(Color.green)
		;
		nameField = TextView(parent, Rect(label.bounds.left, label.bounds.top+label.bounds.height, thisWidth-2, thisHeight-label.bounds.height-2))
			.background_(Color.white)
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.string_("Add some notes if you like")
			.visible_(false)
			.keyUpAction_({ wdgtInfo = nameField.string })
		;

		if(GUI.id !== \cocoa, {
			label.toolTip_(nameField.string);
		});

		label.action_({ |lbl|
			this.toggleComment(lbl.value.asBoolean);
			if(GUI.id !== \cocoa, {
				lbl.toolTip_(nameField.string)
			})
		});

		mSlider = MultiSliderView(parent, Rect(thisXY.x+1, thisXY.y+16, thisWidth-2, thisHeight-2-79))
			.drawRects_(true)
			.isFilled_(true)
			.colors_(Color.clear, Color.blue)
			.background_(Color.white)
			.elasticMode_(1)
			.focusColor_(Color.green)
		;

		activeSliderB = Button(parent, Rect(thisXY.x+mSlider.bounds.width-8, thisXY.y+mSlider.bounds.height+8, 7, 7))
			.states_([
				["", Color.black, Color.red],
				["", Color.black, Color.green]
			])
			.action_({ |b| this.connectGUI(b.value.asBoolean, nil) })
		;

		connectS !? { activeSliderB.value_(connectS.asInteger) };

		// "model: %\n".postf(wdgtControllersAndModels.slidersTextConnection);

		if(GUI.id !== \cocoa, {
			if(wdgtControllersAndModels.slidersTextConnection.model.value[0], {
				activeSliderB.toolTip_("deactivate CV-slider connection")
			}, {
				activeSliderB.toolTip_("activate CV-slider connection")
			})
		});

		nextY = thisXY.y+mSlider.bounds.height+label.bounds.height+1;

		calibViewsWidth = (thisWidth-2).div(msSize);
		calibViewsNextX = thisXY.x+1;
		msSize.do({ |sl|
			calibViews.add(
				CompositeView(parent, Rect(calibViewsNextX, nextY, calibViewsWidth, 2)).background_(Color.green);
			);
			calibViewsNextX = calibViewsNextX+calibViewsWidth;
			if(GUI.id !== \cocoa, {
				if(this.getCalibrate(sl), {
					text = "Calibration for slot % is active.".format(sl);
				}, {
					text = "Calibration for slot % is inactive.".format(sl);
				});
				calibViews[sl].toolTip_(text);
			})
		});

		nextY = nextY+2+1;

		numVal = TextView(parent, Rect(thisXY.x+1, nextY, thisWidth-2, 30))
			.string_(widgetCV.value.asCompileString).font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9.5))
			.keyDownAction_({ |nv, char, modifiers, unicode, keycode|
				if(char == $\r and:{ modifiers == 131072 }, {
					if(nv.string.interpret.class == Array and:{
						nv.string.interpret.select(_.isNumber).size == mSlider.size
					}, { nv.doAction })
				})
			})
			.focusColor_(Color.green)
		;

		activeTextB = Button(parent, Rect(thisXY.x+numVal.bounds.width-8, nextY+22, 7, 7))
			.states_([
				["", Color.black, Color.red],
				["", Color.black, Color.green]
			])
			.action_({ |b| this.connectGUI(nil, b.value.asBoolean) })
		;

		connectTF !? { activeTextB.value_(connectTF.asInteger) };

		// "model: %\n".postf(wdgtControllersAndModels.slidersTextConnection);

		if(GUI.id !== \cocoa, {
			if(wdgtControllersAndModels.slidersTextConnection.model.value[1], {
				activeTextB.toolTip_("deactivate CV-numberbox connection")
			}, {
				activeTextB.toolTip_("activate CV-numberbox connection")
			})
		});

		// nextY = thisXY.y+numVal.bounds.top+numVal.bounds.height+1;
		nextY = numVal.bounds.top+numVal.bounds.height+1;

		midiBut = Button(parent, Rect(thisXY.x+1, nextY, thisWidth-2/2, 15))
			.states_([
				["MIDI"+"("++numMidiResponders++"/"++msSize++")", stringColor, background]
			])
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.focusColor_(Color.green)
			.action_({ |mb|
				if(editor.msEditor.isNil or:{ editor.msEditor.isClosed }, {
					CVWidgetMSEditor(this, 1);
				}, {
					editor.msEditor.front(1)
				});
				if(midiOscEnv.select({ |slot| slot.cc.notNil }).size > 0, {
					editor.msEditor.midiTabs.focus(1)
				}, {
					editor.msEditor.midiTabs.focus(0)
				});
				msSize.do({ |i|
					wdgtControllersAndModels.slots[i].oscDisplay.model.value_(
						wdgtControllersAndModels.slots[i].oscDisplay.model.value;
					).changedKeys(synchKeys);
					wdgtControllersAndModels.slots[i].midiDisplay.model.value_(
						wdgtControllersAndModels.slots[i].midiDisplay.model.value
					).changedKeys(synchKeys);
				})
			})
		;

		if(GUI.id !== \cocoa, { midiBut.toolTip_(
			"Edit all MIDI-options of this widget.\nmidiMode:"+(
				msSize.collect(this.getMidiMode(_))
			)++"\nmidiMean:"+(
				msSize.collect(this.getMidiMean(_))
			)++"\nmidiResolution:"+(
				msSize.collect(this.getMidiResolution(_))
			)++"\nsoftWithin:"+(
				msSize.collect(this.getSoftWithin(_))
			)++"\nctrlButtonBank:"+(
				msSize.collect(this.getCtrlButtonBank(_))
			))
		});

		if(GUI.id === \qt, {
			midiBut.mouseEnterAction_({ |mb|
				if(wdgtControllersAndModels.slots.select({ |slot| slot.midiConnection.model.value.isNil }).size == msSize, {
					mb.states_([[mb.states[0][0], Color.white, Color.red]])
				})
			}).mouseLeaveAction_({ |mb|
				if(wdgtControllersAndModels.slots.select({ |slot| slot.midiConnection.model.value.isNil }).size == msSize, {
					mb.states_([[mb.states[0][0], stringColor, background]])
				})
			})
		});

		nextX = thisXY.x+1+midiBut.bounds.width;
		oscBut = Button(parent, Rect(nextX, nextY, thisWidth-2/2, 15))
			.states_([
				["OSC"+"("++numOscResponders++"/"++msSize++")", stringColor, background]
			])
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.focusColor_(Color.green)
			.action_({ |oscb|
				if(editor.msEditor.isNil or:{ editor.msEditor.isClosed }, {
					CVWidgetMSEditor(this, 2);
				}, {
					editor.msEditor.front(2)
				});
				if(midiOscEnv.select({ |slot| slot.oscResponder.notNil }).size > 0, {
					editor.msEditor.oscTabs.focus(1)
				}, {
					editor.msEditor.oscTabs.focus(0)
				});
				msSize.do({ |i|
					wdgtControllersAndModels.slots[i].oscDisplay.model.value_(
						wdgtControllersAndModels.slots[i].oscDisplay.model.value;
					).changedKeys(synchKeys);
					wdgtControllersAndModels.slots[i].midiDisplay.model.value_(
						wdgtControllersAndModels.slots[i].midiDisplay.model.value
					).changedKeys(synchKeys);
				})
			})
		;

		if(GUI.id === \qt, {
			oscBut.mouseEnterAction_({ |oscb|
				// "oscb.states[0][0] on enter: %\n".postf(oscb.states[0][0]);
				if(wdgtControllersAndModels.slots.select({ |slot| slot.oscConnection.model.value == false }).size == msSize, {
					oscb.states_([[oscb.states[0][0], Color.white, Color.cyan(0.5)]])
				})
			}).mouseLeaveAction_({ |oscb|
				// "oscb.states[0][0] on leave: %\n".postf(oscb.states[0][0]);
				if(wdgtControllersAndModels.slots.select({ |slot| slot.oscConnection.model.value == false }).size == msSize, {
					oscb.states_([[oscb.states[0][0], stringColor, background]])
				})
			})
		});

		if(GUI.id !== \cocoa, { oscBut.toolTip_("no OSC-responders present.\nClick to edit.") });

		nextY = nextY+oscBut.bounds.height;
		specBut = Button(parent, Rect(thisXY.x+1, nextY, thisWidth-2/2, 15))
			.states_([
				["Spec", Color.white, Color(1.0, 0.3)]
			])
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.focusColor_(Color.green)
			.action_({ |spb|
				if(editor.msEditor.isNil or:{ editor.msEditor.isClosed }, {
					CVWidgetMSEditor(this, 0);
				}, {
					editor.msEditor.front(0)
				})
			})
		;

		if(GUI.id !== \cocoa, { specBut.toolTip_("Edit the CV's ControlSpec:\n"++(this.getSpec.asCompileString)) });

		nextX = nextX+specBut.bounds.width;
		actionsBut = Button(parent, Rect(thisXY.x+1+specBut.bounds.width, nextY, thisWidth-2/2, 15))
			.states_([
				["Actions ("++this.wdgtActions.select({ |v| v.asArray[0][1] == true }).size++"/"++this.wdgtActions.size++")", Color(0.08, 0.09, 0.14), Color(0.32, 0.67, 0.76)]
			])
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.focusColor_(Color.green)
			.action_({ |spb|
				if(editor.msEditor.isNil or:{ editor.msEditor.isClosed }, {
					CVWidgetMSEditor(this, 3);
				}, {
					editor.msEditor.front(3)
				})
			})
		;

		if(GUI.id !== \cocoa, {
			text = [];
			text = text.add(this.wdgtActions.size);
			text = text.add(this.wdgtActions.select({ |v| v.asArray[0][1] == true }).size);
			if(text[0] == 1, { tActions = "action" }, { tActions = "actions" });
			actionsBut.toolTip_("% of % % active.\nClick to edit.".format(text[1], text[0], tActions));
		});

		visibleGuiEls = [
			mSlider,
			activeSliderB,
			calibViews,
			numVal,
			activeTextB,
			midiBut,
			specBut,
			oscBut,
			actionsBut
		];

		allGuiEls = [
			widgetBg,
			label,
			nameField,
			mSlider,
			activeSliderB,
			calibViews,
			numVal,
			activeTextB,
			midiBut,
			oscBut,
			specBut,
			actionsBut
		];

		guiEnv = (
			msEditor: editor.msEditor,
			editor: editor.editors,
			mSlider: mSlider,
			calibViews: calibViews,
			numVal: numVal,
			midiBut: midiBut,
			oscBut: oscBut,
			specBut: specBut,
			actionsBut: actionsBut
		);

		focusElements = allGuiEls.copy.removeAll([
			widgetBg, calibViews, nameField, activeSliderB, activeTextB
		]);

		msSize.do({ |slot| this.initControllerActions(slot) });

		connectS !? { this.connectGUI(connectS, nil) };
		connectTF !? { this.connectGUI(nil, connectTF) };

		// this.setShortcuts;
		focusElements.do({ |el|
			KeyDownActions.setShortcuts(el, this.class.shortcuts);
		});

		oldBounds = parent.bounds;
		if(parent.respondsTo(\name), { oldName = parent.name });
	}

	open { |window, wdgtBounds|
		var thisWdgt, thisBounds;

		if(window.isNil, {
			thisBounds = Rect(oldBounds.left, oldBounds.top, oldBounds.width-14, oldBounds.height-7);
		}, {
			if(wdgtBounds.isNil, { thisBounds = oldBounds });
		});

		if(this.notNil and:{ this.isClosed and:{ isPersistent }}, {
			thisWdgt = this.class.new(
				parent: window,
				cv: widgetCV,
				name: oldName,
				connectMSlider: wdgtControllersAndModels.slidersTextConnection.model.value[0],
				connectTextField: wdgtControllersAndModels.slidersTextConnection.model.value[1],
				bounds: thisBounds,
				setup: this.setup,
				controllersAndModels: wdgtControllersAndModels,
				cvcGui: (midiOscEnv: midiOscEnv),
				numSliders: msSize,
				persistent: true
			).front;
			msSize.do({ |slot|
				thisWdgt.wdgtControllersAndModels.slots[slot].oscDisplay.model.value_(
					wdgtControllersAndModels.slots[slot].oscDisplay.model.value
				).changedKeys(synchKeys);
				thisWdgt.wdgtControllersAndModels.slots[slot].midiOptions.model.value_(
					wdgtControllersAndModels.slots[slot].midiOptions.model.value
				).changedKeys(synchKeys);
				thisWdgt.wdgtControllersAndModels.slots[slot].midiDisplay.model.value_(
					wdgtControllersAndModels.slots[slot].midiDisplay.model.value
				).changedKeys(synchKeys);
				thisWdgt.wdgtControllersAndModels.actions.model.value_(
					wdgtControllersAndModels.actions.model.value
				).changedKeys(synchKeys);
				thisWdgt.wdgtControllersAndModels.slots[slot].calibration.model.value_(
					wdgtControllersAndModels.slots[slot].calibration.model.value
				).changedKeys(synchKeys);
			});
			thisWdgt.parent.onClose_(thisWdgt.parent.onClose.addFunc({
				msSize.do({ |slot|
					// "thisWdgt.editor.editors[%]: %\n".postf(slot, thisWdgt.editor.editors[slot]);
					thisWdgt.editor.editors[slot] !? {
						if(thisWdgt.editor.editors[slot].isClosed.not, { thisWdgt.editor.editors[slot].close });
					}
				});
				// "thisWdgt.editor.msEditor: %\n".postf(thisWdgt.editor.msEditor);
				thisWdgt.editor.msEditor !? {
					if(thisWdgt.editor.msEditor.isClosed.not, { thisWdgt.editor.msEditor.close });
				}
			}));
			^thisWdgt;
		}, {
			"Either the widget you're trying to reopen hasn't been closed yet or it doesn't even exist.".warn;
		})
	}

	background_ { |color|
		background = color;
		widgetBg.background_(background);
		if(midiOscEnv.select({ |slot| slot.cc.notNil }).size < 1, {
			midiBut.states_([
				[midiBut.states[0][0], midiBut.states[0][1], background]
			])
		});
		if(midiOscEnv.select({ |slot| slot.oscResponder.notNil }).size < 1, {
			oscBut.states_([
				[oscBut.states[0][0], oscBut.states[0][1], background]
			])
		})
	}

	stringColor_ { |color|
		stringColor = color;
		if(midiOscEnv.select({ |slot| slot.cc.notNil }).size < 1, {
			midiBut.states_([
				[midiBut.states[0][0], stringColor, midiBut.states[0][2]]
			])
		});
		if(midiOscEnv.select({ |slot| slot.oscResponder.notNil }).size < 1, {
			oscBut.states_([
				[oscBut.states[0][0], stringColor, oscBut.states[0][2]]
			])
		})
	}

}