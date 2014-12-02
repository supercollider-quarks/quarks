
VoicerMIDISocket : AbstractMIDISocket {
	// interface between incoming MIDI messages and one voicer
	// includes keysplit info

	classvar	<>defaultMidiToFreq;

	var	<>lowkey, <>hikey, <>transpose = 0;
	
		// allow voicer args to be drawn from an event pattern for each note on
		// strictly optional
	var	<noteOnArgsPat, noteOnArgsStream, <>noteOnArgsEvent;
	
	var	<ccs;		// VoicerMIDIControls

	var	<midiToFreq;	// a function describing how to convert the midi note into frequency
					// default uses midicps -- override for other intonation schemes

	*initClass {
		defaultMidiToFreq = _.midicps;
	}

	init { arg lo, hi, noteOnPat, midi2Freq;
		lowkey = lo ? 0;
		hikey = hi ? 127;
		ccs = Array.new;
		this.noteOnArgsPat_(noteOnPat);
		noteOnArgsEvent = ();	// baseline is an empty event
		midiToFreq = midi2Freq ?? { defaultMidiToFreq };
	}
	
	midiToFreq_ { |func|
		midiToFreq = func ?? { defaultMidiToFreq };
	}

	noteOnArgsPat_ { |pat|
		noteOnArgsPat = pat;
		noteOnArgsStream = pat.asStream;
	}
	
	noteOn { arg note, vel;
		var	argEvent, argArray;
		((note >= lowkey) && (note <= hikey)).if({
			argEvent = noteOnArgsEvent.copy.put(\midinote, note).put(\velocity, vel);
			(argEvent = noteOnArgsStream.next(argEvent)).notNil.if({
				argArray = Array.new(argEvent.size * 2);
				argEvent.keysValuesDo({ |k, v|
					argArray.add(k).add(v);
				});
			});
				// nil means use no latency
			destination.trigger1(midiToFreq.value(note + transpose), vel/127, argArray, nil);
		});
	}
	
	noteOff { arg note;
		((note >= lowkey) && (note <= hikey)).if({
			destination.release1(midiToFreq.value(note + transpose), nil);
		});
	}
	
	clear {
		(destination.notNil /* && { destination.active } */).if({
			ccs.copy.do({ arg cc;		// unmap cc's
				cc.notNil.if({
					this.removeControl(cc);
				});
			});
				// voicer object is left alone; must be freed separately
			ccs = nil;	// must be reinited before we can use again
			noteOnArgsPat = noteOnArgsStream = noteOnArgsEvent = nil;  // garbage collection
		});
	}
	
	active { ^(destination.notNil and: { destination.active }) }
	
	panic {
		destination.panic;
	}
		
	addControl { arg ccnum, name, value = 0, spec, overrideChan;
		var ratio, gc, cc, vcontrol, newMChan;

			// which channel to pull controls from?
		overrideChan = (overrideChan ?? { parent.channel }).asChannelIndex;
		newMChan = MIDIPort.at(overrideChan) ?? {
				// if this channel hasn't been used yet, make new channel
			newMChan = MIDIChannel.new(overrideChan);
		};
		
			// AbstractMIDIControl does most of this, but I need to handle \pb differently

			// if you're giving a reserved controller name, get it from the allocator
		ccnum.isSymbol.if({ ccnum = newMChan.ccAllocator.get(nil, ccnum) });
		cc = ccnum ?? {  // if I don't have a control, get one
			newMChan.ccAllocator.get(\knob, name) ?? {	// try for a knob first
				newMChan.ccAllocator.get(nil, name)	// then settle for anything
			}
		};
		ccnum = cc.value;	// turns CControl into cc number or \pb symbol
		((cc.tryPerform(\type) == \pb) || (cc == \pb)).if({ 
			ccnum = 128;		// pitch bend stored in special place
				// spec must be adjusted for pitch bend
			spec = spec ? 2;	// if no spec, assume whole-step bend
			spec.isNumber.if({		// simple numbers are semitones
				ratio = spec.midiratio; 	// ratio to bend up by spec semitones
				spec = [ratio.reciprocal, ratio, \exponential].asSpec;
			});
			spec = spec.asSpec;		// in case an array or symbol is sent in
			value = 1;	// initialize to middle (no bending)
		}, {
			spec = spec.asSpec ?? { ControlSpec.new };
		});
			// if making the midisocket after control has been globalized, use existing
			// else make a new global control
		gc = destination.globalControls.at(name)
			?? { destination.mapGlobal(name, nil, value, spec) };

			// if I'm pointing to a proxy, midi controller should point to a proxy also
			// respondsTo(\proxy) is false for VoicerProxies
		vcontrol = VoicerMIDIController.new(newMChan, cc, 
			destination.respondsTo(\proxy).if({ gc }, { gc.proxify }));

		ccs = ccs.add(vcontrol);
	}
	
	removeControl { arg control;	// either number or name
		var cc1;
		control.isKindOf(VoicerMIDIController).if({
			cc1 = control;
		}, {
				// if name, look up number
			((control == \pb) || (control.tryPerform(\type) == \pb)).if({
				cc1 = this.searchControl(\pb);
			}, {
				control = control.value;
				control.isNumber.not.if({
					cc1 = this.searchControl(control.asSymbol);
					cc1.isNil.if({ ^this });	// if not found, don't go forward
				}, {
					cc1 = this.searchControlNum(control);
					cc1.isNil.if({ ^this });
				});
			});
		});
		cc1.notNil.if({
			cc1.free;
			ccs.remove(cc1);
		});
	}
	
	searchControl { arg control;	// takes control name, returns VoicerMIDIController
		var cc1;
		ccs.do({ arg cc;
			(cc1.isNil && cc.isKindOf(VoicerMIDIController)).if({
				(cc.destination.name == control).if({
					cc1 = cc;
				});
			});
		});
		^cc1
	}
	
	searchControlNum { arg control;	// takes control number, returns VoicerMIDIController
		var cc1;
		ccs.do({ arg cc;
			(cc1.isNil && cc.isKindOf(VoicerMIDIController)).if({
				(cc.ccnum.value == control).if({
					cc1 = cc;
				});
			});
		});
		^cc1
	}
	
}
