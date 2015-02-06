

MxDrawer.add("Slider", { NumberEditor(1.0) });

MxDrawer.add("CC", { CCResponder.new });

MxDrawer.add("MxControlRecorder", { MxControlRecorder.new });

if(\BezierSpline.asClass.notNil, {
    MxDrawer.add("SplineKr", { KrSplinePlayer.new });

    MxDrawer.add("SplineFr", { SplineFr.new });

    MxDrawer.add("MultiSplineFr", {
        var b;
        b = \BezierSpline.asClass.fromPoints(
                            [[0, 0, 0, 0, 0],
                            [30, 0, 0, 0, 0],
                             [60, 0, 0, 0, 0]]);
        MultiSplineFr(b, spec:\unipolar.asSpec);
    });
});

MxDrawer.addGroup("Instr", {
    Instr.leaves.collect { arg instr; [instr.dotNotation , instr] }
}, { arg instr, instrname;
    instr
});

MxDrawer.addGroup("InstrGateSpawner", {
    Instr.leaves.collect { arg instr; [instr.dotNotation , instr] }
}, { arg instr, instrname;
    InstrGateSpawner(instr)
});

MxDrawer.add("Patterns from Instr...", {
    var defAction;
    defAction = MxDeferredDrawerAction.new;
    InstrBrowser({ arg layout, instr;
        var min, max;
        var p, graph, graphIt;

        p = Patch(instr);
        min = NumberEditor(0, [-1000000, 1000000]);
        max = NumberEditor(0, [-1000000, 1000000]);

        graphIt = {
            var vals;
            try {
                vals = p.value.asStream.nextN(200);
                min.value_(vals.minItem).changed;
                max.value_(vals.maxItem).changed;
            } { arg e;
                "Patch failed to build".inform;
                e.postln;
                e.throw;
            };
            if(vals.notNil, {
                graph.value = vals;
                graph.refresh;
            });
        };
        p.args.do { arg inp, i;
            layout.startRow;
            ArgNameLabel(instr.argNames.at(i), layout);
            inp.gui(layout);
        };
        layout.startRow;
        ActionButton(layout, "RND", {
            p.rand;
            graphIt.value();
        });
        ActionButton(layout, "Graph it", {
            graphIt.value();
        });
        ActionButton(layout, "INSERT TO MX", {
            var pattern, spec, warp=\lin, step=0.0, units;
            pattern = p.value;
            try {
                warp = instr.outSpec.itemSpec.warp;
                step = instr.outSpec.itemSpec.step;
                units = instr.outSpec.itemSpec.units;
            };
            spec = StreamSpec( ScalarSpec(min.value, max.value, warp:warp, step:step, units:units) );
            defAction.value(MxUnit.make([pattern, spec], Pattern))
        });
        layout.startRow;
        layout.comp({ arg layout;
            graph = Plotter("graph", Rect(0, 0, 700, 200).insetBy(2, 2), layout);
        }, Rect(0, 0, 700, 200));
        graph.plotMode = \levels; // \steps
        graph.setProperties(\plotColor, Color.blue);

        CXLabel(layout.startRow, "Spec min:", minWidth:100);
        min.smallGui(layout);
        CXLabel(layout.startRow, "Spec max:", minWidth:100);
        max.smallGui(layout);

        layout.startRow;

        graphIt.value();

    }).rate_('stream').init.gui;
    defAction
});




[    [ "~/SoundFiles", "~/SoundFiles/*"], //personal
    [ "Recordings",   thisProcess.platform.recordingsDir +/+ "*"]
].do { arg nameSearchPath;

    MxDrawer.addGroup(nameSearchPath[0], {
        var all, formats, search;
        all = List.new;
        formats = [\aif, \wav, \WAV, \AIF, \AIFF, \aiff];
        search = { arg searchPath;
            searchPath.pathMatch.do { arg path;
                var pn = PathName(path);
                if(formats.includes( (pn.extension ? \none).asSymbol ), {
                    all.add([pn.fileNameWithoutExtension, path])
                }, {
                    if(pn.isFolder, {
                        search.value(pn +/+ "*")
                    })
                })
            }
        };
        search.value(nameSearchPath[1]);
        all
    }, { arg path, filename;
        SFP(path)
    });
};

MxDrawer.add("Sample", {
    Sample.new
});


MxDrawer.add("New document", {

    MxDocument.new

});
