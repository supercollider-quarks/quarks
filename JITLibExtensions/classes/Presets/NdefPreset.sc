NodeProxyPreset : ProxyPreset {

	*proxyClass { ^NodeProxy }

	*new { |obj, namesToStore, settings, specs, morphFuncs|

		var res, proxy;
		if (obj.isKindOf(this.proxyClass)) {
			proxy = obj;
			res = super.new(proxy, namesToStore,
				settings, specs, morphFuncs);
			res.currFromProxy;
		} {
			"NodeProxyPreset: object not a NodeProxy.".postln;
		};
		^res
	}

	proxy_ { |px|
		if (px.isKindOf(this.proxyClass)) {
			proxy = px;
			this.useHalo(proxy);
			// properly init state
			this.currFromProxy;
			currSet = targSet = this.getSet(\curr);
			this.setPath;
		};
	}

	// DIFFERENT FROM other ProxyPresets
	getFromProxy { |except| ^proxy.getKeysValues(except) }

}

NdefPreset : NodeProxyPreset {

	classvar <all;
	var <key;

	*initClass { all = () }

	*proxyClass { ^Ndef }

	*new { |key, namesToStore, settings, specs, morphFuncs|

		var res, proxy;
		if (key.isKindOf(this.proxyClass)) {
			proxy = key;
			key = proxy.key;
		};

		res = all[key];

		if (res.isNil) {
			// find proxy with same name
			proxy = this.proxyClass.dictFor(Server.default)[key];

			if (proxy.notNil) {
				res = super.new(proxy, namesToStore,
					settings, specs, morphFuncs).prAdd(key);
				res.currFromProxy;
				// by default, store to disk, next to the
				// code file from which the preset was loaded.
				res.storeToDisk_(true).setPath;
			} {
				"% - no preset or proxy found.\n".postf(this.proxyClass);
			};
		};
		^res
	}

	prAdd { arg argKey;
		key = argKey;
		all.put(argKey, this);
	}

	proxy_ { |px|
		if (px.isKindOf(this.proxyClass)) {
			proxy = px;
			this.useHalo(proxy);
			// properly init state
			this.currFromProxy;
			currSet = targSet = this.getSet(\curr);
			this.setPath;
		};
	}

	storeArgs { ^[key] }
	printOn { | stream | ^this.storeOn(stream) }

}
