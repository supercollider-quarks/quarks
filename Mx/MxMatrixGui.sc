

MxMatrixGui : SCViewHolder {

    var <numRows, <numCols, masterCol;
    var <mx;
    var bounds, <focusedPoint;
    var <>currentDragPoint, draggingXY, draggingOutlet, mouseDownPoint, isDown=false;

    var <selected, hovering, dragging;
    var points;

    var <>ioHeight=10, <>faderHeight=80.0;

    var <>dragOn=4; // pixel distance to initiate drag or a symbol like \isCntrl
    var <>background, <styles, <defaultStyle;
    var pen, boxWidth, boxHeight, boxBounds, font;
    var plus, mainInlets, mainOutlets, mainPlus;

    *new { arg mx, w, bounds;
        ^super.new.init(mx, w, bounds);
    }

    init { arg argmx, w, argbounds;

        var skin;
        mx = argmx;
        skin = GUI.skin;
        pen = GUI.pen;
        font = Font(skin.fontSpecs.first, 9);
        defaultStyle = (
            font: font,
            fontColor: skin.fontColor,
            boxColor: skin.offColor,
            borderColor: skin.foreground,
            center: false
            );
        this.makeDefaultStyles(skin);

        this.calcNumRows;
        selected = [];
        boxWidth = 100.0;//bounds.width.asFloat / numCols;
        boxHeight = 50.0;//min( (bounds.height.asFloat - faderHeight) / numRows, 80 );

        // will leave room for mixer control at bottom
        bounds = argbounds ?? {Rect(20, 20, min(numCols * boxWidth, 1000), numRows * boxHeight + faderHeight + ioHeight + ioHeight)};
        bounds = bounds.asRect;
        bounds = Rect(bounds.left + 0.5, bounds.top + 0.5, bounds.width, bounds.height);

        if(w.isNil, {
            w = Window(mx.asString, bounds.resizeBy(40, 40).moveTo(10, 250) );
            w.front
        });
        Updater(mx, { arg mx, message;
            if(message == 'grid', {
                this.calcNumRows;
                this.updatePoints;
            });
            if(message == 'mixer', {
                this.refresh;
            });
        }).removeOnClose(w);

        bounds = Rect(bounds.left+1, bounds.top+1, bounds.width, bounds.height);
        view = UserView(w, bounds);

        //view.resize = 5;
        bounds = bounds.moveTo(0, 0); // my reference
        this.calcBoxBounds;
        this.updatePoints;

        view.focusColor = defaultStyle.borderColor;

        pen = GUI.pen;
        view.drawFunc = { this.drawGrid };

        // mouses
        view.mouseOverAction = { arg me, x, y, modifiers;
            this.mouseOver(x, y, modifiers)
        };
        view.mouseDownAction = { arg me, x, y, modifiers, buttonNumber, clickCount;
            mouseDownPoint = x@y;
            if(this.mouseDownIsDragStart(modifiers, x, y), {
                this.startDrag(x, y, modifiers, buttonNumber);
            }, {
                isDown = true;
                // select whatever is hit
                this.mouseDown(x, y, modifiers, buttonNumber, clickCount);
                this.view.refresh;
            });
        };
        view.mouseMoveAction = { arg me, x, y, modifiers;
            var outlet;
            if(this.isDragging(modifiers, x, y), {
                if(dragging.isNil, { // initiating drag now because it moved far enough
                    dragging = this.getByCoord(mouseDownPoint.x, mouseDownPoint.y);
                    if(dragging.class === MxInlet, { // not these
                        dragging = nil
                    });
                });
                draggingXY = x@y;
                this.view.refresh;
            });
            // else move fader
        };
        view.mouseUpAction = { arg me, x, y, modifiers;
            isDown = false;
            if(this.isDragging(modifiers, x, y), {
                this.endDrag(x, y, modifiers);
                this.view.refresh;
            });
        };

        // dragging on and off the matrix
        view.beginDragAction = { arg me;
            //this.handleByFocused('beginDragAction', []) ?? {this.focusedUnit}
        };
        view.canReceiveDragHandler = { arg me;
            //View.currentDrag.debug;
            focusedPoint.notNil
            // View.currentDrag
        };
        view.receiveDragHandler = { arg me;
            this.put( focusedPoint.x, focusedPoint.y, View.currentDrag );
            this.view.refresh;
        };


        // keys
        view.keyDownAction = this.keyDownResponder;

        view.keyUpAction = { arg me, char, modifiers, unicode, keycode;
            //this.handleByFocused('keyUpAction', [char, modifiers, unicode, keycode])
        };
        view.keyModifiersChangedAction = { arg me, char, modifiers, unicode, keycode;
            //this.handleByFocused('keyModifiersChangedAction', [char, modifiers, unicode, keycode])
        };
    }

    mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
        var obj;
        obj = this.getByCoord(x, y);
        // move fader, mute/solo button

        if(clickCount == 2, {
            if(obj.isKindOf(MxUnit), {
                obj.gui
            });
        }, {
            // click to select
            if(modifiers.isShift, {
                if(obj.notNil, {
                    selected.remove(obj) ?? { selected = selected.add(obj) }
                })
            }, {
                if(obj.notNil, {
                    selected = [obj]
                }, {
                    selected = []
                })
            });
            focusedPoint = this.boxPoint(x, y)
        });
    }
    mouseOver { arg x, y, modifiers;
        var obj, unit;
        hovering = this.getByCoord(x, y);
    }
    keyDownResponder {
        // probably move to MxGui
        var k, default;
        default = 0@0;
        k = KeyResponder.new;
        k.register('backspace', false, false, false, false, {
            selected.do { arg obj;
                if(obj.class === MxOutlet, {
                    // all cables going to or from this
                    mx.disconnectOutlet(obj)
                }, {
                    if(obj.class === MxInlet, {
                        mx.disconnectInlet(obj)
                    }, {
                        if(obj.class === MxUnit, {
                            mx.removeUnit(obj)
                        });
                    })
                })
            };
            mx.update;
            this.refresh;
        });

        k.register('up', false, false, false, false, {
            var p;
            p = (focusedPoint ? default);
            focusedPoint = Point(p.x, max(p.y - 1, 0));
            this.refresh;
        });

        k.register('down', false, false, false, false, {
            var p;
            p = (focusedPoint ? default);
            focusedPoint = Point(p.x, p.y + 1);
            this.refresh;
        });

        k.register('left', false, false, false, false, {
            var p;
            p = (focusedPoint ? default);
            focusedPoint = Point( max(p.x - 1, 0), p.y);
            this.refresh;
        });

        k.register('right', false, false, false, false, {
            var p;
            p = (focusedPoint ? default);
            focusedPoint = Point(p.x + 1, p.y);
            this.refresh;
        });

        // VOLUMES
        k.register('up', true, false, false, false, {
            var chan;
            if(focusedPoint.notNil, {
                chan = mx.channels.at(focusedPoint.x)     ?? { if(focusedPoint.x == masterCol, {mx.master}, nil) };
                if(chan.notNil, {
                    chan.fader.db = chan.fader.db + 1.0;
                    mx.changed('mixer');
                });
            }, {
                selected.do { arg obj;
                    if(obj.isKindOf(MxChannel), {
                        obj.fader.db = obj.fader.db + 1.0
                    })
                };
                mx.changed('mixer');
            });
        });
        k.register('down', true, false, false, false, {
            var p, chan;
            if(focusedPoint.notNil, {
                chan = mx.channels.at(focusedPoint.x)     ?? { if(focusedPoint.x == masterCol, {mx.master}, nil) };
                if(chan.notNil, {
                    chan.fader.db = chan.fader.db - 1.0;
                    mx.changed('mixer');
                });
            }, {
                selected.do { arg obj;
                    if(obj.isKindOf(MxChannel), {
                        obj.fader.db = obj.fader.db + 1.0
                    })
                };
                mx.changed('mixer');
            });
        });
        //  m
        k.register(   $m  ,   false, false, false, false, { arg view;
            var chan;
            if(view.isKindOf(GUI.userView) and: {focusedPoint.notNil}, {
                mx.mute(focusedPoint.x);
                mx.changed('mixer');
            });
        });
        //  s
        k.register(   $s  ,   false, false, false, false, { arg view;
            var chan;
            if(view.isKindOf(GUI.userView) and: focusedPoint.notNil, {
                mx.solo(focusedPoint.x);
                mx.changed('mixer');
            })
        });
        ^k
    }
    // internal dragging
    endDrag { arg x, y, modifiers;
        // patch it
        var target, targetPoint, dp, fi;
        targetPoint = this.boxPoint(x, y);
        target = this.getByCoord(x, y);
        if(target.notNil, {
            if(dragging.isKindOf(MxOutlet) and: {target.isKindOf(MxInlet)} and: {dragging !== target}, {
                if(modifiers.isShift.not, {
                    mx.disconnectOutlet(dragging)
                });
                mx.connect(nil, dragging, nil, target);
                mx.update;
                currentDragPoint = nil;
                this.transferFocus(targetPoint);
                dragging = nil;

                ^this
            })
        });
        // to a unit
        if(dragging.isKindOf(MxUnit) and: {target.isKindOf(MxChannel).not}, {
            // move it, copy it, replace it
            dp = points[dragging];
            if(modifiers.isAlt, {
                mx.copy( this.asChannelIndex(dp.x), dp.y,
                    this.asChannelIndex(targetPoint.x), targetPoint.y );
            }, {
                mx.move(this.asChannelIndex(dp.x), dp.y,
                    this.asChannelIndex(targetPoint.x), targetPoint.y)
            });
            mx.update;
        }, {
            // dragging an outlet to a fader
            if(dragging.isKindOf(MxOutlet), {
                fi = this.detectFader(x@y);
                if(fi.notNil, {
                    if(fi >= mx.channels.size, {
                        mx.extendChannels(fi)
                    });
                    mx.connect(nil, dragging, nil, mx.channels[fi].myUnit.inlets.first);
                    mx.update;
                });
            })
        });
        currentDragPoint = nil;
        this.transferFocus(targetPoint);
        dragging = nil;
    }
    startDrag { arg x, y;
        dragging = this.getByCoord(x, y);
        if(dragging.notNil, {
            this.transferFocus(this.boxPoint(x, y));
        });
        draggingXY = x@y;
        this.view.refresh;
    }
    asChannelIndex { arg x;
        ^if(x == masterCol, inf, x)
    }
    focusedUnit {
        ^focusedPoint !? {
            this.getUnit(focusedPoint)
        }
    }
    transferFocus { arg toBoxPoint;
        focusedPoint = toBoxPoint
    }

    refresh {
        view.refresh
    }
    calcNumRows {
        numCols = mx.channels.size + 1;
        numRows = mx.channels.maxValue({ arg ch; ch.units.size }) ? 0;
        numRows = max(numRows, mx.master.units.size) + 1;
    }
    updatePoints {
        var lastRow, bounds;
        points = IdentityDictionary.new;
        bounds = view.bounds;
        lastRow = (boxBounds.height / boxHeight).floor - 1;
        masterCol = (bounds.width / boxWidth).floor - 1;
        mx.channels.do { arg ch, ci;
            ch.units.do { arg un, ri;
                if(un.notNil) {
                    points[un] = ci@ri;
                }
            };
            // or should it put the fader there ?
            points[ch.myUnit] = ci@(lastRow + 1);
        };
        // should be all outlets
        mx.master.units.do { arg un, ri;
            if(un.notNil) {
                points[un] = masterCol@ri
            }
        };
        // or master fader
        points[mx.master.myUnit] = masterCol@(lastRow + 1)
    }
    calcBoxBounds {
        var n;
        boxBounds = Rect(bounds.left,
                        bounds.top + ioHeight,
                        view.bounds.width,
                        view.bounds.height - faderHeight - ioHeight - ioHeight);
        n = boxBounds.width div: 100;
        boxWidth = boxBounds.width / n;
    }
    fadersBounds {
        ^Rect(0, boxBounds.bottom, boxBounds.width, faderHeight)
    }
    getFaderBounds { arg chani;
        ^Rect(chani * boxWidth, boxBounds.bottom, boxWidth, faderHeight)
    }
    detectFader { arg p; // which fader is the point inside of ?
        var fb;
        fb = this.fadersBounds;
        if(fb.containsPoint(p), {
            // return nil if past numCols
            // how is master expressed ?
            ^(p.x / boxWidth).asInteger
        });
        ^nil
    }
    boxPoint { arg x, y;// view coords
        var col, row, p;
        p = x@y;
        if(boxBounds.containsPoint(p).not, {
            ^nil
        });
        p = p - boxBounds.origin;
        col = this.intFloor( p.x.asFloat / boxWidth );
        row = this.intFloor( p.y.asFloat / boxHeight );
        ^col@row
    }

    intFloor { arg i; // a bandaid
        if(i.frac.equalWithPrecision(1.0), {
            ^i.round.asInteger
        }, {
            ^i.floor.asInteger
        })
    }

    getUnit { arg boxPoint;
        if(boxPoint.x == masterCol, {
            ^mx.master.units[boxPoint.y]
        }, {
            ^mx.at(boxPoint.x, boxPoint.y)
        });
    }
    boxPointForUnit { arg unit;
        ^points[unit]
    }
    getUnitFromCoords { arg x, y; // view coords
        ^this.getUnit(this.boxPoint(x, y) ?? {^nil});
    }
    getByCoord { arg x, y;
        // what got hit at x@y ?
        // outlet, inlet, box, fader
        var unit, bp, oi, ioArea, b, iolets, p, fi;
        p = x@y;
        bp = this.boxPoint(x, y);
        if(bp.notNil, {
            unit = this.getUnit(bp);
            if(unit.isNil, {
                ^nil
            });
            b = this.getBounds(bp);
            // outlet hit
            if(unit.outlets.size > 0, {
                ioArea = this.outletsArea(b);
                if(ioArea.containsPoint(p), {
                    ^this.findIOlet(unit.outlets, ioArea, p)
                })
            });
            // inlet hit
            if(unit.inlets.size > 0, {
                ioArea = this.inletsArea(b);
                if(ioArea.containsPoint(p), {
                    ^this.findIOlet(unit.inlets, ioArea, p)
                })
            });
            ^unit
        }, {
            // fader. returns the channel
            // should return the channels input
            fi = this.detectFader(p);
            if(fi.notNil, {
                ^mx.channels.at(fi) ?? { if(fi == masterCol, {mx.master}, nil) }
            });
        });
        // + in top left
        if(plus.contains(p), {
            ^nil // not yet
        });

        // top level inlets
        if(mx.inlets.size > 0, {
            if(p.y < ioHeight) {
                ^this.findIOlet( mx.inlets, Rect(0, 0, bounds.width, ioHeight), p )
            }
        });
        // top level outlets
        if(mx.outlets.size > 0, {
            if(p.y >= (bounds.bottom - ioHeight), {
                ^this.findIOlet( mx.outlets, Rect(0, bounds.bottom - ioHeight, bounds.width, ioHeight), p )
            });
        });
        ^nil
    }
    put { arg x, y, obj;
        if(x == masterCol, {
            // want to use app and do a
            // replaceWith
            mx.putMaster( y, obj );
        }, {
            mx.put( x, y, obj );
        });
    }
    findIOlet { arg iolets, ioArea, screenPoint;
        // inside an iolet area find which one the point is on
        var oi;
        oi = ((screenPoint.x - ioArea.left).asFloat / (ioArea.width.asFloat / iolets.size)).floor.asInteger;
        ^iolets[oi]
    }

    getBounds { arg boxPoint;
        // x is col
        // y is row
        ^Rect(boxPoint.x * boxWidth, boxPoint.y * boxHeight + boxBounds.top, boxWidth, boxHeight)
    }
    mouseDownIsDragStart { arg modifiers, x, y;
        if(dragOn.isNumber, {
            // wait for mouse move to confirm
            ^false
        }, {
            // immediate pick up
            ^modifiers.perform(dragOn);
        })
    }
    isDragging { arg modifiers, x, y;
         if(dragOn.isNumber, {
             if(mouseDownPoint.notNil, {
                 if((x@y).dist(mouseDownPoint) > dragOn, {
                     //mouseDownPoint = nil;
                     ^true
                }, {
                    ^false
                })
            }, {
                ^currentDragPoint.notNil
            })
        }, {
            ^modifiers.perform(dragOn)
        })
    }
    outletsArea { arg rect;
        ^Rect.newSides( rect.left, rect.bottom - ioHeight, rect.right, rect.bottom)
    }
    inletsArea { arg rect;
        ^Rect( rect.left, rect.top, rect.width, ioHeight)
    }
    ioArea { arg iosArea, i, iowidth;
        ^Rect(iosArea.left + (iowidth * i), iosArea.top, iowidth, ioHeight)
    }
    inletArea { arg inlet;
        var p, b, r;
        p = points[inlet.unit] ?? {^nil};
        b = this.getBounds(p);
        r = this.inletsArea(b);
        ^this.ioArea( r , inlet.index, r.width.asFloat / inlet.unit.inlets.size )
    }
    outletArea { arg outlet;
        var p, b, r;
        p = points[outlet.unit] ?? {^nil};
        b = this.getBounds(p);
        r = this.outletsArea(b);
        ^this.ioArea( r , outlet.index, r.width.asFloat / outlet.unit.outlets.size )
    }
    drawIOlets { arg ioarea, lets;
        var iowidth;
        iowidth = ioarea.width.asFloat / lets.size;
        lets.do { arg outlet, i;
            var or;
            or = this.ioArea(ioarea, i, iowidth);
            this.drawIOletBox(or, outlet.spec.color, selected.includes(outlet), outlet.name.asString);
        }
    }
    drawIOletBox { arg rect, color, isSelected, title, center=false;
        pen.color = color;
        pen.fillRect(rect.insetBy(1, 1));
        if(isSelected, {
            pen.color = styles['selected']['borderColor']
        }, {
            pen.color = Color(0.64179104477612, 0.64179104477612, 0.64179104477612, 0.5134328358209);
        });
        pen.strokeRect(rect);
        pen.color = Color.black;
        pen.use {
            pen.font = font;
            if(center, {
                pen.stringCenteredIn(title, rect.insetBy(1, 1))
            }, {
                pen.stringLeftJustIn(title, rect.insetBy(1, 1))
            })
        }
    }
    drawGrid {
        var d, box, style, r, fb;

        Pen.capStyle = 1;
        Pen.joinStyle = 1;

        d = { arg rect, unit, styleName, boxPoint, blown=false;
            var style, styleNames, name, ioarea, iowidth;
            // cascade styles: defaultStyle + box style + box's set styles (playing, selected) + temp style (down, focused)
            style = defaultStyle.copy;
            if(unit.notNil, {
                styleNames = ['unit'];
                name = unit.name
            }, {
                styleNames = [];
            });
            if(styleName.notNil, {
                styleNames = styleNames.add(styleName)
            });
            styleNames.do { arg sn;
                styles[sn].keysValuesDo { arg k, v;
                    style[k] = v.value(style[k], unit)
                }
            };
            if(blown, {
                pen.color = Color.red;
            }, {
                pen.color = style['boxColor'];
            });
            pen.fillRect( rect );

            // or is the fader in selected
            pen.color = style['borderColor'];
            selected.any { arg thing;
                var sel;
                sel = (thing === unit or: {thing.isKindOf(MxChannel) and: {thing.myUnit === unit}});
                if(sel, {
                    pen.color = styles['selected']['borderColor'];
                });
                sel
            };
            pen.strokeRect( rect );

            if(unit.notNil, {
                // central box draw writes the name
                unit.draw(pen, rect, style);

                // outlets
                if(unit.outlets.size > 0, {
                    this.drawIOlets(this.outletsArea(rect), unit.outlets);
                });
                if(unit.inlets.size > 0, {
                    this.drawIOlets(this.inletsArea(rect), unit.inlets);
                });
            });
        };

        pen.width = 1;
        pen.color = background;
        pen.fillRect(bounds); // background fill

        // only need this on resize
        this.calcBoxBounds;

        points.keysValuesDo { arg unit, p;
            var r, blown=false;
            if(unit.source.class === MxChannel, {
                r = this.getFaderBounds(p.x);
                blown = unit.source.fader.fuseBlown;
            }, {
                r = this.getBounds(p);
            });
            d.value(r, unit, nil, p, blown);
        };
        plus = Rect(bounds.left, bounds.top, boxWidth, ioHeight);
        mainInlets = Rect(bounds.left, bounds.top, bounds.width, ioHeight);
        mainPlus = Rect(bounds.left, 0, boxWidth, ioHeight).bottom_(bounds.bottom);
        mainOutlets = Rect(bounds.left + boxWidth, bounds.top, bounds.width - boxWidth, ioHeight).bottom_(bounds.bottom);

        this.drawIOletBox(plus, Color(0.0, 0.23880597014925, 1.0, 0.35820895522388), false, "+", true );

        // main inlets / outlets
        this.drawIOlets(mainInlets, mx.inlets);
        this.drawIOlets(mainOutlets, mx.outlets);

        // draw focused on top so border style wins out against neighbors
        if(focusedPoint.notNil, {
            if(isDown) {
                style = 'down'
            } {
                style = 'focused'
            };
            d.value(this.getBounds(this.focusedPoint), mx.at(this.focusedPoint.x, this.focusedPoint.y), style, this.focusedPoint );
        });

        // show:
        // inlets that can accept what you are dragging
        // hovering

        fb = this.fadersBounds;
        mx.cables.do { arg cable, i;
            var f, t, c, chan, ci, fcenter, tcenter;

            f = this.outletArea(cable.outlet);

            // if its to a MxChannel then draw to fader top
            chan = cable.inlet.unit.source;
            if(chan.class !== MxChannelInput, { // no messy cables to master channel input
                if(chan.class === MxChannel, { // going to the fader box
                    if(chan === mx.master, {
                        t = Rect(fb.right - boxWidth, fb.top, boxWidth, ioHeight)
                    }, {
                        t = Rect(mx.channels.indexOf(chan) * boxWidth, fb.top, boxWidth, ioHeight)
                    });
                }, {
                    t = this.inletArea(cable.inlet);
                });

                if(f.notNil and: t.notNil, {
                    c = cable.outlet.spec.color;
                    if(cable.active.not, {
                        c = Color(c.red, c.green, c.blue, 0.2)
                    }, {
                        if(cable.inlet.unit.source === mx.master, {
                            c = Color(c.red, c.green, c.blue, 0.1)
                        }, {
                            c = Color(c.red, c.green, c.blue, 0.6)
                        })
                    });
                    pen.color = Color(alpha:0.5);
                    pen.width = 3;
                    fcenter = f.center;
                    tcenter = t.center;
                    pen.moveTo(fcenter);
                    pen.lineTo(tcenter);
                    pen.stroke;
                    pen.color = c;
                    pen.width = 1;
                    pen.moveTo(fcenter);
                    pen.lineTo(tcenter);
                    pen.stroke;
                })
            })
        };

        if(dragging.notNil, {
            if(dragging.isKindOf(MxUnit), {
                d.value(
                    Rect(draggingXY.x, draggingXY.y + boxBounds.top, boxWidth, boxHeight)
                        .moveBy((boxWidth / 2).neg, (boxHeight / 2).neg),
                      dragging,
                      'dragging',
                      draggingXY
                  )
            }, {
                // outlet
                pen.color = dragging.spec.color;
                pen.width = 2;
                r = Rect(draggingXY.x, draggingXY.y, 4, 4).moveBy(ioHeight.neg / 2, ioHeight.neg / 2);
                pen.fillOval( r );
                pen.color = Color.blue;
                pen.strokeOval( r );
            });
        });
    }
    makeDefaultStyles { arg skin;
        background = Color(0.85236548378624, 0.86567164179104, 0.82691022499443); //skin.background;
        /*
            fontSpecs:        ["Helvetica", 10],
            fontColor:        Color.black,
            background:        Color(0.8, 0.85, 0.7, 0.5),
            foreground:     Color.grey(0.95),
            onColor:        Color(0.5, 1, 0.5),
            offColor:        Color.clear,
            gap:            0 @ 0,
            margin:            2@2,
            boxHeight:        16
            */

        styles = IdentityDictionary.new;
        styles['focused'] = (
            borderColor: Color(0.37100213219616, 0.68900395979287, 0.86567164179104, 0.87313432835821)
            );
        styles['over'] = (
            boxColor: { |c| c.saturationBlend(Color.black, 0.3) }
            );
        styles['loading'] = (
            boxColor: { |c| Color.yellow }
            );
        styles['down'] = (
            boxColor: Color(0.37100213219616, 0.68900395979287, 0.86567164179104, 0.37313432835821),
            borderColor: skin.foreground
            );
        styles['dragging'] = (
            boxColor: { arg c; c.blend(Color.blue(alpha:0.2), 0.8) },
            borderColor: Color.blue(alpha:0.5)
            );

        styles['deactivated'] = (
            fontColor: { |c| c.alpha_(0.2) },
            boxColor: { |c| c.alpha_(0.2) },
            borderColor: { |c| c.alpha_(0.2) }
            );
        styles['selected'] = (
            borderColor: Color(0.24258303049245, 0.23167743372689, 0.59701492537313),
            boxColor: Color(0.37100213219616, 0.68900395979287, 0.86567164179104, 0.37313432835821)
            );
        styles['unit'] = (
            boxColor: { arg c; c.darken(Color(0.2202380952381, 0.40008503401361, 0.5)) },
            borderColor: Color(0.64179104477612, 0.64179104477612, 0.64179104477612, 0.5134328358209)
                //Color(0.40298507462687, 0.40298507462687, 0.40298507462687)
            );
    }

    // rearranging
    /*
      copy { arg fromBoxPoint, toBoxPoint;
        var box;
        box = this.getBox(fromBoxPoint).copy;
        box.point = toBoxPoint;
        boxes.put(toBoxPoint, box);
      }
      clear { arg point;
        boxes.removeAt(point);
      }
      clearAll {
          boxes = Dictionary.new;
      }
      move { arg fromBoxPoint, toBoxPoint;
        if(fromBoxPoint != toBoxPoint, {
            this.copy(fromBoxPoint, toBoxPoint);
            this.clear(fromBoxPoint);
        });
      }
      swap { arg fromBoxPoint, toBoxPoint;
        var tmp;
        tmp = this.getBox(toBoxPoint).copy;
        this.copy(fromBoxPoint, toBoxPoint);
        tmp.point = toBoxPoint;
        boxes[toBoxPoint] = tmp;
      }
    */

    // the key responders are passed to the FOCUSED box
    // box, char, modifiers, unicode, keycode
    keyUpAction_ { arg func;
        //this.setHandler('keyUpAction', func)
    }
    // box, char, modifiers, unicode, keycode
    keyDownAction_ { arg func;
        //this.setHandler('keyDownAction', func)
    }
    // box, modifiers
    keyModifiersChangedAction_ { arg func;
        //this.setHandler('keyModifiersChangedAction', func)
    }
}

/*
        addKeyHandler(function, keycode, shift, cntl, alt, cmd, caps, numPad, fun)
        addUnicodeHandler(function, keycode, shift, cntl, alt, cmd, caps, numPad, fun)
        navByArrows
*/
