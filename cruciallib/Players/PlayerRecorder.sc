

/*
	a utility class to prepare and precisely record a player to a soundfile
*/

PlayerRecorder {
		
	var <>player,<>recHeaderFormat,<>recSampleFormat;
	var recordBuf,bus,group,synth,responder,prOnComplete;
	var <>limit;
	
	*new { arg player,recHeaderFormat=\AIFF,recSampleFormat=\float;
		var s;
		s = player.server ?? {Server.default};
		^super.newCopyArgs(player,
			recHeaderFormat ?? {s.recHeaderFormat},
			recSampleFormat ?? {s.recSampleFormat})
	}
	
	// record the player to disk from start to finish
	// starting and stopping it precisely
	record { arg path,endBeat,onComplete,atTime;

		var timeOfRequest,server,do;
		if(player.isPlaying,{ 
			^Error("Cannot start record while playing").throw
		});
		prOnComplete = onComplete;
		timeOfRequest = Main.elapsedTime;
		server = this.server;
		path = path ?? {this.makePath};
		endBeat = endBeat ?? {player.beatDuration ? 64};
		do = {
			this.prRecord(path,endBeat,atTime,timeOfRequest)
		};
		if(server.serverRunning.not,{
			server.startAliveThread(0.1,0.4);
			server.waitForBoot({
				if(server.dumpMode != 0,{
					server.stopAliveThread;
				});
				InstrSynthDef.clearCache(server);
				if(server.isLocal,{
					InstrSynthDef.loadCacheFromDir(server);
				});
				this.bufferAndThen(path,do);
				nil
			});
		},{
			this.bufferAndThen(path,do);
		});
	}
	// record the player to disk starting atTime and without end
	// call stop or use command-. to end recording
	liveRecord { arg path,onComplete,atTime;

		var do, timeOfRequest;

		if(player.isPlaying.not,{ // not right yet
			^this.record(path,nil,onComplete,atTime)
		});

		path = path ?? {this.makePath()};
		timeOfRequest = Main.elapsedTime;
		prOnComplete = onComplete;
		do = {
			this.prLiveRecord(path,atTime,timeOfRequest)
		};
		this.bufferAndThen(path,do);
	}
	stop {
		this.free
	}
	cmdPeriod {
		this.free;
	}	
	makePath {
		var path;
		path = thisProcess.platform.recordingsDir +/+ player.asString.select(_.isFileSafe);
		if(thisProcess.platform.name == \windows) {
			path = path ++ Main.elapsedTime.round(0.01)
		} {
			path = path ++ Date.localtime.asSortableString
		};
		path = path ++ "." ++ recHeaderFormat;
		^path
	}
	server { ^player.server ? Server.default }
	
	bufferAndThen { arg path,then;
		var bufLoaded=false,timeout=60,server;

		server = this.server;
		CmdPeriod.add(this);
		responder = OSCresponderNode(server.addr,'/b_info',{ arg time,responder,msg;
			if(msg[1] == recordBuf.bufnum,{
				bufLoaded = true;
				responder.remove;
			});
		});
		responder.add;
				
		recordBuf = Buffer.alloc(server, 65536, player.numChannels,{ arg buf;
			buf.writeMsg(path, recHeaderFormat, recSampleFormat, 0, 0, true,{|buf|["/b_query",buf.bufnum]});
		});
		{
			while { bufLoaded.not and: {timeout > 0} } {
				timeout = timeout - 0.1;
				0.1.wait
			};
			then.value
		}.fork
	}
	prRecord { arg path, endBeat, atTime, timeOfRequest;
		// buffer should be ready
		var bundle,def,defName,server;
		server = this.server;
		bus = Bus.audio(server,player.numChannels);
		bundle = AbstractPlayer.bundleClass.new;
		group = Group.basicNew(server);
		bundle.add( group.addToTailMsg );
		player.prepareToBundle(group, bundle, false, bus);
		player.spawnToBundle(bundle);
		
		defName = this.synthDefToBundle(bundle);

		synth = Synth.basicNew(	defName, server );
		bundle.add( synth.addToTailMsg(group,[\bufnum,recordBuf.bufnum,\busnum,bus.index]) );
		if(endBeat.notNil,{
			bundle.addFunction({
				var ender;
				ender = AbstractPlayer.bundleClass.new;
				ender.add(recordBuf.closeMsg(recordBuf.freeMsg));
				ender.addFunction({
					recordBuf = nil;
					synth = nil;
					group = nil;
					bus.free;
					bus = nil;
				});
				player.stopToBundle(ender);
				player.freeToBundle(ender);
				ender.add(group.freeMsg);
				OSCSched.sched(endBeat,server,ender.messages,{
					ender.doFunctions;
					("Recording ended:" + path).inform;
					CmdPeriod.remove(this);
					prOnComplete.value(this,path)
				})
			})
		});
		("Recording" + path + "beat duration:" + endBeat).inform;
		bundle.sendAtTime(player.server,atTime ? player.defaultAtTime,timeOfRequest);
	}
	prLiveRecord { arg path, atTime, timeOfRequest;
		// buffer should be ready
		var bundle,def,defName,server;
		var bus; // not my bus, its owned by the player
		server = this.server;
		bus = player.bus;
		bundle = AbstractPlayer.bundleClass.new;
		group = Group.basicNew(server);
		bundle.add( group.addToTailMsg );

		defName = this.synthDefToBundle(bundle,false);

		synth = Synth.basicNew(	defName, server);
		bundle.add( synth.addToTailMsg(group,[\bufnum,recordBuf.bufnum,\busnum,bus.index]) );

		("Recording" + path).inform;
		bundle.sendAtTime(player.server,atTime ? player.defaultAtTime,timeOfRequest);
	}
	synthDefToBundle { arg bundle,listen=true;
		var defName,def;
		defName = "__player-record-" ++ player.numChannels ++ listen.if("-listen","");
		def = SynthDef(defName, { arg bufnum,busnum;
			var out;
			out = In.ar(busnum, player.numChannels);
			if(limit ?? {recSampleFormat.asString.contains("int")}
			,{
				out = Limiter.ar(out,-0.01.dbamp)
			});
			if(listen,{
				Out.ar(0, out );
			});
			DiskOut.ar(bufnum, out);
		});
		bundle.addPrepare([ "/d_recv", def.asBytes]);
		^defName
	}
	free {
		if(recordBuf.notNil,{
			"Recording ended".inform;
			recordBuf.close;
			recordBuf.free;
			recordBuf = nil;
		});
		group.free;
		bus.free;
		synth = group = bus = nil;
		responder.remove;
		CmdPeriod.remove(this);
		prOnComplete.value;
		prOnComplete = nil;
	}
}

