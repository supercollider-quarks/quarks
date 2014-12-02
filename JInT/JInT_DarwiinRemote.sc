/**
2007 Till Bovermann (Bielefeld University)
*/

JInT_DarwiinRemote : JInT {
	var resp, netAddr;
	var notInitialized = true;
	
	/** 
	 * @todo 
	 */
	*new {|server, netAddr = nil|
		^super.new.initWii(server, netAddr)
	}
	initWii {|server, argNetAddr|
		netAddr = argNetAddr;
		
		controllers = 
		/* 0..8 */
		["A", "B", "C", "Z", "+", "-", "HOME", "1", "2"].collect{|name, i| 
			JInTC_Button(
				"Knob" + name, 
				server, 
				[
					0, 
					1
				].asSpec
			).short_(name.asSymbol)
		} ++ 
		/* 9 */
		["Navigation Cross"].collect{|name, i| 
			JInTC_nState(name, server, 5).short_(\cross)
		} ++ 
		/* 10 */
		["Analog Joystick"].collect{|name, i| 
			JInTC_SimpleThumbStick(name, server, [ControlSpec(29, 230, default: 129), ControlSpec(27, 218, default: 122)]).short_(\joy)
		} ++
		/* 11, 12 */
		["Wiimote Acceleration", "Nunchuk Acceleration", ].collect{|name, i| 
			JInTC_Acceleration(name, server, [ControlSpec(0, 240, default: 127), ControlSpec(27, 218, default: 122)]).short_(#[\wiiAcc, \nunAcc][i])
		};		
		
	} // end initWii
	
	startCustom {
		if (notInitialized) {
			this.initialize;
		}; // fi
		
		resp.do(_.add);
	} // end startCustom
	
	stopCustom {
		resp.do(_.remove);
	}
	initialize {
		resp = 
			[
				"/wii/button/a", "/wii/button/b", "/nunchuk/button/c", "/nunchuk/button/z", 
				"/wii/button/plus", "/wii/button/minus", 
				"/wii/button/home", 
				"/wii/button/one", "/wii/button/two"
			].collect{|oscName, i|
				OSCresponderNode(netAddr, oscName, {|time, resp, msg|
					controllers[i].set(0, msg[1]);
				})
			} ++
			[
				"/wii/button/up", "/wii/button/down", "/wii/button/left", "/wii/button/right"
			].collect {|oscName, i|
				OSCresponderNode(netAddr, oscName, {|time, resp, msg|
						controllers[9].set(i, msg[1]);
				})
			} ++
			OSCresponderNode(netAddr, "/nunchuk/joystick", {|time, resp, msg|
				controllers[10].setAll(msg[1..]);
			}) ++
			[
				"/wii/acc", 
				"/nunchuk/acc"
			].collect{|oscName, i|
				OSCresponderNode(netAddr, oscName, {|time, resp, msg|
					controllers[11+i].setAll(msg[1..]);
				})
			};

		notInitialized = false;
	} 
}