/*
\peak

Creates an UMap for following the highest or lowest peak from the incoming value (or UMap)

value: the input value (or UMap)
decay: (0-1) a decay parameter for the (low)peak detection. 0 means no decay, 1 means infinite decay
direction: (-1 - 1) if 1 (default) the UMap will follow output the peak / maximum value, if -1 the UMap will follow the bottom / minimum value. Values in between will interpolate between these two.

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \peak, { |value = 0.5, decay = 0.999, direction = 1.0|
	var peak;
	peak =  LinXFade2.kr( 
		1 - PeakFollower.kr( 1 - value, decay ), 
		PeakFollower.kr( value, decay ), 
		direction
	);
	UMapOut.kr(peak);
})
	.mappedArgs_( [ \value ] )
	.setSpec( \decay, [0,1,-8].asSpec )
	.setSpec( \direction, [-1,1].asSpec )
	.category_( 'utility' );
