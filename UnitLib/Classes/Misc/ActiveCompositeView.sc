ActiveCompositeView {

	var <view, <topView, <uview;
	var <>action;
	var <>mouseUpAction, <>mouseDownAction, <>mouseMoveAction;
	var <>canReceiveDragHandler, <>receiveDragHandler;
	
	*new { |parent, bounds|
		^super.new.init( parent, bounds );
		}
		
	init { |parent, bounds|
		view = CompositeView( parent, bounds );
		uview = UserView( view, view.bounds.moveTo(0,0) ).enabled_( true );
		uview.mouseUpAction_({ |vw ...args|
			mouseUpAction.value( this, *args ); action.value( this, *args ); });
		uview.mouseDownAction_({ |vw ...args| mouseDownAction.value( this, *args ); });
		uview.mouseMoveAction_({ |vw ...args| mouseMoveAction.value( this, *args ); });
		uview.canReceiveDragHandler_({ canReceiveDragHandler.value });
		uview.receiveDragHandler_({ receiveDragHandler.value });
		topView = CompositeView( view, view.bounds.moveTo(0,0) );
		}

	
	background_ { |color| view.background = color }
	background { ^view.background }
	
	bounds { ^view.bounds }
	bounds_ { |rect|
		rect = rect.asRect;
		view.bounds = rect;
		uview.bounds = view.bounds.moveTo(0,0);
		topView.bounds = view.bounds.moveTo(0,0);
		}
		
	add { |child| topView.add( child ); }
	asView { ^topView }
	mouseUp { |x,y, mod| uview.mouseUp( this, x, y, mod ); }
	mouseDown { |x,y, mod| uview.mouseDown( this, x, y, mod ); }
	mouseMove { |x,y, mod| uview.mouseMove( this, x, y, mod ); }
	
	doesNotUnderstand { |selector ... args|
		var res;
		res = topView.perform( selector, *args );
		if( res == topView )
			{ ^this }
			{ ^res };
		}
	
	
	
	}