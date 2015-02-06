
/*

inlets
    audio input
    mute
    solo
    level
    fade to, time

*/

MxUnit.register('MxChannel',
(
    make: { arg chan;
        var inlets, outlets, patch, inps, connectors, conn, handlers;

        inlets  = [
            MxInlet("MxChanIn", 0, AudioSpec(chan.numChannels),
                 MxListensToBus({chan.source.bus}, {chan.server}, {chan.mixGroup}))/*,
            MxInlet("db", 1, ControlSpec(-inf, 6.0, 'db', 0.0, -inf, " dB"),
                MxSetter({ arg v; chan.fader.db = v }) ) */
         ];

        outlets = [ MxOutlet("MxChanOut", 0, AudioSpec(chan.numChannels), MxPlaysOnBus({chan.source.bus}) ) ];

        MxUnit(chan, inlets, outlets);
    },
    draw: { arg pen, bounds, style;
        ~source.fader.draw(pen, bounds, style)
    }
));
