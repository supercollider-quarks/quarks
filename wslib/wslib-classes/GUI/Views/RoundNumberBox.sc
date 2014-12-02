RoundNumberBox : RoundView {
	
	classvar <>defaultFormatFunc, <>defaultInterpretFunc, <>defaultFontFace, <>defaultFontSize;
	
	var <value = 0; // from roundbutton
	var <string;
	var <font;
	var <align = \left;
	var <radius, <border = 1;
	var <extrude = true;
	var <inverse = false;
	var <focusColor, <stringColor;
	var <formatFunc, <>interpretFunc, <>allowedChars = "+-.eE*/()%";
	var <innerShadow;
	
	var <autoScale = false; // BETA
	
	var <keyString, <>step=1, <>scroll_step=1;
	var <typingColor, <normalColor;
	var <background;
	var <>clipLo = -inf, <>clipHi = inf, hit, startHit, inc=1.0, 
		<>scroll=true; //, <>shift_step=0.1, <>ctrl_step=10;
	
	var <>wrap = false;
	
	var <>shift_scale = 100.0, <>ctrl_scale = 10.0, <>alt_scale = 0.1;
		
	var <charSelectColor, <charSelectIndex = -1;
	
	var <>actionOnlyOnChange = true;
	
	var <>logScale; // nil, 10, 2
		
	// *viewClass { ^SCUserView }
	
	refresh { { super.refresh }.defer }
	
	*initClass { 
		defaultFormatFunc = { |value| value };
		StartUp.add({ 
			if( GUI.current.notNil ) { 
				defaultFontFace = Font.defaultSansFace; 
			};
		});
		defaultFontSize = 12;
		defaultInterpretFunc = { |string| string.interpret };
	}
		 
	*defaultFont { ^Font( defaultFontFace, defaultFontSize ) }
	*defaultFont_ { |font| defaultFontFace = font.name; defaultFontSize = font.size }
	
	doesNotUnderstand { |selector ... args| // dirty maybe, but any reason not to do this?
		if( selector.isSetter )
			{ if( this.class.instVarNames.includes( selector.asGetter ) )
				{ this.slotPut( selector.asGetter, args[0] ); this.refresh }
				{ ^super.doesNotUnderstand( selector, *args ) }; }
			{ ^super.doesNotUnderstand( selector, *args ) };
		}
		
	respondsTo { arg ... args;
		^if( super.respondsTo( *args ),
		 	true, 
		 	{ this.class.instVarNames.includes( args[0].asSymbol.asGetter ) });
	}
		
	background_ { |color| background = color; this.refresh; }
	
	init { |parent, bounds|
		super.init( parent, bounds );
		typingColor = Color.red;
		normalColor = Color.black;
		background = Color.white;
		stringColor = normalColor;
		formatFunc = defaultFormatFunc;
		interpretFunc = defaultInterpretFunc;
		font = Font( defaultFontFace ? "Helvetica", defaultFontSize );
		}
			
	getScale { |modifiers| 
		var inc = 1;
		switch( logScale,
			10, { inc = (10**(value.abs.log10.floor)).max(0.001); },
			2, { inc = (2**(value.abs.log2.floor)).max(0.0625);  }
		);
		^case
			{ modifiers & 131072 == 131072 } { shift_scale * inc }
			{ modifiers & 262144 == 262144 } { ctrl_scale * inc }
			{ modifiers & 524288 == 524288 } { alt_scale * inc }
			{ inc };
	}

	valueAction_ { arg val;
		var oldValue;
		oldValue = value;
		value = val;
		this.prClipValue;
		if( actionOnlyOnChange nand: { (value == oldValue) } )
			{ action.value(this, value); };
		this.refresh;
		}
	
	value_ { arg val;
		value = val;
		this.prClipValue;
		this.refresh;
		}
		
	steps_ { |newStep| step = scroll_step = newStep ? step }
		
	interpret {
		var oldValue;
		oldValue = value;
		value = interpretFunc.value(keyString) ? value;
		keyString = nil;
		if( actionOnlyOnChange nand: { (value == oldValue) } )
			{ action.value(this, value); };
		stringColor = normalColor;
		this.refresh;
		}
		
	prClipValue {
		if( value.respondsTo( 'clip' ) && { value.class != String } )
			{ 
			if( wrap )
				{ if ( (clipLo != -inf) && { this.clipHi != inf } )
					{ value = value.wrap( clipLo, this.clipHi ) }
				}
				{ value = value.clip(clipLo, this.clipHi); };
			};
		}
			
	defaultGetDrag { 
		^value
	}
	
	defaultCanReceiveDrag {
		^(this.currentDrag.isNumber) or: { this.currentDrag.class == String };
	}
	defaultReceiveDrag {
		if( this.currentDrag.class == String )
			{ this.valueAction = this.currentDrag.interpret ? value; }
			{ this.valueAction = this.currentDrag;  }	
	}
	
	draw {
		var rect, localRadius;
		var shadeSide, lightSide, stringRect, stringBounds, stringStart, stringWidth;
		rect = this.drawBounds.insetBy(1,1);
		
		radius = radius ?? { (rect.height/4).min( rect.width/2 ) };
		
		/*
		if( this.hasFocus ) // rounded focus rect
			{
			Pen.use({
				Pen.color = focusColor ?? { Color.gray(0.2).alpha_(0.8) };
				Pen.width = 2;
				Pen.roundedRect( rect.insetBy(-2,-2), radius + 1 );
				Pen.stroke;
				});
			};
		*/

		if( inverse )
			{ lightSide = Color.black.alpha_(0.5);
		       shadeSide = Color.white.alpha_(0.5); }
			{ lightSide = Color.white.alpha_(0.5);
		       shadeSide = Color.black.alpha_(0.5); };
		
		Pen.use {
			//Pen.color_( background ?? { Color.clear } );
				
			background !? { 
				Pen.roundedRect( rect, radius );
				background.penFill( rect );
				};
			
			if( border > 0 ) {
				if( extrude ) { 
					Pen.extrudedRect( rect, radius, border, 0.17pi, false,
						[ shadeSide, lightSide ] ); 
				} {  
					Pen.color = shadeSide;
					Pen.width = border;
					Pen.roundedRect( rect.insetBy( border/2,border/2 ), radius - 
					   	(border/2)  ).stroke; 
				};
			};
			
			if( innerShadow.notNil )
				{
				Pen.use({
					Pen.roundedRect( rect.insetBy(border - 0.1, border - 0.1), radius ).clip;
					if( innerShadow.isNumber )
							{ Pen.setShadow( innerShadow@(innerShadow.neg), innerShadow, 
								Color.black.alpha_(0.5) ); }
							{ Pen.setShadow( *innerShadow.asCollection );  };
					Pen.width = 5;
					Pen.color = Color.black;
					Pen.roundedRect(  rect.insetBy( border - 3.5, border - 3.5 ),
						 radius ).stroke;
					});
				};
										
			Pen.use({
				Pen.roundedRect( rect.insetBy( border ), radius ).clip;
				
				string = keyString ?? { formatFunc.value(value).asString };
				stringRect = this.stringRect;
				
				if( autoScale )
					{ 
						stringBounds = string.bounds( font );
						if( GUI.id == \qt ) { 
							stringBounds.width = stringBounds.width * 1.2;
							stringBounds.height = stringBounds.height * 1.3;
						};
						Pen.transformToRect( stringRect.insetBy(1,0), stringBounds, true, 
							move: ( left: (0@0.5), right: (1@0.5), center: (0.5@0.5), 
								middle: (0.5@0.5) )[ align ] ? (0@0.5) );
					 	 stringRect = stringBounds;
					 };
					
				//Pen.color = Color.green.alpha_( 0.5 ); // debug
				//Pen.fillOval( stringRect );
					
						
				if( (charSelectIndex >= 0) and: { charSelectIndex < string.size } )
					{
					Pen.color = charSelectColor ?? { Color.gray(0.66) };
					Pen.addRect( this.charSelectRect( stringRect,
						charSelectIndex, 1 ) ).fill;
					
					};
					
				Pen.font_( font );
				Pen.color_( stringColor ? Color.black );
				
				if( autoScale )
				{ Pen.stringCenteredIn( string, stringRect ); }
				{
				Pen.perform( 
					( left: \stringLeftJustIn, right: \stringRightJustIn, 
						center: \stringCenteredIn, middle: \stringCenteredIn )[ align ]
						? \stringLeftJustIn, string, stringRect );
				}
						
				});
			
			if( enabled.not )
				{ Pen.use{
					Pen.fillColor = Color.white.alpha_(0.5);
					Pen.roundedRect( rect, radius ).fill;
					};
				};
			
			
			this.drawFocusRing( rect, radius );
			
			} 
		}
		
	stringRect { ^this.drawBounds.insetBy(1,1).insetAll( (radius.asCollection@@[0,3]).maxItem / 2,
				0, (radius.asCollection@@[1,2]).maxItem / 2, 0 );
			}
			
	charSelectRect { |stringRect, start = 0, range = 1|
		var rect, stringStart, stringWidth;
		if( autoScale )
			{
			stringRect = stringRect ?? { string.bounds( font ); };
			stringStart = string[..start-1].bounds( font ).width;
			stringWidth = string[start..(start+range)-1].bounds( font ).width;
			^Rect( stringStart + stringRect.left + 
				((stringRect.width - string.bounds(font).width)/2), 
				stringRect.top, stringWidth + 0.5, stringRect.height );
			}
			{	
			rect = this.drawBounds;
			stringRect = stringRect ?? { this.stringRect; };
			stringStart = string[..start-1].bounds( font ).width;
			stringWidth = string[start..(start+range)-1].bounds( font ).width;
			^Rect( switch( align,
					\left, { (stringStart + stringRect.left) + 1 },
					\right, {((stringStart + stringRect.right) - string.bounds(font).width)-2 },
					\center, { stringStart + stringRect.left +
								((stringRect.width - string.bounds(font).width)/2)  },
					\middle, { stringStart + stringRect.left +
								((stringRect.width - string.bounds(font).width)/2)
							 }) ?? {  stringStart + stringRect.left },
				rect.top, stringWidth + 0.5, rect.height );
			};
		}
		
	charIndexFromPoint { |point, exclude|
		var i = 0, stringWidth, stringSize, stringRect;
		stringSize = string.size;
		stringRect = this.charSelectRect(nil, 0, stringSize );
		//stringWidth = stringRect.width;
		exclude = exclude ? [];
		
		if( autoScale )
			{ point = point.transformFromRect(
			 this.stringRect.insetBy(1,0), string.bounds( font ), true, 
						move: ( left: (0@0.5), right: (1@0.5), center: (0.5@0.5), 
							middle: (0.5@0.5) )[ align ] ? (0@0.5) ) };
		if( exclude.includes(i) ) { i = i+1 };
		while { (i < string.size) && { 
			(stringRect.left + string[..i].bounds( font ).width) < point.x; }  }
			{ i = i+1; while { exclude.includes(i) } { i = i+1 };  
				};
		^i;
		}
		
	increment {arg mul=1; this.valueAction = this.value + (step*mul); }
	decrement {arg mul=1; this.valueAction = this.value - (step*mul); }
	
	*paletteExample { arg parent, bounds;
		var v;
		v = this.new(parent, bounds);
		v.value = 123.456;
		^v
	}
	
	mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
		if( enabled )
		{	
			hit = Point(x,y);
			mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);
			startHit = hit;
			if (scroll == true, { inc = this.getScale(modifiers) });			
			
		};
	}

	mouseMove { arg x, y, modifiers;
		var direction;
		var angle;
		if( enabled ) {
		
		if (scroll == true, {
			direction = 1.0;
				// horizontal or vertical scrolling:
			//if ( (x - hit.x) < 0 or: { (y - hit.y) > 0 }) { direction = -1.0; };
			inc = this.getScale( modifiers );
			angle = ((x@y) - hit).theta.wrap(-0.75pi, 1.25pi);
			//angle = angle = ((x@y) - startHit).theta.wrap(-0.75pi, 1.25pi);
			direction = 
				case { angle.inclusivelyBetween( -0.6pi, 0.1pi ) }
					{ 1.0 }
					{ angle.inclusivelyBetween( 0.4pi, 1.1pi )  }
					{ -1.0 }
					{ true }
					{ 0.0 };
			if( value.respondsTo( '+' ) && { value.class != String }  )
				{ this.valueAction = (this.value + (inc * this.scroll_step * direction)); };
			hit = Point(x, y);
		});
		mouseMoveAction.value(this, x, y, modifiers);
			
		};
	}
	
	mouseUp{  |x, y, modifiers, buttonNumber|
		if( enabled ) { inc=1 }; 
		mouseUpAction.value( this, x, y, modifiers, buttonNumber );
 }
	
	keyDown { arg char, modifiers, unicode, keycode, key;
		var zoom = this.getScale(modifiers);
		var arrowKey = unicode.getArrowKey ? key.getArrowKey;
		
		// standard chardown
		switch( arrowKey,
			\up, { this.increment(zoom); ^this },
			\right, { this.increment(zoom); ^this },
			\down, { this.decrement(zoom); ^this },
			\left, { this.decrement(zoom); ^this }
		);
		
		if ((char == 3.asAscii) || (char == $\r) || (char == $\n), { // enter key
			if (keyString.notNil,{ // no error on repeated enter
				value = interpretFunc.value(keyString) ? value;
				this.prClipValue;
				keyString = nil;
				action.value( this, value );
				stringColor = normalColor;
				this.refresh;
			});
			^this
		});
		if (char == 127.asAscii or: { unicode == 8 }, { // delete key
			keyString = nil;
			//this.value = object;
			stringColor = normalColor;
			this.refresh;
			
			^this
		});
		if (char.isDecDigit || allowedChars.includes(char), { 
		
			// simple expressions will be interpreted
			if (keyString.isNil, { 
				keyString = String.new;
				stringColor = typingColor;
			});
			keyString = keyString.add(char);
			this.refresh;
			//this.value = keyString.interpret;
			^this
		});
		^nil		// bubble if it's an invalid key
	}

	}
	
SmoothNumberBox : RoundNumberBox {
	
	init { |parent, bounds|
		super.init( parent, bounds );
		extrude = false;
		border = 0;
		background = Color.white.alpha_(0.5);
		typingColor = Color.red(0.5).alpha_(0.75);
		}
	
	}