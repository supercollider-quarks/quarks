Runner {			

	// the runner is the object controlling all the active actants
	// there can be many Actants but one and only Runner
	
	
	var <>graph ;
	var <>bpm ; 
	var <>actantDict, <>na ;
	var <>statsDict ; // counts each vertex occurence
	var <>weight ; 
	// ^^^ this is a weight associated to an actant --> abstraction for amp
	// used as default for new actants
	var <>offsetWeight ;
	var <>name ; // a symbolic name associated to the Runner
	var <>startTime ;	// history



	// constructor: we need a Graph object	
	*new { arg graph, bpm = 60, weight = 1, offsetWeight = 0.0, name = \runner ; 
		^super.new.initRunner(graph, bpm, weight, offsetWeight, name) 
	}

	initRunner { arg aGraph,  aBpm = 60, aWeight = 1, anOffsetWeight, aName = \runner ;
		graph = aGraph ;
		graph.addDependant(this) ;
		bpm = aBpm ;
		actantDict = IdentityDictionary.new ;
		na = 1 ; // total number of actants
		// initializing the statsDict
		statsDict = IdentityDictionary.new ;
		graph.graphDict.keys.do({ arg key ; statsDict[key] = 0 });
		weight = aWeight ; // default value, tuned on amp = 1
		offsetWeight = anOffsetWeight ;
		name = aName ;
		startTime = thisThread.seconds ;
		this.changed(this, [\handling, \initRunner]) ;
	}
	
	
// test me please	
	/* Storing/retrieving in internal format */

// MEMENTO:
// the following doesn't work with task (actants). A different strategy is needed

/*	
	// for consistence use .run extension
	write { arg path ;
		this.writeArchive(path)
	}
	
	// this means you can do:
	// a = Runner.read("/test.run")	
	*read { arg path ;
		^Object.readArchive(path) ;
		
	}
*/

	// path is a path with no extension
	// wrtites two files addin gra and run extension
	// the write/open methods are strictly related
	write { arg path ;
		var act, actDict ;
		// a simple starting vertices list + 1 bpm info would be enough
		// but the dict open other possibilities
		actDict = IdentityDictionary.new ; 
		graph.write(path++".gra") ;
		actantDict.keys.do({ arg key ;
			act = actantDict[key] ;
			actDict[key] = [act.clock.tempo*60, act.startingVertex] ;
		}) ;
		actDict[\name] = this.name ;
		actDict.writeArchive(path++".run")
	}
	
	open { arg path ; 
		var actDict, graphDict ;
		var graPath = path.split($.)[0]++".gra" ;
		graphDict = Object.readArchive(graPath).graphDict ;
		graph.setGraphDict(graphDict) ;
		actDict = Object.readArchive(path) ;
		//reinit
		name = actDict[\name] ;
		actDict.removeAt(\name) ;
		actantDict = IdentityDictionary.new ;
		na = 1 ;
		bpm = actDict[actDict.keys.choose][0] ;
		actDict.keys.asArray.sort.do({ arg key ;
			this.addAndSetup(actDict[key][1])
		}) ;
		this.changed(this, [\handling, \initRunner]) ;
	}
	
	
	// act is an index
	start { arg aID ; actantDict[aID].start; this.changed(this, 
		[\handling, \start, actantDict[aID].aID]) }
	
	resume { arg aID ; actantDict[aID].resume ; this.changed(this, 
		[\handling, \resume, actantDict[aID]]) } 
	
	stop { arg aID ; actantDict[aID].stop ; this.changed(this, 
		[\handling, \stop, actantDict[aID].aID]) }
	
/*	pause { arg aID ; actantDict[aID].pause ; this.changed(this, 
		[\handling, \pause, actantDict[aID].aID]) } */ //obsolete, when actant was a Task, now is a Routine, Routine don't pause
	
	run { arg aID ; actantDict[aID].run ; 
		this.changed(this, [\handling, \run, actantDict[aID].aID]) }

	// general

	setupAll { arg startingVerticesArray, timesArray ;
		var start, times ;
		actantDict.keys.asArray.sort.do({ arg key, index ;
			start = if (startingVerticesArray.isNil, { nil }, { startingVerticesArray[index] }) ;
			times = if (timesArray.isNil, { nil }, { timesArray[index] }) ;
			actantDict[key].setup(start, times)
		}) ;
	}
	
	startAll { actantDict.keys.asArray.do({ arg act; this.start(act) });
				this.changed(this, [\handling, \startAll])
	}
	
	resumeAll { actantDict.keys.asArray.do({ arg act; this.resume(act) });
		this.changed(this, [\handling, \resumeAll])
	}
	
	stopAll { actantDict.keys.asArray.do({ arg act; this.stop(act) }) ;
		this.changed(this, [\handling, \stopAll])
	}
	
/*	pauseAll { actantDict.keys.asArray.do({ arg act; this.pause(act) }) ;
		this.changed(this, [\handling, \pauseAll])
	} */ //obsolete, when actant was a Task, now is a Routine, Routine don't pause

	
	runAll { this.setupAll ; this.startAll ;
		this.changed(this, [\handling, \runAll])
	 }
	
	setTempo { arg aBpm ;
		bpm = aBpm ;
		actantDict.do({ arg act ; act.setTempo(bpm) }) ;
		this.changed(this, [\tempo]) ; 	
	}
	
	
	// actant insertion

/*	
	addActant { 
		actantDict[na] = Actant.new(graph, na, bpm, weight) 
						.addDependant(this).setTempo(bpm) ;
		na = na+1 ;
		this.changed(this, [\handling, \addActant, na-1]) ; 
		}
*/

	// much more useful
	addAndSetup { arg aStartingVertexID, times ;
		actantDict[na] = Actant.new(graph, na, bpm, weight)
						.addDependant(this)
						.setTempo(bpm) 
						.setup(aStartingVertexID, times) ;
		na = na+1 ;
		this.changed(this, [\handling, \addAndSetup, na-1]) ;
	}
	
	/*
	// to solve: aID problem
	addAndSetupAll { arg startingVerticesArray, timesArray ;
		startingVerticesArray.size.do({ arg ind ;
			this.addAndSetup(startingVerticesArray[ind], timesArray[ind])
		})
	}
	*/
	
	removeActant { arg aID ;
		var dependant = actantDict[aID] ;
		actantDict[aID].stop ;
		actantDict.removeAt(aID) ;
		this.removeDependant(dependant);
		this.changed(this, [\handling, \removeActant, aID]) ;
		}
		
	removeActants	{ arg aIDsArray; 
		aIDsArray.do({ arg ind; this.removeActant(ind)}) ;
	}

	removeAllActants { actantDict.keys.do({ arg aID ; this.removeActant(aID) })}
	
	setWeight{ arg actant, weight ;
		actant.setWeight(weight) ;
		this.changed(this, [\weight, actant.aID, weight])
	}


	setAllWeights{ arg weight ;
		actantDict.do({ |actant| actant.setWeight(weight) })
	}

	setOffsetWeight{ arg offset = 0 ;
		offsetWeight = offset ;
		this.changed(this, [\offsetWeight])
	}
	

	update { arg theChanged, theChanger, more;
		// more is the list being sent 
		case 
			{ theChanged.class == Graph }
				{ graph.graphDict.keys.do({ arg key ;
				statsDict.atFail(key, { statsDict[key] = 0 })
				})}
			{ (theChanged.class == Actant).and(more[2].notNil) }	//here more = [startingVertex, vertex[..4], eID, edge, aID, weight]
				 {
			statsDict[more[0]] = statsDict[more[0]]+1 ; 
			// allows to have a counter info for the synths
			more = [\actant].addAll(more.add(offsetWeight).add(statsDict[more[0]])) ;	
			//[\actant, vID, vertex, eID, edge, aID, weight, offsetWeight, count]
			this.changed(this, more) ;
				}
			{ (theChanged.class == Actant).and(more[2].isNil) } //this means that the graph sequence is finished. No loop
				{this.removeActant(more[4]) }
			
			
	}

	gui { arg step = 35, controlNum ; 
		RunnerGUI.new(this, step, controlNum: controlNum).connectAllActants 
	 } 

	clock { arg x = 0, y = 120 ;
		GeoClock.new(this, x, y)
	}

}

