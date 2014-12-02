// wslib 2006

SCAlert {
	
	classvar <>modal = true;
	
	var <string, <buttons, <>actions, <color, <iconName, iconView, stringView, <buttonViews;
	var <window, <>onCloseIndex = -1;
	var <>buttonClosesWindow = true;
	
	string_ {  |newString = ""|
		string = newString;
		if( window.notNil && { window.isClosed.not } )
			{ stringView.string = string };
		}
		
	iconName_ { |newIconName = \warning|
		iconName = newIconName.asSymbol;
		if( window.notNil && { window.isClosed.not } )
			{ iconView.refresh; };
		}
		
	color_ { |newColor|
		color = newColor ? Color.red.alpha_(0.75);
		if( window.notNil && { window.isClosed.not } )
			{ window.refresh; };
		}
		
	background { ^window.view.background }
	background_ { |aColor|
		if( window.notNil && { window.isClosed.not } )
			{  window.view.background = aColor };
		}
		
	hit { |index| // focussed or last if no index provided
		if( window.notNil && { window.isClosed.not } )
			{ index = index ?? { buttonViews.detectIndex({ |bt| bt.hasFocus }) ?
					 ( buttonViews.size - 1 ) }; 
				 buttonViews[ index ] !? 
				 	{ buttonViews[ index ].action.value( buttonViews[ index ], this ) }
				};
		}
		
	enable { |index| if( index.notNil ) // all if no index provided
				{ buttonViews[ index ].enabled_( true ) }
				{ buttonViews.do( _.enabled_( true ) ) };
		}
		
	disable { |index| if( index.notNil )
				{ buttonViews[ index ].enabled_( false ) }
				{ buttonViews.do( _.enabled_( false ) ) };
		}
		
	isEnabled { |index| if( index.notNil )
			{ ^buttonViews[ index ].enabled }
			{ ^buttonViews.collect( _.enabled ) };
		}
		
	focus { |index| if( index.notNil )
				{ buttonViews[ index ].focus( true ); }
				{ buttonViews.last.focus( true ); }
		}
		
	buttonLabel { |index = 0|
		^buttonViews.wrapAt( index ).states[0][0];
		}
	
	buttonLabel_ { |index = 0, newLabel = ""|
		buttonViews.wrapAt( index ).states = [ [ newLabel ] ];
		buttonViews.wrapAt( index ).refresh;
		buttons[ index.wrap( 0, buttons.size - 1 ) ] = newLabel;
		}
	
		
	*new { | string = "Warning!", buttons, actions, color, background, iconName = \warning,
			border = true |
		^super.newCopyArgs( string, buttons, actions, color, iconName ).init( background, border );
		}
		
	openAgain { ^this.init; }
		
	init { |background, border|
		//var buttonViews;
		var charDict;
		
		background = background ? Color.white;
		color = color ? Color.red.alpha_( 0.75 );
		buttons = buttons ?? 
			{buttons = [	["cancel"],
						["ok"]
					 ];
			};
		
		buttons = buttons.collect( { |item|
			case { item.isString }
				{ [ item,  ] }
				{ item.class == Symbol }
				{ [ item.asString ] }
				{ item.isArray }
				{ item }
				{ true }
				{ [ item.asString ] }
			} );
				
		actions = actions ?? { ( { |i| { |button| buttons[i][0].postln; } } ! buttons.size ); };
			
		if( modal && { GUI.id == \cocoa } )		
			{ window = SCModalWindow( "Alert", 
				Rect.aboutPoint( Window.screenBounds.center, 
					((buttons.size * 42) + 2).max( 160 ), 
						((26 + (string.occurrencesOf( $\n ) * 10) ) + 4).max( 52 )
						), false, border ? true );
			} {
			window = Window( "Alert", 
				Rect.aboutPoint( Window.screenBounds.center, 
					((buttons.size * 42) + 2).max( 160 ), 
						((26 + (string.occurrencesOf( $\n ) * 10) ) + 4).max( 52 )
						), false, border ? true );
			
			window.front;
			};
		
		window.view.background_( background );
		window.alwaysOnTop_( true );
		window.alpha_( 0.95 );
		window.perform( if( window.respondsTo( \drawFunc_ ) ) { \drawFunc_ } { \drawHook_ }, { |w|
			Pen.width = 2;
			Pen.color = color;
			Pen.strokeRect( w.bounds.left_(0).top_(0).insetBy(1, 1) );
			} );
		
		iconView = UserView( window, Rect( 4,4, 72, 72) ).drawFunc_({ |vw|
			Pen.color = color;
			DrawIcon.symbolArgs( iconName, vw.bounds );
			}).canFocus_( false );
		
		stringView = StaticText(window, Rect(80,4, window.bounds.width - 84, 
				window.bounds.height - 28 ) )
			.string_( string ).font_( Font( "Helvetica-Bold", 12 ) );
			//.align_( \center );
		
		buttonViews = { |i| 
			var rect;
			rect = Rect( 
					(window.view.bounds.width) - ((buttons.size - i ) * 84), 
					window.view.bounds.height - 24, 80,20 );
			Button(window, rect)
					.states_( [
						buttons[i] ] )
					.action_( { |button|
						if( button.enabled )
							{ actions.wrapAt(i).value( button, this );
								if( buttonClosesWindow && { window.isClosed.not } )
									{ window.close; };
								};
						} );
					} ! buttons.size;
					
		buttonViews.last.focus;
		
		charDict = ();
		buttonViews.do({ |item, i| // keydownactions for first letters of buttons
			charDict[ item.states[0][0][0].toLower.asSymbol ] = { 
				item.action.value( item, this ) };
			});
		
		buttonViews.do({ |item|
			item.keyDownAction = { |v, char, a,b|
				case { [13,3].includes( b ) } // enter or return
					{ v.action.value( v, this ) }
					{ true }
					{ charDict[ char.asSymbol ].value; };
				};
			});
		
		window.refresh;
		window.onClose_({ buttonViews[onCloseIndex] !? 
			{ buttonViews[onCloseIndex].action.value( buttonViews[onCloseIndex], this ) }; });
		//^super.newCopyArgs( window, string, buttonViews, actions, color, iconName, iconV, strV );
	}
}

