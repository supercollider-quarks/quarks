

AbsApp {

    var <model, mxapp;

    *new { arg model, mxapp;
        ^super.newCopyArgs(model, mxapp).prInit
    }
    prInit {}
    mx { ^mxapp.model }
    printOn { arg stream;
        stream << model
    }
    source { ^model }
    dereference { ^this.source }
    checkThat { arg that;
        if(that.isKindOf(AbsApp).not, { (this.asString + "cannot >> to" + that).error });
    }
}


MxApp : AbsApp {

    var cache, convertor, transactionCount = 0;

    mx { ^model }
    at { arg point;
        var u;
        u = model.at(point.x, point.y) ?? { ^nil };
        ^this.prFind( u )
    }
    put { arg point, object;
        ^this.prFind(model.put(point.x, point.y, object))
    }
    atID { arg id;
      var obj = model.atID(id);
      if(obj.isNil, {
          Error("Mx unit not found:" + id).throw;
      });
      ^this.prFind(obj)
    }
    units {
        ^MxQuery(model.allUnits(false).all.collect(this.prFind(_)), this)
    }
    outlets { ^this.units.outlets }
    inlets { ^this.units.inlets }
    channels {
        ^MxQuery(model.channels.collect(this.prFind(_)), this)
    }
    channel { arg i;
        var c;
        c = model.channelAt(i) ?? {
                c = model.insertChannel(i);
                model.update;
                c
            };
        ^this.prFind( c )
    }
    master {
        ^this.prFind( model.master )
    }

    newChan {
        ^this.channel( model.channels.size )
    }
    add { arg ... sources;
        // if one object supplied then adds it to a new channel and returns a single unit
        // if many then returns a channel filled with object(s)
        var chan;
        chan = this.prFind( model.add(*sources) );
        this.commit;
        if(sources.size == 1, {
            ^chan.units.first
        }, {
            ^chan
        })
    }

    play { arg then, atTime;
        if(model.isPlaying.not, {
            if(then.notNil, { model.onPlay(then) });
            model.play(atTime: atTime);
        }, then)
    }
    stop { arg then, atTime;
        if(model.isPlaying, {
            if(then.notNil, { model.onFree(then) });
            model.free(atTime: atTime);
        }, then)
    }

    beat { ^model.beat }
    relocate { arg beat, q=4;
        model.gotoBeat(beat, q);
    }

    save {
        model.save
    }
    gui { arg parent, bounds;
        // detect and front
        //var open;
        //open = model.dependants.detect(_.isKindOf(MxGui));
        //if(open.notNil and: { open.isClosed.not }, {
        //    open.front
        //}, {
            model.gui(parent, bounds);
        //})
    }

    //select

    transaction { arg function;
        var result;
        transactionCount = transactionCount + 1;
        result = function.value;
        transactionCount = max(transactionCount - 1, 0);
        this.commit;
        ^result
    }
    commit {
        if(transactionCount == 0, {
            this.mx.update;
            this.mx.changed('grid')
        })
    }

    //copy to an app buffer
    //paste
    prInit {
        cache = IdentityDictionary.new;
    }
    prFind { arg obj;
        // gets or wraps the object in an app class wrapper
        var app;
        ^cache[obj] ?? {
            app = (obj.class.name.asString ++ "App").asSymbol.asClass.new(obj, this);
            cache[obj] = app;
            app
        }
    }
    printOn { arg stream;
        if(model.name.notNil, {
            stream << model
        }, {
            stream << "an MxApp"
        })
    }
}


