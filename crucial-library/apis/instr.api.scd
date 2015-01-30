var cb = { arg reply, name, args, atTime, targetStyle, target;
	var b = MixedBundle.new, synth, s = Server.default;

	synth = Instr(name).prMakeSynth(targetStyle, target, args, b, atTime);
	b.addFunction({ reply.value(synth.nodeID) });
	b.sendAtTime(s, atTime);
};


(
	loadAll: { arg reply;
		Instr.loadAll;
		reply.value
	},

	detail: { arg reply, name;
		var instr;
		name = name.asString;
		instr = Instr.at(name) ?? {Instr.load(name)};
		if(instr.isNil, {
			Error("Instr not found" + name).throw;
		});
		reply.value((
			name: name,
			path: instr.path,
			code: instr.funcDef.sourceCode,
			specs: instr.specs,
			outSpec: instr.outSpec
		));
	},
	play: { arg reply, name ... args;
		cb.value(reply, name, args, nil, \addToTailMsg, nil);
	},
	after: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addAfterMsg, nil);
	},
	before: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addBeforeMsg, nil);
	},
	head: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addToHeadMsg, nil);
	},
	tail: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addToTailMsg, nil);
	},
	replace: { arg reply, target, name, atTime ... args;
		cb.value(reply, name, args, atTime, \addReplaceMsg, nil);
	},

	// spawnEvent

	list: { arg reply;
		reply.value(Instr.leaves.collect(_.dotNotation))
	},
	listBySpec: { arg reply, outSpec;
		reply.value(Instr.selectBySpec(outSpec.asSymbol.asSpec).collect(_.dotNotation))
	},

	addSynthDesc: { arg reply, name, args;
		// for patterns
		Instr(name).add(args);
		reply.value  // not properly synced
	}

);