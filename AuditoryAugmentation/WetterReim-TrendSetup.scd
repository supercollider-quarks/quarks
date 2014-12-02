// this file is read by the WetterReim system

q.server = q.server ? Server.default;

// the audio in channel where the transducer is located (using SoundIn)
q.sourceChannel = q.sourceChannel ? 2;

// channel where to play back the auditory augmentation (Using Out)
q.outChannel = q.outChannel ? 0;

// name of the platform, used for multi-platform compatibility
q.os = thisProcess.platform.name;

q.prepareServer = {
	// set server parameters before booting
	// minimise latency
/*	q.server.options.blockSize = 32;
	q.server.options.hardwareBufferSize = 32;
*/
	q.server.options.blockSize = 32;
	q.server.options.hardwareBufferSize = 32;
	
	// allow for a bunch of interconnections in synth graphs
	q.server.options.numWireBufs = 64;
};

q.startServer = {
	// boot server
	q.server.boot;
};

// currently written in python.
q.startAcquireProcess = {
	q.os.switch (
		\osx, {
"python \"%/quarks/AuditoryAugmentation/python/wunderground-PWS.py\"".format(Platform.userAppSupportDir).runInTerminal;
		},
		\linux, {
		// this should work...
"xterm -e \"python \"%/quarks/AuditoryAugmentation/python/wunderground-PWS.py\"\"".format(Platform.userAppSupportDir).unixCmd;		
		},
		{"Only linux and osx are currently supported, sorry.".error}
	)
};

q.init = {
	// some specs of the weather
	q.specs = IdentityDictionary[
		\temp -> ControlSpec(-30, 40),
		\hum -> ControlSpec(0, 100),
		\wind -> ControlSpec(0, 30),
		\press -> ControlSpec(940, 1070)
	];
	
	// here we hold the raw data
	q.weatherDict = IdentityDictionary[
		\temp -> 0.1,
		\hum -> 0.2,
		\wind -> 0.3,
		\press -> 0.4
	];
	
	// this is the place the data is stored for the sonification
	q.reimData = ReimData(s, q.specs.size);
	q.historyData = ReimData(s, q.specs.size);
	
	// the responder that listens to the incoming data stream. 
	// If there is something new, it changes the ReimData object accordingly.
	q.responder = OSCresponderNode(nil, "/weather", {|time, resp, msg|
		var updated = 0;
		var newVal;
		msg[1..].pairsDo{|k v|
			newVal = q.specs[k].unmap(v);
			updated = updated + (q.weatherDict[k] != newVal).binaryValue;
			q.weatherDict[k] = newVal;
		};
		(updated>0).if{
			"updated".inform;
			q.historyData.data = q.reimData.data.copy;
			q.reimData.data = q.weatherDict.asArray;
		}
	});
};

q.addFilterPresets = {
	ReimFilter.presets[\multiFreqReso] = {|in, sreim|
		// multichannel controls
		var freqs, amps, rings;
		
		var ringtime, highFreq, baseFreq;
		
		// controls (rebuild for 3.2 compatibility)
		// ringtime = \ringtime.kr(0.1);
		ringtime = Control.names([\ringtime]).kr(#[0.1]);
		// highFreq = \highFreq.kr(1000);
		highFreq = Control.names([\highFreq]).kr(#[1000]);
		// baseFreq = \baseFreq.kr(523);
		baseFreq = Control.names([\baseFreq]).kr(#[523]);
	
		freqs = (sreim.collect{|reim, i|
			(i + reim) * 24
		} + baseFreq.cpsmidi).midicps;
		
		freqs = Select.kr(sreim.abs > 0, [1, freqs]);
	
	
		amps = sreim > 0;
		//rings = sreim > 0;

		rings = sreim;
		in = (in + HPF.ar(in, highFreq)) * 0.5;
		DynKlank.ar(
			`[freqs.lag(0.1), DelayN.kr(amps, 0.1, 0.1), rings * ringtime], 
			in * 0.25
		).tanh;
	};
};


q.startAll = {
	q.responder.add;
};

q.stopAll = {
	q.responder.remove;
};
