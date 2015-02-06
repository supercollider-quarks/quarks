

MxUnit.register('EventListPlayer', (

    make: { arg elp;

        var instr, inlets, outlets, inps, adapters, conn, deltaSpec;
        var sbk;

        ~source = elp;

        inlets = [];
        outlets = [ MxOutlet("out", 0, elp.spec, MxPlaysOnBus({elp.bus}, {elp.server}, {elp.group})) ];

        MxUnit(elp, inlets, outlets);
    },
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
    save: {
        [~source]
    },
    load: { arg args;
        args.first
    },
    name: { ~source.name ? "EventListPlayer "},

    gui: { arg layout;
        var l;
        l = layout ?? {PageLayout(~name.value, Rect(0, 0, 500, 600))};
        ~source.gui(l);
        if(layout.isNil, {
            l.resizeToFit
        })
    },
    timeGui: { arg layout, bounds, maxTime;
        ~elpGui = ~source.guiClass.new(~source);
        ~elpGui.timeGui(layout, bounds, maxTime);
    },
    zoomTime: { arg from, to;
        ~elpGui.setZoom(from, to).update;
    },
    beatDuration: {
        ~source.beatDuration
    }
));
