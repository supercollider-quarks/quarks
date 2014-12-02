// wslib 2006

// 1st step to SwingOsc compat :: 03/2008
// 	- no rounded rects yet: all 

+ Pen {

	*roundedRect { |rect, radius| // radius can be array for 4 corners
		var points, lastPoint;
		
		radius = radius ?? {  rect.width.min( rect.height ) / 2; };
			
		if( radius != 0 )
			{	
				radius = radius.asCollection.collect({ |item| 
					item ?? {  rect.width.min( rect.height ) / 2; }; });
				
				// auto scale radius if too large
				if ( radius.size == 1 )
					{ radius = min( radius, min( rect.width, rect.height ) / 2 ) }
					{ if( ((radius@@0) + (radius@@3)) > rect.height )
						{ radius = radius * ( rect.height / ((radius@@0) + (radius@@3))); };
					 if( ((radius@@1) + (radius@@2)) > rect.height )
						{ radius = radius * ( rect.height / ((radius@@1) + (radius@@2))); };
					 if( ((radius@@0) + (radius@@1)) > rect.width )
						{ radius = radius * ( rect.width / ((radius@@0) + (radius@@1))); };
					 if( ((radius@@2) + (radius@@3)) > rect.width )
						{ radius = radius * ( rect.width / ((radius@@2) + (radius@@3))); };
					};
					
					
				case { GUI.pen.respondsTo( \arcTo ) }						// after rev7947 (redfrik added Meta_Pen:arcTo)
				 	// also for swingosc now

					{
					points = [rect.rightTop, rect.rightBottom,rect.leftBottom, rect.leftTop];
					lastPoint = points.last;
					
					Pen.moveTo( points[2] - (0@radius.last) );
					
					points.do({ |point,i|
						Pen.arcTo( lastPoint, point, radius@@i );
						lastPoint = point;
						});
						
					^Pen; // allow follow-up methods
					}
					
					{ GUI.scheme.id === \swing } // swingosc; cannot draw a good rounded rect
					{ ^Pen.addRect( rect ); } 
					
					{ true } // before rev7947 (old)
					{
					Pen.moveTo( rect.leftTop + ((radius@@0)@0) );
					Pen.lineTo( rect.rightTop - ((radius@@1)@0) );
					Pen.addArc( rect.rightTop - ((radius@@1)@((radius@@1).neg)), 
						(radius@@1), 1.5pi, 0.5pi );
					Pen.lineTo( rect.rightBottom - (0@(radius@@2)) );
					Pen.addArc( rect.rightBottom - ((radius@@2)@(radius@@2)), 
						(radius@@2), 0, 0.5pi );
					Pen.lineTo( rect.leftBottom + ((radius@@3)@0) );
					Pen.addArc( rect.leftBottom + ((radius@@3)@((radius@@3).neg)), 
						(radius@@3), 0.5pi, 0.5pi );
					Pen.lineTo( rect.leftTop + (0@(radius@@0)) );
					^Pen.addArc( rect.leftTop + ((radius@@0)@(radius@@0)), 
						(radius@@0), pi, 0.5pi );
					};
			}
			{	^Pen.addRect( rect ); }
					
		}
		
	*extrudedRect {	 |rect, radius, width = 2, angle, inverse = false, colors|
	
		//var centers;
		var points, cornerFunc, sidesFunc, rpt1, rpt2, width2;
		
		angle = angle ? 0.17pi;	
		
		/*
		if(  GUI.scheme.id === \swing ) // should be swing compatible now
			{ radius = 0; }  // no rounded rects in SwingOSC yet (TODO)
		*/
	
		radius = radius ?? {  rect.width.min( rect.height ) / 2; };
		radius = radius.asCollection;
		radius = radius.collect({ |item|
			item ?? {  rect.width.min( rect.height ) / 2; }; 
			});
			
		// auto scale radius if too large
				if ( radius.size == 1 )
					{ radius = min( radius, min( rect.width, rect.height ) / 2 ) }
					{ if( ((radius@@0) + (radius@@3)) > rect.height )
						{ radius = radius * ( rect.height / ((radius@@0) + (radius@@3))); };
					 if( ((radius@@1) + (radius@@2)) > rect.height )
						{ radius = radius * ( rect.height / ((radius@@1) + (radius@@2))); };
					 if( ((radius@@0) + (radius@@1)) > rect.width )
						{ radius = radius * ( rect.width / ((radius@@0) + (radius@@1))); };
					 if( ((radius@@2) + (radius@@3)) > rect.width )
						{ radius = radius * ( rect.width / ((radius@@2) + (radius@@3))); };
					};
					
		
		//angle = angle.asCollection; 
		
		colors = colors ? [ Color.white.alpha_(0.5), Color.black.alpha_(0.5) ];
		if( inverse ) { colors = colors.reverse };
		
		cornerFunc = { |points, start = (-1), angle = (0.17pi)| 
			var lastPoint;
			lastPoint =  points.rotate(start.neg).last;
			points.rotate(start.neg)[..2].do({ |point,ii|
				var rd, sta, rectPts, i; // radius, startAngle, rctPoints
				i = ii+start;
				rd = radius@@i;
				sta = i*0.5pi;
				
				case { rd != 0 }
					{ Pen.addAnnularWedge( point + ((rd@rd).rotate(sta)),
					 rd - width,  rd,
					  sta - [ 0.5pi+angle, 0.5pi, 0.5pi+angle][ii], 
					  	[angle, -0.5pi, angle-0.5pi][ii]); };
				
				lastPoint = point;
				});
			};
		
		sidesFunc = { |points, start = 0| 
			var lastPoint;
			lastPoint = points.rotate(start.neg)[3];	
			points.rotate(start.neg)[..1].do({ |point,i|
				var rd, sta, rectPts, prevRdNeg; // radius, startAngle, rctPoints
				i = i + start;
				rd = radius@@i;
				prevRdNeg = (radius@@(i-1)).neg;
				sta = i*0.5pi;
	
				rectPts = [lastPoint, point].stutter(2) +
				 	[ 0@prevRdNeg, width@prevRdNeg.min(width.neg),  
				 	  width@rd.max(width), 0@rd ].collect( _.rotate(sta) );
				
				Pen.moveTo( rectPts.last );
				rectPts.do( Pen.lineTo(_) );
				
				lastPoint = point;
				});
			
			};
			
	points = [  rect.leftTop, rect.rightTop, rect.rightBottom, rect.leftBottom ];
			
	// light side (top)
	
	//Pen.fillColor = colors[0];
	cornerFunc.( points, -1, 0 );	
	if( rect.width > rect.height ) 
		{ sidesFunc.( points, 0 ); }
		{ sidesFunc.( points, 2 ); };
	//Pen.fill;
	
	
	Pen.fillAxialGradient( 
		(rect.right - (radius@@1).max( width ))@(rect.top + width.min(radius@@1)),
		(rect.right - width.min(radius@@1))@(rect.top + (radius@@1).max( width )),
		colors[0], colors[1] ); 
	
	
	// dark side
	
	//Pen.fillColor = colors[1];
	
	cornerFunc.( points, -3, 0 );
	if( rect.width > rect.height ) 
		{ sidesFunc.( points, 2 ); }
		{ sidesFunc.( points, 0 ); };
	//Pen.fill;
	
	Pen.fillAxialGradient( 
		(rect.left + (radius@@3).max(width) )@((rect.bottom - width.min(radius@@3))),
		(rect.left + width.min(radius@@3))@(rect.bottom - (radius@@3).max(width)),
		 colors[1], colors[0] ); 
	

		
		/* // OLD VERSION (not rev7947 compatible)
		centers = [
			 (radius@@0)@(radius@@0),
			 ((radius@@1).neg)@(radius@@1),
			 ((radius@@2).neg)@((radius@@2).neg),
			 (radius@@3)@((radius@@3).neg)
			 ];
			
		centers = centers + [ rect.leftTop, rect.rightTop, rect.rightBottom, rect.leftBottom ];
		
		

		// light side
		/* if( radius@@3 != 0 )
		 	{ Pen.moveTo( centers[3] + Polar( (radius@@3) - width,  pi - (angle@@1) ).asPoint );
		 	  Pen.lineTo( centers[3] + Polar( (radius@@3),  pi - (angle@@1) ).asPoint );
		 	  Pen.addArc( centers[3], radius@@3, pi - (angle@@1), angle@@1 ); }
		 	{ Pen.moveTo( centers[3] + ((width)@(width.neg)));
		 	  Pen.lineTo( rect.leftBottom ); };
		*/		
		// OLD VERSION
		// light side
		
		 if( radius@@3 != 0 )
		 	{ Pen.moveTo( centers[3] + Polar( (radius@@3) - width,  pi - (angle@@1) ).asPoint );
		 	  Pen.lineTo( centers[3] + Polar( (radius@@3),  pi - (angle@@1) ).asPoint );
		 	  Pen.addArc( centers[3], radius@@3, pi - (angle@@1), angle@@1 ); }
		 	{ Pen.moveTo( centers[3] + ((width)@(width.neg)));
		 	  Pen.lineTo( rect.leftBottom ); };
		 
		 if( radius@@0 != 0 )
		 	{ Pen.lineTo( rect.leftTop + (0@(radius@@0)) );
		 	  Pen.addArc( centers[0], radius@@0, pi, 0.5pi ); }
		 	{ Pen.lineTo( centers[0] ) }; 
		 
		 if( radius@@1 != 0 )
		 	{	Pen.lineTo( rect.rightTop - ((radius@@1)@0) );
		 		Pen.addArc( centers[1], radius@@1, 1.5pi,  0.5pi-(angle@@0) ); 
		 		Pen.lineTo( centers[1] + Polar( (radius@@1) - width, (angle@@0).neg ).asPoint );
		 		Pen.addArc( centers[1], (radius@@1) - width, (angle@@0).neg, 
		 			(0.5pi-(angle@@0)).neg ); }
		 	{   Pen.lineTo( centers[1] ); 
		 		Pen.lineTo( centers[1] + ((width.neg)@(width)) ); };
		 
		 if( radius@@0 != 0 )
		 	{ Pen.lineTo( rect.leftTop + ((radius@@0)@(width)) );
		 	  Pen.addArc( centers[0], (radius@@0) - width, 1.5pi, -0.5pi ); }
		 	{ Pen.lineTo( centers[0] + ((width)@(width)) ) };
		 	
		 if( radius@@3 != 0 )
		 	{ Pen.lineTo( rect.leftBottom - ((width.neg)@(radius@@3)) );
		 	  Pen.addArc( centers[3], (radius@@3) - width, pi, (angle@@1).neg ); }
		 	{ Pen.lineTo( centers[3] + ((width)@(width.neg)) ) };
		 
		 Pen.fill; 
		 
		// dark side
		 if( inverse )  { Pen.color = colors[0]; } { Pen.color = colors[1]; };
		
		 if( radius@@1 != 0 )
		 	{ Pen.moveTo( centers[1] + Polar( (radius@@1) - width, (angle@@0).neg ).asPoint );
		 	  Pen.addArc( centers[1], radius@@1, (angle@@0).neg, (angle@@0) ); }
		 	{ Pen.moveTo( centers[1] + ((width)@(width.neg)) ); Pen.lineTo( centers[1] )  };
		 
		 if( radius@@2 != 0 )
		 	{ Pen.addArc( centers[2], radius@@2, 0, 0.5pi ); }
		 	{ Pen.lineTo( centers[2] ); };
		 
		 if( radius@@3 != 0 )
			{ Pen.addArc( centers[3], radius@@3, 0.5pi,  0.5pi-(angle@@1) ); 
		  	  Pen.addArc( centers[3], (radius@@3) - width, pi - (angle@@1), 
		  	  	(0.5pi-(angle@@1)).neg );  }
		  	{ Pen.lineTo( rect.leftBottom );
		  	  Pen.lineTo( centers[3] + ((width)@(width.neg))); };
		  	
		 if( radius@@2 != 0 )
		 	{ Pen.addArc( centers[2], (radius@@2) - width, 0.5pi, -0.5pi ); }
		 	{ Pen.lineTo( centers[2] + ((width.neg)@(width.neg)) ) };
		 
		 if( radius@@1 != 0 )
		 	{ Pen.addArc( centers[1], (radius@@1) - width, 0, (angle@@0).neg ); }
		 	{ Pen.lineTo( centers[1] + ((width.neg)@(width)) ) }; 
		 
		 Pen.fill;
		 */
		 
	}
		
	*circle { |rect|
		var radius;
		radius = rect.width.min(rect.height) * 0.5;
		Pen.addArc( rect.center, radius, 0, 2pi );
		}
		
	*extrudedCircle { |rect, width = 2, angle, inverse = false, colors|
	
		var center, radius;
		
		angle = angle ? 0.17pi;	
		radius = rect.width.min(rect.height) * 0.5;
		
		colors = colors ? [ Color.white.alpha_(0.5), Color.black.alpha_(0.5) ];
		
		center = rect.center;
			
		// light side
		 if( inverse ) { Pen.color = colors[1] } { Pen.color = colors[0] };
		 
		 Pen.addAnnularWedge( center, radius - width, radius, pi - angle, pi );
		
		 Pen.fill; 
		 
		// dark side
		 if( inverse ) { Pen.color = colors[0] } { Pen.color = colors[1] };
		 
		 Pen.addAnnularWedge( center, radius - width, radius, angle.neg, pi );
		
		 Pen.fill; 
	}

	

	}
	
/*
+ SCPen {
	*roundedRect { |rect, radius| // radius can be array for 4 corners
		^Pen.roundedRect( rect, radius );					}
		
	*extrudedRect { |rect, radius, width = 2, angle, inverse = false, colors|
		^Pen.extrudedRect( rect, radius, width, angle, inverse, colors );
		}}
*/

/*
+ GUIPen {

	*roundedRect { |rect, radius| // radius can be array for 4 corners
		^Pen.roundedRect( rect, radius );					}
		
	*extrudedRect { |rect, radius, width = 2, angle, inverse = false, colors|
		^Pen.extrudedRect( rect, radius, width, angle, inverse, colors );
		}
		
	*circle { |rect|
		^Pen.circle( rect );
		}
		
	*extrudedCircle { |rect, width = 2, angle, inverse = false, colors|
		^Pen.extrudedCircle( rect, width, angle, inverse, colors );
		}
	

	
}
*/
