SWDataNetworkLogGui {

	var <w;
	var <fn,open;

	var <recB,<closeB;
	var <stamp,<break,<dt,<breakt;

	var <>network;
	
	*new{ |network,parent|
		^super.new.network_(network).init(parent);
	}

	init{ |parent|
		
		w = Window.new( "SWDataNetworkLog", Rect( 0, 0, 200, 92 ));
		w.view.decorator = FlowLayout.new( Rect(0,0,200,92), 2@2, 2@2);
		
		fn = TextField.new(w, Rect( 0, 0, 148, 20 ));
		open = Button.new(w, Rect( 150, 0, 45, 20 )).states_( [["create"]]);

		//	nextB = Button.new( w, Rect( 0, 0, 22, 20)).states_( [["N"]]);
		recB = Button.new(w, Rect( 0, 0, 95, 20 )).states_( [["record",Color.red],["pause",Color.red]]);
		closeB = Button.new(w, Rect( 0, 0, 98, 20 )).states_( [["close"]]);

		dt = EZNumber.new( w, 95@20, "dt", [0.05,3600].asSpec, initVal: 0.05, labelWidth: 30);
		stamp = Button.new(w, Rect( 0, 0, 98, 20 )).states_( [["stamp on"],["no stamp"]]);
		break = EZNumber.new( w, 95@20, "break", [1000,100000,\linear,1].asSpec, initVal: 18000, labelWidth: 40);
		breakt = StaticText.new( w, 98@20 ).string_( (dt.value * break.value * 25).asTimeCodeString ).align_( \right );

		[recB,closeB].do{ |it| it.enabled_( false )};

		open.action_( {
			network.initRecord( fn.string, dt.value, stamp.value.booleanValue, break.value ); 
			[recB,closeB].do{ |it| it.enabled_( true )};
			[dt,break,stamp].do{ |it| it.enabled_( false )};
		} );

		closeB.action_( {
			network.closeRecord; 
			[recB,closeB].do{ |it| it.enabled_( false )};
			[dt,break,stamp].do{ |it| it.enabled_( true )};
		} );

		recB.action = { |but| network.record( but.value.booleanValue ) };

		w.front;
	}
}
