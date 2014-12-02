
// to do: VC(0) => VP(0); aVoicer => VC(0) -- should this update VP's value?
// would need to use dependants for this

// to do: easier access to controlProxies

// passive mode recorder - how to specify wrapper for BP?
// - how to start/stop? Use MBM trigger button?
// - on stop, need to unwrap and go

// openly stealing from ChucK for high-level control of a performance interface

	// this is the top-level abstract class
	// methods that should be inherited by all chuckables should be defined here
AbstractChuckArray {
	classvar	collection,	// each chuckable is a collection of related objects
			<>directories,
			<>defaultSubType = \basic,	// set this when loading a piece to differentiate
									// in the browser
			<>passThru = true;		// pass not-understood messages thru to the target object?
	
	var	<collIndex,	// the instance is stored at this.class(collIndex)
		<>value,		// what I'm pointing to
		<>subType,
		<>path;

		// collection is shared among all subclasses, so must index by classname
	*initClass {
		var traverseSubclasses;
		collection = IdentityDictionary.new;
			// recursively go thru subclasses to add a collection for each subclass to the coll
		(traverseSubclasses = { |class|
				// do not add a collection for abstract classes
			class.name.asString.contains("Abstract").not.if({
				collection.put(class.name, class.collectionType.new);
			});
			class.subclasses.do({ |subcl|
				traverseSubclasses.value(subcl);
			});
		}).value(this);
		
			// finalize installation by loading startup*.scd files
			// from quark directory/Prototypes
			// there may be support for multiple directories later
		StartUp.add({
			var path;
			directories = [PathName(this.filenameSymbol.asString).pathOnly +/+ "Prototypes/"];

			directories.do({ |dir|
				path = (dir ++ "startup*.scd").pathMatch;
				(path.size > 0).if({
					path.loadPath(warnIfNotFound: false);
					"Loaded % chucklib files from %.\n".postf(path.size, dir);
				}, {
					"Skipped chucklib directory %.\n".postf(dir);
				});
			});
		});
	}
	
	*collectionType { ^Array }

	*collection { ^collection[this.name] }
	*keys {
		^(this.collection.size > 0).if({
			this.collection.collectIndices(_.notNil)
		}, { Array.new });
	}
	*values { ^this.collection }
	
		// to be removed later
	*debugCollection { ^collection }
	
		// new is misleading: I want to index the collection by CLASS(index)
		// the default method for AClass() is new, so...

		// this is for collections that cannot create new members
		// if index is an array, the output is an array. sweet!
	*new { |index|
		^index.notNil.if({ collection[this.name][index] });
	}

	*prNew { |index|
		var	temp;
		index.respondsTo(\at).not.if({
			this.put(index, temp = super.new.prInit(index));
		}, {
			Error("Index must not be a collection when creating a new object").throw;
		});
		^temp
	}
	
	*put { |index, member|
			// might need to extend the array so .put will succeed
		var	collTemp = collection[this.name];
		(index > (collTemp.size-1)).if({
			collection.put(this.name, collTemp.extend((index + 1).max(collTemp.size)));
		});
			// can't use collTemp here because it may be replaced with a new collection
		collection[this.name].put(index, member);
		ChuckableBrowser.updateGui(this);
	}
	
	removeFromCollection {
			// do it this way b/c removeAt will change indices of other objects, not good
		this.class.collection[collIndex] = nil;
		ChuckableBrowser.updateGui(this.class);
	}
	
		// for objects that don't do anything special on free
	free {
		^this.removeFromCollection;
	}
	
		// returns values -- since collection is an array, just give the collection
	*all { ^this.collection }
	
		// to garbage collect everything for a piece
		// e.g. Fact.freeType(\techno1)
	*freeType { |type|
		type.notNil.if({
			this.values.do({ |item|
				(item.subType == type).if({ item.free });
			});
		});
	}
	
		// free all objects, from any subclass, whose subType == type
	*freeTypeAll { |type|
		type.notNil.if({
			collection.keys.do({ |classname|
				classname.asClass.persistent.not.if({ classname.asClass.freeType(type); });
			});
		});
	}
	
	*persistent { ^false }
	
	*freeAll {
		this.collection.values.do(_.free);
	}
	
	*subTypes {
		var	types = IdentitySet.new;
		this.collection.do({ |item| types.add(item.subType) });
		^types.asArray.sort
	}
	
	*allOfType { |type|
		type.notNil.if({
			^this.collection.select({ |item| item.subType == type }).asArray;
		}, { ^[] });
	}
	
	exists { ^value.notNil }
	*exists { |index|
		var	temp = this.collection[index];
		^temp.tryPerform(\exists) ? false
	}
	
	init {}

	prInit { |index|
		collIndex = index;
		subType = defaultSubType;
		path = thisProcess.nowExecutingPath ?? { path };
		this.init(index);
	}
	
	v { ^value }	// shortcut to get value - keystroke efficiency is paramount!
	
	storeArgs { ^[collIndex] }  // compilestring will be ClassName.new('index')
	printOn { |stream|
		if (stream.atLimit, { ^this });
		stream << this.class.name << "(" <<< collIndex << ")";
	}
	openFile {
		^(path !? { path.asString.openDocument })
	}
	*loadFromChuckDirectories { |path|
		directories.reverseDo({ |dir|
			path = (dir ++ path).pathMatch;
			(path.size > 0).if({
				path.loadPath(warnIfNotFound: false);
				^this
			});
		});
		^nil
	}
	*loadWindowBounds { |width|
		var	path;
		width = (width ?? { GUI.window.screenBounds.width }).asInteger;
		this.loadFromChuckDirectories("windowbounds%.scd".format(width)).isNil.if({
			"Screen width % not defined. Reverting to 1024.".format(width).warn;
			this.loadFromChuckDirectories("windowbounds1024.scd").isNil.if({
				"Failed to load screen size variables.".warn;
				^nil
			});
		});
	}
	*loadGui { |width|
		var	path;
		this.loadWindowBounds(width);
		this.loadFromChuckDirectories("devEnvironment.scd").isNil.if({
			"Could not find devEnvironment.scd".warn;
			^nil
		});
	}
	*openCodeDoc { |path|
		var doc;
		doc = Document.open(path);
		{ doc.bounds_(Library.at(\codeBounds)) }.defer(1);
	}
	*open {
		GetFileDialog.new({ |ok, path|
			ok.if({ { this.openCodeDoc(path) }.defer })
		});
	}
	*runFile {
		GetFileDialog.new({ |ok, path|
			ok.if({ path.loadPath });
		});
	}
	
	doesNotUnderstand { |selector ... args|
		passThru.if({ ^value.performList(selector, args) }, { ^super.doesNotUnderstand(selector, *args) });
	}
}

AbstractChuckNewArray : AbstractChuckArray {
		// this subclass is for collections that make a new member automatically
	*new { |index|
		var temp;
		^index.notNil.if({
				// if an object exists at index, give it back
			(collection[this.name][index] ??
					// else, make a new object and give it back
				{ temp = this.prNew(index); })
		});
	}
}

AbstractChuckDict : AbstractChuckArray {
		// allow array indexing by passing an array of keys
		// BP([\mel, \chord, \drums]).play(4)
	*new { |index|
		var	collTemp;
		index.isNil.if({ ^nil });
		index.isValidIDictKey.not.if({
			MethodError("% is not a valid key for this storage class.".format(index), this).throw;
		});
		^index.notNil.if({
			collTemp = collection[this.name];
			index.respondsTo(\at).if({
				index.collect({ |i| collTemp[i] });
			}, {
				collTemp[index] ? collTemp[\default]
			});
		});
	}

	*put { |index, member|
			// should not call extend on a dictionary
		collection[this.name].put(index, member);
		ChuckableBrowser.updateGui(this);
	}
	
	removeFromCollection {
		this.class.collection.removeAt(collIndex);
		ChuckableBrowser.updateGui(this.class);
	}
	
	*collectionType { ^IdentityDictionary }
	*keys { ^this.collection.keys }
	*values { ^this.collection.values }
	
	*listKeys { |type|
		var collTemp = collection[this.name];
		collTemp.keys.asArray.reject({ |k| collTemp[k].isNil }).sort.do({ |k|
			(type.isNil or: { collTemp[k].tryPerform(\subType) == type }).if({
				k.postln
			});
		});
	}
	
		// collection is a dictionary, return a flat Set of values
	*all { ^this.collection.values }
}

