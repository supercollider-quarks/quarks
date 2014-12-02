GenericSpace : EnvironmentRedirect {
	
	var <>proxyspace;
	
	*new { arg server, clock;
		^super.new.init(server, clock)
	}
	
	*push { arg server, clock;
		if(name.isNil and: { currentEnvironment.isKindOf(this) }) 
			{ currentEnvironment.clear.pop }; // avoid nesting
		^this.new(server, clock).push;
	}

	init { arg server, clock;
		proxyspace.clear;
		proxyspace = ProxySpace(server, nil, clock);
	}
	
	clock {
		^proxyspace.clock;
	}
	
	clock_ { arg clock;
		proxyspace.clock = clock;
	}
	
	clear { arg fadeTime=0.0;
		proxyspace.clear(fadeTime);
		envir.do(_.stop);
		envir.clear;
	}
	
	makeProxy { arg key;
			var proxy, segIndex, class, str;
			str = key.asString;
			segIndex = str.find("_").postln;
			if(segIndex.isNil) { ^nil };
			proxy = switch(str[0..segIndex - 1],
				"pn", { PatternProxy.new },
				"pe", { EventPatternProxy.new },
				"t", { TaskProxy.new },
				"n", { proxyspace.at(key) },
				"f", { Maybe.new }
			);
			if(this.clock.notNil and: {proxy.respondsTo('clock_')}) { 
				proxy.clock_(this.clock) 
			};
			proxy !? { envir.put(key, proxy) };
			^proxy
	}
	
	put { arg key, obj;
		var res, proxy;
		res = this.localAt(key);
		res = res ?? { this.makeProxy(key, obj) };
		if(res.isSourceProxy) { res.source = obj } { envir.put(key, res ? obj) };
		dispatch.value(key, obj); // forward to dispatch for networking
	}
	
	localAt { arg key;
		^envir.at(key) ?? { proxyspace.envir.at(key) }
	}
	
}