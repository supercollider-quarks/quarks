+ Function {
	
	render {  // use the same way as Function:play
			// renders as a soundfile with NRT synthesis
		arg path, duration = 10, fadeTime=0.02, sampleRate = 44100,
			headerFormat = "AIFF", sampleFormat = "int24", options, inputFilePath,
			prependScore, 
			sfBuffer, async = true, action; // 1 or more buffers containing <>path information
		
		// 24 bits AIFF as default
			
		var def, synth, synthMsg;
		var numChannels;
		var oscFilePath, score, file;
		
		// "recording.. (please wait)".postln; // doesn't post anyway
		// generate synthdef
		def = this.asSynthDef(
			fadeTime:fadeTime, 
			name: "NRT_temp_" ++ this.identityHash.abs.asString
		);
		
		// write, not load synthdef
			
		def.writeDefFile;
				
		// get number of channels from Function
		numChannels = this.value.asCollection.size; 
		options = options ? Score.options;
		options = options.deepCopy; // copy before we modify
		options.numOutputBusChannels = numChannels;
		
		// create one synth instance at nodeID 1000
		synth = Synth.basicNew(def.name, nodeID: 1000);
		
		// create a score playing the synth once, with release
		score = Score( [ 
			[0.0, synth.newMsg ], 
			[duration-fadeTime, synth.releaseMsg],
			[duration, [\c_set, 0, 0]],
			] );
			
		if( sfBuffer.notNil )
			{ prependScore = sfBuffer.asCollection
				.collect({ |buf| [0.0, buf.allocReadMsg( buf.path ) ] }) ++ prependScore };
			
		if( prependScore.notNil )
			{ score.score = prependScore ++ score.score };
			
		// render the score
		oscFilePath = "temp_oscscore" ++ UniqueID.next;
		path = path.standardizePath;
		path.dirname.makeDir;
		
		if( async )
		{
		
		/*
		 score.recordNRT(
				oscFilePath, path.standardizePath, inputFilePath, sampleRate = 44100, 
				headerFormat, sampleFormat, options, 
				"; rm" + oscFilePath + 
				"; rm synthdefs/" ++ def.name ++ ".scsyndef" // delete synthdef file afterwards
				); 
		*/
			
		score.writeOSCFile(oscFilePath, 0, duration);
		
		(Score.program + " -N" + oscFilePath + (inputFilePath ? "_") + "\""++path++"\""
		 	+ sampleRate + headerFormat + sampleFormat +
			options.asOptionsString
			+ " > ~/Desktop/output.txt; rm" + oscFilePath + 
				"; rm synthdefs/" ++ def.name ++ ".scsyndef" // delete synthdef file afterwards
		).unixCmd({ |...args| // result, pid
			"done recording file: '%'\n".postf( path ); 
			action.value( *[path] ++ args ); });
		//"recording file: '%'\n".postf( path );
		^path;	
				
		}
		{ score.recordNRTs(
				oscFilePath, path.standardizePath, inputFilePath, sampleRate = 44100, 
				headerFormat, sampleFormat, options, 
				"; rm" + oscFilePath + 
				"; rm synthdefs/" ++ def.name ++ ".scsyndef" // delete synthdef file afterwards
				); 
		 "recorded file: '%'\n".postf( path );
		 ^path;
		  };
		
		
	
		}
	
	}