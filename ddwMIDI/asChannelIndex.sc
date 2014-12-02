// allow different things to act as channel indices

// array - [port, channel #] - port may be uid or a MIDIPort object
// integer - assumes first port, int is channel index

+ SequenceableCollection {
	asChannelIndex {
		var port;
		MIDIPort.init;
		port = this.at(0);
		port.isNumber.if({
				// low numbers are not uid's, but indices to sources
			(port.abs < MIDIPort.numPorts).if({
				port = MIDIPort.sources.at(port).uid
			})
		}, {
			if(port == \all) { port = 0x80000001 };
		});
		^MIDIChannelIndex.new(port, this.at(1).asMIDIChannelNum)
	}
}

+ Integer {
	asChannelIndex {
		MIDIPort.init;
		^MIDIChannelIndex.new(MIDIPort.sources.at(MIDIChannelIndex.defaultPort), this)
	}
}

+ Object {
	asMIDIChannelNum { 
		^this
	}	// will probably throw error
}

+ Nil {	// first port, channel 0
	asChannelIndex { 
		^0.asChannelIndex
	}
	
	asMIDIChannelNum { ^0 }

	active { ^false }	// if a midi control's destination is nil, it should be inactive
}

+ Symbol {	// allow for omni responder
	asChannelIndex {
		MIDIPort.init;
		^MIDIChannelIndex(MIDIPort.sources.at(MIDIChannelIndex.defaultPort), this.asMIDIChannelNum)
	}
	
	asMIDIChannelNum {
		(this == \omni).if({ ^16 },		// only \omni is valid
			{ (this.asString ++ " is not valid as a MIDI channel number. Symbol should be 'omni'.").die });
	}
}