MxChannelApp : AbsApp {

    at { arg i;
        var unit;
        unit = model.at(i) ?? {
            ^nil
            // slot is still nil
            //model.extendUnits(i);
            //model.at(i)
        };
        ^mxapp.prFind( unit )
    }
    put { arg i, source;
        this.mx.put( this.channelNumber, i, source );
        mxapp.commit;
        ^this.at(i)
    }
    removeAt { arg i;
        this.mx.remove( this.channelNumber, i );
        mxapp.commit;
    }
    insertAt { arg i, source;
        // source.asArray.do
        this.mx.insert( this.channelNumber, i, source );
        mxapp.commit;
        ^this.at(i)
    }

    units {
        ^MxQuery(model.units.select(_.notNil).collect({ arg u; mxapp.prFind(u) }), mxapp)
    }
    fader {
        // the audio inlet to the fader
        ^mxapp.prFind(model.myUnit.inlets.first)
    }
    //next
    //prev

    //select
    add { arg ... sources; // add 1 or more to the end
        var start, apps, ci;
        start = model.units.size;
        ci = this.channelNumber;
        apps = sources.collect { arg source, i;
            var unit;
            if(source.notNil, {
                unit = this.mx.put( ci, start + i, source );
                mxapp.prFind(unit)
            }, {
                nil
            })
        };
        mxapp.commit;
        if(apps.size == 1, {
            ^apps.first
        }, {
            ^apps
        })
    }
    dup { arg fromIndex, toIndex;
        // toIndex defaults to the next slot, pushing any others further down
        var unit, ci;
        ci = this.channelNumber;
        unit = this.mx.copy( ci, fromIndex, ci,     toIndex ?? {fromIndex + 1} );
        mxapp.commit;
        if(unit.isNil, { ^nil });
        ^mxapp.prFind( unit )
    }
    mute { arg boo=true;
        this.mx.mute(this.channelNumber, boo);
        this.mx.changed('mixer');
    }
    muted {
        ^model.fader.mute
    }
    unmute {
        this.mx.mute(this.channelNumber, false);
        this.mx.changed('mixer');
    }
    toggle {
        this.mx.mute(this.channelNumber, model.fader.mute.not);
        this.mx.changed('mixer');
    }
    solo { arg boo=true;
        this.mx.solo(this.channelNumber, boo);
        this.mx.changed('mixer');
    }
    unsolo {
        this.mx.solo(this.channelNumber, false);
        this.mx.changed('mixer');
    }
    soloed {
        ^model.fader.solo
    }

    db {
        ^model.fader.db
    }
    db_ { arg db;
        model.fader.db = db;
        this.mx.changed('mixer');
    }
    //fade { arg db, seconds=5; // easing
        // will need a little engine in the frame rate engine
    //}

    channelNumber {
        ^this.mx.indexOfChannel(model)
    }
    printOn { arg stream;
        stream << "Channel" << this.channelNumber
    }
}


