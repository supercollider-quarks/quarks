/*
\freeverb

a simple reverb.

mix: wet/dry mix (0-1)
room: room size (0-1)
damp: damp (0-1) (0 means no damping)
*/

Udef( \freeverb, { |mix = 0.33, room = 0.5, damp = 0.5|
	var in, delayed;
	in = UIn.ar( 0, 1 );
	in = FreeVerb.ar( in, mix, room, damp );
	UOut.ar( 0, in )
} )
	.category_( \effect )
	.setSpec( \mix, [ 0, 1, \lin, 0, 0.33 ] )
	.setSpec( \room, [ 0.1, 1.0, \lin, 0, 0.5 ] )
	.setSpec( \damp, [0,1,\lin,0,0.5] );	