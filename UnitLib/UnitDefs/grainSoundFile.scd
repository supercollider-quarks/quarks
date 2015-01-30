/*
\grainSoundFile

A granular soundfile player. This will play tiny fragments of the soundfile with optional random dispersion and distribution.

soundFile: a MonoBufSndFile object, a region of an existig soundfile url on your hard drive. The object knows the duration and file path of the soundfile, and can be set to clip off the start and end of the file.
	- start: region startoffset (samples or seconds)
	- end: end of the region (samples or seconds)
	- loop: loop mode on or off (can be changed during playback)
	- rate: playback rate (semitones or ratio)
If the file has multiple channels, only the first one will be used
pos: range of position in the file between 0 (start) and 1 (end)
density: the average number of grains per second
timeVar: time variation factor (0-4). 0 means no variation, 4 means times between grains can be within (1/4) and 4 times the original duration
rateVar: ratio variation in number of octaves
overlap: average number of overlapping grains. The duration of the grains is change to match this.
pan: the pan position range. This is only active if numChannels > 1
seed: random seed
level: playback level (amplitude of the soundfile)
numChannels: number of output channels

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \grainSoundFile, {
	var numChannels, bufnum, rate, trigger, startFrame, loop;
	var pos, density, timeVar, rateVar, rateScale, overlap, pan, sig;
	#numChannels, bufnum, rate, trigger, startFrame, loop = 
		BufSndFilePlayer.getArgs( 1, startPos: 0 );
	rate = rate / BufRateScale.kr( bufnum );
	pos = \pos.kr( [0.5,0.5] );
	density = \density.kr( 100 );
	timeVar = (\timeVar.kr( 0 ) * 6).midiratio;
	rateVar = (\rateVar.kr( 0 ) * 12).midiratio;
	rate = rate * \rateScale.kr( 1 );
	overlap = \overlap.kr(4);
	pan = \pan.kr( [ -1,1 ] );
	URandSeed.ir(\seed.ir(12345));
	trigger = TDuty.ar( Dwhite( 0,1 ).linexp( 0,1, 1/timeVar/density, timeVar/density ) );
	sig = GrainBuf.ar( Udef.numChannels, trigger, overlap/density, bufnum, 
		Demand.ar( trigger, 0, Dwhite( 0,1 ).linexp( 0,1, rate/rateVar, rate*rateVar ) ),
		Demand.ar( trigger, 0, Dwhite( *pos ) ),
		4,
		Demand.ar( trigger, 0, Dwhite( *pan ) ),
	);
	UMixOut.ar( 0, sig * \level.ukr( 1, \amp ), 0, true );
}, [ 
	[ \soundFile, nil, MonoBufSndFileSpec(nil) ], 
	[ \pos, [0,1], [0,1].asSpec.asRangeSpec ],
	[ \density, 100, [1,10000,\exp,0,100].asSpec ],
	[ \timeVar, 0, [0,4,2].asSpec ],
	[ \rateVar, 0, [0,4,2].asSpec ],
	[ \rateScale, 1, [0.125,8,\exp,0,1].asSpec ],
	[ \overlap, 4, [0.01,100,\exp,0,1].asSpec ],
	[ \pan, [-1,1], [-1,1].asSpec.asRangeSpec ],
	[ \seed, 12345, PositiveIntegerSpec(), false, \init ]
], \soundFile )
