

MxQuery : AbsApp {

    /* further refining the search result. returns a new query
        functions are supplied:   source, i, app
    */
    select { arg function;
        ^MxQuery(this.objects.select({ arg obj, i; function.value(obj.source, i, obj) }), mxapp)
    }
    reject { arg function;
        ^MxQuery(this.objects.reject({ arg obj, i; function.value(obj.source, i, obj) }), mxapp)
    }
    //distinct

    units {
        var all, h;
        all = IdentitySet.new;
        h = (
                MxChannelApp: { arg app; all.addAll(app.units.objects) },
                MxOutletApp: { arg app; all.add(app.unit) },
                MxInletApp: { arg app; all.add(app.unit) },
                MxIOletsApp: { arg app; all.add(app.unit) },
                MxUnitApp: { arg app; all.add(app) }
            );

        this.do { arg obj, i, app;
            h[app.class.name].value(app)
        };
        ^MxQuery(all.as(Array), mxapp)
    }
    channels {
        var chans;
        chans = this.units.collect({arg obj, i, app; app.channel }).as(IdentitySet);
        ^MxQuery(chans, mxapp)
    }
    inlets {
        var all;
        all = List.new;
        this.units.do { arg obj, i, app;
            app.inlets.do { arg ioapp;
                all.add(ioapp)
            }
        };
        ^MxQuery(all, mxapp)
    }
    outlets {
        var all;
        all = List.new;
        this.units.do { arg obj, i, app;
            app.outlets.do { arg ioapp;
                all.add(ioapp)
            }
        };
        ^MxQuery(all, mxapp)
    }

    where { arg selector, equalsValue;
        ^this.select({ arg obj, i, app; obj.perform(selector) == equalsValue })
    }
    whereIsA { arg class;
        ^MxQuery(this.select({ arg obj, i, app; obj.class === class }), mxapp)
    }
    whereIsKindOf { arg class;
        ^MxQuery(this.select({ arg obj, i, app; obj.isKindOf(class) }), mxapp)
    }
    whereAppClassIs { arg class;
        ^MxQuery(this.select({ arg obj, i, app; app.class === class }), mxapp)
    }

    /*  iteration */
    do { arg function;
        // iterates giving the source object. ie. the Instr/Env/thing not the MxUnitApp
        // inlets and channels are supplied as themselves
        this.objects.do({ arg obj, i; function.value(obj.source, i, obj) })
    }
    collect { arg function;
        ^this.objects.collect({ arg obj, i; function.value(obj.source, i, obj) })
    }

    /* collection support */
    size {
        ^this.objects.size
    }
    isEmpty {
        ^this.objects.isEmpty
    }
    notEmpty {
        ^this.objects.notEmpty
    }
    at { arg i;
        ^this.objects.at(i)
    }
    first {
        ^this.at(0)
    }
    asArray {
        ^Array.fill(this.size, { arg i; this.at(i) })
    }
    // includes
    // every
    // any



    /*  perform actions on all items in the query result */
    >> { arg that;
        // unless that is another query
        this.prAllDo({ arg app; app >> that })
    }
    disconnect {
        this.prAllDo({ arg app; app.tryPerform(\disconnect) })
    }
    remove { // remove from the mx, not from the collection !
        // units and channels remove themselves
        // others ignore it
        this.prAllDo({ arg app; app.remove })
    }
    get { arg selector ... args;
        // get an attribute from the source objects, return a collection
        ^this.collect({ arg obj; obj.performList(selector, args) })
    }
    set { arg selector, value;
        // set a single value to all source objects
        this.do({ arg obj; obj.perform(selector, value) })
    }

    // units
    dup { arg num=1;
        this.prUnitsDo({ arg unit; unit.dup(num) })
    }
    moveBy { arg vector;
        this.prUnitsDo({ arg unit; unit.moveBy(vector) })
    }
    copyBy { arg vector;
        this.prUnitsDo({ arg unit; unit.copyBy(vector) })
    }
    stop {
        this.prUnitsDo({ arg unit; unit.stop })
    }
    play {
        this.prUnitsDo({ arg unit; unit.play })
    }
    free {
        this.prUnitsDo({ arg unit; unit.free })
    }
    respawn {
        this.prUnitsDo({ arg unit; unit.respawn })
    }

    // channels
    mute { arg boo = true;
        this.prChannelsDo({ arg chan; chan.mute(boo) })
    }
    solo { arg boo = true;
        this.prChannelsDo({ arg chan; chan.solo(boo) })
    }
    db_ { arg db;
        this.prChannelsDo({ arg chan; chan.db = db })
    }




    // private
    prAllDo { arg function;
        mxapp.transaction({
            this.do({ arg obj, i, app; function.value(app) })
        })
    }
    prUnitsDo { arg function;
        mxapp.transaction({
            this.units.do({ arg obj, i, app; function.value(app) })
        })
    }
    prChannelsDo { arg function;
        mxapp.transaction({
            this.channels.do({ arg obj, i, app; function.value(app) })
        })
    }

/*

    connectToOutlet(outlet)
        this.do { arg obj;
            unit as compatible inlet
                mx.connect( outlet, inlet )
            inlet
                mx.connect( outlet, inlet )
    connectToQuery(query)
        do them zipped

    */
    objects { ^model }
    sources { ^model.collect(_.source) }
}
