(
~points = all {: Point(6*x,6*y),  x <- [-1,1], y <- [-1,1]};
Udef(\justAPoint,
	{ |point  = #[0,0]| UOut.kr(0, point.asArray) },
	[[ \point: 		0@0,  	WFSPointSpec( 200, 0.1 ) ]]).sendSynthDef(s);
UChain(*([\diskSoundFile, \justAPoint

]++4.collect{|i|[
	U(\simpleReverb)
		.setAudioOut(0,i+1)
		.set(\amp,0.8),
	U(\wfsStaticPlane)
		.setAudioIn(0,i+1)
		.point_(~points[i])
]
}.flat++[\wfsDynamicPoint])).gui
)

UChain().gui

