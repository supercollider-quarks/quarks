
var cb = { arg reply, msg, result;
		var m = MixedBundle.new;
		m.addFunction({ reply.value(result) });
		m.add(msg);
		m.send( Server.default );
	};

(
	new: { arg reply, defName ... args;
		var y = Synth.basicNew(defName);
		var m = y.newMsg(nil, args);
		cb.value(reply, m, y.nodeID);
	},
	head: { arg reply, target, defName ... args;
		var y = Synth.basicNew(defName);
		var m = y.addToHeadMsg(target, args);
		cb.value(reply, m, y.nodeID);
	},
	tail: { arg reply, target, defName ... args;
		var y = Synth.basicNew(defName);
		var m = y.addToTailMsg(target, args);
		cb.value(reply, m, y.nodeID);
	},
	grain: { arg reply, target, defName, addAction=\addToHead ... args;
		Synth.grain( defName, args, target, addAction );
		// no response
	},

	free: { arg reply, nodeID;
		var y = Node.basicNew(nil, nodeID);
		y.onFree(reply);
		y.free
	},
	release: { arg reply, nodeID, releaseTime;
		var y = Node.basicNew(nil, nodeID);
		y.onFree(reply);
		y.release(releaseTime)
	},

	get: { arg reply, nodeID, argIndex;
		var s = Server.default;
		var rsp = OSCpathResponder(s.addr,
			['/n_set', nodeID, argIndex],
			{ arg time, r, msg;
				if(rsp === r,{
					reply.value(msg.at(3));
					r.remove;
				})
			}).add;
		s.sendMsg("/s_get", nodeID, argIndex);
	},
	set: { arg reply, nodeID, argIndex, value;
		Server.default.sendMsg(
			"/n_set",
			nodeID,
			*([argIndex, value].asOSCArgArray));

		// var y = Node.basicNew(nil, nodeID);
		// y.set(argIndex, value);
		// no response
	},

	// isPlaying
);

