

MxUnit.register('Bus', (

    make: { arg bus;

        var inlets, outlets;

        ~source = bus;

        inlets = [ MxInlet("in", 0, AudioSpec(bus.numChannels), MxListensToBus({bus}, {bus.server}) ) ];
        outlets = [ MxOutlet( "out", 0, AudioSpec(bus.numChannels), MxPlaysOnBus({bus}, {bus.server}) ) ];

        MxUnit(bus, inlets, outlets);
    }
));
