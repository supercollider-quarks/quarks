/*
SynthDefPool - a quark to facilitate sharing SynthDefs in a structured way
Created by Dan Stowell 2009
SynthDefPool.gui
*/
SynthDefPool {

	classvar <global;
	var <poolpath, <dict;


	*new { |poolpath|
	  ^super.newCopyArgs(poolpath).init;
	}
	init {
	  dict = IdentityDictionary.new;
	  (poolpath ++ "/*.scd").pathMatch.do{ |apath|
	  	// Lazy loading - ignore the file contents for now
	  	dict.put(apath.basename.splitext[0].asSymbol, 0); // we put "0" because can't actually put nil into a dictionary...
	  };
	}

	*initClass {
	  StartUp.add{
	    global = this.new(this.filenameSymbol.asString.dirname +/+ "pool")
	  }
	}

	*at { |key|
		^global.at(key)
	}
	at { |key|
	  // Lazy loading
	  if(dict[key]==0){
	    dict[key] = thisProcess.interpreter.compileFile("%/%.scd".format(poolpath, key)).value;
	  };
	  ^dict[key]
	}
	scanAll { // Unlazy
		dict.keysDo{|key| this[key]}
	}
	forget { // relazy
		dict.keysDo{|key| this[key] = 0}
	}

	*gui {
		^global.gui
	}
	gui {
		var w, list, listview, wrect, metadataview, mdv_updater, startButton, aSynth, cmdPeriodFunc;
		Server.default.waitForBoot{
			this.memStore;
			
			w = GUI.window.new("<SynthDefPool>", Rect(0, 0, 600, 100).center_(GUI.window.screenBounds.center));
			list = dict.keys(Array);
			list.sort;
			
			wrect = w.view.bounds;
			listview = GUI.popUpMenu.new(w, wrect.copy.width_(wrect.width/2 - 75).height_(50).insetBy(5, 15)).items_(list);
			
			// add a button to start and stop the sound.
			startButton = GUI.button.new(w, Rect(listview.bounds.right+10, listview.bounds.top, 65, listview.bounds.height))				.states_([
					["Start", Color.black, Color(0.5, 0.7, 0.5)],
					["Stop", Color.white, Color(0.7, 0.5, 0.5)]
				]).action_{|widg|
					if(widg.value==0){
						if(aSynth.notNil){aSynth.free; aSynth=nil };
					}{
						aSynth = Synth(listview.item);
						OSCresponderNode(Server.default.addr, '/n_end', { |time, resp, msg|
							if(aSynth.notNil and: {msg[1]==aSynth.nodeID}){
								// Synth has freed (itself?) so ensure button state is consistent
								{startButton.value=0}.defer;
							};
						}).add.removeWhenDone;
					}
				};
			cmdPeriodFunc = { startButton.value = 0; };
			CmdPeriod.add(cmdPeriodFunc);
			// stop the sound when window closes and remove cmdPeriodFunc.
			w.onClose = {
				if(aSynth.notNil) {
					aSynth.free;
				};
				CmdPeriod.remove(cmdPeriodFunc);
			};

			GUI.button.new(w, wrect.copy.left_(wrect.width*3/6).width_(wrect.width/6).height_(50).insetBy(5, 15))
				.states_([["makeWindow"]])
				.action_{ SynthDescLib.at(listview.item).makeWindow };
			GUI.button.new(w, wrect.copy.left_(wrect.width*4/6).width_(wrect.width/6).height_(50).insetBy(5, 15))
				.states_([["usage"]])
				.action_{ Document.new(string:"s.boot;
SynthDefPool.at(\\"++listview.item++").memStore; // ensure the server knows about it
x = Synth(\\"++listview.item++", [\\freq, 440]);
x.set(\\freq, 330);
"
				).syntaxColorize.promptToSave_(false) };
			GUI.button.new(w, wrect.copy.left_(wrect.width*5/6).width_(wrect.width/6).height_(50).insetBy(5, 15))
				.states_([["source"]])
				.action_{ Document.open(poolpath +/+ listview.item ++ ".scd") };
			
			metadataview = GUI.staticText.new(w, wrect.copy.top_(50).height_(50).insetBy(5));
			mdv_updater = {
				var desc = SynthDescLib.global[listview.item];
				metadataview.string = 
					if(desc.metadata[\credit].notNil){"Credit: %\n".format(desc.metadata[\credit])}{""}
						++
					if(desc.metadata[\tags].notNil){"Tags:   %\n".format(desc.metadata[\tags].join(", "))}{""}
						;
				};
			mdv_updater.value;
			listview.action = mdv_updater;
			
			w.front;
		};

	}
	
	*defnames {
		^global.defnames
	}
	defnames {
		^dict.keys
	}
	
	// Conveniences to load/store/etc all our defs
	*writeDefFile { |dir|
		^global.writeDefFile(dir)
	}
	writeDefFile { |dir|
		this.scanAll;
		dict.do{|def| def.writeDefFile(dir)};
	}
	*load { |server|
		^global.load(server)
	}
	load { |server|
		this.scanAll;
		dict.do{|def| def.load(server)};
	}
	*store { |libname=\global, completionMsg, keepDef = true, mdPlugin| 
		^global.store(libname, completionMsg, keepDef, mdPlugin)
	}
	store { |libname=\global, completionMsg, keepDef = true, mdPlugin|
		this.scanAll;
		dict.do{|def| def.store(libname, completionMsg, keepDef, mdPlugin)};
	}
	*memStore { |libname=\global, completionMsg, keepDef = true| 
		^global.memStore(libname, completionMsg, keepDef)
	}
	memStore { |libname=\global, completionMsg, keepDef = true|
		this.scanAll;
		dict.do{|def| def.memStore(libname, completionMsg, keepDef)};
	}


} // end class
