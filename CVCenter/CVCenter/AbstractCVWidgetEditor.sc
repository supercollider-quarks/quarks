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

AbstractCVWidgetEditor {

	classvar <allEditors, <>shortcuts, xySlots, nextX, nextY, shiftXY;
	var thisEditor, <window, <tabs, <scv, editorEnv, labelStringColors;
	var <specField, <specsList, <specsListSpecs;
	var <midiModeSelect, <midiMeanNB, <softWithinNB, <ctrlButtonBankField, <midiResolutionNB;
	var <midiInitBut, <midiSourceSelect, <midiLearnBut, <midiSrcField, <midiChanField, <midiCtrlField;
	var <calibBut;
	var <deviceListMenu, <cmdListMenu, addDeviceBut, thisCmdNames;
	var <deviceDropDown, <portRestrictor, /*<ipField, <portField, */<nameField, <indexField;
	var inputConstraintLoField, inputConstraintHiField, <alwaysPosField;
	var <mappingSelect, <connectorBut;
	var actionName, enterAction, enterActionBut, <actionsUIs;
	var name;
	var tabView0, tabView1, tabView2, tabView3;
	var flow0, flow1, flow2, flow3;

	*initClass {
		var localOscFunc, scFunc;
		var prefs, scPrefs = false;

		Class.initClassTree(KeyDownActions);
		// Class.initClassTree(CVCenterPreferences);

		prefs = CVCenterPreferences.readPreferences;
		prefs !? { prefs[\shortcuts] !? { prefs[\shortcuts][\cvwidgeteditor] !? { scPrefs = true }}};

		allEditors = IdentityDictionary.new;
		this.shortcuts = IdentityDictionary.new;

		// "prefs[\cvwidgeteditor]: %\n".postf(prefs[\shortcuts].cvwidgeteditor);

		if(scPrefs == false, {
			scFunc =
			"// focus 'specs' tab
			{ |view|
				AbstractCVWidgetEditor.allEditors.do({ |ed|
					case
						{ ed.keys.includes(\\hi) or:{ ed.keys.includes(\\lo) }} {
							#[lo, hi].do({ |k|
								ed[k] !? {
									if(ed[k].editor.notNil and:{
										ed[k].editor.isClosed.not and:{
											view == ed[k].tabs.view
										}
									}) { ed[k].editor.tabs.focus(0) }
								}
							})
						}
						{ ed.keys.select(_.isNumber).size == ed.keys.size } {}
						{
							if(ed.editor.notNil and:{
								ed.editor.isClosed.not and:{
									view == ed.tabs.view
								}
							}) { ed.editor.tabs.focus(0) }
						}
					;
				})
			}";
			this.shortcuts.put(
				\s,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$s])
			);
			scFunc =
			"// focus 'midi' tab
			{ |view|
				AbstractCVWidgetEditor.allEditors.do({ |ed|
					case
						{ ed.keys.includes(\\hi) or:{ ed.keys.includes(\\lo) }} {
							#[lo, hi].do({ |k|
								ed[k] !? {
									if(ed[k].editor.notNil and:{
										ed[k].editor.isClosed.not and:{
											view == ed[k].tabs.view
										}
									}) { ed[k].editor.tabs.focus(1) }
								}
							})
						}
						{ ed.keys.select(_.isNumber).size == ed.keys.size } {
							ed.keys.do({ |k|
								ed[k] !? {
									if(ed[k].editor.notNil and:{
										ed[k].editor.isClosed.not and:{
											view == ed[k].tabs.view
										}
									}) { ed[k].editor.tabs.focus(0) }
								}
							})
						}
						{
							if(ed.editor.notNil and:{
								ed.editor.isClosed.not and:{
									view == ed.tabs.view
								}
							}) { ed.editor.tabs.focus(1) }
						}
					;
				})
			}";
			this.shortcuts.put(
				\m,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$m])
			);
			scFunc =
			"// focus 'osc' tab
			{ |view|
				AbstractCVWidgetEditor.allEditors.do({ |ed|
					case
						{ ed.keys.includes(\\hi) or:{ ed.keys.includes(\\lo) }} {
							#[lo, hi].do({ |k|
								ed[k] !? {
									if(ed[k].editor.notNil and:{
										ed[k].editor.isClosed.not and:{
											view == ed[k].tabs.view
										}
									}) { ed[k].editor.tabs.focus(2) }
								}
							})
						}
						{ ed.keys.select(_.isNumber).size == ed.keys.size } {
							ed.keys.do({ |k|
								ed[k] !? {
									if(ed[k].editor.notNil and:{
										ed[k].editor.isClosed.not and:{
											view == ed[k].tabs.view
										}
									}) { ed[k].editor.tabs.focus(1) }
								}
							})
						}
						{
							if(ed.editor.notNil and:{
								ed.editor.isClosed.not and:{
									view == ed.tabs.view
								}
							}) { ed.editor.tabs.focus(2) }
						}
					;
				})
			}";
			this.shortcuts.put(
				\o,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$o])
			);
			scFunc =
			"// focus 'actions' tab
			{ |view|
				AbstractCVWidgetEditor.allEditors.do({ |ed|
					case
						{ ed.keys.includes(\\hi) or:{ ed.keys.includes(\\lo) }} {
							#[lo, hi].do({ |k|
								ed[k] !? {
									if(ed[k].editor.notNil and:{
										ed[k].editor.isClosed.not and:{
											view == ed[k].tabs.view
										}
									}) { ed[k].editor.tabs.focus(3) }
								}
							})
						}
						{ ed.keys.select(_.isNumber).size == ed.keys.size } {}
						{
							if(ed.editor.notNil and:{
								ed.editor.isClosed.not and:{
									view == ed.tabs.view
								}
							}) { ed.editor.tabs.focus(3) }
						}
					;
				})
			}";
			this.shortcuts.put(
				\a,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$a])
			);
			scFunc =
			"// close the editor
			{ |view|
				AbstractCVWidgetEditor.allEditors.do({ |ed|
					case
						{ ed.keys.includes(\\hi) or:{ ed.keys.includes(\\lo) }} {
							#[lo, hi].do({ |k|
								ed[k] !? {
									if(ed[k].editor.notNil and:{
										ed[k].editor.isClosed.not and:{
											view == ed[k].tabs.view
										}
									}) { ed[k].editor.close(k) }
								}
							})
						}
						{ ed.keys.select(_.isNumber).size == ed.keys.size } {
							ed.keys.do({ |k|
								ed[k] !? {
									if(ed[k].editor.notNil and:{
										ed[k].editor.isClosed.not and:{
											view == ed[k].tabs.view
										}
									}) { ed[k].editor.close(k) }
								}
							})
						}
						{
							if(ed.editor.notNil and:{
								ed.editor.isClosed.not and:{
									view == ed.tabs.view
								}
							}) { ed.editor.close }
						}
					;
				})
			}";
			this.shortcuts.put(
				\esc,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[\esc])
			);
			scFunc =
			"// collect OSC-commands resp. open the collector's GUI
			{ OSCCommands.makeWindow }";
			this.shortcuts.put(
				\c,
				(func: scFunc, keyCode: KeyDownActions.keyCodes[$c])
			)
		}, {
			this.shortcuts = prefs[\shortcuts][\cvwidgeteditor];
		});
	}

	front { |tab|
		thisEditor.window.front;
		tab !? {
			// thisEditor[\tabs].stringFocusedColor_(labelStringColors[tab]);
			thisEditor[\tabs].focus(tab);
		}
	}

	isClosed {
		var ret;
		thisEditor.window !? {
			ret = defer { thisEditor.window.isClosed };
			^ret.value;
		}
	}

}