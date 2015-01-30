/*
\x_y

Creates an UMap intended for use on modulatable point parameters. It converts the point into two linear controls for x and y, which on their turn can be used to assign other UMaps to.

x: the x value
y: the y value

The 'x' and 'y' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \x_y, { |x = 0.5, y = 0.5|
	UMapOut.kr([x,y]);
})
	.mappedArgs_( [ \x, \y ] )
	.category_( 'point_convert' );
