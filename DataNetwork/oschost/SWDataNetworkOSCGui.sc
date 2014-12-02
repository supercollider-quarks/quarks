SWDataNetworkOSCClientGui{
	classvar <>xposScreen=0, <>yposScreen=20, <>font;
	classvar <>xsize = 235;
	classvar <>xsizeBig = 207;

	classvar <>ysize = 60;
	var <>w, <>client, <watcher;
	var <>cw;

	var <key,<mpongs,<nsub,<nset,<addrIP,<addrPort;
	var <type;
	var <expandBut;
	var <subs,<sets;

	var <parent;

	var <editKey = false;

	*initClass{
		StartUp.add( { 
			if ( GUI.scheme.notNil ){
				if ( thisProcess.platform.name == \linux ){
					this.font = GUI.font.new( "Lucida Sans", 9 );
				};
				if ( thisProcess.platform.name == \osx ){
					this.font = GUI.font.new( "Helvetica", 9 );
				}; 
			};
		} );
	}

	//	var <monitor;

	*new{ |client, w,xpos=0,ypos=0| 
		^super.new.w_(w).client_(client).init(xpos,ypos);
	}
	
	init { |xpos,ypos|
		var decorator;

		w = w ?? { 
			w = GUI.window.new("SW OSC client", Rect(xposScreen, yposScreen, xsize, ysize )).front; 
			w.view.decorator = FlowLayout(Rect(0, 0, xsize, ysize), 2@2, 2@2);
			decorator = w.view.decorator;
			w;
		};

		if ( decorator.isNil, {
			try { decorator = w.view.decorator };
			try { decorator = w.decorator };
		});

		cw = GUI.compositeView.new( w, Rect( xpos, ypos, xsize, ysize ) ).resize_( 2 );
		cw.decorator = FlowLayout(Rect( xpos, ypos, xsize, ysize), 2@2, 2@2);
		cw.background = Color.white;
		decorator = cw.decorator;
		
		addrIP = GUI.staticText.new( cw, Rect( 0, 0, 100, 16 )).string_( client.addr.ip.asString ).font_( font ).align_( \right );

		addrPort = GUI.staticText.new( cw, Rect( 0, 0, 40, 16 )).string_( client.addr.port.asString ).font_( font ).align_( \right );

		key = GUI.textField.new( cw, Rect( 0, 0, 120, 16 )).string_( client.key.asString ).action_( { |tf| client.key = tf.value.asSymbol; editKey = false; } ).font_( font );

		key.mouseDownAction = { editKey = editKey.not; };

		mpongs = GUI.textField.new( cw, Rect( 0, 0, 20, 16 )).string_( client.missedPongs.asString ).font_( font );

		nsub = GUI.textField.new( cw, Rect( 0, 0, 20, 16 )).string_( client.subscriptions.size.asString ).font_( font );

		nset = GUI.textField.new( cw, Rect( 0, 0, 20, 16 )).string_( client.setters.size.asString ).font_( font );

		type = GUI.staticText.new( cw, Rect( 0, 0, 60, 16 )).string_( 
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				"hive"
			}{ "" };
			//	client.key.asString
		).font_( font );

		decorator.nextLine;

		GUI.staticText.new( cw, Rect( 0, 0, 65, 16 )).string_( "subscriptions" ).font_( font ).align_( \left );

		subs = GUI.textField.new( cw, Rect( 0, 0, xsize - 75, 16 )).string_( client.subscriptions.asArray.sort.asString ).font_( font ).align_( \left ).resize_(2);

		decorator.nextLine;

		GUI.staticText.new( cw, Rect( 0, 0, 65, 16 )).string_( "setters" ).font_( font ).align_( \left );
		
		sets = GUI.textField.new( cw, Rect( 0, 0, xsize - 75, 16 )).string_( client.setters.collect{ |it| it.id }.asArray.sort.asString ).font_( font ).align_( \left ).resize_(2);
		
		watcher = SkipJack.new({ { this.updateVals }.defer }, 1.0, { w.isClosed }, (\dataoscclientgui ++ client.addr).asSymbol, autostart: false );

		//	watcher.start;		
		w.refresh;

	}

	parent_{ |p|
		parent = p;
		addrIP.mouseOverAction = { parent.setInfo( "Address IP of this client" ) };
		addrPort.mouseOverAction = { parent.setInfo( "Address port of this client" ) };
		mpongs.mouseOverAction = { parent.setInfo( "Missed pongs" ) };
		nsub.mouseOverAction = { parent.setInfo( "Number of subscriptions" ) };
		nset.mouseOverAction = { parent.setInfo( "Number of setters" ) };
		key.mouseOverAction = { parent.setInfo( "label of this client. Click to edit." ) };
		if ( expandBut.notNil ){
			expandBut.mouseOverAction = { parent.setInfo( "show node with slots" ) };
		};
		/*		slots.do{ |it|
			it.parent = parent;
			}*/
	}

	updateRate_{ |dt|
		watcher.dt = dt;
		//	slots.do{ |it| it.dt = dt };
	}

	updateVals { 
			{ 
			mpongs.string_( client.missedPongs.asString );

			nsub.string_( client.subscriptions.size.asString );
			nset.string_( client.setters.size.asString );

			subs.string_( client.subscriptions.asArray.sort.asString );

			sets.string_( client.setters.collect{ |it| it.id }.asArray.sort.asString );

			if ( editKey.not ){ key.string_( client.key.asString ); };
			//	slots.do{ |it| it.updateVals };
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
		//	watcher.start;
	}
	
	start{
		watcher.start;
	}

}


