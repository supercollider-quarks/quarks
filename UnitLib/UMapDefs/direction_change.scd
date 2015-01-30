/*
\direction_change

This UMap can switch between two values, depending on an incoming value. If the value is changing upwards (getting higher) the result is 'up', if the value is changing downwards it becomes 'down'. If the value doesn't change, the current state is kept. All parameters can be UMaps.

value: the value to test (0-1)
upValue: the value (or UMap) to output when the value changes upwards
downValue: the value (or UMap) to output when the value changes downwards

upValue and downValue arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \direction_change, { |value = 0.0, upValue = 1.0, downValue = 0.0|
	var index, output;
	index = Schmidt.kr( HPZ1.kr( value ), 0, 0 );
	output = Select.kr( index, [ downValue, upValue ] );
	UMapOut.kr( output );
})
.mappedArgs_([ \upValue, \downValue ])
.category_( 'trigger_test' )