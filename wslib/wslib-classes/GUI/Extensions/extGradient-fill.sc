+ Color {
	alphaCopy_ { |alpha=1.0| 
		if( alpha == 1.0 ) { 
			^this;
		} { 
			^this.copy.alpha_(alpha * this.alpha); 
		};
	}
}

+ Gradient {
	fill { |rect|
		if( direction == \h )
			{ ^Pen.fillAxialGradient( rect.left@rect.center.y, rect.right@rect.center.y,
				color1, color2 ); }
			{ ^Pen.fillAxialGradient( rect.center.x@rect.top, rect.center.x@rect.bottom,
				color1, color2 ); };
		}
		
	penFill { |rect, alpha=1.0|
		if( direction == \h )
			{ ^Pen.fillAxialGradient( rect.left@rect.center.y, rect.right@rect.center.y,
				color1.alphaCopy_(alpha), color2.alphaCopy_(alpha) ); }
			{ ^Pen.fillAxialGradient( rect.center.x@rect.top, rect.center.x@rect.bottom,
				color1.alphaCopy_(alpha), color2.alphaCopy_(alpha) ); };
		}
	}
	
+ Color {
	fill { Pen.fillColor = this; ^Pen.fill; }
	
	penFill { |rect, alpha=1.0| 
		Pen.fillColor = this.alphaCopy_( alpha ); 
		^Pen.fill; 
	}
}
	
+ Function {
	penFill { |rect, alpha=1.0| 
		Pen.use({ 
			Pen.clip; 
			this.value( rect, alpha );
		}) 
	}
}

+ Symbol {
	penFill { |rect|
		Pen.use({ 
			Pen.clip; 
			DrawIcon.symbolArgs( this, rect );
		}) 
	}
}