Smacof {
	classvar eps = 1e0, mxIter = 1000, maxElems = 100, maxDims = 3;
	var  <>inharm, <>coords, <>stress, <>iter;
	
	
	init {
		stress = 0.0; 
//		iter = 0; // this is a return value!
	}
	
	*new { ^super.new.init }
	
	calc {|data, nDims = 2| 
		coords = Array.newClear(nDims)!data.size;
		// make a harmonic metric into an inharmonic metric
		inharm = data.collect{|row|
			row.collect{|elem| if (elem == 0) {0} {elem.reciprocal}
			}
		};
		// format metric matrix
//		inharm.size.do{|z|
//			z.do{|q|
//				inharm[q][z] = inharm[z][q]; // symmetrical
//			};
//			inharm[z][z] = 0; // hollow
//		};
		inharm = this.makeSymmetrical(inharm);
		^this.smacof(inharm, coords, nDims);
	}
				
	makeSymmetrical {|mtx| var res;
		mtx.size.do{|i|
			mtx.size.do{|j|
				mtx[j][i] = mtx[i][j]
			};
			mtx[i][i] = 0;
		};
		^mtx			
	}	
		
	stress0 {|d, m| var s = 0;
		d.size.do{|i|
			d.size.do{|j|
				s = s + (d[i][j] - m[i][j]).squared
			}
		};
		postf("s:: %\n\n", s);
		^s
	}
	
	stress1 {|d, m| var s, t = 0;
		s = this.stress0(d, m); 
		d.size.do{|i|
			d.size.do{|j|
				t = t + m[i][j].squared
			}
		};
		^(s/t).sqrt
	}
	
	dist {|v, mtxD, nDims|
		var d = mtxD;
		v.size.do{|i|
			v.size.do{|j|
				d[i][j] = 0;
				if (i != j) {
					nDims.do{|k|
						d[i][j] = d[i][j] + (v[i][k] - v[j][k]).squared;
					};	
				};
			};
		}
		^d.sqrt
	}
	
	smacof {|m, mtxV, nDims|
		var d0, 
		d,									// matrix for pairwise Euclidean distance
		b, s0, s, r, done, v0;
		var v = mtxV; 						// matrix for current L-dim configuration
											// Guttman transform
											// of N data points
		b = 		Array.newClear(m.size)!m.size;	// weights * (dissimilarity/distances)
		d0 = 	Array.newClear(m.size)!m.size; 	// previous pairwise distances
		v0 = 	Array.newClear(nDims)!m.size;  	// previos L-dim config
		
		done = false;
		r = 	0;

		m.size.do{|i|
			m.size.do{|j|
				if (m[i][j].abs > r) { r = m[i][j].abs }
			}
		};
		r = r * 2;
		
		postf("r: %\n", r);
		
		m.size.do{|i|
			nDims.do{|k|
				v0[i][k] = r * 1.0.rand
			}
		};
		
		postf("v0: %\n", v0);
		
		d = this.dist(v0, d0, nDims);
		s0 = this.stress0(d0, m);
		iter = 0;
		done = false;

		postf("d0: %\n\ns0: %\n\n", d0, s0);

		{ done.not }.while({
			iter = iter + 1; 
			m.size.do{|i|
				m.size.do{|j|
					if ( (i != j) and: (d0[i][j] != 0) ) {
						b[i][j] = (m[i][j] / d0[i][j] / m.size).neg;
					}{
						b[i][j] = 0; 
					};
				};
			};
			m.size.do{|i|
				m.size.do{|j|
					if (i != j) { b[i][i] = b[i][i] - b[i][j] }
				}
			};
			
			postf("b: %\n\n\n", b);
			
			m.size.do{|i|
				nDims.do{|k|
					v[i][k] = 0;
					m.size.do{|j|
						postf("v[%,%] = %\tb[%,%] = %\tv0[%,%] = %",
			i, k, v[i][k].round(0.01), i, j, b[i][j].round(0.01), j, k, v0[j][k].round(0.01));
						v[i][k] = v[i][k] + (b[i][j] * v0[j][k]);
						postf("\tb*v0 = %\n", (b[i][j] * v0[j][k]).round(0.001));
					}
				}
			};
			d = this.dist(v, d, nDims);
			s = this.stress0(d, m);

			postf("v[%]: %\n\nd: %\n\ns: %\n\n\n[....]\n\n", iter, v, d, s);  
			
			// maybe delete mxIter > 0 
			done = ( ((s0 - s) < eps) or: ((mxIter > 0) and: (iter >= mxIter)) );
			if (done.not) { \aucha.postln;
				v0 = v;
				d0 = d;
				s0 = s;
			};
		});
		this.coords = v;
		this.stress = this.stress1(d, m); // does not return a value, instead replaces vars
		^"okioki"
	}


/* 

	adapted from pascal code by C. Barlow (who adopted it from A. Gräf)
	(CC) 2010 jsl
*/	
}