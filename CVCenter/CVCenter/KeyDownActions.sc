KeyDownActions {

	// classvar <allEditors;
	// classvar <viewActions;
	classvar <>keyCodes, <>modifiersQt, <>modifiersCocoa, <>arrowsModifiersQt, <>arrowsModifiersCocoa;
	classvar <>globalShortcuts, <globalShortcutsEnabled=true, globalShortcutsEnableFunc;
	classvar trackingSynth, syncResponder, trackingSynthID;
	// var <window, <>actions;

	*initClass {
		var keyCodesAndModsPath, keyCodesAndMods;
		var globalShortcutsPath, globalShortcuts;
		var platform, scFunc;
		var syncStarter, cmdPeriodSynthRestart;
		var test;

		Class.initClassTree(Platform);
		Class.initClassTree(GUI);
		Class.initClassTree(SynthDescLib);
		Class.initClassTree(SynthDef);

		globalShortcutsEnableFunc = {
			if(globalShortcutsEnabled) {
				this.globalShortcutsSync;
			}
		};

		Platform.case(
			\osx, { platform = "OSX" },
			\linux, { platform = "Linux" },
			\windows, { platform = "Windows" },
			{ platform = "NN" }
		);

		keyCodesAndModsPath = this.filenameSymbol.asString.dirname +/+ "keyCodesAndMods"++platform;
		if(File.exists(keyCodesAndModsPath)) {
			keyCodesAndMods = Object.readArchive(keyCodesAndModsPath);
		};
		globalShortcutsPath = this.filenameSymbol.asString.dirname +/+ "globalShortcuts";
		if(File.exists(globalShortcutsPath)) {
			globalShortcuts = Object.readArchive(globalShortcutsPath);
		};

		Platform.case(
			\linux, {
				if(keyCodesAndMods.notNil and:{ keyCodesAndMods[\keyCodes].notNil }) {
					this.keyCodes = keyCodesAndMods[\keyCodes];
				} {
					this.keyCodes = IdentityDictionary[
						'fn + F1' -> 		67,
						'fn + F2' -> 		68,
						'fn + F3' -> 		69,
						'fn + F4' -> 		70,
						'fn + F5' -> 		71,
						'fn + F6' -> 		72,
						'fn + F7' -> 		73,
						'fn + F8' -> 		74,
						'fn + F9' -> 		75,
						'fn + F10' -> 		76,
						'fn + F11' -> 		95,
						'fn + F12' -> 		96,
						$1 ->				10,
						$2 ->				11,
						$3 ->				12,
						$4 ->				13,
						$5 ->				14,
						$6 ->				15,
						$7 ->				16,
						$8 ->				17,
						$9 ->				18,
						$0 ->				19,
						$- ->				20,
						$= ->				21,
						$q ->				24,
						$w ->				25,
						$e ->				26,
						$r ->				27,
						$t ->				28,
						$y ->				29,
						$u ->				30,
						$i ->				31,
						$o ->				32,
						$p ->				33,
						$[ ->				34,
						$] ->				35,
						$a ->				38,
						$s ->				39,
						$d ->				40,
						$f ->				41,
						$g ->				42,
						$h ->				43,
						$j ->				44,
						$k ->				45,
						$l ->				46,
						$; ->				47,
						$' ->				48,
						(92.asAscii) ->		51,
						$< ->				94,
						$z ->				52,
						$x ->				53,
						$c ->				54,
						$v ->				55,
						$b ->				56,
						$n ->				57,
						$m ->				58,
						$, ->				59,
						$. ->				60,
						$/ ->				61,
						\return ->			36,
						\tab ->				23,
						\space -> 			65,
						\esc ->				9,
						$` ->				49,
						'arrow up' ->		111,
						'arrow down' ->		116,
						'arrow left' ->		113,
						'arrow right' ->	114,
					]
				};

				// arrowsModifiers = IdentityDictionary[];

				if(keyCodesAndMods.notNil and:{ keyCodesAndMods[\modifiersQt].notNil }) {
					this.modifiersQt = keyCodesAndMods[\modifiersQt];
				} {
					this.modifiersQt = IdentityDictionary[
						\none ->			0,
						\shift ->			131072,
						\alt ->				524288,
						'alt + shift' ->	655360,
					]
				};

				this.modifiersCocoa = this.modifiersQt;
				this.arrowsModifiersQt = this.modifiersQt;
				this.arrowsModifiersCocoa = this.modifiersQt;
			},

			\osx, {
				if(keyCodesAndMods.notNil and:{ keyCodesAndMods[\keyCodes].notNil }) {
					this.keyCodes = keyCodesAndMods[\keyCodes];
				} {
					this.keyCodes = IdentityDictionary[
						'fn + F1' -> 		122,
						'fn + F2' -> 		120,
						'fn + F3' -> 		99,
						'fn + F4' -> 		118,
						'fn + F5' -> 		96,
						'fn + F6' -> 		97,
						'fn + F7' -> 		98,
						'fn + F8' -> 		100,
						'fn + F9' -> 		101,
						'fn + F10' -> 		109,
						'fn + F11' -> 		103,
						'fn + F12' -> 		111,
						$1 -> 				18,
						$2 -> 				19,
						$3 -> 				20,
						$4 -> 				21,
						$5 -> 				23,
						$6 -> 				22,
						$7 -> 				26,
						$8 -> 				28,
						$9 -> 				25,
						$0 -> 				29,
						$- -> 				27,
						$= -> 				24,
						$q -> 				12,
						$w -> 				13,
						$e -> 				14,
						$r -> 				15,
						$t -> 				17,
						$y -> 				16,
						$u -> 				32,
						$i -> 				34,
						$o -> 				31,
						$p -> 				35,
						$[ -> 				33,
						$] -> 				30,
						$a -> 				0,
						$s -> 				1,
						$d -> 				2,
						$f -> 				3,
						$g -> 				5,
						$h -> 				4,
						$j -> 				38,
						$k -> 				40,
						$l -> 				37,
						$; -> 				41,
						$' -> 				39,
						(92.asAscii) -> 	42, // backslash
						$` -> 				50,
						$z -> 				6,
						$x -> 				7,
						$c -> 				8,
						$v -> 				9,
						$b -> 				11,
						$n -> 				45,
						$m -> 				46,
						$, -> 				43,
						$. -> 				47,
						$/ -> 				44,
						\space ->			49,
						\tab ->				48,
						\return ->			36,
						\esc -> 			53,
						'arrow up' -> 		126,
						'arrow down' -> 	125,
						'arrow left' -> 	123,
						'arrow right' -> 	124,
					]
				};

				if(keyCodesAndMods.notNil and:{ keyCodesAndMods[\arrowsModifiersCocoa].notNil }) {
					this.arrowsModifiersCocoa = keyCodesAndMods[\arrowsModifiersCocoa];
				} {
					this.arrowsModifiersCocoa = IdentityDictionary[
						'none' ->			10486016,
						'alt' ->			11010336,
						'shift' ->			10617090,
						'alt + shift' ->	11141410
					]
				};

				if(keyCodesAndMods.notNil and:{ keyCodesAndMods[\modifiersCocoa].notNil }) {
					this.modifiersCocoa = keyCodesAndMods[\modifiersCocoa];
				} {
					this.modifiersCocoa = IdentityDictionary[
						\none ->			0,
						\alt ->				524576,
						\shift ->			131330,
						'alt + shift' ->	655650,
					]
				};

				if(keyCodesAndMods.notNil and:{ keyCodesAndMods[\arrowsModifiersQt].notNil }) {
					this.arrowsModifiersQt = keyCodesAndMods[\arrowsModifiersQt];
				} {
					this.arrowsModifiersQt = IdentityDictionary[
						\none ->			2097152,
						'alt' ->			2621440,
						'shift' ->			2228224,
						'alt + shift' ->	2752512
					]
				};

				if(keyCodesAndMods.notNil and:{ keyCodesAndMods[\modifiersQt].notNil }) {
					this.modifiersQt = keyCodesAndMods[\modifiersQt];
				} {
					this.modifiersQt = IdentityDictionary[
						\none ->			0,
						\shift ->			131072,
						\alt ->				524288,
						'alt + shift' ->	655360,
					]
				}
			},

			\windows, {
				if(keyCodesAndMods.notNil and:{ keyCodesAndMods[\keyCodes].notNil }) {
					this.keyCodes = keyCodesAndMods[\keyCodes];
				} {
					this.keyCodes = IdentityDictionary[
						'fn + F1' -> 		67,
						'fn + F2' -> 		68,
						'fn + F3' -> 		69,
						'fn + F4' -> 		70,
						'fn + F5' -> 		71,
						'fn + F6' -> 		72,
						'fn + F7' -> 		73,
						'fn + F8' -> 		74,
						'fn + F9' -> 		75,
						'fn + F10' -> 		76,
						'fn + F11' -> 		95,
						'fn + F12' -> 		96,
						$1 -> 				49,
						$2 -> 				50,
						$3 -> 				51,
						$4 -> 				52,
						$5 -> 				53,
						$6 -> 				54,
						$7 -> 				55,
						$8 -> 				56,
						$9 -> 				57,
						$0 -> 				48,
						$- -> 				189,
						$= -> 				187,
						$q -> 				81,
						$w -> 				87,
						$e -> 				69,
						$r -> 				82,
						$t -> 				84,
						$y -> 				89,
						$u -> 				85,
						$i -> 				73,
						$o -> 				79,
						$p -> 				80,
						$[ -> 				219,
						$] -> 				221,
						$a -> 				65,
						$s -> 				83,
						$d -> 				68,
						$f -> 				70,
						$g -> 				71,
						$h -> 				72,
						$j -> 				74,
						$k -> 				75,
						$l -> 				76,
						$; -> 				186,
						$' -> 				222,
						(92.asAscii) ->		220,
						$< -> 				226,
						$z -> 				90,
						$x -> 				88,
						$c -> 				67,
						$v -> 				86,
						$b -> 				66,
						$n -> 				78,
						$m -> 				77,
						$, -> 				188,
						$. -> 				190,
						$/ -> 				191,
						\esc -> 			27,
						\space -> 			65,
						$` -> 				192,
						'arrow up' -> 		38,
						'arrow down' -> 	40,
						'arrow left' -> 	37,
						'arrow right' -> 	39,
					]
				};

				if(keyCodesAndMods.notNil and:{ keyCodesAndMods[\modifiersQt].notNil }) {
					this.modifiersQt = keyCodesAndMods[\modifiersQt];
				} {
					this.modifiersQt = IdentityDictionary[
						\none ->			0,
						\shift -> 			131072,
						\alt -> 			524288,
						'alt + shift' -> 	655360,
					]
				};

				this.modifiersCocoa = this.modifiersQt;
				this.arrowsModifiersQt = this.modifiersQt;
				this.arrowsModifiersCocoa = this.modifiersQt;
			},
			{
				// dummies for unknown platforms
				this.keyCodes = IdentityDictionary.new;
				this.modifiersQt = IdentityDictionary.new;
				this.arrowsModifiersQt = IdentityDictionary.new;
				this.modifiersCocoa = IdentityDictionary.new;
				this.arrowsModifiersCocoa = IdentityDictionary.new;
			}
		);

		this.globalShortcuts = IdentityDictionary.new;

		if(globalShortcuts.isNil) {
			scFunc =
			"// bring all Windows to front and call CVCenter.makeWindow *afterwards*
			{ Window.allWindows.do(_.front); CVCenter.makeWindow }";
			this.globalShortcuts.put(
				'fn + F1',
				(func: scFunc, keyCode: KeyDownActions.keyCodes['fn + F1'])
			)
		} {
			this.globalShortcuts = globalShortcuts;
		};

		ServerTree.add({
			// syncStarter.value;
			if(globalShortcutsEnabled) {
				this.globalShortcutsSync;
			}
		}, \default);

		ServerQuit.add({
			CmdPeriod.remove({
				if(globalShortcutsEnabled) {
					this.globalShortcutsSync;
				}
			});
			syncResponder.free;
			"\nglobal key-down actions deactivated\n".inform;
		});
	}

	*globalShortcutsEnabled_ { |bool|
		if(Server.default.serverRunning) {
			if(bool) { this.globalShortcutsSync } { [trackingSynth, syncResponder].do(_.free) };
		};
		globalShortcutsEnabled = bool;
	}

	*globalShortcutsSync {
		var funcSlot;
		// "syncStarter now executing".postln;
		if(this.globalShortcuts.notNil and:{ this.globalShortcuts.isEmpty.not }) {
			SynthDef(\keyListener, {
				var state;
				this.globalShortcuts.asArray.collect(_.keyCode).collect({ |kcode|
					state = KeyState.kr(kcode, lag: 0);
					SendTrig.kr(Changed.kr(state), kcode, state);
				})
			}).add(completionMsg: {
				[trackingSynth, syncResponder].do(_.free);
				trackingSynth = Synth.basicNew(\keyListener);
				trackingSynthID = trackingSynth.nodeID;
				if(Main.versionAtLeast(3, 5)) {
					syncResponder = OSCFunc({ |msg, time, addr, recvPort|
						// "msg: %\n".postf([msg, time, addr, recvPort]);
						if(msg[1].asInt == trackingSynthID) {
							funcSlot = this.globalShortcuts.values.detect({ |sc|
								sc.keyCode == msg[2].asInt and:{ msg[3].asInt.asBoolean }
							});
							funcSlot !? { { funcSlot.func.interpret.value }.defer };
						};
					}, '/tr', Server.default.addr);
				} {
					syncResponder = OSCresponderNode(Server.default.addr, '/tr', { |t, r, msg|
						// "msg: %\n".postf([t, r, msg]);
						if(msg[1].asInt == trackingSynthID) {
							funcSlot = this.globalShortcuts.values.detect({ |sc|
								sc.keyCode == msg[2].asInt and:{ msg[3].asInt.asBoolean }
							});
							funcSlot !? { { funcSlot.func.interpret.value }.defer };
						};
					}).add
				};
				// [this.method, this.method.name].postln;
				CmdPeriod.objects.includes(globalShortcutsEnableFunc).not.if{
					CmdPeriod.add(globalShortcutsEnableFunc)
				};
				"\nglobal key-down actions enabled\n".inform;
				trackingSynth.newMsg;
			}.value);
		} { [trackingSynth, syncResponder].do(_.free) };

	}

	*setShortcuts { |view, shortcutsDict|
		var thisMod, thisArrMod;
		var modsDict, arrModsDict, arrowKeys;

		switch(GUI.id,
			\cocoa, {
				modsDict = this.modifiersCocoa;
				arrModsDict = this.arrowsModifiersCocoa;
			},
			\qt, {
				modsDict = this.modifiersQt;
				arrModsDict = this.arrowsModifiersQt;
			}
		);

		arrowKeys = [
			this.keyCodes['arrow up'],
			this.keyCodes['arrow down'],
			this.keyCodes['arrow left'],
			this.keyCodes['arrow right']
		];

		view.keyDownAction_(nil);

		shortcutsDict.do({ |keyDowns|
			// "view: %\n".postf(view);
			view.keyDownAction_(
				view.keyDownAction.addFunc({ |view, char, modifiers, unicode, keycode|
					switch(GUI.id,
						\cocoa, {
							thisMod = keyDowns.modifierCocoa;
							thisArrMod = keyDowns.arrowsModifierCocoa;
						},
						\qt, {
							thisMod = keyDowns.modifierQt;
							thisArrMod = keyDowns.arrowsModifierQt;
						}
					);

					case
						{ modifiers == modsDict[\none] or:{ modifiers == arrModsDict[\none] }} {
							if(keycode == keyDowns.keyCode and:{
								thisMod.isNil and:{ thisArrMod.isNil }
							}) {
								keyDowns.func.interpret.value(view, char, modifiers, unicode, keycode)
							};
						}
						{
							(char !== 0.asAscii).or(arrowKeys.includes(keycode)) and:{
								modifiers != modsDict[\none] and:{
									modifiers != arrModsDict[\none]
								}
							}
						} {
							if(keycode == keyDowns.keyCode and:{
								(modifiers == thisArrMod).or(modifiers == thisMod)
							}) {
								keyDowns.func.interpret.value(view, char, modifiers, unicode, keycode)
							}
						}
					;
				})
			)
		});
		// [view, view.keyDownAction].postln
	}

}