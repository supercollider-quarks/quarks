ReimData {

	// represented data
	var <data;
	var <dimension;
	var <server;
	var <>dataBus;
		
	*new {|server, dimension|
		^super.new.init(server, dimension)
	}
	
	init {|argServer, argDim|
		server = argServer;
		dimension = argDim;
		data = Array.fill(dimension, 0);
	}
	
	data_ {|newData|
	
		// should we check for correct dimensionality?
		//data = newData.flat(0..dimension);
		data = newData;
		this.updateAudioRendering;
	}
	
	updateAudioRendering {
		this.initBus;
		// set buses values
		dataBus.setn(data);
	}
	initBus {
		dataBus.isNil.if({
			dataBus = Bus.control(server, dimension);
		});	
	}
	kr {
		this.initBus;
		^In.kr(dataBus.index, dataBus.numChannels)
	
	}
}


ReimFilter {
	classvar presets;
	classvar uninitialized = true;
	classvar <>presetpath;


	*presets {
		this.init;
		^presets
	}

	*init {
		presetpath.isNil.if{
			presetpath = "%%ReimFilter.scd".format(
				thisProcess.platform.userAppSupportDir, 
				thisProcess.platform.pathSeparator
			)
		};

		this.read.not.if({
			"\tUsing default presets.".inform;
			// add various synthesis strategies
			presets = IdentityDictionary[
			
				\reson -> {|in, sreim|
					// multichannel controls
					var freqs, amps, rings;
					
					var ringtime, highFreq;
					
					// controls
					ringtime = \ringtime.kr(0.1);
					highFreq = \highFreq.kr(1000);
					
					freqs = Select.kr(sreim.abs > 0, [100, sreim.abs]) * 4000 + 2000;
			
					amps = sreim > 0;
					rings = sreim > 0;
					
					in = (in + HPF.ar(in, highFreq)) * 0.5;
					DynKlank.ar(
						`[freqs.lag(0.1), DelayN.kr(amps, 0.1, 0.1), rings * ringtime], 
						in * 0.25
					).tanh;
				}
			];
		}); // fi

		uninitialized = false;
	}
	*add{|key, definition|
		presets[key] = definition
	}
	*at{|key|
		uninitialized.if{
			this.init;
		};
		^presets[key];		
	}
	*controlNames{|key|
		// get controlNames by instatiating a temporary SynthDef (hack!)
		^SynthDef(\tmp, presets[key], [\ar, \kr]).allControlNames
	}
	*ar {|key(\reson), in, sreim|
		uninitialized.if{
			this.init;
		};
		^presets[key].value(in, sreim.kr)
	}

	*read {
		var file;
		File.exists(presetpath).if({
			"ReimFilter : reading presets from %.".format(presetpath).inform;
			file = File(presetpath,"r");
			presets = file.readAllString.interpret;
			file.close;
			^true;
		}, {
			"ReimFilter: % does not exist.".format(presetpath).inform;
			^false
		})
	}
	
	*write {
		"ReimFilter : writing presets to %.".format(presetpath).inform;
		File.use(presetpath, "w", {|f| f.write(presets.asCompileString)});
	}
}


ReimPresets : ReimFilter {}
