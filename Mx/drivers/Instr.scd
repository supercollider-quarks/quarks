

MxUnit.register('Instr', (

    make: { arg instr; // accepts either an instr or [instr, inputs]

        var inlets, outlets, patch, inps, connectors, conn, handlers;

        if(instr.isSequenceableCollection, {
            # instr, inps = instr;
            instr = instr.asInstr;
        }, {
            instr = instr.asInstr;
            inps = instr.specs.collect({ arg spec, i;
              MxJack.forSpec(spec, instr.defArgAt(i) )
            });
        });
        if(instr.outSpec.isNil, {
            instr.outSpec = \audio.asSpec
        });
        patch = Patch(instr, inps );
        connectors = patch.inputs.collect({ arg inp, i;
            if(inp.isKindOf(MxArJack), {
                MxHasJack({inp}, {patch.server}, {patch.group})
            }, {
                if(inp.isKindOf(MxControlJack), {
                  // ControlSpec
                    MxHasKrJack(
                      {inp},
                      {patch.server},
                      {patch.group},
                      {
                        inp.value;
                      },
                      { arg v;
                        inp.value = v;
                      })
                }, {
                    // many things it could be
                    // NumberEditor : wont modulate but can set on spawn
                    // but MxIrJack won't modulate, set on spawn
                    ("No adapter found for" + inp + instr.specs.at(i) + instr.argNameAt(i) + "in" + instr).warn;
                    nil
                })
            })
        });
        inlets = instr.specs
            .collect({ arg spec, i; MxInlet(instr.argNameAt(i), i, spec, connectors[i] ) });

        conn = if(instr.outSpec.rate  === 'control', {MxPlaysOnKrBus}, {MxPlaysOnBus}).new({patch.bus}, {patch.server}, {patch.group});

        if(instr.outSpec.notNil, {
            outlets = [MxOutlet( (instr.outSpec.findKey ? instr.outSpec.class).asSymbol, 0, instr.outSpec, conn ) ];
        }, {
            outlets = [ ];
        });
        ~patch = patch;

        MxUnit(instr, inlets, outlets);
    },
    prepareToBundle: { arg agroup, bundle, private, bus;
        ~patch.prepareToBundle(agroup, bundle, private, bus);
        },
    spawnToBundle: { arg bundle;
        ~patch.spawnToBundle(bundle);
        ~up = Updater(~patch.instr, {
            // have to check if the args or specs changed
            ~respawn.value
        }.inEnvir);
        },
    stopToBundle: { arg bundle;
        ~patch.stopToBundle(bundle);
        ~up.remove
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
        var group, bus, patch;
        patch = ~patch;
        group = patch.group;
        bus = patch.bus;
        patch.stop;
        {
            patch.play(group, 1, bus)
        }.defer(0.2)
    },

    isPlaying: {
        ~patch.isPlaying
    },
    save: {
        var inps;
        inps = ~patch.inputs.collect { arg inp;
            if(inp.isKindOf(MxKrJack) and: {inp.isKindOf(MxArJack).not}, {
                inp.value
            }, {
                inp
            })
        };
        [~patch.instr.dotNotation, inps ]
    },
    load: { arg nameinp;
        var inps, instr;
        inps = nameinp[1];
        instr = Instr(nameinp[0]);
        inps = instr.specs.collect({ arg spec, i; MxJack.forSpec(spec, inps[i]) });

        [instr, inps]
    },
    name: { ~patch.instr.dotNotation },

    gui: { arg layout;
        var l;
        l = layout ?? {PageLayout(~patch.instr.dotNotation, Rect(0, 0, 500, 600))};
        ActionButton(l, "respawn", {
            ~respawn.value
        }.inEnvir);
        ActionButton(l, "rand", {
            ~patch.rand
        }.inEnvir);
        l.startRow;
        ~patch.gui(l);
        InstrGui(~patch.instr).sourceGui(l, 500);
        if(layout.isNil, {
            l.resizeToFit
        })
    }
));
