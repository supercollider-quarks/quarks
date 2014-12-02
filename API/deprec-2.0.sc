
// deprecated as of 2.0
// removed in 3.0 if there ever is one

+ API {

	// old style call does not supply a callback function as the first argument	// return is always immediate
	// '/apiname/cmdName', arg1, arg2
	*oldCall { arg selector ... args;
		var blank,app,cmd;
		# blank,app ... cmd = selector.asString.split($/);
		^this.load(app).oldCall(cmd.join($/).asSymbol,*args);
	}
	oldCall { arg selector ... args;
		var m = this.prFindHandler(selector);
		^m.valueArray(args);
	}

	oldAdd { arg selector,func;
		functions.put(selector, { arg callback; callback.value(func.value) })
	}
	oldAddAll { arg dict;
		dict.keysValuesDo({ arg key, func;
			this.oldAdd(key, func);
		});
	}


	// create a function
	func { arg selector ... args;
		^{ arg ... ags; this.call(selector,*(args ++ ags)) }
	}
	// convienience method, but just makes the class more crowded
	make { arg func;
		this.addAll(Environment.make(func))
	}
	functionNames {
		^functions.keys
	}
}

/*
old duplex system.  harder to work with and understand

		oscResponders = oscResponders.addAll( [
			// yes, these overwrite any at this addr / path
			// even if for other APIs, because its the same action
			// and callback paths are /absolute
			// may change this
			OSCresponder(addr,'/API/registerListener',
				{ arg time,resp,message,addr;
					var listeningPort,callbackCmdName,blah,hostname;
					if(message.size == 3,{
						# blah,listeningPort,callbackCmdName = message;
					},{
						# blah, listeningPort = message;
					});
					API.registerListener(addr,NetAddr.fromIP(addr.addr,listeningPort),callbackCmdName);
				}
			).add,
			OSCresponder(addr,'/API/call',
				{ arg time,resp,message,addr;
					var pathToSendReturnValue, apiCallPath,args,blah,returnAddr,returnPath,result;
					# blah, pathToSendReturnValue, apiCallPath ... args = message;
					result = API.prFormatResult( API.call(apiCallPath,*args) );
					// should support /API/call [returnPath returnIdentifiers] etc.
					# returnAddr,returnPath = API.prResponsePath(addr);
					returnAddr.sendMsg( * ([pathToSendReturnValue] ++ result) )

				}
			).add
		]);


*/

