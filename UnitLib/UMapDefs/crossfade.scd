/*
\crossfade

Creates an UMap for linear crossfading between two values (or other UMaps)

a: the first value
b: the second value
crossfade: (0-1) the crossfading position (a to b)

The 'a' and 'b' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \crossfade, { |a = 0.0, b = 1.0, crossfade = 0.5, lag = 0.0|
	UMapOut.kr( (a * (1-crossfade)) + (b * crossfade) );
})
	.category_( 'utility' )
	.mappedArgs_( [ \a, \b ] );