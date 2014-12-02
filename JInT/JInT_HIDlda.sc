/**
	2006  Till Bovermann (IEM)
*/

JInT_HIDlda : JInT {
	var locID;
	var responders;
	
	/** 
	 * @todo test if HIDresponder loaded  
	 * @todo test if device is ready at hand
	 * 
	 */
	*new {|server, locID|
		^super.new.initLDA(server, locID)
	}
	initLDA {|server, argID|
		locID = argID;
		////////////
		controllers = [
	/*  0 */	JInTC_nState("A navigation Cross", server, 9).short_(\cross),
	/*  1 */	JInTC_Button.new("Button  1", server, [1, 0, \linear, 1].asSpec).short_(\b1),
	/*  2 */	JInTC_Button.new("Button  2", server, [1, 0, \linear, 1].asSpec).short_(\b2),
	/*  3 */	JInTC_Button.new("Button  3", server, [1, 0, \linear, 1].asSpec).short_(\b3),
	/*  4 */	JInTC_Button.new("Button  4", server, [1, 0, \linear, 1].asSpec).short_(\b4),
	/*  5 */	JInTC_Button.new("Button  9", server, [1, 0, \linear, 1].asSpec).short_(\b9),
	/*  6 */	JInTC_Button.new("Button 10", server, [1, 0, \linear, 1].asSpec).short_(\b10),
	/*  7 */	JInTC_Button.new("FireButton 5", server, [1, 0, \linear, 1].asSpec).short_(\fb5),
	/*  8 */	JInTC_Button.new("FireButton 6", server, [1, 0, \linear, 1].asSpec).short_(\fb6),
	/*  9 */	JInTC_Button.new("FireButton 7", server, [1, 0, \linear, 1].asSpec).short_(\fb7),
	/* 10 */	JInTC_Button.new("FireButton 8", server, [1, 0, \linear, 1].asSpec).short_(\fb8),
	/* 11 */	JInTC_ThumbStick.new("Left  small analog joystick.", server,[0, 255, \linear, 1].asSpec!2 ++ [1, 0].asSpec).short_(\lj),
	/* 12 */	JInTC_ThumbStick.new("Right small analog joystick.", server, [0, 255, \linear, 1].asSpec!2 ++ [1, 0].asSpec).short_(\rj)
		];

		////////////
		responders = [];

		/* Buttons */
		responders = responders.add(HIDresponder(locID, 4, {|val|
			controllers[1].set(0, val);
		}));
		responders = responders.add(HIDresponder(locID, 5, {|val|
			controllers[2].set(0, val);
		}));
		responders = responders.add(HIDresponder(locID, 6, {|val|
			controllers[3].set(0, val)
		}));
		responders = responders.add(HIDresponder(locID, 7, {|val|
			controllers[4].set(0, val);
		}));
		
		/* FireButtons */
		responders = responders.add(HIDresponder(locID, 8, {|val|
			controllers[7].set(0, val);
		}));
		responders = responders.add(HIDresponder(locID, 9, {|val|
			controllers[8].set(0, val);
		}));
		responders = responders.add(HIDresponder(locID, 10, {|val|
			controllers[9].set(0, val);
		}));
		responders = responders.add(HIDresponder(locID, 11, {|val|
			controllers[10].set(0, val);
		}));

		/* middle Buttons*/
		responders = responders.add(HIDresponder(locID, 12, {|val|
			controllers[5].set(0, val);
		}));
		responders = responders.add(HIDresponder(locID, 13, {|val|
			controllers[6].set(0, val);
		}));
		
		/* left Joystick */
		responders = responders.add(HIDresponder(locID, 16, {|val|
			controllers[11].set(0, val);
		}));
		responders = responders.add(HIDresponder(locID, 17, {|val|
			controllers[11].set(1, val);
		}));
		responders = responders.add(HIDresponder(locID, 14, {|val|
			controllers[11].set(2, val);
		}));

		/* right Joystick */
		responders = responders.add(HIDresponder(locID, 18, {|val|
			controllers[12].set(0, val);
		}));
		responders = responders.add(HIDresponder(locID, 19, {|val|
			controllers[12].set(1, val);
		}));
		responders = responders.add(HIDresponder(locID, 15, {|val|
			controllers[12].set(2, val);
		}));

		/* Cross */
		responders = responders.add(HIDresponder(locID, 20, {|val|
			controllers[0].set(0, val);
		}));
	}
	startCustom {
		responders.do(_.add);
	}
	stopCustom {
		responders.do(_.remove)
	}

}