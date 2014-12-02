// Rotation of input arrays (RotateL, RotateN, XFadeRotate)
// wslib 2005 / 2012

RotateN { // rotate an array of channels
	
	*new1 { |rate, n = 0, array|
		var selector = UGen.methodSelectorForRate(rate);
		var size = array.size;
		^Array.fill(size, { |i| 
			this.selectClass.perform(selector, (i + n)%size, array ); 
		})
	}
	
	*ar { arg n = 0, array;
		^this.new1(\audio, n, array);
	}
	
	*kr { arg n = 0, array;
		^this.new1(\control, n, array);
	}
		
	*selectClass { ^Select }
}

RotateL : RotateN { // rotate an array of channels with interpolation (linear crossfade)
	
	*selectClass { ^SelectL }
		
}

XFadeRotate : MultiOutUGen { // same as RotateL, but with equal power crossfading
	 *ar { arg n = 0, in;
		var insize = in.size;
		^Mix.fill(insize, {|i|
			PanAz.ar(insize, in[i], (i + n) * (2 / insize));
			});
		}
		
	*kr { arg n = 0, in;
		var insize = in.size;
		^Mix.fill(insize, {|i|
			PanAz.kr(insize, in[i], (i + n) * (2 / insize));
			});
		}
	
	}

SelectL : UGen { // select and interpolate // does wrap!!
	*ar { arg which, array;
		var whichfrac, whichfloor;
		// if 'which' is .kr klicks are heard at the turning points..
		// use .arSwitch to avoid this (or use K2A.ar on the 'which' input)
		whichfrac = which.frac; whichfloor = which.floor;
		^(Select.ar(whichfloor%array.size, array) * (1 - whichfrac))
		+ (Select.ar((whichfloor + 1)%array.size, array) * whichfrac)
		}
		
	*arSwitch { arg which, array;
		^if(which.rate == 'control')
			{ SelectL.ar(K2A.ar(which), array); }
			{ SelectL.ar(which, array); };
		}

	*kr { arg which, array;
		var whichfrac, whichfloor;
		whichfrac = which.frac; whichfloor = which.floor;
		^(Select.kr(whichfloor%array.size, array) * (1 - whichfrac))
		+ (Select.kr((whichfloor + 1)%(array.size), array) * whichfrac)
		}
	}
