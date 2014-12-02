// wslib 2010

+ UGen { 
	// doesn't work for demand rate
	blend { arg that, blendFrac = 0.5;
			var pan;
			^if (rate == \demand || that.rate == \demand) {
				this.notYetImplemented(thisMethod)
			} {
				pan = blendFrac.linlin(0.0, 1.0, -1, 1);
				if (rate == \audio) {
					^XFade2.ar(this, that, pan)
				};
	
				if (that.rate == \audio) {
					^XFade2.ar(that, this, pan.neg)
				};
	
				^LinXFade2.perform(LinXFade2.methodSelectorForRate(rate), this, that, pan)
			}
		}
	}