
// 	physical mod May06''HH: 
//	adds a default function to be evaluated on any play action, so that the synth 
//	can derive general activity from it.
//	the function is added per synth in the hidMap together with the other functions 
//	in the .putProxy call
//	changes are marked with $$
/*
HID 	usage codes: 
	0 =	all
	1 =	nil
	2 =	mouses
	3 =	nil
	4 =	normal gamepads
	5 =	airstick, ..?
	6 =	keyboard
	
*/

GamePad {
	classvar <metaMap, <q, <>hidMaps, <space, <cookieMaps, <normMaps, <wingsAndRooms, <modStates; 
	classvar <>verbose=false, <>defaultAction, <>recChan = 1, pxMix, 
	<modNames = #[\lHat, \rHat, \midL, \midR];
	
	*initClass { 
		hidMaps = hidMaps ?? { () };
		cookieMaps = cookieMaps ?? { () };
		normMaps = normMaps ?? { () };
		wingsAndRooms = wingsAndRooms ?? { () };
		modStates = modStates ?? { () };
		
		defaultAction = { arg productID, vendorID, locID, cookie, val;
				if (verbose) { [productID, vendorID, locID, cookie, val].postln };
		};

		q = q ?? { () }; 
				// space is where proxies, their ctLoops etc are kept together.
		space = space ?? { () }; 

		Spec.specs.addAll([
			\loopTempo -> ControlSpec(0.1, 10, \exp)
		]);

				// fix this last.
		q[\usedRatios] = ();	// per buffer -> BufBank

		metaMap = (
			\shiftMidR: true,
			midL: { arg val, loop; }, 
			midR: { arg val, loop; var flag; 
				if (val==0, { 
					flag = metaMap[\shiftMidR].not; metaMap[\shiftMidR] = flag; 
					("meta" + #["loop start/length", "value range/shift"][flag.binaryValue]).postln;
				}) 
			}, 
			lHat: { arg val, loop; if (val==0, { "flipInv loop".postln; loop.flipInv })  }, 
			rHat: { arg val, loop; if (val==0, { "flip loop".postln; loop.flip }) }, 
			joyLX: { arg val, loop; 
				if (metaMap[\shiftMidR], { loop.scaler_(val * 2); }, { loop.start_(val); })
			}, 
			joyLY: { arg val, loop; 
				if (metaMap[\shiftMidR]) { loop.shift_(val - 0.5); } { loop.length_(val.squared * 4); }
			}, 
			joyRY: { arg val, loop; loop.tempo_(\loopTempo.asSpec.map(val)); }, 
			joyRX: { arg val, loop; loop.jitter_(val - 0.5) }
		);  
	}
	
	*init { 
		HIDDeviceService.buildDeviceList(0); 
		HIDDeviceService.devices.do({ arg dev; 
			[dev.manufacturer, dev.product, dev.productID, dev.vendorID, dev.locID].postln;
			cookieMaps[ dev.locID ] = cookieMaps[dev.vendorID];
			normMaps[ dev.locID ] = normMaps[dev.vendorID];
			modStates[ dev.locID ] = (midL: false, midR: false, lHat: false, rHat: false);
			
			dev.queueDevice;
		});
		HIDDeviceService.action_({ arg productID, vendorID, locID, cookie, val; 
			var cookieMap; 
			cookieMap = cookieMaps[locID];
			if (cookieMap.notNil, { 
				this.hidAction(locID, cookie, val);
			}, { 
				if(vendorID != 131){ // catch ugly exception in Ferrari!
//					"GamePad defaultAction:".postln;
					defaultAction.value(productID, vendorID, locID, cookie, val);
				}
			});	
		});	
	}
	
	*startHID {|mixer| this.init; HIDDeviceService.runEventLoop; pxMix = mixer }
	
	*stop { HIDDeviceService.stopEventLoop } 
	
	*findProxy { |name| 
		var proxy = currentEnvironment[name] 
		?? { Ndef.all[Server.default.name.asSymbol] }
		?? { Tdef.all[name] }
		?? { Pdef.all[name] };
		
		if (proxy.isNil) { "GamePad: no proxy named '%' found!".postln };
		^proxy;
	}
	
	*putProxy { |pos, proxy, map| 
		// only works if currentEnvironment is a proxyspace. 
		// may be made more general later.
		var ctLoop;
		
		if (proxy.isKindOf(Symbol)) { 
			proxy = this.findProxy(proxy);
		};
		
		[proxy, map].postcs;
		
		ctLoop = CtLoop(pos, map).rescaled_(true)
			.dontRescale([\midL, \midR, \lHat, \rHat]); 
		
		
		space.put(pos, (
			name: name, 
			proxy: proxy,
			ctLoop: ctLoop, 
			map: map, 
			meta: false, 
			recNames: [
				\joyRY, \joyLX, \joyRX, \joyLY, 
				\midL, \midR, \lHat, \rHat, 
				\throtL, \throtR, \wheel
			] 
		));
	}	

	*hidAction { arg locID, cookie, rawVal; 
		
		var cookieMap, cookieName, mySpace, wingAndRoom, val;   
		var curLoop, curPx, curMap, curMeta, padOffsets;

		var recTime, recSynths = Array.newClear(16), normMap, modState;
					// we know we have a cookieMap!
		cookieMap = cookieMaps[locID]; 
		cookieName = cookieMap.getID(cookie);

				// normalize ranges:
		normMap = normMaps[locID]; 	

				// if no normMap was found, this fails. it should.
		val = normMap[cookieName].value(rawVal) ? rawVal; 
				
		if (verbose == true, { [this.name, locID, cookieName, cookie, rawVal, val.round(0.0001) ].postln });
	
		wingAndRoom = wingsAndRooms[locID] ?? { wingsAndRooms.put(locID, [0, 0]); [0, 0] };
		mySpace = space[wingAndRoom.sum];
		modState = modStates[locID]; 

		if (mySpace.isNil, {
			// "no mans land... jump to switching spaces.".postln;
		}, { 
			curPx = mySpace[\proxy];
			curMap = mySpace[\map]; 
			curLoop = mySpace[\ctLoop];
			curMeta = mySpace[\meta]; 

//Open relative editor in pxmix  !!
		try{
			pxMix.pxMons.collect({ |it, i| if(it.nameView.string == curPx.key.asString)			{ { pxMix.arGuis[i].edBut.doAction}.defer } 
			})
		};
	

			if (mySpace[\recNames].includes(cookieName) and: curMap.notNil, {
	
				if (curMeta, 
					{ 	metaMap[cookieName].value(val, curLoop); },
					// normal play mode: 
					{ 	curMap[cookieName].value(val, modState); 
// $$ insert a default trig function here for activity measurement!
//					 	curPx.set(\act, val);
//					 	curPx.set(\t_act, 1);
					 	curMap[\activity].value(val);
					}
				);
							
				if (curLoop.notNil, { curLoop.recordEvent(cookieName, val); });
					// write modifier states!
				if (modNames.includes(cookieName), { 
					[cookieName, val, val == 0];
					(modState.put(cookieName, val == 0)); 
				});
				
				^this;
			});
							// turn this into a case.switch thing? 
			if (cookieName == \rfTop, { 
				// mute button toggles: if paused, play, else pause. 
				// $$ or: add in a Tdef toggle with the same name as the proxy.... ask for existance !??!?
				// stop the metaCtl here?
				if (val == 0 and: curPx.notNil, { 
					if (curPx.paused, 
						{ curPx.resume.wakeUp; ["Resuming !", mySpace.name].postln;}, 
						{ curPx.pause; [" PAUSE", mySpace.name].postln; }) 
				});
				^this;
			}); 
	
			if (cookieName == \lfTop, {	
				mySpace.meta = false;
				[ { curLoop.startRec; }, { curLoop.stopRec; } ][val].value;
				^this;
			});
			if (cookieName == \lfBot and: { val == 0 }, {
				if (curLoop.isPlaying) { mySpace.meta = false };
				curLoop.togglePlay;			
				^this;
			});
			if (cookieName == \rfBot and: { val == 0 }, {	 
				curMeta = curMeta.not;
				mySpace.meta = curMeta; 
			//	curLoop.rescaled_(curMeta);		?? really trun it off?
				("	MetaCtl for" + mySpace.name + curMeta).postln;
				^this;	
			});
		}); 		
				// switchOffsets ( specific for locID ;-)
		if (cookieName == \compass and: { val >= 0 }, { 

		////Joker special:: switch sample folders from gamepad/////////

			if ( modState[\midR], { 
				val.switch(
					0, { BufBank.loadFiles(true); },
					2, { BufBank.loadFiles },
					3, { BufBank.stepFolder(-1) },
					1, { BufBank.stepFolder }
				);
			}, {	
				"room: ".postc; wingAndRoom.put(1, val.postln); 
			});
			^this
		});	
		
////////////////////////////////rec/////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
		if ([\bt1, \bt2, \bt3, \bt4].includes(cookieName), { 
			if( val == 0, {
				if( modState[\midL], {								// Zweitnutzung zum Audio Aufnehmen
					recSynths[cookie] = { BufBank.arRec(
							BufBank.jamBufs[[\bt1, \bt2, \bt3, \bt4].indexOf(cookieName)].bufnum, 
							recChan, 
							1.5, 			//gain
							0
//							, if(cookie == 3, {1}, {0} )   // !@#@@@@@!!!!!???!!							// 1st button for loop recording
						)}.play; 
//					recSynths.postln;
					q[\recStartTime]=thisThread.seconds;
					[ "__recording: " ++ (cookieName) ].postln;  
				}, {
					"wing: ".postc; 
					wingAndRoom.put(0, ([\bt1, \bt2, \bt3, \bt4].indexOf(cookieName) * 4).postln); 
				});
			}, { 	
					// val 1; 
					if( modState[\midL], { 
// 	no switch off yet!!!! by now, only record a buffer full in no loop mode!
/*						if(cookie == 3, { recSynths.postln; recSynths[cookie].postln.free; 
							\rec_freed.postln }, 
							{/*recSynths[cookie].set(\limGain, 0)*/}); 
*/							// 1st button for loop recording
						recTime = (thisThread.seconds- q[\recStartTime]).min(BufBank.jamDur);
						q[\usedRatios].put(cookie - 3, (recTime / BufBank.jamDur) 
							? BufBank.jamDur);
						["_record ended" +(cookie-2), recTime ].postln; 
					});
			});
		});
/////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////eo rec///////////////////////////////////////
	}
}

