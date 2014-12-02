

ServerLog : NetAddr {

	var <msgs,<>tail=false,<server;
	var lastStatus;

	*new { arg server;
		server = server ? Server.default;
		if(server.addr.isKindOf(ServerLog),{ ^server.addr });
		^super.new(server.addr.hostname,server.addr.port).slinit(server);
	}
	*forServer { arg server;
		server = server ? Server.default;
		if(server.addr.isKindOf(ServerLog),{ ^server.addr });
		^nil
	}
	*start { |server|
		^this.new(server)
	}
	*stop { |server|
		this.forServer(server).remove
	}
	remove {
		server.addr = NetAddr(hostname,port);
	}
	clear {
		msgs = Array.new(1024*16);
	}
	*ifActive { arg server,func;
		var sl;
		sl = this.forServer(server);
		if(sl.notNil,{
			func.value(sl)
		})
	}


	filterBySynthDef { arg defName;
		^this.matchMsgs({ arg m; m.matchSynthDef(defName) })
	}
	filterBySynth { arg nodeID;
		^this.matchMsgs({arg m; m.matchSynth(nodeID) })
	}
	filterByBus { arg index,rate;
		^this.matchMsgs({arg m; m.matchBus(index,rate) })
	}
	matchMsgs { arg matchFunc;
		var matches;
		matches = [];
		msgs.do { arg msg;
			var ms;
			ms = msg.matchMsgs(matchFunc);
			if(ms.notNil,{  matches = matches ++ ms })
		};
		^matches
	}

	*guiMsgsForSynth { arg synth,layout,showTimes=false;
		this.prGuiMsgsFor([\filterBySynth,synth.nodeID],synth.server,layout,showTimes,"where synth="+synth.nodeID.asString)
	}
	*guiMsgsForSynthDef { arg defName,layout,server,showTimes=false;
		this.prGuiMsgsFor([\filterBySynthDef,defName],server,layout,showTimes,"where defName="+defName.asString)
	}
	*guiMsgsForBus { arg index,rate,layout,server,showTimes=false;
		this.prGuiMsgsFor([\filterByBus,index,rate],server,layout,showTimes,"where bus="+index.asString)
	}
	*prGuiMsgsFor { arg performList,server,layout,showTimes,title;
		var msgs,sl;
		sl = this.forServer(server)  ?? { "ServerLog not running".inform; ^this };
		msgs = sl.perform(*performList);
		if(msgs.notEmpty or: {layout.isNil},{
			ServerLogGui(sl,msgs).showTimes_(showTimes).gui(layout)
		})
	}


	slinit { arg s;
		server = s;
		server.addr = this;
		msgs = Array.new(1024*16);
		thisProcess.addOSCRecvFunc({ arg msg,time,replyAddr,recvPort;
			var status;
			if(msg[0] == '/status.reply') {
				status = msg[0..5];
				if(status != lastStatus,{
					msgs = msgs.add( ServerLogReceivedEvent(time,status) );
					lastStatus = status;
				});
			} {
				msgs = msgs.add( ServerLogReceivedEvent(time,msg) )
			}
		});
	}
	sendMsg { arg ... args;
		if(args != ["/status"],{
			msgs = msgs.add( ServerLogSentEvent( nil, args,false) );
			if(tail,{
			    args.postln;
			});
			args.do { arg a,i;
				if(a.isNumber and: {a.isNaN},{
					("NaN!!!!" + a).error;
					args.put(i,0)
				})
			}
		});
		^super.sendMsg(*args);
	}
	sendBundle { arg time ... args;
		msgs = msgs.add( ServerLogSentEvent( time,args,true) );
		if(tail,{
		    args.postln;
		});
		^super.sendBundle(*([time]++args))
	}
	guiClass { ^ServerLogGui }

	getSortedEvents { arg tail,callback;
		// list in logical time order
		Routine({
			var q,events,since,a,b;

			q = PriorityQueue.new;
			msgs.do({ |it| q.put(it.eventTime,it) });
			events = Array.fill(msgs.size,{ |i|
					if(i % 25 == 0,{0.05.wait});
					if(i % 250 == 0,{ 0.5.wait });
					q.pop
				});

			if(tail.notNil,{
				callback.value( events.copyRange(events.size-tail-1,events.size-1) );
			},{
				callback.value( events )
			})
		}).play(AppClock)
	}
	writeScore { arg path,callback,write;
		// Score.playFromPath(path)
		path = path ?? {
			Document.standardizePath(Date.getDate.asSortableString ++ ".score")
		};
		callback = callback ? {("OSC score written"+path).inform};
		this.getSortedEvents(nil,{ arg events;
			var startTime,x = Array(events.size);
			events.do { arg e;
				if(e.class === ServerLogSentEvent,{
					if(startTime.isNil,{ startTime = e.eventTime });
					x.add( [e.eventTime - startTime,e.msg] )
				})
			};
			x.add( [x.last[0] + 5.0, [\c_set, 0, 0]] );
			Score(x).saveToFile(path);
			//,path,SystemClock);
			callback.value(path);
		})
	}

	*cmdString { |cmd|
		if(cmd.asInteger != 0,{
			cmd = cmd.asInteger;
		});
		^cmd.switch(
			11, { "/n_free" },
			12, {"/n_run"},
			14, {"/n_map"},
			48, {"/n_mapn"},
			15, {"/n_set"},
			16, {"/n_setn"},
			17, {"/n_fill"},
			10, {"/n_trace"},
			46, {"/n_query"},
			18, {"/n_before"},
			19, {"/n_after"},
			21, {"/g_new"},
			22, {"/g_head"},
			23, {"/g_tail"},
			24, {"/g_freeAll"},
			50, {"/g_deepFree"},
			9,  {"/s_new"},
			44, {"/s_get"},
			45, {"/s_getn"},
			cmd.asString
		)
	}
}


