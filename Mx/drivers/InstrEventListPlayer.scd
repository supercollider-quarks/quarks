

MxUnit.register('InstrEventListPlayer', (

    make: { arg elp;

        var instr, inlets, outlets, inps, adapters, conn, deltaSpec;

        ~source = elp;

        inlets = [];
        elp.instrArgs.keysValuesDo { arg k, v, i;
            inlets = inlets.add( MxInlet(k, i, v, MxSetter({ arg v; elp.postFilterPut(k, v) }) ) );
        };
        outlets = [ MxOutlet("out", 0, elp.spec, MxPlaysOnBus({elp.bus}, {elp.server}, {elp.group})) ];

        // ~argWatcher = Updater
        // add new inlets
        // needs an mx unload

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
        [~source.events.enpath, ~source.spec]
    },
    load: { arg args;
        // does not save its postFilter, those are the inlets
        InstrEventListPlayer(args[0], args[1]);
    },
    name: { ~source.name ? "InstrEventListPlayer "},

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
