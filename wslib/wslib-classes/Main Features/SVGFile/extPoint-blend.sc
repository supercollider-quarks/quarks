// wslib 2007

+ Point {
	blend { |that, blendfrac = 0.5|
		that = that.asPoint;
		^Point( x.blend( that.x, blendfrac ), y.blend( that.y, blendfrac ) );
		}
	}
	
+ Rect {
	blend { |that, blendfrac = 0.5|
		that = that.asRect;
		^Rect(
			left.blend( that.left, blendfrac ),
			top.blend( that.top, blendfrac ),
			width.blend( that.width, blendfrac ),
			height.blend( that.height, blendfrac ) 
		);			
	}
}