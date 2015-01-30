
/*
	RelSet is for infinite MIDI knobs that send relative values :
	move a single param of an object by a relative normalized amount.
	let obj provide the spec, if there is none, make it unipolar.

// tests:
Ndef(\a).gui.moveTo(0,400);

// does nothing (no current value yet)
RelSet(Ndef(\a), \freq, 0.05);

// works when there is a value
set(Ndef(\a), \freq, 200);
RelSet(Ndef(\a), \freq, 0.05);

// set to the middle, and do brownian param drift
set(Ndef(\a), \freq, 600);
fork { 30.do { RelSet(Ndef(\a), \freq, 0.05.rand2); 0.2.wait }; };

*/

RelSet {

	*new { |obj, paramName, relVal, spec|
		var currVal, currValNorm, keyVal;
		currVal = obj.get(paramName);
		spec = (spec ?? { this.getSpec(obj, paramName); }).asSpec;

			// cant set relative if object has no currVal
		if (currVal.isNil) { ^nil };

		if (spec.isNil or: spec == \none) {
			^obj.set(paramName, (currVal + relVal).clip(0, 1));
		};

		currValNorm = spec.unmap(currVal);
		obj.set(paramName, spec.map(currValNorm + relVal));
	}

	*getSpec { |obj, paramName|
		^obj.getSpec(paramName);
	}
}
