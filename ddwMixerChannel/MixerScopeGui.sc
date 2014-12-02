
MixerScopeGui : ObjectGui {
	classvar	defaultWidth = 500, defaultHeight = 100;
	var	<layout, <masterLayout, scopeView, <iMadeMasterLayout,
		myModel;

	guify { arg lay,bounds,title;
		if(lay.isNil,{
			masterLayout = lay = ResizeHeightFlowWindow
				(title ?? { model.asString.copyRange(0,50) },
				bounds ?? { Rect(0, 0, defaultWidth, 
					defaultHeight * model.channel.outChannels) });
			iMadeMasterLayout = true;	// now when I'm removed, I'll close the window too
		},{
			masterLayout = lay;	// should only pass in the FixedWidthMultiPageLayout
			lay = lay.asPageLayout(title,bounds);
		});
		// i am not really a view in the hierarchy
		lay.removeOnClose(this);
		^lay
	}

	guiBody { arg lay;
		layout = lay;
		scopeView = GUI.scopeView.new(lay, lay.bounds)
			.bufnum_(model.buffer.bufnum);
		myModel = model;
	}
	
	remove { arg dummy, freeModel = true;	// when model frees programmatically, this is false
		model.dependants.remove(this);	// to avoid recursion with model.free below

			// scopeView is nil if remove has been called before
		if(scopeView.notNil) {
			scopeView.notClosed.if({
				scopeView.remove;
			});
	
			freeModel.if({ myModel.free; });
	
			if(iMadeMasterLayout and: { masterLayout.isClosed.not }) {
				masterLayout.prClose;
			};
			
			layout = masterLayout = scopeView = iMadeMasterLayout = myModel = nil;
		};
	}
}
