/*
\envelope

An envelope generator. 

env: an Env or EnvM object, containing time and level values. 
timeScale: a scale for the time/duration of the Env (can be modulated)
loop: loopmode (0: off, 1: loop, 2: alternate)
delay: delay time before starting the env
trigger: a trigger that restarts the env

The env arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \envelope, {
	var sig;
	sig = UEnvGen.kr( \env, nil, \timeScale, \loop, \delay, \trigger );
	UMapOut.kr(sig);
}).mappedArgs_([ \env ])
.category_( 'automation' )