Actant {
	
	// an actant is one (and only one) path on a graph 
	
	// graph is a Graph object
	// task is global so that it can be accessed from outside
	// i.e. to pause, stop, resume, etc
	// also we need a synthDef
	// standard is an array of names
	
	var <>graph, <>task ;
	var <>clock ; 
	var <>startingVertex ;
	var <>aID ;
	var <>weight ;
	var isPlaying = false;
	
	// constructor: we need a Graph object
	*new { arg graph, aID, bpm = 60, weight = 0.1 ; 
		^super.new.initActant(graph, aID, bpm, weight) 
	}

	initActant { arg aGraph, anaID, aBpm, aWeight ;
		graph = aGraph ;
		clock = TempoClock(aBpm/60) ;
		aID = anaID ;
		weight = aWeight ;
	}
	
	
	setup { arg aStartingVertexID, times ;
		var vertex, edge, next, duration, eID ;
		startingVertex = aStartingVertexID ? graph.graphDict.keys.choose ;
		times = times ? inf ;
		task = Routine({
				times.do ({ arg i ;
					edge = graph.graphDict[startingVertex][5..].choose ;
					if (edge.isNil, { edge = [nil, 1, nil, nil] }) ; 
											// ^^^ an intermediate format 
											// for handling terminals
					next = edge[0] ;
					duration = edge[1] ;
					eID = edge[2] ;
					vertex = graph.graphDict[startingVertex] ;
						this.changed
						(this, [startingVertex, vertex[..4], eID, edge, aID, weight]) ;
 					startingVertex = next ;
 					duration.wait ;
				}) ;
//			"finished".postln ;
		
			}) ; // before was Task: it cannot be used to generate samples played with DiskIn or other object that requires asynchronous commands calls from inside the Task.
		}

	start {  if (isPlaying == false, {
		task.play(clock) ;
		isPlaying = true;
		}, {"actant"+this.aID+"already playing".postln;}) }	
	resume { task.reset }
	
	stop { task.stop; isPlaying = false; }
	
	//pause { task.pause } Old implementation.
	
	run { this.setup ; this.start }
	
	setTempo { arg bpm ;
		clock.tempo_(bpm/60)
	}
	
	setWeight { arg aWeight ;
		weight = aWeight ;
		
	}
		
}


