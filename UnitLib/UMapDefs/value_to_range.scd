/*
\value_to_range

Creates an UMap intended for use on modulatable range parameters. The UMap derrives a value range by taking the peak and the lowest value from the input value over time.

value: the input value (or UMap)
decay: (0-1) a decay parameter for the (low)peak detection. 0 means no decay, 1 means infinite decay

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \value_to_range, { |value = 0.5, decay = 0.999|
	var range;
	range = [  
		1 - PeakFollower.kr( 1 - value, decay ),
		PeakFollower.kr( value, decay )
	];
	UMapOut.kr(range);
})
	.mappedArgs_( [ \value ] )
	.setSpec( \decay, [0,1,-8].asSpec )
	.category_( 'range' );
