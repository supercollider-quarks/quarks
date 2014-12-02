CVEvent {
	classvar <>synthEvent, <>groupEvent, <>controlBusEvent, <>actionEvent, <>bufferEvent;
	

	*initClass {
	// These events create synths, groups, and buses with CVs linked to their controls
		Class.initClassTree(Event);
		CVEvent.bufferEvent = (
			msg: \wave, 				// \cheby, \wave, \signal
			size: 512,
			sine1Phase: { var amps, size, freqs; 
				amps = ~cv.value; 
				size = amps.size;
				freqs = (1..size);
				~buffer.sine3(freqs, amps, ~phases.value);
			},
			sine1: 	{ ~buffer.sine1(~cv.value); },
			cheby: 	{ ~buffer.cheby(~cv.value);  },
			wave: 	{ ~buffer.setn(0, 
				~cv.value.clump(2).collect{ |a| [a[0], a[1] - a[0]]}.flatten); 
			},
			signal: 	{ ~buffer.setn(0, ~cv.value);  },

			play: {
				var ev;
				~finish.value;
				ev = currentEnvironment;
				~server = ~server ?? {Server.default};
				~buffer = Buffer.alloc(~server, ~size, 1);
				~bufnum = ~buffer.bufnum;
				~controller = ~cv.action_ ({ 
					ev.use {
						ev[~msg].value;  
						if (~display.notNil) { ev.get };
					}
				});
				defer( { ev.use { ev[~msg].value; } }, 0.3); 
				if (~display.notNil) { ~get.value(ev,0.2) };
			},
				
			stop: {
				~controller.remove;
				~controller = nil;
				~buffer.free;
			},
			
			get: { | ev, del = 0 |
					Task({ 
						del.wait;
						ev[\server].sync;
						ev[\buffer].getn(0, ev[\display].value.size, { | vals|
							if (ev[\msg] != \signal) {
								vals = vals.clump(2).collect { |a| 
									[ a[0], a[0] + a[1] ] 
								}.flatten
							};
							ev[\display].value_(vals)
						});
					}).play;
				}
		);
		
		CVEvent.synthEvent = (
			lag: 0,
			play: #{ 
				var server, latency, group, addAction;
				var instrumentName, synthLib, desc, msgFunc;
				var msgs, cvs;
				var bndl, ids;
				~finish.value;
				~server = server = ~server ?? { Server.default };
				group = ~group;
				addAction = ~addAction;
				synthLib = ~synthLib ?? { SynthDescLib.global };
				instrumentName = ~instrument.asSymbol;
				desc = synthLib.synthDescs[instrumentName];
				if (desc.notNil) { 
					msgFunc = desc.msgFunc;
					~hasGate = desc.hasGate;
				}{
					msgFunc = ~defaultMsgFunc;
				};
			
				msgs = msgFunc.valueEnvir.flop;
				cvs = ~cvs.value ? [];
				ids = Event.checkIDs(~id);
				if (ids.isNil ) { ids = msgs.collect { server.nextNodeID } };
				bndl = ids.collect { |id, i|
					[\s_new, instrumentName, id, addAction, group]
					 ++ msgs[i]
					 ++ cvs.connectToNode( server, id);
				};
			
				if ((addAction == 0) || (addAction == 3)) {
					bndl = bndl.reverse;
				};
				bndl = bndl.asOSCArgBundle;
				if (~lag !=0) {
					server.sendBundle(server.latency ? 0 + ~lag, *bndl);
				} {
					server.sendBundle(server.latency, *bndl);
				
				};
				~id = ids;
				~isPlaying = true;	 
				~isRunning = true;	 
				NodeWatcher.register(currentEnvironment);
			},
			defaultMsgFunc: #{|freq = 440, amp = 0.1, pan = 0, out = 0| 
				[\freq, freq, \amp, amp, \pan, pan, \out, out] }

		).putAll(Event.partialEvents.nodeEvent);
		
		CVEvent.groupEvent = ( 
			lag: 0,
			play: #{
				var server, group, addAction, ids, bundle, cvs;
				~finish.value;
				group = ~group;
				addAction = ~addAction;
				~server = server= ~server ?? {Server.default};
				ids = Event.checkIDs(~id);
				if (ids.isNil) { ids = ~id = server.nextNodeID };
				cvs = ~cvs.value ? [];
				if ((addAction == 0) || (addAction == 3) ) {
					ids = ids.asArray.reverse;
				};
				bundle = ids.collect {|id, i|
					cvs.connectToNode(server, id);
					[\g_new, id, addAction, group];
 
				};
				server.sendBundle(server.latency + ~lag, *bundle);
				~isPlaying = true;
				~isRunning = true;
				NodeWatcher.register(currentEnvironment);
			}
		).putAll(Event.partialEvents.nodeEvent)[\hasGate] = false;
		
		CVEvent.controlBusEvent = (
			play: #{ var cvs, index, server, bundle, size;
				~server = server = ~server ?? {Server.default};
				~finish.value;
				cvs = ~cvs.asArray;
				index = ~index;
				if (index.isNil) {
					~bus = Bus.control(server,cvs.size);
					index =  ~index =  ~bus.index;
				} {
					~bus = Bus(\control, index,cvs.size, server)
				};
				~bus.setControls(cvs, cvs.size);

			},
			
			stop: #{
				~bus.free;
			}
		);
		CVEvent.actionEvent = ( asEventStreamPlayer: #{|ev| ev } );
		
//		Event.parentEvents[\synthEvent] = CVEvent.synthEvent;
//		Event.parentEvents[\groupEvent] = CVEvent.groupEvent;			 
	}
}