// wslib 2006
// W. Snoei

SCTransportWindow {

	/// Old version of TransportWindow; different visuals, same functionality
	
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
		window = SCWindow( name, Rect( 360, 10, 300, 100 ), false );
		window.front;
		
		pos = startPos;
		pressed = (play: false, stop: false, return: false );
		active = (play: false, stop: false, return: false );
		
		// buttons: (these could be wrapped in a standardized round button class later)
		// play button
		play = SCUserView( window, Rect( 50, 10, 80, 80 ) );
		play.drawFunc_({ |view|
			var width, backColor;
			width = 0.36;
			
			if( active[ \play ] )
				{ backColor = Color.green.alpha_(0.3); }
				{ backColor = Color.green; };
			if( pressed[ \play ] )
				{ backColor = backColor.blend( Color.black, 0.25 );  };
				
			backColor.set;
			Pen.addArc( view.bounds.center, view.bounds.height / 2, 0, 2pi);
			Pen.fill; 
			
			Color.black.alpha_(0.9).set;
			Pen.width = 3;
			Pen.addArc( view.bounds.center, view.bounds.height / 2, 0, 2pi);
			Pen.stroke; 
			
			Color.white.alpha_(0.5).set;
			Pen.addArc( view.bounds.center, view.bounds.height / 2, 
				if( pressed[ \play ] )
					{  5.5pi/3 }
					{  2.5pi/3 }, pi);
			Pen.stroke; 
			
			Color.black.alpha_(0.5).set;
			Pen.moveTo( view.bounds.center + Polar( view.bounds.height * width, 4pi/3 ).asPoint );
			Pen.lineTo( view.bounds.center + Polar( view.bounds.height * width, 0 ).asPoint );
			Pen.lineTo( view.bounds.center + Polar( view.bounds.height * width, 2pi/3 ).asPoint );
			Pen.fill;
			
			});
			
		// stop button
		stop = SCUserView( window, Rect(  5, 5, 40, 40 ) );
		stop.drawFunc_({ |view|
			var width, backColor;
			width = 0.36;
			if( active[ \stop ] or: { active[ \play ] } )
				{ backColor = Color.yellow; }
				{ backColor = Color.yellow.alpha_(0.1); };
			if( pressed[ \stop ] )
				{ backColor = backColor.blend( Color.black, 0.25 );  };
			backColor.set;
		
			Pen.addArc( view.bounds.center, view.bounds.height / 2, 0, 2pi);
			Pen.fill; 
			
			Color.black.alpha_(0.9).set;
			Pen.width = 3;
			Pen.addArc( view.bounds.center, view.bounds.height / 2, 0, 2pi);
			Pen.stroke; 
			
			Color.white.alpha_(0.5).set;
			Pen.addArc( view.bounds.center, view.bounds.height / 2, 
				if( pressed[ \stop ] )
					{  5.5pi/3 }
					{  2.5pi/3 }, pi);
			Pen.stroke; 
			
			Color.black.alpha_(0.5).set;
			Pen.moveTo( view.bounds.center + 
				Polar( view.bounds.height * width, 1.25pi ).asPoint );
			Pen.lineTo( view.bounds.center + 
				Polar( view.bounds.height * width, 0.75pi ).asPoint );
			Pen.lineTo( view.bounds.center + 
				Polar( view.bounds.height * width, 0.25pi ).asPoint );
			Pen.lineTo( view.bounds.center + 
				Polar( view.bounds.height * width, 1.75pi ).asPoint );
			Pen.fill;
			});
			
		// return to 0 button
		return = SCUserView( window, Rect(  5, 55, 40, 40 ) );
		return.drawFunc_({ |view|
			var width, backColor;
			width = 0.25;
			if( active[ \play ].not and: { active[ \return ] } )
				{ backColor = Color.blue; }
				{ backColor = Color.blue.alpha_(0.1); }; 
			if( pressed[ \return ] )
				{ backColor = backColor.blend( Color.black, 0.25 );  };
			backColor.set;
			
			Pen.addArc( view.bounds.center, view.bounds.height / 2, 0, 2pi);
			Pen.fill; 
			
			Color.black.alpha_(0.9).set;
			Pen.width = 3;
			Pen.addArc( view.bounds.center, view.bounds.height / 2, 0, 2pi);
			Pen.stroke; 
			
			Color.white.alpha_(0.5).set;
			Pen.addArc( view.bounds.center, view.bounds.height / 2, 
				if( pressed[ \return ] )
					{  5.5pi/3 }
					{  2.5pi/3 }, pi);
			Pen.stroke; 
			
			Color.black.alpha_(0.5).set;
			Pen.moveTo( view.bounds.center + 
				Point( 3, 0 ) +
				Polar( view.bounds.height * width, 3pi/3 ).asPoint );
			Pen.lineTo( view.bounds.center + 
				Point( 3, 0 ) +
				Polar( view.bounds.height * width, pi/3 ).asPoint );
			Pen.lineTo( view.bounds.center + 
				Point( 3, 0 ) +
				Polar( view.bounds.height * width, 5pi/3 ).asPoint );			Pen.fill;
				
			Pen.moveTo( view.bounds.center + 
				Point( -3, 0 ) +
				Polar( view.bounds.height * width, 4pi/3 ).asPoint );
			Pen.lineTo( view.bounds.center + 
				Point( -3, 0 ) +
				Polar( view.bounds.height * width, 2pi/3 ).asPoint );
			Pen.stroke;
				
			});
			
		// top view to prevent focus rectangles to show up around the buttons
		top = SCUserView( window, window.view.bounds );
		
		top.mouseBeginTrackFunc = { |v,x,y,mod|
			case { play.bounds.containsPoint( x@y ) }
				{ 	if( active[ \play ].not )
						{ pressed[ \play ] = true; };
					}
				{ stop.bounds.containsPoint( x@y ) }
				{ 	if( active[ \play ] )
				  		{ pressed[ \stop ] = true; };
					}
				{ return.bounds.containsPoint( x@y ) }
				{ 	if( active[ \play ].not && { active[ \return ] })
				  		{ pressed[ \return ] = true; };
					};
			this.update; 
			};
			
		top.mouseTrackFunc = { |v,x,y,mod|
			if( play.bounds.containsPoint( x@y ).not )
				{ pressed[ \play ] = false; };
			if( stop.bounds.containsPoint( x@y ).not )
				{ pressed[ \stop ] = false; };
			if( return.bounds.containsPoint( x@y ).not )
				{ pressed[ \return ] = false; };
				
			this.update;
			};
	
		top.mouseEndTrackFunc = { |v,x,y,mod|
			case { play.bounds.containsPoint( x@y ) }
				{ if( active[ \play ].not )
					{ 	active[ \play ] = true;
						playAction.value( this );
						active[ \return ] = true;
					/*
					pos = countStream.value( pos );
					playRoutine = 
						Routine({ 
							loop { pos = countStream.value; 
									{ window.refresh }.defer; step.wait; } });
					playRoutine.play;
					*/
					};
				}
				{ stop.bounds.containsPoint( x@y ) }
				{ 	if( active[ \play ] )
				  		{ active[ \play ] = false;
				  		//playRoutine.stop;
				  		stopAction.value( this );
				  		};
					}
				{ return.bounds.containsPoint( x@y ) }
				{ 	if( active[ \play ].not )
				  		{ pressed[ \return ] = false;
				  			active[ \return ] = false;
				  			returnAction.value( this );
				  		//pos = countStream.value( 0 ); 
				  		};
					};
		
					
			pressed[ \play ] = false;
			pressed[ \stop ] = false;
			pressed[ \return ] = false;
			this.update;
			};

		
		
		// counter
		counter = SMPTEView( window, 145@38 );
		
		//counter = SCNumberBox( window, Rect(145, 38, 100, 20 ));

		
		counter.action = { |v| pos = v.value; window.update; counterAction.value( this ); };
		
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
	
	pos_ { |newPos| pos = newPos; this.update; }
	
	update { if( window2.notNil && { window2.dataptr.notNil } )
				{ { counter.value = pos; window2.refresh; }.defer } }
				
	name { ^window2.name }
	name_ { |newName| window2.name = newName.asString }
	
	isClosed { ^(window2.isNil or: { window2.dataptr.isNil } ) }
	close { window2.close }
	
	window { ^window2 }
	
	play {  if( active[ \play ].not )
					{ 	active[ \play ] = true;
						playAction.value( this );
						active[ \return ] = true;
						this.update;
					};
		}
		
	stop {  	if( active[ \play ] )
				  		{ active[ \play ] = false;
				  		stopAction.value( this );
				  		this.update;
		  				};
		}
		
	return  { if( active[ \play ].not )
				  		{ pressed[ \return ] = false;
				  			active[ \return ] = false;
				  			returnAction.value( this );
				  			this.update;
				  		};
		}		
		
	
	}