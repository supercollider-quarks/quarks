/*
\pulse_divider

This UMap implements a pulse divider. The incoming trigger pulses are counted and every 'division' times one is passed through.

trigger: the trigger
division: number of pulses to divide
offset: start offset for pulse division (0 means first trigger always gets through)
time: the time to hold the trigger. If a new trigger occurs within this time it is ignored.
range: the output range; a passed through trigger makes the output jump from the range minimum to maximum.

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \pulse_divider, {
	var index, output;
	index = \trigger.tr;
	index = PulseDivider.kr( index, \division.kr(2), \offset.ir(0) );
	index = Trig.kr( index, \time.kr(0.0) );
	output = LinSelectX.kr( index, \range.kr([0.0,1.0]) );
	UMapOut.kr( output );
})
.mappedArgs_([ \range ])
.setSpec( \division, IntegerSpec(2,1,inf) )
.setSpec( \offset, IntegerSpec(0,0,inf), \init )
.setDefault( \offset, 0 )
.setSpec( \trigger, TriggerSpec() )
.category_( 'trigger_utility' )