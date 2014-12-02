+ SimpleMIDIFile {
	//  plotting using wslib GUI extensions
	
	// - could be optimized
	// - should make use of GUI style ( waiting for color standardization )
	
	asPenFunction { |bounds, track = 0, type = \pitchBend, channel = 0, cc = 0, 
			minVal = 0, maxVal = 127|
			// width is not set here, colors are.
		var events, length, lastY, step, valRange, c3y;
		bounds = bounds ? Rect(0,0,400,400);
		//events = this.midiTrackTypeEvents(track, type, channel);
		length = this.length.max(1);
		case { type === 'noteOn' }
			{ 
			//[track, absTime, \noteOn, channel, note, velo, dur, upVelo]
			
			events = this.noteSustainEvents( channel, track );
			
			minVal =  (( (minVal - 1) / 12).floor * 12).max(0);
			maxVal =  (( maxVal / 12).ceil * 12).min(127);
			
			valRange =  maxVal - minVal;
			step = 1/valRange;

			// lines
			
			 // big red for C3
			 
			 if( 60.inRange( minVal, maxVal ) )
			 	{ 
			 	Pen.color = Color.red.alpha_(0.5);
			 	c3y = (((60 - minVal) * step) * bounds.height) + bounds.top;
			 	Pen.line( bounds.left@c3y, bounds.right@c3y  );
			 	Pen.stroke;
			 	/*
			 	"C3".drawAtPoint( 
						1@(c3y - (step * bounds.height)),
						Font( "Monaco", 7 ), Color.red.alpha_(0.25) );
				*/
			 	};
			 	
			
			 // red for C (like harp strings)
			Pen.color = Color.red.alpha_(0.25);
			
			(valRange / 12).floor.do({ |i|
				var y;
				y = (( 1 - ( i / (valRange / 12) ) ) * bounds.height) + bounds.top;
				Pen.line( bounds.left@y, bounds.right@y );				});
				
			Pen.stroke;
			
			 // blue for F (like harp strings)
			Pen.color = Color.blue.alpha_(0.25);
			
			((valRange / 12).floor).do({ |i|
				var y;
				y = (( 1 - (( i / (valRange / 12) ) + (5*step) ) ) * bounds.height) + bounds.top;
				Pen.line( bounds.left@y, bounds.right@y );				});
				
			Pen.stroke;
			
			  // gray for other whole tones
			  
			 Pen.color = Color.gray.alpha_(0.33);
			  
			 valRange.do({ |i|	
			 	var y;
			 	if( [2,4,7,9,11].includes( i.asInt%12 ) ) // D, E, G, A, B
					{ 
					y = (( 1 - ( i * step )  ) * bounds.height) + bounds.top;
					Pen.line( bounds.left@y, bounds.right@y );
					}
				});
				
			Pen.stroke;
			
			/*
			// gray for half notes
			 Pen.color = Color.gray.alpha_(0.125);
			  
			 valRange.do({ |i|	
			 	var y;
			 	if( [1,3,6,8,10].includes( i.asInt%12 ) )
					{ 
					y = (( 1 - ( i * step )  ) * bounds.height) + bounds.top;
					Pen.line( bounds.left@y, bounds.right@y );
					}
				});
				
			Pen.stroke;
			*/
			
			
			
			//notes
			
			events.do({ |event|
			
				Pen.color = Color.black.blend( Color.green, event[5] / 127 );
				Pen.addRect( Rect( 
					bounds.left +
					((event[1] / length) * bounds.width),
					((( 1 - ( ( event[4] - minVal ) * step ) ) - (step/2) ) * bounds.height) 
						+ bounds.top,
					(event[6] / length) * bounds.width,
					step * bounds.height  ) );
				
				Pen.fill;
				
				});
			}
			
			{ type === 'cc' }
			{ events = this.controllerEvents( cc, channel, track );
				
				Pen.color = Color.black;
				lastY = bounds.height + bounds.top;
				
				Pen.moveTo( (((events[0][1] / length) * bounds.width) 
					 + bounds.left )@lastY ); // start at 0
				
				events.do({ |event|
					Pen.lineTo( (((event[1] / length) * bounds.width) + bounds.left)@lastY );
					lastY = (( 1 - (event[5] / 127) ) * bounds.height) + bounds.top;
					Pen.lineTo( (((event[1] / length) * bounds.width) + bounds.left)@lastY );
					});
				
				Pen.stroke;
			 }
			
			{ type === 'pitchBend' }
			{  events = this.copy.pitchBendMode_( \float )
					.midiTrackTypeEvents(track, 'pitchBend', channel); 
				Pen.color = Color.gray.alpha_(0.5);
				Pen.line( 
					bounds.left@(( bounds.height / 2) + bounds.top), 
					bounds.width@(( bounds.height / 2) + bounds.top) );
					
				Pen.stroke;
				
				Pen.color = Color.black;
				lastY = ( bounds.height / 2) + bounds.top;
				Pen.moveTo( (((events[0][1] / length) * bounds.width) 
					+ bounds.left )@lastY ); // start at 0
				
				events.do({ |event|
					Pen.lineTo( (((event[1] / length) * bounds.width) + bounds.left)@lastY );
					lastY = (((event[4] - 1) / -2) * bounds.height) + bounds.top;
					Pen.lineTo( (((event[1] / length) * bounds.width) + bounds.left)@lastY );
					});
				
				Pen.stroke;
				
				}
			{ type === 'afterTouch' }
			{   events = this.midiTrackTypeEvents(track, 'afterTouch', channel); 
				
				Pen.color = Color.black;
				lastY = ( bounds.height) + bounds.top;
				Pen.moveTo( (((events[0][1] / length) * bounds.width) 
					+ bounds.left )@lastY ); // start at 0
				
				events.do({ |event|
					Pen.lineTo( (((event[1] / length) * bounds.width) + bounds.left)@lastY );
					lastY = (( 1 - (event[4] / 127) ) * bounds.height) + bounds.top;
					Pen.lineTo( (((event[1] / length) * bounds.width) + bounds.left)@lastY );
					});
				
				Pen.stroke;
				
				}
			{ true }
			{ // not yet implemented
			 };
		}	
	
	plot { |notesOnly = false| 
		var window, events;
		window = ScaledUserViewWindow( pathName.basename );
		
		// this.adjustEndOfTrack;
		events = this.analyzeUsedEvents;
		
		if( notesOnly )	
			{ events = events.select({ |event| event[2] == 'noteOn' }); };
			
		window.userView.gridSpacingV = 1/(events.size.max(1));
		
		if( timeMode == \ticks )
			{  window.userView.gridSpacingH = 1/( this.length.max(1) / division ); }
			{  window.userView.gridSpacingH = 1/( this.length.max(1) );  };
			
		/// can be optimized:
		
		window.userView.unscaledDrawFunc_({ |view| 
			events.do({ |event, i|
				Pen.font = Font( "Monaco", 9 );
				Pen.color = Color.black.alpha_(0.5);
				Pen.stringAtPoint( case { event[2] === 'cc' }
						{ "tr% ch% %%".format( event[0], event[1], event[2], event[3] )  }
					  { event[2] === 'noteOn' }
					    { "tr% ch% notes (% to %)".format( event[0], event[1], 
					    		event[4].midiname, event[5].midiname )  }
					  { true }
						{ "tr% ch% %".format( event[0], event[1], event[2] )  }, 
						view.translateScale( 0@(i/ (events.size) ) ).x_(1)
						 ); 
					
				this.asPenFunction( 
					view.translateScale( Rect(0,i / events.size,1,1 / events.size) ),
					event[0], event[2], event[1], event[3], event[4], event[5] );
				});
			});
		}	
}