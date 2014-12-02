PDKtl : MIDIKtl {
	classvar <>verbose = false; 

	var <>softWithin = 0.05, <lastVals;	// for normal mode
	var <>step, <>endless;				// for endless mode
	
	*new { |srcID, ccDict, endless = false, step = 0.01| 
		^super.newCopyArgs(srcID, ccDict).endless_(endless).step_(step).init;
	}
	
	init { 
		super.init;
		lastVals = ();
	}
	
	*makeDefaults { 
		/*	all midi chan 0, 
		scene 1: 0 - 15 
		scene2: 	16 - 31 
		scene 3: 32 - 47
		scene4: 48 - 63
		*/

		// just one bank of knobs
		defaults.put(this, (
			1: 	(	
				kn01: '0_0', kn02: '0_1', kn03: '0_2', kn04: '0_3', kn05: '0_4', kn06: '0_5', kn07: '0_6', kn08: '0_7',
				kn09: '0_8', kn10: '0_9', kn11: '0_10', kn12: '0_11', kn13: '0_12', kn14: '0_13', kn15: '0_14',kn16: '0_15'
			),

			2: 	(	
				kn01: '0_16', kn02: '0_17', kn03: '0_18', kn04: '0_19', kn05: '0_20', kn06: '0_21', kn07: '0_22', kn08: '0_23', 
				kn09: '0_24', kn10: '0_25', kn11: '0_26', kn12: '0_27', kn13: '0_28', kn14: '0_29', kn15: '0_30',kn16: '0_31'
			),

			3: 	(
				kn01: '0_32', kn02: '0_33', kn03: '0_34', kn04: '0_35', kn05: '0_36', kn06: '0_37', kn07: '0_38', kn08: '0_39', 
				kn09: '0_40', kn10: '0_41', kn11: '0_42', kn12: '0_43', kn13: '0_44', kn14: '0_45', kn15: '0_46',kn16: '0_47'
			),

			4: 	(
				kn01: '0_48', kn02: '0_49', kn03: '0_50', kn04: '0_51', kn05: '0_52', kn06: '0_53', kn07: '0_54', kn08: '0_55', 
				kn09: '0_56', kn10: '0_57', kn11: '0_58', kn12: '0_59', kn13: '0_60', kn14: '0_61', kn15: '0_62',kn16: '0_63'
			)
		));
	}
	
		// map to 
	mapToPxEdit { |editor, scene = 1, indices, lastIsVol = true| 
		var elementKeys, lastKey; 
		indices = indices ? (1..8); 
		
		elementKeys = ctlNames[scene].keys.asArray.sort[indices - 1]; 

		
		if (endless.not) { 
			
			if (lastIsVol) { 
				lastKey = elementKeys.pop;
				
					// use last slider for proxy volume
				this.mapCCS(scene, lastKey, { |ch, cc, val| 
					var lastVal = lastVals[lastKey];
					var mappedVol = \amp.asSpec.map(val / 127);
					var proxy = editor.proxy;
					if (proxy.notNil) { proxy.softVol_(mappedVol, softWithin, lastVal: lastVal) };
					lastVals[lastKey] = mappedVol;
				});
			};
			
			elementKeys.do { |key, i|  	
				this.mapCCS(scene, key, 
					{ |ccval| 
						var proxy = editor.proxy;
						var parKey =  editor.editKeys[i];
						var normVal = ccval / 127;
						var lastVal = lastVals[key];
						if (parKey.notNil and: proxy.notNil) { 
							proxy.softSet(parKey, normVal, softWithin, lastVal: lastVal) 
						};
						lastVals.put(key, normVal);
					}
				)
			};
			
		} { 
				// endless
			if (lastIsVol) { 
				lastKey = elementKeys.pop;
				
					// use last knob for proxy volume
				this.mapCCS(scene, lastKey, { |ccval| 
					var proxy = editor.proxy;
					if (proxy.notNil) { proxy.nudgeVol(ccval - 64 * step) };
				});
			};
			
			elementKeys.do { |key, i|  	
				this.mapCCS(scene, key, 
					{ |ccval| 
						var proxy = editor.proxy;
						var parKey =  editor.editKeys[i];
						if (parKey.notNil and: proxy.notNil) { 
							proxy.nudgeSet(parKey, ccval - 64 * step) 
						};
					}
				)
			}
		}
	}
	
	mapToPxMix { |mixer, scene = 1, splitIndex = 8, lastEdIsVol = true, lastIsMaster = true| 
 	
		var server = mixer.proxyspace.server;
		var elementKeys = ctlNames[scene].keys.asArray.sort; 
		var lastKey; 
		
		if (endless.not) { 
					// add master volume on slider 16
			if (lastIsMaster) { 
				lastKey = elementKeys.pop; 
				Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);
				this.mapCCS(scene, lastKey, { |ccval| server.volume.volume_(\mastaVol.asSpec.map(ccval/127)) });
			};			
	
				// map first n sliders to volumes
			elementKeys.keep(splitIndex).do { |key, i| 
				this.mapCCS(scene, key, 
					{ |ccval| 
						var proxy = mixer.pxMons[i].proxy; 
						var lastVal, mappedVal, lastVol;
						if (proxy.notNil) { 
							lastVal = lastVals[key]; 
							mappedVal = \amp.asSpec.map(ccval / 127); 
							lastVol = if (lastVal.notNil) { \amp.asSpec.map(lastVal) }; 
							proxy.softVol_( \amp.asSpec.map(mappedVal), softWithin, true, lastVol ); 
						};
						lastVals[key] =  mappedVal;
					};
				)
			};
			
		} { 			// endless mode:
					// add master volume on knob 16
					// nudging master vol not working yet
//			if (lastIsMaster) { 
//				lastKey = elementKeys.pop; 
//				Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);
//				this.mapCCS(scene, lastKey, { |ccval| server.volume.volume_(\mastaVol.asSpec.map(ccval/127)) });
//			};			
	
				// map first n knobs to volumes
			elementKeys.keep(splitIndex).do { |key, i| 
				
				this.mapCCS(scene, key, 
					{ |ccval| 
						var proxy = mixer.pxMons[i].proxy; 
						if (proxy.notNil) { 
							proxy.nudgeVol(ccval - 64 * step); 
						};
					};
				)
			};
		
		};
		
		this.mapToPxEdit(mixer.editor, scene, (splitIndex + 1 .. elementKeys.size));
	}
}
