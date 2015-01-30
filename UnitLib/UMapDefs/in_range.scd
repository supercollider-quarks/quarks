/*
\in_range

This UMap can switch between two values, depending on an incoming value. If the value is in between the lower and higher value of the range, the result is 'true', otherwise it is 'false'. All parameters can be UMaps.

value: the value to test (0-1)
range: the range (explanation above)
trueValue: the value (or UMap) to output when the result is true
falseValue: the value (or UMap) to output when the result is false

trueValue and falseValue arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \in_range, { |value = 0.0, range = #[0.25,0.75], trueValue = 1.0, falseValue = 0.0|
	var index, output;
	index = InRange.kr( value, *range );
	output = Select.kr( index, [ falseValue, trueValue ] );
	UMapOut.kr( output );
})
.mappedArgs_([ \trueValue, \falseValue ])
.category_( 'trigger_test' )