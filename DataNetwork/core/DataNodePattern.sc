// Pattern support for SWDataNetwork 

PdataSlot : Pattern {
	var <>slot,<>node,<>network;
	var <>repeats;
	var <>pSlot;

	*new { arg slot, node, network, repeats=inf;
		^super.newCopyArgs(slot, node, network, repeats)
	}


	storeArgs { ^[slot, node, network, repeats] }

	embedInStream { arg inval;

		// slot and type streams
		var slotStr = slot.asStream;
		var nodeStr = node.asStream;
		var slotVal, nodeVal, slotnodes;

		repeats.value.do({

			slotVal = slotStr.next(inval);
			nodeVal = nodeStr.next(inval);

			slotnodes = [slotVal, nodeVal].flop;

			inval = slotnodes.collect{ |it|
				var ret;
				if ( network.nodes[ it[1] ].isNil,
					{ "node not found".warn;
						ret = inval;
					},{
					if ( network.nodes[ it[1] ].slots[ it[0] ].isNil,
						{ "slot not found".warn;
							ret = inval;
						},{
							ret = network.nodes[ it[1] ].slots[ it[0] ].value;
						});
					});
				ret;
			}.unbubble.yield;

		});

		^inval;
	}
}

PdataNode : Pattern {
	var <>node,<>network;
	var <>repeats;
	var <>pSlot;

	*new { arg node, network, repeats=inf;
		^super.newCopyArgs(node, network, repeats)
	}

	storeArgs { ^[node, network, repeats] }

	embedInStream { arg inval;

		// slot and type streams
		var nodeStr = node.asStream;
		var nodeVal;

		repeats.value.do({

			nodeVal = nodeStr.next(inval);
			nodeVal = nodeVal.asArray;

			inval = nodeVal.collect{ |it|
				var ret;
				if ( network.nodes[ it ].isNil,
					{ "node not found".warn;
						ret = inval;
					},{
						ret = network.nodes[ it ].data;
					});
				ret;
			}.flatten.unbubble.yield;

		});

		^inval;
	}
}

PdataKey : Pattern {
	var <>key,<>network;
	var <>repeats;
	var <>pSlot;

	*new { arg key, network, repeats=inf;
		^super.newCopyArgs(key, network, repeats)
	}

	storeArgs { ^[key, network, repeats] }

	embedInStream { arg inval;
		// key stream
		var keyStr = key.asStream;
		var keyVal;

		repeats.value.do({

			keyVal = keyStr.next(inval);
			keyVal = keyVal.asArray;

			inval = keyVal.collect{ |it|
				var ret,sn;
				sn = network.at(it);
				if ( sn.isNil,
					{ "node or slot not found".warn;
						ret = inval;
					},{
						if ( sn.isKindOf( SWDataNode ) ){
							ret = sn.data;
						} {
							if ( sn.isKindOf( SWDataSlot ) ){
								ret = sn.value;
							};
						}
					});
				ret;
			}.flatten.unbubble.yield;
		});
		^inval;
	}
}


