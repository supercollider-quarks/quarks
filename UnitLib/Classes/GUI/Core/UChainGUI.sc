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

UChainGUI {
	
	classvar <>skin;
	classvar <>current;
	classvar <>all;
	classvar <>singleWindow = true;
	classvar <>packUnitsDefault = true;
	classvar <>scrollViewOrigin;
	classvar <>startTimeMode = \time; // \time, \bar
	classvar <>durationMode = \duration; // \duration, \endTime, \endBar
	classvar <>nowBuildingChain;
	
	var <chain, <score;
	
	var <parent, <composite, <views, <startButton, <uguis;
	var <>presetView;
	var <>action;
	var originalBounds;
	var <packUnits = true;
	var <>scrollView;
	var <>massEditWindowIndex;
	var <>tempoMap;
	var <>undoManager;
	
	var <>autoRestart = false;

	*initClass {
		
		skin = ( 
			labelWidth: 80, 
			hiliteColor: Color.black.alpha_(0.33),
			RoundButton: (
				border: 0.75,
				background:  Gradient( Color.white, Color.gray(0.85), \v ) 
			),
			SmoothButton: (
				border: 0.75,
				background:  Gradient( Color.white, Color.gray(0.85), \v ) 
			),
			SmoothSimpleButton: (
				border: 0.75,
				background:  Gradient( Color.white, Color.gray(0.85), \v ) 
			),
		);
	
		StartUp.defer({ skin.font = Font( Font.defaultSansFace, 10 ); });
		
		all = [];

	}
	
	*new { |parent, bounds, chain, score|
		^super.newCopyArgs( chain, score ).init( parent, bounds );
	}
	
	init { |inParent, bounds|
		parent = inParent;
		
		packUnits = packUnitsDefault;
		
		tempoMap = UScore.current !? _.tempoMap ?? { TempoMap() };
		
		if( skin.font.class != Font.implClass ) { // quick hack to make sure font is correct
			skin.font = Font( Font.defaultSansFace, 10 );
		};
		
		if( parent.isNil ) { 
			parent = chain.class.asString;
		};
		if( parent.class == String ) {
			if( singleWindow && current.notNil && { current.window.class == Window.implClass } ) {
				parent = current.parent.asView.findWindow;
				current.views.singleWindow.focus;
				current.remove;
				this.makeViews( bounds );
				this.makeCurrent;
				this.addToAll;
				parent.front;
			} {
				parent = Window(
					parent, 
					bounds ?? { Rect(425 rrand: 525, 300 rrand: 350, 342, 420) }, 
					scroll: false
				).front;
				this.makeViews( bounds );
				this.makeCurrent;
				this.addToAll;
			};
		} {
			this.makeViews( bounds );
			this.makeCurrent;
			this.addToAll;
		};
		
	}
	
	makeCurrent { current = this }
	
	addToAll { all = all.add( this ) }
	removeFromAll { all.remove( this ) }
	
	makeViews { |bounds|
		RoundView.useWithSkin( skin ++ (RoundView.skin ? ()), {
			this.prMakeViews( bounds );
		});
	}
	
	undo { |amt = 1|
		undoManager !? { chain.handleUndo( undoManager.undo( amt ) ) };
	}
	
	getHeight { |units, margin, gap|
		^units.collect({ |unit|
			UGUI.getHeight( unit, 14, margin, gap ) + 14 + gap.y + gap.y;
		}).sum + (4 * (14 + gap.y));
	}
	
	getUnits {
		var units;
		if( this.packUnits ) {
			units = Array( chain.units.size );
			chain.units.do({ |item, i|
				case { i == 0 or: { item.isKindOf( MassEditU ) or: 
						{ item.def.isKindOf( LocalUdef ) } } 
				} {
					units.add( item );
				} { item.def == units.last.def } {
					if( units.last.isKindOf( MassEditU ) ) {
						units.last.units = units.last.units.add(item);
					} {
						units[ units.size - 1 ] = MassEditU([ units.last, item ]);  
					};
				} { units.add( item ); }
			});
			^units;
		} {
			^chain.units
		};
	}
	
	setUnits { |units|
		var actualUnits = [];
		units.do({ |item|
			case { item.isKindOf( MassEditU ) } {
				item.units.do({ |item|
					actualUnits = actualUnits.add( item );
				});
			} {
				actualUnits = actualUnits.add( item );
			};
		});
		chain.units = actualUnits;
	}
	
	packUnits_ { |bool = true|
		packUnits = bool;
		packUnitsDefault = packUnits;
		chain.changed( \units )
	}
	
	canPackUnits {
		var wasPackUnits, res;
		wasPackUnits = packUnits;
		packUnits = true;
		res = this.getUnits.any( _.isKindOf( MassEditU ) );
		packUnits = wasPackUnits;
		^res;
	}
	
	prMakeViews { |bounds|
		var margin = 0@0, gap = 4@4;
		var heights, units;
		var labelWidth, releaseTask;
		var controller;
		var udefController;
		var scoreController;
		// var unitInitFunc;
		
		nowBuildingChain = chain;
		
		labelWidth = 80;
		
		if( RoundView.skin.notNil ) { labelWidth = RoundView.skin.labelWidth ? 80 };
		
		views = ();
		
		originalBounds = bounds.copy;
		
		bounds = bounds ?? { parent.asView.bounds.insetBy(4,4) };
		if( parent.asView.class.name == 'SCScrollTopView' ) {
			bounds.width = bounds.width - 12;
		};
		
		units = this.getUnits;
		
		units.do(_.checkDef);
				
		controller = SimpleController( chain );
		udefController = SimpleController( Udef.all );
		
		composite = CompositeView( parent, bounds ).resize_(5);
		composite.addFlowLayout( margin, gap );
		composite.onClose = { |vw|
			controller.remove; 
			scoreController.remove;
			udefController.remove;
			if( chain.isKindOf( MassEditUChain ) ) { chain.disconnect };
			this.removeFromAll;
			if( composite == vw && { current == this } ) { current = nil } 
		};
		
		// startbutton
		views[ \startButton ] = SmoothButton( composite, 14@14 )
			.label_( ['power', 'power'] )
			.radius_(7)
			.background_( Color.clear )
			.border_(1)
			.hiliteColor_( Color.green )
			.action_( [ { 
					var startAction;
					releaseTask.stop;
					if( chain.releaseSelf or: (chain.dur == inf) ) {
						chain.prepareAndStart;
					} {
						startAction = { 
							chain.start;
							releaseTask = {
								(chain.dur - chain.fadeOut).max(0).wait;
								chain.release;
							}.fork;
						};
						chain.prepare( action: startAction );
							
					};
				}, { 
					releaseTask.stop;
					chain.release 
				} ]
		 	);
		
		composite.decorator.shift( bounds.width - 14 - 80 - 32, 0 );

		if( chain.isKindOf( MassEditUChain ) ) {
			chain.connect;
		} {
			
			composite.decorator.shift( -34, 0 );
			
			undoManager = undoManager ?? { UndoManager() };
			
			if( chain.handlingUndo ) {
				chain.handlingUndo = false;
			} {
				undoManager.add( chain );
			};
			
			UndoView( composite, 30@14, chain, undoManager ).view.resize_(3);
		};
		
		views[ \displayColor ] = UserView( composite, 28@14 )
			.resize_(3)
			.drawFunc_({ |vw|
				var wd = 8, smallRect;
				if( (score ? chain).displayColor.notNil ) {
					Pen.roundedRect(vw.drawBounds, wd);
					(score ? chain).displayColor.penFill(vw.drawBounds, 1, nil, 10) ;
					smallRect = Rect( vw.bounds.width - wd, 0, wd, wd );
					Pen.color = Color.gray(0.66,0.75);
					Pen.addOval( smallRect, 2 );
					Pen.fill;
					Pen.color = Color.black;
					DrawIcon( '-', smallRect );
				} {
					Pen.roundedRect(vw.drawBounds, wd);
					(score ? chain).getTypeColor.penFill( vw.drawBounds );
				};
			})
			.mouseDownAction_({ |vw, x,y|
				var wd = 8, smallRect;
				smallRect = Rect( vw.bounds.width - wd, 0, wd, wd );
				if( smallRect.containsPoint( x@y ) ) {
					 (score ? chain).displayColor = nil; 
					 vw.refresh;
				} {
					if( views[ \colorEditor ].isNil ) { 
						if( (score ? chain).displayColor.isNil or: { 
								(score ? chain).displayColor.class == Color 
							} ) {
								RoundView.pushSkin( skin );
								views[ \colorEditor ] = ColorSpec( 
										(score ? chain).getTypeColor 
									).makeView( "UChain displayColor", 
										action: { |vws, color| 
											(score ? chain).displayColor = color; 
										} 
									);
								views[ \colorEditor ].view.onClose = { 
									views[ \colorEditor ] = nil 
								};
								RoundView.popSkin;
						} {
							"no editor available for %\n".postf( 
								(score ? chain).displayColor.class 
							);
						};
					} {
						views[ \colorEditor ].view.findWindow.front;
					};
				};
			})
			.keyDownAction_({ |vw, a,b,cx| 
				if( cx == 127 ) { (score ? chain).displayColor = nil }; 
			})
			.beginDragAction_({ (score ? chain).displayColor })
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
					(score ? chain).displayColor = View.currentDrag.interpret; 
				} {
					(score ? chain).displayColor = View.currentDrag;
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
		
		if( chain.groups.size > 0 ) {
			views[ \startButton ].value = 1;
		};
			
		composite.decorator.nextLine;
		
		if( score.notNil ) {
			// score name
			StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.string_( "name" )
				.align_( \right );
				
			views[ \name ] = TextField( composite, 84@14 )
				.applySkin( RoundView.skin )
				.string_( score.name )
				.action_({ |tf|
					score.name_( tf.string );
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
					score.startTime_( nb.value );
				});
			
			composite.decorator.shift( -88, 0 );
			
			views[ \startBar ] = TempoBarMapView( composite, 84@14, tempoMap  )
				.applySkin( RoundView.skin )
				.radius_(2)
				.clipLo_(0)
				.visible_( startTimeMode === \bar )
				.action_({ |nb|
					score.startTime_( nb.value );
				});

			views[ \lockStartTime ] = SmoothButton( composite, 14@14 )
				.label_([ 'unlock', 'lock' ])
				.radius_(2)
				.value_( score.lockStartTime.binaryValue )
				.action_({ |bt|
					score.lockStartTime = bt.value.booleanValue;
				});
			
			composite.decorator.nextLine;
		} {	
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
			
			views[ \lockStartTime ] = SmoothButton( composite, 14@14 )
				.label_([ 'unlock', 'lock' ])
				.radius_(2)
				.action_({ |bt|
					chain.lockStartTime = bt.value.booleanValue;
				});
			
			composite.decorator.nextLine;
			
			// duration
			PopUpMenu( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.items_( [ "duration", "endTime", "endBar" ] )
				.canFocus_( false )
				.action_({ |pu|
					durationMode = [ \duration, \endTime, \endBar ][ pu.value ];
					views[ \dur ].visible = (durationMode === \duration );
					views[ \endTime ].visible = (durationMode === \endTime );
					views[ \endBar ].visible = (durationMode === \endBar );
				})
				.value_( [ \duration, \endTime, \endBar ].indexOf( durationMode ) ? 0 );
				
			views[ \dur ] = SMPTEBox( composite, 84@14 )
				.applySmoothSkin
				.applySkin( RoundView.skin )
				.clipLo_(0)
				.visible_( durationMode === \duration )
				.action_({ |nb|
					if( nb.value == 0 ) {
						chain.dur_( inf );
					} {
						chain.dur_( nb.value );
					};
				});
				
			composite.decorator.shift( -88, 0 );
			
			views[ \endTime ] = SMPTEBox( composite, 84@14 )
				.applySmoothSkin
				.applySkin( RoundView.skin )
				.clipLo_(0)
				.visible_( durationMode === \endTime )
				.action_({ |nb|
					if( nb.value <= chain.startTime ) {
						chain.dur_( inf );
					} {
						chain.dur_( nb.value - chain.startTime );
					};
				});
				
			composite.decorator.shift( -88, 0 );
			
			views[ \endBar ] = TempoBarMapView( composite, 84@14, tempoMap  )
				.applySkin( RoundView.skin )
				.radius_(2)
				.clipLo_(0)
				.visible_( durationMode === \endBar )
				.action_({ |nb|
					if( nb.value <= chain.startTime ) {
						chain.dur_( inf );
					} {
						chain.dur_( nb.value - chain.startTime );
					};
				});
				
			views[ \infDur ] = SmoothButton( composite, 25@14 )
				.border_( 1 )
				.radius_( 3 )
				.label_( [ "inf", "inf" ] )
				.hiliteColor_( Color.green )
				.action_({ |bt|
					var dur;
					switch( bt.value, 
						0, { dur = views[ \dur ].value;
							if( dur == 0 ) {
								dur = 1;
							};
							chain.dur_( dur ) },
						1, { chain.dur_( inf ) }
					);
			});
	
			views[ \fromSoundFile ] = SmoothButton( composite, 90@14 )
				.border_( 1 )
				.radius_( 3 )
				.label_( "from soundFile" )
				.action_({ chain.useSndFileDur });
				
			composite.decorator.nextLine;
			
			// fadeTimes
			StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.string_( "fadeTimes" )
				.align_( \right );
			
			views[ \fadeIn ] = SmoothNumberBox( composite, 40@14 )
				.clipLo_(0)
				.scroll_step_(0.1)
				.action_({ |nb|
					chain.fadeIn_( nb.value );
				});
				
			views[ \fadeOut ] = SmoothNumberBox( composite, 40@14 )
				.clipLo_(0)
				.scroll_step_(0.1)
				.action_({ |nb|
					chain.fadeOut_( nb.value );
				});
				
			views[ \releaseSelf ] = SmoothButton( composite, 70@14 )
				.border_( 1 )
				.radius_( 3 )
				.label_( [ "releaseSelf", "releaseSelf" ] )
				.hiliteColor_( Color.green )
				.action_({ |bt|
					chain.releaseSelf = bt.value.booleanValue;
				});
				
			composite.decorator.nextLine;

			StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.string_( "fade curves" )
				.align_( \right );

			views[ \fadeInCurve ] = SmoothNumberBox( composite, 40@14 )
				.clipLo_(-20)
			    .clipHi_(20)
				.scroll_step_(0.1)
				.action_({ |nb|
					chain.fadeInCurve_( nb.value );
				});

			views[ \fadeOutCurve ] = SmoothNumberBox( composite, 40@14 )
				.clipLo_(-20)
			    .clipHi_(20)
				.scroll_step_(0.1)
				.action_({ |nb|
					chain.fadeOutCurve_( nb.value );
				});

			composite.decorator.nextLine;
		};
		
		// gain
		StaticText( composite, labelWidth@14 )
			.applySkin( RoundView.skin )
			.string_( "gain" )
			.align_( \right );
		
		views[ \gain ] = SmoothNumberBox( composite, 40@14 )
			.clipHi_(24) // just to be safe)
			.action_({ |nb|
				chain.setGain( nb.value );
			});
			
		views[ \muted ] = SmoothButton( composite, 40@14 )
			.border_( 1 )
			.radius_( 3 )
			.label_( [ "mute", "mute" ] )
			.hiliteColor_( Color.red )
			.action_({ |bt|
				switch( bt.value, 
					0, { chain.muted = false },
					1, { chain.muted = true }
				);
			});
			
		controller
			.put( \start, { views[ \startButton ].value = 1 } )
			.put( \end, { 
				if( units.every({ |unit| unit.synths.size == 0 }) ) {
					views[ \startButton ].value = 0;
					releaseTask.stop;
				};
			} )
			.put( \gain, { views[ \gain ].value = chain.getGain } )
			.put( \muted, { views[ \muted ].value = chain.muted.binaryValue } )
			.put( \units, { 
				if( composite.isClosed.not ) {
					{
						scrollViewOrigin = this.scrollView.visibleOrigin;
						composite.children[0].focus; // this seems to prevent a crash..
						composite.remove;
						if( chain.class == MassEditUChain ) {
							chain.init;
						};
						this.makeViews( originalBounds );
						this.makeCurrent;
						this.addToAll;
					if( autoRestart and: { chain.isPlaying } ) {
						chain.release(0.5);
						fork{
							while( { chain.isPlaying } ) {
								0.01.wait;
							};
							//currently there's no way to know the startpos so it will start from 0
							chain.prepareAndStart(chain.lastTarget)
						}
					}
					}.defer(0.01);
				};
			})
			.put( \init, { 
				if( composite.isClosed.not ) {
					{
						scrollViewOrigin = this.scrollView.visibleOrigin;
						composite.children[0].focus; // this seems to prevent a crash..
						composite.remove;
						this.makeViews( originalBounds );
						this.makeCurrent;
						this.addToAll;
					}.defer(0.01);
				};
			});
			
		udefController.put( \added, { |obj, msg, def| 
			if( chain.units.any({ |u| u.defName == def.name }) ) {
				chain.changed( \units );
			};
		} );
			
		if( score.isNil ) {
			controller
				.put( \displayColor, { { views[ \displayColor ].refresh; }.defer; } )
				.put( \startTime, { 
					views[ \startTime ].value = chain.startTime ? 0; 
					views[ \startBar ].value = chain.startTime ? 0;
					if( chain.dur == inf ) {
						views[ \endTime ].value = chain.startTime ? 0;
						views[ \endBar ].value = chain.startTime ? 0; 
					} {
						views[ \endTime ].value = (chain.startTime + chain.dur) ? 0; 
						views[ \endBar ].value = (chain.startTime + chain.dur) ? 0; 
					};
				})
				.put( \lockStartTime, {
					views[ \lockStartTime ].value = chain.lockStartTime.binaryValue;
				})
				.put( \dur, { var dur;
					dur = chain.dur;
					if( dur == inf ) {
						views[ \dur ].enabled = false; // don't set value
						views[ \endTime ].enabled = false; // don't set value
						views[ \endBar ].enabled = false; // don't set value
						views[ \infDur ].value = 1;
						views[ \releaseSelf ].hiliteColor = Color.green.alpha_(0.25);
						views[ \releaseSelf ].stringColor = Color.black.alpha_(0.5);
					} {
						views[ \dur ].enabled = true;
						views[ \endTime ].enabled = true;
						views[ \endBar ].enabled = true;
						views[ \dur ].value = dur;
						views[ \endTime ].value = chain.startTime + dur;
						views[ \endBar ].value = chain.startTime + dur;
						views[ \infDur ].value = 0;
						views[ \releaseSelf ].hiliteColor = Color.green.alpha_(1);
						views[ \releaseSelf ].stringColor = Color.black.alpha_(1);
					};
					{ views[ \displayColor ].refresh; }.defer;
				})
				.put( \fadeIn, { views[ \fadeIn ].value = chain.fadeIn })
				.put( \fadeOut, { views[ \fadeOut ].value = chain.fadeOut })
				.put( \fadeInCurve, { views[ \fadeInCurve ].value = chain.fadeInCurve })
				.put( \fadeOutCurve, { views[ \fadeOutCurve ].value = chain.fadeOutCurve })
				.put( \releaseSelf, {  
					views[ \releaseSelf ].value = chain.releaseSelf.binaryValue;
					{ views[ \displayColor ].refresh; }.defer; 
				})
		} {
			scoreController = SimpleController( score );
			scoreController
				.put( \displayColor, { { views[ \displayColor ].refresh; }.defer; } )
				.put( \startTime, { 
					views[ \startTime ].value = score.startTime ? 0; 
					views[ \startBar ].value = score.startTime ? 0;
				})
				.put( \lockStartTime, {
					views[ \lockStartTime ].value = score.lockStartTime.binaryValue;
				});
			score.changed( \startTime );
			score.changed( \lockStartTime );
		};
		
		chain.changed( \gain );
		chain.changed( \muted );
		chain.changed( \startTime );
		chain.changed( \lockStartTime );
		chain.changed( \dur );
		chain.changed( \fadeIn );
		chain.changed( \fadeOut );
		chain.changed( \fadeInCurve );
		chain.changed( \fadeOutCurve );
		chain.changed( \releaseSelf );
		
		composite.getParents.last.findWindow !? _.toFrontAction_({ 
			this.makeCurrent;
		});
		
		uguis = this.makeUnitViews(units, margin, gap );
		
		nowBuildingChain = nil;
	}
	
	makeUnitHeader { |units, margin, gap|
		var comp, header, min, io, defs, mapdefs, code;
		var notMassEdit, headerInset = 0;
		
		notMassEdit = chain.class != MassEditUChain;
		
		comp = CompositeView( composite, (composite.bounds.width - (margin.x * 2))@16 )
			.resize_(2);
			
		if( notMassEdit && { this.canPackUnits }) {
			RoundButton( comp, 13 @ 13 )
				.border_(0)
				.background_( nil )
				.label_([ 'down', 'play' ])
				.hiliteColor_(nil)
				.value_( packUnits.binaryValue )
				.action_({ |bt|
					chain.handlingUndo = true; // don't add a state
					this.packUnits = bt.value.booleanValue;
				});
			headerInset = 14;
		};
		
		header = StaticText( comp, comp.bounds.moveTo(0,0).insetAll( headerInset, 0,0,0 ) )
				.applySkin( RoundView.skin )
				.string_( if( notMassEdit ) { " units" } { " units (accross multiple events)" } )
				.align_( \left )
				.resize_(2);
				
		if( notMassEdit ) {
            io = SmoothButton( comp, Rect( comp.bounds.right - 60, 1, 60, 12 ) )
                .label_( "i/o" )
                .border_( 1 )
                .radius_( 2 )
                .action_({
                    var parent;
                    parent = composite.parent;
                    {
                        composite.remove;
                        UChainIOGUI( parent, originalBounds, chain );
                    }.defer(0.01);

                }).resize_(3);
            code = SmoothButton( comp,
                    Rect( comp.bounds.right - (40 + 4 + 60), 1, 40, 12 ) )
                .label_( "code" )
                .border_( 1 )
                .radius_( 2 )
                .action_({
                    var parent;
                    parent = composite.parent;
                    {
                        composite.remove;
                        UChainCodeGUI( parent, originalBounds, chain );
                    }.defer(0.01);
                }).resize_(3);
		};
		
		defs = SmoothButton( comp, 
				Rect( comp.bounds.right - (
					2 + 40 + (notMassEdit.binaryValue * (4 + 60 + 4 + 40))
					), 1, 42, 12 
				) 
			)
			.label_( "udefs" )
			.border_( 1 )
			.radius_( 2 )
			.action_({
				UdefsGUI();
			}).resize_(3);
			
		CompositeView( comp, Rect( 0, 14, (composite.bounds.width - (margin.x * 2)), 2 ) )
			.background_( Color.black.alpha_(0.25) )
			.resize_(2);

	}
	
	makeUnitSubViews { |scrollView, units, margin, gap|
		var unitInitFunc;
		var comp, uview;
		var addLast, ug, header;
		var width;
		var notMassEdit;
		var scrollerMargin = 16;
		var realIndex = 0;
		var massEditWindow;
		
		if( GUI.id == \qt ) { scrollerMargin = 20 };
		
		notMassEdit = chain.class != MassEditUChain;
		
		
		width = scrollView.bounds.width - scrollerMargin - (margin.x * 2);
		
		unitInitFunc = { |unit, what ...args|
			if( what === \init ) { // close all views and create new
				chain.changed( \units );
			};
		};
		
		if( units.size == 0 ) {
			comp = CompositeView( scrollView, width@100 )
				.resize_(2);
			
			header = StaticText( comp, comp.bounds.width @ 14 )
				.applySkin( RoundView.skin )
				.string_( " empty: drag unit or Udef here" )
				.background_( Color.yellow.alpha_(0.25) )
				.resize_(2)
				.font_( 
					(RoundView.skin.tryPerform( \at, \font ) ?? 
						{ Font( Font.defaultSansFace, 12) }).boldVariant 
				);
				
			uview = UserView( comp, comp.bounds.width @ 100 );
			uview.background_( Color.white.alpha_(0.25) );
				
			uview.canReceiveDragHandler_({ |sink|
				var drg;
				drg = View.currentDrag;
				case { drg.isUdef } 
					{ true }
					{ drg.isKindOf( UnitRack ) }
                    { true }
					{ [ Symbol, String ].includes( drg.class ) }
					{ Udef.all.keys.includes( drg.asSymbol ) }
					{ drg.isKindOf( U ) }
					{ true }
					{ false }
			})
			.receiveDragHandler_({ |sink, x, y|
					case { View.currentDrag.isKindOf( U ) } {
						chain.units = [ View.currentDrag.deepCopy ];
					}{ View.currentDrag.isUdef }{
						chain.units = [ U( View.currentDrag ) ];
					}{ View.currentDrag.isKindOf( UnitRack ) } {
                        chain.units = View.currentDrag.units;
                    }{   [ Symbol, String ].includes( View.currentDrag.class )  } {
						chain.units = [ U( View.currentDrag.asSymbol ) ];
					};
			})
		};
		
		ug = units.collect({ |unit, i|
			var header, comp, uview, plus, min, defs, io;
			var addBefore, indexLabel, ugui;
			var currentUMaps;
			var massEditWindowButton;
				
			indexLabel = realIndex.asString;
			
			if( notMassEdit && { unit.isKindOf( MassEditU ) } ) {
				realIndex = realIndex + unit.units.size;
				indexLabel = indexLabel ++ ".." ++ (realIndex -1);
			} {
				realIndex = realIndex + 1;
			};
			
			addBefore = UserView( scrollView, width@6 )
				.resize_(2);
			
			if( notMassEdit ) {
				addBefore.background_( Color.white.alpha_(0.25) );
				addBefore.canReceiveDragHandler_({ |sink|
						var drg;
						drg = View.currentDrag;
						case { drg.isUdef } 
							{ true }
							{ drg.isKindOf( UnitRack ) }
	                        { true }
							{ [ Symbol, String ].includes( drg.class ) }
							{ Udef.all.keys.includes( drg.asSymbol ) }
							{ drg.isKindOf( U ) }
							{ true }
							{ false }
					})
					.receiveDragHandler_({ |sink, x, y|
							var ii;
							case { View.currentDrag.isKindOf( U ) } {
								ii = units.indexOf( View.currentDrag );
								if( ii.notNil ) {
									units[ii] = nil;
									units.insert( i, View.currentDrag );
									this.setUnits( units.select(_.notNil) );
								} {
									this.setUnits( 
										units.insert( i, View.currentDrag.deepCopy ) 
									);
								};
							} { View.currentDrag.isUdef } {
								this.setUnits( units.insert( i, U( View.currentDrag ) ) );
							}{ View.currentDrag.isKindOf( UnitRack ) } {
								this.setUnits( 
									units[..i-1] ++ View.currentDrag.units ++ units[i..] 
								);                       
	                           }{   [ Symbol, String ].includes( View.currentDrag.class )  } {
								this.setUnits( 
									units.insert( i, U( View.currentDrag.asSymbol ) )
								);
							};
					});
			} {
				addBefore.canFocus = false;
			};
		
			comp = CompositeView( scrollView, width@14 )
				.resize_(2);
			
			header = StaticText( comp, comp.bounds.moveTo(0,0) )
				.applySkin( RoundView.skin )
				.string_( " " ++ indexLabel ++ ": " ++ if(unit.def.class == LocalUdef){"[Local] "}{""}++unit.defName )
				.background_( if( notMassEdit ) 
					{ Color.white.alpha_(0.5) }
					{ Color.white.blend( Color.yellow, 0.33 ).alpha_(0.5) }
				)
				.resize_(2)
				.font_( 
					(RoundView.skin.tryPerform( \at, \font ) ?? 
						{ Font( Font.defaultSansFace, 12) }).boldVariant 
				);
			
			
			//if( chain.class != MassEditUChain )
				
			uview = UserView( comp, comp.bounds.moveTo(0,0) );
			
			uview.canReceiveDragHandler_({ |sink|
				var drg;
				drg = View.currentDrag;
				case { drg.isUdef } 
					{ true }
					{ drg.isKindOf( UnitRack ) }
                        { true }
					{ [ Symbol, String ].includes( drg.class ) }
					{ Udef.all.keys.includes( drg.asSymbol ) }
					{ drg.isKindOf( U ) }
					{ true }
					{ false }
			})
			.receiveDragHandler_({ |sink, x, y|
				var u, ii;
				case { View.currentDrag.isKindOf( U ) } {
					u = View.currentDrag;
					ii = units.indexOf( u );
					if( ii.notNil ) { 
						units[ii] = unit; 
						units[i] = u;
					} {
						units[ i ] = u.deepCopy;
					};
					this.setUnits( units );
					
				} { View.currentDrag.isKindOf( UnitRack ) } {
                        this.setUnits( units[..i-1] ++ View.currentDrag.units ++ units[i+1..] );
                    } { View.currentDrag.isUdef } {
					unit.def = View.currentDrag;
				} {   [ Symbol, String ].includes( View.currentDrag.class )  } {
					unit.def = View.currentDrag.asSymbol.asUdef;
				};
			})
			.beginDragAction_({ 
				unit;
			});
			
			if( unit.isKindOf( MassEditU ) ) {
				massEditWindowButton = SmoothButton( comp, 
					Rect( comp.bounds.right - 
						((18 + 2) + if( notMassEdit){12 + 4 + 12}{0}), 
						1, 18, 12 ) 
					)
					.label_( 'up' )
					.border_( 1 )
					.radius_( 2 )
					.action_({
						var allUnits, userClosed = true;
						if( massEditWindow.notNil && { massEditWindow.isClosed.not }) {
							massEditWindow.close;
						};
						RoundView.pushSkin( skin );
						massEditWindow = Window( unit.defName, 
							this.window.bounds.moveBy( this.window.bounds.width + 10, 0 ),
							scroll: true ).front;
						massEditWindow.addFlowLayout;
						comp.onClose_({ 
							if( massEditWindow.notNil && { massEditWindow.isClosed.not }) {
								userClosed = false;
								massEditWindow.close;
							};	
						});
						allUnits = unit.units.collect({ |item, ii|
							var ugui;
							if( notMassEdit ) { ii = ii + (realIndex - unit.units.size) };
							StaticText( massEditWindow, 
									(massEditWindow.bounds.width - 8 - scrollerMargin) @ 14 )
								.applySkin( RoundView.skin )
								.string_( " " ++ ii ++ ": " ++ item.defName )
								.background_( Color.white.alpha_(0.5) )
								.resize_(2)
								.font_( 
									(RoundView.skin.tryPerform( \at, \font ) ?? 
										{ Font( Font.defaultSansFace, 12) }).boldVariant 
								);
							massEditWindow.view.decorator.nextLine;
							ugui = item.gui( massEditWindow );
							ugui.mapSetAction = { 
								chain.changed( \units );
							};
							[ item ] ++ item.getAllUMaps;
						}).flatten(1);
						allUnits.do({ |item|
							item.addDependant( unitInitFunc )
						});
						massEditWindowIndex = i;
						massEditWindow.onClose_({
							allUnits.do(_.removeDependant(unitInitFunc));
							if( userClosed ) {
								massEditWindowIndex = nil;
							};
						});
						RoundView.popSkin( skin );
					}).resize_(3);
					
				if( massEditWindowIndex == i ) {
					massEditWindowButton.doAction;
				};
			} {
				if( massEditWindowIndex == i ) {
					massEditWindowIndex = nil;
				};
			};
			
			if( notMassEdit ) {	
				min = SmoothButton( comp, 
							Rect( comp.bounds.right - (12 + 4 + 12), 1, 12, 12 ) )
						.label_( '-' )
						.border_( 1 )
						.action_({
							var u = unit;
							if( u.isKindOf( MassEditU ) ) {
								u = u.units.last;
							};
							chain.units = chain.units.select(_ != u);
						}).resize_(3);
				
				if( units.size == 1 ) {
					min.enabled = false;
				};
				
				plus = SmoothButton( comp, 
						Rect( comp.bounds.right - (12 + 2), 1, 12, 12 ) )
					.label_( '+' )
					.border_( 1 )
					.action_({
						var copy;
						if( unit.isKindOf( MassEditU ) ) {
							unit.units = unit.units.add( unit.units.last.deepCopy.increaseIOs )
						} {
							units = units.insert( i+1, unit.deepCopy.increaseIOs );
						};
						this.setUnits( units );
					}).resize_(3);
					
				if(  unit.isKindOf( MassEditU ).not && { unit.audioOuts.size > 0 } ) {					SmoothButton( comp, 
						Rect( comp.bounds.right - (45 + 2 + 12 + 4 + 12), 
							1, 45, 12 ) 
						)
						.label_( "bounce" )
						.border_( 1 )
						.radius_( 2 )
						.action_({
							Dialog.savePanel( { |path|
								chain.bounce( chain.units.indexOf( unit ), path );
							});
						}).resize_(3);
				};
			};	
					
			unit.addDependant( unitInitFunc );
			currentUMaps = unit.getAllUMaps;
			currentUMaps.do(_.addDependant( unitInitFunc ));
			header.onClose_({ 
				unit.removeDependant( unitInitFunc );
				currentUMaps.do(_.removeDependant( unitInitFunc ));
			});
			ugui = unit.gui( scrollView, 
				scrollView.bounds.copy.width_( 
					scrollView.bounds.width - scrollerMargin - (margin.x * 2) 
				)  
			);
			ugui.mapSetAction = { chain.changed( \units ) };
			ugui;
		});
		
		if( notMassEdit && { units.size > 0 } ) {
			addLast = UserView( scrollView, width@6 )
				.resize_(2)
				.background_( Color.white.alpha_(0.25) )
				.canFocus_(false);
					
			addLast.canReceiveDragHandler_({ |sink|
					var drg;
					drg = View.currentDrag;
					case { drg.isUdef } 
						{ true }
						{ drg.isKindOf( UnitRack ) }
                        { true }
						{ [ Symbol, String ].includes( drg.class ) }
						{ Udef.all.keys.includes( drg.asSymbol ) }
						{ drg.isKindOf( U ) }
						{ true }
						{ false }
				})
				.receiveDragHandler_({ |sink, x, y|
						var ii;
						case { View.currentDrag.isKindOf( U ) } {
							ii = units.indexOf( View.currentDrag );
							if( ii.notNil ) {
								units[ii] = nil;
								this.setUnits( units.select(_.notNil) ++
									[ View.currentDrag ] );
							} {
								this.setUnits( units ++ [ View.currentDrag.deepCopy ] );
							};
							
						} { View.currentDrag.isUdef } {
							chain.units = chain.units ++ [ U( View.currentDrag ) ];
						}{ View.currentDrag.isKindOf( UnitRack ) } {
                            chain.units = chain.units ++ View.currentDrag.units;
                        }{   [ Symbol, String ].includes( View.currentDrag.class )  } {
							chain.units = chain.units ++ [ U( View.currentDrag.asSymbol ) ];
						};
				});
		};
		
		if( scrollViewOrigin.notNil ) {
			if( GUI.id == \qt ) {
				{ 
					scrollView.visibleOrigin = scrollViewOrigin; 					scrollViewOrigin = nil;
				}.defer(0.1);
			} {
				scrollView.visibleOrigin = scrollViewOrigin; 				scrollViewOrigin = nil;
			};
		};
		
		^ug;
		
	}

	makeUnitViews { |units, margin, gap|
		
		var scrollView, presetManagerHeight = 0, notMassEdit;
		
		notMassEdit = chain.class != MassEditUChain;
		
		if( notMassEdit ) {
			presetManagerHeight = PresetManagerGUI.getHeight + 12;
		};
		
		this.makeUnitHeader( units, margin, gap );
		
		composite.decorator.nextLine;
		
		scrollView = ScrollView( composite, 
			(composite.bounds.width) 
				@ (composite.bounds.height - 
					( presetManagerHeight ) -
					( composite.decorator.top )
				)
		);
		
		scrollView
			.hasBorder_( false )
			.hasHorizontalScroller_( false )
			.autohidesScrollers_( false )
			.resize_(5)
			.addFlowLayout( margin, gap );
			
		this.scrollView = scrollView;
			
		if( notMassEdit ) {
			
			CompositeView( composite, (composite.bounds.width - (margin.x * 2)) @ 2 )
				.background_( Color.black.alpha_(0.25) )
				.resize_(8);
			
			presetView = PresetManagerGUI( 
				composite, 
				composite.bounds.width @ PresetManagerGUI.getHeight,
				UChain.presetManager,
				chain
			).resize_(7)
		};
			
		^this.makeUnitSubViews( scrollView, units, margin, gap );
	}
	
	remove {
		composite.remove;
	}
	
	window { 
		^composite.getParents.last.findWindow;
	}
	windowName { 
		^this.window.name;
	}
	
	windowName_ { |name| 
		this.window.name = name;
	}
	
	close {
		if( composite.isClosed.not ) {
			composite.getParents.last.findWindow.close;
		};
	}
	
	resize_ { |resize| composite.resize_(resize) }
	
	font_ { |font| uguis.do({ |vw| vw.font = font }); }
		
	view { ^composite }
}

+ UChain {
	gui { |parent, bounds, score| ^UChainGUI( parent, bounds, this, score ) }
}