ThinkPad : Laptop {
	// internal devices
	var <hdaps,<trackpoint;

	myInit{
		keyboardID = [1,1,"isa0060/serio0/input0"];
		touchpadID = [2, 7, "isa0060/serio1/input0"];

		//		Routine{
		//	1.0.wait;
			this.initKeyboard;
		//	1.0.wait;
			this.initTouchpad;
		//	1.0.wait;
			this.initHdaps;
		//	1.0.wait;
			this.initTrackpoint;
		//}.play( AppClock );
	}

	initHdaps{
		hdaps = GeneralHID.open( GeneralHID.findBy( 4116, 20564, "hdaps/input0" ) );
		//		hdaps = GeneralHID.open( GeneralHID.findBy( 0, 0, "isa1600/input0" ) );

		hdaps.add( \tiltx, [3,0]);
		hdaps.add( \tilty, [3,1]);

		internals.addExpected( 2, \hdaps );
		
		hdaps.addToNetwork( internals, 2 );

	}

	initTrackpoint{

		trackpoint = GeneralHID.open( GeneralHID.findBy( 2, 10, "synaptics-pt/serio0/input0" ) );
		internals.addExpected( 4, \trackpoint );
		trackpoint.add( \tpdx, [2,0]);
		trackpoint.add( \tpdy, [2,1]);
		trackpoint.add( \tpleft, [1,272]);
		trackpoint.add( \tpright, [1,273]);
		trackpoint.add( \tpmiddle, [1,274]);

		trackpoint.addToNetwork( internals, 4 );
	}
}