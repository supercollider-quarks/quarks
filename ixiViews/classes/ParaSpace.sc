// (c) 2006-2010, Thor Magnusson - www.ixi-audio.net
// GNU licence - google it.



ParaSpace {

	var <>paraNodes, connections; 
	var chosennode, mouseTracker;
	var win, <bounds;
	var downAction, upAction, trackAction, keyDownAction, rightDownAction, overAction, connAction;
	var backgrDrawFunc;
	var background, fillcolor;
	var nodeCount, shape;
	var startSelPoint, endSelPoint, refPoint;
	var selNodes, outlinecolor, selectFillColor, selectStrokeColor;
	var keytracker, conFlag; // experimental
	var nodeSize, swapNode;
	var font, fontColor;
	
	var refresh 			= true;	// false during 'reconstruct'
	var refreshDeferred	= false;
	var lazyRefreshFunc;
	
	*new { arg w, bounds; 
		^super.new.initParaSpace(w, bounds);
	}
	
	initParaSpace { arg w, argbounds;
		var a, b, rect, relX, relY, pen;
		bounds = argbounds ? Rect(20, 20, 400, 200);
		bounds = Rect(bounds.left + 0.5, bounds.top + 0.5, bounds.width, bounds.height);

		if((win= w).isNil, {
			win = GUI.window.new("ParaSpace",
				Rect(10, 250, bounds.left + bounds.width + 40, bounds.top + bounds.height+30));
			win.front;
		});

		mouseTracker = UserView.new(win, Rect(bounds.left, bounds.top, bounds.width, bounds.height));
 		bounds = mouseTracker.bounds; // thanks ron!
 		
		background = Color.white;
		fillcolor = Color.new255(103, 148, 103);
		outlinecolor = Color.red;
		selectFillColor = Color.green(alpha:0.2);
		selectStrokeColor = Color.black;
		paraNodes = List.new; // list of ParaNode objects
		connections = List.new; // list of arrays with connections eg. [2,3]
		nodeCount = 0;
		startSelPoint = 0@0;
		endSelPoint = 0@0;
		refPoint = 0@0;
		shape = "rect";
		conFlag = false;
		nodeSize = 8;
		font = Font("Arial", 9);
		fontColor = Color.black;
		pen	= GUI.pen;


		mouseTracker
			.canFocus_(true)
			.focusColor_(Color.clear.alpha_(0.0))
			//.relativeOrigin_(false)
			.mouseDownAction_({|me, x, y, mod|
				chosennode = this.findNode(x, y);
				if( (mod & 0x00040000) != 0, {	// == 262401
					paraNodes.add(ParaNode.new(x,y, fillcolor, bounds, nodeCount, nodeSize));
					nodeCount = nodeCount + 1;
					paraNodes.do({arg node; // deselect all nodes
					 		node.outlinecolor = Color.black; 
							node.refloc = node.nodeloc;
					});
					startSelPoint = x-10@y-10;
					endSelPoint =   x-10@y-10;
				}, {
					if(chosennode !=nil, { // a node is selected
						relX = chosennode.nodeloc.x - bounds.left - 0.5;
						relY = chosennode.nodeloc.y - bounds.top - 0.5;
						refPoint = x@y; // var used here for reference in trackfunc
						
						if(conFlag == true, { // if selected and "c" then connection is possible
							paraNodes.do({arg node, i; 
								if(node === chosennode, {a = i;});
							});
							selNodes.do({arg selnode, j; 
								paraNodes.do({arg node, i; 
									if(node === selnode, {b = i;
										this.createConnection(a, b);
									});
								});
							});
						});
						downAction.value(chosennode);
					}, { // no node is selected
					 	paraNodes.do({arg node; // deselect all nodes
					 		node.outlinecolor = Color.black; 
							node.refloc = node.nodeloc;
					 	});
						startSelPoint = x@y;
						endSelPoint = x@y;
						this.refresh;
					});
				});
			})
			.mouseMoveAction_({|me, x, y, mod|
				if(chosennode != nil, { // a node is selected
					relX = chosennode.nodeloc.x - bounds.left - 0.5;
					relY = chosennode.nodeloc.y - bounds.top - 0.5;
					chosennode.setLoc_(Point(x,y));
					block {|break|
						selNodes.do({arg node; 
							if(node === chosennode,{ // if the mousedown box is one of selected
								break.value( // then move the whole thing ...
									selNodes.do({arg node; // move selected boxes
										node.setLoc_(Point(
											node.refloc.x + (x - refPoint.x),
											node.refloc.y + (y - refPoint.y)
										));
									});
								);
							}); 
						});
					};
					trackAction.value(chosennode);
					this.refresh;
				}, { // no node is selected
					endSelPoint = x@y;
					this.refresh;
				});
			})
			.mouseOverAction_({arg me, x, y;
				chosennode = this.findNode(x, y);
				if(chosennode != nil, {  
					relX = chosennode.nodeloc.x - bounds.left - 0.5;
					relY = chosennode.nodeloc.y - bounds.top - 0.5;
					overAction.value(chosennode);
				});
			})
			.mouseUpAction_({|me, x, y, mod|
				if(chosennode !=nil, { // a node is selected
					relX = chosennode.nodeloc.x - bounds.left - 0.5;
					relY = chosennode.nodeloc.y - bounds.top - 0.5;
					upAction.value(chosennode);
					paraNodes.do({arg node; 
						node.refloc = node.nodeloc;
					});
					this.refresh;
				},{ // no node is selected
					// find which nodees are selected
					selNodes = List.new;
					paraNodes.do({arg node;
						if(Rect(	startSelPoint.x, // + rect
								startSelPoint.y,									endSelPoint.x - startSelPoint.x,
								endSelPoint.y - startSelPoint.y)
								.containsPoint(node.nodeloc), {
									node.outlinecolor = outlinecolor;
									selNodes.add(node);
						});
						if(Rect(	endSelPoint.x, // - rect
								endSelPoint.y,									startSelPoint.x - endSelPoint.x,
								startSelPoint.y - endSelPoint.y)
								.containsPoint(node.nodeloc), {
									node.outlinecolor = outlinecolor;
									selNodes.add(node);
						});
						if(Rect(	startSelPoint.x, // + X and - Y rect
								endSelPoint.y,									endSelPoint.x - startSelPoint.x,
								startSelPoint.y - endSelPoint.y)
								.containsPoint(node.nodeloc), {
									node.outlinecolor = outlinecolor;
									selNodes.add(node);
						});
						if(Rect(	endSelPoint.x, // - Y and + X rect
								startSelPoint.y,									startSelPoint.x - endSelPoint.x,
								endSelPoint.y - startSelPoint.y)
								.containsPoint(node.nodeloc), {
									node.outlinecolor = outlinecolor;
									selNodes.add(node);
						});
					});
					startSelPoint = 0@0;
					endSelPoint = 0@0;
					this.refresh;
				});
			})
			.drawFunc_({		
	
				pen.width = 1;
				pen.color = background; // background color
				pen.fillRect(Rect(0,0, bounds.width, bounds.height)); // background fill
				backgrDrawFunc.value; // background draw function
				
				// the lines
				pen.color = Color.black;
				connections.do({arg conn;
					pen.line(paraNodes[conn[0]].nodeloc+0.5, paraNodes[conn[1]].nodeloc+0.5);
				});
				pen.stroke;
				
				// the nodes or circles
				paraNodes.do({arg node;
					if(shape == "rect", {
						pen.color = node.color;
						pen.fillRect(node.rect);
						pen.color = node.outlinecolor;
						pen.strokeRect(node.rect);
					}, {
						pen.color = node.color;
						pen.fillOval(node.rect);
						pen.color = node.outlinecolor;
						pen.strokeOval(node.rect);
					});
					if(GUI.current.id == \swing, {
					    	if( node.string.size > 0, {
						    	pen.fillColor = fontColor;
					    		pen.stringInRect( node.string, 
					    			Rect(node.rect.left+node.size+5, node.rect.top-3, 80, 16));
					    	});
				    	},{
					    	node.string.drawInRect(Rect(node.rect.left+node.size+5,
				    								node.rect.top-3, 80, 16),   
				    								font, fontColor);
				    	});

				});
				pen.stroke;		
				
				pen.color = selectFillColor;
				// the selection node
				pen.fillRect(Rect(	startSelPoint.x + 0.5, 
									startSelPoint.y + 0.5,
									endSelPoint.x - startSelPoint.x,
									endSelPoint.y - startSelPoint.y
									));
				pen.color = selectStrokeColor;
				pen.strokeRect(Rect(	startSelPoint.x + 0.5, 
									startSelPoint.y + 0.5,
									endSelPoint.x - startSelPoint.x,
									endSelPoint.y - startSelPoint.y
									));

				pen.color = Color.black;

				pen.strokeRect(Rect(0,0, bounds.width, bounds.height)); // background frame
			})
			.keyDownAction_({ |me, key, modifiers, unicode |
				if(unicode == 127, {
					selNodes.do({arg box; 
						paraNodes.copy.do({arg node, i; 
							if(box === node, {this.deleteNode(i)});
						})
					});
				});
				if(unicode == 99, {conFlag = true;}); // c is for connecting
				keyDownAction.value(key, modifiers, unicode);
				this.refresh;
			})
			.keyUpAction_({ |me, key, modifiers, unicode |
				if(unicode == 99, {conFlag = false;}); // c is for connecting

			});
	}
	
	clearSpace {
		paraNodes = List.new;
		connections = List.new;
		nodeCount = 0;
		this.refresh;
	}
		
	createConnection {arg node1, node2, refresh=true;
		if((nodeCount < node1) || (nodeCount < node2), {
			"Can't connect - there aren't that many nodes".postln;
		}, {
			block {|break|
				connections.do({arg conn; 
					if((conn == [node1, node2]) || (conn == [node2, node1]), {
						break.value;
					});	
				});
				// if not broken out of the block, then add the connection
				connections.add([node1, node2]);
				connAction.value(paraNodes[node1], paraNodes[node2]);
				if(refresh == true, {this.refresh});
			}
		});
	}

	deleteConnection {arg node1, node2, refresh=true;
		connections.do({arg conn, i; if((conn == [node1, node2]) || (conn == [node2, node1]),
			 { connections.removeAt(i)})});
		if(refresh == true, {this.refresh});
	}

	deleteConnections { arg refresh=true; // delete all connections
		connections = List.new; // list of arrays with connections eg. [2,3]
		if(refresh == true, {this.refresh});
	}

	createNode {arg x, y, color, refresh=true;
		fillcolor = color ? fillcolor;
		paraNodes.add(ParaNode.new(x+0.5, y+0.5, fillcolor, bounds, nodeCount, nodeSize));
		nodeCount = nodeCount + 1;
		if(refresh == true, {this.refresh});
	}
	
	createNode1 {arg argX, argY, color, refresh=true;
		var x, y;
		x = (argX * bounds.width).round(1);
		y = (argY * bounds.height).round(1);
		fillcolor = color ? fillcolor;
		paraNodes.add(ParaNode.new(x+0.5, y+0.5, fillcolor, bounds, nodeCount, nodeSize));
		nodeCount = nodeCount + 1;
		if(refresh == true, {this.refresh});
	}
	
	deleteNode {arg nodenr, refresh=true; var del;
		del = 0;
		connections.copy.do({arg conn, i; 
			if(conn.includes(nodenr), { connections.removeAt((i-del)); del=del+1;})
		});
		connections.do({arg conn, i; 
			if(conn[0]>nodenr,{conn[0]=conn[0]-1});if(conn[1]>nodenr,{conn[1]= conn[1]-1});
		});
		if(paraNodes.size > 0, {paraNodes.removeAt(nodenr)});
		if(refresh == true, {this.refresh});
	}
	
	setNodeLoc_ {arg index, argX, argY, refresh=true;
//		var x, y;
//		x = argX+bounds.left;
//		y = argY+bounds.top;
		paraNodes[index].setLoc_(Point(argX+0.5, argY+0.5));
		if(refresh == true, {this.refresh});
	}
	
	setNodeLocAction_ {arg index, argX, argY, action, refresh=true;
//		var x, y;
//		x = argX+bounds.left;
//		y = argY+bounds.top;
		paraNodes[index].setLoc_(Point(argX, argY));
		switch (action)
			{\down} 	{downAction.value(paraNodes[index])}
			{\up} 	{upAction.value(paraNodes[index])}
			{\track} 	{trackAction.value(paraNodes[index])};
		if(refresh == true, {this.refresh});
	}
	
	getNodeLoc {arg index;
		var x, y;
		x = paraNodes[index].nodeloc.x-0.5;
		y = paraNodes[index].nodeloc.y-0.5;
		^[x, y];
	}

	setNodeLoc1_ {arg index, argX, argY, refresh=true;
		var x, y;
		x = (argX * bounds.width).round(1);
		y = (argY * bounds.height).round(1);
		paraNodes[index].setLoc_(Point(x+0.5, y+0.5));
		if(refresh == true, {this.refresh});
	}

	setNodeLoc1Action_ {arg index, argX, argY, action, refresh=true;
		var x, y;
		x = (argX * bounds.width).round(1);
		y = (argY * bounds.height).round(1);
		paraNodes[index].setLoc_(Point(x+bounds.left, y+bounds.top));
		switch (action)
			{\down} 	{downAction.value(paraNodes[index])}
			{\up} 	{upAction.value(paraNodes[index])}
			{\track} 	{trackAction.value(paraNodes[index])};
		if(refresh == true, {this.refresh});
	}

	getNodeLoc1 {arg index;
		var x, y;
		x = paraNodes[index].nodeloc.x  / bounds.width;
		y = paraNodes[index].nodeloc.y  / bounds.height;
		^[x, y];
	}
	
	getNodeStates {
		var locs, color, size, string;
		locs = List.new; color = List.new; size = List.new; string = List.new;
		paraNodes.do({arg node; 
			locs.add(node.nodeloc);
			color.add(node.color); 
			size.add(node.size);
			string.add(node.string);
		});
		^[locs, connections, color, size, string];
	}

	setNodeStates_ {arg array; // array with [locs, connections, color, size, string]
		if(array[0].isNil == false, {
			paraNodes = List.new; 
			array[0].do({arg loc; 
				paraNodes.add(ParaNode.new(loc.x, loc.y, fillcolor, bounds, nodeCount));
				nodeCount = nodeCount + 1;
				})
		});
		if(array[1].isNil == false, { connections = array[1];});
		if(array[2].isNil == false, { paraNodes.do({arg node, i; node.setColor_(array[2][i];)})});
		if(array[3].isNil == false, { paraNodes.do({arg node, i; node.setSize_(array[3][i];)})});
		if(array[4].isNil == false, { paraNodes.do({arg node, i; node.string = array[4][i];})});
		this.refresh;
	}

	setBackgrColor_ {arg color, refresh=true;
		background = color;
		if(refresh == true, {this.refresh});
	}
		
	setFillColor_ {arg color, refresh=true;
		fillcolor = color;
		paraNodes.do({arg node; 
			node.setColor_(color);
		});
		if(refresh == true, {this.refresh});
	}
	
	setOutlineColor_ {arg color;
		outlinecolor = color;
		this.refresh;
	}
	
	setSelectFillColor_ {arg color, refresh=true;
		selectFillColor = color;
		if(refresh == true, {this.refresh});
	}

	setSelectStrokeColor_ {arg color, refresh=true;
		selectStrokeColor = color;
		if(refresh == true, {this.refresh});
	}
	
	setShape_ {arg argshape, refresh=true;
		shape = argshape;
		if(refresh == true, {this.refresh});
	}
	
	reconstruct { arg aFunc;
		refresh = false;
		aFunc.value( this );
		refresh = true;
		this.refresh;
	}

	refresh {
		if( refresh, { {mouseTracker.refresh}.defer; });
	}

	lazyRefresh {
		if( refreshDeferred.not, {
			AppClock.sched( 0.02, lazyRefreshFunc );
			refreshDeferred = true;
		});
	}
				
	setNodeSize_ {arg index, size, refresh=true;
		if(size == nil, {
			nodeSize = index;
			paraNodes.do({arg node; node.setSize_(nodeSize)});
		}, {
			paraNodes[index].setSize_(size);
		});
		if(refresh == true, {this.refresh});
	}

	getNodeSize {arg index;
		^paraNodes[index].size;
	}
	
	setNodeColor_ {arg index, color, refresh=true;
		paraNodes[index].setColor_(color);
		if(refresh == true, {this.refresh});
	}
	
	getNodeColor {arg index;
		^paraNodes[index].getColor;	
	}
	
	setFont_ {arg f;
		font = f;
	}
	
	setFontColor_ {arg fc;
		fontColor = fc;
	}
	
	setNodeString_ {arg index, string;
		paraNodes[index].string = string;
		this.refresh;		
	}
	
	getNodeString {arg index;
		^paraNodes[index].string;
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
	
	nodeOverAction_ { arg func;
		overAction = func;
		win.acceptsMouseOver = true;
	}
	
	connectAction_ {arg func;
		connAction = func;
	}
	
	setMouseOverState_ {arg state;
		win.acceptsMouseOver = state;
	}
	
	keyDownAction_ {arg func;
		keyDownAction = func;
	}
	
	setBackgrDrawFunc_ { arg func;
		backgrDrawFunc = func;
		this.refresh;
	}
	
	// local function
	findNode {arg x, y;
		paraNodes.do({arg node; 
			if(node.rect.containsPoint(Point.new(x,y)), {
				^node;
			});
		});
		^nil;
	}
}

ParaNode {
	var <>fillrect, <>state, <>size, <rect, <>nodeloc, <>refloc, <>color, <>outlinecolor;
	var <>spritenum, <>temp;
	var bounds;
	var <>string;
	
	*new { arg x, y, color, bounds, spnum, size; 
		^super.new.initGridNode(x, y, color, bounds, spnum, size);
	}
	
	initGridNode {arg argX, argY, argcolor, argbounds, spnum, argsize;
		spritenum = spnum;
		nodeloc =  Point(argX, argY);	
		refloc = nodeloc;
		color = argcolor;	
		outlinecolor = Color.black;
		size = argsize ? 8;
		bounds = argbounds;
		rect = Rect((argX-(size/2))+0.5, (argY-(size/2))+0.5, size, size);
		string = "";
		temp = nil;
	}
		
	setLoc_ {arg point;
		nodeloc = point;
		// keep paranode inside the bounds
		if((point.x) > bounds.width, {nodeloc.x = bounds.width - 0.5});
		if((point.x) < 0, {nodeloc.x = 0.5});
		if((point.y) > bounds.height, {nodeloc.y = bounds.height -0.5});
		if((point.y) < 0, {nodeloc.y = 0.5});
		rect = Rect((nodeloc.x-(size/2))+0.5, (nodeloc.y-(size/2))+0.5, size, size);
	}
		
	setState_ {arg argstate;
		state = argstate;
	}
	
	getState {
		^state;
	}
	
	setSize_ {arg argsize;
		size = argsize;
		rect = Rect((nodeloc.x-(size/2))+0.5, (nodeloc.y-(size/2))+0.5, size, size);
	}
	
	getSize {
		^size;
	}
	
	setColor_ {arg argcolor;
		color = argcolor;
	}
	
	getColor {
		^color;
	}
}