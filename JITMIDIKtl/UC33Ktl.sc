UC33Ktl : MIDIKtl { 	classvar <>verbose = false; 
		var <pxmixers, <pxEditors, <pxOffsets, <parOffsets;	var <>softWithin = 0.05, <lastVals;		*initClass {		this.makeDefaults;	}		init { 		super.init; 
		ctlNames = defaults[this.class];
		lastVals = ();

		pxmixers = ();		pxEditors = ();

		pxOffsets = (1: 0, 2: 0, 3: 0, 4: 0);
		parOffsets = (1: 0, 2: 0, 3: 0, 4: 0);
				^this	}			mapToPxEdit { |editor, scene=1, volPause = true| 		pxEditors.put(scene, editor);					// map 7 lowest knobs to params - can be shifted		 [\knl1, \knl2, \knl3, \knl4, \knl5, \knl6, \knl7].do { |key, i| 
		 				this.mapCCS(scene, key, 				{ |ccval| 					var proxy = pxEditors[scene].proxy;
					var parKey =  pxEditors[scene].editKeys[i + parOffsets[scene]];
					var normVal = ccval / 127;
					var lastVal = lastVals[key];
					if (parKey.notNil and: proxy.notNil) { 
						proxy.softSet(parKey, normVal, softWithin, lastVal: lastVal) 
					};
					lastVals.put(key, normVal);				}			)		};			// and use 8th knob for proxy volume 		this.mapCCS(scene, \knl8, { |ccval| 
			var lastVal = lastVals[\knl8];
			var mappedVol = \amp.asSpec.map(ccval / 127);
			var proxy = pxEditors[scene].proxy;
			if (lastVal.notNil) { lastVal = \amp.asSpec.map(lastVal) };			if (proxy.notNil) { 
				proxy.softVol_(mappedVol, softWithin, pause: volPause, lastVal: lastVal) 
			};
			lastVals[\knl8] = mappedVol;		} );	}			// no softSet - maybe add it.//	mapToPxPars { |scene = 2, proxy ... pairs| //		pairs.do { |pair| //			var ctlName, paramName; //			#ctlName, paramName = pair;//			this.mapCCS(scene, ctlName, //				{  |ch, cc, midival| //					proxy.set(paramName, paramName.asSpec.map(midival / 127))//				}//			);//		};//	}//	//		// convenience method to map to a proxymixer //		// could and should be refactored for more general use! 	mapToPxMix { |mixer, scene = 1| 			var server; 		pxmixers.put(scene, mixer); 		server = mixer.proxyspace.server;
	
			// fix later ...
			// add master volume to all 4 scenes, on slider 9: 		//	(1..4).do { |scene| { |ccval| server.volume.volume_(\mastaVol.asSpec.map(ccval/127)) } };						// scene 1: 			// map first 8 volumes to sliders		[\sl1, \sl2, \sl3, \sl4, \sl5, \sl6, \sl7, \sl8].do { |key, i| 			this.mapCCS(scene, key, 				{ |ccval| 
					var lastVal = lastVals[key]; 
					var mappedVal = \amp.asSpec.map(ccval / 127); 
					
					var lastVol = if (lastVal.notNil) { \amp.asSpec.map(lastVal) }; 				//	[\lastVal, lastVal, \mappedVal, mappedVal].postcs;
					try { 
				//		"/// *** softVol_: ".post;
						pxmixers[scene].pxMons[i + pxOffsets[scene]].proxy						.softVol_( \amp.asSpec.map(mappedVal), softWithin, true, lastVol ); 					};
					lastVals[key] =  mappedVal;				};			)		};		this.pxShift(0, scene);				this.mapToPxEdit(mixer.editor, scene);		this.paramShift(0, scene);	}				// proxymixer shifting support: 	pxShift { |step = 1, scene=1| 		{	 			var onCol = Color(1, 0.5, 0.5);			var offCol = Color.clear;			var numActive = pxmixers[scene].pxMons.count { |mon| mon.zone.visible == true };			var maxOff = (numActive - 8).max(0);			var pxOffset = (pxOffsets[scene] + step).wrap(0, maxOff); 			pxOffsets[scene] = pxOffset;					//	[ \pxOffset, pxOffset].postcs; 						pxmixers[scene].pxMons.do { |mong, i| 				var col = if (i >= pxOffset and: (i < (pxOffset + 8).max(0)), onCol, offCol); 				mong.nameView.background_(col.green_([0.5, 0.7].wrapAt(i - pxOffset div: 2)));				// write indices there as well			} 		}.defer;	}		paramShift { |step = 1, scene=1| 		{		var onCol = Color(1, 0.5, 0.5);		var offCol = Color.clear;		var numActive = pxEditors[scene].edits.count { |edi| edi.visible == true };		var maxOff = (numActive - 8).max(0);		var parOffset = (parOffsets[scene] + step).wrap(0, maxOff); 		parOffsets[scene] = parOffset;			//	[ \parOffset, parOffset].postcs; 				pxEditors[scene].edits.do { |edi, i| 			var col = if (i >= parOffset and: (i < (parOffset + 8).max(0)), onCol, offCol); 			edi.labelView.background_(col.green_([0.5, 0.7].wrapAt(i - parOffset div: 2)));		} }.defer;	}		*makeDefaults {		// lookup for all scenes and ctlNames, \sl1, \kn1, \bu1, \bd1, 
				defaults.put(this, (				// general controls that do not change with scenes - if any	//		0: (			),				// controls in scene 1			1: (				mode: 'toggle', 
				knu1: '0_13', knu2: '1_13', knu3: '2_13', knu4: '3_13', knu5: '4_13', knu6: '5_13', knu7: '6_13', knu8: '7_13', 
				knm1: '0_12', knm2: '1_12', knm3: '2_12', knm4: '3_12', knm5: '4_12', knm6: '5_12', knm7: '6_12', knm8: '7_12', 
				knl1: '0_10', knl2: '1_10', knl3: '2_10', knl4: '3_10', knl5: '4_10', knl6: '5_10', knl7: '6_10', knl8: '7_10', 
				 sl1: '0_7' ,  sl2: '1_7' ,  sl3: '2_7' ,  sl4: '3_7' ,  sl5: '4_7' ,  sl6: '5_7' ,  sl7: '6_7' ,  sl8: '7_7' , 
				 
				 sl9: '0_28',
				 
				but1: '0_19', but2: '0_20', but3: '0_21', 
				but4: '0_22', but5: '0_23', but6: '0_24', 
				but7: '0_25', but8: '0_26', but9: '0_27', 
							but0: '0_18', 
							
				stop: '0_14', play: '0_15', rew: '0_16', fwd: '0_17' 
			),
			2: (
				mode: 'toggle', 
				knu1: '0_70', knu2: '0_71', knu3: '0_76', knu4: '0_7',  knu5: '0_81', knu6: '0_82', knu7: '0_83', knu8: '0_80', 
				knm1: '0_72', knm2: '0_75', knm3: '0_78', knm4: '0_79', knm5: '0_91', knm6: '0_92', knm7: '0_93', knm8: '0_90', 
				knl1: '0_73', knl2: '0_74', knl3: '0_11', knl4: '0_119', knl5: '0_8', knl6: '0_10', knl7: '0_9' , knl8: '0_3' , 
				 sl1: '0_12',  sl2: '0_13',  sl3: '0_14',  sl4: '0_15',  sl5: '0_16',  sl6: '0_17',  sl7: '0_18',  sl8: '0_19', 
				 
				 sl9: '0_20',
				 
				but1: '0_30', but2: '0_31', but3: '0_69',
			//	but4: '0_0' , but5: '0_23', but6: '0_24', // send 0_32, 0 and something on 0_0, 127 - no idea what this is for.
			//	but7: '0_25', but8: '0_26', but9: '0_27', // no simple way to map that, so disable them.
			//				but0: '0_119', // same as kn04 - better disable
							
				stop: '0_1', play: '0_67', rew: '0_68', fwd: '0_66' 
			),
			3: (
				mode: 'toggle', 
				knu1: '0_20', knu2: '0_21', knu3: '0_40', knu4: '0_43', knu5: '0_70', knu6: '0_71', knu7: '0_72', knu8: '0_73', 
				knm1: '0_50', knm2: '0_51', knm3: '0_55', knm4: '0_87', knm5: '0_75', knm6: '0_76', knm7: '0_77', knm8: '0_78', 
				knl1: '0_45', knl2: '0_46', knl3: '0_47', knl4: '0_5' , knl5: '0_80', knl6: '0_81', knl7: '0_82', knl8: '0_83', 
				sl1: '0_105', sl2: '0_106', sl3: '0_107', sl4: '0_108', sl5: '0_110', sl6: '0_111', sl7: '0_112', sl8: '0_116', 
				 
				 sl9: '0_26',
				 
				but1: '0_22', but2: '0_23', but3: '0_24', 
				but4: '0_41', but5: '0_42', but6: '0_44', 
				but7: '0_52', but8: '0_53', but9: '0_54', 
							but0: '0_57', 
							
				stop: '0_25', play: '0_27', rew: '0_28', fwd: '0_29' 
			),
			4: (
				mode: 'toggle', 
				knu1: '0_53', knu2: '0_54', knu3: '0_55', knu4: '0_56', knu5: '0_57', knu6: '0_58', knu7: '0_59', knu8: '0_60', 
				knm1: '0_39', knm2: '0_40', knm3: '0_41', knm4: '0_42', knm5: '0_43', knm6: '0_44', knm7: '0_45', knm8: '0_46', 
				knl1: '0_23', knl2: '0_24', knl3: '0_25', knl4: '0_26', knl5: '0_27', knl6: '0_28', knl7: '0_29', knl8: '0_30', 
				 sl1: '0_8' ,  sl2: '0_9',  sl3:  '0_10',  sl4: '0_12',  sl5: '0_13',  sl6: '0_14',  sl7: '0_15',  sl8: '0_16', 
				 
				 sl9: '0_7'
			)

		));	}}