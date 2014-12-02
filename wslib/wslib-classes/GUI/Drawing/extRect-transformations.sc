+ Rect {
	
	max { |aRect|
		aRect = aRect.asRect;
		#left, top, width, height = this.asArray.max( aRect.asArray );
		}
		
	min { |aRect|
		aRect = aRect.asRect;
		#left, top, width, height = this.asArray.min( aRect.asArray );
		}
	
	translate { |deltaPoint|
		deltaPoint = deltaPoint.asPoint;
		^this.class.new(
			*( this.leftTop.translate( deltaPoint ).asArray
				++ [ width, height] ) );
			 }
			 
	scale { |scalePoint|
		scalePoint = scalePoint.asPoint;
		^this.class.fromPoints(
			this.leftTop.scale( scalePoint ),
			this.rightBottom.scale( scalePoint ) );
		}	
	
	translateScale { |deltaPoint, scalePoint|
		deltaPoint = deltaPoint.asPoint;
		scalePoint = scalePoint.asPoint;
		^this.class.fromPoints(
			this.leftTop.translate( deltaPoint ).scale( scalePoint ),
			this.rightBottom.translate( deltaPoint ).scale( scalePoint ) );
		}	
	
	scaleTranslate { |scalePoint, deltaPoint|
		deltaPoint = deltaPoint.asPoint;
		scalePoint = scalePoint.asPoint;
		^this.class.fromPoints(
			this.leftTop.scale( scalePoint ).translate( deltaPoint ),
			this.rightBottom.scale( scalePoint ).translate( deltaPoint ) );
		}
		
	*rand { |x = 1.0, y = 1.0, w = 1.0, h = 1.0|
		^this.new( x.rand, y.rand, w.rand, h.rand );
		}
		
	*rand2 {  |x = 1.0, y = 1.0, w = 1.0, h = 1.0|
		^this.new( x.rand2, y.rand2, w.rand, h.rand );
		}
	
	}
	
+ Point {

	translateScale { |deltaPoint, scalePoint|
		deltaPoint = deltaPoint.asPoint;
		scalePoint = scalePoint.asPoint;
		^this.translate( deltaPoint ).scale( scalePoint );		}	
	
	scaleTranslate { |scalePoint, deltaPoint|
		deltaPoint = deltaPoint.asPoint;
		scalePoint = scalePoint.asPoint;
		^this.scale( scalePoint ).translate( deltaPoint );
		}
		
	*rand { | x = 1.0, y = 1.0 | ^this.new( x.rand, y.rand ); }
	
	*rand2 { |x = 1.0, y = 1.0| ^this.new( x.rand2, y.rand2 ); }
	
	}
	