Impact : GamePad { 
	classvar <vendorID = 1973; 
	
	*initClass { 
		Class.initClass(GamePad); 

		cookieMaps[vendorID] = TwoWayIdentityDictionary[ 
		
			\bt1 -> 3, 	// 4 buttons, righthand; up is 1, pressed is 0.
			\bt2-> 4, 
			\bt3-> 5, 
			\bt4 -> 6, 
			
			\lfTop -> 7, 	// 4 fire buttons, up 1, down 0
			\lfBot -> 8, 	
			\rfTop -> 9, 
			\rfBot -> 10,
			
			\midL -> 11, 	// middle shift buttons
			\midR -> 12, 
			\lHat -> 13, 	// hat switches on joysticks
			\rHat -> 14, 	
								// newer Impact is docd here, 
								// older is 0 - 255. 
			\joyLX -> 15, // joystick left x-axis (horizontal) left is 127, right is -128!
			\joyLY -> 16, // joy left y-axis, up is 127.
			\joyRX -> 18, // joy right x-axis, left is 127, right is -128!
			\joyRY -> 17, 
			
			\compass -> 19 // west is 2, south is 4, north is 8, east is 6
		];
		
		hidMaps[vendorID] = { arg locID, cookie, val; this.hidAction(locID, cookie, val); }; 

 
/*			// Impact silver, too! classic norm map
		normMaps[vendorID] = (
			joyLX: { |val| val - 127 / -255 },  // l = 127, r = -128
			joyLY: { |val| val + 128 /  255 },  //  o = 127, u = -128
			joyRX: { |val| val - 127 / -255 },  // l = 127, r = -128
			joyRY: { |val| val + 128 /  255 },  //  o = 127, u = -128
			compass: { |val| (2: 0, 4: 1, 6: 2, 8: 3, 9: -1)[val] }
		);

*/			// Impact silver! norm map
		normMaps[vendorID] = (
			joyLX: { |val| 1 + (val / -255) }, // l = 255, r = 0
			joyLY: { |val| val /  255 }, // u = 0, o = 255
			joyRX: { |val| 1 + (val / -255) }, // l = 255, r = 0
			joyRY: { |val| val /  255 }, // u = 0, o = 255
			compass: { |val| (2: 0, 4: 1, 6: 2, 8: 3, 9: -1)[val] }
		);
/*			// Impact old? norm map
		normMaps[vendorID] = (
			joyLX: { |val| val / -255 }, 
			joyLY: { |val| val /  255 }, 
			joyRX: { |val| val / -255 }, 
			joyRY: { |val| val /  255 }, 
			compass: { |val| (2: 0, 4: 1, 6: 2, 8: 3, 9: -1)[val] }
		);
			
			// Impact silver, too! classic norm map
		normMaps[vendorID] = (
			joyLX: { |val| val - 127 / -255 },  // l = 127, r = -128
			joyLY: { |val| val + 128 /  255 },  //  o = 127, u = -128
			joyRX: { |val| val - 127 / -255 },  // l = 127, r = -128
			joyRY: { |val| val + 128 /  255 },  //  o = 127, u = -128
			compass: { |val| (2: 0, 4: 1, 6: 2, 8: 3, 9: -1)[val] }
		);
*/
	}
}

