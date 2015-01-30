MIDIAnalysis {
	classvar elements, respTypes, elemDictByType, results;
	*analyse { |rawCapture|
		respTypes = [];
		elements = rawCapture.clump(2).collect {
			|pair| pair[1].put(\rawElName, pair[0])
		};

		elemDictByType = ();

		"*** MIDICapture analysis: ***".postln;
		MIDIMKtl.allMsgTypes.collect { |type|
			var matchingEls = elements.select { |el| el[\midiMsgType] == type };
			[ type, matchingEls.size].postln;
			if (matchingEls.size > 0) {
				respTypes = respTypes.add(type);
				elemDictByType.put(type, matchingEls)
			};
		};

		results = [
			rawElCount: elements.size,
			respTypes: respTypes];

		if (respTypes.includesAny([\noteOn, \noteOff])) {
			this.analyseNoteEls;
		};
		if (respTypes.includes(\touch)) {
			this.analyseTouch;

		};
		if (respTypes.includes(\cc)) {
			this.analyseCC;
		};
		if (respTypes.includes(\bend)) {
			this.analyseBend;
		};

		^results
	}

	*analyseNoteEls {
		var noteOnEls =elemDictByType[\noteOn];
		var noteOffEls =elemDictByType[\noteOff];
		var noteOnChan, noteOffChan;
		var noteOnNotes, noteOffNotes;
		"noteOnEls, noteOffEls:".postln;

		noteOnEls.postcs;
		noteOffEls.postcs;

		noteOnChan = this.compressInfo(noteOnEls, \midiChan);
		noteOffChan = this.compressInfo(noteOffEls, \midiChan);
		"".postln;

		if (noteOnChan.isKindOf(SimpleNumber)) {
			"noteOn uses single channel on notes: ".post;
			noteOnNotes = this.compressInfo(noteOnEls, \midiNote);
			noteOnNotes.postln;
		};

		if (noteOffChan.isKindOf(SimpleNumber)) {
			"noteOff uses single channel on notes: ".post;
			noteOffNotes = this.compressInfo(noteOffEls, \midiNote);
			noteOffNotes.postln;
		};
results = results ++ [
			noteOn: (channel: noteOnChan),
			noteOff: (channel: noteOffChan)
		];

	}

	*compressInfo { |dict, key|
		^dict.collectAs(_[key], Array).asSet.asArray.sort.unbubble;
	}
	// too tired to figure this out now.. later
	reduceToConsecutive {|array|

	}

	*checkMsgTypes { |devDesc|
		var types = Set.new;
		devDesc.collect { |el, i| if (i.odd) {
			var type = el[\midiMsgType];
			type ?? { Error("MIDI device description element must have midiMsgType field: %".format(el)).throw };
			types.add(el[\midiMsgType])

		} };
		^types.asArray;
	}

	*checkForMultiple { |devDesc, typeToFilterBy, dictKeyToCompress|
		var touchEls = devDesc.select { |el, i| (i.odd and: { el[\midiMsgType] == typeToFilterBy }) };
		var touchChan =  this.compressInfo(touchEls, dictKeyToCompress);
		^(channel: touchChan, numEls: touchEls.size);
	}
}