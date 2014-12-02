MIDIKeyboardWindow {
	
	// needs work
	
	}


KeyboardWindow {
	classvar <blackKeysStructure; 
	classvar <allWindows;
	
	var <nKeys = 36, <startOctave = 1, <>channel, <bounds;
	var <activeKeys, <lastVelo, <notesDict;
	var <window = nil, <userView = nil;
	var <>downAction, <>upAction;
	var <>hold = false;
	
	*initClass {
		blackKeysStructure = [0,1,1,0,1,1,1];
		allWindows = [];
		}
	
	*new { |nKeys = 36, startOctave = 1, channel = 0, bounds|
		var out;
		bounds = bounds ?  Rect( 128, 64, 600, 100 );
		out = super.newCopyArgs( nKeys, startOctave, channel, bounds, [], 64, () )
			.downAction_( { |chan, note, velo| [chan, note, velo].postln; } ).newWindow;
		allWindows = allWindows.add( out );
		^out;
		}
		
	*scale { 
		var last = 0;
		^blackKeysStructure.collect({ |item|
			var out;
			out = last + item + 1;
			last = out;
			out - 1;
		});
	}
	
	*pressNote { |noteNumber = 64, velo = 64, channel = 0|
		allWindows
			.select({ |item| item.channel == channel })
			.do( _.pressNote( noteNumber, velo ) );
		}
		
	*unPressNote { |noteNumber = 64, velo = 64, channel = 0|
		allWindows
			.select({ |item| item.channel == channel })
			.do( _.unPressNote( noteNumber, velo ) );
		}
		
	*unPressAll { |velo = 64, channel|
		if( channel.notNil,
			{ allWindows
				.select({ |item| item.channel == channel }) },
			{ allWindows } )
				.do( _.unPressAll( velo ) );
		}
	
	pressNote { |noteNumber = 64, velo= 64|
		var newKey;
		newKey = noteNumber.keyToDegree( KeyboardWindow.scale, 12 ).round(0.5);
		if( this.pressKey( newKey, velo ) )
			{ downAction.value( channel, noteNumber, velo ); }
			{ upAction.value( channel, noteNumber, velo );   };
		}
	
	unPressNote { |noteNumber = 64, velo= 64|
		var newKey;
		newKey = noteNumber.keyToDegree( KeyboardWindow.scale, 12 ).round(0.5);
		if( this.unPressKey( newKey, velo ) )
			{ upAction.value( channel, noteNumber, velo ); };
		}
		
	unPressAll { |velo = 64|
		this.activeNotes.do( this.unPressNote( _, velo ) );
		}
		
	activeNotes { 
		var scale;
		scale = this.class.scale;
		^activeKeys.collect({ |key| 
			key.floor.degreeToKey( scale, 12 ) + (key.frac * 2) 
				// degreeToKey seems to have a problem with Floats... why?
			});
		}
		
	// private
	pressKey { |key, velo|
		// returns false and switchess off if already pressed
		if( activeKeys.remove( key ).isNil )
				{ activeKeys = activeKeys.add( key );
					notesDict.put( key, velo ); 
					lastVelo = velo;
					window.refresh; ^true;
					}
				{ window.refresh; ^false }
		}
		
	unPressKey { |key, velo| // velo not used yet
		// returns false if already unpressed
		if( activeKeys.includes( key ) )
			{ activeKeys.remove( key ); window.refresh;
			^true }
			{ ^false };
	
		}
	
	newWindow {
		var nWhiteKeys = ((nKeys / 12) * 7).ceil;
		var scale;
		//var blackKeysStructure = [0,1,1,0,1,1,1];
					
		window = Window( "keys (channel " ++ channel ++ " )", bounds );
		window.view.background_( Color.white );	
		window.front;
				
		scale = KeyboardWindow.scale;			
		userView = UserView( window, window.view.bounds ).resize_( 5 );
		
		userView.mouseDownAction_({|v,x,y| 
			var theKey = ( (x / bounds.width) * nWhiteKeys ) + ( (startOctave + 2) * 7 );
			var velo;
			if( ( y < ( bounds.height * 0.66 ) ) )  
				{  case	{ (theKey.frac < 0.25) && 
							{ blackKeysStructure.wrapAt( theKey.floor) == 1 }  }
					{ theKey = theKey.floor - 0.5 }
						{ (theKey.frac > 0.75) && 
							{ blackKeysStructure.wrapAt( theKey.floor + 1) == 1 }  }
					{ theKey = theKey.floor + 0.5 }
						{ true }
					{ theKey = theKey.floor };
					}
				{ theKey = theKey.floor; };
			
			if( theKey.frac == 0.5 )
				{ velo = (( y / ( bounds.height * 0.66 ) ) * 127); }
				{ velo = (( y / bounds.height ) * 127); };
			
			if( this.pressKey( theKey, velo ) )
				{ downAction.value( channel, 
							theKey.floor.degreeToKey( scale, 12 ) + 
							(theKey.frac * 2),
							velo ); } 
				{ upAction.value( channel, 
							theKey.floor.degreeToKey( scale, 12 ) + 
							(theKey.frac * 2),
							velo );
					};
			lastVelo = velo;
			});
			
		userView.mouseUpAction_({|v,x,y| 
			var theKey; 
			var velo;
			if( hold.not )
				{ theKey = ( (x / bounds.width) * nWhiteKeys )  + ( (startOctave + 2) * 7 );
					if( ( y < ( bounds.height * 0.66 ) ) )  
						{  case	{ (theKey.frac < 0.25) && 
									{ blackKeysStructure.wrapAt( theKey.floor) == 1 }  }
							{ theKey = theKey.floor - 0.5 }
								{ (theKey.frac > 0.75) && 
									{ blackKeysStructure.wrapAt( theKey.floor + 1) == 1 }  }
							{ theKey = theKey.floor + 0.5 }
								{ true }
							{ theKey = theKey.floor };
							}
						{ theKey = theKey.floor; };
					
					if( theKey.frac == 0.5 )
						{ velo = (( y / ( bounds.height * 0.66 ) ) * 127); }
						{ velo = (( y / bounds.height ) * 127); };
		
					if( this.unPressKey( theKey, lastVelo.copy ) )
						{ upAction.value( channel, 
									theKey.floor.degreeToKey( scale, 12 ) + 
									(theKey.frac * 2),
									velo );  };
				}
			});
			
		
		userView.drawFunc = { | theWindow |
			bounds = theWindow.bounds;
			//userView.bounds = bounds.copy.top_(0).left_(0);
			Pen.color = Color.black;
			
			nWhiteKeys.do( { |i|
				var position, keyWidth;
				keyWidth = bounds.width / nWhiteKeys;
				position = keyWidth * i;
				if( activeKeys.includes( i + ( (startOctave + 2) * 7 )  ) )
					{ Pen.width = keyWidth;
						Pen.color = Color.gray(0.66).blend(Color.red, (notesDict.at( 
							i + ( (startOctave + 2) * 7 ) ) ? 0.5) / 127 );
						Pen.moveTo( (keyWidth * (i + 0.5))@0 );
						Pen.lineTo( (keyWidth * (i + 0.5))@bounds.height );
						Pen.stroke;  };
					
				Pen.color = Color.black;
				Pen.width = 1;
				Pen.moveTo( position@0 );
				Pen.lineTo( position@bounds.height );
				Pen.stroke;
				if( blackKeysStructure.wrapAt(i) == 1 )
					{ 	if( activeKeys.includes( (i - 0.5) + 
									( (startOctave + 2) * 7 ) ) )
							{ Pen.color = Color.gray(0.33).blend( Color.red, 
								(notesDict.at( 
									(i - 0.5) + 
										( (startOctave + 2) * 7 ) ) ? 0.5) / 127 ); };
						Pen.width = keyWidth * 0.5;
						Pen.moveTo( position@0 );
						Pen.lineTo( position@(bounds.height * 0.66) );
						Pen.stroke; };
				if( i%7 == 0) { ["C" ++ (startOctave + (i / 7))].wrapAt(i)
					.drawAtPoint( (keyWidth * (i + 0.2))@(bounds.height - 14), 
						color: Color.gray ) };
				} );
		
			};
		
		window.onClose_( { allWindows.remove( this ) } );
		
		^this;
		}
		
	storeArgs { `[nKeys, startOctave, channel, window] }
	
	}
		