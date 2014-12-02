

+ Function {
	
	integrate { |a, b, d = 1|
		^(a, a+d .. b).mean(this.value(_))
	}
	
	// Simpson's rule
	
	integrateSimp { |a, b|
		^absdif(a, b) / 6.0 * (this.(a) + this.(b) + (4.0 * this.(a + b / 2)))
	}
	
	
	// William M. McKeeman: Algorithm 145: Adaptive numerical integration by Simpson's rule. 
	// Commun. ACM 5(12): 604 (1962).
	// better methods exist (e.g. Gaussian quadrature), but this is simple
	// eventually it may be worthwhile to implement a primitive based on 	// the GNU Scientific Library
	
	integrateSimpA { |a, b, eps = 1e-10, sum|
		var diff;
		var c = (a + b) * 0.5;
		var left = this.integrateSimp(a, c);
		var right = this.integrateSimp(c, b);
		sum = sum ?? { this.integrateSimp(a, b) };
		diff = left + right - sum;
		^if(abs(diff) <= (15.0 * eps)) {
			left + right + (diff / 15.0)
		} {
			eps = eps * 0.5;
			this.integrateSimpA(a, c, eps, left)
			+
			this.integrateSimpA(c, b, eps, right)
		}
	}
	
}
	