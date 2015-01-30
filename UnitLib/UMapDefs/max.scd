/*
\max

This UMap outputs the highest (maximum) out of two input values

a: the first value
b: the second value

a and b arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \max, { |a = 0.5, b = 0.5|
	UMapOut.kr( a.max(b) );
})
.mappedArgs_([ \a, \b ])
.category_( 'utility' )