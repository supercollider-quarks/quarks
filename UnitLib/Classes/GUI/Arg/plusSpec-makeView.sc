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

+ Spec {
	
	adaptFromObject { ^this }
	
	viewNumLines { ^1 }
}

+ Nil {
	viewNumLines { ^1 }
}

+ ControlSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var view, vws, stp, labelWidth;
		vws = ();
		
		if( (minval != -inf) && { maxval != inf } ) {
			vws[ \valueView ] = 
				EZSmoothSlider( parent, bounds, label !? { label.asString ++ " " }, 
					this, { |vw| action.value( vw, vw.value ) },
					labelWidth: (RoundView.skin ? ()).labelWidth ? 80 );
			
			vws[ \view ] = vws[ \valueView ].view;
			vws[ \sliderView ] = vws[ \valueView ].sliderView;
			vws[ \sliderView ].centered_( true ).centerPos_( this.unmap( default ) );
			
		} {
			view = EZCompositeView( parent, bounds );
			vws[ \view ] = view.view;
			bounds = view.view.bounds;
			stp = this.step;
			if( stp == 0 ) { stp = 1 };
			
			if( label.notNil ) {
				labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
				vws[ \labelView ] = StaticText( view, labelWidth @ 14 )
					.string_( label.asString ++ " " )
					.align_( \right )
					.resize_( 4 )
					.applySkin( RoundView.skin );
			} {
				labelWidth = -4;
			};
		
			vws[ \valueView ] = SmoothNumberBox( view, 
					Rect(labelWidth + 4,0,bounds.width-(labelWidth + 4),bounds.height)
				)
			    .action_({ |vw|
			        action.value( vw, vw.value );
			    } ).resize_(5)
				.step_( stp )
				.scroll_step_( stp )
				.clipLo_( this.minval )
				.clipHi_( this.maxval );	
		};
		if( resize.notNil ) { vws.view.resize = resize };
		^vws;	
	}
	
	setView { |vws, value, active = false|
		vws[ \valueView ].value = value;
		if( active ) { vws[ \valueView ].doAction };
	}
	
	mapSetView { |vws, value, active = false|
		vws[ \valueView ].value = this.map(value);
		if( active ) { vws[ \valueView ].doAction };
	}
	
	adaptFromObject { |object| // if object out of range; change range
		if( object.isArray ) {
			^this.asRangeSpec.adaptFromObject( object );
		} {
			if( object.inclusivelyBetween( minval, maxval ).not ) {
				^this.copy
					.minval_( minval.min( object ) )
					.maxval_( maxval.max( object ) )
			};
			^this;
		};
	}
}

+ ListSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var multipleActions = action.size > 0;
		var vw;
		var lbls;
		lbls = labels.asCollection;
		vw = EZPopUpMenu( parent, bounds, label !? { label.asString ++ " " }, 
			if( multipleActions ) {
				list.collect({ |item, i| 
					(lbls[i] ? item.asSymbol) -> { |vw| action[i].value( vw, list[i] ) };
				});
			} { list.collect({ |item, i| 
				(lbls[i] ? item.asSymbol) -> nil 
			})
			},
			initVal: defaultIndex
		);
		if( multipleActions.not ) {
			vw.globalAction = { |vw| action.value( vw, list[vw.value] ) };
		};
		vw.labelWidth = 80; // same as EZSlider
		vw.applySkin( RoundView.skin ); // compat with smooth views
		if( resize.notNil ) { vw.view.resize = resize };
		^vw
	}
	
	setView { |view, value, active = false|
		{  // can call from fork
			view.value = this.unmap( value );
			if( active ) { view.doAction };
		}.defer;
	}
	
	mapSetView { |view, value, active = false|
		{
			view.value = value;
			if( active ) { view.doAction };
		}.defer;
	}
	
	adaptFromObject { |object|
		if( list.any({ |item| item == object }).not ) {
			^this.copy.add( object )
		} {
			^this
		};
	}
	
	
}

