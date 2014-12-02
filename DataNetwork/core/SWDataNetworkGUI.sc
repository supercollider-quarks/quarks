SWDataSlotGui{
	classvar <>xposScreen=0, <>yposScreen=20;
	classvar <>font, <>ysize = 36;
	var <>w, <>slot, <watcher;
	var <cw;

	var <key,<val,<bus,<debug,<slider,<mon;

	var <parent;

	var <xsize = 150;
	var <editKey = false;
	var decorator;


	//	var <monitor;

	*initClass{
		StartUp.add( {
			if ( GUI.scheme.notNil ){
				Platform.case(
					\linux, { this.font = GUI.font.new( "Lucida Sans", 9 ); },
					\osx, { this.font = GUI.font.new( "Helvetica", 9 ) },
					\windows, { this.font = GUI.font.new( "Helvetica", 9 ) }
				)
			};
		} );
	}

	*new { |slot, w|
		^super.new.w_(w).slot_(slot).init;
	}

	init {
		var xsize, ysize;

		//	ysize = 80;

		w = w ?? {
			w = GUI.window.new("SWDataSlot", Rect(xposScreen, yposScreen, xsize, ysize )).front;
			w.view.decorator = FlowLayout(Rect(0, 0, xsize, ysize), 2@2, 2@2);
			decorator = w.view.decorator;
			w;
		};

		if ( decorator.isNil, {
			try { decorator = w.view.decorator };
			try { decorator = w.decorator };
		});


		/*
		cw = GUI.compositeView.new( w, Rect( decorator.left, decorator.top, xsize, ysize ) );
		cw.decorator = FlowLayout(Rect(2, 2, xsize, ysize), 2@2, 2@2);
		decorator = cw.decorator;
		*/

		cw = w;

		cw.background = Color.gray(0.8);


		GUI.staticText.new( cw, Rect( 0, 0, 25, 16 )).string_( slot.id[1].asString ).font_( font ).align_( \right );

		key = GUI.textField.new( cw, Rect( 0, 0, 65, 16 )).string_( slot.key.asString ).action_( { |tf| slot.key = tf.value.asSymbol; editKey = false; } ).font_( font );

		key.mouseDownAction = { editKey = editKey.not; };
		//		decorator.nextLine;

		if ( slot.type == 0 ){
			this.buildNumberGui;
		}{
			this.buildStringGui;
		};

		watcher = SkipJack.new({ defer{ this.updateVals } }, 1.0, { w.isClosed }, (\dataslotgui ++ slot.id).asSymbol, autostart: false );

		this.updateVals;

		//	watcher.start;
		w.refresh;
	}

	buildStringGui{
		//		val = GUI.numberBox.new( cw, Rect( 0, 0, 35, 16 )).value_( slot.value ).font_( font );

		// spacer
		decorator.shift( 35 + decorator.gap.x, 0 );

		debug = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
			[ [ "Db", Color.blue ], ["Db", Color.red ] ] ).action_( {
				|but| if ( but.value == 1, { slot.debug_( true ) }, { slot.debug_( false ) } ); } ).font_( font );

		this.addSubButton;
		this.addGetButton;

		decorator.nextLine;

		val = GUI.staticText.new( cw, Rect( 0, 0, 205, 16 )).font_( font ).background_( Color.white );


	}


	buildNumberGui{
		val = GUI.numberBox.new( cw, Rect( 0, 0, 35, 16 )).value_( slot.value ).font_( font );

		debug = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
			[ [ "Db", Color.blue ], ["Db", Color.red ] ] ).action_( {
				|but| if ( but.value == 1, { slot.debug_( true ) }, { slot.debug_( false ) } ); } ).font_( font );

		mon = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
			[ [ "Mon", Color.blue ], ["Mon", Color.red ] ] ).action_( {
				|but| slot.monitor( but.value.booleanValue );
			}).font_( font );

		bus = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
			[ [ "Bus", Color.blue ], ["Bus", Color.red ] ] ).action_( {
				|but| if ( but.value == 1, {
					slot.createBus
				}, {
					slot.freeBus;
				});
			}).font_( font );

		this.addSubButton;

		decorator.nextLine;

		slider = GUI.slider.new( cw, Rect( 0, 0, 210, 16 ) );

		this.addGetButton;

	}

	// overload in subclass
	addSubButton{
	}

	// overload in subclass
	addGetButton{
	}

	updateRate_{ |dt|
		watcher.dt = dt;
	}

	updateVals {
		//	{
		if ( slot.type == 0 ){
			val.value_( slot.value.round(0.001) );
			if ( slot.map.notNil, {
				slider.value_( slot.map.unmap( slot.value ) );
			},{
				slider.value_( slot.value );
			});
		}{
			val.string_( slot.value );
		};
		if ( editKey.not ){ key.string_( slot.key.asString ); };
		debug.value_( slot.debug.binaryValue );
		if ( slot.type == 0 ){
			mon.value_( slot.isMonitored.binaryValue );
			bus.value_( slot.bus.notNil.binaryValue );
		};
			//	}.defer;
	}

	parent_{ |p|
		parent = p;
		val.mouseOverAction = { parent.setInfo( "current value of the slot" ) };
		debug.mouseOverAction = { parent.setInfo( "turn on debugging" ) };
		if ( slot.type == 0 ){
			mon.mouseOverAction = { parent.setInfo( "monitor data of this slot" ) };
			bus.mouseOverAction = { parent.setInfo( "create bus for this slot" ) };
		};
		key.mouseOverAction = { parent.setInfo( "label of this slot. Click to edit." ) };	}

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

