

MxMultiSplineTimeGui : MultiSplineFrGui {

    writeName {}
    guiBody { arg layout, bounds, argMaxTime;

        maxTime = argMaxTime ?? {model.spline.points.last[0]};
        splineGui = this.make2DSpline(focusedDim).gui(layout, Rect(0, 0, layout.bounds.width, layout.bounds.height - 25), model.spec, ControlSpec(0, maxTime));
        splineGui.onSelect = { arg i;
            pointSelector.activeValue = if(i.isNil, {-1}, {i});
        };

        ActionButton(layout, "EDIT", {
            model.gui(nil, 900@600)
        });
        CXLabel(layout, "DIM:", 75);
        focusedDim = NumberEditor(focusedDim.value, [1, model.spline.numDimensions-1, \lin, 1]);
        focusedDim.action = {
            this.update;
        };
        focusedDim.gui(layout);

        pointSelector = NumberEditor(-1, [-1, model.spline.points.size-1, 'lin', 1]);
        pointSelector.action = {
            if(pointSelector.value >= 0, {
                model.focusedPoint = pointSelector.value;
                this.update;
            }, {
                model.focusedPoint = nil
            })
        };
    }
    update { arg who, what;
        var v;
        if(who.isKindOf(BezierSpline), {
            super.update(who, what)
        }, {
            pointSelector.spec = [-1, model.spline.points.size-1, 'lin', 1].asSpec;
            pointSelector.value = model.focusedPoint ? -1;
            focusedDim.spec = [1, model.spline.numDimensions-1, \lin, 1].asSpec;
            // bad juggling : if the update did not come due to the splineGui
            if(who !== splineGui, {
                this.focusDim;
            })
        })
    }
}

MxMultiSplineTimeGui2 : MultiSplineFrGui {

    writeName {}
    guiBody { arg layout, bounds, argMaxTime;

        maxTime = argMaxTime;
        // ?? {model.spline.points.last[0]};

        splineGui = VectorSplineGui(model.spline);
        splineGui.gui(layout, Rect(0, 0, layout.bounds.width, layout.bounds.height - 25));

        ActionButton(layout, "EDIT", {
            model.gui(nil, 900@600)
        });
        // pop up to select dim


        /*
        splineGui.onSelect = { arg i;
            pointSelector.activeValue = if(i.isNil, {-1}, {i});
        };

        CXLabel(layout, "DIM:", 75);
        focusedDim = NumberEditor(focusedDim.value, [1, model.spline.numDimensions-1, \lin, 1]);
        focusedDim.action = {
            this.update;
        };
        focusedDim.gui(layout);

        pointSelector = NumberEditor(-1, [-1, model.spline.points.size-1, 'lin', 1]);
        pointSelector.action = {
            if(pointSelector.value >= 0, {
                model.focusedPoint = pointSelector.value;
                this.update;
            }, {
                model.focusedPoint = nil
            })
        };
        */
    }
    update { arg who, what;
        var v;
        if(who.isKindOf(BezierSpline), {
            super.update(who, what)
        }, {
            pointSelector.spec = [-1, model.spline.points.size-1, 'lin', 1].asSpec;
            pointSelector.value = model.focusedPoint ? -1;
            focusedDim.spec = [1, model.spline.numDimensions-1, \lin, 1].asSpec;
            // bad juggling : if the update did not come due to the splineGui
            if(who !== splineGui, {
                this.focusDim;
            })
        })
    }
    setZoom { arg argFromX, argToX;
        splineGui.setZoom(argFromX, argToX).refresh;
    }
    setMaxTime { arg argMaxTime;
        maxTime = argMaxTime;
        splineGui.setDomainSpec(0, maxTime)
    }
}
