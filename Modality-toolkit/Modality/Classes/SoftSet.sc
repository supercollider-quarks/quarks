
SoftSet {
	classvar <>defaultWithin = 0.025;

		// set single param by normalized value, let obj provide the spec
		// convenience for unipolar controllers
		// requires object to provide unipolar value with .getUni
	*uni { |obj, paramName, value,  within = (defaultWithin), lastVal|

		// all vals are normalized, so no spec needed
		var nextPair = this.uniNextPair(obj, paramName, value,  within, lastVal);
		var willSet = nextPair.notNil;
		if (willSet) { obj.setUni(*nextPair); };
		^willSet
	}

	*uniNextPair { |obj, paramName, value, within = (defaultWithin), lastVal|
		var currVal, closeEnough;
		currVal = obj.getUni(paramName);

		if (currVal.isNil) {
			obj.setUni(paramName, value);
			^true
		};

		// we have a currVal
		closeEnough = absdif(currVal, value) <= within;
		if (closeEnough.not and: lastVal.notNil) {
			closeEnough = absdif(currVal, lastVal) <= within;
		};

		if (closeEnough) { obj.setUni(paramName, value); }
		^closeEnough

	}

	// basic setting of single value
	*new { |obj, paramName, value, within = (defaultWithin), lastVal, spec|
		var nextPair = this.nextPair(obj, paramName, value, within, lastVal, spec);
		var willSet = nextPair.notNil;
		if (willSet) { obj.set(*nextPair); };
		^willSet
	}

	// factored out so one can collect multiple nextPairs for multi-set messages
	*nextPair { |obj, paramName, value, within = (defaultWithin), lastVal, spec|
		var currVal, currValNorm, lastValNorm, newValNorm, closeEnough;
		var nextPair;

		spec = (spec ?? { this.getSpec(obj, paramName); }).asSpec;
		currVal = obj.get(paramName);

		// cant tell, so just do it
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

		if (closeEnough) { nextPair = [paramName, value]; };
		^nextPair;
	}

	*getSpec { |obj, paramName|
		^obj.getSpec(paramName);
	}

	*multi { |obj ... pairs|
		var lastValDict, myWithin, nextPair, pairList;

		if (pairs.size.odd) {
			lastValDict = pairs.pop;
			myWithin = lastValDict[\within] ? defaultWithin;
			pairs.pairsDo { |key, val|
				nextPair = this.nextPair(obj, key, val, myWithin, lastValDict[key]);
				pairList = pairList ++ nextPair;
			};
		//	[\lastValDict, lastValDict].postln;
		} {
			pairs.pairsDo { |key, val|
				nextPair = this.nextPair(obj, key, val, myWithin);
				pairList = pairList ++ nextPair;
			};
		};

		obj.set(*pairList.postln);
	}

	*multiUni { |obj ... pairs|
		var lastValDict, myWithin, nextPair, pairList;

		if (pairs.size.odd) {
			lastValDict = pairs.pop ?? {()};
			myWithin = lastValDict[\within] ? defaultWithin;
		};
	//	[\lastValDict, lastValDict].postln;
		pairs.pairsDo { |key, val|
			nextPair = this.uniNextPair(obj, key, val, myWithin, lastValDict[key]);
			pairList = pairList ++ nextPair;
		};

		obj.setUni(*pairList);
	}
}
