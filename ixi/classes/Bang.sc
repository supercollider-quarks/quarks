
// for those missing the PD GUI !!!

Bang {
	
	var win, bounds;
	var mouseTracker, backgrDrawFunc;
	var background, fillmode, fillcolor;
	var state;
	var downAction, upAction;
	
	*new { arg w, bounds; 
		^super.new.initToggle(w, bounds);
	}
	
	initToggle { arg w, argbounds;

		bounds = argbounds ? Rect(20, 20, 400, 200);
		bounds = Rect(bounds.left + 0.5, bounds.top + 0.5, bounds.width, bounds.height);

		win = w ? GUI.window.new("Bang", 
			Rect(10, 250, bounds.left + bounds.width + 40, bounds.top + bounds.height+30));
		win.front;
		background = Color.clear;
		fillcolor = Color.yellow;
		fillmode = true;
		state = false;
		
		mouseTracker = UserView.new(win, bounds)
			.canFocus_(false)
			//.relativeOrigin_(false)
			.mouseDownAction_({|me, x, y, mod|
					if(mod == 262401, { // right mouse down
//						rightDownAction.value(chosennode.nodeloc);
					}, {
						state = true;
						downAction.value(state);
						me.refresh;
					});
			})
			.mouseUpAction_({|me, x, y, mod|
				state = false;
				upAction.value(state);
				me.refresh;
			})
			
			.keyDownAction_({ |me, key, modifiers, unicode |
//				keyDownAction.value(key, modifiers, unicode);
//				this.refresh;
			})

			.drawFunc_({
			
			GUI.pen.color = Color.black;
			GUI.pen.width = 1;
			GUI.pen.color = background; // background color
			GUI.pen.fillRect(Rect(0,0, bounds.width, bounds.height)); // background fill

			backgrDrawFunc.value; // background draw function
			
			GUI.pen.color = Color.black;
			GUI.pen.strokeRect(Rect(0,0, bounds.width, bounds.height)); // stroke toggle rect
			
			if(state == true, {
				GUI.pen.color = fillcolor; // background color
				GUI.pen.fillOval(Rect(2, 2, bounds.width-4, bounds.height-4));
				GUI.pen.color = Color.black;
				GUI.pen.strokeOval(Rect(2, 2, bounds.width-4, bounds.height-4));
			});
			
			GUI.pen.stroke;			
			});

	}
	
	setBangDownAction_ { arg func;
		downAction = func;
	}
	
	setBangUpAction_ { arg func;
		upAction = func;
	}

	setBackgrDrawFunc_ { arg func;
		backgrDrawFunc = func;
	}
	
	setBackground_ {arg color;
		background = color;
		mouseTracker.refresh;
	}

	setFillColor_ {arg color;
		fillcolor = color;
		mouseTracker.refresh;
	}
	
	setState_ {arg bool;
		state = bool;
		mouseTracker.refresh;
	}
		

}