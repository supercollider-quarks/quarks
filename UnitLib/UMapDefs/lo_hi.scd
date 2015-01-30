/*
\lo_hi

Creates an UMap intended for use on modulatable range parameters. It converts the range into two linear controls for lo and hi, which on their turn can be used to assign other UMaps to.

lo: the lower value
hi: the higher value

The 'lo' and 'hi' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \lo_hi, { |lo = 0.0, hi = 1.0|
	UMapOut.kr([lo,hi]);
})
	.mappedArgs_( [ \lo, \hi ] )
	.category_( 'range' );
