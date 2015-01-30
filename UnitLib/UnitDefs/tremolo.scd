/*
\tremolo

speed: number of tremolo's per second.amount: sets the minimum and maximum of the tremolo effect.smooth: smoothen the modulator signal; 0 means square wave, 1 means sine wave, and everything in between.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \tremolo, { |speed = 1, phase = 0, amount = #[0.0,1.0], smooth = 1|
	var modsig, sig;
	smooth = smooth.max(1.0e-12);
	modsig = ((LFTri.ar( speed, phase * 2 ) / smooth) * 0.5pi).clip(-0.5pi,0.5pi);
	modsig = SinOsc.ar( 0, modsig );
	sig = modsig.range(*amount) * UIn.ar( 0 );
	UOut.ar( 0, sig )
} )
	.category_( \effect )
	.setSpec( \speed, [0, 100, 99.squared.log, 0, 1].asSpec )
	.setSpec( \phase, [-2, 2, \lin, 0, 0, "pi"].asSpec, \init )
	.setSpec( \smooth, [0,1,\lin, 0, 1] );