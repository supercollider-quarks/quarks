


MxUnit.register('NodeProxy',(

	make: { arg np;
		
		var inlets,outlets;
		
		~source = np;
		
		inlets = [ MxInlet("in",0,AudioSpec(np.numChannels), MxListensToBus({~source.bus},{~source.server},{~source.group}) ) ];
		outlets = [ MxOutlet( "out", 0, AudioSpec(np.numChannels), MxPlaysOnBus({~source.bus},{~source.server},{~source.group}) ) ];
		
		MxUnit(np,inlets,outlets);
	},
	prepareToBundle: { arg agroup, bundle, private, bus;
		~bus = bus ?? { Bus.audio };
		[bus,agroup].debug("prep");
		bundle.addFunction({
			~source.bus = ~bus;
			~source.group = agroup;
		}.inEnvir)
	},
	spawnToBundle: { arg bundle;
		bundle.addFunction({
			~source.play
		})
	}
));
