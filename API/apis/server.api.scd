
var sf = { arg name;
	Server.fromName(name)
};

(
	boot: { arg reply, name=\default;
		sf.value(name).waitForBoot(reply, 100, {
			Error("Server failed to boot").throw
		});
	},
	quit: { arg reply, name=\default;
		var s = sf.value(name);
		if(s.serverRunning, {
			Routine.run {
				s.quit;
				s.wait(\done);
				0.1.wait;
				reply.value;
			}
		}, reply);
	},
	sendMsg: { arg reply, name=\default ... msg;
		sf.value(name).sendMsg(*msg);
	},
	/*sendMsgCallback: { arg reply, name=\default ... msg;
		var m = MixedBundle.new;
		m.addFunction(reply);
		m.add(msg);
		m.send( sf.value(name) );
	},*/
	freeAll: { arg reply, name=\default;
		// this does all kinds of things
		// unless/until somebody adds something else
		// to that long list of things
		// ServerTree will run last
		var s, done;
		s = sf.value(name);
		done = {
			reply.value;
			ServerTree.remove(done, s);
		};
		ServerTree.add(done, s);
		s.freeAll;
	},

	isRunning: { arg reply, name=\default;
		reply.value( sf.value(name).serverRunning )
	},
	nextNodeID: { arg reply, name=\default;
		reply.value( sf.value(name).nextNodeID )
	}
);