AbstractChuckNewDict : AbstractChuckDict {
	*new { |index|
		var	collTemp;
		index.isNil.if({ ^nil });
		index.isValidIDictKey.not.if({
			MethodError("% is not a valid key for this storage class.".format(index), this).throw;
		});
		^index.notNil.if({
			collTemp = collection[this.name];
			index.respondsTo(\at).if({
				^index.collect({ |i| collTemp[i] });
			}, {
				^(collTemp[index] ?? { this.prNew(index) })
			});
		});
	}
}

/// actual collections

VC : AbstractChuckNewDict {
		// environment may be created by VoicerFactory (Fact)
	var	<env;

	init {
		env = Environment.new.know_(true);
	}
	
	use { |function|
		^env.use(function)
	}
	
	bindVoicer { |voicer|
		value = voicer;
	}
	
	bindFact { |fact, adverb, parms|
			// can be a voicer or an fx factory
		(fact.isVoicer.not).if({ "wrong type".warn },
				// default, assume voicer
			{ this.prBindFact(fact, adverb, parms) });
	}
	
		// does the work, but does not check Factory type
	prBindFact { |fact, adverb, parms|
		value.notNil.if({ this.free });  // free system resources before replacing
		env = fact.make(parms, collIndex).know_(true);
			// make func must return voicer and place support objects (mixer, buffers, etc)
			// in the environment
		value = env[\value];
		subType = fact.subType;
		env[\error].notNil.if({		// oops, something bad happened
			"Error occurred while making %. Backtrace is available at %.env.backtrace."
				.format(fact, this).warn;
		});
	}
	
	free {
			// environment's free func must free all the support objects
		env.tryPerform(\use, {
			var mixer;
			(mixer = ~target.tryPerform(\asMixer)).notNil.if({
				mixer.mcgui.notNil.if({ mixer.mcgui.mixer = nil });
			});
			~free.value(value);	// maybe free func needs to know the voicer object
		});
		value.free;
		this.removeFromCollection;
	}
	
	bindBP { |bp|
		bp.bindVC(this)
	}
	
	draggedIntoVoicerGUI { |dest|
		value.tryPerform(\draggedIntoVoicerGUI, dest);
	}
	
	draggedIntoMixerGUI { |gui|
		var	mixer;
			// must have an environment, a voicer, and the target must be a mixer
		(env.notNil and: { value.notNil and:
			{ (mixer = env[\target].tryPerform(\asMixer)).notNil } }).if({
				gui.mixer = mixer;
			});
	}
	
	gui { ^value.gui }
	asMixerChannelGUI { |board|
		var	mixer;
		if((mixer = env[\target]).notNil) {
			^mixer.mcgui ?? { MixerChannelGUI(mixer, board) }
		} {
			^nil
		};
	}
	
	asVC { ^this }
}

// synth/target descriptor to use with synthNote event
// value is synthdef name
// env contains target and out keys
// populate using a factory
SY : VC {
	// inherits bindFact and free	
	bindBP { |bp|
		bp.v[\event] = bp.v[\event].copy.putAll((
			instrument: value,
			target: env[\target],
			out: env[\out]
		));
	}
	
	bindFact { |fact, adverb, parms|
		this.prBindFact(fact, adverb, parms)
	}
}

	// factory for any kind of object that requires support objects
Fact : AbstractChuckNewDict {
	classvar	<>voicerTypes = #[\voicer, \vc],
			<>bpTypes = #[\bp],
				// automatically get the clock from the containing environment and put it into my env?
			<>autoImportKeys;

	*initClass {
		autoImportKeys = [\clock];
	}

	bindEnvironment { |env|
		value = env.know_(true);
		value.collIndex = collIndex;
	}
	
	bindEvent { |env|
		value = env.know_(true);
		value.collIndex = collIndex;
	}
	
	draggedIntoVoicerGUI { |dest|
		var	vp;
		(value.notNil and: { this.isVoicer }).if({
			this => VC(collIndex);
			(vp = VP.locateVPbyGui(dest)).notNil.if({
					// some ops are done in bind; need to do update thru a bind op
				VC(collIndex) => vp;
			}, {
				dest.model.voicer_(VC(collIndex).v);
			});
		});
	}
	
	draggedIntoMTGui { |gui, index|
		this.isBP.if({
			gui.model.add(this.makev, index);
			^true	// success flag
		}, {
			"Fact(%)'s type must be 'bp', but is %. Not instantiated."
				.format(collIndex.asCompileString, value[\type].asCompileString).warn;
			^false
		});
	}
	
		// convention: the first item in args should be the name of the target object
		// make and makev supply a default automatically, which is the name of the Fact
	make { |argEnv ... args|
		var	env;
		env = this.prepareEnv(argEnv);
		args.first.isNil.if({ args = args.copy.extend(max(1, args.size)).put(0, collIndex) });
		try {
			env.make({
				~value = ~make.value(*args);
					// if the value can have a subtype, assign the Fact's subtype to it
					// otherwise do nothing
				~value.tryPerform(\subType_, subType);
			});
		} { |error|
			error.reportError;
			env[\error] = error;
			env[\backtrace] = error.getBackTrace.caller;
		};
		^env
	}
		// makes the value, but throws out the environment and returns only value
	makev { |argEnv ... args|
		var	env, result;
		env = this.prepareEnv(argEnv);
		args.first.isNil.if({ args = args.copy.extend(max(1, args.size)).put(0, collIndex) });
		result = env.use({
			~make.value(*args);
		});
		result.tryPerform(\subType_, subType);
		^result
	}
	
	prepareEnv { |argEnv|
		var	env;
		env = value.copy;
			// imports: first autoImports, then Fact's keys entry can override
			// if a key already exists in the Fact's environment, it is not imported
			// so the contents of the environment are the final override
		autoImportKeys.do({ |key|
			value[key].isNil.if({ env[key] = key.envirGet });
		});
		value[\keys].do({ |key|
			value[key].isNil.if({ env[key] = key.envirGet });
		});
		(argEnv.respondsTo(\keysValuesDo)).if({
			env.putAll(argEnv);
		});
		^env
	}
	
	proto {
		var	str, func;
		(value.notNil and: { value[\make].notNil }).if({
			str = CollStream(String(256));
			str << "Fact(" <<< collIndex << ").make(argEnv";
			((func = value[\make]).def.argNames > 1).if({
				str << ", ";
			});
			value[\make].streamArgs(str);
			str << ")";
			Document.current.selectedString_(str.collection);
		}, { "\nWARNING:\nFact(%) is empty.\n".postf(collIndex.asCompileString) });
	}
	
	isVoicer { ^voicerTypes.includes(value[\type]) }
	isBP { ^bpTypes.includes(value[\type]) }
}

