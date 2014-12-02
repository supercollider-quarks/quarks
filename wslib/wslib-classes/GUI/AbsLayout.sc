AbsLayout { 
	
	// hack to prevent a lot of work on old code due to deprication of relativeOrigin
	// for CompositeViews
	
	// use on a view that used to have relativeOrigin == true
	
	/*
	composite.decorator = AbsLayout( composite );
	*/
	
	var <absoluteBounds;
	
	*new { arg parent; 
		this.deprecated( thisMethod );
		^super.newCopyArgs(parent.absoluteBounds);
	}
	
	place { arg view;
		var bounds;
		bounds = view.bounds;
		view.bounds = Rect(
			bounds.left - absoluteBounds.left, bounds.top - absoluteBounds.top, 
			bounds.width, bounds.height);
	}
	
	absoluteBounds_ { |absBounds| absoluteBounds = absBounds; }
	
	// can't change, just compat
	bounds_ { } 
	bounds { ^absoluteBounds }
	reset { }
	
}



+ Object { // should kick in as soon as relativeOrigin_ is removed
	
	relativeOrigin_ { |bool|
		this.deprecated( thisMethod );
		if( this.respondsTo( \decorator_ ) ) // both swingosc and osx
			{ if( this.decorator.class != FlowLayout ) // prevent flowlayout removal
				{ if( bool == false )
					{ this.decorator = AbsLayout( this ); }
					{ /* this.decorator = nil; */ } // remove if true
				};
			};
	}
	
}
