/*
\trigger

This UMap implements a simple trigger signal with a certain duration. For convenience also a linear rise/decay filter, so that users can use this UMap for simple envelope creation

trigger: the trigger
time: trigger duration (if 0, it will be a single frame)
rise: a linear lag time before the value reaches it's maximum
decay: a linear lag time before the value reaches it's minimum after the trigger time has ended
range: the value range (when triggered, the value moves from the lower to the upper limit of the range)

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \trigger, {
	var index, output;
	index = Trig.kr( \trigger.tr, \time.kr(0.0) );
	index = Slew.kr(index, 1 / \rise.kr(0.0), 1 / \decay.kr(0.0) );
	output = LinSelectX.kr( index, \range.kr([0.0,1.0]) );
	UMapOut.kr( output );
})
.mappedArgs_([ \range ])
.setSpec( \trigger, TriggerSpec() )
.category_( 'trigger_utility' )