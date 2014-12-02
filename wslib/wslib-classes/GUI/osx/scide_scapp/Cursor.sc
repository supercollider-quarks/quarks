Cursor {
	classvar <>all;
	
	var <image, <center, <bounds, <nsobject;
	
	*initClass { all = (); }
	*new { |image, center, bounds|
		if( all[ image ].notNil && { image.asSymbol != 'current' } )
			{ ^all[ image ] }
			{ ^super.newCopyArgs( image, center, bounds ).init; };
	} 
	
	*builtInCursors {^[	
			'arrow', 
			'IBeam', 
			'crosshair', 
			'closedHand', 
			'openHand', 
			'pointingHand', 
			'resizeLeft', 
			'resizeRight', 
			'resizeLeftRight', 
			'resizeUp', 
			'resizeDown', 
			'resizeUpDown', 
			'disappearingItem',
			'current'  // special case
			];
	}
	
	init { 
		var nsimage, tempImage;
		case { image.class == SCImage }
			{ this.initSCImage( image ); }
			{ this.class.builtInCursors.includes( image.asSymbol ); }
			{ this.initBuiltIn( image ); }
			{ image.isFunction }
			{ bounds = bounds ? (10@10);
			  tempImage = SCImage( bounds );
			  tempImage.draw({ image.value( bounds ); });
			  this.initSCImage( tempImage ); }
			{ (image.class == String) && { (tempImage = SCImage( image )).notNil } }
			{ this.initSCImage( tempImage ); tempImage.free; };
			
		if( nsobject.notNil ) 
			{ all[ image ] = this; }
			{ "Cursor: invalid input; default (arrow) cursor used".warn; 
			  all[ image ] = this.initBuiltIn( 'arrow' );
			 };
	}
	
	initBuiltIn { |inImage|
		nsobject = SCNSObject("NSCursor", inImage.asString ++ "Cursor" );
		}
		 
	initSCImage { |inImage|
			var nsimage;
		 	nsimage = inImage.asNSObject.invoke( "nsimage" );
			center = center ?? { inImage.bounds.center };
			bounds = inImage.bounds;
			nsobject = SCNSObject("NSCursor", "initWithImage:hotSpot:", [nsimage, center]);
		 }
		 
	push { nsobject.invoke( "push" ); }
	pop { nsobject.invoke( "pop" ); }
	set { nsobject.invoke( "set" ); }
	
	setOnMouseEntered { |bool = true|	
		nsobject.invoke( "setOnMouseEntered:", [ bool ]);
	}
	
	setOnMouseExited { |bool = true|	
		nsobject.invoke( "setOnMouseExited:", [ bool ]);
	}
	
	mouseEntered { |bool = true|
		nsobject.invoke( "mouseEntered:", [ bool ]);
	}
	
	mouseExited { |bool = true|
		nsobject.invoke( "mouseExited:", [ bool ]);
	}
	
	
	
}