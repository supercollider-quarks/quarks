// adc & haho - joystickbox
// inherit the rest from PocketFader


JSBKtl : PFKtl { 
	
	init { 		// make sure we reach top and bottom 
				// - the joysticks often only reach 15 - 105.
		^super.init.valRange_([20, 100]);
	}
	*makeDefaults { 

		// just one bank of joysticks
		defaults.put(this, 
			(		// upper row, left to right
				js1X: '0_0',  js2X:  '0_2', js3X: '0_4', js4X:  '0_6', 
				js1Y: '0_1',  js2Y:  '0_3', js3Y: '0_5', js4Y:  '0_7', 
				
				js5X: '0_8',  js6X: '0_10', js7X: '0_12', js8X: '0_14',
				js5Y: '0_9', 	js6Y: '0_11', js7Y: '0_13', js8Y: '0_15'
			)
		);
	}
}
