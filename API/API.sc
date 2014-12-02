

API {

	classvar <all;
	var <name, functions;
	var oscResponders;

	*new { arg name;
		// get or create
		^(all.at(name.asSymbol) ?? { this.prNew(name) })
	}
	*prNew { arg name;
		^super.new.init(name.asSymbol)
	}
	*load { arg name;
		^all.at(name.asSymbol)
			?? { this.prLoadAPI(name) }
			?? { Error("API" + name + "not found").throw; }
	}
	init { arg n;
		name = n;
		functions = Dictionary.new;
		all.put(name, this);
	}
	*initClass {
		all = IdentityDictionary.new;
	}

	// defining
	add { arg selector, func;
		functions.put(selector, func)
	}
	addAll { arg dict;
		functions.putAll(dict)
	}
	// add methods off an object as functions
	exposeMethods { arg obj, selectors;
		selectors.do({ arg m;
			this.add(m, { arg callback ... args;
				callback.value(obj.performList(m, args));
			})
		})
	}
	exposeAllExcept { arg obj, selectors=#[];
		obj.class.methods.do({ arg meth;
			if(selectors.includes(meth.name).not, {
				this.add(meth.name, { arg callback ... args;
					callback.value( obj.performList(meth.name,args) )
				})
			})
		})
	}

	// calling
	async { arg selector, args, callback, onError;
		// passes the result to the callback
		var m;
		m = this.prFindHandler(selector);
		if(onError.notNil,{
			{
				m.valueArray([callback] ++ args);
			}.try(onError);
		},{
			m.valueArray([callback] ++ args);
		});
	}
	sync { arg selector, args, onError;
		// pauses thread if needed and returns
		// the result directly.
		// must be inside a Routine
		var result, c = Condition.new;
		c.test = false;
		this.async(selector, args, { arg r;
			result = r;
			c.test = true;
			c.signal;
		}, onError);
		c.wait;
		^result
	}
	*async { arg path, args, callback, onError;
		var name, selector;
		# name, selector = path.split($.);
		this.load(name).async(selector, args, callback, onError);
	}
	*sync { arg path, args, onError;
		var name, selector;
		# name, selector = path.split($.);
		^this.load(name).sync(selector, args, onError);
	}


	// returns immediately
	// no async, no Routine required
	// the handler must not fork or defer
	// before calling the callback
	// "apiname.cmdName", arg1, arg2
	*call { arg path ... args;
		var name, selector;
		# name, selector = path.split($.);
		^this.load(name).call(selector, *args);
	}
	call { arg selector ... args;
		var m = this.prFindHandler(selector), result;
		m.valueArray([{ arg r; result = r; }] ++ args);
		^result
	}

	// querying
	*apis {
		var out = API.all.keys.copy();
		Main.packages.do({ arg assn;
			(assn.value +/+ "apis" +/+ "*.api.scd").pathMatch.do { arg path;
				out.add( this.prNameFromPath(path) );
			};
		});
		^out.as(Array);
	}
	selectors {
		^functions.keys
	}
	*allPaths {
		var out = List.new;
		all.keysValuesDo({ arg name, api;
			api.selectors.do { arg selector;
				out.add( name.asString ++ "." ++ selector.asString );
			}
		});
		^out
	}

	// for ease of scripting
	// respond as though declared functions were native methods to this object
	doesNotUnderstand { arg selector ... args;
		if(thisThread.class === Thread, {
			^this.call(selector,*args)
		},{
			^this.sync(selector,args)
		})
	}

	*loadAll { arg forceReload=true;
		Main.packages.do({ arg assn;
			(assn.value +/+ "apis" +/+ "*.api.scd").pathMatch.do({ arg path;
				var api, name;
				name = this.prNameFromPath(path);
				if(forceReload or: {all[name].isNil}, {
					{
						api = path.load;
						API.prNew(name).addAll(api);
					}.try({ arg err;
						("While loading" + name).error;
						err.errorString.postln;
					});
				});
			});
		});
	}
	*prNameFromPath { arg path;
		^path.split(Platform.pathSeparator).last.split($.).first
	}
	*prLoadAPI { arg name;
		Main.packages.do({ arg assn;
			(assn.value +/+ "apis" +/+ name.asString ++ ".api.scd").pathMatch.do { arg path;
				var api;
				{
					api = path.load;
				}.try({ arg err;
					err.errorString.error;
					^nil
				});
				^API(name).addAll(api);
			};
		});
		^nil
	}
	prFindHandler { arg path;
		^functions[path.asSymbol] ?? {
			Error(path.asString + "not found in API" + name).throw
		}
	}


	mountOSC { arg baseCmdName, addr;
		// simply registers each function in this API as an OSC responder node
		// baseCmdName : defaults to this.name  ie.  /{this.name}/{path}
		// addr:  default is nil meaning accept message from anywhere
		this.unmountOSC;
		functions.keysValuesDo({ arg k, f;
			var r;
			r = OSCresponderNode(addr,
					("/" ++ (baseCmdName ? name).asString ++ "/" ++ k.asString).asSymbol,
					{ arg time, resp, message, addr;
						this.call(k,*message[1..]);
					}).add;
			oscResponders = oscResponders.add( r );
		});
		^oscResponders
	}
	unmountOSC {
		oscResponders.do(_.remove);
		oscResponders = nil;
	}

	// duplex returns results of API calls as a reply OSC message
	*mountDuplexOSC { arg srcID, recvPort;
		/*
			/API/call : client_id, request_id, fullpath ... args
				client_id and request_id : are used to identify return messages
					and are up to the implementation of the api consumer
					client_id would usually be a specific web browser, program or other independent entity
					request_id would be a unique id for that request for that client
				fullpath:  apiname.methodKey
					dot separated to make it clear that its not an OSC path
			/API/reply : client_id, request_id, result
			/API/not_found : client_id, request_id, fullpath
			/API/error : client_id, request_id, errorString
		*/
		OSCdef('API_DUPLEX', { arg msg, time, addr, recvPort;
			var client_id, request_id, path, args, api, apiName, fullpath, m, ignore;
			# ignore, client_id, request_id, fullpath ... args = msg;
			# apiName, path = fullpath.asString.split($.);
			path = path.asSymbol;

			{
				api = this.load(apiName);
				m = api.prFindHandler(path);
			}.try({ arg error;
				addr.sendMsg('/API/not_found', client_id, request_id, fullpath);
				error.reportError();
			});
			if(m.notNil,{
				api.async(path, args, { arg result;
					// reply is JSON {'result': result }
					addr.sendMsg('/API/reply', client_id, request_id,
						JSON.stringify( (result:result) ));
				}, { arg error;
					addr.sendMsg('/API/error', client_id, request_id, error.errorString() );
					error.reportError();
				});
			});
		}, '/API/call', srcID, recvPort);
	}
	*unmountDuplexOSC {
		OSCdef('API_DUPLEX').free;
	}

	printOn { arg stream;
		stream << this.class.asString << "('" << name << "')"
	}
}

