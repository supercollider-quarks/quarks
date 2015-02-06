


MultiSplineFr : SplineFr {

    var <>liveValues, <focusedPoint;

    initTable {
        var numPoints, step, t1, last, t, r;
        last = spline.points.last[0];
        numPoints = (frameRate*interpolationDensity) * last;
        // these points are unevenly spaced along u
        t1 = spline.interpolate(numPoints);
        step = last / numPoints;

        r = Routine({
             t1.do { arg p, i;
                 if(p[0] >= t, {
                     [p, t1.at(i+1)].yield
                 });
             };
             [nil, nil].yield
        });

        table = Array.fill(numPoints, { arg i;
                    var p1, p2, u0, u1, b;
                    t = step * i;
                    #p1, p2 = r.next;
                    if(p2.notNil, {
                        u0 = p1[0];
                        u1 = p2[0];
                        // where is t along the u0 .. u1
                        b = (t - u0) / (u1 - u0);
                        blend( p1, p2, b)
                    }, {
                        spline.points.last.copy
                    })
                });
    }
    value { arg time;
        var p1, p2;
        if(focusedPoint.notNil, {
            ^spline.points.clipAt(focusedPoint.floor.asInteger)
        });
        ^super.value(time); // from the table, with no time val
    }
    setValue { arg value, time;
        var i, point;
        # i , point = this.findInsertIndex(time);
//        if(point.notNil, {
//            point[0+1] = value
//        }, {
//            spline.createPoint([time, value], i+1)
//        })
    }
    numDimensions {
        ^spline.numDimensions - 1
    }
    addDimension { arg default=0.0;
        spline.points = spline.points.collect { arg p; p.add(default) };
    }
    removeDimension { arg di;
        spline.points = spline.points.collect { arg p; p.removeAt(di); p };
    }
    gui { arg parent, bounds, maxTime;
        ^this.guiClass.new(this).gui(parent, bounds, maxTime)
    }
    guiClass {
        ^MultiSplineFrGui
    }

    savePoint {
        if(focusedPoint.notNil, {
            spline.points[focusedPoint] = [spline.points[focusedPoint][0]] ++ liveValues;
        });
    }
    createPoint {
        var i, p, time;
        if(focusedPoint.isNil, {
            i = spline.points.size;
            // extend time
            time = spline.points.last[0] + (spline.points.last[0] - spline.points.clipAt(spline.points.size-1)[0]);
        }, {
            i = focusedPoint.ceil.asInteger;
            time = blend( spline.points[focusedPoint.floor.asInteger][0], spline.points.clipAt(focusedPoint.ceil.asInteger)[0], focusedPoint.frac);
        });
        if(liveValues.notNil, {
            p = [time] ++ liveValues
        }, {
            p = [time] ++ spline.points.last.copyToEnd(1)
        });
        spline.createPoint(p, i);
    }
    deletePoint {
        if(focusedPoint.notNil, {
            spline.deletePoint(focusedPoint.floor.asInteger)
        });
    }
    setDimValues { arg di, vals;
        spline.points = spline.points.collect { arg p, i; p.copy.put(di, vals[i]) };
    }
    focusedPoint_ { arg fp;
        focusedPoint = fp;
        liveValues = nil;
    }
}
