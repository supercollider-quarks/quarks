
var saveSubType = AbstractChuckArray.defaultSubType;
AbstractChuckArray.defaultSubType = \melodyPlayer;

// simple, raw-midi melody player, no adaptive functionality
PR(\abstractProcess).v.clone({
	~event = (midi: true, eventKey: \voicerNote);
	~requiredKeys = #[\sequence];
	~index = 0;
	~repeats = inf;

		// each sequence type is responsible for its own code to receive a sequence
		// should not mix types: if a chord sequence needs to own a melody sequence,
		// it should contain a separate object and delegate melody functions to it
	~acceptMIDIBuf = #{ |buf, adverb|
		~sequence = buf;
			// necessary because buf can change while sequence is playing
		(~index >= buf.size).if({
			~index = ~index % buf.size;	// or should this just be 0?
		});
		currentEnvironment
	};
	
	~asNotePattern = { 
		// Pfunc is roughly 6.7 times SLOWER than Prout
		Prout({ |inval|
			~repeats.do {
				~index = 0;
				while { ~index < ~sequence.size } {
					inval = ~sequence[~index].embedInStream(inval);  // output
					~index = ~index + 1;
				};
			};
		});
	};
	
	~asPattern = #{ 
		Pbind([\note, \delta], ~asNotePattern.value.collect({ |n| [n, n.dur] }));
	};
}, nil, #[\requiredKeys]) => PR(\mel1);

// melodic process that uses melAdaptSegs
PR(\abstractProcess).v.clone({
	~storageProto = \melodyStorage;
	~useRh = false;	// useRhythmProfile -- but this is a performance parm so I'm shortening it
	~rhythmProfileProto = nil; // \randRhythm;
	~rhythmProfileReset = false;
	~rhythmQuant = 0.25;	// default, quantize to 16th
	~event = ~event.copy.put(\eventKey, \voicerNote);
	~resetSeg = true;	// by default, when resetting, start from the beginning
	~requiredKeys = #[\mel];
	~adTest = \adTest;		// this is a default, can change -- currently unused
	~eugTest = \dummyEugTest;
	~adaptProb = 1; //0.4;
	~variantThreshold = 4;	// when more than 4 variants, drop some; see ~eugenicize
	~splitFunc = \noSplit;	// specify using buf.chuck(aBP, mel, (splitFunc: \xxx))
	~defaultMIDIType = \mel;
	
	~newMelFlag = false;	// internal flag: when a new melody is given, this becomes true

	~acceptMIDIBuf = #{ |buf, adverb, parms|
		var	result, temp;
		parms.isNil.if({
			parms = buf.tryPerform(\properties).copy ?? { () };
		}, {
			(temp = buf.tryPerform(\properties)).notNil.if({
					// should not overwrite parms for values already populated in parms
					// if properties includes a mode, this allows you to override the mode
					// at chuck time
				parms = temp.copy.putAll(parms);	// is this functionally equivalent?
			});
		});
			// not entirely sure about this for splitFunc
		parms.keysValuesDo({ |k, v|
			v.notNil.if({ currentEnvironment.put(k, v) });
		});
		
			// get the proper action, either from supplied adverb or buf type
		adverb = adverb ?? { parms[\type] ? \mel };

		(adverb != \rhythm).if({
			result = (adverb ++ "PrepareSequence").asSymbol.envirGet
				.value(~midiParse.if({ buf = buf.parse }, { buf }), parms);
				// boy, this is an ugly workaround -
				// but without it, adaptation data were incorrectly stored in this rather than storage obj
			(adverb == \mel).if({
				adverb.envirPut(result);
			});
		}, {
			~rhythm = ~rhythmPrepareSequence.value(buf, parms);
		});			

			// populate rhythm profile if there isn't one and I'm receiving a melody
		(~rhythmProfileProto.notNil and: ~rhythm.isNil and: (adverb == \mel)).if({
			~rhythm = ~rhythmPrepareSequence.value(buf, parms);
		});
		
		currentEnvironment
	};
	
		// should always return a new mel object, but should keep adapt (and other stuff?)
		// why always a new object? -- to save mel and reuse it
	~melPrepareSequence = #{ |buf, parms|
		var	saveMel = ~mel;
		(~mel = PR(~storageProto).v.copy)
			.prepareSequence(\mel, buf, currentEnvironment, parms);
		saveMel.tryPerform(\adapt).notNil.if({
			~mel[\adapt] = saveMel[\adapt];
		});
		~passInValue !? { ~mel[\passInValue] = ~passInValue };
		~newMelFlag = true;
		~mel
	};
	
		// add adaptation data to current melody if it exists
	~adaptPrepareSequence = #{ |buf, parms|
		~mel.isNil.if({
			~mel = PR(~storageProto).v.copy;
		});
		~mel.prepareSequence(\adapt, buf, currentEnvironment, parms);
	};
	
	~rhythmPrepareSequence = #{ |buf, parms|
			// create a new rhythm profiler only if needed
		(~rhythm ?? { PR(~rhythmProfileProto).v.copy })
			.prepare(buf, ~getPassInValue.(PR(~rhythmProfileProto).v.keysFromParent, false),
				parms, currentEnvironment)
	};
	
	~dumpSegments = { 
		~mel.mel.do({ |phr, i|
			"Phrase %\n".postf(i);
			phr.mel.do({ |seg, j|
				"\tSegment %\n".postf(j);
				seg.segs.do({ |adapt, k|
					"\t\tAdaptation %\n".postf(k);
					adapt.notes.do({ |note|
						"\t\t%\n".postf(note.asCompileString);
					});
					$\n.post;
				});
				$\n.post;
			});
			$\n.post;
			$\n.post;
		});
	};				
	
