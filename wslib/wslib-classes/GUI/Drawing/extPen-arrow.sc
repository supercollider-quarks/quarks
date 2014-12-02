+ Pen {

	// draw an arrow
	
	*arrow { arg start, end, width = 5, arrowAngle = 0.5pi;
		var angle, leftPoint, rightPoint;
		angle = (start - end).theta;
		leftPoint = Polar( width, angle - ( arrowAngle/2 ) ).asPoint + end;
		rightPoint = Polar( width, angle + ( arrowAngle/2 ) ).asPoint + end;
		GUI.pen.moveTo( start );
		GUI.pen.lineTo( end );
		GUI.pen.moveTo( leftPoint );
		GUI.pen.lineTo( end );
		GUI.pen.lineTo( rightPoint );
		}
	
	*arrow_round { arg start, end, width = 5, arrowAngle = 0.5pi;
		var angle;
		angle = (start - end).theta;
		GUI.pen.moveTo( start );
		GUI.pen.lineTo( end );
		GUI.pen.addWedge( end, width, angle - ( arrowAngle/2 ), arrowAngle )
		}
		
	// draw a cross
		
	*cross {  |center, radius = 5, shape = '+' |  // shape: '+', 'x' or '*'
		var points, x, y;
		var tsqRadius;
		#x, y = center.asPoint.asArray;
		case	{ shape === '+' }
			{ points = [ 
				(x-radius)@y, (x+radius)@y, 
				x@(y-radius), x@(y+radius) ]; }
			{ shape === 'x' }
			{ tsqRadius = radius / 2.sqrt;
			  points = [ 
				(x-tsqRadius)@(y-tsqRadius),
				(x+tsqRadius)@(y+tsqRadius),
				(x-tsqRadius)@(y+tsqRadius),
				(x+tsqRadius)@(y-tsqRadius) ]; }
			{ shape === '*' }
			{ tsqRadius = radius / 2.sqrt;
			  points = [ 
				(x-radius)@y, (x+radius)@y, 
				x@(y-radius), x@(y+radius),  
				(x-tsqRadius)@(y-tsqRadius),
				(x+tsqRadius)@(y+tsqRadius),
				(x-tsqRadius)@(y+tsqRadius),
				(x+tsqRadius)@(y-tsqRadius) ]; };
		
		^points.pairsDo({ |a,b|
			GUI.pen.moveTo( a );
			GUI.pen.lineTo( b ); });		
		}

		
	}
	
	
	