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

EQPlotView {
	
	var <eqSetting, <view, <plotView, <ctrl;
	var <active = true;
	var <selected;
	var <>font, <>viewHeight = 14, <resize = 5;
	var <min = 20, <max = 22000, <range = 24;
	var <showLegend = true;
	var <>action;
	
	*new { |parent, bounds, eqSetting|
		^super.newCopyArgs( eqSetting ).init.makeView( parent, bounds );
	}
	
	init {
		eqSetting = eqSetting ?? { EQSetting() };
		font = RoundView.skin !? { RoundView.skin.font } ?? { Font( Font.defaultSansFace, 10 ); };
	}
	
	*viewNumLines { ^6 }
	
	removeCtrl {
		ctrl.remove;
		ctrl = nil;
	}
	
	addCtrl {
		ctrl.remove;
		ctrl = SimpleController( eqSetting )
			.put( \setting, { this.refresh });
	}
	
	refresh {
		if( plotView.isClosed.not ) {
			{ plotView.refresh }.defer;
		};
	}
	
	selected_ { |index| 
		selected = index; 
		this.changed( \selected, selected );
		this.refresh 
	}
	
	range_ { |newRange|
		range = newRange;
		this.changed( \range );
		this.refresh;
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
	
	eqSetting_ { |new|
		if( new.notNil ) {
			eqSetting = new;
			if( plotView.isClosed.not ) {
				this.addCtrl;
			};
			this.changed( \eqSetting, eqSetting );
			this.refresh;
		};
	}
	
	resize_ { |new|
		resize = new;
		view.resize_( resize );
	}
	
	getPoints { |bounds|
		bounds = bounds ? plotView.drawBounds;
		^eqSetting.argNames.collect({ |array, i|
			var freq;
			if( array.includes( \freq ) ) {
				freq = eqSetting[ i, \freq ];
				[ 
					freq.explin( min, max, 0, bounds.width ),
					(eqSetting[ i, \db ] ? 0)
						.linlin(range.neg,range,bounds.height,0,\none);
				].asPoint;
			} {
				nil;
			};
		});
	}
	
	makeView { |parent, bounds|
		
		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
		
		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.resize_( resize ? 5 );
		
		this.addCtrl;
			
		plotView = UserView( view, bounds.moveTo(0,0) )
			.resize_( 5 )
			.onClose_({ this.removeCtrl; });
			
		
		plotView.mouseDownAction = { |vw,x,y,mod|
			var bounds;
			var pt;
			
			if( active ) {
				bounds = vw.bounds.moveTo(0,0);
				pt = (x@y);
				
				this.selected = this.getPoints( bounds ).detectIndex({ |ptx|
					if( ptx.notNil ) {
						ptx.dist( pt ) <= 5;
					} {
						false;
					};
				});
			};
		};
			
		plotView.mouseMoveAction = { |vw,x,y,mod|
			var bounds;
			var pt;
			
			if( active ) {
				bounds = vw.bounds.moveTo(0,0);
				
				if( selected.notNil ) {
					eqSetting[ selected, \freq ] =
						x.linexp( 0, bounds.width, min, max, \minmax );
					if( eqSetting.argNames[ selected.asInt ].includes( \db ) ) {
						eqSetting[ selected, \db ] =
							y.linlin(bounds.height, 0, range.neg,range, \none)
								.clip( range.neg, range );
					};
					
					action.value( this, eqSetting );
				};
			};
			
			
		};
		
			
		plotView.drawFunc = { |vw|
			var freqs, svals, values, bounds, zeroline;
			var vlines = [10,100,1000,10000].select({ |item|
				item.inclusivelyBetween( min, max );
			});
			var dimvlines = [2.5,5,7.5, 25,50,75, 250,500,750, 2500,5000,7500 ].select({ |item|
				item.inclusivelyBetween( min, max );
			});
			var hlines = (((range+1).neg / 6).floor..((range-1)/6).floor).select(_ != 0) * 6;
			var pts, strOffset = 11;
			
			if( GUI.id === 'swing' ) { strOffset = 14 };
			
			bounds = vw.bounds.moveTo(0,0);
			
			////// PREPARATION ///////
			
			// get freqs to plot
			freqs = ({|i| i } ! (bounds.width+1));
			freqs = freqs.linexp(0, bounds.width, min, max );
			
			// get magResponses
			values = eqSetting.magResponses( freqs ).ampdb.clip(-200,200);
			
			// sum and scale magResponses
			svals = values.sum.linlin(range.neg,range, bounds.height, 0, \none);
			values = values
				.linlin(range.neg,range, bounds.height, 0, \minmax);
			
			// create and scale grid lines
			zeroline = 0.linlin(range.neg,range, bounds.height, 0, \none);
			vlines = vlines.explin( min, max, 0, bounds.width );
			dimvlines = dimvlines.explin( min, max, 0, bounds.width );
			
			// get draggable points
			pts = this.getPoints( bounds );
			
			////// DRAWING ///////
			
			// draw background
			Pen.color_( Color.white.alpha_(0.25) );
			Pen.roundedRect( bounds, 2 ).fill;
			
			// make cliprect
			Pen.roundedRect( bounds.insetBy(0,0), 2 ).clip;
			
			// draw gridlines
			Pen.color = Color.gray(0.2).alpha_(0.125);
			hlines.do({ |hline,i|
				hline = hline.linlin( range.neg,range, bounds.height, 0, \none );
				Pen.line( 0@hline, bounds.width@hline )
				});
			dimvlines.do({ |vline,i|
				Pen.line( vline@0, vline@bounds.height );
				});
			Pen.stroke;
			
			Pen.color = Color.gray(0.2).alpha_(0.5);
			vlines.do({ |vline,i|
				Pen.line( vline@0, vline@bounds.height );
				});
			Pen.line( 0@zeroline, bounds.width@zeroline ).stroke;
			
			if( showLegend ) {	
				// draw grid labels
				Pen.font = font;
				
				Pen.color = Color.gray(0.2).alpha_(0.5);
				hlines.do({ |hline|
					Pen.stringAtPoint( hline.asString ++ "dB", 
						3@(hline.linlin( range.neg,range, bounds.height, 0, \none ) 
							- strOffset) );
					});
				vlines.do({ |vline,i|
					Pen.stringAtPoint( ["100Hz", "1KHz", "10KHz"][i], 
						(vline+2)@(bounds.height - (strOffset + 1)) );
					});
			};
			
				// draw magResponses and hooks
				values.do({ |svals,i|
					var color;
					color = Color.hsv( i.linlin( 0, values.size, 0, 1 ), 0.75, 0.5 )
						.alpha_( if( selected == i ) { 0.75 } { 0.25 } );
					Pen.color = color;
					Pen.moveTo( 0@(svals[0]) );
					svals[1..].do({ |val, i|
						Pen.lineTo( (i+1)@val );
						});
					Pen.lineTo( bounds.width@(bounds.height/2) );
					Pen.lineTo( 0@(bounds.height/2) );
					Pen.lineTo( 0@(svals[0]) );
					Pen.fill;
					
					if( active && { pts[i].notNil }) {
						Pen.color = color.alpha_(0.75);
						Pen.addArc( pts[i], 5, 0, 2pi );
						Pen.stroke;
					};
					
				});
				
			// draw summed magResponse
			Pen.color = Color.blue(0.5);
			Pen.moveTo( 0@(svals[0]) );
			svals[1..].do({ |val, i|
				Pen.lineTo( (i+1)@val );
				});
			Pen.stroke;
			
			// draw outer border
			Pen.extrudedRect( bounds, 2, 1, inverse: true );
		};
		

	}
}

EQEditView {
	
	var <eqSetting, <view, <listView, <argViews, <ctrl;
	var <>viewHeight = 14;
	var <resize = 8;
	var <>font;
	var <selected = 0;
	var <>action;
	
	*new { |parent, bounds, eqSetting|
		^super.newCopyArgs( eqSetting ).init.makeView( parent, bounds );
	}
	
	init {
		eqSetting = eqSetting ?? { EQSetting() };
		font = RoundView.skin !? { RoundView.skin.font } ?? { Font( Font.defaultSansFace, 10 ); };
	}
	
	removeCtrl {
		ctrl.remove;
		ctrl = nil;
	}
	
	addCtrl {
		ctrl.remove;
		ctrl = SimpleController( eqSetting )
			.put( \setting, { |obj, what, name, argName, value|
				var nameIndex, vw;
				if( listView.isClosed.not ) {
					if( name.isNil ) {
						this.update;
					} {
						nameIndex = eqSetting.names.indexOf( name );
						if( nameIndex.notNil ) {
							vw = argViews[ nameIndex ][ argName ];
							if( vw.notNil ) {
								vw.value = value;
							};
						};
					};
				};
			});
	}
	
	update {
		var argNames;
		if( listView.isClosed.not ) {
			argNames = eqSetting.argNames;
			argNames.do({ |names, i|
				names.do({ |argName|
					argViews[ i ][ argName ].value = eqSetting.get( i, argName );
				});
			});
		};
	}
	
	*viewNumLines { ^2 }
	
	selected_ { |new|
		if( selected != new ) {	
			selected = new;
			if( selected.notNil ) {
				argViews.do({ |item, i|
					item[ \comp ].visible = (i == selected);
				});
				{ 	listView.value = selected; 
					view.view.background = Color.hsv( 
						(selected ? 0).linlin( 0, eqSetting.names.size, 0, 1 ), 
						0.75, 0.5 
					).alpha_( 0.25 );
				}.defer;
			};
			this.changed( \selected, selected );
		};
	}
	
	eqSetting_ { |new|
		if( new.notNil ) {
			eqSetting = new;
			if( listView.isClosed.not ) {
				this.addCtrl;
			};
			this.changed( \eqSetting, eqSetting );
			this.update;
			//this.refresh;
		};
	}
	
	resize_ { |new|
		resize = new;
		view.resize_( resize );
	}
	
	
	makeView { |parent, bounds|
		var comp, argNames, specs;
		
		if( bounds.isNil ) { bounds = 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
			
		view = EZCompositeView( parent, bounds, margin: 0@2, gap: 2@2 );
		bounds = view.asView.bounds;
		view.resize_( resize ? 5 );
		argViews = [];
		
		this.addCtrl;
		
		listView = PopUpMenu( view, 80 @ viewHeight )
			.font_( font )
			.value_( selected ? 0 )
			.applySkin( RoundView.skin)
			.items_( eqSetting.names )
			.action_({ |pu|
				this.selected = pu.value;
			});
			
		listView.onClose_({ this.removeCtrl });
		
		view.decorator.nextLine;
		
		comp = CompositeView( view, bounds.width @ (viewHeight + 4))
			.resize_(2)
			.background_( Color.white.alpha_(0.25) );
		
		argNames = eqSetting.argNames;
		specs = eqSetting.specs;
		
		view.view.background = Color.hsv( 
			(selected ? 0).linlin( 0, eqSetting.names.size, 0, 1 ), 
			0.75, 0.5 
		).alpha_( 0.25 );
			
		eqSetting.names.do({ |name, i|
			var vws;
			vws = ();
			
			vws[ \comp ] = CompositeView( comp, bounds.width @ (viewHeight + 4) )
				.resize_(2);
				
			vws[ \comp ].addFlowLayout( 2@2, 2@2 );
			
			argNames[i].do({ |argName, ii|
				var spec, step;
				
				StaticText(  vws[ \comp ], 25 @ viewHeight )
					.string_( argName.asString ++ " " )
					.align_( \right )
					.font_( font )
					.applySkin( RoundView.skin );
					
				spec = specs[i][ii];
				step =  spec !? { spec.step } ? 1;
				if( step == 0 ) { step = 1 };
				
				vws[ argName ] = SmoothNumberBox( vws[ \comp ], 40 @ viewHeight  )
					.value_( eqSetting.get( name, argName ) )
					.font_( font )
					.clipLo_( spec !? { spec.minval } ? -inf )
					.clipHi_( spec !? { spec.maxval } ? inf  )
					.step_( step )
					.scroll_step_( step )
					.action_({ |nb|
						eqSetting.set( name, argName, nb.value );
						action.value( this, eqSetting );
					});
				
			});
			
			vws[ \comp ].visible = ((selected ? 0) == i);
			
			argViews = argViews.add( vws );
		});
			
	}
	
}

EQView {
	
	var <view;
	var <plotView, <editView;
	var <plotCtrl, <editCtrl;
	var <presetManager, <presetView;
	var <>action, <>viewHeight = 14;
	var <resize = 5;
	
	*new { |parent, bounds, eqSetting, presets|
		^this.newCopyArgs.makeView( parent, bounds, eqSetting, presets );
	}
	
	*viewNumLines { ^EQEditView.viewNumLines + EQPlotView.viewNumLines + PresetManagerGUI.viewNumLines }
	
	resize_ { |new|
		resize = new;
		view.resize_( resize );
	}
	
	isClosed { ^view.isClosed }
	front { ^view.front }
	
	eqSetting { ^plotView.eqSetting }
	
	eqSetting_ { |new|
		plotView.eqSetting = new;
		editView.eqSetting = new;
	}
	
	value { ^this.eqSetting }
	value_ { |val| this.eqSetting = val }
	
	doAction { action.value( this ) }
	
	onClose_ { |func| view.onClose = func }
	
	close { view.findWindow.close } 
	
	makeView { |parent, bounds, eqSetting, presets|
		if( eqSetting.isNil ) { eqSetting = EQSetting() };
		
		if( bounds.isNil ) { bounds = 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
			
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		view.resize_( resize ? 5 );
		
		plotView = EQPlotView( view, 
			bounds.copy.height_( bounds.height *
				( EQPlotView.viewNumLines / this.class.viewNumLines) ),
			eqSetting
		).resize_(5);
		
		editView = EQEditView( view, 
			bounds.copy.height_( bounds.height *
				( EQEditView.viewNumLines / this.class.viewNumLines) ),
			eqSetting
		).resize_(8);
		
		plotCtrl = SimpleController( plotView )
			.put( \selected, { editView.selected = plotView.selected } );
		
		editCtrl = SimpleController( editView )
			.put( \selected, { plotView.selected = editView.selected } );
		
		presetView = PresetManagerGUI( view, 
			bounds.copy.height_( bounds.height * ( 1 / this.class.viewNumLines) ),
			eqSetting.getEQdef.presetManager,
			eqSetting
		);
		
		presetView.resize_(8);

		view.asView.onClose_({ plotCtrl.remove; editCtrl.remove });
		
		plotView.action = { |obj, value| action.value( this, value ) };
		editView.action = plotView.action;
		
	}	
	
}