SWDataNodeGui{
	classvar <>xposScreen=0, <>yposScreen=20, <>font;
	classvar <>xsize = 270;
	classvar <>xsizeBig = 230;
	classvar <slottype;


	var <ysize;
	var <>w, <>node, <watcher;
	var <>cw;

	var <key,<elaps,<bus,<debug,<mon,<rec;
	var <expandBut,<nslots;

	var <slots;

	var <parent;
	var <bigNode;



	var <editKey = false;

	*initClass{
		StartUp.add( {
			if ( GUI.scheme.notNil ){
				Platform.case(
					\linux, { this.font = GUI.font.new( "Lucida Sans", 9 ); },
					\osx, { this.font = GUI.font.new( "Helvetica", 9 ) },
					\windows, { this.font = GUI.font.new( "Helvetica", 9 ) }
				)
			};
		} );
		slottype = SWDataSlotGui;
	}

	//	var <monitor;

	*new { |node, w,xpos=0,ypos=0|
		^super.new.w_(w).node_(node).initBig(xpos,ypos);
	}

	*newSmall{ |node, w,xpos=0,ypos=0|
		^super.new.w_(w).node_(node).initSmall(xpos,ypos);
	}

	init { |xpos,ypos|
		var decorator;

		w = w ?? {
			w = GUI.window.new("SWDataNode", Rect(xposScreen, yposScreen, this.class.xsize, ysize )).front;
			w.view.decorator = FlowLayout(Rect(0, 0, this.class.xsize, ysize), 2@2, 2@2);
			decorator = w.view.decorator;
			w;
		};

		if ( decorator.isNil, {
			try { decorator = w.view.decorator };
			try { decorator = w.decorator };
		});

		cw = GUI.compositeView.new( w, Rect( xpos, ypos, this.class.xsize, ysize ) );
		cw.decorator = FlowLayout(Rect( xpos, ypos, this.class.xsize, ysize), 2@2, 2@2);
		cw.background = Color.white;
		decorator = cw.decorator;


		GUI.staticText.new( cw, Rect( 0, 0, 25, 16 )).string_( node.id.asString ).font_( font ).align_( \right );

		key = GUI.textField.new( cw, Rect( 0, 0, 65, 16 )).string_( node.key.asString ).action_( { |tf| node.key = tf.value.asSymbol; editKey = false; } ).font_( font );

		key.mouseDownAction = { editKey = editKey.not; };

		//		decorator.nextLine;

		//		decorator.nextLine;

		elaps = GUI.numberBox.new( cw, Rect( 0, 0, 35, 16 )).value_( node.elapsed.round( 0.001 ) ).font_( font );

		rec = GUI.button.new( cw, Rect( 0, 0, 15, 16 )).states_(
			[ [ "R", Color.blue, Color.gray ], ["R", Color.black, Color.red ] ] ).action_( {
				|but| if ( but.value == 1, { node.record_( true ) }, { node.record_( false ) } ); } ).font_( font );

		debug = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
			[ [ "Db", Color.blue ], ["Db", Color.red ] ] ).action_( {
				|but| if ( but.value == 1, { node.debug_( true ) }, { node.debug_( false ) } ); } ).font_( font );

		if ( node.type == 0 ){
			mon = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
				[ [ "Mon", Color.blue ], ["Mon", Color.red ] ] ).action_( {
					|but|
					node.monitor( but.value.booleanValue );
					/*
					if ( but.value.booleanValue ){
						node.datamonitor.makeGui( node.key.asString );
					};
					*/
				} ).font_( font );

			bus = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
				[ [ "Bus", Color.blue ], ["Bus", Color.red ] ] ).action_( {
					|but| if ( but.value == 1, {
						node.createBus
					}, {
						node.freeBus;
					});
				} ).font_( font );
		};

		this.addSubGetButtons;

		watcher = SkipJack.new({ defer{this.updateVals} }, 1.0, { w.isClosed }, (\datanodegui ++ node.id).asSymbol, autostart: false );

		this.updateVals;

		//	watcher.start;
		w.refresh;
	}

	// overload in subclass
	addSubGetButtons{
	}

	parent_{ |p|
		parent = p;
		elaps.mouseOverAction = { parent.setInfo( "time elapsed since last update" ) };
		debug.mouseOverAction = { parent.setInfo( "turn on debugging" ) };
		rec.mouseOverAction = { parent.setInfo( "turn on recording mode" ) };
		if ( node.type == 0 ){
			mon.mouseOverAction = { parent.setInfo( "monitor data of this node" ) };
			bus.mouseOverAction = { parent.setInfo( "create bus for this node" ) };
		};
		key.mouseOverAction = { parent.setInfo( "label of this node. Click to edit." ) };
		if ( expandBut.notNil ){
			expandBut.mouseOverAction = { parent.setInfo( "show node with slots" ) };
			nslots.mouseOverAction = { parent.setInfo( "number of slots of this node" ) };
		};
		slots.do{ |it|
			it.parent = parent;
		}
	}

	expand{ |exp|
		//		("expand " + exp).postln;
		if ( exp == 1, {
			if ( parent.isNil,{
				this.bigNode = this.class.new( node );
			},{
				this.bigNode = parent.addNodeBig( node );
			})
		},{
			if ( parent.isNil,{
				bigNode.close;
			},{
				parent.removeNodeBig( bigNode );
			});
			bigNode = nil;
		});
	}

	initSmall{ |xpos,ypos|
		ysize = 20;
		this.init( xpos, ypos );

		nslots = GUI.staticText.new( cw, Rect( 0, 0, 20, 16 )).string_( node.slots.size.asString ).font_( font ).align_( \center );

		expandBut = GUI.button.new( cw, Rect( 0, 0, 15, 16 )).states_(
			[ [ ">", Color.blue ], ["<", Color.red ] ] ).action_( {
				|but| this.expand( but.value );
			} ).font_( font );

	}

	bigNode_{ |bn|
		bigNode = bn;
	}

	initBig{  |xpos,ypos|
		var decorator;
		ysize = 20 + (node.slots.size*this.class.slottype.ysize);

		this.init( xpos, ypos );

		// expandBut.enabled_( false );

		decorator = cw.decorator;

		// decorator.nextLine;

		slots = node.slots.collect{ |it,i|
			//			decorator.top.postln;
			decorator.nextLine;
			this.class.slottype.new( node.slots[i], cw );
		};

		//		decorator.top.postln;

	}

	updateRate_{ |dt|
		watcher.dt = dt;
		slots.do{ |it| it.dt = dt };
	}

	updateVals {
		//	{
		elaps.value_( node.elapsed.round(0.001) );
		rec.value_( node.record.binaryValue );
		debug.value_( node.debug.binaryValue );
		if ( node.type == 0 ){
			mon.value_( node.isMonitored.binaryValue );
			bus.value_( node.bus.notNil.binaryValue );
		};
		if ( node.elapsed < watcher.dt ){
			if ( editKey.not ){ key.string_( node.key.asString ); };
			slots.do{ |it| it.updateVals };
		};
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
		//	watcher.start;
	}

	start{
		watcher.start;
	}

}


