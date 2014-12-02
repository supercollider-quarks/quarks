

TdefPreset : ProxyPreset {

	classvar <all;
	var <key;

	*initClass { all = () }

	*proxyClass { ^Tdef }

	*new { |key, namesToStore, settings, specs, morphFuncs|

		var res, proxy;
		if (key.isKindOf(this.proxyClass)) {
			proxy = key;
			key = proxy.key;
		};

		res = all[key];

		if (res.isNil) {
			// find proxy with same name
			proxy = this.proxyClass.all[key];
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

PdefPreset : ProxyPreset {

	classvar <all;
	var <key;

	*initClass { all = () }

	*proxyClass { ^Pdef }

	*new { |key, namesToStore, settings, specs, morphFuncs|

		var res, proxy;
		if (key.isKindOf(this.proxyClass)) {
			proxy = key;
			key = proxy.key;
		};

		res = all[key];

		if (res.isNil) {
			// find proxy with same name
			proxy = this.proxyClass.all[key];
			if (proxy.notNil) {
				res = super.new(proxy, namesToStore,
					settings, specs, morphFuncs)
				.prAdd(key);
				// by default, store to disk, next to the
				// code file from which the preset was loaded.
				res.storeToDisk_(true).setPath;
				res.currFromProxy;
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
