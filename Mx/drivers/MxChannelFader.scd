
/*

inlets
    db
    mute
    solo

outlets
    you don't patch from it directly,
    you do so from the mx channel
*/

MxUnit.register('MxChannelFader',
(
    make: { arg chan;
        var inlets, outlets;

        inlets  = [
            ];

        outlets = [
            ];

        MxUnit(chan, inlets, outlets);
    },
    draw: { arg pen, bounds, style;
        ~source.draw(pen, bounds, style)
    }
));
