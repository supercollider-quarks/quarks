
var saveSubType = AbstractChuckArray.defaultSubType,
	markovSet = 'MarkovSet'.asClass;

// rhythm profiles
// each return value from the pattern should be of the form [delta, length]
// rhythm is applied in PR(\melModeSeg).v[\asNotePattern]
// calls to the pattern with next will be passed the SequenceNote object
AbstractChuckArray.defaultSubType = \rhythmProfile;

PR(\abstractProcess).v.clone({
		// getQuant calls a method
	~keysFromParent = #[rhythmQuant, getQuant, midiParse, rhythmProfileReset, beatsPerBar];

	~prepare = #{ |buf, passInValue, parms, melBP|
		var 	quant = parms.atBackup(\rhythmQuant, passInValue),
			beatsPerBar = passInValue[\beatsPerBar] ??
				{ (parms[\quant] ?? { passInValue[\getQuant] }).quant },
			needToParse = parms.atBackup(\midiParse, passInValue).not,
			reset = parms.atBackup(\rhythmProfileReset, passInValue);

		(needToParse ? false).if({ buf = buf.parse });  // assumes buffer has already been parsed
		buf = buf.quantize(quant, beatsPerBar);  // simple quantize; deal w/ flexQuantize later?
		~durLen = [buf.durs, buf.lengths].flop;
		currentEnvironment
	};

	~asPattern = { 
		Prand(~durLen, inf)
	};
}) => PR(\randRhythm);

if(markovSet.notNil) {
	PR(\randRhythm).v.clone({
		~prepare = { |buf, passInValue, parms|
			var 	quant = parms.atBackup(\rhythmQuant, passInValue),
				beatsPerBar = passInValue[\beatsPerBar] ??
					{ (parms[\quant] ?? { passInValue[\getQuant] }).quant },
				needToParse = parms.atBackup(\midiParse, passInValue).not,
				reset = parms.atBackup(\rhythmProfileReset, passInValue);
	
			(needToParse ? false).if({ buf = buf.parse });  // assumes buffer has already been parsed
			buf = buf.quantize(quant, beatsPerBar);  // simple quantize; deal w/ flexQuantize later?
	
				// may chuck in new material without overwriting previous analyses
			(~durMarkov.isNil or: { reset ? false }).if({
				~durMarkov = markovSet.new(updateSeeds: true);
				~lenMarkov = markovSet.new(updateSeeds: true);
			});
	
			~durMarkov.parse(Pseq(buf.durs, inf), buf.size+1);
				// associate dur to length
			buf.notes.do({ |note| 
				~lenMarkov.read(note.dur, note.length)
			});
			~durStream = ~durMarkov.asStream;
			currentEnvironment
		};
	
		~asPattern = #{ 
			var dur;
			Pfunc(e {
				[dur = ~durStream.next, ~lenMarkov.next(dur)]  // .debug("markovRhythm output")
			})
		};
		
	}) => PR(\markovRhythm);
};


AbstractChuckArray.defaultSubType = saveSubType;
