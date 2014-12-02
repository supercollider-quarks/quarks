
Conductor : Environment {
	classvar <>specs;
	var <>valueKeys, <path;
	var <>gui;			// defines gui display of conductor in windows
	var <>player;
	var <>preset;
	
	*initClass {
		StartUp.add ({
			Conductor.specs = IdentityDictionary.new;
			Conductor.specs.putPairs([
				// set up some ControlSpecs for common mappings
				// you can add your own after the fact.
				
				unipolar: 	ControlSpec(0, 1),
				bipolar: 		ControlSpec(-1, 1, default: 0),
				
				i_out:		ControlSpec(0, 1023, 'lin', 1, 0),
				out:			ControlSpec(0, 1023, 'lin', 1, 0),
				in:			ControlSpec(0, 1023, 'lin', 1, 0),
	
				freq: 		ControlSpec(20, 20000, \exp, 0, 440, units: " Hz"),
				lofreq: 		ControlSpec(0.1, 100, \exp, 0, 6, units: " Hz"),
				midfreq: 		ControlSpec(25, 4200, \exp, 0, 440, units: " Hz"),
				widefreq: 	ControlSpec(0.1, 20000, \exp, 0, 440, units: " Hz"),
				phase: 		ControlSpec(0, 2pi),
				rq: 			ControlSpec(0.001, 2, \exp, 0, 0.707),
	
				audiobus: 	ControlSpec(0, 127, step: 1),
				controlbus: 	ControlSpec(0, 4095, step: 1),
				in: 			ControlSpec(0, 4095, step: 1),
				fin: 		ControlSpec(0, 4095, step: 1),
	
				midi: 		ControlSpec(0, 127, default: 64),
				midinote: 	ControlSpec(0, 127, default: 60),
				midivelocity: ControlSpec(1, 127, default: 64),
				
				
				dbamp: 		ControlSpec(0.ampdb, 1.ampdb, \db, units: " dB"),
				amp: 		ControlSpec(0, 1, \amp, 0, 0),
				boostcut: 	ControlSpec(-20, 20, units: " dB",default: 0),
				db: 			ControlSpec(-100, 20, default: -20),
				
				pan: 		ControlSpec(-1, 1, default: 0),
				detune: 		ControlSpec(-20, 20, default: 0, units: " Hz"),
				rate: 		ControlSpec(0.125, 8, \exp, 0, 1),
				beats: 		ControlSpec(0, 20, units: " Hz"),
				ratio: 		ControlSpec(1/64, 64, \exp, 0, 1),
				dur: 		ControlSpec(0.01, 10, \exp, 0, 0.25),
				
				delay: 		ControlSpec(0.0001, 1, \exp, 0, 0.3, units: " secs"),
				longdelay: 	ControlSpec(0.001, 10, \exp, 0, 0.3, units: " secs"),
	
				fadeTime: 	ControlSpec(0.001, 10, \exp, 0, 0.3, units: " secs")
				
			]);
	 })
	}
	
	*postSpecs {
		var sp;
		specs.keys.asSortedList.do { | key |
			sp = Conductor.specs[key];
			if (sp.class == ControlSpec) { 
				key.postL(15);
				sp.default.postL;
				sp.minval.postL;
				sp.maxval.postL;
				sp.warp.class.postLn(25);
			}
		}
	}
	
	*addSpec { | name, spec |
		specs[name.asSymbol] = spec;
	}
		
	*make { arg func; 
		var obj, args, names;
		obj = this.new;
		^obj.make(func)
	}
	
	*new { ^super.new.init }
	
	init {
		gui = ConductorGUI(this, #[ ]);
		this[\player] = player = ConductorPlayer(this);
		this.noSettings;
	}
			
	make { arg func; 
		var obj, args, names;


		#args, names = this.makeArgs(func);
		valueKeys = valueKeys ++ names;
		gui.keys_( gui.keys ++ names);
		this.usePresets;
		super.make({func.valueArray(this, args)});
	}

	*makeCV { | name, value |
		^CV(this.findSpec(name), value)
	}
	
	*findSpec { | name |
		var spec = specs[name.asSymbol];
		if (spec.isNil) {
			spec = specs[name.asString.select{ | c | c.isAlpha}.asSymbol]
		};
		^spec;
	}
	
	makeArgs { arg func;
		var argList, size, names, argNames;
		var theClassName, name, obj;
		
		size = func.def.argNames.size;
		argList = Array(size);
		argNames = Array(size);
		names = func.def.argNames;
		// first arg is Event under constructions, subsequent are CVs or instances of other classes
		if (size > 1, {
			1.forBy(size - 1, 1, { arg i;
				name = names[i];
				argNames = argNames.add(name);
				theClassName = func.def.prototypeFrame.at(i);
				if (theClassName.notNil) {
					obj = theClassName.asClass.new;
				} {
					obj = Conductor.makeCV(name)
				};
				this.put(name,obj);
				argList = argList.add(obj);
			});
		});
		^[argList, argNames];
		
	}

// saving and restoring state 
	getFile { arg argPath; var file, contents;
		if (File.exists(argPath)) {
			path = argPath;
			file = File(path,"r"); 
			contents = file.readAllStringRTF;
			file.close;
			^contents;
		} {
			(argPath + "not found").postln;
			^nil
		}
	}
	
	putFile { | vals, argPath | 
		path = argPath ? path;
		File(path,"w").putAll(vals).close;
	}

	load { | argPath |
		var v;
		if (argPath.isNil) {
			File.openDialog(nil, { arg path; 
				v = this.getFile(path);
				this.value_(v.interpret)
			});
		} {
			v = this.getFile(argPath);
			this.value_(v.interpret)
		};
	}
	
	save { | path |
		if (path.isNil) {
			File.saveDialog(nil, nil, { arg path; 
				this.putFile(this.value.asCompileString, path)
			});
		} {
			this.putFile(this.value.asCompileString, path)
		};

	}

	path_ { | path |
		this.load(path);
	}

// gui display of file saving
	noSettings { this[\settings] = nil; this[\preset] = NullPreset; }
	
	useSettings { 
		this[\settings] = ConductorSettingsGUI(this);
		this[\preset] = NullPreset;
	}
	
	usePresets { 
		this[\settings] = ConductorSettingsGUI(this);
		this[\preset] = preset =  CVPreset.new; 
		this.presetKeys_(valueKeys);		
	}
	
	useInterpolator { 
		this[\settings] = ConductorSettingsGUI(this);
		this[\preset] = preset =  CVInterpolator.new; 
		this.presetKeys_(valueKeys);
		this.interpKeys_(valueKeys);
	}
	
// interface to default preset and interpolator

	presetKeys_ { | keys, argPreset |
		argPreset = argPreset ? preset;
		preset.items = keys.collect { | k | this[k] };
	}
	
	interpKeys_ { | keys, argPreset |
		argPreset = argPreset ? preset;
		argPreset.interpItems = keys.collect { | k | this[k] };
	}

	input {  var keys;
		if (this[\preset].notNil) { keys = #[preset] };
		^(valueKeys ++ keys).collect { | k| [k, this[k].input ]  }  }
		
	input_ { | kvs | kvs.do { | kv| this[kv[0]].input_(kv[1]); kv[0]; } }
	
	value {  ^(valueKeys ++ #[preset]).collect { | k| [k, this[k].value ]  }  }
	
	value_ { | kvs | kvs.do { | kv| this[kv[0]].value_(kv[1]); kv[0]; } }
	
//gui interface
	show { arg argName, x = 128, y = 64, w = 900, h = 160;
		^gui.show(argName, x, y, w, h);
	}

	draw { | win, name, conductor|
		gui.draw (win, name, conductor)
	}
	
	
// play/stop/pause.resume
	stop {
		player.stop;
 	}
	
	play { 
		player.play;		
	}
 

	pause { 
		player.pause; 
	}

	resume { 	
		player.resume; 
	}

	name_ { | name | player.name_(name) }
	
	name { ^player.name }
	
//player interface

	action_ { | playFunc, stopFunc, pauseFunc, resumeFunc |
		this.player.add ( ActionPlayer(playFunc, stopFunc, pauseFunc, resumeFunc ) )
	}

	buffer_ { | ev| 
		ev.parent = CVEvent.bufferEvent;
		this.player.add(ev);
	}
	
	controlBus_ { | ev, cvs|
		ev[\cvs] = cvs;
		ev.parent = CVEvent.controlBusEvent;
		this.player.add(ev)
	}

	synth_ { | ev, cvs|
		ev[\cvs] = cvs;
		ev.parent = CVEvent.synthEvent;
		this.player.add(ev)
	}

	synthDef_ { | function, cvs, ev|
		var name;
		name = function.hash.asString;
		SynthDef(name, function).store;
		ev = ev ? ();
		ev	.put(\instrument, name)
			.put(\cvs, cvs);
		ev.parent_(CVEvent.synthEvent);
		this.player.add(ev);
		^ev
	}

	group_ { | ev, cvs|
		ev[\cvs] = cvs;
		ev.parent = CVEvent.groupEvent;
		this.player.add(ev)
	}
	
	task_ { |func, clock, quant|
		this.player.add(TaskPlayer(func,clock, quant));
	}
	
	pattern_ { | pat, clock, event, quant |
		this.player.add(PatternPlayer(pat, clock, event, quant) )
	}

	nodeProxy_ { | nodeProxy, args, bus, numChannels, group, multi = false |
		this.player.add(NodeProxyPlayer(nodeProxy, args, bus, numChannels, group, multi) )
	}

	add { | name, obj, guiSpec |
		[name, obj, guiSpec].postln;
		name = name.asSymbol;
		this.put(name, obj);
		if (preset.notNil) { preset.items = preset.items.add(obj) };
		if (guiSpec.notNil) {
			gui.guis.put(name, guiSpec)
		};
		this.gui.addKeys( [name] );
	}
	
	addCon { | name, func|
		var con = Conductor.new
			.name_(name)
			.make(func);
		this.add(con.name, con);
	}

	addActions { | kv |
		var keys;
		keys = kv.pairsDo { | key, func |
			var player;
			player = ConductorPlayer(this);
			player.name_(key);
			gui.guis.put(key, \playStopGUI);
			this.put(key, player);
			if (func.isSequenceableCollection) { 
				player.action_(*func)
			} {
				player.action_({ func.value; defer( { player.stop }, 0.05) })
			};
			key
		};
		gui.keys = gui.keys.add(keys);
		valueKeys = valueKeys ++ keys;
	}
	
	addCV { | name, val, argGui |
		var cv, v;
		name = name.asSymbol;
		cv = Conductor.makeCV(name, val);
		this.put(name, cv);
		valueKeys = valueKeys.add( name );
		if (preset.notNil) { preset.items = preset.items.add(cv) };
		this.gui.addKeys( [name] );
		if (argGui.notNil) {
			this.gui.guis.put(name, argGui);
		};
		^cv;
						
	}

	addCVs {| kv |
		var cv, newPairs = [];
		kv.pairsDo { |key, value|
			if ( (cv = this[key]).notNil) { 
				if (value.notNil) { cv.value_(value) }
			} {
				cv = this.addCV(key, value);
				newPairs = newPairs.add(key).add(cv)
			}		
		};
		^newPairs
	}

	simpleGUI {
		this.noSettings;
		gui.use { ~playerGUI = ~simplePlayerGUI };
	}	
}
