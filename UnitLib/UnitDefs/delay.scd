/*
\delay

time: the length of the delay.
timeScale: the number of seconds by which 'time' is multiplied. This value can not be changed during playback. 
dry: level of the dry signal.
amp: level of the delayed signal.
lag: smoothing time applied to change of delay time, use to prevent clicks during change of delay time.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/


Udef( \delay, { |time = 0.1, timeScale = 1, dry = 1, amp = 0.2, lag = 0|
	var in, delayed;
	in = UIn.ar( 0, 1 );
	delayed = DelayC.ar( in, timeScale * 2, time.lag3( lag ) * timeScale, amp );
	UOut.ar( 0, delayed + ( in * dry ) )
} )
	.category_( \effect )
	.setSpec( \time, [ 0, 2, \lin, 0, 0.1 ] )
	.setSpec( \timeScale, ListSpec([0.1,1,10],1,["0.1s", "1s", "10s"]), \init )
	.setSpec( \dry, [0,1,\lin,0], \normal )
	.setSpec( \lag, [0,1,\lin].asSpec )
	.setSpecMode( \amp, \normal );
	