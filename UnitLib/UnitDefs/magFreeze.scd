/*
\magFreeze
	
An FFT-based (spectral) Udef that is able to freeze the magnitude of an incoming audio signal. The freeze parameter (named 'hold') can be modulated. There is also the option to convolve the signal with noise, effectively randomizing the phase information.

fftSize: (512, 1024, 2048 or 4096) the window size of the FFT. Smaller windows result in faster response but more side effects
hold: (0-1/false-true) freeze magnitudes when true/1
amp: amplitude of output signal
noise: the amount of convolved noise signal
seed: random seed of the noise

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \magFreeze, { |fftSize = 4096, hold = 0, amp = 0.1, noise = 0.0, seed = 12345|
	var sig, fft1, fft2, conv, normal, delay;
	URandSeed.ir( seed );
	sig = UIn.ar( 0, Udef.numChannels );
	fft1 = FFT( LocalBuf(fftSize).clear, sig );
	fft2 = FFT( LocalBuf(fftSize).clear, WhiteNoise.ar(0.05) );
	fft1 = PV_MagFreeze( fft1, hold.round(1));
	fft2 = PV_MagMul( fft2, fft1 );
	conv = IFFT.ar( fft2 ) * 1;
	normal = IFFT.ar( fft1 ) * 1;
	sig = XFade2.ar( normal, conv, noise.linlin(0,1,-1,1) );
	UOut.ar( 0, sig * amp );
})	
	.setSpec( \hold, BoolSpec(false) )
	.setSpec( \seed, PositiveIntegerSpec(), \init )
	.setSpec( \fftSize, ListSpec([512,1024,2048,4096]), \init)
	.category_( \effect );