+ ArrayControlSpec {
	
	 makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, width;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var optionsWidth = 40, operationsOffset = 1;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		bounds.isNil.if{bounds= 350@20};
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		width = bounds.width;
				
		vws[ \view ] = view;
		vws[ \val ] = default.asCollection;
		vws[ \range ] = [ vws[ \val ] .minItem, vws[ \val ].maxItem ];
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ) };
		
		vws[ \operations ] = OEM(
			\edit, { |values|
				var plotter;
				if( vws[ \plotter ].isNil or: { vws[ \plotter ].parent.isClosed } ) {
					plotter = vws[ \val ].plot;
					plotter.editMode_( true )
						.specs_( this )
						.findSpecs_( false )
						.plotMode_( \points )
						.editFunc_({ |vw|
							vws[ \val ] = vw.value;
							vws[ \range ] = [ vws[ \val ].minItem, vws[ \val ].maxItem ];
							vws[ \setRangeSlider ].value;
							vws[ \setMeanSlider ].value;
							action.value( vws, vws[ \val ] );
						});
						
					plotter.parent.onClose = plotter.parent.onClose.addFunc({ 
						if( vws[ \plotter ] == plotter ) {
							vws[ \plotter ] = nil;
						};
					});
					vws[ \plotter ] = plotter;
				} {
					vws[ \plotter ].parent.front;
				};
				values;
			},
			\invert, { |values|
				values = this.unmap( values );
				values = values.linlin( 
					values.minItem, values.maxItem, values.maxItem, values.minItem 
				);
				this.map( values );
			},
			\reverse, { |values|
				values.reverse;
			},
			\sort, { |values|
				values.sort;
			},
			\scramble, { |values|
				values.scramble;
			},
			\rotate, { |values|
				values.rotate(1);
			},
			\squared, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				this.map( this.unmap( values )
					.linlin( min, max, 0, 1 ).squared
					.linlin(0, 1, min, max )
				);
			},
			\sqrt, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				this.map( this.unmap( values )
					.linlin( min, max, 0, 1 ).sqrt
					.linlin(0, 1, min, max )
				);
			},
			\scurve, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				this.map( this.unmap( values )
					.linlin( min, max, 0, 1 ).scurve
					.linlin(0, 1, min, max )
				);
			},
			\flat, {|values|
				var mean;
				mean = values.mean;
				mean ! (values.size);
			},
			\random, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				if( min == max ) { max = vws[ \rangeSlider ].rangeSlider.hi };
				values = values.collect({ 0.0 rrand: 1 }).normalize(min, max);
				this.map( values );
			},
			\line, { |values|
				var min, max;
				#min, max = this.unmap( [values.minItem, values.maxItem] );
				if( min == max ) { max = vws[ \rangeSlider ].hi; };
				values = (0..values.size-1).linlin(0,values.size-1, min, max );
				this.map( values );
			}
		);
		 		
		[ 0.1, 1, 10, 100, 1000 ].do({ |item|
			if( (step < item) && { (maxval - minval) >= item } ) { 
				vws[ \operations ][ "round(%)".format(item).asSymbol ] = { |values|
					this.constrain( values.round(item) );
				};
			};
		});
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
			width = width - labelWidth - 2;
		} {
			labelWidth = 0;
		};
		
		vws[ \rangeSlider ] = EZSmoothRanger( view, (width - 84) @ (bounds.height),
			nil, this, { |sl| 
				var values, min, max;
				values = this.unmap( vws[ \val ] );
				vws[ \range ] = sl.value;
				#min, max = this.unmap( vws[ \range ] );
				if( min == max ) { max = max + 1.0e-11 };
				values = values.linlin( values.minItem, values.maxItem, min, max );
				if( values.every(_==min) ) {
					values = Array.series( values.size, min, ((max - min)/(values.size-1)) );
				};
				vws[ \val ] = this.map( values );
				vws[ \setPlotter ].value;
				vws[ \setMeanSlider ].value;
				action.value( vws, vws[ \val ] ); 
			}
		);
		
		vws[ \setRangeSlider ] = {
			var min, max;
			min = vws[ \val ].minItem;
			max = vws[ \val ].maxItem;
			vws[ \rangeSlider ].value_( [ min, max ] );
		};
		
		vws[ \setRangeSlider ].value;
		
		vws[ \meanSlider ] = SmoothSlider( 
			vws[ \rangeSlider ].rangeSlider.parent, 
			vws[ \rangeSlider ].rangeSlider.bounds.insetAll(0,0,0,
				vws[ \rangeSlider ].rangeSlider.bounds.height * 0.6 )
		)
			.hiliteColor_( nil )
			.background_( Color.white.alpha_(0.125) )
			.knobSize_(0.6)
			.mode_( \move )
			.action_({ |sl|
				var values, min, max, mean;
				values = this.unmap( vws[ \val ] );
				min = values.minItem;
				max = values.maxItem;
				mean = [ min, max ].mean;
				values = values.normalize( *(([ min, max ] - mean) + sl.value).clip(0,1) );
				vws[ \val ] = this.map( values );
				vws[ \setPlotter ].value;
				vws[ \setRangeSlider ].value;
				action.value( vws, vws[ \val ] );
			});
		
		vws[ \meanSlider ].mouseDownAction = { |sl, x,y,mod, xx, clickCount|
			if( clickCount == 2 ) {
				vws[ \val ] = this.map( sl.value ) ! vws[ \val ].size;
				vws[ \setRangeSlider ].value;
				vws[ \setPlotter ].value;
			};
		};
		
		vws[ \setMeanSlider ] = {
			var min, max;
			min = vws[ \val ].minItem;
			max = vws[ \val ].maxItem;
			vws[ \meanSlider ].value_( this.unmap( [ min, max ] ).mean );
		};
		
		vws[ \setMeanSlider ].value;
		
		if( GUI.id === \qt ) { optionsWidth = 80; operationsOffset = 0; };
		
		vws[ \options ] = PopUpMenu( view, optionsWidth @ (bounds.height) )
			.items_( [ "do", " " ] ++ vws[ \operations ].keys[operationsOffset..] )
			.font_( font )
			.applySkin( RoundView.skin )
			.action_({ |vw|
				var func;
				func = vws[ \operations ][ vw.item ];
				if( func.notNil ) {	
					vws[ \val ] = func.value( vws[ \val ] );
					vws[ \update ].value;
					action.value( vws, vws[ \val ] );
				};
				vw.value = 0;
			});
			
		if( GUI.id != \qt ) {	
			vws[ \edit ] = SmoothButton( view, 40 @ (bounds.height) )
				.label_( "edit" )
				.border_( 1 )
				.radius_( 2 )
				.font_( font )
				.action_({
					vws[ \operations ][ \edit ].value;
				});
			vws[ \edit ].resize_(3);
		};
			
		vws[ \setPlotter ] = {
			if( vws[ \plotter ].notNil ) {
				{ vws[ \plotter ].value = vws[ \val ]; }.defer;
			};
		};
		
		vws[ \update ] = {
			vws[ \setRangeSlider ].value;
			vws[ \setMeanSlider ].value;
			vws[ \setPlotter ].value;
		};
		
		vws[ \rangeSlider ].view.resize_(2);
		vws[ \meanSlider ].resize_(2);
		vws[ \options ].resize_(3);
			
		view.view.onClose_({
			if( vws[ \plotter ].notNil ) {
				vws[ \plotter ].parent.close
			};
		});
	
		^vws;
	 }
	 
	 setView { |vws, value, active = false|
		vws[ \val ] = value.asCollection;
		vws[ \update ].value; 
		if( active ) { vws[ \doAction ].value };
	}
	
	mapSetView { |vws, value, active = false|
		this.setView( vws, this.map(value), active );
	}
	 
}

+ StringSpec {
	
	makeView { |parent, bounds, label, action, resize| 
		var vws, view, labelWidth;
		vws = ();
		
		// this is basically an EZButton
		
		bounds.isNil.if{bounds= 320@20};
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		vws[ \string ] = TextField( view, 
			Rect( labelWidth + 2, 0, bounds.width-(labelWidth+2), bounds.height )
		)	.resize_(2)
			.applySkin( RoundView.skin ? () )
			.action_({ |tf|
				action.value( vws, tf.value );
			});

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}
	
	setView { |view, value, active = false|
		{ view[ \string ].value = this.constrain( value ); }.defer;
		if( active ) { view[ \string ].doAction };
	}

}

+ SMPTESpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		
		bounds.isNil.if{bounds= 160 @ 18 };
		
		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};
		
		vws[ \box ] = SMPTEBox( vws[ \view ], 
				Rect(labelWidth + 4,0,bounds.width-(labelWidth + 4),bounds.height)
			)
			.applySmoothSkin
		    .action_({ |vw|
		        action.value( vw, vw.value );
		    } ).resize_(5)
		    .fps_( fps )
			.clipLo_( minval )
			.clipHi_( maxval );
		    
		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view[ \box ].value = value;
		if( active ) { view.doAction };
	}

}

