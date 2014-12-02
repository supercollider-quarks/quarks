PFKtl : MIDIKtl {
	classvar <>verbose = false; 
	var <>softWithin = 0.05, <lastVals;
	var <valRange, <minval, <range;

	init { 
		super.init; 
		orderedCtlNames = ctlNames.keys.asArray.sort;
		lastVals = ();
		this.valRange = [0, 127];
	}
	
	norm { |val| ^val - minval / range }
	
	valRange_ { |inRange| 
		range = inRange[1] - inRange[0];
		minval = inRange[0];
		valRange = inRange;
	}

	*makeDefaults { 

		// just one bank of sliders
		defaults.put(this, 
			(
				sl01: '0_7', sl02: '1_7', sl03: '2_7', sl04: '3_7', 
				sl05: '4_7', sl06: '5_7', sl07: '6_7', sl08: '7_7', 
				sl09: '8_7', sl10: '9_7', sl11: '10_7', sl12: '11_7', 
				sl13: '12_7', sl14: '13_7', sl15: '14_7',sl16: '15_7'
			)
		);
	}

	mapToEnvirGui { |gui, indices| 
		var elementKeys; 
		indices = indices ? (1..8); 
		
		elementKeys = orderedCtlNames[indices - 1].postcs; 
				
		elementKeys.do { |key, i|  	
			this.mapCC(key, 
				{ |ccval| 
					var envir = gui.envir;
					var parKey =  gui.editKeys[i];
					var normVal = this.norm(ccval);
					var lastVal = lastVals[key];
					if (envir.notNil and: { parKey.notNil } ) { 
						envir.softSet(parKey, normVal, softWithin, false, lastVal, gui.getSpec(parKey))
					};
					lastVals.put(key, normVal) ;
				}
			)
		};
	}
	
	mapToNdefGui { |gui, indices, lastIsVol = true| 
		var elementKeys, lastKey; 
		indices = indices ? (1..8); 
		
		elementKeys = orderedCtlNames[indices - 1].postcs; 
		
		if (lastIsVol) { 
			lastKey = elementKeys.pop;
			indices.pop;
			
				// use last slider for proxy volume
			this.mapCC(lastKey, { |ccval| 
				var lastVal = lastVals[lastKey];
				var mappedVol = \amp.asSpec.map(this.norm(ccval));
				var proxy = gui.proxy;
				if (proxy.notNil) { proxy.softVol_(mappedVol, softWithin, lastVal: lastVal) };
				lastVals[lastKey] = mappedVol;
			});
		};
		
		this.mapToEnvirGui(gui.paramGui, indices);
	}
	
	mapToPdefGui { |gui, indices| 
		this.mapToEnvirGui(gui.envirGui, indices);
	}
	
	mapToTdefGui { |gui, indices| 
		this.mapToEnvirGui(gui.envirGui, indices);
	}
		
	mapToMixer { |mixer, numVols = 8, indices, lastEdIsVol = true, lastIsMaster = true| 
 	
		var server = mixer.proxyspace.server;
		var 	elementKeys, lastKey, spec;

		indices = indices ? (1..16); 
		elementKeys = orderedCtlNames[indices - 1]; 
		
				// add master volume on slider 16
		if (lastIsMaster) { 
			lastKey = elementKeys.pop; 
			spec = Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);
			// this.mapCC(lastKey, Volume.softMasterVol(0.05, server, \midi.asSpec));
			this.mapCC(lastKey, { |ccval| server.volume.volume_(spec.map(this.norm(ccval))) });
		};			

			// map first n sliders to volumes
		elementKeys.keep(numVols).do { |key, i| 
			this.mapCC(key, 
				{ |ccval| 
					var proxy = mixer.arGuis[i].proxy; 
					var lastVal, mappedVal, lastVol;
					var spec = \amp.asSpec;
					if (proxy.notNil) { 
						lastVal = lastVals[key]; 
						mappedVal = spec.map(this.norm(ccval)); 
						lastVol = if (lastVal.notNil) { spec.asSpec.map(lastVal) }; 
						proxy.softVol_(spec.map(mappedVal), softWithin, true, lastVol ); 
					};
				//	[key, proxy.key, mappedVal].postcs;
					lastVals[key] =  mappedVal;
				};
			)
		};
		
		this.mapToNdefGui(mixer.editGui, (numVols + 1 .. elementKeys.size), lastEdIsVol);
	}
	
	
	
	
			// map to 
	mapToPxEdit { |editor, indices, lastIsVol = true| 
		var elementKeys, lastKey; 
		indices = indices ? (1..8); 
		
		elementKeys = ctlNames.keys.asArray.sort[indices - 1]; 
		
		if (lastIsVol) { 
			lastKey = elementKeys.pop;
			
				// use last slider for proxy volume
			this.mapCC(lastKey, { |ccval| 
				var lastVal = lastVals[lastKey];
				var mappedVol = \amp.asSpec.map(this.norm(ccval));
				var proxy = editor.proxy;
				if (proxy.notNil) { proxy.softVol_(mappedVol, softWithin, lastVal: lastVal) };
				lastVals[lastKey] = mappedVol;
			});
		};
		
		elementKeys.do { |key, i|  	
			this.mapCC(key, 
				{ |ccval| 
					var proxy = editor.proxy;
					var parKey =  editor.editKeys[i];
					var normVal = this.norm(ccval);
					var lastVal = lastVals[key];
					if (parKey.notNil and: proxy.notNil) { 
						proxy.softSet(parKey, normVal, softWithin, lastVal: lastVal) 
					};
					lastVals.put(key, normVal);
				}
			)
		};
	}
	
	mapToPxMix { |mixer, splitIndex = 8, lastEdIsVol = true, lastIsMaster = true| 
 	
		var server = mixer.proxyspace.server;
		var 	elementKeys = ctlNames.keys.asArray.sort; 
		var lastKey; 
		
				// add master volume on slider 16
		if (lastIsMaster) { 
			lastKey = elementKeys.pop; 
			Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);
			this.mapCC(lastKey, { |ccval| server.volume.volume_(\mastaVol.asSpec.map(ccval/127)) });
		};			

			// map first n sliders to volumes
		elementKeys.keep(splitIndex).do { |key, i| 
			this.mapCC(key, 
				{ |ccval| 
					var proxy = mixer.pxMons[i].proxy; 
					var lastVal, mappedVal, lastVol;
					if (proxy.notNil) { 
						lastVal = lastVals[key]; 
						mappedVal = \amp.asSpec.map(this.norm(ccval)); 
						lastVol = if (lastVal.notNil) { \amp.asSpec.map(lastVal) }; 
						proxy.softVol_( \amp.asSpec.map(mappedVal), softWithin, true, lastVol ); 
					};
					lastVals[key] =  mappedVal;
				};
			)
		};
		
		this.mapToPxEdit(mixer.editor, (splitIndex + 1 .. elementKeys.size));
	}
}
