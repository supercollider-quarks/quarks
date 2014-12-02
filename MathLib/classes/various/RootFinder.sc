/*
 * Levenberg-Marquardt algorithm for least squares. opts = [tau,eps1,eps2,kmax], where range of tau is typically 
 * somewhere between (1e-08 - 1) (smaller values of tau apply if it is assumed x0 is close to x*), kmax is the
 * max no. of iterations. Requires analytic Jacobian 
 *
 * source: http://www2.imm.dtu.dk/~hbn/Software/
 * ported to SC by Michael Dzjaparidze, July 2012
 */
RootFinder {
	
	*findRoot { arg func,jacobian,x0,param,opts=[1e-04,1e-06,1e-12,500];
		var n,x,xnew,j,jn,a,g,f,fb,fbn,fn,df,ng,mu,k,nu,nh,nx,stop,h,dl,eps=2.2204e-16,formatMatrix;
		
		(func.isNil or: { jacobian.isNil } or: { x0.isNil }).if { Error("please supply a valid func, jacobian and x0 arguments").throw };
		
		// check input
		func.respondsTo('at').not.if { func = [func] };
		jacobian.respondsTo('at').not.if { jacobian = [jacobian] };
		x0.respondsTo('at').not.if { x0 = [x0] };
		n = x0.size;
						
		(x0.size != func.size).if { 
			Error("please make sure x0 contains at least the same nr. of elements as func").throw
		};
		x0.isKindOf(Matrix).if {
			(x0.shape[1] > x0.shape[0]).if { Error("column size of x0 can't be bigger than its row size\n").throw };
			x = Matrix.newFrom(x0)
		} {
			x = Matrix.newFrom(x0.clump(1))
		};
		
		formatMatrix = { |fm,xm,pm,cs| var ft,jt;
			ft = fm collect: _.(xm.asArray.flop.flatten,pm);
			ft = (fm.size > 1).if { Matrix.newFrom(ft.clump(cs)) } { Matrix.newFrom(ft) }
		};
		
		f = formatMatrix.(func,x,param,1);
		j = formatMatrix.(jacobian,x,param,n);
			
		// initial values
		a = j.flop*j; g = j.flop*f; fb = 0.5*(f.flop*f)[0,0]; ng = g.getCol(0).abs.maxItem; mu = opts[0]*a.getDiagonal.maxItem;
		k = 1; nu = 2; nh = 0; stop = false;
					
		({ stop.not }).while {
			(ng <= opts[1]).if { 
				postf("function converged to a solution x\n");
				stop = true
			} {
				h = (a+mu*Matrix.newIdentity(n)).solve(g.getCol(0).neg,\gauss);
				h = Matrix.newFrom(h.clump(1));
				nh = h.norm; nx = opts[2]+x.norm;
				(nh <= (opts[2]*nx)).if { 
					"possible inaccuracy of solution -> change in x was smaller than the specified tolerance".warn; stop = true 
				} { 
					(nh >= (nx*eps.reciprocal)).if { "possible singular matrix".warn; stop = true } 
				}
			};
			stop.not.if {
				xnew = x+h; h = xnew-x; dl = 0.5*(h.flop*(mu*h-g))[0,0];
				fn = formatMatrix.(func,xnew,param,1);
				jn = formatMatrix.(jacobian,xnew,param,n);
				fbn = 0.5*(fn.flop*fn)[0,0]; df = fb-fbn;
				(dl > 0 and: { df > 0 }).if {
					x = xnew; fb = fbn; j = jn; f = fn;
					a = j.flop*j; g = j.flop*f; ng = g.getCol(0).abs.maxItem;
					mu = mu*max(3.reciprocal,1-((2*df*dl.reciprocal-1)**3)); nu = 2
				} {
					mu = mu*nu; nu = 2*nu;
				};
				k = k+1;
				(k > opts[3]).if { "number of iterations exceeds kmax".warn; stop = true }
			}
		};
		^x.getCol(0)
	}
	
}