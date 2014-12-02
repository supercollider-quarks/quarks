// MIDIBendWindow, MIDITouchWindow, MIDIControlWindow, MIDICinetixWindow
//
// Automatically creates a window with faders for each controller
// which outputs to control busses on the default server.
// It will also work as a regular slider window even when no 
// MIDI devices are connected (to work on the road..)
//
// wslib 2005



BasicMIDIWindow {

	classvar <>responder;
	
	// so far this class is only used for MIDIBendWindow and MIDITouchWindow
	
	*addFaderAction { |n = 0, action|
		this.actionDict[n] = action }
		
	*addTreshAction { |n = 0, val = 0.5, up, down| // val, up and down can be arrays
		val = val.asCollection;
		up = up.asCollection;
		down = down.asCollection;
		this.treshActionDict[n] = { |i, v ... h|
			val.do({ |subval, j|
			if( (v >= subval) && (h[0] < subval) )
				{ up.wrapAt(j).value(i, v, *h) };
			if( (v < subval) && (h[0] >= subval) )
				{ down.wrapAt(j).value(i, v, *h) };
				});
			};
		}
		
	*midiRecorderAdd { |i, val| // by default record \cc7 values 
			if( this.midiRecorder.notNil ) 
				{ this.midiRecorder.addType( \cc, i, 7, val * 127 ); };
		}
		
	*playFile { |file, doneAction| // file = midiFile
		var events;
		
		events = file.asDicts.select( { |item|
					( (item.type === 'cc') && { item.val1 === 7 } ); } );
							
		events.collect( _.absTime ).do({ |time, i|
			SystemClock.sched( time, { 
				{ this.sliders[ events[i].channel ].valueAction
							= events[i].val2 / 127; }.defer;
					});
					
			});
			
		SystemClock.sched( ((events.last) ? (absTime: 0)).absTime, { doneAction.defer } );
		
		}
		
	*importFile { |file, doBefore|
		this.midiRecorder.addAll( file.volumeEvents.collect({ |event| event[1..] }) );
		}
		
	*playMIDIFile { |path|
			var file;
			file = SimpleMIDIFile( path ).read;
			file.timeMode = \seconds;
			file.pitchBendMode = \float;
			this.midiRecorder.resetArm; 
			this.midiRecorder.isRunning = false;
			this.playFile( file );
		}
		
	*close { var window = this.window; if(window.notNil) 
		{ window.close; this.window = nil;  this.disconnect; this.window = nil; } }
		
	*resetActions {
		this.action = nil;
		this.treshActionDict = ();
		this.actionDict = ();
		 }
	
	*new1 {arg nC = 16, busOffset = 0, initValues = [], historySize = 1, 
		maxMIDIVal = 127, inAction, windowName, windowOffset = Point(540,10), windowH = 290;
	
		var midiValues, busMessage, fullAction, midiSliders, midiFunc, midiRoutineButton;
		var history;
		var window;
		
		window = this.window;

		history = (0!historySize)!nC;  // previous values are passed to action
		this.action = inAction ? this.action;
		
		if(window.isNil) {
			midiValues = { |i| initValues[i] ? 0 }!nC;
			window = SCWindow(nC.asString ++ " x " ++ windowName, 
				Rect(windowOffset.x, windowOffset.y, (nC * 40) + 30, windowH), false);
				
			this.midiRecorder = MIDIEventRecorder.arm.isRunning_( false );
				
			busMessage = { |chan, val| Server.default.sendMsg("/c_set", chan + busOffset, val) };
			
			fullAction = { |i, val| 
				this.action.value(i, val, *history[i]); // evaluate global action
				this.actionDict[i].value(i, val, *history[i]); // evaluate action from dict
				this.treshActionDict[i].value(i, val, *history[i]);
				this.midiRecorderAdd( i, val );
				history[i].pop;
				history[i].insert(0,val); };
			
			midiSliders = Array.fill(nC, { |i|
				SCStaticText(window, Rect(20 + (i*40), windowH - 28, 30, 20))
					.string_(i + 1).align_(\center);
			    SmoothSlider(window, Rect(20 + (i*40), 40, 30, windowH - 70))
					.action_({ |slider|
				var val = slider.value;
				if(this.updateBusses) { midiValues[i] = val; busMessage.(i, val); };
				fullAction.(i, val);
				
				}).canFocus_( false )
				.background_( Color.gray ).hilightColor_( Color.blue.alpha_(0.5) );
			});
	
		midiFunc = {arg src, chan, num, vel;
			var val;
			if(chan < nC) { 
				val = num / maxMIDIVal; 
				if(this.updateBusses) { busMessage.(chan, val); };
				if(this.updateFaders)
					{	midiValues[chan] = val;
						{	midiSliders.do({ |item, i|
							item.value = midiValues[i]; }); }.defer
						};
			fullAction.(chan, val);
			
			};
		};
	
		midiRoutineButton = RoundButton(window, Rect(20, 10, 58, 18))
			.states_([["MIDI"],["MIDI", Color.red.blend( Color.white, 0.5 ), Color.gray(0.2)]])
			.value_(1).extrude_( false )
			.canFocus_( false )
			.action_({ |button| case 
				{button.value == 1}
					{ this.connect(midiFunc); }
				{button.value == 0}
					{ this.disconnect }
			});

		RoundButton(window, Rect(90, 10, 68, 18))
			.states_([["to busses"],["to busses", Color.red.blend( Color.white, 0.5 ), 
				Color.gray(0.2)]])
			.value_(this.updateBusses.binaryValue).extrude_( false )
			.canFocus_( false )
			.action_({ |button| case 
				{button.value == 1}
					{ this.updateBusses = true }
				{button.value == 0}
					{ this.updateBusses = false }
				});
			
		{ var composite, recButton, playButton;
			
			composite = SCHLayoutView( window, Rect( ( 10 + (nC * 40)) - 151, 10, 151, 18 ) )
				.spacing_( 2 );
				//.background_( Color.white );
			
			SCStaticText( composite, 45@20 ).string_( "record:" ).setProperty( \align, \center );
			
			recButton = RoundButton( composite, 18@18 )
				.states_( [ 
					[ \record, Color.red(0.75), Color.clear ] , 
					[ \stop , Color.black, Color.red(0.75) ] ] )
				.extrude_( false ).canFocus_( false )
				.action_( { |button|
					if( button.value == 1 )
						{  if( playButton.value == 1 ) { playButton.valueAction = 0 };
							this.midiRecorder.start;
							/*
							this.sliders.do({ |sl, i|
								this.midiRecorderAdd( i, sl.value ) });
							*/
							}
						{  this.midiRecorder.stop;  }
					} );
					
			playButton = RoundButton( composite, 18@18 )
				.states_( [ [ \play, Color.green(0.75), Color.clear ] , 
					[ \stop, Color.green(0.25), Color.clear ] ] )
				.extrude_( false ).canFocus_( false )
				.action_( { |button|
					var file, events, clock; 
					if( button.value == 1 )
						{ 	if( recButton.value == 1 ) { recButton.valueAction = 0 };
							
							file = this.midiRecorder.asSimpleMIDIFile( pitchBendMode: \float );							
							this.playFile( file, { button.value = 0 } );
							
								
							}
						{ SystemClock.clear; }
					} );
			
			SCPopUpMenu( composite, 60@20 )
					.items_( ["(options", /*)*/ "-", "save..", "open..", "plot", "clear" ] )
					.canFocus_( false )
					.action_( { |pu|
						var file;
						case { pu.value == 2 }
							 {
							CocoaDialog.savePanel( { |path|
								file = this.midiRecorder
									.asSimpleMIDIFile( pitchBendMode: \float );
								file.checkWrite( path );
								} ); }
							{ pu.value == 3 }
							{  CocoaDialog.getPaths( { |paths|
								file = SimpleMIDIFile( paths[0] ).read;
								file.timeMode = \seconds;
								if( recButton.value == 1 ) { recButton.valueAction = 0 };
								if( playButton.value == 1 ) { playButton.valueAction = 0 };
								this.midiRecorder.resetArm; 
								this.midiRecorder.isRunning = false;
								this.importFile( file );
								} );
							}  
							{ pu.value == 4 }
							{ this.midiRecorder
								.asSimpleMIDIFile( pitchBendMode: \float ).plot; }
							{ pu.value == 5 }
							{ 	if( recButton.value == 1 ) { recButton.valueAction = 0 };
								if( playButton.value == 1 ) { playButton.valueAction = 0 };
								this.midiRecorder.resetArm; 
								this.midiRecorder.isRunning = false; };
						pu.value = 0;
						} );
				
			}.value;
			
			
			MIDIWindow.new;
			this.connect(midiFunc);
			window.onClose_({ window = nil; this.disconnect; this.window = nil });
			this.window = window;
			this.sliders = midiSliders;
	};
	window.front;
	}
}

