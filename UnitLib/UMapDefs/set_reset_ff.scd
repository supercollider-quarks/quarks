/*
\set_reset_ff

This UMap implements a flip-flop switch mechanism. 

trigger: the set trigger
reset: the reset trigger
lag: a linear lag time between value1 and value2 in seconds
value1: the first value (or UMap)
value2: the second value (or UMap)

value1 and value2 arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \set_reset_ff, {
	var index, output;
	index = SetResetFF.kr( \trigger.tr, \reset.tr );
	index = Slew.kr(index, 1 / \lag.kr(0.0), 1 / \lag.kr );
	output = Select.kr( index, [ \value1.kr(0.0), \value2.kr(1.0) ] );
	UMapOut.kr( output );
})
.mappedArgs_([ \value1, \value2 ])
.setSpec( \trigger, TriggerSpec() )
.setSpec( \reset, TriggerSpec() )
.category_( 'trigger_to_value' )