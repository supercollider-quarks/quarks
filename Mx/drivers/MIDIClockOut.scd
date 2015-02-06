
// crucial-library - sends midi clock and song position pointer
// x.add( MIDIClockOut("IAC Driver", "IAC Bus 1") )

MxUnit.register('MIDIClockOut',
(
    make: { arg mco;
        ~source = mco;
        MxUnit(mco, [], [])
    },
    spawnToBundle: { arg b;
        b.addFunction({~source.start}.inEnvir)
    },
    stopToBundle: { arg b;
        b.addFunction({~source.stop}.inEnvir)
    }
    /*,
    gotoBeat: { arg beat, atBeat, bundle;
        [beat, atBeat, bundle].debug;
        ~source.gotoBeatAtBeat(beat, atBeat)
    }*/

));
