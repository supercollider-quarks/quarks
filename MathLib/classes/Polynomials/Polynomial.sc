/* A number of methods for common polynomial operations. Michael Dzjaparidze, 2010. 
 * GNU General Public License - http://www.gnu.org/licenses/gpl-3.0.html
 */
Polynomial[slot] : ArrayedCollection {
	
	species { ^this.class }	
	
	* { arg c2; var prod;
		if(c2.isKindOf(Polynomial), {
			if(c2.size > 1, {
				prod = Polynomial.fill((this.size+c2.size-1), { 0 });
				this.do({ |item1, i|
					c2.do({ |item2, j|
						prod[i+j] = prod[i+j] + (item1 * item2);
						if(prod[i+j].isKindOf(Complex) and: { prod[i+j].imag.abs < 1e-14 }, {
							prod[i+j] = prod[i+j].real
						})
					})
				})
			}, {
				prod = c2[0] * this
			})
		}, {
			if(c2.isKindOf(Number), {
				prod = c2 * this
			}, {
				Error("Operation is not defined for the argument given.").throw
			})
		});
		while({ prod[prod.size-1] == 0 }, { prod.pop })
		^prod
	}
	
	/ { arg c2; var ns = this.size-1, ds = c2.size-1, rem, quo;
		if(c2.isKindOf(Polynomial), {
			if(c2.size > 1, { 
				while({ ds >= 0 and: { c2[ds] == 0.0 } }, { ds = ds - 1 });
				if(ds < 0, { Error("Divide by zero detected.").throw });
				quo = Polynomial.fill(this.size-ds, { 0.0 });
				rem = this.copy;
				//Synthetic division
				for(ns-ds, 0, { |i| 
					quo[i] = rem[ds+i] / c2[ds];
					for(ds+i-1, i, { |j| rem[j] = rem[j] - (quo[i] * c2[j-i]) });
				});
				for(ds, ns, { |i| rem.pop })
			}, {
				rem = c2[0].reciprocal * this
			})
		}, {
			if(c2.isKindOf(Number), {
				rem = c2.reciprocal * this
			}, {
				Error("Operation is not defined for the argument given.").throw
			})
		});
		while({ rem[rem.size-1] == 0 }, { rem.pop });
		^[rem, quo]
	}
	
	pow { arg n; var d = this.degree, l = this.offLow, diff = d - l, c0, cs, index;
		if(n.real == 0.0 and: { n.imag == 0.0 }, { ^1 });
		if(n.real == 1.0 and: { n.imag == 0.0 }, { ^this });
		if(n.real == 2.0 and: { n.imag == 0.0 }, { ^(this * this) });
		if(d < 0, { ^nil });
		if(this.isMonomial, { 
			index = this.detectIndex(_ != 0);
			if(index.isNil, { 
				^0 
			}, { 
				if(n.isKindOf(SimpleNumber), {
					^(Polynomial.fill(d*n.floor, { 0 }) ++ this[index].pow(n))
				}, {
					if(n.isKindOf(Complex) or: { n.isKindOf(Polar) }, {
						^(Polynomial.fill(d*n.magnitude.floor, { 0 }) ++ this[index].cpow(n))
					})
				})
			})
		});
		if(this.isBinomial, {
			if(n > 1 and: { n.isKindOf(Integer) }, {
				^this.binPow(n)
			}, {
				if(this[0] == 1, {		//Calculate binomial series if first coefficient is 1
					^this.binPowFrac(n)
				})
			})
		});
		/* If none of the above, calculate (a_i*x_i + a_(i+1)*x_(i+1) + ... + a_n*x_n)^p, 
		 * where p > 1 and is integer.
		 */
		if(n.isKindOf(Integer), {
			cs = this * this;
			c0 = this.copy;
			(n-2).do({ diff.do({ c0 = c0.add(0) }); cs = cs * c0 });
			^cs
		})
	}
	
	//Expand an array of 1st degree binomial factors of the form: a_0 + a_1*x
	*expandBinomialFactors { arg factors; var constant = 1, c = Array.newClear(factors.size);
		factors.do({ |item, i| 
			if(item.size < 1 or: { item[0].magnitude == 0 } or: { item[1].magnitude == 0 } or: 			{ item.size > 2 }, { 
				Error("Check input. Either not a binomial or not a first degree factor").throw
			}, {
				constant = constant * item[0];
				c[i] = item[1] / item[0];
			})
		});
		if(constant.imag.abs < 1e-14, { constant = constant.real });
		^(constant * c.multiplyFactors)
	}
	
	isMonomial { var count = 0, i;
		i = 0; 
		while({ count < 2 and: { i < this.size } }, { 
			if(this[i] != 0, { count = count + 1 }); 
			i = i + 1 
		});
		if(count != 1, { ^false }, { ^true })
	}
	
	isBinomial { var n = this.offLow+1, m = this.degree;
		while({ n < m and: { this[n] == 0.0 } }, { n = n + 1 });
		if(n == m, { ^true }, { ^false })
	}
	
	binPow { arg n; var l = this.offLow, m = this.degree, ins, result = Polynomial.new;
		ins = m - l - 1;
		(n+1).do({ |k| 
			result = result.add(this[l].pow(n-k) * n.nCk(k) * this[m].pow(k));
			if(m > 1 and: { k < n }, { ins.do({ result = result.add(0) }) })
		});
		^(Polynomial.fill(n*l, { 0 }) ++ result)
	}
	
	binPowFrac { arg alpha; var x = 0.01, prev, curr, yn, i, delta, m = this.degree, 	ins, result = Polynomial.new, maxIter = 20;
		ins = m - 1;
		i = 0;
		delta = 1; prev = 0;
		while({ delta > 1e-08 and: { i < maxIter } }, {   // <-- Change error tolerance???
			if(i > 0 and: { m > 1 }, { ins.do({ result = result.add(0) }) });
			yn = this[m].pow(i) * alpha.nCk(i);
			result = result.add(yn);
			curr = yn * x.pow(i*m);
			delta = (prev - curr).abs;
			prev = curr;
			i = i + 1
		});
		^result
	}
	
	degree { var d = this.size-1;
		while({ this[d] == 0.0 }, { d = d - 1 });
		^d
	}
	
	offLow { var index = 0;
		while({ this[index] == 0.0 }, { index = index + 1 });
		^index
	}
	
	eval { arg x; var y;
		y = this[this.size-1];
		(this.size-1).reverseDo({ |i| y = this[i] + (x * y) });
		^y
	}
	
	evalDerivs { arg x; var i, n, nmax, k, l, lmax, len = this.size, res = Array.newClear(len), f;
		i = 0; n = 0; nmax = 0;
		while({ i < len }, {
			if(n < len, {
				res[i] = this[len-1];
				nmax = n;
				n = n + 1
			}, {
				res[i] = 0.0
			});
			i = i + 1
		});
		i = 0;
		while({ i < (len-1) }, {
			k = len - 1 - i;
			res[0] = x * res[0] + this[k-1];
			lmax = if(nmax < k, { nmax }, { k-1 });
			l = 1;
			while({ l <= lmax }, {
				res[l] = x * res[l] + res[l-1];
				l = l + 1
			});
			i = i + 1
		});
		f = 1.0;
		i = 2;
		while({ i <= nmax }, {
			f = f * i;
			res[i] = res[i] * f;
			i = i + 1
		});
		^res
	}
	
	findRoots { arg method = \laguerre; var zroots = Array.new, coL, coH, m, p, z, magnitude, 	angle, incr, coefs, err, a2, d;
		//Check for zero coefficients of the highest terms. If found, adjust the degree
		m = this.degree;
		if(m < 1, { Error("Zero degree polynomial: no roots to be found.").throw });
		coH = m;
		//Divide out any roots at the origin
		coL = this.offLow;
		coL.do({ zroots = zroots.add(0.0) });
		m = m - coL;
		coefs = this[coL..coH];
		//From here on, first and last coefficients are non-zero
		if(method == \laguerre, {
			while({ m > 2 }, {
				/* If all coefficients in between the first and last are zero, we have a 				 * (complex) binomial.
				 */
				//If so, find all solutions at once
				if(coefs.isBinomial, {
					p = coefs[0].neg / coefs[m];
					magnitude = pow(p.magnitude, m.reciprocal);
					angle = p.angle / m;
					incr = 2pi / m;
					while({ m > 0 }, {
						//TO DO: MAKE INTO A FUNCTION (CAN BE USED AT MULTIPLE PLACES)
						if((angle-0.0).abs < 1e-14 or: { (angle-pi).abs < 1e-14 } or: 						{ (angle-2pi).abs < 1e-14 }, { 
							zroots = zroots.add(magnitude)
						}, {
							if((angle-0.5pi).abs < 1e-14 or: { (angle-1.5pi).abs < 1e-14 }, {
								zroots = zroots.add(Complex(0, magnitude*sin(angle)))
							}, {
								zroots = zroots.add(
									Complex(magnitude*cos(angle), magnitude*sin(angle))
								)
							})
						});
						angle = angle + incr;
						m = m - 1
					});
				}, {
					//Solve for the roots numerically using Laguerre method
					z = coefs.laguerre(Complex(-64, -64));
					//Divide out the found root from the original polynomial
					#err, coefs = coefs / Polynomial[z.neg, 1];
					if(err.size > 0 and: { err[0].magnitude >= 1e-14 }, { 
						Error("Failed to find roots.").throw 
					});
					if(z.imag.abs < 1e-14, { z = z.real });
					zroots = zroots.add(z);
					m = m - 1
				})
			});
			//Solve quadratic equation analytically
			if(m == 2, {
				a2 = coefs[2] * 2.0;
				d = (coefs[1]*coefs[1]) - (4*coefs[2]*coefs[0]);
				if(d.isKindOf(SimpleNumber) and: { d < 0 }, { 
					d = sqrt(Complex(d, 0)) 
				}, { 
					d = sqrt(d)
				});
				zroots = zroots.add((coefs[1].neg+d) / a2);
				zroots = zroots.add((coefs[1].neg-d) / a2);
			}, { //Solve linear equation analytically
				if(m == 1, {
					zroots = zroots.add(coefs[0].neg / coefs[1])
				})
			});
			^zroots
		}, {
			if(coefs.detect({ |it| it.isKindOf(Complex) or: { it.isKindOf(Polar) } }).notNil, {
				Error("Method eigenvalue only works for real polynomial coefficients.").throw
			}, {
				^(zroots ++ coefs.eigenvalue)
			})
		})		
	}
	
	/* Works for polynomials with real and/or complex coefficients. Based on the code from: 	 * "Numerical Analysis: The Mathematics of Scientific Computing" by D.R. Kincaid & E.W. Cheney, 	 * Brooks/Cole Publishing, 1990, http://www.netlib.org/kincaid-cheney/laguerre.f
	 */
	laguerre { arg z; var n = this.size-1, j, k, maxIter = 20, ca, ca2, cb, c1, c2, cc, 
	cc1, cc2, alpha, beta, gamma;
		k = 1;
		while({ k <= maxIter }, {
			alpha = this[n];
			beta = 0.0;
			gamma = 0.0;
			j = n - 1;
			while({ j >= 0 }, {
				gamma = (z * gamma) + beta;
				beta = (z * beta) + alpha;
				alpha = (z * alpha) + this[j];
				j = j - 1
			});
			if(alpha.magnitude <= 1e-14, { // <-- probably have to change this...
				if(z.imag.abs < 1e-14, { ^z.real }, { ^z })
			});
			ca = beta.neg / alpha;
			ca2 = ca * ca;
			cb = ca2 - (2.0 * gamma / alpha);
			c1 = sqrt((n-1) * ((n*cb) - ca2));
			cc1 = ca + c1;
			cc2 = ca - c1;
			if(cc1.magnitude > cc2.magnitude, { cc = cc1 }, { cc = cc2 });
			cc = cc / n;
			c2 = 1 / cc;
			z = z + c2;
			k = k + 1
		});
		if(z.imag.abs == 0.0, { ^z.real }, { ^z })
	}
	
	/* Only works for polynomials with real coefficients. Based on the code from the GNU 	 * Scientific Library, http://www.gnu.org/software/gsl
	 */
	eigenvalue { var n = this.size-1, cm = Matrix.newClear(n, n), i, j, g, f, s, notConverged = 	true, rowNorm, colNorm, radix = 2, radix2 = radix*radix, t, zroots = Array.newClear(n), e, k, 	m, w, x, 	y, z, p, q, r, notlast, iter, a1, a2, a3;
		//Construct the companion matrix
		i = 1; while({ i < n }, { cm[i, i-1] = 1.0; i = i + 1 });
		i = 0; while({ i < n }, { cm[i, n-1] = this[i].neg / this[n]; i = i + 1 });
		//Balance the companion matrix
		rowNorm = 0; colNorm = 0;
		while({ notConverged }, {
			notConverged = false;
			i = 0;
			while({ i < n }, {
				//Column norm, excluding the diagonal
				if(i != (n-1), {
					colNorm = cm[i+1, i].abs
				}, {
					colNorm = 0.0;
					j = 0; 
					while({ j < (n-1) }, { colNorm = colNorm + cm[j, n-1].abs; j = j + 1 });
				});
				//Row norm, excluding the diagonal
				if(i == 0, {
					rowNorm = cm[0, n-1].abs
				}, {
					if(i == (n-1), {
						rowNorm = cm[i, i-1].abs
					}, {
						rowNorm = cm[i, i-1].abs + cm[i, n-1].abs
					})
				});
				if(colNorm != 0.0 and: { rowNorm != 0.0 }, {
					g = rowNorm / radix;
					f = 1;
					s = colNorm + rowNorm;
					while({ colNorm < g }, {
						f = f * radix;
						colNorm = colNorm * radix2
					});
					g = rowNorm * radix;
					while({ colNorm > g }, {
						f = f / radix;
						colNorm = colNorm / radix2
					});
					if((rowNorm+colNorm) < (0.95*s*f), {
						notConverged = true;
						g = f.reciprocal;
						if(i == 0, {
							cm[0, n-1] = cm[0, n-1] * g
						}, {
							cm[i, i-1] = cm[i, i-1] * g;
							cm[i, n-1] = cm[i, n-1] * g
						});
						if(i == (n-1), {
							j = 0; while({ j < n }, { cm[j, i] = cm[j, i] * f; j = j + 1 });
						}, {
							cm[i+1, i] = cm[i+1, i] * f
						})
					})
				});
				i = i + 1
			})
		});
		//Find the roots
		t = 0.0; p = 0; q = 0; r = 0;
		//NEXT ROOT
		while({ true }, { block { |next_root|
			if(n == 0, { ^zroots });
			iter = 0;
			//NEXT ITERATION
			while({ true }, { block { |next_iter|
				e = n;
				block { |break| 
					while({ e >= 2 }, {
						a1 = cm[e-1, e-2].abs;
						a2 = cm[e-2, e-2].abs;
						a3 = cm[e-1, e-1].abs;
						if(a1 <= (2.2204460492503131e-16*(a2+a3)), { break.value }); 
						e = e - 1
					})
				};
				x = cm[n-1, n-1];
				if(e == n, { 
					zroots[n-1] = x + t;		//One real root
					n = n - 1;
			 		next_root.value		//GOTO NEXT ROOT
				});
				y = cm[n-2, n-2];
				w = cm[n-2, n-1] * cm[n-1, n-2];
				if(e == (n-1), {
					p = (y - x) / 2;
					q = p * p + w;
					y = q.abs.sqrt;
					x = x + t;
					if(q > 0, {				//Two real roots
						if(p < 0, { y = y.neg });
						y = y + p;
						zroots[n-1] = x - (w/y);
						zroots[n-2] = x + y
					}, {
						zroots[n-1] = Complex(x + p, y.neg);
						zroots[n-2] = Complex(x + p, y);
					});
					n = n - 2;
					next_root.value		//GOTO NEXT ROOT
				});
				//No more roots found yet, do another iteration
				if(iter == 60, {
					Error("Too many iterations!").throw
				});
				if((iter % 10) == 0 and: { iter > 0 }, {
					//Use an exceptional shift
					t = t + x;
					i = 1; while({ i <= n }, { cm[i-1, i-1] = cm[i-1, i-1] - x; i = i + 1 });
					s = cm[n-1, n-2].abs + cm[n-2, n-3].abs;
					y = 0.75 * s;
					x = y;
					w = -0.4375 * s * s
				});
				iter = iter + 1;
				m = n - 2;
				block { |break| 
					while({ m >= e }, {
						z = cm[m-1, m-1];
						r = x - z;
						s = y - z;
						p = cm[m-1, m] + ((r * s - w) / cm[m, m-1]);
						q = cm[m, m] - z - r - s;
						r = cm[m+1, m];
						s = p.abs + q.abs + r.abs;
						p = p / s;
						q = q / s;
						r = r / s;
						if( m == e, { break.value });
						a1 = cm[m-1, m-2].abs;
						a2 = cm[m-2, m-2].abs;
						a3 = cm[m, m].abs;
						if((a1*(q.abs+r.abs))<=(2.2204460492503131e-16*p.abs*(a2+a3)), { 
							break.value 
						});
						m = m - 1
					})
				};
				i = m + 2; while({ i <= n }, { cm[i-1, i-3] = 0; i = i + 1 });
				i = m + 3; while({ i <= n }, { cm[i-1, i-4] = 0; i = i + 1 });
				//Double QR step
				k = m;
				while({ k <= (n-1) }, {
					block { |continue| 
						notlast = k != (n-1);
						if(k != m, {
							p = cm[k-1, k-2];
							q = cm[k, k-2];
							r = if(notlast, { cm[k+1, k-2] }, { 0.0 });
							x = p.abs + q.abs + r.abs;
							if(x == 0, { continue.value });
							p = p / x;
							q = q / x;
							r = r / x
						});
						s = ((p*p) + (q*q) + (r*r)).sqrt;
						if(p < 0, { s = s.neg });
						if(k != m, { 
							cm[k-1, k-2] = s.neg * x
						}, {
							if(e != m, {
								cm[k-1, k-2] = cm[k-1, k-2] * -1
							})
						});
						p = p + s;
						x = p / s;
						y = q / s;
						z = r / s;
						q = q / p;
						r = r / p;
						//Do row modifications
						j = k;
						while({ j <= n }, {
							p = cm[k-1, j-1] + (q * cm[k, j-1]);
							if(notlast, {
								p = p + (r * cm[k+1, j-1]);
								cm[k+1, j-1] = cm[k+1, j-1] - (p * z)
							});
							cm[k, j-1] = cm[k, j-1] - (p * y);
							cm[k-1, j-1] = cm[k-1, j-1] - (p * x);
							j = j + 1
						});
						j = if((k+3) < n, { k + 3 }, { n });
						//Do column modifications
						i = e;
						while({ i <= j }, {
							p = (x * cm[i-1, k-1]) + (y * cm[i-1, k]);
							if(notlast, {
								p = p + (z * cm[i-1, k+1]);
								cm[i-1, k+1] = cm[i-1, k+1] - (p * r)
							});
							cm[i-1, k] = cm[i-1, k] - (p * q);
							cm[i-1, k-1] = cm[i-1, k-1] - p;
							i = i + 1
						});
					};
					k = k + 1
				});
				next_iter.value		//GOTO NEXT INTERATION
			} })
		} })
	}
}

+ Number {
	//Calculate the coefficient of the x^k term in the expansion of a binomial power
	nCk { |k| var n = this, c = 1.0;
		if(k == 0 or: { k == n }, { ^1 });
		if(n > 1 and: { n.isKindOf(Integer) }, {
			if(k > (n-k), { k = n - k })	//Make use of symmetry
		});
		k.do({ |i| 
			c = c * (n-i) / (i+1)
		});
		^c
	}
}

+ ArrayedCollection {	
	/* Optimized method for expanding multiple 1st degree binomial factors of the form: 1 + a_1*x
	 * An array of all a_1 coefficients should be supplied.
	 */
	multiplyFactors { var li = this.size, c = Polynomial.newClear(li+1);
		c[0] = 1.0;
		//Recursive multiplication to find polynomial coefficients
		for(1, c.size-1, { |i| var j = i - 1;
			c[i] = this[i-1] * c[i-1];
			while({ j >= 1 }, {
				c[j] = c[j] + (this[i-1] * c[j-1]);
				if(c[j].imag.abs < 1e-14, { c[j] = c[j].real });
				j = j - 1
			})
		});
		if(c[li].imag.abs < 1e-14, { c[li] = c[li].real });
		^c
	}
}