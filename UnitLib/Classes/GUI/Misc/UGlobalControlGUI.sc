UGlobalControlGUI {
	
	classvar <>current;
	
	var <view, <views, <scrollView, <composite, <presetView, <ugui;
	var <>performUpdate = nil;
	var <>storedKeys, <>umaps, <>unitInitFunc;
	
	*new { |parent, bounds, makeCurrent = true|
		if( parent.isNil && { current.notNil && { current.view.isClosed.not } } ) {
			^current.front;
		} {
			^super.new.init( parent, bounds ).makeCurrent( makeCurrent );
		};
	}
	
	makeCurrent { |bool| if( bool == true ) { current = this } }
	
	front { view.findWindow.front }
	
	getHeight { |margin, gap|
		^UGUI.getHeight( UGlobalControl.current, 14, margin, gap ) + (4 * (14 + gap.y));
	}
	
	update { |key, value|
		if( performUpdate !? { performUpdate } ?? { UGlobalControl.current.keys != storedKeys } ) {
			performUpdate = nil;
			this.rebuild;
		};
	}
	
	init { |parent, bounds|
		if( parent.notNil ) {
			bounds = bounds ?? { parent.bounds.moveTo(0,0).insetBy(4,4) };
		} {
			bounds = bounds ? Rect(
				Window.screenBounds.width - 445, 
				Window.screenBounds.height - 845, 
				340, 240
			);

		};
		
		view = EZCompositeView( parent ? "UGlobalControl", bounds, true ).resize_(5);
		bounds = view.bounds;
		
		views = ();
		
		RoundView.pushSkin( UChainGUI.skin );
		
		// startbutton
		views[ \startButton ] = SmoothButton( view, 14@14 )
			.label_( ['power', 'power'] )
			.radius_(7)
			.background_( Color.clear )
			.border_(1)
			.hiliteColor_( Color.green )
			.action_( [ {
					umaps.do({ |item|
						if( item.argNames.includes( \active ) ) {
							item.set( \active, true );
						};
					});
					UGlobalControl.current.prepare;
				}, { 
					UGlobalControl.current.dispose;
				} ]
		 	);
		 	
		 view.decorator.shift( view.decorator.indentedRemaining.width - 42, 0 );
		 
		 views[ \defs ] = SmoothButton( view, 40 @ 14 )
			.label_( "udefs" )
			.border_( 1 )
			.radius_( 2 )
			.action_({
				UdefsGUI();
			}).resize_(3);
		 	
		view.decorator.nextLine;
		
		scrollView = ScrollView( view, view.decorator.indentedRemaining
			.insetAll( 0,0,0, 20) 
		).resize_(5);
		scrollView.addFlowLayout;
		
		unitInitFunc = { |unit, what ...args|
			if( what === \init ) { // close all views and create new
				this.rebuild;
			};
		};

		UGlobalControl.addDependant( this );
		
		view.onClose_( { 
			UGlobalControl.removeDependant( this ); 
			umaps.do( _.removeDependant( unitInitFunc ) );
			if( current == this ) { current = nil };
		} );
		
		this.makeView;
		
		presetView = PresetManagerGUI( 
				view, 
				view.bounds.width @ PresetManagerGUI.getHeight,
				UGlobalControl.presetManager,
				UGlobalControl.current
			).resize_(7);
		
		RoundView.popSkin;
	}
	
	rebuild {
		umaps.do( _.removeDependant( unitInitFunc ) );
		{
			views[ \startButton ].focus( true );
			ugui.composite.remove;
			this.makeView;
		}.defer(0.1);
	}
	
	makeView {
		var width;
		var scrollerMargin = 12;
		
		if( GUI.id == \qt ) { scrollerMargin = 20 };
		width = scrollView.bounds.width - scrollerMargin - 8;
		scrollView.decorator.reset;
		
		RoundView.pushSkin( UChainGUI.skin );
		
		storedKeys = UGlobalControl.current.keys.copy;
		
		umaps = UGlobalControl.current.getAllUMaps;
		umaps.do( _.addDependant( unitInitFunc ) );
		ugui = UGUI( scrollView, width @ 0, UGlobalControl.current );
		
		ugui.mapSetAction = { performUpdate = true; this.update; };
		
		RoundView.popSkin;
	}
	
	
	remove {
		ugui.composite.remove;
	}
}