MIDIBendWindow : BasicMIDIWindow {

	// 16 channels of pitchbend values to control busses
	// only one of these can be working at the same time
	//
	// Designed for Peavey 1600x preset "Pitch Correct" with 16 channels
	// pitchbend. Fine adjustment is done with it's wheel.
	//
	// example of action:
	// action = { |channel, value, lastValue| [channel, value, lastValue].postln; }
	
	classvar <>window = nil;
	classvar <>action = nil;
	classvar <>treshActionDict = nil, <>actionDict = nil;
	classvar <>updateFaders = true, <>updateBusses = true;
	classvar <>sliders = nil;
	classvar <>midiRecorder;
		
	*initClass { actionDict = (); treshActionDict = (); }
		/* cannot state an array or dict in the classvars.. */ 
		
	*new { arg nC = 16, busOffset = 0, initValues = [], historySize = 1, 
		maxMIDIVal = 16383, inAction, left = 580, bottom = 30, height = 290;
		^this.new1(nC, busOffset, initValues, historySize, maxMIDIVal, inAction, "pitchbend",
			Point(left, bottom), height); }
			
	// preset for lower res 16 ch version:
	*new1024 {arg inAction; MIDIBendWindow(maxMIDIVal: 1024, inAction:inAction) }

	
	*connect { |midiFunc| if( responder.isNil ) { responder = BendResponder( midiFunc ); } }
	*disconnect { responder.remove; responder = nil; }
	
	*midiRecorderAdd { |i, val|
			if( midiRecorder.notNil ) { midiRecorder.addType( \pitchBend, i, (val * 2) - 1 ); };
		}
		
	*playFile { |file, doneAction| // file = midiFile
		var events;
		
		events = file.asDicts.select( { |item|
					( item.type === 'pitchBend' ); } );
							
		events.collect( _.absTime ).do({ |time, i|
			SystemClock.sched( time, { 
				{ this.sliders[ events[i].channel ].valueAction 
							= ( events[i].val1 + 1 ) / 2; }.defer;
					});
					
			});
			
		SystemClock.sched( ((events.last) ? (absTime: 0)).absTime, { doneAction.defer } );
		
		}
		
	*importFile { |file|
		file.pitchBendMode = \float;
		this.midiRecorder.addAll( file.pitchBendEvents.collect({ |event| event[1..] }) );
		
		}
	
	}


