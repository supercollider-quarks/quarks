// wslib 2006
// slider based on blackrain's knob

SmoothSlider : RoundView {
	var <>color, <value, <>step, hit, <>mode, isCentered = false, <centerPos = 0.5;
	var <border = 0, <baseWidth = 1, <extrude = false, <knobBorderScale = 2;
	var <knobSize = 0.25, hitValue;
	var <orientation = \v;
	var <thumbSize = 0; // compatible with old sliders
	var <focusColor;
	
	var <>deltaAction, <>allwaysPerformAction = false;
	var <>outOfBoundsAction;
	
	var <>clipMode = \clip; // or \wrap, \fold (or any unary or binary op)
	var <string, <font, <align, <stringOrientation = \h, <stringAlignToKnob = false;
	
	var <>shift_scale = 100.0, <>ctrl_scale = 10.0, <>alt_scale = 0.1;
	
	var <>grid = 0;
	
	*viewClass { ^SCUserView }
	
	init { arg parent, bounds;
		bounds = bounds.asRect;
		if( bounds.width > bounds.height )
			{ orientation = \h };
					
		//super.init( parent, bounds );
				
		mode = \jump;  // \jump or \move
		value = 0.0;
		
		// background, hilightColor, borderColor, knobColor, stringColor
		color = [Color.gray(0.5, 0.5), Color.blue.alpha_(0.5), Color.white.alpha_(0.5),
			Color.black.alpha_(0.7), Color.black ];
	}
	
	refresh { { super.refresh }.defer; } // no need to use defer anymore
	
	sliderBounds {
		var realKnobSize, drawBounds, rect;
		
		rect = this.drawBounds;
				
		drawBounds = rect.insetBy( border, border );
				
		if( orientation == \h )
				{  drawBounds = Rect( drawBounds.top, drawBounds.left, 
					drawBounds.height, drawBounds.width ); 
				};
				
		realKnobSize = (knobSize * drawBounds.width)
					.max( thumbSize ).min( drawBounds.height );
		
		
		^drawBounds.insetBy( 0, realKnobSize / 2 );
		}
		
	focusColor_ { |newColor| focusColor = newColor; this.parent.refresh; }
		
	knobColor { ^color[3] }
	knobColor_ { |newColor| color[3] = newColor; this.refresh; }
	
	background { ^color[0] }
	background_ { |newColor| color[0] = newColor; this.refresh; }
	
	borderColor { ^color[2] }
	borderColor_ { |newColor| color[2] = newColor; this.refresh; }
	
	border_ { |newBorder| border = newBorder; this.refresh; }
	
	extrude_ { |bool| extrude = bool; this.refresh; }
	
	knobBorderScale_ { |value| knobBorderScale = value; this.refresh; }
	
	baseWidth_ { |newBaseWidth| baseWidth = newBaseWidth; this.refresh; }
	
	hilightColor { ^color[1] }
	hilightColor_ { |newColor| color[1] = newColor; this.refresh; }
	
	hiliteColor { ^color[1] } // slang but compatible
	hiliteColor_ { |newColor| color[1] = newColor; this.refresh; }
	
	thumbSize_ { |newSize = 0| thumbSize = newSize; this.refresh; }
	
	relThumbSize { if( orientation == \h ) 
		{ ^thumbSize / this.bounds.width  }
		{ ^thumbSize / this.bounds.height };
		}
	
	relThumbSize_ { |newSize = 0| if( orientation == \h ) 
		{ thumbSize = newSize * this.bounds.width }
		{ thumbSize = newSize * this.bounds.height };
		this.refresh;
		}
		
	absKnobSize {  if( orientation == \h ) 
		{ ^knobSize * this.bounds.height  }
		{ ^knobSize * this.bounds.width };
	 }
	 
	stringColor { ^color[4] } // slang but compatible
	stringColor_ { |newColor| 
		if( color.size > 4 )
			{ color[4] = newColor; }
			{ color = color ++ [newColor] };
		this.refresh;
		 }
		 
	font_ { |newFont| font = newFont; this.refresh; }
	string_ { |newString| string = newString; this.refresh; }
	align_ { |newAlign| align = newAlign; this.refresh; }
	
	stringAlignToKnob_ { |bool| stringAlignToKnob = (bool == true); this.refresh; }
	
	draw {
		var startAngle, arcAngle, size, widthDiv2, aw;
		var knobPosition, realKnobSize;
		var rect, drawBounds, radius;
		var baseRect, knobRect;
		var center, strOri;
		
		var bnds; // used with string
		
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
			
			/*
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
			*/
			
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
				
			this.drawFocusRing( 
				baseRect.insetBy( border.neg , border.neg ), 
				radius.min( baseRect.width/2) + border 
			);
			
			Pen.use{	
			
			color[1] !? { 
				//color[1].set; // hilight
				if( isCentered )
				{
				Pen.roundedRect( Rect.fromPoints( 
						baseRect.left@
							((knobPosition - (realKnobSize / 2))
								.min( baseRect.bottom.blend( baseRect.top, centerPos ) ) ),
						baseRect.right@
							((knobPosition + (realKnobSize / 2))
								.max( baseRect.bottom.blend( baseRect.top, centerPos ) ) ))
						
					, radius ); //.fill;
				color[1].penFill( baseRect );
				}
				{
				Pen.roundedRect( Rect.fromPoints( 
						baseRect.left@(knobPosition - (realKnobSize / 2)),
						baseRect.right@baseRect.bottom ), radius.min( baseRect.width/2) );
				
				color[1].penFill( baseRect );
				};
				};
				
				};
				
			Pen.use{
	
			color[3] !? {	 
				knobRect =  Rect.fromPoints(
					Point( drawBounds.left, 
						( knobPosition - (realKnobSize / 2) ) ),
					Point( drawBounds.right, knobPosition + (realKnobSize / 2) ) );

				Pen.roundedRect( knobRect, radius );//.fill; 
				
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
				};
				
				};
			
			string !? {
				
				
				if( stringAlignToKnob )
					{ bnds = knobRect ?? { Rect.fromPoints(
						Point( drawBounds.left, 
							( knobPosition - (realKnobSize / 2) ) ),
						Point( drawBounds.right, knobPosition + (realKnobSize / 2) ) );  }; }
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
		
	getScale { |modifiers| 
		^case
			{ modifiers & 131072 == 131072 } { shift_scale }
			{ modifiers & 262144 == 262144 } { ctrl_scale }
			{ modifiers & 524288 == 524288 } { alt_scale }
			{ 1 };
	}
	
	pixelStep { 
		var bounds = this.sliderBounds; 
		^(bounds.width.max(bounds.height) - this.thumbSize).reciprocal
	}
	

	mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
		var bounds, oldValue;
		if( enabled ) {		
			mouseDownAction.value( this, x, y, modifiers, buttonNumber, clickCount );
			bounds = this.drawBounds; 
			if( orientation == \v ) { 
				hit = Point(x, y); 
			} { 
				hit = Point(y, bounds.right - x); 
				bounds = Rect( bounds.top, bounds.left, bounds.height, bounds.width );
			};
			
			if( mode == \jump ) { // move slider to mouse point
				
				oldValue = value;
				if( thumbSize < bounds.height ) { 
					value = 1 - ((hit.y - (bounds.top + (
						( knobSize * bounds.width )
							.max( thumbSize.min( bounds.height ) ) / 2))) / 
						(bounds.height - (knobSize * bounds.width )
							.max( thumbSize )  
						)
					)
				};
				
				value = value.round( step ? 0 );
				deltaAction.value( this, value - oldValue );
				this.clipValue;

				if( allwaysPerformAction or: { oldValue != value } )
					{ action.value(this, x, y, modifiers); };
				this.refresh;
			};
			
			hitValue = value;
		};
		
	}

	
	mouseMove { arg x, y, modifiers;
		var pt, angle, inc = 0;
		var bounds, oldValue, delta;
		if( enabled ) {	
			mouseMoveAction.value( this, x, y, modifiers );
			bounds = this.drawBounds;
			if( orientation == \v ) {
				pt = Point(x, y);
			} { 
				pt = Point(y, bounds.right - x);
				bounds = Rect( bounds.top, bounds.left, bounds.height, bounds.width );
			};
			
			if (modifiers != 1048576, { // we are not dragging out - apple key
				
				oldValue = value;
				
				if( thumbSize < bounds.height ) { 
					value = ( hitValue + ( 
						( (hit.y - pt.y) / this.sliderBounds.height )  
						* this.getScale( modifiers ) ) 
					)
				};
				
				value = value.round( step ? 0 );
				deltaAction.value( this, value - oldValue );
				this.clipValue;
				
				if( allwaysPerformAction or: { oldValue != value } )
					{ action.value(this, x, y, modifiers); };
					
				this.refresh;
			});
		};
	}
	
	clipValue { |active = true|
		var newVal;
		newVal = value.perform( clipMode, 0.0, 1.0 );
		if( active && {newVal != value} )
			{ outOfBoundsAction.value( this, value, newVal  ); value = newVal; }
		}

	value_ { arg val;
		value = val.round( step ? 0 );
		this.clipValue( false );
		this.refresh;
	}

	valueAction_ { arg val;
		var oldVal;
		deltaAction.value( this, val - value );
		oldVal = value;
		value = val.round( step ? 0 );
		this.clipValue;
		if( allwaysPerformAction or: { oldVal != value } ) { action.value(this); };
		this.refresh;
		}
	
	delta { |val = 0|
		this.valueAction = value + val;
		}
	
	/* // obsolete
	safeValue_ {  // prevent crash when window is closed
		 arg val;
		value = val.clip(0.0, 1.0);
		if( parent.notNil && { parent.findWindow.dataptr.notNil } )
			{ this.refresh; }
		}
	*/
	

	centered_ { arg bool;
		isCentered = bool;
		this.refresh;
	}
	
	centered {
		^isCentered
	}
	
	centerPos_ { |value = 0.5|
		centerPos = value;
		this.refresh;
	}
	
	orientation_ { |newOrientation| 
		if( stringOrientation == orientation ) { stringOrientation = newOrientation };
		orientation = newOrientation ? orientation; this.refresh;
		
		 }
		 
	stringOrientation_ { |newOrientation| // resets if nil
		stringOrientation = newOrientation ? orientation;
		this.refresh;
		}
	
	knobSize_ { |newSize| knobSize = newSize ? knobSize; this.refresh; }
	
	increment { |zoom=1| ^this.valueAction = 
		( this.value + (max(this.step ? 0, this.pixelStep) * zoom) ).min(1); }
	decrement { |zoom=1| ^this.valueAction = 
		( this.value - (max(this.step ? 0, this.pixelStep) * zoom) ).max(0); }
	

	keyDown { arg char, modifiers, unicode, keycode, key;
		var zoom = this.getScale(modifiers); 
		var arrowKey = unicode.getArrowKey ? key.getArrowKey;
		
		// standard keydown
		if (char == $r, { this.valueAction = 1.0.rand; });
		if (char == $n, { this.valueAction = 0.0; });
		if (char == $x, { this.valueAction = 1.0; });
		if (char == $c, { this.valueAction = centerPos; });
		if (char == $], { this.increment(zoom); ^this });
		if (char == $[, { this.decrement(zoom); ^this });
		
		switch( arrowKey,
			\up, { this.increment(zoom); ^this },
			\right, { this.increment(zoom); ^this },
			\down, { this.decrement(zoom); ^this },
			\left, { this.decrement(zoom); ^this }
		);
		
		^nil;
		
	}

	defaultReceiveDrag {
		this.valueAction_(View.currentDrag);
	}
	defaultGetDrag { 
		^value
	}
	defaultCanReceiveDrag {
		^View.currentDrag.isFloat;
	}
}


RoundSlider : SmoothSlider {

	// a SmoothSlider with different default styling
	// matches with RoundButton and RoundNumberBox
	
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


