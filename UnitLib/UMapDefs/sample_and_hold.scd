/*
\sample_and_hold

This UMap implements sample-and-hold process. Every time a trigger is received the output value becomes the input value of that moment.

value: the value or UMap to sample
trigger: the trigger
time: a linear lag time to move to the new value

the value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \sample_and_hold, {
	var value, trigger, output;
	value = \value.kr(0.0);
	trigger = \trigger.tr;
	output = Select.kr( Peak.kr( trigger ), [ DC.kr( value ), Latch.kr( value, trigger ) ] );
	output = output.varlag( \time.kr(0.0) );
	UMapOut.kr( output );
})
.mappedArgs_([ \value ])
.setSpec( \trigger, TriggerSpec() )
.category_( 'trigger_to_value' )