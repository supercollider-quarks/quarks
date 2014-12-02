
SimpleRepublic {
	
	var <broadcastAddr, <republicName;
	var <addrs, <nickname, <nameList, <myNameIndex = -1, <joined = false;
	var <graceCount = 16;
	var <>verbose = false, <>private = false; // use this later to delegate traffic
	var <skip, <resp, <broadcastWasOn, <presence;
	
	var <groups;
	
	classvar <>default;
	
	*new { |broadcastAddr, republicName = '/republic'|
		^super.newCopyArgs(broadcastAddr, republicName).init
	}
	
	makeDefault { default = this }
	
	init {
		broadcastAddr = broadcastAddr ?? {
			NetAddrMP("255.255.255.255", 57120 + (0..7))
		};
		// broadcastAddr.checkServesLangPort;
		this.switchBroadcast(true);
		
		addrs = ();
		nameList = List.new;
		presence = ();
		
		groups = ();

		this.makeResponder; 
		this.makeSkip; 
	}
	
	addGroup { |name, names| groups.put(name, names) }
	removeGroup { |name, names| groups.put(name, names) }
	replaceGroupNames { |names| 
		^names.collect { |name|
			if (groups[name].notNil) { groups[name] } { name }
		}.flat;
	}
	
	canJoin {  |name|
		name = name.asSymbol;

		if (this.hasJoined(name)) { 
			if (name == nickname) { 
				inform("Republic - you have joined already as %.".format(name));
			} { 
				warn("Republic - could not join: name % is in use.".format(name));
			};
			^false;
		};
		^true
	}
	
	joinStart { 
		this.statusFunc;
		skip.play;
		fork( { 0.5.wait; this.checkJoined }, AppClock)
	}
		
		// clientID for subclass...
	join { |name, argClientID|
		name = name.asSymbol;
		if (this.canJoin(name)) { 
	
				// leave quickly when rejoining under different name
			if (nickname.notNil) { this.leave; };
			nickname = name;
			
			this.joinStart;
		}
	}
	
	leave { |free = false| 
		this.removeParticipant(nickname);
		nickname = nil;
		joined = false;
		if (free) { this.free }
	}
	
	free { 
		skip.stop; 
		SkipJack.all.remove(skip);
		resp !? { resp.remove };
		addrs.do(_.disconnect);
		addrs = ();
		this.switchBroadcast(false);
		OSCresponder(nil, '/hist').remove;
	}
	
	send { |name ... args|
		this.prSendWithDict(addrs, name, [args])
	}
	
	
	// testing
	
	nameIsFree { |name| ^this.hasJoined(name).not }

	hasJoined { |name| ^addrs.at(name).notNil }
		
	checkJoined { 
		joined = addrs.keys.includes(nickname); 
		if (joined.not) { 
			warn("Nickname % has not joined Republic yet.\n"
			"BroadcastAddr may be wrong!"
				.format(nickname));
		}
	}
	
	
	// compatibility with Collective
	
	myIP {
		^this.myAddr.hostname.asIPString
	}
	
	myAddr {
		^addrs[nickname]
	}
	
	channel { ^("/" ++ republicName).asSymbol }
	
	clientID { ^0 }
	
	// private implementation

	
	assemble {
		presence.keys.do { |key|
			var newVal = presence[key] - 1;
			
			if(newVal < 0) {
				this.removeParticipant(key);
				presence.removeAt(key);
				(" --- % has just left the building. --->".format(key)).repostln;
			} { 
				presence.put(key, newVal)
			}
		};
		if(verbose) { presence.repostln };
	}
	
	addParticipant { |key, addr|
		addrs.put(key, addr);
		if(nameList.includes(key).not) {
			nameList.add(key);
			myNameIndex = nameList.indexOf(nickname);
		};
	}
	
	removeParticipant { |key|
		addrs.removeAt(key);
		nameList.remove(key);
		myNameIndex = nameList.indexOf(nickname);
		presence.removeAt(key);
	}
	
	switchBroadcast { |flag|
		if(flag) {
			broadcastWasOn = NetAddr.broadcastFlag;
			NetAddr.broadcastFlag = true;
		} {
			NetAddr.broadcastFlag = broadcastWasOn;
		};
		"\n\nRepublic: switched global NetAddr broadcast flag to %.\n".format(flag).repostln;
	}
		
	makeResponder {
		resp = OSCresponderNode(nil, republicName, 
			{ |t, r, msg, replyAddr|
				var otherNick, otherID, addr, extraData;
				var tempo, beats, serverConfig;
				
				otherNick = msg[1];
				otherID = msg[2];
				extraData = msg[3..];
							
				if(this.hasJoined(otherNick).not) { 
					addr = NetAddr(replyAddr.addr.asIPString, replyAddr.port);
					this.addParticipant(otherNick, addr, otherID, extraData);
					(" ---> % has joined the Republic. ---".format(otherNick)).repostln;
				};
				presence.put(otherNick, graceCount);
		}).add;
	}
		
	statusFunc {
		var msg;
		if (nickname.notNil) {
			msg = [republicName] ++ this.statusData;
			broadcastAddr.do(_.sendBundle(nil, msg)) 
		};
		this.assemble; 
	}
	
	statusData {
		// subclass compatibility
		// the two 0's are for backwards compatibility (used to be beats and phase) 
		^[nickname, this.clientID, 0, 0] 
	}
	
	makeSkip {
		this.statusFunc; // do once immediately
		skip.stop;
		skip = SkipJack({ this.statusFunc }, 1.0, name: republicName);
	}

	
	// GUI
	
	gui { |numItems = 12, parent, bounds|
	//	^EZRepublicGui(parent, bounds, this);
		^RepublicGui(this, numItems, parent, bounds);
	}
	
	shareHistory { |useShout = true, winWhere| 
		if (useShout) { 
			OSCresponder(nil, '/hist', {|t,r,msg| 
				var who = msg[1];
				var codeStr = msg[2].asString; 
				
				if (private.not) { History.enter(codeStr, who); };
				
				if (codeStr.beginsWith(Shout.tag)) { 
					defer { 
						Shout((codeStr.drop(Shout.tag.size).reject(_ == $\n) + ("/" ++ who)).repostcs) 
					}
				}; 
			}).add; 	
		} {
			OSCresponder(nil, '/hist', {|t,r,msg| 
				History.enter(msg[2].asString, msg[1]) 
			}).add; 
		} { 
		
		};
		
		History.forwardFunc = { |code|
			if(joined) { this.send(\all, '/hist', nickname, code) }
		};	
	
		History.start;
		History.makeWin(winWhere);
		History.localOff;
			
	}
	
	*dumpOSC { |flag = true|
		thisProcess.recvOSCfunc = if(flag) { { |time, replyAddr, msg| 
			if(#['status.reply', '/status.reply'].includes(msg[0]).not) {
				"At time %s received message % from %\n".repostf( time, msg, replyAddr )
			}  // post all incoming traffic except the server status messages
			} } { nil }
	}
	
	
	// if name == \all, send to each item in the dict.
	// otherwise send to each of the given names
	
	prSendWithDict { |dict, names, messages, latency|
	//	var isServer = dict.choose.isKindOf(Server); 
	//	if (isServer) { "prSendWithDict, server - incoming names: ".repost; names.repostcs; };
		names = names ? nickname; // send to myself if none is given 
		
	//	if (isServer) {"replaced names: ".repost; names.repostcs;};
		
		
		if(verbose) { "sent messages to: %.\nmessages: %\nlatency: %\n"
				.repostf(names, messages, latency)};
		if(names == \all) {
	//	if (isServer) {"sending to all.".repostln;};
			
	//		dict.repostcs;
			dict.do { |recv| recv.sendBundle(latency, *messages) }
		} {
	//	if (isServer) { "names.asArray: ".repost; };
			
			names = this.replaceGroupNames(names.asArray);
			
			names.do { |name|
				var recv;
				if(name.isInteger) {
					name = nameList.wrapAt(name + myNameIndex);
				};
				recv = dict.at(name);
				if(recv.isNil) {
					"% is currently absent.\n".repostf(name)
				} {
					recv.sendBundle(latency, *messages)
				};
			}
		}
	}
	
	// deprecated
	
	*fixedLangPort {
		^this.deprecated(thisMethod)
	}
	
	*fixedLangPort_ {
		^this.deprecated(thisMethod)
	}
	
	*getBroadcastIPs {
		"Please use instead: NetAddr.getBroadcastIPs".repostln;
		^this.deprecated(thisMethod, NetAddr.findRespondingMethodFor(\getBroadcastIPs))
	}
	
}

