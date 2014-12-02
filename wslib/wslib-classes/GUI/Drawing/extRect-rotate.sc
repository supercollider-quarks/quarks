+ Rect {

	corners { 
		var l, r, t, b;
		l = this.left; r = this.right; t = this.top; b = this.bottom;
		^[ l@t, r@t, r@b, l@b ];
		 }

	rotatedCorners { |amt = 0, x, y|
	
		var center, pts;
		center = this.center;
		
		x = x ? center.x;
		y = y ? center.y;
		center = x@y;
		
		pts = this.corners - [ center ];
		^pts.collect( _.rotate( amt ) ) + [ center ];
	
		}
		
	rotate { |amt, x, y|
		var center, pts;
		center = this.center;
		x = x ? center.x;
		y = y ? center.y;
		center = x@y;
		
		pts = [ this.leftTop, this.rightBottom ] - [ center ];
		
		pts = pts.collect( _.rotate( amt ) ) + [ center ];
		
		^Rect.fromPoints( *pts );
		}
	}