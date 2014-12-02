/* Code adapted from the GNU Scientific Library: http://www.gnu.org/software/gsl. Translation to SC  
 * done by Michael Dzjaparidze, 2010
 */
+ AbstractFunction { 	
	findMinimum { arg a, b, method = \golden, bracket = true; var coord;
		if(a.isNil or: { b.isNil }, {
			Error("Please supply a valid interval in which to search for a minimum.").throw
		}, {
			if(bracket.booleanValue, { 
				coord = this.bracket(a, b) 
			}, { 
				coord = [a, 0, b, this.value(a), 0, this.value(b)]
			});
			if(method == \brent, {
				^this.brent(*coord)
			}, {
				^this.golden(*coord)
			})
		})
	}
	
	bracket { arg a, b; var golden = 0.3819660, xl = a, xr = b, xc, fl = this.value(a), fr = 	this.value(b), fc, nbEval = 0, maxEval = 100, sqrtDblEps = 1.4901161193847656e-08;
		if(fr >= fl, {
			xc = ((xr - xl) * golden) + xl;
			nbEval = nbEval + 1;
			fc = this.value(xc)
		}, {
			xc = xr;
			fc = fr;
			xr = ((xc - xl) / golden) + xl;
			nbEval = nbEval + 1;
			fr = this.value(xr)
		});
		while({ nbEval < maxEval and: { (xr-xl) > (sqrtDblEps*((xr+xl)*0.5)+sqrtDblEps) } }, {
			if(fc < fl, {
				if(fc < fr, {
					//Return x_lower, x_upper, x_min, fx_lower, fx_upper, fx_min
					^[xl, xr, xc, fl, fr, fc]
				}, {
					if(fc > fr, {
						xl = xc;
						fl = fc;
						xc = xr;
						fc = fr;
						xr = ((xc - xl) / golden) + xl;
						nbEval = nbEval + 1;
						fr = this.value(xr)
					}, { /* fc == fr */
						xr = xc;
						fr = fc;
						xc = ((xr - xl) * golden) + xl;
						nbEval = nbEval + 1;
						fc = this.value(xc)
					})
				})
			}, { /* fc >= fl */
				xr = xc;
				fr = fc;
				xc = ((xr - xl) * golden) + xl;
				nbEval = nbEval + 1;
				fc = this.value(xc)
			});
		});
		//"Method bracket has failed.".warn;
		^[xl, xr, xc, fl, fr, fc]
	}

	brent { arg xlow, xup, xmin, fxlow, fxup, fxmin; var golden = 0.3819660, xl, xr, z, fz, d = 0, 	e = 0, u, fu, v = xlow + (golden * (xup - xlow)), w = v, fv = this.value(v), fw = fv, wlow, 	wup, tol, p, q, r, mid, iter = 0, maxIter = 100, t2, epsAbs = 0.001, epsRel = 0.001, absMin, 	continue = true;
		while({ continue and: { iter < maxIter } }, {
			xl = xlow;
			xr = xup;
			z = xmin;
			fz = fxmin;
			d = e;
			e = d;
			wlow = z - xl;
			wup = xr - z;
			tol = 1.4901161193847656e-08 * z.abs;
			p = 0; q = 0; r = 0;
			mid = 0.5 * (xl + xr);
			if(e.abs > tol, {
				r = (z - w) * (fz - fv);
				q = (z - v) * (fz - fw);
				p = ((z - v) * q) - ((z - w) * r);
				q = 2 * (q - r);
				if(q > 0.0, { p = p.neg }, { q = q.neg });
				r = e;
				e = d
			});
			if(p.abs < (0.5 * q * r).abs and: { p < (q * wlow) } and: { p < (q * wup) }, {
				t2 = 2 * tol;
				d = p / q;
				u = z + d;
				if((u - xl) < t2 or: { (xr - u) < t2 }, {
					d = if(z < mid, { tol }, { tol.neg })
				})
			}, {
				e = if(z < mid, { xr - z }, { (z - xl).neg });
				d = golden * e
			});
			if(d.abs >= tol, {
				u = z + d
			}, {
				u = z + if(d > 0.0, { tol }, { tol.neg })
			});
			fu = this.value(u);
			if(fu <= fz, {
				if(u < z, {
					xup = z;
					fxup = fz
				}, {
					xlow = z;
					fxlow = fz
				});
				v = w;
				fv = fw;
				w = z;
				fw = fz;
				xmin = u;
				fxmin = fu;
			}, {
				if(u < z, {
					xlow = u;
					fxlow = fu
				}, {
					xup = u;
					fxup = fu
				});
				if(fu <= fw or: { w == z }, {
					v = w;
					fv = fw;
					w = u;
					fw = fu;
				}, {
					if(fu <= fv or: { v == z } or: { v == w }, {
						v = u;
						fv = fu;
					})
				})
			});
			//Test the interval
			if(xlow > 0.0 and: { xup > 0.0 } or: { xlow < 0.0 and: { xup < 0.0 } }, {
				absMin = xlow.abs.min(xup.abs)
			}, {
				absMin = 0.0
			});
			if((xup - xlow).abs < (epsAbs + (epsRel * absMin)), { continue = false });
			iter = iter + 1
		});
		//Return found values
		^[xmin, fxmin]
	}
	
	golden { arg xlow, xup, xmin, fxlow, fxup, fxmin; var golden = 0.3819660, xc, xl, xr, fm, wlow, 	wup, xn, fn, iter = 0, maxIter = 100, epsAbs = 0.001, epsRel = 0.001, absMin, continue = true;
		while({ continue and: { iter < maxIter } }, {
			xc = xmin;
			xl = xlow;
			xr = xup;
			fm = fxmin;
			wlow = xc - xl;
			wup = xr - xc;
			xn = xc + (golden * if(wup > wlow, { wup }, { wlow.neg })); 
			fn = this.value(xn);
			if(fn < fm, {
				xmin = xn;
				fxmin = fn
			}, {
				if(xn < xc and: { fn > fm }, {
					xlow = xn;
					fxlow = fn
				}, {
					if(xn > xc and: { fn > fm }, {
						xup = xn;
						fxup = fn
					})
				})
			});
			//Test the interval
			if(xlow > 0.0 and: { xup > 0.0 } or: { xlow < 0.0 and: { xup < 0.0 } }, {
				absMin = xlow.abs.min(xup.abs)
			}, {
				absMin = 0.0
			});
			if((xup - xlow).abs < (epsAbs + (epsRel * absMin)), { continue = false });
			iter = iter + 1
		});
		//Return found values
		^[xmin, fxmin]		
	}
}
			