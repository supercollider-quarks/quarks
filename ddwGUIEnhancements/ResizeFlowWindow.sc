
ResizeFlowWindow {
	classvar	windowMethods;
	classvar	<>debug = false;
	var	<window, <view, <>onClose;
	
	*initClass {
		StartUp.add({
			windowMethods = #[acceptsClickThrough, acceptsClickThrough_, acceptsMouseOver,
				acceptsMouseOver_, alpha_, alwaysOnTop, alwaysOnTop_, /*asPageLayout,*/ bounds,
				bounds_, callDrawHook, closed, dataptr, didBecomeKey, didResignKey,
				drawHook, drawHook_, editable, endFrontAction, endFrontAction_, endFullScreen,
				front, fullScreen, isClosed, minimize, name, name_, play,
				refresh, setInnerExtent, storeArgs, toFrontAction, toFrontAction_, unminimize,
				userCanClose, userCanClose_, view, visible_
			];
		})
	}
	
	*new { |title, bounds, resizable = true, border = true, server, scroll = true|
		^super.new.init(title, bounds, resizable, border, server, scroll)
	}
	
	init { |title, bounds, resizable, border, server, scroll|
		bounds  ?? { bounds = GUI.window.screenBounds };
		window = GUI.window.new(title, bounds, resizable, border, server, scroll)
			.onClose_({ this.close });
		view = this.class.viewClass.new(window.asView, bounds.moveTo(0, 0), margin: 2@2);
	}
	
	doesNotUnderstand { |selector ...args|
		var	result, check;
if(debug) { [selector, args].debug(">> " ++ this.class.name ++ ":doesNotUnderstand") };
		if(windowMethods.includes(selector)) {
			result = window.performList(selector, args);
			check = window;
		} {
			result = view.performList(selector, args);
			check = view;
		};
if(debug) { [result, check].debug("<< " ++ this.class.name ++ ":doesNotUnderstand") };
		^if(result === check) { this } { result };
	}

	recursiveResize {
		if(window.isClosed.not) {
			view.recursiveResize;
			this.synchWindowBounds;
		}
	}
	
		// it's bad if this gets passed thru to the view
	resizeToFit {
		this.recursiveResize;
	}
	
		// match window bounds to flowview bounds
	synchWindowBounds {
		window.bounds = view.bounds.resizeBy(15, 15).moveTo(window.bounds.left, window.bounds.top)
	}

	*viewClass { ^FlowView }
	
	asFlowView { ^view }
	asView { ^view.asView }
	
	close {
		onClose.value;
		view.remove;
		this.prClose;
	}
	
		// use prClose when there could be infinite recursion with view.remove as in close
	prClose {
		if(window.isClosed.not) { window.close };
	}
}

ResizeHeightFlowWindow : ResizeFlowWindow {
	*viewClass { ^FixedWidthFlowView }
}
