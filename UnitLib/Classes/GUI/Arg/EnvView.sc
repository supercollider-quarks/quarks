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

EnvPlotView {
	
	var <env, <view, <plotView, <ctrl;
	var <active = true;
	var <selected;
	var <selectedLine;
	var <>font, <>viewHeight = 14, <resize = 5;
	var <min = 0, <max = 1;
	var <showLegend = true;
	var <>action;
	var <>editMode = \move;
	var <>fixedDuration = true;
	var hitPoint;
	
	*new { |parent, bounds, env|
		^super.newCopyArgs( env ).init.makeView( parent, bounds );
	}
	
	init {
		env = env ?? { Env() };
		font = RoundView.skin !? { RoundView.skin.font } ?? { Font( Font.defaultSansFace, 10 ); };
	}
	
	*viewNumLines { ^6 }
	
	removeCtrl {
		ctrl.remove;
		ctrl = nil;
	}
	
	addCtrl {
		ctrl.remove;
		ctrl = SimpleController( env )
			.put( \times, { this.refresh })
			.put( \levels, { this.refresh })
			.put( \curves, { this.refresh });
	}
	
	refresh {
		if( plotView.userView.view.isClosed.not ) {
			{ plotView.refresh }.defer;
		};
	}
	
	selected_ { |index| 
		selected = index; 
		this.changed( \selected, selected );
		this.refresh 
	}
	
	selectedLine_ { |index| 
		selectedLine = index; 
		this.changed( \selectedLine, selectedLine );
		this.refresh 
	}
	
	active_ { |bool=true|
		active = bool;
		this.changed( \active );
		this.refresh;
	}
	
	showLegend_ { |bool=true|
		showLegend = bool;
		this.changed( \showLegend );
		this.refresh;
	}
	
	env_ { |new|
		if( new.notNil ) {
			env = new;
			if( plotView.userView.view.isClosed.not ) {
				this.addCtrl;
			};
			this.changed( \env, env );
			this.refresh;
			action.value( this, env );
		};
	}
	
	resize_ { |new|
		resize = new;
		view.resize_( resize );
	}
	
	getPoints { |bounds|
		var dur;
		dur = env.times.sum;
		bounds = bounds ? plotView.fromBounds;
		^env.unmappedLevels.collect({ |level, i|
			Point(
				(env.times[..i-1].sum / dur) * bounds.width, 
				level.linlin( min, max, bounds.height, 0 )
			)
		});
	}
	
	setPoints { |pts, bounds|
		var dur, levels, times;
		dur = env.times.sum;
		pts = pts.sort({ |a,b| a.x <= b.x });
		#times, levels = pts.collect({ |pt|
			[
				(pt.x / bounds.width) * dur,
				pt.y.linlin( 0, bounds.height, max, min )
			]
		}).flop;
		env.unmappedLevels = levels;
		env.times = times.differentiate[1..];
		env.changed( \levels );
	}
	
	makeView { |parent, bounds|
		
		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
		
		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.resize_( resize ? 5 );
		
		this.addCtrl;
			
		plotView = ScaledUserView.withSliders( view, bounds.moveTo(0,0),  bounds.moveTo(0,0))
			.resize_( 5 )
			.scaleVEnabled_( false )
			.moveVEnabled_( false )
			.onClose_({ this.removeCtrl; });
			
		
		plotView.mouseDownAction = { |vw,x,y,mod|
			var bounds, pts;
			
			if( active ) {
				bounds = vw.fromBounds.moveTo(0,0);
				hitPoint = (x@y);
				pts = this.getPoints( bounds );
				this.selectedLine = nil;
				this.selected = pts.detectIndex({ |ptx|
					ptx.dist( hitPoint ) <= (vw.pixelScale.x * 6);
				});
				if( selected.isNil && { 
						(
							env.at( x.linlin(0,bounds.width,0,env.times.sum) )
								.linlin(min,max,bounds.height,0) - y 
						).abs <  (vw.pixelScale.x * 6)
					} ) {
					this.selectedLine = pts.lastIndexForWhich({ |ptx| ptx.x < hitPoint.x });
				};
			};
		};
			
		plotView.mouseMoveAction = { |vw,x,y,mod|
			var bounds;
			var pt, pts, oldX, curves;
			
			if( active ) {
				bounds = vw.fromBounds.moveTo(0,0);
				
				pts = this.getPoints( bounds );
				
				if( selected.notNil ) {
					switch( editMode,
						\move, {
							pt = pts[ selected ];
							pt.y = pt.y - (hitPoint.y - y);
							if( selected != 0  and: { 
								if( fixedDuration ) { selected != (pts.size - 1) } { true }
								} ) {
								pt.x = pt.x - (hitPoint.x - x);
							};
							
							hitPoint = x@y;
							pts = pts.sort({ |a,b| a.x <= b.x });
							this.selected = pts.indexOf( pt );
							this.setPoints( pts, bounds );
							
							action.value( this, env );
						},
						\elastic, {
							pt = pts[ selected ];
							oldX = pt.x;
							pt.y = pt.y - (hitPoint.y - y);
							if( selected != 0 and: { 
								if( fixedDuration ) { selected != (pts.size - 1) } { true }
								} ) {
								pt.x = pt.x - (hitPoint.x - x);
							};
							pts[..selected-1].do({ |item, i|
								item.x = item.x.linlin(0,oldX,0,pt.x);							});
							pts[selected+1..].do({ |item, i|
								item.x = item.x.linlin(oldX,bounds.width,pt.x,bounds.width);
							});
							hitPoint = x@y;
							pts = pts.sort({ |a,b| a.x <= b.x });
							this.selected = pts.indexOf( pt );
							this.setPoints( pts, bounds );
							
							action.value( this, env );
						},
						\extend, {
							pt = pts[ selected ];
							oldX = pt.x;
							pt.y = pt.y - (hitPoint.y - y);
							if( selected != 0 and: { selected != (pts.size - 1)}) {
								pt.x = pt.x - (hitPoint.x - x);
							};
							pts[selected+1..].do({ |item, i|
								item.x = item.x + (pt.x - oldX);
							});
							hitPoint = x@y;
							pts = pts.sort({ |a,b| a.x <= b.x });
							this.selected = pts.indexOf( pt );
							this.setPoints( pts, bounds );
							
							action.value( this, env );
						});
				};
				
				if( selectedLine.notNil ) {
					curves = env.curves.asCollection.wrapExtend(env.times.size);
					if( Env.shapeNumber( curves[selectedLine] ) == 1 ) {
						 curves[selectedLine] = 0;
					};
					if( curves[selectedLine].isNumber ) {
						if( pts[selectedLine].y > (pts[selectedLine+1].y) ) {
							curves[selectedLine] = curves[selectedLine] - 
								((( hitPoint.y - y ) / bounds.height) * 10);
						} {
							curves[selectedLine] = curves[selectedLine] + 
								((( hitPoint.y - y ) / bounds.height) * 10);
						};
						hitPoint = x@y;
						env.curves = curves;
						env.changed( \curves );
						action.value( this, env );
					};
				};
			};
			
			
		};
		
		plotView.view
			.beginDragAction_({ env })
			.canReceiveDragHandler_({ |vw|
				var drg = View.currentDrag;
				drg.isKindOf( Env ) or: { 
					drg.isString && { 
						{ drg.interpret }.try !? { |obj| obj.isKindOf( Env ) } ? false;
					};
				};
			})
			.receiveDragHandler_({ |vw|
				var obj;
				var drg = View.currentDrag;
				if( drg.isString ) {
					this.env = drg.interpret;
				} {
					this.env = View.currentDrag.deepCopy;
				};
			});
		
		plotView.beforeDrawFunc = { |vw|
			var dur;
			dur = env.times.sum;
			plotView.fromBounds = Rect( 0, 0, dur, 
				(dur * vw.bounds.height / vw.bounds.width) / vw.scaleH
			);
		};
		
		plotView.drawFunc = { |vw|
			var freqs, svals, bounds, sline, slineX;
			var pts, strOffset = 11;
			var res;
			var pscale;
			
			if( GUI.id === 'swing' ) { strOffset = 14 };
			
			bounds = vw.fromBounds.moveTo(0,0);
			
			res = ((vw.bounds.width / bounds.width) * vw.scaleH).round(1);
			
			vw.drawTimeGrid;
			
			////// PREPARATION ///////
			
			pscale = vw.pixelScale.x;
			
			Pen.width = pscale / 2;
			Pen.color = Color.gray.alpha_(0.25);
			[0.25,0.5,0.75].do({ |item|
				Pen.line( 0@(bounds.height * item), (bounds.width)@(bounds.height * item) );
			});
			Pen.stroke;
			
			Pen.width = pscale;
			
			svals = env.discretize( bounds.width * res ).linlin(min,max, bounds.height, 0, \none);
			
			// get draggable points
			pts = this.getPoints( bounds );
			
			if( selectedLine.notNil ) {
				slineX = (pts.clipAt(selectedLine).x * res).asInt;
				sline = svals
					[slineX..(pts.clipAt(selectedLine+1).x * res).ceil.asInt-1];
				Pen.color = Color.yellow;
				Pen.width = pscale * 3;
				Pen.moveTo( slineX.linlin(0,svals.size, 0,bounds.width)@(sline[0]) );
				sline[1..].do({ |val, i|
					Pen.lineTo(( (i+1+slineX).linlin(0,svals.size, 0,bounds.width))@val );
				});
				Pen.stroke;
			};

			Pen.color = Color.blue(0.5);
			Pen.width = pscale;
			Pen.moveTo( 0@(svals[0]) );
			svals[1..].do({ |val, i|
				Pen.lineTo( (i+1).linlin(0,svals.size, 0,bounds.width)@val );
			});
			Pen.stroke;
			
			pts.do({ |pt, i|
				Pen.color = Color.hsv( i.linlin( 0, pts.size, 0, 1 ), 0.22, 0.66, 1 );
				Pen.addArc( pt, 5 * pscale, 0, 2pi );
				Pen.fill;
				if( selected == i ) { 
					Pen.color = Color.yellow;
					Pen.addArc( pt, 4 * pscale, 0, 2pi );
					Pen.fill;
				};
			});
			
		};
	}
}

