// wslib 2006, Wouter Snoei

/// workaround for String_GetBounds failure
/// draws Strings with Font( "Monaco", 9 )

// partially obsolete but still here to prevent code breaking

+ String {
	
	*widthM9 { |n = 1|  ^(n * 6) } // width of n chars in Font( "Monaco", 9 )
	*heightM9 { |n = 1|  ^(n * 12) } // height of n lines in Font( "Monaco", 9 )
	
	boundsM9 { |origin| // no tabs supported!!
		// M9 stands for Font( "Monaco", 9 )
		/*
		var lines;
		origin = ( origin ?? { Point( 0,0 ) } ).asPoint;
		lines = this.split( $\n );
		^Rect( origin.x, origin.y,
			this.class.widthM9( lines.collect( _.size ).maxItem ), 
			this.class.heightM9( lines.size ) );
		*/
		
		origin = ( origin ?? { Point( 0,0 ) } ).asPoint;
		^this.bounds( Font( "Monaco", 9 ) ) + Rect( origin.x, origin.y, 0, 0 );
		}
		
	clipToRectM9 { |rect, clipSign = "", allowHeight = 4, allowWidth = 0|
		var lines, maxSizeH;
		lines = this.split( $\n );
		maxSizeH = ( ( rect.width + allowWidth ) / this.class.widthM9 );
		lines = lines.copyRange(0, 
			(( ( rect.height + allowHeight) / this.class.heightM9 ).floor.asInt - 1) );
		lines = lines.collect({ |line|
			if( line.size > maxSizeH  )
				{ line = line.copyRange(0, maxSizeH.asInt - (clipSign.size + 1) ) ++ clipSign }
				{ line };
			});
		^lines.join( $\n )
		}
		
	drawM9 { |color|
		this.drawAtPointM9(Point(0,0), color ? Color.black);
	}
	
	drawAtPointM9 { arg point, color;
		this.drawAtPoint( point ? Point(0,0), Font( "Monaco", 9 ), color ? Color.black);
	}
	
	drawInRectM9 { arg rect, color;
		this.drawInRect( rect ?? { this.boundsM9 }, Font( "Monaco", 9 ), color ? Color.black )
	}
		
	drawCenteredInM9 { arg inRect, color;
		this.drawAtPoint( this.boundsM9.centerIn(inRect), 
			Font( "Monaco", 9 ), color ? Color.black );
	}
	
	drawLeftJustInM9 { arg inRect, color;
		var pos, bounds;
		bounds = this.boundsM9;
		pos = bounds.centerIn(inRect);
		pos.x = inRect.left + 2;
		this.drawAtPoint(pos, Font( "Monaco", 9 ), color ? Color.black );
	}
	drawRightJustInM9 { arg inRect, color;
		var pos, bounds;
		bounds = this.boundsM9;
		pos = bounds.centerIn(inRect);
		pos.x = inRect.right - 2 - bounds.width;
		this.drawAtPoint(pos, Font( "Monaco", 9 ), color ? Color.black );
	}
	
	drawStretchedInM9 { arg inRect, color;
		var lines, bounds, centeredRect, spacingH, spacingV;
		lines = this.split( $\n );
		spacingH = inRect.width / lines.collect( _.size ).maxItem;
		spacingV = inRect.height / lines.size;
		bounds = this.boundsM9;
		centeredRect = bounds.centerIn(inRect);
		centeredRect = Rect( centeredRect.x, centeredRect.y, bounds.width, bounds.height );
 		lines.do({ |line, i|
 			line.do({ |char, ii|
 				char.asString
 					.drawAtPointM9( 
 						( ( ( (ii + 0.5) * spacingH) - this.class.widthM9(0.5)) + inRect.left)@
 						( ( ( (i + 0.5) * spacingV) - this.class.heightM9(0.5)) + inRect.top),
 						color
 						)
 				});
 			});
		}
	
	/*
	drawStretchedIn { arg inRect, font, color; 
		// can use different fonts, best to use equal spacing fonts
		var lines, bounds, centeredRect, spacingH, spacingV;
		var charWidth, charHeight;
		font = font ? Font( "Monaco", 9 );
		lines = this.split( $\n );
		spacingH = inRect.width / lines.collect( _.size ).maxItem;
		spacingV = inRect.height / lines.size;
		bounds = this.bounds( font );
		//bounds.width = bounds.width * font.size / 9;
		//bounds.height = bounds.height * font.size / 9;
		//charWidth = this.class.widthM9(0.5) * font.size / 9;
		//charHeight = this.class.heightM9(0.5) * font.size / 9;
		centeredRect = bounds.centerIn(inRect);
		centeredRect = Rect( centeredRect.x, centeredRect.y, bounds.width, bounds.height );
 		lines.do({ |line, i|
 			line.do({ |char, ii|
 				char.asString
 					.drawAtPoint( 
 						( ( ( (ii + 0.5) * spacingH) - charWidth ) + inRect.left)@
 						( ( ( (i + 0.5) * spacingV) - charHeight ) + inRect.top),
 						font, color ? Color.black )
 				});
 			});
		}
	*/
	
	drawStretchedIn { arg inRect, font, color; // equal spaced stretched text
		var rects, wd, size, subStrings;
		if( this.includes( $\n ) )
			{ subStrings = this.split( $\n );
			  wd = inRect.asRect.height / subStrings.size;
			  subStrings.do({ |string, i|
			  		string.drawStretchedIn( 
			  			Rect( inRect.left, inRect.top + (i * wd), inRect.width, wd ), 
			  			font, color );
			  		});
			}
			{ wd = inRect.asRect.width / this.size;
			Pen.font = font;
			Pen.color = color;
			this.do({ |char,i|
				Pen.stringCenteredIn( char.asString, 
					Rect( inRect.left + (i * wd), inRect.top, wd, inRect.height ));
				});
			}
		}
	

}