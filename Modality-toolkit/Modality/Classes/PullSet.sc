/*

// PullSet is like SoftSet: it "softens" a parameter set action.
// 1. When the newVal is close enough to the current value, or the lastVal
//  .set(name, value) is done.
// 2. When it is too far away, SoftSet does nothing;
//    PullSet pulls the value in the right direction by "within".


x = Ndef(\a).set(\freq, 200); x.gui.skipjack.dt = 0.05;

// close enough
PullSet(x, \freq, 220);
// pulling
PullSet(x, \freq, 5000);
// pulling again
PullSet(x, \freq, 5000);

// jump - allowed when lastVal is close to current value
PullSet(x, \freq, 5000, lastVal: x.get(\freq));


(
fork {
	20.do { PullSet(x, \freq, rrand(5000, 5100)); 0.1.wait; };
	20.do { PullSet(x, \freq, rrand(400, 500)); 0.1.wait; };
};
)

*/

PullSet : SoftSet {

	// factored out so one can collect multiple nextPairs for multi-set messages
	*nextPair { |obj, paramName, value, within = (defaultWithin), lastVal, spec|
		var currVal, currValNorm, lastValNorm, newValNorm, closeEnough;
		var nextPair;

		spec = (spec ?? { this.getSpec(obj, paramName); }).asSpec;
		currVal = obj.get(paramName);

		// can't tell, so just do it
		if (currVal.isNil or: {spec.isNil}) {
			^[paramName, value]
		};

		// if we have a current value and a spec

		if (spec.notNil) {
			newValNorm = spec.unmap(value);
			currValNorm = spec.unmap(currVal);

			closeEnough = absdif(currValNorm, newValNorm) <= within;
			if (closeEnough.not and: lastVal.notNil) {
				lastValNorm = spec.unmap(lastVal);
				closeEnough = absdif(currValNorm, lastValNorm) <= within;
			};
		};

		if (closeEnough) {
			nextPair = [paramName, value];
		} {

		// ONLY THIS HERE IS NEW - pull when too far off
		// 	"PullSet: pulling.".postln;

			newValNorm = currValNorm + (within * (newValNorm - currValNorm).sign);
			value = spec.map(newValNorm);
			nextPair = [paramName, value];
		};

		^nextPair;
	}

}
