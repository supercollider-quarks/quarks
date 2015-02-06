

MxUnit.register('MultiSplineFr',
(
    make: { arg sfr;
        var inlets, outlets, rec;
        ~source = sfr;
        ~recording = false;
        rec = MxSetter({ arg v;
            if(~recording, { v !? {sfr.setValueOnNextTick(v)} })
        }.inEnvir);
        // needs PointSpec or ArraySpec
        inlets  = [ MxInlet("setValueNow", 0, sfr.spec, rec) ];
        outlets = Array.fill(~source.spline.numDimensions-1,
                 { arg di;
                     var lastVal, default=sfr.spec.default;
                    MxOutlet("DIM" ++ (di+1), di, sfr.spec,
                        MxIsFrameRateDevice({ arg val;
                                val[di+1]
                            },
                            nil,
                            nil,
                            {    var beat; // get
                                beat = ~mx.beat;
                                lastVal = sfr.value(beat).at(di+1);
                                lastVal
                            }.inEnvir,
                            nil
                            ))
                 });
        MxUnit(sfr, inlets, outlets);
    },
    frameRateDevice: {
        ~source
    },
    play: { arg group, atTime, bus;
    },
    stop: { arg atTime, andFreeResources = true;
    },
    save: {
        ~source.storeArgs
    },
    load: { arg data;
        MultiSplineFr(*data)
    },
    spec: {
        ~source.spec
    },
    gui: { arg layout;
        ~source.gui(layout, 900@700)
    },
    timeGui: { arg layout, bounds, maxTime;
        ~splineGui = MxMultiSplineTimeGui2(~source).gui(layout, bounds, nil, [0, maxTime].asSpec);
    },
    zoomTime: { arg from, to;
        ~splineGui.setZoom(from, to);
    },
    setMaxTime: { arg maxTime;
        ~splineGui.setMaxTime( maxTime );
    },
    beatDuration: {
        ~source.spline.points.maxValue(_.at(0))
    },
    record: { arg boo, atTime;
        ~recording = boo;
        if(boo.not, {
            ~splineGui.update
        })
    }
));
