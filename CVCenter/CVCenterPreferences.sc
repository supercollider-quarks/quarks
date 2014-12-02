/* (c) 2010-2012 Stefan Nussbaumer */
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

	*dialog {
		var labelColors, labelStringColors, flow;
		var staticTextFont, staticTextColor, textFieldFont, textFieldFontColor, textFieldBg;
		var saveGuiPosition, leftText, left, topText, top, widthText, width, heightText, height;
		var saveClassVars, removeResponders;
		var saveMidiMode, saveMidiResolution, saveCtrlButtonBank, saveMidiMean, saveSoftWithin;
		var textMidiMode, textMidiResolution, textCtrlButtonBank, textMidiMean, textSoftWithin;
		var prefSaveGuiProps, buildCheckbox, buildNumTextBox, uView, vHeight;
		var cvcBounds, propsText, classVarsText;
		var fFact, specialHeight;
		var prefs, rect;

		prefs = this.readPreferences;

		if(GUI.id === \cocoa, { fFact = 0.9 }, { fFact = 1 });

		staticTextFont = Font("Arial", 12 * fFact);
		staticTextColor = Color(0.2, 0.2, 0.2);
		textFieldFont = Font("Andale Mono", 12);
		textFieldFontColor = Color.black;
		textFieldBg = Color.white;

		buildCheckbox = { |active|
			var cBox;
			if(GUI.id === \cocoa, {
				cBox = Button(window.view, 15@15)
					.states_([
						["", Color.white, Color.white],
						["X", Color.black, Color.white],
					])
					.font_(Font(Font.defaultSansFace, 10, true))
				;
				if(active, { cBox.value_(1) }, { cBox.value_(0) });
			}, {
				cBox = \CheckBox.asClass.new(window.view, 15@15).value_(active);
			});
			cBox;
		};

		buildNumTextBox = { |val, kind, width, height, clip|
			var ntBox;
			case
				{ kind === \text } {
					ntBox = TextField(window.view, (width ?? { 30 }) @ (height ?? { 20 })).string_(val);
				}
				{ kind === \num } {
					ntBox = NumberBox(window.view, (width ?? { 30 }) @ ( height ?? { 20 }))
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
				Window.screenBounds.height/2-180,
				498, 360
			)).front;

			window.view.decorator = flow = FlowLayout(window.view.bounds, 7@7, 3@3);

			uView = UserView(window.view, flow.bounds.width-20@50)
				.background_(Color(0.95, 0.95, 0.95))
				.enabled_(false)
			;

			flow.nextLine.shift(5, -50);

			saveGuiPosition = PopUpMenu(window.view, flow.bounds.width-30@20)
				.items_([
					"No specific settings for GUI-properties",
					"Remember GUI-properties on shutdown / window-close",
					"Remember GUI-properties as set below"
				])
			.value_(prefs !? { prefs[\saveGuiProperties] } ?? { 0 })
				.font_(staticTextFont)
			;

			leftText = StaticText(window.view, flow.bounds.width/10@20)
				.string_("left: ")
				.font_(staticTextFont)
				.align_(\right)
			;

			flow.shift(0, 0);

			left = buildNumTextBox.(prefs !? {
				prefs[\guiProperties] !? {
					prefs[\guiProperties].left ?? { cvcBounds.left }
				}
			}, kind: \text, width: 60);

			flow.shift(0, 0);

			topText = StaticText(window.view, flow.bounds.width/10@20)
				.string_("top: ")
				.font_(staticTextFont)
				.align_(\right)
			;

			top = buildNumTextBox.(prefs !? {
				prefs[\guiProperties] !? {
					prefs[\guiProperties].top ?? { cvcBounds.top }
				}
			}, kind: \text, width: 60);

			flow.shift(0, 0);

			widthText = StaticText(window.view, flow.bounds.width/10@20)
				.string_("width: ")
				.font_(staticTextFont)
				.align_(\right)
			;

			width = buildNumTextBox.(prefs !? {
				prefs[\guiProperties] !? {
					prefs[\guiProperties].width ?? { cvcBounds.width }
				}
			}, kind: \text, width: 60);

			flow.shift(0, 0);

			heightText = StaticText(window.view, flow.bounds.width/10@20)
				.string_("height: ")
				.font_(staticTextFont)
				.align_(\right)
			;

			height = buildNumTextBox.(prefs !? {
				prefs[\guiProperties] !? {
					prefs[\guiProperties].height ?? { cvcBounds.height }
				}
			}, kind: \text, width: 60);

			if(saveGuiPosition.value == 0 or:{ saveGuiPosition.value == 1 }, {
				[leftText, topText, widthText, heightText].do(_.stringColor_(Color(0.7, 0.7, 0.7, 0.7)));
				[left, top, width, height].do(_.enabled_(false));
				uView.background_(Color(0.95, 0.95, 0.95));
			}, {
				[leftText, topText, widthText, heightText].do(_.stringColor_(Color.black));
				[left, top, width, height].do(_.enabled_(true));
			});

			saveGuiPosition.action_({ |dd|
				if(dd.value == 0 or:{ dd.value == 1 }, {
					[leftText, topText, widthText, heightText].do(_.stringColor_(Color(0.7, 0.7, 0.7, 0.7)));
					[left, top, width, height].do(_.enabled_(false));
					uView.background_(Color(0.95, 0.95, 0.95));
				}, {
					[leftText, topText, widthText, heightText].do(_.stringColor_(Color.black));
					[left, top, width, height].do(_.enabled_(true));
				})
			});

			flow.nextLine.shift(0, 6);

			if(GUI.id ===\cocoa, { vHeight = 226 }, { vHeight = 220 });

			UserView(window.view, flow.bounds.width-20@vHeight)
				.background_(Color(0.95, 0.95, 0.95))
				.enabled_(false)
			;

			flow.nextLine.shift(5, vHeight.neg);

			if(prefs.notNil, {
				saveClassVars = buildCheckbox.(prefs[\saveClassVars]);
			}, {
				saveClassVars = buildCheckbox.(false);
			});

			flow.shift(5, 1);

			classVarsText = StaticText(window.view, flow.bounds.width-100@20)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Remember CVCenter's classvar-values on shutdown.")
			;

			if(GUI.id !== \cocoa, {
				classVarsText.toolTip_(
					"Selecting this option will make CVCenter remember the current\nvalues for midiMode, midiResolution, midiMean, softWithin and\nctrlButtonBank. These can also be set by entering appropriate\nvalues in the following text-boxes. If this option is not selected\nany of the following values will only be remembered until the\nnext library recompilation.\nNote also that these values get overridden by the corresponding\nsettings in a CVWidget.\nFor more information please have a look at the regarding\nsections in CVCenter's helpfile."
				);
				saveClassVars.toolTip_("Select this option to make CVCenter remember the current values\nof its classvars midiMode, midiResolution, midiMean, softWithin,\nctrlButtonBank on shutdown resp. startup.")
			});

			flow.nextLine.shift(28, 0);

			saveMidiMode = buildNumTextBox.(prefs !? {
				prefs[\midiMode] } ?? { CVCenter.midiMode }, \text
			);

			flow.shift(5, 2);

			textMidiMode = StaticText(window.view, flow.bounds.width-100@20)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set CVCenter's midi-mode (0 or 1).")
			;

			if(GUI.id !== \cocoa, {
				saveMidiMode.toolTip_(
					"Either 0 (hardware-slider output 0-127)\nor 1 (in-/decremental output)."
				)
			});

			flow.nextLine.shift(28, 0);

			saveMidiResolution = buildNumTextBox.(prefs !? {
				prefs[\midiResolution] } ?? { CVCenter.midiResolution }, \text
			);

			flow.shift(5, 2);

			textMidiResolution = StaticText(window.view, flow.bounds.width-100@20)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set CVCenter's midi-resolution. Applies only if midi-mode is 1.")
			;

			if(GUI.id !== \cocoa, {
				saveMidiResolution.toolTip_(
					"A floating point value representing the slider's resolution.\n1 has proven to be a sensible default. Smaller values mean\na higher resolution. Applies only if midiMode is set to 1."
				)
			});

			flow.nextLine.shift(28, 0);

			saveMidiMean = buildNumTextBox.(prefs !? {
				prefs[\midiMean] } ?? { CVCenter.midiMean }, \text
			);

			flow.shift(5, 2);

			textMidiMean = StaticText(window.view, flow.bounds.width-100@30)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set CVCenter's midi-mean: the default-output of your MIDI-device's\nsliders in neutral position. Applies only if midi-mode is 1.")
			;

			if(GUI.id !== \cocoa, {
				saveMidiMean.toolTip_(
					"The output of your device's sliders in neutral position.\nOnly needed if output != 0 and midiMode is set to 1."
				)
			});

			flow.nextLine.shift(28, 0);

			saveSoftWithin = buildNumTextBox.(prefs !? {
				prefs[\softWithin] } ?? { CVCenter.softWithin }, \text
			);

			flow.shift(5, 2);

			textSoftWithin = StaticText(window.view, flow.bounds.width-100@42)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set the soft-within threshold: the widget will only respond if the\ncurrent MIDI-output is within the widget's current value +/- threshold.\nApplies only if midi-mode is 0.");

			if(GUI.id !== \cocoa, {
				saveSoftWithin.toolTip_(
					"Set an arbitrary floating point value. Recomended: 0.1.\nApplies only if midiMode is set to 0."
				)
			});

			flow.nextLine.shift(28, 0);

			saveCtrlButtonBank = buildNumTextBox.(prefs !? {
				prefs[\ctrlButtonBank] } ?? { CVCenter.ctrlButtonBank }, \text
			);

			flow.shift(5, 2);

			if(GUI.id === \cocoa, { specialHeight = 60 }, { specialHeight = 54 });

			textCtrlButtonBank = StaticText(window.view, flow.bounds.width-100@specialHeight)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Set the number of sliders on in one bank of your MIDI-device.\nSetting this number will display the selected slider in a widget not as\na single number but rather as combination of the selected bank and\nthe slider number (e.g.: 4:3 means bank nr. 4, slider nr. 3)")
			;

			saveClassVars.action_({ |b|
				if(b.value == false or:{ b.value == 0 }, {
					[saveMidiMode, saveMidiMean, saveMidiResolution, saveSoftWithin, saveCtrlButtonBank].do(
						_.enabled_(false)
					);
					[textMidiMode, textMidiMean, textMidiResolution, textSoftWithin, textCtrlButtonBank].do(
						_.stringColor_(Color(0.7, 0.7, 0.7))
					)
				});
				if(b.value == true or:{ b.value == 1 }, {
					[saveMidiMode, saveMidiMean, saveMidiResolution, saveSoftWithin, saveCtrlButtonBank].do(
						_.enabled_(true)
					);
					[textMidiMode, textMidiMean, textMidiResolution, textSoftWithin, textCtrlButtonBank].do(
						_.stringColor_(Color.black)
					)
				})
			});

			if(saveClassVars.value == false or:{ saveClassVars.value == 0 }, {
				[saveMidiMode, saveMidiMean, saveMidiResolution, saveSoftWithin, saveCtrlButtonBank].do(
					_.enabled_(false)
				);
				[textMidiMode, textMidiMean, textMidiResolution, textSoftWithin, textCtrlButtonBank].do(
					_.stringColor_(Color(0.7, 0.7, 0.7))
				)
			});
			if(saveClassVars.value == true or:{ saveClassVars.value == 1 }, {
				[saveMidiMode, saveMidiMean, saveMidiResolution, saveSoftWithin, saveCtrlButtonBank].do(
					_.enabled_(true)
				);
				[textMidiMode, textMidiMean, textMidiResolution, textSoftWithin, textCtrlButtonBank].do(
					_.stringColor_(Color.black)
				)
			});

			if(GUI.id !== \cocoa, {
				saveCtrlButtonBank.toolTip_(
					"Set an arbitrary integer number, corresponding\nto the number of sliders on your device."
				)
			});

			flow.nextLine.shift(0, 8);

			UserView(window.view, flow.bounds.width-20@25)
				.background_(Color(0.95, 0.95, 0.95))
				.enabled_(false)
			;

			flow.nextLine.shift(5, -25);

			if(prefs.notNil and:{ prefs[\removeResponders].notNil }, {
				removeResponders = buildCheckbox.(prefs[\removeResponders])
			}, {
				removeResponders = buildCheckbox.(CVWidget.removeResponders)
			});

			flow.shift(5, 1);

			StaticText(window.view, flow.bounds.width-100@20)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Remove all OSC-/MIDI-responders on cmd/ctrl-period.")
			;

			flow.nextLine.shift(0, 8);

			Button(window.view, flow.bounds.width/2-10@25)
				.states_([["Cancel", Color.black, Color.white]])
				.font_(Font(Font.defaultSansFace, 14, true))
				.action_({ window.close })
			;

			flow.shift(-2, 0);

			Button(window.view, flow.bounds.width/2-10@25)
				.states_([["Save", Color.white, Color.red]])
				.font_(Font(Font.defaultSansFace, 14, true))
				.action_({
					if(saveGuiPosition.value == 2 and:{
						[left, top, width, height].select({ |field|
							field.string.interpret.isInteger
						}).size < 4
					}, {
						[leftText, topText, widthText, heightText].do(_.stringColor_(Color.red));
						uView.background_(Color.yellow);
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
						this.writePreferences(
							saveGuiPosition.value,
							rect,
							saveClassVars.value,
							saveMidiMode.string.interpret,
							saveMidiResolution.string.interpret,
							saveMidiMean.string.interpret,
							saveSoftWithin.string.interpret,
							saveCtrlButtonBank.string.interpret,
							removeResponders.value
						);
						window.close;
					})
				})
			;
		});
		window.front;
	}

	*writePreferences { |saveGuiProperties, guiProperties, saveClassVars, midiMode, midiResolution, midiMean, softWithin, ctrlButtonBank, removeResponders, informString|
		var prefsPath, prefs, thisGuiProperties, thisSaveClassVars, thisRemoveResponders, thisInformString;

		thisSaveClassVars = saveClassVars.asBoolean;
		thisRemoveResponders = removeResponders.asBoolean;

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
			Error("Please provide either a Rect or an Array for your desired GUI-properties").throw;
		});

		prefsPath = this.filenameSymbol.asString.dirname +/+ "CVCenterPreferences";
		if(File.exists(prefsPath), {
			prefs = Object.readArchive(prefsPath);
		}, {
			prefs = ();
		});
		prefs.put(\saveGuiProperties, saveGuiProperties);
		if(saveGuiProperties == 2 or:{ saveGuiProperties == 1 }, {
			prefs.put(\guiProperties, thisGuiProperties)
		}, { prefs.removeAt(\guiProperties) });
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
		prefsPath = this.filenameSymbol.asString.dirname +/+ "CVCenterPreferences";
		if(File.exists(prefsPath), {
			prefs = Object.readArchive(prefsPath);
			if(args.size > 0, {
				res = ();
				args.do({ |val| res.put(val.asSymbol, prefs[val.asSymbol]) });
				^res;
			}, {
				^prefs;
			})
		})
		^nil;
	}
}