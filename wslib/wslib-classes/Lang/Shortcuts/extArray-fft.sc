
+ ArrayedCollection {
	fft {  arg imag = 0;// shortcut for fft
		var size, sig;
		size = this.size.nextPowerOfTwo;
		sig = Signal.newFrom( this.extend( size, 0 ) );
		if( imag.size != size )
			{ imag = imag.asCollection;
			  imag = imag.extend( size, imag[0] );
			  };
		imag = Signal.newFrom( imag );
		^sig.fft( imag, Signal.fftCosTable( size ) );
		}
		
	ifft {  arg imag = 0;// shortcut for fft
		var size, sig;
		size = this.size.nextPowerOfTwo;
		sig = Signal.newFrom( this.extend( size, 0 ) );
		if( imag.size != size )
			{ imag = imag.asCollection;
			  imag = imag.extend( size, imag[0] );
			  };
		imag = Signal.newFrom( imag );
		^sig.ifft( imag, Signal.fftCosTable( size ) );
		}
	
	}
	
+ Complex {
	fft {
	 	case { real.class == Signal }
	 		{  ^real.fft( imag, Signal.fftCosTable( real.size ) ) }
	 		{ real.size > 1 }
	 		{ ^real.fft( imag ); }
	 		{ true }
	 		{ "Complex:fft - real part is not a Signal or Array".postln;  } 
		}
	ifft {
	 	case { real.class == Signal }
	 		{  ^real.ifft( imag, Signal.fftCosTable( real.size ) ) }
	 		{ real.size > 1 }
	 		{ ^real.ifft( imag ); }
	 		{ true }
	 		{ "Complex:fft - real part is not a Signal or Array".postln;  } 
		}
	}