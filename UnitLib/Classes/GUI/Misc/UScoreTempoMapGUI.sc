UScoreTempoMapGUI {
	
	classvar <>current;
	
	var <parent, <view, <barMapGUI, <tempoMapGUI, <presetView;
	
	*new { |parent, score, openNew = false|
		if( openNew.not && { current.notNil }) {
			current.score = score;
			current.parent.asView.findWindow.front;
			^current;
		} {
			^super.new.init( parent, score ).makeCurrent;
		};
	}
	
	init { |inParent, score|
		if( inParent.isNil ) {
			parent = Window( bounds: Rect(530, 580, 390, 140) ).front;
			parent.addFlowLayout;
		} {
			parent = inParent;
		};
		this.makeViews( score );
	}
	
	makeCurrent { current = this }
	
	close { 
		var win;
		win = parent.asView.findWindow;
		if( win.notNil && { win.isClosed.not } ) {
			win.close; 
		};
	}
	
	score_ { |score|
		barMapGUI.barMap = score.tempoMap.barMap;
		tempoMapGUI.tempoMap = score.tempoMap;
		presetView.object = score.tempoMap;
	}
	
	makeViews { |score|
		if( parent.class == Window.implClass ) {
			parent.name = "TempoMap : %".format( score.name );
		};
		parent.onClose = {
			if( current == this ) {
				current = nil;
			};
		};
		view = CompositeView( parent, parent.asView.bounds.insetAll(0,0,0,30) );
		view.resize_(5);
		view.addFlowLayout(0@0, 4@4);
		RoundView.useWithSkin( UChainGUI.skin, {
			barMapGUI = BarMapGUI( view, score.tempoMap.barMap );
			tempoMapGUI = TempoMapGUI( view, score.tempoMap );
			tempoMapGUI.scrollView.resize_(5);
			
			presetView = PresetManagerGUI( 
				parent, 
				parent.asView.bounds.width @ PresetManagerGUI.getHeight,
				TempoMap.presetManager,
				score.tempoMap
			).resize_(7)
		});
	}
	
}