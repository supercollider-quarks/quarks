FFLV2Ktl : MIDIKtl {
	classvar <>verbose = false;
	var <lastVals;

	*initClass { this.makeDefaults }

	init { 
		super.init; 
		lastVals = ();
	}
			
		// to do: how to map to editor? 

		// how to map to proxymixer?
		
		// support incremental controllers in proxies? 
		// proxy.change(<param>, 0.03 (normalized range)); 

	*makeDefaults { 

		// just one layer: ctlNames, \sl1, \kn1, \bu1, \bd1, 
		defaults.put(this, (
					// 3 settings for joystick XY:
				joyXPan: '12_31', joyYPan: '12_11', 
				joyXfx1: '14_38', joyYfx1: '14_36', 
				joyXfx2: '14_39', joyYfx2: '14_37',

					// 2 settings for the 6 sliders, numbered 1-6, 7-12
				sl1: '14_0', sl2: '14_1', sl3: '14_2', sl4:  '14_3', sl5:  '14_4',  sl6:  '14_5', 
				sl7: '14_6', sl8: '14_7', sl9: '14_8', sl10: '14_9', sl11: '14_10', sl12: '14_11', 

					// 3 settings \send \eq, \clip
					// sends 1 to 127 like a knob
				potAsend: '12_40', potBsend: '12_60', potCsend: '12_80', potDsend: '12_100',
				potAeq:   '13_51', potBeq:   '13_71', potCeq:   '13_91', potDeq:   '13_111',
				
					// sends 1 .. 4 OR 123 .. 127, i.e. sends up or down increments!
				potAclip: '14_20', potBclip: '14_21', potCclip: '14_22', potDclip: '14_23',


					// not done yet...
//				potAfx3: '_', potBfx3: '_', potCfx3: '_', potDfx3: '_',
//				potAfx4: '_', potBfx4: '_', potCfx4: '_', potDfx4: '_',
//				potCfx5: '_', 
				
				potDfx5: '14_35'
			)
		);
	}
}


