+ SoundFile { 
			
	extractChannel {  |ch = 0, numFrames = -1, startFrame = 0, outPath, server, doneAction|
		// extract one channel using server
		var outFile;
		server = server ? Server.default;
		
		outPath =  outPath ?? {  
			path.removeExtension.makeDir;
			path.removeExtension ++ "/" ++ 
				path.basename.removeExtension ++ "_" ++ (ch+1)
				++ "." ++ ( path.extension ? headerFormat ); 
			};
			
		outFile = this.class.new( outPath );
		
		server.waitForBoot( {
		"reading channel % of file %\n".postf( ch+1, path.basename );
		Buffer.readChannel(server, path, channels: [ch],
			action: { |buf| 
				"writing channel % of file %\n".postf( ch+1, path.basename );
				Buffer( server, buf.numFrames, buf.numChannels, buf.bufnum )
					.writeAction(outPath, headerFormat, sampleFormat,
					action: { |buf2|
						 buf.free;
						 "done channel % of file %\n".postf( ch+1, path.basename );
						 doneAction.value( outFile, buf );
						 }
				) } ); 
			} );
		
		^outFile
		}
		
	splitChannels { |outPath, startFrame = 0, numFrames, server, 
					doneAction, newHeaderFormat, newSampleFormat|
					
			// carefull: overwrites existing files
			//
			// files will go into folder with file name of original, containing
			// the channels as numbered files of the same format as the original
			
			var outFiles;
			var xFunc;
			
			server = server ? Server.default;
			
			outPath = outPath ? path;
			
			outFiles = { |i| // array with soundfiles for channels
				var aPath;
				SoundFile( outPath.removeExtension ++ 
						"/" ++ outPath.basename.removeExtension ++ "_" ++ (i+1)
						++ "." ++ ( outPath.extension ? headerFormat ) ); } ! numChannels;
						
			outPath.removeExtension.makeDir; // will create a dir even if operation fails
			
			// runs in background, channels one by one
			// might be slow, but doesn't freeze the lang
			// use splitChannelsLang for doing this from the lang instead of server
			
			xFunc = { |ch| // recursive function
				if( ch < numChannels )
					{ this.extractChannel( ch, 
						numFrames ? -1, startFrame ? 0, outFiles[ ch ].path, 
						server, { xFunc.value( ch + 1 ) } ) }
					{ doneAction.value( outFiles ); }; 
				};
			
			xFunc.value( 0 );
				
			^outFiles
		}
		
	splitChannelsLang { |outPath, startFrame = 0, numFrames, chunkSize = 1048576, 
					doneAction, newHeaderFormat, newSampleFormat|
					
			// carefull: overwrites existing files
			//
			// files will go into folder with file name of original, containing
			// the channels as numbered files of the same format as the original
			
			var	rawData, rawChannels;
			
			var outFiles;
			
			numFrames.notNil.if({ numFrames = numFrames * numChannels; },
				{ numFrames = inf });
	
				// chunkSize must be a multiple of numChannels
			chunkSize = (chunkSize/numChannels).floor * numChannels;
			
			outPath = outPath ? path;
			
			outFiles = { |i| // array with soundfiles for channels
				var aPath;
				SoundFile.new
					.headerFormat_( newHeaderFormat ? headerFormat )
					.sampleFormat_( newSampleFormat ? sampleFormat )
					.numChannels_(1)
					.path_( outPath.removeExtension ++ 
						"/" ++ outPath.basename.removeExtension ++ "_" ++ (i+1)
						++ "." ++ ( outPath.extension ? headerFormat ) ); } ! numChannels;
						
			outPath.removeExtension.makeDir; // will create a dir even if operation fails
	
			outFiles.do( _.openWrite );
			
			this.seek(startFrame, 0);
			
			
			{	(numFrames > 0) and: {
					rawData = FloatArray.newClear(min(numFrames, chunkSize));
					this.readData(rawData);
					//"next chunk (size=%) read\n".postf( rawData.size );
					rawData.size > 0
				}
			}.while({
			
				rawData = rawData.clump( numChannels );
				
				rawChannels = { FloatArray.newClear( rawData.size ); } ! numChannels;
				rawData.do({ |samp, i|
					numChannels.do({ |ii| rawChannels[ii][i] = samp[ii] });
					});
					
				rawChannels.do({ |channelData, i|
					if( outFiles[i].writeData(channelData) == false )
						{ MethodError("SoundFile writeData failed.", this).throw };
					//"next channel (%) written chunk\n".postf( i+1 );
					
					// write, and check whether successful
					// throwing the error invokes error handling that closes the files
				
					});
				numFrames = numFrames - chunkSize;
			});
			outFiles.do( _.close );
			doneAction.value( outFiles );
			^outFiles
		}	
		
	}
	
+ Buffer {
	
	writeAction { arg path,headerFormat="aiff",sampleFormat="int24",
			numFrames = -1, startFrame = 0,leaveOpen = false, action;
		this.addToServerArray;
		doOnInfo = action;
		this.waitForBufInfo;
		server.listSendMsg( 
			this.writeMsg(path,headerFormat,sampleFormat,numFrames,startFrame,
				leaveOpen, {|buf|["/b_query",buf.bufnum]}) 
			);
		}
	}
