

// when playEvent is called it spawns the event
// otherwise silent
EventPlayer : AbstractPlayer {

    // needs to make a group

    var <>postFilter, <>protoEvent, <>spec;
    var postStream, <>verbose=false;

    *new { arg postFilter, protoEvent, spec=\audio;
        ^super.new.init(postFilter, protoEvent).spec_(spec.asSpec)
    }
    storeArgs {
        ^[postFilter.enpath, protoEvent.enpath, spec]
    }
    init { arg pf, pe;
        postFilter = pf;
        protoEvent = pe ? Event.default;
    }
    playEvent { arg event;
        var e;
        e = protoEvent.copy.putAll(event);
        if(postStream.notNil, {
            e = postStream.next(e)
        });
        e.play;
        if(verbose, { e.debug });
        ^e
    }
    postFilterPut { arg k, v;
        var pf;
        pf = (postFilter ?? { postFilter = Event.new});
        if(v.notNil, {
            pf.put(k, v);
            if(this.isPlaying, {
                v.prepareForPlay(this.group, true)
            });
        }, {
            pf.removeAt(k)
        });
        this.resetProtoStream;
    }
    loadDefFileToBundle {}
    spawnToBundle { arg b;
        this.resetProtoStream;
        b.addFunction({
            this.prSetStatus(\isPlaying)
        })
    }
    resetProtoStream {
        postStream = nil;
        if(postFilter.size > 0, {
            postStream = Pbind(*postFilter.getPairs).asStream
        });
        protoEvent['group'] = this.group;
        protoEvent['bus'] = this.bus;
    }
    stopToBundle { arg b;
        b.addFunction({
            this.prSetStatus(\isStopped)
        })
    }
    freeAll {
        this.group.freeAll
    }
    isPlaying { ^(status == \isPlaying) }
    /*group {
        ^group ?? {
            group = Group(this.server.asTarget)
        }
    }*/
}


// plays the events at their \beat
// or when playEventAt(i) is called
EventListPlayer : EventPlayer {

    var <events;
    var sched, ei=0;

    *new { arg events, spec=\audio, postFilter, protoEvent;
        ^super.new(postFilter, protoEvent, spec).initElp.events_(events)
    }
    storeArgs {
        ^[events.enpath, spec, postFilter.enpath, protoEvent.enpath]
    }
    initElp {
        sched = BeatSched.new;
        events = SortedList(128, {arg a, b;
            (a['beat']?inf) <= (b['beat']?inf)
        });
    }
    events_ { arg evs;
        var start;
        evs = evs ? #[];
        start = events.size;
        evs.do { arg e, i;
            events.insert(start+i, e)
        };
        events.sort;
    }
    spawnToBundle { arg bundle;
        bundle.addFunction({
            sched.beat = 0.0;
            this.schedNext(0);
        });
        super.spawnToBundle(bundle);
        // TODO spawn event on 0.0 in the bundle
    }
    stopToBundle { arg b;
        b.addFunction({
            sched.clear
        });
        super.stopToBundle(b)
    }
    schedFromNow {
        sched.clear;
        this.schedNext(this.findNextAfter(sched.beat))
    }
    schedNext { arg newEi;
        var e, delta;
        ei = newEi;
        e = events[ei];
        if(e.notNil and: {e[\beat].notNil}, {
            delta = e['beat'] - sched.beat;
            if(delta.inclusivelyBetween(-0.02, 0.01), {
                this.playEvent(e);
                this.schedNext(ei + 1)
            }, {
                sched.schedAbs(e[\beat], {
                    this.playEvent(e);
                    this.schedNext(ei + 1)
                })
            })
        })
    }
    findNextAfter { arg beat;
        var lasti;
        lasti = events.lastIndexForWhich({ arg e; e[\beat] < beat });
        if(lasti.isNil, {^0});
        ^lasti + 1
    }
    addEvent { arg ev;
        var nei, evi;
        events = events.add(ev);
        if(this.isPlaying, {
            if(ev['beat'].notNil, {
                // is it next ?
                evi = events.indexOf(ev);
                if(evi <= ei, {
                    this.schedFromNow(nei);
                })
            })
        })
    }
    removeEvent { arg ev;
        events.remove(ev);
        if(ev['beat'].notNil and: {ev['beat'] >= sched.beat}, {
            // actually only needed if its next
            // will optimize later
            this.schedFromNow;
        })
    }
    playEventAt { arg i, inval;
        var te;
        te = events[i];
        if(te.notNil, {
            this.playEvent(te, inval)
        })
    }
    playEvent { arg event, inval;
        var e;
        e = protoEvent.copy.putAll(event);
        if(postStream.notNil, {
            e = postStream.next(e)
        });
        if(inval.notNil, {
            e.putAll(inval)
        });
        e.play;
        if(verbose, { e.asCompileString.postln; "".postln; });
        ^e
    }

    getEventBeat { arg i;
        ^events[i][\beat]
    }
    setEventBeat { arg i, beat;
        events[i].put(\beat, beat);
        if(this.isPlaying, {
            this.schedFromNow;
        })
    }
    beatDuration {
        ^events.maxValue({ arg e; e[\beat] ? 0 })
    }
    beat { ^sched.beat }
    gotoBeat { arg beat, atBeat, bundle;
        var f;
        f = {
            sched.beat = beat;
            this.schedFromNow;
        };
        if(bundle.notNil, {bundle.addFunction(f)}, f);
    }
    sorted {
        ^events
    }
    guiClass { ^EventListPlayerGui }
}


// specialized for instr events only
InstrEventListPlayer : EventListPlayer {

    instrArgs {
        var an;
        an = IdentityDictionary.new;
        events.do { arg e;
            var instr;
            if(e['instr'].notNil, {
                instr = e['instr'].asInstr;
                instr.argNames.do { arg aname, i;
                    an.put(aname, instr.specs.at(i) )
                }
            })
        };
        ^an
    }
    guiClass { ^InstrEventListPlayerGui }
}
