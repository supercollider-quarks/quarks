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

CVWidget2D : CVWidget {

	var <slider2d, <rangeSlider, <numVal, <specBut;
	var <midiHead, <midiLearn, <midiSrc, <midiChan, <midiCtrl, <oscEditBut, <calibBut, <actionsBut;

	*new { |parent, widgetCV, name, connectSliders, connectTextFields, bounds, defaultActions, setup, controllersAndModels, cvcGui, persistent, server|
		^super.newCopyArgs(parent, widgetCV, name, connectSliders, connectTextFields).init(
			bounds,
			defaultActions,
			setup,
			controllersAndModels,
			cvcGui,
			persistent,
			server
		)
	}

	init { |bounds, actions, setupArgs, controllersAndModels, cvcGui, persistent, server|
		var thisXY, thisX, thisY, thisWidth, thisHeight, knobsize, widgetSpecsActions;
		var msrc = "source", mchan = "chan", mctrl = "ctrl", margs;
		var nextY, rightColumnX, oscEditButHeight, right, left;
		var text, tActions, rSliderClass, slider2DClass;
		var tmpWin;

		background ?? { background = Color.white };
		stringColor ?? { stringColor = Color.black };

		synchKeys ?? { synchKeys = [\default] };

		if(Main.versionAtLeast(3, 7), {
			rSliderClass = RangeSlider; slider2DClass = Slider2D;
		}, {
			rSliderClass = QRangeSlider; slider2DClass = QSlider2D;
		});

		if(GUI.id === \qt, {
			CV.viewDictionary[slider2DClass].props_(#[xValue, yValue]);
			CV.viewDictionary[rSliderClass].props_(#[loValue, hiValue]);
		});

		setupArgs !? {
			(setupArgs.class !== Event).if{
				Error( "a setup for a CVWidget2D has to be provided as an Event: (lo: (argname: arg), hi: (argname: arg))!").throw;
			}
		};

		prCalibrate = (lo: true, hi: true);
		prMidiMode = (lo: 0, hi: 0);
		prMidiMean = (lo: 64, hi:64);
		prMidiResolution = (lo: 1, hi: 1);
		prSoftWithin = (lo: 0.1, hi: 0.1);
		prCtrlButtonBank = ();

		this.wdgtActions ?? { this.wdgtActions = (lo: (), hi: ()) };

		guiEnv = (lo: (), hi: ());

		cvcGui !? { isCVCWidget = true };

		if(cvcGui.class == Event and:{ cvcGui.midiOscEnv.notNil }, { midiOscEnv = cvcGui.midiOscEnv }, { midiOscEnv = () });
		#[lo, hi].do({ |hilo|
			midiOscEnv[hilo] ?? { midiOscEnv.put(hilo, ()) };
			midiOscEnv[hilo].oscMapping ?? { midiOscEnv[hilo].oscMapping = \linlin };
		});

		name ?? { name = "2D" };
		// wdgtInfo = name.asString;

		// [parent, widgetCV, name].postln;

		if(widgetCV.isNil, {
			// "widgetCV.isNil".postln;
			widgetCV = ();
			widgetCV.lo = CV.new; widgetCV.hi = CV.new;
		}, {
			#[lo, hi].do({ |hilo| widgetCV[hilo] ?? { widgetCV.put(hilo, nil.asSpec) } });
		});

		numVal = (); specBut = ();
		midiHead = (); midiLearn = (); midiSrc = (); midiChan = (); midiCtrl = ();
		oscEditBut = (); calibBut = (); actionsBut = ();
		editor = ();

		#[lo, hi].do(this.initControllersAndModels(controllersAndModels, _));

		setupArgs !? {
			#[lo, hi].do({ |slot|
				setupArgs[slot] !? { setupArgs[slot][\midiMode] !? { this.setMidiMode(setupArgs[slot][\midiMode], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\midiResolution] !? { this.setMidiResolution(setupArgs[slot][\midiResolution], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\midiMean] !? { this.setMidiMean(setupArgs[slot][\midiMean], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\ctrlButtonBank] !? { this.setCtrlButtonBank(setupArgs[slot][\ctrlButtonBank], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\softWithin] !? { this.setSoftWithin(setupArgs[slot][\softWithin], slot) }};
				setupArgs[slot] !? { setupArgs[slot][\calibrate] !? { this.setCalibrate(setupArgs[slot][\calibrate], slot) }};
			})
		};

		actions !? {
			if(actions.class !== Event, {
				Error("Please provide actions in the following way: (lo: action1, hi: action2)").throw;
			}, {
				actions[\lo] !? { this.addAction(\default, actions[\lo], \lo) };
				actions[\hi] !? { this.addAction(\default, actions[\hi], \hi) };
			})
		};

		if(bounds.isNil, {
			thisXY = 7@0;
			thisX = 50; thisY = 50;
			thisWidth = 122;
			thisHeight = 166;
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
				#[lo, hi].do({ |hilo|
					if(editor[hilo].notNil, {
						if(editor[hilo].isClosed.not, {
							editor[hilo].close(hilo);
						}, {
							if(AbstractCVWidgetEditor.allEditors.notNil and:{
								AbstractCVWidgetEditor.allEditors[name.asSymbol].notNil
							}, {
								AbstractCVWidgetEditor.allEditors[name.asSymbol].removeAt(hilo);
								if(AbstractCVWidgetEditor.allEditors[name.asSymbol].isEmpty, {
									AbstractCVWidgetEditor.allEditors.removeAt(name.asSymbol);
								})
							})
						})
					})
				})
			});
			if(persistent == false or:{ persistent.isNil }, {
				parent.onClose_(parent.onClose.addFunc({
					#[lo, hi].do({ |hilo|
						this.oscDisconnect(hilo);
						this.midiDisconnect(hilo);
						// midiOscEnv[hilo].oscResponder !? { midiOscEnv[hilo].oscResponder.remove };
						// midiOscEnv[hilo].cc !? { midiOscEnv[hilo].cc.remove };
						wdgtControllersAndModels[hilo].do({ |mc| mc.isKindOf(SimpleController).if{ mc.controller.remove } });
					})
				}))
			}, {
				isPersistent = true;
			})
		};

		persistent !? { if(persistent, { isPersistent = true }) };

		widgetBg = CompositeView(parent, Rect(thisXY.x, thisXY.y, thisWidth, thisHeight))
			.background_(Color.white)
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
			});
		});

		nextY = thisXY.y+1+label.bounds.height;

		slider2d = Slider2D(parent, Rect(thisXY.x+1, nextY, thisWidth-42, thisWidth-42))
		// .canFocus_(false)
			.background_(Color.white)
			.knobColor_(Color.red)
			.focusColor_(Color.green)
		;

		activeSliderB = Button(parent, Rect(thisXY.x+thisWidth-50, thisXY.y+slider2d.bounds.height+7, 7, 7))
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

		nextY = nextY+slider2d.bounds.height;

		rangeSlider = RangeSlider(parent, Rect(
			thisXY.x+1,
			nextY,
			thisWidth-42,
			15
		))
			// .canFocus_(false)
			.focusColor_(Color.green)
			.background_(Color.white)
		;
		nextY = nextY+this.rangeSlider.bounds.height;

		numVal.lo = NumberBox(parent);
		numVal.hi = NumberBox(parent);

		[numVal.lo, [thisXY.x+1, widgetCV.lo], numVal.hi, [thisXY.x+(thisWidth-44/2), widgetCV.hi]].pairsDo({ |k, v|
			k.bounds_(Rect(
				v[0],
				nextY,
				this.rangeSlider.bounds.width/2,
				15
			));
			k.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9.5)).focusColor_(Color.green);
		});

		activeTextB = Button(parent, Rect(thisXY.x+thisWidth-50, nextY+7, 7, 7))
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

		specBut.lo = Button(parent)
			.action_({ |btn|
				if(editor.lo.isNil or:{ editor.lo.isClosed }, {
					editor.lo = CVWidgetEditor(this, 0, \lo);
					guiEnv.lo.editor = editor.lo;
				}, {
					editor.lo.front(0)
				});
				wdgtControllersAndModels.lo.oscConnection.model.value_(
					wdgtControllersAndModels.lo.oscConnection.model.value;
				).changedKeys(synchKeys);
				wdgtControllersAndModels.lo.midiConnection.model.value_(
					wdgtControllersAndModels.lo.midiConnection.model.value
				).changedKeys(synchKeys);
			})
		;
		specBut.hi = Button(parent)
			.action_({ |btn|
				if(editor.hi.isNil or:{ editor.hi.isClosed }, {
					editor.hi = CVWidgetEditor(this, 0, \hi);
					guiEnv.hi.editor = editor.hi;
				}, {
					editor.hi.front(0)
				});
				wdgtControllersAndModels.hi.oscDisplay.model.value_(
					wdgtControllersAndModels.hi.oscDisplay.model.value;
				).changedKeys(synchKeys);
				wdgtControllersAndModels.hi.midiDisplay.model.value_(
					wdgtControllersAndModels.hi.midiDisplay.model.value
				).changedKeys(synchKeys);
			})
		;

		midiHead.lo = Button(parent)
			.action_({ |btn|
				if(editor.lo.isNil or:{ editor.lo.isClosed }, {
					editor.hi = CVWidgetEditor(this, 1, \lo);
					guiEnv.lo.editor = editor.lo;
				}, {
					editor.lo.front(1)
				});
				wdgtControllersAndModels.lo.oscDisplay.model.value_(
					wdgtControllersAndModels.lo.oscDisplay.model.value;
				).changedKeys(synchKeys);
				wdgtControllersAndModels.lo.midiDisplay.model.value_(
					wdgtControllersAndModels.lo.midiDisplay.model.value
				).changedKeys(synchKeys);
			})
		;
		midiHead.hi = Button(parent)
			.action_({ |btn|
				if(editor.hi.isNil or:{ editor.hi.isClosed }, {
					editor.hi = CVWidgetEditor(this, 1, \hi);
					guiEnv.hi.editor = editor.hi;
				}, {
					editor.hi.front(1)
				});
				wdgtControllersAndModels.hi.oscConnection.model.value_(
					wdgtControllersAndModels.hi.oscConnection.model.value;
				).changedKeys(synchKeys);
				wdgtControllersAndModels.hi.midiConnection.model.value_(
					wdgtControllersAndModels.hi.midiConnection.model.value
				).changedKeys(synchKeys);
			})
		;
		midiLearn.lo = Button(parent);
		midiLearn.hi = Button(parent);
		midiSrc.lo = TextField(parent);
		midiSrc.hi = TextField(parent);
		midiChan.lo = TextField(parent);
		midiChan.hi = TextField(parent);
		midiCtrl.lo = TextField(parent);
		midiCtrl.hi = TextField(parent);

		nextY = thisXY.y+1+label.bounds.height;
		rightColumnX = thisXY.x+slider2d.bounds.width+1;

		[specBut.hi, [nextY, \hi], specBut.lo, [nextY+52, \lo]].pairsDo({ |k, v|
			k.bounds_(Rect(rightColumnX, v[0], thisWidth-slider2d.bounds.width-2, 13))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 8))
			.focusColor_(Color.green)
			.states_([["edit Spec", Color.white, Color(1.0, 0.3)]])
		});

		if(GUI.id !== \cocoa, {
			specBut.pairsDo({ |k, v|
				v.toolTip_("Edit the CV's ControlSpec in '"++k++"':\n"++(this.getSpec(k).asCompileString))
			})
		});

		nextY = nextY+14;

		[midiHead.hi, nextY, midiHead.lo, nextY+52].pairsDo({ |k, v|
			k.bounds_(Rect(rightColumnX, v, thisWidth-slider2d.bounds.width-2-12, 13))
				.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 7))
				.focusColor_(Color.green)
				.states_([["MIDI", stringColor, background]])
			;

			if(GUI.id === \qt, {
				k.mouseEnterAction_({ |mb|
					mb.states_([["MIDI", Color.white, Color.red]])
				}).mouseLeaveAction_({ |mb|
					mb.states_([["MIDI", stringColor, background]])
				})
			})
		});

		if(GUI.id !== \cocoa, {
			midiHead.pairsDo({ |k, v|
				v.toolTip_("Edit all MIDI-options\nof this widget's '"++k++"' slot.\nmidiMode:"+this.getMidiMode(k)++"\nmidiMean:"+this.getMidiMean(k)++"\nmidiResolution:"+this.getMidiResolution(k)++"\nsoftWithin:"+this.getSoftWithin(k)++"\nctrlButtonBank:"+this.getCtrlButtonBank(k))
			})
		});

		[midiLearn.hi, [\hi, nextY], midiLearn.lo, [\lo, nextY+52]].pairsDo({ |k, v|
			k.bounds_(Rect(rightColumnX+midiHead.lo.bounds.width, v[1], 12, 13))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 7))
			.focusColor_(Color.green)
			.states_([
				["L", Color.white, Color.blue],
				["X", Color.white, Color.red]
			])
			.action_({ |ml|
				ml.value.switch(
					1, {
						margs = [
							[midiSrc[v[0]].string, msrc],
							[midiChan[v[0]].string, mchan],
							[midiCtrl[v[0]].string, mctrl]
						].collect({ |pair| if(pair[0] != pair[1], { pair[0].asInt }, { nil }) });
						if(margs.select({ |i| i.notNil }).size > 0, {
							this.midiConnect(uid: margs[0], chan: margs[1], num: margs[2], slot: v[0]);
						}, {
							this.midiConnect(slot: v[0]);
						})
					},
					0, { this.midiDisconnect(v[0]) }
				)
			})
		});

		if(GUI.id !== \cocoa, {
			midiLearn.pairsDo({ |k, v|
				v.toolTip_("Click and and move an arbitrary\nslider on your MIDI-device to\nconnect the widget at '"++k++"' to that slider.")
			})
		});

		nextY = nextY+13;

		[midiSrc.hi, [\hi, nextY], midiSrc.lo, [\lo, nextY+52]].pairsDo({ |k, v|
			k.bounds_(Rect(rightColumnX, v[1], thisWidth-slider2d.bounds.width-2, 13))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 8.5))
			.focusColor_(Color.green)
			.string_("source")
			.background_(Color.white)
			.stringColor_(Color.black)
			.action_({ |tf|
				if("^[-+]?[0-9]*$".matchRegexp(tf.string), {
					wdgtControllersAndModels[v[0]].midiDisplay.model.value_((
						learn: "C",
						src: tf.string,
						chan: wdgtControllersAndModels[v[0]].midiDisplay.model.value.chan,
						ctrl: wdgtControllersAndModels[v[0]].midiDisplay.model.value.ctrl
					)).changedKeys(synchKeys)
				})
			})
			.mouseDownAction_({ |tf|
				tf.stringColor_(Color.red)
			})
			.keyUpAction_({ |tf, char, modifiers, unicode, keycode|
				if(unicode == 13, {
					tf.stringColor_(Color.black);
				})
			})
		});

		if(GUI.id !== \cocoa, {
			midiSrc.pairsDo({ |k, v|
				v.toolTip_("Enter your MIDI-device's ID,\nhit 'return' and click 'C' to\nconnect all sliders of your\ndevice to the widget's '"++k++"' slot")
			})
		});

		nextY = nextY+13;

		[midiChan.hi, [\hi, nextY], midiChan.lo, [\lo, nextY+52]].pairsDo({ |k, v|
			k.bounds_(Rect(rightColumnX, v[1], thisWidth-slider2d.bounds.width-2-25, 13))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 8.5))
			.focusColor_(Color.green)
			.string_("chan")
			.background_(Color.white)
			.stringColor_(Color.black)
			.action_({ |tf|
				if("^[0-9]*$".matchRegexp(tf.string), {
					wdgtControllersAndModels[v[0]].midiDisplay.model.value_((
						learn: "C",
						src: wdgtControllersAndModels[v[0]].midiDisplay.model.value.src,
						chan: tf.string,
						ctrl: wdgtControllersAndModels[v[0]].midiDisplay.model.value.ctrl
					)).changedKeys(synchKeys)
				})
			})
			.mouseDownAction_({ |tf|
				tf.stringColor_(Color.red)
			})
			.keyUpAction_({ |tf, char, modifiers, unicode, keycode|
				if(unicode == 13, {
					tf.stringColor_(Color.black);
				})
			})
		});

		if(GUI.id !== \cocoa, {
			midiChan.pairsDo({ |k, v|
				v.toolTip_("Enter a MIDI-channel, hit 'return'\nand click 'C' to connect all sliders\nin that channel to this widget's '"++k++"' slot.")
			})
		});

		[midiCtrl.hi, [\hi, nextY], midiCtrl.lo, [\lo, nextY+52]].pairsDo({ |k, v|
			k.bounds_(Rect(rightColumnX+midiChan[v[0]].bounds.width, v[1], 25, 13))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 8.5))
			.focusColor_(Color.green)
			.string_("ctrl")
			.background_(Color.white)
			.stringColor_(Color.black)
			.action_({ |tf|
				if("^[0-9]*$".matchRegexp(tf.string), {
					wdgtControllersAndModels[v[0]].midiDisplay.model.value_((
						learn: "C",
						src: wdgtControllersAndModels[v[0]].midiDisplay.model.value.src,
						chan: wdgtControllersAndModels[v[0]].midiDisplay.model.value.chan,
						ctrl: tf.string
					)).changedKeys(synchKeys)
				})
			})
			.mouseDownAction_({ |tf|
				tf.stringColor_(Color.red)
			})
			.keyUpAction_({ |tf, char, modifiers, unicode, keycode|
				if(unicode == 13, {
					tf.stringColor_(Color.black);
				})
			})
		});

		if(GUI.id !== \cocoa, {
			midiCtrl.pairsDo({ |k, v|
				v.toolTip_("Enter a MIDI-ctrl-nr., hit 'return'\nand click 'C' to connect the slider\nwith that number to this widget's '"++k++"' slot")
			})
		});

		if(
			(left = slider2d.bounds.height
			+rangeSlider.bounds.height
			+numVal.lo.bounds.height)
			>=
			(right = specBut.lo.bounds.height
			+midiLearn.lo.bounds.height
			+midiSrc.lo.bounds.height
			+midiChan.lo.bounds.height*2),
		{
			nextY =
			label.bounds.top
			+label.bounds.height
			+left;
			oscEditButHeight = thisHeight-left-32;
		}, {
			nextY =
			label.bounds.top
			+label.bounds.height
			+right;
			oscEditButHeight = thisHeight-right-32;
		});

		oscEditBut.lo = Button(parent);
		oscEditBut.hi = Button(parent);
		calibBut.lo = Button(parent);
		calibBut.hi = Button(parent);
		actionsBut.lo = Button(parent);
		actionsBut.hi = Button(parent);

		[oscEditBut.lo, [\lo, thisXY.x+1], oscEditBut.hi, [\hi, thisXY.x+(thisWidth/2)]].pairsDo({ |k, v|
			k.bounds_(Rect(v[1], nextY, thisWidth/2-1, oscEditButHeight))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 8.5))
			.focusColor_(Color.green)
			.states_([
				["edit OSC", stringColor, background]
			])
			.action_({ |oscb|
				if(editor[v[0]].isNil or:{ editor[v[0]].isClosed }, {
					editor.put(v[0], CVWidgetEditor(this, 2, v[0]));
					guiEnv[v[0]].editor = editor[v[0]];
				}, {
					editor[v[0]].front(2)
				});
				editor[v[0]].calibNumBoxes !? {
					wdgtControllersAndModels[v[0]].mapConstrainterLo.connect(editor[v[0]].calibNumBoxes.lo);
					editor[v[0]].calibNumBoxes.lo.value_(wdgtControllersAndModels[v[0]].oscInputRange.model.value[0]);
					wdgtControllersAndModels[v[0]].mapConstrainterHi.connect(editor[v[0]].calibNumBoxes.hi);
					editor[v[0]].calibNumBoxes.hi.value_(wdgtControllersAndModels[v[0]].oscInputRange.model.value[1]);
				};
				wdgtControllersAndModels[v[0]].oscDisplay.model.value_(
					wdgtControllersAndModels[v[0]].oscDisplay.model.value;
				).changedKeys(synchKeys);
				wdgtControllersAndModels[v[0]].midiDisplay.model.value_(
					wdgtControllersAndModels[v[0]].midiDisplay.model.value
				).changedKeys(synchKeys);
			});

			if(GUI.id === \qt, {
				k.mouseEnterAction_({ |oscb|
					if(wdgtControllersAndModels[v[0]].oscConnection.model.value === false, {
						oscb.states_([["edit OSC", Color.white, Color.cyan(0.5)]]);
					})
				}).mouseLeaveAction_({ |oscb|
					if(wdgtControllersAndModels[v[0]].oscConnection.model.value === false, {
						oscb.states_([["edit OSC", stringColor, background]])
					})
				})
			})
		});

		if(GUI.id !== \cocoa, {
			oscEditBut.pairsDo({ |k, v|
				v.toolTip_("no OSC-responder present in '"++k++"'.\nClick to edit.")
			})
		});

		[calibBut.lo, [\lo, thisXY.x+1], calibBut.hi, [\hi, thisXY.x+(thisWidth/2)]].pairsDo({ |k, v|
			k.bounds_(Rect(v[1]+oscEditBut[v[0]].bounds.width-10, nextY+oscEditBut.lo.bounds.height-10, 10, 10))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.focusColor_(Color.green)
			.states_([
				["", Color.black, Color.green],
				["", Color.white, Color.red]
			])
			.action_({ |cb|
				switch(cb.value,
					0, { this.setCalibrate(true, v[0]) },
					1, { this.setCalibrate(false, v[0]) }
				)
			})
		});

		if(GUI.id !== \cocoa, {
			calibBut.pairsDo({ |k, v|
				if(this.getCalibrate(k), {
					text = "Calibration in '"++k++"' is active.\nClick to dectivate.";
				}, {
					text = "Calibration in '"++k++"' is inactive.\nClick to activate.";
				});
				v.toolTip_(text);
			})
		});

		nextY = nextY+oscEditBut.lo.bounds.height;
		// nextY = nextY+calibBut.lo.bounds.height;

		[actionsBut.lo, [\lo, thisXY.x+1], actionsBut.hi, [\hi, thisXY.x+(thisWidth/2)]].pairsDo({ |k, v|

			k.bounds_(Rect(v[1], nextY, thisWidth/2-1, 15))
			.font_(Font(Font.available("Arial") ? Font.defaultSansFace, 9))
			.focusColor_(Color.green)
			.states_([
				["actions ("++this.wdgtActions[v[0]].select({ |v| v.asArray[0][1] == true }).size++"/"++this.wdgtActions[v[0]].size++")", Color(0.08, 0.09, 0.14), Color(0.32, 0.67, 0.76)]
			])
			.action_({ |ab|
				if(editor[v[0]].isNil or:{ editor[v[0]].isClosed }, {
					editor.put(v[0], CVWidgetEditor(this, 3, v[0]));
					guiEnv[v[0]].editor = editor[v[0]];
				}, {
					editor[v[0]].front(3)
				});
			})
		});

		if(GUI.id !== \cocoa, {
			actionsBut.pairsDo({ |k, v|
				text = [];
				text = text.add(this.wdgtActions[k].size);
				text = text.add(this.wdgtActions[k].select({ |vv| vv.asArray[0][1] == true }).size);
				if(text[0] == 1, { tActions = "action" }, { tActions = "actions" });
				v.toolTip_("% of % % active in '%'.\nClick to edit.".format(text[1], text[0], tActions));
			})
		});

		#[lo, hi].do({ |slot| if(prCalibrate[slot], { calibBut[slot].value_(0) }, { calibBut[slot].value_(1) }) });

		visibleGuiEls = [
			slider2d,
			rangeSlider,
			activeSliderB,
			numVal.lo, numVal.hi,
			activeTextB,
			specBut.lo, specBut.hi,
			midiHead.lo, midiHead.hi,
			midiLearn.lo, midiLearn.hi,
			midiSrc.lo, midiSrc.hi,
			midiChan.lo, midiChan.hi,
			midiCtrl.lo, midiCtrl.hi,
			oscEditBut.lo, oscEditBut.hi,
			calibBut.lo, calibBut.hi,
			actionsBut.lo, actionsBut.hi
		];

		allGuiEls = [
			widgetBg,
			label,
			nameField,
			slider2d,
			rangeSlider,
			activeSliderB,
			numVal.lo, numVal.hi,
			activeTextB,
			specBut.lo, specBut.hi,
			midiHead.lo, midiHead.hi,
			midiLearn.lo, midiLearn.hi,
			midiSrc.lo, midiSrc.hi,
			midiChan.lo, midiChan.hi,
			midiCtrl.lo, midiCtrl.hi,
			oscEditBut.lo, oscEditBut.hi,
			calibBut.lo, calibBut.hi,
			actionsBut.lo, actionsBut.hi
		];

		focusElements = allGuiEls.copy.removeAll([
			widgetBg, nameField, calibBut.lo, calibBut.hi, activeSliderB, activeTextB
		]);

		#[lo, hi].do({ |slot|
			guiEnv[slot] = (
				editor: editor[slot],
				calibBut: calibBut[slot],
				slider2d: slider2d,
				rangeSlider: rangeSlider,
				specBut: specBut[slot],
				oscEditBut: oscEditBut[slot],
				calibBut: calibBut[slot],
				actionsBut: actionsBut[slot],
				midiHead: midiHead[slot],
				midiSrc: midiSrc[slot],
				midiChan: midiChan[slot],
				midiCtrl: midiCtrl[slot],
				midiLearn: midiLearn[slot]
			);
			this.initControllerActions(slot);
		});

		connectS !? { this.connectGUI(connectS, nil) };
		connectTF !? { this.connectGUI(nil, connectTF) };

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
				cvs: [widgetCV.lo, widgetCV.hi],
				name: oldName,
				connectSliders: wdgtControllersAndModels.slidersTextConnection.model.value[0],
				connectTextFields: wdgtControllersAndModels.slidersTextConnection.model.value[1],
				bounds: thisBounds,
				setup: this.setup,
				controllersAndModels: wdgtControllersAndModels,
				cvcGui: (midiOscEnv: midiOscEnv),
				persistent: true
			).front;
			#[lo, hi].do({ |hilo|
				thisWdgt.wdgtControllersAndModels[hilo].oscDisplay.model.value_(
					wdgtControllersAndModels[hilo].oscDisplay.model.value
				).changedKeys(synchKeys);
				thisWdgt.wdgtControllersAndModels[hilo].midiOptions.model.value_(
					wdgtControllersAndModels[hilo].midiOptions.model.value
				).changedKeys(synchKeys);
				thisWdgt.wdgtControllersAndModels[hilo].midiDisplay.model.value_(
					wdgtControllersAndModels[hilo].midiDisplay.model.value
				).changedKeys(synchKeys);
				thisWdgt.wdgtControllersAndModels[hilo].actions.model.value_(
					wdgtControllersAndModels[hilo].actions.model.value
				).changedKeys(synchKeys);
				thisWdgt.wdgtControllersAndModels[hilo].calibration.model.value_(
					wdgtControllersAndModels[hilo].calibration.model.value
				).changedKeys(synchKeys);
			});
			thisWdgt.parent.onClose_(thisWdgt.parent.onClose.addFunc({
				#[lo, hi].do({ |hilo|
					if(thisWdgt.editor[hilo].notNil and:{
						thisWdgt.editor[hilo].isClosed.not
					}, { thisWdgt.editor[hilo].close });
				})
			}));
			^thisWdgt;
		}, {
			"Either the widget you're trying to reopen hasn't been closed yet or it doesn't even exist.".warn;
		})
	}

	background_ { |color|
		background = color;
		widgetBg.background_(background);
		#[lo, hi].do({ |slot|
			midiHead[slot].states_([
				[midiHead[slot].states[0][0], midiHead[slot].states[0][1], background]
			]);
			if(midiOscEnv[slot].oscResponder.isNil, {
				oscEditBut[slot].states_([
					[oscEditBut[slot].states[0][0], oscEditBut[slot].states[0][1], background]
				])
			})
		})
	}

	stringColor_ { |color|
		stringColor = color;
		#[lo, hi].do({ |slot|
			midiHead[slot].states_([
				[midiHead[slot].states[0][0], stringColor, midiHead[slot].states[0][2]]
			]);
			if(midiOscEnv[slot].oscResponder.isNil, {
				oscEditBut[slot].states_([
					[oscEditBut[slot].states[0][0], stringColor, oscEditBut[slot].states[0][2]]
				])
			})
		})
	}

}