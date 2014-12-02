// curve with 2 control points drawing
// wslib 2006

+ Pen {
	
	*addCurve { |start, c1, c2, end, div=10|
		c2 = c2 ? c1;
		Pen.lineTo( start );
		(div-1).do({ |i| var x,y;
			x = [start.x, end.x].splineIntFunction( (i+1) / div, c1.x, c2.x );
			y = [start.y, end.y].splineIntFunction( (i+1) / div, c1.y, c2.y );
			Pen.lineTo( x@y );
			});
		Pen.lineTo( end );
		}
		
	*addQuadCurve { |start, c1, end, div=10|
		//c2 = c2 ? c1;
		Pen.lineTo( start );
		(div-1).do({ |i| var x,y;
			x = [start.x, end.x].quadIntFunction( (i+1) / div, c1.x );
			y = [start.y, end.y].quadIntFunction( (i+1) / div, c1.y );
			Pen.lineTo( x@y );
			});
		Pen.lineTo( end );
		}
		
	/*
	// workaround for missing curveTo and quadCurveTo
	// obsolete now (thank you TheLych!
	*curveTo { |end, c1, c2, start, div=10|	
		^this.addCurve( start, c1, c2, end, div );
	  }
	  
	*quadCurveTo { |end, cp, start, div=10|	
		^this.addQuadCurve( start, cp, end, div );
	  }
	 */
	 
	*drawSpline { |start, c1, c2, end, strokeColor, controlColor, div=10|
		// draw spline and controls; GUI class compatible
		controlColor = controlColor ? Color.red;
		strokeColor = strokeColor ? Color.black;
		
		GUI.pen.color = controlColor;
		
		GUI.pen.moveTo( start ).lineTo( c1 ).stroke;
		GUI.pen.addArc(c1,2,0,2pi).fill;
		
		GUI.pen.moveTo( end ).lineTo( c2 ).stroke;
		GUI.pen.addArc(c2,2,0,2pi).fill;
		
		GUI.pen.color = strokeColor;
		GUI.pen.moveTo( start ).curveTo( end, c1, c2, start, div ).stroke;
		}
	 
	
	}
	
+ Point {

	mirrorTo { |aPoint = 0|
		aPoint = aPoint.asPoint;
		^(aPoint * 2) - this;
		}
	
	}