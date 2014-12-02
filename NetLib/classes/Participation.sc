// collective keeps names / addresses from everyone who joined
// within the local network
// the collector collects them



Collective {

	var <myName, <groupName, <everybody;
	var <myIP, <broadcast;
	var <addresses; 
	
	var <broadcastIP, <>collector;
	var <>newAddrAction, <>sortFunc; 
		// if sortFunc is given, addresses are sorted when new addr was added.
	var <>verbose = false;
	 
	classvar >default, <>defaultName;
	classvar <allActive;
	
	*initClass {
		allActive = IdentityDictionary.new;
		ShutDown.add({ allActive.do(_.quit) });
	}
	
	// maybe should use def access scheme
	*new { arg myName, groupName=\all, participants;
		^super.newCopyArgs(myName ? defaultName ?? { this.makeName }, groupName)
						.init(participants)
	}
	
	*default { ^default ?? {default = this.new.autoCollect } }
	
	*makeName { ^format("%_%", String.rand(8, 1), NetAddr.myIP.split($.).last).asSymbol }
	channel { ^("/" ++ groupName).asSymbol }
	myAddr { ^everybody[myName] }
	
	init { arg participants;
		addresses = Array.new;
		everybody = IdentityDictionary.new;
		myIP = NetAddr.myIP;
		broadcastIP = NetAddr.broadcastIP;
		if(broadcastIP.isNil) { 
			"no broadcast available. using loopback instead for now.".warn;
			broadcastIP = "127.0.0.1"
		} {
			if(NetAddr.broadcastFlag.not) {
				"setting NetAddr broadcast flag to true ... ".postln;
				NetAddr.broadcastFlag = true;
			}
		};
		broadcast = NetAddr(broadcastIP, 57120);
		this.addAll(participants);
	}
	
	
	start {
		allActive.put(myName, this);
		this.addMe;
		collector !? {
			collector.start;
		}
	}
	stop {
		allActive.removeAt(myName);
		collector !? {
			collector.stop;
		}
	}
	quit { arg onComplete;
		this.broadcastDisconnect {
			("quitted. " ++ myName).postln;	
			everybody.clear; // todo: check for sideeffects of this!
			addresses = [];
			this.stop;
			onComplete.value;
		}
	}
	
	broadcastDisconnect { arg onComplete;
		if(collector.notNil, {
			collector.quit(4, onComplete)
		}, onComplete)
	}
	
	// only works for broadcasted communication so far!
	myName_ { arg newName;
			this.stop;
			myName = newName;
			this.start;
	}
	
	// adding and removing participants
	
	autoCollect { arg flag = true;
		if(flag) {
			this.addMe;
			if(collector.isNil) {
				"created new collector".postln;
				collector = Collector(this);
			};
			collector.start;
		} {
			collector.stop;
			collector = nil;
		}
	}
	
	autoCollectIsActive {
		^collector.notNil and: { collector.isListening }
	}
	
	fixConnections { arg repeat=5;
		this.autoCollect(true);
		fork {
			repeat.do {
				collector.sendMyNameToEach;
				2.wait
			}
		}
	}
	
	addAll { arg pairs;
		pairs.pairsDo { |nickname, hostname|
			this.addSomeone(nickname, hostname)
		};
	}
	addMe { this.addSomeone(myName, myIP) }
	
	addSomeone { arg nickname, hostname;  // no nickname changes possible currently.
			var newAddr, addr;
			
			hostname = hostname.asSymbol;
			if(nickname.isNil) {
					nickname = Symbol.rand;
					("A new, random name (%) has been chosen for %.").postf(nickname, hostname);
			} {
				addr = everybody[nickname];
				nickname = nickname.asSymbol;
			};
			if(addr.isNil) {
				newAddr = NamedNetAddr(nickname, hostname.asString, 57120);
				addresses = addresses.add(newAddr);
				postf("% received new address: % has been added as %\n", 
								myName, hostname, nickname.asString.quote);
					everybody.put(nickname, newAddr);
				if(sortFunc.notNil) {
					addresses.sort { |a, b| sortFunc.value(a.name, b.name) }
				};
				newAddrAction.value(addr, nickname);
			};
			
	}
	
	removeSomeone { arg nickname, hostname;
			var addr;
			nickname = nickname.asSymbol;
			hostname = hostname.asSymbol;
			addr = everybody[nickname];
			
			addr !? {
				if(hostname.notNil and: {hostname != addr.hostname.asSymbol}) { 
					"failed to remove participant % : % with hostname %\n"
						.postf(nickname, addr.hostname, hostname);
					^this
				};
				everybody.removeAt(nickname);
				addresses.remove(addr);
				addr.disconnect;
				"removed participant % : %\n".postf(nickname, hostname);
			};
	}
	
	// sending messages 
	
	sendMsg { arg ... args; // sends to all.
		broadcast.listSendNamedMsg(args)
	}

	sendToAll { arg ... args;
		broadcast.listSendNamedMsg(args)
	}
	sendToEach { arg ... args;
		addresses.do(_.listSendNamedMsg(args))
	}
	sendToIndex { arg i ... args;
		var addr = addresses[i];
		if(addr.isNil) { "no address at that index".warn; ^this };
		addr.listSendNamedMsg(args);
	}
	sendToName { arg name ... args;
		var addr = everybody[name];
		if(addr.isNil) { "no address for that name: %".format(name).warn; ^this };
		addr.listSendNamedMsg(args)
	}
	
	makeResponder { arg addr, cmd, func;
		^this.myAddr.makeResponder(addr, cmd, func)
	}
	
	avoidTheWorst { arg obj;
		var str = obj.asString;
		^str.find("unixCmd").isNil 
			and: { str.find("File").isNil } 
			and: { str.find("Pipe").isNil }
			and: { str.find("Public").isNil }
	}
	
	// lookup
	
	findKeyForAddr { arg addr;
		everybody.keysValuesDo {|key, val|
			if(val == addr) {^key }
		};
		^nil
	}
	
	
	// posting
	
	postEverybody {
		everybody.keysValuesDo { |key, val|
			postf("%\t\t%\n", key, val)
		}
	}
	
	postPairs {
		addresses.collect { |addr|
			[addr.name, addr.hostname]
		}.flatten(1).postcs;
	}
	
	storeArgs { ^[myName, groupName] }
	
	printOn { arg stream;
		stream << this.class.name;
		this.storeParamsOn(stream)
	}
	
	// gui
	
	makeWin {
		^CollectorGui(this)
	}
}


