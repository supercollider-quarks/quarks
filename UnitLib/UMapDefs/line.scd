/*
\line

An line / ramp generator.

a: start value
b: end value
curve: a curve value for the line. A 0 (zero) curve creates a linear line, a positive curve value makes the line tend towards the start value, and v.v.
duration: duration of the line in seconds (can be modulated)
loop: loopmode (0: off, 1: loop, 2: alternate)
delay: delay time before the line starts
trigger: a trigger that restarts the line

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \line, { |a = 0.0, b = 1.0, curve = 0, duration = 10|
	var sig;
	duration = duration.max( ControlDur.ir + SampleDur.ir );
	sig = ULine.kr( 0, 1, duration, \loop, \delay, \trigger );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0, 1, a, b, curve ),
		sig.linlin(0, 1, a, b )
	]);
	UMapOut.kr(sig);
})
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \duration, SMPTESpec() )
	.category_( 'automation' )
	.mappedArgs_( [ \a, \b ] );