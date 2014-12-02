RoundView : UserViewHolder {

	// fix for drawing slightly outside an SCUserView
	// this class doesn't draw the focusring itself,
	// it only handles the resizing.
	
	classvar <>focusRingSize = 3;
	classvar <>skins;
	
	var <expanded = true;
	var <shrinkForFocusRing = false; // only when expanded == false
	
	var <enabled = true, couldFocus = true;
	var <focusColor;
	
	var <backgroundImage;
	
	*new { arg parent, bounds ... rest;
		^super.new( parent, bounds, *rest ).initRoundView( parent, bounds );
	}
	
	*prShouldExpand { arg v;
		^( [ \cocoa ].includes( GUI.id ) and: { v.isKindOf( GUI.hLayoutView ).not && 
			{ v.isKindOf( GUI.vLayoutView ).not }  } );
	}
	
	initRoundView { |parent, bounds|
		if( this.class.prShouldExpand( parent ).not, { expanded = false });
		if( expanded ) { this.bounds = view.bounds }; // resize after FlowLayout
		view.focusColor = Color.clear;
		if( skins.size > 0 ) { this.applySkin( skins.last ) };
	}
	
	*useWithSkin { |skin, function|
		this.pushSkin( skin );
		function.value;
		this.popSkin;
	}
	
	*pushSkin { |skin|
		this.skins = this.skins.add( skin );
	}
	
	*popSkin {
		this.skins.pop;
	}
	
	*skin_ { |skin|
		if( skin.isNil )
			{ skins = [] } // remove all skins
			{ if( skin != (skins ? []).last ) // only push if really new
				{ this.pushSkin( skin ) } 
			};
	}
	
	*skin { ^(skins ? []).last }
	
	drawBounds { ^if( expanded ) 
			{ this.bounds.moveTo(focusRingSize,focusRingSize); } 
			{ if( shrinkForFocusRing )
				{ this.bounds.insetBy(focusRingSize,focusRingSize)
						.moveTo(focusRingSize,focusRingSize) }
				{ this.bounds.moveTo(0,0); }; 
			}
		}
			
	bounds { ^if( expanded ) 
			{ view.bounds.insetBy(focusRingSize,focusRingSize); } 
			{ view.bounds; }; 
		}
	
	bounds_ { |newBounds| 
		if( expanded ) 
			{ view.bounds = newBounds.asRect.insetBy(focusRingSize.neg,focusRingSize.neg); }
			{ view.bounds = newBounds; } ;
		}
		
	expanded_ { |bool|
		var bnds;
		bnds = this.bounds;
		expanded = bool ? expanded;
		this.bounds = bnds;
		}
		
	shrinkForFocusRing_ { |bool|
		shrinkForFocusRing = bool ? shrinkForFocusRing;
		this.refresh;
		}
		
	canFocus_ { |bool|
		if( enabled ) { super.canFocus = bool };
		couldFocus = bool;
		}
		
	enabled_ { |bool|
		enabled = (bool != false); // can be anything
		if( enabled == true )
			{ super.canFocus = couldFocus; } 
			{ super.canFocus = false; };
		this.refresh;
		}
		
	backgroundImage_ { arg image, tileMode=1, alpha=1.0, fromRect;
		if( image.notNil )
			{ backgroundImage = [ image, tileMode, alpha, fromRect ]; }
			{ backgroundImage = nil };
		this.refresh;
		}
	
	drawFocusRing { |rect, radius|
		if( this.hasFocus ) {
			if( GUI.id === \qt ) {
				Pen.use({
					Pen.width = 1.5;
					Pen.color = Color.white.alpha_(0.75);
					Pen.roundedRect( rect.insetBy(1,1), radius-1 ).stroke;
					Pen.color = Color.blue.hue_(0.7).alpha_(0.5);
					Pen.roundedRect( rect.insetBy(0.5,0.5), radius-0.5 ).stroke
				});
			} {
				Pen.use({
					Pen.color = focusColor ?? { Color.gray(0.2).alpha_(0.8) };
					Pen.width = 2;
					Pen.roundedRect( rect.insetBy(-2,-2), radius + 1 );
					Pen.stroke;
				});
			};
		};
	}
}
	
RoundView2 : RoundView { } // still here for backwards compat

+ Object { // make skinning work for any object
	applySkin { |skin|
		var classSpecific;
		skin.pairsDo({ |key, value|
				if( key.isClassName ) { 
					if( this.class.name == key ) {
						classSpecific = value;	
					};
				} {
					key = key.asSetter;
					if( this.respondsTo( key ) ) { this.perform( key, value ); };
				};
			});
		classSpecific !? { this.applySkin( classSpecific ) };
	}
}

+ SimpleNumber {
	getArrowKey {
		// use like this:
		// unicode.getArrowKey ? key.getArrowKey;
		^if( GUI.id == \qt ) {
			switch( this.asInt,
				16r1000012, \left,
				16r1000013, \up,
				16r1000014, \right,
				16r1000015, \down
			);		
		} {
			switch( this.asInt,
				16rF700, \up,
				16rF701, \down,
				16rF702, \left,
				16rF703, \right
			);
		};
	}
}

+ Nil {
	getArrowKey { }
}
 