+ TriggerSpec {	
	
	makeView { |parent, bounds, label, action, resize| 
		var vws, view, labelWidth;
		vws = ();
		
		// this is basically an EZButton
		
		bounds.isNil.if{bounds= 350@20};
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 vws[ \val ] = this.default;
		 		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		vws[ \buttonView ] = SmoothButton( vws[ \view ], 
				Rect( labelWidth + 2, 0, 60, bounds.height ) )
			.label_( this.label ? "set" );
		
		if( spec.notNil ) {
			vws[ \valueView ] = spec.asSpec.makeView( view, 
				Rect( labelWidth + 64, 0, bounds.width-(labelWidth+64), bounds.height ),
				nil, { |vw, val| vws[ \val ] = val }
			);
			spec.setView( vws[ \valueView ], vws[ \val ] );
		};
		
		vws[ \buttonView ]
				.radius_( bounds.height / 8 )
				.mouseDownAction_({
					if( spec.notNil ) {
						action.value( vws, vws[ \val ] );
					} {
						action.value( vws, 1 );
					}
				})
				.resize_( 1 );
				
		vws[ \normalBackground ] = vws[ \buttonView ].background;
				
		vws[ \task ] = Task({ 
			0.1.wait; 
			vws[ \buttonView ].background = vws[ \normalBackground ]; 
		}).start;

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}
	
	setView { |view, value, active = false|
		if( view[ \task ].isPlaying ) {
			view[ \task ].stop;
		} {
			view[ \buttonView ].background = Color.red(0.75);
		};
		view[ \task ] = Task({ 
			0.1.wait; 
			view[ \buttonView ].background = view[ \normalBackground ]; 
		}).start;
		view[ \val ] = value;
		if( view[ \valueView ].notNil ) {
			spec.setView( view[ \valueView ], view[ \val ] );
		};
		if( active ) { view[ \buttonView ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		this.setView( view, value, active );
	}
}

+ BoolSpec {	
	
	makeView { |parent, bounds, label, action, resize| 
		var vws, view, labelWidth;
		vws = ();
		
		// this is basically an EZButton
		
		bounds.isNil.if{bounds= 160@20};
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		if( trueLabel.isNil && falseLabel.isNil ) {
			vws[ \buttonView ] = SmoothButton( vws[ \view ], 
					Rect( labelWidth + 2, 0, bounds.height, bounds.height ) )
				.label_( [ "", 'x' ] )
		} {	
			vws[ \buttonView ] = SmoothButton( vws[ \view ], 
					Rect( labelWidth + 2, 0, bounds.width-(labelWidth+2), bounds.height ) )
				.label_( [ falseLabel ? "off", trueLabel ? "on" ] );
		};
		
		vws[ \buttonView ]
				.radius_( bounds.height / 8 )
				.value_( this.unmap( this.constrain( default ) ) )
				.action_({ |bt| action.value( vws, this.map( bt.value ) ) })
				.resize_( 1 );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \buttonView ].value = this.unmap( this.constrain( value ) );
		if( active ) { view[ \buttonView ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		view[ \buttonView ].value = this.map(  value );
		if( active ) { view[ \buttonView ].doAction };
	}
}

+ BoolArraySpec {

	 makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, width;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		bounds.isNil.if{bounds= 350@20};
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		width = bounds.width;
				
		vws[ \view ] = view;
		vws[ \val ] = default.asCollection;
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ) };
				 		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
			width = width - labelWidth - 2;
		} {
			labelWidth = 0;
		};
		
		if( trueLabel.isNil && falseLabel.isNil ) {
			vws[ \state ] = SmoothButton( view, (bounds.height)@(bounds.height) )
				.states_([
					[ "", Color.black, Color.clear ],
					[ 'x', Color.black, Color.gray(0.2,0.5) ],
					[ '-', Color.black, Color.gray(0.2,0.25) ]
				])
		} {
			vws[ \state ] = SmoothButton( view, 80@(bounds.height) )
				.states_([
					[ falseLabel ? "off", Color.black, Color.clear ],
					[ trueLabel ? "on", Color.black, Color.gray(0.2,0.5) ],
					[ "mixed" , Color.black, Color.gray(0.2,0.25) ]
				])
		};
			
		vws[ \state ]
				.border_( 1 )
				.radius_( 2 )
				.font_( font )
				.action_({ |bt|
					switch( bt.value.asInt,
						2, { vws[ \val ] = vws[ \val ].collect( false ); },
						1, { vws[ \val ] = vws[ \val ].collect( true ); },
						0, { vws[ \val ] = vws[ \val ].collect( false ); }
					);
					vws[ \update ].value;
					action.value( vws, vws[ \val ] ); 
				});

		view.decorator.left_( bounds.width - (40+2+40) );
		
		vws[ \invert ] = SmoothButton( view, 40@(bounds.height) )
			.label_( "invert" )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({ |bt|
				vws[ \val ] = vws[ \val ].collect( _.not );
				vws[ \update ].value;
				action.value( vws, vws[ \val ] ); 
			});
			
		vws[ \edit ] = SmoothButton( view, 40 @ (bounds.height) )
			.label_( "edit" )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				var plotter;
				if( vws[ \plotter ].isNil or: { vws[ \plotter ].parent.isClosed } ) {
					plotter = vws[ \val ].collect(_.binaryValue).plot;
					plotter.editMode_( true )
						.specs_( ControlSpec(0,1,\lin,1,1) )
						.findSpecs_( false )
						.plotMode_( \points )
						.editFunc_({ |vw|
							vws[ \val ] = vw.value.collect(_.booleanValue);
							action.value( vws, vws[ \val ] );
						});
						
					plotter.parent.onClose = plotter.parent.onClose.addFunc({ 
						if( vws[ \plotter ] == plotter ) {
							vws[ \plotter ] = nil;
						};
					});
					vws[ \plotter ] = plotter;
				} {
					vws[ \plotter ].parent.front;
				};
			});
			
		vws[ \setPlotter ] = {
			if( vws[ \plotter ].notNil ) {
				{ vws[ \plotter ].value = vws[ \val ].collect(_.binaryValue); }.defer;
			};
		};
		
		vws[ \update ] = {
			case { vws[ \val ].every(_ == true) } {
				vws[ \state ].value = 1;
			} { vws[ \val ].every(_ == false) } {
				vws[ \state ].value = 0;
			} { vws[ \state ].value = 2; };
			vws[ \setPlotter ].value;
		};
			
		view.view.onClose_({
			if( vws[ \plotter ].notNil ) {
				vws[ \plotter ].parent.close
			};
		});
	
		^vws;
	 }
	 
	 setView { |vws, value, active = false|
		vws[ \val ] = value.asCollection.collect(_.booleanValue);
		vws[ \update ].value; 
		if( active ) { vws[ \doAction ].value };
	}
	
	mapSetView { |vws, value, active = false|
		this.setView( vws, this.map(value), active );
	}

}
	
+ PointSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var startVal;
		vws = ();
		
		vws[ \val ] = 0@0;
		
		localStep = step.copy;
		if( step.x == 0 ) { localStep.x = 1 };
		if( step.y == 0 ) { localStep.y = 1 };
		
		bounds.isNil.if{bounds= 160@20};
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		vws[ \x ] = SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				vws[ \val ] = nb.value @ vws[ \y ].value;
				action.value( vws, vws[ \val ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( rect.left )
			.clipHi_( rect.right )
			.value_(0);
			
		vws[ \xy ] = XYView( vws[ \view ], 
			Rect( labelWidth + 2 + 42, 0, bounds.height, bounds.height ) )
			.action_({ |xy|
				startVal = startVal ?? { vws[ \val ].copy; };
				vws[ \x ].value = (startVal.x + (xy.x * localStep.x))
					.clip( rect.left, rect.right );
				vws[ \y ].value = (startVal.y + (xy.y * localStep.y.neg))
					.clip( rect.top, rect.bottom );
				action.value( vws, vws[ \x ].value @ vws[ \y ].value );
			})
			.mouseUpAction_({
				vws[ \val ] = vws[ \x ].value @ vws[ \y ].value;
				startVal = nil;
			});
			
		vws[ \y ] = SmoothNumberBox( vws[ \view ], 
				Rect( labelWidth + 2 + 42 + bounds.height + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				vws[ \val ] = vws[ \x ].value @ nb.value;
				action.value( vws,  vws[ \val ] );
			})
			//.step_( localStep.y )
			.scroll_step_( localStep.y )
			.clipLo_( rect.top )
			.clipHi_( rect.bottom )
			.value_(0);
				
		^vws;
	}
	
	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		view[ \x ].value = constrained.x;
		view[ \y ].value = constrained.y;
		view[ \val ] = constrained;
		if( active ) { view[ \x ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		view[ \x ].value = mapped.x;
		view[ \y ].value = mapped.y;
		view[ \val ] = mapped;
		if( active ) { view[ \x ].doAction };
	}
	
}

+ CodeSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		bounds.isNil.if{bounds= 160@20};
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
				
		vws[ \view ] = view;
		vws[ \val ] = default;
		 		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		vws[ \objectLabel ] = StaticText( view,
			(bounds.width-(labelWidth + 2) - 42) @ (bounds.height) 
		).applySkin( RoundView.skin ).background_( Color.gray(0.8) );
			
		vws[ \setLabel ] = {
			{ vws[ \objectLabel ].string = " " ++ vws[ \val ].asString; }.defer;
		};
		
		vws[ \setLabel ].value;
			
		vws[ \edit ] = SmoothButton( view, 40 @ (bounds.height) )
			.label_( "edit" )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				var editor;
				if( vws[ \editor ].isNil or: { vws[ \editor ].view.isClosed } ) {
					editor = CodeEditView( bounds: 400 @ 200, object: vws[ \val ] )
						.action_({ |vw|
							var obj;
							obj = vw.object;
							if( obj.class == Function ) { 
								if( obj.def.sourceCode
									.select({ |item| 
										[ $ , $\n, $\r, $\t ].includes(item).not 
									})
									.size > 2
								) {
									vws[ \val ] = obj;
									vws[ \setLabel ].value;
									action.value( vws,  vws[ \val ] );
								} {
									vws[ \val ] = nil;
									vw.object = {};
									vw.setCode( vw.object );
									vws[ \setLabel ].value;
									action.value( vws,  vws[ \val ] );
								};
							} {
								vws[ \val ] = obj;
								vws[ \setLabel ].value;
								action.value( vws,  vws[ \val ] );
							};
						})
						.failAction_({ |vw|
							vws[ \val ] = nil;
							vw.textView.string.postln;
							vw.object = {};
							vw.setCode( vw.object );
							vws[ \setLabel ].value;
							action.value( vws,  vws[ \val ] );
						});
					editor.view.onClose_({ 
						if( vws[ \editor ] == editor ) {
							vws[ \editor ] = nil;
						};
					});
					vws[ \editor ] = editor;
				} {
					vws[ \editor ].view.getParents.last.findWindow.front;
				};
			});
			
		view.view.onClose_({
			if( vws[ \editor ].notNil ) {
				vws[ \editor ].view.getParents.last.findWindow.close
			};
		});
	
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \setLabel ].value;
		if( view[ \editor ].notNil ) { 
			view[ \editor ].object = view[ \val ] ?? {{}};
			view[ \editor ].setCode( view[ \editor ].object );
		};
	}

}

+ UEnvSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var skin;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		skin = RoundView.skin;
		
		bounds.isNil.if{bounds= 160@20};
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
				
		vws[ \view ] = view;
		vws[ \val ] = Env();
		 		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
			
		vws[ \edit ] = SmoothButton( view, 40 @ (bounds.height) )
			.label_( "edit" )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				var editor;
				if( vws[ \editor ].isNil or: { vws[ \editor ].isClosed } ) {
					RoundView.pushSkin( skin );
					editor = EnvView( "Envelope editor - "++label, env: vws[ \val ], spec: spec )
						.onClose_({ 
							if( vws[ \editor ] == editor ) {
								vws[ \editor ] = nil;
							};
						});
					RoundView.popSkin;
					vws[ \editor ] = editor;
				} {
					vws[ \editor ].front;
				};
				
			});
			
		view.view.onClose_({
			if( vws[ \editor ].notNil ) {
				vws[ \editor ].close;
			};
		});
	
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \val ] = value;
		if( view[ \editor ].notNil ) { 
			view[ \editor ].env = view[ \val ];
		};
	}

}