MIDITouchWindow : BasicMIDIWindow  {

	// 16 channels of midi aftertouch, the smallest continous
	// channel related controller message (as far as MIDI can be
	// continous.. )
	//
	// Only one of these can be working at the same time
	//
	// example for action:
	// action = { |channel, value| [channel, value, lastValue].postln; }
	
	classvar <>action = nil;
	classvar <>updateFaders = true, <>updateBusses = true;
	classvar <>treshActionDict = nil, <>actionDict = nil;
	
	classvar <>window = nil;
	classvar <>sliders = nil;
	classvar <windowType = 'sliders';
	classvar <>midiRecorder;
	
	
	*initClass { actionDict = (); treshActionDict = (); }
		/* cannot state an array or dict in the classvars.. */ 
		
	*new {arg nC = 16, busOffset = 48, initValues = [], historySize = 1, inAction;
		^this.new1(nC, busOffset, initValues, historySize, 127, inAction, "aftertouch",
			Point(540,330), 190); 
		}
	
	/*
	*connect { |midiFunc| MIDIIn.touch = midiFunc; }
	*disconnect { MIDIIn.touch = nil; }
	*/
	
	*connect { |midiFunc| if( responder.isNil ) { responder = TouchResponder( midiFunc ); } }
	*disconnect { responder.remove; responder = nil; }

	
	*midiRecorderAdd { |i, val|
			if( midiRecorder.notNil ) { midiRecorder.addType( \afterTouch, i, val * 127 ); };
		}
		
	*playFile { |file, doneAction| // file = midiFile
		var events;
		
		events = file.asDicts.select( { |item|
					( item.type === 'afterTouch' ); } );
							
		events.collect( _.absTime ).do({ |time, i|
			SystemClock.sched( time, { 
				{ this.sliders[ events[i].channel ].valueAction 
							= events[i].val1 / 127; }.defer;
					});
					
			});
			
		SystemClock.sched( ((events.last) ? (absTime: 0)).absTime, { doneAction.defer } );
		
		}
		
	*importFile { |file|
		this.midiRecorder.addAll( file.afterTouchEvents.collect({ |event| event[1..] }) );
		}


	// different implementation for buttons (not based on BasicMIDIWindow)
	
*buttonActions { arg downAction, upAction; // replace or remove buttonactions
	action = if( upAction.notNil or: downAction.notNil) 
	{ { |chan, val| [upAction, downAction][val].(chan) }; }
	{ nil };
	^action;
	}

*buttons {arg nC = 16, downAction, upAction, busOffset = 48, toBusses = false;
	
// Switches to 1 ("down") when value goes above 0.5 (64 in MIDI values)
// by default doesn't send to buses; meant for lang-side use.
// Not optimised for fader use: the upAction or downAction will be executed
// every time a value is received. Please only use with two way buttons on a
// controller, otherwise use the regular fader version of this class.
//
// downAction example:
//
// { |chan| (0: {"down ch 1".postln}, 1: {"down ch 2".postln})[chan].value; }
 
var midiValues, busMessage, midiSliders, midiFunc, midiRoutineButton;
var inAction;

if((windowType == 'sliders') && (window.notNil))
	{window.close; MIDIIn.touch = nil; window = nil;};
windowType = 'buttons';

inAction = if( upAction.notNil or: downAction.notNil) 
	{ { |chan, val| [upAction, downAction][val].(chan) }; }
	{ nil };
	
action = inAction ? action;

if(toBusses.notNil) { updateBusses = toBusses };

if(window.isNil) {

window = SCWindow(nC.asString ++ " x aftertouch buttons", Rect(580, 355, (nC * 40) + 30, 80), false);
midiValues = 0!nC;
busMessage = { |chan, val| Server.default.sendMsg("/c_set", chan + busOffset, val) };

midiSliders = Array.fill(nC, { |i|  // these are of course not sliders
		RoundButton(window, Rect(20 + (i*40), 40, 30, 30))
					.states_([[(i+1).asString],[(i+1).asString,  Color.red, Color.black]])
					.extrude_( false )
					.radius_( 8 )
					.action_({ |button|
			var val = button.value;
			if(updateBusses) { midiValues[i] = val; busMessage.(i, val); };
			action.value(i, val);
				});
	});
	
midiFunc = {arg src, chan, val;
	if(chan < nC)
		{
		val = ( val > 64 ).binaryValue;
		if(updateBusses)
			{ busMessage.(chan, val); };
		if(updateFaders)
			{	midiValues[chan] = val;
				{midiSliders[chan].value = val;}.defer;
				//{midiSliders.do({ |item, i| item.value = midiValues[i]; }); }.defer
			};
		action.value(chan, val);
		};
	};
	
midiRoutineButton = RoundButton(window, Rect(20, 10, 58, 18))
		.states_([["MIDI"],["MIDI", Color.red.blend( Color.white, 0.5 ), Color.gray(0.2)]])
		.value_(1).extrude_( false )
		.action_({ |button| case 
			{button.value == 1}
				{ MIDIIn.touch = midiFunc }
			{button.value == 0}
				{ MIDIIn.touch = nil }
			});
	
RoundButton(window, Rect(90, 10, 68, 18))
		.states_([["to busses"],["to busses", Color.red.blend( Color.white, 0.5 ), Color.gray(0.2)]])
		.value_(updateBusses.binaryValue).extrude_( false )
		.action_({ |button| case 
			{button.value == 1}
				{ updateBusses = true }
			{button.value == 0}
				{ updateBusses = false }
			});

MIDIWindow.new;
//MIDIIn.touch = midiFunc;
this.connect( midiFunc );
window.onClose_({ 
	window = nil; MIDIIn.disconnect });
};
window.front;

	}

}


