/*
Implementation of a MT2OSC server for SETO (for master thesis of rtuenner)
	http://tuio.lfsaw.de/SETO.html

Author: 
	2008
	Till Bovermann 
	Neuroinformatics Group 
	Faculty of Technology 
	Bielefeld University
	Germany
*/

SETOMTServer : SETOServer {
	var interface;


	var specs;




	*new {|netaddr, setoClass, interactionClass|
		^super.new("_ixy", setoClass, interactionClass)
			.pr_initMTServer(netaddr);
	}
	start{
		interface.start;
	}
	stop{
		interface.stop;
	}

	setFunc_{|function|
		setFunc = function;
		OSCReceiverFunction(interface, \set, {|numItems ... args|
			// format: /mt "set" numItems id, x, y, ...
			args   = args.reshape(numItems, 3);
			// eval setFunc for each 3-tuple 
			args.do{|val|
				// all objects are from the same type (0)
				setFunc.value(
					val[0], 
					0,
					val[1]/450,
					val[2]/450
				);
			};
			// eval aliveFunc for each incoming message
			(numItems > 0).if({
				aliveFunc.value(*args.flop[0]);
			}, {
				aliveFunc.value([]);
			})
		});
	}
	pr_initMTServer {|netaddr|
		interface = OSCReceiver('/mt', netaddr);
		this.setFunc_(setFunc);
	}
}