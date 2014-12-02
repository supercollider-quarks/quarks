// wslib 2006
// a MIDI event recorder for SimpleMIDIFile

// the timeline is kept by Process.elapsedTime, so there are no routines running


MIDIEventRecorder {
	
	classvar <>global;
	
	
	var <maxEvents = inf;
	var <startTime = 0; 
	var <events;
	var <armed = false;
	var tempTime; 
	var <paused = false;
	var <>isRunning = true;
			
	// events format:  [ absTime (since startTime), type (\noteOn, \noteOff etc.),
	// 					channel, val1, (val2) ] 
	// event types are according to SimpleMIDIFile midiEvent types
	
	*new { |maxEvents, startTime|  // times are in seconds
		maxEvents = maxEvents ? inf;
		startTime = startTime ?? { Process.elapsedTime };
		^super.newCopyArgs( maxEvents, startTime, [] );
		}
		
	*arm { |maxEvents|   // will start recording at first 'addEvent' or 'start' call
		maxEvents = maxEvents ? inf;
		^super.newCopyArgs( maxEvents, 0, [], true );
		}
	
	start { 
		if( armed ) 
			{ if( armed ) { startTime = Process.elapsedTime; armed = false }; };
		if( paused )
			{  this.unPause };
		isRunning = true;
		}
	
	stop { this.pause; isRunning = false; }
	
	*with { |... events|
		var startTime;
		startTime =  Process.elapsedTime;
		^super.newCopyArgs( inf, startTime, events )
		}
		
	*initClass {
		global = MIDIEventRecorder.arm( 256 ); // a global collector with 256 empty spaces
		}
		
	makeGlobal { global = this; }
	
	resetGlobal { |maxEvents = 256| global = MIDIEventRecorder.arm( maxEvents ); }
	
	reset { |newmaxEvents, newStartTime| // delete all events and reset startTime;
		this.unPause;
		maxEvents = newmaxEvents ? maxEvents;
		startTime = newStartTime ?? { Process.elapsedTime; };
		events = [];
		}
	
	resetArm { this.reset; armed = true; }
	
	addEvent { | eventArray, timeOffset = 0, absTime | 
		// absTime will be calculated in this method if not provided.
		// timeOffset is added to the absTime
		if( isRunning )
			{ absTime = absTime ?? { Process.elapsedTime };
				if( armed ) { startTime = absTime; armed = false };
				events = events.add( [ ( absTime - startTime) + timeOffset ] ++ eventArray );
				if( events.size > maxEvents )  
				// when maxEvents is exceded, first event is removed
					{ events.removeAt(0) };
			};
		}
		
	currentTime { 
		if( armed ) { ^startTime };
		if( paused ) { ^tempTime - startTime };
		^Process.elapsedTime - startTime;
		}
	
	addType { |type = \noteOn, channel = 0 ... values| 
		values = values ? [64,64];
		this.addEvent( [type, channel] ++ values );
		}
	
	addNoteOn { |channel = 0, noteNr = 64, velo = 64|
		this.addEvent( [\noteOn, channel, noteNr, velo] );
		}
	
	addNoteOff { |channel = 0, noteNr = 64, velo = 64|
		this.addEvent( [\noteOff, channel, noteNr, velo] );
		}
	
	addNote { |channel = 0, noteNr = 64, velo = 64, dur = 1, upVelo|
		var absTime;
		upVelo = upVelo ? velo;
		absTime = Process.elapsedTime;
		this.addEvent( [\noteOn, channel, noteNr, velo], 0, absTime );
		this.addEvent( [\noteOff, channel, noteNr, velo], dur, absTime );
		}
	
	add { |eventArray| // should include time
		events = events.add( eventArray );
		}
	
	addAll { |arrayOfEventArrays|
		events = events ++ arrayOfEventArrays;
		}
	
	sort { |function|
		function = function ? { |a,b| a[0] <= b[0] };
		events.sort( function );
		}
		
	size { events.size; }
	
	asSimpleMIDIFileTrack { |trackNumber = 0|
		^events.collect({ |event| [trackNumber] ++ event });
		}
		
	asSimpleMIDIFile { |pathName, pitchBendMode = \int8|
		var midiFile;
		pathName = pathName ? "~/scwork/eventRecorder.mid";
		pathName = pathName.standardizePath;
		midiFile = SimpleMIDIFile( pathName ).init0; // init as format 0 file
		midiFile.timeMode_( \seconds, false );      // set timeMode to seconds
		midiFile.pitchBendMode_( pitchBendMode ); 
		midiFile.addAllMIDIEventsToTrack( events ); // add recorded events to track 0
		midiFile.adjustEndOfTrack;
		^midiFile;
		}
	
	maxEvents_ { |newMax = inf|
		maxEvents = newMax;
		if(events.size > maxEvents)
			{ events = events[ (events.size - maxEvents)..];
				("MIDIEventController : Deleted " ++ (events.size - maxEvents) ++
					" events").postln;  };
		}
	
	// pause:
	//  when pause is called the current time is stored
	//  this doesn't change anything to the behaviour until unPause
	//  is called; time is then set back, so that new added events
	//  are added right after the events from before the first pause call
	
	setPaused { |bool = true|
		var currTime;
		if( bool )
		{ tempTime = Process.elapsedTime; paused = true }
		{ 	currTime = Process.elapsedTime;
			tempTime = tempTime ?? currTime;
			startTime = startTime - (tempTime - currTime);
			paused = false };
		}
		
	pause { if( paused.not) { this.setPaused( true ); }; }  // set only once
	
	unPause { this.setPaused( false ); }
	
	paused_ { |bool = true|
		if( bool )
			{ this.pause }
			{ this.unPause }; 
		}
		
	notPaused { ^paused.not } // test
	
	shiftTime { |deltaTime|  // by default shifts first event to start
		deltaTime = deltaTime ?? { events.first[0].neg };
		events = events.collect({ |event|
			[( event[0] + deltaTime ).max(0)] ++ event[1..];
			});
		}
	
	timeLine {
		var lastTime = 0;
		^events.collect({ |event| 
			var out;
			out = event[0] - lastTime;
			lastTime = event[0];
			out; })[1..]
		}
		
	collectEventsAt { |index = 3|
		^events.collect({ |event| event[index] });
		}
		
	plot { |index = 3|
		Env( this.collectEventsAt(index), this.timeLine, \step ).plot;
		}
		
	analyzeTypes {
		var types;
		types = ();
		events.do({ |event|
			if( types.keys.includes( event[1] ) )
				{ types[ event[1] ] = types[ event[1] ] + 1; }
				{ types.put( event[1], 1 ) }
			});
		^types;
		}
		
	analyzeChannels { |type|
		var channels;
		var types;
		if( type.notNil )
			{ 	channels = ();
				events.do({ |event|
					if( event[1] == type )
						{ if( channels.keys.includes( event[2] ) )
							{ channels[ event[2] ] = channels[ event[2] ] + 1; }
							{ channels.put( event[2], 1 ) }
						}
				});
				^channels;
			}
			{ types = this.analyzeTypes;
				types.keys.do({ |key| types[key] = this.analyzeChannels( key ) });
				^types;
			}
		}
	
	analyzeCC {
		var ccChannels;
		ccChannels = this.analyzeChannels('cc');
		ccChannels.keys.do({ |channel|
			var ccNrs;
			ccNrs = ();
			events.do({ |event|
				if( ( event[1] == 'cc' ) and: ( event[2] == channel ) )
					{  if( ccNrs.keys.includes( event[3] ) )
							{ ccNrs[ event[3] ] = ccNrs[ event[3] ] + 1; }
							{ ccNrs.put( event[3], 1 ) }
						 }
					});
			ccChannels[channel] = ccNrs;
			});
		^ccChannels;
		}
		
	printOn { arg stream;
		stream << "MIDIEventRecorder( " << maxEvents << ", " << startTime << 
			if(armed) { ", armed" } { "" } <<
			if(paused) { ", paused" } { "" } <<
			 " )";
		}
		
	storeArgs {
		^[maxEvents, startTime, events];
		}		
		
	}