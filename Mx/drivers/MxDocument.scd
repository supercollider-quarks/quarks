
MxUnit.register('MxDocument', (

    make: { arg doc;
        ~source = doc;
        MxUnit(doc, [], []);
    },
    didLoad: {
        ~evalCode.value();
    },
    evalCode: {
        var pn;
        try {
            ~source.eval
        } { arg e;
            ~source.error;
            e.errorString.postln;
            e.reportError
        }
    },
    save: {
        ~source
    },
    load: { arg doc;
        doc
    },

    name: {
        ~source.name
    },
    gui: { arg layout;
        ~source.gui(layout)
    },
/*
    prepareToBundle: { arg agroup, bundle, private, bus;
        ~source.prepareToBundle(agroup, bundle, private, bus);
        },
    spawnToBundle: { arg bundle;
        ~source.spawnToBundle(bundle)
        },
    stopToBundle: { arg bundle;
        ~source.stopToBundle(bundle)
        },
    freeToBundle: { arg bundle;
        ~source.freeToBundle(bundle)
        },
    moveToHead: { arg aGroup, bundle, currentGroup;
        bundle.add( ~source.spawnGroup.moveToHeadMsg(aGroup) );
    },

    numChannels: {
        ~source.numChannels
        },
    spec: {
        ~source.spec
    },
    play: { arg group, atTime, bus;
        ~source.play(group, atTime, bus)
        },
    stop: { arg atTime, andFreeResources = true;
        ~source.stop(atTime, andFreeResources)
        },
    respawn: {
        var group, bus;
        group = ~source.group;
        bus = ~source.bus;
        ~source.stop;
        ~source.play(group, 1, bus)
    },
    gotoBeat: { arg beat, atBeat, bundle;
        ~source.gotoBeat(beat, atBeat, bundle)
    },

    isPlaying: {
        ~source.isPlaying
    },
*/
));