MIDIControlWindow {
	// based on above
	//
	// 16 channels of cc values to control busses
	// only one of these can be working at the same time
	// * when 'type' is set to 'channel', it will map the
	//   values of controller number 'cc' on every channel
	//   from 'ch' to ('ch' + 'nC') to the sliders.
	//   -> ch1, cc7 to slider1,  ch2, cc7 ot slider2, etc. 
	// * when 'type' is not set to 'channel', it will map
	//   all controller values on channel 'ch' from 'cc' to
	//   ('cc' + 'nC') to the sliders.
	//	-> ch1, cc1 to slider1,  ch1, cc2 to slider2, etc.
	//
	// the default setting is 16 channels of volume controllers
	//
	// example for action:
	// action = { |channel, value, lastValue| [channel, value, lastValue].postln; }
	
	classvar <>action = nil;
	classvar <>updateFaders = true, <>updateBusses = true;
	classvar <>treshActionDict = nil, <>actionDict = nil;
	classvar <>window;
	classvar <>sliders = nil;
	classvar <>ccType = 'channel';
	
	*close { if(window.notNil) { window.close; window = nil;  MIDIIn.control = nil;  } }
	
	*initClass { actionDict = (); treshActionDict = ();
		/* cannot state an array or dict in the classvars.. */ }
	
	*addFaderAction { |n = 0, action|
		actionDict[n] = action }
		
	*addTreshAction { |n = 0, val = 0.5, up, down| // val, up and down can be arrays
		val = val.asCollection;
		up = up.asCollection;
		down = down.asCollection;
		treshActionDict[n] = { |i, v ... h|
			val.do({ |subval, j|
			if( (v >= subval) && (h[0] < subval) )
				{ up.wrapAt(j).value(i, v, *h) };
			if( (v < subval) && (h[0] >= subval) )
				{ down.wrapAt(j).value(i, v, *h) };
				});
			};
			
		}

	
	*new {arg nC = 16, busOffset = 48, type='channel', cc=7, ch=0, initValues = [], historySize = 1, inAction;
	
var midiValues, busMessage, midiSliders, midiFunc, midiRoutineButton;
var history;

history = (0!historySize)!nC; 

action = inAction ? action;
ccType = type ? ccType;
if(window.isNil) {

window = SCWindow(nC.asString ++ " x controller", Rect(540, 330, (nC * 40) + 30, 190), false);
midiValues = { |i| initValues[i] ? 0 }!nC;
busMessage = { |chan, val| Server.default.sendMsg("/c_set", chan + busOffset, val) };

midiSliders = Array.fill(nC, { |i|
		SCStaticText(window, Rect(20 + (i*40), 162, 30, 20))
					.string_(i + 1).align_(\center);
		SmoothSlider(window, Rect(20 + (i*40), 40, 30, 120))
					.action_({ |slider|
			var val = slider.value;
			if(updateBusses) { midiValues[i] = val; busMessage.(i, val); };
			action.value(i, val, *history[i]);
			actionDict[i].value(i, val, *history[i]); // evaluate action from dict
			treshActionDict[i].value(i, val, *history[i]);
			history[i].pop;
			history[i].insert(0,val);
				}).canFocus_( false )
				.background_( Color.gray ).hilightColor_( Color.blue.alpha_(0.5) );
	});
	
midiFunc = {arg src, inChan, inNum, inVal;
	var val, chan;
	if(type == 'channel')
		{ if(inNum == cc)
			{chan = inChan + ch}
			{chan = nC + 1}; }
		{ if(inChan == ch)
			{chan = inNum + cc }
			{chan = nC + 1};
		};
	if(chan < nC)
		{
		val = inVal / 127;
		if(updateBusses)
			{ busMessage.(chan, val); };
		if(updateFaders)
			{	midiValues[chan] = val;
				{midiSliders.do({ |item, i|
					item.value = midiValues[i]; }); }.defer
			};
		action.value(chan, val, *history[chan]);
		actionDict[chan].value(chan, val, *history[chan]); // evaluate action from dict
		treshActionDict[chan].value(chan, val, *history[chan]);

		history[chan].pop;
		history[chan].insert(0,val);
		};
	};
	
midiRoutineButton = SCButton(window, Rect(20, 10, 58, 18))
		.states_([["MIDI"],["MIDI", Color.red, Color.black]])
		.value_(1)
		.action_({ |button| case 
			{button.value == 1}
				{ MIDIIn.control = midiFunc }
			{button.value == 0}
				{ MIDIIn.control = nil }
			});
	
SCButton(window, Rect(90, 10, 68, 18))
		.states_([["to busses"],["to busses", Color.red, Color.black]])
		.value_(updateBusses.binaryValue)
		.action_({ |button| case 
			{button.value == 1}
				{ updateBusses = true }
			{button.value == 0}
				{ updateBusses = false }
			});

MIDIWindow.new;
MIDIIn.control = midiFunc;
window.onClose_({ window = nil; MIDIIn.control = nil });
};
window.front;

	}
}


