/* 
	Jeff - just one scene
	cc, notes, sysex, aftertouch
(
	m = MPD24Ktl.new;
//	m.dump;

	m.mapCC(\k1, { 'yo!'.postln });	
	m.mapCC(\sl6, { |val| val.postln });	
	
	// vel is scaled from 0 - 127
	m.mapNoteOn(36, { |note, vel| [\noteOn, note, vel, "boom"].postln });
	m.mapNoteOn(39, { |note, vel| [\noteOn, note, vel, "tchak"].postln });
	m.mapNoteOff(36, { |note, vel| [\noteOff, note, vel, "moob"].postln });
	
	// sends mono touch only. 
	
	m.mapTouch({ |val| [\press, val].postln });
	m.mapTrans(\fwd, { \fwd.postln });
	m.mapTrans(\rew, { \rew.postln });
	m.mapTrans(\rec, { \rec.postln });
	m.mapTrans(\play, { \play.postln });
	m.mapTrans(\stop, { \stop.postln });
	
)
*/ 

MPD24Ktl : MIDINKtl { 	classvar <>verbose = false; 
	
	var <sysexDict, <touchFunc, <sysexResp, <touchResp;	var <pxmixer, <pxEditor, <pxOffset, <parOffset;	var <>softWithin = 0.05, <lastVals;		*initClass {		this.makeDefaults;	}

	*new { |srcID, chan = 0, ccDict, noteOnDict, noteOffDict, sysexDict| 
		^super.newCopyArgs(srcID, ccDict).init(chan, noteOnDict, noteOffDict, sysexDict );
	}
		init { 		super.init; 
		sysexDict = sysexDict ?? { () }; 
		touchResp = TouchResponder({ |src, chan, press| touchFunc.value(press / 127) });
		// sysexResp = SysexResponder ... // should be written at some point.
			// index 4 is crucial
		MIDIIn.sysex = { arg src, sysex;	sysexDict[sysex[4]].value.postln; };

		ctlNames = defaults[this.class];
		orderedCtlNames = ctlNames.keys.reject(_ == 'mode').asArray.sort.clump(9)[[2, 3, 1, 0]].flat.postcs;
		
		lastVals = ();

		pxOffset = 0;
		parOffset = 0;
				^this	}
	
	mapTouch { |func| touchFunc = func }

	mapTrans { |ctl, action| 
		var dictKey = ctlNames[ctl]; // '0_42'
		if (dictKey.isNil) { 
			warn("key % : no transport command found!\n".format(ctl));
			^this
		}; 
		sysexDict.put(dictKey, action);
	}	

//	mapToEnvirGui { |gui, scene, indices| 
//		var elementKeys; 
//		indices = indices ? (1..8); 
//		
//		elementKeys = orderedCtlNames[indices - 1].postcs; 
//				
//		elementKeys.do { |key, i|  	
//			this.mapCCS(scene, key, 
//				{ |ccval| 
//					var envir = gui.envir;
//					var parKey =  gui.editKeys[i];
//					var normVal = ccval / 127;
//					var lastVal = lastVals[key];
//					if (envir.notNil and: { parKey.notNil } ) { 
//						envir.softSet(parKey, normVal, softWithin, false, lastVal, gui.getSpec(parKey))
//					};
//					lastVals.put(key, normVal) ;
//				}
//			)
//		};
//	}
//
//		// map directly to a proxy's params. no Gui needed. 
//	mapToProxyParams { |scene = 2, proxy ... pairs| 
//		pairs.do { |pair| 
//			var ctlName, paramName, spec; 
//			#ctlName, paramName = pair;
//			spec = paramName.asSpec; 
//			
//			this.mapCCS(scene, ctlName, 
//				{  |ccval| 
//					proxy.softSet(paramName, ccval / 127, softWithin, false, lastVals[ctlName], spec)
//				}
//			);
//		};
//	}
//
//
//	mapToNdefGui { |gui, scene=1, volPause = true| 
//		pxEditors.put(scene, gui);
//		
//			// map 8 knobs to params - can be shifted
//		 [\kn1, \kn2, \kn3, \kn4, \kn5, \kn6, \kn7, \kn8].do { |key, i| 
//		 	
//			this.mapCCS(scene, key, 
//				{ |ccval| 
//					var proxy = pxEditors[scene].proxy;
//					var parKey =  pxEditors[scene].editKeys[i + parOffsets[scene]];
//					var normVal = ccval / 127;
//					var lastVal = lastVals[key];
//					if (parKey.notNil and: proxy.notNil) { 
//						proxy.softSet(parKey, normVal, softWithin, mapped: false, lastVal: lastVal) 
//					};
//					lastVals.put(key, normVal);
//				}
//			)
//		};
//			// and use 9th knob for proxy volume 
//		this.mapCCS(scene, \kn9, { |ccval| 
//			var lastVal = lastVals[\kn9];
//			var mappedVol = \amp.asSpec.map(ccval / 127);
//			var proxy = pxEditors[scene].proxy;
//			if (lastVal.notNil) { lastVal = \amp.asSpec.map(lastVal) };
//			if (proxy.notNil) { 
//				proxy.softVol_(mappedVol, softWithin, pause: volPause, lastVal: lastVal) 
//			};
//			lastVals[\kn9] = mappedVol;
//		} );
//	}
//	
//
//	mapToMixer { |mixer, scene = 1| 
//	
//		var server, mastaFunc, spec; 
//		pxmixers.put(scene, mixer); 
//		server = mixer.proxyspace.server;
//		
////		mastaFunc = Volume.softMasterVol(0.05, server, \midi.asSpec);
//		
//			spec = Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);
//			(1..4).do { |scene| this.mapCCS(scene, \sl9, 
//				{ |val| server.volume.volume_(spec.map(val/127)) }) };
//			
//			// scene 1: 
//
//			// map first 8 volumes to sliders
//		[\sl1, \sl2, \sl3, \sl4, \sl5, \sl6, \sl7, \sl8].do { |key, i| 
//			this.mapCCS(scene, key, 
//				{ |ccval| 
//					var lastVal = lastVals[key]; 
//					var ampSpec = \amp.asSpec;
//					var normVal = ccval / 127;
//					var mappedVal = ampSpec.map(normVal); 
//					var lastVol = if (lastVal.notNil) { ampSpec.map(lastVal) }; 
//
//					try { 
//						
//						pxmixers[scene].pxMons[i + pxOffsets[scene]].proxy
//							.softVol_( mappedVal, softWithin, true, lastVol, ampSpec ); 
//					};
//					
//					lastVals[key] =  normVal;
//				};
//			)
//		};
//			// upper buttons: send to editor
//		[\bu1, \bu2, \bu3, \bu4, \bu5, \bu6, \bu7, \bu8].do { |key, i| 
//			this.mapCCS(scene, key, { |ccval| 
//				defer { if (ccval > 0) { 
//						pxmixers[scene].pxMons[i + pxOffsets[scene]].edBut.doAction 
//				} }; 
//			})
//		};
//		
//			// lower buttons: toggle play/stop 
//		 [\bd1, \bd2, \bd3, \bd4, \bd5, \bd6, \bd7, \bd8].do { |key, i| 
//			this.mapCCS(scene, key, 
//				{ |ccval| defer { 
//
//					var pxGui = pxmixers[scene].pxMons[i + pxOffsets[scene]]; 
//					var playBut = pxGui.monitorGui.playBut;
//					var proxy = pxGui.proxy;
//					
//					 
//					if (proxy.notNil) {	 
//						if ( ctlNames[scene]['mode'] == 'push' ){
//							if (ccval == 127) { 
//								playBut.valueAction_(1 - playBut.value); // toggle on pushing
//							};
//						}; 
//						if ( ctlNames[scene]['mode'] == 'toggle' ) { 
//							playBut.valueAction_(ccval.sign);
//						};
//					}; 
//				};
//			});
//		};
//
//		this.mapCCS(scene, \bu9, { |ccval| if (ccval > 0) { this.pxShift(1, scene) } });
//		this.mapCCS(scene, \bd9, { |ccval| if (ccval > 0) { this.paramShift(1, scene) } });
//
//		this.pxShift(0, scene);		
//		this.mapToNdefGui(mixer.editor, scene, true);
//		this.paramShift(0, scene);
//	}
//
//	
//	
//		// old style
//
//	mapToPxEdit { |editor, scene=1, volPause = true| //		pxEditors.put(scene, editor);//		//			// map 8 knobs to params - can be shifted//		 [\kn1, \kn2, \kn3, \kn4, \kn5, \kn6, \kn7, \kn8].do { |key, i| 
//		 	//			this.mapCCS(scene, key, //				{ |ccval| //					var proxy = pxEditors[scene].proxy;
//					var parKey =  pxEditors[scene].editKeys[i + parOffsets[scene]];
//					var normVal = ccval / 127;
//					var lastVal = lastVals[key];
//					if (parKey.notNil and: proxy.notNil) { 
//						proxy.softSet(parKey, normVal, softWithin, lastVal: lastVal) 
//					};
//					lastVals.put(key, normVal);//				}//			)//		};//			// and use 9th knob for proxy volume //		this.mapCCS(scene, \kn9, { |ccval| 
//			var lastVal = lastVals[\kn9];
//			var mappedVol = \amp.asSpec.map(ccval / 127);
//			var proxy = pxEditors[scene].proxy;
//			if (lastVal.notNil) { lastVal = \amp.asSpec.map(lastVal) };//			if (proxy.notNil) { 
//				proxy.softVol_(mappedVol, softWithin, pause: volPause, lastVal: lastVal) 
//			};
//			lastVals[\kn9] = ccval / 127;//		} );//	}//
//
//
//		// support old style guis for a while longer//		// convenience method to map to a ProxyMixerOld//		//	mapToPxMix { |mixer, scene = 1| //	//		var server, mastaFunc; //		pxmixers.put(scene, mixer); //		server = mixer.proxyspace.server;//
//
////		mastaFunc = Volume.softMasterVol(0.05, server, \midi.asSpec);
//		//			Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);//			(1..4).do { |scene| this.mapCCS(scene, \sl9, mastaFunc) };//			//			// scene 1: ////			// map first 8 volumes to sliders//		[\sl1, \sl2, \sl3, \sl4, \sl5, \sl6, \sl7, \sl8].do { |key, i| //			this.mapCCS(scene, key, //				{ |ccval| 
//					var lastVal = lastVals[key]; 
//					var mappedVal = \amp.asSpec.map(ccval / 127); 
//					
//					var lastVol = if (lastVal.notNil) { \amp.asSpec.map(lastVal) }; //				//	[\lastVal, lastVal, \mappedVal, mappedVal].postcs;
//					try { 
//				//		"/// *** softVol_: ".post;
//								////////////// FIXXXXX
//						pxmixers[scene].pxMons[i + pxOffsets[scene]].proxy//						.softVol_( \amp.asSpec.map(mappedVal), softWithin, true, lastVol ); //					};
//					lastVals[key] =  mappedVal;//				};//			)//		};//			// upper buttons: send to editor//		[\bu1, \bu2, \bu3, \bu4, \bu5, \bu6, \bu7, \bu8].do { |key, i| //			this.mapCCS(scene, key, { |ccval| 
//											////////////// FIXXXXX
//				defer { if (ccval > 0) { pxmixers[scene].editBtnsAr[i + pxOffsets[scene]].doAction } }; 
//			})//		};//		//			// lower buttons: toggle play/stop //		 [\bd1, \bd2, \bd3, \bd4, \bd5, \bd6, \bd7, \bd8].do { |key, i| //			this.mapCCS(scene, key, //				{ |ccval| defer { 
//									////////////// FIXXXXX//					var px = pxmixers[scene].pxMons[i + pxOffsets[scene]].proxy;//					if ( ctlNames[scene]['mode'] == 'push' ){//						if (ccval == 127) { //							try { if (px.monitor.isPlaying) { px.stop } { px.play } };//						};//					}; 
//						////////////// FIXXXXX: support playN here as well! remote control button? //					if ( ctlNames[scene]['mode'] == 'toggle' ){//						if (ccval == 127) { px.play };//						if (ccval == 0) { px.stop };//					};//					};//				});//		};////		this.mapCCS(scene, \bu9, { |ccval| if (ccval > 0) { this.pxShift(1, scene) } });//		this.mapCCS(scene, \bd9, { |ccval| if (ccval > 0) { this.paramShift(1, scene) } });////		this.pxShift(0, scene);		//		this.mapToPxEdit(mixer.editor, scene);//		this.paramShift(0, scene);//	}//	//		// proxymixer shifting support: ////	pxShift { |step = 1, scene=1| //
//
//		var numActive = pxmixers[scene].pxMons.count { |mon| mon.proxy.notNil };//		var maxOff = (numActive - 8).max(0);//		var pxOffset = (pxOffsets[scene] + step).wrap(0, maxOff); //		pxOffsets[scene] = pxOffset;			
//		
//		pxmixers[scene].highlightSlots(pxOffset, 8);//	}//	//	paramShift { |step = 1, scene=1| 
//		//		var numActive = pxEditors[scene].editKeys.size;//		var maxOff = (numActive - 8).max(0);//		var parOffset = (parOffsets[scene] + step).wrap(0, maxOff); //		parOffsets[scene] = parOffset;
//		//		pxEditors[scene].highlightParams(parOffset, 8);//	}		*makeDefaults { 		// lookup for all scenes and ctlNames, \sl1, \kn1, \bu1, \bd1, 
				defaults.put(this, (				k1: '0_7', kn2: '0_8', kn3: '0_9', kn4: '0_10', kn5: '0_11', kn6: '0_12', kn7: '0_13', kn8: '0_14',
				sl1: '0_1',  sl2: '0_2',  sl3: '0_3',  sl4: '0_4',  sl5: '0_5',  sl6: '0_6', 
				rew: 5, fwd: 4, stop: 1, play: 2, rec: 6
			)
		);				}}