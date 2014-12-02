
// trig inputs, gate outputs

Instr([\gateTrigs,\Decay2],{ arg trig,attack=0.05,decay=0.2;

	var out;
	out = Decay2.kr(trig,attack,decay);

	out
	
},[
	nil,nil,nil
],\gate);




Instr([\gateTrigs,\extend],{ arg trig,duration=0.2;

	Trig.kr(trig,duration)
	
},nil,\gate);


Instr([\gateTrigs,\invert],{ arg trig;

	1.0 - trig
	
},nil,\gate);


Instr([\gateTrigs,\ToggleFF],{ arg trig;
	
	ToggleFF.kr(trig)
	
},nil,\gate);

Instr([\gateTrigs,\SetResetFF],{ arg trig,reset;
	// holds trig value until it gets a reset
	// then waits for trig again
	SetResetFF.kr(trig,reset);
	
},[
	\trig,
	\trig
],\gate);

Instr([\gateTrigs,\dustySetResetFF],{ arg settiness=5.0,resettiness=5.0;
	var a,b;
	a = Dust.kr(settiness); // the set trigger
	b = Dust.kr(resettiness); // the reset trigger
	SetResetFF.kr(a,b)
},[
	\density,
	\density
],\gate);


Instr([\gateTrigs,\delayedTimer],{ arg trig;

	// every trigger is timed, sets the duration of the next gate
			
	Trig.kr( trig, Timer.kr(trig ) )
	
},nil,\gate);



//Instr([\gateTrigs,\peakSensor],{ arg bipolar,tempoFactor=4,tempo;
//	
//	var sensor;
//	sensor = Latch.kr(bipolar,
//		Impulse.kr(tempo * (tempoFactor * 0.25)) 
//		//Impulse.kr(10)
//		);
//		
//	//(Integrator.kr(sensor,0.6) > 0.0) * sensor
//	sensor > 0.0
//},nil,\gate);