SCRequestString {

	var <>modal = true;

	var <window, <stringView, <buttonViews, <action, <>keyDownAction;
	
	*new { |default="", question = "Please enter string:", action|
		^super.new.init( default, question, action );
			}
	
	init { |default="", question = "Please enter string:", func|
		
		//var window, buttons, buttonViews, stringView;
		var extraLines, buttons;
		
		buttons =[["cancel"], ["ok"]];
		
		if( GUI.id != \cocoa ) { modal = false };
						
		action = func ? { |inString| inString.postln };
						
		extraLines = (default.size / 56).floor;
		
		if( modal )
		{	window = SCModalWindow( question, 
				Rect.aboutPoint( Window.screenBounds.center, 175, 
				27 + ( 6 * extraLines ) ), false );
		}
		{	window = Window( question, 
				Rect.aboutPoint( Window.screenBounds.center, 175, 
				27 + ( 6 * extraLines ) ), false );
			
			window.front;
			window.alwaysOnTop_( true );
		};
		
		window.view.decorator = FlowLayout( window.view.bounds );
		window.alpha_( 0.9 );
		
		stringView = TextView(window, 340@(18 + ( 12 * extraLines )) )
			.string_( default );
				
		//stringView.enterInterpretsSelection_( true );
		stringView.keyDownAction_({ |v, char, mod, unicode, keycode|
				keyDownAction.value( v, char, mod, unicode, keycode );
				
				/*
				if( unicode == 27 ) //// Crashes !!! why?
					{ { window.close }.defer(0.1); };
				*/
				
				if( unicode == 13 ) // ok
					{ action.value( v.string );  //// Crashes sometimes !!! why?
					  { window.close }.defer(0.1);
					  };
			 	});
		
		CompositeView( window, 170@20 ); // move buttons to right
			
		buttonViews = [ 
			Button(window, 80@20)
					.states_( [ ["cancel"] ] )
					.action_( { |button| window.close; } ),
			 Button(window, 80@20)
					.states_( [ ["ok"] ] )
					.action_( { |button|
						action.value( stringView.string );
						window.close;
							} )
					];
					
		buttonViews.last.focus;
		//^super.newCopyArgs( window, stringView, buttonViews, func);
	}
	
	string { ^stringView.string }
	string_ { |newString| stringView.string_( newString ); }

}



