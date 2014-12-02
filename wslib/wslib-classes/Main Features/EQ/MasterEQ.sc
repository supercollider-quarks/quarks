// requires wslib & TabbedView quarks
// wslib 2009

MasterEQ {
	classvar <>eq; // dict with all variables
	classvar <window;
	
	*new { |numCha = (2), server|
		
		if( \TabbedView.asClass.notNil )
			{ if( eq.isNil or: { window.isClosed } )
				{ ^this.newEQ( numCha, server ) }
				{ if( eq[ \numChannels ] != numCha )
					{ if( eq[ \playing ] ) { eq[ \free ].value; };
					  eq[ \numChannels ] = numCha;
					  window.name = "MasterEQ (% ch)".format( numCha );
					  eq[ \play ].value; }
					{ if( eq[ \playing ].not ) 
						{ eq[ \play ].value; eq[ \bypass_button ].value=1 }; }
				};
			} { "MasterEQ requires the TabbedView Quark".error };
		}
		
	*stop { eq !? { if( eq[ \playing ] ) { eq[ \free ].value; eq[ \bypass_button ].value=0 }; } }
	*start { this.new( eq !? { eq[ \numChannels ] } ); }
			
	*newEQ { |numCha = 2, server|
		var s;
		s = server ? Server.default;
		
		eq = ();
		
		window = Window.new( "MasterEQ (% ch)".format( numCha ), 
				Rect(299, 130, 305, 220), true ).front; 
				
		window.view.decorator = FlowLayout( window.view.bounds, 10@10, 4@0 );
		
		eq[ \uvw ] = UserView( window, 
			window.view.bounds.insetBy(10,10)
				.height_(window.view.bounds.height - 80) 
			).resize_(5);
			
		eq[ \font ] = Font( Font.defaultSansFace, 10 );
		
		// eq[ \uvw ].relativeOrigin = false;
		
		eq[ \uvw ].focusColor = Color.clear;
		
		eq[ \frdb ] = [[100,0,1], [250,0,1], [1000,0,1], [3500,0,1], [6000,0,1]];
		
		eq[ \frpresets ] = [ // x_ = cannot delete or modify 
			'x_flat', [ [ 100, 0, 1 ], [ 250, 0, 1 ], [ 1000, 0, 1 ], [ 3500, 0, 1 ], 
				[ 6000, 0, 1 ] ], 
			'x_loudness', [ [ 78.0, 7.5, 0.65 ], [ 250, 0, 1 ], [ 890.0, -9.5, 3.55 ], 
				[ 2800.0, 3.5, 1.54 ], [ 7400.0, 7.0, 1.0 ] ], 
			'x_telephone', [ [ 600.0, -22.0, 0.7 ], [ 250, 0, 1 ], [ 1200.0, -2.0, 0.5 ],
				[ 1800.0, 1.0, 0.5 ], [ 4000.0, -22.0, 0.7 ] ]
			];
			
		eq[ \toControl ] = { eq[ \frdb ].collect({ |item,i|
					[ item[0].cpsmidi - 1000.cpsmidi, item[1], item[2].log10 ] }).flat; };
			
		eq[ \send_current ] = { // set any node listening to \eq_controls
			s.sendMsg( "/n_setn", 0, "eq_controls", 15, *eq[ \toControl ].value );
			};
			
		eq[ \fromControl ] = { |controls|
			controls.clump(3).collect({ |item|
					[ (item[0] + 1000.cpsmidi).midicps, item[1], 10**item[2]]
					});
			};
			
		//eq[ \frdb ] = eq[ \frpresets ][1].deepCopy;
		
		eq[ \selected ] = -1;
		
		eq[ \tvw ] = TabbedView( window, 
				window.view.bounds.insetBy(10,10).height_(35).top_(200),
			[ "low shelf", "peak 1", "peak 2", "peak 3", "high shelf" ],
			{ |i| Color.hsv( i.linlin(0,5,0,1), 0.75, 0.5).alpha_( 0.25 ); }!5 )
				.font_( eq[ \font ] )
				.resize_( 8 )
				.tabPosition_( \bottom );
				
		eq[ \tvw ].focusActions = { |i| { eq[ \selected ] = i; eq[ \uvw ].refresh;  }; }!5;
		
		eq[ \tvw_views ] = [];
		
		
		window.view.decorator.shift(0,8);
		
		eq[ \tvw ].views.do({ |view,i| 
			var vw_array = [];
			
			view.decorator = FlowLayout( view.bounds.moveTo(0,0) ); 
			
			StaticText( view, 35@14 ).font_( eq[ \font ] ).align_( \right ).string_( "freq:" );
			vw_array = vw_array.add( 
				RoundNumberBox( view, 40@14 ).font_( eq[ \font ] ).value_( eq[ \frdb ][i][0] )
					.clipLo_(20).clipHi_(22000)
					.action_({ |vw|
						eq[ \frdb ][i][0] = vw.value;
						eq[ \send_current ].value;
						eq[ \uvw ].refresh;
						eq[ \pumenu_check ].value;
						})  );
			
			StaticText( view, 25@14 ).font_( eq[ \font ] ).align_( \right ).string_( "db:" );
			vw_array = vw_array.add( 
				RoundNumberBox( view, 40@14 ).font_( eq[ \font ] ).value_( eq[ \frdb ][i][1] )
					.clipLo_( -36 ).clipHi_( 36 )
					.action_({ |vw|
						eq[ \frdb ][i][1] = vw.value;
						eq[ \send_current ].value;
						eq[ \uvw ].refresh;
						eq[ \pumenu_check ].value;
						})  );
			
			StaticText( view, 25@14 ).font_( eq[ \font ] ).align_( \right )
				.string_( (0: "rs:", 4:"rs:")[i] ? "rq"  );
			vw_array = vw_array.add( 
				RoundNumberBox( view, 40@14 ).font_( eq[ \font ] ).value_( eq[ \frdb ][i][2] )
					.step_(0.1).clipLo_( if( [0,4].includes(i) ) { 0.6 } {0.01}).clipHi_(10)
					.action_({ |vw|
						eq[ \frdb ][i][2] = vw.value;
						eq[ \send_current ].value;
						eq[ \uvw ].refresh;
						eq[ \pumenu_check ].value;
						}) 
						);
			
			eq[ \tvw_views ] = eq[ \tvw_views ].add( vw_array );
			
			});
			
		
		eq[ \tvw_refresh ] = { 
			eq[ \frdb ].do({ |item,i| 
				item.do({ |subitem, ii| 
					eq[ \tvw_views ][ i ][ ii ].value = subitem;
					})
				});
			};
			
		eq[ \bypass_button ] = RoundButton.new( window, 17@17 )
				.extrude_( true ).border_(1) //.font_( eq[ \font ] )
				.states_( [
					[ 'power', Color.gray(0.2), Color.white(0.75).alpha_(0.25) ],
					[ 'power', Color.red(0.8), Color.white(0.75).alpha_(0.25) ]] )
				.value_(1)
				.action_({ |bt| switch( bt.value,
					1, { eq[ \play ].value },
					0, { eq[ \free ].value });
					})
				.resize_(7);
			
		eq[ \pumenu ] = PopUpMenu.new( window, 100@16 )
			.font_( eq[ \font ] ).canFocus_(false)
			.resize_(7);
			
		if( GUI.id == \swing ) {
			 eq[ \pumenu ].bounds = 
			 	eq[ \pumenu ].bounds
			 		.insetBy(-3,-3)
			 		.moveBy( 0, 1 ) };
			
		//eq[ \pumenu_check ].value;
		eq[ \pu_buttons ] = [
			RoundButton.new( window, 16@16 )
				.border_(1)
				.states_( [[ '+' ]] )
				.resize_(7)
				,
			RoundButton.new( window,  16@16 )
				.border_(1)
				.resize_(7)
				.states_( [[ '-' ]] ),	
			];
		
		eq[ \pu_filebuttons ] = [
			RoundButton.new( window, 55@16 )
				.extrude_( true ).border_(1).font_( eq[ \font ] )
				.states_( [[ "save", Color.black, Color.red(0.75).alpha_(0.25) ]] )
				.resize_(7),
			RoundButton.new( window,  55@16 )
				.extrude_( true ).border_(1).font_( eq[ \font ] )
				.states_( [[ "revert", Color.black, Color.green(0.75).alpha_(0.25) ]] )
				.resize_(7)
			];
		
		eq[ \pu_filebuttons ][0].action_({
				File.use("eq-prefs.txt", "w",
			{ |f| f.write(
				(  current: eq[ \frdb ],
				   presets: eq[ \frpresets ] ).asCompileString
				); })
			});
		
		eq[ \pu_filebuttons ][1].action_({
			var contents;
				if( File.exists( "eq-prefs.txt" ) )
				 {	File.use("eq-prefs.txt", "r", { |f| 
						contents = f.readAllString.interpret;
						//contents.postln;
						eq[ \frdb ] = contents[ \current ];
						eq[ \frpresets ] = contents[ \presets ];
						eq[ \send_current ].value;
						eq[ \pumenu_create_items ].value;
						eq[ \pumenu_check ].value;
						eq[ \uvw ].refresh;
						eq[ \tvw_refresh ].value;
							 	});
				};
			});	
		
		
			
		eq[ \pumenu_create_items ] = {
			var items;
			items = [];
			eq[ \frpresets ].pairsDo({ |key, value|
				if( key.asString[..1] == "x_" )
					{ items = items.add( key.asString[2..] ); }
					{ items = items.add( key.asString ); };
				
				});
			items = items ++ [ "-", "(custom" /*)*/ ];
			eq[ \pumenu ].items = items;
			};
			
		eq[ \pumenu_create_items ].value;
		
		eq[ \pumenu ].action = { |pu|
			eq[ \frdb ] = eq[ \frpresets ][ (pu.value * 2) + 1 ].deepCopy;
			//eq[ \frdb ].postln;
			eq[ \send_current ].value;
			eq[ \uvw ].refresh;
			eq[ \tvw_refresh ].value;
			if( eq[ \frpresets ][ pu.value * 2 ].asString[..1] == "x_" )
				  	{  eq[ \pu_buttons ][0].enabled_(false); 
				  	    eq[ \pu_buttons ][1].enabled_(false);   }
				  	{   eq[ \pu_buttons ][0].enabled_(false);
				  	     eq[ \pu_buttons ][1].enabled_(true);    };
			};
			
		
			
		eq[ \pu_buttons ][0].action = { |bt|
			var testPreset, addPreset, replacePreset;
			
			testPreset = { |name = "user"|
				var index, xnames, clpresets;
				name = name.asSymbol;
				index = eq[ \frpresets ].clump(2)
					.detectIndex({ |item| item[0] == name.asSymbol });
				xnames = eq[ \frpresets ].clump(2)
					.select({ |item| item[0].asString[..1] == "x_" })
					.collect({ |item| item[0].asString[2..].asSymbol });
				if( index.isNil )
					{
					if( xnames.includes( name ).not )
						{ addPreset.value( name ); }
						{ 
		SCAlert( "EQ preset '%' cannot be overwritten.\nPlease choose a different name"
								.format( name ), ["ok"] ); };
					} {
		SCAlert( "EQ preset '%' already exists.\nDo you want to overwrite it?"
								.format( name ), ["cancel","ok"], 
								[{}, { replacePreset.value( name, index ) }] ); 
					};
				};
				
			addPreset = { |name = "user"|
				eq[ \frpresets ] = eq[ \frpresets ] ++ [ name.asSymbol, eq[ \frdb ].deepCopy ];
				eq[ \pumenu_create_items ].value;
				eq[ \pumenu_check ].value;
				};
				
			replacePreset = { |name = "x_default", index = 0|
				eq[ \frpresets ][ index * 2 ] = name.asSymbol;
				eq[ \frpresets ][ (index * 2)+1 ] = eq[ \frdb ].deepCopy;
				eq[ \pumenu_create_items ].value;
				eq[ \pumenu_check ].value;
				};
			
			SCRequestString(  "user", "Enter a short name for the new preset",
				{ |str| testPreset.value(str); });
				
			};
		
		eq[ \pu_buttons ][1].action = { |bt|
			 SCAlert( "Are you sure you want to\ndelete preset '%'"
						.format( eq[ \pumenu ].items[ eq[ \pumenu ].value ] ), ["cancel","ok"], 
						[{}, {
						eq[ \frpresets ].removeAt(  eq[ \pumenu ].value * 2 );
						eq[ \frpresets ].removeAt(  eq[ \pumenu ].value * 2 );
						eq[ \pumenu_create_items ].value;
						eq[ \pumenu_check ].value;
					}] ); 
					
			};
		
		eq[ \pumenu_check ]	= {
			var index;
			index = eq[ \frpresets ].clump(2).detectIndex({ |item| item[1] == eq[ \frdb ] });
			if( index.notNil )
				{ eq[ \pumenu ].value = index;
				  eq[ \pu_buttons ][0].enabled_(false);
				  if( eq[ \frpresets ][ index * 2 ].asString[..1] == "x_" )
				  	{  eq[ \pu_buttons ][1].enabled_(false);  }
				  	{   eq[ \pu_buttons ][1].enabled_(true);  };
					}
				{ eq[ \pumenu ].value = (eq[ \frpresets ].size/2) + 1; 
				    eq[ \pu_buttons ][1].enabled_(false);  
				     eq[ \pu_buttons ][0].enabled_(true);
					};
			};
		
		eq[ \pumenu_check ].value;
		
		eq[ \send_current ].value;
		
		(
		eq[ \uvw ].mouseDownAction = { |vw,x,y,mod|
			var bounds;
			var pt;
			var min = 20, max = 22050, range = 24;
			
			bounds = vw.bounds.moveTo(0,0);
			//pt = (x@y) - (bounds.leftTop);
			pt = (x@y);
			
			eq[ \selected ] =  eq[ \frdb ].detectIndex({ |array|
				(( array[ 0 ].explin( min, max, 0, bounds.width ) )@
				( array[ 1 ].linlin( range.neg, range, bounds.height, 0, \none ) ))
					.dist( pt ) <= 5;
				}) ? -1;
				
			if( eq[ \selected ] != -1 ) { eq[ \tvw ].focus( eq[ \selected ] ) };
			vw.refresh;
			};
			
		eq[ \uvw ].mouseMoveAction = { |vw,x,y,mod|
			var bounds;
			var pt;
			var min = 20, max = 22050, range = 24;
			
			bounds = vw.bounds.moveTo(0,0);
			//pt = (x@y) - (bounds.leftTop);
			pt = (x@y);
			
			if( eq[ \selected ] != -1 )
				{
				case { ModKey( mod ).alt }
					{ 
					if(  ModKey( mod ).shift )
						{
					eq[ \frdb ][eq[ \selected ]] = eq[ \frdb ][eq[ \selected ]][[0,1]] 
						++ [ y.linexp( bounds.height, 0, 0.1, 10, \none ).nearestInList(
							if( [0,4].includes(eq[ \selected ]) ) 
								{[0.6,1,2.5,5,10]} 
								{[0.1,0.25,0.5,1,2.5,5,10]}
								
							) ];
						}
						{
					eq[ \frdb ][eq[ \selected ]] = eq[ \frdb ][eq[ \selected ]][[0,1]] 
						++ [ y.linexp( bounds.height, 0, 0.1, 10, \none ).clip(
								 if( [0,4].includes(eq[ \selected ]) ) { 0.6 } {0.1},
								 	10).round(0.01) ];
						};
					eq[ \tvw_views ][eq[ \selected ]][2].value = eq[ \frdb ][eq[ \selected ]][2];
						 }
					{ ModKey( mod ).shift }
					{
				eq[ \frdb ][eq[ \selected ]] = [
					pt.x.linexp(0, bounds.width, min, max )
						.nearestInList( [25,50,75,100,250,500,750,1000,2500,5000,7500,10000] ),
					pt.y.linlin( 0, bounds.height, range, range.neg, \none )
						.clip2( range ).round(6),
					eq[ \frdb ][eq[ \selected ]][2] 
					];
				eq[ \tvw_views ][eq[ \selected ]][0].value = eq[ \frdb ][eq[ \selected ]][0];
				eq[ \tvw_views ][eq[ \selected ]][1].value = eq[ \frdb ][eq[ \selected ]][1];	
					}
					{ true }
					{
				eq[ \frdb ][eq[ \selected ]] = [
					pt.x.linexp(0, bounds.width, min, max ).clip(20,20000).round(1),
					pt.y.linlin( 0, bounds.height, range, range.neg, \none ).clip2( range )
						.round(0.25),
					eq[ \frdb ][eq[ \selected ]][2] 
					];	
				eq[ \tvw_views ][eq[ \selected ]][0].value = eq[ \frdb ][eq[ \selected ]][0];
				eq[ \tvw_views ][eq[ \selected ]][1].value = eq[ \frdb ][eq[ \selected ]][1];		};
			eq[ \send_current ].value;
			vw.refresh;
			eq[ \pumenu_check ].value;
				};
		
			};
		);
		
		(
		eq[ \uvw ].drawFunc = { |vw|
			var freqs, svals, values, bounds, zeroline;
			var freq = 1200, rq = 0.5, db = 12;
			var min = 20, max = 22050, range = 24;
			var vlines = [100,1000,10000];
			var dimvlines = [25,50,75, 250,500,750, 2500,5000,7500];
			var hlines = [-18,-12,-6,6,12,18];
			var pt, strOffset = 11;
			
			if( GUI.id === 'swing' ) { strOffset = 14 };
			
			bounds = vw.bounds.moveTo(0,0);
			
			#freq,db,rq = eq[ \frdb ][0] ? [ freq, db, rq ];
			
			freqs = ({|i| i } ! (bounds.width+1));
			freqs = freqs.linexp(0, bounds.width, min, max );
			
			values = [
				BLowShelf.magResponse( freqs, 44100, eq[ \frdb ][0][0], eq[ \frdb ][0][2], 
					eq[ \frdb ][0][1]),
				BPeakEQ.magResponse( freqs, 44100, eq[ \frdb ][1][0], eq[ \frdb ][1][2], 
					eq[ \frdb ][1][1]),
				BPeakEQ.magResponse( freqs, 44100, eq[ \frdb ][2][0], eq[ \frdb ][2][2], 
					eq[ \frdb ][2][1]),
				BPeakEQ.magResponse( freqs, 44100, eq[ \frdb ][3][0], eq[ \frdb ][3][2], 
					eq[ \frdb ][3][1]),
				BHiShelf.magResponse( freqs, 44100, eq[ \frdb ][4][0], eq[ \frdb ][4][2], 
					eq[ \frdb ][4][1])
					].ampdb.max(-200).min(200);
			
			zeroline = 0.linlin(range.neg,range, bounds.height, 0, \none);
			
			svals = values.sum.linlin(range.neg,range, bounds.height, 0, \none);
			values = values.linlin(range.neg,range, bounds.height, 0, \none);
			
			vlines = vlines.explin( min, max, 0, bounds.width );
			dimvlines = dimvlines.explin( min, max, 0, bounds.width );
			
			pt = eq[ \frdb ].collect({ |array|
				(array[0].explin( min, max, 0, bounds.width ))
				@
				(array[1].linlin(range.neg,range,bounds.height,0,\none));
				});

				Pen.color_( Color.white.alpha_(0.25) );
				Pen.roundedRect( bounds, [6,6,0,0] ).fill;
				
				Pen.color = Color.gray(0.2).alpha_(0.5);
				//Pen.strokeRect( bounds.insetBy(-1,-1) );
				
				//Pen.addRect( bounds ).clip;
				Pen.roundedRect( bounds.insetBy(0,0), [6,6,0,0] ).clip;
				
				Pen.color = Color.gray(0.2).alpha_(0.125);
				
				hlines.do({ |hline,i|
					hline = hline.linlin( range.neg,range, bounds.height, 0, \none );
					Pen.line( 0@hline, bounds.width@hline )
					});
				dimvlines.do({ |vline,i|
					Pen.line( vline@0, vline@bounds.height );
					});
				Pen.stroke;
			
				Pen.color = Color.gray(0.2).alpha_(0.5);
				vlines.do({ |vline,i|
					Pen.line( vline@0, vline@bounds.height );
					});
				Pen.line( 0@zeroline, bounds.width@zeroline ).stroke;
				
				/*
				Pen.color = Color.white.alpha_(0.5);
				Pen.fillRect( Rect( 33, 0, 206, 14 ) );
				*/
				
				Pen.font = eq[ \font ];
				
				Pen.color = Color.gray(0.2).alpha_(0.5);
				hlines.do({ |hline|
					Pen.stringAtPoint( hline.asString ++ "dB", 
						3@(hline.linlin( range.neg,range, bounds.height, 0, \none ) 
							- strOffset) );
					});
				vlines.do({ |vline,i|
					Pen.stringAtPoint( ["100Hz", "1KHz", "10KHz"][i], 
						(vline+2)@(bounds.height - (strOffset + 1)) );
					});
				
				//Pen.roundedRect( bounds.insetBy(0.5,0.5), [5,5,0,0] ).stroke;
				
				/*
				if( eq[ \selected ] != -1 )
					{ Pen.stringAtPoint(
						[ "low shelf: %hz, %dB, rs=%",
						  "peak 1: %hz, %dB, rq=%",
						  "peak 2: %hz, %dB, rq=%",
						  "peak 3: %hz, %dB, rq=%",
						  "hi shelf: %hz, %dB, rs=%"
						][ eq[ \selected ] ].format(
							eq[ \frdb ][eq[ \selected ]][0],
							eq[ \frdb ][eq[ \selected ]][1],
							eq[ \frdb ][eq[ \selected ]][2]
							),
						35@1 );
					 }
					 { Pen.stringAtPoint( "shift: snap, alt: rq", 35@1 ); };
				*/
						
				values.do({ |svals,i|
					var color;
					color = Color.hsv(
						i.linlin(0,values.size,0,1), 
						0.75, 0.5).alpha_(if( eq[ \selected ] == i ) { 0.75 } { 0.25 });
					Pen.color = color;
					Pen.moveTo( 0@(svals[0]) );
					svals[1..].do({ |val, i|
						Pen.lineTo( (i+1)@val );
						});
					Pen.lineTo( bounds.width@(bounds.height/2) );
					Pen.lineTo( 0@(bounds.height/2) );
					Pen.lineTo( 0@(svals[0]) );
					Pen.fill;
					
					Pen.addArc( pt[i], 5, 0, 2pi );
					
					Pen.color = color.alpha_(0.75);
					Pen.stroke;
		
					});
				
				Pen.color = Color.blue(0.5);
				Pen.moveTo( 0@(svals[0]) );
				svals[1..].do({ |val, i|
					Pen.lineTo( (i+1)@val );
					});
				Pen.stroke;
				
				Pen.extrudedRect( bounds, [6,6,0,0], 1, inverse: true );
				
				
		
			
			};
		eq[ \pu_filebuttons ][1].action.value; // revert
		 window.refresh;
		 
		//eq[ \uvw ].refreshInRect( eq[ \uvw ].bounds.insetBy(-2,-2) );
		);
		
		(
		eq[ \playing ] = false;
		eq[ \numChannels ] = numCha; /// 3 channels
		eq[ \doOnServerTree ] = {
			if( eq[ \playing ] ) { { eq[ \play ].value; }.defer(0.05); };
			};
			
		eq[ \play ] = {
			/*
			// single synth version
			 eq[ \synth ] = Synth.basicNew( "eq_%ch".format( eq[ \numChannels ] ) );
				s.sendBundle( nil, eq[ \synth ].newMsg( s, addAction: \addAfter ), 
					eq[ \synth ].setnMsg(  \eq_controls, eq[ \toControl ].value ) ); 
			*/
			s.waitForBoot{
			// group version (more flexible, more overhead)
			eq[ \group ] = Group.basicNew(s, s.nextPermNodeID );
			eq[ \synths ] = eq[ \numChannels ].collect({ |i|
				Synth.basicNew( "param_beq", nodeID: s.nextPermNodeID );
				});
					
			s.sendBundle( nil, 
				eq[ \group ].newMsg( s, \addAfter ),
				*(eq[ \synths ].collect({ |synth, i|
					synth.newMsg( eq[ \group ], [ \in, i, \doneAction, [14,2].clipAt(i) ]);
					}) ++ [ eq[ \group ].setnMsg(  \eq_controls, eq[ \toControl ].value ) ] )
				);
			
			
			eq[ \playing ] = true;
			ServerTree.add( eq );
			}
			};
			
		eq[ \free ] = {
			eq[ \group ].release;
			s.freePermNodeID( eq[ \group ].nodeID );
			eq[ \synths ].do({ |item|
				s.freePermNodeID( item.nodeID );
			});
			eq[ \group ] = nil;
			eq[ \playing ] = false;
			ServerTree.remove( eq );
			};
			
		window.onClose = { if( eq[ \playing ] != false ) { eq[ \free ].value; }; };
		
		(
		eq[ \ar ] = { |input|
			var frdb;
			frdb = eq[ \fromControl ].value( Control.names([\eq_controls]).kr( 0!15 ) );
					
			input = BLowShelf.ar( input, *frdb[0][[0,2,1]].lag(0.1) );
			input = BPeakEQ.ar( input, *frdb[1][[0,2,1]].lag(0.1));
			input = BPeakEQ.ar( input, *frdb[2][[0,2,1]].lag(0.1));
			input = BPeakEQ.ar( input, *frdb[3][[0,2,1]].lag(0.1));
			input = BHiShelf.ar( input, *frdb[4][[0,2,1]].lag(0.1));
			input = RemoveBadValues.ar( input );
			
			input;
			};
			
		eq[ \synthdef ] = SynthDef( "param_beq", { |in = 0, gate = 1, fadeTime = 0.05, 
					doneAction = 2|
				// doneAction 14: free group, doneAction 2: free node
				var frdb, input, env;
				env = EnvGen.kr( Env.asr(fadeTime,1,fadeTime), gate, doneAction: doneAction );
				input = In.ar(in, 1 );
				input = eq[ \ar ].value( input );
				XOut.ar( in, env, input );
			}).store;
		
		);
		
		);
		
		eq[ \play ].value;
		
		}
	
	
	}