// wslib 2009-2011

XYView : UserViewHolder {

	var <x = 0, <y = 0;
	var <foreground, <background;
	var <>returnAfterMouseUp = true;
	var mouseDownPoint, mouseDownValue;
	var <radius;
	var <>stepSize = 1;

		
	properties { ^[] }
	
	init { arg parent, bounds;
		bounds = bounds.asRect;
		foreground = Color.grey( 0.2 );
		background = Color.white.alpha_(0.5);
		radius = bounds.width.min(bounds.height) / 2;
		this.canFocus_( false );
	}
	
	value { ^(x@y) }
	
	x_ {  arg xx;
		x = xx;
		view.refresh;
		}
		
	y_ {  arg yy;
		y = yy;
		view.refresh;
		}

	radius_ { arg newRadius;
		radius = newRadius;
		view.refresh;
		}
	
	value_ { arg val;
		val = val.asPoint;
		x = val.x;
		y = val.y;
		view.refresh;
		}
	
	valueAction_ { arg val;
		this.value_( val );
		this.doAction;
	}
	
	foreground_ { |color| foreground = color; this.refresh; }
	
	background_ { |color| background = color; this.refresh; }
	
	draw {
		var relBounds = this.drawBounds;
		var inset = 2;
		var arrowRadius = (relBounds.width.min(relBounds.height) / 2) - inset;
		
		Pen.roundedRect( relBounds, radius); 
		
		background.penFill( relBounds );
		
		Pen.use({
			Pen.translate( *relBounds.center.asArray );
			
			Pen.color = foreground;
			
			4.do({ |i|
				if( switch( i,
					0, { x > 0  },
					1, { y > 0  },
					2, { x < 0  },
					3, { y < 0  }), 
					{ Pen.width = 2.5 },
					{ Pen.width = 1 });

				Pen.arrow( 0@0, Polar( arrowRadius, (i / 2) * pi ).asPoint, 3 );
				Pen.stroke;
				});
			
		 });
	}
	
	mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);
		if( clickCount > 1 ) { this.value = 0@0 }; // reset
		mouseDownPoint = (x@y);
		mouseDownValue = this.value;
		action.value( this, x, y );
	}

	
	mouseMove { arg x, y, modifiers;
		mouseMoveAction.value(this, x, y, modifiers);
		this.value_( mouseDownValue + 
			(((x@y) - mouseDownPoint) * stepSize) );
		action.value( this, x, y );
	}
	
	mouseUp { arg x, y, modifiers, buttonNumber;
		mouseUpAction.value(this, x, y, modifiers, buttonNumber);
		if( returnAfterMouseUp ) { this.value_( mouseDownValue ); };
	}
}