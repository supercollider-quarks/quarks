
Sensitivity {		// a macro ugen - jamshark70@dewdrop-world.net

	// if value ranges from 0..1:
	//		sense == 0: function always returns 1
	//		sense == 0.5: function ranges from 0.5..1
	//		sense == 1: function ranges from 0..1 (unchanged)
	// this mimics the sensitivity parameter on hardware synths

	*kr { arg scaler = 1, value = 0, sense = 1;
		^case
			{ scaler == 0 } { 0 }
			{ sense == 0 } { scaler }
			{ sense == 1 } { scaler * value }
		{ scaler * ((value - 1) * sense + 1) }
//		(scaler == 0).if({
//			^0
//		}, {
//			(sense == 1).if({
//				^value
//			}, {
//				^scaler * ((value - 1) * sense + 1)
//			})
//		});
	}

	*ar { arg scaler, value, sense;
		^this.kr(scaler, value, sense)
	}	
	
}
