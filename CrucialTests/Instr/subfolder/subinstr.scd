

Instr([\subfolder,\subinstr,\one],{ arg stereo=0.0,mix=0.0;

	var l,r,center,sides;
	
	# l, r = NumChannels.ar(stereo,2);
	sides = [ l - r, r - l];
	center = stereo - sides;
	
	LinXFade2.ar(center,sides,mix)
},
	nil,
	\stereoEffect
);


// this is improperly named
// should post a warning ?  it can't really
/*Instr([\subfolder,\leaf],{ arg stereo=0.0,mix=0.0;

	var l,r,center,sides;
	
	# l, r = NumChannels.ar(stereo,2);
	sides = [ l - r, r - l];
	center = stereo - sides;
	
	LinXFade2.ar(center,sides,mix)
},
	nil,
	\stereoEffect
);
*/


