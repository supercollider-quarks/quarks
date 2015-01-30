/*
\mixer

mixes multiple channels together to a single one by summing

amp0 .. ampN: amplitude of each input signal
numChannels: number of channels (*).
	
(*) only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \mixer, { 
	var sig, amps;
	sig = UIn.ar( 0, Udef.numChannels );
	amps = Udef.numChannels.collect({ |i| 
		("amp" ++ i).asSymbol.ukr( 1/Udef.numChannels.sqrt, warp: 'amp' )
	});
	UOut.ar( 0, (sig * amps).sum );
})
	.category_( \utility )