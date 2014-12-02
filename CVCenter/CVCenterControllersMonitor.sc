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

CVCenterControllersMonitor {
	classvar <window, <tabs;
	
	*new { |focus|
		var ctrlrs;
		var midiOrder, orderedMidiCtrlrs;
		var oscOrder, orderedOscCtrlrs;
		var labelColors, labelStringColors, flow0, flow1;
		var thisFocus, tmp;
		var staticTextFont = Font(Font.defaultSansFace, 10);
		var staticTextColor = Color(0.2, 0.2, 0.2);
		
		if(focus.isNil, { thisFocus = 0 }, { thisFocus = focus });
				
		ctrlrs = this.getControllersList;
		
		if(window.isNil or:{ window.isClosed }, {
			window = Window(
				"MIDI- and OSC-responders currently active in CVCenter", 
				Rect(
					Window.screenBounds.width/2-250, 
					Window.screenBounds.height/2-250, 
					500, 500
				)
			);
			
			tabs = TabbedView(
				window, Rect(
					0, 1, 
					window.bounds.width, 
					window.bounds.height
				), ["MIDI-responders", "OSC-responders"], scroll: true
			);
			tabs.view.resize_(5);
			tabs.tabCurve_(4);
			tabs.labelColors_(Color.white!2);
			labelColors = [
				Color.red, //midi
				Color(0.0, 0.5, 0.5), //osc
			];
			labelStringColors = labelColors.collect({ |c| Color(c.red * 0.8, c.green * 0.8, c.blue * 0.8) });
			tabs.unfocusedColors_(labelColors);
			tabs.stringColor_(Color.white);
			tabs.views[0].decorator = flow0 = FlowLayout(window.view.bounds, 7@7, 3@3);
			tabs.views[1].decorator = flow1 = FlowLayout(window.view.bounds, 7@7, 3@3);
			[0, 1].do({ |t| tabs.focusActions[t] = { tabs.stringFocusedColor_(labelStringColors[t]) } });
			tabs.stringFocusedColor_(labelStringColors[thisFocus]);
			
			tabs.view.keyDownAction_({ |view, char, modifiers, unicode, keycode|
				switch(unicode,
					111, { tabs.focus(1) }, // key "o" -> osc
					109, { tabs.focus(0) }, // key "m" -> midi
					120, { window.close } // key "x" -> close window
				) 
			});
			
			midiOrder = ctrlrs.midiCtrlrs.order;
			orderedMidiCtrlrs = ctrlrs.midiCtrlrs.atAll(midiOrder);
			oscOrder = ctrlrs.oscCtrlrs.order;
			orderedOscCtrlrs = ctrlrs.oscCtrlrs.atAll(midiOrder);
			
			midiOrder.do({ |mc, i|
				flow0.shift(0, 0);
				StaticText(tabs.views[0], Rect(0, 0, flow0.bounds.width-30, 20))
					.string_(""+mc+"(used:"+ctrlrs.midiCtrlrs[mc][0]++"x)"+ctrlrs.midiCtrlrs[mc][1])
					.background_(Color(1.0, 1.0, 1.0, 0.5))
					.font_(staticTextFont)
				;
			});
			oscOrder.do({ |oc, i|
				flow0.shift(0, 0);
				StaticText(tabs.views[1], Rect(0, 0, flow0.bounds.width-30, 20))
					.string_(""+oc+"(used:"+ctrlrs.oscCtrlrs[oc][0]++"x)"+ctrlrs.oscCtrlrs[oc][1])
					.background_(Color(1.0, 1.0, 1.0, 0.5))
					.font_(staticTextFont)
				;
			})
		});
		window.front;
		tabs.focus(thisFocus);
	}
	
	*getControllersList {
		var midiCtrlrs, oscCtrlrs, tmp;
		
		midiCtrlrs ? midiCtrlrs = ();
		oscCtrlrs ? oscCtrlrs = ();
		
		CVCenter.cvWidgets.pairsDo({ |k, w|
			switch(w.class,
				CVWidgetKnob, {
					if(w.wdgtControllersAndModels.oscConnection.model.value != false, { 
						tmp = (w.wdgtControllersAndModels.oscConnection.model.value[2].asString+"(slot"+w.wdgtControllersAndModels.oscConnection.model.value[3]++")").asSymbol;
						if(oscCtrlrs[tmp.asSymbol].isNil, {
							oscCtrlrs.put(tmp.asSymbol, [1, [k.asString]]);
						}, { 
							oscCtrlrs[tmp.asSymbol][0] = oscCtrlrs[tmp.asSymbol][0]+1;
							oscCtrlrs[tmp.asSymbol][1] = oscCtrlrs[tmp.asSymbol][1].add(k.asString);
						});
					});
					w.midiOscEnv.midinum !? {
						tmp = w.midiOscEnv.midinum.asSymbol;
						tmp !? {
							if(midiCtrlrs[tmp].isNil, {
								midiCtrlrs.put(tmp, [1, [k.asString]]);
							}, { 
								midiCtrlrs[tmp][0] = midiCtrlrs[tmp][0]+1;
								midiCtrlrs[tmp][1] = midiCtrlrs[tmp][1].add(k.asString);
							})
						}
					}
				},
				CVWidget2D, {
					#[lo, hi].do({ |hilo|
						if(w.wdgtControllersAndModels[hilo].oscConnection.model.value !== false, { 
							tmp = (w.wdgtControllersAndModels[hilo].oscConnection.model.value[2].asString
							+"(slot"+w.wdgtControllersAndModels[hilo].oscConnection.model.value[3]++")").asSymbol;
							if(oscCtrlrs[tmp].isNil, {
								oscCtrlrs.put(tmp.asSymbol, [1, [k.asString++"["++hilo++"]"]])
							}, {
								oscCtrlrs[tmp.asSymbol][0] = oscCtrlrs[tmp.asSymbol][0]+1;
								oscCtrlrs[tmp.asSymbol][1] = oscCtrlrs[tmp.asSymbol][1].add(k.asString++"["++hilo++"]")
							});
						});
						w.midiOscEnv[hilo].midinum !? {
							tmp = w.midiOscEnv[hilo].midinum.asSymbol;
							tmp !? {
								if(midiCtrlrs[tmp].isNil, {
									midiCtrlrs.put(tmp, [1, [k.asString++"["++hilo++"]"]]);
								}, { 
									midiCtrlrs[tmp][0] = midiCtrlrs[tmp][0]+1;
									midiCtrlrs[tmp][1] = midiCtrlrs[tmp][1].add(k.asString++"["++hilo++"]");
								})
							}
						}
					})
				}
			)
		});
		
		^(midiCtrlrs: midiCtrlrs, oscCtrlrs: oscCtrlrs);
	}
	
}