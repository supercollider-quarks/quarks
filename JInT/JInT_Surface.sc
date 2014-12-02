JInT_Surface : JInT {
	var <>notInitialized = true;
	var <sensitivity, <dt, <audioInBus;
	var responder, bus;
	
	*new {|server, audioInBus = 1, sensitivity = 0.025, dt = 0.1|
		^super.new.initSurface(server, audioInBus, sensitivity, dt)
	}
	sensitivity_{|val|
		sensitivity = val;
		controllers.first.sensitivity = val;
		controllers.first.nodeProxy.set(\sensitivity, val);
	}
	dt_{|val|	
		dt = val;
		controllers.first.dt = val;
		controllers.first.nodeProxy.set(\dt, val);
	}
	audioInBus_{|val|
		audioInBus = val;
		controllers.first.audioInBus = val;
		controllers.first.nodeProxy.set(\audioInBus, val);
	}
	
	initSurface {|server, argInBus, argSens, argDt|
		audioInBus 		= argInBus;
		sensitivity 	= argSens;
		dt 			= argDt;
		
		controllers = [
		 	JInTC_Knock("Knock Surface", server, 
				ControlSpec(0, 1, default: 0), // knock pressure
				audioInBus
			).short_(\knock) 
		]
	}
	startCustom {
		if (notInitialized) {
			this.initialize;
		}; // fi
		this.controllers.first.nodeProxy.resume;
		responder = OSCresponderNode(controllers.first.server.addr,'/tr',{ arg time,responder,msg;
			(msg[1] == controllers.first.nodeProxy.objects.first.nodeID).if{
			/** @todo get nodeID of nodeproxy and use it to compare with msg[1]*/
				controllers.first.setWithoutProxy(0, msg[3]);
			}
		});
		responder.add;
	}
	stopCustom {
		// stop sending to server
		this.controllers.first.nodeProxy.pause;
		responder.remove;
	}
	initialize {
		bus = Bus.audio(controllers.first.server, 1);
		notInitialized = false;
	} 
}