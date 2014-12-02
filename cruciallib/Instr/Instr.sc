

Instr  {

	classvar <dir;

	var  <>name, <>func, <>specs, <>outSpec, >path;
	var <explicitSpecs;// specs that were explicitly stated on construction (not guessed)

	// specs are optional : can be guessed from the argnames
	// outSpec is optional but recommended : can be determined by evaluating the func and examining the result
	*new { arg name, func, specs, outSpec;
		var previous;
		if(func.isNil,{ ^this.at(name) });
		name = this.symbolizeName(name);
		previous = Library.atList(name.copy.addFirst(this));
		if(previous.notNil,{
			if(previous.isKindOf(Instr).not,{
				Error("The Instr name address " + name +
					"is already occupied by a branch node. You may only add new Instr to the leaves").throw;
			});
			previous.func = func;
			previous.init(specs,outSpec);
			previous.changed(this);
			^previous
		});
		^super.newCopyArgs(name,func).init(specs,outSpec)
	}
	*prNew {
		^super.new
	}
	*at { arg  name;
		^this.objectAt(name);
	}
	*load { arg name;
		// forces a reload from file
		name = this.symbolizeName(name);
		Library.global.removeAtPath(name.copy.addFirst(this));
		^this.at(name)
	}
	*loadAll {
		this.prLoadDir(this.dir);
		this.prLoadDir(Platform.userExtensionDir ++ "/quarks/*/Instr");
	}
	*prLoadDir { arg dir;
		var paths,ext;
		ext = this.extensions;
		paths = (dir +/+ "*").pathMatch.select { |path| ext.includesEqual(path.splitext[1]) };
		paths.do { |path|
			if(path.last == $/,{
				this.prLoadDir(path)
			},{
				{
					path.loadPath(false);
				}.try({ arg err;
					("ERROR while loading " + path).postln;
					err.throw;
				});
			});
		};
	}
	*addExcludePaths {
		// 3.5 + only
		(Platform.userExtensionDir ++ "/quarks/*/Instr").pathMatch.do { arg path;
			LanguageConfig.addExcludePath(path)
		}
		/*
		Instr.addExcludePaths;
		LanguageConfig.store;
	*/
	}
	*clearAll {
		Library.global.removeAt(this)
	}

	*ar { arg name, args;
		var instr;
		instr=this.at(name);
		if(instr.isNil,{
			Error("Instr not found !!"
					+ name.asCompileString + "in Meta_Instr:ar").throw;
		},{
			^instr.valueArray(args)
		})
	}
	convertArgs { arg args;
		if(args.isKindOf(Dictionary),{
			^this.argNames.collect({ arg an,i;
				this.convertArg(args.at(an),i)
			})
		});
		^args ? []
	}
	convertArg { arg a,i;
		if(a.isNil,{^this.defArgAt(i)});
		if(a.isKindOf(Symbol).not
			or: {this.specs.at(i).isKindOf(SymbolSpec)},{
			^a
		});
		// these objects act as placeholders to create the desired synth control
		if(a == \kr,{
			^KrNumberEditor(this.defArgAt(i),this.specs.at(i).as(ControlSpec))
		});
		if(a == \ir,{
			^IrNumberEditor(this.defArgAt(i),this.specs.at(i).as(StaticSpec))
		});
		if(a == 'tr',{
			^SimpleTrigger.new
		});
		if(a == 'ar',{
			^PlayerInputProxy.new;
		});
		Error("Unrecognized Symbol supplied to Instr" + a).throw
	}
	ar { arg ... inputs;
		^func.valueArray(this.convertArgs(inputs));
	}
	value { arg inputs; // this should be ... inputs
		^func.valueArray(this.convertArgs(inputs))
	}
	valueEnvir { arg inputs;
		^func.valueArrayEnvir(inputs ? []);
	}
	valueArray { arg inputs;
		^func.valueArray(this.convertArgs(inputs))
	}
	isValidUGenInput {
		^true
	}
	asUGenInput { ^this.ar }
	asControlInput { ^this.value }
	*kr { arg name, args;
		^this.ar(name,args)
	}
	kr { arg ... inputs;
		^func.valueArrayEnvir(inputs);
	}
	asSynthDef { arg args,outClass=\Out;
		var synthDef;
		synthDef = InstrSynthDef.new;
		synthDef.build(this,this.convertArgs(args),outClass);
		^synthDef
	}
	writeDefFile { arg dir,args;
		this.asSynthDef(args).writeDefFile(dir);
	}
	write { arg dir,args;
		// deprec. same as writeDefFile
		var synthDef;
		synthDef = this.asSynthDef(args);
		synthDef.writeDefFile(dir);
	}
	// for use in patterns
	add { arg args, libname, completionMsg, keepDef = true;
		^this.asSynthDef(args).add(libname,completionMsg, keepDef);
	}
	store { arg args;
		^this.asSynthDef(args).store
	}
	// create a synth
	after { arg anode,args,bundle,atTime,out=0;
		^this.prMakeSynth(\addAfterMsg,anode,args,bundle,atTime,out)
	}
	before { arg anode,args,bundle,atTime,out=0;
		^this.prMakeSynth(\addBeforeMsg,anode,args,bundle,atTime,out)
	}
	head { arg anode,args,bundle,atTime,out=0;
		^this.prMakeSynth(\addToHeadMsg,anode,args,bundle,atTime,out)
	}
	tail { arg anode,args,bundle,atTime,out=0;
		^this.prMakeSynth(\addToTailMsg,anode,args,bundle,atTime,out)
	}
	replace { arg anode,args,bundle,atTime,out=0;
		^this.prMakeSynth(\addReplaceMsg,anode,args,bundle,atTime,out)
	}

	spawnEvent { arg event;
		event['type'] = \instr;
		event['instr'] = this;
		event.play;
	}

	// using in a stream
	next { arg ... inputs;
		^func.valueArray(inputs)
	}
	<>> { arg that;
		// function composition :
		// create an instr that passes output of this to the first input of that
		^CompositeInstr(this,that.asInstr)
	}
	papply { arg ... args;
		// partial application :
		// dict[key=>value, ... ] or [ args1, nil, arg3 , ...]
		if(args.size == 1 and: { args.first.isKindOf(Dictionary) }, {
			args = args.first
		});
		^PappliedInstr(this, args)
	}
	// set the directory where your library of Instr is to be found
	*dir_ { arg p;
		dir = p.standardizePath.withTrailingSlash;
	}

	rate {
		^if(outSpec.notNil,{
			outSpec.rate;
		},{
			// if you aren't audio, you must specify an outSpec
			\audio
		})
	}
	numChannels {
		^if(outSpec.notNil,{
			outSpec.numChannels
		},{ // if you are more than one channel, you must specify an outSpec
			1
		});
	}
	path { ^path }

	maxArgs { ^this.argsSize }
	argsSize { ^func.def.argNames.size }
	argNames { ^(func.def.argNames ? []).asList }
	defArgs { ^(func.def.prototypeFrame ? []).asList }

	argNameAt { arg i;
		var nn;
		nn = func.def.argNames;
		^if(nn.notNil,{nn.at(i)},nil);
	}
	defArgAt { arg i;
		var nn;
		nn = func.def.prototypeFrame;
		^nn.at(i)
	}
	// the default value supplied in the function
	initAt { arg i;
		^(this.defArgAt(i) ?? {this.specs.at(i).tryPerform(\default)})
	}

	defName { ^this.class.symbolizeName(name).collect(_.asString).join($.) }
	// used by Synth
	asDefName { arg args;
		^this.asSynthDef(args).name
	}

	prepareToBundle { arg group,bundle;
		this.asSynthDef.prepareToBundle(group,bundle);
	}

	funcDef { ^func.def }


	prMakeSynth { arg targetStyle,anode,args,bundle,atTime,out=0;
		var b,def, synth,synthDefArgs,argNames;
		anode = anode.asTarget;
		b = bundle ?? {MixedBundle.new};
		args = args ?? { this.specs.collect(_.default) };
		args = this.convertArgs(args);
		def = this.asSynthDef(args);
		InstrSynthDef.loadDefFileToBundle(def,b,anode.server);
		synth = Synth.basicNew(def.name,anode.server);
		synthDefArgs = Array.new(args.size);
		argNames = this.argNames;
		def.allControlNames.do { arg cn,i;
			var ai;
			if(cn.rate != \noncontrol,{
				ai = argNames.indexOf(cn.name);
				if(ai.notNil,{
					 synthDefArgs.add( args.at(ai) )
				},{
					// probably the \out
					if(cn.name == \out,{
						synthDefArgs.add( out )
					},{
						Error("% unmatched controlName %".format(this,cn)).throw;
					})
				})
			})
		};
		b.add( synth.perform(targetStyle,anode,synthDefArgs) );
		if(bundle.isNil,{
			b.sendAtTime(anode.server,atTime)
		});
		^synth
	}

	test { arg ... args;
		var p;
		p = Patch(this.name,args);
		p.topGui;
		^p
	}
	play { arg ... args;
		^Patch(this.name,args).play
	}
	plot { arg args,duration=5.0;
		^Patch(this.name,args).plot(duration)
	}


	*choose { arg start;
		// this is only choosing from Instr in memory,
		// it is not loading all possible Instr from file
		^if(start.isNil,{
			Library.global.choose(this)
		},{
			Library.global.performList(\choose,([this] ++ this.symbolizeName(start)))
		})
	}
	*leaves { arg startAt; // address array
		var dict;
		if(startAt.isNil,{
			dict = Library.global.at(this);
			if(dict.isNil,{ ^[] });
		},{
			dict = this.at(this.symbolizeName(startAt).first);
		});
		if(dict.isNil,{
			Error("Instr address not found:" + startAt.asCompileString).throw;
		});
		^Library.global.prNestedValuesFromDict(dict).flat
	}
	// select instr in your entire library that output the given spec
	*selectBySpec { arg outSpec;
		outSpec = outSpec.asSpec;
		^this.leaves.select({ |ins| ins.outSpec == outSpec })
	}
	// i'm feeling lucky...
	*chooseBySpec { arg outSpec;
		^this.selectBySpec(outSpec).choose
	}

	//private
	*put { arg instr;
		^Library.putList([Instr,this.symbolizeName(instr.name),instr].flatten )
	}
	*remove { arg instr;
		^Library.global.removeAt([this,this.symbolizeName(instr.name)].flatten )
	}
	// bulk insert an orchestra of instr
	*orc { arg name, pairs, outSpec = \audio;
		forBy(0,pairs.size-1,2, { arg i;
			this.new( [name,pairs@i ],pairs@(i+1),nil,outSpec)
		})
	}

	*symbolizeName { arg name;
		if(name.isString,{
			^name.split($.).collect(_.asSymbol);
		 });
		if(name.isKindOf(Symbol),{
			^[name];
		});
		if(name.isSequenceableCollection,{
			^name.collect(_.asSymbol);
		});
		error("Invalid name for Instr : "++name);
	}
	*isDefined { arg name;
		^Library.atList([this] ++ this.symbolizeName(name)).notNil;
	}
	*objectAt { arg name;
		var symbolized,search;
		symbolized = this.symbolizeName(name);
		search = Library.atList([this] ++ symbolized);
		if(search.notNil,{ ^search });
		symbolized.debug("Instr not loaded. Loading from file");
		this.findFileFor(symbolized);

		// its either loaded now or its nil
		^Library.atList([this] ++ symbolized) ?? {("Instr " + symbolized + "still not found after loading from file").warn; nil};
	}
	*findFileFor { arg symbolized;
		var quarkInstr,found;
		// the user's primary Instr directory
		found = this.findFileInDir(symbolized,this.dir);
		if(found.notNil,{ ^found });

		// look in each quark with an Instr directory
		quarkInstr = (Platform.userExtensionDir ++ "/quarks/*/Instr").pathMatch;
		quarkInstr.do({ |path|
			found = this.findFileInDir(symbolized,path);
			if(found.notNil,{ ^found });
		});
		^nil
	}
	*extensions { ^["scd","rtf","txt"] }
	*findFileInDir { arg symbolized, rootPath;
		var pathParts;

		pathParts = symbolized.collect(_.asString);
		pathParts.size.do { arg i;
			var pn;
			pn = rootPath +/+ pathParts[ (0..pathParts.size - i - 1)].join(thisProcess.platform.pathSeparator);

			(pn ++ ".*").pathMatch.do { arg path;
				var pathName;
				var symbols,orcname;
				pathName = PathName(path);
				if(this.extensions.includesEqual( pathName.extension ),{
					path.load;

					orcname = pathName.fileNameWithoutExtension;

					// set path on all those within this file that we just loaded
					symbols = [];
					symbolized.any({ |n|
						n = n.asSymbol;
						symbols = symbols.add(n);
						n === orcname.asSymbol
					});
					Instr.leaves(symbols).do({ |instr| instr.path = path });
					^path
				})
			}
		};
		^nil
	}

	dotNotation { // "dir.subdir.file.instrName"
		^String.streamContents({ arg s;
			name.do({ arg n,i;
				if(i > 0,{ s << $. });
				s << n;
			})
		})
	}

	asString { ^"%(%)".format(this.class.name, this.defName.asCompileString) }
	storeArgs {
		if(this.path.notNil,{
			^[this.dotNotation]
		},{
			^[this.dotNotation,this.func,this.specs,this.outSpec]
		});
	}
	storeableFuncReference {
		if(this.path.notNil,{
			^this.dotNotation
		},{
			^this.func.def.sourceCode
		})
	}
	copy { ^this } // unless you change the address its the same instr

	*initClass {
		Class.initClassTree(Document);

		// default is relative to your doc directory
		if(dir.isNil,{ dir = Document.dir ++ "Instr/"; });

		Class.initClassTree(Event);
		Event.addEventType(\instr,{ arg server;
			var instr, instrArgs,patch;
			~server = server;

			instr = ~instr.asInstr;
			if(instr.notNil,{
				~freq = ~detunedFreq.value;
				~amp = ~amp.value;
				~sustain = ~sustain.value;
				instrArgs = instr.argNames.collect({ arg an,i;
								var inp,spec;
								inp = currentEnvironment[an] ?? {instr.defArgAt(i)};
								spec = instr.specs.at(i);
								if(spec.rate == \control,{
									if(inp.rate == \control,{
										inp
									},{
										IrNumberEditor( inp.synthArg, spec )
									})
								},{
									inp
								})
							});
				patch = Patch(instr,instrArgs);
				patch.play(~group ? server,server.latency,~bus);
				patch.patchOut.releaseBusses; // not needed, and I wont free myself
				~patch = patch;
			})
		});
	}
	init { arg specs,outsp;
		if(path.isNil,{
			path = thisProcess.nowExecutingPath; //  ?? { Document.current.path };
		});
		specs = specs ? #[];
		if(specs.isKindOf(SequenceableCollection).not,{
			Error("Specs should be of type array or nil.").throw
		});
		this.makeSpecs(specs ? #[]);
		if(outsp.isNil,{
			outSpec = nil;
		},{
			outSpec = outsp.asSpec;
			if(outSpec.isNil,{
				("Out spec not found: " + outsp.asCompileString + "for" + this).warn
			});
		});
		this.class.put(this);
		this.class.changed(this)
	}
	makeSpecs { arg argspecs;
		explicitSpecs = argspecs ? [];
		specs =
			Array.fill(this.argsSize,{ arg i;
				var sp,name;
				name = this.argNameAt(i);
				sp = explicitSpecs.at(i);
				// backwards compatibility with old spec style
				if(sp.isSequenceableCollection,{
					// [\envperc]
					// [[0,1]]
					// [StaticSpec()]
					// [0,1]
					if(sp.first.isNumber,{
						sp = sp.asSpec;
					},{
						sp = (sp.first ? name).asSpec
					});
				},{
					sp = (sp ? name).asSpec ?? {ControlSpec.new};
				});
				sp
			});
	}
	// a filter is any instr that has an input the same spec as its output
	isFilter { ^this.specs.any({ |sp| sp == this.outSpec }) }

	guiClass { ^InstrGui }

}


// an object that points to an Instr and is saveable/loadable as a compile string
// rarely needed these days.  to be deprec.
InstrAt {
	var <>name,<>path,instr;
	*new { arg name;
		^super.new.name_(name).init
	}
	init {
		instr = Instr.at(name);
	}
	value { arg ... args; instr.valueArray(args) }
	valueEnvir { arg ... args; ^instr.valueEnvir(args) }
	prepareToBundle { arg group,bundle;
		instr.prepareToBundle(group,bundle);
	}
	asDefName { ^instr.asDefName }
	storeArgs { ^[name] }

}


// make a virtual Instr by reading the *ar and *kr method def
// eg Patch(SinOsc,[ 440 ])

UGenInstr {

	var <ugenClass,<rate,<specs;

	*new { arg ugenClass,rate=\ar;
		^super.new.init(ugenClass,rate)
	}
	storeArgs {
		^[ugenClass , rate ]
	}
	init { arg uc,r;
		ugenClass = uc.asClass;
		rate = r;

		//specs
		specs = this.argNames.collect({ arg ag,i;
			var da,sp;
			ag.asSpec ?? {
				da = this.defArgAt(i) ? 0;
				if(da.isNumber) {
					if(da.inclusivelyBetween(0.0,1.0),{
						//("UGenInstr:init Spec.specs has no entry for: % so guessing default ControlSpec".format(ag.asCompileString)).warn;
						nil.asSpec
					},{
						sp = ControlSpec(da,da,default:da);
						("UGenInstr:init Spec.specs has no entry for: % so creating spec: %".format(ag.asCompileString,sp)).warn;
						sp
					});
				} {
					ObjectSpec.new
				};
			}
		});
	}

	value { arg args;
		^ugenClass.performList(rate,args)
	}
	valueArray { arg args;
		^ugenClass.performList(rate,args)
	}

	ar { arg ... args; ^this.value(args) }
	kr { arg ... args; ^this.value(args) }
	outSpec {
		^rate.switch(
				\ar,\audio,
				\kr,\control,
				\new,\fft // temp hack
				);
	}
	dotNotation { ^ugenClass }
	storeableFuncReference { ^this.dotNotation }
	funcDef {
		^ugenClass.class.findMethod(rate) ?? {
			ugenClass.superclasses.do { arg sc;
				var fd;
				if(sc == UGen) {
					^nil
				};
				fd = sc.class.findMethod(rate);
				if(fd.notNil,{
					^fd
				});
			}
		};
	}
	path { ^ugenClass.filenameSymbol.asString }
	maxArgs { ^this.argsSize }
	argsSize { ^this.funcDef.argNames.size - 1 }
	argNames {
		var an;
		an = this.funcDef.argNames;
		^if(an.isNil,{
			[]
		},{
			an.copyRange(1,an.size - 1)
		})
	}

	//defaultArgs
	defArgs {
		var nn;
		nn=this.funcDef.prototypeFrame;
		^if(nn.notNil,{nn.copyRange(1,nn.size-1)},{[]});
	}

	initAt { arg i;  ^(this.defArgAt(i) ?? {this.specs.at(i).tryPerform(\default)}) }
	argNameAt { arg i;
		var nn;
		nn=this.funcDef.argNames;
		^if(nn.notNil,{nn.at(i + 1)},{nil});
	}
	defArgAt {
		 arg i;
		var nn;
		nn=this.funcDef.prototypeFrame;
		^if(nn.notNil,{nn.at(i + 1)},{nil});
	}

	guiClass { ^UGenInstrGui }
	asString { ^"UGenInstr " ++ ugenClass.name.asString }
	asInstr { ^this }
	name { ^ugenClass.asString }

	*leaves { arg rateMethod; // ar kr new
		var ll;
		^Library.atList([this,'leaves',rateMethod ? 'all']) ?? {
			ll = UGen.allSubclasses;
			if(rateMethod.notNil,{
				ll = ll.select({ arg cls; cls.class.findMethod(rateMethod).notNil })
			});
			ll = ll.sort({ arg a,b; a.charPos <= b.charPos });
			ll = ll.sort({ arg a,b; a.filenameSymbol.asString <= b.filenameSymbol.asString });
			ll = ll.collect(UGenInstr(_));
			Library.putList([this,'leaves',rateMethod ? 'all',ll]);
			ll
		}
	}
}


PappliedInstr : Instr { // partial application

	var <>a,<appliedArgs;
	var <argNames,<defArgs,pindices;

	*new { arg a,args;
		^super.prNew.a_(a.asInstr).appliedArgs_(args).init
	}
	storeArgs {
		var ans;
		ans = ();
		a.argNames.do { arg an,i;
			if(appliedArgs[i].notNil,{
				ans[an] = appliedArgs[i]
			})
		};
		^[if(a.class==Instr,{a.dotNotation},{a}), ans]
	}
	appliedArgs_ { arg args;
		if(args.isKindOf(Dictionary),{
			appliedArgs = a.argNames.collect({ arg an; args[an] });
		},{
			appliedArgs = args.extend(a.argNames.size,nil);
		})
	}
	init {
		var compname;
		compname = a.name.last ++ "|" ++ InstrSynthDef.hashEncode(appliedArgs);
		name = ['_papply', (a.dotNotation ++ "|" ++ InstrSynthDef.hashEncode(appliedArgs))];
		specs = a.specs.select({ arg sp,i; appliedArgs[i].isNil });
		explicitSpecs = [];
		argNames = a.argNames.select({ arg sp,i; appliedArgs[i].isNil });
		defArgs = a.defArgs.select({ arg sp,i; appliedArgs[i].isNil });
		pindices = a.argNames.collect({ arg sp,i; if(appliedArgs[i].notNil,i,nil) });
		this.class.put(this);
		this.class.changed(this);
	}

	value { arg ... args;
		^this.valueArray(args)
	}
	valueArray { arg args;
		var agz;
		// expand args to line up indices with master
		agz = Array.fill(a.argsSize,{ arg i;
				if(pindices[i].isNil,{
					if(args.notEmpty,{
						args.removeAt(0)
					},{
						// ordinarily function would fill in unsupplied args with defArgs
						a.defArgAt(i)
					})
				},{
					appliedArgs[pindices[i]]
				})
			});
		^a.valueArray(agz)
	}

	ar { arg ... args; ^this.valueArray(args) }
	kr { arg ... args; ^this.valueArray(args) }
	outSpec {
		^a.outSpec
	}
	maxArgs { ^this.argsSize }
	argsSize { ^argNames.size }

	initAt { arg i;  ^(defArgs.at(i) ?? {specs.at(i).tryPerform(\default)}) }
	argNameAt { arg i;
		^argNames[i]
	}
	defArgAt { arg i;
		^defArgs.at(i)
	}
	funcDef { ^nil }
	storeableFuncReference { ^this }
	asString { ^this.dotNotation }
	asInstr { ^this }
	guiClass { ^PappliedInstrGui }
}


CompositeInstr : PappliedInstr {

	// output of a goes to the {index} input of b

	var <>b,<>index=0;

	*new { arg a,b,index=0;
		^super.prNew.a_(a.asInstr).b_(b.asInstr).index_(index).init
	}
	storeArgs {
		^[	if(a.class==Instr,{a.dotNotation},{a}),
			if(b.class==Instr,{b.dotNotation},{b}),
			index]
	}
	init {
		var compname;
		compname = a.name.last ++ "|" ++ b.name.last;
		name = ['<>>', (a.dotNotation ++ "|@" ++ index ++ "|" ++ b.dotNotation)];
		specs = a.specs ++ b.specs.select({ arg sp,i; i != index });
		explicitSpecs = [];
		argNames = a.argNames.copy;
		b.argNames.do { |an,i|
			if(i != index,{
				if(argNames.includes(an),{
					argNames = argNames.add( this.findUniqueNameForArg(an,argNames) )
				},{
					argNames = argNames.add( an )
				})
			})
		};
		defArgs = a.defArgs ++ b.defArgs.select({ arg da,i; i != index });
		this.class.put(this);
		this.class.changed(this);
	}
	findUniqueNameForArg { arg argName,argNames;
		var sy;
		argName = argName.asString;
		inf.do { arg i;
			sy = (argName ++ (i+2).asString).asSymbol;
			if(argNames.includes(sy).not,{
				^sy
			})
		}
	}

	valueArray { arg args;
		var f,second,defArgs;
		f = a.valueArray( args.copyRange(0,a.argsSize-1) );

		second = b.defArgs;
		second[index] = f;
		args.copyToEnd(a.argsSize).do { arg ag,i;
			if(i < index,{
				second[i] = ag
			},{
				second[i+1] = ag
			})
		};
		^b.valueArray( second )
	}

	outSpec {
		^b.outSpec
	}
	guiClass { ^CompositeInstrGui }
}


// see Interface
InterfaceDef : Instr {

	var <>onLoad,
		<>onPlay,
		<>onStop,
		<>onFree,

		<>onNoteOn,
		<>onNoteOff,
		<>onPitchBend,
		<>onCC,

		<guiBodyFunction,
		<>keyDownAction,
		<>keyUpAction;

		// do your own views to handle these
		//<>beginDragAction,
		//<>mouseDownAction,
		//<>mouseUpAction,
	gui_ { arg function; guiBodyFunction = function; }

}

