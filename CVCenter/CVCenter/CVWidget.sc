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

CVWidget {

	classvar <>removeResponders = true, <>midiSources, <>shortcuts, prefs/*, midiStateObserver*/;
	classvar <>debug = false;
	var <parent, <widgetCV, <name, <connectS = true, <connectTF = true;
	var <activeSliderB, <activeTextB;
	var sliderConnection, textConnection;
	var <guiEnv;
	var prDefaultAction, <>wdgtActions, <background, <stringColor, <alwaysPositive = 0.1;
	var prMidiMode, prMidiMean, prCtrlButtonBank, prMidiResolution, prSoftWithin;
	var prCalibrate, netAddr; // OSC-calibration enabled/disabled, NetAddr if not nil at instantiation
	var visibleGuiEls, allGuiEls, <focusElements, <isCVCWidget = false;
	var <widgetBg, <label, <nameField, wdgtInfo; // elements contained in any kind of CVWidget
	var widgetXY, widgetProps, <>editor;
	var <wdgtControllersAndModels, <midiOscEnv, <>oscReplyPort;
	// persistent widgets
	var isPersistent, oldBounds, oldName;
	// extended API
	var <synchKeys, synchedActions;
	// special bookkeeping for CVWidgetMS
	var msCmds, msSlots;
	var slotCmdName, lastIntSlots, msSlotsChecked = false;
	var lastMsgIndex, msMsgIndexDiffers = false, count = 0;
	// CVWidgetMS
	var <msSize;

	*initClass {
		var scFunc, scPrefs = false;

		Class.initClassTree(KeyDownActions);

		StartUp.add({
			midiSources = ();
			if(Quarks.isInstalled("cruciallib"), {
				Spec.add(\in, StaticIntegerSpec(0, Server.default.options.firstPrivateBus-1, 0));
			})
		});

		prefs = CVCenterPreferences.readPreferences;
		// "prefs[\shortcuts][\cvwidget]: %\n".postf(prefs[\shortcuts][\cvwidget]);
		prefs !? { prefs[\shortcuts] !? { prefs[\shortcuts][\cvwidget] !? { scPrefs = true }}};

		this.shortcuts = IdentityDictionary.new;

		if(scPrefs == false, {
			scFunc =
			"// focus previous widget (alphabetically ordered)
			{ |view|
				block { |break|
					CVCenter.cvWidgets.order.do({ |name, i|
						if(CVCenter.cvWidgets[name].focusElements.includes(view)) {
							break.value(
								CVCenter.cvWidgets[CVCenter.cvWidgets.order.wrapAt(i-1)].parent.front.focus;
								CVCenter.cvWidgets[CVCenter.cvWidgets.order.wrapAt(i-1)].label.focus;
							)
						}
					})
				};
				true;
			}";
			this.shortcuts.put(
				'alt + arrow left',
				(
					func: scFunc,
					keyCode: KeyDownActions.keyCodes['arrow left'],
					modifierQt: KeyDownActions.arrowsModifiersQt[\alt],
					modifierCocoa: KeyDownActions.arrowsModifiersCocoa[\alt]
				)
			);
			scFunc =
			"// focus next widget (alphabetically ordered)
			{ |view|
				block { |break|
					CVCenter.cvWidgets.order.do({ |name, i|
						if(CVCenter.cvWidgets[name].focusElements.includes(view)) {
							break.value(
								CVCenter.cvWidgets[CVCenter.cvWidgets.order.wrapAt(i+1)].parent.front.focus;
								CVCenter.cvWidgets[CVCenter.cvWidgets.order.wrapAt(i+1)].label.focus;
							)
						}
					})
				};
				true;
			}";
			this.shortcuts.put(
				'alt + arrow right',
				(
					func: scFunc,
					keyCode: KeyDownActions.keyCodes['arrow right'],
					modifierQt: KeyDownActions.arrowsModifiersQt[\alt],
					modifierCocoa: KeyDownActions.arrowsModifiersCocoa[\alt]
				)
			);
			scFunc =
			"// open a CVWidget(MS)Editor and focus its Spec tab
			{ |view|
				block { |break|
					CVCenter.all.keys.do({ |key|
						if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
							break.value(
								switch(CVCenter.cvWidgets[key].class,
									CVWidgetMS, {
										if(CVCenter.cvWidgets[key].editor.msEditor.isNil or:{
											CVCenter.cvWidgets[key].editor.msEditor.isClosed }
										) {
											CVWidgetMSEditor(CVCenter.cvWidgets[key], 0)
										} {
											CVCenter.cvWidgets[key].editor.msEditor.front(0)
										}
									},
									CVWidget2D, { #[lo, hi].do({ |slot|
										if(CVCenter.cvWidgets[key].editor[slot].isNil or:{
											CVCenter.cvWidgets[key].editor[slot].isClosed }
										) {
											CVWidgetEditor(CVCenter.cvWidgets[key], 0, slot)
										} {
											CVCenter.cvWidgets[key].editor[slot].front(0)
										}
									})},
									{
										if(CVCenter.cvWidgets[key].editor.isNil or:{
											CVCenter.cvWidgets[key].editor.isClosed }
										) {
											CVWidgetEditor(CVCenter.cvWidgets[key], 0)
										} {
											CVCenter.cvWidgets[key].editor.front(0)
										}
									}
								)
							)
						}
					})
				};
				true;
			}";
			this.shortcuts.put(
				\s,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$s])
			);
			scFunc =
			"// open a CVWidget(MS)Editor and focus its MIDI tab
			{ |view|
				block { |break|
					CVCenter.all.keys.do({ |key|
						if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
							break.value(
								switch(CVCenter.cvWidgets[key].class,
									CVWidgetMS, {
										if(CVCenter.cvWidgets[key].editor.msEditor.isNil or:{
											CVCenter.cvWidgets[key].editor.msEditor.isClosed }
										) {
											CVWidgetMSEditor(CVCenter.cvWidgets[key], 1)
										} {
											CVCenter.cvWidgets[key].editor.msEditor.front(1)
										}
									},
									CVWidget2D, { #[lo, hi].do({ |slot|
										if(CVCenter.cvWidgets[key].editor[slot].isNil or:{
											CVCenter.cvWidgets[key].editor[slot].isClosed }
										) {
											CVWidgetEditor(CVCenter.cvWidgets[key], 1, slot)
										} {
											CVCenter.cvWidgets[key].editor[slot].front(1)
										}
									})},
									{
										if(CVCenter.cvWidgets[key].editor.isNil or:{
											CVCenter.cvWidgets[key].editor.isClosed }
										) {
											CVWidgetEditor(CVCenter.cvWidgets[key], 1)
										} {
											CVCenter.cvWidgets[key].editor.front(1)
										}
									}
								)
							)
						}
					})
				};
				true;
			}";
			this.shortcuts.put(
				\m,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$m])
			);
			scFunc =
			"// open a CVWidget(MS)Editor and focus its OSC tab
			{ |view|
				block { |break|
					CVCenter.all.keys.do({ |key|
						if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
							break.value(
								switch(CVCenter.cvWidgets[key].class,
									CVWidgetMS, {
										if(CVCenter.cvWidgets[key].editor.msEditor.isNil or:{
											CVCenter.cvWidgets[key].editor.msEditor.isClosed }
										) {
											CVWidgetMSEditor(CVCenter.cvWidgets[key], 2)
										} {
											CVCenter.cvWidgets[key].editor.msEditor.front(2)
										}
									},
									CVWidget2D, { #[lo, hi].do({ |slot|
										if(CVCenter.cvWidgets[key].editor[slot].isNil or:{
											CVCenter.cvWidgets[key].editor[slot].isClosed }
										) {
											CVWidgetEditor(CVCenter.cvWidgets[key], 2, slot)
										} {
											CVCenter.cvWidgets[key].editor[slot].front(2)
										}
									})},
									{
										if(CVCenter.cvWidgets[key].editor.isNil or:{
											CVCenter.cvWidgets[key].editor.isClosed }
										) {
											CVWidgetEditor(CVCenter.cvWidgets[key], 2)
										} {
											CVCenter.cvWidgets[key].editor.front(2)
										}
									}
								)
							)
						}
					})
				};
				true;
			}";
			this.shortcuts.put(
				\o,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$o])
			);
			scFunc =
			"// open a CVWidget(MS)Editor and focus its Actions tab
			{ |view|
				block { |break|
					CVCenter.all.keys.do({ |key|
						if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
							break.value(
								switch(CVCenter.cvWidgets[key].class,
									CVWidgetMS, {
										if(CVCenter.cvWidgets[key].editor.msEditor.isNil or:{
											CVCenter.cvWidgets[key].editor.msEditor.isClosed }
										) {
											CVWidgetMSEditor(CVCenter.cvWidgets[key], 3)
										} {
											CVCenter.cvWidgets[key].editor.msEditor.front(3)
										}
									},
									CVWidget2D, { #[lo, hi].do({ |slot|
										if(CVCenter.cvWidgets[key].editor[slot].isNil or:{
											CVCenter.cvWidgets[key].editor[slot].isClosed }
										) {
											CVWidgetEditor(CVCenter.cvWidgets[key], 3, slot)
										} {
											CVCenter.cvWidgets[key].editor[slot].front(3)
										}
									})},
									{
										if(CVCenter.cvWidgets[key].editor.isNil or:{
											CVCenter.cvWidgets[key].editor.isClosed }
										) {
											CVWidgetEditor(CVCenter.cvWidgets[key], 3)
										} {
											CVCenter.cvWidgets[key].editor.front(3)
										}
									}
								)
							)
						}
					})
				};
				true;
			}";
			this.shortcuts.put(
				\a,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$a])
			);
			scFunc =
			"// set focus to the view that contains the widget
			{ CVCenter.prefPane.focus }";
			this.shortcuts.put(
				\esc,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[\esc])
			);
			scFunc =
			"// start or stop OSC calibration
			{ |view|
				block { |break|
					CVCenter.all.keys.do({ |key|
						if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
							break.value(
								switch(CVCenter.cvWidgets[key].class,
									CVWidgetMS, {
										if(
											CVCenter.cvWidgets[key].msSize.collect(
												CVCenter.cvWidgets[key].getCalibrate(_)
											).select(_ == true).size == CVCenter.cvWidgets[key].msSize
										) {
											CVCenter.cvWidgets[key].msSize.do(
												CVCenter.cvWidgets[key].setCalibrate(false, _)
											)
										} {
											CVCenter.cvWidgets[key].msSize.do(
												CVCenter.cvWidgets[key].setCalibrate(true, _)
											)
										}
									},
									CVWidget2D, {
										if(
											#[lo, hi].collect(
												CVCenter.cvWidgets[key].getCalibrate(_)
											).select(_ == true).size == 2
										) {
											#[lo, hi].do(CVCenter.cvWidgets[key].setCalibrate(false, _))
										} {
											#[lo, hi].do(CVCenter.cvWidgets[key].setCalibrate(true, _))
										}
									},
									{
										if(CVCenter.cvWidgets[key].getCalibrate == true) {
											CVCenter.cvWidgets[key].setCalibrate(false)
										} {
											CVCenter.cvWidgets[key].setCalibrate(true)
										}
									}
								)
							)
						}
					})
				};
				true;
			}";
			this.shortcuts.put(
				\c,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$c])
			);
			scFunc =
			"// reset current OSC calibration constraints and start OSC calibration
			{ |view|
				block { |break|
					CVCenter.all.keys.do({ |key|
						if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
							break.value(
								switch(CVCenter.cvWidgets[key].class,
									CVWidgetMS, {
										CVCenter.cvWidgets[key].msSize.do(
											CVCenter.cvWidgets[key].setOscInputConstraints(Point(0.0001), _);
											CVCenter.cvWidgets[key].setCalibrate(true, _)
										)
									},
									CVWidget2D, {
										#[lo, hi].do(
											CVCenter.cvWidgets[key].setOscInputConstraints(Point(0.0001), _);
											CVCenter.cvWidgets[key].setCalibrate(true, _)
										)
									},
									{
										CVCenter.cvWidgets[key]
										.setOscInputConstraints(Point(0.0001))
										.setCalibrate(true)
									}
								)
							)
						}
					})
				};
				true;
			}";
			this.shortcuts.put(
				\r,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$r])
			);
			scFunc =
			"// connect or disconnect sliders
			{ |view|
				CVCenter.all.keys.do({ |key|
					if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
						CVCenter.cvWidgets[key].connectGUI(CVCenter.cvWidgets[key].connectS.not, nil);
					}
				});
				true;
			}";
			this.shortcuts.put(
				'shift + b',
				(
					func: scFunc,
					keyCode: KeyDownActions.keyCodes[$b],
					modifierQt: KeyDownActions.modifiersQt[\shift],
					modifierCocoa: KeyDownActions.modifiersCocoa[\shift]
				)
			);
			scFunc =
			"// connect or disconnect textfields
			{ |view|
				CVCenter.all.keys.do({ |key|
					if(CVCenter.cvWidgets[key].focusElements.includes(view)) {
						CVCenter.cvWidgets[key].connectGUI(nil, CVCenter.cvWidgets[key].connectTF.not);
					}
				});
				true;
			}";
			this.shortcuts.put(
				'shift + v',
				(
					func: scFunc,
					keyCode: KeyDownActions.keyCodes[$v],
					modifierQt: KeyDownActions.modifiersQt[\shift],
					modifierCocoa: KeyDownActions.modifiersCocoa[\shift]
				)
			);
		}, {
			this.shortcuts = prefs[\shortcuts][\cvwidget];
		})
	}

	setup {
		^(
			midiMode: prMidiMode,
			midiResolution: prMidiResolution,
			midiMean: prMidiMean,
			ctrlButtonBank: prCtrlButtonBank,
			softWithin: prSoftWithin,
			calibrate: prCalibrate
		);
	}

	toggleComment { |visible|
		visible.switch(
			false, {
				visibleGuiEls.do({ |el|
					if(el.isKindOf(Collection), {
						el.do(_.visible_(true))
					}, {
						el.visible_(true)
					});
					nameField.visible_(false);
				})
			},
			true, {
				visibleGuiEls.do({ |el|
					if(el.isKindOf(Collection), {
						el.do(_.visible_(false))
					}, {
						el.visible_(false)
					});
					nameField.visible_(true);
				})
			}
		)
	}

	widgetXY_ { |point|
		var originXZero, originYZero;
		originXZero = allGuiEls.collect({ |view|
			if(view.class == List, {
				view.collect({ |el| el.bounds.left })
			}, { view.bounds.left });
		});
		originXZero = originXZero-originXZero.copy.takeThese({ |it| it.isArray }).minItem;
		originYZero = allGuiEls.collect({ |view|
			if(view.class == List, {
				view.collect({ |el| el.bounds.top })
			}, { view.bounds.top });
		});
		originYZero = originYZero-originYZero.copy.takeThese({ |it| it.isArray }).minItem;

		allGuiEls.do({ |view, i|
			if(view.class == List, {
				view.do({ |el, j|
					el.bounds_(
						Rect(
							originXZero[i][j]+point.x,
							originYZero[i][j]+point.y,
							el.bounds.width,
							el.bounds.height
						)
					)
				});
			}, {
				view.bounds_(Rect(
					originXZero[i]+point.x,
					originYZero[i]+point.y,
					view.bounds.width,
					view.bounds.height
				))
			})
		})
	}

	widgetXY {
		^widgetBg.bounds.left@widgetBg.bounds.top;
	}

	widgetProps {
		^widgetBg.bounds.width@widgetBg.bounds.height;
	}

	bounds {
		^Rect(this.widgetXY.x, this.widgetXY.y, this.widgetProps.x, this.widgetProps.y);
	}

	remove {
		allGuiEls.do({ |el|
			if(el.class == List, { el.do(_.remove) }, { el.remove });
		});
	}

	close {
		if(isCVCWidget and:{ isPersistent == false or:{ isPersistent == nil }}, { this.remove }, { parent.close });
	}

	addAction { |name, action, slot, active=true|
		var act, controller, thisEditor;

		// "\nACTION!!!\n".postln;
		// "this.guiEnv: %\n".postf(this.guiEnv.asCompileString);

		switch(this.class,
			CVWidgetKnob, { thisEditor = this.guiEnv.editor },
			CVWidget2D, { thisEditor = this.guiEnv[slot.asSymbol].editor },
			CVWidgetMS, { thisEditor = this.guiEnv.msEditor }
		);

		name ?? { Error("Please provide a name under which the action will be added to the widget").throw };
		action ?? { Error("Please provide an action!").throw };
		if(action.isFunction.not and:{
			action.interpret.isFunction.not
		}, {
			Error("'action' must be a function or a string that compiles to one").throw;
		});
		this.wdgtActions ?? { this.wdgtActions = () };
		if(action.class === String, { act = action.interpret }, { act = action });
		switch(this.class,
			CVWidget2D, {
				slot ?? { Error("Please provide either 'lo' or 'hi' as third argument to addAction!").throw };
				this.wdgtActions[slot.asSymbol] ?? { this.wdgtActions.put(slot.asSymbol, ()) };
				// avoid duplicates
				this.wdgtActions[slot.asSymbol][name.asSymbol] ?? { this.wdgtActions[slot.asSymbol].put(name.asSymbol, ()) };
				if(this.wdgtActions[slot.asSymbol][name.asSymbol].size < 1, {
					if(active == true, {
						controller = widgetCV[slot.asSymbol].action_(act);
						this.wdgtActions[slot.asSymbol][name.asSymbol].put(controller, [act.asCompileString, true]);
					}, {
						controller = \dummy;
						this.wdgtActions[slot.asSymbol][name.asSymbol].put(controller, [act.asCompileString, false]);
					});
					wdgtControllersAndModels[slot.asSymbol].actions.model.value_((
						numActions: this.wdgtActions[slot.asSymbol].size,
						activeActions: this.wdgtActions[slot.asSymbol].select({ |v| v.asArray[0][1] == true }).size
					)).changedKeys(synchKeys);
					// thisGuiEnv = this.guiEnv[slot.asSymbol];
					if(thisEditor.notNil and: {
						thisEditor.isClosed.not;
					}, {
						thisEditor.amendActionsList(
							this, \add, name.asSymbol, this.wdgtActions[slot.asSymbol][name.asSymbol], slot.asSymbol, active;
						)
					})
				})
			},
			{
				this.wdgtActions[name.asSymbol] ?? {
					this.wdgtActions.put(name.asSymbol, ());
					if(active == true, {
						controller = widgetCV.action_(act);
						this.wdgtActions[name.asSymbol].put(controller, [act.asCompileString, true]);
					}, {
						controller = \dummy;
						this.wdgtActions[name.asSymbol].put(controller, [act.asCompileString, false]);
					});
					wdgtControllersAndModels.actions.model.value_((
						numActions: this.wdgtActions.size,
						activeActions: this.wdgtActions.select({ |v| v.asArray[0][1] == true }).size
					)).changedKeys(synchKeys);
				};
				// thisGuiEnv = this.guiEnv;
				// "thisGuiEnv: %\n".postf(thisGuiEnv);
				if(thisEditor.notNil and: {
					thisEditor.isClosed.not;
				}, {
					thisEditor.amendActionsList(
						this, \add, name.asSymbol, this.wdgtActions[name.asSymbol], active: active;
					)
				})
			}
		)
	}

	removeAction { |name, slot|
		var controller, thisEditor;

		switch(this.class,
			CVWidgetKnob, { thisEditor = this.guiEnv.editor },
			CVWidget2D, { thisEditor = this.guiEnv[slot.asSymbol].editor },
			CVWidgetMS, { thisEditor = this.guiEnv.msEditor }
		);

		name ?? { Error("Please provide the action's name!").throw };
		switch(this.class,
			CVWidget2D, {
				slot ?? { Error("Please provide either 'lo' or 'hi' as second argument to removeAction!").throw };
				// thisGuiEnv = this.guiEnv[slot.asSymbol];
				this.wdgtActions[slot.asSymbol][name.asSymbol] !? {
					this.wdgtActions[slot.asSymbol][name.asSymbol].keys.do({ |c|
						if(c.class === SimpleController, { c.remove });
					});
					this.wdgtActions[slot.asSymbol].removeAt(name.asSymbol);
					this.wdgtActions[slot.asSymbol].isEmpty.if { this.wdgtActions.removeAt(slot.asSymbol) };
					wdgtControllersAndModels[slot.asSymbol].actions.model.value_((
						numActions: this.wdgtActions[slot.asSymbol].size,
						activeActions: this.wdgtActions[slot.asSymbol].select({ |v| v.asArray[0][1] == true }).size
					)).changedKeys(synchKeys);
					if(thisEditor.notNil and: {
						thisEditor.isClosed.not;
					}, {
						thisEditor.amendActionsList(
							this, \remove, name.asSymbol;
						)
					})
				}
			},
			{
				// thisGuiEnv = this.guiEnv;
				this.wdgtActions[name.asSymbol] !? {
					this.wdgtActions[name.asSymbol].keys.do({ |c|
						if(c.class === SimpleController, { c.remove });
					});
					this.wdgtActions.removeAt(name.asSymbol);
					wdgtControllersAndModels.actions.model.value_((
						numActions: this.wdgtActions.size,
						activeActions: this.wdgtActions.select({ |v| v.asArray[0][1] == true }).size
					)).changedKeys(synchKeys);
					if(thisEditor.notNil and: {
						thisEditor.isClosed.not;
					}, {
						thisEditor.amendActionsList(
							this, \remove, name.asSymbol;
						)
					})
				}
			}
		);
		controller.do({ |c| c = nil });
	}

	activateAction { |name, activate=true, slot|
		var action, actions, cv, thisEditor, wcm, controller, thisAction;

		switch(this.class,
			CVWidget2D, {
				cv = widgetCV[slot.asSymbol];
				actions = this.wdgtActions[slot.asSymbol];
				action = this.wdgtActions[slot.asSymbol][name.asSymbol];
				wcm = wdgtControllersAndModels[slot.asSymbol];
			},
			{
				cv = widgetCV;
				actions = this.wdgtActions;
				action = this.wdgtActions[name.asSymbol];
				wcm = wdgtControllersAndModels;
			}
		);

		switch(this.class,
			CVWidgetKnob, { thisEditor = this.guiEnv.editor },
			CVWidget2D, { thisEditor = this.guiEnv[slot.asSymbol].editor },
			CVWidgetMS, { thisEditor = this.guiEnv.msEditor }
		);

		if(action.notNil, {
			switch(activate,
				true, {
					if(action.keys.asArray[0].class != SimpleController, {
						if(action.asArray[0][0].class === String, {
							thisAction = action.asArray[0][0].interpret;
						}, {
							thisAction = action.asArray[0][0];
						});
						controller = cv.action_(thisAction);
						action.put(controller, [thisAction.asCompileString, true]);
						action.removeAt(\dummy);
					})
				},
				false, {
					if(action.keys.asArray[0].class == SimpleController, {
						controller = action.keys.asArray[0];
						controller.remove;
						action.put(\dummy, [action.asArray[0][0], false]);
						action[controller] = nil;
					})
				}
			);
			wcm.actions.model.value_((
				numActions: actions.size,
				activeActions: actions.select({ |v| v.asArray[0][1] == true }).size
			)).changedKeys(synchKeys);
			if(thisEditor.notNil and: {
				thisEditor.isClosed.not;
			}, {
				switch(activate,
					true, {
						thisEditor.actionsUIs[name.asSymbol].activate.value_(1);
					},
					false, {
						thisEditor.actionsUIs[name.asSymbol].activate.value_(0);
					}
				)
			})
		})
	}

	connectGUI { |connectSlider = true, connectTextField = true|
		wdgtControllersAndModels.slidersTextConnection.model.value_(
			[connectSlider, connectTextField]
		).changedKeys(synchKeys)
	}

	setMidiMode { |mode, slot|
		var thisSlot, wcm;

		if(mode.asInteger != 0 and:{ mode.asInteger != 1 }, {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		});

		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[slot];
			},
			{ wcm = wdgtControllersAndModels }
		);

		switch(this.class,
			CVWidgetKnob, {
				prMidiMode = mode;
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode,
						midiMean: prMidiMean,
						ctrlButtonBank: prCtrlButtonBank,
						midiResolution: prMidiResolution,
						softWithin: prSoftWithin
					)
				).changedKeys(synchKeys);
			},
			{
				prMidiMode[thisSlot] = mode;
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode[thisSlot],
						midiMean: prMidiMean[thisSlot],
						ctrlButtonBank: prCtrlButtonBank[thisSlot],
						midiResolution: prMidiResolution[thisSlot],
						softWithin: prSoftWithin[thisSlot]
					)
				).changedKeys(synchKeys);
			}
		);
	}

	getMidiMode { |slot|
		var thisSlot;

		switch(this.class,
			CVWidget2D, { thisSlot = slot.asSymbol },
			CVWidgetMS, { thisSlot = slot.asInteger }
		);
		switch(this.class,
			CVWidgetKnob, {
				^prMidiMode;
			},
			{ ^prMidiMode[thisSlot] }
		)
	}

	setMidiMean { |meanval, slot|
		var thisSlot, thisMeanVal, wcm;

		thisMeanVal = meanval.asInteger;

		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
			},
			{ wcm = wdgtControllersAndModels }
		);
		switch(this.class,
			CVWidgetKnob, {
				prMidiMean = thisMeanVal;
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode,
						midiMean: prMidiMean,
						ctrlButtonBank: prCtrlButtonBank,
						midiResolution: prMidiResolution,
						softWithin: prSoftWithin
					)
				).changedKeys(synchKeys);
			},
			{
				prMidiMean[thisSlot] = thisMeanVal;
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode[thisSlot],
						midiMean: prMidiMean[thisSlot],
						ctrlButtonBank: prCtrlButtonBank[thisSlot],
						midiResolution: prMidiResolution[thisSlot],
						softWithin: prSoftWithin[thisSlot]
					)
				).changedKeys(synchKeys);
			}
		)
	}

	getMidiMean { |slot|
		var thisSlot;
		switch(this.class,
			CVWidget2D, { thisSlot = slot.asSymbol },
			CVWidgetMS, { thisSlot = slot.asInteger }
		);
		switch(this.class,
			CVWidgetKnob, {
				^prMidiMean;
			},
			{ ^prMidiMean[thisSlot] }
		)
	}

	setSoftWithin { |threshold, slot|
		var thisSlot, thisThresh, wcm;

		thisThresh = threshold.asFloat;
		if(thisThresh > 0.5 or:{ thisThresh < 0.01 }, {
			Error("threshold must be between 0.01 and 0.5").throw;
		});

		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
			},
			{ wcm = wdgtControllersAndModels }
		);
		switch(this.class,
			CVWidgetKnob, {
				prSoftWithin = thisThresh;
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode,
						midiMean: prMidiMean,
						ctrlButtonBank: prCtrlButtonBank,
						midiResolution: prMidiResolution,
						softWithin: prSoftWithin
					)
				).changedKeys(synchKeys);
			},
			{
				prSoftWithin[thisSlot] = thisThresh;
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode[thisSlot],
						midiMean: prMidiMean[thisSlot],
						ctrlButtonBank: prCtrlButtonBank[thisSlot],
						midiResolution: prMidiResolution[thisSlot],
						softWithin: prSoftWithin[thisSlot]
					)
				).changedKeys(synchKeys);
			}
		)
	}

	getSoftWithin { |slot|
		var thisSlot;
		switch(this.class,
			CVWidget2D, { thisSlot = slot.asSymbol },
			CVWidgetMS, { thisSlot = slot.asInteger }
		);
		switch(this.class,
			CVWidgetKnob, {
				^prSoftWithin;
			},
			{ ^prSoftWithin[thisSlot] }
		)
	}

	setCtrlButtonBank { |numSliders, slot|
		var thisSlot, wcm;

		if(numSliders.notNil and:{ numSliders.isInteger.not }, {
			Error("setCtrlButtonBank: 'numSliders' must either be an Integer or nil!").throw;
		});

		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
			},
			{ wcm = wdgtControllersAndModels }
		);
		switch(this.class,
			CVWidgetKnob, {
				if(numSliders.asString == "nil" or:{ numSliders.asInteger === 0 }, {
					prCtrlButtonBank = nil;
				}, {
					prCtrlButtonBank = numSliders.asInteger;
				});
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode,
						midiMean: prMidiMean,
						ctrlButtonBank: prCtrlButtonBank,
						midiResolution: prMidiResolution,
						softWithin: prSoftWithin
					)
				).changedKeys(synchKeys);
			},
			{
				prCtrlButtonBank.put(thisSlot, numSliders);
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode[thisSlot],
						midiMean: prMidiMean[thisSlot],
						ctrlButtonBank: prCtrlButtonBank[thisSlot],
						midiResolution: prMidiResolution[thisSlot],
						softWithin: prSoftWithin[thisSlot]
					)
				).changedKeys(synchKeys);
			}
		)
	}

	getCtrlButtonBank { |slot|
		var thisSlot;
		switch(this.class,
			CVWidget2D, { thisSlot = slot.asSymbol },
			CVWidgetMS, { thisSlot = slot.asInteger }
		);
		switch(this.class,
			CVWidgetKnob, {
				^prCtrlButtonBank;
			},
			{ ^prCtrlButtonBank[thisSlot] }
		)
	}

	setMidiResolution { |resolution, slot|
		var thisSlot, wcm;

		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
			},
			{ wcm = wdgtControllersAndModels }
		);
		switch(this.class,
			CVWidgetKnob, {
				prMidiResolution = resolution;
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode,
						midiMean: prMidiMean,
						ctrlButtonBank: prCtrlButtonBank,
						midiResolution: prMidiResolution,
						softWithin: prSoftWithin
					)
				).changedKeys(synchKeys);
			},
			{
				prMidiResolution[thisSlot] = resolution;
				wcm.midiOptions.model.value_(
					(
						midiMode: prMidiMode[thisSlot],
						midiMean: prMidiMean[thisSlot],
						ctrlButtonBank: prCtrlButtonBank[thisSlot],
						midiResolution: prMidiResolution[thisSlot],
						softWithin: prSoftWithin[thisSlot]
					)
				).changedKeys(synchKeys);
			}
		)
	}

	getMidiResolution { |slot|
		var thisSlot;
		switch(this.class,
			CVWidget2D, { thisSlot = slot.asSymbol },
			CVWidgetMS, { thisSlot = slot.asInteger }
		);
		switch(this.class,
			CVWidgetKnob, {
				^prMidiResolution;
			},
			{ ^prMidiResolution[thisSlot] }
		)
	}

	setCalibrate { |bool, slot|
		var thisSlot, wcm;

		// "setCalibrate called: %\n".postf(this.name);

		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
			},
			{ wcm = wdgtControllersAndModels }
		);
		if(bool.isKindOf(Boolean).not, {
			Error("calibration can only be set to true or false!").throw;
		});
		switch(this.class,
			CVWidgetKnob, {
				prCalibrate = bool;
			},
			{
				prCalibrate[thisSlot] = bool;
			}
		);
		wcm.oscConnection.model.value_(
			wcm.oscConnection.model.value
		).changedKeys(synchKeys);
		wcm.calibration.model.value_(bool).changedKeys(synchKeys);
	}

	getCalibrate { |slot|
		var thisSlot;
		switch(this.class,
			CVWidget2D, { thisSlot = slot.asSymbol },
			CVWidgetMS, { thisSlot = slot.asInteger }
		);
		switch(this.class,
			CVWidgetKnob, {
				^prCalibrate;
			},
			{ ^prCalibrate[thisSlot] }
		)
	}

	setSpec { |spec, slot|
		var thisSpec;
		if((thisSpec = spec.asSpec).isKindOf(ControlSpec).not, {
			Error("Please provide a valid spec! (its class must inherit from ControlSpec)").throw;
		});
		switch(this.class,
			CVWidget2D, {
				wdgtControllersAndModels[slot.asSymbol].cvSpec.model.value_(thisSpec).changedKeys(synchKeys);
			},
			{
				wdgtControllersAndModels.cvSpec.model.value_(thisSpec).changedKeys(synchKeys);
			}
		)
	}

	getSpec { |slot|
		switch(this.class,
			CVWidget2D, {
				^widgetCV[slot.asSymbol].spec;
			},
			{
				^widgetCV.spec;
			}
		)
	}

	setOscMapping { |mapping, slot|
		var thisSlot, wcm;
		var thisMapping;

		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
			},
			{ wcm = wdgtControllersAndModels }
		);

		// "mapping: %\n".postf(mapping);

		if(mapping.asSymbol !== \linlin and:{
			mapping.asSymbol !== \linexp and:{
				mapping.asSymbol !== \explin and:{
					mapping.asSymbol !== \expexp
				}
			}
		}, {
			Error("A valid mapping can either be \\linlin, \\linexp, \\explin or \\expexp").throw;
		});

		if(mapping.asSymbol === \linexp or:{ mapping.asSymbol === \expexp }, {
			if(this.getSpec(thisSlot).safeHasZeroCrossing, { thisMapping = \linlin }, { thisMapping = mapping.asSymbol });
		}, { thisMapping = mapping.asSymbol });

		switch(this.class,
			CVWidgetKnob, {
				midiOscEnv.oscMapping = thisMapping;
				wcm.oscInputRange.model.value_(
					wcm.oscInputRange.model.value;
				).changedKeys(synchKeys);
			},
			{
				midiOscEnv[thisSlot].oscMapping = thisMapping;
				wcm.oscInputRange.model.value_(
					wcm.oscInputRange.model.value;
				).changedKeys(synchKeys);
			}
		)
	}

	getOscMapping { |slot|
		var thisSlot;
		switch(this.class,
			CVWidget2D, { thisSlot = slot.asSymbol },
			CVWidgetMS, { thisSlot = slot.asInteger }
		);
		switch(this.class,
			CVWidgetKnob, {
				^midiOscEnv.oscMapping;
			},
			{
				^midiOscEnv[thisSlot].oscMapping
			}
		)
	}

	oscConnect { |ip, port, name, oscMsgIndex=1, slot|
		var thisSlot, wcm;
		var thisIP, intPort;

		// "oscConnect called: %\n".postf(this.name);

		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
			},
			{ wcm = wdgtControllersAndModels }
		);

		ip !? { thisIP = ip.asString.replace(" ", "") };

		if(thisIP.size > 0 and:{
			"^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$".matchRegexp(thisIP).not and:{
				thisIP != "nil"
			}
		}, {
			Error("Please provide a valid IP-address or leave the IP-field empty").throw;
		});

		if(thisIP.size == 0 or:{ thisIP == "nil" }, { thisIP = nil });

		intPort = port.asString;

		if(intPort.size > 0, {
			if("^[0-9]{1,5}$".matchRegexp(intPort).not and:{ intPort != "nil" }, {
				Error("Please provide a valid port or leave this field empty").throw;
			}, {
				intPort = intPort.asInteger;
			})
		});

		if(port == "nil" or:{ port == nil }, { intPort = nil });

		if("^\/".matchRegexp(name.asString).not, {
			Error("You have to supply a valid OSC-typetag (command-name), beginning with an \"/\" as third argument to oscConnect").throw;
		});

		if(oscMsgIndex.isKindOf(Integer).not, {
			Error("You have to supply an integer as forth argument to oscConnect").throw;
		});

		if(slot.notNil, {
			if(midiOscEnv[thisSlot].oscResponder.notNil, { "Already connected!".warn });
		}, {
			if(midiOscEnv.oscResponder.notNil, { "Already connected!".warn });
		});

		wcm.oscConnection.model.value_([thisIP, intPort, name.asSymbol, oscMsgIndex]).changedKeys(synchKeys);
		switch(this.class,
			CVWidgetKnob, {
				CmdPeriod.add({ if(this.class.removeResponders, { this.oscDisconnect }) });
			},
			{
				CmdPeriod.add({ if(this.class.removeResponders, { this.oscDisconnect(thisSlot) }) });
			}
		)
	}

	oscDisconnect { |slot|
		var thisSlot, wcm;
		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
			},
			{ wcm = wdgtControllersAndModels }
		);
		wcm.oscConnection.model.value_(false).changedKeys(synchKeys);
		wcm.oscInputRange.model.value_([0.00001, 0.00001]).changedKeys(synchKeys);
		switch(this.class,
			CVWidgetKnob, {
				CmdPeriod.remove({ this.oscDisconnect });
			},
			{
				CmdPeriod.remove({ this.oscDisconnect(slot) });
			}
		)
	}

	// if all arguments besides 'slot' are nil .learn should be triggered
	midiConnect { |uid, chan, num, slot|
		var thisSlot, wcm;
		switch(this.class,
			CVWidget2D, {
				slot ?? {
					Error("Missing 'slot'-argument. Maybe you forgot to explicitely provide the slot: e.g. <wdgt>.midiConnect(slot: \lo)").throw;
				};
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot]
			},
			CVWidgetMS, {
				slot ?? {
					Error("Missing 'slot'-argument. Maybe you forgot to explicitely provide the slot: e.g. <wdgt>.midiConnect(slot: 0)").throw;
				};
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
			},
			{ wcm = wdgtControllersAndModels }
		);
		switch(this.class,
			CVWidgetKnob, {
				if(midiOscEnv.cc.isNil, {
					wcm.midiConnection.model.value_(
						(src: uid, chan: chan, num: num)
					).changedKeys(synchKeys);
					CmdPeriod.add({ if(this.class.removeResponders, {
						this !? { this.midiDisconnect(thisSlot) }
					}) });
				}, {
					"Already connected!".warn;
				})
			},
			{
				if(midiOscEnv[slot].cc.isNil, {
					wcm.midiConnection.model.value_(
						(src: uid, chan: chan, num: num)
					).changedKeys(synchKeys);
					CmdPeriod.add({ if(this.class.removeResponders, {
						this !? { this.midiDisconnect(thisSlot) }
					}) });
				}, {
					"Already connected!".warn;
				})
			}
		)
	}

	midiDisconnect { |slot|
		var thisSlot, wcm;
		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot]
			},
			{ wcm = wdgtControllersAndModels }
		);
		wcm.midiConnection.model.value_(nil).changedKeys(synchKeys);
		switch(this.class,
			CVWidgetKnob, {
				CmdPeriod.remove({ this.midiDisconnect });
			},
			{
				CmdPeriod.remove({ this.midiDisconnect(slot) });
			}
		)
	}

	setOscInputConstraints { |constraintsHiLo, slot|
		var thisSlot, thisEditor,wcm;

		switch(this.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = wdgtControllersAndModels[thisSlot];
				thisEditor = this.editor[thisSlot];
			},
			CVWidgetMS, {
				thisSlot = slot.asInteger;
				wcm = wdgtControllersAndModels.slots[thisSlot];
				thisEditor = this.editor.editors[thisSlot];
			},
			{
				wcm = wdgtControllersAndModels;
				thisEditor = this.editor;
			}
		);

		if(constraintsHiLo.isKindOf(Point).not, {
			Error("setOSCInputConstraints expects a Point in the form of Point(lo, hi) or lo@hi").throw;
		}, {
			this.setCalibrate(false, slot);
			wcm.oscInputRange.model.value_([constraintsHiLo.x, constraintsHiLo.y]).changedKeys(synchKeys);
		})
	}

	getOscInputConstraints { |slot|
		var thisSlot;
		switch(this.class,
			CVWidget2D, { thisSlot = slot.asSymbol },
			CVWidgetMS, { thisSlot = slot.asInteger }
		);
		switch(this.class,
			CVWidgetKnob, {
				^midiOscEnv.calibConstraints;
			},
			{
				^midiOscEnv[thisSlot].calibConstraints;
			}
		)
	}

	setOSCfeedback { |cv, cmd, port, slot|
		var constr, thisSlot, thisMidiOscEnv;
		switch(this.class,
			CVWidget2D, { thisSlot = slot.asSymbol },
			CVWidgetMS, { thisSlot = slot.asInteger }
		);
		switch(this.class,
			CVWidgetKnob, { thisMidiOscEnv = midiOscEnv },
			{ thisMidiOscEnv = midiOscEnv[thisSlot] }
		);
		// what if more than 1 reply-address??
		// keep an array of NetAddresses??
		constr = Point(midiOscEnv.calibConstraints.lo, midiOscEnv.calibConstraints.hi);
		midiOscEnv.oscReplyAddrs.do({ |addr|
			if(addr.port != port, { addr.port_(port) });
			addr.sendMsg(cmd, cv.input.linlin(0, 1, constr.x, constr.y));
		})
	}

	front {
		parent.front;
	}

	window { ^parent }

	isClosed {
		if(isCVCWidget, {
			// we're within a CVCenter-gui or some other gui
			// -> a widget 'is closed' if its elements have been removed
			if(allGuiEls.select({ |el|
				if(el.class == List, {
					el.select(_.isClosed).size == 0
				}, { el.isClosed.not })
			}).size == 0, { ^true }, { ^false });
		}, {
			// we just want to check for a single widget resp. its parent parent
			^parent.isClosed;
		})
	}

	// controllers, controllers, controllers...

	initControllersAndModels { |controllersAndModels, slot|
		var wcm, tmp;

		if(controllersAndModels.notNil, {
			// "controllersAndModels not nil???".postln;
			wdgtControllersAndModels = controllersAndModels;
		}, {
			// "controllersAndModels: %\n".postf(controllersAndModels);
			switch(this.class,
				CVWidgetMS, {
					wdgtControllersAndModels ?? {
						wdgtControllersAndModels = (slots: Array.newClear(msSize))
					}
				},
				{ wdgtControllersAndModels ?? { wdgtControllersAndModels = () }}
			)
		});

		slot !? {
			switch(this.class,
				CVWidget2D, {
					wdgtControllersAndModels[slot] ?? { wdgtControllersAndModels.put(slot, ()) }
				},
				CVWidgetMS, {
					wdgtControllersAndModels.slots[slot] ?? { wdgtControllersAndModels.slots[slot] = () }
				}
			)
		};

		if(slot.notNil, {
			switch(this.class,
				CVWidget2D, { wcm = wdgtControllersAndModels[slot] },
				CVWidgetMS, { wcm = wdgtControllersAndModels.slots[slot] }
			)
		}, {
			wcm = wdgtControllersAndModels;
		});

		wcm.calibration ?? { wcm.calibration = () };
		wcm.calibration.model ?? {
			if(slot.notNil, {
				wcm.calibration.model = Ref(prCalibrate[slot]);
			}, {
				wcm.calibration.model = Ref(prCalibrate);
			})
		};

		switch(this.class,
			CVWidgetMS, {
				wdgtControllersAndModels.cvSpec ?? {
					wdgtControllersAndModels.cvSpec = ();
				};
				wdgtControllersAndModels.cvSpec.model ?? {
					wdgtControllersAndModels.cvSpec.model = Ref(this.getSpec);
				}
			},
			{
				wcm.cvSpec ?? { wcm.cvSpec = () };
				wcm.cvSpec.model ?? {
					wcm.cvSpec.model = Ref(this.getSpec(slot));
				}
			}
		);

		wcm.oscInputRange ?? { wcm.oscInputRange = () };
		wcm.oscInputRange.model ?? {
			wcm.oscInputRange.model = Ref([0.0001, 0.0001]);
		};

		wcm.oscConnection ?? { wcm.oscConnection = () };
		wcm.oscConnection.model ?? {
			wcm.oscConnection.model = Ref(false);
		};

		wcm.oscDisplay ?? { wcm.oscDisplay = () };
		wcm.oscDisplay.model ?? {
			if(this.class == CVWidgetMS, { tmp = slot.asString++": edit OSC" }, { tmp = "edit OSC" });
			wcm.oscDisplay.model = Ref((
				but: [tmp, stringColor, background],
				ipField: nil,
				portField: nil,
				nameField: "/my/cmd/name",
				index: 1,
				connectorButVal: 0,
				editEnabled: true
			))
		};

		wcm.midiConnection ?? { wcm.midiConnection = () };
		wcm.midiConnection.model ?? {
			wcm.midiConnection.model = Ref(nil);
		};

		wcm.midiDisplay ?? { wcm.midiDisplay = () };
		wcm.midiDisplay.model ?? {
			wcm.midiDisplay.model = Ref((src: "source", chan: "chan", ctrl: "ctrl", learn: "L"));
		};

		wcm.midiOptions ?? { wcm.midiOptions = () };
		wcm.midiOptions.model ?? {
			switch(this.class,
				CVWidgetMS, {
					wcm.midiOptions.model = Ref(
						(
							midiMode: prMidiMode[slot],
							midiMean: prMidiMean[slot],
							ctrlButtonBank: prCtrlButtonBank[slot],
							midiResolution: prMidiResolution[slot],
							softWithin: prSoftWithin[slot]
						)
					)
				},
				{ wcm.midiOptions.model = Ref(
					(
						midiMode: prMidiMode,
						midiMean: prMidiMean,
						ctrlButtonBank: prCtrlButtonBank,
						midiResolution: prMidiResolution,
						softWithin: prSoftWithin
					)
				)}
			)
		};

		wcm.mapConstrainterLo ?? {
			wcm.mapConstrainterLo = CV([-inf, inf].asSpec, wcm.oscInputRange.model.value[0])
		};
		wcm.mapConstrainterHi ?? {
			wcm.mapConstrainterHi = CV([-inf, inf].asSpec, wcm.oscInputRange.model.value[1])
		};

		switch(this.class,
			CVWidgetMS, {
				wdgtControllersAndModels.actions ?? {
					wdgtControllersAndModels.actions = ()
				};
				wdgtControllersAndModels.actions.model ?? {
					wdgtControllersAndModels.actions.model = Ref((numActions: 0, activeActions: 0))
				}
			},
			{
				wcm.actions ?? {
					wcm.actions = ();
				};
				wcm.actions.model ?? {
					wcm.actions.model = Ref((numActions: 0, activeActions: 0))
				}
			}
		);

		wdgtControllersAndModels.slidersTextConnection ?? {
			wdgtControllersAndModels.slidersTextConnection = ()
		};
		wdgtControllersAndModels.slidersTextConnection.model ?? {
			wdgtControllersAndModels.slidersTextConnection.model = Ref([true, true])
		};

	}

	initControllerActions { |slot|
		var wcm, thisGuiEnv, midiOscEnv, tmpSetup, thisWidgetCV;
		var thisCalib;

//		(
//			slot: slot,
//			wdgtControllersAndModels: wdgtControllersAndModels[slot],
//			midiOscEnv: this.midiOscEnv[slot],
//			widgetCV: this.widgetCV,
//			guiEnv: this.guiEnv,
//			prCalibrate: prCalibrate[slot]
//		).pairsDo({ |k, v| [k, v].postcs });

		if(slot.notNil, {
			switch(this.class,
				CVWidget2D, { wcm = wdgtControllersAndModels[slot] },
				CVWidgetMS, {
					wcm = wdgtControllersAndModels.slots[slot];
					wcm.cvSpec = ();
					wcm.actions = ();
				}
			);
			midiOscEnv = this.midiOscEnv[slot];
			switch(this.class,
				CVWidget2D, {
					thisWidgetCV = this.widgetCV[slot];
					thisGuiEnv = this.guiEnv[slot];
				},
				CVWidgetMS, {
					thisWidgetCV = this.widgetCV;
					thisGuiEnv = this.guiEnv;
				}
			);
			thisCalib = prCalibrate[slot];
		}, {
			wcm = wdgtControllersAndModels;
			thisGuiEnv = this.guiEnv;
			midiOscEnv = this.midiOscEnv;
			thisWidgetCV = this.widgetCV;
			thisCalib = prCalibrate;
		});

		#[
			prInitCalibration,
			prInitSpecControl,
			prInitMidiConnect,
			prInitMidiDisplay,
			prInitMidiOptions,
			prInitOscConnect,
			prInitOscDisplay,
			prInitOscInputRange,
			prInitActionsControl,
			prInitSlidersTextConnection
		].do({ |method|
			this.perform(method, wcm, thisGuiEnv, midiOscEnv, thisWidgetCV, thisCalib, slot);
		});
	}

	prInitCalibration { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|
		var thisEditor, numCalib;

		wcm.calibration.controller ?? {
			wcm.calibration.controller = SimpleController(wcm.calibration.model);
		};

		wcm.calibration.controller.put(\default, { |theChanger, what, moreArgs|
			// "prInitCalibration: %\n".postf(theChanger.value);
			if(debug, { "widget '%' (%) at slot '%' calibration.model: %\n".postf(this.name, this.class, slot, theChanger) });

			if(this.class == CVWidgetMS, {
				thisEditor = guiEnv[\editor][slot];
			}, {
				thisEditor = guiEnv[\editor];
			});

			// [slot, thisEditor.calibNumBoxes.lo, thisEditor.calibNumBoxes.hi].postln;

			theChanger.value.switch(
				true, {
					if(this.class != CVWidgetMS, {
						parent.isClosed.not.if {
							guiEnv.calibBut.value_(0);
							if(GUI.id !== \cocoa, {
								guiEnv.calibBut.toolTip_("Calibration is active.\nClick to deactivate.");
							})
						};
					}, {
						parent.isClosed.not.if { this.calibViews[slot].background_(Color.green) };
					});
					if(thisEditor.notNil and:{ thisEditor.isClosed.not }, {
						thisEditor.calibBut.value_(0);
						wcm.mapConstrainterLo ?? {
							wcm.mapConstrainterLo = CV([-inf, inf].asSpec, 0.00001);
							wcm.mapConstrainterLo.connect(thisEditor.calibNumBoxes.lo);
						};
						wcm.mapConstrainterHi ?? {
							wcm.mapConstrainterHi = CV([-inf, inf].asSpec, 0.00001);
							wcm.mapConstrainterHi.connect(thisEditor.calibNumBoxes.hi);
						};
						[thisEditor.calibNumBoxes.lo, thisEditor.calibNumBoxes.hi].do({ |nb|
							nb.enabled_(false);
							nb.action_(nil);
						})
					});
					if(this.class == CVWidgetMS, {
						numCalib = msSize.collect(this.getCalibrate(_)).select(_ == true).size;
						// "numCalib on true: %\n".postf(numCalib);
						if(numCalib == msSize, {
							if(guiEnv.msEditor.notNil and:{
								guiEnv.msEditor.isClosed.not
							}, {
								guiEnv.msEditor.calibBut.states_([
									["calibrating all", Color.black, Color.green],
									["calibrate all", Color.white, Color.red]
								]).value_(0);
								guiEnv.msEditor.oscCalibBtns[slot].value_(0);
								if(GUI.id !== \cocoa, {
									guiEnv.msEditor.oscCalibBtns[slot].toolTip_("Calibration is active.\nClick to deactivate.");
								})
							})
						}, {
							if(guiEnv.msEditor.notNil and:{
								guiEnv.msEditor.isClosed.not
							}, {
								guiEnv.msEditor.calibBut.states_([
									["partially calibrating", Color.black, Color.yellow],
									["calibrate all", Color.white, Color.red]
								]).value_(0);
								guiEnv.msEditor.oscCalibBtns[slot].value_(0);
								if(GUI.id !== \cocoa, {
									guiEnv.msEditor.oscCalibBtns[slot].toolTip_("Calibration is active.\nClick to deactivate.");
								})
							})
						})
					})
				},
				false, {
					if(this.class != CVWidgetMS, {
						parent.isClosed.not.if {
							guiEnv.calibBut.value_(1);
							if(GUI.id !== \cocoa, {
								guiEnv.calibBut.toolTip_("Calibration is inactive.\nClick to activate.");
							})
						};
					}, {
						parent.isClosed.not.if { this.calibViews[slot].background_(Color.red) };
					});
					if(thisEditor.notNil and:{ thisEditor.isClosed.not }, {
						thisEditor.calibBut.value_(1);
						[wcm.mapConstrainterLo, wcm.mapConstrainterHi].do({ |cv| cv = nil; });
						[thisEditor.calibNumBoxes.lo, thisEditor.calibNumBoxes.hi].do({ |nb|
							nb.enabled_(true);
							nb.action_({ |b|
								this.setOscInputConstraints(
									thisEditor.calibNumBoxes.lo.value @ thisEditor.calibNumBoxes.hi.value, slot;
								)
							})
						})
					});
					if(this.class == CVWidgetMS, {
						numCalib = msSize.collect(this.getCalibrate(_)).select(_ == true).size;
						if(numCalib == 0, {
							if(guiEnv.msEditor.notNil and:{
								guiEnv.msEditor.isClosed.not
							}, {
								guiEnv.msEditor.calibBut.states_([
									["calibrating all", Color.black, Color.green],
									["calibrate all", Color.white, Color.red]
								]).value_(1);
								guiEnv.msEditor.oscCalibBtns[slot].value_(1);
								if(GUI.id !== \cocoa, {
									guiEnv.msEditor.oscCalibBtns[slot].toolTip_("Calibration is inactive.\nClick to activate.");
								})
							})
						}, {
							if(guiEnv.msEditor.notNil and:{
								guiEnv.msEditor.isClosed.not
							}, {
								guiEnv.msEditor.calibBut.states_([
									["partially calibrating", Color.black, Color.yellow],
									["calibrate all", Color.white, Color.red]
								]).value_(0);
								guiEnv.msEditor.oscCalibBtns[slot].value_(1);
								if(GUI.id !== \cocoa, {
									guiEnv.msEditor.oscCalibBtns[slot].toolTip_("Calibration is inactive.\nClick to activate.");
								})
							})
						})
					})
				}
			)
		})
	}

	prInitSpecControl { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|
		var tmp, tmpMapping;
		var specSize, calibViewsWidth;
		var specEditor, msEditors, oscIndex;
		// var calibViewLeft;
		var thisSpec, customName, thisMidiOscEnv;
		var reference;
		var calibViewsNextX, btnIndex;

		wcm.cvSpec.controller ?? {
			switch(this.class,
				CVWidgetMS, {
					wcm.cvSpec.controller = SimpleController(wdgtControllersAndModels.cvSpec.model);
				},
				{ wcm.cvSpec.controller = SimpleController(wcm.cvSpec.model) }
			)
		};

		wcm.cvSpec.controller.put(\default, { |theChanger, what, moreArgs|
			if(debug, { "widget '%' (%) at slot '%' cvSpec.model: %\n".postf(this.name, this.class, slot, theChanger) });

			switch(this.class,
				CVWidgetMS, {
					specEditor = guiEnv.msEditor;
					msEditors = guiEnv.editor;
					thisMidiOscEnv = this.midiOscEnv;
				},
				{ specEditor = guiEnv.editor }
			);

			if(theChanger.value.safeHasZeroCrossing, {
				// "has zero crossing".postln;
				if(midiOscEnv.oscMapping === \linexp or:{
					midiOscEnv.oscMapping === \expexp
				}, {
					if(this.class == CVWidgetMS, {
						msSize.do({ |sl| this.setOscMapping(\linlin, sl) });
					}, {
						this.setOscMapping(\linlin, slot);
					});

					if(specEditor.notNil and:{
						specEditor.isClosed.not
					}, {
						if(this.class == CVWidgetMS, {
							specEditor.mappingSelect.value_(1);
							msEditors.do({ |it|
								if(it.notNil and:{ it.isClosed.not }, {
									it.mappingSelect.value_(0)
								})
							})
						}, {
							specEditor.mappingSelect.value_(0);
						})
					})
				})
			}, {
				if(specEditor.notNil and:{
					specEditor.isClosed.not
				}, {
					tmpMapping = specEditor.mappingSelect.item;
					specEditor.mappingSelect.items.do({ |item, i|
						if(item == tmpMapping, {
							specEditor.mappingSelect.value_(i)
						})
					});
				})
			});

			// if(specEditor.notNil and:{
			// 	specEditor.isClosed.not
			// 	}, {
			if(this.class == CVWidgetMS, {
				if([
					theChanger.value.minval,
					theChanger.value.maxval,
					theChanger.value.warp,
					theChanger.value.step,
					theChanger.value.default
				].select(_.isArray).size == 0, {
					thisSpec = ControlSpec(
						theChanger.value.minval!msSize,
						theChanger.value.maxval!msSize,
						theChanger.value.warp,
						theChanger.value.step!msSize,
						theChanger.value.default!msSize,
						theChanger.value.units,
					)
				}, {
					thisSpec = ControlSpec(
						theChanger.value.minval.asArray,
						theChanger.value.maxval.asArray,
						theChanger.value.warp,
						theChanger.value.step.asArray,
						theChanger.value.default.asArray,
						theChanger.value.units
					)
				});

						// "do I get here?".postln;

				specSize = [
					thisSpec.minval.size,
					thisSpec.maxval.size,
					thisSpec.step.size,
					thisSpec.default.size
				].maxItem;

				if(specSize < msSize, {
					this.mSlider.indexThumbSize_(this.mSlider.bounds.width/specSize);
					(msSize-1..specSize).do({ |sl|
						this.oscDisconnect(sl);
						this.midiDisconnect(sl);
						if(specEditor.notNil and:{
							specEditor.isClosed.not
						}, {
							if(sl == specSize, {
								specEditor.oscFlow1.left_(
									specEditor.oscEditBtns[sl].bounds.left
								).top_(specEditor.oscEditBtns[sl].bounds.top);
								specEditor.midiFlow1.left_(
									specEditor.midiEditGroups[sl].bounds.left
								).top_(specEditor.midiEditGroups[sl].bounds.top);
							});
							specEditor.oscEditBtns[sl].remove;
							specEditor.oscCalibBtns[sl].remove;
							specEditor.midiEditGroups[sl].remove;
							specEditor.oscEditBtns.removeAt(sl);
							specEditor.oscCalibBtns.removeAt(sl);
							specEditor.midiEditGroups.removeAt(sl);
						});

						// "msEditors: %\n".postf(msEditors);

						if(msEditors[sl].notNil and:{
							msEditors[sl].isClosed.not
						}, {
							msEditors[sl].close;
						});
						msEditors.removeAt(sl);

						if(parent.notNil and:{ parent.isClosed.not }, {
							this.calibViews[sl].remove;
							this.calibViews.removeAt(sl);
							calibViewsWidth = this.mSlider.bounds.width/specSize;
							this.calibViews.do({ |cv, i|
								if(i == 0, {
									calibViewsNextX = cv.bounds.left
								}, {
									calibViewsNextX = calibViewsNextX+calibViewsWidth
								});
								cv.bounds_(Rect(calibViewsNextX, cv.bounds.top, calibViewsWidth, cv.bounds.height));
							})
						})
					});

					(msSize-1..specSize).do({ |sl|
						this.midiOscEnv.removeAt(sl);
						wdgtControllersAndModels.slots.removeAt(sl);
						[prMidiMode, prMidiMean, prMidiResolution, prSoftWithin, prCtrlButtonBank, prCalibrate].do(_.removeAt(sl));
					})
				});

				if(specSize > msSize, {
					this.mSlider.indexThumbSize_(this.mSlider.bounds.width/specSize);
					calibViewsWidth = this.mSlider.bounds.width/specSize;
					this.calibViews.do({ |cv, i|
						if(i == 0, { calibViewsNextX = cv.bounds.left });
						cv.bounds_(Rect(calibViewsNextX, cv.bounds.top, calibViewsWidth, cv.bounds.height));
						calibViewsNextX = calibViewsNextX+calibViewsWidth;
					});
					btnIndex = (msSize..specSize-1);
					(specSize-msSize).do({ |i|
						this.calibViews.add(
							CompositeView(
								parent, Rect(calibViewsNextX, this.calibViews[0].bounds.top, calibViewsWidth, 2)
							).background_(Color.green)
						);
						calibViewsNextX = calibViewsNextX+calibViewsWidth;
						msEditors = msEditors.add(nil);
						thisMidiOscEnv = thisMidiOscEnv.add((oscMapping: \linlin));
						wdgtControllersAndModels.slots = wdgtControllersAndModels.slots.add(nil);
						this.initControllersAndModels(slot: msSize+i);
						prMidiMode = prMidiMode.add(this.getMidiMode(msSize-1));
						prMidiMean = prMidiMean.add(this.getMidiMean(msSize-1));
						prMidiResolution = prMidiResolution.add(this.getMidiResolution(msSize-1));
						prSoftWithin = prSoftWithin.add(this.getSoftWithin(msSize-1));
						prCtrlButtonBank = prCtrlButtonBank.add(this.getCtrlButtonBank(msSize-1));
						prCalibrate = prCalibrate.add(true);
						if(specEditor.notNil and:{ specEditor.isClosed.not }, {
							specEditor.midiEditGroups.add(
								CVMidiEditGroup(specEditor.midiTabs.views[1], specEditor.midiFlow1.bounds.width/5-10@39, this, msSize+i);
							);
							oscIndex = msSize+i;
							specEditor.oscEditBtns.add(
								Button(specEditor.oscTabs.views[1], specEditor.oscFlow1.bounds.width/5-10@25)
									.states_([
										[oscIndex.asString++": edit OSC", Color.black, Color.white(0.2)]
									])
									.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9.4))
									.action_({ |bt|
										// (btnIndex[i]).postln;
										if(msEditors[btnIndex[i]].isNil or:{
											msEditors[btnIndex[i]].isClosed
										}, {
											msEditors[btnIndex[i]] = CVWidgetEditor(
												this, this.name, 1, btnIndex[i]
											);
										}, {
											msEditors[btnIndex[i]].front(1)
										});
										msEditors[btnIndex[i]].calibNumBoxes !? {
											wdgtControllersAndModels.slots[btnIndex[i]].mapConstrainterLo.connect(
												msEditors[btnIndex[i]].calibNumBoxes.lo;
											);
											msEditors[btnIndex[i]].calibNumBoxes.lo.value_(
												wdgtControllersAndModels.slots[btnIndex[i]].oscInputRange.model.value[0];
											);
											wdgtControllersAndModels.slots[btnIndex[i]].mapConstrainterHi.connect(
												msEditors[btnIndex[i]].calibNumBoxes.hi;
											);
											msEditors[btnIndex[i]].calibNumBoxes.hi.value_(
												wdgtControllersAndModels.slots[btnIndex[i]].oscInputRange.model.value[1];
											)
										};
										wdgtControllersAndModels.slots[btnIndex[i]].oscDisplay.model.value_(
											wdgtControllersAndModels.slots[btnIndex[i]].oscDisplay.model.value;
										).changedKeys(this.synchKeys);
										wdgtControllersAndModels.slots[btnIndex[i]].midiDisplay.model.value_(
											wdgtControllersAndModels.slots[btnIndex[i]].midiDisplay.model.value
										).changedKeys(this.synchKeys);
									})
								;
							);

							specEditor.oscFlow1.shift(-13, specEditor.oscEditBtns[oscIndex].bounds.height-10);

							specEditor.oscCalibBtns.add(
								Button(specEditor.oscTabs.views[1], 10@10)
									.states_([
										["", Color.black, Color.green],
										["", Color.white, Color.red]
									])
									.action_({ |cb|
										cb.value.switch(
											0, {
											this.setCalibrate(true, btnIndex[i]);
												wdgtControllersAndModels.slots[btnIndex[i]].calibration.model.value_(true).changedKeys(this.synchKeys);
											},
											1, {
											this.setCalibrate(false, btnIndex[i]);
												wdgtControllersAndModels.slots[btnIndex[i]].calibration.model.value_(false).changedKeys(this.synchKeys);
											}
										)
									})
								;
							);

							specEditor.oscFlow1.shift(0, (specEditor.oscEditBtns[oscIndex].bounds.height-10).neg);

							this.initControllerActions(msSize+i);
						})
					});

				});

				if(specSize != msSize, {
					if(parent.notNil and:{ parent.isClosed.not }, {
						this.oscBut.states_([
							[
								"OSC ("++this.midiOscEnv.select({ |it| it.oscResponder.notNil }).size++"/"++specSize++")",
								this.oscBut.states[0][1], this.oscBut.states[0][2]
							]
						]);
						this.midiBut.states_([
							[
								"MIDI ("++this.midiOscEnv.select({ |it| it.cc.notNil }).size++"/"++specSize++")",
								this.midiBut.states[0][1], this.midiBut.states[0][2]
							]
						]);
					});

					if(specEditor.notNil and:{ specEditor.isClosed.not }, {
						specEditor.specsListSpecs.do({ |spec, i|
							if((tmp = [spec.minval, spec.maxval, spec.step, spec.default].select(_.isArray)).size > 0, {
								if(tmp.collect(_.size).includes(specSize).not, {
									// "spec not matching: %\n".postf([i, spec]);
									specEditor.specsListSpecs.removeAt(i);
									specEditor.specsList.items.removeAt(i);
								})
							})
						});
						specEditor.extMidiCtrlArrayField.string_("(0.."++(specSize-1)++")");
						specEditor.extOscCtrlArrayField.string_("(1.."++specSize++")");
					})
				});

				msSize = specSize;
				// "msSize: %\n".postf(msSize);

				if(Spec.findKeyForSpec(theChanger.value).notNil, {
					customName = Spec.findKeyForSpec(theChanger.value).asString++"_"++specSize;
				}, {
					customName = "custom_"++specSize;
				})
			}, {
				thisSpec = theChanger.value;
			});

			if(specEditor.notNil and:{ specEditor.isClosed.not }, {
				specEditor.specField.string_(thisSpec.asCompileString);
				tmp = specEditor.specsListSpecs.detectIndex({ |item, i| item == thisSpec });
				if(tmp.notNil, {
					specEditor.specsList.value_(tmp);
				}, {
					customName ?? { customName = "custom" };
					specEditor.specsList.items = List[customName++":"+(thisSpec.asString)]++specEditor.specsList.items;
					Spec.add(customName.asSymbol, thisSpec);
					specEditor.specsListSpecs.array_([thisSpec]++specEditor.specsListSpecs.array);
					specEditor.specsList.value_(0);
					specEditor.specsList.refresh;
				})
			});

			// "argWidgetCV: %\nthisSpec: %\n".postf(argWidgetCV, thisSpec);
			argWidgetCV.spec_(thisSpec);

			if(this.class == CVWidgetMS, {
				reference = [];
				msSize.do({ |sl|
					tmp = ControlSpec(thisSpec.minval.wrapAt(sl), thisSpec.maxval.wrapAt(sl));
					if(tmp.excludingZeroCrossing, {
						if(tmp.minval < tmp.maxval, { reference = reference.add(tmp.minval.abs/(tmp.maxval-tmp.minval)) });
						if(tmp.minval > tmp.maxval, { reference = reference.add(tmp.maxval.abs/(tmp.maxval-tmp.minval).abs) });
					});
					if(tmp.minval.isNegative and:{ tmp.maxval.isNegative }, { reference = reference.add(1) });
					if(tmp.minval.isPositive and:{ tmp.maxval.isPositive }, { reference = reference.add(0) });
				});
				// "reference: %\n".postf(reference);
				this.mSlider.reference_(reference);
			});


			if(GUI.id !== \cocoa, {
				if(this.specBut.class == Event, {
					this.specBut[slot].toolTip_(
						"Edit the CV's ControlSpec in '"++slot++"':\n"++(this.getSpec(slot).asCompileString)
					)
				}, {
					this.specBut.toolTip_(
						"Edit the CV's ControlSpec:\n"++(this.getSpec.asCompileString)
					)
				})
			});

			if(this.class === CVWidgetKnob, {
				if(argWidgetCV.spec.excludingZeroCrossing, {
					guiEnv.knob.centered_(true);
				}, {
					guiEnv.knob.centered_(false);
				})
			});

			msEditors !? {
				msEditors.do({ |ed|
					if(ed.notNil and:{ ed.isClosed.not }, {
						ed.specConstraintsText.string_(
							" current widget-spec constraints lo / hi:"+this.getSpec.minval.wrapAt(slot)+"/"+this.getSpec.maxval.wrapAt(slot)
						)
					})
				})
			};
			if(this.class != CVWidgetMS, {
				if(specEditor.notNil and:{ specEditor.isClosed.not }, {
					specEditor.specConstraintsText.string_(
						" current widget-spec constraints lo / hi:"+this.getSpec(slot).minval+"/"+this.getSpec(slot).maxval
					)
				})
			})
		})
	}

	prInitMidiConnect { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|
		var ctrlString, meanVal, ccResponderAction, makeCCResponder;

		wcm.midiConnection.controller ?? {
			wcm.midiConnection.controller = SimpleController(wcm.midiConnection.model);
		};

		wcm.midiConnection.controller.put(\default, { |theChanger, what, moreArgs|
			if(debug, { "widget '%' (%) at slot '%' midiConnection.model: %\n".postf(this.name, this.class, slot, theChanger) });

			// "midiConnection.model: %\n".postf(theChanger);

			if(theChanger.value.isKindOf(Event), {
				ccResponderAction = { |src, chan, num, val|
					ctrlString ? ctrlString = num+1;
					if(this.getCtrlButtonBank(slot).notNil, {
						if(ctrlString % this.getCtrlButtonBank(slot) == 0, {
							ctrlString = this.getCtrlButtonBank(slot).asString;
						}, {
							ctrlString = (ctrlString % this.getCtrlButtonBank(slot)).asString;
						});
						ctrlString = ((num+1/this.getCtrlButtonBank(slot)).ceil).asString++":"++ctrlString;
					}, {
						ctrlString = num+1;
					});

					this.getMidiMode(slot).switch(
						0, {
							switch(this.class,
								CVWidgetMS, {
									if(val/127 < (argWidgetCV.input[slot]+(this.getSoftWithin(slot)/2)) and:{
										val/127 > (argWidgetCV.input[slot]-(this.getSoftWithin(slot)/2));
									}, {
										argWidgetCV.input_(argWidgetCV.input.collect({ |it, i|
											if(i == slot, { val/127 }, { it })
										}))
									})
								},
								{
									if(val/127 < (argWidgetCV.input+(this.getSoftWithin(slot)/2)) and:{
										val/127 > (argWidgetCV.input-(this.getSoftWithin(slot)/2));
									}, {
										argWidgetCV.input_(val/127)
									})
								}
							)
						},
						1, {
							meanVal = this.getMidiMean(slot);
							switch(this.class,
								CVWidgetMS, {
									argWidgetCV.input_(
										argWidgetCV.input.collect({ |it, i|
											if(i == slot, {
												argWidgetCV.input[slot]+(
													(val-meanVal)/127*this.getMidiResolution(slot)
												)
											}, { it })
										})
									)
								},
								{
									argWidgetCV.input_(
										argWidgetCV.input+((val-meanVal)/127*this.getMidiResolution(slot))
									)
								}
							)
						}
					);

					src !? { midiOscEnv.midisrc = src };
					chan !? { midiOscEnv.midichan = chan };
					num !? { midiOscEnv.midinum = ctrlString; midiOscEnv.midiRawNum = num };
				};
				makeCCResponder = { |argSrc, argChan, argNum|
					if(midiOscEnv.cc.isNil, {
						CCResponder(ccResponderAction, argSrc, argChan, argNum);
					}, {
						midiOscEnv.cc.function_(ccResponderAction);
					})
				};

				{
					block { |break|
						loop {
							0.01.wait;
							if(midiOscEnv.midisrc.notNil and:{
								midiOscEnv.midichan.notNil and:{
									midiOscEnv.midinum.notNil;
								}
							}, {
								break.value(
									wcm.midiDisplay.model.value_(
										(
											src: midiOscEnv.midisrc,
											chan: midiOscEnv.midichan,
											ctrl: midiOscEnv.midinum,
											learn: "X"
										)
									).changedKeys(synchKeys)
								)
							})
						}
					}
				}.fork(AppClock);

				if(theChanger.value.isEmpty, {
					midiOscEnv.cc = makeCCResponder.().learn;
				}, {
					midiOscEnv.cc = makeCCResponder.(theChanger.value.src, theChanger.value.chan, theChanger.value.num);
				});
			}, {
				midiOscEnv.cc.remove;
				midiOscEnv.cc = nil;
				wcm.midiDisplay.model.value_(
					(src: "source", chan: "chan", ctrl: "ctrl", learn: "L")
				).changedKeys(synchKeys);
				midiOscEnv.midisrc = nil;
				midiOscEnv.midichan = nil;
				midiOscEnv.midinum = nil;
				midiOscEnv.midiRawNum = nil;
			})
		})
	}

	prInitMidiDisplay { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|
		var ctrlToolTip,typeText, r, p, sourceNames;
		var midiInitFunc, thisEditor;
		// CVWidgetMS
		var numMidiResponders, numMidiString, midiButBg, midiButTextColor;
		var tmp;

		midiInitFunc = { |val|
			if(val.editor.notNil and:{ val.editor.isClosed.not }, {
				if(MIDIClient.initialized, {
					val.editor.midiInitBut.states_([
						["restart MIDI", Color.black, Color.green]
					])
				}, {
					val.editor.midiInitBut.states_([
						["init MIDI", Color.white, Color.red]
					])
				});
				sourceNames = midiSources.keys.asArray.sort;
				val.editor.midiSourceSelect.items_(
					[val.editor.midiSourceSelect.items[0]]++sourceNames
				);
			})
		};

		wcm.midiDisplay.controller ?? {
			wcm.midiDisplay.controller = SimpleController(wcm.midiDisplay.model);
		};

		wcm.midiDisplay.controller.put(\default, { |theChanger, what, moreArgs|
			if(debug, { "widget '%' (%) at slot '%' midiDisplay.model: %\n".postf(this.name, this.class, slot, theChanger) });

			if(this.class == CVWidgetMS, {
				thisEditor = guiEnv.editor[slot];
			}, {
				thisEditor = guiEnv.editor;
			});

			MIDIClient.sources.do({ |source|
				if(midiSources.values.includes(source.uid.asInteger).not, {
					// OSX/Linux specific tweek
					if(source.name == source.device, {
						midiSources.put(source.name.asSymbol, source.uid.asInteger)
					}, {
						midiSources.put(
							(source.device++":"+source.name).asSymbol, source.uid.asInteger
						)
					})
				})
			});

			AbstractCVWidgetEditor.allEditors.pairsDo({ |k, v|
				// "widget: % editor: %\n".postf(k, v);
				if(v.keys.includes(\editor), {
					// [v.name, v.editor].postln;
					midiInitFunc.(v);
				}, {
					v.pairsDo({ |vk, vv|
						// [vv.name, vv.editor].postln;
						midiInitFunc.(vv)
					})
				})
			});


			if(thisEditor.notNil and:{ thisEditor.isClosed.not }, {
				if(midiSources.values.includes(theChanger.value.src), {
					thisEditor.midiSourceSelect.value_(
						thisEditor.midiSourceSelect.items.indexOfEqual(
							midiSources.findKeyForValue(theChanger.value.src)
						)
					)
				})
			});

			theChanger.value.learn.switch(
				"X", {
					if(this.class != CVWidgetMS, {
						defer {
							if(parent.isClosed.not, {
								guiEnv.midiSrc.string_(theChanger.value.src.asString)
									.background_(Color.red)
									.stringColor_(Color.white)
									.canFocus_(false)
								;
								guiEnv.midiChan.string_((theChanger.value.chan+1).asString)
									.background_(Color.red)
									.stringColor_(Color.white)
									.canFocus_(false)
								;
								guiEnv.midiCtrl.string_(theChanger.value.ctrl)
									.background_(Color.red)
									.stringColor_(Color.white)
									.canFocus_(false)
								;
								if(slot.notNil, { typeText = " at '"++slot++"'" }, { typeText = "" });
								guiEnv.midiLearn.value_(1);
								if(GUI.id !== \cocoa, {
									guiEnv.midiLearn.toolTip_("Click to remove the current\nMIDI-responder in this widget %.".format(typeText));
									[guiEnv.midiSrc, guiEnv.midiChan, guiEnv.midiCtrl].do({ |elem|
										if(theChanger.value.ctrl.class == String and:{
											theChanger.value.ctrl.includes($:)
										}, {
											ctrlToolTip = theChanger.value.ctrl.split($:);
											ctrlToolTip = ctrlToolTip[1]++" in bank "++ctrlToolTip[0];
										}, { ctrlToolTip = theChanger.value.ctrl });
										elem.toolTip_(
											"currently connected to\ndevice-ID %,\non channel %,\ncontroller %".format(theChanger.value.src.asString, (theChanger.value.chan+1).asString, ctrlToolTip)
										)
									})
								})
							})
						}
					});

					if(thisEditor.notNil and:{
						thisEditor.isClosed.not
					}, {
						thisEditor.midiSrcField
							.string_(theChanger.value.src.asString)
							.background_(Color.red)
							.stringColor_(Color.white)
							.canFocus_(false)
						;
						thisEditor.midiChanField
							.string_((theChanger.value.chan+1).asString)
							.background_(Color.red)
							.stringColor_(Color.white)
							.canFocus_(false)
						;
						thisEditor.midiCtrlField
							.string_(theChanger.value.ctrl)
							.background_(Color.red)
							.stringColor_(Color.white)
							.canFocus_(false)
						;
						thisEditor.midiLearnBut.value_(1);
						thisEditor.midiSourceSelect.enabled_(false);
					});

					if(this.class == CVWidgetMS, {
						if(guiEnv.msEditor.notNil and:{
							guiEnv.msEditor.isClosed.not
						}, {
							guiEnv.msEditor.midiEditGroups[slot].midiSrc.string_(
								theChanger.value.src.asString
							);
							guiEnv.msEditor.midiEditGroups[slot].midiChan.string_(
								(theChanger.value.chan+1).asString
							);
							guiEnv.msEditor.midiEditGroups[slot].midiCtrl.string_(
								theChanger.value.ctrl
							);
							#[midiSrc, midiChan, midiCtrl].do({ |el|
								guiEnv.msEditor.midiEditGroups[slot].perform(el)
									.background_(Color.red)
									.stringColor_(Color.white)
									.canFocus_(false)
								;
								if(theChanger.value.ctrl.class == String and:{
									theChanger.value.ctrl.includes($:)
								}, {
									ctrlToolTip = theChanger.value.ctrl.split($:);
									ctrlToolTip = ctrlToolTip[1]++" in bank "++ctrlToolTip[0];
								}, { ctrlToolTip = theChanger.value.ctrl });
								if(GUI.id !== \cocoa, {
									guiEnv.msEditor.midiEditGroups[slot].perform(el).toolTip_(
										"currently connected to\ndevice-ID %,\non channel %,\ncontroller %".format(theChanger.value.src.asString, (theChanger.value.chan+1).asString, ctrlToolTip)
									)
								})
							});
							guiEnv.msEditor.midiEditGroups[slot].midiLearn.value_(1);
							if(GUI.id !== \cocoa, {
								[
									guiEnv.msEditor.midiConnectorBut,
									guiEnv.msEditor.midiDisconnectorBut
								].do({ |b|
									if(b.enabled, {
										b.toolTip_(
											"Currently connected to external MIDI-controllers: %".format(
												this.midiOscEnv.selectIndex({ |sl| sl.cc.notNil })
											)
										)
									})
								})
							})
						})
					})
				},
				"C", {
					if(this.class != CVWidgetMS, {
						defer {
							if(parent.isClosed.not, {
								guiEnv.midiLearn.states_([
									["C", Color.white, Color(0.11, 0.38, 0.2)],
									["X", Color.white, Color.red]
								]).refresh;
								if(slot.notNil, { typeText = " at '"++slot++"' " }, { typeText = " " });
								guiEnv.midiLearn.value_(0);
								if(GUI.id !== \cocoa, {
									guiEnv.midiLearn.toolTip_("Click to connect the widget% to\nthe slider(s) as given in the fields below.".format(typeText));
								});
								r = [
									guiEnv.midiSrc.string != "source" and:{
										try{ guiEnv.midiSrc.string.interpret.isInteger }
									},
									guiEnv.midiChan.string != "chan" and:{
										try{ guiEnv.midiChan.string.interpret.isInteger }
									},
									guiEnv.midiCtrl.string != "ctrl"
								].collect({ |r| r });

								if(GUI.id !== \cocoa, {
									p = "Use ";
									if(r[0], { p = p++" MIDI-device ID "++theChanger.value.src++",\n" });
									if(r[1], { p = p++"channel nr. "++theChanger.value.chan++",\n" });
									if(r[2], { p = p++"controller nr. "++theChanger.value.ctrl });
									p = p++"\nto connect widget%to MIDI";

									[guiEnv.midiSrc, guiEnv.midiChan, guiEnv.midiCtrl].do(
										_.toolTip_(p.format(slot !? { " at '"++slot++"' " } ?? { " " }))
									)
								})
							})
						}
					});

					if(thisEditor.notNil and:{
						thisEditor.isClosed.not
					}, {
						thisEditor.midiLearnBut
							.states_([
								["C", Color.white, Color(0.11, 0.38, 0.2)],
								["X", Color.white, Color.red]
							])
							.value_(0)
						;
						thisEditor.midiSrcField
							.string_(theChanger.value.src)
							.background_(Color.white)
							.stringColor_(Color.black)
							.canFocus_(true)
						;
						thisEditor.midiChanField
							.string_(theChanger.value.chan)
							.background_(Color.white)
							.stringColor_(Color.black)
							.canFocus_(true)
						;
						thisEditor.midiCtrlField
							.string_(theChanger.value.ctrl)
							.background_(Color.white)
							.stringColor_(Color.black)
							.canFocus_(true)
						;
						thisEditor.midiSourceSelect.enabled_(true);
					});

					if(this.class == CVWidgetMS, {
						if(guiEnv.msEditor.notNil and:{
							guiEnv.msEditor.isClosed.not
						}, {
							guiEnv.msEditor.midiEditGroups[slot].midiLearn
								.states_([
									["C", Color.white, Color(0.11, 0.38, 0.2)],
									["X", Color.white, Color.red]
								])
								.value_(0)
							;
							guiEnv.msEditor.midiEditGroups[slot].midiSrc.string_(
								theChanger.value.src
							);
							guiEnv.msEditor.midiEditGroups[slot].midiChan.string_(
								theChanger.value.chan
							);
							guiEnv.msEditor.midiEditGroups[slot].midiCtrl.string_(
								theChanger.value.ctrl
							);

							r = [
								guiEnv.msEditor.midiEditGroups[slot].midiSrc.string != "source" and:{
									try{ guiEnv.msEditor.midiEditGroups[slot].midiSrc.string.interpret.isInteger }
								},
								guiEnv.msEditor.midiEditGroups[slot].midiChan.string != "chan" and:{
									try{ guiEnv.msEditor.midiEditGroups[slot].midiChan.string.interpret.isInteger }
								},
								guiEnv.msEditor.midiEditGroups[slot].midiCtrl.string != "ctrl"
							].collect({ |r| r });

							if(GUI.id !== \cocoa, {
								p = "Use ";
								if(r[0], { p = p++" MIDI-device ID "++theChanger.value.src++",\n" });
								if(r[1], { p = p++"channel nr. "++theChanger.value.chan++",\n" });
								if(r[2], { p = p++"controller nr. "++theChanger.value.ctrl });
								p = p++"\nto connect widget%to MIDI";
								[
									guiEnv.msEditor.midiEditGroups[slot].midiSrc,
									guiEnv.msEditor.midiEditGroups[slot].midiChan,
									guiEnv.msEditor.midiEditGroups[slot].midiCtrl
								].do(
									_.toolTip_(p.format(slot !? { " at '"++slot++"' " } ?? { " " }))
								)
							})
						})
					})
				},
				"L", {
					if(this.class != CVWidgetMS, {
						defer {
							if(parent.isClosed.not, {
								guiEnv.midiSrc
									.string_(theChanger.value.src)
									.background_(Color.white)
									.stringColor_(Color.black)
									.canFocus_(true)
								;
								guiEnv.midiChan
									.string_(theChanger.value.chan)
									.background_(Color.white)
									.stringColor_(Color.black)
									.canFocus_(true)
								;
								guiEnv.midiCtrl
									.string_(theChanger.value.ctrl)
									.background_(Color.white)
									.stringColor_(Color.black)
									.canFocus_(true)
								;
								guiEnv.midiLearn.states_([
									["L", Color.white, Color.blue],
									["X", Color.white, Color.red]
								])
								.value_(0).refresh;
								if(GUI.id !== \cocoa, {
									if(slot.notNil, { typeText = " at '"++slot++"' " }, { typeText = " " });
									guiEnv.midiLearn.toolTip_("Click and and move an arbitrary\nslider on your MIDI-device to\nconnect the widget%to that slider.".format(typeText));
									guiEnv.midiSrc.toolTip_("Enter your MIDI-device's ID,\nhit 'return' and click 'C' to\nconnect all sliders of your\ndevice to this widget%".format(typeText));
									guiEnv.midiChan.toolTip_("Enter a MIDI-channel, hit 'return'\nand click 'C' to connect all sliders\nin that channel to this widget%".format(typeText));
									guiEnv.midiCtrl.toolTip_("Enter a MIDI-ctrl-nr., hit 'return'\nand click 'C' to connect the slider\nwith that number to this widget%".format(typeText));
								})
							})
						}
					});

					if(thisEditor.notNil and:{
						thisEditor.isClosed.not
					}, {
						thisEditor.midiSrcField.string_(theChanger.value.src)
							.background_(Color.white)
							.stringColor_(Color.black)
							.canFocus_(true)
						;
						thisEditor.midiChanField.string_(theChanger.value.chan)
							.background_(Color.white)
							.stringColor_(Color.black)
							.canFocus_(true)
						;
						thisEditor.midiCtrlField.string_(theChanger.value.ctrl)
							.background_(Color.white)
							.stringColor_(Color.black)
							.canFocus_(true)
						;
						thisEditor.midiLearnBut.states_([
							["L", Color.white, Color.blue],
							["X", Color.white, Color.red]
						])
						.value_(0);
						thisEditor.midiSourceSelect.enabled_(true);
					});

					if(this.class == CVWidgetMS, {
						if(guiEnv.msEditor.notNil and:{
							guiEnv.msEditor.isClosed.not
						}, {
							guiEnv.msEditor.midiEditGroups[slot].midiSrc.string_(
								theChanger.value.src
							);
							guiEnv.msEditor.midiEditGroups[slot].midiChan.string_(
								theChanger.value.chan
							);
							guiEnv.msEditor.midiEditGroups[slot].midiCtrl.string_(
								theChanger.value.ctrl
							);
							#[midiSrc, midiChan, midiCtrl].do({ |el|
								guiEnv.msEditor.midiEditGroups[slot].perform(el)
									.background_(Color.white)
									.stringColor_(Color.black)
									.canFocus_(true)
								;
							});
							guiEnv.msEditor.midiEditGroups[slot].midiLearn
								.states_([
									["L", Color.white, Color.blue],
									["X", Color.white, Color.red]
								])
								.value_(0)
							;
							if(GUI.id !== \cocoa, {
								if(slot.notNil, { typeText = " at '"++slot++"' " }, { typeText = " " });
								guiEnv.msEditor.midiEditGroups[slot].midiLearn.toolTip_("Click and and move an arbitrary\nslider on your MIDI-device to\nconnect the widget%to that slider.".format(typeText));
								guiEnv.msEditor.midiEditGroups[slot].midiSrc.toolTip_("Enter your MIDI-device's ID,\nhit 'return' and click 'C' to\nconnect all sliders of your\ndevice to this widget%".format(typeText));
								guiEnv.msEditor.midiEditGroups[slot].midiChan.toolTip_("Enter a MIDI-channel, hit 'return'\nand click 'C' to connect all sliders\nin that channel to this widget%".format(typeText));
								guiEnv.msEditor.midiEditGroups[slot].midiCtrl.toolTip_("Enter a MIDI-ctrl-nr., hit 'return'\nand click 'C' to connect the slider\nwith that number to this widget%".format(typeText));
							});
							if(GUI.id !== \cocoa, {
								[
									guiEnv.msEditor.midiConnectorBut,
									guiEnv.msEditor.midiDisconnectorBut
								].do({ |b|
									if(b.enabled, {
										b.toolTip_(
											"Currently connected to external MIDI-controllers: %".format(
												if((tmp = this.midiOscEnv.selectIndex({ |sl| sl.cc.notNil })).size > 0, { tmp }, { "none" })
											)
										)
									})
								})
							})
						})
					})
				}
			);

			if(this.class == CVWidgetMS, {
				if(parent.notNil and:{ parent.isClosed.not }, {
					numMidiResponders = this.midiOscEnv.select({ |it| it.cc.notNil }).size;
					numMidiString = "MIDI ("++numMidiResponders++"/"++msSize++")";
					if(numMidiResponders > 0, {
						midiButBg = Color.red;
						midiButTextColor = Color.white;
					}, {
						midiButBg = background;
						midiButTextColor = stringColor;
					});
					this.guiEnv[\midiBut].states_([[
						numMidiString,
						midiButTextColor, // text
						midiButBg // background
					]]);
					if((tmp = this.midiOscEnv.selectIndex({ |sl| sl.cc.notNil })).size > 0, {
						if(GUI.id !== \cocoa, {
							this.midiBut.toolTip_(
								"Currently connected to external MIDI-controllers: %".format(tmp)
							)
						})
					}, {
						if(GUI.id !== \cocoa, {
							this.midiBut.toolTip_(
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
								)
							)
						})
					})
				});
				if(guiEnv.msEditor.notNil and:{
					guiEnv.msEditor.isClosed.not
				}, {
						// midiOscEnv.postln;
					if(this.midiOscEnv.collect({ |it| it[\cc] }).takeThese(_.isNil).size < msSize, {
						guiEnv.msEditor.midiConnectorBut.enabled_(true).states_([
							[guiEnv.msEditor.midiConnectorBut.states[0][0], guiEnv.msEditor.midiConnectorBut.states[0][1], Color.red]
						]);
						[
							guiEnv.msEditor.midiSourceSelect,
							guiEnv.msEditor.midiSrcField,
							guiEnv.msEditor.midiChanField,
							guiEnv.msEditor.extMidiCtrlArrayField
						].do(_.enabled_(true));
					}, {
						guiEnv.msEditor.midiConnectorBut.enabled_(false).states_([
							[guiEnv.msEditor.midiConnectorBut.states[0][0], guiEnv.msEditor.midiConnectorBut.states[0][1], Color.red(alpha: 0.5)]
						]);
						[
							guiEnv.msEditor.midiSourceSelect,
							guiEnv.msEditor.midiSrcField,
							guiEnv.msEditor.midiChanField,
							guiEnv.msEditor.extMidiCtrlArrayField
						].do(_.enabled_(false));
					});
					if(this.midiOscEnv.collect({ |it| it[\cc] }).takeThese(_.isNil).size > 0, {
						guiEnv.msEditor.midiDisconnectorBut.enabled_(true).states_([
							[guiEnv.msEditor.midiDisconnectorBut.states[0][0], guiEnv.msEditor.midiDisconnectorBut.states[0][1], Color.blue]
						])
					}, { guiEnv.msEditor.midiDisconnectorBut.enabled_(false).states_([
						[guiEnv.msEditor.midiDisconnectorBut.states[0][0], guiEnv.msEditor.midiDisconnectorBut.states[0][1], Color.blue(alpha: 0.5)]
					]) })
				})
			})
		})

	}

	prInitMidiOptions { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|
		var thisEditor, typeText, tmp;

		wcm.midiOptions.controller ?? {
			wcm.midiOptions.controller = SimpleController(wcm.midiOptions.model);
		};

		wcm.midiOptions.controller.put(\default, { |theChanger, what, moreArgs|
			if(debug, { "widget '%' (%) at slot '%' midiOptions.model: %\n".postf(this.name, this.class, slot, theChanger) });

			// "guiEnv: %\n".postf(guiEnv);
			switch(this.class,
				CVWidgetMS, {
					thisEditor = guiEnv.editor[slot];
				},
				{ thisEditor = guiEnv.editor }
			);

			if(thisEditor.notNil and:{
				thisEditor.isClosed.not
			}, {
				thisEditor.midiModeSelect.value_(theChanger.value.midiMode);
				thisEditor.midiMeanNB.value_(theChanger.value.midiMean);
				thisEditor.softWithinNB.value_(theChanger.value.softWithin);
				thisEditor.midiResolutionNB.value_(theChanger.value.midiResolution);
				thisEditor.ctrlButtonBankField.string_(theChanger.value.ctrlButtonBank);
			});

				// guiEnv.postln;
			if(this.class != CVWidgetMS, {
				if(parent.notNil and:{ parent.isClosed.not }, {
					if(slot.notNil, { typeText = "'s '"++slot++"' slot" }, { typeText = "" });
					if(GUI.id !== \cocoa, {
						guiEnv.midiHead.toolTip_(("Edit all MIDI-options\nof this widget%.\nmidiMode:"+theChanger.value.midiMode++"\nmidiMean:"+theChanger.value.midiMean++"\nmidiResolution:"+theChanger.value.midiResolution++"\nsoftWithin:"+theChanger.value.softWithin++"\nctrlButtonBank:"+theChanger.value.ctrlButtonBank).format(typeText));
					})
				})
			}, {
				// guiEnv.msEditor.postln;
				if(guiEnv.msEditor.notNil and:{ guiEnv.msEditor.isClosed.not }, {
					// [prMidiMode[slot], this.getMidiMode(slot)].postln;
					(
						midiModeSelect: prMidiMode,
						midiMeanNB: prMidiMean,
						midiResolutionNB: prMidiResolution,
						softWithinNB: prSoftWithin,
						ctrlButtonBankField: prCtrlButtonBank
					).pairsDo({ |field, prVal|
						tmp = msSize.collect({ |sl| prVal[sl] });
						switch(field,
							\midiModeSelect, {
								if(tmp.minItem != tmp.maxItem, {
									if(guiEnv.msEditor.midiModeSelect.items.size == 2, {
										guiEnv.msEditor.midiModeSelect.items = guiEnv.msEditor.midiModeSelect.items.add("--");
									});
									guiEnv.msEditor.midiModeSelect.value_(2)
								}, {
									if(guiEnv.msEditor.midiModeSelect.items.size == 3, {
										guiEnv.msEditor.midiModeSelect.items.remove(
											guiEnv.msEditor.midiModeSelect.items.last
										);
										guiEnv.msEditor.midiModeSelect.items_(
											guiEnv.msEditor.midiModeSelect.items
										)
									});
									guiEnv.msEditor.midiModeSelect.value_(prVal[slot]);
								})
							},
							\ctrlButtonBankField, {
								if((try { tmp.minItem == tmp.maxItem } ?? {
									tmp.select(_.isNumber).size == tmp.size
								}), {
									guiEnv.msEditor.ctrlButtonBankField.string_(prVal[slot]);
								}, {
									guiEnv.msEditor.ctrlButtonBankField.string_("--")
								})
							},
							{
								if(tmp.minItem == tmp.maxItem, {
									guiEnv.msEditor.perform(field).string_(prVal[slot]);
								}, {
									guiEnv.msEditor.perform(field).string_("--");
								})
							}
						)
					});
					if(GUI.id !== \cocoa, { guiEnv.midiBut.toolTip_(
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
					if(GUI.id !== \cocoa, {
						msSize.do({ |sl|
							guiEnv.msEditor.midiEditGroups[sl].midiHead.toolTip_(
								"Edit all MIDI-options for slot %:\nmidiMode: %\nmidiMean: %\nmidiResolution: %\nsoftWithin: %\nctrlButtonBank: %".format(
									sl, this.getMidiMode(sl), this.getMidiMean(sl), this.getMidiResolution(sl), this.getSoftWithin(sl), this.getCtrlButtonBank(sl)
								)
							)
						})
					})
				})
			})
		})
	}

	prInitOscConnect { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|
		var oscResponderAction, tmp;
		var intSlots;

		wcm.oscConnection.controller ?? {
			wcm.oscConnection.controller = SimpleController(wcm.oscConnection.model);
		};

		wcm.oscConnection.controller.put(\default, { |theChanger, what, moreArgs|
			// "prInitOscConnect: %\n".postf(theChanger);
			if(debug, { "widget '%' (%) at slot '%' oscConnection.model: %\n".postf(this.name, this.class, slot, theChanger) });
			// "% isCVCWidget: %\n".postf(this.name, this.isCVCWidget);

			switch(prCalibrate.class,
				Event, { thisCalib = prCalibrate[slot] },
				Array, { thisCalib = prCalibrate[slot] },
				{ thisCalib = prCalibrate }
			);

			if(theChanger.value.size == 4, {
// 				OSCresponderNode: t, r, msg
// 				OSCfunc: msg, time, addr // for the future
				oscResponderAction = { |t, r, msg, addr|
					// "msg: %\n".postf(msg);
					// "msg[theChanger.value[3]]: %\n".postf(msg[theChanger.value[3]]);
					this.oscReplyPort !? { addr.port_(this.oscReplyPort) };
					midiOscEnv.oscReplyAddrs ?? { midiOscEnv.oscReplyAddrs = [] };
					if(midiOscEnv.oscReplyAddrs.includesEqual(addr).not, {
						midiOscEnv.oscReplyAddrs = midiOscEnv.oscReplyAddrs.add(addr);
						midiOscEnv.oscReplyAddrs = midiOscEnv.oscReplyAddrs.asBag.contents.keys.asArray;
					});
					if(thisCalib, {
						if(midiOscEnv.calibConstraints.isNil, {
							midiOscEnv.calibConstraints = (lo: msg[theChanger.value[3]], hi: msg[theChanger.value[3]]);
						}, {
							if(msg[theChanger.value[3]] <= 0 and:{
								msg[theChanger.value[3]].abs > alwaysPositive;
							}, { alwaysPositive = msg[theChanger.value[3]].abs+0.1 });
							if(msg[theChanger.value[3]] < midiOscEnv.calibConstraints.lo, {
								midiOscEnv.calibConstraints.lo = msg[theChanger.value[3]];
								wcm.oscInputRange.model.value_([
									msg[theChanger.value[3]],
									wcm.oscInputRange.model.value[1]
								]).changedKeys(synchKeys);
							});
							if(msg[theChanger.value[3]] > midiOscEnv.calibConstraints.hi, {
								midiOscEnv.calibConstraints.hi = msg[theChanger.value[3]];
								wcm.oscInputRange.model.value_([
									wcm.oscInputRange.model.value[0],
									msg[theChanger.value[3]]
								]).changedKeys(synchKeys);
							});
						});
						// [slot, midiOscEnv.calibConstraints.lo, midiOscEnv.calibConstraints.hi].postln;
						wcm.mapConstrainterLo.value_(midiOscEnv.calibConstraints.lo);
						wcm.mapConstrainterHi.value_(midiOscEnv.calibConstraints.hi);
					}, {
						if(midiOscEnv.calibConstraints.isNil, {
							midiOscEnv.calibConstraints = (
								lo: wcm.oscInputRange.model.value[0],
								hi: wcm.oscInputRange.model.value[1]
							)
						})
					});
					if(this.class == CVWidgetKnob or:{ this.class == CVWidget2D }, {
						argWidgetCV.value_(
							(msg[theChanger.value[3]]+alwaysPositive).perform(
								midiOscEnv.oscMapping,
								midiOscEnv.calibConstraints.lo+alwaysPositive,
								midiOscEnv.calibConstraints.hi+alwaysPositive,
								this.getSpec(slot).minval, this.getSpec(slot).maxval,
								\minmax
							)
						)
					}, {
						argWidgetCV.value_([
							argWidgetCV.value[..(slot-1)],
							(msg[theChanger.value[3]]+alwaysPositive).perform(
								midiOscEnv.oscMapping,
								midiOscEnv.calibConstraints.lo+alwaysPositive,
								midiOscEnv.calibConstraints.hi+alwaysPositive,
								[this.getSpec(slot).minval].flat.wrapAt(slot),
								[this.getSpec(slot).maxval].flat.wrapAt(slot),
								\minmax
							),
							argWidgetCV.value[(slot+1)..]
						].flat);
					})
				};

				if(theChanger.value[0].size > 0, { netAddr = NetAddr(theChanger.value[0], theChanger.value[1]) });

				if(midiOscEnv.oscResponder.isNil, {
					midiOscEnv.oscResponder = OSCresponderNode(netAddr, theChanger.value[2].asSymbol, oscResponderAction).add;
//					midiOscEnv.oscResponder = OSCFunc(oscResponderAction, theChanger.value[2].asSymbol, netAddr);
					midiOscEnv.oscMsgIndex = theChanger.value[3];
				}, {
					midiOscEnv.oscResponder.action_(oscResponderAction);
				});

				tmp = theChanger.value[2].asString++"["++theChanger.value[3].asString++"]"++"\n"++midiOscEnv.oscMapping.asString;
				if(this.class == CVWidgetMS, {
					tmp = slot.asString++":"+tmp;
				});

				// "now synching oscDisplay: %[%]\n".postf(this.name, slot);
				wcm.oscDisplay.model.value_(
					(
						but: [tmp, Color.white, Color.cyan(0.5)],
						ipField: theChanger.value[0] !? { theChanger.value[0].asString },
						portField: theChanger.value[1] !? { theChanger.value[1].asString },
						nameField: theChanger.value[2].asString,
						index: theChanger.value[3],
						connectorButVal: 1,
						editEnabled: false
					)
				).changedKeys(synchKeys);
			});

			if(theChanger.value == false, {
				midiOscEnv.oscResponder.remove;
				midiOscEnv.oscResponder = nil;
				midiOscEnv.msgIndex = nil;
				wcm.oscInputRange.model.value_([0.0001, 0.0001]).changedKeys(synchKeys);
				midiOscEnv.calibConstraints = nil;
				// if(this.class == CVWidgetMS, { msSlots[slot] = nil; msCmds[slot] = nil });

				tmp = "edit OSC";
				if(this.class == CVWidgetMS, { tmp = slot.asString++":"+tmp });
				wcm.oscDisplay.model.value_(
					(
						but: [tmp, stringColor, background],
						ipField: wcm.oscDisplay.model.value.ipField,
						portField: wcm.oscDisplay.model.value.portField,
						nameField: wcm.oscDisplay.model.value.nameField,
						index: wcm.oscDisplay.model.value.index,
						connectorButVal: 0,
						editEnabled: true
					)
				).changedKeys(synchKeys);
			})
		})
	}

	prInitOscDisplay { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|
		var thisEditor, thisOscEditBut, p, tmp;
		var numOscString, numOscResponders, oscButBg, oscButTextColor;
		var msEditEnabled;

		wcm.oscDisplay.controller ?? {
			wcm.oscDisplay.controller = SimpleController(wcm.oscDisplay.model);
		};

		wcm.oscDisplay.controller.put(\default, { |theChanger, what, moreArgs|
			// "prInitOscDisplay: %\n".postf(theChanger);
			if(debug, { "widget '%' (%) at slot '%' oscDisplay.model: %\n".postf(this.name, this.class, slot, theChanger) });

			// "oscDisplay.controller in - %[%]: %\n".postf(this.name, slot, midiOscEnv);

			switch(prCalibrate.class,
				Event, { thisCalib = prCalibrate[slot] },
				Array, { thisCalib = prCalibrate[slot] },
				{ thisCalib = prCalibrate }
			);

			if(this.class == CVWidgetMS, {
				thisEditor = guiEnv.editor[slot];
				guiEnv[\msEditor] !? {
					thisOscEditBut = guiEnv.msEditor.oscEditBtns[slot];
					if(GUI.id !== \cocoa, {
						if(theChanger.value.but[0] == "edit OSC", {
							if(slot.notNil, { p =  " in '"++slot++"'" }, { p = "" });
							thisOscEditBut.toolTip_("no OSC-responder present%.\nClick to edit.".format(p));
						}, {
							thisOscEditBut.toolTip_("Connected, listening to\n%, msg-slot %,\nusing '%' in-output mapping".format(theChanger.value.nameField, theChanger.value.index, midiOscEnv.oscMapping));
						})
					});
				};
				if(GUI.id !== \cocoa, {
					case
						{ this.midiOscEnv.select({ |it| it.oscResponder.notNil }).size > 0 and:{
							this.midiOscEnv.select({ |it| it.oscResponder.notNil }).size < msSize
						}} {
							guiEnv.oscBut.toolTip_("partially connected - connected slots:\n"++this.midiOscEnv.selectIndex({ |it| it.oscResponder.notNil }))
						}
						{ this.midiOscEnv.select({ |it| it.oscResponder.notNil }).size == msSize } {
							guiEnv.oscBut.toolTip_("all slots connected.\nClick to edit.")
						}
						{ this.midiOscEnv.select({ |it| it.oscResponder.notNil }).size == 0 } {
							guiEnv.toolTip_("no OSC-responders present.\nClick to edit.")
						}
					;
				})
			}, {
				thisEditor = guiEnv.editor;
				if(GUI.id !== \cocoa, {
					if(theChanger.value.but[0] == "edit OSC", {
						if(slot.notNil, { p =  " in '"++slot++"'" }, { p = "" });
						guiEnv.oscEditBut.toolTip_("no OSC-responder present%.\nClick to edit.".format(p));
					}, {
						guiEnv.oscEditBut.toolTip_("Connected, listening to\n%, msg-slot %,\nusing '%' in-output mapping".format(theChanger.value.nameField, theChanger.value.index, midiOscEnv.oscMapping));
					})
				});
				thisOscEditBut = guiEnv.oscEditBut;
			});

			if(parent.isClosed.not, {
				if(this.class != CVWidgetMS, {
					if(midiOscEnv.oscResponder.isNil, {
						// this.name.postln;
						// "midiOscEnv.oscResponder is nil".postln;
						tmp = background
					}, { tmp = Color.cyan(0.5) });
					guiEnv.oscEditBut.states_([
						[theChanger.value.but[0], theChanger.value.but[1], tmp]
					]);
					guiEnv.oscEditBut.refresh;
				}, {
					numOscResponders = this.midiOscEnv.select({ |it| it.oscResponder.notNil }).size;
					numOscString = "OSC ("++numOscResponders++"/"++msSize++")";
					if(numOscResponders > 0, {
						// "numOscResponders > 0: %\n".postf(numOscResponders);
						oscButBg = Color.cyan(0.5);
						oscButTextColor = Color.white;
					}, {
						// "no OSCresponders".postln;
						oscButBg = background;
						oscButTextColor = stringColor;
					});
					this.guiEnv[\oscBut].states_([[
						numOscString,
						oscButTextColor, // text
						oscButBg // background
					]]);
				})
			});

			if(this.class == CVWidgetMS, {
				defer {
					if(guiEnv.msEditor.notNil and:{
						guiEnv.msEditor.isClosed.not
					}, {
						if(this.midiOscEnv.select({ |sl| sl.oscResponder.notNil }).size < msSize, {
							guiEnv.msEditor.connectorBut.enabled_(true).states_([
								[guiEnv.msEditor.connectorBut.states[0][0], guiEnv.msEditor.connectorBut.states[0][1], Color.red]
							]);
						}, { guiEnv.msEditor.connectorBut.enabled_(false).states_([
							[guiEnv.msEditor.connectorBut.states[0][0], guiEnv.msEditor.connectorBut.states[0][1], Color.red(alpha: 0.5)]
						]) });
						if(this.midiOscEnv.select({ |sl| sl.oscResponder.notNil }).size > 0, {
							guiEnv.msEditor.oscDisconnectorBut.enabled_(true).states_([
								[guiEnv.msEditor.oscDisconnectorBut.states[0][0], guiEnv.msEditor.oscDisconnectorBut.states[0][1], Color.blue]
							])
						}, { guiEnv.msEditor.oscDisconnectorBut.enabled_(false).states_([
							[guiEnv.msEditor.oscDisconnectorBut.states[0][0], guiEnv.msEditor.oscDisconnectorBut.states[0][1], Color.blue(alpha: 0.5)]
						]) });
						// guiEnv.msEditor.connectorBut.value_(theChanger.value.connectorButVal);
						if(theChanger.value.ipField.notNil, {
							if(theChanger.value.portField.notNil, {
								guiEnv.msEditor.portRestrictor.value_(1);
								if(this.midiOscEnv.collect({ |it|
									it.oscResponder !? { it.oscResponder.addr }
								}).takeThese(_.isNil).asBag.contents.size > 1, {
									guiEnv.msEditor.deviceDropDown.items_(
										["receiving OSC-messages from various addresses..."]++guiEnv.msEditor.deviceDropDown.items[1..]
									)
								}, {
									if(theChanger.value.portField.notNil, {
										guiEnv.msEditor.deviceDropDown.items_(
											["select IP-address:port... (optional)"]++guiEnv.msEditor.deviceDropDown.items[1..]
										)
									}, {
										guiEnv.msEditor.deviceDropDown.items_(
											["select IP-address... (optional)"]++guiEnv.msEditor.deviceDropDown.items[1..]
										)
									})
								})
							})
						});
						thisOscEditBut.states_([theChanger.value.but]);
						if(this.midiOscEnv.select({ |sl| sl.oscResponder.notNil }).size < msSize, {
							msEditEnabled = true;
						}, {
							msEditEnabled = false;
						});
						[
							guiEnv.msEditor.deviceDropDown,
							guiEnv.msEditor.portRestrictor,
							guiEnv.msEditor.deviceListMenu,
							guiEnv.msEditor.cmdListMenu,
							guiEnv.msEditor.extOscCtrlArrayField,
							guiEnv.msEditor.intStartIndexField,
							guiEnv.msEditor.nameField,
							guiEnv.msEditor.indexField
						].do(_.enabled_(msEditEnabled));

						if(GUI.id !== \cocoa, {
							[
								guiEnv.msEditor.connectorBut,
								guiEnv.msEditor.oscDisconnectorBut
							].do({ |b|
								if(b.enabled, {
									b.toolTip_(
										"Currently connected to external OSC-controllers: %".format(
											if((tmp = this.midiOscEnv.selectIndex({ |sl| sl.oscResponder.notNil })).size > 0, { tmp }, { "none" })
										)
									)
								})
							})
						})
					})
				}
			});

			if(thisEditor.notNil and:{
				thisEditor.isClosed.not
			}, {
				defer {
					thisEditor.connectorBut.value_(theChanger.value.connectorButVal);
					thisEditor.nameField.string_(theChanger.value.nameField);
					if(thisCalib, {
						[
							thisEditor.calibNumBoxes.lo,
							thisEditor.calibNumBoxes.hi
						].do(_.enabled_(theChanger.value.editEnabled));
					});
					thisEditor.indexField.value_(theChanger.value.index);
					[
						thisEditor.deviceDropDown,
						thisEditor.portRestrictor,
						thisEditor.cmdListMenu,
						thisEditor.deviceListMenu,
						thisEditor.nameField,
						thisEditor.indexField
					].do(_.enabled_(theChanger.value.editEnabled));
				}
			});
			// "oscDisplay.controller out - %[%]: %\n".postf(this.name, slot, midiOscEnv);
		})
	}

	prInitOscInputRange { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|
		var thisEditor, thisOscEditBut, p, tmp;
		var mappingsDiffer;

		wcm.oscInputRange.controller ?? {
			wcm.oscInputRange.controller = SimpleController(wcm.oscInputRange.model);
		};

		wcm.oscInputRange.controller.put(\default, { |theChanger, what, moreArgs|
			// "prInitOscInputRange: %\n".postf(theChanger.value);
			if(debug, { "widget '%' (%) at slot '%' oscInputRange.model: %\n".postf(this.name, this.class, slot, theChanger) });

			midiOscEnv.calibConstraints = (lo: theChanger.value[0], hi: theChanger.value[1]);

			if(this.class == CVWidgetMS, {
				thisEditor = guiEnv.editor[slot];
				guiEnv.msEditor !? {
					thisOscEditBut = guiEnv.msEditor.oscEditBtns[slot];
				}
			}, {
				thisEditor = guiEnv.editor;
				thisOscEditBut = guiEnv.oscEditBut;
			});

			{
				if(thisEditor.notNil and:{
					thisEditor.isClosed.not
				}, {
					thisEditor.mappingSelect.items.do({ |item, i|
						if(item.asSymbol === midiOscEnv.oscMapping, {
							thisEditor.mappingSelect.value_(i)
						})
					});
					wcm.mapConstrainterLo.value_(theChanger.value[0]);
					wcm.mapConstrainterHi.value_(theChanger.value[1]);
					thisEditor.alwaysPosField.string_(" +"++(alwaysPositive.trunc(0.1)));
				});

				if(this.class == CVWidgetMS, {
					if(guiEnv.msEditor.notNil and:{
						guiEnv.msEditor.isClosed.not
					}, {
						tmp = msSize.collect({ |sl| this.getOscMapping(sl) });
						block { |break|
							(1..msSize-1).do({ |sl|
								if(tmp[0] != tmp[sl], { break.value(mappingsDiffer = true) }, { mappingsDiffer = false });
							})
						};

						midiOscEnv.oscResponder !? {
							thisOscEditBut.states_([[
								thisOscEditBut.states[0][0].split($\n)[0]++"\n"++midiOscEnv.oscMapping.asString,
								thisOscEditBut.states[0][1],
								thisOscEditBut.states[0][2]
							]])
						};


						if(mappingsDiffer, {
							guiEnv.msEditor.mappingSelect.value_(0);
						}, {
							guiEnv.msEditor.mappingSelect.items.do({ |item, i|
								if(item.asSymbol === midiOscEnv.oscMapping, {
									guiEnv.msEditor.mappingSelect.value_(i);
								})
							})
						})
					})
				});

				if(parent.isClosed.not, {
					if(this.class != CVWidgetMS, {
						if(guiEnv.oscEditBut.states[0][0].split($\n)[0] != "edit OSC", {
							guiEnv.oscEditBut.states_([[
								guiEnv.oscEditBut.states[0][0].split($\n)[0]++"\n"++midiOscEnv.oscMapping.asString,
								guiEnv.oscEditBut.states[0][1],
								guiEnv.oscEditBut.states[0][2]
							]]);
							if(GUI.id !== \cocoa, {
								p = guiEnv.oscEditBut.toolTip.split($\n);
								p[2] = "using '"++midiOscEnv.oscMapping.asString++"' in-output mapping";
								p = p.join("\n");
								guiEnv.oscEditBut.toolTip_(p);
							});
							guiEnv.oscEditBut.refresh;
						})
					})
				})
			}.defer;
		})
	}

	prInitActionsControl { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|

		wcm.actions.controller ?? {
			switch(this.class,
				CVWidgetMS, {
					wcm.actions.controller = SimpleController(wdgtControllersAndModels.actions.model);
				},
				{ wcm.actions.controller = SimpleController(wcm.actions.model) }
			)
 		};

		wcm.actions.controller.put(\default, { |theChanger, what, moreArgs|
			// "prInitActionsControl: %\n".postf(theChanger.value);
			if(debug, { "widget '%' (%) at slot '%' actions.model: %\n".postf(this.name, this.class, slot, theChanger) });

			if(parent.isClosed.not, {
				guiEnv.actionsBut.states_([[
					"actions ("++theChanger.value.activeActions++"/"++theChanger.value.numActions++")",
					Color(0.08, 0.09, 0.14),
					Color(0.32, 0.67, 0.76),
				]]);
				if(GUI.id !== \cocoa, {
					guiEnv.actionsBut.toolTip_(""++theChanger.value.activeActions++" of "++theChanger.value.numActions++" active.\nClick to edit")
				})
			})
		})
	}

	prInitSlidersTextConnection { |wcm, guiEnv, midiOscEnv, argWidgetCV, thisCalib, slot|

		wdgtControllersAndModels.slidersTextConnection.controller ?? {
			wdgtControllersAndModels.slidersTextConnection.controller = SimpleController(
				wdgtControllersAndModels.slidersTextConnection.model
			);
		};

		wdgtControllersAndModels.slidersTextConnection.controller.put(\default, { |theChanger, what, moreArgs|
			if(debug, { "widget '%' (%) slidersTextConnection.model: %\n".postf(this.name, this.class, theChanger) });
			theChanger.value[0] !? {
				if(theChanger.value[0], {
					// connect sliders
					switch(this.class,
						CVWidget2D, {
							[this.slider2d, this.rangeSlider].do({ |view|
								sliderConnection = [widgetCV.lo, widgetCV.hi].cvWidgetConnect(view);
							})
						},
						CVWidgetMS, {
							sliderConnection = widgetCV.cvWidgetConnect(this.mSlider);
						},
						CVWidgetKnob, {
							sliderConnection = widgetCV.cvWidgetConnect(this.knob);
						}
					);
					this.activeSliderB.value_(1);
					if(GUI.id !== \cocoa, {
						this.activeSliderB.toolTip_("deactivate CV-slider connection");
					})
				}, {
					// disconnect sliders
					if(this.class == CVWidget2D, {
						[widgetCV.lo, widgetCV.hi].cvWidgetDisconnect(sliderConnection);
					}, {
						widgetCV.cvWidgetDisconnect(sliderConnection);
					});
					this.activeSliderB.value_(0);
					sliderConnection = nil;
					if(GUI.id !== \cocoa, {
						this.activeSliderB.toolTip_("activate CV-slider connection");
					})
				})
			};
			theChanger.value[1] !? {
				if(theChanger.value[1], {
					// connect textfields
					if(this.class == CVWidget2D, {
						textConnection = ();
						#[lo, hi].do{ |hilo|
							textConnection.put(hilo, widgetCV[hilo].cvWidgetConnect(this.numVal[hilo]));
						}
					}, {
						textConnection = widgetCV.cvWidgetConnect(this.numVal);
					});
					this.activeTextB.value_(1);
					if(GUI.id !== \cocoa, {
						this.activeTextB.toolTip_("deactivate CV-numberbox connection");
					})
				}, {
					// disconnect textfields
					if(this.class == CVWidget2D, {
						#[lo, hi].do{ |hilo|
							widgetCV[hilo].cvWidgetDisconnect(textConnection[hilo]);
						}
					}, {
						widgetCV.cvWidgetDisconnect(textConnection);
					});
					this.activeTextB.value_(0);
					textConnection = nil;
					if(GUI.id !== \cocoa, {
						this.activeTextB.toolTip_("activate CV-numberbox connection");
					})
				})
			};

			// "theChanger.value: %\n".postf(theChanger.value);
			theChanger.value[0].isKindOf(Boolean).if{ connectS = theChanger.value[0] };
			theChanger.value[1].isKindOf(Boolean).if{ connectTF = theChanger.value[1] };
			// "connectS: %, connectTF: %\n".postf(connectS, connectTF);

			// "this: %, sliderConnection: %, textConnection: %\n".postf(name, sliderConnection, textConnection);

			wdgtControllersAndModels.slidersTextConnection.model.value_(
				[sliderConnection.notNil, textConnection.notNil]
			)
		})
	}

	// EXPERIMENTAL: extended API
	extend { |key, func ... controllers|
		var thisKey, thisControllers;

		thisKey = key.asSymbol;
		thisControllers = controllers.collect({ |c| c.asSymbol });
		synchedActions ?? { synchedActions = IdentityDictionary.new };

		synchKeys = synchKeys.add(thisKey);
		synchedActions.put(thisKey, func);

		if(thisKey != \default, {
			if(controllers.size == 0, {
				wdgtControllersAndModels.pairsDo({ |k, v|
					if(k != \mapConstrainterHi and:{
						k != \mapConstrainterLo
					}, {
						v.controller.put(thisKey, synchedActions[thisKey])
					})
				})
			}, {
				thisControllers.do({ |c|
					if(wdgtControllersAndModels[c].notNil and:{
						c != \mapConstrainterHi and:{
							c != \mapConstrainterLo
						}
					}, {
						wdgtControllersAndModels[c].controller.put(thisKey, synchedActions[thisKey]);
					})
				})
			})
		}, { Error("'default' is a reserved key and can not be used to extend a controller-action.").throw })
	}

	reduce { |key|
		var thisKey;

		thisKey = key.asSymbol;
		if(key.notNil and:{ thisKey !== \default and:{ synchKeys.includes(thisKey) }}, {
			synchedActions[thisKey] = nil;
			synchKeys.remove(thisKey);
		}, {
			synchKeys.do({ |k|
				if(k != \default, {
					synchedActions[k] = nil;
					synchKeys.remove[k];
				})
			})
		})
	}

}