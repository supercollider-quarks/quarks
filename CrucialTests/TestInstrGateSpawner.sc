/*
with a  Patch that builds a stream

g = InstrGateSpawner("filterEvent.rlpfenv",[
	Patch("klankperc.impulse.thum2",[
		BeatClockPlayer(
			4.0, 1.0
		),
		1.0,
		0.0,
		1.0,
		0.1
	]),
	Patch("freqStream.streamByDegree",[
		6.0,
		ArrayStreamSeq( StaticSpec(-100, 100, 'linear', 0.0, -100, ""),
			[ -81.741642951965, -10.38761138916, 16.922473907471, -61.227345466614, -33.356475830078, -16.014933586121, 17.344117164612, -79.472947120667, -25.517272949219, 3.9153575897217, 8.9343786239624, -50.024485588074, -74.807715415955, 33.483028411865, 56.256437301636, 61.595940589905 ], inf
		)
	]).value,
	PseqLive([ 0.0019942650016631],inf),
	0.90902054309845,
	Patch("pigs.ampStep",[
		1.0,
		4.0
	]).value,
	Patch("deltaStream.geomHesitate",[
		-4,
		9,
		0,
		3,
		3
	]).value,
	 2.3679300785065
],1.0);

g.play



(

g = InstrGateSpawner("filterEvent.rlpfenv",[
	Patch("klankperc.impulse.thum2",[
		BeatClockPlayer(
			4.0, 1.0
		),
		1.0,
		0.0,
		1.0,
		0.1
	]),
	Patch("freqStream.streamByDegree",[
		6.0,
		ArrayStreamSeq( StaticSpec(-100, 100, 'linear', 0.0, -100, ""),
			[ -81.741642951965, -10.38761138916, 16.922473907471, -61.227345466614, -33.356475830078, -16.014933586121, 17.344117164612, -79.472947120667, -25.517272949219, 3.9153575897217, 8.9343786239624, -50.024485588074, -74.807715415955, 33.483028411865, 56.256437301636, 61.595940589905 ], inf
		)
	]).value,
	PseqLive([ 0.0019942650016631],inf),
	0.90902054309845,
	Patch("pigs.ampStep",[
		1.0,
		4.0
	]).value,
	Patch("deltaStream.geomHesitate",[
		-4,
		9,
		0,
		3,
		3
	]).value,
	 2.3679300785065
],1.0);

g.play


)
tried to load the PseqLive's def

why ?
*/
