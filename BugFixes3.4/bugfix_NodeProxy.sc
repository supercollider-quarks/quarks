
// this fixes missing update with audio rate mapping

+ NodeProxy {
	
	put { | index, obj, channelOffset = 0, extraArgs, now = true |			
			var container, bundle, orderIndex;
			if(obj.isNil) { this.removeAt(index); ^this };
			if(index.isSequenceableCollection) { 						^this.putAll(obj.asArray, index, channelOffset)
			};

			orderIndex = index ? 0;
			container = obj.makeProxyControl(channelOffset, this);
			container.build(this, orderIndex); // bus allocation happens here

			if(this.shouldAddObject(container, index)) {
				bundle = MixedBundle.new;
				if(index.isNil)
					{ this.removeAllToBundle(bundle) }
					{ this.removeToBundle(bundle, index) };
				objects = objects.put(orderIndex, container);
			} {
				format("failed to add % to node proxy: %", obj, this).inform;
				^this
			};

			if(server.serverRunning) {
				now = awake && now;
				if(now) {
					this.prepareToBundle(nil, bundle);
				};
				container.loadToBundle(bundle, server);
				loaded = true;
				if(now) {
					container.wakeUpParentsToBundle(bundle);
					this.sendObjectToBundle(bundle, container, extraArgs, index);
				};
				nodeMap.wakeUpParentsToBundle(bundle); // bugfix: wake up mapped audio rate proxies
				bundle.schedSend(server, clock ? TempoClock.default, quant);
			} {
				loaded = false;
			}

	}	
	
	
}

+ Ndef {
	
	*new { | key, object |
		// key may be simply a symbol, or an association of a symbol and a server name
		var res, server, dict;

		if(key.isKindOf(Association)) {
			server = Server.named.at(key.value);
			if(server.isNil) {
				Error("Ndef(%): no server found with this name.".format(key)).throw
			};
			key = key.key;
		} {
			server = Server.default;
		};

		dict = this.dictFor(server);
		res = dict.envir.at(key);
		if(res.isNil) {
			res = super.new(server).key_(key);
			dict.initProxy(res);
			dict.envir.put(key, res)
		};

		object !? { res.source = object };
		^res;
	}
	
	*do { |func|
		all.do { |dict| dict.envir.do(func) }	
	}
	
	
}

+ ProxySpace {
	
	makeProxy { arg class;
		var proxy = NodeProxy.new(server);
		this.initProxy(proxy);
		^proxy
	}
	
	initProxy { arg proxy;
		proxy.clock = clock;
		proxy.awake = awake;
		if(fadeTime.notNil) { proxy.fadeTime = fadeTime };
		if(group.isPlaying) { proxy.parentGroup = group };
		if(quant.notNil) { proxy.quant = quant };
	}	
	
}