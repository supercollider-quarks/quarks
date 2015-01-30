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

ObjectView {
	
	classvar <viewHeight = 14;
	
	var <object, <key, <spec, <parent, <composite, <views; 
	var <>action, <>testValue;
		// views can be anything; i.e. the output of the makeFunc in the def
	
	*new { |parent, bounds, object, key, spec, controller, label|
		^super.newCopyArgs( object, key, spec ).init( parent, bounds, controller, label);	}
		
	init { |inParent, bounds, controller, label|
		var margin = 0, value;
		parent = inParent;
		if( parent.isNil ) { parent = Window( "%:%".format(object) ).front.decorate };
		
		if( bounds.isNil ) { 
			if( parent.asView.decorator.notNil )
				{ margin = parent.asView.decorator.margin.x; };
			bounds = parent.bounds.width - (2 * margin);
		};
		
		spec = (spec ?? {
			spec = (key.asSpec ? [0,1]).asSpec;
		}).asSpec;
		
		// spec = spec.adaptFromObject( object.perform( key ) );
		
		if( bounds.isNumber ) { bounds = 
			(bounds @ ((spec.viewNumLines * viewHeight) + ((spec.viewNumLines-1) * 4))) 
		};
		this.makeView( bounds, controller, label );
	}
	
	makeView { | bounds, controller, label |
		var createdController = false, setter;
		
		controller = controller ?? { 
			createdController = true; 
			SimpleController( object ); 
		};
		
		composite = CompositeView( parent, bounds ).resize_(2);
		composite.onClose = { 
			controller.put( key, nil );
			if( createdController ) { controller.remove };
		};
		
		setter = key.asSetter;
		
		views = spec.makeView( composite, bounds.asRect.moveTo(0,0), label ? key, 
				{ |vw, value| 
					object.perform( key.asSetter, value ); 
					action.value( this, value ); 
				}, 5 );
			
		this.update;
		
		controller.put( key, { |obj, key, value| 
			if( testValue.isNil or: { testValue.(value) } ) {
				spec.setView( views, value, false );
			};
		});
	}
	
	update {
		spec.setView( views, object.perform( key ) ); 
	}
	
	remove { composite.remove }
	
	resize_ { |val| composite.resize_(val); }
	
}