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

CVWidgetKnob : CVWidget {

	var <knob, <numVal, <specBut, <midiHead, <midiLearn, <midiSrc, <midiChan, <midiCtrl, <oscEditBut, <calibBut, <actionsBut;
	// persistent widgets
	var isPersistent, oldBounds, oldName;

	*new { |parent, widgetCV, name, connectKnob, connectNumVal, bounds, defaultAction, setup, controllersAndModels, cvcGui, persistent, server|
		^super.newCopyArgs(parent, widgetCV, name, connectKnob, connectNumVal).init(
			bounds,
			defaultAction,
			setup,
			controllersAndModels,
			cvcGui,
			persistent,
			server // swing compatibility. well, ...
		)
	}

	init { |bounds, action, setupArgs, controllersAndModels, cvcGui, persistent, server|
		var thisXY, thisX, thisY, thisWidth, thisHeight, knobsize, widgetSpecsActions;
		var msrc = "source", mchan = "chan", mctrl = "ctrl", margs;
		var nextY, knobX, knobY;
		var text, tActions;

		background ?? { background = Color.white };
		stringColor ?? { stringColor = Color.black };

		synchKeys ?? { synchKeys = [\default] };

		prCalibrate = true;
		prMidiMode = 0;
		prMidiMean = 64;
		prMidiResolution = 1;
		prSoftWithin = 0.1;

		guiEnv = ();
		cvcGui !? { isCVCWidget = true };

		if(cvcGui.class == Event and:{ cvcGui.midiOscEnv.notNil }, { midiOscEnv = cvcGui.midiOscEnv }, { midiOscEnv = () });
		midiOscEnv.oscMapping ?? { midiOscEnv.oscMapping = \linlin };

		name ?? { name = "knob" };

		widgetCV ?? { widgetCV = CV.new };

		this.initControllersAndModels(controllersAndModels);

		setupArgs !? {
			setupArgs.isKindOf(Event).not.if { Error("a setup has to be provided as a Dictionary or an Event").throw };
			setupArgs[\midiMode] !? { this.setMidiMode(setupArgs[\midiMode]) };
			setupArgs[\midiResolution] !? { this.setMidiResolution(setupArgs[\midiResolution]) };
			setupArgs[\midiMean] !? { this.setMidiMean(setupArgs[\midiMean]) };
			setupArgs[\ctrlButtonBank] !? { this.setCtrlButtonBank(setupArgs[\ctrlButtonBank]) };
			setupArgs[\softWithin] !? { this.setSoftWithin(setupArgs[\softWithin]) };
			setupArgs[\calibrate] !? { this.setCalibrate(setupArgs[\calibrate]) };
		};

		action !? { this.addAction(\default, action) };

		if(bounds.isNil, {
			thisXY = 7@0;
			thisX = 50; thisY = 50;
			thisWidth = 52;
			thisHeight = 164;
		}, {
			if(parent.isNil, { thisXY = 7@0 }, { thisXY = bounds.left@bounds.top });
			thisX = bounds.left; thisY = bounds.top;
			thisWidth = bounds.width;
			thisHeight = bounds.height;
		});

		parent ?? {
			parent = Window(name, Rect(thisX, thisY, thisWidth+14, thisHeight+7), server: server);
		};
		// parent.acceptsMouseOver_(true);

		cvcGui ?? {
			parent.onClose_({
				if(this.editor.notNil, {
					if(this.editor.isClosed.not, {
						this.editor.close;
					}, {
						if(AbstractCVWidgetEditor.allEditors.notNil and:{
							AbstractCVWidgetEditor.allEditors[name.asSymbol].notNil;
						}, {
							AbstractCVWidgetEditor.allEditors.removeAt(name.asSymbol)
						})
					})
				})
			});
			if(persistent == false or:{ persistent.isNil }, {
				parent.onClose_(parent.onClose.addFunc({
					// "remove, remove".postln;
					this.oscDisconnect;
					this.midiDisconnect;
					// midiOscEnv.oscResponder !? { midiOscEnv.oscResponder.remove };
					// midiOscEnv.cc !? { midiOscEnv.cc.remove };
					wdgtControllersAndModels.do({ |mc| mc.isKindOf(SimpleController).if{ mc.controller.remove } });
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
				[""+name.asString, Color.white, Color.blue],
				[""+name.asString, Color.black, Color.yellow],
			])
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.action_({ |b|
				this.toggleComment(b.value.asBoolean);
			})
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

		knobsize = thisHeight-2-120;
		if(knobsize >= thisWidth, {
			// "knobsize greater than or equal width".postln;
			knobsize = thisWidth;
			knobY = thisXY.y+16+(thisHeight-124-knobsize/2);
			knobX = thisXY.x;
		}, {
			// "knobsize smaller than width".postln;
			knobsize = thisHeight-124;
			knobX = thisWidth-knobsize/2+thisXY.x;
			knobY = thisXY.y+16;
		});
		knob = Knob(parent, Rect(knobX, knobY, knobsize, knobsize))
			// .canFocus_(false)
			.mode_(\vert)
			.focusColor_(Color.green)
		;

		activeSliderB = Button(parent, Rect(thisXY.x+thisWidth-9, thisXY.y+knobsize+5, 7, 7))
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

		if(widgetCV.spec.excludingZeroCrossing, { knob.centered_(true) });
		// if(widgetCV.spec.minval == widgetCV.spec.maxval.neg, { knob.centered_(true) });
		nextY = thisXY.y+thisHeight-112;

		numVal = NumberBox(parent, Rect(thisXY.x+1, nextY, thisWidth-2, 15))
			.value_(widgetCV.value).font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9.5))
			.focusColor_(Color.green)
		;

		activeTextB = Button(parent, Rect(thisXY.x+thisWidth-9, nextY+7, 7, 7))
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

		nextY = nextY+numVal.bounds.height;
		specBut = Button(parent, Rect(thisXY.x+1, nextY, thisWidth-2, 15))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.states_([["edit Spec", Color.white, Color(1.0, 0.3)]])
			.action_({ |btn|
				if(this.editor.isNil or:{ this.editor.isClosed }, {
					CVWidgetEditor(this, 0);
				}, {
					this.editor.front(0)
				});
				wdgtControllersAndModels.oscDisplay.model.value_(
					wdgtControllersAndModels.oscDisplay.model.value;
				).changedKeys(synchKeys);
				wdgtControllersAndModels.midiDisplay.model.value_(
					wdgtControllersAndModels.midiDisplay.model.value
				).changedKeys(synchKeys);
			})
			.focusColor_(Color.green)
		;
		if(GUI.id !== \cocoa, { specBut.toolTip_("Edit the CV's ControlSpec:\n"++(this.getSpec.asCompileString)) });

		nextY = nextY+specBut.bounds.height+1;
		midiHead = Button(parent, Rect(thisXY.x+1, nextY, thisWidth-17, 15))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.states_([["MIDI", stringColor, background]])
			.action_({ |ms|
				if(this.editor.isNil or:{ this.editor.isClosed }, {
					CVWidgetEditor(this, 1);
				}, {
					this.editor.front(1)
				});
				wdgtControllersAndModels.oscDisplay.model.value_(
					wdgtControllersAndModels.oscDisplay.model.value;
				).changedKeys(synchKeys);
				wdgtControllersAndModels.midiDisplay.model.value_(
					wdgtControllersAndModels.midiDisplay.model.value
				).changedKeys(synchKeys);
			})
			.focusColor_(Color.green)
		;
		if(GUI.id !== \cocoa, { midiHead.toolTip_("Edit all MIDI-options\nof this widget.\nmidiMode:"+this.getMidiMode++"\nmidiMean:"+this.getMidiMean++"\nmidiResolution:"+this.getMidiResolution++"\nsoftWithin:"+this.getSoftWithin++"\nctrlButtonBank:"+this.getCtrlButtonBank) });

		if(GUI.id === \qt, {
			midiHead.mouseEnterAction_({ |mb|
				mb.states_([["MIDI", Color.white, Color.red]])
			}).mouseLeaveAction_({ |mb|
				mb.states_([["MIDI", stringColor, background]])
			})
		});

		midiLearn = Button(parent, Rect(thisXY.x+thisWidth-16, nextY, 15, 15))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.states_([
				["L", Color.white, Color.blue],
				["X", Color.white, Color.red]
			])
			.action_({ |ml|
				ml.value.switch(
					1, {
						margs = [
							[midiSrc.string, msrc],
							[midiChan.string, mchan],
							[midiCtrl.string, mctrl]
						].collect({ |pair| if(pair[0] != pair[1], { pair[0].asInt }, { nil }) });
						if(margs.select({ |i| i.notNil }).size > 0, {
							this.midiConnect(*margs);
						}, {
							this.midiConnect;
						})
					},
					0, { this.midiDisconnect }
				)
			})
			.focusColor_(Color.green)
		;
		if(GUI.id !== \cocoa, { midiLearn.toolTip_("Click and and move an arbitrary\nslider on your MIDI-device to\nconnect the widget to that slider.") });

		nextY = nextY+midiLearn.bounds.height;
		midiSrc = TextField(parent, Rect(thisXY.x+1, nextY, thisWidth-2, 12))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.string_(msrc)
			.background_(Color.white)
			.stringColor_(Color.black)
			.action_({ |tf|
				if("^[-+]?[0-9]*$".matchRegexp(tf.string), {
					wdgtControllersAndModels.midiDisplay.model.value_((
						learn: "C",
						src: tf.string,
						chan: wdgtControllersAndModels.midiDisplay.model.value.chan,
						ctrl: wdgtControllersAndModels.midiDisplay.model.value.ctrl
					)).changedKeys(synchKeys)
				})
			})
			.mouseDownAction_({ |tf|
				tf.stringColor_(Color.red)
			})
			.keyDownAction_({ |tf, char, modifiers, unicode, keycode|
				if(unicode == 13, {
					tf.stringColor_(Color.black);
				})
			})
			.focusColor_(Color.green)
		;
		if(GUI.id !== \cocoa, { midiSrc.toolTip_("Enter your MIDI-device's ID,\nhit 'return' and click 'C' to\nconnect all sliders of your\ndevice to this widget") });

		nextY = nextY+midiSrc.bounds.height;
		midiChan = TextField(parent, Rect(thisXY.x+1, nextY, thisWidth-2/2, 12))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.string_(mchan)
			.background_(Color.white)
			.stringColor_(Color.black)
			.action_({ |tf|
				if("^[0-9]*$".matchRegexp(tf.string), {
					wdgtControllersAndModels.midiDisplay.model.value_((
						learn: "C",
						src: wdgtControllersAndModels.midiDisplay.model.value.src,
						chan: tf.string,
						ctrl: wdgtControllersAndModels.midiDisplay.model.value.ctrl
					)).changedKeys(synchKeys)
				})
			})
			.mouseDownAction_({ |tf|
				tf.stringColor_(Color.red)
			})
			.keyDownAction_({ |tf, char, modifiers, unicode, keycode|
				if(unicode == 13, {
					tf.stringColor_(Color.black);
				})
			})
			.focusColor_(Color.green)
		;
		if(GUI.id !== \cocoa, { midiChan.toolTip_("Enter a MIDI-channel, hit 'return'\nand click 'C' to connect all sliders\nin that channel to this widget") });

		midiCtrl = TextField(parent, Rect(thisXY.x+(thisWidth-2/2)+1, nextY, thisWidth-2/2, 12))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.string_(mctrl)
			.background_(Color.white)
			.stringColor_(Color.black)
			.action_({ |tf|
				if("^[0-9]*$".matchRegexp(tf.string), {
					wdgtControllersAndModels.midiDisplay.model.value_((
						learn: "C",
						src: wdgtControllersAndModels.midiDisplay.model.value.src,
						chan: wdgtControllersAndModels.midiDisplay.model.value.chan,
						ctrl: tf.string
					)).changedKeys(synchKeys)
				})
			})
			.mouseDownAction_({ |tf|
				tf.stringColor_(Color.red)
			})
			.keyDownAction_({ |tf, char, modifiers, unicode, keycode|
				if(unicode == 13, {
					tf.stringColor_(Color.black);
				})
			})
			.focusColor_(Color.green)
		;
		if(GUI.id !== \cocoa, { midiCtrl.toolTip_("Enter a MIDI-ctrl-nr., hit 'return'\nand click 'C' to connect the slider\nwith that number to this widget") });

		nextY = nextY+midiCtrl.bounds.height+1;

		oscEditBut = Button(parent, Rect(thisXY.x+1, nextY, thisWidth-2, 25))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.states_([
				["edit OSC", stringColor, background]
			])
			.action_({ |oscb|
				if(this.editor.isNil or:{ this.editor.isClosed }, {
					CVWidgetEditor(this, 2);
				}, {
					this.editor.front(2)
				});
				this.editor.calibNumBoxes !? {
					wdgtControllersAndModels.mapConstrainterLo.connect(this.editor.calibNumBoxes.lo);
					this.editor.calibNumBoxes.lo.value_(wdgtControllersAndModels.oscInputRange.model.value[0]);
					wdgtControllersAndModels.mapConstrainterHi.connect(this.editor.calibNumBoxes.hi);
					this.editor.calibNumBoxes.hi.value_(wdgtControllersAndModels.oscInputRange.model.value[1]);
				};
				wdgtControllersAndModels.oscDisplay.model.value_(
					wdgtControllersAndModels.oscDisplay.model.value;
				).changedKeys(synchKeys);
				wdgtControllersAndModels.midiDisplay.model.value_(
					wdgtControllersAndModels.midiDisplay.model.value
				).changedKeys(synchKeys);
			})
			.focusColor_(Color.green)
		;

		if(GUI.id === \qt, {
			oscEditBut.mouseEnterAction_({ |oscb|
				if(wdgtControllersAndModels.oscConnection.model.value === false, {
					oscb.states_([["edit OSC", Color.white, Color.cyan(0.5)]]);
				})
			}).mouseLeaveAction_({ |oscb|
				if(wdgtControllersAndModels.oscConnection.model.value === false, {
					oscb.states_([["edit OSC", stringColor, background]])
				})
			})
		});
		if(GUI.id !== \cocoa, { oscEditBut.toolTip_("no OSC-responders present.\nClick to edit.") });

		calibBut = Button(parent, Rect(
			thisXY.x+oscEditBut.bounds.width-9,
			nextY+oscEditBut.bounds.height-10,
			10, 10
		))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.states_([
				["", Color.black, Color.green],
				["", Color.white, Color.red]
			])
			.action_({ |cb|
				switch(cb.value,
					0, { this.setCalibrate(true) },
					1, { this.setCalibrate(false) }
				)
			})
		;
		if(GUI.id !== \cocoa, {
			if(this.getCalibrate, {
				text = "Calibration is active.\nClick to deactivate.";
			}, {
				text = "Calibration is inactive.\nClick to activate.";
			});
			calibBut.toolTip_(text);
		});

		nextY = nextY+oscEditBut.bounds.height;
		// nextY = nextY+calibBut.bounds.height;
		actionsBut = Button(parent, Rect(thisXY.x+1, nextY, thisWidth-2, 15))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.states_([
				["actions ("++this.wdgtActions.select({ |v| v.asArray[0][1] == true }).size++"/"++this.wdgtActions.size++")", Color(0.08, 0.09, 0.14), Color(0.32, 0.67, 0.76)],
			])
			.action_({ |ab|
				if(this.editor.isNil or:{ this.editor.isClosed }, {
					CVWidgetEditor(this, 3);
				}, {
					this.editor.front(3)
				});
			})
			.focusColor_(Color.green)
		;
		if(GUI.id !== \cocoa, {
			text = [];
			text = text.add(this.wdgtActions.size);
			text = text.add(this.wdgtActions.select({ |v| v.asArray[0][1] == true }).size);
			if(text[0] == 1, { tActions = "action" }, { tActions = "actions" });
			actionsBut.toolTip_("% of % % active.\nClick to edit.".format(text[1], text[0], tActions));
		});

		if(prCalibrate, { calibBut.value_(0) }, { calibBut.value_(1) });

		// [knob, numVal].do({ |view| widgetCV.connect(view) });

		visibleGuiEls = [
			knob,
			numVal,
			activeSliderB,
			activeTextB,
			specBut,
			midiHead,
			midiLearn,
			midiSrc,
			midiChan,
			midiCtrl,
			oscEditBut,
			calibBut,
			actionsBut
		];
		allGuiEls = [
			widgetBg,
			label,
			nameField,
			knob,
			numVal,
			activeSliderB,
			activeTextB,
			specBut,
			midiHead,
			midiLearn,
			midiSrc,
			midiChan,
			midiCtrl,
			oscEditBut,
			calibBut,
			actionsBut
		];
		guiEnv = (
			editor: this.editor,
			calibBut: calibBut,
			actionsBut: actionsBut,
			knob: knob,
			oscEditBut: oscEditBut,
			midiHead: midiHead,
			midiSrc: midiSrc,
			midiChan: midiChan,
			midiCtrl: midiCtrl,
			midiLearn: midiLearn
		);

		focusElements = allGuiEls.copy.removeAll([
			widgetBg, nameField, calibBut, activeSliderB, activeTextB
		]);

		this.initControllerActions;

		connectS !? { /*"connectSliders".postln; */this.connectGUI(connectS, nil) };
		connectTF !? { /*"connectTextFields".postln; */this.connectGUI(nil, connectTF) };

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
				connectKnob: wdgtControllersAndModels.slidersTextConnection.model.value[0],
				connectNumVal: wdgtControllersAndModels.slidersTextConnection.model.value[1],
				bounds: thisBounds,
				setup: this.setup,
				controllersAndModels: wdgtControllersAndModels,
				cvcGui: (midiOscEnv: midiOscEnv),
				persistent: true
			).front;
			thisWdgt.wdgtControllersAndModels.oscDisplay.model.value_(
				wdgtControllersAndModels.oscDisplay.model.value
			).changedKeys(synchKeys);
			thisWdgt.wdgtControllersAndModels.midiOptions.model.value_(
				wdgtControllersAndModels.midiOptions.model.value
			).changedKeys(synchKeys);
			thisWdgt.wdgtControllersAndModels.midiDisplay.model.value_(
				wdgtControllersAndModels.midiDisplay.model.value
			).changedKeys(synchKeys);
			thisWdgt.wdgtControllersAndModels.actions.model.value_(
				wdgtControllersAndModels.actions.model.value
			).changedKeys(synchKeys);
			thisWdgt.wdgtControllersAndModels.calibration.model.value_(
				wdgtControllersAndModels.calibration.model.value
			).changedKeys(synchKeys);
			thisWdgt.parent.onClose_(thisWdgt.parent.onClose.addFunc({
				if(thisWdgt.editor.notNil and:{
					thisWdgt.editor.isClosed.not
				}, { thisWdgt.editor.close });
			}));
			^thisWdgt;
		}, {
			"Either the widget you're trying to reopen hasn't been closed yet or it doesn't even exist.".warn;
		})
	}

	background_ { |color|
		background = color;
		widgetBg.background_(background);
		midiHead.states_([
			[midiHead.states[0][0], midiHead.states[0][1], background]
		]);
		if(midiOscEnv.oscResponder.isNil, {
			oscEditBut.states_([
				[oscEditBut.states[0][0], oscEditBut.states[0][1], background]
			]);
		})
	}

	stringColor_ { |color|
		stringColor = color;
		midiHead.states_([
			[midiHead.states[0][0], stringColor, midiHead.states[0][2]]
		]);
		if(midiOscEnv.oscResponder.isNil, {
			oscEditBut.states_([
				[oscEditBut.states[0][0], stringColor, oscEditBut.states[0][2]]
			])
		})
	}

}