VP : AbstractChuckArray {
	*persistent { ^true }

	bindVoicerProxy { |proxy|
		value = proxy;
	}
	
	bindVC { |vc|
		this.bindVoicer(vc.value);
		vc.use({ ~onVPchuck.value(this) });	// allow other user defined ops
	}

		// Fact() => VP() is a shortcut for Fact() => VC() => VP()
	bindFact { |factory|
		factory.isVoicer.if({
			this.bindVC(factory => VC(factory.collIndex));
		});
	}
	
	bindVoicer { |voicer|
		value.tryPerform(\clearControlProxies);
		value.voicer_(voicer);
	}
	
	bindNil { value.voicer_(NullVoicer.new) }
	
	bindBP { |bp|
			// assign the process to play on this voicerproxy
			// or call useGui if we have it
		bp.v[\useGui].notNil.if({
			bp.v.useGui(collIndex)
		}, {
			bp.voicer_(value);
		});
	}
	
	bindGenericGlobalControl { |gc, adverb|
		var gcproxy;
		(adverb.asString[0].inclusivelyBetween($0, $9)).if({
			gcproxy = value.controlProxies[adverb.asInteger]; // returns nil if invalid index
		}, {
			gcproxy = value.getFreeControlProxy(gc);
		});
		
			// assign only if we got a valid proxy from the VP
		gcproxy !? { gcproxy.gc_(gc) };
	}
	
	bindCC { |cc, adverb|
		var gcproxy;
		(adverb.asString[0].inclusivelyBetween($0, $9)).if({
			gcproxy = value.controlProxies[adverb.asInteger]; // returns nil if invalid index
		}, {
			gcproxy = value.controlProxies.detect({ |gcp| gcp.midiControl.isNil });
		});

		gcproxy !? { cc.v.destination = gcproxy };
	}
	
	draggedIntoVoicerGUI { |dest|
		value.tryPerform(\draggedIntoVoicerGUI, dest);
	}
	
	*doUseGuiOnBP { |bp, gui|
		var vpInColl;
			// useGui in a BP expects the index of the VP object
		(vpInColl = this.locateVPbyGui(gui)).notNil.if({
			bp.v.useGui(vpInColl.collIndex)
		});
	}
	
	*locateVPbyGui { |gui|
		^block { |break|
			this.collection.do({ |vp|
				(vp.v.editor === gui).if({ break.(vp) })
			});
			break.(nil)
		}
	}
	
	gui { ^value.gui }
	
	asVC {
		^VC.collection.values.detect({ |vc|
			vc.value === this.value.voicer
		});
	}
}

/// midi recording and buffer management

// stand-in for MIDIRecSocket
// does not contain or create the recsocket; MBM does that when it receives MRS as a chuck
// to use, this should be chucked into a MBM (MIDIBufManager)
// then use the MIDI control for the stop button, or click on the gui
// supply the name of the new buf as a symbol at instantiation

MRS {
	var <name, <>properties, <>mbm;
	*new { |name, properties, mbm|
		(mbm.notNil and: mbm.isKindOf(MBM).not).if({
				// allow index into collection
			(mbm = MBM(mbm)).isKindOf(MBM).not.if({
				Error("Invalid bufmgr. Must be an MBM or index thereto.").throw;
			});
		});
		^super.newCopyArgs(name, properties ?? (),
			mbm ? MBM(0)
// if you don't create your MBM first, you lose control over clock, channel, and controller
				?? { MIDIBufManager(TempoClock.default, 0, nil) => MBM.prNew(0) }
		)
	}
}

MBM : AbstractChuckArray {
	*persistent { ^true }

	bindMIDIBufManager { |obj|
		value = obj;
	}
	
	bindMIDIRecBuf { |obj|
		value.add(obj);
	}
	
	bindMRS { |obj, index|
		var	setNameFunc = { |buf|
				buf.name = obj.name;	// apply name specified in MRS(\name)
				value.postRecFunc = value.postRecFunc.removeFunc(setNameFunc);
				buf
			};
		value.recorder.notNil.if({
			"Already recording. Can't start a new recording.".warn;
		}, {
				// was index given as adverb? make sure buf goes in that slot
				// can also say MRS(\newbufname) =>.new MBM(0)
			(index.notNil and: (index != \new)).if({
				try { index = index.asInteger }
						// throw a more meaningful error than "asInteger not understood"
					{ Error("Invalid index supplied as adverb. Must be an integer.").throw };
				(index >= value.bufs.size).if({
					value.bufs.grow(index - value.bufs.size + 1);
				});
				value.value_(index);
			}, {
				value.value_(value.bufs.size);	// so that new buf will be created
			});
			value.postRecFunc = value.postRecFunc.addFunc(setNameFunc);
			value.initRecord(obj.properties);
		});
	}
	
		// so I can say MBM(0)[1] => BP(\x)
	at { |index| ^value[index] }
	current { ^value.current }
}

// eventually I will want a passive-mode player that will be chuckable into MBM

PR : AbstractChuckNewDict {
	classvar	<defaultEnv;

	*initClass {
			// sometime I might put default pseudo-methods into parent environment
		defaultEnv = Environment.make({
			~canStream = {
				var out;
				out = true;
				~requiredKeys.do({ |key|
					key.envirGet.isNil.if({
						"Required variable BP(%).% is empty. Cannot play."
							.format(~collIndex.asCompileString, key).warn;
						out = false;
					});
				});
					// if this is a wrapper process,
				~canWrap.if({
						// result depends on whether child exists and can stream
					out and: #{ ~child.tryPerform(\canStream, ~child) ? false }
				}, {
					out		// if not, result is whether the required keys are there
				});
			};
			~getQuant = {
					// not ideal to replicate this code from BP-quant
					// but I may not have a collIndex in my environment
					// so I can't call BP-quant
				(~quant.value(currentEnvironment)
					?? { BP.defaultQuant.value(currentEnvironment) })
					.dereference.asTimeSpec
			};
				// this is done after putting a new value into the Proto
				// should not be global for Proto, but yes for PR/BP
			~putAction = { |key, value|
				var	streamKey;
				(value.isPattern
					or: {	streamKey = (key ++ "Stream").asSymbol;
							streamKey.envirGet.notNil })
				.if({
					(streamKey ?? { (key ++ "Stream").asSymbol }).envirPut(value.asStream);
				});
			};
		});
	}

	bindProto { |ad|
		value = ad;
			// merge the default environment parent entries into this environment's parent
		value.env.parent.isNil.if({
			value.env.parent_(Environment.new);
		});
		this.addDefaultMethods;
		value[\canWrap].isNil.if({ value.env.parent.put(\canWrap, false) });
		value.isPrototype = true;	// protect this Adhoc from accidental direct use in BP
		value.putAction = { |key, value, self|
			(currentEnvironment !== self).if({
				self.use({ ~putAction.(key, value) })
			}, {
				~putAction.(key, value)
			});
		};
	}
	
	hasDefaultMethods {
		defaultEnv.keysDo({ |key|
			value[key].isNil.if({ ^false })
		});
		^true
	}

	addDefaultMethods {
		defaultEnv.keysValuesDo({ |key, val|
			(value[key] !== val).if({ value.parent[key] = val });
		});
	}

	bindEvent { |event|
		value.put(\event, event)
	}	

	bindProtoEvent { |proto|
		value.put(\event, proto.value)
	}

		// midi trigger support
	draggedIntoMTGui { |gui, index|
		gui.model.add(this => BP(collIndex), index);
		^true	// success flag
	}
	
		// to avoid clumsy PR(\abc).v.clone - why not PR(\abc).clone({ ... }) => PR(\def)?
	clone { |func, parentKeys| ^value.clone(func, parentKeys) }
	copy { ^value.copy }
}

