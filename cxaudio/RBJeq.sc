/*

see also Josh UGens for the same formulas.

sample by sample calculations use these forumula
	where 
		in(i) is the current input sample
		in(i-1) is the last input sample
		out(i - 1) is the last output sample (feeding back: what makes an IIR 'infinite')
		etc.

r b-j:
The most straight forward implementation would be the Direct I form (using Eq 2):

    y[n] = (b0/a0)*x[n] + (b1/a0)*x[n-1] + (b2/a0)*x[n-2]
                        - (a1/a0)*y[n-1] - (a2/a0)*y[n-2]               (Eq 4)

equivalent to  (SOS helpfile):
    	out(i) = (a0 * in(i)) + (a1 * in(i-1)) + (a2 * in(i-2)) 
				+ (b1 * out(i-1)) + (b2 * out(i-2))
*/


/*

peakingEQ:  H(s) = (s^2 + s*(A/Q) + 1) / (s^2 + s/(A*Q) + 1) // analog transfer function

            b0 =   1 + alpha*A
            b1 =  -2*cos
            b2 =   1 - alpha*A
            a0 =   1 + alpha/A
            a1 =  -2*cos
            a2 =   1 - alpha/A
             
 */
 
 
PeakingEQ {
	*ar { arg in, freq=440, rq=0.1, gain=1.0,mul=1.0,add=0.0;
		var b0, b1, b2, a0, a1, a2;
		var omega, sin, cos, alpha, a0n;
		var aovera, atimesa;

		//a = dbgain.dbamp; // gain shouldnt go to 0.0 !
		omega = 2pi * freq/(Server.default.sampleRate ? Server.default.options.sampleRate ? 44000.0); // convert to radian frequency
		sin = omega.sin;
		cos = omega.cos;
		alpha = sin * rq / 2;

		aovera = (alpha/gain);
		atimesa = (alpha*gain);

		a0 = 1 + aovera;
		a1 = -2 * cos;
		a2 = 1 - aovera;

		b0 = 1 + atimesa;
		b1 = a1;
		b2 = 1 - atimesa;

		a0n = a0.neg; // the last two coefs need to be subtracted rather than added

		^SOS.ar( in, b0/a0, b1/a0, b2/a0, a1/a0n, a2/a0n, mul, add )
	}
}

/*
	parametric

	        b0 = (1 + gamma*sqrt(K))/(1 + gamma/sqrt(K))
	        b1 = (-2*cos(omega))/(1 + gamma/sqrt(K))
	        b2 = (1 - gamma*sqrt(K))/(1 + gamma/sqrt(K))
	        a1 = b1
	        a2 = (1 - gamma/sqrt(K))/(1 + gamma/sqrt(K))
	
	where
	        gamma = sqrt[K*(F*F-1)/(K*K-F*F)]*tan(BW/2)
	or
	        gamma = sqrt[K*(F*F-1)/(K*K-F*F)]*sinh[(ln(2)/2)*bw*(omega/sin(omega))]*sin(omega)

 */
 
Parametric {

	*ar {		arg in,freq=440.0,amp,f,bW,mul=1.0,add=0.0;
	
		var gamma,b0,b1,b2,a1,a2,a0,a0n;
		var omega;
		omega = 2pi * freq/(Server.default.sampleRate ? Server.default.options.sampleRate ? 44000.0);
		gamma = sqrt(amp*(f*f-1.0)/((amp*amp)-(f*f))) * tan(bW/2.0);


		b0 = (1.0 + (gamma*sqrt(amp)))/(1.0 + (gamma/sqrt(amp)));
		b1 = (-2.0*cos(omega))/(1.0+(gamma/sqrt(amp)));
		b2 = (1.0 - (gamma*sqrt(amp)))/(1.0 + (gamma/sqrt(amp)));
		a0 = 1.0; // guessing ?
		a1 = b1;
		a2 = (1.0 - (gamma/sqrt(amp)))/(1.0 + (gamma/sqrt(amp)));
		
		a0n = a0.neg;
	
		// not right, no sound
		^SOS.ar( in, b0/a0, b1/a0, b2/a0, a1/a0n, a2/a0n, mul,add )

	}
}

/*
	LPF:        H(s) = 1 / (s^2 + s/Q + 1)

            b0 =  (1 - cos)/2
            b1 =   1 - cos
            b2 =  (1 - cos)/2
            a0 =   1 + alpha
            a1 =  -2*cos
            a2 =   1 - alpha

 // sounds identical to RLPF to me
{ RLPF.ar(Saw.ar(200,0.1), FSinOsc.kr(XLine.kr(0.7,300,20),3600,4000), 0.2) }.play;
{ RbjLPF.ar(Saw.ar(200,0.1), FSinOsc.kr(XLine.kr(0.7,300,20),3600,4000), 0.2.reciprocal) }.play;

*/


