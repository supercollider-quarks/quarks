
// not done

MxUnit.register('SoundIn',
(
	make: { arg chan;
		var inlets,outlets,patch,inps,connectors,conn,handlers;
		// isnt it a MxHasJack ??
		inlets  = [ MxInlet("MxChanIn", 0, AudioSpec(chan.numChannels), MxListensToBus({chan.source.bus},{chan.server},{chan.mixGroup})) ];
		// to add: control inputs for fader
		
		outlets = [ MxOutlet("MxChanOut", 0, AudioSpec(chan.numChannels),MxPlaysOnBus({chan.source.bus}) ) ];

		MxUnit(chan,inlets,outlets);
	},
	draw: { arg pen,bounds,style;
		~source.fader.draw(pen,bounds,style)
	}	
));
