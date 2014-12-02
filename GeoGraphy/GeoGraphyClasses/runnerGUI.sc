// Setup a GUI to view/control an Actant to be passed 
RunnerGUI {

	var <>runner, <>yStep, <>w, <>gui, <>x, <>y ;
	var <max, guiArr, <controlNum ;
	var runnerControl ;

	*new {Ê arg runner, yStep = 35, x = 1040, y = 900, controlNum ;
		^super.new.initViewer(runner, yStep, x, y, controlNum)Ê
	}

	initViewer { arg aRunner, anYStep, aX, aY, aControlNum ;
		runner = aRunner ;
		runner.addDependant(this) ;
		yStep = anYStep ;
		x = aX ;
		y = aY ;
		controlNum = if ( aControlNum == nil,  { (850/yStep).trunc }, { aControlNum })  ;
		if ( controlNum < 2, { controlNum = 2 });
		this.createGUI ; 
	}

	
	createGUI { arg alpha = 0.9 ;	
		var wi = (runner.actantDict.size+1)*yStep ;
		var index ;
		guiArr = [] ;
		w = GUI.window.new("The Parallax View: "+runner.name.asString, 
			Rect.new(x, y, yStep*11.5, controlNum*yStep)) ;
		w.view.background_(Color(0.9, 0.9, 0.9)) ;
		w.onClose_({ runner.removeDependant(this) }) ;
		runnerControl = [
				GUI.staticText.new(w, Rect(0, 0*yStep, yStep*2, yStep))
				.string_("  "++runner.name.asString)
				.stringColor_(Color(0, 0, 0.3)) ,
				GUI.button.new(w, Rect(yStep*2, 0*yStep, yStep, yStep)) 
				.states_([["start", Color(0.9, 0.9, 0.9), Color(0, 0, 0.3)], 
					["stop", Color(0.9, 0.9, 0.9), Color.red]])
				.action_( { arg button ; 
						if ( button.value == 1, { runner.startAll }, { runner.stopAll })}
						),
				GUI.slider.new(w, Rect(yStep*4.5, 0*yStep, yStep*4, yStep))
				.value_(runner.offsetWeight+1*0.5)
				.action_({ arg sl ; runner.setOffsetWeight(sl.value.round(0.001)*2-1) } ),
				GUI.staticText.new(w, Rect(yStep*8.5, 0*yStep, yStep*1.5, yStep))
				.string_(runner.offsetWeight.asString).align_( \right )  
				.stringColor_(Color(0, 0, 0.3))
			] ;
		(controlNum-1).do({ arg startIndex ;
			index = startIndex+1 ;
			guiArr = guiArr.add([ nil,
				GUI.staticText.new(w, Rect(0, index*yStep, yStep*2, yStep))
				.string_("  Free")
				.stringColor_(Color(0, 0, 0.3)) ,
				GUI.button.new(w, Rect(yStep*2, index*yStep, yStep, yStep)) 
				.states_([["start", Color(0.9, 0.9, 0.9), Color(0, 0, 0.3)], 
					["stop", Color(0.9, 0.9, 0.9), Color.red]])
					.action_{"not connected".postln },
				GUI.staticText.new(w, Rect(yStep*3, index*yStep, yStep*1.5, yStep))
				.string_("").align_( \right)
				.stringColor_(Color(0, 0, 0.3)) ,
				GUI.slider.new(w, Rect(yStep*4.5, index*yStep, yStep*4, yStep))
					.action_{"not connected".postln },
				GUI.staticText.new(w, Rect(yStep*8.5, index*yStep, yStep*1.5, yStep))
					.string_	("")
					.stringColor_(Color(0, 0, 0.3))
			]) ;		
		}) ;
		w.front;
		if ( GUI.current.name == \SwingGUI, 
			{ w.server.sendMsg
	 	( '/methodr', '[', '/method', w.id, \getPeer, ']', \setAlpha, alpha )}) ;
	}

	connectAllActants {
		runner.actantDict.keys.asArray.sort.do({ arg aID ;
			this.connectActant(aID)
		})
	}
		
	connectActant { arg actantID ;
		var available, gui, id ;
		var actant = runner.actantDict[actantID]  ;
		gui = guiArr.detect({ |i| i[0] == nil }) ;
		if (gui.notNil, {
			gui[0] = actant.aID ;
			gui[1].string_("  ActID:"+actant.aID.asString) ;
			gui[2]
				.action_( { arg button ; 
					if ( button.value == 1, 
						{ actant.start ; runnerControl[1].value_(1) }, { actant.stop })}
				)
				.valueAction_(if (actant.task.isPlaying, {1}, {0})) ;
			gui[3].string_("") ;
			gui[4]
				.value_(actant.weight)
				.action_({ arg sl ; runner.setWeight(actant, sl.value.round(0.001)) }) ;
			gui[5].string_(actant.weight).align_( \right ) ;
		})
	}

	disconnectActant { arg actantID ;
		var available, gui, id ;
		var actant = runner.actantDict[actantID]  ;
		gui = guiArr.detect({ |i| i[0] == actantID }) ;
		if (gui.notNil, {
			gui[0] = nil ;
			gui[1].string_("  Free") ;
			gui[2]
				.action_( { arg button ; "not connected".postln }
				)
				.valueAction_( "not connected".postln ) ;
			gui[3].string_("") ;	
			gui[4]
				.action_({ "not connected".postln })
				.value_(0.0) ;
			gui[5].string_("").align_( \right ) ;
		})
	}

	setState { arg actantID, state ;
		var available, gui, id ;
		var actant = runner.actantDict[actantID]  ;
		gui = guiArr.detect({ |i| i[0] == actantID }) ;
		if (gui.notNil, {
			gui[2]
			.valueAction_(state) ;
		})
	}
	

	update { arg theChanged, theChanger, more;
		var gui ;
		case { more[0] == \handling } { {
			case 
			{ more[1] == \addAndSetup } { this.connectActant(more[2]) }
			{ more[1] == \removeActant } { this.disconnectActant(more[2]) }
			{ more[1] == \start } { this.setState(more[2], 1); runnerControl[1].value_(1) }
			{ more[1] == \stop } { this.setState(more[2], 0) }
			{ more[1] == \startAll } { runnerControl[1].valueAction_(1) }
			{ more[1] == \stopAll } { runnerControl[1].valueAction_(0) }
			{ more[1] == \initRunner } { 
				if (w.notNil, { w.close }) ;
				this.createGUI ;
				this.connectAllActants 
				}
			}.defer }
			{ more[0] == \weight } 
					{ {
					gui = guiArr.detect({ |i| i[0] == more[1] }) ;
					gui[5].string_(more[2]) ;
					gui[4].value_(more[2]) ;
					 }.defer } 
			{ more[0] == \offsetWeight } 
					 { runnerControl[3].string_(runner.offsetWeight) ;
					   runnerControl[2].value_(runner.offsetWeight+1*0.5) ;
					 }
			// i.e. the actant is moving on the graph
			// a \actant selector messager should be implemented
			{ more[0] == \actant }
				{
					{ 
				gui = guiArr.detect({ |i| i[0] == more[5] }) ;
				if (gui.notNil, {
					gui[3].string_(" "+
						runner.graph.graphDict[more[1]][3]) 
					})
					}.defer
					}
	}


}



