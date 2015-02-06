

MxChannelInput : AbstractPlayerProxy {
    /*
        used to listen to a bus that one or more units are playing onto

        channel input was playing on same bus it listens in on
        thus adding to it.
        really no point in this thing, eh ?
        well it creates on bus, doesn't even need a synth
        its just a unit that others can connect to
        and since they play on its bus, then it doesn't need a synth just to echo no-op

        currently making an extra bus and copying it
    */

    var instr, busJack, <>numChannels=2, inBus;

    *new { ^super.new.init }
    init {
        var instr;
        instr = MxChannelInput.instr;
        busJack = MxIrJack(126).spec_(instr.specs.at(1));
        source = Patch(instr, [ this.numChannels, busJack ])
    }
    prepareToBundle { arg agroup, bundle, private = false, argbus;
        super.prepareToBundle(agroup, bundle, private , argbus);
        inBus = this.patchOut.allocBus("inBus", \audio, this.numChannels);
        busJack.value = inBus.index;
    }

    *instr {
        if(Instr.isDefined("MxChannelInput").not, {
            Instr("MxChannelInput", { arg numChannels=2, bus;
                In.ar(bus, numChannels)
            }, [
                StaticIntegerSpec(1, 127, 'linear', 1, 0, "Num Channels"),
                ScalarSpec(0, 1028, 'linear', 1, 0, "Audio Bus")
            ], AudioSpec.new);
        });
        ^Instr.at("MxChannelInput")
    }
}


MxChannelFader : AbstractPlayerProxy {

    var <numChannels, <db=0, <mute=false, <solo=false;
    var <>limit=nil, <>breakOnBadValues=true, <>breakOnDbOver=12, <>fuseBlown=false;

    var busJack, dbJack;

    *new { arg db=0.0, mute=false, solo=false,
            limit=nil, breakOnBadValues=true, breakOnDbOver=12.0, numChannels=2;
        ^super.new.init(db, mute, solo, limit, breakOnBadValues, breakOnDbOver, numChannels)
    }
    storeArgs {
        ^[db, mute, solo, limit, breakOnBadValues, breakOnDbOver, numChannels]
    }

    init { arg argdb, argmute, argsolo,
            arglimit, argbreakOnBadValues, argbreakOnDbOver, argnumChannels;
        db = argdb;
        mute = argmute;
        solo = argsolo;
        limit = arglimit;
        breakOnBadValues = argbreakOnBadValues;
        breakOnDbOver = argbreakOnDbOver;
        numChannels = argnumChannels ? 2;

        busJack = MxKrJack(126).spec_(ControlSpec(0, 127, 'linear', 1, 0, "Audio Bus"));
        dbJack = MxKrJack(db).spec_(ControlSpec(-1000, 24, 'db', 0.0, 0.0, \db));

         source = Patch(MxChannelFader.channelInstr, [
                    numChannels,
                    busJack,
                    dbJack,
                    limit ? 0,
                    breakOnBadValues.binaryValue,
                    breakOnDbOver,
                    { arg val; val.inform; fuseBlown = true; }
                ], ReplaceOut);
    }
    prepareToBundle { arg agroup, bundle, private = false, argbus;
        fuseBlown = false;
        super.prepareToBundle(agroup, bundle, private , argbus);
        busJack.value = this.bus.index;
    }
    db_ { arg d;
        db = d;
        dbJack.value = db;
        dbJack.changed;
    }
    mute_ { arg boo;
        mute = boo;
        solo = false;
        if(mute, {
            dbJack.value = -300.0;
            dbJack.changed;
        }, {
            dbJack.value = db;
            dbJack.changed;
        })
    }
    setSolo {
        solo = true;
        mute = false;
        dbJack.value = db;
        dbJack.changed;
    }
    unsetSolo {
        solo = false;
        mute = false;
        dbJack.value = db;
        dbJack.changed;
    }
    muteForSoloist {
        solo = false;
        mute = true;
        dbJack.value = -300;
        dbJack.changed;
    }

    draw { arg pen, bounds, style;
        pen.color = style['fontColor'];
        pen.font = style['font'];
        if(mute, {
            ^pen.stringCenteredIn("muted", bounds);
        });
        pen.stringCenteredIn(db.round(0.1).asString ++ "dB", bounds);
    }

    *channelInstr {
        // or create it on the fly so the on trigs can be sent
        // could also pass a responder function in
        if(Instr.isDefined("MxChannelFader").not, {
            Instr("MxChannelFader", { arg numChannels=2, inBus=126,
                            db=0, limit=0.999, breakOnBadValues=1, breakOnDbOver=12, onBad;
                var ok, threshold, c, k, in;
                in = In.ar(inBus, numChannels);
                if(breakOnBadValues > 0, {
                    ok = BinaryOpUGen('==', CheckBadValues.kr(Mono(in), 0, 2), 0);
                    (1.0 - ok).onTrig({
                        "bad value, muting".inform;
                        onBad.value("bad value");
                    });
                    in = in * ok;
                });
                if(breakOnDbOver > 0, {
                    threshold = breakOnDbOver.dbamp;
                    c = max(0.0, (Amplitude.ar(Mono(in), 0.001, 0.001) - 2.0));
                    k = c > threshold;
                    A2K.kr(k).onTrig({
                        "amp > threshold, muting".inform;
                        onBad.value("over threshold");
                    });
                    k = 1.0 - k;
                    in = in * k; //Lag.kr(k, 0.01);
                });
                if(limit > 0, {
                    Limiter.ar(
                        ( in ) * db.dbamp,
                        limit
                    ) // .clip2(1.0, -1.0)
                }, {
                    in = in * db.dbamp
                });
                NumChannels.ar(in, numChannels)
            }, [
                StaticIntegerSpec(1, 127, 'linear', 1, 0, "Num Channels"),
                ControlSpec(0, 127, 'linear', 1, 0, "Audio Bus"),
                ControlSpec(-1000, 24, 'db', 0.0, 0.0, \db),
                StaticSpec(0, 1.0),
                StaticSpec(0, 1),
                StaticSpec(0, 100),
                ObjectSpec.new
            ], AudioSpec(2))
        });
        ^Instr("MxChannelFader")
    }
}
