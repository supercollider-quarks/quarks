AxialGradient {
	var <>startPoint, <>endPoint, <>color0, <>color1;
	var <>rect;
	
	*new { arg startPoint, endPoint, color0, color1;
		^super.newCopyArgs( startPoint, endPoint, color0, color1 );
		}
	
	fill { ^Pen.fillAxialGradient( startPoint, endPoint, color0, color1 ); }
	
	penFill { |rect, alpha = 1, fromRect| //add operation later?
		var tempArgs;
		rect = rect ?? { Rect(0,0,400,400) };
		fromRect = fromRect ?? { this.asRect }; // auto-scale
		alpha = alpha ? 1;
		if( rect == fromRect )
			{^Pen.fillAxialGradient( startPoint, endPoint,
				color0.copy.alpha_( color0.alpha * alpha ), 
				color1.copy.alpha_( color1.alpha * alpha ) ); 
			} {	
			^Pen.fillAxialGradient(	
				startPoint.transformToRect( rect, fromRect ), 
				endPoint.transformToRect( rect, fromRect ),
				color0.copy.alpha_( color0.alpha * alpha ), 
				color1.copy.alpha_( color1.alpha * alpha )
				);
			}
		}
		
	asRect { |dev = 0.01|
		var rct;
		^rect ?? {
		rct = Rect.fromPoints( startPoint, endPoint );
		if( rct.width == 0 )
			{ rct.width = dev; };
		if( rct.height == 0 )
			{ rct.height = dev; };
		rct; };
		}
	
	at { |index| // 0-1
		^blend(color0, color1, index);
		}
		
	atPoint { |point|
		
		// TODO ..
		
		}
	}
	
RadialGradient {
	var <>innerCircleCenter, <>outerCircleCenter, <>startRadius, <>endRadius, <>color0, <>color1;
	var <>rect;

	*new { arg innerCircleCenter, outerCircleCenter, startRadius, endRadius, color0, color1;
		^super.newCopyArgs( innerCircleCenter, outerCircleCenter, startRadius, endRadius, 
				color0, color1 );
		}
	
	fill { ^Pen.fillRadialGradient(
		innerCircleCenter, outerCircleCenter, startRadius, endRadius, color0, color1 ); }
	
	penFill { |rect, alpha = 1, fromRect| //add operation later?
		var tempArgs;
		rect = rect ?? { Rect(0,0,400,400) };
		fromRect = fromRect ?? { this.asRect }; // auto-scale
		alpha = alpha ? 1;
		if( rect == fromRect )
			{^Pen.fillRadialGradient(
		innerCircleCenter, outerCircleCenter, startRadius, endRadius, 
			color0.copy.alpha_( color0.alpha * alpha ), 
			color1.copy.alpha_( color1.alpha * alpha ) ); 
			} {	
			^Pen.use({	
				Pen.clip;
				Pen.alpha_( alpha );
				Pen.use({
					Pen.transformToRect( rect, fromRect );
					Pen.fillRadialGradient(
			innerCircleCenter, outerCircleCenter, startRadius, endRadius, color0, color1 ); 
						});
					});
				}
		}
		
	asRect { ^rect ?? {	
			Rect.aboutPoint( innerCircleCenter, startRadius, startRadius )
				.union( Rect.aboutPoint( outerCircleCenter, endRadius, endRadius ) );
			}; 
		}
	
	at { |index| // 0-1
		^blend(color0, color1, index);
		}
		
	atPoint { |point|
		
		// TODO ..
		
		}
	}