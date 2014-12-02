
+ BLowPass {
	
	// wslib 2009 - plot support for BEQSuite
	// copied and modified from .sc methods because they use SampleRate and SampleDur
	// could use optimization ?
	
	*coeffs { arg sr = 44100, freq = 1200.0, rq = 1.0;
		var w0, cos_w0, i, alpha, a0, a1, b0rz, b1, b2;
		w0 = (pi * 2 * freq) / sr;
		cos_w0 = w0.cos; i = 1 - cos_w0;
		alpha = w0.sin * 0.5 * rq;
		b0rz = (1 + alpha).reciprocal;	
		a0 = i * 0.5 * b0rz;
		a1 = i * b0rz;
		b1 = cos_w0 * 2 * b0rz;
		b2 = (1 - alpha) * b0rz.neg;	
		^[[a0, a1, a0], [b1, b2].neg ];
	}
}

+ BHiPass {

	*coeffs { arg sr = 44100, freq = 1200.0, rq = 1.0;
		var i, w0, cos_w0, alpha, a0, a1, b0rz, b1, b2;
		w0 =  (pi * 2 * freq) / sr;
		cos_w0 = w0.cos; i = 1 + cos_w0;
		alpha = w0.sin * 0.5 * rq;
		b0rz = (1 + alpha).reciprocal;		
		a0 = i * 0.5 * b0rz;
		a1 = i.neg * b0rz;
		b1 = cos_w0 * 2 * b0rz;
		b2 = (1 - alpha) * b0rz.neg;	
		^[[a0, a1, a0], [b1, b2].neg];
	}
}

+ BAllPass {

	*coeffs { arg sr = 44100, freq = 1200.0, rq = 1.0;
		var w0, alpha, a0, b1, b0rz;
		w0 = (pi * 2 * freq) / sr;
		alpha = w0.sin * 0.5 * rq;
		b0rz = (1 + alpha).reciprocal;
		a0 = (1 - alpha) * b0rz;
		b1 = 2.0 * w0.cos * b0rz;
		^[[a0, b1.neg, 1.0], [b1.neg, a0]];
	}
}

+ BBandPass {
	
	*coeffs  { arg sr = 44100, freq = 1200.0, bw = 1.0;
		var w0, sin_w0, alpha, a0, b0rz, b1, b2;
		w0 = (pi * 2 * freq) / sr;
		sin_w0 = w0.sin;
	//	alpha = w0.sin * 0.5 * rq;
		alpha = sin_w0 * sinh(0.34657359027997 * bw * w0 / sin_w0);
		b0rz = (1 + alpha).reciprocal;
		a0 = alpha * b0rz;
		b1 = w0.cos * 2 * b0rz;
		b2 = (1 - alpha) * b0rz.neg;	
		^[[a0, 0.0, a0.neg], [b1, b2].neg];
	}
}

+ BBandStop {
	
	*coeffs  { arg sr = 44100, freq = 1200.0, bw = 1.0;
		var w0, sin_w0, alpha, b1, b2, b0rz;
		w0 = (pi * 2 * freq) / sr;
		sin_w0 = w0.sin;
	//	alpha = w0.sin * 0.5 * rq;
		alpha = sin_w0 * sinh(0.34657359027997 * bw * w0 / sin_w0);
		b0rz = (1 + alpha).reciprocal;
		b1 = 2.0 * w0.cos * b0rz;
		b2 = (1 - alpha) * b0rz.neg;	
		^[[b0rz, b1.neg, b0rz], [b1, b2].neg];
	}
}

+ BPeakEQ {

	*coeffs  { arg sr = 44100, freq = 1200.0, rq = 1.0, db = 0.0;
		var a, w0, alpha, a0, a2, b1, b2, b0rz;
		a = pow(10, db/40);
		w0 = (pi * 2 * freq) / sr;
		alpha = w0.sin * 0.5 * rq;
		b0rz = (1 + (alpha / a)).reciprocal;
		a0 = (1 + (alpha * a)) * b0rz;
		a2 = (1 - (alpha * a)) * b0rz;
		b1 = 2.0 * w0.cos * b0rz;
		b2 = (1 - (alpha / a)) * b0rz.neg;
		^[[a0, b1.neg, a2], [b1, b2].neg];
	}
}

+ BLowShelf {
	
	*coeffs  { arg sr = 44100, freq = 120.0, rs = 1.0, db = 0.0;
		var a, w0, sin_w0, cos_w0, alpha, i, j, k, a0, a1, a2, b0rz, b1, b2;
		a = pow(10, db/40);
		w0 = (pi * 2 * freq) / sr;
		cos_w0 = w0.cos;
		sin_w0 = w0.sin;
		alpha = sin_w0 * 0.5 * sqrt((a + a.reciprocal) * (rs - 1) + 2.0);
		i = (a+1) * cos_w0;
		j = (a-1) * cos_w0;
		k = 2 * sqrt(a) * alpha;
		b0rz = ((a+1) + j + k).reciprocal;
		a0 = a * ((a+1) - j + k) * b0rz;
		a1 = 2 * a * ((a-1) - i) * b0rz;
		a2 = a * ((a+1) - j - k) * b0rz;
		b1 = 2.0 * ((a-1) + i) * b0rz;
		b2 = ((a+1) + j - k) * b0rz.neg;
		^[[a0, a1, a2], [b1, b2].neg];
	}
}

+ BHiShelf {

	*coeffs  { arg sr = 44100, freq = 120.0, rs = 1.0, db = 0.0;
		var a, w0, sin_w0, cos_w0, alpha, i, j, k, a0, a1, a2, b0rz, b1, b2;
		a = pow(10, db/40);
		w0 = (pi * 2 * freq) / sr;
		cos_w0 = w0.cos; 
		sin_w0 = w0.sin;
		alpha = sin_w0 * 0.5 * sqrt((a + a.reciprocal) * (rs - 1) + 2.0);
		i = (a+1) * cos_w0; 
		j = (a-1) * cos_w0;
		k = 2 * sqrt(a) * alpha;
		b0rz = ((a+1) - j + k).reciprocal;
		a0 = a * ((a+1) + j + k) * b0rz;
		a1 = -2.0 * a * ((a-1) + i) * b0rz;
		a2 = a * ((a+1) + j - k) * b0rz;
		b1 = -2.0 * ((a-1) - i) * b0rz;
		b2 = ((a+1) - j - k) * b0rz.neg;
		^[[a0, a1, a2], [b1, b2].neg];
	}
}
