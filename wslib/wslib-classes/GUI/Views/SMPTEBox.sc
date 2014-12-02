// wslib 2010

SMPTEBox : RoundNumberBox {
	var <fps = 1000, scaleValues;
	
	init { |parent, bounds|
		super.init( parent, bounds );
		clipLo = -60;
		clipHi = (60*60*99);
		allowedChars = "";
		
		this.setScaleValues;
		
		formatFunc = { |value| 
			SMPTE.global.fps_(fps).newSeconds( value ).toString 
		};
		
		interpretFunc = { |string| 
			SMPTE.global.fps_(fps).string_( string ).asSeconds 
		};
	}
	
	fps_ { |newFps = 1000| fps = newFps; this.setScaleValues; this.updateStepSize; this.refresh }
	
	setScaleValues { 
		scaleValues = [60*60*10, 60*60, 0, 60*10, 60, 0, 10, 1, 0 ] ++ 
			(10**(fps.log10.floor - 1..0)/fps);
	}
	
	updateStepSize { 
		scroll_step = scaleValues[charSelectIndex] ? 1;
		step = scroll_step;
	}
	
	charSelect { |i, forward = true| this.prCharSelect( i, forward ); this.refresh; }
	charSelectAmt { |amt = 1| this.prCharSelectAmt( amt ); this.refresh; }
	
	prCharSelect { |i, forward = true|
		charSelectIndex = i.wrap(-1, string.size-1);
		if( [2,5,8].includes( charSelectIndex ) ) 
		 	{ charSelectIndex = charSelectIndex + [-1,1][ forward.binaryValue ] };
		 this.updateStepSize;
	}
	
	prCharSelectAmt { |amt = 1|
		this.charSelect( charSelectIndex + amt, amt > 0 );
	}
	

	mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
		if( enabled )
		{	
			hit = Point(x,y);
			startHit = hit;
			if (scroll == true, { inc = this.getScale(modifiers) });
			
			// additions for SMPTE (rest copied from super)
			charSelectIndex = this.charIndexFromPoint( x@y, [2,5,8] );
			scroll_step = scaleValues[charSelectIndex] ? 1;
			step = scroll_step;
						
			mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);
		};
	}
	
	keyDown { arg char, modifiers, unicode, keycode, key;
		var zoom = this.getScale(modifiers);
		var arrowKey = unicode.getArrowKey ? key.getArrowKey;
		
		// standard chardown
		switch( arrowKey,
			\up, { this.increment(zoom); ^this },	
			\down, { this.decrement(zoom); ^this },
			\right, { this.prCharSelectAmt( 1 ); ^this },
			\left, { this.prCharSelectAmt( -1); ^this }
		);
				
		if ( [ $., $:, $ ].includes( char ), {
			charSelectIndex = [3,6,9].detect({ |item| 
				item > charSelectIndex; 
			}) ? charSelectIndex;
			^this.updateStepSize;
		});
		
		if ( char.isDecDigit, {
			if( charSelectIndex == -1 ) { this.prCharSelect( 0 ); };
			value = SMPTE.global.fps_(fps)
				.string_( string.put( charSelectIndex, char ) )
				.asSeconds;
			 this.prCharSelectAmt( 1 );
			 if( charSelectIndex == -1 ) { this.doAction };
			 ^this
		 }); 
		
		if ((char == 3.asAscii) || (char == $\r) || (char == $\n), { // enter key
			this.charSelect( -1 ); this.doAction;
			^this
		});
		
		^nil		// bubble if it's an invalid key
	}
	
	// bw compat with SMPTEView
	
	pos { ^value }
	pos_ { |val| this.value = val; }
	posD_ { |val| this.value = val;  }
	selected { ^charSelectIndex }
	selected_ { |index| this.charSelect( index ) }
	smpte { |inFps| ^SMPTE( value, inFps ? fps ) }
	
	string_ { |string, inFps| this.value = string.asSeconds( inFps ? fps ); }
	smpte_ { |smpte| this.value = smpte.asSMPTE.asSeconds; }
	
	fontName { ^font.name }
	fontSize { ^font.size }
	fontColor { ^normalColor }

	fontName_ { |name| font.name = name ?? { Font.defaultSansFace }; this.refresh; }
	fontSize_ { |size| font.size = size ? font.size; this.refresh; }
	fontColor_ { |color| this.normalColor = color }
	
	applySmoothSkin {
		this.applySkin( ( 
			extrude: false,
			border: 0,
			background: Color.white.alpha_(0.5),
			typingColor: Color.red(0.5).alpha_(0.75)
		) );
	}
	
	applyRoundSkin {
		this.applySkin( ( 	
			extrude: true,
			border: 2,
			background: Color.white,
			typingColor: Color.red
		) );
	}
	
}