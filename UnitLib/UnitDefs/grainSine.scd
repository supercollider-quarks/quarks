/*
\grainSine

A granular synthesis generator. This will play tiny segments ("grains") of sine waves with optional random dispersion and distribution.

range: the minimum and maximum frequency in between which each grain is randomly chosen
pos: range of position in the file between 0 (start) and 1 (end)
density: the average number of grains per second
timeVar: time variation factor (0-4). 0 means no variation, 4 means times between grains can be within (1/4) and 4 times the original duration
overlap: average number of overlapping grains. The duration of the grains is change to match this.
pan: the pan position range. This is only active if numChannels > 1
amp: the overall amplitude of the generator
seed: random seed
numChannels: number of output channels

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \grainSine, {
	var range, density, timeVar, overlap, pan, sig, trigger, amp;
	range = \range.kr( [440,880] );
	density = \density.kr( 100 );
	timeVar = (\timeVar.kr( 0 ) * 6).midiratio;
	overlap = \overlap.kr(4);
	pan = \pan.kr( [ -1,1 ] );
	amp = \amp.kr( 0.1 );
	URandSeed.ir(\seed.ir(12345));
	trigger = TDuty.ar( Dwhite( 0,1 ).linexp( 0,1, 1/timeVar/density, timeVar/density ) );
	sig = GrainSin.ar( Udef.numChannels, trigger, overlap/density, 
		Demand.ar( trigger, 0, Dwhite( 0,1 ).linexp( 0,1, *range ) ),
		Demand.ar( trigger, 0, Dwhite( *pan ) ),
	) * amp;
	UMixOut.ar( 0, sig, 0, true );
}, [ 
	[ \range, [440,880], FreqSpec(2,20000).asRangeSpec ],
	[ \density, 100, [1,10000,\exp,0,100].asSpec ],
	[ \timeVar, 0, [0,4,2].asSpec ],
	[ \overlap, 4, [0.01,100,\exp,0,1].asSpec ],
	[ \pan, [-1,1], [-1,1].asSpec.asRangeSpec ],
	[ \amp, 0.1, \amp.asSpec ],
	[ \seed, 12345, PositiveIntegerSpec(), false, \init ]
], \synthesis )
