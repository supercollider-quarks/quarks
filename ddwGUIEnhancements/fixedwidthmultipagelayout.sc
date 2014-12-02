
// note: the window's width is not fixed, but the width of the master flowview
// inside the window is fixed

FixedWidthMultiPageLayout : MultiPageLayout {
	*new { arg title,bounds,margin,background,scroll=true,front=true;
		var	new;
		"FixedWidthMultiPageLayout is deprecated. Use ResizeHeightFlowWindow instead.".warn;
		new = ResizeHeightFlowWindow(title, bounds, true, true, scroll: true);
		if(front) { new.front };
		^new
	}

/*
	init { arg title,bounds,argmargin,argmetal=true;
		var w,v;
		bounds = if(bounds.notNil,{  bounds.asRect },{GUI.window.screenBounds.insetAll(10,20,0,25)});
		windows=windows.add
		(	
			w=GUI.window.new("< " ++ title.asString ++ " >",
						bounds, border: true )
				.onClose_({
					this.close; // close all windows in this layout
				})
		);
		metal = argmetal;
		if(metal.not,{
			w.view.background_(bgcolor);
		});
		isClosed = false;
		v =  FixedWidthFlowView( w );
		margin = argmargin;
		if(margin.notNil,{
			v.decorator.margin_(margin);
		});
		views = views.add(v );
		autoRemoves = [];
	}

	initon { arg parent,bounds,argmargin,argmetal=true;
		var r,v;
		windows = []; // first window not under my control
		v = FixedWidthFlowView(parent,bounds);
		margin = argmargin;
		if(margin.notNil,{
			v.decorator.margin_(margin);
		});
		views = [v];
		metal = argmetal;
		onBoundsExceeded = \warnError;
		autoRemoves = [];
	}
*/
}
