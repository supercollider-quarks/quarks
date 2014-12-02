/* required variants:
1. prepare values :
   * raw outValDict
   * opt. rescale ?
   * opt. shift by offset ?

   a. keep orig ranges and names, or
      take dest. names

   b. remap to dest. ranges

     // optimize them out
   setRaw
   setRawNames
   setScaled
   setScaledNames
   influRaw
   influRawNames
   influScaled
   influScaledNames

2. sending: setCmd, or influenceCmd + sourceName

FuncChain

*/
InfluxSpread : InfluxBase {
	// make several default sendFuncs,
	// choose between them later
	classvar <>sendFuncs;

	var <destsDict;

	*initClass {
		sendFuncs = (
			\setMapped: { |destDict, infspr|
				// \sendMapped
				var sendVals = infspr.remapFor(destDict);
				destDict[\object].set(*sendVals.asKeyValuePairs);
			},
			\infMapped: { |destDict, infspr|
				// \influMapped
				var sendVals = infspr.remapFor(destDict);
				destDict[\object].influence(
					destDict[\srcName],
					*sendVals.asKeyValuePairs
				);
			}
		);
	}

	setScaler { |name, val|
		destsDict[name].put(\scaler, val);
		action.value(this);
	}

	setOffsets { |name, values|
		destsDict[name].put(\offsets, values);
		action.value(this);
	}

	init {
		super.init;
		destsDict = ();
	}

	remapFor { |destDict|
		// have names to send for each destination?
		var newDict;
		var sendVals = inValDict;
		var object = destDict[\object];
		var paramMap = destDict[\paramMap];
		var objSpecs = destDict[\specs];
		var scaler = destDict[\scaler];
		var offsets = destDict[\offsets];

		// list of ifs may be confusing -
		// - better untangle into spearate methods?

		if (paramMap.notNil) {
			newDict = ();
			sendVals.keysValuesDo { |key, val|
				//	[key, paramMap[key], val].postcs;
				if (paramMap[key].notNil) {
					newDict.put (paramMap[key], val);
				};
			};
			sendVals = newDict;
		};

		if (scaler != 1) {
			sendVals = sendVals.collect(_ * scaler);
		};

		if (offsets.notNil) {
			// check if complete?
			sendVals = sendVals.collect({|val, key|
				val + (offsets[key] ? 0) });
		};

		if (objSpecs.notNil) {
			sendVals = sendVals.collect { |value, key|
				// if no spec found, pass as is.
				var spec = objSpecs[key];
				if (spec.notNil) {
					spec.map(value.biuni);
				} {
					value
				};
			};
		};

		^sendVals;
	}

	addDest { |name, object, specs, paramMap, sendFunc,
		srcName, offsets, scaler|
		var destDict = destsDict[name];
		destDict ?? { destsDict[name] = destDict = () };

		destDict.put(\name, name);
		destDict.put(\object, object);
		// this may not be the best way to do this.
		// adding specs to destsDict.specs
		// will write into object halo specs...
		destDict.put(\specs, specs ?? { object.getSpec });
		destDict.put(\paramMap, paramMap);
		destDict.put(\scaler, scaler ? 1);

		// sendFunc can be a func or a symbol or nil

		sendFunc = sendFunc ? \sendMapped;
		if (sendFuncs[sendFunc].notNil) {
			destDict.put(\sendFunc, sendFuncs[sendFunc]);
		} {
			// assume its a function
			destDict.put(\sendFunc, sendFunc);
		};

		action.add(name, { |infspr|
			destDict[\sendFunc].value(destDict, infspr)
		});
	}

}