+ RealVector3DSpec {

    makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var startVal;
		vws = ();

		vws[ \val ] = RealVector3D[0,0,0];

		localStep = step.collect{ |x| if(x == 0 ){ 1 }{ x } };

		bounds.isNil.if{bounds= 160@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \coord ] = 3.collect{ |i|

            SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2 + (i*42), 0, 40, bounds.height ) )
                .action_({ |nb|
                    vws[ \val ] = vws[ \coord ].collect(_.value).as(RealVector3D);
                    action.value( vws, vws[ \val ]);
                })
                //.step_( localStep[i] )
                .scroll_step_( localStep[i] )
                .clipLo_( nrect.sortedConstraints[i][0] )
                .clipHi_( nrect.sortedConstraints[i][1] )
                .value_(0);
         };

		^vws;
	}

	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		[view[ \coord ], constrained].flopWith(_.value(_));
		view[ \val ] = constrained;
		if( active ) { view[ \x ].doAction };
	}

	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		[view[ \coord ], mapped].flopWith(_.value(_));
        view[ \val ] = mapped;
		if( active ) { view[ \x ].doAction };
	}

}

+ RectSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, val = 0@0, wh = 0@0;
		var localStep, setCenter, setWH;
		var font;
		vws = ();
		
		font = Font( Font.defaultSansFace, 10 );
		
		localStep = 0.01@0.01;
		
		vws[ \rect ] = this.default;
		
		setCenter = { |center|
			vws[ \rect ] = vws[ \rect ].center_( center );
		};
		
		setWH = { |whx|
			vws[ \rect ].centeredExtent_( whx );
		};
		
		bounds.isNil.if{bounds= 320@20};
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 
		view.addFlowLayout( 0@0, 2@2 );
		 		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		vws[ \centerLabel ] = StaticText( vws[ \view ], 12 @ bounds.height )
			.string_( "c" )
			.align_( \right )
			.font_( font )
			.applySkin( RoundView.skin );
		
		vws[ \x ] = SmoothNumberBox( vws[ \view ], 40 @ bounds.height )
			.action_({ |nb|
				setCenter.value( nb.value @ vws[ \y ].value );
				val = vws[ \rect ].center;
				action.value( vws, vws[ \rect ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( rect.left )
			.clipHi_( rect.right )
			.value_(0);
			
		vws[ \xy ] = XYView( vws[ \view ],  bounds.height @ bounds.height )
			.action_({ |xy|
				vws[ \x ].value = (val.x + (xy.x * localStep.x))
					.clip( rect.left, rect.right );
				vws[ \y ].value = (val.y + (xy.y * localStep.y.neg))
					.clip( rect.top, rect.bottom );
				setCenter.value( val + (xy.value * localStep * (1 @ -1) ) );
				action.value( vws, vws[ \rect ] );
			})
			.mouseUpAction_({
				val = vws[ \x ].value @ vws[ \y ].value;
			});
			
		vws[ \y ] = SmoothNumberBox( vws[ \view ], 40 @ bounds.height )
			.action_({ |nb|
				setCenter.value( vws[ \x ].value @ nb.value );
				val = vws[ \rect ].center;
				action.value( vws, vws[ \rect ] );
			})
			//.step_( localStep.y )
			.scroll_step_( localStep.y )
			.clipLo_( rect.top )
			.clipHi_( rect.bottom )
			.value_(0);
			
		vws[ \whLabel ] = StaticText( vws[ \view ], 20 @ bounds.height )
			.string_( "w/h" )
			.align_( \right )
			.font_( font )
			.applySkin( RoundView.skin );
			
		vws[ \width ] = 
			SmoothNumberBox( vws[ \view ], 40 @ bounds.height )
			.action_({ |nb|
				setWH.value( nb.value @ vws[ \height ].value );
				wh = vws[ \rect ].extent;
				action.value( vws, vws[ \rect ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( 0)
			.clipHi_( rect.right )
			.value_(0);
				
		vws[ \wh ] = XYView( vws[ \view ], bounds.height @ bounds.height )
			.action_({ |xy|
				vws[ \width ].value = (wh.x + (xy.x * localStep.x))
					.clip( 0, rect.width );
				vws[ \height ].value = (wh.y + (xy.y * localStep.y.neg))
					.clip( 0, rect.height );
				setWH.value( wh + (xy.value * localStep * (1 @ -1) ) );
				action.value( vws, vws[ \rect ] );
			})
			.mouseUpAction_({
				wh = vws[ \width ].value @ vws[ \height ].value;
			});

		vws[ \height ] = 
			SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				setWH.value( vws[ \width ].value @ nb.value );
				wh = vws[ \rect ].extent;
				action.value( vws, vws[ \rect ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( 0 )
			.clipHi_( rect.right )
			.value_(0);
				
		^vws;
	}
	
	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		view[ \rect ] = constrained.copy;
		view[ \x ].value = constrained.center.x;
		view[ \y ].value = constrained.center.y;
		view[ \width ].value = constrained.width;
		view[ \height ].value = constrained.height;
		if( active ) { view[ \x ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		view[ \rect ] = mapped.copy;
		view[ \x ].value = mapped.center.x;
		view[ \y ].value = mapped.center.y;
		view[ \width ].value = mapped.width;
		view[ \height ].value = mapped.height;
		if( active ) { view[ \x ].doAction };
	}
	

}

+ RangeSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vw = EZSmoothRanger( parent, bounds, label !? { label.asString ++ " " }, 
			this.asControlSpec, 
			{ |sl| sl.value = this.constrain( sl.value ); action.value(sl, sl.value) },
			labelWidth: (RoundView.skin ? ()).labelWidth ? 80
			).value_( this.default );
		// later incorporate rangeSpec into EZSmoothRanger
		if( resize.notNil ) { vw.view.resize = resize };
		^vw;		
	}
	
	setView { |vws, value, active = false|
		vws.value = value;
		if( active ) { vws.doAction };
	}
	
	mapSetView { |vws, value, active = false|
		vws.value = this.map(value);
		if( active ) { vws.doAction };
	}
	
	adaptFromObject { |object|
		if( object.isArray.not ) {
			^this.asControlSpec.adaptFromObject( object );
		} {	
			if(  (object.minItem < minval) or: (object.maxItem > maxval) ) {
				^this.copy
					.minval_( minval.min( object.minItem ) )
					.maxval_( maxval.max( object.maxItem ) )
			};
			^this;
		};
	}

}

+ ColorSpec {
	
	viewNumLines { ^7 }
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var viewHeight, viewWidth;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		bounds.isNil.if{bounds= 320@100};
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		view.asView.resize_(5);
		
		vws[ \view ] = view;
		vws[ \val ] = this.default;
		 		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		viewHeight = (bounds.height / 6) - 4;
		viewWidth = bounds.width - (labelWidth + 6);
		
		vws[ \colorView ] = UserView( view, viewWidth @ viewHeight )
			.drawFunc_({ |vw|
				var rect;
				rect = vw.drawBounds;
				Pen.color = Color.black;
				
				Pen.line( (rect.right * 2/5) @ (rect.top), rect.rightBottom );
				Pen.lineTo( rect.rightTop );
				Pen.lineTo( (rect.right * 2/5) @ (rect.top) );
				
				Pen.fill;
				Pen.color = Color.white;
				Pen.line( rect.leftTop, (rect.right * 3/5) @ (rect.bottom));
				Pen.lineTo( rect.leftBottom );
				Pen.lineTo( rect.leftTop );
				Pen.fill;
				
				Pen.color = vws[ \val ];
				Pen.fillRect( vw.drawBounds );
			})
			.canReceiveDragHandler_({
				var obj;
				obj = View.currentDrag;
				if( obj.class == String ) {
					obj = { obj.interpret }.try;
				};
				obj.respondsTo( \asColor );
			})
			.receiveDragHandler_({
				var obj;
				if( View.currentDrag.class == String ) {
					obj = View.currentDrag.interpret.asColor; 
				} {
					obj = View.currentDrag.asColor;
				};
				if( obj.notNil ) { vws[ \val ] = obj };
				vws[ \updateViews ].value;
				action.value( vws, vws[ \val ] );
			})
			.beginDragAction_({ vws[ \val ] })
			.resize_(2);
			
		vws[ \r ] = EZSmoothSlider(view, viewWidth @ viewHeight, "red" ).value_(0.5);
		vws[ \g ] = EZSmoothSlider(view, viewWidth @ viewHeight, "green" ).value_(0.5);
		vws[ \b ] = EZSmoothSlider(view, viewWidth @ viewHeight, "blue" ).value_(0.5);
		vws[ \a ] = EZSmoothSlider(view, viewWidth @ viewHeight, "alpha" ).value_(1);
		
		[\r,\g,\b,\a].collect(vws[_]).do({ |item| 
			item.sliderView.hiliteColor = nil;
			item.view.resize_(2);
		});

		vws[ \updateViews ] = {
			vws[ \r ].sliderView.background = Gradient( 
					vws[ \val ].copy.red_(1).alpha_(1), 
					vws[ \val ].copy.red_(0).alpha_(1), \v 
				);
			vws[ \g ].sliderView.background = Gradient( 
					vws[ \val ].copy.green_(1).alpha_(1), 
					vws[ \val ].copy.green_(0).alpha_(1), \v 
				);
			vws[ \b ].sliderView.background = Gradient( 
				vws[ \val ].copy.blue_(1).alpha_(1), 
				vws[ \val ].copy.blue_(0).alpha_(1), \v 
				);
			vws[ \a ].sliderView.background = Gradient( 
				vws[ \val ].copy.alpha_(1), vws[ \val ].copy.alpha_(0), \v 
			);
			vws[ \r ].value = vws[ \val ].red;
			vws[ \g ].value = vws[ \val ].green;
			vws[ \b ].value = vws[ \val ].blue;
			vws[ \a ].value = vws[ \val ].alpha;
			{ vws[ \colorView ].refresh }.defer;
		};
		
		vws[ \updateViews ].value;
	
		editAction = { 
			vws[ \val ]
				.red_( vws[ \r ].value )
				.green_( vws[ \g ].value )
				.blue_( vws[ \b ].value )
				.alpha_( vws[ \a ].value );
			vws[ \updateViews ].value;
			action.value( vws, vws[ \val ] );
		};
		
		vws[ \r ].action = editAction;
		vws[ \g ].action = editAction;
		vws[ \b ].action = editAction;
		vws[ \a ].action = editAction;
		
		vws[ \presetManager ] = PresetManagerGUI( 
			view, viewWidth @ viewHeight, presetManager, vws[ \val ] 
		).action_({ |pm|
			vws[ \val ] = pm.object;
			vws[ \updateViews ].value;
			action.value( vws, vws[ \val ] );
		});
		
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \updateViews ].value;
	}


}

+ BufSndFileSpec {
	
	viewNumLines { ^BufSndFileView.viewNumLines }
	
	viewClass { ^BufSndFileView }
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		
		vws = ();
		
		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 view.addFlowLayout(0@0, 4@4);
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};
		
		if( resize.notNil ) { vws[ \view ].resize = resize };
		
		vws[ \sndFileView ] = this.viewClass.new( vws[ \view ], 
			( bounds.width - (labelWidth+4) ) @ bounds.height, { |vw|
				action.value( vw, vw.value )
			} )
		
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \sndFileView ].value = value;
		if( active ) { view.doAction };
	}
}

+ DiskSndFileSpec {
	viewClass { ^DiskSndFileView }
}

+ MultiSndFileSpec {
	
	viewNumLines { ^3 }
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var font;
		var editAction;
		var loopSpec, rateSpec;
		var viewHeight;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };
		
		viewHeight = (bounds.height / this.viewNumLines).floor - 2;
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		
		vws[ \view ] = view;
		
		vws[ \val ] = this.default ? [];
		
		loopSpec = BoolSpec(true).massEditSpec( vws[ \val ].collect(_.loop) );
		rateSpec = [-24,24].asSpec.massEditSpec( vws[ \val ].collect({|x| x.rate.ratiomidi }) );
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ viewHeight )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

				
		editAction = { |vw|
			vws[ \val ] = vw.object;
			action.value( vws, vws[ \val ] );
		};
		
		vws[ \list ] = SmoothButton( view, 40 @ viewHeight )
			.label_( "list" )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				var missing;
				
				if( vws[ \listdoc ].notNil ) {
					vws[ \listdoc ].close;
				};
				
				missing = vws[ \val ].select({ |x| x.exists.not }).collect(_.path);
				if( missing.size > 0 ) {
					missing = "missing files:\n" ++ missing.join("\n") ++ "\n\n";
				} {
					missing = "";
				};
				
				vws[ \listdoc ] = Document().string_(
					missing ++ "all soundfile paths:\n" ++
					vws[ \val ].collect(_.path).join("\n")
				).promptToSave_(false);
			});
			
		view.view.onClose_({
			if( vws[ \listdoc ].notNil ) {
				vws[ \listdoc ].close;
			};
		});
		
		vws[ \copy ] = SmoothButton( view, 60 @ viewHeight )
			.label_( "copy all" )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				var paths;
				Dialog.savePanel({ |path|
					path = path.dirname;
					paths = vws[ \val ].collect({ |item| 
						item.path.getGPath.asSymbol
					}).as(Set).as(Array).do({ |pth|
						pth.asString.copyTo( path );
					});
					vws[ \val ].do({ |item|
						item.path = path +/+ item.path.basename;
					});
				});
			});
			
		vws[ \browse ] = SmoothButton( view, 20 @ viewHeight )
			.label_( 'folder' )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				var paths;
				Dialog.openPanel({ |paths|
					case { paths.size >= vws[ \val ].size } {
						vws[ \val ].do({ |item, i|
							item.path = paths[i];
							item.fromFile;
						});
					} { paths.size == 1 } {
						SCAlert( "You selected one soundfile for % units.\nUse only on the first unit,\nor the same for all?".format( vws[ \val ].size ), [ "cancel", "first", "all" ], [ {}, {
							vws[ \val ][0].path = paths[0];
							vws[ \val ][0].fromFile;
						}, {
							vws[ \val ].do({ |item, i|
								item.path = paths[0];
								item.fromFile;
							});
						} ] );
					} { SCAlert( "You selected % soundfiles for % units.\nUse them only for the first % units,\nor wrap around for all?".format( paths.size, vws[ \val ].size, paths.size ), [ "cancel", "first %".format( paths.size ), "all" ], [ {}, {
							paths.do({ |item, i|
								vws[ \val ][i].path = item;
								vws[ \val ][i].fromFile;
							});
						}, {
							vws[ \val ].do({ |item, i|
								item.path = paths.wrapAt(i);
								item.fromFile;
							});
						} ] );					
					};
				}, {}, true);
			});
			
		view.view.decorator.nextLine;
		view.view.decorator.shift( labelWidth, 0 );
		
		RoundView.pushSkin( (RoundView.skin.deepCopy ? ()).labelWidth_(35) );
		
		vws[ \loop ] = loopSpec.makeView( view, (view.bounds.width - labelWidth) @ viewHeight,
			" loop", { |vw, val| 
				var size;
				vws[ \updateLoop ] = false;
				size = val.size - 1;
				val.do({ |item, i|
					if( i == size ) { vws[ \updateLoop ] = true };
					vws[ \val ][ i ].loop = item;
				});
			}, 2 );
			
		vws[ \loop ].labelView.align_( \left );
			
		vws[ \setLoop ] = { |evt, value|
			if( evt.updateLoop != false ) {
				loopSpec.setView( evt[ \loop ], value.collect(_.loop) );
			};
		};
			
		view.view.decorator.nextLine;
		view.view.decorator.shift( labelWidth, 0 );
		
		vws[ \rate ] = rateSpec.makeView( view, (view.bounds.width - labelWidth) @ viewHeight,
			" rate", { |vw, val| 
				var size;
				vws[ \updateRate ] = false;
				size = val.size - 1;
				val.do({ |item, i|
					if( i == size ) { vws[ \updateRate ] = true };
					vws[ \val ][ i ].rate = item.midiratio;
				})  
			}, 2 );
		
		vws[ \rate ].labelView.align_( \left );
			
		vws[ \setRate ] = { |evt, value|
			if( evt.updateRate != false ) {
				rateSpec.setView( evt[ \rate ], value.collect({|x| x.rate.ratiomidi }) );
			};
		};
		
		RoundView.popSkin;

		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \val ] = value;
		view.setLoop( value );
		view.setRate( value );
	}
}

