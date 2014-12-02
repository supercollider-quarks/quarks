

NamedNetAddr : NetAddr {
	var <>name;
	*new { arg name="", hostname, port=57120;
		if(hostname.isNil) {
			hostname = NetAddr.broadcastIP;
			if(NetAddr.broadcastFlag.not) {
				"setting NetAddr broadcast flag to true ... ".postln;
				NetAddr.broadcastFlag = true;
			};
		};
		^super.new(hostname, port).name_(name.asSymbol)
	}
	sendNamedMsg { arg ... args; 
		super.sendMsg(*args.insert(1, name))
	}
	listSendNamedMsg { arg args;
		super.sendMsg(*args.insert(1, name))
	}
	
	responderFunc { arg func;
		 ^{ |time, recv, msg|
			var receiverName = msg[1];
			
		//	postf("\nreceiverName: % my name: % firstArgSent: % myCmdName: %\nI match: %\n\n", 
		//		receiverName, name, msg[2], cmd, receiverName === name);
				
			if(receiverName === \broadcast  // broadcast
				or: 
				{ receiverName === name })
			{
				func.value(time, recv, msg[1..]) // remove name
			}
		}
	}
	
	// return a responder that listens to [cmd, name] only.
	makeResponder { arg addr, cmd, func;
		^OSCresponderNode(addr, cmd, this.responderFunc(func));
	}
		
	== { arg something;
		^(super == something) and: { something.respondsTo(\name) } and: { something.name == name }
	}
	hash {
		^super.hash bitXor: name.hash
	}
	storeArgs { ^[name, hostname, port] }
	printOn { arg stream; 
		stream << this.class.name;
		this.storeParamsOn(stream);
	}
}