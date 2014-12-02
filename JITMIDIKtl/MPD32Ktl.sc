/*** 

* Tobi & adc, one scene only for starting.

* make polytouch responder!

MPD32Ktl.verbose = true;
MPD32Ktl.verbose = false;


m = MPD32Ktl.new;

m.mapCC(\sl6, { |val| ("sl6" + val).postln });
m.mapCC(\bt4, { |val| ("bt4" + val).postln });
m.mapCC(\kn6, { |val| ("kn6" + val).postln });
m.mapCC(\rec, { |val| ("rec" + val).postln });

	// pads: bank 1 has notenumbers 36 - 51
m.mapNoteOn(36, { |note, vel| ("noteon, vel: " + [note, vel]).postln; });
m.mapNoteOff(36, { |note, vel| ("noteoff, vel: " + [note, vel]).postln; });


m.mapPolyTouch(36, { |touch| ("touch 36: " + [touch]).postln; });
m.mapPolyTouch(51, { |touch| ("touch 51: " + [touch]).postln; });


***/

MPD32Ktl : MIDINKtl {	// MIDINKtl has note responders too
	
	classvar <>verbose = false; 	// debugging flag
	
	var <>softWithin = 0.05;		// will use softSet, so have a tweakable limit for it
	var <lastVals;				// remember the last values for better control
	var <valRange, <minval, <range;	
	var <touchResp, <touchDict;	// some MIDI controllers may not reach full 0-127 range, 


	init { |chan, noteOnD, noteOffD|
		super.init(chan);	
					
		ctlNames = defaults[this.class];		// get the ctlNames
		orderedCtlNames = ctlNames.keys.asArray.sort;	// a sorted list of the names
		lastVals = ();			// initialise lastVals
		valRange = [0, 127];		// set default valRange; 
		
		this.initPolyTouch(chan, noteOnD, noteOffD);	
	}
	
	initPolyTouch { |chan|

		touchDict = ();
		touchResp = PolyTouchResponder({ |src, ch, note, touch|
			touchDict[note].value(touch);
		}, srcID, chan);
	}
	
	mapPolyTouch { |note, func| touchDict.put(note, func); }


					// makeDefaults puts ctlNames for this device/class 
					// into MIDIKtl.defaults.
	*makeDefaults { 

		// quick first version: 
		// only first scene, mostly defaults
		defaults.put(this, 
			(				// clear names for the elements;
							// '0_7' is midi chan 0, cc number 7, 
							// combined into a symbol for speed. 
							
				sl1: '0_10', sl2: '0_11', sl3: '0_12', sl4: '0_13', 
				sl5: '0_14', sl6: '0_15', sl7: '0_16', sl8: '0_17', 
				
					// buttons under sliders:
				bt1: '0_28', bt2: '0_29', bt3: '0_30', bt4: '0_31', 
				bt5: '0_35', bt6: '0_41', bt7: '0_46', bt8: '0_47', 

				kn1: '0_104', kn2: '0_105', kn3: '0_106', kn4: '0_107', 
				kn5: '0_108', kn6: '0_109', kn7: '0_110', kn8: '0_111',

				rew: '0_115', ffwd: '0_116', stop: '0_117', play: '0_118', rec: '0_119'
			)
		);
	}
	
		// maybe later - think of ways to connect MPD32 to NdefMixer, NdefGui, 
		// T/PdefGui, T/PdefAllGui, EnvirGui etc.

//		
//	mapToEnvirGui { |gui, indices| 
//	}
//							// for PdefGui and TdefGui, just map to their EnvirGui.
//	mapToPdefGui { |gui, indices| 
//		this.mapToEnvirGui(gui.envirGui, indices);
//	}
//	
//	mapToTdefGui { |gui, indices| 
//		this.mapToEnvirGui(gui.envirGui, indices);
//	}
//
//							// NdefGui: 
//	mapToNdefGui { |gui, indices, lastIsVol = true| 
//	}
//				
//	mapToMixer { |mixer, numVols = 8, indices, lastEdIsVol = true, lastIsMaster = true| 
// 	
//	}
}
