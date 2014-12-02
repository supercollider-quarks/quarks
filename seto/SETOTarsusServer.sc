/*
Implementation of a Tarsus2OSC server for SETO
	http://tuio.lfsaw.de/SETO.html

Author: 
	2004, 2005, 2006, 2007
	Till Bovermann 
	Neuroinformatics Group 
	Faculty of Technology 
	Bielefeld University
	Germany
*/

SETOTarsusServer : SETOServer {
	var interface;

	var specs;
	var head;
	/**
	 * @arg specs array of specs for each dimension [x, y, z]
	 *
	 */
	*new {|netaddr, setoClass, interactionClass, specs, head = 3|
		^super.new("_ixyzuvw", setoClass, interactionClass)
			.pr_initTarsusServer(netaddr, specs, head);
	}
	start{
		interface.start;
	}
	stop{
		interface.stop;
	}

	setFunc_{|function|
		setFunc = function;
		OSCReceiverFunction(interface, \set, {|time ... args|
			var numObj;
			var pos, rot;
			
			numObj = ((args.size) div: 6);
			
			// format: /client "set" <time> <6DOF1> ... <6DOFn>
			//    where 6DOFi == rrrppp
			args   = args.reshape(numObj, 2, 3);

			// eval setFunc for each 6-tuple 
			args.do{|config, id|
				// all objects are from the same type (0) apart from the head (1)
				// scale positions according to specs
				setFunc.value(
					id, 
					(id == head).if({1}, {0}), // assign head a different classID (1)
					*(config[1].collect{|val, i| specs[i].unmap(val)} ++ config[0])
				);
			};
			// eval aliveFunc for each incoming message
			aliveFunc.value(*(0..numObj - 1));
		});
	}
	pr_initTarsusServer {|netaddr, argSpecs, argHead|
		specs = argSpecs ? Array.fill(3, {[-10, 10].asSpec});
		interface = OSCReceiver('/client', netaddr);
		head = argHead;
		this.setFunc_(setFunc);
	}
}