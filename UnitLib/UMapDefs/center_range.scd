/*
\center_range

Creates an UMap intended for use on modulatable range parameters. It converts the range into two linear controls: center and range, which on their turn can be used to assign other UMaps to.

center: the center of the range
range: the width of the range (0-1)

The 'center' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \center_range, { |center = 0.5, range = 1.0|
	var lo, hi, halfRange;
	halfRange = range/2;
	center = center.linlin( 0, 1, halfRange, 1-halfRange );
	lo = center - halfRange;
	hi = center + halfRange;
	UMapOut.kr([lo,hi]);
})
	.mappedArgs_( [ \center ] )
	.category_( 'range' );
