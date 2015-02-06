

MultiSplineFrGui : ObjectGui {

    var splineGui, focusedDim=1, pointSelector, values, valuesD, times, maxTime, maxTimeV;
    var thetas, rhos;

    writeName { arg layout;
        CXLabel(layout, "Multi Dimensional Spline Designer", font:Font("Helvetica-Bold", 14));
        layout.startRow;
    }
    guiBody { arg layout, bounds, argMaxTime;

        maxTime = argMaxTime ?? {model.spline.points.last[0]};
        splineGui = this.make2DSpline(focusedDim).gui(layout, Rect(0, 0, layout.bounds.width, 150), model.spec, ControlSpec(0, maxTime));
        splineGui.onSelect = { arg i;
            pointSelector.activeValue = if(i.isNil, {-1}, {i});
        };

        layout.startRow;
        CXLabel(layout, "Dimension", 75);
        focusedDim = NumberEditor(focusedDim.value, [1, model.spline.numDimensions-1, \lin, 1]);
        focusedDim.action = {
            this.update;
        };
        focusedDim.gui(layout);

        ActionButton(layout, "+dim", {
            model.addDimension(model.spec.default).initTable.changed;
            this.updateSplineGui;
        });
        ActionButton(layout, "-dim", {
            model.removeDimension(model.spline.numDimensions-1).initTable.changed
        });

        layout.startRow;
        // pause play / focus a point
        pointSelector = NumberEditor(-1, [-1, model.spline.points.size-1, 'lin', 1]);
        pointSelector.action = {
            if(pointSelector.value >= 0, {
                model.focusedPoint = pointSelector.value;
                this.update;
            }, {
                model.focusedPoint = nil;
                this.update;
            })
        };
        CXLabel(layout, "Point", 75);
        pointSelector.gui(layout);

        ActionButton(layout, "+", {
            model.createPoint.initTable.changed
        });
        ActionButton(layout, "-", {
            model.deletePoint.initTable.changed
        });
        ActionButton(layout, "RND all", {
            model.spline.points.do { arg p, i;
                if(i > 0, {
                    model.focusedPoint = i;
                    model.liveValues = model.spec.map( Array.fill(model.spline.numDimensions - 1, {1.0.rand}) );
                    model.savePoint;
                })
            };
            pointSelector.activeValue = -1;
            model.initTable.changed
        });

        layout.startRow;
        ActionButton(layout, "Values @ P", {
            values.valueAction = values.value.collect { 1.0.rand };
        }, 75);
        values = MultiSliderView(layout, 100@150);
        values.action = {
            model.liveValues = model.spec.map( values.value );
            if(model.focusedPoint.notNil, {
                model.savePoint.initTable.changed;
                this.updateSplineGui;
            });
        };

        ActionButton(layout, "Values in DIM", {
            model.setDimValues( focusedDim.value, model.spec.map( valuesD.value.collect { 1.0.rand } ) );
            model.initTable.changed;
        }, 75);
        valuesD = MultiSliderView(layout, 100@150);
        valuesD.action = {
            model.setDimValues( focusedDim.value, model.spec.map( valuesD.value ) );
            model.initTable.changed;
        };

        layout.startRow;
        ActionButton(layout, "Times", {
            times.valueAction = times.value.collect { 1.0.rand };
        }, 75);
        times = MultiSliderView(layout, 100@150);
        times.action = {
            var t, total, scale, maxValue;
            t = times.value;
            total = t.sum;
            scale = maxTime / total;
            t = t.integrate * scale;
            model.setDimValues(0, [0] ++ t );
            model.initTable.changed;
        };
        // maxTime
        CXLabel(layout, "maxTime:", 75);
        maxTimeV = NumberEditor(maxTime, [0, 10000, \lin, 1]);
        maxTimeV.action = {
            maxTime = maxTimeV.value;
            times.action.value;
        };
        maxTimeV.smallGui(layout);

        // when move point on splineGui that should change the times        // and do sliders so they can be edited non-linear

        layout.startRow;
        CXLabel(layout, "Theta", 75);
        thetas = MultiSliderView(layout, 100@150);
        thetas.value = Array.fill(model.spline.points.size - 1, {0.5});
        thetas.action = {
            this.setControlPoints;
            model.initTable.changed;
        };
        CXLabel(layout, "Rho", 75);
        rhos = MultiSliderView(layout, 100@150);
        rhos.value = Array.fill(model.spline.points.size - 1, {0.5});
        rhos.action = {
            this.setControlPoints;
            model.initTable.changed;
        };

        this.update

    }
    update { arg who, what;
        var v, stop;
        stop = model.spline.points.size - 1;
        if(who.isKindOf(BezierSpline), {
            // update from editable spline
            who.points.do { arg p, i;
                var di = focusedDim.value;
                if(model.spline.points[i][0] != p[0], {
                    model.spline.points[i][0] = p[0]
                });
                if(p[1] != model.spline.points[i][di], {
                    model.spline.points[i][di] = p[1]
                })
            };
            model.spline.controlPoints = Array.fill(stop, { arg i;
                var gp;
                // return [ ] or [ [t, x, y, z] ]
                if(who.controlPoints[i].size == 0, {
                    []
                }, {
                    gp = who.controlPoints[i].first;
                    [ Array.fill(model.numDimensions + 1, { arg di;
                        var prev, cur, v, b, t;

                        t = gp[0];
                        if(di == 0, {
                            t
                        }, {
                            if(di==focusedDim.value, {
                                gp[1]
                            }, {
                                try {
                                    model.spline.controlPoints[i][0][di]
                                } {
                                    // insert a control point value that lies along the tangent
                                    // at time t
                                    prev = model.spline.points.clipAt(i);
                                    cur = model.spline.points.clipAt(i+1);
                                    v = cur[di] - prev[di];
                                    b = (t - prev[0]) / (cur[0] - prev[0]);
                                    blend( prev[di], cur[di], b )
                                }
                            })
                        })
                    }) ]
                })
            });

            this.update(splineGui);
        }, {
            values.value = v = model.spline.points[pointSelector.value.asInteger.max(0)].copyToEnd(1);
            values.thumbSize = values.bounds.width / v.size * 0.9;

            v = model.spec.unmap( model.spline.points.collect({ arg p; p[focusedDim.value] }) );
            valuesD.value = v;
            valuesD.thumbSize = valuesD.bounds.width / v.size * 0.9;

            maxTime = model.spline.points.last[0];
            maxTimeV.value = maxTime;

            v = model.spline.points.collect(_[0]).differentiate / maxTime;
            times.value = v = v.copyToEnd(1);
            times.thumbSize = times.bounds.width / v.size * 0.9;

            if( thetas.value.size != stop, {
                thetas.value = Array.fill(stop, {0.5});
            });
            if( rhos.value.size != stop, {
                rhos.value = Array.fill(stop, {0.5});
            });

            pointSelector.spec = [-1, stop, 'lin', 1].asSpec;
            pointSelector.value = model.focusedPoint ? -1;
            focusedDim.spec = [1, model.spline.numDimensions-1, \lin, 1].asSpec;
            // bad juggling : if the update did not come due to the splineGui
            if(who !== splineGui, {
                this.focusDim;
            })
        })
    }
    focusDim { arg di;
        if(di.isNil, {
            this.updateSplineGui
        }, {
            focusedDim.value = di;
            this.updateSplineGui
        })
    }
    updateSplineGui {
        splineGui.setDomainSpec( [0, maxTime, \lin].asSpec );
        splineGui.model = this.make2DSpline(focusedDim.value.asInteger);
        splineGui.select(if(pointSelector.value == -1, nil, {pointSelector.value}));
    }
    make2DSpline { arg di;
        var bs;
        if(splineGui.notNil, {
            splineGui.model.removeDependant(this)
        });
        bs = model.spline.sliceDimensions([0, di]);
        bs.addDependant(this);
        ^bs
    }
    setControlPoints {
        var t, r;
        t = thetas.value - 0.5 * 2pi / 32.0;
        r = rhos.value;
        model.spline.controlPoints = Array.fill(model.spline.points.size - 1, { arg i;
            var prev, next, newCP;
            prev = model.spline.points[i];
            next = model.spline.points.clipAt(i+1);
            newCP = [ blend( prev[0], next[0], r[i] ? 0.5 ) ];
            model.numDimensions.do { arg di;
                var vs, vals, cp, p, n, newDimCp;
                di = di + 1;
                p = Point( prev[0], prev[di] );
                n = Point( next[0], next[di] );
                vs = (n - p).asPolar;
                vs = vs.rotate( t[i] );
                newDimCp = p + vs.asPoint;
                newCP = newCP.add( newDimCp.y );
            };
            [newCP]
        });
    }
    setZoom { arg argFromX, argToX;
        splineGui.setZoom(argFromX, argToX).update;
    }
    setMaxTime { arg mt;
        maxTime = mt;
        if(maxTimeV.notNil, {
            maxTimeV.value = mt;
        })
    }
}
