/*
\loopSoundFile

loops a soundfile buffer, with settable crossfade time and optional random variation of loop start and duration.

loopStart: (range) minimum and maximum start position of the loop (0-1). 
loopDur: (range) minimum and maximum duration of the loop (0-1). 
The duration of the whole soundfile is mapped from 0 to 1 in loopStart and loopDur. The actual start position and duration are randomly chosen every time the loop starts again.
crossfade: the crossfade time in seconds. The loop engine has 4 voices, which means that if the loop duration in seconds is less than 1/4 of the crossfade time there may be audible clicks.
seed: random seed. The same value for seed gives the exact same signal on every machine, every time the unit is played.
soundFile: a BufSndFile object, a region of an existig soundfile url on your hard drive. The object knows the duration and file path of the soundfile, and can be set to clip off the start and end of the file.
	- start: region startoffset (samples or seconds)
	- end: end of the region (samples or seconds)
	- loop: loop mode on or off (can be changed during playback)
	- rate: playback rate (semitones or ratio)
rateScale: a scale value for the playback rate (can be modulated)
level: playback level (amplitude of the soundfile)
	
only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \loopSoundFile, { |loopStart = #[0,0], loopDur = #[1,1], crossfade = 0.1, 
			seed = 12345|
		var sig;
		var numChannels, bufnum, rate, trigger, startPos, loop;
		
		URandSeed.ir(seed);
		
		#numChannels, bufnum, rate, trigger, startPos, loop =
			BufSndFilePlayer.getArgs( Udef.numChannels, \soundFile, 1, 0 );
			
		rate = rate * \rateScale.kr( 1 );
			
		sig = PlayBufCF.ar( 
			numChannels, bufnum, rate, 
			Dwhite( *loopDur * ( BufDur.ir( bufnum ) / rate) ), 
			Dwhite( *loopStart * BufFrames.ir( bufnum ) ),
			loop, crossfade, 4 // 4 voices
		);
	
		UMixOut.ar( 0, sig * \level.ukr( 1, \amp ), 0, true );
		
		
	}, [ [ \soundFile, nil, BufSndFileSpec(nil) ] ], \soundFile, true )
	.setSpec( \loopStart, RangeSpec( default: [0,0] ) )
	.setSpec( \loopDur, RangeSpec( default: [1,1] ) )
	.setSpec( \rateScale,  [0.125,8,\exp,0,1].asSpec )
	.setSpec( \crossfade, [0,1,\lin,0,0.1," s"].asSpec )
	.setSpec( \seed, PositiveIntegerSpec(12345) )
	.chooseFunc_({ |args|
		var sf, numChannels;
		sf = (args ? []).pairsAt( \soundFile );
		if( sf.notNil ) { sf.numChannelsForPlayBuf } { 1 };
	})
	.nameFunc_({ |unit|
		var sf, path;
		if( (sf = unit.get( \soundFile )).notNil && { sf.path.notNil } ) {
			sf.path.basename ++ " : loopSoundFile";
		} {
			"loopSoundFile"
		};
	});