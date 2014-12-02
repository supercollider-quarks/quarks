// (c) 2006-2010, Thor Magnusson - www.ixi-audio.net
// GNU licence - google it.

// (converted to GUI.sc usage by sciss nov-2006 ; last-mod 26-feb-07)

BoxGrid {

	var <>gridNodes; 
	var tracknode, chosennode, mouseTracker;
	var win, bounds;
	var downAction, upAction, trackAction, keyDownAction, rightDownAction, backgrDrawFunc;
	var background;
	var columns, rows;
	var fillcolor, fillmode;
	var traildrag, bool;
	var font, fontColor;

	var refresh 			= true;	// false during 'reconstruct'
	var refreshDeferred	= false;
	var lazyRefreshFunc;

	*new { arg w, bounds, columns, rows; 
		^super.new.initBoxGrid(w, bounds, columns, rows);
	}
	
	initBoxGrid { arg w, argbounds, argcolumns, argrows;
		var p, rect, pen;

		lazyRefreshFunc = { this.refresh; refreshDeferred = false; };
		
		bounds = argbounds ? Rect(20, 20, 400, 200);
		bounds = Rect(bounds.left , bounds.top , bounds.width, bounds.height);
		
		if((win= w).isNil, {
			win = GUI.window.new("BoxGrid",
				Rect(10, 250, bounds.left + bounds.width + 40, bounds.top + bounds.height+30));
			win.front
		});

		tracknode = 0;
		columns = argcolumns ? 6;
		rows = argrows ? 8;
		background = Color.clear;
		fillcolor = Color.new255(103, 148, 103);
		fillmode = true;
		traildrag = false;
		bool = false;
		font = GUI.font.new("Arial", 9);
		fontColor = Color.black;
		
		gridNodes = Array.newClear(columns) ! rows;
		
		mouseTracker = GUI.userView.new(win, Rect(bounds.left, bounds.top, bounds.width+1, bounds.height+1));
 		//bounds = mouseTracker.bounds;
		
		pen	= GUI.pen;
		columns.do({arg c;
			rows.do({arg r;
				rect = Rect(((c*(bounds.width/columns))).round(1)+0.5, 
							((r*(bounds.height/rows))).round(1)+0.5, 
							(bounds.width/columns).round(1), 
							(bounds.height/rows).round(1)
						);

				gridNodes[r][c] = Box.new(rect, c, r, fillcolor);
			});
		});
				
		mouseTracker
			.canFocus_(false)
			//.relativeOrigin_(false)
			.mouseDownAction_({|me, x, y, mod|
					chosennode = this.findNode(x, y);
					if( (mod & 262144) != 0, { // right mouse down (ctrl pressed)
						rightDownAction.value(chosennode.nodeloc);
					}, {
						if(chosennode !=nil, {  
							chosennode.state = not(chosennode.state);
							tracknode = chosennode;
							downAction.value(chosennode.nodeloc);
							this.lazyRefresh;	
						});
					});
			})
			.mouseMoveAction_({|me, x, y, mod|
				chosennode = this.findNode(x, y);
				if(chosennode != nil, {  
					if(tracknode.rect != chosennode.rect, {
						if(traildrag == true, { // on dragging mouse
							if(bool == true, { // boolean switching
								chosennode.state = not(chosennode.state);
							}, {
								chosennode.state = true;
							});
						},{
							chosennode.state = true;
							tracknode.state = false;
						});
						tracknode = chosennode;
						trackAction.value(chosennode.nodeloc);
						this.lazyRefresh;
					});
				});
			})
			.mouseUpAction_({|me, x, y, mod|
				chosennode = this.findNode(x, y);
				if(chosennode !=nil, {  
					tracknode = chosennode;
					upAction.value(chosennode.nodeloc);
					this.lazyRefresh;
				});
			})
			.keyDownAction_({ |me, key, modifiers, unicode |				keyDownAction.value(key, modifiers, unicode);
//				this.refresh;
			})
			.drawFunc_({ var fillColor;
			pen.width = 1;
//			background.set; // background color
			pen.color = background;
			pen.fillRect(Rect(0,0, bounds.width, bounds.height)); // background fill
			backgrDrawFunc.value; // background draw function
//			Color.black.set;
			pen.color = Color.black;
			//pen.font = font;
			// Draw the boxes
			gridNodes.do({arg row;
				row.do({arg node; 
					if(node.state == true, {
						if(fillmode, {
							pen.color = node.color;
							//if( fillColor != node.color, { pen.fillColor = fillColor = node.color });
							pen.fillRect(node.fillrect);
							pen.color = Color.black;
							pen.strokeColor = Color.black;
							pen.strokeRect(node.fillrect);
						},{
							pen.color = Color.black;
							pen.strokeColor = Color.black;
							pen.strokeRect(node.fillrect);
						});
						if( node.string != "", {
							//if( fillColor != fontColor, {Êpen.fillColor = fillColor = fontColor });
							if(GUI.current.id == \swing, {
								pen.font = font;
								pen.stringInRect( node.string, Rect(node.fillrect.left+5,
										node.fillrect.top+(node.fillrect.height/2)-(font.size/1.5), 
					    					80, 16));
							}, {
								node.string.drawInRect(Rect(node.fillrect.left+5,
				    					node.fillrect.top+(node.fillrect.height/2)-(font.size/1.5), 
				    					80, 16),   
				    					font, fontColor);
				    			});
					    	});
					});
				});
			});

			// Draw the grid
//			Color.black.set;
			(columns+1).do({arg i;
				pen.line(
					Point((i*(bounds.width/columns)),
							0).round(1) + 0.5, 
					Point((i*(bounds.width/columns)),
							bounds.height).round(1) + 0.5
				);
			});
			
			(rows+1).do({arg i;
				pen.line(
					Point(0, 
						(i*(bounds.height/rows))).round(1) + 0.5, 
					Point(bounds.width, 
						(i*(bounds.height/rows))).round(1) + 0.5
				);
			});
			pen.stroke;			
			});
	}
	
	// GRID
	setBackgrColor_ {arg color;
		background = color;
		this.refresh;
	}
		
	setFillMode_ {arg mode;
		fillmode = mode;
		this.refresh;
	}
	
	setFillColor_ {arg color;
		gridNodes.do({arg row;
			row.do({arg node; 
				node.setColor_(color);
			});
		});
		this.refresh;
	}
	
	setTrailDrag_{arg mode, argbool=false;
		traildrag = mode;
		bool = argbool;
	}

	reconstruct { arg aFunc;
		refresh = false;
		aFunc.value( this );
		refresh = true;
		this.refresh;
	}

	refresh {
		if( refresh, { 
			{
			win.isClosed.not.if({ // if window is not closed, update...
				mouseTracker.refresh;
			});
			}.defer;
		 });
	}
		
	lazyRefresh {
		if( refreshDeferred.not, {
			AppClock.sched( 0.02, lazyRefreshFunc );
			refreshDeferred = true;
		});
	}
	
	// NODES	
	setNodeBorder_ {arg border;
		gridNodes.do({arg row;
			row.do({arg node; 
				node.setBorder_(border);
			});
		});
		this.refresh;
	}
	
	// depricated
	setVisible_ {arg row, col, state;
		gridNodes[col][row].setVisible_(state);
		this.refresh;
	}

	setState_ {arg row, col, state;
		if(state.isInteger, {state = state!=0}); // returns booleans
		gridNodes[col][row].setState_(state);
		this.refresh;
	}
	
	getState {arg row, col;
		var state;
		state = gridNodes[col][row].getState;
		^state.binaryValue;
	}	
	
	setBoxColor_ {arg row, col, color;
		gridNodes[col][row].setColor_(color);
		this.refresh;
	}
	
	getBoxColor {arg row, col;
		^gridNodes[col][row].getColor;	
	}
	
	getNodeStates {
		var array;
		array = Array.newClear(columns) ! rows;
		gridNodes.do({arg rows, r;
			rows.do({arg node, c; 
				array[r][c] = node.state.binaryValue;
			});
		});
		^array;
	}
	
	setNodeStates_ {arg array;
		gridNodes.do({arg rows, r;
			rows.do({arg node, c; 
				node.state = array[r][c]!=0;
			});
		});
		this.refresh;
	}
	
	clearGrid {
		gridNodes.do({arg rows, r;
			rows.do({arg node, c; 
				node.state = false;
			});
		});
		this.refresh;
	}	
	
	// PASSED FUNCTIONS OF MOUSE OR BACKGROUND
	nodeDownAction_ { arg func;
		downAction = func;
	}
	
	nodeUpAction_ { arg func;
		upAction = func;
	}
	
	nodeTrackAction_ { arg func;
		trackAction = func;
	}
	
	keyDownAction_ {arg func;
		mouseTracker.canFocus_(true); // in order to detect keys the view has to be focusable
		keyDownAction = func;
	}
	
	rightDownAction_ {arg func;
		rightDownAction = func;
	}
	
	setBackgrDrawFunc_ { arg func;
		backgrDrawFunc = func;
	}
	
	setFont_ {arg f;
		font = f;
	}
	
	setFontColor_ {arg fc;
		fontColor = fc;
	}
	
	setNodeString_ {arg row, col, string;
		gridNodes[col][row].string = string;
		this.refresh;		
	}
	
	getNodeString {arg row, col;
		^gridNodes[col][row].string;
	}
	
	remove {
		mouseTracker.remove;
		win.refresh;
	}
	// local function
	findNode {arg x, y;
		gridNodes.do({arg row;
			row.do({arg node; 
				if(node.rect.containsPoint(Point.new(x,y)), {
					^node;
				});
			});
		});
		^nil;
	}
}

Box {
	var <>fillrect, <>state, <>border, <>rect, <>nodeloc, <>color;
	var <>string;
	
	*new { arg rect, column, row, color ; 
		^super.new.initGridNode( rect, column, row, color);
	}
	
	initGridNode {arg argrect, argcolumn, argrow, argcolor;
		rect = argrect;
		nodeloc = [ argcolumn, argrow ];	
		color = argcolor;	
		border = 3;
		fillrect = Rect(rect.left+border, rect.top+border, 
					rect.width-(border*2), rect.height-(border*2));
		state = false;
		string = "";
	}
	
	setBorder_ {arg argborder;
		border = argborder;
		fillrect = Rect(rect.left+border, rect.top+border, 
					rect.width-(border*2), rect.height-(border*2));
	}
	
	setVisible_ {arg argstate;
		state = argstate;
	}
	
	setState_ {arg argstate;
		state = argstate;
	}
	
	getState {
		^state;
	}
	
	setColor_ {arg argcolor;
		color = argcolor;
	}
	
	getColor {
		^color;
	}
}