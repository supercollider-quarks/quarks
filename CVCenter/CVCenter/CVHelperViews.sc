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

CVMidiEditGroup {

	var cview;
	var <midiLearn, <midiHead, <midiSrc, <midiChan, <midiCtrl;

	*new { |parent, bounds, widget, slot|
		// ^super.newCopyArgs(bounds).init(parent, bounds, widget, slot)
		^super.new.init(parent, bounds, widget, slot);
	}

	init { |parentView, bounds, widget, slot|
		var flow, thisBounds, thisSlot;
		var margs, msrc="source", mchan="chan", mctrl="ctrl";
		var wcm, editor, tabIndex;
		var staticTextFont = Font(Font.available("Arial") ? Font.defaultSansFace, 8.5);
		var textFieldFont = Font(Font.available("Courier New") ? Font.defaultSansFace, 7);
		var slotText = "";

		if(bounds.class == Rect, { thisBounds = bounds });
		if(bounds.class == Point, { thisBounds = Rect(0, 0, bounds.x, bounds.y) });

		switch(widget.class,
			CVWidget2D, {
				thisSlot = slot.asSymbol;
				wcm = widget.wdgtControllersAndModels[thisSlot];
				if(widget.editor[thisSlot].notNil and:{ widget.editor[thisSlot].isClosed.not }, {
					editor = widget.editor[thisSlot];
				})
			},
			CVWidgetMS, {
				thisSlot = slot.asInt;
				wcm = widget.wdgtControllersAndModels.slots[thisSlot];
				if(widget.editor[thisSlot].notNil and:{ widget.editor[thisSlot].isClosed.not }, {
					editor = widget.editor[thisSlot];
				})
			},
			{
				wcm = widget.wdgtControllersAndModels;
				if(widget.editor.notNil and:{ widget.editor.isClosed.not }, { editor = widget.editor })
			}
		);

		if(widget.isNil or:{ widget.isKindOf(CVWidget).not }, {
			Error("CVMidiEditGroup is a utility-class to be used with CVWidgets only.").throw
		});

		cview = CompositeView(parentView, thisBounds);
		cview.decorator = flow = FlowLayout(thisBounds, 0@0, 0@0);

		// "parentView, bounds, flow.bounds: %, %, %\n".postf(parentView, bounds, flow.bounds);

		// "flow, bounds: %, %\n".postf(flow, bounds);

		midiHead = Button(cview, flow.bounds.width-(flow.bounds.height/3*1.1)-1@(flow.bounds.height/3*1.1))
			.font_(staticTextFont)
			.action_({ |mh|
				if(widget.class == CVWidgetMS, { tabIndex = 0 }, { tabIndex = 1 });
				if(editor.isNil or:{ editor.isClosed }, {
					editor = CVWidgetEditor(widget, widget.label.states[0][0], tabIndex, thisSlot);
					switch(widget.class,
						CVWidget2D, {
							widget.editor.put(thisSlot, editor);
							widget.guiEnv[thisSlot].editor = editor;
						},
						CVWidgetMS, {
							widget.editor[\editors][thisSlot] = editor;
							widget.guiEnv[\editor][thisSlot] = editor;
						},
						{
							widget.editor = editor;
							widget.guiEnv.editor = editor;
						}
					)
				}, {
					editor.front(tabIndex)
				});
				wcm.oscDisplay.model.value_(
					wcm.oscDisplay.model.value;
				).changedKeys(widget.synchKeys);
				wcm.midiDisplay.model.value_(
					wcm.midiDisplay.model.value
				).changedKeys(widget.synchKeys);
			})
		;

		if(slot.notNil, {
			midiHead.states_([[thisSlot.asString++": MIDI", Color.black, Color.white]]);
		}, {
			midiHead.states_([["MIDI", Color.black, Color.white]]);
		});

		if(GUI.id === \qt, {
			if(slot.notNil, {
				midiHead.mouseEnterAction_({ |mh|
					mh.states_([[thisSlot.asString++": MIDI", Color.white, Color.red]])
				}).mouseLeaveAction_({ |mh|
					mh.states_([[thisSlot.asString++": MIDI", Color.black, Color.white]])
				}).toolTip_("Edit all MIDI-options for slot %:\nmidiMode: %\nmidiMean: %\nmidiResolution: %\nsoftWithin: %\nctrlButtonBank: %".format(
					thisSlot, widget.getMidiMode(thisSlot), widget.getMidiMean(thisSlot), widget.getMidiResolution(thisSlot), widget.getSoftWithin(thisSlot), widget.getCtrlButtonBank(thisSlot)
				))
			}, {
				midiHead.mouseEnterAction_({ |mh|
					mh.states_([["MIDI", Color.white, Color.red]])
				}).mouseLeaveAction_({ |mh|
					mh.states_([["MIDI", Color.black, Color.white]])
				}).toolTip_("Edit all MIDI-options for this widget:\nmidiMode: %\nmidiMean: %\nmidiResolution: %\nsoftWithin: %\nctrlButtonBank: %".format(
					thisSlot, widget.getMidiMode, widget.getMidiMean, widget.getMidiResolution, widget.getSoftWithin, widget.getCtrlButtonBank
				))
			})
		});


		midiLearn = Button(cview, flow.bounds.height/3*1.1@(flow.bounds.height/3*1.1))
			.font_(staticTextFont)
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
							widget.midiConnect(uid: margs[0], chan: margs[1], num: margs[2], slot: thisSlot);
						}, {
							widget.midiConnect(slot: thisSlot);
						})
					},
					0, { widget.midiDisconnect(thisSlot) }
				)
			})
		;

		slot !? { slotText = "at slot % ".format(slot) };

		if(GUI.id !== \cocoa, { midiLearn.toolTip_("Click and and move an arbitrary\nslider on your MIDI-device to\nconnect the widget %to that slider.".format(slotText)) });

		midiSrc = TextField(cview, flow.bounds.width@(flow.bounds.height/3*0.9))
			.font_(textFieldFont)
			.string_(msrc)
			.background_(Color.white)
			.stringColor_(Color.black)
			.action_({ |ms|
				if("^[-+]?[0-9]*$".matchRegexp(ms.string), {
					wcm.midiDisplay.model.value_((
						learn: "C",
						src: ms.string.asInt,
						chan: wcm.midiDisplay.model.value.chan,
						ctrl: wcm.midiDisplay.model.value.ctrl
					)).changedKeys(widget.synchKeys)
				})
			})
			.mouseDownAction_({ |ms|
				ms.stringColor_(Color.red)
			})
			.keyUpAction_({ |ms, char, modifiers, unicode, keycode|
				if(unicode == 13, {
					ms.stringColor_(Color.black)
				})
			})
		;

		slot !? { slotText = " at slot %".format(slot) };

		if(GUI.id !== \cocoa, { midiSrc.toolTip_("Enter your MIDI-device's ID,\nhit 'return' and click 'C' to\nconnect all sliders of your\ndevice to this widget%.".format(slotText)) });

		midiChan = TextField(cview, flow.bounds.width/2@(flow.bounds.height/3*0.9))
			.font_(textFieldFont)
			.string_(mchan)
			.background_(Color.white)
			.stringColor_(Color.black)
			.action_({ |mch|
				if("^[0-9]*$".matchRegexp(mch.string), {
					wcm.midiDisplay.model.value_((
						learn: "C",
						src: wcm.midiDisplay.model.value.src,
						chan: mch.string.asInt,
						ctrl: wcm.midiDisplay.model.value.ctrl
					)).changedKeys(widget.synchKeys)
				})
			})
			.mouseDownAction_({ |mch|
				mch.stringColor_(Color.red)
			})
			.keyDownAction_({ |mch, char, modifiers, unicode, keycode|
				if(unicode == 13, {
					mch.stringColor_(Color.black);
				})
			})
		;

		slot !? { slotText = " at slot %".format(slot) };

		if(GUI.id !== \cocoa, { midiChan.toolTip_("Enter a MIDI-channel, hit 'return'\nand click 'C' to connect all sliders\nin that channel to this widget%.".format(slotText)) });

		midiCtrl = TextField(cview, flow.bounds.width/2@(flow.bounds.height/3*0.9))
			.font_(textFieldFont)
			.string_(mctrl)
			.background_(Color.white)
			.stringColor_(Color.black)
			.action_({ |mctrl|
				if("^[0-9]*$".matchRegexp(mctrl.string), {
					wcm.midiDisplay.model.value_((
						learn: "C",
						src: wcm.midiDisplay.model.value.src,
						chan: wcm.midiDisplay.model.value.chan,
						ctrl: mctrl.string.asInt
					)).changedKeys(widget.synchKeys)
				})
			})
			.mouseDownAction_({ |mctrl|
				mctrl.stringColor_(Color.red)
			})
			.keyDownAction_({ |mctrl, char, modifiers, unicode, keycode|
				if(unicode == 13, {
					mctrl.stringColor_(Color.black);
				})
			})
		;
		slot !? { slotText = " at slot %".format(slot) };

		if(GUI.id !== \cocoa, { midiCtrl.toolTip_("Enter a MIDI-ctrl-nr., hit 'return'\nand click 'C' to connect the slider\nwith that number to this widget%.".format(slotText)) });
	}

	bounds {
		^cview.bounds;
	}

	remove {
		cview.remove;
	}

}