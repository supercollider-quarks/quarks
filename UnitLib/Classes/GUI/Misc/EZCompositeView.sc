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

EZCompositeView : EZGui {
	
	classvar <>defaultBounds;
	
	*initClass {
		defaultBounds = 350@20;
	}
	
	*new { |parent, bounds, addFlowLayout = true, gap, margin|
		^super.new.init( parent, bounds, addFlowLayout, gap, margin );
	}
	
	init { |parentView, bounds, addFlowLayout, argGap, argMargin|
		
		var windowName;
		
		if( parentView.isString ) {
			windowName = parentView;
			parentView = nil;
		};
		
		this.prMakeMarginGap(parentView, argMargin, argGap);

		bounds.isNil.if{ bounds = defaultBounds };

		// if no parent, then pop up window
		# view,bounds = this.prMakeView( parentView,bounds);
		
		if( windowName.notNil ) {
			this.setWindowName( windowName );
		};
		
		if( addFlowLayout ) { this.addFlowLayout };
	}
	
	addFlowLayout { |argMargin, argGap|
		view.addFlowLayout( argMargin ? margin, argMargin? gap );
	}
	
	decorator { ^view.decorator }
	
	parent { ^view.parent }

	asView { ^view }
	
	add { |aView| view.add(aView) }
	
	resize { ^view.resize }
	resize_ { |resize| view.resize = resize }
	
	onClose { ^view.onClose }
	onClose_ { |func| view.onClose = func; }
}

+ EZGui {
	
	findWindow { ^view.getParents.last.findWindow }
	
	setWindowName { |name|
		this.findWindow.name = name ? "";
	}
	
	front { this.findWindow.front }
	isClosed { ^view.isClosed }
	
}