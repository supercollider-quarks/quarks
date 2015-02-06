

MxUnit.register('Sample',
(
    make: { arg sample;
        var inlets, outlets;
        ~source = sample;
        inlets  = [  ];
        outlets = [ MxOutlet("smp", 0, \sample.asSpec, MxSendSelfOnChanged({arg val; sample}) ) ];
        MxUnit(sample, inlets, outlets);
    },
    play: { arg group, atTime, bus;
    },
    stop: { arg atTime, andFreeResources = true;
    },
    save: {
        ~source
    },
    load: { arg smp;
        smp
    },
    spec: {
        ~source.spec
    }
));
