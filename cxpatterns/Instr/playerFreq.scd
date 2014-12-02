
/*
	these simple instr convert degree, scale and delta streams 
	into kr rate player whose output is within \freq : 20..10000
*/



Instr("playerFreq.degree",{ arg degreeStream,scaleStream,root=36,durationStream=0.25,lag=0.05;

	StreamKrDur( (degreeStream +  root).degreeToKey(scaleStream).midicps,
				durationStream,
				lag)
},nil,
	\playerFreq
);

Instr("playerFreq.freqStreamKrDur",{ arg freqStream,durationStream=0.25,lag=0.05;

	StreamKrDur( freqStream.midicps,
				durationStream,
				lag)
},nil,
	\playerFreq
);
