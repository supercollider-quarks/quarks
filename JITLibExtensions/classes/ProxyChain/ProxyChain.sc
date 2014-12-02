/********
///////////////// Possible next extensions ////////////////

*	insert new slotNames by name, or remove existing slotnames, keeping the structure consistent;
	for reconfiguration of the list of proxychain slots that can be used.
	That would require a better gui where the buttons can be updated.

	////////////// not done yet //////////

	// replace a slot given by name
c.replace(\dust, \noyz, mix ->  { |nfreq1=1200| LFDNoise0.ar(nfreq1) });


	insertAt(index, name, funcOrAssoc)
		inserts in the chain at this index,
		replaces if a slot exists there.

c.insertAt(5, \noyz, mix ->  { |nfreq2=1200| GrayNoise.ar(nfreq2) });

	insertAfter(index, name, funcOrAssoc)
	insertBefore(index, name, funcOrAssoc)
		inserts after (or before) a given slot - halfway toward the neighbour.
		e.g.
c.insertAfter(\dust, \klong, \filter -> { |in, freq=400, att=0.01, decay=0.3, slope=0.8|
	Formlet.ar(in, freq * [0.71, 1, 1.4], att, decay * [1/slope, 1, slope]).sum;
});

	// after which slot, name, funcOrAssoc;
c.insertBefore(\dust, \klong, \filter -> { |in, freq=400, att=0.01, decay=0.3, slope=0.8|
	Formlet.ar(in, freq * [0.71, 1, 1.4], att, decay * [1/slope, 1, slope]).sum;
});
******/


ProxyChain {

	classvar <allSources;
	classvar <all;

	var <slotNames, <slotsInUse, <proxy, <sources;

	*initClass {
		allSources = ();
		all = ();
	}

	*add { |...args|
		args.pairsDo { |k, v| allSources.put(k, v) }
	}

	*from { arg proxy, slotNames = #[];
		^super.new.init(proxy, slotNames)
	}

	key { ^all.findKeyForValue(this) }

	*new { arg key, slotNames, numChannels, server;
		var proxy;
		var res = all.at(key);
		if(res.isNil) {
			proxy = NodeProxy.audio(server ? Server.default, numChannels);
			res = this.from(proxy, slotNames);
			if (key.notNil) { all.put(key, res) };
		};

		if(slotNames.notNil) { res.slotNames_(slotNames) }

		^res
	}

	init { |argProxy, argSlotNames|

		slotNames = Order.new;
		slotsInUse = Order.new;
		sources = ();
		sources.parent_(allSources);

		proxy = argProxy;
		if (proxy.key.notNil) { all.put(proxy.key, this) };

		this.slotNames_(argSlotNames);
	}

	slotNames_ { |argSlotNames|
		slotNames.clear;
		argSlotNames.do { |name, i| slotNames.put(i + 1 * 10, name) };
	}

	add { |key, wet, func| 	// assume the index exists
		var index = slotNames.indexOf(key);
			// only overwrite existing keys so far.
		if (func.notNil, { this.sources.put(key, func) });
		this.addSlot(key, index, wet);
	}

	remove { |key|
	 	var oldSlotIndex = slotsInUse.indexOf(key);
		if (oldSlotIndex.notNil) { proxy[oldSlotIndex] = nil; };
		slotsInUse.remove(key);
	}

	addSlot { |key, index, wet|

		var func = sources[key];
		var prefix, prevVal, specialKey;
		if (func.isNil) { "ProxyChain: no func called \%.\n".postf(key, index); ^this };
		if (index.isNil) { "ProxyChain: index was nil.".postln; ^this };

		this.remove(key);
		slotsInUse.put(index, key);

		if (func.isKindOf(Association)) {
			prefix = (filter: "wet", mix: "mix", filterIn: "wet")[func.key];
			specialKey = (prefix ++ index).asSymbol;
			prevVal = proxy.nodeMap.get(specialKey).value;
			if (wet.isNil) { wet = prevVal ? 0 };
			proxy.set(specialKey, wet);
		};
		proxy[index] = func;
	}

	setSlots { |keys, levels=#[], update=false|
		var keysToRemove, keysToAdd;
		if (update) {
			keysToRemove = slotsInUse.copy;
			keysToAdd = keys;
		} {
			keysToRemove = slotsInUse.difference(keys);
			keysToAdd = keys.difference(slotsInUse);
		};

		keysToRemove.do(this.remove(_));
		keysToAdd.do { |key, i| this.add(key, levels[i]) };
	}

		// forward basic messages to the proxy
	play { arg out, numChannels, group, multi=false, vol, fadeTime, addAction;
		proxy.play(out, numChannels, group, multi=false, vol, fadeTime, addAction)
	}

	playN { arg outs, amps, ins, vol, fadeTime, group, addAction;
		proxy.playN(outs, amps, ins, vol, fadeTime, group, addAction);
	}

	stop { arg fadeTime, reset=false;
		proxy.stop(fadeTime, reset);
	}

	end { arg fadeTime, reset=false;
		proxy.end(fadeTime, reset);
	}


		// JIT gui support
	gui { |numItems = 16, buttonList, parent, bounds, isMaster = false|
		^ProxyChainGui(this, numItems, parent, bounds, true, buttonList, isMaster);
	}

	// // this is probably not needed anymore
	// // old NodeProxyEditor
	// informEditor { |ed|
	// 	slotNames.do { |name, i| ed.replaceKeys.put(("wet" ++ i).asSymbol, name) };
	// 	slotNames.do { |name, i| ed.replaceKeys.put(("mix" ++ i).asSymbol, name) };
	// }
	//
	// makeEdit { |name, nSliders=24, parent, bounds|
	// 	var ed = NdefGui(proxy, nSliders, parent, bounds);
	// 	//	this.informEditor(ed);
	// 	^ed
	// }
}
