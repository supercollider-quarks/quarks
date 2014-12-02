
RPC { // Remote procedure call
	
	classvar <all;
	var <name, <>addr,<>sendAPI,<receiveAPI,mountName;
	
	*new { arg name,netAddr,sendAPI,receiveAPI;
		var nu;
		nu = (all.at(name.asSymbol) ?? {super.new.init(name.asSymbol)});
		nu.addr = netAddr;
		nu.sendAPI_(sendAPI);
		nu.receiveAPI_(receiveAPI);
		^nu
	}
	init { arg n;
		name = n;
		all.put(name,this);
	}
	*initClass {
		all = IdentityDictionary.new;
	}
	receiveAPI_ { arg obj;
		if(obj.isString or: obj.isKindOf(Symbol),{
			receiveAPI = API(obj);
			mountName = receiveAPI.name;
			^this
		});
		if(obj.isKindOf(API),{
			receiveAPI = obj;
			mountName = receiveAPI.name;
			^this
		});
		receiveAPI = API(this.name);
		if(obj.isKindOf(Dictionary),{
			receiveAPI.addAll(obj);
			mountName = "";// mount at root
		})
	}
	mountOSC { arg baseCmdName;
		this.receiveAPI.mountOSC(baseCmdName ? mountName)
	}

	// methods on this object are sent as OSC commands
	doesNotUnderstand { arg selector ... args;
		var path;
		path = RPC.selectorToPath(selector);
		if(sendAPI.notNil,{
			if(sendAPI.includes(path).not,{
				Exception("" + path + " is not a registered RPC path. (" + this + " got " + selector+")").throw;
				//^super.doesNotUnderstand(*([selector] ++ args))
			})
		});
		^this.call(*([path]++args))
	}
	
	call { arg path ... args;
		var m;
		addr.sendMsg(*([path] ++ args))
	}	
	*selectorToPath { arg selector;
		var path;
		path = selector.asString.replace("_","/");
		if(path[0] != $/,{
			path = "/" ++ path;
		});
		^path.asSymbol
	}
	
}



	
	