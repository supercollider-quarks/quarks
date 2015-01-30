/*
\greater_than

A comparator. This UMap can switch between two values, depending on two incoming values; a and b. If a > b the UMap outputs trueValue, otherwise it outputs falseValue. All values can be UMaps.

a: the first value
b: the second value
trueValue: the value (or UMap) to output when  a > b
falseValue: the value (or UMap) to output when a <= b

trueValue and falseValue arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \greater_than, { |a = 0.0, b = 0.5, trueValue = 1.0, falseValue = 0.0|
	var index, output;
	index = a > b;
	output = Select.kr( index, [ falseValue, trueValue ] );
	UMapOut.kr( output );
})
.mappedArgs_([ \trueValue, \falseValue ])
.category_( 'trigger_test' )