// takes care for a number of responders

Participation {
	var <collective, <responders, <isListening=false;
	var >channel;
	
	*new { arg collective; 
		^super.new.prSetCollective(collective ?? {Collective.default }).init
	}
	
	init {}
	
	channel { ^channel ? this.defaultChannel }
	defaultChannel { ^this.subclassResponsibility(thisMethod) }
	
	addResponder { |cmd, func|
		var r = collective.makeResponder(nil, cmd, func);
		responders = responders.add(r);
		if(isListening) { r.add };
	}
	
	removeResponder { |cmd|
		var resp = responders.detect { |r| r.cmdName == cmd };
		responders.remove(resp);
		^resp.remove;
	}
	
	start {
		if(isListening.not) {
			responders.do(_.add);
			isListening = true;
		}
	}
	stop {
		responders.do(_.remove);
		isListening = false;
	}
	clear {
		this.stop;
		responders = nil;
	}
	
	// ping
	
	addPing {
		this.addResponder(this.channel ++ "_test", { arg r, t, msg;
			var nick;
			nick = msg[1];
			collective.sendToName(nick, this.channel ++ "_test_reply") // maybe add name?
		});
	}
	// should be replaced by a notification protocol. for now this is easy.
	pingFirst { arg timeout, func, failedFunc;
		var resp, received = false;
		fork {
			resp = collective.makeResponder(nil, this.channel ++ "_test_reply", {
					resp.remove;
					received = true;
			}).add;
			
			timeout.wait;
			if(received, func, failedFunc)
		}
	}	
	
	// private
	
	prSetCollective { arg coll;
		collective = coll;
	}
}

