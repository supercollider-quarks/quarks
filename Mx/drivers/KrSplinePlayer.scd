

MxUnit.register('KrSplinePlayer',
(
    make: { arg player;
        var inlets, outlets;
        inlets  = [];
        outlets = [ MxOutlet("kr", 0, player.spec , MxPlaysOnKrBus({player.bus}) ) ];
        MxUnit(player, inlets, outlets);
    },

    play: { arg group, atTime, bus;
        ~source.play(group, atTime, bus)
    },
    stop: { arg atTime, andFreeResources = true;
        ~source.stop(atTime, andFreeResources)
    },
    save: {
        ~source.storeArgs
    },
    load: { arg data;
        KrSplinePlayer(*data)
    },
    beatDuration: {
        ~source.beatDuration
    },
    gui: { arg layout;
        ~source.gen.spline.gui(layout)
    }
));
