BCRKtl : MIDIKtl {
	classvar <>verbose = false;
	classvar <>midiOut;

	*makeMIDIOut { |index = 0|
		midiOut = MIDIOut(index, MIDIClient.destinations[index].uid)
	}

	*initClass {
		this.makeDefaults;
	}
	
	mapToPxPars { |proxy ... pairs|
		if (midiOut.notNil) {
			this.sendFromProxy(proxy, pairs);
		};

		pairs.do { |pair|
			var ctlName, paramName;
			#ctlName, paramName = pair;
			this.mapCC(ctlName,
				{ Ê|midival|
					proxy.set(paramName, paramName.asSpec.map(midival / 127))
				}
			);
		};
	}
	
	sendFromProxy { |proxy, pairs|
		var ctlNames, paramNames, currVals, midivals;
		#ctlNames, paramNames = pairs.flop;
		currVals = proxy.getKeysValues(paramNames).flop[1];
		midivals = currVals.collect { |currval, i|
			(paramNames[i].asSpec.unmap(currval) * 127).round.asInteger;
		};
		midivals.postln;
		[ctlNames, midivals].flop.do { |pair|
			this.sendCtlValue(*pair);
		}
	}

	sendCtlValue { |ctlName, midival| 
		var chanCtl = this.ccKeyToChanCtl(ctlNames[ctlName]);
		midiOut.control(chanCtl[0], chanCtl[1], midival);
	}

	*makeDefaults { 
		// lookup for all scenes and ctlNames, \sl1, \kn1, \bu1, \bd1,
		defaults.put(this, (

		// top knob push mode
			tr1: '0_57', 	tr2:  '0_58', tr3: '0_59', 	tr4: '0_60',   tr5:  '0_61', tr6:  '0_62', tr7:  '0_63', tr8: '0_64',
		// knobs (top row)
			knA1: '0_1', 	knA2: '0_2', 	knA3: '0_3', 	knA4: '0_4',   knA5: '0_5',  knA6: '0_6',  knA7: '0_7', knA8:  '0_8', 

		// buttons 1st row
			btA1: '0_89', btA2: '0_90', btA3: '0_91', btA4: '0_92',  btA5: '0_93', btA6: '0_94', btA7: '0_95', btA8: '0_96',
		// buttons 2nd row
			btB1: '0_97', btB2: '0_98', btB3: '0_99', btB4: '0_100', btB5: '0_101',btB6: '0_102',btB7: '0_103',btB8: '0_104',

		// knobs (lower 3 rows)
			knB1: '0_33', knB2: '0_34', knB3: '0_35', knB4: '0_36',  knB5: '0_37', knB6: '0_38', knB7: '0_39', knB8: '0_40',
			knC1: '0_41', knC2: '0_42', knC3: '0_43', knC4: '0_44',  knC5: '0_45', knC6: '0_46', knC7: '0_47', knC8: '0_48', 
			knD1: '0_49', knD2: '0_50', knD3: '0_51', knD4: '0_52',  knD5: '0_53', knD6: '0_54', knD7: '0_55', knD8: '0_56'
			
		));
	}
}