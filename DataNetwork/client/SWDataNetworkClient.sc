SWDataNetworkClient : SWDataNetwork{

	var <host;
	var <myaddr;
	var responders;
	var <lasttime;

	var <>name;

	var <subscriptions,<setters;

	// these could be moved to SWDataNetwork later on, as they may be useful there too, to abstract the newNodeHooks.
	//	var <hooks;
	
	var <>autoregister = true;
	// do not set unless you are the class itself
	var <registered = false;

	*new{ |hostip,name="",reg=true|
		^super.new.init.myInit( hostip,name, reg );
	}

	myInit{ |hst,nm="",reg=true|
		var ip,prefix,foundHost;
		name = nm;
		lasttime = Process.elapsedTime;
		host = NetAddr( hst, NetAddr.langPort);

		foundHost = this.findHost;

		if ( thisProcess.platform.name == \linux, {
			prefix = "/sbin/";
		},{ prefix = "" });

		ip = NetAddr.myIP(prefix);
		//		network = SWDataNetwork.new;
		myaddr = NetAddr( ip, NetAddr.langPort );

		subscriptions = Set.new;
		setters = Set.new;

		//	hooks = SWHookSet.new;

		this.addResponders;

		if ( reg and: foundHost ){
			this.register;
		}{
			if ( reg ){ // make sure that we start worrying right away, so we keep looking for the host
				lasttime = lasttime - worrytime;
			}
		};

		watcher = SkipJack.new( { this.worryAboutTime }, 1, name: "SWDataNetworkClient", autostart: true );			

		ShutDown.add(
			{ 
				if ( this.registered ){ this.unregister; };
			}
		)

	}

	registered_{ |reg|
		registered = reg;
		if ( registered ){
			"Registered as client at DataNetwork".postln;
			this.queryAll;
			subscriptions.do{ |it|
				if ( it.isArray ){
					this.subscribeSlot( it );
				}{
					this.subscribeNode( it );
				}
			};
			setters.do{ |it|
				if ( nodes[it].notNil ){
					this.addExpected( it, spec.findNode( it ), nodes[it].size, nodes[it].type );
				}{
					this.addExpected( it, spec.findNode( it ) );
				};
			};
		}{
			"Unregistered as client at DataNetwork".postln;
		};

	}

	addResponders{
		responders = [
			OSCresponderNode( nil, '/datanetwork/announce', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.setHost( *(msg.copyToEnd( 1 )) );
			}),
			OSCresponderNode( host, '/datanetwork/quit', { |t,r,msg,addr|
				verbose.value( 2, msg );
				// could do checking of hostname and port!
				this.lostHost( *(msg.copyToEnd( 1 )) );
			}),
			OSCresponderNode( host, '/error', { |t,r,msg,addr|
				"DataNetwork Error: ".post; msg.postln;
				if ( gui.notNil ){ gui.setInfo( msg )};
			}),
			OSCresponderNode( host, '/warn', { |t,r,msg,addr|
				"DataNetwork Warning: ".post; msg.postln;
				if ( gui.notNil ){ gui.setInfo( msg )};
			}),
			OSCresponderNode( host, '/ping', { |t,r,msg,addr|
				verbose.value( 3, msg );
				this.sendPong;
			}),
			OSCresponderNode( host, '/registered', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.registered_( true );				
			}),
			OSCresponderNode( host, '/unregistered', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.registered_( false );				
			}),
			OSCresponderNode( host, '/info/expected', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.addExpected( msg[1], msg[2], fromnw: true );				
			}),
			OSCresponderNode( host, '/info/node', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.nodeInfo( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/info/slot', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.slotInfo( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/info/client', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.clientInfo( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/info/setter', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.setterInfo( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/subscribed/node', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.subscribeNodeInfo( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/subscribed/slot', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.subscribeSlotInfo( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/unsubscribed/node', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.unsubscribeNodeInfo( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/unsubscribed/slot', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.unsubscribeSlotInfo( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/removed/node', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.removeNode( msg[1], true );
			}),
			OSCresponderNode( host, '/data/node', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.nodeData( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/data/slot', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.slotData( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/info/minibee', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.minibeeInfo( msg.copyToEnd( 1 ) );
			}),
			OSCresponderNode( host, '/mapped/minibee/output', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.mappedNode( msg[1], msg[2], 'output' );
			}),
			OSCresponderNode( host, '/mapped/minibee/custom', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.mappedNode( msg[1], msg[2], 'custom' );
			}),
			OSCresponderNode( host, '/unmapped/minibee/output', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.unmappedNode( msg[1], msg[2], 'output' );
			}),
			OSCresponderNode( host, '/unmapped/minibee/custom', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.unmappedNode( msg[1], msg[2], 'custom' );
			}),
			OSCresponderNode( host, '/mapped/minihive/output', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.mappedNode( msg[1], -1, 'output' );
			}),
			OSCresponderNode( host, '/mapped/minihive/custom', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.mappedNode( msg[1], -1, 'custom' );
			}),
			OSCresponderNode( host, '/unmapped/minihive/output', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.unmappedNode( msg[1], -1, 'output' );
			}),
			OSCresponderNode( host, '/unmapped/minihive/custom', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.unmappedNode( msg[1], -1, 'custom' );
			}),
			OSCresponderNode( host, '/configured/minibee', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.configuredBee( msg[1], msg[2] );
			}),
			OSCresponderNode( host, '/minihive/configuration/created', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.createdConfig( msg[1], msg.copyToEnd( 2 ) );
			}),
			OSCresponderNode( host, '/minihive/configuration/deleted', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.deletedConfig( msg[1], msg[2] );
			}),
			OSCresponderNode( host, '/minihive/configuration/saved', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.savedConfig( msg[1], msg[2] );
			}),
			OSCresponderNode( host, '/minihive/configuration/loaded', { |t,r,msg,addr|
				verbose.value( 2, msg );
				this.loadedConfig( msg[1], msg[2] );
			}),
		];

		responders.do{ |it| it.add };
	}

	removeResponders{
		responders.do{ |it| it.remove };
	}

	/// ---- host interaction ---

	setHost{ |ip,port|
		host = NetAddr( ip.asString, port );
		this.resetHost;
	}

	resetHost{ |addr|
		this.removeResponders;
		this.addResponders;
		if ( autoregister, {
			this.unregister;
			this.register;
		});
	}

	// used to find the port on which the host is listening
	findHost{ |ip|
		var port;
		if ( ip.isNil, { ip = host.hostname });
		port = ("curl -s http://" ++ ip ++ "/SenseWorldDataNetwork").unixCmdGetStdOut.asInteger;
		if ( port == 0 ){
			"The port I found is 0. This may be an indication that the http access to the host machine is not set up correctly. Try and visit: http://" ++ ip ++ "/SenseWorldDataNetwork to verify this. If you cannot reach that page, then check the helpfile [SW_Apache_setup] for instructions to make the http setup on the host work properly".warn;
			^false;
		}{
			host.port = port;
			^true;
		}
	}

	lostHost{ |ip,port|
		"DataNetwork host has quit".postln;
		if ( gui.notNil ){ gui.setInfo( "DataNetwork host has quit" )};
	}

	tryReconnect{
		if ( this.findHost ){
			this.resetHost;
		};
	}

	osc_{
		"cannot create a osc-datanetwork-host from a client".warn;
	}

	// ------------

	worryAboutTime{
		if ( Process.elapsedTime - lasttime > worrytime,
			{
				this.tryReconnect;
			});
	}

	// overloaded from base class
	addExpected{ |id,label,size=nil,type=0,fromnw=false|
		if ( fromnw.not, {
			if ( size.isNil ){
				this.sendMsgWithArgs( '/add/expected', [ id ] );
			}{
				if ( label.isNil ){
					this.sendMsgWithArgs( '/add/expected', [ id, size ] );
				}{
					this.sendMsgWithArgs( '/add/expected', [ id, size, label, type] );
				}
			};
		},{
			hooks.perform( \expected, id, [id,label,size,type] );
			("DataNetwork: expected node %, with label % and % slots".format( id, label, size )).postln;
			// use the method from the super-class
			super.addExpected( id, label, size, type );
		});
	}

	// overloaded from base class
	setData{ |id,data,fromnw=false|
		//	var type;
		var ret;
		verbose.value( 2, [id,data] );
		//	if ( verbose > 1, { [id,data].postln; } );
		
		ret = super.setData( id, data );
		
		/*
		if ( nodes[id].isNil, {
			type = this.checkDataType( data );
			ret = this.registerNode( id, data.size, type );
			if ( verbose > 0 ) { ("registering node"+id+ret).postln; };
		});
		*/

		if ( ret == 0 ) { 
			//	nodes[id].data = data;
			if ( fromnw.not, {
				this.sendData( id, data );
			});
		};
	}

	// overloaded from base class
	add{ |key, slot,fromnw=true|
		var ns;
		super.add( key, slot );
		//	spec.add( key, slot );
		if ( fromnw.not, {
			ns = this.at( key );
			if ( ns.isKindOf( SWDataNode ),{
				this.labelNode( ns );
			});
			if ( ns.isKindOf( SWDataSlot ),{
				this.labelSlot( ns );
			});
		});
	}

	// overloaded from base class
	removeNode{ |id,fromnw=false|
		verbose.value( 2, ("remove" + id) );
		//	if ( verbose > 0, { ("remove" + id).postln; });
		if ( fromnw.not ){
			this.sendMsgWithArgs( '/remove/node', [ id.asInteger] );
		}{
			super.removeNode( id.asInteger );
			//		nodes.removeAt( id.asInteger );			
		};	
	}

	mappedNode{ |id,mid,type|
		("Mapped node"+id+"to minibee"+mid+type).postln;
	}

	unmappedNode{ |id,mid,type|
		("Unmapped node"+id+"from minibee"+mid+type).postln;
	}

	configuredBee{ |id,mid|
		("Configured minibee"+id+"with configuration"+mid).postln;
	}

	createdConfig{ |id,config|
		("Created configuration"+id+"as"+config).postln;
	}

	deletedConfig{ |id|
		("Deleted configuration"+id).postln;
	}

	savedConfig{ |id|
		("Saved configuration"+id).postln;
	}

	loadedConfig{ |id|
		("Loaded configuration"+id).postln;
	}

	/// OSC interface

	sendSimpleMsg{ |msg|
		host.sendMsg( msg, NetAddr.langPort, name.asString );
	}

	sendMsgWithArgs{ |msg, args|
		var fullMsg = [ msg, NetAddr.langPort, name.asString ]++args;
		host.sendMsg( *fullMsg );
	}

	register{
		this.sendSimpleMsg( '/register');
	}

	unregister{
		this.sendSimpleMsg( '/unregister');
	}

	// Querying ---

	queryAll{
		this.sendSimpleMsg( '/query/all' );
	}

	queryExpected{
		this.sendSimpleMsg( '/query/expected' );
	}

	queryNodes{
		this.sendSimpleMsg( '/query/nodes' );
	}

	querySlots{
		this.sendSimpleMsg( '/query/slots' );
	}

	querySetters{
		this.sendSimpleMsg( '/query/setters' );
	}

	querySubscriptions{
		this.sendSimpleMsg( '/query/subscriptions' );
	}

	queryClients{
		this.sendSimpleMsg( '/query/clients' );
	}

	queryBees{
		this.sendSimpleMsg( '/query/minibees' );
	}


	// -- Subscribing --

	subscribeAll{ 
		this.sendSimpleMsg( '/subscribe/all' );
	}

	unsubscribeAll{ 
		this.sendSimpleMsg( '/unsubscribe/all' );
	}

	removeAll{ 
		this.sendSimpleMsg( '/remove/all' );
	}

	configureBee{ |miniBee,cid|
		var mb;
		if ( miniBee.isKindOf( SWMiniBee ) ){
			mb = miniBee.id;
		}{
			mb = miniBee;
		};
		this.sendMsgWithArgs( '/configure/minibee', [mb,cid] )
	}

	createConfig{ |...config|
		this.sendMsgWithArgs( '/minihive/configuration/create', config )
	}

	deleteConfig{ |cid|
		this.sendMsgWithArgs( '/minihive/configuration/delete', [cid] )
	}

	saveConfig{ |name|
		this.sendMsgWithArgs( '/minihive/configuration/save', [name] )
	}

	loadConfig{ |name|
		this.sendMsgWithArgs( '/minihive/configuration/load', [name] )
	}

	mapBee{ |node,miniBee,type=\output|
		var id,mb;
		if ( node.isKindOf( SWDataNode ) ){
			id = node.id;
		}{
			id = node;
		};
		if ( miniBee.isKindOf( SWMiniBee ) ){
			mb = miniBee.id;
		}{
			mb = miniBee;
		};
		switch( type,
			'custom', { this.sendMsgWithArgs( '/map/minibee/custom', [id, mb] )},
			'output', { this.sendMsgWithArgs( '/map/minibee/output', [id, mb] )}
		);
	}

	unmapBee{ |node,miniBee,type=\output|
		var id,mb;
		if ( node.isKindOf( SWDataNode ) ){
			id = node.id;
		}{
			id = node;
		};
		if ( miniBee.isKindOf( SWMiniBee ) ){
			mb = miniBee.id;
		}{
			mb = miniBee;
		};
		switch( type,
			'custom', { this.sendMsgWithArgs( '/unmap/minibee/custom', [id, mb] )},
			'output', { this.sendMsgWithArgs( '/unmap/minibee/output', [id, mb] )}
		);
	}

	mapHive{ |node,type=\output|
		var id;
		if ( node.isKindOf( SWDataNode ) ){
			id = node.id;
		}{
			id = node;
		};
		switch( type,
			'custom', { this.sendMsgWithArgs( '/map/minihive/custom', [id] )},
			'output', { this.sendMsgWithArgs( '/map/minihive/output', [id] )}
		);
	}

	unmapHive{ |node,type=\output|
		var id;
		if ( node.isKindOf( SWDataNode ) ){
			id = node.id;
		}{
			id = node;
		};
		switch( type,
			'custom', { this.sendMsgWithArgs( '/unmap/minihive/custom', [id] )},
			'output', { this.sendMsgWithArgs( '/unmap/minihive/output', [id] )}
		);
	}

	subscribeNode{ |node|
		if ( node.isKindOf( SWDataNode ) ){
			this.sendMsgWithArgs( '/subscribe/node', node.id );
		}{
			if ( node.isKindOf( Symbol ) ){
				this.subscribeBySymbol( node );
			}{
				this.sendMsgWithArgs( '/subscribe/node', node );
			}
		}
	}

	subscribeBySymbol{ |label|
		var thisone = this.at( label );
		if ( thisone.isNil ){
			"WARNING: no node or slot found with this name".postln;
		}{
			if ( thisone.isKindOf( SWDataNode ) ){
				this.sendMsgWithArgs( '/subscribe/node', thisone.id );				
			};
			if ( thisone.isKindOf( SWDataSlot ) ){
				this.sendMsgWithArgs( '/subscribe/slot', thisone.id );				
			}
		};
	}

	unsubscribeBySymbol{ |label|
		var thisone = this.at( label );
		if ( thisone.isNil ){
			"WARNING: no node or slot found with this name".postln;
		}{
			if ( thisone.isKindOf( SWDataNode ) ){
				this.sendMsgWithArgs( '/unsubscribe/node', thisone.id );				
			};
			if ( thisone.isKindOf( SWDataSlot ) ){
				this.sendMsgWithArgs( '/unsubscribe/slot', thisone.id );				
			}
		};
	}

	getBySymbol{ |label|
		var thisone = this.at( label );
		if ( thisone.isNil ){
			"WARNING: no node or slot found with this name".postln;
		}{
			if ( thisone.isKindOf( SWDataNode ) ){
				this.sendMsgWithArgs( '/get/node', thisone.id );				
			};
			if ( thisone.isKindOf( SWDataSlot ) ){
				this.sendMsgWithArgs( '/get/slot', thisone.id );				
			}
		};
	}

	unsubscribeNode{ |node|
		if ( node.isKindOf( SWDataNode ) ){
			this.sendMsgWithArgs( '/unsubscribe/node', node.id );
		}{
			if ( node.isKindOf( Symbol ) ){
				this.unsubscribeBySymbol( node );
			}{
				this.sendMsgWithArgs( '/unsubscribe/node', node );
			}
		}
	}

	subscribeSlot{ |slot|
		if ( slot.isKindOf( SWDataSlot ) ){
			this.sendMsgWithArgs( '/subscribe/slot', slot.id );
		}{
			if ( slot.isKindOf( Symbol ) ){
				this.subscribeBySymbol( slot );
			}{
				this.sendMsgWithArgs( '/subscribe/slot', slot );
			}
		}
	}

	unsubscribeSlot{ |slot|
		if ( slot.isKindOf( SWDataSlot ) ){
			this.sendMsgWithArgs( '/unsubscribe/slot', slot.id );
		}{
			if ( slot.isKindOf( Symbol ) ){
				this.unsubscribeBySymbol( slot );
			}{
				this.sendMsgWithArgs( '/unsubscribe/slot', slot );
			}
		}
	}

	getNode{ |node|
		if ( node.isKindOf( SWDataNode ) ){
			this.sendMsgWithArgs( '/get/node', node.id );
		}{
			if ( node.isKindOf( Symbol ) ){
				this.getBySymbol( node );
			}{
				this.sendMsgWithArgs( '/get/node', node );
			}
		}
	}

	getSlot{ |slot|
		if ( slot.isKindOf( SWDataSlot ) ){
			this.sendMsgWithArgs( '/get/slot', slot.id );
		}{
			if ( slot.isKindOf( Symbol ) ){
				this.getBySymbol( slot );
			}{
				this.sendMsgWithArgs( '/get/slot', slot );
			}
		}
	}

	// ----------

	nodeInfo{ |msg|
		this.addExpected( msg[0], msg[1], fromnw: true );
		this.registerNode( msg[0], msg[2], msg[3] );
		//	"node info ".post; msg.postln;
		if ( msg[1] != 0,{
			this.add( msg[1], msg[0], true );
		});
		("DataNetwork: info node %, label %, % slots and type %".format( msg[0], msg[1], msg[2], msg[3] )).postln;
	}

	slotInfo{ |msg|
		//	"slot info ".post; msg.postln;
		if ( msg[2] !=  0,{
			this.add( msg[2], [msg[0],msg[1].asInteger], true );
		});
		("DataNetwork: info slot [%, %], label %".format( msg[0], msg[1], msg[2] )).postln;
	}

	minibeeInfo{ |msg|
		//	"slot info ".post; msg.postln;
		/*		if ( msg[2] !=  0,{
			this.add( msg[2], [msg[0],msg[1].asInteger], true );
			});*/
		("DataNetwork: info minibee: id %, number of inputs %, number of outputs %".format( *msg )).postln;
	}

	nodeData{ |msg|
		this.setData( msg[0], msg.copyToEnd( 1 ), true );
	}

	slotData{ |msg|
		nodes.at( msg[0] ).slots.at( msg[1].asInteger ).value = msg[2];
		nodes.at( msg[0] ).setLastTime;
	}

	// ----

	unsubscribeNodeInfo{ |msg|
		("unsubscribed node"+msg).postln;
		subscriptions.remove( msg[2] );
		//		if ( gui.notNil ){ gui.setNodeSub( msg[1], 0 )};
		if ( gui.notNil ){ gui.subsetChanged = true };
	}

	unsubscribeSlotInfo{ |msg|
		("unsubscribed slot"+msg).postln;
		subscriptions.remove( [msg[2],msg[3]] );
		//		if ( gui.notNil ){ gui.setSlotSub( [msg[1],msg[2]], 0 )};
		if ( gui.notNil ){ gui.subsetChanged = true };
	}

	subscribeNodeInfo{ |msg|
		("subscribed node"+msg).postln;
		subscriptions.add( msg[2]);
		//		if ( gui.notNil ){ gui.setNodeSub( msg[1], 1 )};
		if ( gui.notNil ){ gui.subsetChanged = true };
	}

	subscribeSlotInfo{ |msg|
		("subscribed slot"+msg).postln;
		subscriptions.add( [msg[2],msg[3]] );
		//		if ( gui.notNil ){ gui.setSlotSub( [msg[1],msg[2]], 1 )};
		if ( gui.notNil ){ gui.subsetChanged = true };
	}

	clientInfo{ |msg|
		("client:"+msg).postln;
		if ( gui.notNil ){ gui.setInfo( "client:" + msg )};
	}

	setterInfo{ |msg|
		("setter of node: "+msg).postln;
		setters.add( msg[0]);
		hooks.perform( \setter, msg[0], msg );
		if ( gui.notNil ){ 
			gui.setInfo( "setter of node:" + msg );
			gui.subsetChanged = true;
			//			gui.setSetter( msg[0] );
		};
	}

	// ---

	labelNode{ |node|
		this.sendMsgWithArgs( '/label/node', [node.id, node.key] );
	}

	labelSlot{ |slot|
		this.sendMsgWithArgs( '/label/slot', slot.id ++ slot.key );
	}

	//-------------

	sendData{ |id, data|
		this.sendMsgWithArgs( '/set/data', [id] ++ data );
		//	host.sendMsg( '/set/data', NetAddr.langPort, name.asString, id, *data );
	}


	sendPong{
		this.sendSimpleMsg( '/pong' );
		lasttime = Process.elapsedTime;
	}

	/*
	addHook{ |id,action,type=\newnode|
		//	"adding hook DN".postln;
		hooks.add( type, id, action );
	}
	*/

	// -------

	makeGui{
		^SWDataNetworkClientGui.new( this );
	}
}