// collect adresses automatically
// todo: timeout

Collector : Participation {
	var <task, <>updatePeriod, <>eternal=true;
	var shutdownFunc;
	
	init {
		updatePeriod = rrand(3.0, 3.5);
		this.addResponder(\collect, { |r, t, msg|
				var flag = msg[3];
				if(flag == -1) {
					collective.removeSomeone(msg[1], msg[2]);
					if(collective.verbose) { "collective: %, trying to remove: % (ip: %)"
							.format(collective.myName, msg[1], msg[2])
					};
				} {
					collective.addSomeone(msg[1], msg[2]);
					if(collective.verbose) { "collective: %, trying to add: % (ip: %)"
							.format(collective.myName, msg[1], msg[2])
					};
				};
				
		});
		this.addResponder(\testPing, { arg r, t, msg;
			var name = msg[1];
			var time = msg[2];
			name !? {
				"I (%) was pinged by %\n".postf(collective.myName, name);
				this.replyTo(name, time);
			}
		});
		this.addResponder(\recvPing, { arg r, t, msg;
			var name = msg[1];
			var time = msg[2];
			time !? {time = (Main.elapsedTime - time).round(0.001) };
			name !? {
				"% responded after % sec.\n".postf(name, time ? "??");
				//recvAction.value(name, time);
			}
		});
				
		task = Task {
				0.1.rand.wait;
				"Collector for % started\n".postf(collective.myName);
				loop {
					this.sendMyNameToAll;
					updatePeriod.value.wait;
				}
		};
	}
	
	sendMyNameToAll { arg flag; // -1 remove me.
		collective.sendToAll(\collect, collective.myName, collective.myIP, flag)
	}
	sendMyNameToEach { arg flag; // -1 remove me.
		collective.sendToEach(\collect, collective.myName, collective.myIP, flag)
	}
	sendMyNameTo { arg name, flag; // -1 remove me.
		collective.sendToName(name, \collect, collective.myName, collective.myIP, flag)
	}
	replyTo { arg name, time;
		collective.sendToName(name, \recvPing, collective.myName, time)
	}
	ping { arg name;
		collective.sendToName(name, \testPing, collective.myName, Main.elapsedTime)
	}
	pingAll {
		collective.sendToEach(\testPing, collective.myName, Main.elapsedTime)
	}
	

	//  stop == quit?
	quit { arg repeats=4, onComplete;
		var procedure = SkipJack(
			Routine {
				this.stop;
				repeats.do { 
					this.sendMyNameToAll(-1);
					1.yield; // to return something.
				};
				onComplete.value;
				procedure.stop;
			},
			0.5);
		procedure.start;
	}
	start {
		task.play;
		super.start;
		CmdPeriod.add(this);
		shutdownFunc = { this.quit };
		ShutDown.add(shutdownFunc);
		
	}
	stop {
		task.stop;
		super.stop;
		CmdPeriod.remove(this);
		ShutDown.remove(shutdownFunc);
	}
	cmdPeriod { if(eternal) {this.start } }
	
	// to improve.
	makeWindow { arg w;
		var label, listview;
		if (w.notNil, { ^w.front });
		
		if(w.isNil) {
			label = collective.myName.asString + "Collective";
			w = SCWindow(label, 
						Rect(10, Server.named.values.size * 120 + 10, 306, 292));
			w.view.decorator = FlowLayout(w.view.bounds);
		} { label = w.name };
		listview = SCListView(w, w.view.bounds.insetBy(10, 10))
			.items_(
				collective.everybody.keys.as(Array)
			);
		listview.enterKeyAction = {|v| 
			this.ping(v.items[v.value]);
		};
	
		w.front;
	}
	
}

