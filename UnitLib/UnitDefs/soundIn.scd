/*
\soundIn

Read sound from a hardware audio input. 

Note to WFSCollider users: This will get sound input on both the master and the servers, which is not typically what we want. Use the wfsMasterIn and wfsServerIn Udefs (in the wfs_io category) to make a distinction between these.
bus: the bus that you want to use to read audio from. Starts counting from 0; 0 means the first input of your audio device.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \soundIn, { |bus = 0|
	var input;
	input = SoundIn.ar( bus );
	input = input * if( bus > (NumInputBuses.ir - 1), 0, 1 );
	UOut.ar( 0, input );
} ).setSpec( \bus, PositiveIntegerSpec( 0 ) )
	.category_( \io );