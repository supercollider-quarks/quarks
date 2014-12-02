
+ Object {
		// chucking
		// subclass responsibility to implement bind methods for each class
		// it can receive: aVP.bindVC(aVC) ---> aVoicerProxy.voicer_(aVoicer)
	=> { |dest, adverb| ^this.chuck(dest, adverb) }
	
	chuck { |dest, adverb, parms|
		dest.tryPerform(("bind" ++ this.bindClassName).asSymbol, this, adverb, parms).notNil.if({
			^dest
		}, {
			Post << "WARNING:\nCould not bind " << this.class.name << " into "
				<< dest.class.name << ". Returning nil.\n";
			^nil
		});
	}
	
		// subclasses of an abstract class can override this to simplify the bind methods
		// for a chuck receiver
	bindClassName { ^this.class.name }
	
	isValidIDictKey { ^false }
	isPattern { ^false }
}

+ SimpleNumber {
	bindClassName { ^\SimpleNumber }	// all subclasses of simplenumber bind as simplenumber
	isValidIDictKey { ^true }
}

+ Pattern {
	isPattern { ^true }
	bindClassName { ^Pattern }	// same for Patterns
}

+ Proto {
	isPattern {
		// can't use env.isPattern
		// because anIdentityDictionary.isPattern will fall back to Object
		^if(env[\isPattern].notNil) { env.use { ~isPattern.() } }
			{ false }
	}
}

	// simultaneous control of multiple processes
	// BP([\mel, \chord, \bass, \drums]).play(4)
+ SequenceableCollection {
	play { |...args|
		this.do({ |item| item.play(*args) });
	}
	
	stop { |...args|
		this.do({ |item| item.stop(*args) });
	}
	
	free {
		this.do(_.free)
	}
	
	clearAdapt { this.do(_.clearAdapt) }
	
	asNoteArray {
		^this.collect(_.asNoteArray).flat
	}
	
		// convert array of freqs, durs, lengths and gates to sequencenotes
	asNotes {
		^this.flop.collect(_.asSequenceNote)
	}
	
	isValidIDictKey { ^true }
}

// posting of function prototypes

+ Function {
	proto {
		var	stream;
		stream = CollStream(String.new(256));
		this.streamArgs(stream);
		Document.current.selectedString_(stream.collection)
	}
	
	listArgs {
		this.streamArgs(Post);
	}
	
	streamArgs { |collstream|
		(def.argNames.size > 0).if({
			def.argNames.do({ |name, i|
				(i > 0).if({ collstream << ", " });
				collstream << name;
			});
		});
	}
}


// kind of bizarre: \aSymbol.free frees the synthdef on all existing servers

+ Symbol {
	free {
		Server.set.do({ |server| server.sendMsg(\d_free, this) });
	}
	
	asMode {
		^Mode(this) ?? { Mode(\default) }
	}
	
	prMap { |degree|
		^this.asMode.prMap(degree)
	}

	prUnmap { |key|
		^this.asMode.prUnmap(key)
	}

	isValidIDictKey { ^true }
}

+ Nil {
	asMode {
		^Mode(\default)
	}
}

+ String {
	free {
		this.asSymbol.free
	}
	
		// needed because Cocoa GUI no longer interprets strings for you
	draggedIntoMTGui { |gui, index|
		^this.interpret.draggedIntoMTGui(gui, index)
	}
}

// chuckable browser uses this
+ ArrayedCollection {
	keys {
		^(this.size == 0).if({ [] }, { (0..this.size-1) });
	}
}


// for passing parms in dictionaries

+ Object {
	atBackup { |key ... fallbacks|
		var out;
		(out = this.tryPerform(\at, key)).notNil.if({ ^out }, {
			fallbacks.do({ |dict|
// eventually I want tryPerform here but Proto doesn't handle it well yet
				dict.isKindOf(Proto).if({
					out = dict[key]
				}, {
					out = dict.tryPerform(\at, key)
				});
				out.notNil.if({ ^out });
			});
		});
//		"atBackup failed: %".format(key).warn;
//		this.dumpBackTrace;
		^nil
	}

	eval { |... args| ^this.value(*args) }
}

//+ EventStreamPlayer {
//	stopWithoutTerminating { stream = nil; isWaiting = false; }
//}


// for importing direct from PR

+ Symbol {
	asProtoImportable { ^PR(this).v }

		// experimental
	eval { |... args| ^Func(this).value(*args) }
}
