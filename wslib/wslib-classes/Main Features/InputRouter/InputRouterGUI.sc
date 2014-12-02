InputRouterGUI {
	
	var <inputRouter, <view, <views, <controller, <skin;
	var <rowHeight = 15;
	var <>buttons;
	
	*new { |inputRouter, parent, bounds|
		^super.new.init( parent, bounds, inputRouter)
	}
	
	init { |parent, bounds, ir|
		var resize =0;
		
		if( parent.isNil or: parent.isString ) {
			bounds = bounds ? Rect( 72,256, 220, 4 + 
				(ir.settings.size * (rowHeight + 12)) +
				//(ir.settings.collect(_.size).sum * (rowHeight + 4)) +
				24 
				 );
		} {
			bounds = bounds ? parent.asView.bounds;
		};
	
		view = SmoothScrollView( parent ? "% InputRouter".format(ir.name), bounds );
		view.composite.bounds = view.composite.bounds.resizeBy(0, -24);
		view.hasHorizontalScroller = false;
		view.asView.addFlowLayout( 2@0, 4@4 );
		view.asView.onClose_({ 
			 inputRouter.removeDependant( this );
		});
		skin = ( 
			hiliteColor: Color.gray(0.3), 
			font: Font( Font.defaultSansFace, 10 ) 
		);
		
		this.inputRouter_( ir );
		this.createButtons;
		
	}
	
	inputRouter_ { |ir|
		inputRouter.removeDependant( this );
		inputRouter = ir;
		this.rebuildViews;
	}
	
	rebuildViews { 
		var collapsed;
		collapsed = views.collect({ |item| item.view.collapsed }); 
		this.removeViews;
		this.createViews( collapsed );
	}
	
	deferRebuildViews {
		var collapsed;
		collapsed = views.collect({ |item| item.view.collapsed }); 
		{ 
			this.removeViews;
			this.createViews( collapsed );
		}.defer;
	}
	
	removeViews {
		 inputRouter.removeDependant( this );
		 view.asView.removeAll;
		 view.asView.decorator.reset;
	}
		
	update { |ir, what ...args| // this object is also it's own controller
		if( what.notNil )
			{ switch( what,
				\level, { this.setLevelView( *args ); },
				\meterLevel, { this.setMeterView( *args ) },
				\input, { this.setInputView( *args ) },
				\filter, { this.setFilterViews( *args ); },
				\compression, { this.setCompressionViews( *args ); },
				
				\inputLabels, { this.deferRebuildViews; },
				\settings, { this.deferRebuildViews; },
				\useCompression, { this.deferRebuildViews; },
				\useFilters, { this.deferRebuildViews; },
				\stop, { 
					buttons[ \power ].value = 0;
					{	 
						inputRouter.settings.do({ |item, i|
							item.do({ |level, ii|
								this.setMeterView( i, ii, [-80,-80] );
							});
						});
					}.defer(0.1); 
				},
				\start, {
					buttons[ \power ].value = 1;
				}
				
				
				);
			};
	}
	
	setLevelView { |i = 0, ii = 0, dB = 0|
		views[i].channelViews[ii].level.value = dB;
	}
	
	prSetView { |view, what, selector = \value_, defer = false|
		if( defer ) {
			{ this.prSetView( view, what, selector, false ); }.defer;
		} {
			if( view.notNil && { view.isClosed.not } ) {
				view.perform( selector, what );
			}
		};
	}
	
	setFilterViews { |i = 0, ii = 0, lowCut, lowFreq, hiCut, hiFreq|
		if( lowCut.notNil ) {
			this.prSetView( views[i].channelViews[ii].lowCut, lowCut );		};
		if( lowFreq.notNil ) {
			this.prSetView( views[i].channelViews[ii].lowFreq, lowFreq );		};
		if( hiCut.notNil ) {
			this.prSetView( views[i].channelViews[ii].hiCut,  hiCut );		};
		if( hiFreq.notNil ) {
			this.prSetView( views[i].channelViews[ii].hiFreq, hiFreq );		};
	}
	
	setCompressionViews { |i = 0, ii = 0, thresh, amt, attack|
		if( thresh.notNil ) {
			this.prSetView( views[i].channelViews[ii].thresh, thresh );		};
		if( amt.notNil ) {
			this.prSetView( views[i].channelViews[ii].amt, amt, defer: true );		};
		if( attack.notNil ) {
			this.prSetView( views[i].channelViews[ii].attack,  attack );		};
	}
	
	setMeterView { |i = 0, ii = 0, array|
		var meterView;
		{
			meterView = views[i].meterViews[ii];
			if( meterView.notNil && { meterView.isClosed.not } ) {
				meterView.value = array[0].linlin(-80,0,0,1,\none);
				meterView.peakLevel = array[1].linlin(-80,0,0,1,\none);
			};
		}.defer;
	}
	
	setInputView { |i = 0, ii = 0, in = 0|
		views[i].channelViews[ii].input.value = in;
	}
	
	createButtons {
		var bounds, parent;
		buttons = ();
		
		bounds = view.composite.bounds;
		parent = view.composite.parent;
		
		RoundView.useWithSkin( skin, {	
			buttons[ \save ] = RoundButton.new( parent, 
				Rect( bounds.left + 27, bounds.bottom + 4, 55, 16 ) )
					.extrude_( true ).border_(1)
					.states_( [[ "save", Color.black, Color.red(0.75).alpha_(0.25) ]] )
					.action_({ inputRouter.writeSettings })
					.resize_(7);
			
			
			buttons[ \revert ] = RoundButton.new( parent, 
				Rect( bounds.left + 86, bounds.bottom + 4, 55, 16 ) )
					.extrude_( true ).border_(1)
					.states_( [[ "revert", Color.black, Color.green(0.75).alpha_(0.25) ]] )
					.action_({ inputRouter.readSettings })
					.resize_(7);
				
			buttons[ \power ] = RoundButton.new( parent, 
				Rect( bounds.left + 4, bounds.bottom + 4, 17, 17 ) )
					.extrude_( true ).border_(1)
					.states_( [
						[ 'power', Color.gray(0.2), Color.white(0.75).alpha_(0.25) ],
						[ 'power', Color.red(0.8), Color.white(0.75).alpha_(0.25) ]] )
					.value_( inputRouter.isRunning.binaryValue )
					.action_([{ this.inputRouter.start }, { this.inputRouter.stop }])
					.resize_(7);
		});
	}
	
	createViews { |collapsed|
		var width, nRows = 1;
		width = view.asView.bounds.width - 4;
		
		collapsed = collapsed ?? { true!(inputRouter.settings.size) };
		
		if( inputRouter.useFilters ) { nRows = nRows + 1 };
		if( inputRouter.useCompression ) { nRows = nRows + 1 };
		
		RoundView.useWithSkin( skin, {
			views = inputRouter.settings.collect({ |setting, i|
				var el, inWidth, mViewHeight;
				el = ();
				el.view = ExpandView( view.asView, 
					width @ ( ( setting.size * (nRows * (rowHeight + 4)) ) + rowHeight + 8 ),
					width @ ( rowHeight + 8 ), 
					collapsed[i] );
				el.view.asView.addFlowLayout;
				inWidth = el.view.asView.bounds.width - 8;
				el.label = StaticText( el.view, 80  @ rowHeight )
					.applySkin( skin )
					.string_( inputRouter.outputLabels[i] );
				
				el.meterParent = CompositeView( el.view, (inWidth - 88) @ rowHeight );
				el.meterParent.addFlowLayout( 0@0, 0@0 );
				
				el.meterViews = setting.collect({
					this.createMeterView( el.meterParent, 
						el.meterParent.bounds.width,
						rowHeight / setting.size
					);
				});
				
				el.view.asView.decorator.nextLine;
				
				el.channelViews = setting.collect({ |value, ii| 
					var out = this.createChannelView( el.view, inWidth, i, ii, 
						inputRouter.useFilters,
						inputRouter.useCompression
					);
					el.view.asView.decorator.nextLine;
					out;
				});
				
				if( setting.size == 1 ) {
					el.channelViews[0].minus.enabled = false;
				};
				
				el.view.hideOutside;
				el;
				
			});
		});
		
		inputRouter.addDependant( this );
	}
	
	createMeterView { |parent, width, height|
		^LevelIndicator( parent,  width  @ height )
			.drawsPeak_( true )
			.warning_( -6.linlin(-80,0,0,1) ) // warn at -6
			.critical_(1);
	}
	
	createChannelView { |parent, width, i, ii, filters = true, compression = true|
		var el;
		el = ();
				
		el.input = PopUpMenu( parent, 80 @ rowHeight )
			.items_( inputRouter.inputLabels )
			.value_( inputRouter.settings[i][ii] )
			.action_({ |pu| inputRouter.setInput( i, ii, pu.value ); })
			.applySkin( skin );
			
		StaticText( parent, 25 @ rowHeight )
			.applySkin( skin )
			.string_( "gain:" )
			.align_( \right );

		el.level = SmoothNumberBox( parent, 20 @ rowHeight )
			.step_(1).clipLo_(-100).clipHi_(20).scroll_step_(1)
			.action_({ |sl|  inputRouter.setLevel( i, ii, sl.value ); })
			.value = inputRouter.inputLevels[i][ii];	
			
		
		el.minus = SmoothButton( parent, rowHeight @ rowHeight )
				.label_( '-' )
				.action_({ { 0.1.wait; inputRouter.removeInput( i, ii ); }.fork });
		
		el.plus = SmoothButton( parent, rowHeight @ rowHeight )
				.label_( '+' )
				.action_({ { 0.1.wait; inputRouter.addInput( i ) }.fork });
		
	
		if( filters == true ) { 
			parent.view.decorator.nextLine;
			
			/*
			StaticText( parent, 25 @ rowHeight )
				.applySkin( skin )
				.string_( "cut" )
				.align_( \right );
			*/
			
			el.lowCut = RoundButton( parent, 40 @ rowHeight )
					.extrude_( true ).border_(1).radius_(rowHeight/4)
					.states_( [
						[ "lowCut", Color.gray(0.6) ],
						[ "lowCut", Color.gray(0), Color.clear ]] )
					.value_( inputRouter.filterSettings[i][ii][0] )
					.action_({ |bt|  inputRouter.setFilter( i, ii, lowCut: bt.value ); });			
			el.lowFreq = SmoothNumberBox( parent, 35 @ rowHeight )
				.step_(1).clipLo_(20).clipHi_(22000).scroll_step_(1)
				.value_( inputRouter.filterSettings[i][ii][1] )
				.action_({ |sl|  inputRouter.setFilter( i, ii, lowFreq: sl.value ); });
			
			parent.view.decorator.shift(10);
			
			el.hiCut = RoundButton( parent, 40 @ rowHeight )
					.extrude_( true ).border_(1).radius_(rowHeight/4)
					.states_( [
						[ "hiCut", Color.gray(0.6) ],
						[ "hiCut", Color.gray(0), Color.clear ]] )
					.value_( inputRouter.filterSettings[i][ii][2] )
					.action_({ |bt|  inputRouter.setFilter( i, ii, hiCut: bt.value ); });
			
			el.hiFreq = SmoothNumberBox( parent, 35 @ rowHeight )
				.step_(1).clipLo_(20).clipHi_(22050).scroll_step_(1)
				.value_( inputRouter.filterSettings[i][ii][3] )
				.action_({ |sl|  inputRouter.setFilter( i, ii, hiFreq: sl.value ); });
			
		};
		
		if( compression == true ) { 
			parent.view.decorator.nextLine;
			
			StaticText( parent,  30 @ rowHeight )
				.applySkin( skin )
				.string_( "comp:" )
				.align_( \right );
				
			el.amt = Knob( parent, rowHeight @ rowHeight )
				.applySkin( skin )
				.value_( inputRouter.compressionSettings[i][ii][1] )
				.action_({ |sl|  inputRouter.setCompression( i, ii, amt: sl.value ); });
			
			if( GUI.scheme == \cocoa ) {
				el.amt.skin_( (
					scale:	Color.clear,
					center: 	Color.gray(0.5, 0.5),
					level:	Color.black.alpha_(0.7)
				) )
			};
				
				
			StaticText( parent, 20 @ rowHeight )
				.applySkin( skin )
				.string_( "db:" )
				.align_( \right );	
						
			el.thresh = SmoothNumberBox( parent, 20 @ rowHeight )
				.step_(1).scroll_step_(1).clipLo_(-80).clipHi_(0)
				.value_( inputRouter.compressionSettings[i][ii][0] )
				.action_({ |sl|  inputRouter.setCompression( i, ii, thresh: sl.value ); });

			StaticText( parent, 36 @ rowHeight )
				.applySkin( skin )
				.string_( "attack:" )
				.align_( \right );
			
			el.attack = SmoothNumberBox( parent, 30 @ rowHeight )
				.step_(0.001).clipLo_(0).clipHi_(0.5).scroll_step_(0.001)
				.value_( inputRouter.compressionSettings[i][ii][2] )
				.action_({ |sl|  inputRouter.setCompression( i, ii, attack: sl.value ); });		
			
		};
			
		^el;
	}

	
}