MxUnitApp : AbsApp {

    name {
        ^model.name
    }
    source {
        ^model.source
    }
    use { arg function;
        ^model.use(function)
    }
    stop {
        model.stop;
        // unit should send state change notifications
        this.mx.changed('grid');
    }
    play {
        model.play;
        this.mx.changed('grid');
    }
    respawn {
        model.respawn
    }
    isPlaying {
        ^model.isPlaying
    }
    spec {
        ^model.spec
    }
    beatDuration {
        model.beatDuration
    }
    gui { arg parent, bounds;
        ^model.gui(parent, bounds)
    }

    remove {
        this.mx.remove(*this.point.asArray);
        mxapp.commit;
        // should mark self as dead
    }
    moveTo { arg point;
        var me;
        me = this.point;
        this.mx.move(me.x, me.y, point.x, point.y);
        mxapp.commit;
    }
    moveBy { arg vector;
        var me, dort;
        me = this.point;
        dort = me + vector;
        this.mx.move(me.x, me.y, dort.x, dort.y);
        mxapp.commit;
    }
    dup { arg num=1;
        // insert copies below self
        var p, below, bp, cop;
        p = this.point;
        ^mxapp.transaction({
            var results;
            results = Array.fill(num, { arg i;
                bp = Point(p.x, p.y+(i+1));
                below = this.mx.at(bp.x, bp.y);
                    if(below.notNil, {
                        this.channel.insertAt(bp, nil);
                    });
                    this.copy(bp);
                });
            if(num == 1, {
                results.first
            }, {
                results
            })
        })
    }
    copy { arg toPoint;
        // toIndex defaults to the next slot, pushing any others further down
        var unit, ci, p;
        p = this.point;
        unit = this.mx.copy( p.x, p.y, toPoint.x, toPoint.y );
        mxapp.commit;
        ^mxapp.prFind( unit )
    }
    copyBy { arg vector;
        // copy relative to self
        ^this.copy(this.point + vector)
    }
    replaceWith { arg source;
        var p = this.point,
        insFrom = this.i.collect(_.from),
        outsTo = this.o.collect(_.to);

        ^mxapp.transaction({
            var new;
            new = mxapp.put(p, source);
            insFrom.do { arg outs, i;
                outs.do { arg out;
                    if(new.i.size > i, {
                        out >> new.i[i]
                    })
                }
            };
            outsTo.do { arg ins, i;
                ins.do { arg in;
                    if(new.o.size > i, {
                        new.o[i] >> in
                    })
                }
            };
            new
        })
    }
    // below
    // above
    // left
    // right
    disconnect {
        model.inlets.do { arg io;
            this.mx.disconnectInlet(io);
        };
        model.outlets.do { arg io;
            this.mx.disconnectOutlet(io);
        };
        mxapp.commit;
    }

    i { ^this.inlets }
    o { ^this.outlets }
    inlets {
        ^MxIOletsApp(model.inlets, mxapp, model)
    }
    outlets {
        ^MxIOletsApp(model.outlets, mxapp, model)
    }
    cables { ^this.i.cables ++ this.o.cables }
    out {
        ^this.outlets.out
    }
    channel {
        ^mxapp.prFind( this.mx.channelAt( this.point.x ) )
    }
    >> { arg that;
        this.checkThat(that);
        ^mxapp.transaction({
            this.outlets >> that
        })
    }
    addInlet { arg name, spec, adapter;
        model.addInlet(name, spec, adapter)
    }
    addOutlet { arg name, spec, adapter;
        model.addOutlet(name, spec, adapter)
    }
    point { ^this.mx.pointForUnit(model) }
    id {
      ^this.mx.findID(model)
    }
    printOn { arg stream;
        var p;
        p = this.point ? Point(nil, nil);
        stream << p.x << "@" << p.y << "(" << this.name << ")"
    }
}


MxIOletsApp : AbsApp {

    var <unit;

    *new { arg model, mxapp, unit, desc;
        ^super.newCopyArgs(model, mxapp, unit).prInit
    }

    at { arg key;
        ^this.prFindIOlet(key, true)
    }
    first {
        ^this.prFindIOlet(0, true)
    }
    size { ^model.size }
    out {
        // shortcut to the first output
        ^this.prFindIOlet('out') ?? {this.prFindIOlet(0, true)}
    }
    cables {
        var cables = [];
        model.do { arg io, i;
            cables.addAll(io.cables)
        };
        ^cables
    }
    >> { arg inlet;
        var outlet;
        this.checkThat(inlet);
        outlet = (this.out ?? { (this.asString ++ "has no out").error; ^this });
        ^outlet >> inlet
    }
    do { arg function;
        model.do(this.prRedirected(function))
    }
    collect { arg function;
        ^model.collect(this.prRedirected(function))
    }
    select { arg function;
        ^model.select(this.prRedirected(function))
    }
    prRedirected { arg function;
        // curry the function for collect/select while supplying the app objects
        ^{ arg io, i;
            function.value(mxapp.prFind(io), i)
        }
    }
    // finds iolet by name
    doesNotUnderstand { arg selector ... args;
        ^this.prFindIOlet(selector) ?? {
            (selector.asString + "is not an iolet.\nIOlets:" + model).error;
            this.superPerformList(\doesNotUnderstand, selector, args);
        }
    }
    disconnect {
        model.do { arg io;
            if(io.class === MxOutlet, {
                this.mx.disconnectOutlet(io);
            }, {
                this.mx.disconnectInlet(io);
            })
        };
        mxapp.commit;
    }
    prFindIOlet { arg i, warn=false;
        if(i.isNumber, {
            if(i >= model.size, {
                if(warn, {
                    ("IOlet index out of range:"+ i.asCompileString + this).warn;
                });
                ^nil
            }, {
                ^mxapp.prFind(model.at(i))
            })
        }, {
            i = i.asSymbol;
            model.do { arg inl;
                if(inl.name == i, {
                    ^mxapp.prFind(inl)
                })
            }
        });
        if(warn, {
            ("IOlet index not found:"+ i.asCompileString + "in:" + this).warn;
        });
        ^nil
    }
    printOn { arg stream;
        stream << mxapp.prFind(unit) << ":" << (", ".join(model.collect(_.name)))
    }
}


