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

UChainIOGUI : UChainGUI {
	
	classvar <>showControl = true, <>showAudio = true;
	
	var <analyzers;
	var <unitColors;
	var <>audioMax = 0, <>controlMax = 0;
	
	makeViews { |bounds|
		
		analyzers = ( 
			\audio: UChainAudioAnalyzer(chain),
			\control:  UChainControlAnalyzer(chain)
		);
		
		RoundView.useWithSkin( skin ++ (RoundView.skin ? ()), {
			this.prMakeViews( bounds );
		});
		
	}
	
	getHeight { |units, margin, gap|
		^units.collect({ |unit, i|
			((14 + gap.y) * (
				analyzers[ \audio ].numIOFor( i ) + 
				analyzers[ \control ].numIOFor( i ))
			) +
				14 + gap.y + gap.y;
		}).sum + (4 * (14 + gap.y));
	}
	
	getUnits { ^chain.units }
	
	makeUnitHeader { |units, margin, gap|
		var comp, header,params;
		var audio, control;
		
		comp = CompositeView( composite, (composite.bounds.width - (margin.x * 2))@16 )
			.resize_(2);
		
		header = StaticText( comp, comp.bounds.moveTo(0,0) )
				.applySkin( RoundView.skin )
				.string_( " units" )
				.align_( \left )
				.resize_(2);
		
		audio = SmoothButton( comp, Rect( comp.bounds.right - (80+8+60), 1, 40, 12 ) )
			.label_( ["audio", "audio"] )
			.border_( 1 )
			.radius_( 2 )
			.value_( showAudio.binaryValue )
			.hiliteColor_( Color.green )
			.action_({ |bt|
				 showAudio = bt.value.booleanValue;
				 chain.changed( \units );
			}).resize_(3);
		
		control = SmoothButton( comp, Rect( comp.bounds.right - (40+4+60), 1, 40, 12 ) )
			.label_( ["control", "control"] )
			.border_( 1 )
			.radius_( 2 )
			.value_( showControl.binaryValue )
			.hiliteColor_( Color.green )
			.action_({ |bt|
				 showControl = bt.value.booleanValue;
				 chain.changed( \units );
			}).resize_(3);

				
		params = SmoothButton( comp, Rect( comp.bounds.right - 60, 1, 60, 12 ) )
			.label_( "params" )
			.border_( 1 )
			.radius_( 2 )
			.action_({
				var parent;
				parent = composite.parent;
				{
					composite.remove;
					UChainGUI( parent, originalBounds, chain );
				}.defer(0.01);
			}).resize_(3);
						
		CompositeView( comp, Rect( 0, 14, (composite.bounds.width - (margin.x * 2)), 2 ) )
			.background_( Color.black.alpha_(0.25) )
			.resize_(2)

	}
	
	makeUnitSubViews { |scrollView, units, margin, gap|
		var unitInitFunc;
		var labelWidth;
		var width;
		
		width = scrollView.bounds.width - 12 - (margin.x * 2);
		
		labelWidth = 80;
		
		if( RoundView.skin.notNil ) { labelWidth = RoundView.skin.labelWidth ? 80 };

		unitInitFunc = { |unit, what ...args|
			if( what === \init ) { // close all views and create new
				chain.changed( \units );
			};
		};
		
		unitColors = units.collect({ |item, i|
			Color.hsv( i.linlin( 0, units.size, 0, 1 ), 0.1, 0.9 );
		});
		
		^units.collect({ |unit, i|
			var header, comp, views, params;
			
			comp = CompositeView( scrollView, width@14 )
				.resize_(2);
			
			header = StaticText( comp, comp.bounds.moveTo(0,0) )
				.applySkin( RoundView.skin )
				.string_( " " ++ i ++ ": " ++ if(unit.def.class == LocalUdef){"[Local] "}{""} ++ unit.defName )
				.background_( unitColors[i] )
				.resize_(2)
				.font_( 
					(RoundView.skin.tryPerform( \at, \font ) ?? 
						{ Font( Font.defaultSansFace, 12) }).boldVariant 
				);
			
			views = this.makeUnitView( scrollView, unit, i, labelWidth, width );
			
			unit.addDependant( unitInitFunc );
			
			header.onClose_({ 
				unit.removeDependant( unitInitFunc );
				views[ \ctrl ].remove;
			});
			
			views;
			
		});
	
	}
	
	getMax { |rate = \audio|
		^analyzers[ rate ].usedBuses.maxItem ? 0;
	}
	
	makeUnitView { |scrollView, unit, i, labelWidth, width|
		var ctrl;
		var setPopUps;
		var max;
		var views;
		var array = [];
		
		max = (
			\audio: this.getMax( \audio ),
			\control: this.getMax( \control )
		);
		
		views = ();
		
		ctrl = SimpleController( unit );
		
		if( showAudio ) {
			array = [ 
				[ \audio, \in ], 
				[ \audio, \out ], 
			]
		};
		if( showControl ) {
			array = array ++ [ 
				[ \control, \in ], 
				[ \control, \out ],
			]
		};
		
		array.do({ |item|
			var rate, mode;
			var io, etter, setter, getter;
			var mixOutSetter;
			#rate, mode = item;
			etter = "et" ++ (rate.asString.firstToUpper ++ mode.asString.firstToUpper);
			setter = ("s" ++ etter).asSymbol;
			getter = ("g" ++ etter).asSymbol;
			
			io = analyzers[ rate ].ioFor( mode, i );
			
			if( io.notNil ) {
				
				views[ rate ] = views[ rate ] ? ();
				views[ rate ][ mode ] = [ ];
				
				io[2].do({ |item, ii|
					var nb, pu, mx, stringColor;
					
					
					if( rate === \control ) {
						stringColor = Color.gray(0.25);
					} {
						stringColor = Color.black;
					};
					
					StaticText( scrollView, labelWidth @ 14 )
						.applySkin( RoundView.skin )
						.align_( \right )
						.stringColor_( stringColor )
						.string_( "% % %".format( 
								if( ii == 0 ) { rate } { "" }, 
								mode,
								unit.def.prGetIOName( mode, rate, item ) ? item 
							) 
						);
						
					nb = SmoothNumberBox( scrollView, 20@14 )
						.clipLo_( 0 )
						.stringColor_( stringColor )
						.value_(  io[3][ii] )
						.action_( { |nb|
							unit.perform( setter, ii, nb.value ); 					} );
						
					pu = PopUpMenu( scrollView, (width - labelWidth - 28)@14 )
						.applySkin( RoundView.skin )
						.stringColor_( stringColor )
						.resize_(2)
						.action_({ |pu| 
							unit.perform( setter, ii, pu.value );
						});
					
					this.setPopUp( pu, io[3][ii], i, rate, mode, max[ rate ] );
					
					setPopUps = setPopUps.addFunc({
						this.setPopUp( pu, nb.value, i, rate, mode, max[ rate ] );
					});
					
					if( (mode === \out) && { io[4][ii].notNil }) {
						
						mixOutSetter = "set" ++ rate.asString.firstToUpper ++ "MixOutLevel";
						mixOutSetter = mixOutSetter.asSymbol;
							
						mx = EZSmoothSlider(  scrollView, width@14,
							"% mix %".format( 
								if( ii == 0 ) { rate } { "" }, 
								unit.def.prGetIOName( mode, rate, item ) ? item 
							),
							\amp.asSpec, 
							{ |vw| 
								unit.perform( mixOutSetter, ii, vw.value );
							}, 
							labelWidth: labelWidth
						);
						
						mx.setColors( stringColor: stringColor );
						
						mx.value = io[4][ii];
						mx.view.resize = 2;
						
						ctrl.put( unit.getIOKey( mode, rate, ii, "lvl" ), {  |obj, what, val|
							mx.value = val;				
						});
						
						this.setMixSlider( mx, io[3][ii], i, rate, stringColor );
						
						setPopUps = setPopUps.addFunc({
							this.setMixSlider( mx, nb.value, i, rate, stringColor );
						});
					};
					
					views[ rate ][ mode ] = views[ rate ][ mode ].add( [nb, pu, mx] );
					
					ctrl.put( unit.getIOKey( mode, rate, ii ), { |obj, what, val|
						nb.value = val;
						analyzers[ rate ].init;
						max[ rate ] = this.getMax( rate );
						uguis.do({ |vws| vws[ \setPopUps ].value; });
					});
						
					composite.decorator.nextLine;
				});		
			};
		});
		
		if( views.size == 0 ) { 
			ctrl.remove;
		} {
			views[ \ctrl ] = ctrl;
		};
		views[ \setPopUps ] = setPopUps;
		^views;
		
	}
	
	setPopUp { |pu, value = 0, i = 0, rate = \audio, mode = \in, max = 10|
		var busConnections;
		busConnections = this.getBusConnections( i, rate, mode, max );
		{ 	
			pu.items = this.prGetPopUpItems( busConnections, mode, rate ); 
			pu.value = value.clip( 0, busConnections.size-1);
			pu.background = this.getPopUpColor( busConnections, value );
		}.defer;
	}
	
	setMixSlider { |mx, bus = 0, i = 0, rate = \audio, stringColor|
		var busConnection;
		busConnection = analyzers[ rate ].busConnection( \in, bus.asInt, i.asInt );
		mx.sliderView.string = this.getBusLabel( busConnection, \in, bus, rate );
		mx.sliderView.hiliteColor = this.getPopUpColor( [busConnection], 0 );
		mx.sliderView.stringColor = stringColor;
		mx.numberView.stringColor = stringColor;
	}
	
	getBusConnections { |i = 0, rate = \audio, mode = \in, max = 10|
		var items, lastNotNil = 0;
		var func;
		
		func = { |bus| analyzers[ rate ].busConnection( mode, bus.asInt, i.asInt ) };
				
		items = (max+2).asInt.collect( func );
		items.do({ |item, ii|
			if( item.notNil ) {
				lastNotNil = ii;
			};
		});
		^items[..lastNotNil + 1];
	}
	
	getBusLabel { |busConnection, mode = \in, bus = 0, rate = \audio|
		var prefix, index, reverseMode;
		
		prefix = switch( mode, \in, "from", \out, "to" );
		reverseMode =  switch( mode, \in, \out, \out, \in );
		
		^if( busConnection.notNil ) {
			index = busConnection[2][ 
				busConnection[3].indexOfEqual( bus.asInt ) 
			];
			"% %:% (%)".format( 
				prefix,
				busConnection[1], 
				busConnection[0].defName,
				busConnection[0].def.prGetIOName( reverseMode, rate, index ) ? index
			);
		} {
			"no signal"
		};
	}
	
	prGetPopUpItems { |busConnections, mode = \in, rate = \audio|
		^busConnections.collect({ |item, bus| this.getBusLabel( item, mode, bus, rate ); });			
	}

	getPopUpItems { |i = 0, rate = \audio, mode = \in, max = 10|
		^this.prGetPopUpItems( this.getBusConnections( i, rate, mode, max ), mode, rate );	}
	
	getPopUpColor { |busConnections, bus = 0|
		var item;
		item = busConnections.clipAt( bus.asInt );
		if( item.notNil ) {
			^unitColors[ item[1] ]
		} {
			^Color.clear;
		};
	}

}