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

UndoView {
	
	var <object, <undoManager;
	var <view, <views, <ctrlFunc;
	
	*new { |parent, bounds, object, undoManager|
		^super.newCopyArgs( object )
			.addUndoManagerFromObject
			.makeView( parent, bounds, undoManager )
	}
	
	addUndoManagerFromObject { |obj|
		obj = obj ? object;
		if( obj.respondsTo( \undoManager ) ) {
			this.undoManager = obj.undoManager;
		};
	}
	
	undoManager_ { |newUM|
		ctrlFunc = ctrlFunc ?? { { this.updateViews }; };
		
		if( undoManager.notNil ) {
			undoManager.removeDependant( ctrlFunc );
		};
		
		undoManager = newUM;
		
		if( undoManager.notNil ) {
			undoManager.addDependant( ctrlFunc );
		};

	}
	
	updateViews {
		if( undoManager.notNil ) {
			views[0].enabled = (undoManager.current +1) <= (undoManager.history.size -1);
			views[1].enabled = undoManager.current > 0;
		} {
			views.do(_.enabled_(false));
		};
	}
	
	object_ { |newObj|
		object = newObj;
		this.addUndoManagerFromObject;
		this.updateViews;
	}
	
	makeView { |parent, bounds, inUM| 
		var height = 16;
		
		if( bounds.isNumber ) { height = bounds; bounds = nil };
		
		bounds = bounds ?? { ((height * 2) + 2) @ height };
	
		view = CompositeView( parent, bounds );
		view.addFlowLayout( 0@0, 2@2 );
		view.onClose_({ undoManager.removeDependant( ctrlFunc ); });
		
		bounds = view.drawBounds;
		
		if( object.respondsTo( \undoManager ) ) {
			inUM = inUM ? object.undoManager;
		};
		
		this.undoManager = inUM;
		
		views = 2.collect({ |i|
			SmoothButton( view, bounds.height @ bounds.height )
				.radius_(2)
				.border_(1)
				.canFocus_(false)
				.enabled_( false )
				.states_([ [ ['arrow_pi', 'arrow'][i] ] ])
				.action_({ 
					if( undoManager.notNil ) {
						object.handleUndo( undoManager.undo( [1,-1][i] ) );
					};
				});
		});
		
		this.undoManager.changed( \update );
	}
	
}