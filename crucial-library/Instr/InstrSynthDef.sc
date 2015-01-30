
InstrSynthDef : SynthDef {

	var <longName;
	var instr,<instrName;
	var inputs; // the inputs supplied the last time this was built
	var outputProxies; // the output proxies those inputs created, primarily used for error reporting

	// secret because the function doesn't have them as arg names
	// they are created in the context of the synth def and added
	// magically/secretly.  backdoor access.
	// this is for adding buffers, samples, inline players, subpatches, spawners etc.
	var secretIr, secretKr,stepchildren,synthProxy;

	var <>tempTempoKr;

	var <rate,<numChannels; // known only after building

	*new {
		^super.prNew
	}
	*build { arg instr,args,outClass = \Out;
		^super.prNew.build(instr,args,outClass)
	}
	build { arg instr,args,outClass= \Out;
		var stacked;
		if(UGen.buildSynthDef.notNil,{
			stacked = UGen.buildSynthDef;
		});
		protect {
			this.initBuild;
			this.buildUgenGraph(instr,args ? #[],outClass);
			this.finishBuild;
			UGen.buildSynthDef = stacked;
		} {
			UGen.buildSynthDef = stacked;
		};
		# longName, name = InstrSynthDef.makeDefName(instr,args,outClass);
	}
	buildUgenGraph { arg argInstr,args,outClass;
		var result,fixedID="",firstName;
		var isScalarOut, saveControlNames;

		outClass = outClass.asClass;

		instr = argInstr;
		instrName = instr.dotNotation;
		inputs = args;

		// restart controls in case of *wrap
		saveControlNames = controlNames;
		controlNames = nil;
		controls = nil;
		secretIr = nil;
		secretKr = nil;
		stepchildren = nil;

		{
			// create OutputProxy In InTrig Float etc.
			outputProxies = this.buildControlsWithObjects(instr,inputs);
			result = instr.valueArray(outputProxies);

			if(result != 0.0,{
				rate = result.rate;
				numChannels = max(1,result.size);

				rate.switch(
					\audio, {
						result = outClass.ar(Control.names([\out]).ir([0]) , result);
						// can still add Out controls if you always use \out, not index
					},
					\control, {
						result = outClass.kr(Control.names([\out]).ir([0]) , result);
					},
					\scalar, { // doesn't make sense for a UGen
						("InstrSynthDef: result of your Instr function was a scalar rate object:"
							+ result + this.buildErrorString).error;
					},
					\demand, { // you can't patch these between synths
						("InstrSynthDef: result of your Instr function was a demand rate object:"
							+ result + this.buildErrorString).error;
					},
					\noncontrol,{
						("InstrSynthDef: result of your Instr function was a noncontrol rate object:"
							+ result + this.buildErrorString).error;
					},
					{
						("InstrSynthDef: result of your Instr function was an object with unknown rate:"
							+ result + rate + this.buildErrorString).error;
					}
				);
			});
		}.try({ arg err;
			var info;
			err.errorString.postln;
			info = this.buildErrorString;
			info.postln;
			//this.dumpUGens;
			// since we failed while loading synth defs
			// any other previously successful builds will
			// assume that they finished loading
			// so clear all from the cache
			InstrSynthDef.clearCache(Server.default);

			err.throw;
		});

		controlNames = saveControlNames;
		^result
	}
	*makeDefName { arg instr, args, outClass=\Out;
		var name, longName, firstName;

		longName = [instr.dotNotation, outClass];
		args.do { arg obj,i;
			var r, shard;
			r = obj.rate;
			shard = if([\audio, \control].includes(r), r, { obj });
			longName = longName.add(shard);
		};

		firstName = instr.name.last.asString;
		if(firstName.size > 18, {
			firstName = firstName.copyRange(0, 16);
		});
		name = firstName ++ "#" ++ this.hashEncode(longName);
		^[longName, name]
	}
	*hashEncode { arg object;
		var fromdigits,todigits,res,x=0,digit;
		fromdigits = "-0123456789";
		todigits = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890";
		object.hash.asString.do { arg char;
			x = x * fromdigits.size + fromdigits.indexOf(char);
		};
		if(x == 0,{
			^todigits[0]
		});
		res = "";
		loop {
			digit = x % todigits.size;
			res = todigits[digit].asString ++ res;
			x = (x / todigits.size).floor.asInteger;
			if(x <= 0,{
				^res
			})
		}
	}

	// passed to Instr function but not to synth
	addInstrOnlyArg { arg name,value;
		this.addNonControl(name, value);
	}


	// to cache this def, this info needs to be saved
	// argi points to the slot in objects (as supplied to secretDefArgs)
	// selector will be called on that object to produce the synthArg
	// thus sample can indicate itself and be asked for \tempo or \bufnum

	// initialValue is used for building the synth def
	// selector is what will be called on the object to obtain the real value
	// when the synth is sent
	addSecretIr { arg object,initialValue,selector;
		var name;
		name = "__secret_ir__"++(secretIr.size);
		secretIr = secretIr.add([object,name,initialValue,selector]);
		^Control.names([name]).ir([initialValue])
	}
	addSecretKr { arg object,initialValue,selector;
		var name;
		name = "__secret_kr__"++(secretIr.size);
		secretKr = secretKr.add( [object,name,initialValue,selector]);
		^Control.names([name]).kr([initialValue])
	}
	// a player uses this to install itself into the InstrSynthDef
	// so that it gets added to the Patch's stepchildren
	// and gets prepared and spawned when the Patch plays
	playerIn { arg object;
		var bus;
		bus = this.addSecretIr(object,0,\synthArg);
		^object.instrArgFromControl(bus);
	}

	secretObjects {
		var so;
		so = IdentitySet.new;
		if(secretIr.notNil,{
			secretIr.do({ |list|
				so.add( list[0] );
			})
		});
		if(secretKr.notNil,{
			secretKr.do({ |list|
				so.add( list[0] );
			})
		});
		if(stepchildren.notNil,{
			stepchildren.do({ |stepchild|
				so.add( stepchild )
			})
		});
		^so.as(Array)
	}
	secretDefArgs {
		var synthArgs,size;
		size = secretIr.size * 2 + (secretKr.size * 2);
		if(size == 0, { ^#[] });
		synthArgs = Array(size);
		secretIr.do({ arg n,i;
			var object,name,value,selector;
			#object,name,value,selector = n;
			synthArgs.add(name); // secret arg name
			synthArgs.add(object.perform(selector)); // value
		});
		secretKr.do({ arg n,i;
			var object,name,value,selector;
			#object,name,value,selector = n;
			synthArgs.add(name); // secret arg name
			synthArgs.add(object.perform(selector)); // value
		});
		^synthArgs
	}

	buildControlsWithObjects { arg instr,objects;
		var argNames,defargs,outputProxies;
		objects.do({ arg obj,argi; obj.initForSynthDef(this,argi) });
		argNames = instr.argNames;
		defargs = argNames.collect({ arg name,defargi;
			var obj,init;
			obj = objects.at(defargi);
			init = instr.initAt(defargi);
			if(obj.isNil,{
				obj = instr.specs.at(defargi).defaultControl(init);
			});
			obj.addToSynthDef(this,name,init);
			obj
		});
		outputProxies = this.buildControls;
		// the objects themselves know how best to be represented in the synth graph
		// they wrap themselves in In.kr In.ar or they add themselves directly eg (Env)
		^outputProxies.collect({ arg outp,i;
			defargs.at(i).instrArgFromControl(outp,i)
		})
	}

	// give more information, relevant to the def function evaluated
	checkInputs {
		var errors,message;
		children.do { arg ugen;
			var err;
			if ((err = ugen.checkInputs).notNil) {
				errors = errors.add([ugen, err]);
			};
		};
		if(errors.notNil,{
			this.buildErrorString.postln;
			errors.do({ |err|
				var ugen,msg;
				#ugen,msg = err;
				("UGen" + ugen.class.asString + msg).postln;
				ugen.dumpArgs;
			});
			//instr.func.asCompileString.postln;
			//this.dumpUGens;
			Error("SynthDef" + this.name + "build failed").throw;
		});
		^true
	}
	finishBuild {
		super.finishBuild;
		inputs = nil;
		outputProxies = nil;
		instr = nil; // could hold onto stuff
	}
	instr {
		^(instr ?? {instrName.asInstr})
	}
	buildErrorString {
		^String.streamContents({ arg stream;
				stream << Char.nl << "ERROR: Instr build failed; " <<< instr << Char.nl;
				stream << "Inputs:" << Char.nl;
				inputs.do({ |in,i|
					stream << Char.tab << instr.argNameAt(i) << ": " << in << Char.nl;
				});
				stream << "Args passed into Instr function:" << Char.nl;
				outputProxies.do({ |op,i|
					stream << Char.tab << instr.argNameAt(i) << ": " << op << Char.nl;
				});
				if(secretIr.notNil or: secretKr.notNil,{
					stream << "Secret args passed in:" << Char.nl;
					secretIr.do({ |op,i|
						stream << Char.tab << op[1] << ": " << op[2] << " (" << op[0] << ":" << op[3] << ")" << Char.nl;
					});
					secretKr.do({ |op,i|
						stream << Char.tab << op[1] << ": " << op[2] << " (" << op[0] << ":" << op[3] << ")" << Char.nl;
					});
				});
			});
	}
	tempoKr { arg object,selector;
		// first client to request will later be asked to produce the TempoBus
		^(tempTempoKr ?? {
			tempTempoKr = In.kr(
				this.addSecretKr(object,1.0,selector)
			)
		})
	}

	*cacheAt { arg defName,server;
		^Library.at(SynthDef,server ? Server.default,defName.asSymbol)
	}
	*cachePut { arg def,server;
		Library.put(SynthDef,server ? Server.default,def.name.asSymbol,def)
	}
	*loadDefFileToBundle { arg def,bundle,server;
		var dn;
		dn = def.name.asSymbol;
		if(Library.at(SynthDef,server,dn).isNil,{
			bundle.addPrepare(["/d_recv", def.asBytes]);
			this.watchServer(server);
			this.cachePut(def,server);
		});
	}
	*watchServer { arg server;
		if(NotificationCenter.registrationExists(server,\didQuit,this).not,{
			NotificationCenter.register(server,\didQuit,this,{
				if(server.isLocal and: {thisProcess.platform.isKindOf(UnixPlatform)}, {
					if(("ps -ef|grep" + server.pid).unixCmdGetStdOut.split("\n").any(_.contains("scsynth")).not,{
						this.clearCache(server);
						NotificationCenter.notify(server,\reallyDidQuit)
					},{
						("Server process still found, not dead yet:" + server.pid).debug;
					})
				}, {
					this.clearCache(server);
				});
			});
		});
	}
	*clearCache { arg server;
		"Clearing AbstractPlayer SynthDef cache".inform;
		Library.global.removeAt(SynthDef,server ? Server.default);
	}
	*loadCacheFromDir { arg server,dir;
		dir = dir ? SynthDef.synthDefDir;
		(dir++"*").pathMatch.do({ arg p;
			var defName;
			defName = PathName(p).fileNameWithoutExtension;
			Library.put(SynthDef,server,defName.asSymbol,\assumedLoaded);
		})
	}
	*cacheRemoveAt { arg defName,server;
		(server ? Server.allRunningServers).do({ |s|
			Library.global.removeAt(SynthDef,s,defName.asSymbol);
		})
	}
	*freeDef { arg defName,server;
		(server ? Server.allRunningServers).do({ |s|
			s.sendMsg("/d_free",defName);
			Library.global.removeAt(SynthDef,s,defName.asSymbol);
		})
	}
	*freeAll { arg server;
		(server ? Server.allRunningServers).do({ |s|
			Library.at(SynthDef,s).keys.do({ arg defName;
				s.sendMsg("/d_free",defName);
				Library.global.removeAt(SynthDef,s,defName.asSymbol);
			})
		})
	}

	*buildSynthDef {
		var sd;
		sd = UGen.buildSynthDef;
		if(sd.isNil,{
			Error("Not currently inside a synth def ugenFunc; No synth def is currently being built.").throw;
		});
		if(sd.isKindOf(InstrSynthDef).not,{
			Error("This requires an InstrSynthDef.").throw;
		})
		^sd
	}

	// execute the func in the client whenever triggered
	// see Patch help
	onTrig { |trig,func,value=0.0|
		// triggerID is the nTh onTrig we have so far added + 9999
		var triggerID,onTrig;
		triggerID = stepchildren.select({|sc|sc.isKindOf(ClientOnTrigResponder)}).size + 9999;
		onTrig = ClientOnTrigResponder(triggerID,func);
		stepchildren = stepchildren.add(onTrig);
		if(trig.rate == \control,{
			^SendTrig.kr(trig,triggerID,value)
		},{
			^SendTrig.ar(trig,triggerID,value)
		})
	}

	/*synthProxy {
		^synthProxy ?? {
			synthProxy = SynthProxy.new;
			stepchildren = stepchildren.add(synthProxy);
			synthProxy
		}
	}*/
}



ClientOnTrigResponder {

	var <>triggerID, <>func,responder;

	*new { |triggerID,func|
		^super.newCopyArgs(triggerID,func)
	}
	didSpawn { |synth|
		var commandpath = ['/tr', synth.nodeID, triggerID];
		responder = OSCpathResponder(synth.server.addr, commandpath,
			{|time,responder,message| func.value(time,message[3]) });
		responder.add;
	}
	stopToBundle { |b|
		b.addFunction({ responder.remove; responder = nil })
	}
}

// SynthProxy is a way to access the Synth once the SynthDef has started playing
// there is only one SynthProxy per synth def, though there may be multiple synths spawned
// the synthProxy is in stepchildren and in the Patch's stepChildren so it is prepared and spawned.
// it is roughly equivalent to the synth argument in SC2's Spawn
//
/*SynthProxy  {

	var events,sched,<synth;

	spawnToBundle { |b|
		b.addMessage(this,\didSpawn)
	}

	didSpawn { |synth|
		sched = BeatSched.new;
		// sched any events
		events.do({ |df|
			sched.sched(df[0],df[1])
		})
	}
	sched { |delta, function|
		events = events.add([delta,function]);
	}
	/ *channelOffset_ {
		// shift the Out.ar
	}* /
}*/
