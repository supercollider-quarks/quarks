/*
\delay

This UMapDef creates an UMap for delaying value changes. The delay time itself can also be modulated.

value: the value (or UMap) to be delayed
time: the length of the delay.
timeScale: the number of seconds by which 'time' is multiplied. This value can not be changed during playback. 
lag: smoothing time applied to change of delay time, use to prevent sudden jumps during change of delay time.

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/


UMapDef( \delay, { |value = 0.0, time = 0.1, timeScale = 1, lag = 0|
	var delayed;
	time = time.lag3( lag ) * timeScale;
	delayed = DelayC.kr( value, timeScale * 2, time );
	delayed = Select.kr( Line.kr(0,1,time) >= 1, [Latch.kr( value, Impulse.kr(0) ), delayed] );
	UMapOut.kr( delayed )
} )
	.category_( \filter )
	.setSpec( \time, [ 0, 2, \lin, 0, 0.1 ] )
	.setSpec( \timeScale, ListSpec([0.1,1,10],1,["0.1s", "1s", "10s"]), \init )
	.setSpec( \lag, [0,1,\lin].asSpec )
	.mappedArgs_([ \value ]);