RunnerMixer {

	// the runner mixer is just a container class
	// allowing to register some runers so that you can control 
	// their offset weight
	// gui not separated from the rest but very simple
		
	var <>runner, <>yStep, <>w, <>x, <>y, <>guiDict ;
	var <>runnerArray ;
	var <>total ;

	// constructor: we need a Graph object	
	*new { arg runnerArray = [], yStep = 35, x = 0, y = 900 ; 
		^super.new.initMixerRunner(runnerArray, yStep, x, y) 
	}

	initMixerRunner { arg aRunnerArray = [], anYStep, aX, aY ;
		runnerArray = aRunnerArray ;
		//runnerArray.do({ |runner| runner.addDependant(this)) ;
		yStep = anYStep ;
		x = aX ;
		y = aY ;
		total = 0 ;
		guiDict = IdentityDictionary.new ;
	}
	
	setWeight { arg runner, weight ;
		runner.setOffsetWeight(weight)
	}
	
	
	setAllWeights { arg weight; 
		runnerArray.do({ |runner |this.setWeight(runner, weight)}
		) }
		
		
	addRunner { arg runner ; runnerArray = runnerArray.add(runner) ;
		this.changed
	}	
	
	removeRunner { arg runner ; runnerArray = runnerArray.remove(runner) ;
	}	
	
	gui { arg alpha = 0.9 ;	
		var wi = (runnerArray.size)*yStep ;
		var lab ;
		w = GUI.window.new("RunnerMixer", 
			Rect.new(x, y, yStep*11.5, wi)) ;
		runnerArray.do({ arg runner, index ;
			GUI.staticText.new(w, Rect(0, index*yStep, yStep*2, yStep))
				.string_("  "+runner.name.asString) ;
			GUI.button.new(w, Rect(yStep*2, index*yStep, yStep, yStep)) 
				.states_([["start", Color.black], ["stop", Color.red]])
				.action_( { arg button ; 
						if ( button.value == 1, { runner.startAll }, { runner.stopAll })}
						) ;
				//.valueAction_(if (runner[0].task.isPlaying, {1}, {0})) ;
			GUI.staticText.new(w, Rect(yStep*3, index*yStep, yStep*1.5, yStep))
				.string_("").align_( \right) ;
			lab = GUI.staticText.new(w, Rect(yStep*8.5, index*yStep, yStep*1.5, yStep))
				.string_(runner.offsetWeight).align_( \right ) ;
			GUI.slider.new(w, Rect(yStep*4.5, index*yStep, yStep*4, yStep))
				.valueAction_(0.5)
				.action_({ arg sl ; 
					runner.setOffsetWeight( sl.value.round(0.001)*2-1) ;
					lab.string_(sl.value.round(0.001)*2-1) ;
				} ) ;  
		}) ;	
		w.front;
		if ( GUI.current.name == \SwingGUI, 
			{ w.server.sendMsg
	 	( '/methodr', '[', '/method', w.id, \getPeer, ']', \setAlpha, alpha )}) ;
	}

	
}