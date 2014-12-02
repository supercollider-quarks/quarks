	// This version of the Manta class implements the scaling and noteOn logic suggested by AdC in SC3.
	// It works with MantaCocoa 0.97 and 0.97.1, with unscaled data, centroid,
	// noteOn, velocity and centroid turned off,
	// so that the Manta class does all the logic for these!

	// this may revert to Manta as before when MantaCocoa does these directly.

Manta {

	classvar <>verbose = false;
	classvar <>appPath = "/Applications/manta*/MantaCocoa.app";
	classvar <setups;
	classvar <messageNames;

	var <addr, <responders, <globalActions, <actions, <multi, <indiv;
	var <values, <buses;
	var <usesTops, <numPads, <topNames;
	var <listenAddr, <canSetLEDs = false;
	var <padMaxima, <sliderMaxima, maxPath;

	var <>normed = true;
	var <>usesNoteOn = true, <usesCentroid = false;
	var <noteOnCounts,  <lastNValues, <resendTime;

	var <centroid = #[0,0,0], <>centroidNeedsUpdate = true;
	var <centroidIndices, <centroidSkip;

	*initClass {
		messageNames = #[
			'/manta/slider', '/manta/value',
			'/manta/noteOn', '/manta/noteOff', '/manta/velocityValue',
			'/manta/centroid'];

		setups = ();
	}

	*start { unixCmd("open" + appPath) }

		// preferences get corrupted quite often,
		// then one gets an error with index: -1.
	*killPrefs { unixCmd("rm" + "~/Library/Application\\ Support/MantaCocoa/lastSettings.xml") }

		// addr can be used for identifying multiple Mantas
	*new { |addr, addResps = true, makeBuses = true, server, normed = true,
		usesTops = true, usesNoteOn = false, usesCentroid = false|

		^super.newCopyArgs(addr).normed_(normed).usesNoteOn_(usesNoteOn)
			.init(addResps, makeBuses, server, usesTops);
	}

		// cleanup if multiple resps were added by accident
	*removeAllResps {
		OSCresponder.all.copy.do { |x|
			if (messageNames.includes(x.cmdName)) { x.remove; "removed resp: %\n".postf(x.cmdName) }
		}
	}

	defaultPadMaxima {
		// measured with AdC's Manta - substitute your own.
		^[ 	1,
			177, 184, 187, 188, 192, 192, 183, 180,
			188, 193, 197, 200, 201, 201, 193, 188,
			192, 197, 202, 205, 205, 204, 199, 191,
			201, 207, 210, 215, 215, 214, 209, 200,
			204, 210, 215, 219, 212, 218, 212, 203,
			211, 215, 221, 226, 222, 227, 221, 213,

			219, 199, 199, 206
		];
	}

	defaultSliderMaxima {
		// my sliders go fram ca 5 minimum to ca 4090 max.
		// the top and bottom values can't be reached reproducibly,
		// so better cut them off a little earlier.

		// so these limits are hardcoded in the slider responder!
		// ^[ 1, 4096 - 10, 4096 - 10];
	}