MIDICinetixWindow {
	// convert cinetix cv to midi machines output (www.cinetix.de)
	// the cheapest on the market, but with a little strange midi behaviour..
	//
	// uses routine for cpu savings
	
	classvar <window = nil;
	classvar <routineIsOn = false;
	classvar <updateFaders = true, <>updateBusses = true, <>useFaders = true;
	classvar <>action = nil;
	classvar <>sliders = nil;
	classvar <>port = nil; // set to uid for specific port
	
	*close { var func; if(window.notNil) { func = window.onClose; window.close;  
		func.value;
	  	}
	  }
	
	*wheels { arg busOffset = 32, inAction;
		// preset for my own wheel controller
		^this.new([4,1,2,5], busOffset, [0],inAction) }
	
	*new { arg channels = 4 , busOffset = 32, reverse, inAction;
var busMessage = { |chan, val| Server.default.sendMsg("/c_set", chan + busOffset, val) };
var routine, routineButton;
var values;
if(channels.size == 0) { channels = (_.asInt)!channels };
values = channels.collect({0.0});
MIDIWindow.new;

action = inAction ? action;
if(reverse.isNil) {reverse = []};

if(window.isNil) {
window = Window.new(channels.size.asString ++ " x Cinetix", 
	Rect(1020, 470, 30 + (channels.size * 40), 320), false); 
window.front;

// 4.do({ |i| Bus(\control, busOffset + i, 1).get({ |val| values[i] = val }); }); // no good

sliders = Array.fill(channels.size, { | i | 
	SmoothSlider(window, Rect(20 + (i * 40), 40, 30, window.view.bounds.height - 50))
		.value_(values[i])
		.canFocus_( false )
		.clipMode_( \wrap )
		.action_({ |slider|
			action.value( i, slider.value); 
			if(useFaders && updateBusses)
			{ busMessage.(i, slider.value) };
			});
		 });
	
routine = Routine({
	var event, val, slNum;
	loop {   // event: b=num, c=vel
		event = MIDIIn.waitNoteOn( port );
		//slNum =  (1: 1, 2: 2, 4: 0, 5: 3).at(event.b.wrap(0,15));
		slNum = channels.indexOf(event.b.wrap(0,15));
		if (slNum.notNil)
			{ 
			val = ((event.c * 8) + ((event.b / 16).round(1))) / 1024; //the actual conversion
			if (reverse.includes(slNum)) {val = 1-val };
			if(updateBusses) {busMessage.(slNum, val); };
				if(updateFaders)
				{values[slNum] = val; 
					{4.do({ |i| sliders.at(i).value_(values[i]); }) }.defer; };
				action.value( slNum, val); 
			};
	};
}).play(AppClock);

routineIsOn = true;

routineButton = RoundButton(window, Rect(20, 10, 58, 18))
		.states_([["run"],["stop", Color.red(0.8), Color.gray(0.2)]])
		.extrude_( false )
		.value_(if(routineIsOn) {1} {0})
		.action_({ |button|
			case {button.value == 1}
				{ routine.stop; routine.reset;
					routine.play(AppClock); 
					routineIsOn = true;}
				{button.value == 0}
				{ routine.stop;
					routineIsOn = false;} 
			});
			
RoundButton(window, Rect(90, 10, 70, 18))
		.states_([["faders"],["faders", Color.red(0.8), Color.gray(0.2)]])
		.extrude_( false )
		.value_(if(updateFaders) {1} {0})
		.action_({ |button|
			case {button.value == 1}
				{ updateFaders = true;}
				{button.value == 0}
				{ updateFaders = false;} 
			});

window.onClose_({ 
	//window.bounds.postln;
	routine.stop; routineIsOn = false;
	window = nil;
	});
};

}
}


