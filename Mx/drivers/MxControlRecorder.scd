

MxUnit.register('MxControlRecorder',
(
    make: { arg mcr;
        ~source = mcr;
        MxUnit(mcr, [], [])
    },
    didLoad: {
        ~source.didLoad()
    },
    gui: { arg layout;
        var l;
        l = layout ?? {PageLayout("Recorder", Rect(100, 100, 800, 600))};
        ~source.gui(l);
        if(layout.isNil, {
            l.resizeToFit
        })
    }
));
