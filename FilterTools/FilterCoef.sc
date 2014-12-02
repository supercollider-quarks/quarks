/* Code partially adapted from "Elements of Computer Music", F. Richard Moore, Prentice-Hall, 1990.  
 * Translation to SC and all other functionality by Michael Dzjaparidze, 2010.
 */
FilterCoef {
	var <acoefs, <bcoefs, <poles, fpoles, <zeros, <real, <norm;
	
	*new { arg poles, zeros, real, norm;
		^super.new.init(poles, zeros, real, norm)
	}
	
	init { arg poles, zeros, real, norm;
		this.poles = poles;
		this.zeros = zeros;
		this.real = real;
		this.norm = norm
	}
	
	poles_ { arg newPoles; var angle;
		//Check and parse input args
		if(newPoles != nil, {
			if(newPoles.isKindOf(Array), {
				newPoles.do({ arg pole;
					if(pole.isKindOf(Polar), {
						angle = pole.angle.wrap(-pi, pi);
						poles = poles.add(Polar.new(pole.magnitude, angle));
						//Post a warning if the new pole magnitude is > 1.0
						if(pole.magnitude > 1.0, {
							"Pole magnitude is larger than 1.0.".warn
						})
					}, {
						Error("Input is not in required format.\n").throw 
					})
				});
				poles = poles.insert(0, 0)   		//Insert dummy pole
			}, {
				Error("Input is not in required format.\n").throw
			})
		})
	}
	
	zeros_ { arg newZeros; var angle;
		if(newZeros != nil, {
			if(newZeros.isKindOf(Array), {
				newZeros.do({ arg zero;
					if(zero.isKindOf(Polar), {
						angle = zero.angle.wrap(-pi, pi);
						zeros = zeros.add(Polar.new(zero.magnitude, angle));
					}, {
						Error("Input is not in required format.\n").throw 
					})
				});
				zeros = zeros.insert(0, 0)   //Insert dummy zero
			}, {
				Error("Input is not in required format.\n").throw
			})
		})
	}
	
	real_ { arg answer;
		real = (answer ? true).booleanValue
	}
	
	norm_ { arg answer;
		norm = (answer ? true).booleanValue
	}
	
	checkIfReal { 
		block { |break|
			[poles, zeros].do({ |type|
				this.format(type).do({ |item| 
					if(item.size < 2, { 
						if(item[0].angle.abs != 0.0 and: { item[0].angle.abs != pi }, {
							real = false;
							break.value
						})
					})
				})
			});
			real = true
		};
		^real
	}
	
	*calc { arg poles, zeros, norm = true;
		^this.new(poles, zeros).calc(norm)
	}
	
	calc { arg argNorm = true;
		norm = argNorm;
		if(poles.notNil and: { zeros.notNil }, {
			//Only multiply factors if poles or zeros are supplied
			if(zeros.every({ |item| item.magnitude == 0 }), {
				acoefs = Polynomial[1]
			}, {
				acoefs = zeros.neg.multiplyFactors
			});
			if(poles.every({ |item| item.magnitude == 0 }), {
				bcoefs = Polynomial[1]
			}, {
				bcoefs = poles.neg.multiplyFactors
			});
			
			//If norm is set to true, normalize the frequency response to 1.0
			if(norm, { acoefs = acoefs * this.returnMaxMag.reciprocal });
			
			acoefs = acoefs.select({ |item| item.magnitude.abs > 0.0 });
			bcoefs = bcoefs.select({ |item| item.magnitude.abs > 0.0 });
			
			//If the filter is real, return real part only
			if(real, { ^[acoefs.real, bcoefs.real] }, { ^[acoefs, bcoefs] })
		}, {
			Error("There are no poles and/or zeros specified.\n").throw
		})
	}
	
	calcImpResp { var imp = Array.fill(80, { arg i; if(i == 0, { 1 }, { 0 }) }), impResp = 	Array.new, poll = Array.fill(3, { 0.1 }), i = 0;
		//Run the while loop as long as there is a noticeable response and i < 60
		while({ poll.magnitude.sum > 0.001 and: { i < 60 } }, {
			impResp = impResp.add(0);
			acoefs.size.do({ arg j;
				if(imp[i-j] == nil, {
					impResp[i] = impResp[i] + 0
				}, {
					impResp[i] = impResp[i] + (imp[i-j] * acoefs[j])
				})
			});
			(bcoefs.size-1).do({ arg j; j = j + 1;
				if(imp[i-j] == nil, {
					impResp[i] = impResp[i] - 0
				}, {
					impResp[i] = impResp[i] - (impResp[i-j] * bcoefs[j])
				})
			});
			i = i + 1;
			if(i >= 3, { poll = impResp[(i-3)..i] })
		});
		^impResp
	}
	
	//PRIVATE (CLASS) METHODS
	
	//Groups items into (complex) conjugate pairs
	format { arg type; var i, b = Array.new;
		i = 2;		//Normally start from 1, but now from 2 since first item is dummy zero
		while({ i < (type.size+1) }, {
			if(type[i].notNil and: { type[i-1].imag.abs != 			0.0 } and: { type[i-1].imag.abs != pi } and: { type[i-1].real == type[i].real and: 			{ type[i-1].imag == type[i].imag.neg } }, {
				//b = b.add([type[i-1], type[i]]);	
				//Multiply conjugate terms, resulting in a real-valued 2nd degree polynomial
				b = b.add([0, type[i-1], type[i]].multiplyFactors).real;
				i = i + 1
			}, {
				if(type[i-1].imag.abs < 1e-06, {
					b = b.add([0, 1, type[i-1]]).real
				}, {
					b = b.add([0, 1, type[i-1]])
				})
			});
			i = i + 1
		});
		^b
	}	
	
	/* Returns the maximum magnitude in the frequency response of the filter. Can be used for the 	 * a0 coefficient to normalize the frequency response to 1.0
	 */
	returnMaxMag { var func;
		/* Function to extract maximum from (In this case a modified version of the method to 		 * calculate the frequency response of the filter, see calcFreqResponse in ZPlane)
		 */
		func = { arg omega; var den, num, amp, dist;
			//Return the distance from pole/zero location to frequency omega on the unit circle
			dist = { arg omega, item; var x, y;
				x = item.real - cos(omega);	//X dist from pole/zero to freq omega on unit circle
				y = item.imag - sin(omega);	//Y dist from pole/zero to freq omega on unit circel
		
				sqrt((x*x) + (y*y))		//Return magnitude of the distance
			};
			den = num = 1.0;
			zeros.select({ |item, i| i > 0 }).do({ arg zero; 
				num = num * dist.value(omega, zero);
			});
			poles.select({ |item, i| i > 0 }).do({ arg pole;
				den = den * dist.value(omega, pole);
			});
			if(den != 0.0, { 
				amp = num / den 
			}, { 
				if(num >= 0.0, { amp = inf }, { amp = inf.neg }) 
			});
			amp.neg	//Negate result because .golden find min of a function and we want the max
		};
		/* .golden returns the minimum of the independent and dependent variable. The negated 		 * dependent variable gives us the maximum magnitude in the frequency response of the 		 * filter
		 */
		^func.findMinimum(0, pi)[1].neg
	}
}