+ NodeProxy {
	gui { | numItems, bounds, preset|
		// which options to support? 
		numItems = numItems ?? { max(8, this.controlKeys.size) };
		^NdefGui(this, numItems, nil, bounds, options: preset);
	}
}

+ ProxySpace {
	gui { | numItems, bounds, preset|
		numItems = numItems ?? { max(12, this.envir.size) };
		^ProxyMixer(this, numItems, nil, bounds, options: preset);
	}
}

+ Ndef {
	*gui { |server, numItems, bounds, preset|
		var space;
		server = server ? Server.default;
		space = all[server.name];
		numItems = numItems ?? { max(8, try { space.envir.size } ? 0) };
		^NdefMixer(space, numItems, nil, bounds, options: preset);
	}
}

+ Tdef {
	*gui { | numItems, bounds, preset|
		numItems = numItems ?? { max(12, all.size) };
		^TdefAllGui(numItems, nil, bounds, options: preset);
	}
	gui { | numItems, bounds, preset|
		numItems = numItems ?? { max(8, this.envir.size) };
		^TdefGui(this, numItems, nil, bounds, options: preset);
	}
}
+ Pdef {
	*gui { | numItems, bounds, preset|
		numItems = numItems ?? { max(12, all.size) };
		^PdefAllGui(numItems, nil, bounds, options: preset);
	}
	gui { | numItems, bounds, preset|
		numItems = numItems ?? { max(8, this.envir.size) };
		^PdefGui(this, numItems, nil, bounds, options: preset);
	}
}

+ Pdefn {
	*gui { | numItems, bounds, preset|
		numItems = numItems ?? { max(12, all.size) };
		^PdefnAllGui(numItems, nil, bounds, options: preset);
	}
	gui { | bounds, preset|
		^PdefnGui(this, 1, bounds, options: preset);
	}
}


+ Dictionary {
	gui { | numItems, bounds, preset|
		numItems = numItems ?? { max(8, this.size) };
		^EnvirGui(this, numItems, nil, bounds, options: preset);
	}
}
