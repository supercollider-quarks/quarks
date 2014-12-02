
MFunc : AbstractFunction {

	var <funcDict, <orderedNames, <modes, <mode;
	var <activeNames, <activeFuncs, <activeIndices;
	var <modeLists;

	*new { |pairs, modes, initMode, modeLists|
		^super.new.init(pairs, modes, initMode, modeLists);
	}

	init { |pairs, argModes, argMode, argModeLists|
		funcDict = ();
		orderedNames = List[];
		activeFuncs = List[];
		activeNames = List[];

		pairs.pairsDo { |name, func|  this.addLast(name, func) };

		modes = argModes ?? { () };
		modes.put(\all, (on: { this.orderedNames }));
		modeLists = argModeLists ?? {()};
		modeLists.put(\all, { this.orderedNames });
		argMode = argMode ? \all;
		this.mode_(argMode);
	}

	// ... addAction, name2 - addFirst, addBefore, addAfter, addLast
	// ... remove(name)
	// provide anonymous addFunc, removeFunc like FunctionList ?
	// maybe for abckawrds compatibility?
	// but why do admin elsewhere?

	add { |name, func, active = true, addAction = \replace, otherName|
		this.perform(addAction, name, func, active, otherName);
	}

	replace { |name, func, active = true|
		var foundIndex;
		if (orderedNames.includes(name).not) {
			^this.addLast(name, func, active);
		};

		foundIndex = activeNames.indexOf(name);
		funcDict.put(name, func);
		if (foundIndex.notNil) {
			activeFuncs.put(foundIndex, func)
		};
	}

	addLast { |name, func, active = true| // no where
		if (func.isNil) { ^this };
		this.remove(name);
		funcDict.put(name, func);
		orderedNames.add(name);
		if (active) { this.enable(name) };
	}

	addFirst { |name, func, active = true| // no where
		if (func.isNil) { ^this };
		this.remove(name);
		funcDict.put(name, func);
		orderedNames.addFirst(name);
		if (active) { this.enable(name) };
	}

	addBefore { |name, func, active = true, otherName|
		var newIndex;
		if (func.isNil) { ^this };
		this.remove(name);
		funcDict.put(name, func);
		newIndex = orderedNames.indexOf(otherName);
		if (newIndex.isNil) {
			warn("MFunc:addBefore - otherName '%' not found.! adding to head.".format(name));
			this.addFirst(name, func, active);
		} {
			orderedNames.insert(newIndex, name);
			if (active) { this.enable(name) };
		};
	}

	addAfter { |name, func, active = true, otherName|
		var newIndex;
		if (func.isNil) { ^this };
		this.remove(name);
		funcDict.put(name, func);
		newIndex = orderedNames.indexOf(otherName);
		if (newIndex.isNil) {
			warn("MFunc:addAfter - otherName '%' not found.! adding to tail.".format(name));
			this.addLast(name, func, active);
		} {
			orderedNames.insert(newIndex + 1, name);
			if (active) { this.enable(name) };
		};
	}

	remove {|name|
		name.do { |nm|
			this.disable(nm);
			funcDict.removeAt(nm);
			orderedNames.remove(nm);
		};
	}

	enable { |names| names.do ( this.prEnable(_)) }

	prEnable { |name|
		var foundFunc, ordIndexOfName, indexToInsertAt;

		// already active
		if (activeNames.includes(name)) { ^this };

		foundFunc = funcDict[name];

		if (foundFunc.isNil) {
			"MFunc:enable : no func at name '%'.\n".postf(name);
			^this
		};

		ordIndexOfName = orderedNames.indexOf(name);
		if (ordIndexOfName.isNil) {
			"MFunc:enable : no index for name '%'.\n".postf(name);
			^this
		};

		indexToInsertAt = activeNames.detectIndex { |name, i|
			orderedNames.indexOf(name) > ordIndexOfName; };
		if (indexToInsertAt.isNil) {
			activeNames.add(name);
			activeFuncs.add(foundFunc);
		} {
			activeNames.insert(indexToInsertAt, name);
			activeFuncs.insert(indexToInsertAt, foundFunc);

		}
	}

	disable { |names|
		var func;
		names.do { |name|
			func = funcDict[name];
			activeNames.remove(name);
			activeFuncs.remove(func);
		}
	}

	value { |...args|
		^activeFuncs.array.collect(_.value(*args));
	}

	makeExclusiveModes { |name, modeList, modeNames|
		modeList = modeList ? modeLists[name];
		if (modeList.notNil) {
			modeLists.put(name, modeList);
		} {
			modeList = modeLists[name];
			if (modeList.isNil) {
				(thisMethod.asString + ": no mode named %.\n").postf(name);
				^this
			};
		};
		modeNames = modeNames ? modeList;
		modeNames.do { |modeName, i|
			modes.put(modeName, (on: modeList[i], off: name));
		}
	}

	mode_ { |name|
		var toDisable;
		var newMode = modes[name];
		if (newMode.isNil) {
			"MFunc:mode_ : no mode named '%' - no change.\n".postf(name);
			^this
		};
		mode = name;
		toDisable = newMode[\off];
		toDisable = modeLists[toDisable] ? toDisable;
		this.disable(toDisable.value).enable(newMode[\on].value);
	}
}

MFdef : MFunc {
	classvar <all;
	var <key;

	*initClass { all = () }

	*new { |key, pairs, modes, selectedName|
		var res = all.at(key);
		if (res.notNil) {
			if ( [pairs, modes, selectedName].any(_.notNil)) {
				"MFdef - ignore args here for now...".postln;
			};
			^res
		};
		^super.new(pairs, modes, selectedName).prKey(key);
	}

	prKey { |argKey| key = argKey; all.put(key, this); }
}