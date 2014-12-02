+ Score {

*batchNRT { |inputpath, outputpath, synthdefname, 
	synthparams=nil, maxtimeperfile=0, outputlengthfunc=nil, synthdurfunc=nil,
	plot=false, inchannels=1, outchannels=1, 
	outputnameadd="",
	extrainitcommands=nil,
	headerFormat="WAV", sampleFormat="int16",
	action=nil,
	really=true,
	opts=nil|
	
	
	var files, infilelength, outfilelength, sfreader, outfilepath, commands, 
		limit, check, synthdur, filesproduced, nrtrunning;
	
	
	// Grab a list of files matching the pattern
	files = inputpath.pathMatch.collect(PathName.new(_));
	if(files.size==0, {
		"Error in Score.batchNRT: No files matched input path pattern.".postln;
		^nil;
	});
	
	sfreader = SoundFile.new;
	
	outputpath = outputpath.standardizePath;
	
	filesproduced = Array.new(files.size);
	
	Routine({
	
	files.do
	{ |filepath|
		if(sfreader.openRead(filepath.fullPath).not, {
			("Error: SoundFile could not read "++filepath).postln;
		}, {
			//"DEBUG: Opened sound file".postln;
			infilelength = sfreader.numFrames / sfreader.sampleRate;
			
			if(sfreader.numChannels != inchannels, {
				("Warning: "++inchannels++" expected, but "++sfreader.numChannels
					++" found in file "++filepath.fileName).postln;
			});
			// Decide on the length of the output file, using the func if supplied
			outfilelength = if(outputlengthfunc.isNil, sfreader.numFrames, outputlengthfunc.value(infilelength));
			synthdur = if(synthdurfunc.isNil, infilelength, synthdurfunc.value(infilelength));
			// Decide on the output file path
			outfilepath = outputpath ++ "/" ++ 
				filepath.fileNameWithoutExtension ++ outputnameadd 
				++ "." ++ filepath.extension;
			
			if(really, {	
				
				// Create the list of commands, which must:
				commands = extrainitcommands ++
				     [
					//  - Load the input buffer
					[0.0, [\b_allocRead, 0, filepath.fullPath]],
					//  - Create the output buffer
					[0.0, [\b_alloc, 1, outfilelength, outchannels]],
					//  - Create the synth
					[0.0, [ \s_new, synthdefname, 1000, 0, 0,
								\inbufnum,0, \outbufnum, 1, \length, infilelength] ++ synthparams],
					// [...later...]
					//  - Write the output data to disk
					[synthdur,[\b_write, 1, outfilepath, sfreader.headerFormat, "float"]],
					// Kill the synth
					[synthdur, [\n_free, 1000]]
				];
				
				commands.postcs;
				
				opts = (opts ?? {ServerOptions.new}).numOutputBusChannels_(outchannels);
	
				// RUN THE NRT PROCESS
				("Launching NRT for file "++filepath.fileName).postln;

				nrtrunning = true;
				Score.recordNRTThen(commands, "NRTanalysis", nil, nil,44100, headerFormat, sampleFormat, opts,
					action: {|pid| nrtrunning = false}); // synthesize
				while({nrtrunning}, {0.2.wait});
				
			}); // End of "really?"

			if(File.exists(outfilepath), {
				
				// List of actually-created files
				filesproduced = filesproduced.add(outfilepath);
				
				// Plot the output buffer if requested (would need to load from output sound file)
				if(plot, { { var psfr; // ws 09 prevent inlining warning
						psfr = SoundFile.new;
						psfr.openRead(outfilepath);
						{
							psfr.plot;
						}.defer;
					}.value;
				});
			});
		});
	};
	// End of foreach file
	
	"-----------------------------------".postln;
	"Score.batchNRT process has finished".postln;
	"-----------------------------------".postln;
	
	action.value(filesproduced);
	
	}).play(SystemClock);

} // End .batchNRT


}