ImpactNu : GamePad { 
	classvar <vendorID = 3888; 
	
	*initClass { 
		Class.initClass(GamePad); 

		cookieMaps[vendorID] = TwoWayIdentityDictionary[ 
		
			\bt1 -> 3, 	// 4 buttons, righthand; up is 1, pressed is 0.
			\bt2-> 4, 
			\bt3-> 5, 
			\bt4 -> 6, 
			
			\lfTop -> 7, 	// 4 fire buttons, up 1, down 0
			\lfBot -> 8, 	
			\rfTop -> 9, 
			\rfBot -> 10,
			
			\midL -> 11, 	// middle shift buttons
			\midR -> 12, 
			\lHat -> 13, 	// hat switches on joysticks
			\rHat -> 14, 	
								// newer Impact is docd here, 
								// older is 0 - 255. 
			\joyLX -> 15, // joystick left x-axis (horizontal) left is 127, right is -128!
			\joyLY -> 16, // joy left y-axis, up is 127.
			\joyRX -> 18, // joy right x-axis, left is 127, right is -128!
			\joyRY -> 17, 
			
			\compass -> 19 // west is 2, south is 4, north is 8, east is 6
		];
		
		hidMaps[vendorID] = { arg locID, cookie, val; this.hidAction(locID, cookie, val); }; 

 
			// Impact silver! norm map
		normMaps[vendorID] = (
			joyLX: { |val| 1 + (val / -255) }, // l = 255, r = 0
			joyLY: { |val| val /  255 }, // u = 0, o = 255
			joyRX: { |val| 1 + (val / -255) }, // l = 255, r = 0
			joyRY: { |val| val /  255 }, // u = 0, o = 255
			compass: { |val| (2: 0, 4: 1, 6: 2, 8: 3, 9: -1)[val] }
		);


	}
}

