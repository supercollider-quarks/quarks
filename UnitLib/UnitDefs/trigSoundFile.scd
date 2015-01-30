/*
\trigSoundFile

plays a soundfile buffer upon a trigger (can be UMap), with settable crossfade time and start position.

trigger: the trigger that makes the playback jump to the start position
crossfade: crossfade time applied when trigger is initiated (max. voices: 4)
pos: position in the soundfile (0-1)
reverse: if true, the soundfile is played back in reverse. Position is also reversed in this case
soundFile: a BufSndFile object, a region of an existig soundfile url on your hard drive. The object knows the duration and file path of the soundfile, and can be set to clip off the start and end of the file.
	- start: region startoffset (samples or seconds)
	- end: end of the region (samples or seconds)
	- loop: loop mode on or off (can be changed during playback, crossfade time does not apply here)
	- rate: playback rate (semitones or ratio)
rateScale: a scale value for the playback rate (can be modulated)
level: playback level (amplitude of the soundfile)
	
only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \trigSoundFile, {
		var sig;
		var pos, reverse, crossfade;
		var numChannels, bufnum, rate, trigger, startPos, loop;
		
		trigger = \trigger.tr(1);
		crossfade = \crossfade.kr(0.1);
		pos = \pos.kr(0.0);
		reverse = \reverse.kr(0.0);
		pos = (pos + reverse).fold(0,1);
		
		#numChannels, bufnum, rate, trigger, startPos, loop =
			BufSndFilePlayer.getArgs( Udef.numChannels, \soundFile, trigger, 0 );
			
		rate = rate * \rateScale.kr( 1 ) * reverse.linlin(0,1,1,-1);
			
		sig = PlayBufCF.ar( 
			numChannels, bufnum, rate, trigger, pos * BufFrames.ir( bufnum ),
			loop, crossfade, 4 // 4 voices
		);
	
		UMixOut.ar( 0, sig * \level.ukr( 1, \amp ), 0, true );
		
		
	}, [ [ \soundFile, nil, BufSndFileSpec(nil) ] ], \soundFile, true )
	.setSpec( \trigger, TriggerSpec() )
	.setSpec( \reverse, BoolSpec(false) )
	.setSpec( \rateScale,  [0.125,8,\exp,0,1].asSpec )
	.setSpec( \crossfade, [0,1,\lin,0,0.1," s"].asSpec )
	.chooseFunc_({ |args|
		var sf, numChannels;
		sf = (args ? []).pairsAt( \soundFile );
		if( sf.notNil ) { sf.numChannelsForPlayBuf } { 1 };
	})
	.nameFunc_({ |unit|
		var sf, path;
		if( (sf = unit.get( \soundFile )).notNil && { sf.path.notNil } ) {
			sf.path.basename ++ " : trigSoundFile";
		} {
			"trigSoundFile"
		};
	});