// part of wslib 2005
// W. Snoei 2005

SynthTracker {
	
	classvar <runningSynths;
	classvar <isReleasable;
	classvar <>server;
	classvar <routines;
	classvar <>warn = false;
	classvar <watch = true; // use NodeWatcher to watch Synths on server
	
	// Synth tracking/bookkeeping system based on defnames.
	// Keeps track of all running synths by name and order of execution.
	// Only works with one server running.
	//
	// Use like Synth: SynthTracker("default") will behave like Synth("default").
	// Only feed it releasable or free-able synths when watch is false	
	// args can be Dictionary or Event as well - like this: ( freq: 440, gate: 1 )
	
	*new { arg defName, args, canRelease, maxCount = inf, addAction = \addToTail;
		var synth, alreadyRunningSynths;
		this.deprecated( thisMethod );
		defName = defName.asSymbol;
		//SynthTracker.initialize;
		if( runningSynths.at(defName).size < maxCount )
			{ synth = Synth.newKeepArgs(defName, args.asArgsArray, server, addAction);
		synth.isPlaying = true;
		if( watch ) { NodeWatcher.register(synth); }; // should be fast enough to catch the \n_go
		if(warn) { ("SynthTracker : '" ++ defName ++ 
			"' (" ++ synth.nodeID ++
			") started").postln; };
		runningSynths.put(defName, runningSynths.at(defName) ++ [synth]);
		isReleasable.put(defName, canRelease ? isReleasable.at(defName) ? false); }
			{ if(warn) { ("SynthTracker : '" ++ defName ++ "' not started because maxCount (" 
				++ maxCount ++ ") was reached." ).postln; } };
		SynthTrackerWindow.update;
		^synth;
		}
		
	*loadSavedDefs { arg filter = nil; // will also initialize
		// load saved synthdefs to store canRelease data
		// using SynthDescLib : make sure synthTracker-methods.sc is also installed
		var synthDescs, whichLoaded = [];
		//SynthTracker.initialize;
		SynthDescLib.global.read;
		SynthDescLib.global.synthDescs.keysValuesDo({
			|key, value|
			value.track;
			if(value.willFreeSynth.not)
				{ whichLoaded = whichLoaded.add(value.name); };
			});
		^whichLoaded;
		}
	
	*fullReset  { SynthTracker.reset;
		isReleasable = (default: true); }
	
	*initClass { 
			if(CmdPeriod.objects.includes(SynthTracker).not)
				{ CmdPeriod.add(SynthTracker); };
			runningSynths = ();
			isReleasable = (default: true); // default synthdef is releasable
			routines = [];
			server = Server.default;
			}
	
	/// NOT USED ANYMORE ; replaced by *initClass 
	*initialize { // does not reset!!
		if(runningSynths.isNil) 
			{
			if(CmdPeriod.objects.includes(SynthTracker).not)
				{ CmdPeriod.add(SynthTracker); };
			runningSynths = ();
			isReleasable = (default: true); // default synthdef is releasable
			routines = [];
			server = Server.default;}; }
	/// ----------------
		
	*cmdPeriod { SynthTracker.reset; SynthTracker.killRoutines;  }
	
	*window { arg names; var out = SynthTrackerWindow(if(names.notNil) {names.asCollection} {nil});
				SynthTrackerWindow.update;
				^out; }
	
	*deleteUnused {
		var newRS, newIR;
		newRS = ();
		if(warn) { "SynthTracker : deleted non-running defnames".postln; };
		runningSynths.keys.do({ |key|
			var item;
			item = runningSynths.at(key);
			if(item.size !== 0)
				{newRS.put(key, item);
					newIR.put(key, isReleasable.at(key)); }
				{if(warn) { key.postln; }; }; });
		runningSynths = newRS; isReleasable = newIR;
	}
	
	// < routine support >
	
	*doWhenStopped { arg defName, func, time = 0.5, minCount = 1;
		// creates and runs a routine
		// replaces any existing routine for this defName
		// the routine will perform cleanUp every <time> seconds
		var routine, oldRoutines;
		SynthTracker.initialize;
		oldRoutines = routines.select({ |item| item.defName == defName.asSymbol });
		if( oldRoutines.size != 0 )
			{ oldRoutines.do( _.stop );
				SynthTracker.cleanRoutines; };
		routine = Routine({ while { SynthTracker.isRunning(defName, minCount) }
					{  time.wait; };
					func.value; });
		routine.addUniqueMethod('defName', { defName });
		
		routines = routines.add(routine);
		^routine.play;
		}
		
	*doAlwaysWhenStopped { arg defName, func, time = 0.5, minCount = 1;
		var routine, oldRoutines;
		// be sure to kill this routine later or it will run forever
		oldRoutines = routines.select({ |item| item.defName == defName.asSymbol });
		if( oldRoutines.size != 0 )
			{ oldRoutines.do( _.stop );
				SynthTracker.cleanRoutines; };
				
		routine = Routine({ loop { while { SynthTracker.isRunning(defName, minCount) }
						{  time.wait; };
					func.value; 
					while { SynthTracker.isRunning(defName).not }
						{ time.wait; }; };
					});
					
		routine.addUniqueMethod('defName', { defName.asSymbol });
		if(routines.isNil) {routines = []};
		routines = routines.add(routine);
		^routine.play;
	}
	
	*killRoutines { // this is also called at cmdPeriod
		routines.do( _.stop );
		routines = []; }
	
	*killRoutine { |defName|
		if(defName.isNil)
			{ SynthTracker.killRoutines; }
			{ routines.do({ |routine| if(routine.defName == defName.asSymbol)
				{ routine.stop; }; });
			SynthTracker.cleanRoutines; }
		}
			
		
	*cleanRoutines { routines = routines.select( _.isPlaying ); }
	
	*routinesPlaying { SynthTracker.cleanRoutines;
		^routines.size; }
	
	// </ routine support >

	*register { |synthToRegister, canRelease, isPlaying = true| 
		// can be Synth, array of Synths, defName, array of defNames
		var defName;
		//SynthTracker.initialize;
		case {synthToRegister.class == Synth}
			{ 	defName = synthToRegister.defName.asSymbol;
				synthToRegister.isPlaying = isPlaying;
				if(watch) {NodeWatcher.register(synthToRegister)};
					isReleasable.put(defName, canRelease ? isReleasable.at(defName) ? false); 
					runningSynths.put(defName, runningSynths.at(defName) ++ 
						synthToRegister.asCollection); 
				^synthToRegister; }
			{ (synthToRegister.class == String) or: (synthToRegister.class == Symbol) }
			{	defName = synthToRegister.asSymbol;
				isReleasable.put(defName, canRelease ? isReleasable.at(defName) ? false); 
				^defName; }
			{ synthToRegister.size !== 0 }
			{ ^synthToRegister.do({ |item, i| SynthTracker.register(item, 
				canRelease.asCollection.wrapAt(i),
				isPlaying.asCollection.wrapAt(i))
					}); }
		}
	
	*unregister { |synthToUnRegister| // can be array
		if( synthToUnRegister.size == 0 )
			{	if(watch) {NodeWatcher.unregister(synthToUnRegister)};
				^runningSynths.at(synthToUnRegister.defName.asSymbol).remove(synthToUnRegister); 
				}
			{^synthToUnRegister.do({ |item| SynthTracker.unregister(item) }); }
		}
	
	*reset { |defName|
		// after reboot or command-period, will keep the isReleasable information
		// cmdPeriod calls this method already
		if(defName.notNil)
			{ runningSynths.put(defName.asSymbol, []); }
			{ runningSynths = Event.new; }
		}
		
	*report { arg defName, nodeIDs = true, cleanUp=true; // for checking..
		var savedNames = [], totalRunning = 0;
		//SynthTracker.initialize;
		SynthTracker.cleanUp(cleanUp);
		if(defName.notNil)
			{ defName = defName.asSymbol;
				(defName.asCompileString ++
					if(isReleasable.at(defName) == true, {" (releasable)"}, {""}) ++ 
					" running : " ++ runningSynths.at(defName).size).postln;
				if(nodeIDs)
					{ runningSynths.at(defName).collect(_.nodeID)
					 .postItems(8, startString: "   (\t", endString: " )\n"); };
			} {	"SynthTracker : report\n".extend(41, $-).postln;
				runningSynths.keys.do({	|item|
					var size;
					size = runningSynths.at(item).size;
					if(runningSynths.at(item).size > 0)
						{ SynthTracker.report(item, nodeIDs); 
							totalRunning = totalRunning + size;}
						{ savedNames = savedNames ++ [item]; }
					});
				"".extend(20, $_).postln;
				("total running : " ++ totalRunning).postln;
				if(savedNames.size != 0)
				{ "".extend(20, $-).postln;
				("registered but not running : " ++ savedNames.size).post;
				savedNames.postItems(6, startString: "\n   (\t", endString: " )\n\n"); };
				Post.nl;
					};
		}
	
	*fullReport { arg cleanUp = false, includeNonPlaying = false, includeReleasable = true;
		var totalRunning = 0, totalRegistered = 0;
		var maxDefNameSize;
		// alternative version of *report
		//SynthTracker.initialize;
		SynthTracker.cleanUp(cleanUp);
		"SynthTracker : full report".postln;
		if(runningSynths.size != 0)
			{ "".extend(25, $-).postln;
			if(watch) { "Watch mode is on (true)\n".postln; 
				} { "Watch mode is off (false)".postln; };
			runningSynths.keysValuesDo( { |key, array|
				var playing, notPlaying;
				//("\t" ++ key.asCompileString).post;
				if(array.size != 0)
					{ playing = array.select({ |synth| synth.isPlaying });
					 notPlaying = array.select({ |synth| synth.isPlaying.not });
					 ("\t" ++ key.asCompileString ++ " (" ++ playing.size ++ ")").postln;
					 playing.do({ |synth, i| 
					 	("\t\t" ++ i ++ " : " ++ synth.nodeID).postln; 
					 	totalRunning = totalRunning + 1;
					 	totalRegistered = totalRegistered + 1});
					 if(notPlaying.size > 0)
					 	{ ("\t\tnot playing (" ++ notPlaying.size ++ ")" ).postln;
					 	 notPlaying.do({ |synth, i|
					 	  ("\t\t" ++ i ++ " : " ++ synth.nodeID).postln;
					 	   }); }; 
					}
					{if(includeNonPlaying) { ("\t" ++ key.asCompileString ++" (0)").postln; } };
			} ); };
		if((isReleasable.size != 0) && includeReleasable )
			{"".extend(25, $-).postln;
			maxDefNameSize = isReleasable.keys.collect( { |item| item.asString.size; } ).maxItem;
			"Synth is releasable :".postln;
			isReleasable.sortedKeysValuesDo( { |key, value|
				(("\t" ++ key.asCompileString).extend(maxDefNameSize + 3, $ ) ++
				 " : " ++ value).postln;
				} ); };
		"".extend(25, $_).postln;
		if(watch) { ("total registered synths : " ++ totalRegistered).postln; };
		("total playing : " ++ totalRunning).postln;
		("total registered defNames : " ++ isReleasable.keys.size).postln;
		"".postln;
		}
	
	*canRelease { arg defName;
		if( isReleasable.notNil )
			{ if( isReleasable.at(defName).notNil )
				{ ^isReleasable.at(defName) }
				{ ^false };
			} { ^false };
		}
	
	*findByNodeID { |nodeID = 1000|
		runningSynths.keysValuesDo { |name, array|
			array.do { |synth| if (synth.nodeID == nodeID) { ^synth } };
			};
		^nil
		}
	
	*findNameByNodeID { |nodeID = 1000|
		runningSynths.keysValuesDo { |name, array|
			array.do { |synth| if (synth.nodeID == nodeID) { ^synth.defName } };
			};
		^nil
		}
	
	*findSynth { |synth| ^{ runningSynths.at(synth.defName.asSymbol).indexOf(synth)}.try }
	
	*allSynths { |defName|
		var outArray = [];	
		if(defName.isNil)
			{ runningSynths.keysValuesDo({ |key, array|
				if(array.notNil)
					{ outArray = outArray ++ array }; }); }
			{ outArray = runningSynths.at(defName) };
		^outArray;	
		}
	
	watch_ { |bool = true|
		if(bool)
			{ if(watch.not) // if not already on
				{
				SynthTracker.allSynths.do({ |synth|
					synth.isPlaying = true; NodeWatcher.register(synth); })  }; }
			{ if(watch) // if not already off
				{SynthTracker.allSynths.do({ |synth|
					NodeWatcher.unregister(synth); })   }
			 };
		watch = bool;
		}
		
	// <extra>
	
	*getSavedDefs {  ^Cocoa.getPathsInDirectory("synthdefs/".standardizePath)
			.find(".scsyndef").collect({ |item| item[..item.size-10];})
		}
	
	*getUserDefs { ^SynthTracker.getSavedDefs.select({ |defName|
			["mixer_", "system-", "system_", "bbcs", "help"]
					.collect({ |word| defName.contains(word).not; })
					.every({ |item| item }) &&
				(defName != "default") 
			});
		}
	
	*reportSavedDefs { arg post = true;
		var defs, systemDefs, mixerDefs, bbcDefs, helpDefs;
			defs = SynthTracker.getSavedDefs;
			mixerDefs = defs.removeAllSuchThat({ |item| item.contains("mixer_") });
			systemDefs = defs.removeAllSuchThat({ |item| 
				item.contains("system-") or: item.contains("system_")
					or: (item == "default")});
			bbcDefs = defs.removeAllSuchThat({ |item| item.contains("bbcs") });
			helpDefs = defs.removeAllSuchThat({ |item| item.contains("help") });
			if(post) {
			if(mixerDefs.size > 0)
				{("------------\nMixer (" ++ mixerDefs.size ++ "):").postln;
				mixerDefs.do(_.postln);};
			if(systemDefs.size > 0)
				{("------------\nSystem (" ++ systemDefs.size ++ "):").postln;
				systemDefs.do(_.postln);};
			if(helpDefs.size > 0)
				{("------------\nHelp (" ++ helpDefs.size ++ "):").postln;
				helpDefs.do(_.postln);};
			if(bbcDefs.size > 0)
				{("------------\nBBCut (" ++ bbcDefs.size ++ "):").postln;
				bbcDefs.do(_.postln);};
			("------------\nOther (" ++ defs.size ++ "):").postln;
			defs.do(_.postln);
			"------------".postln; };
			^(mixer: mixerDefs, system: systemDefs, help: helpDefs, bbc: bbcDefs, other: defs);
		}
		
	// </extra>
	
	*cleanUp { |go = true| // remove and unregister non-playing synths when watch is on
		go = go ? true;
		if( watch && go ) 
			{ runningSynths.keys.do({ |key|
				runningSynths.put(key, runningSynths.at(key)
					.select({ |synth| if( synth.isPlaying )
						{ true; }
						{ NodeWatcher.unregister(synth); false; };
							}) );
					});
			}
		}

	*isRunning { arg defName, minCount = 1, cleanUp=true;
		SynthTracker.cleanUp(cleanUp);
		 ^runningSynths.at(defName.asSymbol).size >= minCount; 
		}
	
	*synthIsRunning { arg synth;
		^runningSynths.at(synth.defName.asSymbol).includes(synth) }
	
	*nodeIDIsRunning { arg nodeID = 1000; ^SynthTracker.findByNodeID(nodeID).notNil; }
	
	*remove { arg defName, index = 0, cleanUp=true;
		// remove last: index = -1
		var size, removedSynth;
		SynthTracker.cleanUp(cleanUp);
		defName = defName.asSymbol;
		size = runningSynths.at(defName).size;
		if(size !== 0)
			{ removedSynth = runningSynths.at(defName).removeAt(index%size); 
				if(watch) { NodeWatcher.unregister(removedSynth); };
				^removedSynth }
			{ ^nil }
		}
	
	*removeAll { arg defName;
		if( defName.notNil )
			{ 	if(watch) { runningSynths.at(defName).do({ |item| 
				NodeWatcher.unregister(item) }); };
				runningSynths.put(defName.asSymbol, nil); }
			{ runningSynths.keys.do({ |key| SynthTracker.removeAll(key) }) }
		}
	
	*removeSynth { arg synth;
		^{ runningSynths.at(synth.defName.asSymbol).remove(synth); }.try }
		
	*release { arg defName, cleanUp=true; ^SynthTracker.releaseAt(defName, 0, cleanUp) }
	*releaseLast {  arg defName, cleanUp=true; ^SynthTracker.releaseAt(defName, -1, cleanUp) }
	
	*releaseAt { arg defName, index=0, cleanUp=true;
		var synths, freeSynth;
		SynthTracker.cleanUp(cleanUp);
		defName = defName.asSymbol;
		synths = runningSynths.at(defName);
		if(synths.notNil)
			{index = index % synths.size;
				if( SynthTracker.canRelease(defName) ) 
					{ freeSynth = synths[index].release } { freeSynth = synths[index].free };
				SynthTracker.remove(defName, index, false);
				SynthTrackerWindow.update;
				if(warn) { ("SynthTracker : '" ++ defName ++ "' ("
					++ freeSynth.nodeID ++ ") released").postln; };
				^freeSynth; }
			{ if(warn) {"SynthTracker : nothing to release".postln; ^nil;  }; };
		}
		
	*releaseAll { arg defName, cleanUp = true;
		var synths;
		if(defName.notNil)
		{
		SynthTracker.cleanUp(cleanUp);
		defName = defName.asSymbol;
		synths = runningSynths.at(defName);
		if( SynthTracker.canRelease(defName) ? false ) 
			{ synths.do( _.release ); 
			if(warn) { ("SynthTracker : all " ++ synths.size ++ 
				" running Synths of '" ++ defName ++ 
				"' released").postln; }; } 
			{ synths.do(_.free);
			if(warn) { ("SynthTracker : all " ++ synths.size ++ 
				" running Synths of '" ++ defName ++ 
				"' free-ed").postln; };  };
		SynthTracker.removeAll(defName);
		} { runningSynths.keys.do({ |key| SynthTracker.releaseAll(key, false) }) };
		SynthTrackerWindow.update;
		}
		
	*free { arg defName, cleanUp=true; ^SynthTracker.freeAt(defName, 0, cleanUp) }
	*freeLast {  arg defName, cleanUp=true; ^SynthTracker.freeAt(defName, -1, cleanUp) }
	
	*freeAt { arg defName, index=0, cleanUp=true;
		var synths, freeSynth;
		SynthTracker.cleanUp(cleanUp);
		defName = defName.asSymbol;
		synths = runningSynths.at(defName);
		index = index % synths.size;
		if(synths.notNil) { freeSynth = synths[index].free;
		SynthTracker.remove(defName, index, false);
		SynthTrackerWindow.update;
		if(warn) { ("SynthTracker : '" ++ defName ++ "' ("
					++ freeSynth.nodeID ++ ") free-ed").postln; };
		^freeSynth;}
		{ ^nil };
		}
		
	*freeAll { arg defName, cleanUp = true;
		var synths;
		SynthTracker.cleanUp;
		if(defName.notNil) {
		defName = defName.asSymbol;
		synths = runningSynths.at(defName);
		synths.do( _.free);
		if(warn) { ("SynthTracker : all " ++ synths.size ++ 
				" running Synths of '" ++ defName ++ 
				"' free-ed").postln; };  
		SynthTracker.removeAll(defName);
		} { runningSynths.keys.do({ |item| SynthTracker.freeAll(item, false) }) };
		SynthTrackerWindow.update;
		}
		
	// voicer alike support
		
	*matchArgs { |args, defName = \default|
		args = args.asArgsDict;
		defName = defName.asSymbol;
		^runningSynths.at(defName).select({ |synth|
			var theArgs, test = [];
			theArgs = synth.args ? ();
			args.keysValuesDo({ |key, value|
				test = test.add( theArgs[key] == value );
				});
			test.every( _.value );
			});
		}
	
	*matchArgsAsIndexes { |args, defName = \default|
		var synths;
		defName = defName.asSymbol;
		synths = SynthTracker.matchArgs( args, defName );
		^synths.collect({ |synth| runningSynths[defName].indexOf( synth ) });
		}
	
	*releaseArgs { |defName, args, cleanUp = true| // will release all if no args provided
		var releasedSynths;
		if( args.isNil )
			{ ^SynthTracker.releaseAll( defName, cleanUp ); }
			{ releasedSynths = SynthTracker
				.matchArgsAsIndexes(args, defName )
				.collect( { |index|
					SynthTracker.releaseAt( defName, index, false ) } );
			SynthTracker.cleanUp(cleanUp);
			^releasedSynths
			};
		}
		
	*freeArgs { |defName, args, cleanUp = true| // will release all if no args provided
		var releasedSynths;
		if( args.isNil )
			{ ^SynthTracker.freeAll( defName, cleanUp ); }
			{ releasedSynths = SynthTracker
				.matchArgsAsIndexes(args, defName )
				.collect( { |index|
					SynthTracker.freeAt( defName, index, false ) } );
			SynthTracker.cleanUp(cleanUp);
			^releasedSynths
			};
		}
		
	*toggle {  arg defName, args, canRelease, maxCount = 1, addAction = \addToTail;
		//SynthTracker.initialize;
		if(SynthTracker.isRunning(defName, maxCount))
			{SynthTracker.releaseAt(defName, 0, false); ^false; }
			{SynthTracker(defName, args, canRelease, maxCount, addAction); ^true; };
		}
	
	*replace {   arg defName, args, canRelease, maxCount = 1, addAction = \addToTail;
		//SynthTracker.initialize;
		if(SynthTracker.isRunning(defName, maxCount))
			{SynthTracker.releaseAt(defName, 0, false); };
		^SynthTracker(defName, args, canRelease, maxCount, addAction);
		}
		
	*controls { arg defName = \default; // wrapper for SynthDescLib
		defName = defName.asSymbol;
		^SynthDescLib.at(defName).controls.collect(_.name);
		}
	
	*set { arg defName = \default, argsArray = [freq: 440];
		SynthTracker.setAt(0, defName, argsArray); }
	
	*setLast {  arg defName = \default, argsArray = [freq: 440];
		SynthTracker.setAt(-1, defName, argsArray); }
		
	*setAt { arg index = 0, defName = \default, argsArray = [freq: 440];
		var synths, synth;
		//SynthTracker.initialize;
		synths = runningSynths.at(defName.asSymbol);
		if(synths.size != 0)
			{ synth = synths.wrapAt(index);
				synth.set(*argsArray.asArgsArray); }
			{ if( warn )
				 { ("SynthTracker : no instance of '" ++ defName ++ "' running").postln };
			}
		}
	
	*setMatchArgs { arg defname = \default, argsToSet = [freq: 440], argsToMatch;
		var synths;
		synths = this.matchArgs( argsToMatch, defname.asSymbol )
			.do({ |synth|
				synth.setn(*argsToSet.asArgsArray); });
		}
	
	*setn { arg defName = \default, argsArray = [freq: 440];
		SynthTracker.setnAt(0, defName, argsArray); }
	
	*setnLast {  arg defName = \default, argsArray = [freq: 440];
		SynthTracker.setnAt(-1, defName, argsArray); }
		
	*setnAt { arg index = 0, defName = \default, argsArray = [freq: 440];
		var synths, synth;
		//SynthTracker.initialize;
		synths = runningSynths.at(defName.asSymbol);
		if(synths.size != 0)
			{ synth = synths.wrapAt(index);
				synth.setn(*argsArray.asArgsArray); }
			{ if( warn )
				{ ("SynthTracker : no instance of '" ++ defName ++ "' running").postln };
			 }
		}
		
}
