/*
\function

Creates an UMap that evaluates a SuperCollider function every time the UChain is started

function: the function. The result of the function should be a value between 0 and 1
value: the result of the function (for display only)

The 'value' arg range is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \function, { |unit, function|
	function.value( unit ) ? 0;
})
	.setDefault( \function, { |unit| 
	// result shoud be between 0 and 1
	// and will be mapped to the output spec
	1.0.rand;
} )
	.setSpec( \function, CodeSpec() )
	.category_( 'function' )