/*
MIDITouchWindow {
	// based on above
	//
	// 16 channels of midi aftetouch, the smallest continous
	// channel related controller message (as far as MIDI can be
	// continous.. 
	//
	// Only one of these can be working at the same time
	//
	// example for action:
	// action = { |channel, value| [channel, value, lastValue].postln; }
	
	classvar <>action = nil;
	classvar <>updateFaders = true, <>updateBusses = true;
	classvar <>treshActionDict = nil, <>actionDict = nil;
	
	classvar <>window = nil;
	classvar <windowType = 'sliders';
	
	*close { if(window.notNil) { window.close; window = nil;  MIDIIn.touch = nil;  } }
	
	*initClass { actionDict = (); treshActionDict = ();
		/* cannot state an array or dict in the classvars.. */ }
	
	*addFaderAction { |n = 0, action|
		actionDict[n] = action }
		
	*addTreshAction { |n = 0, val = 0.5, up, down| // val, up and down can be arrays
		val = val.asCollection;
		up = up.asCollection;
		down = down.asCollection;
		treshActionDict[n] = { |i, v ... h|
			val.do({ |subval, j|
			if( (v >= subval) && (h[0] < subval) )
				{ up.wrapAt(j).value(i, v, *h) };
			if( (v < subval) && (h[0] >= subval) )
				{ down.wrapAt(j).value(i, v, *h) };
				});
			};
			
		}
	
	*new {arg nC = 16, busOffset = 48, initValues = [], historySize = 1, inAction;
	
var midiValues, busMessage, midiSliders, midiFunc, midiRoutineButton;
var history;

history = (0!historySize)!nC; 

if((windowType == 'buttons') && (window.notNil))
	{window.close; MIDIIn.touch = nil; window = nil;};
windowType = 'sliders';

action = inAction ? action;
if(window.isNil) {

window = SCWindow(nC.asString ++ " x aftertouch", Rect(540, 330, (nC * 40) + 30, 190), false);
midiValues = { |i| initValues[i] ? 0 }!nC;
busMessage = { |chan, val| Server.default.sendMsg("/c_set", chan + busOffset, val) };

midiSliders = Array.fill(nC, { |i|
		SCStaticText(window, Rect(20 + (i*40), 162, 30, 20))
					.string_(i + 1).align_(\center);
		SCSlider(window, Rect(20 + (i*40), 40, 30, 120))
					.action_({ |slider|
			var val = slider.value;
			if(updateBusses) { midiValues[i] = val; busMessage.(i, val); };
			action.value(i, val, *history[i]);
			actionDict[i].value(i, val, *history[i]); // evaluate action from dict
			treshActionDict[i].value(i, val, *history[i]);
			history[i].pop;
			history[i].insert(0,val);
				});
	});
	
midiFunc = {arg src, chan, val;
	if(chan < nC)
		{
		val = val / 127;
		if(updateBusses)
			{ busMessage.(chan, val); };
		if(updateFaders)
			{	midiValues[chan] = val;
				{midiSliders.do({ |item, i|
					item.value = midiValues[i]; }); }.defer
			};
		action.value(chan, val, *history[chan]);
		actionDict[chan].value(chan, val, *history[chan]); // evaluate action from dict
		treshActionDict[chan].value(chan, val, *history[chan]);

		history[chan].pop;
		history[chan].insert(0,val);
		};
	};
	
midiRoutineButton = SCButton(window, Rect(20, 10, 58, 18))
		.states_([["MIDI"],["MIDI", Color.red, Color.black]])
		.value_(1)
		.action_({ |button| case 
			{button.value == 1}
				{ MIDIIn.touch = midiFunc }
			{button.value == 0}
				{ MIDIIn.touch = nil }
			});
	
SCButton(window, Rect(90, 10, 68, 18))
		.states_([["to busses"],["to busses", Color.red, Color.black]])
		.value_(updateBusses.binaryValue)
		.action_({ |button| case 
			{button.value == 1}
				{ updateBusses = true }
			{button.value == 0}
				{ updateBusses = false }
			});

MIDIWindow.new;
MIDIIn.touch = midiFunc;
window.onClose_({ window = nil; MIDIIn.touch = nil });
};
window.front;

	}
	
	
