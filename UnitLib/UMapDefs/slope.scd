/*
\slope

An UMap that differentiates its input, returning the amount of change per control cycle.

value: the value upon which the lag is applied (this can be another UMap as well)
abs: if true the UMap returns the absolute slope, if false it returns negative and positive slope
range: the range to which the output values are mapped

The 'value' and 'range' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \slope, { |value = 0.0, abs = 0, range = #[0.0,1.0]|
	var slope;
	slope = HPZ1.kr( value.linlin(0,1,-1,1) );
	slope = if( abs, slope.abs.linlin(0,1,-1,1), slope );
	UMapOut.kr( slope.linlin( -1, 1, *range ) );
})
.mappedArgs_([ \value, \range ])
.setSpec( \abs, BoolSpec(false) )
.category_( 'filter' )