ServerLogSentEvent {

	var <>delta,<>msg,<>isBundle,<>timeSent;
	var unbundled;

	*new { arg delta,msg,isBundle;
		^super.newCopyArgs(delta,msg,isBundle,Main.elapsedTime)
	}
	eventTime {
		^timeSent + (delta?0)
	}
	report {
		var msgFormat;
		// if(isBundle,{  TODO
			// i use the gui mostly

		(">>> % (% + %) % %".format(this.eventTime,timeSent,delta,this.cmdString,msg.copyToEnd(1))).postln
	}
	cmdString { ^ServerLog.cmdString(msg[0]) }

	unbundled {
		^unbundled ?? { unbundled = msg.collect(ServerLogSentEvent(delta,_,false,timeSent)) }
	}
	matchMsgs { arg func;
		if(this.isBundle,{
			^this.unbundled.select(func)
		},{
			if(func.value(this), { ^[this] }, { ^nil })
		})
	}
	// for non bundles only
	matchSynthDef { arg defName;
		var cmd;
		^(msg[1] == defName) and: {
			cmd = ServerLog.cmdString(msg[0]);
			cmd == "/s_new" /* or: { cmd == "/d_recv" } */
			// have to decode the bytes
		}
	}
	matchSynth { arg nodeID;
		^(msg[2] == nodeID) and: {  ServerLog.cmdString(msg[0]) == "/s_new" }
	}
	matchBus { arg index,rate;
		var cmd;
		cmd = ServerLog.cmdString(msg[0]);
		if(cmd == "/s_new",{
			// ideally retrieve the synth def and find args of correct spec
			^msg.any({ arg val,i;
				val.asString.containsi("bus") and: { msg[i+1] == index }
			})
		});
		// set messages

		^false
	}

}


ServerLogReceivedEvent {

	var <>time,<>msg,<>timeReceived;

	*new { arg time,msg,isBundle;
		^super.newCopyArgs(time,msg,Main.elapsedTime)
	}
	eventTime {
		^time
	}
	report {
		var cmd, one, numUGens, numSynths, numGroups, numSynthDefs,
					avgCPU, peakCPU, sampleRate, actualSampleRate;
		if(msg[0] == '/status.reply',{
			#cmd, one, numUGens, numSynths, numGroups, numSynthDefs,
					avgCPU, peakCPU, sampleRate, actualSampleRate = msg;
			("<<< % % ugens % synths % groups % synthDefs".format(this.eventTime,numUGens,numSynths,numGroups,numSynthDefs)).postln
		},{
			("<<< % % %".format(this.eventTime,ServerLog.cmdString(msg[0]),msg.copyToEnd(1))).postln;
		});
	}
	isBundle {
		^false
	}
	matchMsgs { arg func;
		if(func.value(this), { ^[this] },{ ^nil })
	}
	matchSynthDef { ^false }
	matchSynth { arg nodeID;
		^(msg[1] == nodeID) and: {  ServerLog.cmdString(msg[0]) == "/n_go" }
	}
	matchBus { ^false }
}

