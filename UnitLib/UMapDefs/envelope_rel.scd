/*
\envelope_rel

An envelope generator of which the duration is automatically scaled to that of the event.

env: an Env or EnvM object, containing time and level values. 
timeScale: if timeScale is 1, the duration of the envelope will become exactly that of the event, lower and higher timeScales result in a shorter and longer envelope durations.
loop: loopmode (0: off, 1: loop, 2: alternate)
delay: delay time before starting the env (not affected by timeScale)
trigger: a trigger that restarts the env

The env arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \envelope_rel, {
	var sig;
	sig = UEnvGenRel.kr( \env, nil, \timeScale, \loop, \delay, \trigger );
	UMapOut.kr(sig);
}).mappedArgs_([ \env ])
.category_( 'automation' )