BP : AbstractChuckNewDict {
	classvar	<>defaultQuant, <>defaultClock, <>defaultLeadTime = 0,  //.5,
				// a clock, or a function to get the clock for a new BP dynamically from an environment
			<>defaultInitClock,
			<>defaultEvent,
				// true = BP plays on voicerproxy (can switch vp's voicer during play)
				// false = BP maintains a hard link to a specific voicer (change by code only)
			<>useVoicerProxy = true;

	var	<leadTime;	// leadTime is given in beats and corresponds to ~timingOffset in Event

	*initClass {
		StartUp.add({
			defaultClock = TempoClock.default;
				// no matter what the meter is, this will go to the next barline
			defaultQuant = BasicTimeSpec(-1);
			defaultEvent = (eventKey: \default);
		});
	}
	
	init {
		leadTime = defaultLeadTime;
	}
	
	free {
		this.exists.if({
				// some processes need to do different cleanups if freed while playing
			value[\wasPlayingWhenFreed] = this.isPlaying;
			this.stopNow;
			value[\eventStreamPlayer] !? { value[\eventStreamPlayer].releaseDependants };
			value.freeCleanup;
			this.changed(\free);
			BP.removeDependant(value);
			value = nil;
		});
		this.releaseDependants;
		this.removeFromCollection;
	}

	bindPR { |process, adverb, parms|
		this.bindProto(process.value.copy.isPrototype_(false), adverb, parms);
		subType = process.subType;
	}
	
		// BPs can store state of wrapper processes, which can be reused by
		// BP(\wrapper) => BP(\child)
	bindBP { |process, adverb, parms|
			// if the Adhoc implements bindBP, use the Adhoc's method
			// this is for support of driver processes
		this.exists.if({
			value[\bindBP].isFunction.if({
				value.bindBP(process, adverb, parms)
			}, {
					// otherwise wrap/replace
				this.bindProto(process.value, adverb, parms)
			});
			subType = process.subType;
		}, {
			MethodError("Target BP(%) does not exist.".format(collIndex), this).throw;
		});
	}
	
	bindProto { |process, adverb, parms|
		(process === value).if({
			"Cannot chuck a process into itself. Chuck operation ignored.".warn;
			^this
		});
			// PR(\xyz).v => BP(\abc) is dangerous
		process.isPrototype.if({
			process = process.copy;	// you need a new instance, not the prototype
		});
			// midi input needs to be able to access this object from within the Proto
		process.put(\collIndex, this.collIndex);
		this.exists.not.if({
			value = process;
		}, {
				// make sure outermost process is a dependant if the respondsToBass flag is set
				// first remove current outermost as dependant
			BP.removeDependant(value);
			case { adverb == \nest }
					// typically doReplay will be true, but MIDI input processes
					// want to stop the child process on receipt of the first midi msg
					// so they should not replay immediately
					{ this.wrap(process, process.doReplay ? true); }
				{ adverb.isNil or: { adverb == \wrap } }
					{ this.rewrap(process, process.doReplay ? true); }
				{ adverb == \replace }
					{ this.replace(process, process.doReplay ? true); }
				{ adverb == \overwrite }
					{ this.overwrite(process) }	// no doreplay for this guy
				{ adverb == \relate }	// for bp's that modify other bps' behavior
					{ value.put(\subordinate, process) }
				{ ("Unrecognized adverb " ++ adverb).warn; };
		});
		value[\isPlaying].isNil.if({
			value.put(\isPlaying, false).put(\isWaiting, false);
		});
		(value.notNil and: { value[\respondsToBass] == true }).if({
			BP.addDependant(value);
		});
			// if bp's clock is nil, check the defaultInitClock (which may itself be nil)
			// if nil the BP will revert to BP.defaultClock
		value[\clock] ?? {
			this.value.put(\clock, defaultInitClock.value);
		};
			// allow some instance vars to be set at instantiation, before prep
		parms.respondsTo(\keysValuesDo).if({ value.putAll(parms) });
		this.promoteChildEventObjects;
		value.prep;	// process can do some initialization on instantiation
					// if unwrapping, child process should be aware that it's already inited
	}
	
	bindFact { |fact, adverb, parms|
		(fact.isBP).if({
			^fact.makev(parms, collIndex).subType_(fact.subType);
		}, {
			"%'s subtype is %; must be 'bp' to chuck into BP.".format(fact, fact.v[\type]).warn;
		});
	}
	
		// collapse multiple binds into one operation
		// order of binding is not guaranteed (limitation of Dictionaries)
		// event is (adverb: thing, adverb: thing)
		// translates to thing =>.adverb BP; thing =>.adverb BP
	bindEvent { |evt|
		evt.keysValuesDo({ |adverb, thing|
			thing.perform('=>', this, adverb);
		})
	}
	
		// 4 => BP(0) sets quant to BasicTimeSpec(4)
	bindNilTimeSpec { |spec|
		this.exists.if({ value.put(\quant, spec); });
	}

	bindQuant { |quant|
		this.bindNilTimeSpec(quant.asTimeSpec);
	}
	
	bindSimpleNumber { |num, adverb|
		adverb.isNil.if({
			this.bindNilTimeSpec(num.asTimeSpec);
		}, {
			value.bindSimpleNumber(num, adverb);
		});
	}
	
	bindArray { |ar|
		this.bindNilTimeSpec(ar.asTimeSpec);
	}

		// 4 => BP sets default quant for all processes to BasicTimeSpec(4)
	*bindNilTimeSpec { |spec|
		defaultQuant = spec;
	}
	
	*bindSimpleNumber { |num|
		defaultQuant = num.asTimeSpec;
	}
	
	*bindArray { |ar|
		defaultQuant = ar.asTimeSpec;
	}
	
	bindSymbol { |symbol, adverb|
		this.exists.if({ 
			adverb.isNil.if({
				"Must supply an adverb when chucking a symbol into a BP. No action taken.".warn;
			}, {
				value.bindSymbol(symbol, adverb)
			});
		});
	}

		// adverb is the environment key into which the midi buf will go
		// MBM(0)[1] =>.melody BP(0)
	bindMIDIRecBuf { |buf, adverb, parms|
		this.exists.if({ value.acceptMIDIBuf(buf, adverb, parms); });
	}
	
	bindMIDIBufManager { |bufmgr, adverb, parms|
		this.bindMIDIRecBuf(bufmgr.current, adverb, parms)
	}

	bindMBM { |bufmgr, adverb, parms|
		this.bindMIDIBufManager(bufmgr.v, adverb, parms)
	}
	
		// this needs to wrap the current process in a MIDI input process
	bindMRS { |mrs, adverb|	// adverb is type of material
		this.exists.if({
			adverb = adverb ? value.defaultMIDIType;
				// process may need streams to be populated before midi input can commence
				// must make them before wrapping in the midi process
			this.prepareForPlay;
			value.event.parent.isNil.if({ value[\event] = this.prepareEvent });
			this.bindPR(PR((adverb ++ "MIDI").asSymbol), \nest);  // PR(wrapper) => this
				// this chuck operation also makes the MIDIRecSocket
			mrs.mbm.bindMRS(mrs, nil);	// always create a new buf here (nil); mrs => mbm
			value.getMIDIParser(mrs.mbm.v.recorder);	// getMIDIParser should assign to ~parser
			value.eventStreamPlayer.isNil.if({
					// populate streams
				this.asEventStreamPlayer;
			});
			value.preparePlay;
		});
	}
	
	bindModalSpec { |mode, adverb|
		this.exists.if({ value.mode_(mode, adverb); });
	}
	
	bindMode { |mode, adverb|
		this.exists.if({ value.mode_(mode.collIndex, adverb); });
	}		
	
		// remove all adaptation sources and results (original material only will be left)
		// will also traverse children
	clearAdapt {
		var	child;
		this.exists.if({ 
			child = value;
			{ child.notNil }.while({
				child.clearAdapt;
				child = child.child;
			});
		});
	}
	
		// patterns for micro/macro rhythm keys
	bindPattern { |pat, adverb|
		this.exists.if({ value.bindPattern(pat, adverb); });
	}
	
	bindMacRh { |pat, adverb|
		this.exists.if({ value.bindPattern(pat, adverb ? \macro) });
	}
	
		// really? not sure about this
	bindMicRh { |pat, adverb|
		this.exists.if({ value.bindPattern(pat, adverb ? \micro) });
	}
	
	bindArpegPat { |pat, adverb|
		this.exists.if({ value.bindPattern(pat, adverb ? \arpeg) });
	}

	bindSA { |sa, adverb|
		this.exists.if({ value.bindSA(sa, adverb) });
	}
	
		// must not do this while playing
	clock_ { |cl|
		this.isPlaying.not.if({		// also handles situation of empty bp
			value.put(\clock, cl);
		}, {
			Error("Cannot change the clock while playing.").throw;
		});
	}
	
	bindTempoClock { |cl| this.clock = cl }
	
	clock { ^value.clock ? defaultClock }

	*bindTempoClock { |cl|
		defaultClock = cl;
	}
	
		// add a method to this BP from the Func library
	bindFunc { |func, adverb|
		value[adverb] = func.v;
	}
	
		// will try to change voicer while playing
		// if this is a wrapper, voicer will not change until the next event for outermost wrapper
	voicer_ { |vc|
		this.exists.if({
			value.event.put(\voicer, vc);
			value.bindVoicer(vc);
		});
	}
	
	bindVoicer { |vc|
		this.voicer = vc;
	}

	bindVoicerProxy { |vp|
		this.voicer = vp;
		value.bindVoicerProxy(vp);
	}
	
	bindVC { |vc|
		this.voicer = vc.value;
		value.bindVC(vc);
	}
	
	bindVP { |vp|
		this.voicer = vp.value;
		value.bindVP(vp);
	}

		// play support
		// reversing argument order because quant is more important in performance
	play { |argQuant, argClock, doReset, notify = true|
		var	goTime;
		this.isPlaying.if({
			"already playing".warn;
		}, {
			this.exists.if({
				case { value[\asPattern].isNil }		// support for single-action BP's
					{		// must set clock variable so event can be retriggered later
						this.populateAdhocVariables(argClock);
						value[\event] = this.prepareEvent;
						(goTime = this.eventSchedTime(argQuant)).isNil.if({
							"BP(%): Scheduling failed: scheduled time is earlier than now %.\n"
								.format(collIndex, this.clock.beats).warn;
							this.changed(\schedFailed);
							^this
						});
						value[\eventSchedTime] = goTime;
						value.put(\isPlaying, true);
						notify.if({ this.changed(\play); });
						this.clock.schedAbs(this.eventSchedTime(argQuant), {
							value.doAction;  // doAction is a pseudomethod in the Proto
							value.put(\isPlaying, false);
							notify.if({ this.changed(\stop, \stopped); });
						});
					}
				{ this.canStream }
					{
						this.populateAdhocVariables(argClock);
						(goTime = this.eventSchedTime(argQuant)).isNil.if({
							"BP(%): Scheduling failed: scheduled time is earlier than now %.\n"
								.format(collIndex, this.clock.beats).warn;
							this.changed(\schedFailed);
							^this
						});
						value[\eventSchedTime] = goTime;
						this.prepareForPlay(argQuant, argClock, doReset);
						if(value[\eventStreamPlayer].notNil) {
							(this.clock == AppClock).if({
								// AppClock has no schedAbs method
								value.eventStreamPlayer.play(this.clock, doReset, 0);
								value[\isWaiting] = false;
							}, {
								this.clock.schedAbs(goTime, {
									// nextBeat.isNil means that the stream is stopped
									(value[\isWaiting]
										and: { value[\eventStreamPlayer]
											.tryPerform(\nextBeat).isNil })
									.if({
										value.eventStreamPlayer.play(this.clock, doReset,
											AbsoluteTimeSpec(goTime));
									});
									value[\isWaiting] = false;
									nil
								});
							});
							value.put(\isPlaying, true).put(\isWaiting, true);
							notify.if({ this.changed(\play, goTime); });  // update MTGui
						} {
							if(notify) { this.changed(\couldNotPrepare, goTime) };
						};
					};
			});
		});
	}
	prepareForPlay { |argQuant, argClock, doReset|
		var oldEventStreamPlayer;
		(this.exists and: { this.canStream }).if({
			this.populateAdhocVariables(argClock);
			(value.eventStreamPlayer.isNil or: { doReset ? false
				or: { value[\alwaysReset] ? false } })
				.if({
					(oldEventStreamPlayer = value.eventStreamPlayer).notNil.if({
						(argClock ? this.clock ? defaultClock)
							.schedAbs(this.eventSchedTime(argQuant), {
								oldEventStreamPlayer.stop;
							})
					});
					if(value[\alwaysReset] == true) { value.reset };
					this.asEventStreamPlayer;
				});
		});
	}
	populateAdhocVariables { |argClock|
		value.put(\clock, argClock ? this.clock ? defaultClock).
			put(\leadTime, leadTime);
		value.event.isNil.if({ value[\event] = defaultEvent });
		value.event.put(\clock, this.clock)
			.put(\timingOffset, leadTime)
			.put(\child, value.child)
			.put(\propagateDownward, this.propagateDownFunc)
			.put(\collIndex, collIndex);
	}
	eventSchedTime { |argQuant|
		var	time;
		this.exists.if({ 
			time = this.quant(argQuant).bpSchedTime(this);
			^(time >= this.clock.beats).if({ time }, { nil });
		}, { ^nil });
	}
		// dereference allows you to force play to start exactly now on the clock with `nil
	quant { |argQuant|
		this.exists.if({
			^(argQuant.value(this)
				?? { value.quant(this) }
				?? { defaultQuant.value(this) }
			).dereference.asTimeSpec
		}, { ^nil });
	}
	leadTime_ { |lat|
		this.isPlaying.if({
			"Cannot set leadTime while BP(%) is playing.".format(collIndex).warn;
		}, {
			leadTime = lat;
			this.populateAdhocVariables;
		});
	}
	nextBeat {
		this.exists.if({
			^value[\eventStreamPlayer].tryPerform(\nextBeat)
		}, {
			^nil
		});
	}
	
		// useful for wrapper processes--set up a midi trigger to fire ONE child process
	triggerOneEvent { |argQuant, argClock, doReset|
		var	event, saveEvent;
		(this.exists and: { this.canStream }).if({
			if(value.eventStreamPlayer.isNil or: { doReset == true }, {
				this.prepareForPlay(argQuant, argClock, doReset);
			});
			this.isPlaying.if({
				this.stop(argQuant);
			});
			value[\eventSchedTime] = this.eventSchedTime(this.quant(argQuant));
			this.clock.schedAbs(value[\eventSchedTime], {
				if((event = value.eventStream.next(value.event.copy)).notNil) {
					saveEvent = event.copy;
					event.play;
					this.changed(\oneEventPlayed, saveEvent);
				} {
					this.changed(\oneEventEmpty)
				};
				nil	// otherwise it will play again after ~delta beats
			});
		});
	}
			
	stop { |argQuant|
		var	time;
		this.exists.if({
			try {
				time = this.eventSchedTime(argQuant);
				value[\eventSchedTime] = time;
					// 1e-3 is to force this func to wake up before the thread it's stopping
					// but if this is using NilTimeSpec, that time could be in the past
					// so use current beats if it's greater
				this.clock.schedAbs(max(time - (1e-3), this.clock.beats), {
						// can't assume value still exists
					if(value.tryPerform(\isPlaying) == false) {
						this.stopNow(nil, argQuant, notifyTime: time);
					};
				});
			} {
				this.stopNow(nil, argQuant);
			};
			value.put(\isPlaying, false).put(\isWaiting, false);
			this.changed(\stop, \request);
		});
	}
	
		// for rewrapping/replacing -- specify a Proto to use
		// there may be cases where I don't want to notify dependents
	stopNow { |adhoc, quant, notify = true, doCleanup = true, notifyTime|
		var	child;	// to iterate down the chain of child processes
		this.exists.if({
			notifyTime ?? { notifyTime = this.clock.beats };
			adhoc = adhoc ? value;
			adhoc.eventStreamPlayer.stop;
			doCleanup.if({ adhoc.stopCleanup(false, quant); });
			child = adhoc;
			{ (child = child.child).notNil }.while({
				child.eventStreamPlayer.stop;
					// false = not auto-stop (see CleanupStream in asStream for true condition)
				doCleanup.if({ child.stopCleanup(false, quant); });
				child.put(\isPlaying, false);
			});
			value.put(\isPlaying, false).put(\isWaiting, false);
				// parent might need to know the quantized stop time
				// but the notification should be sent slightly ahead of the beat
			notify.if({ this.changed(\stop, \stopped, notifyTime) });
		});
	}
	
	asStream {
		var	stream;
		this.exists.if({ 
			stream = value.use({
					// by entering the environment for asStream, PR/BP code can be simpler
				this.asPattern.asStream;
			});
			value.put(\eventStream, stream);
			^stream
		}, { ^nil });
	}
	asPattern { ^value.asPattern }
	asEventStreamPlayer {
			// replay needs to change the cleanup func in the old stream before stopping
			// so I need to refer to other adhocs than my own in that case
			// ('value' may change but 'adhoc' will not)
		var event, adhoc = value, updater, err;
		this.exists.if({
			value.preparePlay;
			value[\event] = event = this.prepareEvent;
				// if I don't do this, a process with alwaysReset = true
				// can cause an object leak in the global dependants dictionary
			if(value[\eventStreamPlayer].notNil) {
				value[\eventStreamPlayer].releaseDependants;
			};
			// asStream may not be able to make a stream
			// if so, ~asPattern should return nil (or an error object with explanation)
			// then, this will make sure eventStreamPlayer is also not populated
			try { this.asStream } { |e| err = e };
			if(value[\eventStream].isKindOf(Stream)) {
				value.put(\eventStreamPlayer, 
					BlockableEventStreamPlayer(value[\eventStream], event)/*.refresh*/);
				value[\eventStreamPlayerWatcher] = updater = Updater(value[\eventStreamPlayer], { |obj, what|
					if(what == \stopped and: { value.notNil and: { obj === value[\eventStreamPlayer] } }) {
						this.streamCleanupFunc(this, adhoc);
						// just in case there's still some garbage floating about
						updater.remove;
						adhoc[\eventStreamPlayerWatcher] = nil;
					};
				});
			} {
				value.put(\eventStreamPlayer, nil).put(\eventStream, nil);
				if(err.isKindOf(Exception)) {
					// can't reliably use "switch" to match nil
					// default behavior should be to pass the error up to the caller
					if(value[\onAsStreamError].isNil) { err.throw };
					switch(value[\onAsStreamError])
					{ \throw } { err.throw }
					{ \report } {
						"\nvv BP(%): Problem making stream."
						.format(collIndex.asCompileString).postln;
						err.reportError;
						"^^ BP(%): Problem making stream."
						.format(collIndex.asCompileString).postln;
					}
					{ \warn } {
						"BP(%): Problem making stream.".format(collIndex.asCompileString).warn;
					}
					// default case: may be a function, or arbitrary symbol to swallow error
					{ value.use({ ~onAsStreamError.(err) }) }
				};
			};
			^value[\eventStreamPlayer]
		}, { ^nil });
	}
	canStream { ^(value.canStream ? true) }
	
	prepareEvent {
		var	key = value[\event][\eventKey];
		(key.notNil or: { value[\event].parent.isNil }).if({
			key ?? { key = \default };
			ProtoEvent(key).exists.if({
				^value[\event].copy.put(\parent, ProtoEvent(key).v)
					.put(\collIndex, collIndex);
			});
		});
		^value[\event]
	}

		// replay needs to change the cleanup func in the old stream before stopping
		// so I need to refer to other adhocs than my own in that case
	streamCleanupFunc { |self, adhoc|
		if(adhoc[\isPlaying] == true) {
			if(adhoc === value and: { value[\printStopMsg] ? true }) {
				"% stream stopped, cleaning up".format(this).postln;
			};
				// if a sequence stops of its own accord, eventStreamPlayer needs to be nil
				// so that stream will be recreated on next play
			adhoc[\eventStreamPlayerWatcher].remove;
			adhoc[\eventStreamPlayerWatcher] = nil;
			adhoc.put(\eventStreamPlayer, nil);
				// optional post-stop activity--true means stopped automatically
			adhoc.stopCleanup(true);
			adhoc.put(\isPlaying, false);
			self.changed(\stop, \stopped);	// if self is nil, this is still OK
		}
	}
		
		// re-pattern, and restart stream if playing
		// should I follow the naming convention of stopNow / stop?
		// would break code
	reset {
		var	oldPlayer; // , oldUpdater;
		this.exists.if({
			(oldPlayer = value[\eventStreamPlayer]).notNil.if({
				value[\eventStreamPlayerWatcher].remove;
			});
			value.reset;
			this.prepareForPlay(doReset:true);
			this.isPlaying.if({
				value[\eventStreamPlayer].play(this.clock, false, AbsoluteTimeSpec(oldPlayer.nextBeat));
				oldPlayer.stop;
			});
			this.changed(\reset);
		});
	}
	
	resetq { |argQuant|
		this.exists.if({
			value[\clock].notNil.if({
				value[\clock].schedAbs(this.eventSchedTime(argQuant) - 0.05, {
					this.reset;
				});
			}, { this.reset; });	// no clock, must do it now
		});
	}
	
		// tryPerform because otherwise Object-isPlaying is invoked (always false)
	isPlaying { ^value.tryPerform(\isPlaying) ? false }
	isDriven { ^value.tryPerform(\isDriven) ? false }

	wrap { |process, doReplay = true|
		var	saveAdhoc;
		process.canWrap.if({
			process = process.copy;
				// maybe not necessary to put in the adhoc?
			process.put(\child, value)
				.put(\clock, value[\clock]);
			saveAdhoc = value;
			value = process;
			this.recalcPropagateKeys;
			doReplay.if({ this.replay(saveAdhoc[\eventStreamPlayer], saveAdhoc) });
		}, {
			Error("This is not a wrapper process. Wrapping not done.").throw;
		});
	}

	rewrap { |process, doReplay = true|
		var	saveAdhoc;
		process.canWrap.if({
				// if there is no child, must delegate to -wrap
			value[\child].isNil.if({ ^this.wrap(process, doReplay) });
			process = process.copy;
				// maybe not necessary to put in the adhoc?
			process.put(\child, value[\child])
				.put(\clock, value[\clock]);
			process[\event].put(\child, value[\child])
				.put(\clock, value[\clock]);
				// transfer other required keys
			process.rewrapKeys.do({ |key|
				process[key].isNil.if({
					process.put(key, value[key])
				});
			});
			saveAdhoc = value;
			BP(\saved).value = nil;
				// clear child so saved process can't be replayed
				// and save; typical chucking use precludes saving, so I do it automatically
				// user should do BP(\saved) => BP(\myWrapper) after rewrap
				// this should also be used for freeing resources
			saveAdhoc =>.overwrite BP(\saved);
			value = process;
			this.recalcPropagateKeys;
			doReplay.if({ this.replay(saveAdhoc[\eventStreamPlayer], saveAdhoc) });
		}, {
			Error("This is not a wrapper process. Wrapping not done.").throw;
		});
	}
	
		// pop one wrapper process off the stack (lifo)
		// can use BP(\x).unwrap => BP(\y) to save state of wrapper, or to call freeCleanup
	unwrap { |doReplay = true|
			// extract the child process
		var	oldWrapper;
		value.canWrap.if({
			oldWrapper = value;
			value = value.child;
			this.recalcPropagateKeys;
			doReplay.if({ this.replay(oldWrapper[\eventStreamPlayer], oldWrapper) });
		}, {
			Error("Can't unwrap a child process.").throw
		});
		^oldWrapper	// returns nil on failure
	}
	
// throw everything out and start with this incoming process
// this method is not fully supported yet
	overwrite { |process|
		var	saveAdhoc, esp, updater;
		saveAdhoc = value;		// to preserve this Proto through the scheduling
// this test is bad
		((esp = value[\eventStreamPlayer]).notNil
			and: { esp.isPlaying and: { esp.nextBeat.notNil } }).if({
				// let it play through this event
			value[\clock].schedAbs(esp.nextBeat - 0.05, {
				saveAdhoc[\eventStreamPlayerWatcher].remove;
				saveAdhoc[\eventStreamPlayerWatcher] = nil;
				updater = Updater(esp, { |obj, what|
					if(what === \stopped) {
						this.streamCleanupFunc(nil, saveAdhoc);
						updater.remove;	// one-shot
					};
				});
				this.stopNow(saveAdhoc)
			});
		});
		value = process;
	}
	
	replay { |oldEventStreamPlayer, oldAdhoc|	// process must be playing
		var	nextTime, updater;
		(oldEventStreamPlayer.isPlaying and: { oldEventStreamPlayer.nextBeat.notNil }).if({
			nextTime = oldEventStreamPlayer.nextBeat;
				// to prevent the stream's cleanup from messing up my flags
			oldAdhoc[\eventStreamPlayerWatcher].remove;
			oldAdhoc[\eventStreamPlayerWatcher] = nil;
			updater = Updater(oldEventStreamPlayer, { |obj, what|
				if(what === \stopped) {
					this.streamCleanupFunc(nil, oldAdhoc);
					updater.remove;	// one-shot
				};
			});
			oldEventStreamPlayer.stop;
			value[\isPlaying] = true;
				// make a new one and schedule it for the next event time
			this.asEventStreamPlayer.play(value[\clock], false, AbsoluteTimeSpec(nextTime));
		});
	}
	
		// to be stored in play event
		// assumes we're in the environment already
	propagateDownFunc {
		^#{	~child.notNil.if({
				~child.event[\allKeysToPropagate].do({ |key|
					~child.event.put(key, key.envirGet);
				});
				~child.event.put(\child, ~child.child)
					.put(\clock, ~clock)
						// i.e., this func!
					.put(\propagateDownward, ~propagateDownward);
			});
		}
	}
	
		// this should be called on wrapping/unwrapping
		// I don't want to do this per note!
	recalcPropagateKeys {
		var	keys, event, child;
		keys = IdentitySet.new;
			// first descend into all child events to get a distinct set of keysToPropagate
			// from each
		child = value;	// start with outermost
		{ child.notNil }.while({
			(event = child[\event]).notNil.if({
				keys = keys.union(event[\keysToPropagate])
					.union(ProtoEvent(event[\eventKey]).v[\keysToPropagate]);
			});
			child = child[\child];
		});
			// now populate all child events with allKeysToPropagate
		child = value;
		{ child.notNil }.while({
			child.event.tryPerform(\put, \allKeysToPropagate, keys);
			child = child.child;
		});
	}
	
	promoteChildEventObjects {
		var	childEvent, event;
		(value[\child].notNil and: { (childEvent = value[\child][\event]).notNil
			and: { (event = value[\event]).notNil } }).if({
			childEvent[\allKeysToPropagate].do({ |k|
				(childEvent[k].notNil and: { event[k].isNil }).if({
					event[k] = childEvent[k];
				});
			});
		});
	}
	
		// GUI support
	
		// midi trigger support
	draggedIntoMTGui { |gui, index|
		gui.model.add(this, index);
		^true	// success flag
	}
	
	draggedIntoVoicerGUI { |gui|
		var	voicer;
		(voicer = gui.model).active.if({
			useVoicerProxy.if({
				this.voicer = gui.model;
			}, {
				this.voicer = gui.model.voicer;
			});
			Post << "Changed voicer for BP(" << collIndex.asCompileString << ") to "
				<< voicer.asString << "\n";
		}, {
				// otherwise, if this process has a useGui method, use it
				// delegate because this is more complex than it sounds
			value[\useGui].notNil.if({ VP.doUseGuiOnBP(this, gui) });
		});
	}
	
	draggedIntoMixerGUI { |gui|
		value[\chan].notNil.if({
			gui.mixer_(value[\chan])
		}, {
			"No mixerchannel defined in this process. Cannot assign to MCGui.".warn;
		});
	}

	asMixerChannelGUI { |board|
		var	mixer;
		if((mixer = value[\chan]).notNil) {
			^mixer.mcgui ?? { MixerChannelGUI(mixer, board) }
		} {
			^nil
		};
	}

		// for BP, using a pseudomethod not defined in the Proto
		// should throw an error
	doesNotUnderstand { |selector ... args|
		(this.exists and: { selector.isSetter or: { value.respondsTo(selector) } }).if({
			^value.performList(selector, args)
		}, { DoesNotUnderstandError(this, selector, args).throw });
	}
}

