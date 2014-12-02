/**
2007 Till Bovermann (Bielefeld University)
*/

JInT_WISE : JInT {
	var resp, accessID, netAddr;
	var notInitialized = true;
	
	/** 
	 * @todo 
	 */
	*new {|server, netAddr = nil, src="WB01"|	// src string of my WISEBox
		^super.new.initWISE(server, netAddr, src)
	}
	initWISE {|server, argNetAddr, src|
		accessID = "/"++src;
		netAddr = argNetAddr;
		
		controllers = Array.fill(16, {|i| 
			JInTC_Knob(
				"joint" ++ i, 
				server, 
				[
					0, 
					65535 /* == 2**16 */, 
					\linear, 
					0
				].asSpec
			).short_(("j"++(i)).asSymbol)
		})
	} // end initWISE
	
	startCustom {
		if (notInitialized) {
			this.initialize;
		}; // fi
		
		resp.add;
	} // end startCustom
	
	stopCustom {
		resp.remove;
	}
	initialize {
		resp = OSCresponderNode(netAddr, accessID, {|time, resp, msg|
			msg[1..].collect{|val, i|
				controllers[i].set(0, val)
			}
		});
		notInitialized = false;
	} 
}

JInT_Bender : JInT_WISE {
	initWISE {|server, argNetAddr, src|
		accessID = "/"++src;
		netAddr = argNetAddr;
		
		controllers = 
			[JInTC_Knob("topKnob", server, [117, 65095, \linear, 1].asSpec).short_(\k1)] ++ 
			Array.fill(5, {|i| 
				JInTC_Knob(
					"joint" + (i+1),
					server, 
					[
						119, 
						49087,
						\linear, 
						1
					].asSpec
				).short_(("j"++(i+1)).asSymbol)
		})
	} // end initWISE
	initialize {
		resp = OSCresponderNode(netAddr, accessID, {|time, resp, msg|
			msg[3..8].collect{|val, i|
				controllers[i].set(0, val)
			}
		});
		notInitialized = false;
	} 
}