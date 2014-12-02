ServerRecordWindow {
	
	classvar all;
	
	var <w, <server, nCha, hFormat, sFormat, fileName, folderName, wasAuto = true;
	var controls, counter, counterRoutine, counterRoutineFunc;
	var <>recordNodeID;
	
	*new { |server, nodeID|
		var existing;
		if( all.asCollection.any({ |item| item.server == server; }) )
			{ existing = all.detect({ |item| item.server == server; });
			existing.w.front;
			^existing; }
			{ ^super.new.init( server, nodeID ); }
		  }
		  
	prepare { if( controls[ \prepare ].enabled )
				{ controls[ \prepare ].action.value; };
		   }
		   
	record {  if( controls[ \record ].value == 1 )
				{ controls[ \record ].valueAction = 2; };
		   }
		   
	pause {  if( controls[ \pause ].value == 1 )
				{ controls[ \pause ].valueAction = 2; };
			if( controls[ \pause ].value == 2 )
				{ controls[ \pause ].valueAction = 0; };
		   }
		   
	stop { if( controls[ \stop ].value == 1 )
				{ controls[ \stop ].valueAction = 0; };
		}
		
	fileName { ^fileName.items[2] }	
		  
	addToAll { all = all.asCollection.add( this ); }
	
	init { |inServer, nodeID|	
		this.init2( inServer, nodeID ).addToAll;
		}
	
	init2 { |inServer, nodeID|
		
		recordNodeID = nodeID;
		server = inServer ? Server.default;
		w = Window( "record : " ++ server.name, 
			Rect.aboutPoint( Window.screenBounds.center, 150, 75 ),
			false ).front;
			
		w.decorate;
		
		w.onClose = { all.remove( this ) };
		
		StaticText( w, 60@20 ).string_( "#channels" ).align_( \right );
		nCha = NumberBox( w, 40@20 ).value_( server.recChannels )
			.clipLo_( 1 )
			.action_({ |v| server.recChannels = v.value.asInt });
			
		//w.view.decorator.nextLine;
		StaticText( w, 60@20 ).string_( "format" ).align_( \right );
		hFormat = PopUpMenu( w, 48@20 ).items_( ["aiff", "wav", "caf"] )
			.action_({ |v| server.recHeaderFormat =  v.items[ v.value ][ v.value ]; 
					fileName.items = fileName.items.put( 2, 
						fileName.items[2].replaceExtension( v.items[ v.value ] ) );
					});
		sFormat = PopUpMenu( w, 60@20 ).items_( 
			["float", "double", "int16", "int24", "int32", "mu", "a"] )
			.action_({ |v| server.recSampleFormat =
		     v.items[ v.value ].asString; });
		
		w.view.decorator.nextLine;
		StaticText( w, 60@20 ).string_( "filename" ).align_( \right );
		fileName = PopUpMenu( w, 220@20 ).items_( 
					["auto name", "-" , "recording.aiff", "-", "specify path..", "browse path.."] )
				.action_({ |v|
					case { v.value == 4 }
						{ 
						 v.value = 0;
						 	SCRequestString( folderName.string ++ v.items[2],
								"please specify a file path", { |string| 
									v.items = v.items.put( 2, string.basename); v.value = 2; 
									folderName.string = 
										string.dirname.deStandardizePath ++ "/";} );
						}
						{ v.value == 5 }
						{ Dialog.savePanel(
							{ |path| v.items =  v.items.put( 2, path.basename
								.replaceExtension( hFormat.items[ hFormat.value ] ) );
								v.value = 2;
								folderName.string = path.dirname.deStandardizePath ++ "/";
								 }, { v.value = 0 } ); };
						});
						
		w.view.decorator.nextLine;
		StaticText( w, 60@20 ).string_( "folder" ).align_( \right );
		folderName = StaticText(  w, 180@20 ).string_( "recordings/" ).align_( \center );
		Button( w, 35@20 ).states_( [["show"]] ).action_({ folderName.string.openInFinder });
		
		w.view.decorator.nextLine;
		
		counterRoutineFunc = { Routine({ 
					loop{ controls[ \counter ].posD = 
						controls[ \counter ].pos + 0.05; 0.05.wait } 
					})
				.play( SystemClock ); 
			};
		
		controls = (
			counterText: StaticText( w, 60@20 ).string_( "time" ).align_( \right ),
			counter: SMPTEBox( w, 120@20 ).fontSize_( 12 ).fontColor_( Color.black ),
			nextLine: w.view.decorator.nextLine,
			prepare:
				Button( w, 60@20 )
					.states_( [[ "prepare", Color.black, Color.red.alpha_(0.5) ]] )
					.action_({ 
						var inFileName;
						inFileName =  
							if( fileName.value == 0 )
								{ wasAuto = true;
									folderName.string.standardizePath ++ "/SC_" ++ 
									Date.localtime.stamp ++ "." ++ server.recHeaderFormat; }
								{ wasAuto = false;
									(folderName.string ++ 
										fileName.items[2]).standardizePath };
						
						fileName.items = fileName.items.put( 2, inFileName.basename );
						fileName.value = 2;
						
						[ nCha, hFormat, sFormat, fileName ].do( _.enabled_( false ) );
							 
						server.recSampleFormat = sFormat.items[ sFormat.value ].asString;
						server.recHeaderFormat = hFormat.items[ hFormat.value ].asString; 
						server.recChannels = nCha.value.asInt;
						server.prepareForRecord( inFileName );
						controls[ \prepare ].enabled_( false );
						controls[ \record ].value = 1;
						controls[ \counter ].pos = 0;
					 	}),
			spacer: StaticText( w, 30@20 ), // spacer
			record: RoundButton( w, 60@40 )
				.states_( [
					[ \record, Color.red(0.5).blend( Color.white, 0.5 ) , Color.gray(0.75) ],
					[ \record, Color.red(0.5), Color.gray(0.75) ],
					[ \record, Color.red, Color.gray(0.5) ]
						] )
				//.enabled_( false )
				.canFocus_( false )
				.action_({ |view|
					case { view.value == 2 }
						{   server.recordOnNodeID( recordNodeID );
							counterRoutine = counterRoutineFunc.value;
							//controls[ \pause ].enabled_( true );
							controls[ \pause ].value = 1;
							controls[ \stop ].value = 1; }
						{ view.value == 1 }
						{ view.value = 0 }
						{ view.value == 0 }
						{ view.value = 2 };
				 	}),
			pause: RoundButton( w, 60@40 ).states_( [
						[ \pause, Color.green(0.5).blend( Color.white, 0.5 ), 
							Color.gray(0.75) ],
						[ \pause, Color.green(0.5), Color.gray(0.75) ],
						[ \pause, Color.green, Color.gray(0.5) ]
						] )
				//.enabled_( false )
				.canFocus_( false )
				.action_({ |v| 
					case { v.value==2 }
						 { server.pauseRecording;
						 	counterRoutine !? counterRoutine.stop; 
						 	 }
						 { v.value == 0 }
						 { server.recordOnNodeID( recordNodeID );
						 	counterRoutine = counterRoutineFunc.value;
						 	 v.value = 1; }
						 { v.value == 1 }
						 { v.value = 0 }; 
						 }),
			stop: RoundButton( w, 60@40 ).states_( [
					[ \stop, Color.black.blend( Color.white, 0.5), Color.grey(0.75) ],
					[ \stop, Color.black, Color.grey(0.75) ]] )
				//.enabled_( false )
				.canFocus_( false )
				.action_({ |v|
					if( v.value == 0 )
					{	server.stopRecording;
						counterRoutine !? counterRoutine.stop;
						controls[ \pause ].value = 0;
						controls[ \record ].value = 0;
						controls[ \prepare ].enabled_( true );
						if( wasAuto ) { fileName.value = 0 };
						[ nCha, hFormat, sFormat, fileName ].do( _.enabled_( true ) );
					} { v.value = 0 }
				 	});
			
		 );
		controls[ \counter ].view.enabled_( true );
	 }
}