// microrhythms
// contains a pattern that returns a 4-item array: [delta, length, gate, args]
// "args" is optional
// maybe somePattern.collect({ |val| [val, val*2, someOtherVal] })
// or Ptuple([deltaPat, lengthPat, gatePat], repeats)
// if finite, this determines the length of a chord gesture triggered by macrorhythm player
// chord prototype has a microrhythm pattern that outputs symbolic keys into this collection

	// should this allow new? no new, allows default
MicRh : AbstractChuckNewDict {
	bindPattern { |pattern|
		value = pattern;
	}
	
		// #{ populate some variables, then return a pattern based on the vars } => MicRh(...)
		// also, #{ |notePattern| Pfin(notePattern.estimateLength, notePattern) }
		// to allow gestures to retain their integrity across note-pattern changes
	bindFunction { |func|
		value = func
	}

	bindProto { |proto|
		if(proto.canEmbed) { value = proto } {
			Error("Proto => %: Proto cannot embed into stream, not valid here"
				.format(this.asCompileString)).throw;
		}
	}
	
		// will be called at play time
		// if value is already a Pattern, .value will return the pattern unmodified
		// a function will be executed -- notePattern is for pattern length estimation
	asPattern { |... args|
		^value.value(*args)
	}

	embedInStream { |inval ... args| ^this.asPattern(*args).embedInStream(inval) }
}

