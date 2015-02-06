

MxUnit.register('InstrGateSpawner', (

    make: { arg igs;

        var instr, inlets, outlets, inps, adapters, conn, deltaSpec;
        var sbk;

        deltaSpec = StreamSpec(StaticSpec(0.0125, 64, default: 1));
        ~igs = igs;
        instr = igs.instr;
        inps = instr.specs.collect({ arg spec, i;
                    var inp, sp;
                    if(spec.class === ControlSpec, {
                        sp = StreamSpec(spec)
                    }, {
                        sp = spec;
                    });
                    inp = igs.args.at(i);
                    if(inp.isKindOf(NumberEditor), {
                        MxJack.forSpec(sp, inp.value )
                    }, {
                        inp
                    })
                });
        igs.args = inps;
        if(igs.deltaPattern.isKindOf(MxJack).not, {
            igs.deltaPattern = MxStreamJack( igs.deltaPattern, deltaSpec );
        });

        sbk = SwitchByKind(
                MxArJack, { arg inp; MxHasJack({inp}, {igs.server}, {igs.group}) },
                MxControlJack, { arg inp; MxHasKrJack({inp}, {igs.server}, {igs.group}) },
                MxStreamJack, { arg inp; MxHasStreamJack({inp}, {igs.server}, {igs.group}) },
                Object, { arg inp, i;
                        // many things it could be
                        // NumberEditor : wont modulate but can set on spawn
                        // but MxIrJack won't modulate, set on spawn
                        ("No adapter found for" + inp + instr.specs.at(i) + instr.argNameAt(i) + "in" + instr).warn;
                        nil
                });

        adapters = igs.inputs.collect({ arg inp, i; sbk.value(inp, i) });

        inlets = instr.specs
            .collect({ arg spec, i; MxInlet(instr.argNameAt(i), i, spec, adapters[i] ) });

        inlets = inlets.add( MxInlet("delta", instr.specs.size, deltaSpec) );

        conn = if(instr.outSpec.rate  === 'control', {MxPlaysOnKrBus}, {MxPlaysOnBus}).new({igs.bus}, {igs.server}, {igs.group});

        if(instr.outSpec.notNil, {
            outlets = [MxOutlet( (instr.outSpec.findKey ? instr.outSpec.class).asSymbol, 0, instr.outSpec, conn ) ];
        }, {
            outlets = [ ];
        });

        MxUnit(igs, inlets, outlets);
    },
    prepareToBundle: { arg agroup, bundle, private, bus;
        ~igs.prepareToBundle(agroup, bundle, private, bus);
        },
    spawnToBundle: { arg bundle;
        ~igs.spawnToBundle(bundle)
        },
    stopToBundle: { arg bundle;
        ~igs.stopToBundle(bundle)
        },
    freeToBundle: { arg bundle;
        ~igs.freeToBundle(bundle)
        },
    moveToHead: { arg aGroup, bundle, currentGroup;
        bundle.add( ~igs.spawnGroup.moveToHeadMsg(aGroup) );
    },

    numChannels: {
        ~igs.numChannels
        },
    spec: {
        ~igs.spec
    },
    play: { arg group, atTime, bus;
        ~igs.play(group, atTime, bus)
        },
    stop: { arg atTime, andFreeResources = true;
        ~igs.stop(atTime, andFreeResources)
        },
    respawn: {
        var group, bus;
        group = ~igs.group;
        bus = ~igs.bus;
        ~igs.stop;
        ~igs.play(group, 1, bus)
    },

    isPlaying: {
        ~igs.isPlaying
    },
    save: {
        [~igs]
    },
    load: { arg args;
        args.first
    },
    name: { ~igs.instr.dotNotation },

    gui: { arg layout;
        var l;
        l = layout ?? {PageLayout(~igs.instr.dotNotation, Rect(0, 0, 500, 600))};
        ~igs.gui(l);
        if(layout.isNil, {
            l.resizeToFit
        })
    }
));
