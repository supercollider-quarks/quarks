/*
\map_control

Creates an UMap that listens to a control bus from a previous unit in the chain and routes it to the parameter it is connected to, including scaling and clipping

bus: the number of the control bus (usually 0)
fromRange: the input range to listen to (-1 to 1)
toRange: the range to which the output is scaled
curve: a curvature value for toRange. If curve == 0, the range is linear.
invert: if true, the fromRange is inverted (i.e. low values become high and v.v.)
lag: lagging filter time for value changes
clipMode: can be one of the following modes:
	0 - 'clip' (default): clip values outside fromRange
	1 - 'fold': fold values outside fromRange
	2 - 'wrap': wrap around values outside fromRange
	3 - 'none': no clipping applied (values can go outside toRange)
clipSoftness: softens the edges of 'clip' and 'fold' modes.

The 'toRange' arg range is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \map_control, { |bus = 0, fromRange = #[0.0, 1.0], toRange = #[0.0,1.0], curve = 0, invert = 0, 	lag = 0.0, clipMode = 0, clipSoftness = 0.1|
	var sig;
	sig = In.kr(bus + UIn.firstControlBus);
	fromRange[1] = fromRange[1].max(fromRange[0] + 1.0e-8);
	sig = sig.linlin(*fromRange ++ [-1,1,\none]);
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(-1,1,-1,1, curve, \none ),
		sig
	]);
	sig = if( invert, sig.neg, sig );
	sig = Select.kr( clipMode, [
		sig.softclip2(1, clipSoftness),
		sig.softfold2(1, clipSoftness),
		sig.wrap2(1),
		sig
	]);
	sig = sig.lag( lag );
	sig = sig.linlin(-1,1,*toRange ++ [ \none ]);
	UMapOut.kr(sig);
})
	.setSpec( \fromRange, [-1,1].asSpec.asRangeSpec )
	.setSpec( \clipMode, ListSpec( (..3), 0, [ \clip, \fold, \wrap, \none ] ) )
	.setSpec( \clipSoftness, [0,1,\lin,0,0.1].asSpec )
	.setSpec( \invert, BoolSpec(false) )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \bus, PositiveIntegerSpec(0,0,32) )
	.mappedArgs_([ \toRange ])
	.category_(\private)