RbjLPF {
	*ar {		arg in,freq=440.0,q=0.5,mul=1.0,add=0.0;
	
		var b0,b1,b2,a1,a2,a0,a0n;
		var omega,sin,cos,alpha;
	
		omega = 2pi * freq/(Server.default.sampleRate ? Server.default.options.sampleRate ? 44000.0);
		sin   = sin(omega);
  	 	cos   = cos(omega);
		alpha =   sin/ (2.0 * q );	
		
		b0 = (1.0 - cos) / 2.0;
		b1 = (1.0 - cos);
		b2 = (1.0 - cos) / 2.0;
		a0 = 1.0 + alpha;
		a1 = -2.0 * cos;
		a2 = 1.0 - alpha;

		a0n = a0.neg;
	
		^SOS.ar( in, b0/a0, b1/a0, b2/a0, a1/a0n, a2/a0n, mul, add )
	}
}


/*

highShelf:  H(s) = A * (A*s^2 + (sqrt(A)/Q)*s + 1) / (s^2 + (sqrt(A)/Q)*s + A)

            b0 =    A*[ (A+1) + (A-1)*cos + beta*sin ]
            b1 = -2*A*[ (A-1) + (A+1)*cos            ]
            b2 =    A*[ (A+1) + (A-1)*cos - beta*sin ]
            a0 =        (A+1) - (A-1)*cos + beta*sin
            a1 =    2*[ (A-1) - (A+1)*cos            ]
            a2 =        (A+1) - (A-1)*cos - beta*sin

     _or_ S, a "shelf slope" parameter (for shelving EQ only).  When S = 1, 
        the shelf slope is as steep as it can be and remain monotonically 
        increasing or decreasing gain with frequency.  The shelf slope, in 
        dB/octave, remains proportional to S for all other values.
*/

// see HighShelf in BiquadEQ.sc / JoshUGens
// which is the same thing as here but slightly optimized
// update: missing

HiShelf {

	*ar {		arg in,freq=440.0,gain=1.0,shelf=1.0,mul=1.0,add=0.0;
	
		var b0,b1,b2,a1,a2,a0,a0n;
		var omega,sin,cos,beta;
		
		omega = 2pi * freq/SampleRate.ir;//(Server.default.sampleRate ? Server.default.options.sampleRate ? 44000.0);
		sin   = sin(omega);
  	 	cos   = cos(omega);
		beta = sqrt( (gain.squared + 1)/shelf - ((gain-1.0).squared));
		
           b0 =    gain*( (gain+1) + ((gain-1)*cos) + (beta*sin) );
           b1 = -2*gain*( (gain-1) + ((gain+1)*cos)            );
           b2 =    gain*( (gain+1) + ((gain-1)*cos) - (beta*sin) );
           a0 =        (gain+1) - ((gain-1)*cos) + (beta*sin);
           a1 =    2*( (gain-1) - ((gain+1)*cos )           );
           a2 =        (gain+1) - ((gain-1)*cos) - (beta*sin);

		a0n = a0.neg;
	
		//wrong: a breif attack, then no sound (blew up)
		^SOS.ar( in, b0/a0, b1/a0, b2/a0, a1/a0n, a2/a0n, mul, add )
		// only with shelf = 1e-10 would it work as expected
		// but then it eventually slowly blows up

	}
}

//
//
//HiShelf2 { // using q  rather than shelf
//
//	*ar {		arg in,freq=440.0,gain=1.0,q=1.0,mul=1.0,add=0.0;
//	
//		var b0,b1,b2,a1,a2,a0,a0n;
//		var omega,sin,cos,beta;
//		
//		omega = 2pi * freq/(Server.default.sampleRate ? Server.default.options.sampleRate ? 44000.0);
//		sin   = sin(omega);
//  	 	cos   = cos(omega);
//		beta = sqrt(gain) / q;
//			// precendence errors
//           b0 =    gain*( (gain+1) + (gain-1)*cos + beta*sin );
//           b1 = -2*gain*( (gain-1) + (gain+1)*cos            );
//           b2 =    gain*( (gain+1) + (gain-1)*cos - beta*sin );
//           a0 =        (gain+1) - (gain-1)*cos + beta*sin;
//           a1 =    2*( (gain-1) - (gain+1)*cos            );
//           a2 =        (gain+1) - (gain-1)*cos - beta*sin;
//
//		a0n = a0.neg;
//	
//		//same wrong behavior
//		^SOS.ar( in, b0/a0, b1/a0, b2/a0, a1/a0n, a2/a0n, mul, add )
//	}
//}
//
//




 