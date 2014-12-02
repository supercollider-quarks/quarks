+ Env {

	duration { ^times.sum; }
	
	minLevel { ^levels.minItem; }
	maxLevel { ^levels.maxItem; }
	
	timeLine { ^[0] ++ times.integrate; }
		

	asSVGPolyLine { |width = 400, height = 200,
			name, strokeColor, fillColor, strokeWidth, transform|
		var points, lastTime = 0;
		points = [this.timeLine, levels.copy];
		
		points[0] = (points[0] / this.duration) * width;
		points[1] = points[1].normalize(height,0); // upside down		
		^SVGPolyLine( points.flop, name, strokeColor, fillColor, strokeWidth, transform );
		}
		
	asSVGGroup { |width = 400, height = 200, name = "group", scale = 1|
		var polyline, circles, startLine, minMaxArray, middleLineY = 0, middleLine;
		var labels, transform;
		var group;
		
		polyline = this.asSVGPolyLine( width, height );
		circles = SVGGroup( polyline.points.collect({ |point, i|
			SVGCircle( point.x, point.y, height / 80, 
				"point" + i ++ ":" + levels[i].asString, 
				"none", "red"  );
			}), "circles" );
			
		startLine = SVGLine.fromPoints([ 0@0, 0@height ], "y axis", "gray", "none", 1 );
		
		minMaxArray = [levels.minItem, levels.maxItem];
		
		if( (minMaxArray[0] <= 0) and: { minMaxArray[1] >= 0 } )
			{ middleLineY = (1 - minMaxArray.add(0).normalize(0,1)[2]) * height; };
			
		middleLine = SVGLine.fromPoints([ 0@middleLineY, width@middleLineY ], "x axis", "gray", "none", 1 );
				
		labels = SVGGroup( [ 
			SVGText( levels.maxItem.round(0.01).asString, -1, 0).anchor_( \end ),
			SVGText( "0", -1, middleLineY).anchor_( \end ),
			SVGText( levels.minItem.round(0.01).asString, -1, height).anchor_( \end ),
			SVGText( times.sum.round(0.01).asString, width, height).anchor_( \start )
			], "labels" );
		
		group = [ startLine, polyline, circles, labels];
		if( 	middleLine.notNil ) { group = group.addFirst( middleLine ); };
		
		if( scale != 1 ) { transform = SVGTransform( "scale(" ++ scale + scale ++") translate( "
				++ (width * ((1-scale)/2) ) ++ ", " ++ (height * ((1-scale)/2)) ++" )" ) };
		
		^SVGGroup( group, name, transform );
		}
		
	asSVGFile { |path = "~/Desktop/Env.svg" width = 400, height = 200, scale = 1|
		^SVGFile( path, [ this.asSVGGroup(width, height, scale: scale) ], width, height); 
		}
		
	*fromSVGPolyLine { |polyline, min = -1, max = 1, duration = 1|
		var points, lastTime;
		points = polyline.points.collect({ |point| [ point.x, point.y] }).flop;
		
		points[0] = (points[0] / points[0].last) * duration;
		lastTime = points[0][0];
		points[0] = points[0][1..].collect({ |absTime|
			var out;
			out = absTime - lastTime;
			lastTime = absTime;
			out;
			});
		
		points[1] = points[1].normalize(max, min); // downside up
		
		^Env( points[1], points[0] );
		}
	
	}
	
	
