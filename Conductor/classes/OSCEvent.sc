OSCEvent {
	var <>addr, <>cmdName, <>thread, <>time, <>msg, <>respAddr, dispatcher;

	*new { arg addr, cmdName;
		^super.newCopyArgs(addr, cmdName.asSymbol);
		
	}
	add {
		var found;
		dispatcher = OSCMultiResponder(addr, cmdName);
		found = OSCresponder.all.findMatch(dispatcher);
		if(found.isNil) { 
			dispatcher.nodes = [this]; 
			dispatcher.add; 
		} {
			if (found.class === OSCresponder, {
				found.remove;
				dispatcher.nodes = [found, this];
				dispatcher.add;
			},{
				dispatcher = found;
				found.nodes = found.nodes.add(this)
			});
		}
	}
	
	remove { 
		dispatcher.nodes.remove(this);
		if(dispatcher.isEmpty, { dispatcher.remove });
		dispatcher = nil;
	}
	
	value { | argTime, argMsg, argAddr |
		time = argTime; msg = argMsg; respAddr = argAddr;
		if (thread.next(this).isNil) { this.remove};
	}
	
	wait { 
		thread = thisThread;
		if (dispatcher.isNil) { this.add };
		nil.yield
	}	
	
	action {}							// needed to patch into current OSCresponder set up

}

/*
r = Routine({ var ev;
	ev = OSCEvent(nil,'/n_go');
	a = ev;
	loop { ev.wait; ev.msg.postln; ev.respAddr.postln; }
});
r.reset.next
( ).play
OSCresponder.all
r.stop

().group.play
*/