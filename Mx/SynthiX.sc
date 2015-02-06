
/*
    an EMS Synthi A style patchbay for connecting things in Mx

    aka "I sunk your battleship"

    http://en.wikipedia.org/wiki/EMS_Synthi_A
    http://www.thesynthi.de/data/SYNTHIA/A2011_1.jpg

    synthi pegs are red yellow white : varying resistance

    outlets and inlets should be iterables, preferrably MxQuery
    this will be especially interesting when MxQuery can do lazy evaluation and thus the patchbay will update dynamically.

    sorts by execution order
        thus for synths everything above the diagonal
        is a precendence error

    control-clicking on the inlets column headers will set the value if possible
        so a synth type input can be set by clicking to see what it sounds like to wiggle it.

    the outlets row headers show the output value if possible
        ie. for things that have a simple float value like a CC or spline
*/


SynthiX {

    var <>outlets, <>inlets;
    var uv, pen;
    var <>labelSize = 100, <>on, <>off, <>cant, <>font, bigFont;
    var black, blue, faint, background;
    var gridRect, width, height;
    var ins, outs;
    var <updateRate=0.5, animator;
    var drawInlets=true, drawOutlets=true, drawGrid=true;

    *new { arg outlets, inlets;
        ^super.newCopyArgs(outlets, inlets)
    }
    gui { arg parent, bounds;
        if(parent.notNil, {
            parent = parent.asFlowView(bounds);
            uv = UserView(parent, bounds ?? {parent.indentedRemaining});
        }, {
            bounds = bounds ?? {Rect(0, 0, 500, 500)};
            parent = Window("SynthiX", bounds).front;
            uv = UserView(parent, parent.bounds.moveTo(0, 0));
            uv.resize = 5;
        });
        uv.drawFunc = {this.draw};
        uv.clearOnRefresh = false;
        uv.mouseDownAction = Message(this, \mouseDownAction, []);
        pen = GUI.pen;
        on = Color.yellow;
        off = Color.black;
        cant = Color.grey;
        font = Font.sansSerif(9);
        bigFont = Font.sansSerif(12);
        black = Color.black;
        blue = Color.yellow(alpha:0.9);
        faint = Color.white;//Color(0.6, 0.6, 0.8, 0.7);
        background = Color.grey(181/255.0);
        this.update;
        if(ins.size > 0, {
            ins[0].mx.addDependant(this);
        })
    }
    update {
        // if what is 'grid'
        ins = inlets.asArray.sort({ arg a, b; a.unit.point <= b.unit.point });
        outs = outlets.asArray.sort({ arg a, b; a.unit.point <= b.unit.point });
    }
    draw {
        var b, gr;
        b = uv.bounds.moveTo(0, 0);


        gr = Rect(labelSize, 17, b.width - labelSize, b.height - 17);
        if(gr != gridRect, {
            gridRect = gr;
            drawInlets = drawGrid = drawOutlets = true;
            height = max(gridRect.height / outs.size.asFloat, 0);
            width = max(gridRect.width / ins.size.asFloat, 0);
        });

        pen.font = font;

        if(drawInlets, {
            "drawInlets".debug;
            this.drawInlets
        });
        if(drawGrid, {
            "drawGrid".debug;
            this.drawGrid
        });
        if(drawOutlets, {
            "drawOutlets".debug;
            this.drawOutlets
        });
        if(animator.notNil, {
            pen.color = blue;
            pen.fillRect(Rect(0, 0, labelSize, height))
        })
    }
    drawInlets {
        var curs;
        pen.use {
            ins.do { arg in, ii;
                var r, v;
                r = Rect(width*ii+labelSize, 0, width, 17);
                pen.color = in.spec.color;
                pen.fillRect(r);
                pen.stringInRect(in.name, r.moveBy(1, 1), font, black);

                // mark value of inlet value
                if(in.canGet, {
                    v = in.spec.unmap(in.get);
                    pen.color = blue;
                    r = r.moveBy( v * width, 0 );
                    r.width = 2;
                    pen.fillRect( r );
                });
            };
            curs = labelSize;
            ins.separate({ arg a, b; a.unit !== b.unit }).do { arg clump, i;
                var r;
                if(clump.size > 0, {
                    r = Rect( curs, 0, clump.size * width, 17 );
                    pen.stringCenteredIn(clump.first.unit.name, r, bigFont, faint);
                    pen.strokeColor = faint;
                    pen.strokeRect(r);
                    curs = r.right;
                })
            };
        };
        drawInlets = false;
    }
    drawGrid {
        pen.color = background;
        pen.fillRect(gridRect);
        pen.use {
            pen.translate(0, 17);
            outs.do { arg out, oi;
                var to;
                pen.use {
                    pen.translate(labelSize, 0);
                    to = out.to.asArray;
                    ins.do { arg in, ii;
                        var can, r;
                        r = Rect(0, 0, width, height).insetAll(0, 0, 1, 1);
                        can = out.unit !== in.unit;
                        if(can, {
                            pen.color = if(to.includes(in), on, off);
                        }, {
                            pen.color = cant;
                        });
                        pen.fillRect(r);
                        pen.translate(width, 0)
                    };
                };
                pen.translate(0, height)
            };
        };
        drawGrid = false;
    }
    drawOutlets {
        var curs = 17; // label height
        pen.color = background;
        pen.fillRect(Rect(0, curs, labelSize, gridRect.height));
        pen.use {
            pen.translate(0, 17);
            outs.do { arg out, oi;
                pen.use {
                    var r, v;
                    r = Rect(1, 1, labelSize, height);
                    pen.color = out.spec.color;
                    pen.fillRect(r);
                    pen.stringInRect(out.name, r, font, black);
                    if(out.canGet, {
                        v = out.spec.unmap(out.get);
                        pen.color = blue;
                        r = r.moveBy( v * labelSize, 0 );
                        r.width = 2;
                        pen.fillRect( r );
                    });
                };
                pen.translate(0, height)
            };
        };
        outs.separate({ arg a, b; a.unit !== b.unit }).do { arg clump, i;
            var r;
            if(clump.size > 0, {
                r = Rect( 0, curs, labelSize, clump.size * height );
                pen.stringCenteredIn(clump.first.unit.name, r, bigFont, faint);
                pen.strokeColor = faint;
                pen.strokeRect(r);
                curs = r.bottom;
            })
        };
        drawOutlets = false;
    }
    mouseDownAction { arg view, x, y, modifiers, buttonNumber, clickCount;
        var p, col, row;
        var out, in, v;
        p = x@y;
        if(gridRect.contains(p), {
            p = p - gridRect.origin;
            col = (p.x / width).asInteger;
            row = (p.y / height).asInteger;
            out = outs.at(row);
            in = ins.at(col);
            if(in.notNil and: {out.unit !== in.unit}, {
                if(out.to.includes(in), {
                    in.disconnect;
                }, {
                    out >> in;
                });
                this.refreshGrid;
            })
        }, {
            // mouse move actually

            // top or side ?
            if(p.x < gridRect.left, {
                if(p.y < gridRect.top, {
                    // top corner
                    if(modifiers.isCtrl, {
                        if(animator.isNil, {
                            animator = Routine({
                                            loop {
                                                this.refresh;
                                                updateRate.wait
                                            }
                                        });
                            animator.play(AppClock);
                        }, {
                            animator.stop;
                            animator = nil
                        })
                    });
                    this.refreshIOlets;
                }, {
                    row = ((p.y - gridRect.top) / height).asInteger;
                    if(clickCount == 2, {
                        outs.at(row).unit.gui
                    }, {
                        this.refreshIOlets;
                    })
                })
            }, {
                if(p.y < gridRect.top, {
                    col = ((p.x - gridRect.left) / width).asInteger;
                    in = ins.at(col);
                    if(clickCount == 2, {
                        in.unit.gui
                    }, {
                        if(modifiers.isCtrl, {
                            if(in.canSet, {
                                v = (p.x - gridRect.left).excess(col * width) / width;
                                in.set(v);
                                {this.refreshIOlets}.defer(0.05);
                            })
                        })
                    })
                })
            })

        })
    }
    refresh {
        drawInlets = drawGrid = drawOutlets = true;
        uv.refresh;
    }
    refreshIOlets {
        drawInlets = drawOutlets = true;
        uv.refresh;
    }
    refreshGrid {
        drawGrid = true;
        uv.refresh
    }
}
