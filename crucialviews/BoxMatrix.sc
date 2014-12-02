

BoxMatrix : SCViewHolder {

    var <numRows,<numCols;
    var bounds,boxes,<focusedPoint,handlers;
    var <>draggingPoint,draggingXY,mouseDownPoint;
    var <>dragOn=inf; // pixel distance to initiate drag or a symbol like \isCntrl
    var <>background,<styles,<defaultStyle;
    var pen, boxWidth,boxHeight,isDown=false;

    *new { arg w,bounds,numCols=10,numRows=6;
        ^super.new.init(w, bounds,numRows,numCols);
    }

    init { arg w,argbounds,argNumRows,argNumCols;

        var skin;
        skin = GUI.skin;
        pen = GUI.pen;
        defaultStyle = (
            font: GUI.font.new(*skin.fontSpecs),
            fontColor: skin.fontColor,
            boxColor: skin.offColor,
            borderColor: skin.foreground,
            center: false
            );        
        this.makeDefaultStyles(skin);

        numRows = argNumRows;
        numCols = argNumCols;

        boxes = Dictionary.new;
        handlers = IdentityDictionary.new;

        bounds = argbounds ?? {Rect(20, 20, min(numCols * 100,1000), numRows * skin.buttonHeight)};
        bounds = bounds.asRect;
        // cargo-culted from thor <- best code comment ever
        bounds = Rect(bounds.left + 0.5, bounds.top + 0.5, bounds.width, bounds.height);

        if(w.isNil, {
            w = Window("BoxMatrix", bounds.resizeBy(40,40).moveTo(10,250) );
            w.front
        });

        bounds = Rect(bounds.left+1, bounds.top+1, bounds.width, bounds.height);
        view = UserView(w, bounds);
        bounds = bounds.moveTo(0,0); // my reference
        boxWidth = bounds.width.asFloat / numCols;
        boxHeight = bounds.height.asFloat / numRows;
	   view.focusColor = defaultStyle.borderColor;
	   
        view.drawFunc = { this.drawGrid };

        // mouses
        view.mouseOverAction = { |me,x,y,modifiers|
            this.handleCoords(x,y,'mouseOverAction',[modifiers])
        };
        view.mouseDownAction = {|me, x, y, modifiers, buttonNumber, clickCount|
            if(this.mouseDownIsDragStart(modifiers,x,y),{
	            this.startDrag(x,y);
            },{
                isDown = true;
                this.handleCoords(x,y,'mouseDownAction',[modifiers, buttonNumber, clickCount],
                    { arg boxPoint;
                        this.transferFocus(boxPoint);
                    })
            });
        };
        view.mouseMoveAction = { arg me,x,y,modifiers;
            if(this.isDragging(modifiers,x,y),{
                if(draggingPoint.isNil,{ // initiate dragging even after down-click
                    draggingPoint = this.boxPoint(x,y);
                    this.transferFocus(draggingPoint);
                });
                // show dragging
                draggingXY = x@y;
                this.view.refresh;
            },{
                this.handleCoords(x,y,'mouseMoveAction',[modifiers])
            });
        };
        view.mouseUpAction = { |me, x, y, modifiers|
            isDown = false;
            if(this.isDragging(modifiers,x,y),{
                this.receiveBoxDrag(this.boxPoint(x,y),modifiers);
                this.view.refresh;
            },{
                this.handleCoords(x,y,'mouseUpAction',[modifiers])
            });
        };

		// dragging on/off the matrix
		// only Qt supplies x,y
        view.beginDragAction = { arg me,x,y;
			if(x.notNil,{
				this.handleCoords(x,y,'beginDragAction')
			},{
            	this.handleByFocused('beginDragAction',[]) ?? {this.focusedBox}
			})
        };
        view.canReceiveDragHandler = { arg me,x,y;
			if(x.notNil,{
				this.handleCoords(x,y,'canReceiveDragHandler')
			},{
            	this.handleByFocused('canReceiveDragHandler',[]) ? true
			})
        };
        view.receiveDragHandler = { arg me,x,y;
			if(x.notNil,{
				this.handleCoords(x,y,'receiveDragHandler')
			},{
		   		this.handleByFocused('receiveDragHandler',[])
			})
        };


        // keys
        view.keyDownAction_({ arg me,char,modifiers,unicode,keycode;
            this.handleByFocused('keyDownAction',[char,modifiers,unicode,keycode]).notNil
        });
        view.keyUpAction = { arg me,char,modifiers,unicode,keycode;
            this.handleByFocused('keyUpAction',[char,modifiers,unicode,keycode]).notNil
        };
        view.keyModifiersChangedAction = { arg me,char,modifiers,unicode,keycode;
            this.handleByFocused('keyModifiersChangedAction',[char,modifiers,unicode,keycode]).notNil
        };
    }

    // same interface as a view
    // but the box's environment is pushed
    // and these args are supplied:
    //  box,  modifiers, buttonNumber, clickCount
    mouseDownAction_ { arg func;
        this.setHandler('mouseDownAction',func)
    }
    //  box,  modifiers
    mouseUpAction_ { arg func;
        this.setHandler('mouseUpAction',func)
    }
    //  box,  modifiers
    mouseOverAction_ { arg func;
        this.setHandler('mouseOverAction',func)
    }
    //  box,  x, y, modifiers
    mouseMoveAction_ { arg func;
        this.setHandler('mouseMoveAction',func)
    }


	// dragging on or off the matrix using command-drag
    // box
    beginDragAction_ { arg func;
        this.setHandler('beginDragAction',{arg box;func.value(box)})
    }
    // itemBeingDragged
    canReceiveDragHandler_ { arg func;
        this.setHandler('canReceiveDragHandler',{arg box;func.value(box,View.currentDrag)})
    }
    // focusedBoxPoint for now, should be the box at the drop point
    // but user view doesnt give x,y yet
    receiveDragHandler_ { arg func;
        this.setHandler('receiveDragHandler',{arg box;func.value(box,View.currentDrag)})
    }

	// box to box dragging
    // args: fromBox,toBox,modifiers
    onBoxDrag_ { arg func;
        this.setHandler('onBoxDrag',{ arg toBox,draggingPoint,modifiers;
	         func.value(this.at(this.boxPoint(draggingPoint.x,draggingPoint.y)),toBox,modifiers,draggingPoint) 
	    });
        if(dragOn == inf,{
            dragOn = 4
        });
    }
    // private
    receiveBoxDrag { arg toBoxPoint,modifiers;
        this.handle(toBoxPoint,'onBoxDrag',[draggingPoint,modifiers]);
        draggingPoint = nil;
        this.transferFocus(toBoxPoint);
    }
    startDrag { arg x,y;
        draggingPoint = this.boxPoint(x,y);
        draggingXY = x@y;
        this.transferFocus(draggingPoint);
        this.view.refresh;
    } 

    // rearranging
    copy { arg fromBoxPoint,toBoxPoint;
	    var box;
	    box = this.getBox(fromBoxPoint).copy;
	    box.point = toBoxPoint;
        boxes.put(toBoxPoint,box);
    }
    clear { arg point;
        boxes.removeAt(point);
    }
    clearAll {
        boxes = Dictionary.new;
    }
    move { arg fromBoxPoint,toBoxPoint;
        if(fromBoxPoint != toBoxPoint,{
            this.copy(fromBoxPoint,toBoxPoint);
            this.clear(fromBoxPoint);
        });
    }
    swap { arg fromBoxPoint,toBoxPoint;
        var tmp;
        tmp = this.getBox(toBoxPoint).copy;
        this.copy(fromBoxPoint,toBoxPoint);
        tmp.point = toBoxPoint;
        boxes[toBoxPoint] = tmp;
    }


    // the key responders are passed to the FOCUSED box
    // box, char,modifiers,unicode,keycode
    keyUpAction_ { arg func;
        this.setHandler('keyUpAction',func)
    }
    // box, char,modifiers,unicode,keycode
    keyDownAction_ { arg func;
        this.setHandler('keyDownAction',func)
    }
    // box,modifiers
    keyModifiersChangedAction_ { arg func;
        this.setHandler('keyModifiersChangedAction',func)
    }

    /* SETTING AND GETTING */
    at { arg boxPoint;
        ^this.getBox(boxPoint)
    }
    colsRowsDo { arg f,createBoxes=false;
	    numRows.do({ arg ri;
		    numCols.do({ arg ci;
			    if(createBoxes,{
				    f.value(this.getBox(ci@ri),ci@ri)
			    },{
				    f.value(boxes.at(ci@ri),ci@ri)
			    })
		    })
	    })
    }
    set { arg boxPoint,attr,value;
        this.getBox(boxPoint).put(attr,value)
    }
    setAll { arg boxPoint,dict;
       this.getBox(boxPoint).putAll(dict)
    }
    withBox { arg boxPoint,func;
        // execute function in en
        boxes[boxPoint] = this.getBox(boxPoint).make(func)
    }
    get { arg boxPoint,attr;
        ^this.getBox(boxPoint).at(attr)
    }
    getBox { arg boxPoint;
        var b;
        if(boxPoint.isKindOf(Point).not,{
            Error("type check failure: not a Point" + boxPoint).throw
        });
        ^boxes.at(boxPoint) ?? {
            b = Environment.new;
            b.know = true;
            boxes.put(boxPoint,b);
            b.point = boxPoint;
            b
        }
    }
    focusedBox {
        ^focusedPoint !? {
            this.getBox(focusedPoint)
        }
    }
    transferFocus { arg toBoxPoint;
        focusedPoint = toBoxPoint
    }
    
	addStyle { arg box,styleName;
		box.styles = box.styles ?? {Set.new};
		box.styles.add(styleName);
	}
	removeStyle { arg box,styleName;
		(box.styles ?? {^nil}).remove(styleName);
	}
    refresh {
	    view.refresh
    }
	
	// private
    setHandler { arg selector,func;
        handlers.put(selector,func)
    }
    handle { arg boxPoint,selector,args,preFunc;
	   var box,ret;
		box = this.at(boxPoint);
        preFunc.valueArray([boxPoint] ++ args);
        ret = handlers.at(selector).valueArray([box] ++ args);
        this.view.refresh;
        ^ret
    }
    handleCoords { arg x,y,selector,args,preFunc;
        ^this.handle(this.boxPoint(x,y),selector,args,preFunc)
    }
    handleByFocused { arg selector,args,preFunc;
        ^focusedPoint !? {
            this.handle(focusedPoint,selector,args,preFunc)
        }
    }

    boxPoint { arg x,y;// view coords
        var col,row;
        x = x.clip(bounds.left,bounds.right);
        col = ((x-bounds.left / bounds.width) * (numCols)).asInteger;
        y = y.clip(bounds.top,bounds.bottom);
        row = ((y-bounds.top / bounds.height) * (numRows)).asInteger;
        ^col@row
    }
    getBoxFromCoords { arg x,y; // view coords
        ^this.getBox(this.boxPoint(x,y));
    }
    withButtonAtCoords { arg x,y,func ... args;
        ^func.performList(\value,[this.getBoxFromCoords(x,y)] ++ args);
    }
    getBounds { arg boxPoint;
        // x is col
        // y is row
        ^Rect(boxPoint.x * boxWidth,boxPoint.y * boxHeight, boxWidth, boxHeight)
    }
    mouseDownIsDragStart { arg modifiers,x,y;
        if(dragOn.isNumber,{
            mouseDownPoint = x@y;
            ^false
        },{
            ^modifiers.perform(dragOn)
        })
    }
    isDragging { arg modifiers,x,y;
         if(dragOn.isNumber,{
             if(mouseDownPoint.notNil,{
                 if((x@y).dist(mouseDownPoint) > dragOn,{
                     draggingPoint = mouseDownPoint;
                     mouseDownPoint = nil;
                     ^true
                },{
                    ^false
                })
            },{
                ^draggingPoint.notNil
            })
        },{
            ^modifiers.perform(dragOn)
        })
    }
    drawGrid {
        var d,box,style;
        pen = GUI.pen;
        d = { arg rect,envir,styleName;
	        var style,styleNames;
	        // cascade styles: defaultStyle + box style + box's set styles (playing, selected) + temp style (down, focused)
	        style = defaultStyle.copy.putAll(envir);
	        styleNames = (envir['styles'] ? []).copy;
	        if(styleName.notNil,{
		        styleNames = styleNames.add(styleName)
	        });
	        styleNames.do { arg sn;
		        styles[sn].keysValuesDo { arg k,v;
			        style[k] = v.value(style[k],envir)
		        }
	        };        
			        
            pen.color = style['boxColor'];
            pen.fillRect( rect );
            pen.color = style['borderColor'];
            pen.strokeRect( rect );
            if(envir['title'].notNil,{
                pen.color = style['fontColor'];
                pen.font = style['font'];
                if(style['center'],{
	                pen.stringCenteredIn(style['title'].asString,rect)
                },{
	                pen.stringLeftJustIn(style['title'].asString, rect.insetBy(2,2) )
                })
            });
        };

        pen.width = 1;
        pen.color = background;
        pen.fillRect(bounds); // background fill

        numCols.do({ arg ci;
            numRows.do({ arg ri;
                var p,box;
                p = ci@ri;
                box = this.getBox(p);
                d.value(this.getBounds(p),box );
            })
        });
        
        // draw focused on top so border style wins out against neighbors
        if(focusedPoint.notNil,{
            box = this.getBox(this.focusedPoint);
            if(isDown) {
                style = 'down'
            }{
                style = box['style'] ? 'focused'
            };
            d.value(this.getBounds(this.focusedPoint),box,style );
        });

        if(draggingPoint.notNil,{
            d.value(
            	Rect(draggingXY.x,draggingXY.y,boxWidth,boxHeight)
              	.moveBy((boxWidth / 2).neg,(boxHeight / 2).neg),
              this.getBox(draggingPoint),
              'dragging')
        })
    }
    makeDefaultStyles { arg skin;
        background = skin.background;
        /*
            fontSpecs:  ["Helvetica", 10],
            fontColor:  Color.black,
            background:     Color(0.8, 0.85, 0.7, 0.5),
            foreground: Color.grey(0.95),
            onColor:        Color(0.5, 1, 0.5),
            offColor:       Color.clear,
            gap:            0 @ 0,
            margin:         2@2,
            boxHeight:  16
            */

        styles = IdentityDictionary.new;
        styles['focused'] = (
            borderColor: Color(0.2499443083092, 0.55516802266236, 0.76119402985075)
            );
        styles['over'] = (
            boxColor: { |c| c.saturationBlend(Color.black,0.3) }
            );
        styles['down'] = (
            boxColor: Color(0.093116507017153, 0.25799716055499, 0.28358208955224, 0.86567164179104),
            borderColor: skin.foreground
            );
        styles['dragging'] = (
            boxColor: { arg c; c.blend(Color.blue(alpha:0.2),0.8) },
            borderColor: Color.blue(alpha:0.5)
            );

        styles['deactivated'] = (
            fontColor: { |c| c.alpha_(0.2) },
            boxColor: { |c| c.alpha_(0.2) },
            borderColor: { |c| c.alpha_(0.2) }
            );
        styles['selected'] = (
            borderColor: Color.blue
            );
        styles['playing'] = (
            boxColor: { arg c; c.darken(Color(0.2202380952381, 0.40008503401361, 0.5)) },        
            borderColor: Color(0.2202380952381, 0.40008503401361, 0.5)
            );
    }
}

/*
        addKeyHandler(function,keycode,shift,cntl,alt,cmd,caps,numPad,fun)
        addUnicodeHandler(function,keycode,shift,cntl,alt,cmd,caps,numPad,fun)
        navByArrows
*/