Betop : GamePad { 
	classvar <rangeMap, <vendorID = 3727; 

	*initClass { 
		Class.initClass(GamePad); 

		cookieMaps[vendorID] = TwoWayIdentityDictionary[ 
		
			\bt1 -> 4, 	// 4 buttons, righthand; up is 1, pressed is 0.
			\bt2 -> 5, 
			\bt3 -> 6, 
			\bt4 -> 7, 
			
			\lfTop -> 8, 	// 4 fire buttons, up 1, down 0
			\lfBot -> 10, 	
			\rfTop -> 9, 
			\rfBot -> 11,
			
			\midL -> 12, 	// middle shift buttons
			\midR -> 13, 
			\lHat -> 14, 	// hat switches on joysticks
			\rHat -> 15, 	
	
			\joyLX -> 19, // joystick left x-axis (horizontal) left is 255, right 0!
			\joyLY -> 20, // joy left y-axis, up is 255.
			\joyRX -> 18, // joystick right x-axis (horizontal) left is 255, right 0!
			\joyRY -> 17, // joy right x-axis, left is 127, right is -128!
			
			\compass -> 22 // west is 1, south is 3, north is 7, east is 5
		];
		
		hidMaps[vendorID] = { arg locID, cookie, val; this.hidAction(locID, cookie, val); }; 

		normMaps[vendorID] = (
			joyLX: { |val| 255 - val / 255 }, 
			joyLY: { |val| val /  255 }, 
			joyRX: { |val| 255 - val / 255 }, 
			joyRY: { |val| val /  255 }, 
			compass: { |val| (1: 0,3: 1, 5: 2, 7: 3, -8: -1)[val] }
		);
	}
}


