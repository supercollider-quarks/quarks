// wslib 2006

LineView : UserViewHolder {
	
	// just a simple line..
	// use as border or separator
	
	var <orientation = \h, <color, <width, <margin = 0;
	
	init { |parent,bounds|
		bounds = bounds.asRect;
		if( bounds.width < bounds.height )
			{ orientation = \v };
		if( orientation == \h ) { 
			width = bounds.height;
			this.bounds = this.bounds.width_( parent.bounds.width - (margin * 2) );
		} { 
			width = bounds.width;
		};
		color = Color.gray(0.25).alpha_(0.5);
		this.canFocus_( false );		
	}
	
	draw {
		var realMargin = 0, bounds;
		Pen.color_( color );
		Pen.width_( width );
		//realMargin = margin * width;
		bounds = this.drawBounds;
		switch ( orientation, 
			\h, { Pen.line( 
					(bounds.left + realMargin) @ (bounds.center.y),
					(bounds.right - realMargin) @ (bounds.center.y)
				);
			}, 
			\v, { Pen.line( 
					(bounds.center.x) @ (bounds.top + realMargin),
					(bounds.center.x) @ (bounds.bottom - realMargin)
				); 
			});
			 
		Pen.stroke;
	}	
	
	full { DeprecatedError( this, thisMethod, nil, this.class ).throw; ^true }
	full_ { DeprecatedError( this, thisMethod, nil, this.class ).throw; }
	
	color_ { |newColor| color = newColor; this.refresh; }
	orientation_ { |newOrientation| orientation = newOrientation ? orientation; this.refresh; }
	width_ { |newWidth| width = newWidth ? { 
		if( orientation == \h )
			{ this.bounds.height }
			{ this.bounds.width }; }; 
		this.refresh;
		}
	margin_ { |newMargin| 
		margin = newMargin; 
		this.bounds = this.bounds.width_( view.parent.bounds.width - (margin * 2) );
		this.refresh; 
	}
		
	alpha { ^color.alpha }
	alpha_ { |newAlpha| color.alpha_( newAlpha ); this.refresh; }
	
}