/*
// should move Client, LocalClient in here!
ResponderDef : Participation {
	defaultChannel { ^\collectiveResp }
	init {
		this.addResponder(this.channel, { arg r, t, msg; 
			this.receive(msg[1..])
		})
	}
	receive { arg args;
		
	}
}
*/


Chat : Participation {
	var <>recvMsgAction;
	
	defaultChannel { ^\chat }

	init {
		this.addResponder(this.channel, { arg r, t, msg; 
			this.receive(msg[1], msg[2])
		})
	}
	talk { arg str;
		collective.sendToEach(this.channel, collective.myName, str);
	}
	receive { arg who, string;
		recvMsgAction.value(who, string);
	}
}

ChatWindow : Chat {
	var <readWin, <writeWin;
	
	start {
		super.start;
		readWin = Document("chat" + collective.myName).background_(Color.rand)
			.bounds_(Rect(30, 10, 400, 200));
		writeWin = Document("chat-write").background_(Color.rand)
			.bounds_(Rect(30, 210, 400, 50));
		
		readWin.onClose_({ this.stop });
		writeWin.keyDownAction_({arg doc, char;
				var string;
				 if(char === Char.tab) {
	 					string = doc.currentLine;
	 					this.talk(string.copy); 
	 					AppClock.sched(0.1, { writeWin.string = "" }); 	 			} 
		});
		
	}
	receive { arg who, string;
		readWin !? {
			{ readWin.string =  
				format("%%: %", 
							readWin.string ++ Char.nl, 
							who, 
							string).clean
			}.defer
		};
		super.receive(who, string);
	}
	
	close {
		readWin.close;
		writeWin.close;
	}
}

NetLog : Chat {
	var <>window, <>cmdLineFunc;
	
	defaultChannel { ^\netlog }
	
	start {
		super.start;
		window =  Document("log" + collective.myName).background_(Color.rand);
		cmdLineFunc = { |str| this.talk(str) };
		thisProcess.interpreter.codeDump = thisProcess.interpreter.codeDump.addFunc(cmdLineFunc);
	}
	receive { arg string, who;
		this.addStringToWindow(this.wrapString(string, who));
		super.receive;
	}

	wrapString { arg str, nickname;
		^"\n// % ______ % ________________________________________________\n\n%\n\n"
			.format(nickname, Date.getDate.hourStamp, str)
	}
	addStringToWindow { arg str;
		window !? { {
			window.selectRange(window.text.size-1, 0); // deselect user
			window.string = window.string ++ str;
		}.defer }
	}
	
	stop {
		thisProcess.interpreter.codeDump.removeFunc(cmdLineFunc);
		super.stop;
	}
	
}

Cadavre : Participation {
	var <docs;
	var <backups;
	var <>myColor;
	
	defaultChannel { ^\cadavre }

	init {
		this.addResponder(this.channel, { arg r, t, msg;
			var doc, title;
			msg.postcs;
			defer {
				title = msg[2].asString;
				doc = this.openDoc(msg[1].asString, title);
			};
		});
		this.addPing;
		myColor = Color.rand(0.9, 1.0);
	}
	
	backup { arg str, title;
		backups = backups.add("\n-------\n\n// " ++ title ++ "\n\n" ++ str);
	}
	
	openDoc { arg str, title, inBackground=true;
		var currentDoc = Document.current;
		var doc;
		title = title ?? { String.rand(6) };
		str = str ? "";
		str = str.clean;
		// avoid the worst..
		str = str.replace("unixCmd", "u**xC**d").replace("File", "F***").replace("Pipe", "P*pe");
		doc = Document(title, (str ? "").clean);
		doc.background = Color.fromArray(title.keep(3)
			.collectAs(_.ascii, Array).linlin(64, 127, 0.6, 1.0)
		);
		doc.onClose_({
				if(collective.addresses.size > 1) {
					docs.remove(doc);
					this.backup(doc.text, doc.title);
					this.sendToNext(doc.text, doc.title);
				} {
					"no other participant to send to.".postln;
					if(isListening) { 
						this.openDoc(doc.text, doc.title) 
					};
				}
			});
		docs = docs.add(doc);
		if(inBackground) { currentDoc.front };
		^doc
	}
	
	sendToNext { arg str, title="";
		var index, delay=1.0, resp, received=false;
		("send to next: title: %\n").postf(title);
		if(isListening) {
			// my addr is assumed the first here.
			index = (collective.addresses.size - 1).rand + 1;
			if(collective.verbose) { 
				"% (cadavre) sending doc to next. index: %\n".postf(collective.myName, index)
			};
			collective.sendToIndex(index, this.channel ++ "_test", collective.myName);
			// maybe better with reciept
			this.pingFirst(1.0, 
				{ collective.sendToIndex(index, this.channel, str, title) }, 
				{ this.sendToNext(str, title) } // try again.
			)
		}
	}
	
}