MxInletApp : AbsApp {

    << { arg outlet;
        //this.checkThat(outlet);
        this.mx.connect(outlet.model.unit, outlet.model, model.unit, model);
        mxapp.commit;
        ^outlet
    }
    disconnect {
        this.mx.disconnectInlet(model);
        mxapp.commit;
    }
    spec {
        ^model.spec
    }
    name {
        ^model.name
    }
    index {
        ^model.index
    }
    unit {
        ^mxapp.prFind(model.unit)
    }
    from { // outlets that connect to me
        ^this.mx.cables.toInlet(model).collect { arg cable;
            mxapp.prFind( cable.outlet )
        }
    }
    printOn { arg stream;
        stream << mxapp.prFind(model.unit) << "::";
        model.printOn(stream)
    }
    cables {
        ^this.mx.cables.toInlet(model).collect({ arg cable;
            mxapp.prFind(cable)
        })
    }

    canGet { ^model.canGet }
    canSet { ^model.canSet }
    set { arg v; model.set(v) }
    get { ^model.get }
}


MxOutletApp : AbsApp {

    >> { arg inlet;
        /*
        could connect to fader of channel,
        or take connect to channel as meaning connect to top of channel strip
        if(inlet.isKindOf(MxChannelApp), {
            inlet = inlet.fader
        }); */
        //this.checkThat(inlet);
        if(inlet.isKindOf(MxInletApp).not, {
            //[model, inlet, inlet.model].insp("connect these");
            // will support this later: connect to unit by finding first usable inlet
            Error("" + this + "cannot >> to" + inlet).throw;
        });
        if(inlet.isKindOf(MxQuery), {
            mxapp.transaction {
                inlet.do { arg in;
                    this >> in
                }
            }
            ^inlet // return query
        });
        this.mx.connect(model.unit, model, inlet.model.unit, inlet.model);
        mxapp.commit;
        ^inlet // or magically find the outlet of that unit; or return that unit
    }
    disconnect {
        this.mx.disconnectOutlet(model);
        mxapp.commit;
    }

    spec {
        ^model.spec
    }
    name {
        ^model.name
    }
    index {
        ^model.index
    }
    unit {
        ^mxapp.prFind(model.unit)
    }
    to { // inlets that I connect to
        ^this.mx.cables.fromOutlet(model).collect { arg cable;
            mxapp.prFind( cable.inlet )
        }
    }
    cables {
        ^this.mx.cables.fromOutlet(model).collect({ arg cable;
            mxapp.prFind(cable)
        })
    }

    canGet { ^model.canGet }
    canSet { ^model.canSet }
    set { arg v; model.set(v) }
    get { ^model.get }

    printOn { arg stream;
        if(model.unit.isNil, {
            stream << "Nil unit::";
        }, {
            stream << mxapp.prFind(model.unit) << "::";
        });
        model.printOn(stream)
    }
}


MxCableApp : AbsApp {

    // insert
    // outlet
    // inlet
    // fromUnit
    // toUnit
    // remove

}
