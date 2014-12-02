SWDataNetworkOSCClient{

	var <>key;
	var <addr;

	var <active = false;

	var <missedPongs = 0;
	var <subscriptions;
	var <setters;

	var <nodeSubs;
	var <slotSubs;
	var <slotNodesSubs;


	*new{ |addr,name|
		^super.new.init( addr,name );
	}

	init{ |address,name|
		addr = address;
		key = name.asSymbol;

		subscriptions = Set.new;

		slotNodesSubs = Set.new;
		nodeSubs = Set.new;
		slotSubs = IdentityDictionary.new;

		setters = IdentityDictionary.new;
		//		this.sendRegistered;
	}

	sendRegistered{
		addr.sendMsg( '/registered', addr.port.asInteger, key.asString );
	}

	sendClientInfo{ |address|
		address.sendMsg( '/info/client', addr.ip, addr.port.asInteger, key.asString );
	}

	addr_{ |newaddr|
		addr = newaddr;
		//	addr.sendMsg( '/registered', addr.port.asInteger );
	}

	ping{
		addr.sendMsg( '/ping', addr.port.asInteger, key.asString );
		missedPongs = missedPongs + 1;
	}

	pong{
		this.active = true;
		missedPongs = 0;
		//		missedPongs = missedPongs - 1;
	}

	active_{ |a|
		active = a;
		//	thisProcess.dumpBackTrace;
	}


	addSetter{ |node|
		var existing;
		// check for nodes with same id:
		/*
		existing = setters.select( { |it| it.id == node.id });
		existing.postln;
		if ( existing.notNil ){
			existing.do{ |it| setters.remove( it ).postln; };
		};
		setters.add( node );
		*/
		setters.put( node.id, node );
		addr.sendMsg( '/info/setter', node.id, node.key.asString, node.slots.size, node.type );
	}

	setterQuery{
		if ( setters.size == 0, {
			^false;
		});
		setters.do{ |it|
			addr.sendMsg( '/info/setter', it.id, it.key.asString, it.slots.size, it.type );
		};
		^true;
	}

	welcomeBack{
		//	this.dump;
		this.sendRegistered;
		this.pong;
		this.setterQuery;		
		this.subscriptionQuery;
		setters.do{ |it|
			this.newNode( it );
		};
	}

	hostQuit{ |myhost|
		addr.sendMsg( '/unregistered', addr.port.asInteger, key.asString );
		addr.sendMsg( '/datanetwork/quit', myhost.hostname, myhost.port.asInteger );
	}

	checkForSetter{ |node|
		^setters.at(node.id).notNil;
	}

	subscriptionQuery{
		if ( subscriptions.size == 0, {
			^false;
		});

		nodeSubs.do{ |it|
			this.newExpected( it );
			addr.sendMsg( '/subscribed/node', addr.port, key.asString, it );
		};
		slotNodesSubs.do{ |it|
			slotSubs[it].do{ |jt|
				addr.sendMsg( '/subscribed/slot', addr.port, key.asString, it, jt );
			}
		};
		/*
		subscriptions.do{ |it|
			//			it.postln;
			if ( it.isKindOf( Array ),
				{
					addr.sendMsg( '/subscribed/slot', addr.port, it[0], it[1] );
				},{
					addr.sendMsg( '/subscribed/node', addr.port, it );
				})
		};
		*/
		^true;
	}


	unsubscribeAll{
		nodeSubs.copy.do{ |it|
			this.unsubscribeNode( it );
		};
		slotNodesSubs.copy.do{ |it|
			slotSubs[it].copy.do{ |jt|
				this.unsubscribeSlot( it, jt );
			}
		};
	}

	subscribeNode{ |id|
		subscriptions.add( id );

		nodeSubs.add( id );

		addr.sendMsg( '/subscribed/node', addr.port, key.asString, id );
	}

	subscribeSlot{ |id1,id2|
		subscriptions.add( [id1, id2] );

		slotNodesSubs.add( id1 );
		if ( slotSubs.at(id1).isNil ){
			slotSubs.put( id1, Set.new );
		};
		slotSubs[id1].add( id2 );

		addr.sendMsg( '/subscribed/slot', addr.port, key.asString, id1, id2 );
	}

	unsubscribeNode{ |id|
		subscriptions.remove( id );
		nodeSubs.remove( id );
		addr.sendMsg( '/unsubscribed/node', addr.port, key.asString, id );
	}

	unsubscribeSlot{ |id1,id2|
		subscriptions.remove( [id1, id2] );

		slotSubs[id1].remove( id2 );

		if ( slotSubs[id1].size == 0 ){
			slotSubs.removeAt(id1);
			slotNodesSubs.remove( id1 );
		};

		addr.sendMsg( '/unsubscribed/slot', addr.port, key.asString, id1, id2 );
	}

	newExpected{ |id,label|
		if ( label.notNil ){
			addr.sendMsg( '/info/expected', id, label.asString );
		}{
			addr.sendMsg( '/info/expected', id, "" );
		}
	}

	newNode{ |node|
		//	node.dump;
		addr.sendMsg( '/info/node', node.id, node.key.asString, node.slots.size, node.type );
		/*
		node.slots.do{ |it,i|
			this.newSlot( it );
		};
		*/
	}

	newSlot{ |slot|
		addr.sendMsg( '/info/slot', slot.id[0], slot.id[1], slot.key.asString, slot.type );
	}

	newBee{ |bee|
		addr.sendMsg( '/info/minibee', bee.id, bee.noInputs, bee.noOutputs );
	}

	nodeRemoved{ |id|
		if ( subscriptions.includes( id ),{
			addr.sendMsg( '/removed/node', id );
		});
	}

	sendData{ |id,data|
		var msg;
		//		if ( verbose, { 
		//		["sendData", id,data].postln;// } );
		if ( subscriptions.includes( id ),
			{
				msg = ['/data/node', id] ++ data;
				//	"node subscribed".postln;
				//	msg.postln;
				addr.sendMsg( *msg );
			});
		data.do{ |it,i|
			if ( subscriptions.includes( [id,i] ),
				{
					//	"slot subscribed".postln;
					addr.sendMsg( '/data/slot', id, i, it );
				})
		};
	}

	sendDataNode{ |node|
		var msg;
		//		if ( verbose, { 
		//		["sendData", id,data].postln;// } );
		// check node subscriptions:
		if ( nodeSubs.includes(node.id),
			//		if ( subscriptions.includes( node.id ),
			{
				msg = ['/data/node', node.id] ++ node.data;
				//	"node subscribed".postln;
				//	msg.postln;
				addr.sendMsg( *msg );
			});
		if ( slotNodesSubs.includes( node.id ),{
			slotSubs[node.id].do{ |it|
				//	"slot subscribed".postln;
				addr.sendMsg( '/data/slot', node.id, it, node.slots[it].value );
			};
		});
	}
}
