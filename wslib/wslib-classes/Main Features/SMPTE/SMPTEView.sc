SMPTEView { // fps = 1000
	var <>parent, <view, <pos = 0, <selected = -1, <fontSize = 20;
	var <fontName, <fontColor, <background;
	var <>keyDownAction, <>keyUpAction, <>mouseDownAction, <>mouseMoveAction, <>mouseUpAction;
	var <>action;
	var <>minTime = 0, <>maxTime = 62330.591114739;
	
	*new { |parent, bnds, fontSize = 20| // bounds can be point; only top and left are used anyway
		this.deprecated( thisMethod );
		^super.newCopyArgs( parent ).init( bnds ? (0@0), fontSize );
		}
	
	init { |inBounds, inFontSize = 20|
		var counterDragStart;
		if( inBounds.class == Point ) 
			{ inBounds = Rect( inBounds.x, inBounds.y, 0,0 ); };
		
		fontColor = fontColor ? Color.black.alpha_(0.5);
		fontName = fontName ? "Monaco";
		fontSize = inFontSize ? 20;
		
		view = UserView( parent, Rect( inBounds.left, inBounds.top, 
				(fontSize * 7.2) + 2, fontSize * 1.2 ) )
			//.relativeOrigin_( false )
			//.canFocus_( false )
			;
		
		view.drawFunc = { |v|
			if( background.notNil )
				{ Pen.color = background; Pen.fillRect( v.drawBounds ) };
					
			pos.asSMPTEString( 1000 )
				.drawStretchedIn( v.drawBounds, Font( fontName, fontSize ), fontColor );
				//.drawAtPoint( v.drawBounds.leftTop, Font( fontName, fontSize ), fontColor );
		
			if( v.hasFocus && { [-1,3,6,9].includes( selected ).not } )
				{ 
				Pen.color = Color.black;
				Pen.strokeRect( Rect( 
					(v.drawBounds.left + ((11 - selected) * ((v.drawBounds.width) / 12)))
						-1,
					v.drawBounds.top, (v.drawBounds.width) / 12, v.drawBounds.height ) ); };
				
			};
			
		view.mouseDownAction = { |v, x, y, mod|
			selected = (11 - (((x+1) - v.drawBounds.left) /
				((v.drawBounds.width) / 12) ).floor).asInt;
			counterDragStart = [x@y, pos.copy];
			mouseDownAction.value( this, x, y, mod );
			};
			
		view.mouseMoveAction = { |v, x, y, mod|
			var scaler;
			scaler = [ 0.001, 0.01, 0.1, nil, 1, 10, nil, 60, 600, nil, 3600, 36000 ][ selected ];
			if( scaler.notNil )
				{ pos = ( ((counterDragStart[0].y - y) * scaler) + counterDragStart[1] )
						.max( minTime ).min( maxTime );
					action.value( this );
					parent.refresh; };
			mouseMoveAction.value( this, x, y, mod );
			};
	
		view.mouseUpAction = { |v, x, y, mod|
			
			mouseUpAction.value( this, x, y, mod );
			action.value( this );
			parent.refresh;
			};
		
			
		view.keyDownAction = { |v, char, a, b|
			var validSelections;
			var scaler;
			validSelections = [0,1,2,4,5,7,8,10,11];
			if( char.isDecDigit && { validSelections.includes( selected ) } )
				{	pos = pos.asSMPTEString( 1000 )
						.overWrite( char.asString, 11 - selected )
						.asSeconds( 1000 );
					action.value( this );
					parent.refresh;
					
				};
				
			if( b == 63234 ) // <-
				{ selected = validSelections[ 
					(validSelections.indexOf( selected ) ? 10) + 1 ] ? 11;
					parent.refresh; };
			if( b == 63235 ) // ->
				{ selected = validSelections[ 
					(validSelections.indexOf( selected ) ? 1) - 1 ] ? 0;
					parent.refresh; };
			if( b == 63233 ) // v
				{	
				scaler = [ 0.001, 0.01, 0.1, nil, 1, 10, nil, 
						60, 600, nil, 3600, 36000 ][ selected ];
				if( scaler.notNil )
						{ pos = (pos - scaler).max(0).min( 60*60*9 );
						action.value( this );
						parent.refresh;
						};
				};
			if( b == 63232 ) // ^
				{	
				scaler = [ 0.001, 0.01, 0.1, nil, 1, 10, nil, 
						60, 600, nil, 3600, 36000 ][ selected ];
				if( scaler.notNil )
						{ pos = (pos + scaler).max(0).min( 60*60*9 ); 
						action.value( this );
						parent.refresh; };
				};
			keyDownAction.value( this, char, a, b );
			
			};
			
		view.keyUpAction = { |v, char, a,b| 
			var validSelections;
			validSelections = [0,1,2,4,5,7,8,10,11];
			if( char.isDecDigit && { validSelections.includes( selected ) } )
				{ 
				selected = validSelections[ validSelections.indexOf( selected ) - 1 ] ? -1; 
				action.value( this );
				parent.refresh;
				};
			
			keyUpAction.value( this, char, a, b );
			};
			
				
		
		}
	
	pos_ { |newPos| pos = newPos ? pos; parent.refresh; }
	posD_ { |newPos| pos = newPos ? pos; { parent.refresh }.defer; }
	//posNoRefresh_ { |newPos| pos = newPos ? pos; }
	selected_ { |index| selected = index ? selected; parent.refresh; }
	fontSize_ { |newFontSize| fontSize = newFontSize ? fontSize;
		view.bounds = Rect( view.bounds.left, view.bounds.top, 
				(fontSize * 7.2) + 1, fontSize * 1.2 );
		 parent.refresh; }
	fontName_ { |name| fontName = name ? "Monaco"; parent.refresh; }
		 
	fontColor_ { |color| fontColor = color ? fontColor; parent.refresh; }
	background_ { |color| background = color; parent.refresh; }
	
	value { ^pos; }
	string { ^pos.asSMPTEString( 1000 ); }
	smpte { |fps| ^SMPTE( pos, fps ? 1000 ) }
	
	value_ { |val| pos = val ? pos; }
	string_ { |string| pos = string.asSeconds; parent.refresh; }
	smpte_ { |smpte| pos = smpte.asSMPTE.fps_( 1000 ).asSeconds; parent.refresh; }
	
	bounds { ^view.bounds }
	bounds_ { |newBounds| view.bounds = newBounds ? view.bounds; }
		
	}