// macrorhythms
// used in wrapper processes
// should output either a simplenumber (delta) or an array [delta, length]
// if length is not nil, it is the maximum time a child process will be allowed to play

// process should pattern as Pbind(#[\delta, \length], pattern)

MacRh : MicRh {
}

// proto-event holder
ProtoEvent : AbstractChuckNewDict {
	bindEvent { |event|
		event[\play].isNil.if({
			"Event is missing a play function. Not added.".warn;
		}, {
				// consolidate keys from event and its parent into one parent event
			value = Event(parent: ());
			event.parent.notNil.if({ value.parent.putAll(event.parent) });
			value.parent.putAll(event);
		});
	}
	
		// quick way to compose an event with multiple types from existing ProtoEvents
		// ProtoEvent.composite(#[singleSynthPlayer, polySynthPlayer]) => ProtoEvent(\new);
		// protoEvent in the event to play indicates which play function to use
		// first key in the array becomes the default
	*composite { |keys|
		var	out;
		(keys.size > 0).if({
			out = Event.new(parent: ());
			keys.do({ |key|
				out.parent[key] = true;		// indicate that the reference is valid
										// but DO NOT EMBED the prototype
			});
			out.parent.defaultProtoEvent = keys.first;
			out.parent.put(\play, {
				var	proto = ~protoEvent ?? { ~defaultProtoEvent };
				proto.envirGet.notNil.if({
						// currentEnvironment == the event to be played
					~parent = ProtoEvent(proto).value;
						// play func should be replaced in this event copy by now
					currentEnvironment.play;
				});
			});
		}, {
			Error("Must supply keys to ProtoEvent:composite.").throw;
		});
		^out
	}
	
	copy { ^value.copy }
}

