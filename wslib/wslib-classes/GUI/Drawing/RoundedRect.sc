RoundedRect : Rect {
	var <radius;
	
	*new { arg left=0, top=0, width=0, height=0, radius;
		radius = radius ?? { width.min( height ) / 2; };
		^this.newCopyArgs( left, top, width, height, radius );
		}
		
	*fromRect { |inRect, radius|
		inRect = inRect.asRect;
		^this.new( inRect.left, inRect.top, inRect.width, inRect.height, radius );
		}
		
	asRect {
		^Rect( this.left, this.top, this.width, this.height );
		}
		
	asPenFunction { ^{ Pen.roundedRect( this.asRect, radius ); } } 
		
	stroke { |penClass|
		penClass = penClass ?? { GUI.pen };
	 	this.asPenFunction( penClass ).value;
	 	penClass.stroke; 
	 	}
	 	
	 fill { |penClass|
		penClass = penClass ?? { GUI.pen };
	 	this.asPenFunction( penClass ).value;
	 	penClass.fill; 
	 	}
	
	clip { |penClass|
		penClass = penClass ?? { GUI.pen };
	 	this.asPenFunction( penClass ).value;
	 	penClass.fill; 
	 	}

	drawExtruded { |penClass, border = 2, angle, inverse = false, colors|
		^Pen.extrudedRect( radius, border, angle, inverse, colors ); 
		}
	
	}
	
	


/* // old asPenFunction
		|penClass|
	
		// ** plots badly in SwingOSC !!
		var rc, maxR;
		penClass = penClass ?? { GUI.pen };
		maxR =  width.min( height ) / 2; 
		if( radius != 0 )
		{	rc = radius.asCollection.collect({ |item| 
				( item ? maxR ).min( maxR ) });
			^{
			penClass.moveTo( this.leftTop + ((rc@@0)@0) );
			
			//penClass.lineTo( this.rightTop - ((rc@@1)@0) ); // not needed
			
			penClass.addArc( this.rightTop - ((rc@@1)@((rc@@1).neg)), (rc@@1), -0.5pi, 0.5pi );
			
			//penClass.lineTo( this.rightBottom - (0@(rc@@2)), (rc@@2) );
			
			penClass.addArc( this.rightBottom - ((rc@@2)@(rc@@2)), (rc@@2),  0, 0.5pi );
			
			//penClass.lineTo( this.leftBottom + ((rc@@3)@(0)), (rc@@3),  0.5pi, 0.5pi );
			
			penClass.addArc( this.leftBottom + ((rc@@3)@((rc@@3).neg)), (rc@@3),  0.5pi, 0.5pi );
			
			//penClass.lineTo( this.leftTop + (0@(rc@@0)) );
			
			penClass.addArc( this.leftTop + ((rc@@0)@(rc@@0)), (rc@@0),  pi, 0.5pi );
			}
		}
		{^{ penClass.addRect( this ); } }
		*/
		
/* // old drawExtruded
		var centers, rc;
		
		penClass = penClass ?? { GUI.pen };
		
		angle = angle ? 0.17pi;	
		//radius = radius ? (rect.width.min(rect.height) * 0.5);
		
		rc = radius.asCollection;
		angle = angle.asCollection;
		
		colors = colors ? [ Color.white.alpha_(0.5), Color.black.alpha_(0.5) ];
		
		centers = [
			 (rc@@0)@(rc@@0),
			 ((rc@@1).neg)@(rc@@1),
			 ((rc@@2).neg)@((rc@@2).neg),
			 (rc@@3)@((rc@@3).neg)
			 ];
			
		centers = centers + [ this.leftTop, this.rightTop, this.rightBottom, this.leftBottom ];
			
			
		// light side
		 if( inverse ) { colors[1].set } { colors[0].set };
		
		 if( rc@@3 != 0 )
		 	{ penClass.moveTo( centers[3] + Polar( (rc@@3) - border,  pi - (angle@@1) ).asPoint );
		 	  penClass.addArc( centers[3], rc@@3, pi - (angle@@1), angle@@1 ); }
		 	{ penClass.moveTo( centers[3] + ((border)@(border.neg)));
		 		penClass.lineTo( this.leftBottom ); };
		 
		 penClass.addArc( centers[0], rc@@0, pi, 0.5pi ); 
		 
		 if( rc@@1 != 0 )
		 	{	penClass.addArc( centers[1], rc@@1, 1.5pi,  0.5pi-(angle@@0) ); 
		 		penClass.addArc( centers[1], (rc@@1) - border, (angle@@0).neg, 
		 			(0.5pi-(angle@@0)).neg ); }
		 	{   penClass.lineTo( centers[1] ); 
		 		penClass.lineTo( centers[1] + ((border.neg)@(border)) ); };
		 
		 penClass.addArc( centers[0], (rc@@0) - border, 1.5pi, -0.5pi );
		 
		 if( rc@@3 != 0 )
		 	{ penClass.addArc( centers[3], (rc@@3) - border, pi, (angle@@1).neg ); }
		 	{ penClass.lineTo( centers[3] + ((border)@(border.neg)) ) };
		 
		 penClass.fill; 
		 
		// dark side
		 if( inverse ) { colors[0].set } { colors[1].set };
		
		 if( rc@@1 != 0 )
		 	{ penClass.moveTo( centers[1] + Polar( (rc@@1) - border, (angle@@0).neg ).asPoint );
		 	  penClass.addArc( centers[1], rc@@1, (angle@@0).neg, (angle@@0) ); }
		 	{ penClass.moveTo( centers[1] + ((border)@(border.neg)) ); 
		 	  penClass.lineTo( centers[1] )  };
		 
		 penClass.addArc( centers[2], rc@@2, 0, 0.5pi ); 
		 
		 if( rc@@3 != 0 )
			{ penClass.addArc( centers[3], rc@@3, 0.5pi,  0.5pi-(angle@@1) ); 
		  	  penClass.addArc( centers[3], (rc@@3) - border, pi - (angle@@1), 
		  	  	(0.5pi-(angle@@1)).neg );  }
		  	{ penClass.lineTo( this.leftBottom );
		  	  penClass.lineTo( centers[3] + ((border)@(border.neg))); };
		  	
		 penClass.addArc( centers[2], (rc@@2) - border, 0.5pi, -0.5pi );
		 
		 if( rc@@1 != 0 )
		 	{ penClass.addArc( centers[1], (rc@@1) - border, 0, (angle@@0).neg ); }
		 	{ penClass.lineTo( centers[1] + ((border.neg)@(border)) ) }; 
		 
		 penClass.fill; 
		 
		 */
