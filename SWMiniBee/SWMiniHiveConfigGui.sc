SWMiniHiveConfigGui{

	var w,view,hview,view2;
	var <confs;
	var <header;
	var <save;
	var <hiveConf;

	var <configEdit;

	*new{ |hc|
		^super.new.init(hc);
	}

	init{ |hc|
		hiveConf = hc;
		
		w = Window.new("MiniHive Configuration", Rect( 200, 0, 500, 300 + 380 ));
		
		hview = CompositeView.new(w, Rect( 0,0, 500, 50));
		hview.addFlowLayout(2@2);

		save = [
			TextField.new( hview, 325@20 ),
			Button.new( hview, 60@20 ).states_( [ ["save"] ]).action_( { |b|
				hiveConf.save( this.getSaveString );
			}),
			Button.new( hview, 60@20 ).states_( [ ["load"] ]).action_( { |b|
				hiveConf.load( this.getSaveString );
				this.updateGui;
			})
		];

		header = [
			StaticText.new( hview, 130@20 ).string_("serial number").align_( \center ), // serial number
			StaticText.new( hview, 60@20 ).string_( "node ID" ).align_( \center ), // node ID
			StaticText.new( hview, 150@20 ).string_( "configuration" ).align_( \center ), // choice of configs
			StaticText.new( hview, 40@20 ).string_( "status" ).align_( \center ), // active/not active
			StaticText.new( hview, 80@20 ).string_( "send config").align_( \center ), // send config
		];

		view = ScrollView.new( w, Rect( 0,50, 500, 235 ));
		view.addFlowLayout(2@2);

		confs = List.new;

		configEdit = SWMiniBeeConfigGui.new( nil, w, Rect(0, 360, 500, 380 ) );
		configEdit.hive = hiveConf;

		this.updateGui;
		w.front;
	}

	getSaveString{
		var str = save[0].string;
		if ( str.isNil ){ ^str };
		if ( str.size == 0 ){ ^nil }{ ^str };
	}

	getConfigLabelMenu{
		^([ "*new*" ] ++ hiveConf.configLabels.order({ |a,b| a.value < b.value }));
	}

	getConfigMenuValue{ |label|
		^this.getConfigLabelMenu.indexOf( label );
	}

	addLine{ |key|
				
		confs.add([
			StaticText.new( view, 130@20 ), // serial number
			StaticText.new( view, 60@20 ).align_('center'), // node ID
			PopUpMenu.new( view, 150@20 ).items_( 
				this.getConfigLabelMenu;
			).action_({ |men|
				//	men.value.postln;
				if ( men.value > 0 ){
					//	"making deep Copy in updating menu in lines action".postln;
					configEdit.config_( 
						hiveConf.getConfigByLabel( men.items[men.value].asSymbol ),
						hiveConf );
					hiveConf.setConfig( men.items[men.value].asSymbol, key );
				}
			}), // choice of configs
			Button.new( view, 40@20 ).states_( [
				['s',Color.black, Color.blue], // (0) has sent serial
				['w',Color.black, Color.yellow], // (1) is waiting for config
				['c',Color.black, Color.green], // (2) has confirmed config
				['d',Color.black, Color.green], // (3) is sending data
				['x', Color.black, Color.red ], // (4) stopped sending data
				['i', Color.black, Color.white ] // (5) inactive
			]), // active/not active
			//			Button.new( view, 60@20 ).states_( [['known'],['send'],['define']]), // send config
			Button.new( view, 60@20 ).states_( [['send']]).action_({
				hiveConf.hive.sendID( key );
			}), // send config
		]);
	}

	updateLine{ |key|
		var configID, bee;
		bee = hiveConf.getBee( key );

		confs.last[0].string_( key );

		if ( bee.notNil ){
			confs.last[1].string_( bee.nodeID );

			configID = hiveConf.getConfigIDLabel( bee.configLabel );

			if ( configID.notNil ){
				confs.last[2].value_( this.getConfigMenuValue( bee.configLabel ) );
			}{
				confs.last[2].value_( 0 );
			};

			// set status
			confs.last[3].value_( bee.status );
		};
	}


	updateMenu{
		confs.do{ |it|
			it[2].items_( 
				this.getConfigLabelMenu;
			);
		};
	}

	updateGui{
		defer{
			view.removeAll;
			view.decorator.reset;
			confs = List.new;
		
			hiveConf.hiveMap.keys.do{ |key,i|
				this.addLine(key);
				this.updateLine( key );
			}
		}
	}
}