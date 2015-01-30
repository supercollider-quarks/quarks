/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

FilePathView {
	
	var <value;
	var <views, <view;
	var <viewHeight = 14;
	var <>action;
	
	*new { |parent, bounds, filePath|
		^super.new.makeView( parent, bounds );
	}
	
	*viewNumLines { ^2 }
	
	setViews { |inPath|
		var dirname, basename;
		if( inPath.notNil ) {
			basename = inPath.asString.basename;
			dirname = inPath.asString.dirname;
		};
		
		{
				views[ \basename ].string = basename ? "";
				views[ \dirname ].string = dirname ? "";
		}.defer;		
	}
	
	value_ { |inPath|
		value = inPath;
		this.setViews( value );
	}
	
	font_ { |font|
		this.setFont( font );
	}
	
	setFont { |font|
		font = font ??
			{ RoundView.skin !? { RoundView.skin.font } } ?? 
			{ Font( Font.defaultSansFace, 10 ) };
		
		{
			views[ \basename ].font = font;
			views[ \dirname ].font = font;
		}.defer;
	}
	
	stringColor_ { |color|
		this.setStringColor( color );
	}
	
	setStringColor { |color|
		{ 
			views[ \basename ].stringColor = color;
			views[ \dirname ].stringColor = color;
		}.defer;
	}
	
	resize_ { |resize|
		view.resize = resize ? 5;
	}
	
	doAction { action.value( this ) }
	
	makeView { |parent, bounds, resize|
		
		bounds = bounds ?? { 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
		
		view = EZCompositeView( parent, bounds, gap: 4@4 );
		view.resize_( resize ? 5 );
		views = ();
		bounds = view.view.bounds;
		
		views[ \basename ] = TextField( view, (bounds.width - (viewHeight + 4)) @ viewHeight )
			.applySkin( RoundView.skin )
			.resize_( 2 )
			.canReceiveDragHandler_({ |vw| View.currentDrag.class == String })
			.action_({ |tf|
				if( tf.string.size > 0 ) {
					value = value.asString.dirname +/+ tf.string;
				} {
					value = nil;
				};
				action.value( this );	
			});
			
		views[ \browse ] = SmoothButton( view, viewHeight @ viewHeight )
			.radius_( 0 )
			.border_(0)
			.resize_( 3 )
			.label_( 'folder' )
			.action_({
				Dialog.getPaths( { |paths|
				  this.value = paths[0];
				  action.value( this );
				});
			});
			
		views[ \dirname ] = TextField( view, bounds.width @ viewHeight )
			.applySkin( RoundView.skin )
			.canReceiveDragHandler_({ |vw| View.currentDrag.class == String })
			.resize_( 2 )
			.action_({ |tf|
				if( value.notNil ) {
					value = tf.string +/+ value.asString.basename;
				};
				action.value( this );
			});
			
		this.setFont;
	}
	
}