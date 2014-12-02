SensorLog{
	var <inbus,<outbus;
	var <path, <logbuffer;
	var <lresponder,<presponder;
	var <server;
	var <logsynth,<playsynth;
	var <id;
	var <timebuffer,<databuffer;
	var <logfiles, <playid;
	classvar <logFolder;
	classvar <nsensors = 0;

	*makeLogFolder { 
		var supportDir = thisProcess.platform.userAppSupportDir; 
		var specialFolder = supportDir ++ "/SensorLogs";
		
		if (pathMatch(supportDir).isEmpty) { logFolder = ""; ^this };
		
		if (pathMatch(specialFolder).isEmpty) { 
			unixCmd("mkdir \"" ++ specialFolder ++ "\""); 
			if (pathMatch(specialFolder).isEmpty) { 
				logFolder = supportDir; // if not there, put it in flat
			}
		} { 
			logFolder = specialFolder;
		}; 
		
		("// SensorLog.logFolder:" +  logFolder).postln;
	}


	*loadSynthDefs{ |s|
		// log data with timer intervals
		SynthDef( \sensorLogRecord, { |inbus=0,buffer=0,t_reset=1|
			var trig,input,time,sample;
			trig = InTrig.kr( inbus );
			time = Timer.kr( trig );
			input = In.kr( inbus, 1 );
			sample = PulseCount.kr( trig, t_reset );
			SendTrig.kr( sample > ( BufFrames.kr(buffer) - 10 ), 0, 1 ); 
			Logger.kr( [time,input], trig, buffer, t_reset );
		}).send(s);

		// playback recorded sensor data
		// read one channel with the time intervals, and the other channel for the data
		SynthDef( \sensorLogPlayback, { |outbus=0,timebuf=0,databuf=0,t_reset=1|
			var trig,data,phase;
			trig = ListTrig2.kr( timebuf, t_reset ); // triggers for each time interval
			phase = PulseCount.kr( trig, t_reset ); // counts the samples
			SendTrig.kr( phase > ( BufFrames.kr(databuf) - 10 ), 0, 1 ); 
			data = BufRd.kr( 1, databuf, phase );
			Out.kr( outbus, data );
		}).send(s);
	}

	*new{ |input,length,server|
		^super.new.init(input,length,server);
	}

	*newFromPack{ |path,server|
		^super.new.initFromPack( path,server );
	}

	initFromPack{ |path,s|
		logfiles = Array.new;
		server = s ? Server.local;
		this.class.loadSynthDefs( server );
		this.class.makeLogFolder;
		this.unpack( path );
	}

	init{ |in,length,s|
		logfiles = Array.new;
		id = nsensors;
		nsensors = nsensors + 1;
		length = length ? 4096;
		server = s ? Server.local;
		// send synthdefs
		this.class.loadSynthDefs( server );
		this.class.makeLogFolder;
		// allocate buffer
		logbuffer = Buffer.alloc( server, length, 2);
		inbus = in;
	}

	startLog{
		this.createPathName;
		// start synths
		logsynth = Synth.new( \sensorLogRecord, [ \inbus, inbus, \buffer, logbuffer ] );
		lresponder = OSCresponderNode( server.addr, '/tr', { 
			arg time, responder, msg;
			msg.postln;
			if ( msg[1] == logsynth.nodeID, {
				this.saveBuffer;
				//, completionMessage: { logsynth.set( \t_reset, 1 ) } );
				SystemClock.sched(0.5, { logsynth.set( \t_reset, 1 ); this.createPathName; } );
			});
		}).add;
	}

	createPathName{
		path = logFolder++"/sensorLog_"++id++"_"++Date.getDate.stamp++".aiff";
		logfiles = logfiles.add( path );
	}

	saveBuffer{
		// save buffer
		logbuffer.write( path, "aiff", "float" );
	}

	stopLog{
		// stop synths
		this.saveBuffer;
		logsynth.free;
		lresponder.remove;
	}

	playback{
		playid = 0;
		if ( outbus.isNil,{
			outbus = Bus.control( server, 1 );
		});
		this.readPlaybuffer( {
			playsynth = Synth.new( \sensorLogPlayback,
				[\outbus, outbus, \timebuf, timebuffer, \databuf, databuffer ], server, \addToHead );
		} );
		// start synths
		lresponder = OSCresponderNode( server.addr, '/tr', { 
			arg time, responder, msg;
			msg.postln;
			if ( msg[1] == playsynth.nodeID, {
				this.readPlaybuffer( { 
					playsynth.set( \timebuf, timebuffer, \databuf, databuffer, \t_reset, 1 ); 
				} );
			});
		}).add;
		
	}

	readPlaybuffer{ |action|
		var oldtime, olddata;
		oldtime = timebuffer;
		olddata = databuffer;
		if ( playid < (logfiles.size - 1), {
			path = logfiles[playid];
			timebuffer = Buffer.readChannel( server, path, channels: [0] );
			databuffer = Buffer.readChannel( server, path, channels: [1], 
				action: { action.value;
					if ( oldtime.notNil, { oldtime.free } );
					if ( olddata.notNil, { olddata.free } );
				});
		});
		playid = playid + 1;
	}

	stop{
		// stop synths
		playsynth.free;
		presponder.remove;
	}

	freeAll{
		if ( outbus.notNil, { outbus.free } );
		if ( timebuffer.notNil, { timebuffer.free });
		if ( databuffer.notNil, { databuffer.free });
		if ( logbuffer.notNil, { logbuffer.free });
		if ( playsynth.notNil, { playsynth.free });
		if ( logsynth.notNil, { logsynth.free });
		if ( presponder.notNil, { presponder.remove } );
		if ( lresponder.notNil, { lresponder.remove } );
	}

	pack{ arg filename;
		var cmdname;
		("mkdir"+filename).unixCmd;
		logfiles.do{ |it|
			("cp"+it+filename++"/").unixCmd;
		};
		("tar -cvv --remove-files -f"+filename++".tar"+filename++"/").unixCmd;
	}

	unpack{ arg filename;
		("tar -xvvf"+filename++".tar").unixCmd;
		logfiles = (filename++"/*").pathMatch.sort;
	}
}