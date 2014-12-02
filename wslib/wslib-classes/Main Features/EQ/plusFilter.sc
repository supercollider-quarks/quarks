// Sciss 2008
// part of wslib 2009

+ MidEQ {
	// XXX they are not correct XXX
	*coeffs { arg sr, freq = 440, rq = 1.0, db = 0.0;
		var amp, pfreq, pbw, c, d, a0, b1, b2;
		
		amp		= db.dbamp - 1.0;
		pfreq	= freq * 2pi / sr;
		pbw		= rq   * pfreq * 0.5;	// bw == rq?
		
		c		= 1.0 / tan( pbw );
		d		= 2.0 * cos( pfreq );
	
		a0		= 1.0 / (1.0 + c);
		b1		= c * d * a0 ;
		b2		= (1.0 - c) * a0;
		a0		= a0 * amp;

		^[[ a0 ], [ b1, b2 ]]
	}
}

+ HPZ1 {
	* coeffs { ^[[ 0.5, -0.5 ]]}
}

+ HPZ2 {
	* coeffs { ^[[ 0.25, -0.5, 0.25 ]]}
}

+ LPZ1 {
	* coeffs { ^[[ 0.5, 0.5 ]]}
}

+ LPZ2 {
	* coeffs { ^[[ 0.25, 0.5, 0.25 ]]}
}

+ BPZ2 {
	* coeffs { ^[[0.5, 0.0, -0.5]] }
}

+ BRZ2 {
	* coeffs { ^[[0.5, 0.0, 0.5]] }
}


+ OnePole {
	* coeffs { |sr = 44100, coef = 0.5|
		var a0, b0;
		a0 = 1 - abs(coef);
		b0 = coef;
		^[[a0],[b0.neg]]
	}
}

+ OneZero {
	* coeffs { |sr = 44100, coef = 0.5|
		var a0, a1;
		a0 = 1 - abs(coef);
		a1 = coef;
		^[[a0, a1]]		
	}
}

+ TwoPole {
	* coeffs { |sr = 44100, freq = 440, radius = 0.8|
		var b1, b2;
		var pfreq;
		pfreq = freq * 2pi / sr;
		b1 = 2.0 * radius * cos( pfreq );
		b2 = radius.squared.neg;
		^[[ 1.0 ],[b1, b2].neg ];
	}
}

+ TwoZero {
	* coeffs { |sr = 44100, freq = 440, radius = 0.8|
		var b1, b2;
		var pfreq;
		pfreq = freq * 2pi / sr;
		b1 = -2.0 * radius * cos( pfreq );
		b2 = radius.squared;
		^[[ 1.0 ],[b1, b2].neg ];
	}
}

+ Integrator {
	* coeffs { |sr = 44100, coef = 1.0|
		^[[ 1.0 ], [ coef ]] // seems wrong, is it?
	}
}

+ FOS {
	* coeffs { |sr = 44100, a0 = 0.0, a1 = 0.0, b1 = 0.0|
		^[[a0, a1], [b1].neg ]
	}
}

+ SOS {
	* coeffs { |sr = 44100, a0 = 0.0, a1 = 0.0, a2 = 0.0, b1 = 0.0, b2 = 0.0|
		^[[a0, a1, a2], [b1, b2].neg ]
	}
}

