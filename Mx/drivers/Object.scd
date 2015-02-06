
MxUnit.register('Object',
(
    make: { arg object; ~source = object; MxUnit(object) },
    save: { ~source.asCompileString },
    load: { arg string; string.compile.value() },
    copy: { ~load.value(~save.value()) },

    // methods that the driver can implement, called after make
    didLoad: {},

    prepareToBundle:  { arg agroup, bundle, private, bus; ~source.prepareToBundle(agroup, bundle, private, bus) },
    spawnToBundle: { arg bundle; ~source.spawnToBundle(bundle) },
    stopToBundle: { arg bundle; ~source.stopToBundle(bundle) },
    freeToBundle: { arg bundle; ~source.freeToBundle(bundle) },
    moveToHead: { arg aGroup, bundle, currentGroup;
        // default is to stop it and fully restart it
        // objects that can move themselves can implement this cleaner
        ~stopToBundle.value(bundle);
        ~prepareToBundle.value(aGroup, bundle);
        ~spawnToBundle.value(bundle);
    },
    play: { arg group, atTime, bus;},
    stop: { arg atTime, andFreeResources = true; },
    respawn: { arg atTime; },
    isPlaying: { ~source.isPlaying },

    numChannels: { ~source.numChannels ? 2 },
    spec: { ~source.spec ?? {'audio'.asSpec} },
    beatDuration: { nil }, // meaning unknownable or unending
    gui: { arg layout, bounds;
        ~source.gui(layout ?? {Window(~name.value, bounds).front}, bounds)
    },
    draw: { arg pen, bounds, style;
        pen.color = style['fontColor'];
        pen.font = style['font'];
        if(style['center'], {
            pen.stringCenteredIn(~name.value, bounds)
        }, {
            pen.stringLeftJustIn(~name.value, bounds.insetBy(2, 2) )
        });
    },
    name: { ~source.asString }
    // crop
    // relocate: { arg toBeat, atTime; }
    // timeGui
    // zoomTimeGui
    // asCompileString
    //
));
