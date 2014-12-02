GeoClock {
	
	var <>runner, <>w, <>x, <>y, <>clockField, <>doc, <>tempoView, <>tempoSlider ;
	var <>r ;

	*new { arg runner, x = 10, y = 120 ; 
		^super.new.initGeoClock(runner, x, y) 
	}

	initGeoClock { arg aRunner, aX, aY ;
		runner = aRunner ;
		runner.addDependant(this) ;
		x = aX ;
		y = aY ;		 
		this.createGUI(x, y) ;
	} 
	
	createGUI { arg x = 10, y = 120 ;
		w = GUI.window.new("Tempus fugit: "+runner.name, Rect(x, y, 425, 80)) ;
		doc = Document.listener.title_("Nim Chimpsky's Post Window")
			.background_(Color(0, 0, 0))
			.stringColor_(Color(1, 1, 1))
			.bounds_(Rect(50, 50, 300, 800)) ;

		clockField = GUI.staticText.new(w, Rect(5,5, 415, 30))
			.align_(\center)
			.stringColor_(Color(1.0, 0.0, 0.0))
			.background_(Color(0,0,0))
			.font_(GUI.font.new("Optima", 24)) ; 
		r = Task.new({ arg i ; 
			loop({ 
			clockField.string_((thisThread.seconds-runner.startTime).asTimeString) ;
			1.wait })// a clock refreshing once a second
			}).play(AppClock) ;	
		tempoView = GUI.staticText.new (w, Rect(350, 35, 75, 40));
		tempoSlider = GUI.slider.new(w, Rect(0, 35, 350, 40))
				.action_({arg it; var t = (it.value*999+1).asInteger; // tempo must be > 0
					tempoView.string_("bpm: "+t) ;
					runner.setTempo(t) ;
				}) ;
		w.front ;
		w.onClose_({ doc
			.background_(Color.white)
			.stringColor_(Color.black)
			.bounds_(Rect(50, 400, 625, 500)) ;
			r.stop ;
			runner.removeDependant(this)
			}) ;

	}
	
}