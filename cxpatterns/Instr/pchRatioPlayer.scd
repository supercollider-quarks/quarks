
//Instr([\pchRatio,\degree],{ arg degreeStream,scaleStream;
//
//	(degreeStream - 48).degreeToKey(scaleStream).midiratio
//	
//},nil,
//	\pchRatioStream
//);


/*
	kr rate players whose output is within pchRatio
	for use in sampler modulations.
	
	this is a gateway instrument, allowing the entire degree and scale library to
	be used with samplers
*/

Instr([\pchRatio,\StreamKrDur],{ arg pchRatioStream,durationStream=0.25,lag=0.05;

	StreamKrDur( pchRatioStream,
				durationStream,
				lag)
},nil,
	PlayerSpec(\pchRatio)
);

Instr([\pchRatio,\degreePlayer],{ arg degreeStream,scaleStream,durationStream=0.25,lag=0.05;

	StreamKrDur( (degreeStream - 48).degreeToKey(scaleStream).midiratio,
				durationStream,
				lag)
},nil,
	PlayerSpec(\pchRatio)
);

