// wslib 2009

SmoothRangeSlider : SmoothSlider { 
	
	var <nowMoving = nil;
	
	var <>minRange = 0, <>maxRange = 1;
	var <>rangeMode = \lo; // \lo, \hi, \center

	init {  arg parent, bounds;
		bounds = bounds.asRect;
		if( bounds.width > bounds.height )
			{ orientation = \h };
					
		//super.init( parent, bounds );
				
		mode = \jump;  // \jump or \move
		value = [0.0,0.0];
		
		// background, hilightColor, borderColor, knobColor, stringColor
		color = [Color.gray(0.5, 0.5), Color.blue.alpha_(0.5), Color.white.alpha_(0.5),
			Color.black.alpha_(0.7), Color.black ];
	
	}	
	
	value_ { |val| if( val.size != 2 ) 
			{ val = val.asCollection.wrapAt([0,1]) };
			super.value_( val );
		}
		
	valueAction_ { |val| if( val.size != 2 ) 
			{ val = val.asCollection.wrapAt([0,1]) };
			super.valueAction_( val );
		}
			
	lo { ^value.minItem }
	hi { ^value.maxItem }
	range { ^this.hi - this.lo; }
	center { ^value.mean }
	
	lo_ { |lo| this.value = [lo, max( lo, this.hi )]; }
	hi_ { |hi| this.value = [ min(this.lo, hi), hi]; }
	center_ { |center| this.value = center + [ this.range/ -2, this.range/2 ] }

	activeLo_ { |lo| this.valueAction = [lo, this.hi.max(lo)];  }
	activeHi_ { |hi| this.valueAction = [this.lo.min(hi), hi]; }
	activeCenter_ { |center| this.valueAction = center + [ this.range/ -2, this.range/2 ] }
	
	prRange { |range, val|
		val = val ? value;
		 ^switch( rangeMode,
				\lo, { val.minItem + [0, range]; },
				\hi, { val.maxItem - [range, 0]; },
				\center, { val.mean + [range/ -2, range/2] }
				);
		}
		
	activeRange_ { |range| this.valueAction = this.prRange( range ) }
	range_ { |range| this.value = this.prRange( range ) }
	
	setSpan { |lo, hi| this.value = [lo, hi]; }
	setSpanActive_  { |lo, hi| this.valueAction = [lo, hi]; }
	
	setDeviation { arg deviation, average;
			var lo = ( 1 - deviation ) * average;
			this.setSpan(lo, lo + deviation);
	}
	
	draw {
		var startAngle, arcAngle, size, widthDiv2, aw;
		var knobPosition, realKnobSize;
		var rect, drawBounds, radius;
		var baseRect, knobRect;
		var center, strOri;
		
		var bnds; // used with string
		
		value = value.asCollection.wrapAt([0,1]).sort;
		
		Pen.use {
			
			rect = this.drawBounds;
				
			drawBounds = rect.insetBy( border, border );
			
			if( orientation == \h )
				{  drawBounds = Rect( drawBounds.top, drawBounds.left, 
					drawBounds.height, drawBounds.width );
					
				   // baseRect = drawBounds.insetBy( (1-baseWidth) * (drawBounds.width/2), 0 );
				   
				   Pen.rotate( 0.5pi, (rect.left + rect.right) / 2, 
				   					 rect.left + (rect.width / 2)  );
				};
			
			baseRect = drawBounds.insetBy( (1-baseWidth) * (drawBounds.width/2), 0 );
			
			size = drawBounds.width;
			widthDiv2 = drawBounds.width * 0.5;
					
			realKnobSize = (knobSize * drawBounds.width)
					.max( thumbSize ).min( drawBounds.height );
			radius = (knobSize * drawBounds.width) / 2;
			knobPosition = drawBounds.top + ( realKnobSize / 2 )
						+ ( (drawBounds.height - realKnobSize) * (1- value).max(0).min(1));
			
			if( this.hasFocus ) // rounded focus rect
				{
				Pen.use({
					Pen.color = focusColor ?? { Color.gray(0.2).alpha_(0.8) };
					Pen.width = 2;
					Pen.roundedRect( baseRect.insetBy(-2 - border,-2 - border), 
						(radius.min( baseRect.width/2) + 1) + border );
					Pen.stroke;
					});
				};
				
			Pen.use{	
			color[0] !? { // base / background
				//Pen.fillColor = color[0];
				Pen.roundedRect( baseRect, radius.min( baseRect.width/2) );
				color[0].penFill( baseRect );
				};
			
			if( backgroundImage.notNil )
				{ 
				Pen.roundedRect( baseRect, radius.min( baseRect.width/2) );
				backgroundImage[0].penFill( baseRect, *backgroundImage[1..] );
				}
			};
			
			Pen.use{
			color[2] !? { // // border
				if( border > 0 )
					{ 
					
					  if( color[2].notNil && { color[2] != Color.clear } )
					  	{	 Pen.strokeColor = color[2];
						  Pen.width = border;
						  Pen.roundedRect( baseRect.insetBy( border/(-2), border/(-2) ), 
						  	radius.min( baseRect.width/2) + (border/2) ).stroke;
						  };
					  if( extrude )
					  	{ 
					  	Pen.use{	
						  	  Pen.rotate( (h: -0.5pi, v: 0 )[ orientation ],
					   				(rect.left + rect.right) / 2, 
					   				rect.left  + (rect.width / 2)  );
					   		
						  	  Pen.extrudedRect( 
						  	  	baseRect.rotate((h: 0.5pi, v: 0 )[ orientation ],
					   				(rect.left + rect.right) / 2, 
					   				rect.left  + (rect.width / 2))
					   					.insetBy( border.neg, border.neg ), 
						  		(if( radius == 0 ) 
						  			{ radius } { radius + border }).min( baseRect.width/2 ),
						  		border, 
						  		inverse: true )
						  	}
					  	};
					};
				};
				};
			
			Pen.use{	
			
		
			color[1] !? { 
				//color[1].set; // hilight
				
				//knobPosition.postln;
				
				Pen.roundedRect( Rect.fromPoints( 
						baseRect.left@(knobPosition[0] + (realKnobSize / 2)),
						baseRect.right@(knobPosition[1] - (realKnobSize / 2))
						 ), radius.min( baseRect.width/2) );
				
				color[1].penFill( baseRect );
				};
			
				};
				
			
			Pen.use{
	
			color[3] !? {	  // knob
				knobPosition.do({ |knobPosition,i|
					var knobRect;
					
					//knobPosition = knobPosition - ((realKnobSize / 2)*i);
					
					knobRect =  Rect.fromPoints(
						Point( drawBounds.left,  knobPosition - (realKnobSize / 2) ),
						Point( drawBounds.right, knobPosition + (realKnobSize / 2) ) );
	
					Pen.roundedRect( knobRect, radius );
						// ( [ 0, 0, radius, radius ].rotate(i*2) );//.fill; 
					
					color[3].penFill( knobRect ); // requires extGradient-fill.sc methods
					
					 if( extrude && ( knobRect.height >= border ) )
						  	{ 
						  	Pen.use{	
							  	  Pen.rotate( (h: -0.5pi, v: 0 )[ orientation ],
						   				(rect.left + rect.right) / 2, 
						   				rect.left  + (rect.width / 2)  );
						   		
							  	  Pen.extrudedRect( 
							  	  	knobRect.rotate((h: 0.5pi, v: 0 )[ orientation ],
						   				(rect.left + rect.right) / 2, 
						   				rect.left  + (rect.width / 2)), 
							  		radius.max( border ), border * knobBorderScale)
							  	}
						  	};
					});
				};
				
				};
			
			
			string !? {
				
				
				if( stringAlignToKnob )
					{ bnds = knobRect ?? { Rect.fromPoints(
						Point( drawBounds.left, 
							( knobPosition.mean - (realKnobSize / 2) ) ),
						Point( drawBounds.right, 
							knobPosition.mean + (realKnobSize / 2) ) );  }; }
					{ bnds = drawBounds };
				
				stringOrientation = stringOrientation ? \h;
								
				Pen.use{	
					
					center = drawBounds.center;
					
					strOri = (h: 0pi, v: 0.5pi, u: -0.5pi, d: 0.5pi, up: -0.5pi, down: 0.5pi )
							[stringOrientation] ? stringOrientation;
					
					strOri = strOri + (h: -0.5pi, v: 0)[ orientation ];
					
					if( strOri != 0 )
					{ Pen.rotate( strOri, center.x, center.y );
					 bnds = bnds.rotate( strOri.neg, center.x, center.y );
					};
						 		 
					font !? { Pen.font = font };
					Pen.color = color[4] ?? { Color.black; };
					string = string.asString;
					
					switch( align ? \center,
						\center, { Pen.stringCenteredIn( string, bnds ) },
						\middle, { Pen.stringCenteredIn( string, bnds ) },
						\left, { Pen.stringLeftJustIn( string, bnds ) },
						\right, { Pen.stringRightJustIn( string, bnds ) } );
					
					font !? { Pen.font = nil; };
					};
				};
			
			if( enabled.not )
				{
				Pen.use {
					Pen.fillColor = Color.white.alpha_(0.5);
					Pen.roundedRect( 
						baseRect.insetBy( border.neg, border.neg ), 
						radius.min( baseRect.width/2) ).fill;
					};
				};
		
			};
		}
	
	xyToPos { |x,y|
		var bounds, pos;
		bounds = this.drawBounds;
		// can be optimized
		if( orientation == \v ) 
			{	if( thumbSize < bounds.height )
					{ pos = ( 1 - ((y - (bounds.top + (
							( knobSize * bounds.width )
							.max( thumbSize.min( bounds.height ) ) / 2))) / 
						(bounds.height - 
							(knobSize * bounds.width )
							.max( thumbSize )  ))
						).clip( 0.0,1.0 );
						};
			}
			{	if( thumbSize < bounds.width )
					{ pos = ((x - (bounds.left + (
						( knobSize * bounds.height )
						.max( thumbSize.min( bounds.width ) ) / 2))) / 
					(bounds.width - (knobSize * bounds.height )
						.max( thumbSize.min( bounds.width ) ) ))
						.clip(0.0,1.0); };
			};
		^pos;
	}

	mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
		if( enabled ) {	
			mouseDownAction.value( this, x, y, modifiers, buttonNumber, clickCount );
			hit = Point(x,y);
			hitValue = value;
			if( mode == \drag ) { 
					value = (this.xyToPos(x,y).dup - ((step ? 0)/2)).round( step ? 0 );
					nowMoving = 1;
					 };
			this.mouseMove(x, y, modifiers);
		};
	}
	
	mouseMove { arg x, y, modifiers;
		var pt, angle, inc = 0, oldValue, range;
		var bounds;
		var pos, closest;
		if( enabled ) {	
			mouseMoveAction.value( this, x, y, modifiers );
			bounds = this.drawBounds;
			if (modifiers != 1048576, { // we are not dragging out - apple key
				
					oldValue = value.copy;
					pos = this.xyToPos( x,y );
					
					nowMoving = nowMoving ?? {
						closest = value.collect(_.absdif(pos));
						if( value.mean.absdif( pos ) < closest.minItem )
							{ [0,1] }
							{ closest.minIndex };
						};

					if( nowMoving.size > 1 )
						{ range = this.range;
						 value = (value - value.mean) + pos.clip(range/2, 1-(range/2)); }
						{ value[ nowMoving ] = pos;
						if( value[0] > value[1] )
							{ value = value[[1,0]]; 
						       nowMoving = 1-nowMoving;
							};
						};
					value = value.round( step ? 0 );
					deltaAction.value( this, value - oldValue );
					this.clipValue;
					
					if( allwaysPerformAction or: { oldValue != value } )
						{ action.value(this, x, y, modifiers); };
					
					//hit = Point(x,y);
					this.refresh;
				
		});
		};
	}
	
	mouseUp { |x, y, modifiers, buttonNumber|
		nowMoving = nil; 
		mouseUpAction.value( this, x, y, modifiers, buttonNumber ); 
	}
	
	clipValue { |active = true|
		var newVal, range;
		newVal = value.sort.perform( clipMode, 0.0, 1.0 );
		
		range = (newVal.maxItem - newVal.minItem);
		case { range  < minRange } 
			{ newVal = this.prRange( minRange, newVal ); 
			  case { newVal[0] < 0 } { newVal = newVal - newVal[0] }
			  	  { newVal[1] > 1 } { newVal = newVal + (1-newVal[1]) }; 
			}
			{ range > maxRange } 
			{ newVal = this.prRange( maxRange, newVal );
			  case { newVal[0] < 0 } { newVal = newVal - newVal[0] }
			  	  { newVal[1] > 1 } { newVal = newVal + (1-newVal[1]) }; 
			};
			
		if( active && {newVal != value} )
			{ outOfBoundsAction.value( this, value, newVal  ); };
			
		value = newVal;
		}
	
	keyDown { arg char, modifiers, unicode,keycode;
		var zoom = this.getScale(modifiers); 
		
		// standard keydown
		if (char == $r, { this.valueAction = 1.0.rand; });
		if (char == $n, { this.valueAction = 0.0; });
		if (char == $x, { this.valueAction = 1.0; });
		if (char == $c, { this.valueAction = 0.5; });
		if (char == $], { this.increment(zoom); ^this });
		if (char == $[, { this.decrement(zoom); ^this });
		if (unicode == 16rF700, { this.increment(zoom); ^this });
		if (unicode == 16rF703, { this.increment(zoom); ^this });
		if (unicode == 16rF701, { this.decrement(zoom); ^this });
		if (unicode == 16rF702, { this.decrement(zoom); ^this });
		
		^nil;
		
	}

	defaultCanReceiveDrag {
		^( View.currentDrag.size == 2 );
	}
}

RoundRangeSlider : SmoothRangeSlider {
	
	init { arg parent, bounds;
		super.init( parent, bounds );
		
		// background, hilightColor, borderColor, knobColor, stringColor
		color = [ nil, nil, Color.clear, Color.clear, Color.black ];
		extrude = true;
		border = 1;
		knobSize = 0.5;
		thumbSize = 12;
		}
		
}

