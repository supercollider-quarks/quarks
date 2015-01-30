/*
\timer

This UMap measures the elapsed time between triggers. Every time a trigger is received, the UMap will output the time between that and the previous trigger. The value can be scaled from a within a time range to the output range of the UMap.

trigger: the trigger
minTime: minimum time, will be mapped to the lower value of 'range'
maxTime: maximum time, will be mapped to the higher value of 'range'. 
range: the output range to scale the time values to. If minTime > maxTime the range gets inverted.
curve: a curvature value for the mapped range. If curve == 0, the mapping is linear.
clipMode: can be one of the following modes:
	0 - 'clip' (default): clip / limit values outside range
	1 - 'fold': fold / mirror values outside range
	2 - 'wrap': wrap around / modulo values outside range
	3 - 'none': no clipping applied (values can go outside range)
clipSoftness: only used for 'clip' and 'fold' (0-1).

the 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \timer, {
	var sig, fromRange, range, curve, clipMode, clipSoftness;
	sig = Timer.kr( \trigger.tr );
	fromRange = [ \minTime.kr(0), \maxTime.kr(10) ];
	sig = sig.linlin(*fromRange ++ [-1,1,\none]).clip2( 2**24 );
	range = \range.kr([0.0,1.0]);
	curve = \curve.kr(0);
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(-1,1,-1,1, curve, \none ),
		sig
	]);
	clipMode = \clipMode.kr(0);
	clipSoftness = \clipSoftness.kr(0);
	sig = Select.kr( clipMode, [
		sig.softclip2(1, clipSoftness),
		sig.softfold2(1, clipSoftness),
		sig.wrap2(1),
		sig
	]);
	sig = sig.linlin(-1,1,*range ++ [ \none ]);
	UMapOut.kr( sig );
})
.mappedArgs_([ \range ])
.setSpec( \trigger, TriggerSpec() )
.setSpec( \minTime, SMPTESpec() )
.setSpec( \maxTime, SMPTESpec() )
.setSpec( \clipMode, ListSpec( (..3), 0, [ \clip, \fold, \wrap, \none ] ) )
.setSpec( \clipSoftness, [0,1,\lin,0,0.0].asSpec )
.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
.category_( 'trigger_to_value' )