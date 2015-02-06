

MxUnit.register('SFP', (

    make: { arg sfp;

        var inlets, outlets;

        inlets = [];

        outlets = [ MxOutlet( "out", 0, AudioSpec(sfp.numChannels), MxPlaysOnBus({sfp.bus}) ) ];

        ~source = sfp;

        MxUnit(sfp, inlets, outlets);
    },
    // should be able to inherit
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
        [~source.soundFilePath]
    },
    load: { arg path;
        SFP(*path)
    },
    name: { PathName(~source.soundFilePath ? "None").fileNameWithoutExtension },
    beatDuration: { ~source.beatDuration },

    gui: { arg layout;
        var l;
        l = layout ?? {PageLayout(~name.value, Rect(0, 0, 500, 600))};
        ~source.gui(l);
        if(layout.isNil, {
            l.resizeToFit
        })
    },
    timeGui: { arg layout, bounds, maxTime;
        ~fbeats = Tempo.secs2beats( ~source.timeDuration );
        ~fratio = ~fbeats / maxTime;// if > 1 maxTime should have been higher
        ~maxTime = maxTime;

        ~view = SoundFileView(layout, Rect(0, 0, bounds.width, 60));
        ~view.soundfile = ~source.file;
        ~view.gridOn = false;
        ~view.timeCursorOn = false;
        ~view.readWithTask(0, min(~source.numFrames, Tempo.beats2secs(maxTime) * ~source.sampleRate), 64, {
            // zoom it
        }.inEnvir, true);
    },
    zoomTime: { arg from, to;
        var ratio;
        ratio = (to-from) / ~maxTime;
        ~view.zoomToFrac( ratio );
        // where is from in ratio of soundfile
        ~view.scrollTo( from / ~fbeats );
    }
));

