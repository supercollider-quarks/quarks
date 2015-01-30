/*
\duplicateChannel

duplicates a single channel to multiple outputs.

numChannels: number of output channels (*).
	
(*) only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \duplicateChannel, { 
	var amps;
	amps = Udef.numChannels.collect({ |i| 
		("amp" ++ i).asSymbol.ukr( 1, warp: 'amp' )
	});
	UOut.ar( 0, UIn.ar( 0, 1 ).dup( Udef.numChannels ) * amps );
})
	.category_( \utility )