Eaxus : GamePad { 
	classvar <rangeMap, <vendorID = 121; 

	*initClass { 
		Class.initClass(GamePad); 

		cookieMaps[vendorID] = TwoWayIdentityDictionary[ 
		
			\bt1 -> 4, 	// 4 buttons, righthand; up is 1, pressed is 0.
			\bt2 -> 5, 
			\bt3 -> 6, 
			\bt4 -> 7, 
			
			\lfTop -> 8, 	// 4 fire buttons, up 1, down 0
			\lfBot -> 10, 	
			\rfTop -> 9, 
			\rfBot -> 11,
			
			\midL -> 12, 	// middle shift buttons
			\midR -> 13, 
			\lHat -> 14, 	// hat switches on joysticks
			\rHat -> 15, 	
	
			\joyLX -> 19, // joystick left x-axis (horizontal) left is 255, right 0!
			\joyLY -> 18, // joy left y-axis, up is 255.
			\joyRX -> 20, // joystick right x-axis (horizontal) left is 255, right 0!
			\joyRY -> 21, // joy right x-axis, left is 127, right is -128!
			
			\compass -> 22 // west is 1, south is 3, north is 7, east is 5
		];
		
		hidMaps[vendorID] = { arg locID, cookie, val; this.hidAction(locID, cookie, val); }; 

		normMaps[vendorID] = (
			joyLX: { |val| 255 - val / 255 }, 
			joyLY: { |val| val /  255 }, 
			joyRX: { |val| 255 - val / 255 }, 
			joyRY: { |val| val /  255 }, 
			compass: { |val| (1: 0,3: 1, 5: 2, 7: 3, -8: -1)[val] }
		);
	}
}


Ferrari : GamePad { 
	classvar <rangeMap, <vendorID = 1103; 
// also transmits al vendorID 131  -- !!

	*initClass { 
		Class.initClass(GamePad); 

		cookieMaps[vendorID] = TwoWayIdentityDictionary[ 
		
			\bt1 -> 2, 	// 4 buttons, righthand; up is 1, pressed is 0.
			\bt2 -> 3, 
			\bt3 -> 4, 
			\bt4 -> 5, 
			
			// Ferrari has the bottom fire Buttons REALLY on the bottom!
			\lfTop -> 6, 	// 4 fire buttons, up 1, down 0 : 
			\lfBot -> 8, 	
			\rfTop -> 7, 
			\rfBot -> 9,
			
			\midL -> 10, 	// middle shift buttons
			\midR -> 11, 
			\lHat -> 12, 	// hat switches on joysticks
			\rHat -> 13, 	
	
			\joyLX -> 15, // joystick left x-axis (horizontal) left is 255, right 0!
			\joyLY -> 16, // joy left y-axis, up is 255.
			\joyRX -> 17, // joystick right x-axis (horizontal) left is 255, right 0!
			\joyRY -> 20, // joy right x-axis, left is 127, right is -128!

// Ferrari's extra analogs!
			\throtL -> 18, // joy left y-axis, up is 255.
			\throtR -> 19, // joystick right x-axis (horizontal) left is 255, right 0!
			\wheel -> 21, // joy right x-axis, left is 127, right is -128!
			
			\compass -> 14 // west is 1, south is 3, east is 5, north is 7
		];
		
		hidMaps[vendorID] = { arg locID, cookie, val; this.hidAction(locID, cookie, val); }; 

		normMaps[vendorID] = (
			joyLX: { |val| 255 - val / 255 }, 
			joyLY: { |val| val /  255 }, 
			joyRX: { |val| 255 - val / 255 }, 
			joyRY: { |val| val /  255 }, 
			throtL: { |val| val /  255 }, 
			throtR: { |val| val /  255 }, 
			wheel: { |val| val /  255 }, 
			compass: { |val| (1: 0, 3: 1, 5: 2, 7: 3, -8: -1)[val] }
		);
	}
}