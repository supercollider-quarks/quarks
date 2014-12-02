// August 10, MMVII --> 
// slight return: 28/09 -->

// Contains: 
// 	- Layout
// 	- GraphParser
// 	- ParserView


// the idea here is that one collects a series of layout algorithms 
// to be selected via algo arg
// indeed actually is relevant (and implemented) only .uniform
Layout {
	
	var <>vStaticDict, <>position ; 
	
	*new { arg v, algo ;  
		^super.new.initLayout(v, algo ) 
	}

	initLayout { arg v = 1, algo ;
		position = [] ;
		case { algo.isNil }{ this.uniform } 
	}

	uniform { arg minX = 0, maxX = 1200, minY = 0, maxY = 800 ;
		position = [rrand(minX, maxX), rrand(minY, maxY)] ;
	}
	
}


GraphParser {

	// graph is the graph to be modified
	// log a string containing all the commands
	// can be written to file or passed to display
	var <>graph, <>runner, <>painter, <>log, <>lastCmdString ;
	var <>specials ;
	var <>startime ;
	var <>iopath ;
	
	// Note: docs assume that Painter is nil
	// code is dirtier but the nil object has no effect
	
	*new { arg graph, runner, painter ;  
		^super.new.initGraph(graph, runner, painter) 
	}

	initGraph { arg aGraph, aRunner, aPainter ;
		graph = aGraph ;
		runner = aRunner ;
		painter = aPainter ;
		log = "" ;
		lastCmdString = "" ;
		startime = thisThread.seconds ;
		specials = ["e+", "e-", "a+", "a-", "t+", "v+", "v-", "i-", "c+", "r+",
			"s+", "o+", "d+", "d-", "w+", "p+"] ;
		iopath = "/graphs/tmp.gra"
		
	}


//


		/*
		// GRAMMAR
		
		// add edge
		e+ [name dur name ... name]
		
		// remove edge
		// OLD --> e- [name dur name dur ... name]
		e- [id id ... id] // TO BE DONE

		// remove edges
		e- [name name ... name]		
		
		// add vertex
		v+ [name name ... name]
		
		// remove vertex
		v- [name name ... name]
		v- [id id ... id]
		
		// rename vertex
		r+ [name name]
		r+ [id id]
		
		// isolate vertex, i.e remove all links in I/O
		i- [name name ... name]
		i- [id id ... id]
		
		// change duration of an edge
		c+ [id dur]
		
		// add and setup actant
		a+ [name name ... name]
		a+ [id id ... id]
		
		// remove actant
		a- [id id ... id]
		
		// set overall tempo: we need t+ for replacement 
		t+ [value]
		
		// save graph to a SC archive format file, actual path (save)
		s+ 
		
		// save graph to a SC archive format file, prompt for a path (save as)
		s+ a
		
		// read a graph from a SC archive format file, prompt for a path (open)
		o+ 
			
		// set auto duration on and pass mulScale and rounding values		d+ mulScale rounding
		
		// reset to false auto duration
		d-
			
		// clean up all
		w+		
		*/



	parseLine { arg aString ;
		var command, arr, pos, aGraph ;
		var na ;	
		arr = aString.split($ ).reject({arg i ; i=="" ; }) ; // strip away spaces
		//arr.postln ;
		// the grammar
		case 
		// avoid bad strings
		{ specials.select ({ arg i ; (i.asSymbol == arr[0].asSymbol)}).size == 0 } 
			{ "unknown command".postln }
		
		// open a .gra file	
		{ arr[0] =="o+" }
			{ GUI.dialog.getPaths({ arg path ; 
				// path here is an array of paths
				runner.open(path[0]) ;
				iopath = path[0] ;  
				this.toLog(arr) ; // maybe remove it?
				})}

		// save as a .gra file, to actual iopath (-->save)
		{ arr[0] =="s+" && arr.size==1}
			{ 	runner.write(iopath) ; 
				this.toLog(arr) ; // maybe remove it?
				}

		// clean up all
		{ arr[0] =="w+" && arr.size==1}
			{ 	//"clean up all".postln ;
				graph.initGraph ;
				runner.initRunner(graph) ; 
				this.toLog(arr) ; // maybe remove it?
				}


		// save as a .gra file (-->save as)
		{ arr[0] =="s+" && arr[1]=="a" }
			{ 			
				GUI.dialog.savePanel({ arg path ; 
					runner.write(path) ; 
					path.postln ;
					//iopath = path ;  
					}) ;
				this.toLog(arr) ; // maybe remove it?
				}
						
		// set auto duration					
		{ arr[0] =="d+" }
			{ //"auto duration".postln ; 
			graph.setAutoDuration(arr[1].interpret.asFloat, arr[2].interpret.asFloat) ;
			painter.auto = true ;
			this.toLog(arr) }
		
		// no auto duration
		{ arr[0] =="d-" }
			{ //"auto duration".postln ; 
			painter.auto = false ;
			this.toLog(arr) }
					
							
		// add vertex
		{ arr[0] =="v+" }
			{ //"add vertex".postln ; 
			arr[1..].do({ arg v ; 
			pos =  Layout.new.position ;
			graph.addVertex(pos[0], pos[1], 0, v.asSymbol) }) ;
			this.toLog(arr) }

		// remove vertex: name
		{ (arr[0] =="v-") && (arr[1].asFloat == 0) }
			{ //"remove vertex".postln ; 
			arr[1..].do({ arg v ; v = this.getKey(v) ; graph.removeVertex(v.asInteger) }) ;
			this.toLog(arr) }

		// remove vertex: ID
		{ (arr[0] =="v-") && (arr[1].asFloat != 0) }
			{ //"remove vertex by ID".postln ; 
				arr[1..].do({ arg v ; graph.removeVertex(v.asInteger) }) ;
			this.toLog(arr) }

		// add actant: name
		{ (arr[0] =="a+")  && (arr[1].asFloat == 0) }
			{ //"add actant".postln ; 
				arr[1..].do({ arg startingVertex, index ;
				startingVertex = this.getKey(startingVertex) ;
				if ( startingVertex.notNil, {
				runner.addAndSetup(startingVertex)
				}) ;
			this.toLog(arr) })
			}
					
		// add actant: ID
		{ (arr[0] =="a+")  && (arr[1].asFloat != 0) }
			{ //"add actant".postln ; 
				arr[1..].do({ arg startingVertex, index ;
				runner.addAndSetup(startingVertex.asInteger) ;
			this.toLog(arr) })
			}

		// remove actant: ID
		{ (arr[0] =="a-")  && (arr[1].asFloat != 0) }
			{ //"remove actant".postln ; 
				arr[1..].do({ arg startingVertex, index ;
				runner.removeActant(startingVertex.asInteger) ;
			this.toLog(arr) })
			}

		// add and start actant: name
		{ (arr[0] =="p+")  && (arr[1].asFloat == 0) }
			{ //"add actant and start".postln ; 
				arr[1..].do({ arg startingVertex, index ;
				startingVertex = this.getKey(startingVertex) ;
				if ( startingVertex.notNil, {
				runner.addAndSetup(startingVertex) ; 
				runner.start(runner.na-1) ;
				}) ;
			this.toLog(arr) })
			}

		// add and start actant: ID
		{ (arr[0] =="p+")  && (arr[1].asFloat != 0) }
			{ //"add actant".postln ; 
				arr[1..].do({ arg startingVertex, index ;
				runner.addAndSetup(startingVertex.asInteger) ;
				runner.start(runner.na-1) ;
			this.toLog(arr) })
			}


		// change tempo
		// note that tempo is global to session
		{ arr[0] =="t+" && (arr[1].asFloat != 0) }
			{ //"change tempo".postln ; 
				runner.setTempo(arr[1].interpret) ;
			this.toLog(arr) }

		// isolate vertex: name
		{ (arr[0] == "i-") && (arr[1].asFloat == 0) }
			{ //"isolate vertex".postln ;  
				arr[1..].do({ arg v ;
				v = this.getKey(v) ;
				graph.isolateVertex(v.asInteger) ;
			this.toLog(arr) })
			}

		// isolate vertex: ID
		{ ( arr[0] == "i-" )  && (arr[1].asFloat != 0)}
			{ //"isolate vertex by ID".postln ;  
				arr[1..].do({ arg v ;
				graph.isolateVertex(v.asInteger) ;
			this.toLog(arr) })
			}
		// remove edge (by ID)
		{ (arr[0] =="e-") && (arr[1].asFloat != 0) }
			{ na = arr[1..] ;
			//"remove edge".postln ;
			na.do({ arg e, index ;
				graph.removeEdge(e.asInteger) ;
			this.toLog(arr) })
			}

		// add edge by ID
		{ (arr[0] =="e+") && (arr[1].asFloat != 0) }
			{ na = arr[1..].clump(2).flop ;
			//"add edge".postln ;
			na[0][0..na[0].size-2].do({ arg v, index ;
				graph.addEdge(na[0][index].asInteger, na[0][index+1].asInteger, na[1][index].interpret) }) ;
			this.toLog(arr) ;
			}
			
		// add edge by name
		{ (arr[0] =="e+") && (arr[1].asFloat == 0) } 
			{ na = arr[1..].clump(2).flop ;
			//"add edge".postln ;
			na[0].do({ arg v ; graph.graphDict.atFail(this.getKey(v), { 
				pos =  Layout.new.position ;
				graph.addVertex(pos[0], pos[1], 0, v.asSymbol)})
				}) ;
			
			na[0][0..na[0].size-2].do({ arg v, index ;
				graph.addEdge(this.getKey(na[0][index]), 
					this.getKey(na[0][index+1]),
				 	na[1][index].interpret) }) ;
			this.toLog(arr)
			}

	
		// change duration of an edge
		{ arr[0] =="c+" }
			{ //"change duration".postln ; 
			graph.setEdgeDuration( arr[1].asInteger, arr[2].asFloat) ;
			this.toLog(arr) }
			
		// change vertex name by ID
		{ (arr[0] =="r+") && (arr[1].asFloat != 0) }
			{ //"rename vertex".postln ; 
			graph.changeVertexName( arr[1].asInteger, arr[2].asSymbol ) ;
			this.toLog(arr) }


		// change vertex name by name
		{ (arr[0] =="r+") && (arr[1].asFloat == 0) }
			{ //"rename vertex".postln ; 
			graph.changeVertexName( this.getKey(arr[1]), arr[2].asSymbol ) ;
			this.toLog(arr) }
		
		}




	parse { arg aString ;
			var stringArr ;
			lastCmdString = aString ; // we get the last cmd string
			specials.do({
				arg special ;
				// replace all the specials
				 aString = aString.replace(special, "@"++special) ;
				 }) ;
				// split the separator
				stringArr = aString.split($@).reject({ arg i ; i == "" }) ;
			stringArr.do({ arg aString ;
				//aString.postln ;
				this.parseLine(aString ) ;
				}) ;
	}

	getKey { arg vLabel ;
		var key ;
		graph.graphDict.do({ arg v ; if ( v[3].asSymbol == vLabel.asSymbol, 
			{ key = graph.graphDict.findKeyForValue(v) })
		}) ;
		^key ;
	
	}
	
	
	toLog { arg arr ;
		var str = (thisThread.seconds-startime).asTimeString[3..7]  ;
		arr.do({ arg i ;
			str = str + i.asString ;
		}) ;
		log = log + "\n" + str ;
	}
	
	
	gui {arg step = 18 ;  ParserGUI.new(this).createGUI(step:step)  }	
}


