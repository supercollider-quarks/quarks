MIDIKtl { 

	classvar <defaults; 
	
	var <srcID, <ccDict, <ccresp; 
	var <ctlNames, <orderedCtlNames;
			
	*initClass { 
		defaults = ();
		this.allSubclasses.do(_.makeDefaults); 
	}

	*makeDefaults { 
		// subclasses override this method. 
		// they put their controller keys and chan/ccnum combinations into 
		// defaults[class]
			// MIDIKtl is empty by default
		defaults.put(this.class, ());
	}
	
	*new { |srcID, ccDict| 
		^super.newCopyArgs(srcID, ccDict).init;
	}
	
	hasScenes { 
		^ctlNames.every(_.isKindOf(Dictionary))
	}
	
	init { 
		ctlNames = defaults[this.class];

		ccDict = ccDict ?? ();
		
		ccresp.remove; 
		ccresp = CCResponder({ |src, chan, ccn, ccval| 
			var lookie = this.makeCCKey(chan, ccn);
			if (this.class.verbose, { ['cc', src, chan, ccn, ccval].postcs });
			
			ccDict[lookie].value(ccval);
		}, srcID);
	}

		// use when ctlNames is one flat dict
	mapCC { |ctl= \sl1, action| 
		var ccDictKey = ctlNames[ctl]; // '0_42'
		if (ccDictKey.isNil) { 
			warn("key % : no chan_ccnum found!\n".format(ctl));
			^this
		}; 
		ccDict.put(ccDictKey, action);
	}
	
		// use when ctlNames are scene-based dicts (NanoKtl, PDKtl)
	mapCCS { |scene=2, ctl= \sl1, action| 
		var ccScene, ccDictKey; 
		
		ccScene = ctlNames[scene];
		if (ccScene.isNil) { 
			warn("% : mapCCS: scene % : not found!\n".format(this, scene));
			^this
		};
		ccDictKey = ccScene[ctl]; // '0_42'
		if (ccDictKey.isNil) { 
			warn("key % : no chan_ccnum found!\n".format(ctl));
			^nil			
		};	
		ccDict.put(ccDictKey, action);
	}

	free { 
		ccresp.remove;
		ccDict.clear;
		// redraw pxmix and pxedit with clear colors...?
	}

	makeCCKey { |chan, cc| ^(chan.asString ++ "_" ++ cc).asSymbol }
	
	ccKeyToChanCtl { |ccKey| ^ccKey.asString.split($_).asInteger }

	makeNoteKey { |chan, note| 
		var key = chan.asString; 
		if (note.notNil) { key = key ++ "_" ++ note };
		^key.asSymbol 
	}

	noteKeyToChanNote { |noteKey| ^noteKey.asString.split($_).asInteger }
	
	findKey { |val| ^ctlNames.findKeyForValue(val); } 
	
	findSceneKey { |val|
		var res;
		ctlNames.keysValuesDo { |scene, dict| 
			res = dict.findKeyForValue(val); 
			if (res.notNil) { ^[scene, res] }; 
		}; 
		^res;
	}
}
	
	// add responders for noteOn, noteOff: 
	
MIDINKtl : MIDIKtl { 
	var <chan, <noteOnDict, <noteOffDict, <noteOnResp, <noteOffResp;
	var <>globalNoteOnFunc, <>globalNoteOffFunc, noGlobal;
	
	*new { |srcID, chan = 0, ccDict, noteOnDict, noteOffDict| 
		^super.newCopyArgs(srcID, ccDict).init(chan, noteOnDict, noteOffDict);
	}

	init { |chan, noteOnD, noteOffD|
		super.init.initNote(chan, noteOnD, noteOffD)
	}
	
	initNote { |inChan, noteOnD, noteOffD|
		chan = inChan ? 0; 
		noteOnDict = noteOnD ?? {()};
		noteOffDict = noteOffD ?? {()};

		noteOnResp.remove; 
		noteOnResp = NoteOnResponder({ |src, chan, note, vel| 
		//	var lookie = this.makeNoteKey(chan, note);
			var specialFunc = noteOnDict[note];
			
			if (this.class.verbose, { ['cc', src, chan, note, vel].postcs });
			
			if (specialFunc.notNil) { 
				specialFunc.value(note, vel / 127) 
			} { 
				globalNoteOnFunc.value(note, vel / 127);
			};
		}, srcID);

		noteOffResp.remove; 
		noteOffResp = NoteOffResponder({ |src, chan, note, vel| 
		//	var lookie = this.makeNoteKey(chan, note);
			var specialFunc = noteOffDict[note];
			
			if (this.class.verbose, { ['cc', src, chan, note, vel].postcs });
			
			if (specialFunc.notNil) { 
				specialFunc.value(note, vel / 127) 
			} { 
				globalNoteOffFunc.value(note, vel / 127);
			};
		}, srcID);
	}
	
	on_ { |func| globalNoteOnFunc = func }
	off_ { |func| globalNoteOffFunc = func }
	
	mapNoteOn { |note, action| noteOnDict.put(note, action) }
	
	mapNoteOff { |note, action| noteOffDict.put(note, action) }	

	free { 
		super.free;
		
		noteOnResp.remove; 
		noteOffResp.remove; 
		noteOnDict.clear;
		noteOffDict.clear;
	}
}
