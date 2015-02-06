

Mx : AbstractPlayerProxy {

    classvar <>defaultFrameRate = 24;

    var <channels, <cables;
    var <myUnit, <inlets, <outlets;

    var <>autoCable=true;
    var <>endBeat, <>loop=false, <>bpm;

    var allocator=0, register, unitGroups, busses;
    var <master;
    var removing, adding, cableEndpoints;
    var <>frameRate=24, sched, ticker, <position, frameRateDevices;

    var app;

    *new { arg data, endBeat, loop=false, bpm;
        ^super.new.endBeat_(endBeat).loop_(loop).bpm_(bpm).init(data)
    }
    storeArgs {
        ^[MxLoader.saveData(this, register), endBeat, loop, bpm];
    }
    init { arg data;
        var loader;

        register = IdentityDictionary.new;
        cables = MxCableCollection.new;

        if(data.isNil, {
            this.register(this, 0);
            master = this.prMakeChannel;
            this.registerChannel(master);
            master.fader.limit = 1.0;
            channels = [];
            inlets = [];
            this.addOutput;
            bpm = Tempo.bpm;
            // add a unit at top right
            this.findMasterInput;
        }, {
            loader = MxLoader(register);
            loader.loadData(data);
            this.register(this, 0);
            allocator = loader.maxID + 1;
            channels = loader.channels;
            master = loader.master;
            loader.cables.do(cables.add(_));

            inlets = loader.inlets;
            outlets = loader.outlets;

            this.allUnits.do { arg unit;
                unit.didLoad;
                this.unitAddFrameRateDevices(unit);
            };
        });
        source = master;
        sched = OSCSched.new;
        position = Position.new;
        this.updateVarPooling;
    }

    nextID {
        ^allocator = allocator + 1;
    }
    register { arg object, uid;
        if(uid.isNil, {
            uid = this.nextID;
        });
        register[uid] = object;
        ^uid
    }
    atID { arg uid;
        ^register[uid]
    }
    findID { arg object;
        ^register.findKeyForValue(object) ?? {
          Error("ID not found in registery for:"+object).throw
        }
    }
    unregister { arg uid;
        var item;
        item = register.removeAt(uid);
        if(item.isKindOf(MxChannel), {
            item.units.do { arg u;
                this.unregister(this.findID(u))
            };
            this.unregister(this.findID(item.myUnit));
        }, {
            if(item.isKindOf(MxUnit), {
                item.inlets.do({ arg in; this.unregister(this.findID(in)) });
                item.outlets.do({ arg in; this.unregister(this.findID(in)) });
            })
        })
    }
    registerUnit { arg unit, uid;
        uid = this.register(unit, uid);
        unit.mx = this;
        unit.inlets.do { arg inlet;
            this.register(inlet); // already registered ?
        };
        unit.outlets.do { arg outlet;
            this.register(outlet); // already registered ?
        };
        this.unitAddFrameRateDevices(unit)
    }
    registerChannel { arg channel, uid;
        uid = this.register(channel, uid);
        channel.myUnit.inlets.do { arg inlet;
            this.register(inlet);
        };
        channel.myUnit.outlets.do { arg outlet;
            this.register(outlet);
        };
    }
    findMasterInput {
        /* locate or make an input unit on the master channel */
        ^master.input ?? {
            master.input = master.units.detect({ arg u; u.source.isKindOf(MxChannelInput) });
            if(master.input.isNil, {
                master.makeInput;
                this.registerUnit(master.input);
            });
            master.input
        }
    }
    add { arg ... objects;
        ^this.insertChannel(channels.size-1, objects)
    }
    channelAt { arg chan;
        ^if(chan == inf, {master}, {channels[chan]});
    }
    indexOfChannel { arg channel;
        var i;
        if(channel === master, { ^inf });
        i = channels.indexOf(channel);
        ^i
    }

    extendChannels { arg toSize;
        // create more channels if needed
        // such that there is a channel at forIndex
        // and there is still the master channel after that
        var prior, nuchan, start, stop;
        start = channels.size;
        stop = toSize;
        if(stop >= start, {
            for(start, stop, { arg i;
                nuchan = this.prMakeChannel;
                channels = channels.insert(i, nuchan);
                this.registerChannel(nuchan);
                adding = adding.add(nuchan);
                nuchan.pending = true;
            });
        });
    }

    insertChannel { arg index, objects;
        // if adding a new channel it will insert it at the end
        // so will add one tp make sure there are at least that many channels
        var chan, units;
        if(index > channels.size, {
            this.extendChannels(index-1);
        });
        units = (objects ? []).collect({ arg obj; obj !? {this.prMakeUnit(obj)}});
        chan = this.prMakeChannel(units);
        this.registerChannel(chan);
        channels = channels.insert(index, chan);

        chan.pending = true;
        adding = adding.add(chan);
        if(autoCable, {
            this.updateAutoCables
        });
        this.updateVarPooling;
        this.changed('grid');
        ^chan
    }

    putChannel { arg index, objects;
        var prev, chan;
        prev = channels[index];
        if(prev.notNil, {
            this.prRemoveChannel(index);
        });
        ^this.insertChannel(index, objects)
    }
    removeChannel { arg index;
        this.prRemoveChannel(index);
        this.updateVarPooling;
        this.changed('grid'); // this is why app should be separate
    }
    prRemoveChannel { arg index;
        var chan;
        chan = channels.removeAt(index);
        chan.pending = true;
        removing = removing.add( chan );
        // cut any cables going to/from any of those units
        chan.units.do { arg unit;
            cables.fromUnit(unit).do(this.disconnectCable(_));
            cables.toUnit(unit).do(this.disconnectCable(_));
        };
    }
    prMakeChannel { arg units;
        var chan;
        chan = MxChannel(units ? []);
        ^chan
    }

    addOutput { arg rate='audio', numChannels=2;
        // not finished
        // just creates a default out for now, coming from the master

        // change this to keep the master as just a normal channel on grid
        // created by .mixer
        // the MxOutlet can specify how to find that
        // and master will be just for the player to use as its out
        // only trick then is to make addChannel insert before the output channels
        // add audio output
        var chan, out;

        // master output
        if(outlets.isNil or: {outlets.isEmpty}, {
            // do not like. this is not in anything and should be created in the same way
            // as other outlets/inlets via make: mx.myUnit
            // otoh outlets is an array here and make would read that
            // and its variable
            chan = master;
            out = MxOutlet(\out, outlets.size, 'audio'.asSpec, MxPlaysOnBus({chan.bus}));
            out.unit = this;
            outlets = outlets.add(out);
            this.register(out);
        });

        ^chan
    }
    at { arg chan, index;
        if(chan == inf, {
            ^master.at(index)
        }, {
            ^(channels.at(chan) ? []).at(index)
        })
    }
    put { arg chan, index, object;
        if(chan == inf, {
            ^this.putMaster(index, object)
        });
        if(channels[chan].isNil, {
            this.insertChannel(chan, Array.fill(index, nil) ++ [object]);
            ^this.at(chan, index)
        });
        ^this.prPutToChannel(channels[chan], index, object)
    }
    copy { arg fromChan, fromIndex, toChan, toIndex;
        var unit, copy, channel;
        unit = this.at(fromChan, fromIndex) ?? { ^nil };
        copy = MxUnit.make(unit.copySource, unit.source.class);
        this.extendChannels(toChan);
        channel = this.channelAt(toChan);
        ^this.prPutToChannel(channel, toIndex, copy);
    }
    prMakeUnit { arg object;
        var unit;
        unit = MxUnit.make(object);
        if(unit.notNil, { // nil object is nil unit which is legal
            this.registerUnit(unit);
            unit.didLoad;
        });
        ^unit
    }
    prPutToChannel { arg channel, index, object;
        var unit, old;
        unit = this.prMakeUnit(object);
        old = channel.at(index);
        if(old.notNil, {
            // cut or take any cables
            this.disconnectUnit(old);
            this.unregister(this.findID(old));
        });
        channel.put(index, unit);
        this.updateVarPooling;
        this.changed('grid');
        ^unit
    }
    putMaster { arg index, object;
        ^this.prPutToChannel(master, index, object)
    }
    move { arg chan, index, toChan, toIndex;
        var moving, unit, unitg, channel;
        moving = this.at(chan, index);
        if(moving.isNil, { ^this });
        channel = this.channelAt(chan);
        if(chan != toChan) {
            # unit, unitg = channel.extractAt(index);

            // could keep them connected, depends on order of execution
            cables.fromUnit(moving).do { arg cable;
                if(cable.inlet.unit.source.isKindOf(MxChannel), {
                    this.disconnectCable(cable);
                })
            };

            // make channel if needed
            if(toChan != inf, {
                this.extendChannels(toChan);
                channel = channels[toChan];
            }, {
                channel = master;
            });

            channel.insertAt(toIndex, unit, unitg);

            if(autoCable, {
                this.updateAutoCables;
            })
        } {
            // not yet checking if some cables need to be cut
            if(index != toIndex, {
                channel.move(index, toIndex);
            })
        };
        this.updateVarPooling;
        this.changed('grid');
    }
    remove { arg chan, index;
        var del;
        del = this.at(chan, index) ?? {^this};
        this.put(chan, index, nil);
        removing = removing.add(del);
        cables.toUnit(del).do { arg cab;
            if(cab.inlet.unit === del or: {cab.outlet.unit === del}, {
                this.disconnectCable(cab)
            })
        };
    }
    insert { arg chan, index, object;
        var channel, unit;
        if(chan == inf, {
            channel = master
        }, {
            channel = channels[chan];
            if(channel.isNil, {
                this.insertChannel(chan, Array.fill(index, nil) ++ [object]);
                ^this
            });
        });
        unit = this.prMakeUnit(object);
        channel.insert(index, unit);
        this.updateVarPooling;
        this.changed('grid');
        ^unit
    }
    removeUnit { arg unit;
        var p = this.pointForUnit(unit);
        ^this.remove(*p.asArray)
    }
    pointForUnit { arg unit;
        channels.do { arg ch, ci;
            ch.units.do { arg u, ri;
                if(unit === u, {
                    ^Point(ci, ri)
                })
            }
        };
        master.units.do { arg u, ri;
            if(unit === u, {
                ^Point(inf, ri)
            })
        };
        ^nil  // private channel unit, not on grid
    }
    unitAddFrameRateDevices { arg unit;
        unit.handlers.use {
            var frdSource;
            frdSource = ~frameRateDevice.value();
            if(frdSource.notNil, {
                // store it in unit data so the cable strategy can find it
                ~mxFrameRateDevice = this.addFrameRateDevice(frdSource, unit)
            })
        };
    }
    addFrameRateDevice { arg func, forUnit;
        var frd;
        frameRateDevices = frameRateDevices.add( frd = MxFrameRateDevice(func, forUnit) );
        if(this.isPlaying and: {ticker.isNil}, {
            this.startTicker
        });
        ^frd
    }
    removeFrameRateDeviceForUnit { arg unit;
        frameRateDevices.remove( frameRateDevices.detect({ arg frd; frd.forUnit === unit }) )
    }
    initialTick {
        frameRateDevices.do { arg frd;
            frd.tick(0.0);
        };
        position.value = 0.0;
    }
    startTicker { arg bundle;
        ticker = Task({
                    var beat, frr;
                    frr = frameRate.reciprocal;
                    loop {
                        beat = sched.beat;
                        frameRateDevices.do { arg frd;
                            frd.tick(beat);
                        };
                        position.value = beat;
                        frr.wait;
                    }
                }, sched.tempoClock);
        if(bundle.notNil, {
            bundle.addFunction({
                ticker.play
            })
        }, {
            ticker.play
        });
    }
    stopTicker { arg bundle;
        if(bundle.notNil, {
            bundle.addFunction({
                ticker.stop;
                ticker = nil;
            });
        }, {
            ticker.stop;
            ticker = nil;
        })
    }
    beatDuration {
        var max;
        // but if loop is on then its endless
        ^endBeat ?? {
            this.allUnits.do { arg unit;
                var numb;
                numb = unit.beatDuration;
                if(numb.notNil, {
                    if(max.isNil, {
                        max = numb
                    }, {
                        max = max(max, numb)
                    })
                })
            };
            max
        }
    }

    // API
    getInlet { arg point, index;
        var unit;
        unit = this.channelAt(point.x).units[point.y] ?? {Error("no unit at" + point).throw};
        ^unit.getInlet(index)
    }
    getOutlet { arg point, index;
        var unit;
        unit = this.channelAt(point.x).units[point.y] ?? {Error("no unit at" + point).throw};
        ^unit.getOutlet(index)
    }

    connect { arg fromUnit, outlet, toUnit, inlet, mapping=nil;
        /*
            unit: channelNumber@slotNumber
            outlet/inlet:
                \outletName
                integer Index
                nil meaning first
                outlet/inlet object
        */
        var cable;
        // should be in API
        // wrong
        if(outlet.isKindOf(MxOutlet).not, {
            outlet = this.getOutlet(fromUnit, outlet);
        });
        if(inlet.isKindOf(MxInlet).not, {
            inlet = this.getInlet(toUnit, inlet);
        });
        // is it possible ?
        if(MxCable.hasStrategy(outlet, inlet).not, {
            ("No MxCableStrategy found for" + outlet + outlet.adapter + "=>" + inlet + inlet.adapter).warn;
            ^this
        });

        // actual connection here
        // remove any that goes to this inlet
        // only the MxChannel inputs are supposed to mix multiple inputs
        // normal patch input points do not
        cables.toInlet(inlet).do { arg oldcable;
            if(oldcable.outlet === outlet, {
                ^this // already connected
            });
            if(oldcable.inlet.unit.source.isKindOf(MxChannel).not
                    and: {oldcable.active}, {
                this.disconnectCable(oldcable);
            })
        };
        cable = MxCable( outlet, inlet, mapping );
        if(this.isPlaying, {
            cable.pending = true;
            adding = adding.add( cable );
        });
        cables = cables.add( cable );
    }
    disconnect { arg fromUnit, outlet, toUnit, inlet;
        // should be in API
        if(outlet.isKindOf(MxOutlet).not, {
            outlet = this.getOutlet(fromUnit, outlet);
        });
        if(inlet.isKindOf(MxInlet).not, {
            inlet = this.getInlet(toUnit, inlet);
        });
        cables.do { arg cab;
            if(cab.inlet === inlet and: {cab.outlet === outlet}, {
                ^this.disconnectCable(cab)
            })
        };
    }
    disconnectCable { arg cable;
        if(this.isPlaying, {
            removing = removing.add( cable );
            cable.pending = true;
        });
        cables.remove(cable);
    }
    disconnectUnit { arg unit;
        cables.copy.do { arg cable;
            if(cable.inlet.unit === unit or: {cable.outlet.unit === unit}, {
                this.disconnectCable(cable);
            })
        };
    }
    disconnectInlet { arg inlet;
        cables.toInlet(inlet).do { arg cable;
            this.disconnectCable(cable)
        }
    }
    disconnectOutlet { arg outlet;
        cables.fromOutlet(outlet).do { arg cable;
            this.disconnectCable(cable)
        };
    }
    mute { arg channel, boo;
        var chan;
        chan = this.channels.at(channel);
        if(chan.notNil, {
            boo = boo ? chan.fader.mute.not;
            chan.fader.mute = boo;
        });
    }
    solo { arg channel, boo;
        var chan;
        chan = this.channels.at(channel);
        if(chan.notNil, {
            boo = boo ? chan.fader.solo.not;
            if(boo, {
                chan.fader.setSolo;
                this.channels.do { arg ch;
                    if(ch !== chan, {
                        ch.fader.muteForSoloist;
                    })
                };
            }, {
                chan.fader.unsetSolo;
                this.channels.do { arg ch;
                    if(ch !== chan, {
                        ch.fader.mute = false;
                    })
                };
            });
        });
    }
    beat {
        if(this.isPlaying, {
            ^sched.beat
        }, {
            ^position.value
        })
    }
    gotoBeat { arg beat, q=4, bundle;
        var b, beats, atBeat;
        beat = beat.trunc(q);
        atBeat = sched.beat.roundUp(q);

        b = bundle ?? {MixedBundle.new};
        b.addFunction({
            sched.beat = beat;
            position.value = beat;
        });
        channels.do { arg chan;
            chan.units.do { arg unit;
                if(unit.notNil, {
                    unit.gotoBeat(beat, atBeat, b)
                })
            }
        };
        if(this.isPlaying, {
            sched.schedAbs(atBeat, this.server, b.messages, {
                b.doSendFunctions;// should be before !
                b.doFunctions;
            });
        }, {
            b.doFunctions.doSendFunctions
        })
    }
    // enact all changes on the server after things have been added/removed dis/connected
    // syncChanges
    update { arg bundle=nil;
        var b;
        if(this.isPlaying, {
            b = bundle ?? { MixedBundle.new };

            removing.do { arg r;
                r.freeToBundle(b);
                b.addFunction({
                    if(r.isKindOf(MxChannel), {
                        r.units.do { arg u;
                            this.removeFrameRateDeviceForUnit(u)
                        }
                    }, {
                        this.removeFrameRateDeviceForUnit(r)
                    })
                })
            };
            // new channels
            adding.do { arg a;
                var g, prev, ci;
                if(a.isKindOf(MxChannel), {
                    g = Group.basicNew(group.server);
                    ci = channels.indexOf(a);
                    prev = channels[ ci - 1];
                    if(prev.notNil, {
                        b.add( g.addAfterMsg( prev.group ) )
                    }, {
                        b.add( g.addToHeadMsg( group ) )
                    });
                    a.prepareToBundle(group, b, true, groupToUse: g);
                }, {
                    a.prepareToBundle(group, b, true);
                });
                // it should be atTime
                a.spawnToBundle(b)
            };
            channels.do { arg chan; chan.update(b); };
            removing = adding = nil;
            b.addFunction({
                removing.do { arg r;
                    this.unregister(r);
                };
            });
            if(bundle.isNil, {
                b.send(this.server)
            });
        });
        ^b
    }
    updateVarPooling {
        // set up parent chain of unit environments that participate in varPooling
        var prevUnit;
        this.allUnits.do { arg u;
            u.parentEnvir = nil;
            if(u.varPooling) {
                if(prevUnit.notNil) {
                    // still worried that a timeGui will show up as implemented
                    // when its just a bleed through
                    u.parentEnvir = prevUnit.handlers
                };
                prevUnit = u
            };
        }
    }
    allUnits { arg includeChanUnit = true;
        ^Routine({
            channels.do({ arg c;
                if(includeChanUnit, { c.myUnit.yield; });
                c.units.do({ arg u;
                    if(u.notNil, {
                        u.yield
                    })
                })
            })
        });
    }

    //////////  private  ////////////
    clearPending {
        removing = [];
        adding = [];
    }

    children {
        ^[master] ++ channels // ++ cables
    }

    loadDefFileToBundle { arg b, server;
        this.children.do(_.loadDefFileToBundle(b, server))
    }
    prepareChildrenToBundle { arg bundle;
        channels.do { arg c;
            c.prepareToBundle(group, bundle, true)
        };
        master.prepareToBundle(group, bundle, false, this.bus);
    }
    spawnToBundle { arg bundle;
        // frameRate tick 0.0
        this.initialTick;
        cables.do(_.setInitial);
        channels.do({ arg chan;
            chan.spawnToBundle(bundle);
        });
        super.spawnToBundle(bundle);

        this.spawnCablesToBundle(bundle);
        bundle.addFunction({
            adding = removing = nil;
            // starting from the start
            sched.time = 0.0;
            sched.beat = 0.0;
        });
        this.startTicker(bundle);
    }

    spawnCablesToBundle { arg bundle;
        cables.do(_.spawnToBundle(bundle));
    }
    updateAutoCables {

        var patched, autoCabled, ac, changed=false, to, input;
        var addingCables=[], removingCables;

        (channels ++ [master]).do { arg chan;
            chan.units.do { arg unit;
                // should be auto cabled, and isn't already
                if(unit.notNil and: {unit.outlets.first.notNil} and: {unit.outlets.first.spec.isKindOf(AudioSpec)}, {
                    if(cables.fromOutlet(unit.outlets.first).isEmpty, {
                        ac = MxCable( unit.outlets.first, chan.myUnit.inlets.first );
                        cables.add(ac);
                        addingCables = addingCables.add(ac);
                        changed = true;
                    })
                })
            };
            // if the channel is not patched to anything then patch it to the master
            if(chan !== master, {
                if(cables.fromUnit(chan.myUnit).isEmpty, {
                    ac = MxCable( chan.myUnit.outlets.first, this.findMasterInput.inlets.first );
                    cables.add(ac);
                    addingCables = addingCables.add(ac);
                    changed = true;
                })
            }, {
                if(cables.fromUnit(chan.input).isEmpty, {
                    to = chan.units.detect({arg u; u.notNil and: {u.inlets.first.isKindOf(AudioSpec)}});
                    if(to.notNil, {
                        to = to.inlets.first
                    }, {
                        to = chan.myUnit.inlets.first
                    });
                    input = this.findMasterInput;
                    ac = MxCable( input.outlets.first, to );
                    cables.add(ac);
                    addingCables = addingCables.add(ac);
                    changed = true;
                })
            })
        };
        // patch units in master
        if(addingCables.notEmpty, {
            adding = adding.addAll(addingCables);
        });
        ^addingCables
    }
    stopToBundle { arg bundle;
        super.stopToBundle(bundle);
        if(ticker.notNil, {
            this.stopTicker(bundle)
        });
        cables.do(_.stopToBundle(bundle));
    }
    gui { arg parent, bounds;
        ^super.gui(parent, bounds ?? {Rect(100, 100, 900, 600)})
    }
    guiClass { ^MxGui }
    app {
        ^app ?? { app = MxApp(this) }
    }
    draw { arg pen, bounds, style;
        // odd
        master.draw(pen, bounds, style)
    }
}