SWDataNetworkGui{
	classvar <>xposScreen=0, <>yposScreen=20;

	classvar <slottype;
	classvar <nodetype;
	classvar <>font;

	var <>w, <>network, <watcher;

	var <key,<verb,<debug,<nodes,<bigNodes;
	var <nodeview,<slotview,<nv2,<sv2;
	var <watch,<worry;
	var <info;

	var <editKey = false;

	var xpos, ypos;

	*initClass{
		StartUp.add( {
			if ( GUI.scheme.notNil ){
		Platform.case(
				\linux, { this.font = GUI.font.new( "Lucida Sans", 9 ); },
				\osx, { this.font = GUI.font.new( "Helvetica", 9 ) }
				)
			};
		} );
		slottype = SWDataSlotGui;
		nodetype = SWDataNodeGui;
	}

	*new { |network, w|
		^super.new.w_(w).network_(network).init;
	}

	init {
		var xsize, ysize;
		var nvsize,svsize;
		var ysub;
		xsize = 800;
		ysize = 500; // + (node.data.size*82);

		w = w ?? {
			w = GUI.window.new("SWDataNetwork", Rect(xposScreen, yposScreen, xsize, ysize )).front;
			w.view.decorator = FlowLayout(Rect(2, 2, xsize, ysize), 2@2, 2@2);
			w;
		};


		key = GUI.textField.new( w, Rect( 0, 0, 250, 20 )).string_( network.spec.name.asString ).action_( { |tf| network.spec.name = tf.value; editKey = false; } ).mouseOverAction_({ this.setInfo( "the network spec name. Click to edit.") });

		key.mouseDownAction = { editKey = editKey.not; };

		verb = GUI.button.new( w, Rect( 0, 0, 30, 20 )).states_(
			[ [ "V0", Color.red ], ["V1", Color.red ], [ "V2", Color.red ], [ "V3", Color.red ] ] ).value_( network.verbose.level ).action_( { |but| network.verbose.level = but.value } ).mouseOverAction_({ this.setInfo( "set the verbosity level") });


		debug = GUI.button.new( w, Rect( 0, 0, 30, 20 )).states_(
			[ [ "Db", Color.blue ], ["Db", Color.red ] ] ).action_( {
				|but| if ( but.value == 1, { network.debug_( true ) }, { network.debug_( false ) } ); } ).mouseOverAction_({ this.setInfo( "turn on debugging for the network") });

		watch = GUI.button.new( w, Rect( 0, 0, 30, 20 )).states_(
			[ [ "W", Color.blue ], ["W", Color.red ] ] ).action_( {
				|but| if ( but.value == 1, { network.watch( true ) }, { network.watch( false ) } ); } ).mouseOverAction_({ this.setInfo( "watch the network") });

		worry = GUI.numberBox.new( w, Rect( 0, 0, 50, 20 ) ).value_( network.worrytime ).mouseOverAction_({ this.setInfo( "worrytime of the network.") });

		//clients =
		GUI.button.new( w, Rect( 0, 0, 30, 20 )).states_(
			[ [ "OSC", Color.blue ] ] ).action_( {
				|but| if ( network.osc.notNil, { network.osc.makeGui } ); } ).mouseOverAction_({ this.setInfo( "create a window with the osc clients") });

		GUI.button.new( w, Rect( 0, 0, 30, 20 )).states_(
			[ [ "log", Color.blue ] ] ).action_( {
				|but| network.makeLogGui } ).mouseOverAction_({ this.setInfo( "create a window to log the data from the nodes") });

		info = GUI.staticText.new( w, Rect( 0, 0, 316, 16 )).align_( \center );

		w.view.decorator.nextLine;

		this.addQueryButtons;

		ysub = w.view.decorator.top + (w.view.decorator.gap.y*2);

		nvsize = this.class.nodetype.xsize + 20;
		svsize = xsize - nvsize - 6;
		nodeview = GUI.scrollView.new( w, Rect( 0,0, nvsize, ysize - ysub ) ).resize_( 4 );

		slotview = GUI.scrollView.new( w, Rect( 0,0, svsize, ysize - ysub ) ).resize_( 5 );

		// nodeview:
		ysize = 22 * network.nodes.size + 4;
		xsize =  this.class.nodetype.xsize;
		nv2 = GUI.compositeView.new( nodeview, Rect( 0,0, xsize, ysize ) );


		// slotview:
		ysize = 50; xsize = 50;
		sv2 = GUI.compositeView.new( slotview, Rect( 0,0, xsize, ysize ) );

		xpos = -1 * this.class.nodetype.xsizeBig;
		ypos = -1 * 20 - 2;

		if ( network.nodes.size > 0 ) {
			nodes = network.nodes.asSortedArray.collect{ |it,key|
				//				it.postln;
				ypos = ypos + 2 + 20;
				this.class.nodetype.newSmall(
					network.nodes.at( it[1].id ), nv2, 2, ypos
				).parent_( this );

			};
		};

		watcher = SkipJack.new({ defer{ this.updateVals} }, 1.0, { w.isClosed }, (\datanetworkgui_ ++ network.spec.name).asSymbol, autostart: false );

		watcher.start;
		w.refresh;

		w.acceptsMouseOver = true;

		network.gui = this;

		this.updateSubscriptions;

		w.onClose = { network.gui = nil };
	}

	// overload in subclass
	addQueryButtons{
	}

	updateSubscriptions{
	}

	setInfo{ |string|
		defer{ info.string_( string ); };
	}

	addNodeSmall{ |node|
		var ysize;
		defer {
			if ( nodes.select( { |it| it.node.id == node.id } ).size == 0 ){
				ysize = 22 * network.nodes.size + 4;
				ypos = ypos + 2 + 20;
				nv2.bounds_( Rect( 0, 0, this.class.nodetype.xsize, ysize ) );
				nodes = nodes.add(
					this.class.nodetype.newSmall( node, nv2, 2, ypos )
					.parent_( this );
				);
			};
		}
	}

	updateSlotView{
		var xsize, ysize = 50;
		if ( bigNodes.size > 0 , {
			ysize = 20 + (bigNodes.collect{ |it| it.node.slots.size }.maxItem*SWDataSlotGui.ysize);
		});
		xsize =  (this.class.nodetype.xsizeBig+2) * bigNodes.size + 4;

		sv2.bounds_( Rect( 0, 0, xsize, ysize ) );
	}

	addNodeBig{ |node|
		var ysize,bigNode;
		xpos = xpos +  this.class.nodetype.xsizeBig + 2;
		bigNode = this.class.nodetype.new( node, sv2, xpos, 0 ).parent_( this );
		bigNodes = bigNodes.add( bigNode );
		this.updateSlotView;
		^bigNode;
	}

	removeNodeBig{ |bnode|
		bigNodes.remove( bnode );
		//		xpos = xpos - (SWDataNodeGui.xsize + 2);
		this.refreshBigNodes;
	}

	refreshBigNodes{
		var xsize, ysize = 50;
		//	var bnJSC = bigNodes.collect{ |it| it.cw };
		var newBigNodes = bigNodes.collect{ |it| it.node };
		var nbn,sn;
		sv2.removeAll;
		// reset the xpos:
		xpos = -1 * this.class.nodetype.xsizeBig;
		bigNodes = Array.new;
		newBigNodes.do{ |it,i|
			nbn = this.addNodeBig( it );
			// find the small node that belongs to this one:
			sn = nodes.detect( { |jt| jt.node == it });
			sn.bigNode_( nbn );
		};
		sv2.refresh;
	}

	addNode{ |node|
		this.addNodeSmall(node);
	}

	updateRate_{ |dt|
		watcher.dt = dt;
		nodes.do{ |it| it.dt = dt };
	}

	updateVals {
		//	{
		worry.value_( network.worrytime );
		if ( editKey.not ){key.string_( network.spec.name.asString );};
		nodes.do{ |it| it.updateVals };
		bigNodes.do{ |it| it.updateVals };
		this.updateReg;
		this.updateSubscriptions;
		//	}.defer;
	}

	updateReg{
		// overload in subclass
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
