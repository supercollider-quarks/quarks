// =====================================================================
// SenseWorld DataNetwork base GUI
// =====================================================================

SWDataNetworkBaseGui {

	var <network;
	var <w,button1,button2,button4;
	var button3;

	*new{ |network|
		^super.new.network_( network ).init;
	}
	
	network_{ |n|
		network = n;
		n.baseGui = this;
	}

	asClient{ |onoff=true|
		button2.enabled_( onoff.not );
		button4.enabled_( onoff.not );
	}

	init{
		w = Window.new( "SenseWorld DataNetwork", Rect( 0, 130, 400, 150 ) );
		w.view.decorator = FlowLayout.new( Rect( 0, 0, 400, 100), 5@5, 5@5 );

		button1 = Button.new( w, Rect( 0, 0, 190, 80)).states_( [["View data nodes"]]).action_( {network.makeNodeGui} ).font_( GUI.font.new( "Helvetica", 20));

		button2 = Button.new( w, Rect( 0, 0, 190, 80)).states_( [["View clients"]]).action_( { 
			if ( network.osc.isNil )
			{ 
				"no OSC interface present, adding OSC interface to network".warn;
				network.addOSCInterface;
			};
			network.osc.makeGui;
		} ).font_( GUI.font.new( "Helvetica", 20));


		button3 = Button.new( w, Rect( 0, 0, 190, 30)).states_( [["Record log"]]).action_( {network.makeLogGui} ).font_( GUI.font.new( "Helvetica", 16));

		button4 = Button.new( w, Rect( 0, 0, 190, 30)).states_( [["View MiniBees"]]).action_( {network.osc.makeHiveGui} ).font_( GUI.font.new( "Helvetica", 16));

		// spacer
		StaticText.new( w, Rect( 0, 0, 190, 20));

		StaticText.new( w, Rect( 0, 0, 190, 20)).string_( [ NetAddr.myIP, NetAddr.langPort ].asString ).font_( GUI.font.new( "Helvetica", 12));

		w.front;
	}
}