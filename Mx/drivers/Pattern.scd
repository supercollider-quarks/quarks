

MxUnit.register('Pattern',
(
    make: { arg pattspec;
        var pattern, spec;
        var inlets, outlets, sips;
        if(pattspec.isArray, {
            # pattern, spec = pattspec;
        }, {
            pattern = pattspec;
            spec = \unipolar.asSpec; // better to run a few items and check min max
        });

        ~source = pattern;
        ~spec = spec;
        inlets  = [];
        outlets = [ MxOutlet( Spec.findKeyForSpec(spec) ? "pattern", 0, spec ,
                            MxIsStream({pattern}) )
                ];
        MxUnit(~source, inlets, outlets);
    },
    spec: {
        ~spec
    },
    prepareToBundle:  { arg agroup, bundle, private, bus;
    },
    spawnToBundle: { arg bundle;
    },
    stopToBundle: { arg bundle;
    },

    save: {
        [~source, ~spec]
    },
    load: { arg data;
        data
    },
    gui: { arg layout, bounds;
        var f, height, tf, source, width=600;
        var l;
        l = layout ?? {PageLayout(~name.value, Rect(0, 0, 500, 600))};

        // set spec here, must recable it then
        ~spec.gui(l);

        l.startRow;
        source = ~source.asCompileString;
        f = GUI.font.new("Courier", 12.0);
        height = source.bounds(f).height + 20;
        tf = TextView(l, Rect(0, 0, width, height));
        tf.string = source;
        tf.font_(f);
        tf.syntaxColorize;

        if(layout.isNil, {
            l.resizeToFit
        });

    }
));
