// this class keeps track of notifications that need to be passed on to certain clients
// for messages that are sent like this:
// [some client] -> host -> hive -> host -> [some client]
// requests include:

// - query/minibees       -> info/minibee    (N)       timeout?
// - query/configurations -> info/configuration (N)    timeout?

// - remove/minibee        -> removed/minibee          id
// - map/minibee/output    -> mapped/minibee/output    id
// - unmap/minibee/output  -> unmapped/minibee/output  id
// - map/minibee/custom    -> mapped/minibee/custom    id
// - unmap/minibee/custom  -> unmapped/minibee/custom  id

// - configure/minibee             -> configured/minibee  id 
// - minihive/configuration/create -> minihive/configuration/created id
// - minihive/configuration/delete -> minihive/configuration/deleted id
// - minihive/configuration/save   -> minihive/configuration/saved   name
// - minihive/configuration/load   -> minihive/configuration/loaded  name


SWAsyncNotifier{

	var <interessees;
	var <replyTags;

	*new{
		^super.new.init;
	}

	init{
		interessees = MultiLevelIdentityDictionary.new;
		replyTags = IdentityDictionary.new;
	}

	add{ |osctag,id,client,replyTag|
		osctag = osctag.asSymbol;
		if ( interessees.at( osctag, id.asSymbol ).isNil ){
			interessees.put( osctag, id.asSymbol, Set.new );
		};
		interessees.at( osctag, id.asSymbol ).add( client );
		replyTags.put( osctag, replyTag );
		//		interessees[osctag].postcs;
		//		replyTags.postcs;
	}

	removeClient{ |osctag, id, client|
		interessees.at( osctag, id.asSymbol ).remove( client );
	}

	successResult{ |osctag, id, message|
		osctag = osctag.asSymbol;
		interessees.at( osctag, id.asSymbol ).do{ |client|
			client.addr.sendMsg( *( [replyTags.at( osctag.asSymbol ), id ] ++ message) );
		};
		//		interessees[osctag].postcs;
		interessees.removeAt( osctag, id.asSymbol );
		//		interessees[osctag].postcs;
	}

	errorResult{ |osctag, id, message|
		osctag = osctag.asSymbol;
		interessees.at( osctag, id.asSymbol ).do{ |client|
			client.addr.sendMsg( *( ["/error", osctag ] ++ message) );
		};
		interessees.removeAt( osctag, id.asSymbol );
	}
	
}
