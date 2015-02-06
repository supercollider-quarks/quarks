

MxUnit.register('InstrStack', (

    make: { arg instr;

        var inlets, outlets, patch, inps, connectors, conn, handlers;
        // accept either an instr or [instr, inputs]
        if(instr.isSequenceableCollection, {
            # instr, inps = instr;
        }, {
            inps = instr.specs.collect({ arg spec, i; MxJack.forSpec(spec, instr.defArgAt(i) ) });
        });
        patch = Patch(instr, inps );
        connectors = patch.inputs.collect({ arg inp, i;
            if(inp.isKindOf(MxArJack), {
                MxHasJack({inp}, {patch.server}, {patch.group})
            }, {
                if(inp.isKindOf(MxKrJack), {
                    MxHasKrJack({inp}, {patch.server}, {patch.group})
                }, {
                    // many things it could be
                    nil
                })
            })
        });
        inlets = instr.specs
            .collect({ arg spec, i; MxInlet(instr.argNameAt(i), i, spec, connectors[i] ) });

        conn = if(instr.outSpec.rate  === 'control', {MxPlaysOnKrBus}, {MxPlaysOnBus}).new({patch.bus}, {patch.server}, {patch.group});

        outlets = [ MxOutlet( (instr.outSpec.findKey ? instr.outSpec.class).asSymbol, 0, instr.outSpec, conn ) ];

        ~source = patch;

        MxUnit(instr, inlets, outlets);
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
        bundle.add( ~source.synth.moveToHeadMsg(aGroup) );
    },

    numChannels: {
        ~source.numChannels
        },

    play: { arg group, atTime, bus;
        ~source.play(group, atTime, bus)
        },
    stop: { arg atTime, andFreeResources = true;
        ~source.stop(atTime, andFreeResources)
        },
    isPlaying: {
        ~source.isPlaying
    },
    save: {
        ~source
    },
    load: { arg stack;
        // not working

    },
    name: { ~source.instr.asString },

    gui: { arg layout;
        var l;
        l = layout ?? {PageLayout(~source.instr.asString, Rect(0, 0, 500, 600))};
        ~source.gui(l);
        if(layout.isNil, {
            l.resizeToFit
        })
    }
));
