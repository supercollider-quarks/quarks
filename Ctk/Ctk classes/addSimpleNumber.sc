+ SimpleNumber {
	mapIntoRange {arg range, target, steps = 12, numturns = 0, maxturns = 10, origval;
		var newtestval;
		origval = origval ? this;
		(numturns < maxturns).if({
			((this - target).abs > range).if({
				newtestval = (this > target).if({
					this - steps
				}, {
					this + steps
				});
				^newtestval.mapIntoRange(range, target, steps, 
					numturns + 1, maxturns, origval)
			}, {
			^this;
			})
		}, {
			"Your Number couldn't be wrapped into the desired range. Check your parameters or increase maxturns.".warn;
			^origval;
		})
	}

	mapIntoBounds {arg low, high, steps = 12;
		var tmp, dif;
		(low > high).if({
			tmp = high;
			high = low;
			low = tmp;
		});
		dif = high - low * 0.5;
		^this.mapIntoRange(dif, low + dif, steps);
	}
}

/*

a = -5.3.mapIntoRange(6, 60);
a
*/