//		// later: measure, write, read manta data ...
//	measureMaxima { |clear = true, plot = true|
//			// if clear, set maxima to 1!52, 1!2;
//
//			// if (plot) { make a plot view that is updated };
//		this.addResps;
//			// set their actions to overwrite old maxima
//		this;
//	}
//
//	writeMaxima { |path| }
//
//	readMaxima { |path| path = path ? maxPath; }

	init { |addResps, makeBuses, server, useTops|

		usesTops = useTops;
		numPads = if (useTops, 48, 52);

		// safe against re-adding multiple responders
		this.removeResps;

		maxPath = Platform.userAppSupportDir +/+ "MantaMaxima.scd";

		padMaxima = this.defaultPadMaxima;
		sliderMaxima = this.defaultSliderMaxima;

		responders = (
			sliderResp: OSCresponderNode(addr, '/manta/slider', { |t, r, msg|
				var key = msg[1];
				var val = msg[3];
						// normal values hardcoded for efficiency
				if (normed) { val = (val - 10 / 4070).clip(0, 1) };
				this.respond(\slider, key, val)
			}),
			padResp: OSCresponderNode(addr, '/manta/value', { |t, r, msg|
				var key = msg[1];
				var val = msg[3];
					// maxvalues vary quite a bit for the different pads
				if (normed) { val = val / padMaxima[key] };
				this.respond(\pad, key, val);
			})
				// off for the time being, until noteOnLogic and scaling are done in MantaCocoa
//			,
//			noteOnResp: OSCresponderNode(addr, '/manta/noteOn', { |t, r, msg| this.respond(\noteOn, msg[1], msg[3]) }),
//			noteOffResp: OSCresponderNode(addr, '/manta/noteOff', { |t, r, msg| this.respond(\noteOff, msg[1], msg[3]) }),
//			velocityResp: OSCresponderNode(addr, '/manta/velocityValue', { |t, r, msg| this.respond(\velocity, msg[1], msg[3]) }),
//
//			centroidResp: OSCresponderNode(addr, '/manta/centroid', { |t, r, msg| this.respond(\centroid, \multiOnly, msg[1..3]) })
		);

		globalActions = (
			multi: (),
			indiv: (
				slider: (),
				pad: (),
				noteOn: (),
				noteOff: (),
				velocity: (),
				centroid: ()
			)
		);

		multi = ();

		indiv = (
			slider: (),
			pad: (),
			noteOn: (),
			noteOff: (),
			velocity: (),
			centroid: ()	// just a dummy to save one check for nil - centroid should not do any indiv actions.
		);

		actions = (multi: multi, indiv: indiv);

		values = (slider: 0!2, centroid: 0!3, pad: 0 ! numPads, note: 0 ! numPads, velocity: 0 ! numPads);

		if (usesTops) {
			topNames = (pad: \topButton, velocity: \topVelocity, noteOn: \topNoteOn, noteOff: \topNoteOff);
			indiv.putAll((topButton: (), topVelocity: (), topNoteOn: (), topNoteOff: ()));
			values.putAll((topButton: 0!4, topVelocity: 0!4, topNote: 0!4));

			globalActions[\indiv].putAll((topButton: (), topVelocity: (), topNoteOn: (), topNoteOff: ()))
		};

		if (addResps) { this.addResps };
		if (makeBuses) { this.makeBuses(server) };

		noteOnCounts = 0 ! 53;
		lastNValues = [0,0,0] ! 53;

		this.prepCentroid;
		if (usesCentroid) { centroidSkip.start; };

	}

	prepCentroid {
		centroidIndices = (1..6).collect { |i| (1..8).collect ([i, _]) }.flatten(1);
		centroidSkip = centroidSkip ?? {
			SkipJack({
				// "cent task.".postln;
				if (centroidNeedsUpdate, { this.calcCentroid; })
			}, 0.008, false, 'MantaCentroid',
			autostart: false)
		};
	}

	calcCentroid {
		var weightSum = values[\pad].sum;
		var xy = [0,0];

		if (weightSum > 0) {
			xy = centroidIndices.sum { |pair, i| (pair * values[\pad][i]) } / weightSum;

		} {
			// remember last xy, - or comment this out to get [0,0,0]
			xy = xy ? centroid.keep(2);
		};
		centroidNeedsUpdate = false;
		centroid = xy ++ (weightSum);
		if (verbose) { "			centroid: (x, y, weight) = %\n".postf(centroid) };
		this.respond(\centroid, \multiOnly, centroid);
	}

	usesCentroid_ { |flag|
		usesCentroid = flag;
		if (flag) { centroidSkip.start } {  centroidSkip.stop };
	}

	makeBuses { |server|

		buses = (
			pad: Bus.control(server, numPads),
			note: Bus.control(server, numPads),
			slider: Bus.control(server, 2),
			velocity: Bus.control(server, numPads),
			centroid: Bus.control(server, 3)
		);

		if (usesTops) {
			buses.putAll((
				topButton: Bus.control(server, 4),
				topVelocity: Bus.control(server, 4),
				topNote: Bus.control(server, 4)
			))
		};
	}

		// add just some responders if needed - by default, add all.
	addResps { |keys|
		if (keys.notNil) { keys.do { |key| responders[key].add } } { responders.do(_.add) };
	}

	removeResps { |keys|
		if (keys.notNil) { keys.do { |key| responders[key].remove } } { responders.do(_.remove) };
	}

	free {
		this.removeResps;
		buses.do(_.free);
		try { listenAddr.disconnect };
	}

	repeatPadFunc { |name, key, val, dt = 0.009|
		fork {
			var count = noteOnCounts[key];
			dt.wait;

			if (count == noteOnCounts[key]) {
			//	"noteOn - resending to replace repeated pad value!".postln;
				this.respond(name, key, val)
			};
		};
	}

	noteOnLogic { |name, key, val|
		var velocity = -1;
		var noteOnCount = noteOnCounts[key];
		var lastN = lastNValues[key];

		if (val == 0) { // noteOff logic first:
		//	"noteOff: key % val %\n\n\n".postf(key, val);
			lastNValues[key] = [0,0,0];
			noteOnCounts[key] = 0;
			this.respond(\noteOff, key, 0);

		} {
			if (noteOnCount < 3) {
				lastNValues[key].put(noteOnCount, val);

				if (noteOnCount < 2) { this.repeatPadFunc(name, key, val) };

				if (noteOnCount == 2) {
					// ok, enough values for noteOn + velocity!

					velocity = lastNValues[key].maxItem;
					this.respond(\noteOn, key, velocity);

//					"\n **** NOTEON: key %, normVel % last 3 values %\n"
//						.postf(key,
//							lastNValues[key].maxItem,
//							lastNValues[key]);
				};
			};

			noteOnCounts[key] = noteOnCount + 1;
		};

//		" post noteOnLogic: key %, val %, count %, lastN %, vel % \n---\n\n".postf(
//			key, val.round(0.0001),
//			noteOnCounts[key],
//			lastNValues[key].round(0.0001),
//			velocity
//		);

	}

	respond { |name, key, val|

			// noteOn, noteOff logic only for pads:
		if (name == \pad and: usesNoteOn, { this.noteOnLogic(name, key, val) });
		if (name == \pad and: usesCentroid, { centroidNeedsUpdate = true });

			// do redirection for topButtons here:
		if (usesTops and: { key > numPads }) { name = topNames[name] ? name };

		if (verbose) { "Manta %, % % \n".postf(name, key, val) };

			// remember current state
		this.keepVals(name, key, val);

			// do globalActions first
		globalActions[\multi][name].value(key, val);
			// then indiv actions only for pad, slider etc
		globalActions[\indiv][name][key].value(val); // centroid does nothing

			// then multi actions
		multi[name].value(key, val);
			// indiv only for pad, slider etc
		indiv[name][key].value(val);
	}

	keepVals { |name, index, val|
		var offset;

		if (name == \centroid) {
			values[name] = val;
			if (buses.notNil) { buses[\centroid].setn(val) };
			^this
		};

		offset = if (#[\topButton, \topVelocity, \topNoteOn, \topNoteOff].any(_ == name), numPads + 1, 1);
		index = index - offset;
		name.switch(
			\noteOff, { name = \note; val = 0; },
			\topNoteOff, { name = \topNote; val = 0; },
			\noteOn, {
				values[\velocity].put(index, val);
				if (buses.notNil) { buses[\velocity].setAt(index, val) };
				name = \note; val = 1;
			},
			\topNoteOn, {
				values[\topVelocity].put(index, val);
				if (buses.notNil) { buses[\topVelocity].setAt(index, val) };
				name = \topNote; val = 1;
			}
		);

		values[name].put(index, val);
		if (buses.notNil) { buses[name].setAt(index, val) };
	}



	// adding/removing functions on different levels:

		// global multi functions:
	addMultiGlobal { |type, func| globalActions.multi.put(type, func) }
	removeMultiGlobal { |type| type.asArray.do { |key| globalActions.multi.put(key, nil) } }

		// global functions for individual pads, sliders, events.
	addGlobal { |type, key, func|
		globalActions.indiv[type].put(key, func)
	}
	removeGlobal { |type, key|
		key.asArray.do { |key| globalActions.indiv[type].put(key, nil) }
	}

		// multi functions:
	addMulti { |type, func| multi.put(type, func) }
	removeMulti { |type| type.asArray.do (multi.put(_, nil)) }

		// individual functions per buttons and slider
	add { |type, key, func| indiv[type].put(key, func) }
	remove { |type, key| indiv[type].put(key, nil) }

		//add functionality to specific setups
		// multi functions:
	addMultiToSetup { |setupName, type, func|
		setups[setupName].multi.put(type, func)
	}
	removeMultiFromSetup { |setupName, type|
		var setup = setups[setupName];
		type.asArray.do (multi.put(_, nil))
	}

		// individual funcs for pads, sliders etc
	addToSetup { |setupName, type, key, func|
		setups[setupName][\indiv][type].put(key, func)
	}
	removeFromSetup { |setupName, type, key|
		setups[setupName][\indiv][type].put(key, nil)
	}

		// store setups -
	clearDicts {
		multi = ();
		indiv.keysDo { |k| indiv[k] = () };
		actions.put(\multi, multi, \indiv, indiv);
	}

	store { |name|
		name = name.asSymbol;
		"Manta: storing current setup as %.\n".postf(name.asCompileString);
		setups.put(name, actions.deepCopy);
	}

	setup { |name|
		var setup = setups.at(name.asSymbol);
		if (setup.isNil) { warn("Manta - no setup named %! Keeping current setup.\n".format(name.asSymbol)); ^this };

		if (name != \backup) { this.store(\backup) };

		"Manta: switching to setup %.\n".postf(name.asCompileString);
		actions = setup.deepCopy;
		multi = setup[\multi];
		indiv = setup[\indiv];
	}

		// set LEDs
	makeListenAddr { |ip = "127.0.0.1", port = 8000|
		listenAddr = NetAddr(ip, port);
	}

	canSetLEDs_ { |flag = true|
		if (listenAddr.isNil) { this.makeListenAddr };
		canSetLEDs = flag;
		listenAddr.sendMsg("/manta/ledControlEnabled", flag.binaryValue);
	}
	setLEDs { |val = 1 ... indices|
		listenAddr.sendMsg("/manta/leds", indices.size, *(indices ++ val));
	}
}

