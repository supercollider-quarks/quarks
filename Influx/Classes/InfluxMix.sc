InfluxMix : InfluxBase {

	classvar <defaultMergeFunc;

	var <trusts, <>mergeFunc, <>damping = 0.5;

	*initClass {
		Class.initClassTree(Spec);
	//	Class.initClassTree(Halo);
	//	this.addSpec(\damping, [0, 1]);
		Spec.add(\damping, [0, 1]);

		// damping 0 is linear sum of contribs,
		// damping 0.5 is scaled by sqrt of contribs (equal power sum)
		// damping 1 is linear average

		defaultMergeFunc = { |in, out, trusts, damping = 0.5|
			var outval, contrib;
			in.keysValuesDo { |paramKey, values|
				if (values.size > 0) {
					outval = 0;
					values.keysValuesDo { |srcName, val|
						contrib = val * (trusts[srcName]);
						outval = outval + contrib;
					};
					outval = outval / (values.size ** damping);
					out.put(paramKey, outval);
				}
			}
		}
	}

	*new { |inNames|
		^super.newCopyArgs(inNames).init;
	}

	init {
		super.init;
		inNames.do(inValDict.put(_, ()));
		trusts = ();
		mergeFunc = mergeFunc ? defaultMergeFunc
	}

	set { warn("InfluxMix - cannot use set, please use influence!"); }

	// only accept the paramKeys we expect
	// later - remember time when last set command happened?
	influence {|who ... keyValPairs|

		keyValPairs.pairsDo { |paramKey, val|
			if (inNames.includes(paramKey)) {
				inValDict[paramKey].put(who, val);
			};
		};
		this.checkTrusts(who);
		mergeFunc.value(inValDict, outValDict, trusts, damping);
		action.value(this);
	}

	checkTrusts { |who|
		if (inNames.includes(who.not)) { inNames.add(who) };
		if (trusts[who].isNil) { trusts[who] = 1 };
	}

	trust { |srcName, val|
		trusts.put(srcName, val);
		mergeFunc.value(inValDict, outValDict, trusts, damping);
		action.value(this);
	}
}
