
// problem: routine does not pick up environments that are pushed

ChuckBrowserKeyController {
	classvar	<identifierCodes,
			<states,
			<wildcards;
	var	<>browser,
		<string, routine,
		<lastMTIndex,
		<registers;
	
	*initClass {
		identifierCodes = (65..90) ++ (97..122) ++ (48..57) ++ $_.ascii ++ 127;

		states = Dictionary[
				// matchItem is done on unicode value from keystroke
				// arrow keys, pass to default action of the view who has focus now
			(63232..63235) -> { |instance, view, char, modifiers, unicode, keycode|
				view.defaultKeyDownAction(char, modifiers, unicode);
				nil	// means reset
			},
				// A-Z, browse for a class
			(65..90) -> { |instance, view, char, modifiers, unicode, keycode|
				var	className, nextKeySpec;
				instance.browser.classMenu.focus(true);
				#className, nextKeySpec = instance.parseIdentifier(char, { |string|
					instance.browser.classMatch(string)
				});
				nextKeySpec
			},
				// :, browse subtypes
			58 -> { |instance, view, char, modifiers, unicode, keycode|
				var	subType, nextKeySpec;
				instance.browser.subTypeMenu.focus(true);
				#subType, nextKeySpec = instance.parseIdentifier(nil, { |string|
					instance.browser.subtypeMatch(string)
				});
				nextKeySpec
			},
				// ., browse instance names
			46 -> { |instance, view, char, modifiers, unicode, keycode|
				var	inst, nextKeySpec;
				instance.browser.instanceListView.focus(true);
				#inst, nextKeySpec = instance.parseIdentifier(nil, { |string|
					instance.browser.instanceMatch(string)
				});
				nextKeySpec
			},
				// *, restrict instance list to the subtype of the current object
			42 -> { |instance|
				var	obj;
				(obj = instance.browser.currentObject).subType.notNil.if({
					instance.browser.setSubtypeForClass(obj.class, obj.subType);
					instance.browser.changeClass;
				});
				nil
			},
				// ^, chuck into default MT (^^ or ^60^)
			94 -> { |instance, view, char, modifiers, unicode, keycode|
				var	num, nextKeySpec;
				#num, nextKeySpec = instance.parseNumber;
				(nextKeySpec[3] == 94).if({		// ^ is command terminator
					instance.browser.currentObject.chuck(MT.default, num);
				});
				nil
			},
				// #, insert ## object, #n name, #p proto into current document
				// 	#, #N insert the same with ', ' after
			35 -> { |instance, view, char, modifiers, unicode, keycode|
				var	nextKeySpec = instance.getKeySpec;
				switch(nextKeySpec[3])
					{ 35 } {
						Document.current.selectedString_(
							instance.browser.currentObject.asCompileString
						)
					}
					{ 110 } {
						Document.current.selectedString_(
							instance.browser.currentObject.collIndex.asCompileString
						)
					}
					{ 112 } {
						instance.browser.currentObject.tryPerform(\proto);
					}
					{ 44 } {
						Document.current.selectedString_(
							instance.browser.currentObject.asCompileString ++ ", "
						)
					}
					{ 78 } {
						Document.current.selectedString_(
							instance.browser.currentObject.collIndex.asCompileString ++ ", "
						)
					};
				nil
			},
				// <, chuck into MCG -- << for first available, <3< for specific index
			60 -> { |instance, view, char, modifiers, unicode, keycode|
				var	num, nextKeySpec;
				#num, nextKeySpec = instance.parseNumber;
				(nextKeySpec[3] == 60).if({
					num.isNil.if({
						num = MCG.collection.detectIndex({ |mcg|
							mcg.value.mixer.isNil
						});
					});
					instance.browser.currentObject => MCG(num);
				});
				nil
			},
				// >, chuck into VP or VC
				// >> into first available VP
				// >3> into specific VP
				// >3. into voicer held in VP
				// >name> into VC(\name)
			62 -> { |instance, view, char, modifiers, unicode, keycode|
				var	target, nextKeySpec;
				instance.browser.keyCommandView.focus;
				#target, nextKeySpec = instance.parseIdentifier;
				(target.size == 0).if({ target = nil }, {
					target[0].inclusivelyBetween($0, $9).if({
						target = target.asInteger
					}/*, { target = target.asSymbol }*/);
				});
				target.isNil.if({
					target = VP.collection.detectIndex({ |vp| vp.value.active.not });
				});
				target.isNumber.if({
					target = VP(target);
				}, {
						// should modularize this
					target = VC.collection.detect({ |vc|
						vc.collIndex.asString[0..target.size-1] == target
					});
				});
				target.notNil.if({
					switch(nextKeySpec[3])
						{ 62 } {
							"% => %".format(instance.browser.currentObject.asCompileString,
								target.asCompileString).debug("Executing");
							instance.browser.currentObject => target;
						}
						{ 46 } {
							"% => %".format(instance.browser.currentObject.asCompileString,
								target.asVC.asCompileString).debug("Executing");
							instance.browser.currentObject => target.asVC;
						}
					});
				nil
			},
				// @, select last bp chucked into MT
			64 -> { |instance|
				var	bp = MT.default.lastBP;
				bp.notNil.if({ instance.browser.currentObject_(bp) },
					{ "MT's lastBP is empty. Ignoring @ command.".warn; });
				nil
			},
				// !, play/stop toggle
				// !! -- if current is BP, do play or stop
				// !name! -- play or stop this BP
			33 -> { |instance|
				var	string, nextKeySpec, obj, method;
					// maybe change this to display BP?
				instance.browser.changeClass(BP);
				#string, nextKeySpec = instance.parseIdentifier(nil, { |string|
					instance.browser.instanceMatch(string)
				});
				(nextKeySpec[3] == 33).if({
					obj = instance.browser.currentObject;
					method = obj.isPlaying.if({ \stop }, { \play });
					"%.%".format(obj.asCompileString, method).debug("Executing");
					obj.isKindOf(BP).if({
						obj.perform(method);
					});
				});
				nil
			},
						
				// /, enter an arbitrary call to apply to current object
				// e.g. F.b/.makev or F.b/.v.insp
			47 -> { |instance, view, char, modifiers, unicode, keycode|
				var	string, nextKeySpec;
				#string, nextKeySpec = instance.getCmdString;
				(nextKeySpec[3] == 13).if({
					((instance.browser.currentObject.asString ++ string)
						.debug("Executing")
						.interpret).postln
				});
				nil
			},
				// =, enter a chuck e.g. F.b=>VC(\newname)
			61 -> { |instance, view, char, modifiers, unicode, keycode|
				var	string, nextKeySpec;
				#string, nextKeySpec = instance.getCmdString("=");
				(nextKeySpec[3] == 13).if({
					((instance.browser.currentObject.asString ++ string)
						.debug("Executing")
						.interpret).postln
				});
				nil
			},
				// ctrl-`, move current doc to front
			30 -> { |instance, view, char, modifiers, unicode, keycode|
				(modifiers bitAnd: 0x40000 > 0).if({
					Document.current.front;
				});
				nil
			},
				// $, get or set a register
			36 -> { |instance, view, char, modifiers, unicode, keycode|
					// next key
				#view, char, modifiers, unicode, keycode = instance.getKeySpec;
				(unicode == 36).if({	// $$a, set register a to current object
					#view, char, modifiers, unicode, keycode = instance.getKeySpec;
					(char.isPrint and: { char != $$ }).if({
						instance.registers[char] = instance.browser.currentObject;
						"Set register $% = %\n".postf(char,
							instance.browser.currentObject.asCompileString);
					});
				}, {		// $a, jump to object held in register
					instance.registers[char].notNil.if({
						instance.browser.currentObject = instance.registers[char];
					});
				});
				nil
			}
		];
		
		wildcards = Dictionary[
				// current object for this class
			"(*)" -> { |string, identifier, pos, instance|
				"%(%)%".format(string[0..pos-1],
					instance.browser.instanceForClass(identifier.asSymbol.asClass)
						.asCompileString,
					string[pos+3..]);
			},
				// last touched BP
			"@" -> { |string, identifier, pos, instance|
				"%BP(%)%".format(string[0..pos-1],
					instance.browser.instanceForClass(BP).asCompileString,
					string[pos+1..]);
			},
				// get a register
			"\$" -> { |string, identifier, pos, instance|
				var	obj = instance.registers[string[pos+1]];
				(string[pos+1] == $$).if({
					"%$%".format(string[0..pos-1], string[pos+2..]);
				}, {
					obj.notNil.if({
						"% % %".format(string[0..pos-1], obj.asCompileString,
							string[pos+2..])
					}, {
						string
					});
				});
			}
		];
	}
	
	*new { |browser|
		^super.newCopyArgs(browser).init
	}
	
	init {
		var	fn = { |view, char, modifiers, unicode, keycode|
			if(view.isKindOfByName('QView')) {
				if(keycode.inclusivelyBetween(16777234, 16777237).not) {
					this.doKey(view, char, modifiers, unicode, keycode);
					true  // required for QT
				} {
					nil  // arrows must bubble up to focused view
				};
			} {
				this.doKey(view, char, modifiers, unicode, keycode);
				true
			};
		};
		browser.classMenu.keyDownAction = fn;
		browser.subTypeMenu.keyDownAction = fn;
		browser.instanceListView.keyDownAction = fn;
		browser.keyCommandView.keyDownAction = fn;
		
		registers = IdentityDictionary.new;
		
		this.resetState(true);
	}
	
	focus { browser.focus }

	doKey { |view, char, modifiers, unicode, keycode|
		routine ?? { this.resetState };
		if(char.tryPerform(\isPrint) ? false) { string = string ++ char };
		if(#[8, 127].includes(unicode)) {
			string = string.left(string.size-1);
		};
		this.updateString;
		routine.next([view, char, modifiers, unicode, keycode, currentEnvironment]);
	}
	
	resetState { |newRoutine = false|
		string = "";
		this.updateString;
		(newRoutine or: { routine.isNil }).if({
			routine = Routine({ |keyspec|
				var	view, char, modifiers, unicode, keycode, newkeyspec, action,
					envir;
				loop {
					#view, char, modifiers, unicode, keycode, envir = keyspec;
					(action = states.matchAt(unicode)).notNil.if({
							// if an error occurs, it might crash the routine
							// and kill the interface
						try {
							envir.use({
								newkeyspec = action.value(this, *keyspec);
							});
						} {	|error|
							error.reportError;
							"Unknown error. Resetting key controller state.".warn;
							this.resetState(true);
						};
						newkeyspec.notNil.if({
							#view, char, modifiers, unicode, keycode, envir = newkeyspec
						});
					}, { newkeyspec = nil });
						// if the action didn't return a keyspec,
						// we have to wait for another key
					newkeyspec.isNil.if({
						this.resetState;
						keyspec = this.getKeySpec;	// running
					}, {
						keyspec = newkeyspec;
					});
				}
			});
		});
	}
	
	parseIdentifier { |firstChar, action|
		var	string;
		var	view, char, modifiers, unicode, keycode, envir;

		string = firstChar.notNil.if({ firstChar.asString }, { "" });
		action.value(string);
		
		{	#view, char, modifiers, unicode, keycode, envir = this.getKeySpec;
			identifierCodes.matchItem(unicode)
		}.while({
			(#[127, 8].includes(unicode)).if({
				string = string.left(string.size-1);
				this.updateString;
				action.value(string);
			}, {
				string = string ++ char;
				(action.value(string).isNil and: { action.notNil }).if({
						// no match, drop the last char
					string = string.left(string.size-1);
				});
			});
		});
		^[string, [view, char, modifiers, unicode, keycode, envir]]
	}
	
	parseNumber { |firstChar, action|
		var	string;
		var	view, char, modifiers, unicode, keycode, envir;

		browser.keyCommandView.focus;
		string = firstChar.notNil.if({ firstChar.asString }, { "" });
		action.value(string);
		
		{	#view, char, modifiers, unicode, keycode, envir = this.getKeySpec;
			unicode.inclusivelyBetween(48, 57) or: { unicode == 127 }
		}.while({
			(#[127, 8].includes(unicode)).if({
				string = string.left(string.size-1);
				this.updateString;
				action.value(string);
			}, {
				string = string ++ char;
				(action.value(string).isNil and: { action.notNil }).if({
						// no match, drop the last char
					string = string.left(string.size-1);
				});
			});
		});
		^[(string.size > 0).if({ string.asInteger }),
			[view, char, modifiers, unicode, keycode, envir]]
	}
	
	getCmdString { |firstChar|	// just collecting chars, no action
		var	string;
		var	view, char, modifiers, unicode, keycode, envir;

		browser.keyCommandView.focus;
		string = firstChar.notNil.if({ firstChar.asString }, { "" });

		{	#view, char, modifiers, unicode, keycode, envir = this.getKeySpec;
			unicode != 13 and: { unicode != 27 }	// esc to cancel
		}.while({
			(#[127, 8].includes(unicode)).if({
				string = string.left(string.size-1);
				this.updateString;
			}, {
				string = string ++ char;
			});
		});
		^[this.processWildcards(string), [view, char, modifiers, unicode, keycode, envir]]

	}
	
	processWildcards { |string|
		var	index, idpos;
		string = string.copy;
		wildcards.keysValuesDo({ |wild, func|
			index = -1;
			{ (index = string.find(wild, false, index+1)).notNil }.while({
				idpos = index - 1;
				{ idpos >= 0 and: { identifierCodes.matchItem(string[idpos].ascii) } }.while({
					idpos = idpos - 1;
				});
					// needed because idpos may be the punctuation before the real identifier
				identifierCodes.matchItem(string[idpos].ascii).not.if({
					idpos = idpos + 1;
				});
					// |string, identifier, pos, instance|
				string = func.value(string, string[idpos..index-1], index, this);
			});
		});
		^string
	}
	
	updateString {
		this.browser.keyCommandView.string = string;
	}

	// this is only needed because QT fires the keyDownAction when a modifier
	// is pressed -- I need to ignore those events
	// doesn't trap caps-lock, though...
	getKeySpec {
		var next;
		while {
			next = true.yield;
			next[1].ascii == 0 and: { next[2] > 0 }  // ignore empty modifier keystrokes
		};
		^next
	}
}