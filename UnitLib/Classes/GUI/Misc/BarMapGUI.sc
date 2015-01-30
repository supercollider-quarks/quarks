BarMapGUI {
	
	var <barMap;
	var <parent, <scrollView, <composite, <startBeatView, <allViews, <localEvents;
	var <>action;
	var <viewHeight = 14;
	var skin;
	
	*new { |parent, barMap, action|
		^super.new.init( parent, barMap, action );
	}
	
	init { |inParent, inBarMap, inAction|
		var scrollViewHeight;
		barMap = inBarMap ?? { BarMap(); };
		action = inAction;
		parent = inParent ?? { Window( "BarMap" ).front; };
		scrollViewHeight = parent.asView.bounds.height;
		if( parent.asView.decorator.notNil ) {
			scrollViewHeight = scrollViewHeight - parent.asView.decorator.top - 4;
		};
		scrollView = ScrollView( parent, (142 + (viewHeight * 2)) @ scrollViewHeight )
			.resize_(4);
		this.makeViews();
	}
	
	rebuild {
		{	
			composite.remove;
			RoundView.useWithSkin( skin, { this.makeViews; });
		}.defer(0.1);
	}
	
	barMap_ { |newBarMap|
		if( newBarMap !== barMap ) {
			barMap = newBarMap;
			this.rebuild;
		};
	}
	
	makeViews {
		var ctrl, font;
		var bounds = Point( 
			122 + (viewHeight * 2), 
			(viewHeight + 4) * (barMap.events.size + 3)
		);	
		
		skin = RoundView.skin;
			
		font = RoundView.skin.tryPerform( \at, \font ) ?? { 
			Font( Font.defaultSansFace, viewHeight - 4) 
		};
		
		localEvents = barMap.events.copy;
		composite = CompositeView( scrollView, bounds );
		composite.addFlowLayout;
		
		ctrl = SimpleController( barMap )
			.put( \events, {
				var currentEvent;
				localEvents.do({ |item, i|
					allViews[ i ][ \bar ].value = item[2];
					allViews[ i ][ \signature ].value = item[[0,1]];
				});
				if( barMap.events != localEvents ) {
					this.rebuild;
				};
			});
			
		composite.onClose_({ ctrl.remove });
		
		StaticText( composite, 50@viewHeight ).string_( "startBeat:" ).font_( font );
		startBeatView = SmoothNumberBox( composite, 50@viewHeight )
			.value_( barMap.startBeat )
			.font_( font )
			.action_({ |nb|
				barMap.startBeat = nb.value;
			});
			
		composite.decorator.nextLine;
		
		StaticText( composite, 50@viewHeight ).string_( "bar" ).font_( font );
		StaticText( composite, 80@viewHeight ).string_( "signature" ).font_( font );
		composite.decorator.nextLine;
		
		allViews = localEvents.collect({ |item, i|
			var views, localAction;
			views = ();
			localAction = {
				// barMap.setSignatureAtBar( views[ \signature ].value, views[ \bar ].value );
				item[ 0 ] = views[ \signature ].num.asInt;
				item[ 1 ] = views[ \signature ].denom.asInt;
				item[ 2 ] = views[ \bar ].value.asInt;
				allViews[i-1] !? { |vws| vws[\bar].clipHi_( item[ 2 ] - 1 ) };
				allViews[i+1] !? { |vws| vws[\bar].clipLo_( item[ 2 ] + 1 ) };
				barMap.events = localEvents.copy;
				action.value;
			};
			views[ \bar ] = SmoothNumberBox( composite, 50@viewHeight )
				.value_( item[2] )
				.step_( 1 )
				.scroll_step_( 1 )
				.font_( font )
				.clipLo_(
					(localEvents[i-1] ? [0,0,-1])[2] + 1
				)
				.clipHi_( (localEvents[i+1] ? [0,0,inf])[2] - 1 )
				.action_( localAction );
				
			views[ \signature ] = SignatureBox( composite, 50@viewHeight )
				.applySmoothSkin
				.font_( font )
				.value_( item[[0,1]] )
				.action_( localAction ); 
				
			if( i != 0 ) {
				views[ \remove ] = SmoothButton( composite, viewHeight@viewHeight )
					.label_( '-' )
					.action_({
						var bb;
						bb = localEvents.copy;
						bb.remove( item );
						barMap.events = bb;
						action.value;
					});
					
			} {
				composite.decorator.shift( viewHeight + 4, 0 );
			};
			views[ \add ] = SmoothButton( composite, viewHeight@viewHeight )
				.label_( '+' )
				.action_({
					barMap.setSignatureAtBar( 
						views[ \signature ].value, 
						views[ \bar ].value + 1, 
						true 
					);
					action.value;
				});
				
			composite.decorator.nextLine;
			views;
		});
		
		SmoothButton( composite, (112 + (viewHeight * 2))@viewHeight )
			.label_( "delete duplicates" )
			.font_( font )
			.action_({ |vw|
				//vw.focus(false);
				barMap.deleteDuplicates;
				action.value;
			});
	}
	
}