
/*

goal:

    offer all ControlSpec inlets in the current Mx
    show all on a gui with sliders
    learn cc, keystroke (toggle), osc

    record those events
    multiple patterns / takes
    mute/play/solo takes
        so pattern switching is by soloing one of the takes
    offset takes in time,
    set endtime of take non-destructively
    separate or merge tracks

    save everything

status:
    just does the gui and the connect, no record

    would be better to use SynthiX to enable recording of wires
        some wires are recordable
        or record outlet ?

        or inlet ?
            by recording what goes to the inlet
            then you can disconnect controller and connect it to something else

    recording gestures might be interesting.

    just recording audio is more interesting
    synthix deserves more attention right now


*/


MxControlRecorder {

    var <inlets, <cc, app, <nums;

    didLoad {
        // will change ~this to be the unit app for all
        app = ~mx.app.prFind(~this);
        cc = CCBank.new;
        inlets = IdentityDictionary.new;
        nums = IdentityDictionary.new;
        app.mx.app.units.inlets.select({ arg inlet, i, iapp;

        inlet.spec.isKindOf(ControlSpec) and: {iapp.from.size == 0} }).do { arg inlet, i, iapp;
            this.addInlet(iapp);
            this.addCC(iapp, nil);
        };
    }
    makeKey { arg iapp;
        var uid;
        uid = iapp.mx.findID(iapp.model).asString;
        ^(uid ++ "#" ++ iapp.unit.name.asString ++ ":" ++ iapp.name.asString).asSymbol
    }
    addInlet { arg iapp;
        var key, ne;
        key = this.makeKey(iapp);
        inlets[key] = iapp;
        // get current value from inlet
        nums[key] = ne = NumberEditor(iapp.spec.default, iapp.spec);
        app.addOutlet(key, iapp.spec,
            MxHasAction({arg action;
                ne.action = action
            })
        );
        app.o.at(key) >> iapp
    }
    addCC { arg iapp, ccnum;
        // remember to make the num too
        // if adding later
        var key;
        key = this.makeKey(iapp);
        cc.add(key, ccnum);
        /* already active as soon as its created,
            does not wait for play */
        cc.responder(key, { arg val;
            this.ccEvent(key, val)
        });
    }
    ccEvent { arg key, val;
        nums[key].valueAction = nums[key].spec.map(val / 127.0);
        // record it
    }
    guiClass { ^MxControlRecorderGui }
}


MxControlRecorderGui : ObjectGui {

    guiBody { arg layout, bounds;
        var inlets;
        //model.cc.initGui;
        // sort by point, index
        inlets = model.inlets.values.sort({ arg a, b;
            var ap, bp;
            ap = a.unit.point;
            bp = b.unit.point;
            if(ap == bp, {
                a.model.index <= b.model.index
            }, {
                ap <= bp
            })
        });
        inlets.do { arg iapp;
            var key;
            key = model.makeKey(iapp);
            //cc
            model.cc.guiOne(layout.startRow, model.cc.findSet(key), 200);
            model.nums[key].gui(layout, 150@17);
            (model.nums[key].spec.units ? "").asString.gui(layout)
        }
    }
}
