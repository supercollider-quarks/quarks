/*
\schmidt

This UMap can switch between two values, depending on an incoming value. If the value passes the upper range boundary, the output will become 'true'. If there-after the value passes the lower range value, the output will become 'false'. The range between the upper and lower value is known as "hysteresis". If the lower and upper value of the range are the same, the UMap will behave as a simple treshold comparator. All parameters can be UMaps.

value: the value to test (0-1)
range: the Schmidt range (explanation above)
trueValue: the value (or UMap) to output when the result is true
falseValue: the value (or UMap) to output when the result is false

trueValue and falseValue arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \schmidt, { |value = 0.0, range = #[0.25,0.75], trueValue = 1.0, falseValue = 0.0|
	var index, output;
	index = Schmidt.kr( value, *range );
	output = Select.kr( index, [ falseValue, trueValue ] );
	UMapOut.kr( output );
})
.mappedArgs_([ \trueValue, \falseValue ])
.category_( 'trigger_test' )