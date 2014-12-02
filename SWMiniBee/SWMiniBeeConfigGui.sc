SWMiniBeeConfigGui{
	var <config;

	var w;
	var left,right,top; //,bottom;

	var <menu;
	var label,store,send;
	var <leftpins, <rightpins;
	var <status,<check;
	var noInputs;

	var msgInt, smpMsg;
	var id;

	*new{ |config, parent, bounds|
		^super.new.init( config, parent, bounds );
	}

	bounds{
		^w.absoluteBounds;
	}

	init{ |conf, parent, bounds|
		
		if ( parent.isNil ){
			w = Window.new("MiniBee Configuration", Rect( 0, 0, 430, 350 ));
			
			top = CompositeView.new( w, Rect( 0,0, 430, 90 ));
			top.addFlowLayout(2@2,2@2);
		}{
			w = CompositeView.new( parent, bounds );

			top = CompositeView.new( w, Rect( 0,0, 430, 120 ));
			top.addFlowLayout(2@2,2@2);
			
			StaticText.new( top, 420@28 ).string_( "MiniBee Configuration").align_( \center ).background_( Color.white );
		};


		menu = PopUpMenu.new( top, 150@20 );
		label = TextField.new( top, 205@25 ).action_( { arg field; config.label = field.value } );
		if ( GUI.id == \swing ){
			label.focusLostAction_( { arg field; config.label = field.value } );
		};	
		store = Button.new( top, 50@25 ).states_( [[ "store"]]).action_({ 
			this.storeConfig;
		});
		//		send = Button.new( top, 50@25 ).states_( [[ "send"]]).action_({ "sending config".postln; });

		msgInt = EZNumber.new( top, 130@20, "delta T (ms)", [5,500,\exponential,1].asSpec, { |g| config.msgInterval = g.value; }, 50, labelWidth: 80 );
		smpMsg = EZNumber.new( top, 130@20, "samples/msg", [1,20,\linear,1].asSpec, { |g| config.samplesPerMsg = g.value; }, 1, labelWidth: 85 );
		id = EZNumber.new( top, 92@20, "config ID", [1,20,\linear,1].asSpec, {}, 1, labelWidth: 60 ).enabled_( false );
		//		noInputs = EZNumber.new( top, 80@20, "#in", labelWidth:40 );

		//		bottom = CompositeView( w, Rect( 0, 320, 430, 30 ) );

		//	top.decorator.nextLine;

		check = Button.new( top, 50@25 ).states_( [["check"]]).action_({ this.checkConfig; });
		status = StaticText.new( top, 357@25 ).background_( Color.white );

		Button.new( top, 50@25 ).states_( [["POST"]]).action_({ [config.label, config.msgInterval, config.samplesPerMsg, config.pinConfig.asCompileString ].postln; });
		
		left = CompositeView( w,  Rect(0,  top.bounds.height, 215, 260) );
		right = CompositeView( w, Rect(215,top.bounds.height, 215, 260) );


		left.addFlowLayout(2@2,2@2);
		right.addFlowLayout(2@2,2@2);
		//		bottom.addFlowLayout(2@2,2@2);

		StaticText.new( left, 180@20 ); // spacer
		
		leftpins = [ \SDA_A4, \SCL_A5, \A0, \A1, \A2, \A3, \A6, \A7 ].collect{ |it|
			this.createPin( it, left );
		};

		leftpins[0][1].action = { |b| 
			if ( b.items.at( b.value ) == \TWIData ) { 
				leftpins[1][1].value = SWMiniBeeConfig.getPinCaps( \SCL_A5 ).indexOf( \TWIClock );
			};
			status.string_( "" );
		};
		leftpins[1][1].action = { |b| 
			if ( b.items.at( b.value ) == \TWIClock ) { 
				leftpins[0][1].value = SWMiniBeeConfig.getPinCaps( \SDA_A4 ).indexOf( \TWIData );
			};
			status.string_( "" );
		};

		rightpins = (13..3).collect{ |it|
			this.createPin( ("D"++it).asSymbol, right );
		};

		this.config_( conf );

		if ( parent.isNil ){
			w.front;
		};

	}

	config_{ |conf,hconf|
		if ( config.isNil ){
			config = SWMiniBeeConfig.new;
		};
		if ( conf.notNil ){
			config.from( conf );
			if ( hconf.notNil ){
				config.hive = hconf;
			};
		};
		this.updateGui;
	}

	hive_{ |hc|
		config.hive = hc;
		this.updateMenu;
	}

	updateGui{
		var rpins,lpins;
		label.string_( config.label.asString );
		if ( config.hive.notNil ){
			id.value = config.hive.getConfigIDLabel( config.label ) ? 0;
		};

		rpins = config.pinConfig.copyRange( 0, 10 ).reverse;
		lpins = config.pinConfig.copyRange( 11, 18 ).at( [4,5, 0,1,2,3, 6,7]);
		rightpins.do{ |it,i|
			it[1].value_( it[1].items.indexOf( rpins[i] ) );
		};
		leftpins.do{ |it,i|
			it[1].value_( it[1].items.indexOf( lpins[i] ) );
		};
	}

	storeConfig{ |checkLabel=true,newlabel|
		this.checkConfig;
		if ( newlabel.notNil ){
			label.string_( newlabel );
			config.label = newlabel;
		};
		if ( checkLabel and: config.hive.getConfigIDLabel( config.label ).notNil ){
			SWMiniBeeConfigLabelChangeDialog.new( this, config.label );
		}{
			config.store;
			this.updateMenu;
			this.updateGui;
		};
	}

	checkConfig{
		var res;
		// checks validity of config and indicates errors if any
		this.getConfig;
		res = config.checkConfig;
		//	res.postln;
		status.string_( res[2] );
		w.refresh;
		// make wrong ones red
		if ( res[0] && res[1] ){
			status.string_( "configuration valid" );
		}
	}

	getConfig{
		// reads the gui status for the config
		config.label = label.value;
		config.msgInterval = msgInt.value.asInteger;
		config.samplesPerMsg = smpMsg.value.asInteger;
		config.pinConfig = this.getPinVals;
	}

	getPinVals{
		^(
			rightpins.collect{ |it| it[1].items.at( it[1].value) }.reverse ++
			leftpins.collect{ |it| it[1].items.at( it[1].value) }.at( [2,3,4,5, 0,1, 6,7])
		)
	}


	updateMenu{
		if ( config.hive.notNil ){
			menu.items_(  
				[ "*new*" ] ++ config.hive.configLabels.keys
			).action_({ |men|
				if ( men.value > 0 ){
					//	"making deep copy in update Menu action".postln;
					this.config_( 
						config.hive.getConfigByLabel( men.items[men.value] ),
						config.hive );
				}{
					this.config_( SWMiniBeeConfig.new, config.hive );
				};
			}); // choice of configs
			menu.value_( menu.items.indexOf( config.label.asSymbol ) ? 0 );
		};
	}


	createPin{ |label,parent|
		^[
			StaticText.new( parent, 50@20 ).string_( label ).align_( \right ),
			PopUpMenu.new( parent, 150@20 ).items_( 
				SWMiniBeeConfig.getPinCaps( label );
			).action_( { |men| 
				//		[men.value, men.items ].postln;
				this.getPinVals;
				status.string_( (label ++ " = " ++ men.items.at( men.value ).asString ).postln; ); } );
		]
	}



}

SWMiniBeeConfigLabelChangeDialog {
	
	var w,overwrite,rename,newLabel;
	var <>configGui;

	*new{ |configGui,label|
		^super.new.configGui_( configGui ).init( label );
	}

	init{ |label|
		w = Window.new( "Confirm name", configGui.bounds.setExtent( 410, 130 ) );
		w.addFlowLayout;
		
		TextView.new( w, 400@60 ).string_( "The label you have specified already exists.\nDo you want to overwrite the configuration or choose a new name?").editable_(false).align_( \center );

		newLabel = TextField.new( w, 400@22 ).string_( label ++ "_1" );
		
		overwrite = Button.new( w, 195@22 ).states_( [["overwrite"]] ).action_( { configGui.storeConfig( false ); w.close; });
		rename = Button.new( w, 195@22 ).states_( [["rename"]] ).action_( { configGui.storeConfig( true, newLabel.string ); w.close; });
		w.front;
	}

}