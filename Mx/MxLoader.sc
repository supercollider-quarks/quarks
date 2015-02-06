

MxLoader {

    classvar <>verbose=false;

    var f, registerData;
    var <registery, <channels, <master, <cables, <inlets, <outlets;
    var allocator = 100000, allInlets, allOutlets;

    /*  SAVE   */
    *saveData { arg mx, register;

        var f, registerData, cables;
        f = IdentityDictionary.new;

        f[MxUnit] = { arg unit;
            [    MxUnit,
                unit.saveData,
                unit.inlets.collect(mx.findID(_)),
                unit.outlets.collect(mx.findID(_))]
        };
        f[MxChannel] = { arg channel;
            [    MxChannel,
                channel.units.collect({ arg unit; unit !? {mx.findID(unit)}}),
                channel.inlets.collect(mx.findID(_)),
                channel.outlets.collect(mx.findID(_)),
                channel.fader.storeArgs
            ]
        };
        f[MxInlet] = { arg inlet;
            [     MxInlet, inlet.name ]
        };
        f[MxOutlet] = { arg outlet;
            [     MxOutlet, outlet.name ]
        };
        f[Mx] = { arg mx;
            [ Mx ,
                mx.inlets.collect({ arg i; [mx.findID(i), i.name, i.spec] }),
                mx.outlets.collect({ arg o; [mx.findID(o), o.name, o.spec] }),
                mx.channels.collect(mx.findID(_)),
                mx.findID(mx.master)
            ]
        };
        registerData = Array.new(register.size);
        register.keysValuesDo({ arg id, object;
            var data;
            data = f[object.class].value(object);
            if(data.notNil, {
                registerData.add( id -> data )
            })
        });
        // should just register cables
        cables = mx.cables.collect { arg cable;
            [mx.findID(cable.outlet), mx.findID(cable.inlet), cable.mapping, cable.active]
        };
        ^[registerData, cables]
    }

    /*  LOAD   */
    *new { arg register;
        ^super.new.init(register)
    }
    log { arg ... msgs;
        if(verbose, {
            msgs.debug
        })
    }
    loadData { arg data;
        this.initForLoad(data[0]);
        this.log("SCAN IOLET NAMES *******************");
        registerData.keysValuesDo { arg id, data;
            if(data[0] == MxInlet, {
                this.log("inlet:", id, data[1]);
                allInlets[id] = data[1]
            });
            if(data[0] == MxOutlet, {
                this.log("outlet:", id, data[1]);
                allOutlets[id] = data[1]
            });
        };
        this.log("READ DATA *******************");
        registerData.keysValuesDo { arg id, data;
            var object;
            this.log("reading id", id, "data", data);
            if(this.registery[id].isNil, {
                this.log("not in registery yet, getting id", id);
                object = this.get(id);
                this.log("found object", id, object);
                this.register(object, id);
            })
        };
        this.log("CABLES *******************");
        cables = data[1].collect { arg data;
            var oid, iid, mapping, active;
            # oid, iid, mapping, active = data;
            this.log("cable data", "out", oid, "in", iid, "mapping", mapping, "active", active);
            MxCable( this.get(oid), this.get(iid), mapping, active)
        }
    }

    init { arg mxRegister;
        registery = mxRegister
    }
    initForLoad { arg rd;
        // associations to dict
        registerData = IdentityDictionary.new;
        rd.do(registerData.add(_));
        allInlets = Dictionary.new;
        allOutlets = Dictionary.new;

        // load functions
        f = IdentityDictionary.new;
        f[Mx] = { arg uid, data;
            var ins, outs, chans, mast;
            # ins, outs, chans, mast = data;
            channels = chans.collect({ arg cid; this.get(cid) });
            master = this.get(mast);
            inlets = ins.collect({ arg d, i;
                        var id, name, spec, io;
                        # id, name, spec = d;
                        io = MxInlet(name, i, spec);
                        this.register(io, id);
                        io
                    });
            outlets = outs.collect({ arg d, i;
                        var id, name, spec, io;
                        # id, name, spec = d;
                        io = MxOutlet(name, i, spec);
                        this.register(io, id);
                        io
                    });
            nil
        };
        f[MxChannel] = { arg uid, data;
            var channel, units, unitIDs, inletIDs, outletIDs, faderArgs;
            # unitIDs, inletIDs, outletIDs, faderArgs = data;
            units = unitIDs.collect({ arg id; id !? {this.get(id)}});
            channel = MxChannel(units, faderArgs);
            this.readIOlets(channel.inlets, inletIDs, allInlets);
            this.readIOlets(channel.outlets, outletIDs, allOutlets);
            this.register(channel, uid);
            channel
        };
        f[MxUnit] = { arg uid, data;
            var unit, saveData, inletIDs, outletIDs;
            # saveData, inletIDs, outletIDs = data;
            this.log("MxUnit.loadData", saveData);
            unit = MxUnit.loadData(saveData);
            this.readIOlets( unit.inlets, inletIDs , allInlets);
            this.readIOlets( unit.outlets, outletIDs , allOutlets);
            this.register(unit, uid);
            unit
        };
    }
    readIOlets { arg unitIOlets, ioletIDs, allIOlets;
        // safe if arg order of unit has changed since saveing
        // if new args in unit
        // and if args removed from unit
        var savedIOlets = Dictionary.new;
        ioletIDs.do { arg id, i;
            var io, name;
            name = allIOlets[id];
            if(name.notNil, {
                savedIOlets[name] = id;
                this.log("found iolet", name, id);
            }, {
                // if no name, then wasn't saved MxInlet name.
                // probably the old data format
                // assume they are saved in correct order
                io = unitIOlets[i];
                if(io.notNil, {
                    savedIOlets[io.name] = id;
                    this.log("old data format, assuming in same order", io, id);
                }, {
                    this.log("NO IOLET FOUND FOR index", i, "in", unitIOlets);
                })
            })
        };
        unitIOlets.do { arg io, i;
            var id;
            //this.log("finding ioletID for", io, ioletIDs, "@", i, "=", ioletIDs[i] ? "NIL!!!!!!!!!!!");
            id = savedIOlets[io.name];
            if(id.isNil, {
                // new iolet
                id = this.allocateNewID;
                this.log("IOLET", io, "not found in saved data. allocating new ID for it", id);
            }, {
                this.log("found", id, io.name, "for", io);
            });
            this.register(io, id)
        };
    }
    get { arg id;
        var klass, data, obj;
        ^registery[id] ?? {
            this.log("get from registerData", id);
            klass = registerData[id][0];
            data = registerData[id].copyToEnd(1);
            this.log("running load function:", klass, id, data, "...");
            obj = f[klass].value(id, data);
            this.log("got", klass, obj, data);
            if(obj.notNil, {
                registery[id] = obj;
            });
            obj
        }
    }
    register { arg object, id;
        registery[id] = object
    }

    maxID {
        var max=0;
        registery.keysDo { arg id;
            max = max(max, id?0)
        }
        ^max
    }
    allocateNewID {
        ^allocator = allocator + 1
    }
}