*buttonActions { arg downAction, upAction; // replace or remove buttonactions
	action = if( upAction.notNil or: downAction.notNil) 
	{ { |chan, val| [upAction, downAction][val].(chan) }; }
	{ nil };
	^action;
	}

*buttons {arg nC = 16, downAction, upAction, busOffset = 48, toBusses = false;
	
// Switches to 1 ("down") when value goes above 0.5 (64 in MIDI values)
// by default doesn't send to buses; meant for lang-side use.
// Not optimised for fader use: the upAction or downAction will be executed
// every time a value is received. Please only use with two way buttons on a
// controller, otherwise use the regular fader version of this class.
//
// downAction example:
//
// { |chan| (0: {"down ch 1".postln}, 1: {"down ch 2".postln})[chan].value; }
 
var midiValues, busMessage, midiSliders, midiFunc, midiRoutineButton;
var inAction;

if((windowType == 'sliders') && (window.notNil))
	{window.close; MIDIIn.touch = nil; window = nil;};
windowType = 'buttons';

inAction = if( upAction.notNil or: downAction.notNil) 
	{ { |chan, val| [upAction, downAction][val].(chan) }; }
	{ nil };
	
action = inAction ? action;

if(toBusses.notNil) { updateBusses = toBusses };

if(window.isNil) {

window = SCWindow(nC.asString ++ " x aftertouch buttons", Rect(540, 330, (nC * 40) + 30, 80), false);
midiValues = 0!nC;
busMessage = { |chan, val| Server.default.sendMsg("/c_set", chan + busOffset, val) };

midiSliders = Array.fill(nC, { |i|  // these are of course not sliders
		SCButton(window, Rect(20 + (i*40), 40, 30, 30))
					.states_([[(i+1).asString],[(i+1).asString,  Color.red, Color.black]])
					.action_({ |button|
			var val = button.value;
			if(updateBusses) { midiValues[i] = val; busMessage.(i, val); };
			action.value(i, val);
				});
	});
	
midiFunc = {arg src, chan, val;
	if(chan < nC)
		{
		val = ( val > 64 ).binaryValue;
		if(updateBusses)
			{ busMessage.(chan, val); };
		if(updateFaders)
			{	midiValues[chan] = val;
				{midiSliders[chan].value = val;}.defer;
				//{midiSliders.do({ |item, i| item.value = midiValues[i]; }); }.defer
			};
		action.value(chan, val);
		};
	};
	
midiRoutineButton = SCButton(window, Rect(20, 10, 58, 18))
		.states_([["MIDI"],["MIDI", Color.red, Color.black]])
		.value_(1)
		.action_({ |button| case 
			{button.value == 1}
				{ MIDIIn.touch = midiFunc }
			{button.value == 0}
				{ MIDIIn.touch = nil }
			});
	
SCButton(window, Rect(90, 10, 68, 18))
		.states_([["to busses"],["to busses", Color.red, Color.black]])
		.value_(updateBusses.binaryValue)
		.action_({ |button| case 
			{button.value == 1}
				{ updateBusses = true }
			{button.value == 0}
				{ updateBusses = false }
			});

MIDIWindow.new;
MIDIIn.touch = midiFunc;
window.onClose_({ 
	window = nil; MIDIIn.touch = nil });
};
window.front;

	}



}

