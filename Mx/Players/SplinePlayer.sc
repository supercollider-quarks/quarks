
/*
    the only thing this adds is a spec and player compatibility.
    but player compat is only adding a few methods.
    this can add a gui that is full-service while leaving basic spline classes alone
*/


KrSplinePlayer : AbstractPlayer {

    var <>gen, >spec;

    *new { arg spline, dimension=0, loop=false;
        ^super.new.gen_(SplineGen(spline ?? {
            BezierSpline(
                0@0,
                  [ 0.2@0.4, 0.5@0.9  ],
                120@1,
                  [],
                false
            )}, dimension, loop))
    }
    storeArgs { ^gen.storeArgs }
    kr {
        // embeds the interpolated spline in the synth def
        ^gen.kr
    }
    defName {
        ^"ksp"++gen.spline.hash
    }
    beatDuration {
        ^gen.spline.points.last[0]
    }
    rate { ^\control }
    spec { ^(spec ?? { \unipolar.asSpec }) }
}


SplineFr {

    var <>spline, <>dimension=0, <>loop=false, <>spec, <>frameRate, <>interpolationDensity=0.25;
    var valueToSetOnTick;
    var table;

    *new { arg spline, dimension=0, loop=false, spec, frameRate;
        ^super.newCopyArgs(spline ?? {
            spec = spec ?? {'unipolar'.asSpec};
            BezierSpline(
                0@0,
                  [],
                120@1,
                  [],
                false
            )}, dimension, loop, spec, frameRate ?? {Mx.defaultFrameRate}).init
    }
    storeArgs { ^[spline, dimension, loop, spec] }

    init {
        this.initTable;
        spline.addDependant(this);
    }
    initTable {
        table = spline.bilinearInterpolate((frameRate*interpolationDensity) * spline.points.last[dimension], dimension, true);
    }
    update {
        this.initTable
    }
    free {
        spline.removeDependant(this)
    }
    value { arg time;
        var t = (time * frameRate * interpolationDensity);
        var vals;
        if(valueToSetOnTick.notNil, {
            this.setValue(valueToSetOnTick, time);
            vals = valueToSetOnTick;
            valueToSetOnTick = nil;
            // note: table is not yet recalculated
            // so it will probably glitch to previous spline until you recalc
            // can only do that with an intelligent interpolator
            // probably better to keep returning last valueToSetOnTick
            // until turn off record at which point initTable
            // and/or blank till end or do a loop record
            ^vals
        });
        vals = table.clipAt(t.floor + [0, 1]);
        ^vals[0].blend(vals[1], t.frac)
    }
    setValueOnNextTick { arg value;
        valueToSetOnTick = value;
    }
    setValue { arg value, time;
        // assuming it was sorted when created
        var i, point;
        # i , point = this.findInsertIndex(time);
        if(point.notNil, {
            point[dimension+1] = value
        }, {
            spline.createPoint([time, value], i+1)
        })
    }
    findInsertIndex { arg time;
        var prev;
        spline.points.do {|elem, i|
            if (elem[dimension] < time) {
                prev = i;
            } {
                if(elem[dimension] == time) {
                    ^[prev, elem]
                } {
                    ^[prev, nil]
                }
            }
        };
        ^[prev, nil]
    }
    gui { arg parent, bounds, maxTime;
        ^spline.gui(parent, bounds, spec, ControlSpec(0, maxTime??{spline.points.last.x}))
    }
}