ArpegPat : MacRh {
	bindPattern { |pattern|
		value = pattern
	}
	// inherits bindFunction and asPattern(notes)
}

SA : AbstractChuckNewDict {
	classvar	<>defaultEvent;

	var	<argKeys;

	*initClass { defaultEvent = () }

		// array is of the form [pattern, keys]
	bindArray { |array|
		#value, argKeys = array;
	}
	
	// bindFunction?
	
	asPattern { ^value }
}

// dynamically assignable mixer gui slots

MCG : AbstractChuckArray {
	*persistent { ^true }

	bindMixerChannelGUI { |gui| value = gui }
	
	bindMixerChannel { |channel|
		value.mixer = channel;
	}
	
	bindVC { |vc, adverb|
		var	mix;
		adverb ?? { adverb = \target };
		vc.env[adverb].isMixerChannel.if({
			value.mixer_(vc.env[adverb]);
		}, {
			mix = vc.v.tryPerform(\asMixer);
			mix.isMixerChannel.if({ value.mixer = mix },
				{ "VC's target is not a MixerChannel. Can't bind into MCG.".postln; });
		});
	}
	
	bindBP { |bp, adverb|
		adverb ?? { adverb = \chan };
		(bp.exists and: { bp.v[adverb].notNil }).if({
			try { value.mixer_(bp.v[adverb]) }
				{ "Error during MCG-bindBP.".postln }
		});
	}
	
	bindVP { |vp|
		var	mix = vp.v.tryPerform(\voicer).tryPerform(\bus).tryPerform(\asMixer);
		mix.notNil.if({
			{ value.mixer_(mix) }.try({
				"VC's target is not a MixerChannel. Can't bind into MCG.".postln;
			});
		}, { "VC's target is not a MixerChannel. Can't bind into MCG.".postln; });
	}		
}

