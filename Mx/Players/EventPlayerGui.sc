

EventListPlayerGui : AbstractPlayerGui {

    var tg, zoomCalc, rs, hitAreas, selected, mouseDownPoint;
    var manager;

    guiBody { arg parent, bounds;
        // zoom control if top
        // test buttons to click each one
        ToggleButton(parent, "debug", { model.verbose = true }, { model.verbose = false }, model.verbose);
        ActionButton(parent, "free children", {
            model.freeAll
        });
        parent.startRow;
        this.timeGui(parent, bounds ?? {Rect(0, 0, parent.bounds.width, 100)});
        // needs a list view
    }
    timeGui { arg parent, bounds, maxTime;
        tg = UserView(parent, bounds);
        if(maxTime.isNil, {
            maxTime = model.beatDuration;
            if(maxTime.isNil, {
                maxTime = 128
            }, {
                maxTime = (model.beatDuration * 1.1 + 0.1).ceil;
            })
        });
        zoomCalc = ZoomCalc([0, maxTime], [0, bounds.width]);
        if(\UserViewObjectsManager.asClass.notNil, {
            manager = UserViewObjectsManager(tg, bounds);
            manager.bindAll;
            manager.onDoubleClick = { arg obj;
                Editor.for(obj).gui
            };
            manager.onMoved = { arg obj, by;
                var r, pixelPos, beat;
                pixelPos = zoomCalc.modelToDisplay(obj['beat']) + by.x;
                beat = zoomCalc.displayToModel(pixelPos);
                obj[\beat] = beat;
                this.updateTimeGui;
            };
            manager.onCopy = { arg obj, by;
                var nobj;
                nobj = obj.copy;
                nobj['beat'] = obj['beat'] + by;
                model.addEvent(nobj);
            };
            manager.onDelete = { arg obj;
                model.removeEvent(obj);
            };
            manager.onDoubleClick = { arg obj, p, modifiers;
                if(modifiers.isCmd, {
                    DictionaryEditor(obj).gui(nil, nil, { arg ev;
                        var beatChanged = ev['beat'] != obj['beat'];
                        ev.keysValuesDo { arg k, v;
                            obj.put(k, v)
                        };
                        if(beatChanged, {
                            model.schedAll
                        })
                    });
                }, {
                    model.playEvent(obj)
                })
            };
            // control would be mute it

            this.updateTimeGui;
            tg.drawFunc = manager;
        }, {
            "UserViewObjectsManager from crucialviews quark required for the EventListPlayer timeline gui".inform;
        });
    }
    updateTimeGui {
        var h, black;
        h = tg.bounds.height;
        black = Color.black;
        model.events.do { arg ev, i;
            var x, r, rs, endBeat, pixelStart, pixelEnd, remove=true;
            if(ev['beat'].notNil, {
                endBeat = ev['beat'] + (ev['dur'] ? 1);
                pixelStart = zoomCalc.modelToDisplay(ev['beat']);
                pixelEnd = zoomCalc.modelToDisplay(endBeat);
                if(pixelStart.notNil and: {pixelEnd.notNil}, { // on screen

                    r = Rect(0, 0, pixelEnd - pixelStart, h);
                    rs = PenCommandList.new;
                    rs.add(\color_, Color.green);
                    rs.add(\addRect, r);
                    rs.add(\draw, 3);
                    rs.add(\color_, black);
                    rs.add(\stringCenteredIn, i.asString, r);

                    if(manager.objects[ev].isNil, {
                        manager.add(ev, rs, r.moveTo(pixelStart, 0));
                        remove = false;
                    }, {
                        manager.setBounds(ev, r.moveTo(pixelStart, 0));
                        manager.objects[ev].renderFunc = rs;
                        remove = false;
                    })
                });
            });
            if(remove, {
                manager.remove(ev)
            })
        };
    }
    setZoom { arg from, to;
        zoomCalc.setZoom(from, to);
    }
    update {
        if(\UserViewObjectsManager.asClass.notNil, {
            this.updateTimeGui;
        });
        tg.refresh
    }
}


InstrEventListPlayerGui : EventListPlayerGui {

    writeName { arg parent;
        super.writeName(parent);
        this.addEventButton(parent)
    }
    addEventButton { arg parent;
        ActionButton(parent, "+", {
            this.addEventDialog(blend(zoomCalc.zoomedRange[0], zoomCalc.zoomedRange[1], 0.5).round(1))
        });
    }
    addEventDialog { arg beat;
        InstrBrowser({ arg parent, instr;
            var patch, beatEditor, playingPatch, up;
            patch = Patch(instr);
            patch.gui(parent);
            parent.startRow;
            ToggleButton(parent, "test", {
                playingPatch = model.playEvent(patch.asEvent);
                // Updater(playingPatch
                // watch for it to die
                // should just have an onFree binding
            }, {
                playingPatch.stop.free
            });
            ActionButton(parent, "RND", {
                patch.rand
            });
            beatEditor = NumberEditor(beat, [0, min(model.beatDuration ? 128, 128) + 512 ]);
            CXLabel(parent, "At beat");
            beatEditor.gui(parent);
            ActionButton(parent, "Insert event", {
                var e;
                e = patch.asEvent;
                e[\beat] = beatEditor.value;
                model.addEvent(e);
                model.changed(\didAddEvent);
            });
            parent.hr;
        }).gui
    }
}
