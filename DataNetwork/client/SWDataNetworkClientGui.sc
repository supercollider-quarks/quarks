SWDataSlotClientGui : SWDataSlotGui {
	var <sub,<get;

	var <xsize = 185;

	addSubButton{
		sub = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
			[ [ "Sub", Color.blue ], ["Uns", Color.red ] ] ).action_( {
				|but| parent.subSlot( slot.id, but.value );
			}).font_( font );
	}

	addGetButton{
		get = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
			[ [ "Get", Color.blue ] ] ).action_( {
				|but| parent.getSlot( slot.id );
			}).font_( font );
	}

	parent_{ |p|
		parent = p;
		super.parent_( p );
		sub.mouseOverAction = { parent.setInfo( "[Sub]scribe or [uns]ubscribe to this slot." ) };
		get.mouseOverAction = { parent.setInfo( "Get the value of this slot." ) };
	}

}

SWDataNodeClientGui : SWDataNodeGui {
	classvar <>xsize = 330;
	classvar <>xsizeBig = 283;

	classvar <slottype;

	var <sub,<get;

	*initClass{
		slottype = SWDataSlotClientGui;
	}

	addSubGetButtons{
		sub = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
			[ [ "Sub", Color.blue ], ["Uns", Color.red ] ] ).action_( {
				|but| parent.subNode( node.id, but.value );
			}).font_( font );

		get = GUI.button.new( cw, Rect( 0, 0, 25, 16 )).states_(
			[ [ "Get", Color.blue ] ] ).action_( {
				|but| parent.getNode( node.id );
			}).font_( font );
	}

	setSetter{ |onoff|
		defer{
			if ( onoff ){
				cw.background = Color(1.0,1.0,0.75,1.0);
			}{
				cw.background = Color.white;
			};
			if ( bigNode.notNil ){ bigNode.setSetter( onoff ); };
			sub.enabled = onoff.not;
			get.enabled = onoff.not;
			slots.do{ |it|
				it.sub.enabled = onoff.not;
				it.get.enabled = onoff.not;
			};
		}
	}

	setSub{ |onoff|
		defer{
			sub.value = onoff;
			if ( bigNode.notNil ){ bigNode.setSub( onoff ); };
			// subscribed to node, no need to subscribe to slot;
			get.enabled = onoff.booleanValue.not;
			slots.do{ |it|
				it.sub.enabled = onoff.booleanValue.not;
				it.get.enabled = onoff.booleanValue.not;
			};
		};
	}

	setSlotSub{ |id,onoff|
		defer {
			if ( bigNode.notNil ){ bigNode.setSlotSub( id, onoff ); };
			if ( slots.size > 0 ){
				slots[id].sub.value = onoff;
				slots[id].get.enabled = onoff.booleanValue.not;
			}
		}
	}

	parent_{ |p|
		parent = p;
		super.parent_(p);
		sub.mouseOverAction = { parent.setInfo( "[Sub]scribe or [uns]ubscribe to this node." ) };
		get.mouseOverAction = { parent.setInfo( "Get the values of this node." ) };
	}

	bigNode_{ |bn|
		super.bigNode_( bn );
		//	if ( parent.notNil ){ parent.updateSubscriptions }{
		bigNode.setSub( sub.value );
		bigNode.setSetter( sub.enabled.not );
		//	}
	}

}


SWDataNetworkClientGui : SWDataNetworkGui{

	classvar <slottype;
	classvar <nodetype;

	var <regButton;

	var <>subsetChanged = false;

	*initClass{
		slottype = SWDataSlotClientGui;
		nodetype = SWDataNodeClientGui;
	}


	addQueryButtons{
		var bs = 85;
		subsetChanged = true;
		regButton = GUI.button.new( w, Rect( 0, 0, bs, 16 )).states_(
			[ [ "Register", Color.blue ],["Unregister", Color.red ] ] ).action_( {
				|but| if ( but.value == 1 ){ network.register; }{ network.unregister };
			}).font_( font );
		/*
		GUI.button.new( w, Rect( 0, 0, bs, 16 )).states_(
			[ [ "Unregister", Color.blue ] ] ).action_( {
				|but| network.unregister;
			}).font_( font );
		*/
		GUI.button.new( w, Rect( 0, 0, bs, 16 )).states_(
			[ [ "Query all", Color.blue ] ] ).action_( {
				|but| network.queryAll;
			}).font_( font );

		GUI.button.new( w, Rect( 0, 0, bs, 16 )).states_(
			[ [ "Query expected", Color.blue ] ] ).action_( {
				|but| network.queryExpected;
			}).font_( font );

		GUI.button.new( w, Rect( 0, 0, bs, 16 )).states_(
			[ [ "Query nodes", Color.blue ] ] ).action_( {
				|but| network.queryNodes;
			}).font_( font );

		GUI.button.new( w, Rect( 0, 0, bs, 16 )).states_(
			[ [ "Query slots", Color.blue ] ] ).action_( {
				|but| network.querySlots;
			}).font_( font );

		GUI.button.new( w, Rect( 0, 0, bs, 16 )).states_(
			[ [ "Query clients", Color.blue ] ] ).action_( {
				|but| network.queryClients;
			}).font_( font );

		GUI.button.new( w, Rect( 0, 0, bs + 15, 16 )).states_(
			[ [ "Query subscriptions", Color.blue ] ] ).action_( {
				|but| network.querySubscriptions;
			}).font_( font );

		GUI.button.new( w, Rect( 0, 0, bs, 16 )).states_(
			[ [ "Query setters", Color.blue ] ] ).action_( {
				|but| network.querySetters;
			}).font_( font );


		w.view.decorator.nextLine;
	}

	getSlot{ |slotid|
		network.getSlot( slotid );
	}

	getNode{ |nodeid|
		network.getNode( nodeid );
	}

	subSlot{ |slotid,sub|
		if ( sub == 1){
			network.subscribeSlot( slotid );
		}{
			network.unsubscribeSlot( slotid );
		}
	}

	subNode{ |nodeid,sub|
		if ( sub == 1){
			network.subscribeNode( nodeid );
		}{
			network.unsubscribeNode( nodeid );
		}
	}

	setSetter{ |nodeid|
		var mynode = nodes.detect{ |it| it.node.id == nodeid };
		if ( mynode.notNil ){
			mynode.setSetter( true );
		};
	}

	setSlotSub{ |slotid,sub|
		var mynode = nodes.detect{ |it| it.node.id == slotid[0] };
		if ( mynode.notNil ){
			mynode.setSlotSub( slotid[1], sub );
		};
	}

	setNodeSub{ |nodeid,sub|
		var mynode = nodes.detect{ |it| it.node.id == nodeid };
		if ( mynode.notNil ){
			mynode.setSub( sub );
		};
	}

	updateReg{
		regButton.value = network.registered.binaryValue;
	}

	updateSubscriptions{
		if ( subsetChanged ){
			// resets all:
			nodes.do{ |it|
				it.setSub( 0 );
				it.setSetter( false );
				it.slots.do{ |jt,j|
					it.setSlotSub( j, 0 );
				};
			};
			network.subscriptions.do{ |it|
				//it.postln;
				if ( it.isArray ){ // slot
					this.setSlotSub( it, 1 );
				}{ // node
					this.setNodeSub( it, 1 );
				}
			};
			network.setters.do{ |it|
				this.setSetter( it );
			};
			subsetChanged = false;
		}
	}

}