+ Filter {
	*coeffs { ^this.subclassResponsibility( thisMethod ) }
	
	*magResponse { arg freqs = 1000, sr = 44100 ... rest;
		var ma, ar, size;
		
		#ma, ar = this.coeffs( sr, *rest );
		size = ma.size.max( ar.size + 1 );
		
		if( freqs.isNumber ) // autoscale 20-22000
			{ freqs = (..freqs).linexp(0,freqs-1, 20, 22000); };
			
		case { size < 4 }
			{ ^this.magResponse2( freqs, sr, ma, ar ) }
			{ size < 7 }
			{ ^this.magResponse5( freqs, sr, ma, ar ) }
			{ ^this.magResponseN( freqs, sr, ma, ar, size ) };
	}

	
	*magResponse2 { arg freqs, sr, ma, ar;
		var pfreq, cos1, cos2, nom, denom;
		var a0, a1, a2, b1, b2;
		var ax0, ax1, ax2, bx0, bx1;
		var radPerSmp; //= 2pi / sr;
		
		sr = sr ?? { Server.default.sampleRate };
		radPerSmp = 2pi / sr;
		
		#a0, a1, a2  = ma ++ #[ 0.0, 0.0, 0.0 ];
		#b1, b2    = ar ++ #[ 0.0, 0.0 ];
	
		ax0 = (a0*a0) + (a1*a1) + (a2*a2);
		ax1 = (a0*a1) + (a1*a2);
		ax2 = a0*a2;
			
		bx0 = 1.0 + (b1*b1) + (b2*b2);
		bx1 = b1 + (b1*b2);
		
		^freqs.collect({ arg freq;
			pfreq = freq * radPerSmp;
			cos1	= cos( pfreq );
			cos2	= cos( pfreq * 2 );
					
			nom = ax0 + (2 * ( ( ax1 * cos1) + ( ax2 * cos2) ));
		
			denom = bx0 + (2 * ( ( bx1 * cos1) + ( b2 * cos2) ));
		
			sqrt( nom / denom );
		});
	}


	*magResponse5 { arg freqs, sr, ma, ar;
		var pfreq, cos1, cos2, cos3, cos4, cos5, nom, denom;
		var a0, a1, a2, a3, a4, a5, b1, b2, b3, b4, b5;
		var ax0, ax1, ax2, ax3, ax4, ax5, bx0, bx1, bx2, bx3, bx4;
		var radPerSmp; //= 2pi / sr;
		
		sr = sr ?? { Server.default.sampleRate };
		radPerSmp = 2pi / sr;

		#a0, a1, a2, a3, a4, a5 = ma ++ #[ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ];
		#b1, b2, b3, b4, b5     = ar ++ #[ 0.0, 0.0, 0.0, 0.0, 0.0 ];
			
		ax0 = (a0*a0) + (a1*a1) + (a2*a2) + (a3*a3) + (a4*a4) + (a5*a5);
		ax1 = (a0*a1) + (a1*a2) + (a2*a3) + (a3*a4) + (a4*a5);
		ax2 = (a0*a2) + (a1*a3) + (a2*a4) + (a3*a5);
		ax3 = (a0*a3) + (a1*a4) + (a2*a5);
		ax4 = (a0*a4) + (a1*a5);
		ax5 = a0*a5;
			
		bx0 = 1.0 + (b1*b1) + (b2*b2) + (b3*b3) + (b4*b4) + (b5*b5);
		bx1 = b1 + (b1*b2) + (b2*b3) + (b3*b4) + (b4*b5);
		bx2 = b2 + (b1*b3) + (b2*b4) + (b3*b5);
		bx3 = b3 + (b1*b4) + (b2*b5);
	 	bx4 = b4 + (b1*b5);
		
		^freqs.collect({ arg freq;
			var complex;
			pfreq = freq * radPerSmp;
			cos1	= cos( pfreq );
			cos2	= cos( pfreq * 2 );
			cos3	= cos( pfreq * 3 );
			cos4	= cos( pfreq * 4 );
			cos5	= cos( pfreq * 5 );
		
			nom = ax0 + (2 * ( ( ax1 * cos1) + ( ax2 * cos2) + ( ax3 * cos3) +
				( ax4 * cos4) + ( ax5 * cos5) ));
		
			denom = bx0 + (2 * ( ( bx1 * cos1) + ( bx2 * cos2) + ( bx3 * cos3) +
		         ( bx4 * cos4) + ( b5 * cos5) ));
		
			sqrt( nom / denom );
		});
	}
	
	*magResponseN { arg freqs, sr, ma, ar, size; // way slower, but can handle higher order
		var radPerSmp; //= 2pi / sr;
		var ax, bx;
		var nom, denom;
		var cosn,  pfreq;
		
		sr = sr ?? { Server.default.sampleRate };
		radPerSmp = 2pi / sr;

		size = size ?? { ma.size.max( ar.size + 1 ) };
		
		ma = ma.extend( size, 0.0 );
		ar = ([1] ++ ar).extend( size, 0.0 );
					
		ax = (size+1).collect({ |i| (ma[i..] *.s ma).sum });
		bx = (size+1).collect({ |i| (ar[i..] *.s ar).sum });
	
		^freqs.collect({ arg freq;
			pfreq = freq * radPerSmp;
			cosn = [0.5] ++ size.collect({ |i| cos( pfreq * (i+1) ) });
				
			nom = (cosn * ax).sum * 2;
			denom = (cosn * bx).sum * 2;
			
			sqrt( nom / denom );
		});
	}
	
	/*
	*magResponse { arg freqs, sr ... rest; // original version
		var pfreq, cos1, cos2, cos3, cos4, cos5, nom, denom;
		var a0, a1, a2, a3, a4, a5, b1, b2, b3, b4, b5;
		var ar, ma;
		var radPerSmp; //= 2pi / sr;
		
		sr = sr ?? { Server.default.sampleRate };
		radPerSmp = 2pi / sr;

		#ma, ar = this.coeffs( sr, *rest );
		#a0, a1, a2, a3, a4, a5 = ma ++ [ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ];
		#b1, b2, b3, b4, b5     = ar ++ [ 0.0, 0.0, 0.0, 0.0, 0.0 ];
		
		if( freqs.isNumber ) // autoscale 20-22000
			{ freqs = (..freqs).linexp(0,freqs-1, 20, 220000); };
		
		^freqs.collect({ arg freq;
			var complex;
			pfreq	= freq * radPerSmp;
			cos1	= cos( pfreq );
			cos2	= cos( pfreq * 2 );
			cos3	= cos( pfreq * 3 );
			cos4	= cos( pfreq * 4 );
			cos5	= cos( pfreq * 5 );
		
			// http://www.musicdsp.org/showone.php?id=108
			nom =   (a0*a0) + (a1*a1) + (a2*a2) + (a3*a3) + (a4*a4) + (a5*a5) + (2 * (
				(((a0*a1) + (a1*a2) + (a2*a3) + (a3*a4) + (a4*a5)) * cos1) +
				(((a0*a2) + (a1*a3) + (a2*a4) + (a3*a5)) * cos2) +
				(((a0*a3) + (a1*a4) + (a2*a5)) * cos3) +
				(((a0*a4) + (a1*a5)) * cos4) +
				  (a0*a5 * cos5)));
		
			denom = 1.0 + (b1*b1) + (b2*b2) + (b3*b3) + (b4*b4) + (b5*b5) + (2 * (
				 ((b1 + (b1*b2) + (b2*b3) + (b3*b4) + (b4*b5)) * cos1) +
				 ((b2 + (b1*b3) + (b2*b4) + (b3*b5)) * cos2) +
		             ((b3 + (b1*b4) + (b2*b5)) * cos3) +
		             ((b4 + (b1*b5)) * cos4) +
		   		  (b5 * cos5)));
		
			sqrt( nom / denom );
		});
	}
	*/
	


	
}