

MxGui : AbstractPlayerGui {

    var boxes, drawerGui;

    writeName {}
    saveConsole { arg layout;
        super.saveConsole(layout);
        layout.startRow;
        ActionButton(layout, "Timeline", {
            MxTimeGui(model).gui(nil, Rect(0, 0, 1000, 800));
        });
        ActionButton(layout, "Mixer", {
            MxMixerGui(model).gui(nil, Rect(0, 0, 1000, 500));
        });
        ActionButton(layout, "SynthiX", {
            SynthiX(model.app.outlets, model.app.inlets).gui(nil, Rect(0, 0, 1000, 500))
        });

        ActionButton(layout, "respawn", {
            boxes.selected.do { arg obj;
                if(obj.isKindOf(MxUnit), {
                    obj.respawn(1);
                })
            }
        });
        ActionButton(layout, "+filter", {
            var subject, unit, spec;
            if(boxes.selected.size == 1, {
                subject = boxes.selected.first;
                // unit
                if(subject.isKindOf(MxUnit), {
                    unit = subject;
                    subject = subject.outlets.first
                }, {
                    if(subject.isKindOf(MxOutlet), {
                        unit = subject.unit
                    });
                });
                if(subject.isKindOf(MxOutlet), {
                    spec = subject.spec;
                    InstrBrowser({ arg layout, instr;
                        ActionButton(layout, "INSERT FILTER", {
                            var point, filter, newUnit, b, oldcables;
                            point = boxes.boxPointForUnit(unit);
                            if(model.at(point.x, point.y + 1).notNil, {
                                newUnit = model.insert(point.x, point.y + 1, instr)
                            }, {
                                newUnit = model.put(point.x, point.y + 1, instr)
                            });
                            // new needs to be prepared because no patchOut to connect to
                            if(model.isPlaying, {
                                b = MixedBundle.new;
                                model.update(b);
                            });
                            // connect old to filter
                            oldcables = model.cables.fromOutlet(subject);
                            model.connect(unit, subject, newUnit, newUnit.getInlet( instr.specs.detectIndex({ arg sp, i; sp == spec }) ) );
                            // connect filter to what the unit was connected to
                            oldcables.do { arg cable;
                                var outSpec;
                                model.disconnectCable(cable);
                                // might not work, assuming first outlet
                                // newUnit not playing or prepared !
                                if(b.notNil, {
                                    model.connect(newUnit, newUnit.getOutlet(0), cable.inlet.unit, cable.inlet );
                                    model.update(b);
                                }, {
                                    model.connect(newUnit, newUnit.getOutlet(0), cable.inlet.unit, cable.inlet );
                                })
                            };
                            if(b.notNil, {
                                model.update(b);
                                b.send(model.server);
                            });
                            model.changed('grid');
                        })
                    }, nil, false).rate_(spec.rate).inputSpec_(spec).outputSpec_(spec).init.gui
                })
            })
        });
        if(\InspButton.asClass.notNil, {
            InspButton.icon(model, layout);
            // these will move into an MxAction
            ActionButton(layout, "Insp selected", {
                var inspMe, in, out, cable;
                inspMe = boxes.selected;
                if(boxes.selected.size == 2, {
                    out = boxes.selected.detect({ arg io; io.class === MxOutlet });
                    in = boxes.selected.detect({ arg io; io.class === MxInlet });
                    if(in.notNil and: out.notNil, {
                        cable = model.cables.detect({ arg cable; cable.inlet === in and: cable.outlet === out });
                        if(cable.notNil, {
                            inspMe = inspMe.add( cable );
                        });
                    })
                });
                inspMe.do(_.insp);
                InspManager.front;
            });
        });
    }

    guiBody { arg layout, bounds;
        var bb, updater;
        bounds = bounds ?? {layout.innerBounds.moveTo(0, 0)};
        bb = bounds.resizeBy(-200, 0);
        boxes = MxMatrixGui(model, layout, bb );
        boxes.transferFocus(0@0);
        this.drawer(layout, (bounds - bb).resizeTo(200, bounds.height));
        boxes.focus;

        updater = SimpleController(model);
        updater.put('grid', {
            boxes.refresh
        });
        updater.put('mixer', {
            boxes.refresh
        });
        layout.removeOnClose(updater)
    }

    drawer { arg layout, bounds;
        var d, doIt;
        doIt = { arg obj, placeIt;
            // which puts to master or channels
            placeIt.value(obj);
            boxes.refresh;
            if(model.isPlaying, {
                model.update;
            }, {
                model.updateAutoCables
            });
        };
        d = MxDrawer({ arg obj;
            var placeIt, fp;
            fp = boxes.focusedPoint;
            if(fp.notNil, {
                placeIt = {
                    boxes.put(fp.x, fp.y, obj);
                };
                if(obj.isKindOf(MxDeferredDrawerAction), {
                    obj.func = { arg obj; doIt.value(obj, placeIt) };
                }, {
                    doIt.value(obj, placeIt)
                })
            })
        });
        drawerGui = d.gui(layout, bounds);
    }
    keyDownResponder {
        ^boxes.keyDownResponder ++ drawerGui.keyDownResponder
    }
}


/*
    a deferred action has a dialog or something
    so it doesn't activate immediately
*/
MxDeferredDrawerAction {

    var <>func;

    value { arg object;
        ^func.value(object)
    }
}
