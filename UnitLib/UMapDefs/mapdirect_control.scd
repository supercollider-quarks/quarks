/*
\mapdirect_control

Creates an lightweight UMap that listens to a control bus from a previous unit in the chain and routes it directly to the parameter it is connected to. No scaling or clipping is applied, so the user has to make sure that the bus contains data within the allowed input range.

bus: the number of the control bus (usually 0)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \mapdirect_control, { |unit, bus = 0|
	("c" ++ (bus + UIn.firstControlBus)).asSymbol
}, valueIsPrivate: true )
	.valueIsMapped_( false )
	.setSpec( \value, MapSpec() )
	.setSpec( \bus, PositiveIntegerSpec(0,0,31) )
	.category_( 'private' )