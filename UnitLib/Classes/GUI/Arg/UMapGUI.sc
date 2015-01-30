UMapGUI : UGUI {
	
	var <>header, <>userView, <>mainComposite;
	var <>removeAction;
	var <>parentUnit;
	
	*viewNumLines { |unit|
		^super.viewNumLines( unit ) + 1.1;
	}
	
	makeViews { |bounds|
		var margin = 0@0, gap = 4@4;
		
		if( bounds.isNil ) { 
			bounds = parent.asView.bounds.insetBy(4,4);
			if( parent.asView.class.name == \SCScrollTopView ) {
				bounds.width = bounds.width - 12;
			};
			if( parent.asView.class.name == \QScrollTopView ) {
				bounds.width = bounds.width - 20;
			};
		};
		bounds = bounds.asRect;
		bounds.height = this.class.getHeight( unit, viewHeight, margin, gap );
		
		if( unit.isKindOf( MassEditU ) ) {
			mapCheckers = unit.units.collect({ |unit|
				if( unit.isKindOf( U ) ) {
					UMapSetChecker( unit, { mapSetAction.value( this ) } ); 
				};
			}).select(_.notNil);
			unit.connect;
		} {
			mapCheckers = [ UMapSetChecker( unit, { mapSetAction.value( this ) } ) ];
		};
		
		mainComposite = CompositeView( parent, bounds ).resize_(2);
		
		userView = UserView( mainComposite, bounds.moveTo(0,0) ).resize_(2);
		
		userView.drawFunc = { |vw|
			Pen.width = 1;
			Pen.fillColor = unit.guiColor;
			Pen.strokeColor = Color.black.alpha_(0.5);
			Pen.roundedRect( vw.bounds.moveTo(0,0).insetBy(0.5,0.5), 3 );
			Pen.fillStroke;
		};
		
		userView.canFocus_( false );
		
		controller = SimpleController( unit );		
		composite = CompositeView( mainComposite, bounds.moveTo(0,0) ).resize_(2);
		composite.addFlowLayout( margin, gap );
		composite.onClose = {
			if( unit.class == MassEditUMap ) {
				unit.disconnect;
			}; 
			controller.remove;
			mapCheckers.do(_.remove);
		 };
		 
		 bounds = bounds.insetAll(0,0,2,0);
		 this.makeSubViews( bounds );
	}
	
	makeHeader { |bounds|
		var boldFont;
		var umapdragbin;
		var umapdragbinTask;
		
		header = CompositeView( composite, bounds.width @ viewHeight )
			.resize_(2);
			
		boldFont = (RoundView.skin.tryPerform( \at, \font ) ?? 
			{ Font( Font.defaultSansFace, 12) }).boldVariant;
		
		StaticText( header, labelWidth @ viewHeight )
			.applySkin( RoundView.skin )
			//.font_( boldFont )
			.string_( unit.unitArgName.asString )
			.align_( \right );

		StaticText( header, 
			Rect( labelWidth + 4, 0, (bounds.width - labelWidth), viewHeight ) 
		)
			.applySkin( RoundView.skin )
			.font_( boldFont )
			.string_( ":" + unit.defName );
			
			SmoothButton( header, Rect( bounds.width - 12, 2, 12, 12 ) )
				.label_( '-' )
				.canFocus_( false )
				.action_({
					removeAction.value( unit );
				});
				
			SmoothButton( header, Rect( 2, 2, 12, 12 ) )
				.label_( ['down', 'play'] )
				.border_( 0 )
				.background_( nil )
				.hiliteColor_( nil )
				.canFocus_( false )
				.value_( unit.guiCollapsed.binaryValue )
				.action_({ |bt|
					unit.guiCollapsed = bt.value.booleanValue;
				});
				
			UserView( header, // insert UMap
				Rect( 14, 2, labelWidth - 10, 12 ) 
			)
				.canReceiveDragHandler_({ |vw, x,y|
					var last;
					if( x.notNil ) {
						last = currentUMapSink;
						currentUMapSink = vw;
						last !? _.refresh;
						vw.refresh;
					};
					View.currentDrag.isKindOf( UMapDef ) && {
						(parentUnit !? (_.canUseUMap( unit.unitArgName, View.currentDrag )) 
							? false) && {
								View.currentDrag.canInsert
							}; 
					};
				})
				.receiveDragHandler_({
					unit.stop;
					if( parentUnit.isKindOf( MassEditU ) ) {
						parentUnit.units.do({ |subunit|
							subunit.insertUMap( unit.unitArgName, View.currentDrag );
						});
					} {
						parentUnit.insertUMap( unit.unitArgName, View.currentDrag );					};
				})
				.drawFunc_({ |vw|
					if( View.currentDrag.notNil && {
						vw.canReceiveDragHandler.value == true;
					}) {
						Pen.width = 2;
						if( currentUMapSink === vw ) {
							Pen.color = Color.blue.alpha_(1);
						} {
							Pen.color = Color.blue.alpha_(0.25);
						};
						Pen.addRect( vw.bounds.moveTo(0,0).insetBy(1,1) );
						Pen.stroke;
						if( umapdragbinTask.isPlaying.not ) {
							umapdragbinTask = Task({
								while { vw.isClosed.not && {												vw.canReceiveDragHandler.value == true
									} 
								} {
									0.25.wait;
								};
								if( vw.isClosed.not ) {
									vw.refresh;
								};
							}, AppClock).start;
						};
					};
				});
			
			UserView( header, // replace UMap
				Rect( labelWidth + 8, 2, (bounds.width - labelWidth - 16 - 6 ), 12 ) 
			)
				.canReceiveDragHandler_({ |vw, x,y|
					var last;
					if( x.notNil ) {
						last = currentUMapSink;
						currentUMapSink = vw;
						last !? _.refresh;
						vw.refresh;
					};
					View.currentDrag.isKindOf( UMapDef ) && {
						parentUnit !? (_.canUseUMap( unit.unitArgName, View.currentDrag )) 
							? false; 
					};
				})
				.receiveDragHandler_({
					unit.stop;
					unit.def = View.currentDrag;
				})
				.drawFunc_({ |vw|
					if( View.currentDrag.notNil && {
						vw.canReceiveDragHandler.value == true;
					}) {
						Pen.width = 2;
						if( currentUMapSink === vw ) {
							Pen.color = Color.blue.alpha_(1);
						} {
							Pen.color = Color.blue.alpha_(0.25);
						};
						Pen.addRect( vw.bounds.moveTo(0,0).insetBy(1,1) );
						Pen.stroke;
						if( umapdragbinTask.isPlaying.not ) {
							umapdragbinTask = Task({
								while { vw.isClosed.not && {												vw.canReceiveDragHandler.value == true
									} 
								} {
									0.25.wait;
								};
								if( vw.isClosed.not ) {
									vw.refresh;
								};
							}, AppClock).start;
						};
					};
				});
	}
}