// todo: maybe bad standard that you have to send your name (collective.myName)

ClockSync : Participation {
	var <>latency=0.0;
	
	var <>setClockAction;
	var <>waitingAction;
	var <>recvPingAction;
	var latencyAverager;
	
	ping { arg name;				
		collective.sendToName(name, "/collPing", collective.myName, Main.elapsedTime);
	}
	
	sync { arg name, quant=4.0; // quant can be [quant, offset]
		collective.sendToName(name, "/syncOurClocks", collective.myName, *quant)
	}
	
	init {
		
		latencyAverager = Routine { arg inval;
			var lastTime = Main.elapsedTime, all;
			var threshhold = 2.0; // max time.
			all = [];
			loop {
				if(Main.elapsedTime - lastTime > threshhold) { all = [] };
				all = all.add(inval);
				inval = all.mean.yield;
				lastTime = Main.elapsedTime;
				
			}
		
		};

		this.addResponder('/collPing', {  arg r, t, msg;
			collective.sendToName(msg[1], '/recvCollPing', msg[2]);		});
		
		this.addResponder('/recvCollPing', {  arg r, t, msg;
			var time = msg[1], dt;
			dt = (Main.elapsedTime - time).max(0) / 2;
			latency = latencyAverager.value(dt);
			postf("approx. network latency: %  sec\naverage latency: %\n", 
				dt.round(0.0001), latency.round(0.0001));
			
			recvPingAction.value(dt, latency);
		});
		
		
		this.addResponder('/syncOurClocks', { arg r, t, msg;
			var tempo, beats, timeToNextBeat;
			var remoteQuant = msg[2..];
			var tc = TempoClock.default;
			
			timeToNextBeat = tc.timeToNextBeat(remoteQuant);
			collective.sendToName(msg[1], 
				'/setClock', tc.tempo, tc.beats, timeToNextBeat, Main.elapsedTime);
		});
		
		
		this.addResponder('/setClock', { arg r, t, msg;
			var tempo, beats, timeToNextBeat, networkLatency, dt;
			#tempo, beats, timeToNextBeat, networkLatency = msg[[1, 2, 3, 4]];
			beats = 0; // could this be a problem?
			dt = timeToNextBeat - latency;
			if(dt > 0) {
				
				// might result in hanging notes:
				//TempoClock.default.clear;
				//TempoClock.default.stop;
				
				// better just replace clock.
				// setting default clock
				TempoClock.default 
					= TempoClock(tempo, beats, Main.elapsedTime + dt).permanent_(true);
				
				waitingAction.value;
				"starting new clock (%, %) ... in % seconds\n"
					.postf(tempo, beats, dt.round(0.0001));
								
				SystemClock.sched(dt, {
					setClockAction.(TempoClock.default);
					"new clock is running.".postln; nil 
				});
			} {
				"Synchronization failed. Please try again.".warn;
			}
			
		});


	}
	
}




