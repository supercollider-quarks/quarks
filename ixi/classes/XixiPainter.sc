
XixiPainter {
	var drawer;
	var win, rect;
	var keyDownAction, keyUpAction;
	var backgrDrawFunc;
	var background, fillcolor;
	var running;
	
	var drawList;
	var playerTask, frameFunc, <>frameRate;

	*new { arg w, bounds; 
		^super.new.initXixiPainter(w, bounds);
	}
	
	initXixiPainter { arg w, argbounds;
 		rect = argbounds ? Rect(20, 20, 400, 200);
	//	bounds = Rect(bounds.left , bounds.top,  bounds.width, bounds.height);
		
		win = w;
		// ? SCWindow("XixiDrawer", 
		//	Rect(10, 250, bounds.left + bounds.width + 40, bounds.top + bounds.height+30));
		win.front;
		//win.acceptsMouseOver = true;

		drawList = List.new;
		
		frameFunc = nil;
		frameRate = 0.06; // used in wait
		running = false;
		
		background = Color.white;


		drawer = UserView.new(win, rect)
			.canFocus_(true)
			.focusColor_(Color.new(0,0,0,0))
			.mouseDownAction_({|me, x, y, mod|
				/*
				if(mod == 262401, { // right mouse down
				}, {// else
				});
				*/
				
				block {|break|
					drawList.do({ |object|
						if(object.mouseDown(x, y), {   // if mousedown returns true
							if(running == false, {this.refresh});
							break.value; 			   // then break out of the loop
						});
					});
				};
				this.refresh;
			})
			.mouseMoveAction_({|me, x, y, mod|
				drawList.do({ |object|
					object.mouseTrack(x, y);
				});
				if(running == false, {this.refresh});
			})
			.mouseOverAction_({|me, x, y|
				drawList.do({ |object|
					object.mouseOver(x, y);
				})
			})
			.keyDownAction_({ |me, key, modifiers, unicode |
				keyDownAction.value(key, modifiers, unicode);
			})
			.keyUpAction_({ |me, key, modifiers, unicode |
				keyUpAction.value(key, modifiers, unicode);
			})
			.mouseUpAction_({|me, x, y, mod|
				drawList.do({ |object|
					object.mouseUp(x, y);
				})
			})
			.drawFunc_({	
				GUI.pen.color = background; // background color
				GUI.pen.fillRect(Rect(0.5, 0.5, rect.width-1, rect.height-1)); // background fill
				GUI.pen.color = Color.black;
				drawList.do({ |object| object.draw.value });
				GUI.pen.color = Color.black;
				GUI.pen.strokeRect(Rect(0.5, 0.5, rect.width-1, rect.height-1)); // background frame
			});
		/*			
			win.drawHook_({	
				GUI.pen.color = background; // background color
				GUI.pen.fillRect(bounds); // background fill
				GUI.pen.color = Color.black;
				drawList.do({ |object| object.draw.value });
				GUI.pen.color = Color.black;
				GUI.pen.strokeRect(bounds); // background frame
			});
		*/	
			win.onClose_({
				playerTask.removedFromScheduler; 
				playerTask.stop;
				this.remove;
			});


	}
	
	clearSpace {
		this.refresh;
	}
	
	setBackgrColor_ {arg color, refresh=true;
		background = color;
		if(refresh == true, {this.refresh});
	}
		
	refresh {
		{
		win.isClosed.not.if({ // if window is not closed, update...
			drawer.refresh;
			if(GUI.id == \swing, {win.refresh;});
		})
		}.defer;
	}		
	
	start {
		running = true;
		playerTask = Task({
			inf.do({ arg i;
				drawList.do({ |object|
					object.update;
				});	
				frameFunc.value; // a function to be valuated on each frame
				this.refresh; // calls draw automatically from win.drawhook
				frameRate.wait;
			})
		}).start;
	}

	stop {
		running = false;
		playerTask.stop;
	}

	pause {
		playerTask.pause;
	}

	resume {
		playerTask.resume;
	}

	setFrameFunc_ {arg func;
		frameFunc = func;
	}
	
	setFrameRate_ { arg rate;
		frameRate = rate;
	}
	
	background_ {arg color;
		background = color;
	}

	keyDownAction_ {arg func;
		keyDownAction = func;
	}

	keyUpAction_ {arg func;
		keyUpAction = func;
	}
	
	addToDrawList { |object|
		if(object.isArray, {
			drawList = drawList++object;
		},{
			drawList.add(object);	
		});
	}
	
	replaceDrawList { |object|
		drawList = List.new;
		if(object.isArray, {
			drawList = drawList++object;
		},{
			drawList.add(object);	
		});
	}
	
	remove {
		drawer.remove;
	}
}