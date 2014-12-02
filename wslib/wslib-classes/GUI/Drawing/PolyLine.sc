PolyLine {
	var <>points;
	
	*new {  | ... points|
		points = points.collect( _.asPoint );
		^super.newCopyArgs( points );
		}
	
	size { ^points.size }
	
	add { |item| ^this.class.new( *(points.add( item.asPoint ) ) ); }
	addAll { |... items| ^this.class.new( *(points ++ items.collect( _.asPoint ) ) ); }
	
	at { |index| ^points[ index ] }
	copyRange { |start, end| ^this.class.new( *points.copyRange( start, end ) ) }
	
	drawFunction {
		^{ 	Pen.moveTo( points[0] );
			points[1..].do({ |point| Pen.lineTo(point) });  }
		}
	
	drawFunctionJ {
		^{ 	JPen.moveTo( points[0] );
			points[1..].do({ |point| JPen.lineTo(point) });  }
		}
		
	stroke {  this.drawFunction.value; ^Pen.stroke }
	fill {  this.drawFunction.value; ^Pen.fill }
	
	}
	
PolySpline {
	var <>points;
	var <>control1, <>control2;
	
	*new {  | ... points|
		points = points.collect( _.asPoint );

		^super.newCopyArgs( points ).cubicControls( 1/3 );
		}
		
	cubicControls { |amt, loop = true|
		amt = amt ?? { 1/3 };
		this.calcControls( 1, { |... args| args.splineIntControls( amt ) }, loop );
		}
		
	bSplineControls { |amt = 4|
		this.deltaControls_( *(
		[	points.collect( _.x ).bSplineIntDeltaControls( amt ),
			points.collect( _.y ).bSplineIntDeltaControls( amt ) ]
				.flop.collect({ |item| Point( item[0], item[1] ) }) )
		);
		}
		
		
	calcControls { |depth = 1, func, loop = true|
		func = func ? { |... args| args.splineIntControls( 1/3 ) };
		
		#control1, control2 = 
			points.collect({ |item, i|
				var pointArray;
				
				if( loop )
					{ pointArray = points.wrapAt(i + (depth.neg, depth.neg+1 .. depth+1) ); }
					{ pointArray = points.clipAt(i + (depth.neg, depth.neg+1 .. depth+1) ); };
				
				[ func.value( *pointArray.collect(_.x) ),
				  func.value( *pointArray.collect(_.y) )
				].flop.collect({ |item| item[0]@item[1] });
				
			}).flop;
		}
		
	deltaControls_ { |... args|
		control1 = points.collect({ |item, i| item + ( args[i] ? 0 ); });
		control2 = points[1..].collect({ |item, i| item - ( args[i+1] ? 0 ); }) ++ [ points.first - args.last ];
		}	
		
	curveSegments {
		^points.collect({ |point, i| [ point ] ++ control1[i].asCollection ++ control2[i].asCollection });
		 }
	
	curveSegments_ { |array|
		points = array.collect( _[0] );
		control1 = array.collect( _[1] );
		control2 = array.collect( _[2] );
		}
	
	size { ^points.size }
	
	add { |item, c1, c2| ^this.class.new( *(points.add( item.asPoint ) ) ); }
	addAll { |... items| ^this.class.new( *(points ++ items.collect( _.asPoint ) ) ); }
	
	at { |index| 
		var indexF, out, a,b,c1,c2;
		indexF = index.floor;
		
		#a,b = [ points[ indexF ], points.wrapAt(indexF+1) ];
		#c1,c2 = [ control1[ indexF ] ? a, control2[ indexF ] ? b ];
	
		out = [ [a.x, b.x ], [a.y, b.y ] ]
			.collect({ |item, i|
		   		item.splineIntFunction( index.frac, [ c1.x, c1.y ][i], [ c2.x, c2.y ][i] );
		   		});
		
		/*
		out = [ [ points[indexF].x, points.wrapAt(indexF+1).x ],
		   [ points[indexF].y, points.wrapAt(indexF+1).y ] ]
			.collect({ |item, i|
		   		item.splineIntFunction( 
		   			index.frac, 
		   			[ (control1[ indexF ]).x, control1[ indexF ].y ][i], 
					[ (control2[ indexF ]).x, control2[ indexF ].y ][i]
		   			);
		   		});
		   */
		   
		^Point( out[0], out[1] );
		}
	
	asPolyLine { |res = 20|
		^PolyLine( *Array.fill( ((this.size - 1) * res) + 1, { |i| this[i/res]; }) );
		}
		
	copyRange { |start, end| ^this.class.new( *points.copyRange( start, end ) ) }
	
	drawFunction { |div = 10|
		^{ /*	Pen.moveTo( points[0] );
			((points.size - 1) * div).do({ | i|
					Pen.lineTo( this.at( (i+1) / div ) )  
				});
			*/
			Pen.moveTo( points[0] );
			points[1..].do({ |point, i|
				Pen.addCurve( points[i], control1[i] ? points[i],  control2[i] ? point , point, div );
				})
			};
		}
	
	drawFunctionJ {
		^{ 	JPen.moveTo( points[0] );
			points[1..].do({ |point, i| 
				JPen.curveTo(point, control1[ i ], control2[ i ]) });  }
		}
		
	stroke {  this.drawFunction.value; ^Pen.stroke }
	fill {  this.drawFunction.value; ^Pen.fill }
	
	drawControls { |dotRadius = 3, controlColor, pointColor|
		controlColor = controlColor ? Color.red;
		pointColor = pointColor ? Color.black;
		[ points, 
			control1.extend( points.size, nil ), 
			control2.extend( points.size, nil ) ].flop.do({ |item, i|
				var c1, c2;
				c1 = item[1] ? item[0];
				c2 = item[2] ? points.wrapAt(i+1);
				pointColor.set;
				Pen.addArc( item[0], dotRadius, 0, 2pi ).fill;
				controlColor.set;
				Pen.addArc( c1, dotRadius, 0, 2pi ).fill;
				Pen.addArc( c2, dotRadius, 0, 2pi ).fill;
				if( item[0] != c1 )
					{ Pen.line( item[0], c1 ).stroke; };
				if( (i < (points.size-1)) && { points.wrapAt(i+1) != c2 }  )
					{ Pen.line( c2, points.wrapAt(i+1) ).stroke; };
				});
		}
	
	}
	