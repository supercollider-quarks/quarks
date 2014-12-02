/**
2008 Till Bovermann (Bielefeld University)
*/

/*
	ToDo:
		ranges knob
		get values into JInT System
		test GUI
*/

JInT_PowerMate : JInT {
	var notInitialized = true;
	var view, window;
	classvar keys = "xyghudlrtz";
	
	*new {|server, view|
		^super.new.initPowerMate(server, view)
	}
	initPowerMate {|server, argView|
		argView = view;
		controllers = [
			JInTC_PowerKnob("Big volume Knob", server, [
			ControlSpec(0, 95, default: 0), // turning
			ControlSpec(0, 95, default: 0), // down->turning 
			ControlSpec(0, 1, default: 0), // shortTrig
			ControlSpec(0, 1, default: 0) // longTrig
			]).short_(\vol)
		]	
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
			window = SCWindow.new("PowerMate", Rect(1000,0, 440, 370)).front;
			window.onClose({notInitialized = true});
			view = Knob.new(window, Rect(10,10,420, 350)).resize_(5);
		};
		view.background = Color.white;
		view.action_({|view,x,y,m| 
			controllers[0].set(0, view.value)
		});

		view.keyDownAction = KeyCodeResponder();
			//.control( 18 -> {i = i+1; i.postln})
			//.control( 19 -> {i = i-1; i.postln});
		notInitialized = false;
	} 
}