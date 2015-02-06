
/*
    should be able to inherit from Instr
*/

MxUnit.register('InstrBuilder', (

    make: { arg data;

        var instr, inlets, outlets, patch, inps, connectors, conn, handlers;
        var builder;

        // accept either an instr or [instr, inputs]
        if(data.isSequenceableCollection, {
            # builder, inps = data;
            ~source = builder;
            if(builder.isString, {
                builder = builder.loadPath;
            });
            instr = builder.makeStack;
        }, {
            ~source = data;
            instr = data.makeStack;
            inps = instr.specs.collect({ arg spec, i; MxJack.forSpec(spec, instr.defArgAt(i) ) });
        });

        patch = Patch(instr, inps);
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

        ~patch = patch;

        ~source.onCompile = { arg stack, builder;
            var playing=false;
            ~patch.instr = stack;
            if(playing = ~patch.isPlaying, {
                ~respawn.value();
            });
        }.inEnvir;

        MxUnit(~source, inlets, outlets);
    },
    prepareToBundle: { arg agroup, bundle, private, bus;
        ~patch.prepareToBundle(agroup, bundle, private, bus);
        },
    spawnToBundle: { arg bundle;
        ~patch.spawnToBundle(bundle)
        },
    stopToBundle: { arg bundle;
        ~patch.stopToBundle(bundle)
        },
    freeToBundle: { arg bundle;
        ~patch.freeToBundle(bundle)
        },
    moveToHead: { arg aGroup, bundle, currentGroup;
        bundle.add( ~patch.synth.moveToHeadMsg(aGroup) );
    },

    numChannels: {
        ~patch.numChannels
        },
    spec: {
        ~patch.spec
    },

    play: { arg group, atTime, bus;
        ~patch.play(group, atTime, bus)
        },
    stop: { arg atTime, andFreeResources = true;
        ~patch.stop(atTime, andFreeResources)
        },
    respawn: {
        var group, bus;
        group = ~patch.group;
        bus = ~patch.bus;
        ~patch.stop;
        {
            ~patch.play(group, 1, bus)
        }.inEnvir.defer(1);
    },
    isPlaying: {
        ~patch.isPlaying
    },
    save: {
        var path;
        if(~source.path.notNil, {
            path = Document.abrevPath(~source.path)
        }, {
            path = ~source
        });
        [path ? ~source, ~patch.inputs]
    },
    load: { arg data;
        var builder, inps;
        # builder, inps = data;
        if(builder.isString, {
            // load from docs if its a relative path
            builder = InstrBuilder.load(builder)
        });
        [builder, inps]
    },
    name: {
        if(~source.path.notNil, {
            PathName(~source.path).fileNameWithoutExtension
        }, {
            "InstrBuilder"
        })
    },

    gui: { arg layout;
        var l;
        l = layout ?? {PageLayout(~name.value, Rect(0, 0, 500, 600))};
        ActionButton(l, "EDIT" + (~name.value), {
            ~source.gui
        }.inEnvir);
        ~patch.inputs.do { arg inp, i;
            ArgNameLabel( ~patch.argNameAt(i), l.startRow );
            inp.gui(l);
        };
        if(layout.isNil, {
            l.resizeToFit
        })
    }
));
