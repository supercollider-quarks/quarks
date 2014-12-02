// proxy has an envir for parameters
// typically nuemrical, may also be different.
// if different, special handling needed.
// ProxyPreset

ProxyPreset {

	var <proxy, <namesToStore, <settings, <specs, <>morphFuncs;
	var <currSet, <targSet, count = 0, <>morphVal = 0, <morphTask;

	var <>storeToDisk = false, <>storePath;


	*new { |proxy, namesToStore, settings, specs, morphFuncs|

		if (proxy.isNil) {
			warn("EnvirPreset cannot be empty!");
			^nil
		};

		^super.newCopyArgs(proxy, namesToStore,
			settings, specs, morphFuncs).init;
	}

	init {
		this.useHalo;
		this.initTask;
		this.checkSpecsMissing;
		// init morph here already?
		this.currFromProxy;
		this.setCurr(\curr);
		this.setTarg(\curr);
	}

	checkSpecsMissing { |autoFill = false, dialog = false|
		// for all missing specs, ask user ...

		var missingSpecNames = namesToStore.select {|name|
			var spec;
			proxy.getSpec(name).isNil;
		};

		if (missingSpecNames.notEmpty) {
			"please supply specs or special funcs for these param names:".postln;
			missingSpecNames.postln;
			if (dialog) { this.specsDialog(missingSpecNames) };
		};
	}

	useHalo {

		var haloNames = proxy.getHalo(\namesToStore);
		var haloSpecs = proxy.getSpec(\spec);

		if (namesToStore.isNil) {
			namesToStore = haloNames ?? { proxy.controlKeys.asArray.sort };
			proxy.addHalo(\namesToStore, namesToStore);
		} {
			if (haloNames.notNil) {
				warn("ProxyPreset: namesToStore given: "
					+ namesToStore.asCompileString +
					"\n  and in proxy halo: "
					+ haloNames.asCompileString ++ "!"
					"\n  using haloNames.");
			} {
				proxy.addHalo(\namesToStore, namesToStore);
			};
		};

		if (specs.isNil) {
			specs = proxy.getSpec ?? { () };
			proxy.addHalo(\namesToStore, namesToStore);
		} {
			if (haloSpecs.notNil) {
				warn("ProxyPreset: specs given: "
					+ specs.asCompileString +
					"\n  and in proxy halo: "
					+ haloSpecs.asCompileString ++ "!"
					"\n  using haloSpecs.");
			} {
				proxy.addHalo(\spec, haloSpecs);
			};
		};

			// settings and morphFuncs belong to preset:
		settings = settings ?? { List[] };
	}

	initTask {

		morphTask = TaskProxy({ |ev|
			var numSteps;
			ev[\dt] = ev[\dt] ? 0.01;
			ev[\morphTime] = ev[\morphTime] ? 1;
			this.prepMorph;

			numSteps = ev[\morphTime] / ev[\dt];
			numSteps.do { |i|
				this.morph(1 + i / numSteps);
				ev[\dt].wait;
			};
			ev[\doneFunc].value;
		});
	}

	addSet { |name, values, toDisk=false|
		var index;
		if (name.isNil, { count = count + 1; name = "set" ++ count; });
		name = name.asSymbol;
		index = this.getIndex(name);

		// - NDEF-specific! abstract out later //
		values = values ?? { this.getFromProxy.copy };

		// writeBackup
		if (toDisk) {
			this.writeSettings(storePath.splitext.insert(1, "_BK.").join, true);
		};

		if (index.notNil) {
			settings.put(index, name -> values)
		} {
			settings.add(name -> values);
		};
		// friendlier with auto-backup...
		if (toDisk) { this.writeSettings(overwrite: true); };
	}

	removeSet { |name|
		var index = this.getIndex(name);
		if (index.notNil, { settings.removeAt(index) });
	}

	addSettings { |list|
		list.do { |assoc| this.addSet(assoc.key, assoc.value) };
	}
	removeSettings { |names| names.do(this.removeSet(_)) }

	getSetNames { ^settings.collect(_.key) }

	getIndex { |name| ^settings.detectIndex({ |assoc| assoc.key == name }) }

	currIndex { ^settings.indexOf(currSet) }
	targIndex { ^settings.indexOf(targSet) }

	getSet { |name|
		var index = this.getIndex(name);
		^if (index.notNil) { settings[index] } { nil };
	}

	setCurr { |name|
		var foundSet = this.getSet(name);
		if (foundSet.notNil) {
			currSet = foundSet;
			proxy.set(*currSet.value.flat);
			this.morphVal_(0);
		};
	}

	setTarg { |name, setCurr=true|
		var foundSet;
		foundSet = this.getSet(name);
		if (foundSet.notNil) { targSet = foundSet; };
		if (setCurr) { this.prepMorph; };
	}

	currFromProxy {
		this.addSet(\curr, this.getFromProxy);
	}

	// assume proxy has an environment
	// to do - this could also be supported
	// with proxy.getKeysValues, as with NodeProxies.
	getFromProxy { |except|
		var envir = proxy.envir;
		var res = [];
		if (envir.isNil) { ^[] };
		namesToStore.copy.removeAll(except).collect { |name|
			var val = envir[name];
			res = res.add([name, envir.at(name)]);
		};
		^res
	}

	stepCurr { |incr=1|
		var currIndex = settings.indexOf(currSet) ? 0;
		this.setCurr(settings.wrapAt(currIndex + incr).key);
	}

	stepTarg { |incr=1|
		var targIndex = settings.indexOf(targSet) ? 0;
		this.setTarg(settings.wrapAt(targIndex + incr).key);
	}

	setProxy { |name| proxy.set(*this.getSet(name).value.flat) }


	// STORAGE to Disk:
	// keep them next to the text file they are created with;
	// or maybe make a preset/settings folder?

	setPath { |name|
		this.storePath_(this.presetPath(name)); // make it once
	}

	presetPath { |name|
		^(thisProcess.nowExecutingPath ? "").dirname
		+/+ (name ?? { this.proxy.key ++ ".pxpreset.scd" });
	}

	loadSettings { |path, clear = false|
		path = path ?? { this.storePath ?? { this.setPath; this.storePath } };
		if (clear) { settings.clear };
		this.addSettings(path.load);
	}

	writeSettings { |path, overwrite=false|
		var file;
		path = path ?? { this.storePath };
		// check first and copy as backup ...
		if (overwrite.not) {
			if (File.exists(path)) {
				warn("ProxyPreset: file" + path + " exists!");
				^this;
			}
		};

		file = File(path, "w");
		file.write(this.settings.asCompileString);
		file.close;
	}


	// randomize settings:

	randSet { |rand=0.25, startSet, except|

		var randKeysVals, set, randRange;
		// vary any given set too?
		set = this.getSet(startSet).value ?? {
			this.getFromProxy(except);
		};

		if (except.notNil) {
			set = set.reject{ |pair| except.includes(pair[0]); };
		};

		randKeysVals = set.collect { |pair|
			var key, val, normVal, randVal, spec;
			#key, val = pair;
			spec = proxy.getSpec(key);
			if (spec.notNil, {
				normVal =  spec.unmap(val);
				randVal = rrand(
					(normVal - rand).max(0),
					(normVal + rand).min(1)
				);
				// [key, val, normVal].postcs;
				[key, spec.map(randVal)];
			}, { "no spec: ".post;
				[key, val].postcs });
		};
		^randKeysVals;
	}


	someRand { |rand=0.1, ratio = 0.5|

		var keys = namesToStore;
		var numToKeep = (keys.size * ratio).clip(1, keys.size).round(1).asInteger;
		var namesToDrop = keys.scramble.drop(keys.size - numToKeep);
		this.setRand(rand, except: namesToDrop);
	}

	setRand { |rand, startSet, except|
		rand = rand ?? { exprand(0.001, 0.25) };
		proxy.set(*this.randSet(rand, startSet, except).flat);
		this.prepMorph;
	}


	// morphing:
	blendSets { |blend = 0.5, set1, set2|
		^set1.blend(set2, blend);
	}

	prepMorph {
		this.currFromProxy;
		this.setCurr(\curr);
		this.morphVal_(0);
	}

	morph { |blend, name1, name2, mapped=true|
		morphVal = blend;
		proxy.set(*(this.blend(blend, name1, name2, mapped).flat));
	}

	xfadeTo { |target, dur, doneFunc|
		var newTargSet;
		if (target.notNil) {
			newTargSet = this.getSet(target);
			if (newTargSet.notNil) {
				targSet = newTargSet;
			} {
				"ProxyPreset: target setting % not found - not xfading.".postf(target);
			};
			morphTask.set(\morphTime, dur);
			morphTask.set(\doneFunc, doneFunc);
			morphTask.stop.play;
		};
	}

	blend { |blend = 0.5, name1, name2, mapped=true|
		var set1, set2;
		set1 = if (name1.isNil, currSet, { this.getSet(name1) }).value;
		set2 = if (name2.isNil, targSet, { this.getSet(name2) }).value;

		if (blend == 0) { ^set1 };
		if (blend == 1) { ^set2 };

		if (set1.isNil) {
			"cannot blend: set % is missing.\n".postf(name1);
			^this;
		};
		if (set2.isNil) {
			"cannot blend: set % is missing.\n".postf(name2);
			^this;
		};

		if (mapped) {
			set1 = this.unmapSet(set1);
			set2 = this.unmapSet(set2);
			^this.mapSet(this.blendSets(blend, set1, set2))
		} {
			^this.blendSets(blend, set1, set2)
		}
	}

	// expects just list of [key, val]s
	mapSet { |set|
		var key, val;
		^set.collect { |pair|
			#key, val = pair;
			[key, specs[key].map(val)]
		}
	}
	// expects just list of [key, val]s
	unmapSet { |set|
		var key, val;
		^set.collect { |pair|

			#key, val = pair;
			[key, specs[key].unmap(val)]
		}
	}

	postSettings {
		this.as
		("<pxPresetNameHere>.addSettings(" + settings.asCompileString + ")")
		.newTextWindow(proxy.key ++ ".pxpreset.scd");
	}

	storeDialog { |name, loc| 		// check before overwriting a setting?
		var w;
		loc = loc ?? {400@300};
		if (name.isNil, { count = count + 1; name = "set" ++ count; });
		w = Window("", Rect(loc.x, loc.y + 40, 150, 40), false);
		StaticText(w, Rect(0,0,70,20)).align_(\center).string_("name set:");
		TextField(w, Rect(70,0,70,20)).align_(\center)
		.string_(name)
		.action_({ arg field;
			this.addSet(field.value.asSymbol, toDisk: storeToDisk);
			w.close;
		})
		.focus(true);
		w.front;
	}

	deleteDialog { |loc|
		var win, names, ezlist;
		var winOrigin, winSize = (150@200);

		names = this.getSetNames;
		names.remove(\curr);
		loc = loc ?? { (100@400) };
		winOrigin = loc - winSize;

		win = Window("delete", Rect(winOrigin.x, winOrigin.y, 150,200)).front;
		win.addFlowLayout;
		ezlist = EZListView(win, win.bounds.insetBy(4, 4),
			"DELETE presets from\n%:"
			"\nselect and backspace".format(this),
			names, nil, labelHeight: 50);
		ezlist.labelView.align_(\center);
		ezlist.view.resize_(5);
		ezlist.widget.resize_(5);
		ezlist.widget.keyDownAction_({ |view, char|
			if(char == 8.asAscii) {
				this.removeSet(view.items[view.value].postln);
				view.items = this.getSetNames;
			};
		});
		^win
	}

	 specsDialog { |keys, specDict|

		var w, loc, name, proxyKeys, specKeys;
		specDict = specDict ? specs;

		 loc = loc ?? {400@300};
		w = Window("specs please", Rect(loc.x, loc.y + 40, 300, 200)).front;
		w.addFlowLayout;
		StaticText(w, Rect(0,0,290,50)).align_(\center)
		.string_(
			"Please enter specs for the following\nparameter keys:"
			"\n(min, max, warp, step, default, units)"
		);

		keys.collect { |key|
			var guessedSpec = Spec.guess(key, proxy.get(key)).storeArgs;
			var eztext;
			eztext = EZText(w, Rect(70,0,290,20), key, { |ez|
				var spec = ez.value.asSpec;
				specDict.put(key, spec);
				[key, spec].postcs;
				},
				guessedSpec
			);
		};
	}
}
