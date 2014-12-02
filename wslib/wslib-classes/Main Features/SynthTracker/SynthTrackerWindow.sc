// wslib 2005

SynthTrackerWindow {
	classvar <window;
	classvar <synthsToCheck;
	classvar <buttons;
	
	*new { arg names, mode = 'toggle'; //only mode so far
		var closeFunc;
		this.deprecated( thisMethod );
		SynthTracker.initialize;
		if(names.isNil)
			{ names = SynthTracker.isReleasable.keys; };
		if(window.notNil)
			{ if(window.isClosed.not) 
				{ window.close; } };
		buttons = ();
		window = SCWindow("SynthTrackerWindow", Rect(10, 250, 150, (names.size * 20) + 7) );
		names.do({ |name, i|
			buttons.put(name, SCButton(window, Rect(7, 1 + (i*20), 16, 16)).states_([
					[""],
					["X", Color.black, Color.red.alpha_(0.5)]])
				 .action_({ |button|
				 	if(button.value == 0)
				 		{ SynthTracker.release(name); }
				 		{ SynthTracker(name); };
				 	}));
			SCStaticText(window, Rect(27, (i*20), 250, 18)).string_(name);
			});
		if(CmdPeriod.objects.includes(SynthTrackerWindow).not)
				{ CmdPeriod.add(SynthTrackerWindow); };
		//window.onClose_({ window = nil });
		window.front;
		SynthTrackerWindow.updateAll(names)
		^window;
		}

	*cmdPeriod { if(buttons.notNil)
		{ buttons.do({ |button| button.value = 0 }) }; }
		
	*update { arg name;
		var button;
		if(window.notNil && {window.isClosed.not})
		{	if(name.isNil)
			{ buttons.keys.do({ |key|
				SynthTrackerWindow.update(key); }) }
			{ 
			button = buttons[name.asSymbol];
			if(button.notNil)
				{ if( SynthTracker.isRunning(name.asSymbol) )
					{ { button.value = 1; }.defer }
					{ { button.value = 0; }.defer };  };	
			};
		};
		}
	
	*updateAll { arg names;
		if(names.notNil)
			{ names.asCollection.do({ |name|
				SynthTrackerWindow.update(name.asSymbol); }); }
		}
	}