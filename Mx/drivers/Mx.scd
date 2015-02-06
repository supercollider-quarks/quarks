

/*
 inlets:
     also channel fader controls

*/

MxUnit.register('Mx',
(
    make: { arg mx;
        var inlets, outlets, patch, inps, connectors, conn, handlers;
        inlets = [];
        outlets = [];
        // MxOutlet("MxChannelOut", 0, AudioSpec(chan.numChannels), MxPlaysOnBus({chan.source.bus}) ) ];

        MxUnit(mx, inlets, outlets);
    }
));