EnvEditView {
	
	var <env, <spec, <view, <comp, <listView, <argViews, <rangeViews, <ctrl, <views;
	var <>maxSize = 32;
	var <>viewHeight = 14;
	var <resize = 8;
	var <>font;
	var <selected = 0;
	var <>action;
	var <>size;
	var <>skin;
	
	*new { |parent, bounds, env, spec|
		^super.newCopyArgs( env, spec ).init.makeView( parent, bounds );
	}
	
	init {
		env = env ?? { Env() };
		font = RoundView.skin !? { RoundView.skin.font } ?? { Font( Font.defaultSansFace, 10 ); };
	}
	
	removeCtrl {
		ctrl.remove;
		ctrl = nil;
	}
	
	addCtrl {
		ctrl.remove;
		ctrl = SimpleController( env );
		ctrl.put( \times, { this.update })
			.put( \levels, { this.update })
			.put( \curves, { this.update });
	}
	
	update {
		var curves;
		if( listView.isClosed.not ) {
			if( size != env.levels.size ) {
				{ this.rebuildViews }.defer;
			} {	
				curves = env.curves.asCollection.wrapExtend( env.levels.size - 1 );
				views[ \duration ].value = env.times.sum;
				rangeViews[ \range ].value = spec.asSpec.map([ 
					env.unmappedLevels.minItem, env.unmappedLevels.maxItem 
				]);
				argViews.do({ |item, i|
					var first;
					first = (i == 0);
					item.level.value = spec.asSpec.map( env.unmappedLevels[i] );
					if( first.not ) {
						item.time
							.value_( env.times[..i-1].sum )
							.clipLo_( env.times[..i-2].sum )
							.clipHi_( if( env.times.size == i ) { inf } { env.times[..i].sum } );
						if( curves[i-1].isNumber ) {
							item.curveNumber.stringColor = Color.black;
							item.curveNumber.value = curves[i-1];
							{ item.curveType.value = 5 }.defer;
						} {
							item.curveNumber.stringColor = Color.black.alpha_(0.5);
							{ item.curveType.value = Env.shapeNumber( curves[i-1] ) }.defer;
						};
					};
				});
			}
		};
	}
	
	*viewNumLines { ^3 }
	
	selected_ { |new|
		if( selected != new ) {	
			selected = new;
			if( selected.notNil ) {
				argViews.do({ |item, i|
					item[ \comp ].visible = (i == selected);
				});
				rangeViews[ \comp ].visible = false;
				{ 	listView.value = selected; 
					view.view.background = Color.hsv( 
						(selected ? 0).linlin( 0, env.levels.size, 0, 1 ), 
						0.75, 0.5 
					).alpha_( 0.25 );
				}.defer;
			} {
				argViews.do({ |item, i| item[ \comp ].visible = false; });
				rangeViews[ \comp ].visible = true;
				{ 	listView.value = listView.items.size - 1; 
					view.view.background = Color.gray(0.7).alpha_( 0.5 );
				}.defer;
			};
			this.changed( \selected, selected );
		};
	}
	
	env_ { |new|
		if( new.notNil ) {
			env = new;
			if( listView.isClosed.not ) {
				this.addCtrl;
			};
			this.changed( \env, env );
			this.update;
		};
	}
	
	spec_ { |new| spec = new.asSpec }
	
	resize_ { |new|
		resize = new;
		view.resize_( resize );
	}
	
	rebuildViews {
		listView.items = env.levels.collect({ |item, i|
			"node" + i;
		}) ++ [ "all" ];
		selected = selected !? { selected.clip( 0, env.levels.size -1 ) };
		if( selected.isNil ) { listView.value = env.levels.size };
		argViews.do({ |item| item.comp.remove });
		rangeViews !? { rangeViews[ \comp ].remove };
		RoundView.useWithSkin( skin ? (), { this.makeSubViews });
	}
	
	makeView { |parent, bounds|
		if( bounds.isNil ) { bounds = 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
			
		view = EZCompositeView( parent, bounds, margin: 0@2, gap: 2@2 );
		view.resize_( resize ? 5 );
		
		this.addCtrl;
		
		views = ();
		
		// selection
		listView = PopUpMenu( view, 80 @ viewHeight )
			.font_( font )
			.value_( selected ? 0 )
			.applySkin( RoundView.skin)
			.items_( 
				env.levels.collect({ |item, i|
					"node" + i;
				}) ++ [ "all" ] 
			)
			.action_({ |pu|
				if( pu.value == (pu.items.size-1) ) {
					this.selected = nil;
				} {
					this.selected = pu.value;
				};
			});
			
		listView.onClose_({ this.removeCtrl });
		
		// duration
		views[ \durationLabel ] = StaticText( view, 45 @ viewHeight )
				.string_( "duration " )
				.align_( \right )
				.font_( font )
				.applySkin( RoundView.skin );
		
		views[ \duration ] = SMPTEBox( view, 70 @ viewHeight  )
			.applySmoothSkin
			.applySkin( RoundView.skin )
			.value_( env.times.sum )
			.clipLo_( 0.001 )
			.font_( font )
			.action_({ |nb|
				var times;
				times = env.times;
				if( times.mutable.not ) {
					times = times.copy;
				};
				times = times.normalizeSum * nb.value;
				env.times = times;
				env.changed( \times );
				action.value( this, env );
			});
			
		views[ \minus ] = SmoothButton( view, viewHeight @ viewHeight )
			.label_( '-' )
			.mouseUpAction_({
				var which;
				which = selected ? (env.levels.size - 1);
				if( which.notNil && { env.levels.size > 2 }) {
					env.unmappedLevels = env.unmappedLevels.copy.select({ |item, i| i != which });
					if( which == env.times.size ) {
						if( env.times.mutable.not ) { env.times = env.times.copy };
						env.times.pop;
						if( env.curves.size > 0 ) {
							env.curves.pop;
						}
					} {
						env.times = env.times.select({ |item, i| i != which });
						if( env.curves.size > 0 ) {
							env.curves = env.curves.select({ |item, i| i != which });
						}
					};
					{ env.changed( \levels ); }.defer(0.1);
					action.value( this, env );
				};
			});
			
		views[ \plus ] = SmoothButton( view, viewHeight @ viewHeight )
			.label_( '+' )
			.action_({
				var which;
				which = selected ? (env.levels.size - 1);
				if( which.notNil && { env.levels.size < maxSize }) {
					env.unmappedLevels = env.unmappedLevels.collect(_.value)
						.insert( which, env.unmappedLevels[which] );
					env.times = env.times.copy.collect(_.value)
						.insert( which, env.times.clipAt( which ) );
					if( env.curves.size > 0 ) {
						env.curves = env.curves.insert( which, env.curves.clipAt( which ) );
					};
					{ env.changed( \levels ); }.defer(0.1);
					action.value( this, env );
				};
			});
		
		view.view.background = Color.hsv( 
			(selected ? 0).linlin( 0, env.levels.size, 0, 1 ), 
			0.75, 0.5 
		).alpha_( 0.25 );
		
		view.decorator.nextLine;
		
		comp = CompositeView( view, bounds.width @ ((viewHeight + 4) * 2))
			.resize_(2)
			.background_( Color.white.alpha_(0.25) );
			
		skin = RoundView.skin;
		
		this.makeSubViews;
	}
	
	makeSubViews { 
		var bounds;
		
		bounds = view.asView.bounds;
		 
		argViews = [];
		
		size = env.levels.size;
					
		env.unmappedLevels.do({ |level, i|
			var vws, first;
			vws = ();
			
			first = (i == 0);
			
			vws[ \comp ] = CompositeView( comp, comp.bounds.moveTo(0,0) )
				.resize_(2);
				
			vws[ \comp ].addFlowLayout( 2@2, 2@2 );
			
			// level
			StaticText(  vws[ \comp ], 35 @ viewHeight )
				.string_( if( spec.isKindOf( Symbol ) ) { spec ++ " " } { "level " } )
				.align_( \right )
				.font_( font )
				.applySkin( RoundView.skin );
			
			vws[ \level ] = EZSmoothSlider( vws[ \comp ],
				(bounds.width - 43) @ viewHeight, nil, spec.asSpec
			)
				.font_( font )
				.value_( spec.asSpec.map( level ) )
				.action_({ |sl|
					var levels;
					levels = env.unmappedLevels;
					if( levels.mutable.not ) {
						levels = levels.copy;
					};
					levels.put( i, spec.asSpec.unmap(sl.value) );
					env.unmappedLevels = levels;
					env.changed( \levels );
					action.value( this, env );
				});
				
			vws[ \level ].view.resize_(2);
			
			vws[ \comp ].decorator.nextLine;
			
			if( first.not ) {
				
				// time
				StaticText(  vws[ \comp ], 35 @ viewHeight )
					.string_( "time " )
					.align_( \right )
					.font_( font )
					.applySkin( RoundView.skin );
				
				vws[ \time ] = SMPTEBox( vws[ \comp ], 70 @ viewHeight  )
					.applySmoothSkin
					.applySkin( RoundView.skin )
					.value_( env.times[..i-1].sum )
					.clipLo_( env.times[..i-2].sum )
					.clipHi_( if( env.times.size == i ) { inf } { env.times[..i].sum } )
					.font_( font )
					.action_({ |nb|
						var times, max;
						times = env.times;
						if( times.mutable.not ) {
							times = times.copy;
						};
						max = times[..i].sum;
						times.put( i-1, nb.value - (times[..i-2].sum) );
						if(  i < env.times.size ) {
							times.put( i, max - nb.value);
						};
						env.times = times;
						env.changed( \times );
						action.value( this, env );
					});
				
				// curve
				StaticText(  vws[ \comp ], 35 @ viewHeight )
					.string_( "curve " )
					.align_( \right )
					.font_( font )
					.applySkin( RoundView.skin );
				
				vws[ \curveType ] = PopUpMenu( vws[ \comp ], 60 @ viewHeight  )
					.items_([ \step, \lin, \exp, \sine, \welch, \curve, \squared, \cubed ])
					.value_( 
						Env.shapeNumber(env.curves.asCollection.wrapAt(i-1)) 
					)
					.font_( font )
					.action_({ |pu|
						if( pu.value != 5 ) {
							env.curves = env.curves.asCollection
								.wrapExtend( env.levels.size - 1 ).put( i-1, pu.item );
							vws[ \curveNumber ].stringColor = Color.black.alpha_(0.5);
						} {
							env.curves = env.curves.asCollection
								.wrapExtend( env.levels.size - 1 )
								.put( i-1, vws[ \curveNumber ].value );
							vws[ \curveNumber ].stringColor = Color.black;
						};
						env.changed( \curves );
						action.value( this, env );
					});
				
				vws[ \curveNumber ] = SmoothNumberBox( vws[ \comp ], 40 @ viewHeight  )
					.value_( 
						if( env.curves.asCollection.wrapAt(i).isNumber ) {
							env.curves.asCollection.wrapAt(i)
						} {
							0
						}
					)
					.clipLo_( -16 )
					.clipHi_( 16 )
					.step_( 0.1 )
					.scroll_step_( 0.1 )
					.font_( font )
					.action_({ |nb|
						env.curves = env.curves.asCollection
							.wrapExtend( env.levels.size - 1 )
							.put( i-1, nb.value );
						vws[ \curveType ].value = 5;
						nb.stringColor = Color.black;
						env.changed( \curves );
						action.value( this, env );
					});
				
				if( env.curves.asCollection.wrapAt(i-1).isNumber.not ) {
					vws[ \curveNumber ].stringColor = Color.black.alpha_(0.5);
				};
				
				vws[ \comp ].decorator.left_( vws[ \comp ].bounds.width - (2 * viewHeight) - 6 );
				
				vws[ \back ] = SmoothButton( vws[ \comp ], viewHeight @ viewHeight )
					.label_( 'back' )
					.action_({
						var levels;
						levels = env.unmappedLevels;
						if( levels.mutable.not ) {
							levels = levels.copy;
						};
						levels = levels[..i-2] ++ levels[[i,i-1]] ++ levels[i+1..];
						env.unmappedLevels = levels;
						env.changed( \levels );
						this.selected = this.selected - 1;
						action.value( this, env );
					})
					.resize_(3);
			} {
				vws[ \comp ].decorator.left_( vws[ \comp ].bounds.width - (1 * viewHeight) - 4 );
			};
			
			if( i < (env.levels.size - 1) ) {
				vws[ \forward ] = SmoothButton( vws[ \comp ], viewHeight @ viewHeight )
					.label_( 'play' )
					.action_({
						var levels;
						levels = env.unmappedLevels;
						if( levels.mutable.not ) {
							levels = levels.copy;
						};
						levels = levels[..i-1] ++ levels[[i+1,i]] ++ levels[i+2..];
						env.unmappedLevels = levels;
						env.changed( \levels );
						this.selected = this.selected + 1;
						action.value( this, env );
					})
					.resize_(3);
			};
			
			vws[ \comp ].visible = (selected == i);
			
			argViews = argViews.add( vws );
		});
		
		rangeViews = ();

		rangeViews[ \comp ] = CompositeView( comp, comp.bounds.moveTo(0,0) )
				.resize_(2);
				
		rangeViews[ \comp ].addFlowLayout( 2@2, 2@2 );
		
		// level
		StaticText( rangeViews[ \comp ], 35 @ viewHeight )
			.string_( "range" )
			.align_( \right )
			.font_( font )
			.applySkin( RoundView.skin );
			
		// range
		rangeViews[ \range ] = EZSmoothRanger( rangeViews[ \comp ],
			(bounds.width - 43) @ viewHeight, nil, spec.asSpec
		)
			.font_( font )
			.value_( spec.asSpec.map([ env.unmappedLevels.minItem, env.unmappedLevels.maxItem]) )
			.action_({ |sl|
				env.unmappedLevels = env.unmappedLevels.normalize( 
					spec.asSpec.unmap( sl.value[0] ), 
					spec.asSpec.unmap( sl.value[1].max(sl.value[0]+1.0e-12) ) 
				);
				env.changed( \levels );
				action.value( this, env );
			});
		rangeViews[ \range ].view.resize_(2);
		
		SmoothButton( rangeViews[ \comp ], 50 @ viewHeight )
			.label_( "invert" )
			.font_( font )
			.radius_(2)
			.action_({ 
				var min, max;
				min = env.unmappedLevels.minItem;
				max = env.unmappedLevels.maxItem;
				env.unmappedLevels = env.unmappedLevels.linlin(min,max,max,min);
				env.changed( \levels );
				action.value( this, env );
			});
			
		SmoothButton( rangeViews[ \comp ], 50 @ viewHeight )
			.label_( "reverse" )
			.font_( font )
			.radius_(2)
			.action_({ 
				env.unmappedLevels = env.unmappedLevels.reverse;
				env.times = env.times.reverse;
				if( env.curves.size > 0 ) {
					env.curves = env.curves.reverse;
				};
				env.changed( \levels );
				action.value( this, env );
			});
			
		SmoothButton( rangeViews[ \comp ], 50 @ viewHeight )
			.label_( "random" )
			.font_( font )
			.radius_(2)
			.action_({ 
				var min, max, dur;
				min = env.unmappedLevels.minItem;
				max = env.unmappedLevels.maxItem;
				dur = env.times.sum;
				env.unmappedLevels = ({0.0 rrand: 1.0}!env.unmappedLevels.size).normalize(min, max);
				env.times = ({0.1 exprand: 1.0}!env.times.size).normalizeSum * dur;
				env.changed( \levels );
				action.value( this, env );
			});
			
		SmoothButton( rangeViews[ \comp ], 50 @ viewHeight )
			.label_( "line" )
			.font_( font )
			.radius_(2)
			.action_({ 
				var min, max, dur;
				min = env.unmappedLevels.minItem;
				max = env.unmappedLevels.maxItem;
				dur = env.times.sum;
				env.unmappedLevels = (..env.unmappedLevels.size-1).normalize(min, max);
				env.times = (1!env.times.size).normalizeSum * dur;
				env.changed( \levels );
				action.value( this, env );
			});
		
		rangeViews[ \comp ].visible = selected.isNil;
	}
	
}

EnvView {
	
	var <view;
	var <plotView, <editView;
	var <plotCtrl, <editCtrl;
	var <>action, <>viewHeight = 14;
	var <resize = 5;
	
	*new { |parent, bounds, env, spec|
		^this.newCopyArgs.makeView( parent, bounds, env, spec );
	}
	
	*viewNumLines { ^EnvEditView.viewNumLines + EnvPlotView.viewNumLines }
	
	resize_ { |new|
		resize = new;
		view.resize_( resize );
	}
	
	isClosed { ^view.isClosed }
	front { ^view.front }
	
	env { ^plotView.env }
	
	env_ { |new|
		plotView.env = new;
		editView.env = new;
	}
	
	spec { ^editView.spec }
	spec_ { |spec| editView.spec = spec;}
	
	maxSize { ^editView.maxSize }
	maxSize_ { |new = 32| editView.maxSize = new }
	
	value { ^this.env }
	value_ { |val| this.env = val }
	
	doAction { action.value( this ) }
	
	onClose_ { |func| view.onClose = func }
	
	close { view.findWindow.close } 
	
	makeView { |parent, bounds, env, spec|
		if( env.isNil ) { env = Env() };
		
		if( bounds.isNil ) { bounds = 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
			
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		view.resize_( resize ? 5 );
		
		plotView = EnvPlotView( view, 
			bounds.copy.height_( bounds.height *
				( EnvPlotView.viewNumLines / this.class.viewNumLines) ),
			env
		).resize_(5);
		
		env = plotView.env;
		
		editView = EnvEditView( view, 
			bounds.copy.height_( bounds.height *
				( EnvEditView.viewNumLines / this.class.viewNumLines) ),
			env, spec
		).resize_(8);
		
		plotCtrl = SimpleController( plotView )
			.put( \selected, { editView.selected = plotView.selected; } )
			.put( \selectedLine, { editView.selected = plotView.selectedLine !? (_ + 1) ? plotView.selected; } )
			.put( \env, {
				env = plotView.env;
				if( editView.env !== env ) {
					editView.env = env;
				};
			});
		
		editCtrl = SimpleController( editView )
			.put( \selected, { 
				if( editView.selected.isNil ) {
					plotView.selected = nil;
					plotView.selectedLine = nil;
				} {	
					if( (plotView.selected != editView.selected)  && {
						plotView.selectedLine !? (_ + 1) != editView.selected
					}) {
						plotView.selected = editView.selected; 
						plotView.selectedLine = nil 
					};
				}
			} )
			.put( \env, {
				env = editView.env;
				if( plotView.env !== env ) {
					plotView.env = env;
				};
			});

		view.asView.onClose_({ plotCtrl.remove; editCtrl.remove });
		
		plotView.action = { |obj, value| action.value( this, value ) };
		editView.action = plotView.action;
		
	}	
	
}