// wslib 2006
// basic transport window.
// user can add actions to play, stop, return and counter

// note that the movement of the counter is included in the actions; 
// if you replace actions the counter will not count anymore

// requires SMPTEView and SMPTE classes


TransportWindow {
	classvar <>default; 
	
	var <pos, <>step; // position in seconds
	
	// views:
	var window, play, stop, return, <counter, <top;
	var <>playAction, <>stopAction, <>returnAction, <>counterAction;
	
	var <countFunc; 
	var <pressed, <active;
		
	var <>playRoutine; // not always needed
	var <window2; // ??
	
	*new { |name = "transport", startPos = 0|
		^super.new.init( name, startPos ).initActions;
		}
		
	makeDefault { default = this; }
	
	init { |name, startPos|
		var countStream, step, 
		window = Window( name, Rect( 360, 30, 300, 100 ), false );
		window.front;
		
		pos = startPos;
		pressed = (play: false, stop: false, return: false );
		active = (play: false, stop: false, return: false );
		
		// buttons: (these could be wrapped in a standardized round button class later)
		// play button
		play = RoundButton( window, Rect( 10, 5, 120, 50 ) )
			.states_( [
				[ \play, Color.green(0.5), Color.gray(0.66) ],
				[ \play, Color.green, Color.gray(0.33) ],
				] )
			.action_({ |v|
				if( v.value == 0 )
					{ v.value = 1; }
					{ stop.value = 1; return.value = 0;
						playAction.value( this ); };
				//v.focus( false );
				})
			.canFocus_( false );
			
		// stop button
		stop = RoundButton( window, Rect(  73, 60, 57, 30 ) )
			.states_( [
				[ \stop, Color.black.blend( Color.gray(0.66), 0.75 ), Color.gray(0.66) ],
				[ \stop, Color.black, Color.gray(0.66) ]
				] )
			.action_({ |v|
				if( v.value == 0 )
					{ play.value = 0; return.value = 1;
						stopAction.value( this ); }
					{ v.value = 0 };
				//v.focus( false );
				})
			.canFocus_( false );
		
		// return to 0 button
		return = RoundButton( window, Rect(  10, 60, 57, 30 ) )
			.states_( [
				[ \return, Color.black.blend( Color.gray(0.66), 0.75 ), Color.gray(0.66) ],
				[ \return, Color.black, Color.gray(0.66) ]] )
			.action_({ |v|
				if( v.value == 0 )
					{ returnAction.value( this ); }
					{ v.value = 0 };
				//v.focus( false );
				})
			.canFocus_( false );
		
		// counter
		counter = SMPTEBox( window, Rect( 145, 35, 150, 24 ) )
			.value_( pos )
			.radius_( 12 )
			.align_( \center )
			.clipLo_(0)
			.background_( Color.clear )
			.charSelectColor_( Color.white.alpha_(0.5) )
			.autoScale_( true );

		counter.action = { |v| pos = v.value; 
			if( pos != 0 ) { return.value = 1 } { return.value = 0 };
			window.update; counterAction.value( this ); };
		
		//window.drawHook = { counter.value = pos; };
		window.alwaysOnTop = true;
		window2 = window;
		}
		
	initActions { |inStep| // default actions; can be overriden
		step = inStep ? (128/44100);
		playAction = { playRoutine = Routine({ 
				pos = pos - step;
				loop { pos = pos + step; 
					{ this.update }.defer; step.wait; } });
			playRoutine.play;
			};
		stopAction = { playRoutine.stop; };
		returnAction = { pos = 0; this.update; };
		}
	
	pos_ { |newPos| pos = newPos; 
		if( play.value == 0 ) {
			if( pos != 0 ) { return.value = 1 } { return.value = 0 };
		};
		this.update; 
	}
	
	update { if( window2.notNil && { window2.isClosed.not } )
				{ { counter.value = pos; window2.refresh; }.defer } }
				
	name { ^window2.name }
	name_ { |newName| window2.name = newName.asString }
	
	isClosed { ^(window2.isNil or: { window2.isClosed } ) }
	close { window2.close }
	
	window { ^window2 }
	
	play { play.valueAction = 1; }
		
	stop { stop.valueAction = 0; }
		
	return  { return.valueAction = 0; }		
		
	
	}