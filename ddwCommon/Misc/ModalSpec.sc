
// a way to convert numbers to scale degrees and back
// understands sharps, but not flats
// with default ( ModalSpec([0, 2, 4, 5, 7, 9, 11], 12) ),
// 0 -> 0 (c), 1 -> 0.5 (c#)
// octaves are preserved in map/unmap operations

ModalSpec {
	var	<>scale,			// representation of intervals starting with 0: [0, 2, 4] = do re mi
		<>stepsPerOctave,	// what's top of scale?
		<>root,			// semitones above midinote 0
		<>tuning = 0,		// cpsFunc adds this to output frequency
		<cpsFunc;		// a function or Tuning object to convert note numbers to cps
	
	var	<degToKey, <keyToDeg;	// conversion dictionaries
	
	*new { arg scale, stepsPerOctave, root, tuning, cpsFunc;
		^super.newCopyArgs((scale ? #[0, 2, 4, 5, 7, 9, 11]), stepsPerOctave ? 12, root,
			tuning ? 0, cpsFunc).init;
	}
	
	*newFromKeys { arg keys, stepsPerOctave, tuning, cpsFunc;
		^this.new(keys - keys.first, stepsPerOctave, keys.first, tuning, cpsFunc)
	}

	storeArgs { ^[scale, stepsPerOctave, root, tuning] }
	
	init {	// build conversion tables for speed
			// keys are assumed to be int - DO NOT USE FRACTIONAL KEYS
			// for qtr tones, set stepsPerOctave = 24 and use your own frequency conversion
			// if you need fractional keys, you will need to customize this class
		var	temp;
		root = (root ? 0) % stepsPerOctave;
		keyToDeg = IdentityDictionary.new;
		temp = 0;		// i scans thru keys, temp thru degrees
		stepsPerOctave.do({ |i|
			(scale[temp] == i).if({
				keyToDeg.put(i, temp);	// found the exact value, put it in the dict
				temp = temp + 1;
			}, {
					// if we're between scale degrees, calculate the ratio
					// of my distance from previous degree vs distance between
					// previous and next degrees (i.e. in a major scale key 3 -> deg 1.5
				keyToDeg.put(i, (i - scale[temp-1])
					/ ((scale[temp] ? stepsPerOctave) - scale[temp-1])
					+ temp-1);
			});
		});
		degToKey = IdentityDictionary.new;
		keyToDeg.keysValuesDo({ |k, v|
			degToKey.put(v, k);	// reverse lookup
		});
		
			// cpsFunc_ provides a default
		this.cpsFunc = cpsFunc;
	}

		// note: using object.mapMode(mode) directly is slightly faster
	map { arg key;
		^key.mapMode(this)
	}
	
	unmap { arg degree;
		^degree.unmapMode(this)
	}

		// map key to degree (i.e., map chromatic value INTO the mode)
		// should work b/c keys were assigned as integers in .init
	prMap { arg key;
		^keyToDeg[(key-root) % stepsPerOctave]
				?? { keyToDeg[(key-root).asInteger % stepsPerOctave] }
			+ (((key-root) / stepsPerOctave).trunc * scale.size);
	}
	
		// map degree to key - 0 = root, 0.5 = root + halfway between steps 0 & 1
		// unmap the modal value OUT OF the mode
		// the null check here may yield incorrect results if a step is > 2 semitones
	prUnmap { arg degree;	// this method must ONLY receive a simplenumber
		var	num;
			// if close enough to an integer
			// some of my algo-comp stuff produces degrees like 28.000000000000014 ???
			// no time to fix it there, so I'll just improve the condition here
		^(((num = degree % 1).inclusivelyBetween(-1e-5, 1e-5)).if({
			degToKey[degree.asInteger % scale.size]
		}, {
			degToKey[degree.asInteger % scale.size] + (num * num.fuzzygcd(1).reciprocal)
		}))
			+ ((degree / scale.size).trunc * stepsPerOctave)
			+ root // + tuning;
	}
	
	addSteps { arg degree, steps;	// add steps semitones to degree
		var newkey;
			// convert to a 1-octave key representation and add steps
		newkey = degToKey[degree % scale.size] ?? { degToKey[degree.asInteger % scale.size] + 1 }
			+ steps;
			// convert back to degree - will wrap around to octave
		^(keyToDeg[(newkey-root) % stepsPerOctave]
				?? { keyToDeg[(newkey-root).asInteger % stepsPerOctave] })
				// add octave back in
			+ degree.trunc(scale.size)
			+ (newkey / stepsPerOctave).trunc;	// adjustment if newkey wrapped around
	}
	
	includes { arg key;	// does this midinote belong to the scale?
		^scale.includesEqual((key - root) % stepsPerOctave)
	}
	
	== { |that|
		(that === this).if({ ^true });	// easy case, skip the loop
		this.instVarSize.do({ |index|
			(this.instVarAt(index) != that.instVarAt(index)).if({ ^false });
		});
		^true
	}
	
	asMode { ^this }
	
		// convert modal note index to raw Hz using the assigned cpsFunc
	cps { |degree|
		^cpsFunc.value(degree.unmapMode(this))
	}
	
	cpsFunc_ { |func|
		cpsFunc = func ? { |midi| (midi + tuning).midicps };
	}
	
		// transpose the scale so that the root is different but the modal notes are the same
		// returns a new object
	transposeRoot { |newRoot = 0|
		var	keys = (0..scale.size-1).unmapMode(this),
			mappedRoot = this.prMap(newRoot),
			rotateAmt,
			first;

			// correct solution is unambiguous unless the mapped value is exactly halfway between integers
			// if 0.5, round down if first scale step is a half step
			// what if first scale step is 4 semitones?
		(mappedRoot - mappedRoot.trunc == 0.5).if({
			(scale[1] == 1).if({ rotateAmt = mappedRoot.trunc },
				{ rotateAmt = mappedRoot.roundUp });
		}, {
			rotateAmt = mappedRoot.round;
		});

			// e.g., if the scale is ebmaj and you want C to be the new root
			// C mapped into the scale is 5, so we need to shift the keys array
			// to the left 5 steps to bring C into the first position
			// roundUp is when this scale doesn't contain the new root -- then approximate
		keys = keys.rotate(rotateAmt.asInteger.neg);
		first = keys.first;
		^ModalSpec.newFromKeys(keys.collect({ |k| (k < first).if({ k + stepsPerOctave }, { k }) }),
			stepsPerOctave)
	}
}