// set up a view/control gui interface for parser et al.
ParserGUI {

	var <>graphParser, <>runner ;
	var <>w, <>enterField, <>multiField, <>terminal, <>clockField ;
	var <>tempoSlider, tempoView ;
	var <>startTime, <>clockTask ;

	*new { arg graphParser ; // runner is needed for tempo
		^super.new.initParserView(graphParser) 
	}

	initParserView { arg  aGraphParser ;
		graphParser = aGraphParser ;
		runner = graphParser.runner ;
		runner.addDependant(this) ;		 
		startTime = thisThread.seconds ;
	}


				
	// this one includes a server window and a post window mirror
	createGUI { arg height = 350, bound = 4, step = 18, alpha = 0.9 ;
		var m, gui, doc ;
		var s = Server.local ;
		var inWidth ; 
		gui = GUI.current ;
		s.makeWindow2(label:"God bless Nim Chimpsky", step:step, bound: bound) ; 
		w = s.window ;
		if ( GUI.current.name == \SwingGUI, 
			{ w.server.sendMsg
			( '/methodr', '[', '/method', w.id, \getPeer, ']', \setAlpha, alpha)});
		w.bounds_(Rect(500, 100, w.bounds.width*2, height)) ;
		inWidth = w.bounds.width-bound-bound ;
		doc = Document.listener.title_("Nim Chimpsky's Post Window")
			.background_(Color(0, 0, 0))
			.stringColor_(Color(1, 1, 1))
			.bounds_(Rect(50, 50, 300, 800)) ;
		multiField = GUI.textView.new(w, Rect(bound, step+(3*step*0.6)+(3*bound), inWidth, 300))
			.string_("// ctrl + P --> speak ixno - ctrl + Return --> speak sc")
			.keyDownAction_({ arg view, char, modifiers, unicode, keycode;
				case { [modifiers, unicode, keycode] == [262144, 16, 80] } 
						{ graphParser.parse(view.getLine) }
					{ [modifiers, unicode, keycode] == [262144, 13, 10] }
						{ view.getLine.interpret } 
			})
			.hasVerticalScroller_( true )
			.autohidesScrollers_( true )
			.focus( true )
			.stringColor_(Color(0.9,0.9,0.9))
			.font_(GUI.font.new("Monaco", 10)) 
			.caretColor_(Color(0.9,0.9,0.9))
			.background_(Color(0, 0, 0.3))
 ;
		clockField = GUI.staticText.new(w, Rect((bound*3)+(inWidth*0.5), bound, (inWidth*0.5)-(bound*2), 30))
			.align_(\center)
			.stringColor_(Color(1.0, 0.0, 0.0))
			.background_(Color(0,0,0))
			.font_(GUI.font.new("Optima", 24)) ; 
		clockTask = Task.new({ arg i ; loop({ 
			clockField.string_((thisThread.seconds-startTime).asTimeString) ;
			1.wait }) // a clock refreshing once a second
			}, AppClock).start ;//.play(AppClock) ;	
		tempoView = GUI.staticText.new (w, Rect(
			(bound*3)+(inWidth)-70, bound+30+bound,
				70, 20 ) );

		tempoSlider = GUI.slider.new(w, Rect(
			(bound*3)+(inWidth*0.5), bound+30+bound,
				inWidth*0.5-(bound*2)-70, 20 ))
				.action_({arg it; var t = (it.value*999+1).asInteger; // tempo must be > 0
					tempoView.string_("bpm: "+t) ;
					graphParser.runner.setTempo(t) ;
				}) ;
		w.front ;
		w.onClose_({ doc
			.background_(Color.white)
			.stringColor_(Color.black)
			.bounds_(Rect(50, 400, 625, 500)) ;
			clockTask.stop ;
			runner.removeDependant(this) ;
			}) ;
		}

	update { arg theChanged, theChanger, more;
		if ( more[0] == \tempo, 
			{ 	{tempoView.string_("bpm: "+runner.bpm) ;
				tempoSlider.value_((runner.bpm-1)/999)}.defer	
		 })
	}
										
}		