*/

/*
MIDIBendWindow {
	
	
	classvar <window = nil;
	classvar <>action = nil;
	classvar <>treshActionDict = nil, <>actionDict = nil;
	classvar <>updateFaders = true, <>updateBusses = true;
	
	// preset for lower res 16 ch version:
	*new1024 {arg inAction; MIDIBendWindow(maxMIDIVal: 1024, inAction:inAction) }
	
	*close { if(window.notNil) { window.close; window = nil;  MIDIIn.bend = nil;  } }
	
	*initClass { actionDict = (); treshActionDict = ();
		/* cannot state an array or dict in the classvars.. */ }
	
	*addFaderAction { |n = 0, action|
		actionDict[n] = action }
		
	*addTreshAction { |n = 0, val = 0.5, up, down| // val, up and down can be arrays
		val = val.asCollection;
		up = up.asCollection;
		down = down.asCollection;
		treshActionDict[n] = { |i, v ... h|
			val.do({ |subval, j|
			if( (v >= subval) && (h[0] < subval) )
				{ up.wrapAt(j).value(i, v, *h) };
			if( (v < subval) && (h[0] >= subval) )
				{ down.wrapAt(j).value(i, v, *h) };
				});
			};
		}
	
	*new {arg nC = 16, busOffset = 0, initValues = [], historySize = 1, 
		maxMIDIVal = 16383, inAction;
	
var midiValues, busMessage, midiSliders, midiFunc, midiRoutineButton;
var history;

history = (0!historySize)!nC;  // previous values are passed to action

action = inAction ? action;
if(window.isNil) {

midiValues = { |i| initValues[i] ? 0 }!nC;
window = SCWindow(nC.asString ++ " x pitchbend", Rect(540,10, (nC * 40) + 30, 290), false);
busMessage = { |chan, val| Server.default.sendMsg("/c_set", chan + busOffset, val) };

midiSliders = Array.fill(nC, { |i|
		
		SCStaticText(window, Rect(20 + (i*40), 262, 30, 20))
					.string_(i + 1).align_(\center);
		SCSlider(window, Rect(20 + (i*40), 40, 30, 220))
					.action_({ |slider|
			var val = slider.value;
			if(updateBusses) { midiValues[i] = val; busMessage.(i, val); };
			action.value(i, val, *history[i]); // evaluate global action
			actionDict[i].value(i, val, *history[i]); // evaluate action from dict
			treshActionDict[i].value(i, val, *history[i]);
			history[i].pop;
			history[i].insert(0,val);
			
				});
	});
	
midiFunc = {arg src, chan, num, vel;
	var val;
	if(chan < nC)
		{
		val = num / maxMIDIVal; // 
		if(updateBusses)
			{ busMessage.(chan, val); };
		if(updateFaders)
			{	midiValues[chan] = val;
				{midiSliders.do({ |item, i|
					item.value = midiValues[i]; }); }.defer
			};
		action.value(chan, val, *history[chan]);
		actionDict[chan].value(chan, val, *history[chan]); // evaluate action from dict
		treshActionDict[chan].value(chan, val, *history[chan]);
		history[chan].pop;
		history[chan].insert(0,val);
		};
	};
	
midiRoutineButton = SCButton(window, Rect(20, 10, 58, 18))
		.states_([["MIDI"],["MIDI", Color.red, Color.black]])
		.value_(1)
		.action_({ |button| case 
			{button.value == 1}
				{ MIDIIn.bend = midiFunc }
			{button.value == 0}
				{ MIDIIn.bend = nil }
		});

SCButton(window, Rect(90, 10, 68, 18))
		.states_([["to busses"],["to busses", Color.red, Color.black]])
		.value_(updateBusses.binaryValue)
		.action_({ |button| case 
			{button.value == 1}
				{ updateBusses = true }
			{button.value == 0}
				{ updateBusses = false }
			});


MIDIWindow.new;
MIDIIn.bend = midiFunc;
window.onClose_({ window = nil; MIDIIn.bend = nil });
};

window.front;

	}
}
*/