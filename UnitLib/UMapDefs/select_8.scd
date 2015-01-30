/*
\select_8

Creates an UMap that can select one from 8 values (or UMaps). If you need to select from > 8 values, use \select_16 instead.

index: the index of the value (0-7)
smooth: smoothening parameter (0: hard steps, 1: linear interpolation)
value0 - value7: the values of the steps (can be UMaps)

The 'value[0-7]' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \select_8, {
	var values, smooth;
	var phase, step, resetPos, sig;
	phase = \index.kr(0);
	smooth = \smooth.kr(0.0);
	values = 8.collect({ |i|
		("value"++i).asSymbol.ukr(0.5);
	});
	phase = (phase.floor * (1-smooth)) + (phase * smooth);
	sig = LinSelectX.kr( phase, values );
	UMapOut.kr( sig );
}).category_( \utility )
	.setSpec( \index, [0,7].asSpec )
	.mappedArgs_( 8.collect({ |i| ("value"++i).asSymbol }) );