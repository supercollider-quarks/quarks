/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UMarkerGUI : UChainGUI {
	
	prMakeViews { |bounds|
		var margin = 0@0, gap = 4@4;
		var heights, units;
		var labelWidth, releaseTask;
		var controller;
		var updateNotes = true;
		// var unitInitFunc;
		
		labelWidth = 80;
		
		if( RoundView.skin.notNil ) { labelWidth = RoundView.skin.labelWidth ? 80 };
		
		views = ();
		
		originalBounds = bounds.copy;
		
		bounds = bounds ?? { parent.asView.bounds.insetBy(4,4) };
		if( parent.asView.class.name == 'SCScrollTopView' ) {
			bounds.width = bounds.width - 12;
		};
				
		controller = SimpleController( chain );
		
		composite = CompositeView( parent, bounds ).resize_(5);
		composite.addFlowLayout( margin, gap );
		composite.onClose = { |vw|
			controller.remove; 
			this.removeFromAll;
			if( composite == vw && { current == this } ) { current = nil } 
		};
		
		composite.decorator.shift( bounds.width - 80 - 28, 0 );
		
		views[ \displayColor ] = UserView( composite, 28@14 )
			.resize_(3)
			.drawFunc_({ |vw|
				var wd = 8, smallRect;
				if( chain.displayColor.notNil ) {	
					Pen.roundedRect(vw.drawBounds, wd);
					chain.displayColor.penFill(vw.drawBounds, 1, nil, 10);
					smallRect = Rect( vw.bounds.width - wd, 0, wd, wd );
					Pen.color = Color.gray(0.66,0.75);
					Pen.addOval( smallRect, 2 );
					Pen.fill;
					Pen.color = Color.black;
					DrawIcon( '-', smallRect );
				} {
					Pen.roundedRect(vw.drawBounds, wd);
					chain.getTypeColor.penFill( vw.drawBounds );
				};
			})
			.mouseDownAction_({ |vw, x,y|
				var wd = 8, smallRect;
				smallRect = Rect( vw.bounds.width - wd, 0, wd, wd );
				if( smallRect.containsPoint( x@y ) ) {
					 chain.displayColor = nil; 
					 vw.refresh;
				} {
					if( views[ \colorEditor ].isNil ) { 
						if( chain.displayColor.isNil or: { 
								chain.displayColor.class == Color 
							} ) {
								RoundView.pushSkin( skin );
								views[ \colorEditor ] = ColorSpec( chain.getTypeColor )
									.makeView( "UMarker displayColor",
										action: { |vws, color| 
											chain.displayColor = color; 
										} 
									);
								views[ \colorEditor ].view.onClose = { 
									views[ \colorEditor ] = nil 
								};
								RoundView.popSkin;
						} {
							"no editor available for %\n".postf( chain.displayColor.class );
						};
					} {
						views[ \colorEditor ].view.findWindow.front;
					};
				};
			})
			.keyDownAction_({ |vw, a,b,cx| 
				if( cx == 127 ) { chain.displayColor = nil }; 
			})
			.beginDragAction_({ chain.displayColor })
			.canReceiveDragHandler_({ 
				var obj;
				obj = View.currentDrag;
				if( obj.class == String ) {
					obj = { obj.interpret }.try;
				};
				obj.respondsTo( \penFill );
			})
			.receiveDragHandler_({ 
				if( View.currentDrag.class == String ) {
					chain.displayColor = View.currentDrag.interpret; 
				} {
					chain.displayColor = View.currentDrag;
				};
			})
			.onClose_({ if( views[ \colorEditor ].notNil ) {
					views[ \colorEditor ].view.findWindow.close;
				};
			});

		
		views[ \singleWindow ] = SmoothButton( composite, 74@14 )
			.label_( [ "single window", "single window" ] )
			.border_( 1 )
			.hiliteColor_( Color.green )
			.value_( this.class.singleWindow.binaryValue )
			.resize_(3)
			.action_({ |bt|
				this.class.singleWindow = bt.value.booleanValue;
			});
		
		composite.decorator.nextLine;
		
		// name
		StaticText( composite, labelWidth@14 )
			.applySkin( RoundView.skin )
			.string_( "name" )
			.align_( \right );
			
		views[ \name ] = TextField( composite, 84@14 )
			.applySkin( RoundView.skin )
			.string_( chain.name )
			.action_({ |tf|
				chain.name_( tf.string );
			});
			
		composite.decorator.nextLine;
			
		// startTime
		PopUpMenu( composite, labelWidth@14 )
			.applySkin( RoundView.skin )
			.canFocus_( false )
			.items_( [ "startTime", "startBar" ] )
			.action_({ |pu|
				startTimeMode = [ \time, \bar ][ pu.value ];
				views[ \startTime ].visible = (startTimeMode === \time );
				views[ \startBar ].visible = (startTimeMode === \bar );
			})
			.value_( [ \time, \bar ].indexOf( startTimeMode ) ? 0 );
			
		views[ \startTime ] = SMPTEBox( composite, 84@14 )
			.applySmoothSkin
			.applySkin( RoundView.skin )
			.clipLo_(0)
			.visible_( startTimeMode === \time )
			.action_({ |nb|
				chain.startTime_( nb.value );
			});
		
		composite.decorator.shift( -88, 0 );
		
		views[ \startBar ] = TempoBarMapView( composite, 84@14, tempoMap  )
			.applySkin( RoundView.skin )
			.radius_(2)
			.clipLo_(0)
			.visible_( startTimeMode === \bar )
			.action_({ |nb|
				chain.startTime_( nb.value );
			});
						
		composite.decorator.nextLine;
		
		// action
		
		RoundView.useWithSkin( RoundView.skin ++ ( labelWidth: 82 ), {
			views[ \action ] = ObjectView( composite, (labelWidth + 120) @ 14, 
				chain, \action, CodeSpec({ |marker, score| }), controller
			);
		});
		
		composite.decorator.nextLine;
		
		// autoPause
		
		RoundView.useWithSkin( RoundView.skin ++ ( labelWidth: 82 ), {
			views[ \action ] = ObjectView( composite, (labelWidth + 120) @ 14, 
				chain, \autoPause, BoolSpec(), controller
			);
		});
		
		composite.decorator.nextLine;
		
		// notes
		StaticText( composite, labelWidth@14 )
			.applySkin( RoundView.skin )
			.string_( "notes" )
			.align_( \right );
			
		views[ \notes ] = TextView( composite, 
			(composite.bounds.width - (labelWidth+4)) @ 
			 	(composite.bounds.height - (PresetManagerGUI.getHeight + 12 + composite.decorator.top ))
			 )
			.applySkin( RoundView.skin )
			.string_( chain.notes ? "" )
			.hasVerticalScroller_( true )
			.autohidesScrollers_( true )
			.keyUpAction_({ |tf|
				updateNotes = false;
				if( tf.string.size > 0 ) {
					chain.notes_( tf.string );
				} {
					chain.notes_( nil );
				};
				updateNotes = true;
			})
			.background_( Color.gray(0.8) )
			.resize_(5);
		
		composite.decorator.nextLine;
		//composite.decorator.top = composite.bounds.height - (PresetManagerGUI.getHeight + 8 );
		
		CompositeView( composite, (composite.bounds.width - (margin.x * 2)) @ 2 )
				.background_( Color.black.alpha_(0.25) )
				.resize_(8);
			
		presetView = PresetManagerGUI( 
			composite, 
			composite.bounds.width @ PresetManagerGUI.getHeight,
			UMarker.presetManager,
			chain
		).resize_(7);

		controller
			.put( \displayColor, { { views[ \displayColor ].refresh; }.defer; } )
			.put( \startTime, { 
				views[ \startTime ].value = chain.startTime ? 0; 
				views[ \startBar ].value = chain.startTime ? 0;
			})
			.put( \name, { { views[ \name ].value = chain.name; }.defer })
			.put( \notes, { 
				if( updateNotes ) {
					{ views[ \notes ].string = chain.notes ? ""; }.defer 
				};
			});
			
		composite.getParents.last.findWindow !? _.toFrontAction_({ 
			this.makeCurrent;
		});
		
		chain.changed( \startTime );
		chain.changed( \name );
	}
}

+ UMarker {
	gui { |parent, bounds, score| ^UMarkerGUI( parent, bounds, this, score ) }
}