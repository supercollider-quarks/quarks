

Instr([\stereo,\bal1],{ arg stereo=0.0,mix=0.0;

	var l,r,center,sides;
	
	# l, r = NumChannels.ar(stereo,2);
	sides = [ l - r, r - l];
	center = stereo - sides;
	
	LinXFade2.ar(center,sides,mix)
},
	nil,
	\stereoEffect
);



Instr([\stereo,\bal2],{ arg stereo=0.0,centerLevel=1.0,sidesLevel=1.5;

	var l,r,center,sides;
	
	# l, r = NumChannels.ar(stereo,2);
	sides = [ l - r, r - l ];
	center = stereo - sides;
	if(centerLevel != 1.0,{ center = center * centerLevel });
	
	Mix.new([ center , sides * sidesLevel ])	

},#[
	\stereo,
	\drive,
	\drive
],
	\stereoEffect
);




Instr([\stereo,\bal2CompSides],{ arg stereo=0.0,centerLevel=1.0,sidesLevel=1.5,threshold=0.8,slope=0.5;

	var l,r,center,sides;
	
	# l, r = NumChannels.ar(stereo,2);
	sides = [ l - r, r - l ];
	center = stereo - sides;
	if(centerLevel != 1.0,{ center = center * centerLevel });

	sides = Compander.ar(sides,sides.max(0.0001),threshold,1, slope, 0.01, 0.2);
	Mix.new([ center , sides * sidesLevel ])	

},#[
	\stereo,
	\drive,
	\drive
],
	\stereoEffect
);


Instr([\stereo,\verbSides],{ arg stereo=0.0,centerLevel=1.0,sidesLevel=1.5,decay=1.0;

	var l,r,center,sides;
	
	# l, r = NumChannels.ar(stereo,2);
	sides = [ l - r, r - l ];
	center = stereo - sides;
	
	4.do({ sides = AllpassL.ar(sides, 0.05, [0.05.rand, 0.05.rand], decay) });
	Mix.new([ center * centerLevel, sides * sidesLevel ])	

},#[
	\stereo,
	\drive,
	\drive
],
	\stereoEffect
);


Instr([\stereo,\hardSides],{ arg stereo=0.0,centerLevel=1.0,sidesLevel=1.5,drive=1.0;

	var l,r,center,sides;
	
	# l, r = NumChannels.ar(stereo,2);
	sides = [ l - r, r - l ];
	center = stereo - sides;
	
	sides = HPF.ar(sides,1000);
	//4.do({ sides = AllpassL.ar(sides, 0.01, [0.01.rand, 0.01.rand], 0.05) });
	sides = 	Instr.ar([\filters,\hardShaper],[sides,drive]);
	
	Mix.new([ center * centerLevel, sides * sidesLevel ])	

},#[
	\stereo,
	\drive,
	\drive
],
	\stereoEffect
);



/**

	add chorus or reverb to sides
	remove bass from sides ( mono it and add it back to center)
	remove highs (not mids) from center and chorus to sides
	
**/


Instr([\stereo,\left],{ arg stereo;
	
	stereo.at(0)

},nil,EffectSpec(1,1));

Instr([\stereo,\right],{ arg stereo;
	
	stereo.at(1)

},nil,EffectSpec(1,1));

