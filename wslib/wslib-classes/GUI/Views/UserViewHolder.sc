/**
 *  Sciss 2009 / WS 2009
 *  part of wslib (temporarily?)
 *
 *	A base class for UserView subclasses. Subclassing UserViewBase avoid the
 *	problem of two concurrent superclasses, SCUserView and JSCUserView, by
 *	holding a UserView internally. This view is created upon instantiation
 *	depending on the current GUI scheme.
 *
 *	Any unknown calls are automatically forwarded to it, and event hooks are
 *	patched to consistently provide the base view as this first argument when
 *	invoking the hooks.
 *
 *	@version	0.10, 14-Jul-09
 */
UserViewHolder : ViewHolder2 { 
	// redirected hooks ((J)SCUserView)
	var <>drawFunc;

	// the GUI scheme's pen (for convenience)
	var pen;
		
	*new { arg ... args;
		^super.new.initHolder( *args );
	}
	
	initHolder { arg ... args;
		var result, context;
		
		context		= GUI.current;
		this.view		= context.userView.new( *args );
		pen			= context.pen;
		
		result		= this.init( *args );
		
		// install this at last, as (in the case of SwingOSC) the drawFunc
		// might be called immediately if the view is already visible,
		// and it could rely on data prepared in the init method.
		view.drawFunc	= { GUI.use( context, { this.draw; drawFunc.( this )})};
		if( GUI.id == \qt ) {
			view.focusLostAction_({ |vw| vw.refresh });
			view.focusGainedAction_({ |vw| vw.refresh });
		};
		^result;
	}
	
	// ------- methods that can be overridden by subclasses if necessary -------
	
	init { } // this is called after instantiation; put any initialization code here
	
	// the draw method gets called inside a GUI context switch,
	// so you may safely use Pen. however, there is a convenient
	// pen field that will also be slightly more efficient.
	// a user supplied drawFunc will be executed _after_ the draw call
	// (so a user could paint on top).
	draw { }
}
