
NanoKtl : MIDIKtl { 	classvar <>verbose = false; 
		var <pxmixers, <pxEditors, <pxOffsets, <parOffsets;	var <>softWithin = 0.05, <lastVals;		*initClass {		this.makeDefaults;	}		init { 		super.init; 
		ctlNames = defaults[this.class];
		orderedCtlNames = ctlNames[1].keys.reject(_ == 'mode').asArray.sort.clump(9)[[2, 3, 1, 0]].flat.postcs;
		
		lastVals = ();

		pxmixers = ();		pxEditors = ();

		pxOffsets = (1: 0, 2: 0, 3: 0, 4: 0);
		parOffsets = (1: 0, 2: 0, 3: 0, 4: 0);
				^this	}

	mapToEnvirGui { |gui, scene, indices| 
		var elementKeys; 
		indices = indices ? (1..8); 
		
		elementKeys = orderedCtlNames[indices - 1].postcs; 
				
		elementKeys.do { |key, i|  	
			this.mapCCS(scene, key, 
				{ |ccval| 
					var envir = gui.envir;
					var parKey =  gui.editKeys[i];
					var normVal = ccval / 127;
					var lastVal = lastVals[key];
					if (envir.notNil and: { parKey.notNil } ) { 
						envir.softSet(parKey, normVal, softWithin, false, lastVal, gui.getSpec(parKey))
					};
					lastVals.put(key, normVal) ;
				}
			)
		};
	}

		// map directly to a proxy's params. no Gui needed. 
	mapToProxyParams { |scene = 2, proxy ... pairs| 
		pairs.do { |pair| 
			var ctlName, paramName, spec; 
			#ctlName, paramName = pair;
			spec = paramName.asSpec; 
			
			this.mapCCS(scene, ctlName, 
				{  |ccval| 
					proxy.softSet(paramName, ccval / 127, softWithin, false, lastVals[ctlName], spec)
				}
			);
		};
	}


	mapToNdefGui { |gui, scene=1, volPause = true| 
		pxEditors.put(scene, gui);
		
			// map 8 knobs to params - can be shifted
		 [\kn1, \kn2, \kn3, \kn4, \kn5, \kn6, \kn7, \kn8].do { |key, i| 
		 	
			this.mapCCS(scene, key, 
				{ |ccval| 
					var proxy = pxEditors[scene].proxy;
					var parKey =  pxEditors[scene].editKeys[i + parOffsets[scene]];
					var normVal = ccval / 127;
					var lastVal = lastVals[key];
					if (parKey.notNil and: proxy.notNil) { 
						proxy.softSet(parKey, normVal, softWithin, mapped: false, lastVal: lastVal) 
					};
					lastVals.put(key, normVal);
				}
			)
		};
			// and use 9th knob for proxy volume 
		this.mapCCS(scene, \kn9, { |ccval| 
			var lastVal = lastVals[\kn9];
			var mappedVol = \amp.asSpec.map(ccval / 127);
			var proxy = pxEditors[scene].proxy;
			if (lastVal.notNil) { lastVal = \amp.asSpec.map(lastVal) };
			if (proxy.notNil) { 
				proxy.softVol_(mappedVol, softWithin, pause: volPause, lastVal: lastVal) 
			};
			lastVals[\kn9] = mappedVol;
		} );
	}
	

	mapToMixer { |mixer, scene = 1| 
	
		var server, mastaFunc, spec; 
		pxmixers.put(scene, mixer); 
		server = mixer.proxyspace.server;
		
//		mastaFunc = Volume.softMasterVol(0.05, server, \midi.asSpec);
		
			spec = Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);
			(1..4).do { |scene| this.mapCCS(scene, \sl9, 
				{ |val| server.volume.volume_(spec.map(val/127)) }) };
			
			// scene 1: 

			// map first 8 volumes to sliders
		[\sl1, \sl2, \sl3, \sl4, \sl5, \sl6, \sl7, \sl8].do { |key, i| 
			this.mapCCS(scene, key, 
				{ |ccval| 
					var lastVal = lastVals[key]; 
					var ampSpec = \amp.asSpec;
					var normVal = ccval / 127;
					var mappedVal = ampSpec.map(normVal); 
					var lastVol = if (lastVal.notNil) { ampSpec.map(lastVal) }; 

					try { 
						
						pxmixers[scene].pxMons[i + pxOffsets[scene]].proxy
							.softVol_( mappedVal, softWithin, true, lastVol, ampSpec ); 
					};
					
					lastVals[key] =  normVal;
				};
			)
		};
			// upper buttons: send to editor
		[\bu1, \bu2, \bu3, \bu4, \bu5, \bu6, \bu7, \bu8].do { |key, i| 
			this.mapCCS(scene, key, { |ccval| 
				defer { if (ccval > 0) { 
						pxmixers[scene].pxMons[i + pxOffsets[scene]].edBut.doAction 
				} }; 
			})
		};
		
			// lower buttons: toggle play/stop 
		 [\bd1, \bd2, \bd3, \bd4, \bd5, \bd6, \bd7, \bd8].do { |key, i| 
			this.mapCCS(scene, key, 
				{ |ccval| defer { 

					var pxGui = pxmixers[scene].pxMons[i + pxOffsets[scene]]; 
					var playBut = pxGui.monitorGui.playBut;
					var proxy = pxGui.proxy;
					
					 
					if (proxy.notNil) {	 
						if ( ctlNames[scene]['mode'] == 'push' ){
							if (ccval == 127) { 
								playBut.valueAction_(1 - playBut.value); // toggle on pushing
							};
						}; 
						if ( ctlNames[scene]['mode'] == 'toggle' ) { 
							playBut.valueAction_(ccval.sign);
						};
					}; 
				};
			});
		};

		this.mapCCS(scene, \bu9, { |ccval| if (ccval > 0) { this.pxShift(1, scene) } });
		this.mapCCS(scene, \bd9, { |ccval| if (ccval > 0) { this.paramShift(1, scene) } });

		this.pxShift(0, scene);		
		this.mapToNdefGui(mixer.editor, scene, true);
		this.paramShift(0, scene);
	}

	
	
		// old style

	mapToPxEdit { |editor, scene=1, volPause = true| 		pxEditors.put(scene, editor);					// map 8 knobs to params - can be shifted		 [\kn1, \kn2, \kn3, \kn4, \kn5, \kn6, \kn7, \kn8].do { |key, i| 
		 				this.mapCCS(scene, key, 				{ |ccval| 					var proxy = pxEditors[scene].proxy;
					var parKey =  pxEditors[scene].editKeys[i + parOffsets[scene]];
					var normVal = ccval / 127;
					var lastVal = lastVals[key];
					if (parKey.notNil and: proxy.notNil) { 
						proxy.softSet(parKey, normVal, softWithin, lastVal: lastVal) 
					};
					lastVals.put(key, normVal);				}			)		};			// and use 9th knob for proxy volume 		this.mapCCS(scene, \kn9, { |ccval| 
			var lastVal = lastVals[\kn9];
			var mappedVol = \amp.asSpec.map(ccval / 127);
			var proxy = pxEditors[scene].proxy;
			if (lastVal.notNil) { lastVal = \amp.asSpec.map(lastVal) };			if (proxy.notNil) { 
				proxy.softVol_(mappedVol, softWithin, pause: volPause, lastVal: lastVal) 
			};
			lastVals[\kn9] = ccval / 127;		} );	}


		// support old style guis for a while longer		// convenience method to map to a ProxyMixerOld			mapToPxMix { |mixer, scene = 1| 			var server, mastaFunc; 		pxmixers.put(scene, mixer); 		server = mixer.proxyspace.server;

