+ Pen {
	*transformToRect {|rect, fromRect, keepRatio = false, scale = 1, move = 0.5|
		var scaleAmt;
		rect = rect ?? {Rect(0,0,400,400)};
		fromRect = fromRect ? rect;
		rect = fromRect.scaleCenteredIn( rect, keepRatio, scale, move );
		scaleAmt = (rect.extent/fromRect.extent).asArray;
						
		Pen.translate( *(fromRect.origin.asArray.neg * scaleAmt) + rect.origin.asArray );
	
		Pen.scale( *scaleAmt );	
		
		/*
		if( GUI.scheme.id == 'swing' && {(scaleAmt[0] != scaleAmt[1])} )
						{ Pen.translate( 0.5, 0.5 ); }; 
				// temp correction for swingosc half-pixel bug
		*/
		
		^Pen;
		}
	}
	
+ Point {

	transformToRect {|rect, fromRect, keepRatio = false, scale = 1, move = 0.5|
		rect = rect ?? {Rect(0,0,400,400)};
		fromRect = fromRect ? rect;
		rect = fromRect.scaleCenteredIn( rect, keepRatio, scale, move );
		^((this - fromRect.origin) * ( rect.extent/fromRect.extent )) + rect.origin;
		}
		
	transformFromRect {|rect, fromRect, keepRatio = false, scale = 1, move = 0.5|
		rect = rect ?? {Rect(0,0,400,400)};
		fromRect = fromRect ? rect;
		rect = fromRect.scaleCenteredIn( rect, keepRatio, scale, move );
		^((this - rect.origin) * ( fromRect.extent/rect.extent )) + fromRect.origin;
		}
	}
	
+ Rect {
	scaleCenteredIn { |toRect, keepRatio = false, ratio = 1, move = 0.5|
		var xyr, rect, spacing;
		
		move = move.asPoint;
		ratio = ratio.asPoint;
		
		toRect = (toRect ? this).asRect;
		
		if( keepRatio )
			{	
			xyr = width/height;
			
			if( (toRect.width / toRect.height).abs < xyr.abs )
				{ rect = Rect(0,0, toRect.width * ratio.x , (toRect.width / xyr) * ratio.y ); }
				{ rect = Rect(0,0, toRect.height * xyr * ratio.x, toRect.height * ratio.y ); };
			}
			{ rect = Rect(0,0, toRect.width * ratio.x,  toRect.height * ratio.y) };
			
		spacing  = (toRect.extent - rect.extent) * move;
		rect.origin = toRect.origin - rect.origin + spacing;
		
		//rect.origin = rect.centerIn( toRect );
		^rect;
		}
		
	transformToRect {  |rect, fromRect, keepRatio = false, ratio = 1, move = 0.5|
		rect = rect ?? {Rect(0,0,400,400)};
		fromRect = fromRect ? this;
		^this.class.fromPoints( *[ this.leftTop, this.rightBottom ]
			.collect( _.transformToRect( rect, fromRect, keepRatio, ratio, move ) ) );
		}
	
	transformFromRect {  |rect, fromRect, keepRatio = false, ratio = 1, move = 0.5|
		rect = rect ?? {Rect(0,0,400,400)};
		fromRect = fromRect ? this;
		^this.class.fromPoints( *[ this.leftTop, this.rightBottom ]
			.collect( _.transformFromRect( rect, fromRect, keepRatio, ratio, move ) ) );
		}
	}
	
+ Collection {
		transformToRect {  |rect, fromRect, keepRatio = false, ratio = 1, move = 0.5|
				^this.collect( _.transformToRect( rect, fromRect, keepRatio, ratio, move ) );		}
				
		transformFromRect {  |rect, fromRect, keepRatio = false, ratio = 1, move = 0.5|
				^this.collect( _.transformFromRect( rect, fromRect, keepRatio, ratio, move ) );		}
	
}