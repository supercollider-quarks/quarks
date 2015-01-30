/*
\convolution

A realtime convolution udef, that uses partioned convolution, useful for creating reverbs. This udef requires soundfiles in a special format, usually with a .partconv extension. These files can be created via the gui interface in the 'operations' menu; convert ir file (converts an existing impulse response audio file) and generate danstowell (creates a new random impulse response based on an algorithm by Dan Stowell. Convolution currently supports only mono impulse responses. For multichannel use you need to create multiple convolution units in your chain.


file: a PartConvBuffer object; points to a .partconv file on disk.
wet: wet (processed sound) level
dry: dry level
	
--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

(
Udef( \convolution, { |file, wet = 0.3, dry = 1|
	var sig, convolved;
	sig = UIn.ar(0,1);
	convolved = PartConv.ar( sig, 2048, file );
	UOut.ar( 0, (sig * dry) + (convolved * wet) );
})
	.category_( \effect )
	.setSpec( \file, PartConvBufferSpec() )
	.setSpec( \dry, \amp.asSpec )
	.setSpec( \wet, \amp.asSpec )
)