// potential problem: do some numeric parms belong to melodyStorage?
// yes: repeats, adaptProb, eugenicizeProb, variantThreshold, rhythmQuant
	~bindSimpleNumber = #{ |num, adverb|
		adverb.envirPut(num);
	};
	
	~clearAdapt = #{
		~mel.clearAdapt;
	};

	~getMode = #{  ~mode ? \default };
	~mode_ = #{ |mode|
		mode = mode.tryPerform(\collIndex) ? mode ? \default;  // pass a symbol in as the mode
		currentEnvironment.put(\mode, mode);
		~mel !? { ~mel.mode = mode };
		~event.put(\mode, ~mode.value);
		currentEnvironment
	};
	
// function test breaks the possibility of passing a function as such
	~getPassInValue = #{ |keys, save = false|
		var	out;
		out = ();
		keys.do({ |key|
			out.put(key, key.envirGet.isFunction.if({ key.envirGet.value }, { key.envirGet }));
		});
		(save ? false).if({ ~passInValue = out; });
		out
	};
	
	~asPattern = #{ 
		var rhythm;
		~event.put(\mode, ~mode.value);  // ensure event knows its mode
		~rhythmStream = ~rhythm.asPattern.asStream;
			// need to pass in repeats among other parms
			// true = save the passinvalue in the main BP environment
		~mel.asSegStream(\asPattern, ~getPassInValue.(~mel.keysFromParent, true));
		~newMelFlag = false;
		Prout({ |inval|
			var phrase, phraseStream, seg, rhy;
				// try to get phrase; if nil, is this a new melody object?
			{ ((phrase = ~mel[\segStream].next).isNil and: { ~newMelFlag }).if({
						// new melody object -- make the phrase stream
					~mel.asSegStream(\asPattern,
						~getPassInValue.(~mel.keysFromParent, true));
					~newMelFlag = false;
					phrase = ~mel[\segStream].next;
				});
				phrase.notNil  // if that fails, nothing to do
			}.while({
				phraseStream = phrase  //.asStream(~getPassInValue.value);
					.perform(\asPattern,
						~getPassInValue.(~mel[\keysFromParent])).asStream;
				{ (seg = phraseStream.next).notNil }.while({
					inval = seg.embedInStream(inval)
				});
					// stick in rest if available
					// passing in currentEnvironment so pattern can get clock
					// passing in phr because (sometime) phrase will contain metric alignment
					// last bit is not implemented now
				(rhy = ~macroStream.next(phrase)).notNil.if({
					(type: \rest, delta: rhy).yield
				});
			});
		})
		.collect(e { |ev|	// apply extra synth args, if available
			~argsStream.notNil.if({
				~argsStream.next(ev).put(\argKeys, ~argKeys)
			}, { ev })
		})
	};

// asNotePattern MIGHT BE BROKEN, pretty much deprecated

	~asNotePattern = #{ 
		~mel.asSegStream(\asNotePattern);
		~newMelFlag = false;
		Prout({ |inval|
			var phrase, phraseStream, seg;
			{ ((phrase = ~mel[\segStream].next).isNil and: { ~newMelFlag }).if({
						// new melody object -- make the phrase stream
					~mel.asSegStream(\asPattern/*, ~rhythmStream*/);
					~newMelFlag = false;
					phrase = ~mel[\segStream].next;
				});
				phrase.notNil  // if that fails, nothing to do
			}.while({
				phraseStream = phrase
					.perform(\asNotePattern, ~getPassInValue.value)
					.asStream;
				{ (seg = phraseStream.next).notNil }.while({
					seg.embedInStream(inval)
				});
			});
		})
	};		

	~bindPatDefault = \adapt;
}) => PR(\aiMel);

PR(\aiMel).v.clone({
	~event = ~event.copy.put(\eventKey, \synthNote);
}) => PR(\aiMelSynth);

PR(\aiMel).v.clone({
	~storageProto = \melRandStorage;
}, nil, #[\storageProto]) => PR(\aiMelRand);

PR(\aiMel).v.clone({
		// rename some methods (effectively, to allow super.asPattern)
	~asBasePattern = ~asPattern;
	~asBaseNotePattern = ~asNotePattern;

		// defaults
	~bassID = \currentBassNote;
	~bassIDStream = \currentBassNote;

		// ProtoEvent(\voicerNote) issues the dependency notification
	~asPattern = #{ 
		~asBasePattern.value
			.collect(e { |ev| ev.put(\bassID, ~bassIDStream.next(ev)) });
	};
}) => PR(\aiBass);


AbstractChuckArray.defaultSubType = saveSubType;
