/*
\diskSoundFile

A soundfile player. This plays soundfiles of any duration, with any (*) number of channels, by loading the whole file into the memory. Use this typically for longer (over apx 2 minutes) audiofiles. For shorter files you can also use \bufSoundFile.

soundFile: a DiskSndFile object, a region of an existig soundfile url on your hard drive. The object knows the duration and file path of the soundfile, and can be set to clip off the start and end of the file.
	- start: region startoffset (samples or seconds)
	- end: end of the region (samples or seconds) (**)
	- loop: loop mode on or off (***)
	- rate: playback rate (semitones or ratio)
level: playback level (amplitude of the soundfile)
	
(*) only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

(**) the playback will not stop at region end. This parameter is mostly here to provide compatibility with the \bufSoundFile player. It is however used for determining the duration of a UChain when pressing the "from soundfile" button in the UChain window.

(***) with \diskSoundFile, loop the loop points will always be the start and end of the original file; it doesn't take into account the region start/end positions.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \diskSoundFile, {
		UMixOut.ar( 0, DiskSndFilePlayer.ar( Udef.numChannels ) * \level.ukr( 1, \amp ), 0, true );
	}, [ [ \soundFile, nil, DiskSndFileSpec(nil) ] ], \soundFile, true )
	.chooseFunc_({ |args|
		var sf, numChannels;
		sf = (args ? []).pairsAt( \soundFile );
		if( sf.notNil ) { sf.numChannels } { 1 };
	})
	.nameFunc_({ |unit|
		var sf, path;
		if( (sf = unit.get( \soundFile )).notNil && { sf.path.notNil } ) {
			sf.path.basename ++ " : diskSoundFile";
		} {
			"diskSoundFile"
		};
	});
