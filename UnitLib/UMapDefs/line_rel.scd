/*
\line_rel

An line generator of which the duration is automatically scaled to that of the event.

a: start value
b: end value
curve: a curve value for the line. A 0 (zero) curve creates a linear line, a positive curve value makes the line tend towards the low value, and v.v.
timeScale: if timeScale is 1, the duration of the line will become exactly that of the event, lower and higher timeScales result in a shorter and longer envelope durations.
loop: loopmode (0: off, 1: loop, 2: alternate)
delay: delay time before the line starts
trigger: a trigger that restarts the line


The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \line_rel, { |a = 0.0, b = 1.0, curve = 0|
	var sig;
	sig = ULineRel.kr( 0, 1, \timeScale, \loop, \delay, \trigger );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0,1, a, b, curve ),
		sig.linlin(0,1, a, b )
	]);
	UMapOut.kr(sig);
})
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \timeScale, [0.25,4,\exp,0,1].asSpec, \init )
	.category_( 'automation' )
	.mappedArgs_( [ \a, \b ] );