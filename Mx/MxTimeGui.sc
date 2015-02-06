

MxTimeGui : ObjectGui {

    var <from, <to, <maxTime, zoomCalc, playZoomCalc;
    var xScale;
    var <>laneHeight=150;
    var zoom, timeRuler, updater, units;

    guiBody { arg parent;
        var i = 0, width, focusColor, kdr, makeSidebar;
        var sidebarSize = 100, buttonHeight = GUI.skin.buttonHeight, gap=GUI.skin.gap.x, currenty;

        parent.startRow;
        width = parent.indentedRemaining.width;
        focusColor = GUI.skin.focusColor ?? {Color.blue(alpha:0.4)};

        kdr = this.keyDownResponder;

        makeSidebar = { arg side, main;
            var s, m, maxUsedHeight, b;
            var lane;

            lane = parent.comp({ arg l;
                s = l.vert({ arg s;
                        side.value(s)
                }, Rect(0, 0, sidebarSize, laneHeight));

                m = FlowView(l, Rect(sidebarSize+gap, 0, width - sidebarSize - gap, laneHeight), 0@0, 0@0);
                main.value(m);
            }, Rect(0, 0, width, laneHeight));

            m.resizeToFit(true);

            maxUsedHeight = s.children.sum({arg c; c.bounds.height });
            maxUsedHeight = max(maxUsedHeight, m.bounds.height);

            s.bounds = s.bounds.height_(maxUsedHeight);
            lane.bounds = lane.bounds.height_(maxUsedHeight).top_(currenty);
            currenty = currenty + maxUsedHeight + gap;
        };

        maxTime = (model.endBeat ?? {model.beatDuration} ? 480) + 8;
        SynthConsole(model, parent).play.stop.tempo;
        CXLabel(parent, "Last beat:");
        NumberEditor(maxTime, [0, 10000].asSpec).action_({ arg num;
            this.maxTime = num.value;
            timeRuler.refresh;
            this.zoom(0, maxTime, true);
        }).smallGui(parent);
        ActionButton(parent, "Rec to disk...", {
            model.record(endBeat:maxTime);
        }).background_(Color(0.76119402985075, 0.0, 0.0, 0.92537313432836));

        parent.startRow;
        zoomCalc = ZoomCalc([0, maxTime], [0, width]);
        playZoomCalc = ZoomCalc([0, maxTime], [0, 1.0]);

        currenty = parent.view.decorator.top;

        // zoom controls
        makeSidebar.value({ arg s;
            ActionButton(s, "<-zoom->", {this.zoom(0, maxTime, true)})
        }, { arg m;
            zoom = RangeSlider(m, m.innerBounds.width@buttonHeight);
        });
        zoom.lo = 0.0;
        zoom.hi = 1.0;
        zoom.action = {this.zoom((zoom.lo * maxTime).round(4), (zoom.hi * maxTime).round(4), false)};
        zoom.knobColor = Color.black;
        zoom.background = Color.white;
        zoom.keyDownAction = kdr;
        zoom.focusColor = focusColor;

        makeSidebar.value({ arg s;
            // goto start
                ActionButton(s, "|<", {model.gotoBeat(0, 1)})
            },
            { arg m;
                timeRuler = TimeRuler(m, Rect(0, 0, m.innerBounds.width, buttonHeight * 2), maxTime);
            });
        timeRuler.keyDownAction = kdr;
        timeRuler.mouseDownAction = { arg beat, modifiers, buttonNumber, clickCount;
            model.gotoBeat( beat.trunc(4)  )
        };
        timeRuler.shiftSwipeAction = { arg start, end;
            this.zoom(start, end, true)
        };
        this.prSetFromTo(0.0, maxTime);

        updater = Updater(model.position, { arg pos;
            {
                if(timeRuler.isClosed.not, {
                    timeRuler.position = pos.current;
                })
            }.defer
        }).removeOnClose(parent);

        units = [];
        model.channels.do { arg chan, ci;
            chan.units.do { arg unit;
                if(unit.notNil and: {unit.handlers.at('timeGui').notNil}, {
                    makeSidebar.value({ arg s;
                        // hide/show
                        // record enable
                        // gui
                        // ActionButton(s, "gui", {unit.gui});
                        if(unit.canRecord, {
                            ToggleButton(s, "Record", {unit.record(true)}, {unit.record(false)},
                                false, 20, nil, Color.red, Color.yellow);
                        });
                    }, { arg v;
                        unit.timeGui(v, v.bounds, maxTime);
                        units = units.add(v);
                    })
                })
            }
        };
    }
    prSetFromTo { arg argFrom, argTo;
        from = argFrom;
        to = argTo;
        zoomCalc.setZoom(from, to);
        playZoomCalc.setZoom(from, to);
        timeRuler.setZoom(from, to);
    }
    zoom { arg argFrom, argTo, updateZoomControl=true;
        this.prSetFromTo(argFrom, argTo);
        model.allUnits.do { arg unit;
            if(unit.handlers.at('zoomTime').notNil, {
                unit.zoomTime(from, to);
            })
        };
        if(updateZoomControl, {
            zoom.setSpan( from / maxTime, to / maxTime )
        })
    }
    zoomBy { arg percentage, round=4.0; // 0 .. 2
        // zoom up or down, centered on the current middle
        var middle, newrange, newfrom, newto;
        middle = (to - from) / 2.0;
        newto = to - middle * percentage + middle;
        newfrom = middle - (middle - from * percentage);
        newfrom = newfrom.clip(0, maxTime - round);
        newto = newto.clip(newfrom + round, maxTime);
        newfrom = newfrom.round(round);
        newto = newto.round(round);
        this.zoom(newfrom, newto);
    }
    moveBy { arg percentage, round=4.0; // -1 .. 1
        // move by a percentage of current shown range, rounded to nearest bar or beat
        var newfrom, newto, range, stepBy;
        range = (to - from);
        stepBy = range * percentage;
        stepBy = stepBy.round(round);
        newfrom = from + stepBy;
        newfrom = newfrom.clip(0.0, maxTime - range).round(round);
        newto = newfrom + range;
        this.zoom(newfrom, newto);
    }
    maxTime_ { arg mt;
        model.endBeat = mt;
        maxTime = mt;
        zoomCalc.modelRange = [0, maxTime];
        playZoomCalc.modelRange = [0, maxTime];
        timeRuler.maxTime = maxTime;
        model.allUnits.do { arg unit;
            unit.callHandler('setMaxTime', maxTime)
        };
    }
    keyDownResponder {
        var k, default;
        default = 0@0;
        k = KeyResponder.new;
        k.register(   \up  ,   false, false, false, false, {
            this.zoomBy(1.1)
        });
        k.register(   \down  ,   false, false, false, false, {
            this.zoomBy(0.9)
        });
        //  shift control \up
        k.register(   \up  ,   true, false, false, true, {
            this.zoomBy(1.01, 1)
        });
        //  shift control
        k.register(   \down  ,   true, false, false, true, {
            this.zoomBy(0.99, 1)
        });
        k.register(   \left  ,   false, false, false, false, {
            this.moveBy(-0.1)
        });
        k.register(   \right  ,   false, false, false, false, {
            this.moveBy(0.1)
        });
        //  shift control
        k.register(   \left  ,   true, false, false, true, {
            this.moveBy(-0.01, 1)
        });
        //  shift control
        k.register(   \right  ,   true, false, false, true, {
            this.moveBy(0.01, 1)
        });
        ^k
    }
    writeName {}
    //background { ^Color(0.81176470588235, 0.80392156862745, 0.79607843137255) }
    background { ^Color.clear }
    guify { arg parent, bounds, title;
        var mine, w;
        mine = parent.isNil;
        w = super.guify(parent, bounds, title ? "Timeline");
        ^w
    }
}
