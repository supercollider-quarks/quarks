// automatic numbering possible?

NetDef {
	var <name;
	var <addr;
	
	classvar <broadcast;
	classvar <all;
	
	*initClass {
		all = IdentityDictionary.new;
	}
	
	
	*new { arg name, cmd, func;
		var res = all.at(name);
		if(res.isNil) { 
			res = super.newCopyArgs(name).init;
			all.put(name, res);
		} {
			res.name = name;
		};
		if(cmd.notNil) {
			
		};
		this.initBroadcast;
		^res
	}

	*initBroadcast {
		var broadcastIP = NetAddr.broadcastIP;
		if(broadcastIP.isNil) { 
			"no broadcast available. using loopback instead for now.".warn;
			broadcastIP = "127.0.0.1"
		} {
			if(NetAddr.broadcastFlag.not) {
				"setting NetAddr broadcast flag to true ... ".postln;
				NetAddr.broadcastFlag = true;
			}
		};
		broadcast = NetAddr(broadcastIP, 57120);
	}
	
	init {
		addr = NamedNetAddr(name, NetAddr.myIP, 57120)
	}
	
	makeResponder { arg addr, cmd, func;
		func = addr.responderFunc(func);
		
		
	
		^this.myAddr.makeResponder(addr, cmd, func)
	}
	
	
}
