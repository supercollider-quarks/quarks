/*
\shared_out

** this UMapDef should be used in conjunction with 'shared_in' **

The shared_out UMapDef creates an UMap that is able to share a value with other UMaps used further in the chain. This is useful when multiple units or unit parameters need to use the same value, or derrive things from it. The shared_out would always need to be first in the order of the chain; it sends the value to a bus. To retreive the value in an other unit or parameter use the 'shared_in' UMapDef, and make sure the 'id' setting is the same on both. The 'shared_in' UMapDef will automatically map the value to the range of the parameter it is connected to, which means it is possible to use for example the 'freq' value of one unit to influence the 'amp' value of another (or the same) unit. This will only work _within_ a single chain.

value: the value to be shared (can be an UMap)
id: the id (0-99) by which the point can be retreived by 'shared_in'
range: the range of the value to be shared.

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \shared_out, { |value = 0.0, id = 0, range = #[0,1]|
	var sig;
	sig = value;
	range[1] = range[1].max(range[0] + 1.0e-8);
	sig = sig.linlin(*range ++ [0,1,\none]);
	ReplaceOut.kr(id + 1400, sig);
	UMapOut.kr(value);
})
	.mappedArgs_([ \value, \range ])
	.setSpec( \id, PositiveIntegerSpec( 0, 0, 99 ) )
	.category_( 'shared_io' );
