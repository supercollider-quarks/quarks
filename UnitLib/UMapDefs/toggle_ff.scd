/*
\toggle_ff

This UMap implements a toggle flip-flop mechanism. Each time a trigger is received the UMap switches to the other value.

trigger: the trigger
lag: a linear lag time between value1 and value2 in seconds
value1: the first value (or UMap)
value2: the second value (or UMap)

value1 and value2 arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \toggle_ff, {
	var index, output;
	index = ToggleFF.kr( \trigger.tr );
	index = Slew.kr(index, 1 / \lag.kr(0.0), 1 / \lag.kr );
	output = LinSelectX.kr( index, [ \value1.kr(0.0), \value2.kr(1.0) ] );
	UMapOut.kr( output );
})
.mappedArgs_([ \value1, \value2 ])
.setSpec( \trigger, TriggerSpec() )
.category_( 'trigger_to_value' )