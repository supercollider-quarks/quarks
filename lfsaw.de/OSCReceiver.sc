
OSCReceiver {
	var <cmdName, <addr;
	var verbose = false;
	var <resp, <isListening=false;

	*new {|cmdName, addr|
	
		^super.new.minit(cmdName, addr);
	}
	minit {|argCmdName, argAddr|
		var respFactory;
		
		respFactory = {|netaddr, cmdName|
			OSCresponderNode(netaddr, cmdName, { arg time, responder, msg;
				var funcName, func;
				funcName = msg[1];
				func = OSCReceiverFunction.at(cmdName, funcName);
				if(verbose) { "OSCReceiver % received: %\n".postf(funcName, msg) };
				func.value(msg.drop(2));
			});
		};

		
		cmdName = (argCmdName).asSymbol;
		addr = argAddr;// ? NetAddr("localhost", 57120);
		addr = addr.asArray;
		
		resp = addr.isEmpty.if({
			[respFactory.value(nil, cmdName)];
		},{
			addr.collect { arg netaddr;
				respFactory.value(netaddr, cmdName);
			};
		});
	}
	start {
		if(isListening.not, { resp.do { arg u; u.add }; isListening = true });
	}
	
	stop {
		resp.do { arg u; u.remove };
		isListening = false;
	}



}


OSCReceiverFunction {
	classvar <>all;
	var <name, <funcName, <func;
	
	*new { arg name, funcName, func;
		^super.new.minit(
			name.isKindOf(OSCReceiver).if({name.cmdName}, {name}), 
			funcName, 
			func
		).toLib;
	}
	minit {|argName, argFuncName, argFunc|
		name = argName.asSymbol;
		funcName = argFuncName.asSymbol;
		func = argFunc;
	}
	*initClass { this.clear }

	*clear { all = IdentityDictionary.new }

	*at { arg name, funcName; ^all.at((name++\_++funcName).asSymbol) }

	*removeAt { arg name, funcName; ^all.removeAt((name++\_++funcName).asSymbol) }

	toLib { all.put((name++\_++funcName).asSymbol, this) }

	value { arg args;
		func.valueArray(args)
	}
	
	
}
