/*
\min

This UMap outputs the lowest (minumum) out of two input values

a: the first value
b: the second value

a and b arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \min, { |a = 0.5, b = 0.5|
	UMapOut.kr( a.min(b) );
})
.mappedArgs_([ \a, \b ])
.category_( 'utility' )