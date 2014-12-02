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

CVWidgetSpecsEditor {
	var <window;

	*new { |displayDialog, object, wdgtName, controlsDict, prefix, pairs2D, metadata, environment|
		^super.new.init(displayDialog, object, wdgtName, controlsDict, prefix, pairs2D, metadata, environment)
	}

	init { |displayDialog, obj, name, controls, prefix, pairs2D, metadata, environment|
		var object;
		var wdgtName, windowTitle;
		var specsList, specsListSpecs, selectMatch;
		var cNameRect, cNameEnterTextRect, specEnterTextRect, specSelectRect, enterTabRect;
		var staticTextFont, staticTextColor, textFieldFont, selectFont, textFieldFontColor, textFieldBg;
		var formEls, nameStr, makeLine, sendBut, cancelBut;
		var flow, lines, allEls, allWidth;
		var cMatrix, specName, prefSpecName, made, tmp;

		object = obj;

		#cNameRect, cNameEnterTextRect, specEnterTextRect, specSelectRect, enterTabRect = Rect.new!5;
		[cNameRect, cNameEnterTextRect, specEnterTextRect, specSelectRect, enterTabRect].do({ |e|
			e.height_(20).left_(0).top_(0);
		});
		[cNameRect, cNameEnterTextRect, enterTabRect].do({ |e| e.width_(70) });

		specSelectRect.width_(240);
		specEnterTextRect.width_(160);

		allEls = [cNameRect, cNameEnterTextRect, specEnterTextRect, specSelectRect];

		staticTextFont = Font(Font.defaultSansFace, 10);
		staticTextColor = Color(0.2, 0.2, 0.2);
		textFieldFont = Font(Font.defaultMonoFace, 10);
		selectFont = Font(Font.defaultMonoFace, 10, true);
		textFieldFontColor = Color.black;
		textFieldBg = Color.white;

		if(object.class !== NodeProxy, {
			if(name.isNil or:{ controls.isNil }, {
				Error("CVWidgetSpecsEditor is a utilty-class, only to be used in connection with cvcGui and the like").throw;
			})
		}, {
			if(controls.isNil, {
				Error("CVWidgetSpecsEditor is a utilty-class, only to be used in connection with cvcGui and the like").throw;
			})
		});

		if(pairs2D.isNil, {
			lines = controls.size;
		}, {
			lines = controls.size-(pairs2D.size);
		});

		switch(object.class,
			Synth, { windowTitle = "Synth('"++name++"')" },
			Ndef, { windowTitle = object.asString },
			{ windowTitle = object.asString++", (node-ID:"+object.asNodeID++")" }
		);

		window = Window("Specs:"+windowTitle, Rect(
			(Window.screenBounds.width-650).div(2),
			(Window.screenBounds.height-(lines * 25 + 65)).div(2),
			650,
			lines * 25 + 65
		), scroll: true).userCanClose_(false);

		window.view.decorator = flow = FlowLayout(window.bounds.insetBy(5));
		flow.margin_(4@0);
		flow.gap_(0@4);

		flow.shift(0, 0);
		StaticText(window, cNameRect)
			.font_(staticTextFont)
			.string_(" argname(s)")
		;

		flow.shift(5, 0);
		StaticText(window, cNameEnterTextRect)
			.font_(staticTextFont)
			.string_(" widget-key")
		;

		flow.shift(5, 0);
		StaticText(window, specEnterTextRect)
			.font_(staticTextFont)
			.string_(" enter a ControlSpec")
		;

		flow.shift(5, 0);
		StaticText(window, specSelectRect)
			.font_(staticTextFont)
			.string_(" or select an existing one")
		;

		flow.shift(5, 0);
		StaticText(window, enterTabRect)
			.font_(staticTextFont)
			.string_(" tab-name")
		;

		specsList = ["Select a spec..."];
		specsListSpecs = [nil];
		Spec.specs.asSortedArray.do({ |spec|
			if(spec[1].isKindOf(ControlSpec), {
				specsList = specsList.add(spec[0]++":"+spec[1]);
				specsListSpecs = specsListSpecs.add(spec[1]);
			})
		});

		#formEls, cMatrix = ()!2;

		makeLine = { |elem, cname, size, pairs2D, prefix|
			if(elem.type.notNil, {
				switch(elem.type,
					\w2d, {
						nameStr = ""+cname+"(lo/hi)";
						specName = cname;
					},
					\w2dc, {
						nameStr = ""+cname;
						specName = cname.split($/);
						specName = pairs2D.select({ |v, k|
							v == [specName[0].asSymbol, specName[1].asSymbol]
						}).keys.asArray[0];
					},
					{
						nameStr = ""+cname+"("++size++")";
						specName = cname;
					}
				)
			}, {
				nameStr = ""+cname;
				specName = cname;
			});

			prefix !? {
				prefSpecName = prefix.asString ++ (specName.asString[0]).toUpper ++ specName.asString[1..];
			};

			flow.shift(0, 0);
			StaticText(window, cNameRect)
				.font_(staticTextFont)
				.background_(Color(1.0, 1.0, 1.0, 0.5))
				.string_(nameStr)
			;

			flow.shift(5, 0);
			elem.cName = TextField(window, cNameEnterTextRect)
				.font_(textFieldFont)
				.string_(prefSpecName ? specName)
				.background_(textFieldBg)
			;

			flow.shift(5, 0);
			elem.specEnterText = TextField(window, specEnterTextRect)
				.font_(textFieldFont)
				.background_(textFieldBg)
			;

			if(cname.asSymbol.asSpec.notNil and:{
				cname.asSymbol.asSpec.isKindOf(ControlSpec)
			}, {
				elem.specEnterText.string_(cname.asSymbol.asSpec.asCompileString)
			}, {
				tmp = cname.asString.select({ | c | c.isAlpha}).asSymbol;
				if(tmp.asSpec.notNil and:{
					tmp.asSpec.isKindOf(ControlSpec)
				}, {
					elem.specEnterText.string_(tmp.asSpec.asCompileString)
				})
			});

			flow.shift(5, 0);
			elem.specSelect = PopUpMenu(window, specSelectRect)
				.items_(specsList)
				.font_(selectFont)
				.action_({ |m|
					elem.specEnterText.string_(specsListSpecs[m.value].asCompileString)
				})
			;

			tmp ?? { tmp = cname.asSymbol };

			selectMatch = specsListSpecs.detectIndex({ |ispec, i| ispec == tmp.asSpec });
			selectMatch !? { elem.specSelect.value_(selectMatch) };
			tmp = nil;

			metadata !? {
				if(metadata.keys.includes(specName.asSymbol), {
					selectMatch = specsListSpecs.detectIndex({ |ispec, i| ispec == metadata[specName.asSymbol].asSpec });
				});
				block { |break|
					metadata.pairsDo({ |k, spec|
						if(spec.asSpec.isKindOf(ControlSpec), {
							if(specsListSpecs.indexOfEqual(spec.asSpec).isNil, {
								if(pairs2D.notNil, {
									if(pairs2D.keys.includes(specName) and:{
										pairs2D[specName].includes(k)
									}, {
										specsList.includes(specName.asString++":"+(spec.asSpec)).not.if{
											specsList = specsList.add(specName.asString++":"+(spec.asSpec));
											// make spec available for all subsequent selections
											Spec.add(specName.asSymbol, spec);
										}
									})
								}, {
									specsList = specsList.add(k.asString++":"+(spec.asSpec));
									// make spec available for all subsequent selections
									Spec.add(k.asSymbol, spec);
								});
								elem.specSelect.items_(specsList);
								specsListSpecs.includes(spec.asSpec).not.if{
									specsListSpecs = specsListSpecs.add(spec.asSpec);
								};

								selectMatch = specsListSpecs.indexOfEqual(spec.asSpec);
								break.value(
									elem.specSelect.value_(selectMatch);
									elem.specEnterText.string_(spec.asSpec.asCompileString);
								);
							}, {
								if(k == cname, {
									selectMatch = specsListSpecs.indexOfEqual(spec.asSpec);
									break.value(
										elem.specSelect.value_(selectMatch);
										elem.specEnterText.string_(spec.asSpec.asCompileString);
									);
								});
								if(pairs2D.notNil and:{ pairs2D[specName].notNil }, {
									if(pairs2D[specName].includes(k), {
										selectMatch = specsListSpecs.indexOfEqual(spec.asSpec);
										break.value(
											elem.specSelect.value_(selectMatch);
											elem.specEnterText.string_(spec.asSpec.asCompileString);
										);
									})
								})
							})
						})
					})
				}
			};

			flow.shift(5, 0);
			elem.enterTab = TextField(window, enterTabRect)
				.font_(textFieldFont)
				.background_(textFieldBg)
			;

			if(object.class == NodeProxy, {
				if(object.isPlaying, {
					elem.enterTab.string_("NodeProxy ("++object.asNodeID++")");
				}, {
					elem.enterTab.string_("NodeProxy");
				})
			}, {
				elem.enterTab.string_(name);
			});

		};

		made = [];

		controls.pairsDo({ |cname, val, i|
			// [cname, val, i].postln;
			if(val.class === Array, {
				if(val.size == 2, {
					formEls.put(cname, ());
					formEls[cname].type = \w2d;
					formEls[cname].slots = val;
					makeLine.(formEls[cname], cname, prefix: prefix);
					made = made.add(cname);
				}, {
					formEls.put(cname, ());
					formEls[cname].type = \wms;
					formEls[cname].slots = val;
					makeLine.(formEls[cname], cname, val.size, prefix: prefix);
					made = made.add(cname);
				})
			});

			pairs2D !? {
				pairs2D.pairsDo({ |k, pair|
					if(pair.includes(cname) and:{
						val.class !== Array and:{
							made.includes(cname).not
						}
					}, {
						formEls.put(cname, ());
						formEls[cname].type = \w2dc;
						formEls[cname].slots = [controls[pair[0]], controls[pair[1]]];
						formEls[cname].controls = pair;
						makeLine.(formEls[cname], pair[0]++"/"++pair[1], pairs2D: pairs2D, prefix: prefix);
						made = made.add(pair[0]);
						made = made.add(pair[1]);
					})
				})
			};

			if(formEls[cname].isNil and:{ made.indexOfEqual(cname).isNil }, {
				formEls.put(cname, ());
				formEls[cname].slots = [val];
				makeLine.(formEls[cname], cname, prefix: prefix);
				made = made.add(cname);
			})

		});

//		cMatrix.postln;

		allWidth = allEls.collect({ |e| e.width }).sum + (allEls.size-1*5);

		flow.shift(20, 10);
		cancelBut = Button(window, Rect(0, 0, 65, 20))
			.states_([[ "cancel", Color(0.1, 0.3, 0.15), Color(0.99, 0.77, 0.11)]])
			.action_({ |cb|  window.close })
		;

		flow.shift(5, 0);
		sendBut = Button(window, Rect(0, 0, 65, 20))
			.states_([[ "gui", Color.white, Color.red ]])
			.action_({ |sb|
				formEls.pairsDo({ |el, vals|
					vals = vals.collect(_.value);
					vals.specSelect = specsListSpecs[vals.specSelect];
					vals = vals.collect({ |val| if(val == "", { nil }, { val }) });
					CVCenter.finishGui(obj, el, environment, vals);
				});
				window.close;
			})
		;

		window.front;
	}

}