// overall modalspec for piece
// make sure Mode(\default) is populated!
Mode : AbstractChuckNewDict {
	bindModalSpec { |mode|
		value = mode
	}
	
	bindArray { |modeNames|
		value = modeNames;  // .collect({ |name| Mode(name).v });
	}
	
	bindSymbol { |modeName|
		value = Mode(modeName).copy;
	}
	
	asMode { ^this }
	
		// delegation to referenced object
	doesNotUnderstand { |selector ... args|
		var mode;
		mode = (value.size == 0).if({ value }, { value[0].asMode });
		^mode.perform(selector, *args)	// if this throws an error, then it should
	}
}

// holder for adaptation functions -- may be material manipulators or support funcs
Func : AbstractChuckNewDict {
	var	<>nilProtect = false;
	bindFunction { |func, adverb|
		value = func;
		nilProtect = (adverb == \protectNil);
	}
	
		// args will usually include source material and material to crossbreed with it
	doAction { |... args|
		(this.exists and: { nilProtect }).if({
			^value.valueArray(args) ? args[0]
		}, { ^value.valueArray(args) });
	}
	
		// .eval is a close alternative, maybe more intuitive than doAction
	eval { |... args|
		(this.exists and: { nilProtect }).if({
			^value.valueArray(args) ? args[0]
		}, { ^value.valueArray(args) });
	}
	
		// this means Func(\xyz).value will not return the Function object itself
		// but you can still get it with Func(\xyz).v
	value { |... args|
		(this.exists and: { nilProtect }).if({
			^value.valueArray(args) ? args[0]
		}, { ^value.valueArray(args) });
	}
	
	listArgs {
		this.streamArgs(Post);
	}
	
	proto {
		var	stream;
		stream = CollStream(String.new(256));
		this.streamArgs(stream);
		Document.current.selectedString_("\n" ++ stream.collection)
	}
	
	streamArgs { |collstream|
		collstream << "Func(" <<< collIndex << ").value(";
		value.streamArgs(collstream);	// add function args
		collstream << ");\n";
	}
}

// chucking MIDI controls

// start/stop processes using MIDI keys

// Midi Trigger -- corresponds to a socket in the MIDI hierarchy
// contains a dictionary of note nums -> processes
MT : AbstractChuckNewDict {
	classvar	<>default;	// used in ChuckBrowserKeyController
	classvar	<>readyThreshold = 5;	// how long to hold a process in ready state before clearing
	classvar	<>defaultMinNote = 48, <>defaultMaxNote = 72;  // integer note numbers
	var	<lastBP, noteAllocator;
	
	var	<socket, <>minNote, <>maxNote;	// midi responder, passes messages here
	var	readyID = 0;

	*persistent { ^true }

		// all indices have to be converted to channel objects
	*prNew { |index|
		var	temp;
		this.put(index = index.asChannelIndex, temp = super.prNew(index));
		^temp
	}

	*new { |index|
		var	collTemp;
		^collection[this.name][index = index.asChannelIndex] ?? { this.prNew(index) }
	}
	
		// needed b/c MIDIChannelIndices can be == but not ===
	*collectionType { ^Dictionary }

	init {
		value = IdentityDictionary.new; // note num -> MTNoteInfo
		minNote = defaultMinNote;
		maxNote = defaultMaxNote;
		socket = MTSocket(collIndex, this);
		noteAllocator = ContiguousBlockAllocator(maxNote+1, minNote);
		default = this;
	}
	
	free {
		this.changed(\free);
		socket.free;
		value.do(_.free);
		this.class.collection.removeAt(collIndex);
		collIndex = value = socket = noteAllocator = minNote = maxNote = nil;
		this.releaseDependants;	// remove from dependants dictionary
		this.removeFromCollection;
	}

	bindPR { |pr, adverb|
		BP(pr.collIndex).free;
		this.add(pr => BP(pr.collIndex), adverb);
	}

	bindBP { |bp, adverb|
		this.add(bp, adverb);
	}
	
	bindFact { |fact, adverb|
		(fact.isBP).if({
			this.add(fact.makev.subType_(fact.subType), adverb)
		}, {
			"%'s type is %; must be 'bp' to chuck into MT.".format(fact, fact.v[\type]).warn;
		});
	}
	
		// support for BP([\pr1, \pr2, \pr3, \pr4]) => MT(0) syntax
		// you give up control over where they go
	bindArray { |ar|
		ar.do({ |bp| bp => this });
	}
	
	add { |bp, adverb, updateGUI = true|
		var new, nextAddNote;
			// remove from other slots first
			// normally it's dangerous to remove items from a collection being iterated over
			// but this kvdo guarantees that you will never remove more than one item
			// since the item can't exist in more than one slot at one time
		value.keysValuesDo({ |notenum, mtinfo|
			(mtinfo.bp === bp).if({ this.removeAt(notenum) });
		});
		lastBP = bp;
		nextAddNote = this.convertAdverb(adverb);
		this.adverbIsValidNote(adverb).if({
			noteAllocator.reserve(adverb, 1, false);	// false == suppress warning
		});
		value[nextAddNote].notNil.if({ value[nextAddNote].free });
		value.put(nextAddNote, new = MTNoteInfo(bp, 0, nextAddNote, this));
		updateGUI.if({ this.changed(new) });	// tell the gui
	}
	
	removeAt { |notenum|
		noteAllocator.free(notenum);
		this.changed(notenum);
		^value.removeAt(notenum).free;
	}

	adverbIsValidNote { |adverb|
		^(adverb = adverb.tryPerform(\asInteger)).notNil
			and: { adverb.inclusivelyBetween(minNote, maxNote) }
	}

	convertAdverb { |adverb|
		(this.adverbIsValidNote(adverb)).if({
			^adverb.asInteger
		}, {
			^noteAllocator.alloc(1)
		});
	}
	
	noteOn { |num|
		var	entry, clock, localReady;
			// entry must exist
		(entry = value[num]).notNil.if({
				// if ready to fire, do play
			if(entry.ready > 0) {
				entry.ready = 0;
				entry.bp.isPlaying.if({
					entry.bp.stop;
				}, {
					entry.bp.play;
				});
				this.changed(entry);
			} {		// else make ready and schedule non-ready
				localReady = (readyID = readyID + 1);
				entry.ready = localReady;
				AppClock.sched(readyThreshold, {
					// disregard switch-off if it's been readied again in the interim
					if(entry.ready == localReady) {
						entry.ready = 0;
						this.changed(entry);
					}
				});
				this.changed(entry);
			};
		});
	}
	
	guiClass { ^MTGui }
}

MTNoteInfo {
	var	<>bp, <>ready, <>noteNum, <owner, <schedFailed = false;
	
	*new { |bp, ready, noteNum, owner|
		var new;
		new = super.newCopyArgs(bp, ready, noteNum, owner);
		bp.addDependant(new);
		^new
	}
	
	asString { ^(noteNum.asMIDINote ++ ": " ++ bp.collIndex) }
	
	playState {
		^case { ready > 0 } { \ready } // ready takes precedence
			{ schedFailed } { \late }
			{ bp.isDriven } { \driven }
			{ bp.isPlaying } { \playing }
			{ \idle }
	}
	
	free { bp.removeDependant(this) }
	
	update { |obj, changer|
		case
			{ changer == \free } {
				owner.removeAt(noteNum);
			}
			{ #[\play, \stop, \driven].includes(changer) } {  // ignore other messages
				schedFailed = false;
				owner.changed(this);	// this should call the gui
			}
			{ changer == \schedFailed } {
				schedFailed = true;
				owner.changed(this);
				AppClock.sched(3.0, {
					if(schedFailed and: { ready <= 0 }) {
						schedFailed = false;
						owner.changed(this);
					};
					nil
				});
			}
	}
}

// storage of midi controllers
CC : AbstractChuckDict {
	*persistent { ^true }

	bindAbstractMIDIControl { |cc|
		value = cc;
	}

	free { |freeCC = true|
		freeCC.if({ value.free });
		this.removeFromCollection;
	}

	draggedIntoVoicerGCGUI { |gui|
		if(this.exists) { gui.model.midiControl = this.value }
	}
}
