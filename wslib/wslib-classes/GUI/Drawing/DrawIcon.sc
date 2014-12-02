// wslib 2006
//
// bank of iconographic Pen-functions

DrawIcon {
	classvar <drawFuncs;
	
	*new { |name, rect ... more|
		rect = (rect ? (32@32)).asRect;
		^drawFuncs[ name ].value( rect, *more ); // returns pen function
		}
		
	*symbolArgs { |name, rect|
		var more;
		more = name.asString.split( $_ );
		name = more[0].asSymbol;
		more = more[1..].collect( _.interpret );
		^this.new( name, rect, *more );
		}
		
	*names { ^drawFuncs.keys }
		
	// use:
	//  DrawIcon( \play, Rect( 20, 20, 30, 30 ) )
	// within a drawfunc this will draw a "play" icon centered in the given rect
	
	*initClass {
		// add more later
		drawFuncs = (
			none: { },
			
			triangle: { |rect, angle = 0, size = 1, width = 1, mode = \fill|
				var radius, center, backCenter;
				radius = (rect.width.min( rect.height ) / 4) * size;
				center = rect.center + Polar( radius * (2/9), angle );
				backCenter =  center + Polar( radius, angle + pi ).asPoint;
				Pen.moveTo( backCenter );
				Pen.lineTo( backCenter + Polar( radius * width, angle + 1.5pi ).asPoint );
				Pen.lineTo( center + Polar( radius, angle ).asPoint );
				Pen.lineTo( backCenter + Polar( radius * width, angle + 0.5pi ).asPoint );
				Pen.lineTo( backCenter );
				Pen.perform( mode );
				},
				
			play:{ |rect, mode = \fill|  // triangle ( > )
				drawFuncs[ \triangle ].value( rect, 0, 1, 1, mode );
				},
				
			back: { |rect, mode = \fill|  // triangle ( < )
				drawFuncs[ \triangle ].value( rect, pi, 1, 1, mode );
				},
								
			up: { | rect, mode = \fill|  // triangle ( ^ )
				drawFuncs[ \triangle ].value( rect, 1.5pi, 1, 1, mode );
				},
				
			down: { | rect, mode = \fill|  // triangle ( v )
				drawFuncs[ \triangle ].value( rect, 0.5pi, 1, 1, mode );
				},
				
			stop: { | rect, mode = \fill| // square
				var square;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
				if( mode == \fill )
					{ Pen.fillRect( square ); }
					{ Pen.strokeRect( square ); };
				},
				
			speaker:	{ | rect, mode = \fill| // square
				var square;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 3, 
						rect.width.min( rect.height ) / 3 );
				
				square = square.insetBy( square.width / 6, 0 );
				square = square.moveBy( square.width / -12, 0 );
	
				Pen.moveTo( square.rightTop );
				Pen.lineTo( square.rightBottom );
				Pen.lineTo( (square.left + (square.width / 2.5))@
					(square.center.y + (square.width / 4)) );
				Pen.lineTo( square.left@(square.center.y + (square.width / 4)) );
				Pen.lineTo( square.left@(square.center.y - (square.width / 4)) );
				Pen.lineTo( (square.left + (square.width / 2.5))@
					(square.center.y - (square.width / 4)) );
				Pen.lineTo( square.rightTop );
				Pen.perform( mode );
				},

			record:	{  | rect, mode = \fill|  // circle at 1/2 size
				var square;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
				if( mode == \fill )
					{ Pen.fillOval( square ); }
					{ Pen.strokeOval( square ); }		
				},
				
			sign: { |rect, text = "@", font, color|
				var center, radius, textBounds;
				center = rect.center; 
				radius = rect.width.min( rect.height ) / 2;
				
				font = font ? Font( "Monaco", 12 );
				textBounds = text.asString.bounds( font );
				Pen.use({
					var scaleFactor;
					scaleFactor =  (radius * 2) / ( textBounds.width.max( textBounds.height ) );
					//scaleFactor = 2.1;
				
					//Pen.scale( radius / ( textBounds.width.max( textBounds.height ) ) );
					
					Pen.translate( center.x, center.y );
					Pen.scale( scaleFactor, scaleFactor );
					text.asString.drawAtPoint( 
					
						 (0@0) - textBounds.center, font, color ? Color.black );
					});

				},
												
			pause:	{ | rect| // two vertical stripes ( || )
				var square;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
	
				Pen.fillRect( square - Rect( 0, 0, square.width / 1.5, 0  ) );
				Pen.fillRect( square + Rect( square.width / 1.5, 0, 
					square.width.neg / 1.5, 0  ) );
				},
				
			skip: { | rect, angle = 0| // triangle and stripe ( >| )
				var square, wd, w2;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
					
				w2 = square.width / 2;
				wd = square.width * 1/4;
				
				Pen.use({	
					Pen.translate( *square.center.asArray );
					Pen.rotate( angle );
					Pen.moveTo( ( w2 @ w2 ).neg );
					Pen.lineTo( (w2 - wd) @ 0);
					Pen.lineTo( w2.neg @ w2 );
					Pen.addRect( Rect( w2 - wd, w2.neg, wd, 2 * w2 ) );
					Pen.fill;
				});
				},
				
			return:	{ | rect| // stripe and backwards triangle ( |< )
				drawFuncs[ \skip ].value( rect, pi );
				},
				
			forward:	{ | rect| // 2 triangles ( >> )
				var square, wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				wd = square.width * 1/4;
				square = square.insetBy( 0, wd / 1.5 );
				square = square.resizeBy( wd / 1.5, 0);
					
				Pen.moveTo( square.leftTop );
				Pen.lineTo( square.center );
				Pen.lineTo( square.leftBottom );
				Pen.fill;
				
				Pen.moveTo( square.center.x@square.top );
				Pen.lineTo( square.right@square.center.y );
				Pen.lineTo(square.center.x@square.bottom );
				Pen.fill;
				},
			
			rewind:	{ | rect| // 2 triangles ( << )
				var square, wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				wd = square.width * 1/4;
				square = square.insetBy( 0, wd / 1.5 );
				square = square.resizeBy( wd / 1.5, 0);
				square = square.moveBy( wd / -1.5, 0);
				
				Pen.moveTo( square.center.x@square.top );
				Pen.lineTo( square.left@square.center.y );
				Pen.lineTo(square.center.x@square.bottom );
				Pen.fill;

				Pen.moveTo( square.rightTop );
				Pen.lineTo( square.center );
				Pen.lineTo( square.rightBottom );
				Pen.fill;
				
				},
				
			delete: { | rect|  // as seen in Mail 2.0; circle with stripe
				var square, wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
				
				wd = square.width * (1/5);
				Pen.width = wd;
				Pen.addArc( square.center, square.width / 2, 0, 2pi );
				//Pen.stroke;	
				Pen.line( 
					Polar( square.width / 2, 1.25pi ).asPoint + square.center,
					Polar( square.width / 2, 0.25pi ).asPoint + square.center
					  );
				Pen.stroke;	
				
				},
				
			power:	{ | rect| // power button sign (stripe + 3/4 circle)
			
				var square, sl,sh,st,sw,sb,sr, w3,wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 3, 
						rect.width.min( rect.height ) / 3 );
						
				#sl, st, sw, sh = square.asArray;
				#sr, sb = square.rightBottom.asArray;
				wd = square.width * (1/5);
				// v2
				Pen.width = wd;
				
				Pen.addArc( square.center, sw / 3, 1.75pi, 1.5pi );
				Pen.stroke;
				Pen.line( square.center, ((sl + sr) / 2)@st );
				Pen.stroke;
				},
				
			cmd: { |rect| // apple key sign
				var square, sl,sh,st,sw;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 3, 
						rect.width.min( rect.height ) / 3 );
				#sl, st, sw, sh = square.asArray;
				Pen.width = (1/12) * sw;
	
				Pen.moveTo( ( sl + ((1/6) * sw ))@( st + ((2/6) * sh )) );
				
				Pen.lineTo( ( sl + ((5/6) * sw ))@( st + ((2/6) * sh )) );
				Pen.addArc( ( sl + ((5/6) * sw ))@( st + ((1/6) * sh )),
					 (1/6) * sw, 0.5pi, -1.5pi );
					 
				Pen.lineTo( ( sl + ((4/6) * sw ))@( st + ((5/6) * sh )) );
				Pen.addArc( ( sl + ((5/6) * sw ))@( st + ((5/6) * sh )),
					 (1/6) * sw, pi, -1.5pi );
					 
				Pen.lineTo( ( sl + ((1/6) * sw ))@( st + ((4/6) * sh )) );
				Pen.addArc( ( sl + ((1/6) * sw ))@( st + ((5/6) * sh )),
					 (1/6) * sw, 1.5pi, -1.5pi );
				
				Pen.lineTo( ( sl + ((2/6) * sw ))@( st + ((1/6) * sh )) );
				Pen.addArc( ( sl + ((1/6) * sw ))@( st + ((1/6) * sh )),
					 (1/6) * sw, 0, -1.5pi );
					 
				Pen.stroke;
				},
			
			alt: { |rect| // option key sign
				var square, sl,sh,st,sw;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 3, 
						rect.width.min( rect.height ) / 3 );
				#sl, st, sw, sh = square.asArray;
				Pen.width = (1/12) * sw;
	
				Pen.moveTo( sl@(st + ((1/4) * sh)) );
				Pen.lineTo( (sl + ((1/3) * sw))@(st + ((1/4) * sh)) );
				Pen.lineTo( (sl + ((2/3) * sw))@(st + ((3/4) * sh)) );
				Pen.lineTo( (sl + sw)@(st + ((3/4) * sh)) );
				
				Pen.moveTo( (sl + ((2/3) * sw))@(st + ((1/4) * sh)) );
				Pen.lineTo( (sl + sw)@(st + ((1/4) * sh)) );
				
				Pen.stroke;

				},
				
			shift: { |rect, direction = 0, mode = \stroke| // shift key sign 
				var square, wd;
				
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 3, 
						rect.width.min( rect.height ) / 3 );
					
				Pen.rotate( direction - 0.5pi, square.center.x, square.center.y );
				
				Pen.moveTo( (square.left)@(square.center.y) );
				Pen.lineTo( (square.left)@(square.top + (square.height * (1/4) ) ) );
				Pen.lineTo( (square.center.x)@(square.top + (square.height * (1/4) ) ) );
				Pen.lineTo( (square.center.x)@(square.top) );
				Pen.lineTo( (square.right)@(square.center.y) );
				Pen.lineTo( (square.center.x)@(square.bottom) );
				Pen.lineTo( (square.center.x)@(square.top + (square.height * (3/4) ) ) );
				Pen.lineTo( (square.left)@(square.top + (square.height * (3/4) ) ) );
				Pen.lineTo( (square.left)@(square.center.y) );
				
				Pen.width = (1/12) * square.width;
				Pen.perform( mode );
				
				Pen.rotate( (direction - 0.5pi).neg, square.center.x, square.center.y );
				
				},
				
			lock: { |rect|
				var size = rect.width.min( rect.height ) * 0.8;
				var radius = size/6;
				var corners = [ -1 @ 0, -1 @ -2, 1 @ -2, 1 @ 0 ] * radius;
				
				Pen.use({
					
					Pen.translate( *rect.center.asArray );
					
					Pen.fillRect( Rect( size.neg * 0.25,0, size / 2, size / 3 ) );
					
					2.do({
						Pen.moveTo( corners[0] )
							.arcTo( corners[1], corners[2], radius )
							.arcTo( corners[2], corners[3], radius )
							.lineTo( corners[3] );
						corners = corners.reverse * [0.5@0.75];
						radius = radius / 2;
					});
					Pen.fill;
			
					});
				},
				
			unlock: { |rect|
				var size = rect.width.min( rect.height ) * 0.8;
				var radius = size/6;
				var corners = [ 0 @ 0, 0 @ -2, 2 @ -2, 2 @ 0 ] * radius;
				
				Pen.use({
					Pen.translate( *rect.center.asArray );
					
					Pen.fillRect( Rect( (size.neg * 0.25) - (radius/2),0, size / 2, size / 3 ) );
					
					2.do({
						Pen.moveTo( corners[0] )
							.arcTo( corners[1], corners[2], radius )
							.arcTo( corners[2], corners[3], radius )
							.lineTo( corners[3] );
						corners = (corners.reverse + [ radius@0 ]) * [0.5@0.75];
						radius = radius / 2;
					});
					Pen.fill;
					
					});
				},
		
			 x: { | rect| // x
			
				var square, sl,sh,st,sw,sb,sr, w3,wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				#sl, st, sw, sh = square.asArray;
				#sr, sb = square.rightBottom.asArray;
				wd = square.width * (1/5);
				
				Pen.width = wd;
	
				Pen.moveTo( rect.center + Polar( (sw / 2), 0.25pi ).asPoint );
				Pen.lineTo( rect.center + Polar( (sw / 2), 1.25pi ).asPoint );
				Pen.moveTo( rect.center + Polar( (sw / 2), 0.75pi ).asPoint );
				Pen.lineTo( rect.center + Polar( (sw / 2), 1.75pi ).asPoint );
				Pen.stroke; 
				
				}, 
			 	 
			i: 	{ | rect| // i
				var square, sl,sh,st,sw,sb,sr, w3,wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				#sl, st, sw, sh = square.asArray;
				#sr, sb = square.rightBottom.asArray;
				wd = square.width * (1/4);
				
				Pen.fillRect( Rect( sl + (( sw - wd ) / 2) , st + (1.4 * wd), 
					wd, sh - (1.4 * wd) ) );
				Pen.fillOval( 
					Rect( sl + (( sw - wd ) / 2), st, wd, wd ).insetBy( wd * -0.1,  
						wd * -0.1   ) );
				
				},
				
			'!':  	{ | rect| // !
				var square, sl,sh,st,sw,sb,sr, w3,wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				#sl, st, sw, sh = square.asArray;
				#sr, sb = square.rightBottom.asArray;
				wd = square.width * (1/4);
				
				Pen.fillRect( Rect( sl + (( sw - wd ) / 2) , st, wd, sh - (1.33 * wd) ) );
				Pen.fillRect( Rect( sl + (( sw - wd ) / 2), sb - wd, wd, wd ) );
				
				},
			'+': 	{ | rect| // +
				var square, sl,sh,st,sw,sb,sr,wd,w3;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				#sl, st, sw, sh = square.asArray;
				#sr, sb = square.rightBottom.asArray;
				wd =  square.width * (1/4);
				w3 = (square.width - wd) / 2;
				
				Pen.moveTo( sl@( st+w3) );
				
				[ (sl+w3)@(st+w3), (sl+w3)@(st), (sr-w3)@(st), (sr-w3)@(st+w3),
					sr@(st+w3), sr@(sb-w3), (sr-w3)@(sb-w3), (sr-w3)@sb,
					(sl+w3)@sb, (sl+w3)@(sb-w3), sl@(sb-w3), sl@( st+w3) ]
					.do( Pen.lineTo( _ ) );
					    
				Pen.fill;
				
				},
			'-': 	{ | rect| // -
				var square, wd, w3;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				wd = square.width * (1/4);
				w3 = (square.width - wd) / 2;
				
				Pen.fillRect( Rect( 
					square.left, square.top + w3,
					square.width,  wd ) )
				
				},
			
			star:   { |rect, numSides = 6, start| // *

				var square, sw, wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
				
				start = start ? -0.5pi;
				sw = square.width;	
				wd = sw * (1/5);
				
				Pen.width = wd;
				
				numSides.do({ |i|
					Pen.moveTo( rect.center );
					Pen.lineTo( rect.center + Polar( (sw / 2), 
						 (2pi * ( i/numSides )) + start).asPoint );
				});
				
				Pen.stroke; 
				
				},
				
			/*
			polygon:   { |rect, numSides = 6, start, mode = \fill|

				var square, sw, wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 3, 
						rect.width.min( rect.height ) / 3 );
				
				start = start ? -0.5pi;
				sw = square.width;	
				wd = sw * (1/8);
				
				//Pen.width = wd;
				
				Pen.moveTo( square.center + Polar( (sw / 2), start ) );
				
				(numSides + 1).do({ |i|
					Pen.lineTo( square.center + Polar( (sw / 2), 
						 (2pi * ( i/numSides )) + start).asPoint );
				});
				
				Pen.perform( mode ); 
				
				},
			*/
				
			polygon:Ê { |rect, numSides = 6, start, mode = \fill, type = \normal| 
					// type can also be \star ( kindly added by Jesper Elen )
				var square, sw, wd, factor;
				if (type == \star) { if (numSides.odd)Ê
						{ factor = (numSides / 2).floor } 
						{ factor = (numSides / 2) + 1 }} 
					{ factor = 1 };				
				square = Rect.aboutPoint( rect.center,Ê
						rect.width.min( rect.height ) / 3,Ê
						rect.width.min( rect.height ) / 3 );
				start = start ? -0.5pi;
				sw = square.width;	
				wd = sw * (1/8);
				Pen.moveTo( square.center + Polar( (sw / 2), start ) );
				(numSides + 1).do({ |i|
					Pen.lineTo( square.center + Polar( (sw / 2),Ê
						(2pi * ( i/numSides * factor )) + start).asPoint );
				});
				Pen.perform( mode );Ê
				},
			
				
			lineArrow: { |rect, direction = 0, inSquare = true |
				
				var square, wd, radius;
				
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
										
				wd = (square.width) * 1/5;
				
				if( inSquare )
					{ radius = square.width/2; }
					{ radius = ( case 
							{ [0.25,0.75,1.25,1.75].includes( (direction % 2pi) / pi ) }
							{ radius = square.width/2; }
							{ 
						(direction % 2pi).inRange(0.25,0.75pi) or:
						(direction % 2pi).inRange(1.25,1.75pi) }
							{ radius = rect.height / 4  }
							{ true }
							{ radius = rect.width / 4  } );
						
						 };
							
				Pen.width_( wd );
				
				Pen.moveTo( square.center + Polar( radius, direction + pi ).asPoint );
				Pen.lineTo( square.center + Polar( radius, direction ).asPoint );
				
				Pen.moveTo( ( square.center + Polar( radius, direction ) ).asPoint
					+ Polar( wd * 2.5, direction + 0.75pi ).asPoint );
				Pen.lineTo( ( square.center + Polar( radius, direction ) ).asPoint );
				Pen.lineTo( ( square.center + Polar( radius, direction ) ).asPoint
					+ Polar( wd * 2.5, direction - 0.75pi ).asPoint );
				
				Pen.stroke;
				},
			
			
			arrow: { |rect, direction = 0, mode = \fill|
				var square, wd;
				
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
					
				Pen.rotate( direction, square.center.x, square.center.y );
				
				Pen.moveTo( (square.left)@(square.center.y) );
				Pen.lineTo( (square.left)@(square.top + (square.height * (1/3) ) ) );
				Pen.lineTo( (square.center.x)@(square.top + (square.height * (1/3) ) ) );
				Pen.lineTo( (square.center.x)@(square.top) );
				Pen.lineTo( (square.right)@(square.center.y) );
				Pen.lineTo( (square.center.x)@(square.bottom) );
				Pen.lineTo( (square.center.x)@(square.top + (square.height * (2/3) ) ) );
				Pen.lineTo( (square.left)@(square.top + (square.height * (2/3) ) ) );
				Pen.lineTo( (square.left)@(square.center.y) );
				
				Pen.perform( mode );
				
				Pen.rotate( direction.neg, square.center.x, square.center.y );
				
				},
			
			roundArrow: { |rect, startAngle = -0.5pi, arcAngle = 1.5pi, width = 0.25|
				var square, radius, wd, arrowSide;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
				
				radius = square.height / 2;
				wd = radius * width;
				arrowSide = (wd*1.5) * 2.sqrt;
				
				Pen.addAnnularWedge( square.center, radius - wd, radius, startAngle, arcAngle );
				Pen.moveTo( Polar( radius + wd, startAngle + arcAngle ).asPoint + square.center );
				Pen.lineTo( Polar( radius + wd, startAngle + arcAngle ).asPoint +
						Polar( arrowSide, (startAngle + arcAngle) + 0.75pi ).asPoint 
							+ square.center);
				Pen.lineTo( Polar( radius - (wd*2),  startAngle + arcAngle ).asPoint 
							+ square.center );
				Pen.lineTo( Polar( radius + wd, startAngle + arcAngle ).asPoint + square.center ); 
				Pen.fill;
			
				
				},
			
			clock: { |rect, h = 4, m = 0, secs| // draw seconds when not nil
				var square, radius, hSize, mSize, hAngle, mAngle, wd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
				radius = square.height / 2;	
				wd = radius * (1/6);
				hSize = radius * (3/5);
				mSize = radius * (4/5);
				m = m + ( ( secs ? 0) / 60 ); 
				h = h + (m / 60);
				
				hAngle = ((h / 12) * 2pi) - 0.5pi;
				mAngle = ((m / 60) * 2pi) - 0.5pi;
				
				Pen.width = wd;
				
				Pen.addArc( square.center, radius, 0, 2pi );
				Pen.stroke;
				
				Pen.line( Polar( wd/2, hAngle - pi ).asPoint + square.center, 
					Polar( hSize, hAngle ).asPoint + square.center );
				//Pen.stroke;
				
				Pen.line( Polar( wd/2, mAngle - pi ).asPoint + square.center, 
					Polar( mSize, mAngle  ).asPoint + square.center );
				Pen.stroke;
				
				if( secs.notNil )
					{ Pen.width = wd / 3;
						Pen.line( Polar( wd/2, ((secs / 60) * 2pi) - 1.5pi ).asPoint 
								+ square.center, 
						Polar( mSize, ((secs / 60) * 2pi) - 0.5pi  ).asPoint 
								+ square.center );
						Pen.stroke;
					};
				},
				
			wait: { |rect, pos = 0, numLines = 16, active = true, color| 
					
				var radius, center, lineWidth = 0.08, centerSize = 0.5;				color = color ? Color.black;
				radius = rect.width.min( rect.height ) / 4;
				center = rect.center;
				if( active )
					{ Array.series( numLines + 1, -0.5pi, 2pi/numLines)[1..] 
						.do({ |val, i|
							color.copy.alpha_(
								(( (i/numLines) + (1 - pos) )%1.0) * 
									( if( active ) { 1 } { 0.5 } ) 
								).set;
							Pen.width_( lineWidth * radius * 2 );
							Pen.moveTo( ((val.cos@val.sin) * (radius * centerSize )) 
									+ center );
							Pen.lineTo( ((val.cos@val.sin) * radius) + center );
							Pen.stroke;
						});
					};
				},
				
			search: { |rect| // spotlight symbol
				var square, wd, rd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
				
				wd = square.width / 6;
				square = square.insetAll( wd, wd, 0, 0 );
				rd = square.width / 4;
				
				Pen.width = wd;
				
				Pen.addArc( ( square.left + rd )@(square.top + rd ), rd + wd, 0, 2pi );
				Pen.moveTo( square.center );
				Pen.lineTo( square.rightBottom );
				
				Pen.stroke;
				
				}, 
				
			sine: { |rect, n = 1, phase = 0, res, fitToRect = false|
			
				var square, wd, step;
				
				if( fitToRect )
					{ square = Rect.aboutPoint( rect.center, 
						rect.width / 2.5, 
						rect.height / 4 ); }
					{ square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 ); };
				
				
				wd = square.height.min( square.width ) / 12;
				
				res = res ?? { (square.width / 6).ceil.max( 50 ) };
				
				
				step = (square.width / (n*res));
				
				Pen.width = wd;
				
				((n*res) + 1).do({ |i|
					var point;
					point = ((i*step) + square.left)
						@(((( (i / res) * 2pi ) + phase).sin.neg + 1 * (square.height / 2))
							+ square.top);
					if( i == 0 )
						{ Pen.moveTo( point ) }
						{ Pen.lineTo( point ) };
					});
				Pen.stroke;
				
				},
				
			document: { |rect|
				var square, docRect, wd, rd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				wd = square.width / 16;
				
				rd = square.width / 4;
				
				docRect = square.insetBy( 
					(square.width / 2) - (( square.height / 2.sqrt ) / 2) ,0);
					
				Pen.width = wd;
					
				Pen.moveTo( (docRect.center.x)@(docRect.top) );
				Pen.lineTo( (docRect.right - rd)@(docRect.top) );
				
				Pen.lineTo( (docRect.right)@(docRect.top + rd ) );
				Pen.lineTo( docRect.rightBottom );
				Pen.lineTo( docRect.leftBottom );
				Pen.lineTo( docRect.leftTop );
				Pen.lineTo( (docRect.center.x)@(docRect.top) );
					
				
			
				Pen.moveTo( (docRect.right )@(docRect.top + rd) );
				Pen.lineTo( (docRect.right - rd )@(docRect.top + rd) );
				Pen.lineTo( (docRect.right - rd )@(docRect.top) );				
				Pen.stroke;	
				
				},
				
			file: { |rect| drawFuncs[ \document ].value( rect ); },
				
			folder: { |rect|
				var square, docRect, wd, rd;
				var folderRect;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				wd = square.width / 16;
				
				rd = square.width / 8;
				
				docRect = square.insetBy( 
					(square.width / 2) - (( square.height / 2.sqrt ) / 2) ,0);
				
				folderRect = square.insetBy( 
					0, (square.width / 2) - (( square.height / 2.sqrt ) / 2)); 
				
					
				Pen.width = wd;
				
					
				Pen.moveTo( (folderRect.right)@(folderRect.center.y) );
				Pen.lineTo( folderRect.rightBottom );
				Pen.lineTo( folderRect.leftBottom );
				Pen.lineTo( folderRect.leftTop );
				
				Pen.lineTo( (folderRect.left + rd )@(folderRect.top - rd ) );
				Pen.lineTo( ( (folderRect.left + (folderRect.width/2)) - rd )
					@(folderRect.top - rd) );
				Pen.lineTo( ( folderRect.left + (folderRect.width/2) )@folderRect.top );
				Pen.lineTo( folderRect.rightTop );
				Pen.lineTo(  (folderRect.right)@(folderRect.center.y) );
				
				
								
				Pen.stroke;	
				
				},
				
			led: { |rect, contents = '8'|  // 2 digits
				var square, docRect, wd, hOffset, vOffset = 0;
				var array, size;
				
				square = Rect.aboutPoint( rect.center, 
						rect.width / 4, rect.height / 4 );
						
				contents = contents.asString.toUpper.reverse;
				
				size = contents.size;				
				wd = ( square.width / ( size * 4 + (size - 1 ) ) )
					.min( square.height / 7 );
				
				//vOffset = (size * 2.5) - 4;
				vOffset = ( square.height - (wd * 7) ) / 2;
				hOffset = ( square.width - ( wd *  ( size * 4 + (size - 1 ) ) ) ) / 2;
				
				
				Pen.translate( hOffset + square.left, vOffset + square.top );
			
				
				array =	( { [0,0,0, 0,0,0,0] } ! size ).collect({ |item, i|
						var out;
						out = ( 	
							////   hor.   vert.
							'0': [ 1,0,1, 1,1,1,1 ],
							'1': [ 0,0,0, 0,1,0,1 ],
							'2': [ 1,1,1, 0,1,1,0 ],
							'3': [ 1,1,1, 0,1,0,1 ],
							'4': [ 0,1,0, 1,1,0,1 ],
							'5': [ 1,1,1, 1,0,0,1 ],
							'6': [ 1,1,1, 1,0,1,1 ],
							'7': [ 1,0,0, 0,1,0,1 ],
							'8': [ 1,1,1, 1,1,1,1 ],
							'9': [ 1,1,1, 1,1,0,1 ],
							'-': [ 0,1,0, 0,0,0,0 ],
							'A': [ 1,1,0, 1,1,1,1 ],
							'B': [ 0,1,1, 1,0,1,1 ],
							'C': [ 1,0,1, 1,0,1,0 ],
							'D': [ 0,1,1, 0,1,1,1 ],
							'E': [ 1,1,1, 1,0,1,0 ],
							'F': [ 1,1,0, 1,0,1,0 ],
							'G': [ 1,0,1, 1,0,1,1 ],
							'H': [ 0,1,0, 1,0,1,1 ],
							'I': [ 0,0,0, 1,0,1,0 ],
							'J': [ 0,0,1, 0,1,0,1 ],
							'L': [ 0,0,1, 1,0,1,0 ],
							'O': [ 0,1,1, 0,0,1,1 ],
							'P': [ 1,1,0, 1,1,1,0 ],
							'Q': [ 1,1,0, 1,1,0,1 ],
							'R': [ 0,1,0, 0,0,1,0 ],
							'S': [ 1,1,1, 1,0,0,1 ],
							'T': [ 0,1,1, 1,0,1,0 ],
							'U': [ 0,0,1, 1,1,1,1 ],
							'Y': [ 0,1,1, 1,1,0,1 ],
							'Z': [ 1,1,1, 0,1,1,0 ],
							'_': [ 0,0,1, 0,0,0,0 ],
							'*': [ 1,1,0, 1,1,0,0 ]
							)[ contents[i].asSymbol ];
						
						out ? item;
						}).reverse;
					
				array.do({ |gItem, i|
					var ghPos;
					ghPos = i * 5;
					
					gItem[[0,1,2]].do({ |item, ii|
						var vPos; 
						if( item != 0 )
						{	vPos = ii * 3;
							
							Pen.moveTo( ( (ghPos + 0.5)@( vPos + 0.5 ) ) * wd  );
							Pen.lineTo( ( (ghPos + 1)@vPos ) * wd );
							Pen.lineTo( ( (ghPos + 3)@vPos ) * wd );
							Pen.lineTo( ( (ghPos + 3.5)@(vPos + 0.5) ) * wd );
							Pen.lineTo( ( (ghPos + 3)@(vPos + 1) ) * wd );
							Pen.lineTo( ( (ghPos + 1)@(vPos + 1) ) * wd );
							Pen.lineTo( ( (ghPos + 0.5)@( vPos + 0.5 ) ) * wd  );
							
							Pen.fill;
						};
						
						});
					
					gItem[[3,4,5,6]].do({ |item, ii|
						var vPos, hPos;
						if( item != 0 )
						{ 	vPos = (ii / 2).floor * 3;
							hPos = (ii % 2) * 3;
							
							Pen.moveTo( ( (ghPos + hPos + 0.5)@( vPos + 0.5 ) ) * wd  );
							Pen.lineTo( ( (ghPos + hPos + 1)@( vPos + 1 ) ) * wd );
							Pen.lineTo( ( (ghPos + hPos + 1)@( vPos + 3 ) ) * wd );
							Pen.lineTo( ( (ghPos + hPos + 0.5)@(vPos + 3.5) ) * wd );
							Pen.lineTo( ( (ghPos + hPos)@(vPos + 3) ) * wd );
							Pen.lineTo( ( (ghPos + hPos)@(vPos + 1) ) * wd );
							Pen.lineTo( ( (ghPos + hPos + 0.5)@( vPos + 0.5 ) ) * wd  );
							 
							Pen.fill;
						};
						
						});
					
										
					});
				
				Pen.translate( hOffset.neg + square.left.neg, vOffset.neg + square.top.neg );
				
				},
				
				
			// combined
			
			textDocument: { |rect|
				var square, docRect, wd, rd;
				square = Rect.aboutPoint( rect.center, 
						rect.width.min( rect.height ) / 4, 
						rect.width.min( rect.height ) / 4 );
						
				wd = square.width / 16;
				
				rd = square.width / 4;
				
				docRect = square.insetBy( 
					(square.width / 2) - (( square.height / 2.sqrt ) / 2) ,0);
				
				drawFuncs[ \document ].value( rect );
				
				4.do({ |i|
					var yy;
					yy = docRect.top + rd + wd + ((i+1) * (wd * 2));
					Pen.line( 
						(docRect.left + (wd * 2))@yy,
						(docRect.right - (wd * 2))@yy  );
					});
				
				Pen.stroke;
								
				},
			
			warning:  { |rect| 
				drawFuncs[ \polygon ].value( rect, 8, 0.125pi );
				if( Pen.respondsTo( \color_ ) )
					{ Pen.color = Color.white; }
					{ Color.white.set; };
				Pen.width = 2;
				drawFuncs[ \polygon ].value( rect
					.insetBy( 4, 4 ), 8, 0.125pi, \stroke );
				drawFuncs[ '!' ].value(  rect
					.insetBy( 8, 8 ) );
				}
				
				);
	
		}
	}