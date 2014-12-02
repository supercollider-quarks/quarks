
Instr([\trigPlayer,\Stream2Trig],{ arg deltaStream, amplitudeStream=1.0;

	Stream2Trig(amplitudeStream, deltaStream)
	
},nil,\trigPlayer);


Instr([\trigPlayer,\BeatClockPlayer],{ arg tempoFactor=2.0;

	BeatClockPlayer(tempoFactor)

},[
	StaticSpec(1.0,8.0,\linear,1)
],\trigPlayer);