+ PartConvBufferSpec {
	
	viewNumLines { ^PartConvBufferView.viewNumLines }
	
	viewClass { ^PartConvBufferView }
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		
		vws = ();
		
		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 view.addFlowLayout(0@0, 4@4);
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};
		
		if( resize.notNil ) { vws[ \view ].resize = resize };
		
		vws[ \bufferView ] = this.viewClass.new( vws[ \view ], 
			( bounds.width - (labelWidth+4) ) @ bounds.height, { |vw|
				action.value( vw, vw.value )
			} )
		
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \bufferView ].value = value;
		if( active ) { view.doAction };
	}
}

+ IntegerSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		
		bounds.isNil.if{bounds= 160 @ 18 };
		
		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};
		
		vws[ \box ] = SmoothNumberBox( vws[ \view ], 
				Rect(labelWidth + 2,0,bounds.width-(labelWidth + 2),bounds.height)
			)
		    .action_({ |vw|
		        action.value( vw, vw.value.asInt );
		    } ).resize_(5)
		    .allowedChars_( "" )
			.step_( step )
			.scroll_step_( step )
			.alt_scale_( alt_step / step )
			.clipLo_( this.minval )
			.clipHi_( this.maxval );
		    
		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view[ \box ].value = value;
		if( active ) { view.doAction };
	}

}

+ FreqSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		localStep = step.copy;
		if( step == 0 ) { localStep = 1 };
		bounds.isNil.if{bounds= 320@20};
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
				
		vws[ \view ] = view;
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		vws[ \comp ] = CompositeView( view, (bounds.width - (labelWidth + 49)) @ (bounds.height) );
		
		vws[ \mode ] = PopUpMenu( view, 45 @ (bounds.height) )
			.font_( font )
			.applySkin( RoundView.skin ? () )
			.items_([ 'hz', 'midi', 'note' ])
			.action_({ |pu|
				mode = pu.item;
				this.setMode( vws, mode );
			});
		
		// hz mode
		vws[ \hz ] = EZSmoothSlider( vws[ \comp ], 
			vws[ \comp ].bounds.width @ (bounds.height),
			nil,  this, { |vw| action.value( vw, vw.value ) }
		).visible_( false );
		
		vws[ \hz ].sliderView.centered_( true ).centerPos_( this.unmap( default ) );
		
		// midi mode
		vws[ \midi ] =  EZSmoothSlider( vws[ \comp ], 
			vws[ \comp ].bounds.width @ (bounds.height),
			nil, 
			[ this.minval.cpsmidi, this.maxval.cpsmidi, \lin, 0.01, this.default.cpsmidi ].asSpec,
			{ |vw| action.value( vw, vw.value.midicps ) }
		).visible_( false );
		
		vws[ \midi ].sliderView.centered_( true ).centerPos_( 
			vws[ \midi ].controlSpec.unmap( default.cpsmidi ) 
		);
				
		// note mode
		vws[ \note ] = SmoothNumberBox( vws[ \comp ], 40 @ (bounds.height) )
			.action_({ |nb|
				action.value( vws, nb.value.midicps * (vws[ \cents ].value / 100).midiratio );
			})
			.scroll_step_( localStep )
			.clipLo_( this.minval.cpsmidi )
			.clipHi_( this.maxval.cpsmidi )
			.value_( 440.cpsmidi )
			.formatFunc_({ |val|
				val.midiname;
			})
			.interpretFunc_({ |string|
				string.namemidi;
			})
			.allowedChars_( "abcdefgABCDEFG#-" )
			.visible_( false );
			
		vws[ \cents ] = EZSmoothSlider( vws[ \comp ], 
				Rect( 44, 0, (vws[ \comp ].bounds.width - 44), bounds.height ),
				nil, [-50,50,\lin,0.1,0].asSpec
			).action_({ |sl|
				action.value( vws, vws[ \note ].value.midicps * (sl.value / 100).midiratio );
			})
			.visible_( false );
		
		vws[ \cents ].sliderView.centered_(true);
			
		this.setMode( vws, mode );
	
		^vws;
	}
	
	setMode { |view, newMode|
		[ \hz, \midi, \note ].do({ |item|
			view[ item ].visible = (item == newMode)	
		});
		view[ \cents ].visible = (newMode == \note);
	}
	
	setView { |view, value, active = false|
		view[ \hz ].value = value;
		view[ \midi ].value = value.cpsmidi;
		view[ \note ].value = value.cpsmidi.round(1);
		view[ \cents ].value = (value.cpsmidi - (view[ \note ].value)) * 100;
		{ 
			this.setMode( view, mode );
			view[ \mode ].value = view[ \mode ].items.indexOf( mode ) ? 0; 
		}.defer;
		if( active ) { view[ \hz ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		this.setView( view, this.map( value ), active );
	}
}

+ AngleSpec {
	
	makeView {  |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var degMul = 180 / pi;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		localStep = step.copy;
		if( step == 0 ) { localStep = 1 };
		bounds.isNil.if{bounds= 320@20};
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
				
		vws[ \view ] = view;
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		vws[ \comp ] = CompositeView( view, (bounds.width - (labelWidth + 49)) @ (bounds.height) );
		
		vws[ \mode ] = PopUpMenu( view, 45 @ (bounds.height) )
			.font_( font )
			.applySkin( RoundView.skin ? () )
			.items_([ 'rad', 'deg' ])
			.action_({ |pu|
				mode = pu.item;
				this.setMode( vws, mode );
			});
		
		// rad mode
		vws[ \rad ] = EZSmoothSlider( vws[ \comp ], 
			vws[ \comp ].bounds.width @ (bounds.height),
			nil,  
			[ this.minval / pi, this.maxval / pi, \lin, step / pi, this.default / pi, "pi" ].asSpec, 
			{ |vw| action.value( vw, vw.value * pi ) },
			unitWidth: 45
		).visible_( false );
		
		vws[ \rad ].sliderView
			.centered_( true )
			.centerPos_( this.unmap( default ) )
			.clipMode_( \wrap );
		
		// deg mode
		vws[ \deg ] = EZSmoothSlider( vws[ \comp ], 
			vws[ \comp ].bounds.width @ (bounds.height),
			nil, 
			[ this.minval * degMul, this.maxval * degMul, \lin, step * degMul, 
				this.default * degMul ].asSpec,
			{ |vw| action.value( vw, vw.value / degMul ) }
		).visible_( false );
		
		vws[ \deg ].sliderView
			.centered_( true )
			.centerPos_( this.unmap( default ) )
			.clipMode_( \wrap );
			
		this.setMode( vws, mode );
	
		^vws;
	}
	
	setMode { |view, newMode|
		[ \rad, \deg ].do({ |item|
			view[ item ].visible = (item == newMode)	
		});
	}
	
	setView { |view, value, active = false|
		view[ \rad ].value = value / pi;
		view[ \deg ].value = value * 180 / pi;
		{ 
			this.setMode( view, mode );
			view[ \mode ].value = view[ \mode ].items.indexOf( mode ) ? 0; 
		}.defer;
		if( active ) { view[ \rad ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		this.setView( view, this.map( value ), active );
	}

}

+ AngleArraySpec {
	
	makeView { |parent, bounds, label, action, resize|
		var mode, vws, act, spec, degMul;
		mode = AngleSpec.mode;
		switch( mode, 
			\rad, { 
				act = { |vws, value|
					action.value( vws, value * pi ) 
				};
				spec = ArrayControlSpec( minval / pi, maxval / pi, \linear, step, default / pi );
				vws = spec.makeView( parent, bounds, label, act, resize );
				vws[ \mode ] = \rad;
				vws[ \spec ] = spec;
			},
			\deg, { 
				degMul = 180 / pi;
				act = { |vws, value|
					action.value( vws, value / degMul );
				};
				spec = ArrayControlSpec( minval * degMul, maxval * degMul, \linear, step, 
					default * degMul );
				vws = spec.makeView( parent, bounds, label, act, resize );
				vws[ \mode ] = \deg;
				vws[ \spec ] = spec;
			}
		);
		^vws;
	}
	
	setView { |view, value, active = false|
		switch( view[ \mode ],
			\rad, { value = value / pi },
			\deg, { value = value * 180 / pi }
		);
		view[ \spec ].setView( view, value, active )
	}
	
	mapSetView { |view, value, active = false|
		this.setView( view, this.map( value ), active );
	}

}

+ DisplaySpec {
	
	makeView {
		 |parent, bounds, label, action, resize|
		var vws, view, labelWidth, font;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		bounds.isNil.if{bounds= 320@20};
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
				
		vws[ \view ] = view;
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};
		
		vws[ \display ] = StaticText( view, (bounds.width - (labelWidth + 4)) @ (bounds.height) )
			.font_( font );
	
		^vws;
	}
	
	setView { |view, value, active = false|
		{ 
			view[ \display ].string = formatFunc.value( value, spec );		}.defer;
	}
	
	mapSetView { |view, value, active = false|
		this.setView( view, this.map( value ), active );
	}
}


+ EZPopUpMenu {
	
	labelWidth { ^labelView !? { labelView.bounds.width } ? 0 }
	
	labelWidth_ { |width = 80|
		var delta;
		if( layout === \horz && { labelView.notNil } ) { // only for horizontal sliders
			delta = labelView.bounds.width - width;
			labelView.bounds = labelView.bounds.width_( width );
			widget.bounds = widget.bounds
				.width_( widget.bounds.width + delta )
				.left_( widget.bounds.left - delta );
		};
	}
}