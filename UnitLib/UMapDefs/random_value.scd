/*
\random_value

Creates an UMap that generates a new random value each time it is started. Typical use for this would be at the 'seed' parameter of any random-generator unit. This UMap can also be used on 'init' mode parameters.

range: the range in between which the value will be created
value: the output value (can only be changed by the UMap itself)

The 'value' and 'range' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \random_value, { |unit, range = #[0.0,1.0]|
	range[0] rrand: range[1]; // result sets \value arg
}).mappedArgs_([\range]).category_( 'function' )