/**
2007 Till Bovermann (Bielefeld University)
*/

/*
	ToDo:
		add pen-dependance
*/

JInT_Wacom : JInT {
	var notInitialized = true;
	var view, window;
	classvar keys = "xyghudlrtz";
	
	*new {|server, view|
		^super.new.initWacom(server, view)
	}
	initWacom {|server, argView|
		argView = view;
		controllers = [
			JInTC_PenPos("Pen Position", server, [
				ControlSpec(0, 1, default: 0), // posX
				ControlSpec(0, 1, default: 0), // posX
			]).short_(\pos), 
			JInTC_PenTilt("Pen Tilt", server, [
				ControlSpec(-1, 1, default: 0), // tiltX
				ControlSpec(-1, 1, default: 0), // tiltY
			]).short_(\tilt), 
			JInTC_PenPressure("Pen Pressure", server, 
				ControlSpec(0, 1, default: 0) // pressure
			).short_(\press)
		] ++  
		Array.newFrom(keys).collect{|key|
			JInTC_Button("Button %".format(key), server, ControlSpec(0, 1, default: 0)).short_(key.asSymbol)
		}
	}
	startCustom {
		if (notInitialized) {
			this.initialize;
		}; // fi
		// start sending to server
	}
	stopCustom {
		// stop sending to server
	}
	initialize {
		view = view ?? {	
			window = SCWindow.new("JInT Wacom", Rect(1000,0, 440, 370)).front;
			window.onClose({notInitialized = true});

			view = SC2DTabletSlider(window, Rect(10,10,420, 350)).resize_(5);
		};
		view.background = Color.white;
		view.mouseUpAction = {arg 
			view, 
			x, y, pressure,
			tiltx, tilty, 
			deviceID,
			buttonNumber, clickCount,
			absoluteZ, rotation;

			controllers[0].setAll([x, y]);
			controllers[1].setAll([tiltx, tilty]);
			controllers[2].set(0, 0);

		};
		view.mouseDownAction = { arg 
			view, 
			x, y, pressure,
			tiltx, tilty, 
			deviceID,
			buttonNumber, clickCount,
			absoluteZ, rotation;
			
			// var theta = Point(tiltx, tilty).theta*(-pi).reciprocal - 0.5;
			controllers[0].setAll([x, y]);
			controllers[1].setAll([tiltx, tilty]);
			controllers[2].setAll([pressure]);
		};
		view.action = { arg 
			view, 
			x, y, pressure,
			tiltx, tilty, 
			deviceID,
			buttonNumber, clickCount,
			absoluteZ, rotation;
			
			// var theta = Point(tiltx, tilty).theta*(-pi).reciprocal - 0.5;
			controllers[0].setAll([x, y]);
			controllers[1].setAll([tiltx, tilty]);
			controllers[2].setAll([pressure]);
		};
		view.keyDownAction = {|me, char, modifiers, unicode, keycode|
			controllers[(keys.indexOf(char)+3)].set(0, 1);
		};
		view.keyUpAction = {|me, char, modifiers, unicode, keycode|
			controllers[(keys.indexOf(char)+3)].set(0, 0);
		};
		notInitialized = false;
	} 
}