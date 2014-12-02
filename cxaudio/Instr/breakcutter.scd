    
	/*
		cuts between a breakbeat and the same beat offset by x-amount of beats.
		like hip-hop cutting with doubles
	*/

Instr([\breakcutter,\doubles],{ arg sample,which,offset1=0.0,offset2=1.0,
					pitch1=1.0,pitch2=1.0,amp1=1.0,amp2=1.0,tempo=1.0;

	/*
		cuts between a breakbeat and the same beat offset by some beats.
		like hip-hop cutting with doubles
	*/
	var lock;
	lock = sample.pchRatioKr(tempo);
	
	XFader.ar(
		PlayBuf.ar(sample.numChannels,
				sample.bufnumIr,
				pitch1 * lock,
				1.0,
				offset1 * sample.beatsize,
				1).madd(amp1)
		,
		PlayBuf.ar(sample.numChannels,
				sample.bufnumIr,
				pitch2 * lock,
				1.0,
				offset2 * sample.beatsize,
				1).madd(amp2)
		,which)
		
},[
	\sample,
	\which,
	[0.0,4.0],
	[0.0,4.0],
	[-4.0,4.0],
	[-4.0,4.0],
	[0.0,2.0],
	[0.0,2.0],
	\tempo
],
	\audio
);




Instr([\breakcutter,\twosamples,\simple],{ arg sample1,sample2,which,offset1=0.0,offset2=1.0,
				pitch1=1.0,pitch2=1.0,amp1=1.0,amp2=1.0,tempo=1.0;
	// xfade between two breakbeats
	XFader.ar(
		PlayBuf.ar(sample1.numChannels,
				sample1.bufnumIr,
				pitch1 * sample1.pchRatioKr(tempo),
				1.0,
				offset1 * sample1.beatsize,
				1.0).madd(amp1)
		,
		PlayBuf.ar(sample2.numChannels,
				sample2.bufnumIr,
				pitch2 * sample2.pchRatioKr(tempo),
				1.0,
				offset2 * sample2.beatsize,
				1.0).madd(amp2)
		,
		which)
		
},[
	\sample,
	\sample,
	\which,
	[0.0,4.0],
	[0.0,4.0],
	[-4.0,4.0],
	[-4.0,4.0],
	\gain,
	\gain
],
	\audio
);


// off off silence
// samp1 samp2 samp3 : could be same breaks (with sample sharing on)
// samp1 samp1 samp2
// break break fixed-pitch fixed-pitch


Instr([\breakcutter,\threesamples,\simple],{ arg 
	sample1,sample2,sample3,
	which,
	offset1=0.0,offset2=1.0,offset3=1.0,
	pitch1=1.0,pitch2=1.0,pitch3=1.0,
	amp1=1.0,amp2=1.0,amp3=1.0,
	width=2.0,tempo=1.0;

	XFaderN.ar([
		PlayBuf.ar(sample1.numChannels,
				sample1.bufnumIr,
				pitch1 * sample1.pchRatioKr(tempo),
				1.0,
				offset1 * sample1.beatsize,
				1.0).madd(amp1)
		,
		PlayBuf.ar(sample2.numChannels,
				sample2.bufnumIr,
				pitch2 * sample2.pchRatioKr(tempo),
				1.0,
				offset2 * sample2.beatsize,
				1.0).madd(amp2)
		,
				PlayBuf.ar(sample3.numChannels,
				sample3.bufnumIr,
				pitch3 * sample3.pchRatioKr(tempo),
				1.0,
				offset3 * sample3.beatsize,
				1.0).madd(amp3)
		]
		,
		which)
		
},[
	\sample,
	\sample,
	\sample,
	\which,
	NoLagControlSpec(0.0,4.0),
	NoLagControlSpec(0.0,4.0),
	NoLagControlSpec(0.0,4.0),
	[-4.0,4.0],
	[-4.0,4.0],
	[-4.0,4.0],
	\gain,
	\gain,
	\gain,
	[0.0,4.0]
],
	\audio
);

