
HJHObjectGui : ObjectGui {
		// like felix's but it saves the layout that was passed in
	var	<masterLayout, <layout, <iMadeMasterLayout = false,
		<argBounds;

	*initClass {
		StartUp.add {
			GUI.skins.put(\dewdrop,
				(
					fontSpecs: 	["Helvetica", 12.0],
					fontColor: 	Color.black,
					background: 	Color.white,
					foreground:	Color.grey(0.95),
					onColor:		Color.new255(255, 250, 250),
					offColor:		Color.clear,
					gap:			4 @ 4,
					margin: 		2@2,
					buttonHeight:	17
				));
//			GUI.setSkin(\dewdrop);
		}
	}

	guify { arg lay,bounds,title;
		argBounds = bounds;	// some of my gui's need to know this
		if(lay.isNil,{
			masterLayout = lay = this.class.windowClass.new
				(title ?? { model.asString.copyRange(0,50) },
				bounds);
			iMadeMasterLayout = true;	// now when I'm removed, I'll close the window too
		},{
			masterLayout = lay;	// should only pass in the FixedWidthMultiPageLayout
			lay = lay.asPageLayout(title,bounds);
		});
		// i am not really a view in the hierarchy
		lay.removeOnClose(this);
		^lay
	}

	remove {
		model.notNil.if({
			view.notClosed.if({
				view.remove;
				masterLayout.recursiveResize;
			});
			model.view = nil;
			model = nil;
			iMadeMasterLayout.if({
				masterLayout.close;
			});
		});
	}
	
	*windowClass { ^ResizeFlowWindow }
}

HJHFixedWidthObjectGui : HJHObjectGui {
	*windowClass { ^ResizeHeightFlowWindow }
}
