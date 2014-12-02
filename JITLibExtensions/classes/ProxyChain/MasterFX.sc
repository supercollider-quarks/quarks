MasterFX { 

	classvar <all, <maxNumChans = 8;
	var <group, <numChannels, <busIndex, <server, <pxChain; 
	var <checkingBadValues = true, <badSynth, badDefName;
	
	*initClass { 
		all = IdentityDictionary.new;
	}
	
	*new { |server, numChannels, slotNames, busIndex=0| 
		// only one masterfx per server ATM.
		// could be changed if different MasterFX 
		// for different outchannel groups are to be used.
		
		var fx = all[server.name];
		if (fx.notNil) { 
			"// MasterFX for server % exists - use \nMasterFX.clear(%) \n// to make a new one.\n"
				.postf(server.name, server.name.asCompileString);
			^fx
		} { 
			^this.make(server, numChannels, slotNames, busIndex) 
		}
	}
	
	*make { |server, numChannels, slotNames| 
		^super.new.init(server, numChannels, slotNames);
	}
	
	add { |key, wet, func| 
		pxChain.add(key, wet, func);
	}
	remove { |key| 
		pxChain.remove(key);
	}
	
	bus { 
		^Bus.new(\audio, busIndex, numChannels, server);
	}

					
	cmdPeriod { 
		group.freeAll; 	// for SharedServers
						// evil just to wait? hmmm. 
		defer({ this.wakeUp }, 0.2);
	}
	
	hide { 
		Ndef.all[server.name].envir.removeAt(pxChain.proxy.key);
	}
		// maybe it is useful to see it under some circumstances
	show { 
		Ndef.all[server.name].envir.put(pxChain.proxy.key, pxChain.proxy);
	}
	
	init { |inServer, inNumChannels, inSlotNames, inBusIndex| 
		var proxy;
		server = inServer ? Server.default;
		numChannels = inNumChannels ? server.options.numOutputBusChannels;
		busIndex = inBusIndex ? 0; 

		proxy = Ndef(\zz_mastafx -> server.name); 
		proxy.ar(numChannels); 
		proxy.bus_(this.bus);
		pxChain = ProxyChain.from(proxy, inSlotNames ? []);
		
		this.hide;	// hide by default
		
		all.put(server.name, this);
		
		this.makeGroup; 
		CmdPeriod.add(this);
		
		badDefName = ("BadMasterFX_" ++ server.name).asSymbol;
		SynthDef(badDefName, {
			var snd = In.ar(busIndex, numChannels); 
			var dt = 0.001;
			var isOK = (CheckBadValues.ar(snd) < 0.001);
			var gate = (isOK * DelayN.ar(isOK, dt * 2));
			var outSnd = 	DelayL.ar(snd, dt) * gate;
			ReplaceOut.ar(busIndex, outSnd)
		}).add;
		
		fork { 
			0.2.wait; 
			this.checkBad(checkingBadValues);
		};
	}
	
	makeGroup { 
		group = Group.new(1.asGroup, \addAfter).isPlaying_(true);
		pxChain.proxy.parentGroup_(group);
	}
	
	wakeUp { 
		"\nMasterFX for server % waking up.\n\n".postf(server.name); 
			this.makeGroup; 
			pxChain.proxy.wakeUp;
			this.checkBad;		
	}
	
	clear { 
		CmdPeriod.remove(this);
		pxChain.proxy.clear;
		all.removeAt(pxChain.proxy.server.name);
	}
	
	*clear { |name| 
		(name ?? { all.keys }).do { |name| 
			all.removeAt(name).clear;
		};
	}
	
	makeName { 
		^(this.class.name ++ "_" ++ server.name 
		++ "_" ++ pxChain.proxy.numChannels).asSymbol 
	}
	
	gui { |name, numItems, buttonList, parent, bounds, makeSkip = true|
			// the effects are all on by default: 
		buttonList = buttonList ?? { pxChain.slotNames.collect ([_, \slotCtl, 1]) };
		name = name ?? { this.makeName };
		numItems = numItems ? 16; 
		^MasterFXGui(pxChain, numItems, parent, bounds, makeSkip, buttonList)
			.name_(name);
	}
	
	checkBad { |flag = true| 
		checkingBadValues = flag;
		badSynth.free; 
		if (checkingBadValues) { 
			badSynth = Synth(badDefName, target: group, addAction: \addAfter);
		};
	}
	
	*default { ^all[Server.default.name] }
}