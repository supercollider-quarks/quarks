
var cs;
cs = ControlSpec(0, 127, \lin, 1);

MxUnit.register('CCBank', // crucial-library
(
    make: { arg bank;
        var outlets;
        outlets = bank.sets.collect { arg set, i;
                        MxOutlet(set.key.asString, i, cs , MxSendsValueOnChanged({bank.at(set.key)}))
                    };
        MxUnit(bank, [], outlets);
    },
    spec: { cs     },
    prepareToBundle:  { arg agroup, bundle, private, bus;
        ~source.sets.do { arg set, i;
            var ccr;
            ccr = ~source.responder(set.key, { arg val; {ccr.changed(val)}.defer });
        };
    },
    spawnToBundle: { arg bundle; },
    freeToBundle: { arg bundle;
        bundle.addFunction({
            ~source.free
        }.inEnvir)
    },
    //save: { ~source },
    //load: { arg bank; bank     },
    gui: { arg layout;
        ~source.gui(layout)
    }
));
