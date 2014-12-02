
CVSync {
	classvar <>all;
	var <>cv, <>view;
	
	*initClass {all = IdentityDictionary.new }
	
	*new { | cv, view | ^super.newCopyArgs(cv, view).init }


	init { 
		this.linkToCV;
		this.linkToView;
		this.update(cv, \synch);
	}

	linkToCV { 
		cv.addDependant(this); 		 	// when CV changes CVsync:update is called
	}
	
	linkToView {						
		view.action = this;			
		CVSync.all[view] = CVSync.all[view].add(this);
		view.onClose = CVSync				
	}
		
	update { | changer, what ...moreArgs |	// called when CV changes
		switch( what,
			\synch, { defer { view.value = cv.input }; }
		); 
	}
	
	value { cv.input = view.value }		// called when view changes

	*value { | view | 					// called onClose
		all[view].do(_.remove); all[view] = nil 
	}

	remove { cv.removeDependant(this) }
}

CVSyncInput : CVSync {
	update { | changer, what ...moreArgs |	// called when CV changes
		switch( what,
			\synch, { defer { view.value = cv.input }; }
		); 
	}
	
	value { cv.input = view.value }		// called when view changes
}

CVSyncValue : CVSync {				// used by NumberBox

	update { | changer, what ...moreArgs |
		switch( what,
			\synch, { defer { view.value = cv.value }; }
		); 
	}
	
	value { cv.value = view.value }

}

CVSyncMulti : CVSync {

	linkToView {
		view.thumbSize = (view.bounds.width - 16 /cv.value.size);
		view.xOffset = 0;
		view.valueThumbSize = 1;
		view.mouseUpAction = this;

		CVSync.all[view] = CVSync.all[view].add(this);
		view.onClose = CVSync;
	}	
}

// one view, many CV's.  
// CVSyncProperty links one CV to a property of a view
// CVSyncProperties links the view to its CV's

CVSyncProperty : CVSync {
	var <>property;
	
	*new { | cv, view, property | ^super.newCopyArgs(cv, view, property).init }

	update { | changer, what ...moreArgs |
		switch( what,
			\synch, { defer { view.setProperty(property, cv.input) }; }
		); 
	}
	
	value { cv.input = view.getProperty(property) }
	
	init { 
		this.linkToCV;
		this.update(cv, \synch);
	}

}


CVSyncProperties : CVSync {
	var <>links, <>view;
	
	*new { | cvs, view, properties | 
		^super.new(cvs, view)
			.view_(view)
			.links_(properties.collect { | p, i | CVSyncProperty( cvs[i], view, p) })
			.init
			
	}
	
 	init { 
		this.linkToView;
	}

	value { links.do(_.value) }
	remove { links.do(_.remove) }

}

CVSyncProps {
	var <>props;
	*new { | props | ^super.newCopyArgs(props) }
	new { | cv, view | ^CVSyncProperties(cv, view, props) }
}

	
SVSync : CVSyncValue {
	init { 
		this.update(cv, \items);
		super.init;
	}

	update { | changer, what ...moreArgs |
		switch( what,
			\synch, { defer { view.value = cv.value }; },
			\items, { defer { view.items = cv.items }; }
		); 
	}
	
}



EVSync : CVSync {
	
	linkToView {						
		view.action = this;			
		CVSync.all[view] = CVSync.all[view].add(this);
		view.onClose = CVSync				
	}
		
	update { | changer, what ...moreArgs |	// called when CV changes
		switch( what,
			\synch, { defer { cv.evToView(view) } }
		); 
	}
	
	value { cv.viewToEV(view) }		// called when view changes
		
}

ConductorSync : CVSync {
	
	linkToCV { 
		cv.player.addDependant(this); 		 	// when CV changes CVsync:update is called
	}
	
	linkToView {	
		view.action = this;			
		CVSync.all[view] = CVSync.all[view].add(this);
		view.onClose = CVSync;				
	}
		
	update { | changer, what ...moreArgs |	// called when CV changes
		switch( what,
			\synch, { defer { view.value = cv.player.value } }
		); 
	}
	
	value { |m,c,v| if (view.value == 0) { cv.stop } { cv.play } }		// called when view changes
		
}

/*
(
~connectDictionary = (
	numberBox:		CVSyncValue,
	slider:			CVSync,
	rangeSlider:		[CVSyncProperties, #[lo, hi]],
	slider2D:			[CVSyncProperties, #[x, y] ],
	multiSliderView:	CVSyncMulti,
	popUpMenu:		SVSync,
	listView:			SVSync,
	tabletSlider2D:	[CVSyncProperties, #[x, y] ],
	ezSlider:			CVSyncValue,
	ezNumber:			CVSyncValue
);

~viewDictionary = ();

GUI.schemes.do { | gui|			
#[ 	numberBox,  slider, rangeSlider, slider2D, multiSliderView, popUpMenu, listView, 
	tabletSlider2D, ezSlider, ezNumber].collect { | name |
		[name, gui.perform(name), ~connectDictionary.at(name)].postln;
//		~viewDictionary.put(gui.perform(name), ~connectDictionary.at(name))
	}
};

)

*/

