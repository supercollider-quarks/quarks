/* 
 * Original C code from http://mymathlib.webtrellis.net, 20-06-2010. Translation to SuperCollider by 
 * Michael Dzjaparidze, may 2010.
 */
GenLaguerre {
	var <>n, <>a;
	
	*new { arg n, a;
		^super.new.init(n, a);
	}
	
	init { arg n, a;
		if(n.isKindOf(Integer) and: { a.isKindOf(SimpleNumber) } or: { n == nil and: { a == nil } } 		or: { n.isKindOf(Integer) and: { a == nil } } or: { n = nil and: 		{ a.isKindOf(SimpleNumber) } }, {
			this.n = n ? 0;
			this.a = a ? 0;
			if(this.n < 0, {
				this.n = nil;
				Error("Argument n can't be a negative Integer.").throw
			});
			if(this.a < -1.0, {
				this.a = nil;
				Error("Argument a can't be smaller than -1.0").throw
			})
		}, {
			Error("Arguments n and a are not of type Integer and/or SimpleNumber").throw
		})
	}
	
	setN { arg n;
		if(n.isKindOf(Integer) and: { n >= 0 }, {
			this.n = n
		}, {
			Error("Argument n is either not of type Integer or is a negative Integer.").throw
		})
	}
	
	getN {
		^n;
	}
	
	setAlpha { arg a;
		if(a.isKindOf(SimpleNumber) and: { a >= -1.0 }, {
			this.a = a
		}, {
			Error("Argument a is either not of type SimpleNumber or is smaller than -1.0.").throw
		})
	}
	
	getAlpha {
		^a;
	}
	
	setNAlpha { arg n, a;
		if(n.isKindOf(Integer) and: { n >= 0 }, {
			this.n = n
		}, {
			Error("Argument n is either not of type Integer or is a negative Integer.").throw
		});
		if(a.isKindOf(SimpleNumber) and: { a >= -1.0 }, {
			this.a = a
		}, {
			Error("Argument a is either not of type SimpleNumber or is smaller than -1.0.").throw
		})
	}
	
	getNAlpha {
		^[n, a];
	}
	
	*calc { arg n, a, x;
		^this.new(n, a).calc(x)
	}
	
	calc { arg x; var alphaOneMx, l0, l1, ln; 
		if(x.isKindOf(SimpleNumber), {
			if(x >= 0, {
				alphaOneMx = a + 1.0 - x;
		
				if(n < 0, {
					^0.0
				});
				if(n == 0, {
					^1.0
				});
				if(n == 1, {
					^alphaOneMx
				});
		
				//If none of the above
				l0 = 1.0;
				l1 = alphaOneMx;
				ln = 0.0;
		
				for(1, n - 1, { arg k;
					ln = (((k + k + alphaOneMx) * l1) - ((a + k) * l0)) / (k + 1);
					l0 = l1;
					l1 = ln
				});
				^ln
			}, {
				Error("Argument x must be equal or greater than zero").throw
			})
		}, {
			Error("Argument x is not of type SimpleNumber").throw
		})
	}
}
		
		
		