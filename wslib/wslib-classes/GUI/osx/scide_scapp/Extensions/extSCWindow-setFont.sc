// wslib 2006

// set all fonts of a SCWindow at once

+ SCWindow {
	
	setFont { |font|
		font = (font ? Font.default) ? Font( "Helvetica", 12);
		view.children.do({|view|
			 if( view.respondsTo( \font_ ) ) { view.font_( font ) };
			 if( view.respondsTo( \setFont ) ) { view.setFont( font ) }; });
		}
	
	}
	
+ SCContainerView {

	setFont { |font|
		font = (font ? Font.default) ? Font( "Helvetica", 12);
		children.do({|view|
			 if( view.respondsTo( \font_ ) ) { view.font_( font ) };
			 if( view.respondsTo( \setFont ) ) { view.setFont( font ) }; });
		}
	}
	