SWDataNetworkOSCGui{
	classvar <>xposScreen=0, <>yposScreen=20;
	var <>w, <network, <watcher;

	var <key,<verb,<debug,<nodes,<bigNodes;
	var <restore,<backup,<announce,<log;
	var <clientview,<nv2;
	var <logview;
	var <watch,<worry;
	var <info;

	var <editKey = false;

	var xpos, ypos;

	*new { |network, w| 
		^super.new.w_(w).network_(network).init;
	}
	
	network_{ |n|
		network = n;
		network.gui = this;
	}
	
	init {
		var xsize, ysize;
		var nvsize,svsize;
		xsize = 500;
		ysize = 460;

		w = w ?? { 
			w = GUI.window.new("DataNetwork client connections", Rect(xposScreen, yposScreen, xsize, ysize )).front; 
			w.view.decorator = FlowLayout(Rect(2, 2, xsize, ysize), 2@2, 2@2);
			w;
		};

		announce = GUI.button.new( w, Rect( 0, 0, 80, 20 )).states_(
			[ [ "ANNOUNCE", Color.black, Color.green ] ] ).action_( { network.announce } ).mouseOverAction_({ this.setInfo( "announce the network") });

		verb = GUI.button.new( w, Rect( 0, 0, 30, 20 )).states_(
			[ [ "V0", Color.red ], ["V1", Color.red ], [ "V2", Color.red ], [ "V3", Color.red ] ] ).action_( { |but| network.verbose.level_( but.value ) } ).mouseOverAction_({ this.setInfo( "set the verbosity level") });

		log = GUI.button.new( w, Rect( 0, 0, 40, 20 )).states_(
			[ [ "log >", Color.green ], ["log []", Color.red ] ] ).action_( { |but| if ( but.value == 1 ){ network.initLog }{ network.closeLog } } ).mouseOverAction_({ this.setInfo( "write a log to file") });

		backup = GUI.button.new( w, Rect( 0, 0, 55, 20 )).states_(
			[ [ "backup", Color.blue ] ] ).action_( { network.backupClients} ).mouseOverAction_({ this.setInfo( "backup client configuration") });

		restore = GUI.button.new( w, Rect( 0, 0, 55, 20 )).states_(
			[ [ "restore", Color.blue ] ] ).action_( { network.restoreClients} ).mouseOverAction_({ this.setInfo( "restore client configuration") });

		info = GUI.staticText.new( w, Rect( 0, 0, 220, 16 )).align_( \center );

		w.view.decorator.nextLine;

		nvsize = 500;
		clientview = GUI.scrollView.new( w, Rect( 0,0, nvsize, ysize - 30 - 120 - 2 ) ).resize_( 5 );

		logview = GUI.textView.new( w, Rect( 0, 0, nvsize, 120) ).editable_( false ).resize_(8).hasVerticalScroller_( true );

		// clientview:
		ysize = (SWDataNetworkOSCClientGui.ysize + 2) * network.clientDictionary.size + 2;
		//	xsize =  1000;
		nv2 = GUI.compositeView.new( clientview, Rect( 0,0, xsize, ysize ) ).resize_(5);

		ypos = -1 * SWDataNetworkOSCClientGui.ysize - 2;

		SWDataNetworkOSCClientGui.xsize = xsize;

		nodes = [];

		if ( network.clientDictionary.size > 0 ) {
			nodes = network.clientDictionary.collect{ |it,key|
				ypos = ypos + 2 + SWDataNetworkOSCClientGui.ysize;
				SWDataNetworkOSCClientGui.new( it, nv2, 2, ypos ).parent_( this );
			}.asArray;
		};
 						
		watcher = SkipJack.new({ {this.updateVals}.defer }, 1.0, { w.isClosed }, (\datanetworkoscgui).asSymbol, autostart: false );

		watcher.start;		
		w.refresh;

		w.acceptsMouseOver = true;

		w.onClose = { network.gui = nil };

		network.gui = this;
	}

	setInfo{ |string|
		defer{ info.string_( string ); }
	}

	addLogMsg{ |msg|
		defer{
			logview.string_( logview.string ++ Date.localtime.asString + ":" + msg + "\n" );
			};
	}

	addClient{ |client|
		var ysize;
		defer{
			ysize = SWDataNetworkOSCClientGui.ysize + 2 * network.clientDictionary.size + 2;
			ypos = ypos + 2 + SWDataNetworkOSCClientGui.ysize;

			nv2.bounds_( Rect( 0, 0, SWDataNetworkOSCClientGui.xsize, ysize ) );

			nodes = nodes.add( SWDataNetworkOSCClientGui.new( client, nv2, 2, ypos ).parent_( this ); );
			nv2.refresh;			
		}
	}

	
	removeClient{ |client|
		nodes.removeAllSuchThat( { |it| it.client == client } );
		this.refreshClients;
	}

	refreshClients{
		var newNodes = nodes.collect{ |it| it.client };
		var nbn,sn;
		
		defer{
			nv2.removeAll;

			//	nv2.children.postln;

			ypos = -1 * SWDataNetworkOSCClientGui.ysize - 2;

			nodes = Array.new;
			newNodes.do{ |it|
				nbn = this.addClient( it );
			};
			nv2.refresh;
			}
	}
 
	updateRate_{ |dt|
		watcher.dt = dt;
		nodes.do{ |it| it.dt = dt };
	}

	updateVals { 
		//	{ 
			//	if ( editKey.not ){key.string_( network.spec.name.asString );};
		nodes.do{ |it| it.updateVals };
		//	}.defer;
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