//		mastaFunc = Volume.softMasterVol(0.05, server, \midi.asSpec);
					Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);			(1..4).do { |scene| this.mapCCS(scene, \sl9, mastaFunc) };						// scene 1: 			// map first 8 volumes to sliders		[\sl1, \sl2, \sl3, \sl4, \sl5, \sl6, \sl7, \sl8].do { |key, i| 			this.mapCCS(scene, key, 				{ |ccval| 
					var lastVal = lastVals[key]; 
					var mappedVal = \amp.asSpec.map(ccval / 127); 
					
					var lastVol = if (lastVal.notNil) { \amp.asSpec.map(lastVal) }; 				//	[\lastVal, lastVal, \mappedVal, mappedVal].postcs;
					try { 
				//		"/// *** softVol_: ".post;
								////////////// FIXXXXX
						pxmixers[scene].pxMons[i + pxOffsets[scene]].proxy						.softVol_( \amp.asSpec.map(mappedVal), softWithin, true, lastVol ); 					};
					lastVals[key] =  mappedVal;				};			)		};			// upper buttons: send to editor		[\bu1, \bu2, \bu3, \bu4, \bu5, \bu6, \bu7, \bu8].do { |key, i| 			this.mapCCS(scene, key, { |ccval| 
											////////////// FIXXXXX
				defer { if (ccval > 0) { pxmixers[scene].editBtnsAr[i + pxOffsets[scene]].doAction } }; 
			})		};					// lower buttons: toggle play/stop 		 [\bd1, \bd2, \bd3, \bd4, \bd5, \bd6, \bd7, \bd8].do { |key, i| 			this.mapCCS(scene, key, 				{ |ccval| defer { 
									////////////// FIXXXXX					var px = pxmixers[scene].pxMons[i + pxOffsets[scene]].proxy;					if ( ctlNames[scene]['mode'] == 'push' ){						if (ccval == 127) { 							try { if (px.monitor.isPlaying) { px.stop } { px.play } };						};					}; 
						////////////// FIXXXXX: support playN here as well! remote control button? 					if ( ctlNames[scene]['mode'] == 'toggle' ){						if (ccval == 127) { px.play };						if (ccval == 0) { px.stop };					};					};				});		};		this.mapCCS(scene, \bu9, { |ccval| if (ccval > 0) { this.pxShift(1, scene) } });		this.mapCCS(scene, \bd9, { |ccval| if (ccval > 0) { this.paramShift(1, scene) } });		this.pxShift(0, scene);				this.mapToPxEdit(mixer.editor, scene);		this.paramShift(0, scene);	}			// proxymixer shifting support: 	pxShift { |step = 1, scene=1| 

		var numActive = pxmixers[scene].pxMons.count { |mon| mon.proxy.notNil };		var maxOff = (numActive - 8).max(0);		var pxOffset = (pxOffsets[scene] + step).wrap(0, maxOff); 		pxOffsets[scene] = pxOffset;			
		
		pxmixers[scene].highlightSlots(pxOffset, 8);	}		paramShift { |step = 1, scene=1| 
				var numActive = pxEditors[scene].editKeys.size;		var maxOff = (numActive - 8).max(0);		var parOffset = (parOffsets[scene] + step).wrap(0, maxOff); 		parOffsets[scene] = parOffset;
				pxEditors[scene].highlightParams(parOffset, 8);	}		*makeDefaults { 		// lookup for all scenes and ctlNames, \sl1, \kn1, \bu1, \bd1, 
				defaults.put(this, (				// general controls that do not change with scenes:			0: (				rew: '0_47',	play: '0_45', fwd: '0_48',				loop: '0_49', stop: '0_46', rec: '0_44'			),				// controls in the scenes 1, 2, 3, 4:			1: (				mode: 'push',				kn1: '0_14', kn2: '0_15', kn3: '0_16', kn4: '0_17', kn5: '0_18', kn6: '0_19', kn7: '0_20', kn8: '0_21', kn9: '0_22',
				sl1: '0_2',  sl2: '0_3',  sl3: '0_4',  sl4: '0_5',  sl5: '0_6',  sl6: '0_8',  sl7: '0_9',  sl8: '0_12', sl9: '0_13',
				bu1: '0_23', bu2: '0_24', bu3: '0_25', bu4: '0_26', bu5: '0_27', bu6: '0_28', bu7: '0_29', bu8: '0_30', bu9: '0_31',
				bd1: '0_33', bd2: '0_34', bd3: '0_35', bd4: '0_36', bd5: '0_37', bd6: '0_38', bd7: '0_39', bd8: '0_40', bd9: '0_41'
			), 									2: (
				mode: 'push', 
				kn1: '0_57', kn2: '0_58', kn3: '0_59', kn4: '0_60', kn5: '0_61', kn6: '0_62', kn7: '0_63', kn8: '0_65', kn9: '0_66',
				sl1: '0_42', sl2: '0_43', sl3: '0_50', sl4: '0_51', sl5: '0_52', sl6: '0_53', sl7: '0_54', sl8: '0_55', sl9: '0_56',				bu1: '0_67', bu2: '0_68', bu3: '0_69', bu4: '0_70', bu5: '0_71', bu6: '0_72', bu7: '0_73', bu8: '0_74', bu9: '0_75',				bd1: '0_76', bd2: '0_77', bd3: '0_78', bd4: '0_79', bd5: '0_80', bd6: '0_81', bd7: '0_82', bd8: '0_83', bd9: '0_84'			),						3: (
				mode: 'push',	
				kn1: '0_94',  kn2: '0_95', kn3:  '0_96',  kn4: '0_97',  kn5: '0_102', kn6: '0_103', kn7: '0_104', kn8: '0_105', kn9: '0_106',
				sl1: '0_85',  sl2: '0_86', sl3:  '0_87',  sl4: '0_88',  sl5: '0_89',  sl6: '0_90',  sl7: '0_91', sl8:  '0_92',  sl9: '0_93',				bu1: '0_107', bu2: '0_108', bu3: '0_109', bu4: '0_110', bu5: '0_111', bu6: '0_112', bu7: '0_113', bu8: '0_114', bu9: '0_115',				bd1: '0_116', bd2: '0_117', bd3: '0_118', bd4: '0_119', bd5: '0_120', bd6: '0_121', bd7: '0_122', bd8: '0_123', bd9: '0_124'			),						4: (
				mode: 'toggle', 
				kn1: '0_10', kn2: '1_10', kn3: '2_10', kn4: '3_10', kn5: '4_10', kn6: '5_10', kn7: '6_10', kn8: '7_10', kn9: '8_10',				sl1: '0_7',  sl2: '1_7',  sl3: '2_7',  sl4: '3_7',  sl5: '4_7',  sl6: '5_7',  sl7: '6_7',  sl8: '7_7',  sl9: '8_7',			// buttons toggle!
				bu1: '0_16', bu2: '1_16', bu3: '2_16', bu4: '3_16', bu5: '4_16', bu6: '5_16', bu7: '6_16', bu8: '7_16', bu9: '8_16',				bd1: '0_17', bd2: '1_17', bd3: '2_17', bd4: '3_17', bd5: '4_17', bd6: '5_17', bd7: '6_17', bd8: '7_17', bd9: '8_17'			)		));	}}