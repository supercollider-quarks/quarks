// wslib 2011

// TODO : make NanoKTL compatible

NanoKONTROL {
	classvar <window, <tabbedView;
	classvar <allViews;
	classvar <>inPort = 0;
	classvar <>allScenesActive = true; // if controllers double in scenes, only react to current
	
	*new { |height = 120, initMIDI = true|
		if( window.isNil or: { window.dataptr.isNil } )
			{ 
			if( initMIDI ) { MIDIWindow(true) };
			inPort =  this.findPort ? inPort;
			allViews = this.makeWindow( height );
			};
		}
		
	*findPort {
		^MIDIClient.sources.detectIndex({ |item| item.device == "nanoKONTROL" }); 
	}
		
	*buttons { |scene|
		if( scene.notNil )
			{ ^allViews[ scene ][18..]; }
			{ ^allViews.collect( _[18..] ) };
		}
	
	*sliders { |scene|
		if( scene.notNil )
			{ ^allViews[ scene ][..8]; }
			{ ^allViews.collect( _[..8] ) };
		}
		
	*knobs { |scene|
		if( scene.notNil )
			{ ^allViews[ scene ][9..17]; }
			{ ^allViews.collect( _[9..17] ) };
		}
		
	*setScene { |scene = 0| 
		tabbedView.focus( scene );
		this.changed( \scene, scene );
	}
	
	*currentScene { ^tabbedView.activeTab }
	
	*isInScene { |scene| ^this.currentScene == scene }
		
	*makeWindow {	 |height = 120|
		//var tabbedView;
		
		window =  Window( "nanoKONTROL", Rect(800, 80, 120+(9*54), height+8+16), false ).front; 
		
		tabbedView = TabbedView( window, window.view.bounds, 
			(1..4).collect(_.asString ),
			({ |i| Color.hsv( i.linlin(0,4,0,1), 0.5, 0.5 ).alpha_(0.5); }!4) );
		
		// enable live scene switching
		MIDIIn.sysex = { |port, msg| if( msg[..8] == 
			Int8Array[ -16, 66, 64, 0, 1, 4, 0, 95, 79] )
				{ { this.setScene( msg[9] ) }.defer; };
				};
		
		window.onClose_({ MIDIIn.sysex = nil });
		
		^tabbedView.views.collect({ |view, i| this.makeScene(height, i+1, view) });
		}
		
	*makeScene {	|height = 120, scene = 1, view|
		var sliders, knobs, buttons, controls;
		var controlsContainer;
		var sliderContainers;
		var allViews, allControllers, usedChannels, usedControllers, resp;
		
		view = view ? { Window( "nanoKontrol :: scene %".format(scene), 
			Rect(100, 100, 120+(9*54), height+8 ), false ).front; };
		view.addFlowLayout;
		
		controlsContainer = CompositeView( view, 112@height );
		
		
		StaticText( controlsContainer, Rect( 0, 0, 112, height - 70) )
			.string_("KORG").align_( \center )
			.font_( Font( "Helvetica-Bold", 13 ) );
		
	
		StaticText( controlsContainer, Rect( 0, height - 12, 112, 12 ) )
			.string_("SCENE %".format(scene) ).align_( \center )
			.font_( Font( "Helvetica-Bold", 10 ) );
		
		controlsContainer = CompositeView( controlsContainer, Rect(0,height-65, 112, 50) )
			.background_( Color.gray(0.85).alpha_(0.75) );
			
		controlsContainer.addFlowLayout;
		
		controls = { |i|
			var state;
			state = [ 'rewind', 'play', 'forward', "O", 'stop', 'record' ][i];
			
			RoundButton( controlsContainer, 32@19 ).radius_(4)
				.states_( [
				[ state, if( i != 5 ) { Color.black } { Color.red }, Color.white.alpha_(0.5) ],
				[ state, if( i != 5 ) { Color.black } { Color.red }, 
					Color.red.blend( Color.white, 0.25 ).alpha_(0.5) ]]
					);
			}!6;
		
		sliderContainers = { 
			CompositeView( view, 50@height )
				.background_( Color.gray(0.85).alpha_(0.75) );
			 }!9;
			 
		sliderContainers.do({ |ct, i|
			var knob, slider, button;
			StaticText( ct, Rect( 4, 4, 20, 20 ) ).string_( (i+1).asString );
			knob = Knob( ct, Rect( 50 - 28, 4, 24, 24 ) ).value_(0.5).centered_(true);
			slider = SmoothSlider( ct, Rect( 50 - 26, 32, 20, height - (4+32) ) );
			button = { |i|
				RoundButton( ct, [Rect( 4, 32, 15, 15 ), Rect( 4, height - (4+15), 15, 15 )][i])
					.states_([[ "", Color.clear, Color.white.alpha_(0.5) ], 
						[ 'record', Color.red.alpha_(0.5), 
							Color.red.blend( Color.white, 0.25 ).alpha_(0.5) ]])
					.radius_(3)
				}!2;
			
			knobs = knobs.add( knob );
			sliders = sliders.add( slider );
			buttons = buttons.add( button );
			});
		
		
		allViews = sliders ++ knobs ++ buttons.flop[0] ++ buttons.flop[1] ++ controls;
		
		// factory preset scenes:
		allControllers = switch( scene,
			1, { [[0], (2..41).select({ |item| 
				[7,10,11,32].includes( item ).not }) ++ [47, 45, 48, 49, 46, 44 ] ].flop;
				},
			2, { [[0], (42..84).select({ |item| 
				[47, 45, 48, 49, 46, 44, 64].includes(item).not}) ++ 
					[47, 45, 48, 49, 46, 44 ] ].flop;
				},
			3, { [[0], (85..124).select({ |item| 
				[98, 99, 100, 101].includes( item ).not }) ++ [47, 45, 48, 49, 46, 44 ] ].flop;
				},
			4, { [7,10,16,17].collect({ |item| 9.collect([_,item]) }).flatten(1) ++
				([ [0], [47, 45, 48, 49, 46, 44 ] ].flop);
				});
			
		allControllers.do({ |item|
			if( (usedChannels ? []).includes( item[0] ).not )
				{ usedChannels = usedChannels.add( item[0] ) };
			if( (usedControllers ? []).includes( item[1] ).not )
				{ usedControllers = usedControllers.add( item[1] ) };
			});
			
		resp = CCResponder( { |port, chan, cc, val| 
			var view;
			if( allScenesActive or: { this.isInScene( scene-1 ) } ) {				view =  allViews[ allControllers.detectIndex({ |item| item == [chan, cc] }) ]; 
				if( view.class == SCKnob )
					{ { view.valueAction = val/127; }.defer; }
					{ view.valueAction = val/127; };
			};
		}, inPort, usedChannels, usedControllers );
				 
		view.onClose_({ resp.remove });
		
		^allViews;
		}
	
	}