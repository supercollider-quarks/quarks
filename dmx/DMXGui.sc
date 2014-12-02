/*
DMX framework for supercollider
(c) 2007-9 Marije Baalman (nescivi)
GNU/GPL v2.0 or later
*/

// class to create a GUI for DMX

DMXGui{
	classvar <>xposScreen=0, <>yposScreen=20;
	classvar <counter = 0;
	var <>dmx;
	var <sliderview, <cueview;
	var <showconsole;
	var <>w, <watcher;

	*new { |dmx, w| 
		^super.new.w_(w).dmx_(dmx).init;
	}
	
	init { 
		var f, xsize, ysize;
		var ncview, rmview, clview;
		counter = counter + 1;

		xsize = 300;
		ysize = 105+130+10;
	
		w = w ?? { 
			w = Window.new("DMX Control", Rect(xposScreen, yposScreen, xsize + 10, ysize)).front;
			//w.view.background_(Color.black); 
			w.view.decorator = FlowLayout(Rect(4, 4, w.bounds.width, w.bounds.height), 2@2, 2@2);
			w;
		};

		// unclutter the windows on the screen:
		yposScreen = yposScreen + 160;
		if ( yposScreen > 700,
			{ yposScreen = 20; xposScreen = xposScreen + xsize + 10;
				if ( xposScreen > 900,
					{ xposScreen = 0; });
			});

		showconsole = Button.new( w, Rect( 0, 0, xsize-5, 20 ) ).states_( [ ["Show console"] ] ).action_( { DMXConsole.new( dmx ) } );

		//		GUI.staticText.new(w, Rect(0, 0, xsize - 2, 20)).string_("WiiMote" + wiimote.id + wiimote.address )
		//			.align_(0);
		//.background_(labelColor); 

		cueview = GUI.compositeView.new( w, Rect( 5, 30, 205, 130 ));
		//		rm = WiiRemoteGUI.new( rmview, wiimote, 30 );

		//		sliderview = GUI.compositeView.new( w, Rect( 5, 160, 205, 105 ));
		//		nc = WiiNunchukGUI.new( ncview, wiimote, 160 );

		watcher = SkipJack.new( { this.updateVals }, 0.1, { w.isClosed }, (\dmx_gui_ ++ counter));
		watcher.start;
	}

	updateVals { 
		{ 
			//			rm.updateVals;
			//			nc.updateVals;
			//			cl.updateVals;
		}.defer;
	}

	hide{
		if ( GUI.scheme.id == \swing,
			{
				w.visible_( false );
			},{
				w.close;
			});
		watcher.stop;
	}

	show{
		if ( GUI.scheme.id == \swing,
			{
				w.visible_( true );
			});
		watcher.start;
	}

}

DMXConsole{
	classvar <>xposScreen=0, <>yposScreen=20;
	classvar <counter = 0;
	var <>dmx;
	var <sliderviews;
	var <showconsole;
	var <tabpane;
	var <sliders;
	var <>w, <watcher;

	*new { |dmx, w| 
		^super.new.w_(w).dmx_(dmx).init;
	}
	
	init { 
		var f, xsize, ysize;
		counter = counter + 1;

		xsize = 16*37 + 15;
		ysize = 220;
	
		w = w ?? { 
			w = GUI.window.new("DMX Console", Rect(xposScreen, yposScreen, xsize + 10, ysize)).front;
			w.view.decorator = FlowLayout( w.bounds, 2@2, 2@2);
			w;
		};

		// unclutter the windows on the screen:
		yposScreen = yposScreen + 160;
		if ( yposScreen > 700,
			{ yposScreen = 20; xposScreen = xposScreen + xsize + 10;
				if ( xposScreen > 900,
					{ xposScreen = 0; });
			});

		//sliderview = GUI.scrollPane.new( w, w.view.bounds )
		tabpane = JSCTabbedPane.new( w, Rect( w.view.bounds.left, w.view.bounds.top, w.view.bounds.width-15, w.view.bounds.height-15) );
		//.verticalScrollBarShown_( \never ).horizontalScrollBarShown_( \auto );

		sliders = Array.new;
		sliderviews = 16.collect{ |it2|
			var gv;
			gv = GUI.compositeView.new( tabpane, Rect( 0, 0, 16*37 + 4, 170) );
			gv.decorator = FlowLayout.new(gv.bounds, 2@2, 2@2);
			sliders = sliders ++ 16.collect{ |it|
				DMXSlider.new( dmx, gv, (37*it)+2 ).channel_( it + (16*it2) ).value_(0);
			};
			tabpane.setTitleAt( it2, (""++(it2*16)++"-"++((it2+1)*16-1)) );
			gv;
		};

	/*		sliders = 16.collect{ |it|
			DMXSlider.new( dmx, sliderview, (37*it)+5 ).channel_( it ).value_(0);
		};*/

		w.refresh;

		w.front;

		watcher = SkipJack.new( { this.updateVals }, 0.1, { w.isClosed }, (\dmx_console_ ++ counter));
		watcher.start;

	}

	updateVals { 
		{ 
			// update only the currently visible slider values
			sliders.copyRange( tabpane.value * 16, (tabpane.value+1)*16-1 ).do{ |it,i|
				it.value = dmx.currentCue.data.at( it.chan.value );
				}
		}.defer;
	}

	hide{
		if ( GUI.scheme.id == \swing,
			{
				w.visible_( false );
			},{
				w.close;
			});
		watcher.stop;
	}

	show{
		if ( GUI.scheme.id == \swing,
			{
				w.visible_( true );
			});
		watcher.start;
	}
}

DMXSlider{
	var <slider, <val, <chan, <nam;
	var <view;
	var <>dmx;

	*new{ |dmx,parent,xoff|
		^super.new.dmx_(dmx).init( parent, xoff );
	}

	init{ |parent,xoffset|
		view = GUI.compositeView.new( parent, 35@152 );
		view.decorator = FlowLayout.new( view.bounds, 2@2, 2@2 );
		slider = GUI.slider.new( view, 35@80 ).action_({ |slid|
			val.valueAction_( slid.value );
		});
		val = GUI.numberBox.new( view, 35@20 ).action_({ |nb|
			var curVal;
			curVal = nb.value.clip(0,1);
			dmx.currentCue.data.put( chan.value, curVal );
			if ( dmx.autoSet, { dmx.setCue } );
			val.value = curVal;
			slider.value = curVal;
			//slider.value = nb.value;
		}).step_(1/256);
		chan = GUI.numberBox.new( view, 35@20 );
		nam = GUI.textField.new( view, 35@20 ).action_({ |tf|
			dmx.map.put( tf.value.asSymbol, chan.value.asInteger );
		});
	}

	channel_{ |cval|
		chan.value = cval;
		nam.value = dmx.map.findKeyForValue( cval );
	}

	value_{ |cval|
		val.value = cval;
		slider.value = cval;
	}
}