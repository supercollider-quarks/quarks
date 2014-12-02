// wslib 2007
// Pen-based plot view

PlotView : SCUserView {
	
	var <value;
	var <lineColor, <lineWidth = 1;
	var <dotColor, <dotSize = 0;
	var <fillColor, <fill = false;
	var <border = 1, <borderColor;
	var <minVal = 0, <maxVal = 1;
	var orientation = \h;
	
	*viewClass { ^SCUserView }
	
	init { arg parent, bounds;
		bounds = bounds.asRect;
		super.init(parent, bounds);
		value = [0.5,0.5];
		lineColor = Color.black;
	     dotColor = Color.red(0.5).alpha_(0.5);
	     fillColor = Color.blue(0.25).alpha_(0.5);
		borderColor = Color.gray(0.2).alpha_(0.5);
		}
	
	draw {
		var drawBounds, pointWidth, pointStart, viewHeight, points;
		
		Pen.use {
			drawBounds = this.bounds;
			if( orientation == \v )
				{  drawBounds = Rect( drawBounds.top, drawBounds.left, 
					drawBounds.height, drawBounds.width );
				   //Pen.translate( drawBounds.height, 0 );
				    Pen.rotate( 0.5pi,
				   	(this.bounds.left + this.bounds.right) / 2, 
				   	this.bounds.left  + (this.bounds.width / 2)  );
				  
				};
			if( value.size < 2 )
				{ value = value.asCollection;
				  value.extend(2, value[0]);
				 };
				
			pointWidth = drawBounds.width / (value.size - 1);
			//pointWidth = 1;
			pointStart = drawBounds.leftTop;
			viewHeight = drawBounds.height;
			points = value.collect({ |item, i|
				pointStart + 
					((i * pointWidth)@(item.linlin(minVal, maxVal, viewHeight, 0)))
					});
			
			if( lineWidth > 0 )
				{ Pen.width = lineWidth;
				  lineColor.set;
				  Pen.moveTo( points[0] );
				  points[1..].do({ |point| Pen.lineTo( point ) });
				  Pen.stroke;
				 };
				 
			if( fill )
				{ fillColor.set;
				  Pen.moveTo( drawBounds.leftBottom );
				  points.do({ |point| Pen.lineTo( point ) });
				  Pen.lineTo( drawBounds.rightBottom );
				  Pen.fill;
				 };
			
			if( dotSize > 0 )
				{ dotColor.set;
				  points.do({ |point|
				  	Pen.addArc( point, dotSize/2, 0, 2pi ).fill;
				  	})
				 };
				 
			if( border > 0 )
				{ borderColor.set;
					Pen.strokeRect( drawBounds ) };
		};
	}
	
	value_ { |newValue| value = newValue; this.refresh; }
	
	minVal_ { |newValue| minVal = newValue; this.refresh; }
	maxVal_ { |newValue| maxVal = newValue; this.refresh; }
	
	fill_ { |bool| fill = bool; this.refresh; }
	dotSize_ { |newValue| dotSize = newValue; this.refresh; }
	lineWidth_ { |newValue| lineWidth = newValue; this.refresh; }
	
	}