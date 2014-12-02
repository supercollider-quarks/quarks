
DebugPbind : Pbind {
	embedInStream { arg inevent;
		var event;
		var sawNil = false;		
		var streampairs = patternpairs.copy;
		var endval = streampairs.size - 1;

		streampairs.pairsDo({ |key, pat, i|
			streampairs[i+1] = pat /*.trace(prefix: key.asString ++ ": ")*/ .asStream;
		});

		loop {
			if (inevent.isNil) { ^nil.yield };
			event = inevent.copy;
			event.debug("\nSource event");
			forBy (0, endval, 2) { arg i;
				var name = streampairs[i].debug("stream ID");
				var stream = streampairs[i+1];		
				var streamout = (event.debug("Event going in");
					stream.next(event).debug("streamout"));
				if (streamout.isNil) {
					"% stream was nil. Exiting Pbind.\n".postf(name)
					^inevent
				};

				if (name.isSequenceableCollection) {
					if (name.size > streamout.size) {  
						("the pattern is not providing enough values to assign to the key set:" + name).warn;
						^inevent 
					};
					name.do { arg key, i;
						event.put(key, streamout[i]);
					};
				}{
					event.put(name, streamout);
				};
			};
			event.debug("Result event");
			inevent = event.yield;
		}		
	}
}



// modified Pbind
// ([\key1, \key2], 1) results in ~key1 == 1, ~key2 == 1

PbindMultiChan : Pbind {
	embedInStream { arg inevent;
		var event;
		var sawNil = false;
		var streampairs, endval;
		
		streampairs = patternpairs.copy;
		endval = streampairs.size - 1;
		forBy (1, endval, 2) { arg i;
			streampairs.put(i, streampairs[i].asStream);
		};

		loop {
			if (inevent.isNil) { ^nil.yield };
			event = inevent.copy;
			forBy (0, streampairs.size-1, 2) { arg i;
				var name, stream, streamout;
				name = streampairs[i];
				stream = streampairs[i+1];		
				streamout = stream.next(event);
				if (streamout.isNil) { ^inevent };
				if (name.isSequenceableCollection) {
					[name, streamout].flop.do { arg pair;
						event.put(*pair);
					}
				}{
					event.put(name, streamout);
				};
			}; // end forBy
			inevent = event.yield;
		}
	}
}

// modified Pbind
// ([\key1, \key2], 1) results in ~key1 == 1, ~key2 unchanged
// ([\key1, \key2], [1, nil]) same behavior
// defaults may be defined in the event or in pairs PRIOR to the array assignment

PbindArrayDefault : Pbind {
	embedInStream { |inevent|
		var streampairs, endval, event;
		
		streampairs = patternpairs.copy;
		endval = streampairs.size - 1;
		forBy (1, endval, 2) { arg i;
			streampairs.put(i, streampairs[i].asStream);
		};

		loop {
			if (inevent.isNil) { ^inevent };
			event = inevent.copy;
			forBy (0, streampairs.size-1, 2) { arg i;
				var name, stream, streamout;
				name = streampairs[i];
				stream = streampairs[i+1];		
				streamout = stream.next(event);
				if (streamout.isNil) { ^inevent };
				if (name.isSequenceableCollection) {
					name.do { arg n, i;
						event.put(n, streamout.tryPerform(\at, i) ?? { event[n] })
					};
				}{
					event.put(name, streamout);
				};
			}; // end forBy
			inevent = event.yield;
		}; // end loop
	}
}

