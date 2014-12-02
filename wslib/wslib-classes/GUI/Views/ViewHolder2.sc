
// SCViewHolder makes it possible to add more capabilities by holding an SCView, not subclassing it

// Based on Sciss SCViewHolder
// as long as that isn't in the main distro, this copy will be used

ViewHolder2 {
	classvar <>consumeKeyDowns = false;	// should the view by default consume keydowns
	classvar <>dontRefresh = false;

	// redirected hooks ((J)SCView)
	var <>action, <>mouseDownAction, <>mouseUpAction, <>mouseOverAction, <>mouseMoveAction;
	var <keyDownAction, <keyUpAction, <keyModifiersChangedAction;
	var <beginDragAction,<canReceiveDragHandler,<receiveDragHandler;
	var <>onClose;
	var <>focusGainedAction, <>focusLostAction;
	
	// the user view, the GUI scheme
	var <view;
	
	view_ { arg v;
		// subclasses need to ALWAYS use this method to set the view
		view = v;
		if( view.isNil, { ^this });

		view	// install double behaviour
			.action_({          arg view ... rest; action.( this, *rest )})
			.mouseDownAction_({ arg view ... rest; this.mouseDown( *rest ); })
			.mouseUpAction_({   arg view ... rest; this.mouseUp( *rest ); })
			.mouseOverAction_({ arg view ... rest; this.mouseOver( *rest ); })
			.mouseMoveAction_({ arg view ... rest; this.mouseMove( *rest ); })
			.onClose_({         arg view ... rest; this.viewDidClose; protect { onClose.( this, *rest )} { this.view = nil }})
			;
		if( view.respondsTo( 'focusGainedAction_' ), {    // currently no support in cocoa
			view.focusGainedAction = { arg view ... rest; this.focusGained( *rest ); focusGainedAction.( this, *rest )};
		});
		if( view.respondsTo( 'focusLostAction_' ), {      // currently no support in cocoa
			view.focusLostAction   = { arg view ... rest; this.focusLost( *rest );   focusLostAction.( this, *rest )};
		});
		this	// install default hooks
			.keyDownAction_( nil )
			.keyUpAction_( nil )
			.keyModifiersChangedAction_( nil )
			.beginDragAction_( nil )
			.canReceiveDragHandler_( nil )
			.receiveDragHandler_( nil )
			;
	}
		
	keyDownResponder { ^nil }
	enableKeyDowns { this.keyDownAction = this.keyDownResponder }
	asView { ^view }

	isClosed { ^(view.isNil or: {view.isClosed}) }
	
	// ---- smart hook registration ----
	
	keyDownAction_ { arg func;
		keyDownAction = func;
		if( func.notNil, {
			view.keyDownAction = { arg view ... rest; keyDownAction.( this, *rest )};
		}, {
			view.keyDownAction = { arg view ... rest; this.keyDown( *rest )};
		});
	}

	keyUpAction_ { arg func;
		keyUpAction = func;
		if( func.notNil, {
			view.keyUpAction = { arg view ... rest; keyUpAction.( this, *rest )};
		}, {
			view.keyUpAction = { arg view ... rest; this.keyUp( *rest )};
		});
	}

	keyModifiersChangedAction_ { arg func;
		keyModifiersChangedAction = func;
		if( func.notNil, {
			view.keyUpAction = { arg view ... rest; keyModifiersChangedAction.( this, *rest )};
		}, {
			view.keyUpAction = { arg view ... rest; this.keyModifiersChanged( *rest )};
		});
	}

	beginDragAction_ { arg func;
		beginDragAction = func;
		if( func.notNil, {
			view.beginDragAction = { arg view ... rest; beginDragAction.( this, *rest )};
		}, {
			view.beginDragAction = { arg view ... rest; this.getDrag( *rest ) };
			//iew.beginDragAction = nil;
		});
	}

	canReceiveDragHandler_ { arg func;
		canReceiveDragHandler = func;
		if( func.notNil, {
			view.canReceiveDragHandler = { arg view ... rest; canReceiveDragHandler.( this, *rest )};
		}, {
			view.canReceiveDragHandler = { arg view ... rest; this.canReceiveDrag( *rest )};
		});
	}

	receiveDragHandler_ { arg func;
		receiveDragHandler = func;
		if( func.notNil, {
			view.receiveDragHandler = { arg view ... rest; receiveDragHandler.( this, *rest )};
		}, {
			view.receiveDragHandler = { arg view ... rest; this.receiveDrag( *rest )};
		});
	}

	// ------- utility methods for subclasses -------
	
	currentDrag { ^view.scheme.view.currentDrag }
	currentDragString { ^view.scheme.view.currentDragString }
	
	// ------- methods that can be overridden by subclasses if necessary -------
	
	init { } // this is called after instantiation; put any initialization code here
	
	// mouse control. these methods get called before user registered actions
	mouseDown { |...rest| mouseDownAction.( this, *rest ) }
	mouseUp { |...rest| mouseUpAction.( this, *rest ) }
	mouseOver { |...rest| mouseOverAction.( this, *rest ) }
	mouseMove { |...rest| mouseMoveAction.( this, *rest ) }
	
	// for the three key methods, returning nil will bubble up events
	keyDown { ^nil }				// corresponds to defaultKeyDownAction
	keyUp { ^nil }				// corresponds to defaultKeyUpAction
	keyModifiersChanged { ^nil }	// like a defaultKeyModifiersChangedAction

	// note: currently these are not called in cocoa
	focusGained { }
	focusLost { }
	
	// to access (J)SCView.currentDrag and currentDragString,
	// please call this.currentDrag and this.currentDragString
	// since they will automatically use the right GUI scheme!
	getDrag { ^nil }				// corresponds to defaultGetDrag
	canReceiveDrag { ^false }		// corresponds to defaultCanReceiveDrag
	receiveDrag { } 				// corresponds to defaultReceiveDrag
	
	viewDidClose { }				// corresponds to onClose
	
	// ---- method forwarding ----
	
	refresh { if( dontRefresh !== true ) { view.refresh } }

	// forwards any unknown calls to the view
	doesNotUnderstand { arg ... args;
		var result = view.perform( *args );
		^if( result === view, { this }, { result }); // be sure to replace view with base
	}
	
	respondsTo { arg ... args;
		^if( super.respondsTo( *args ), true, { view.respondsTo( *args )});
	}
}
