

MxChannel : AbstractPlayerProxy {

    var <>units, <fader, <>input;

    var <numChannels=2, <>pending=false;

    var <myUnit, <unitsGroup, unitGroups, <mixGroup;
    var adding, removing;

    *new { arg units, faderArgs;
        var fader, nc;
        units = units ? [];
        if(faderArgs.isNil, {
            nc = max(units.maxValue(_.numChannels) ? 2, 2);
            fader = MxChannelFader(numChannels: nc)
        }, {
            fader = MxChannelFader(*faderArgs)
        });
        ^super.new.init(units, fader, nc)
    }
    storeArgs {
        ^[units.collect({|u| u !? {u.saveData}}), fader.storeArgs]
    }

    init { arg argunits, argfader, nc;
        units = argunits;
        unitGroups = Array.newClear(units.size);
        fader = argfader;
        source = fader; // 2 proxy layers
        numChannels = nc ? 2;
        myUnit = MxUnit.make(this);
    }
    makeInput {
        input = MxChannelInput.new;
        input.numChannels = numChannels;
        input = MxUnit.make(input);
        if(this.at(0).notNil, {
            this.insert(0, input)
        }, {
            this.put(0, input);
        });
        ^input
    }
    at { arg index;
        ^units[index]
    }
    put { arg index, unit;
        this.removeAt(index);
        this.extendUnits(index);
        units[index] = unit;
        adding = adding.add(unit);
    }
    insert { arg index, unit;
        if(index < units.size, {
            for(units.size - 1, index - 1, { arg i;
                this.move(i, i + 1);
            });
        });
        this.put(index, unit);
    }
    extendUnits { arg index;
        var ip = index + 1;
        if(units.size < ip, {
            units = units.extend(ip, nil)
        });
        if(unitGroups.size < ip, {
            unitGroups = unitGroups.extend(ip, nil)
        });
    }
    removeAt { arg index;
        var old;
        if(units.size > index, {
            old = units.at(index);
            if(old.notNil, {
                removing = removing.add(old)
            });
            units.put(index, nil);
        })
        ^old
    }
    extractAt { arg index;
        // extract unit + group from this channel
        var old, oldg;
        if(units.size > index, {
            old = units.at(index);
            oldg = unitGroups.at(index);
            units.put(index, nil);
            unitGroups.put(index, nil);
        })
        ^[old, oldg]
    }
    insertAt { arg index, unit, unitGroup;
        // insert unit + group to this channel
        // for moving things that are already playing
        if(units[index].notNil, {
            removing = removing.add(units[index]);
            if(this.isPlaying, {
                removing = removing.add(unitGroups[index].freeMsg);
            });
        });
        this.extendUnits(index);
        units[index] = unit;
        unitGroups[index] = unitGroup;
        if(this.isPlaying, {
            adding = adding.add( this.moveUnitGroupMsg(index, unitGroup) );
        });
    }

    move { arg fromIndex, toIndex;
        var moving, g, oldg, old;
        moving = units[fromIndex];
        if(moving.notNil, {
            units.put(fromIndex, nil);

            old = this.removeAt(toIndex);

            // freeing the group now
            oldg = unitGroups[toIndex];
            if(oldg.notNil, {
                removing = removing.add( oldg );
            });

            g = unitGroups[fromIndex];
            if(g.notNil, {
                unitGroups[fromIndex] = nil;
                // just move the whole group with its contents
                if(this.isPlaying, {
                    adding = adding.add( this.moveUnitGroupMsg(toIndex, g) );
                });
            }, {
                g = Group.basicNew(this.server);
                if(this.isPlaying, {
                    adding = adding.add( this.addUnitGroupMsg(toIndex, g) );
                });
            });
            this.extendUnits(toIndex);
            unitGroups[toIndex] = g;
            units[toIndex] = moving;
        })
    }
    children {
        ^super.children ++ units
    }
    prepareToBundle { arg parentGroup, bundle, private, bus, groupToUse;
        // if has a to-destination then play on a private bus
        // and the Mx will cable it to that destination
        // else play on public/main out => this is a master channel
        var inputGroup;
        group = groupToUse ?? {
            group = Group.basicNew(parentGroup.server);
            bundle.add( group.addToTailMsg(parentGroup) );
            this.annotate(group);
            group
        };
        super.prepareToBundle(group, bundle, private, bus);

        unitsGroup = Group.basicNew(this.server);
        this.annotate(unitsGroup, "unitsGroup");
        bundle.add( unitsGroup.addToTailMsg(group) );

        units.do { arg u, i;
            if(u.notNil, {
                this.groupForIndex(i, bundle);
            });
        };

        mixGroup = Group.basicNew(this.server);
        bundle.add( mixGroup.addToTailMsg(group) );
        this.annotate(mixGroup, "mixGroup");

        fader.prepareToBundle(mixGroup, bundle, private, bus);

        units.do { arg unit, i;
            if(unit.notNil and: {unit.isPrepared.not}, {
                unit.prepareToBundle(unitGroups[i], bundle, true)
            })
        };
        adding = removing = nil; // all taken care of
    }
    prepareChildrenToBundle { arg bundle; }
    loadDefFileToBundle { arg b, server;
        // units will load during prepare
        fader.loadDefFileToBundle(b, server)
    }
    spawnToBundle { arg bundle;
        units.do { arg u, i;
            var g;
            if(u.notNil, {
                if(u.isPlaying.not, {
                    // messy. this is if the channel is spawning in an mx already playing
                    //if(u.notNil and: {u.isPrepared.not and: {u.status != \isPreparing}}, {
                    //    u.prepareToBundle(this.groupForIndex(i, bundle), bundle, true)
                    //});
                    u.spawnToBundle(bundle);
                }, {
                    g = this.groupForIndex(i, bundle);
                    if(u.group !== g, { // not sure about this
                        u.moveToHead(g, bundle)
                    });
                    // but is that group inside my group ?
                    // cheapest to assume its not and issue a move command
                    bundle.add( this.moveUnitGroupMsg(i, g) );
                });
            })
        };
        super.spawnToBundle(bundle);
        adding = removing = nil;
    }
    stopToBundle { arg bundle;
        if(input.notNil, {
            input.stopToBundle(bundle)
        });
        super.stopToBundle(bundle);
    }
    freeToBundle { arg bundle;
        super.freeToBundle(bundle);
        mixGroup.freeToBundle(bundle);

        if(input.notNil, {
            input.freeToBundle( bundle );
            input.group.freeToBundle( bundle );
        });
        unitsGroup.freeToBundle(bundle);
        // that removes the unitsGroups
        bundle.addFunction({
            mixGroup = nil;
            unitGroups = Array.newClear(units.size);
            adding = removing = nil;
        });
    }
    update { arg bundle;
        removing.do { arg unit;
            if(unit.isArray, {
                bundle.add(unit)
            }, {
                unit.freeToBundle(bundle);
            })
        };
        adding.do { arg unit;
            var ui, ug;
            if(unit.isArray, {
                bundle.add(unit)
            }, {
                ui = units.indexOf(unit);
                ug = this.groupForIndex(ui, bundle);
                unit.prepareToBundle(ug, bundle, true);
                unit.spawnToBundle(bundle);
            });
        };
        removing = adding = nil;
    }
    bus {
        ^source.bus
    }
    groupForIndex { arg index, bundle;
        // make group on demand
        var g;
        ^unitGroups.at(index) ?? {
            g = Group.basicNew(this.server);
            this.annotate(g, "unit"+index);
            bundle.add( this.addUnitGroupMsg(index, g) );
            if(unitGroups.size < index, {
                unitGroups = unitGroups.extend(index, nil);
            });
            unitGroups.put(index, g);
            g
        }
    }
    addUnitGroupMsg { arg index, g;
        var prev;
        prev = unitGroups.copyRange(0, index).reverse.detect(_.notNil);
        if(prev.notNil, {
            ^g.addAfterMsg(prev)
        }, {
            ^g.addToHeadMsg(unitsGroup)
        });
    }
    moveUnitGroupMsg { arg index, g;
        var prev;
        prev = this.findGroupPreviousTo(index);
        if(prev.notNil, {
            ^g.moveAfterMsg(prev)
        }, {
            ^g.moveToHeadMsg(this.group)
        });
    }
    findGroupPreviousTo { arg index;
        if(index > 0, {
            for(index-1, 0, { arg i;
                unitGroups[i] !? { ^unitGroups[i] }
            });
        });
        ^nil
    }

    inlets { ^myUnit.inlets }
    outlets { ^myUnit.outlets }
}
