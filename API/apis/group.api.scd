
var cb = { arg reply, msg, result;
	var m = MixedBundle.new;
	m.addFunction({ reply.value(result) });
	m.add(msg);
	m.send( Server.default );
};

(
	new: { arg reply;
		var y = Group.basicNew();
		var m = y.newMsg();
		cb.value(reply, m, y.nodeID);
	},
	head: { arg reply, target;
		var y = Group.basicNew;
		var m = y.addToHeadMsg(target);
		cb.value(reply, m, y.nodeID);
	},
	tail: { arg reply, target;
		var y = Group.basicNew;
		var m = y.addToTailMsg(target);
		cb.value(reply, m, y.nodeID);
	},
	free: { arg reply, nodeID;
		var y = Node.basicNew(nil, nodeID);
		y.onFree(reply);
		y.free
	}
);

