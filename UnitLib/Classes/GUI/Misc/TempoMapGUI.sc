TempoMapGUI {
	
	var <tempoMap;
	var <parent, <scrollView, <composite, <allViews, <localEvents;
	var <>action;
	var <viewHeight = 14;
	var <mode = \bar;
	var skin;
	
	*new { |parent, tempoMap, action|
		^super.new.init( parent, tempoMap, action );
	}
	
	init { |inParent, inTempoMap, inAction|
		var scrollViewHeight;
		tempoMap = inTempoMap ?? { TempoMap(); };
		action = inAction;
		parent = inParent ?? { Window( "TempoMap" ).front; };
		scrollViewHeight = parent.asView.bounds.height;
		if( parent.asView.decorator.notNil ) {
			scrollViewHeight = scrollViewHeight - parent.asView.decorator.top - 4;
		};
		scrollView = ScrollView( parent, (180 + (viewHeight * 2)) @ scrollViewHeight )
			.resize_(4);
		this.makeViews();
	}
	
	rebuild {
		{	
			composite.remove;
			RoundView.useWithSkin( skin, { this.makeViews; });
		}.defer(0.1);
	}
	
	tempoMap_ { |newTempoMap|
		if( newTempoMap !== tempoMap ) {
			tempoMap = newTempoMap;
			this.rebuild;
		};
	}
	
	mode_ { |newMode|
		if( (mode != newMode) && { [ \time, \bar ].includes( newMode ) } ) {
			mode = newMode;
			this.rebuild;
		};
	}
	
	makeViews {	
		var ctrl, font;
		var bounds = Point( 
			(160 + (viewHeight * 2)), 
			(viewHeight + 4) * (tempoMap.events.size + 2)
		);	
		
		skin = RoundView.skin;
			
		font = RoundView.skin.tryPerform( \at, \font ) ?? { 
			Font( Font.defaultSansFace, viewHeight - 4) 
		};
		
		localEvents = tempoMap.events.copy;
		composite = CompositeView( scrollView, bounds);
		composite.addFlowLayout;
		
		ctrl = SimpleController( tempoMap )
			.put( \events, {
				var currentEvent;
				if( tempoMap.events != localEvents ) {
					this.rebuild;
				} {
					localEvents.do({ |item, i|
						if( mode == \bar ) {
							allViews[ i ][ \bar ].value = item[1];
						} {
							allViews[ i ][ \time ].value = item[2];
						};
						allViews[ i ][ \tempo ].value = tempoMap.tempoToBPM( item[0] ) 
					});
				};
			});
			
		composite.onClose_({ ctrl.remove; });
	
		PopUpMenu( composite, 90@viewHeight )
			.items_( [ \time, \bar ] )
			.value_( [ \time, \bar ].indexOf( mode ) ? 0 )
			.font_( font )
			.action_({ |pu|
				if( pu.item != mode ) {
					this.mode = pu.item;
				};
			});
		
		StaticText( composite, 80@viewHeight ).string_( " bpm" ).font_( font );
		composite.decorator.nextLine;
		
		allViews = localEvents.collect({ |item, i|
			var views, localAction;
			views = ();
			localAction = {
				// barMap.setSignatureAtBar( views[ \signature ].value, views[ \bar ].value );
				if( mode == \bar ) {
					item[ 1 ] = views[ \bar ].value;
					item[ 0 ] = tempoMap.bpmToTempo( views[ \tempo ].value );
					tempoMap.prUpdateTimes;
					if( i > 1 ) { 
						allViews[i-1] !? { |vws| vws[\bar].clipHi_( item[ 1 ] - 0.001 ) }; 
					};
					allViews[i+1] !? { |vws| vws[\bar].clipLo_( item[ 1 ] + 0.001 ) };
				} {
					item[ 2 ] = views[ \time ].value;
					item[ 0 ] = tempoMap.bpmToTempo( views[ \tempo ].value );
					tempoMap.prUpdateBeats;
					if( i > 1 ) { 
						allViews[i-1] !? { |vws| vws[\time].clipHi_( item[ 2 ] - 0.001 ) }; 
					};
					allViews[i+1] !? { |vws| vws[\time].clipLo_( item[ 2 ] + 0.001 ) };
				};
				tempoMap.events = localEvents.copy;
				action.value;
			};
			
			switch( mode, 
				\time, {
					views[ \time ] = SMPTEBox( composite, 90@viewHeight )
						.applySmoothSkin
						.value_( item[2] )
						.font_( font )
						.clipLo_(
							if( i != 0 ) {
								localEvents[i-1][2] + 0.001
							} {
								0
							};
						)
						.clipHi_( 
							if( i != 0 ) {
								(localEvents[i+1] ? [0,inf,inf])[2] - 0.001
							} {
								0
							}
						)
						.action_( localAction );
					if( i == 0 ) { views[ \time ].enabled_( false ) };
				},
				\bar, {
					views[ \bar ] = BarMapView( composite, 90@viewHeight, tempoMap.barMap )
						.value_( item[1] )
						.setFont( font )
						.clipLo_(
							if( i != 0 ) {
								localEvents[i-1][1] + (0.001/tempoMap.barMap.beatDenom)
							} {
								0
							};
						)
						.clipHi_( 
							if( i != 0 ) {
								(localEvents[i+1] ? [0,inf,inf])[1] - 
								(0.001/tempoMap.barMap.beatDenom)
							} {
								0
							}
						)
						.action_( localAction );
					
					if( i == 0 ) { views[ \bar ].enabled_( false ) };
				}
			);
				
			views[ \tempo ] = SmoothNumberBox( composite, 50@viewHeight )
				.font_( font )
				.clipLo_(1)
				.value_( tempoMap.tempoToBPM( item[0] ) )
				.action_( localAction ); 
				
			if( i != 0 ) {
				views[ \remove ] = SmoothButton( composite, viewHeight@viewHeight )
					.label_( '-' )
					.action_({
						var bb;
						bb = localEvents.copy;
						bb.remove( item );
						tempoMap.events = bb;
						action.value;
					});
					
			} {
				composite.decorator.shift( viewHeight + 4, 0 );
			};
			views[ \add ] = SmoothButton( composite, viewHeight@viewHeight )
				.label_( '+' )
				.action_({
					if( mode == \bar ) {
						tempoMap.setBPMAtBeat( 
							views[ \tempo ].value, views[ \bar ].value + 1, true 
						);
					} {
						tempoMap.setBPMAtTime( 
							views[ \tempo ].value, views[ \time ].value + 1, true 
						);
					};
					action.value;
				});
				
			composite.decorator.nextLine;
			views;
		});
		SmoothButton( composite, (152 + (viewHeight * 2))@viewHeight )
			.label_( "delete duplicates" )
			.font_( font )
			.action_({ |vw|
				//vw.focus(false);
				tempoMap.deleteDuplicates;
				action.value;
			});
		}
	
}