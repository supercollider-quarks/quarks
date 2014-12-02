
MonoPortaSynthVoicerNode : SynthVoicerNode {
	var lastScheduledRelease;  // for event usage

	trigger { arg freq, gate = 1, args, latency;
		var bundle;
		if(freq.isNumber) {
			this.shouldSteal.if({
				bundle = this.setMsg([\freqlag, voicer.portaTime, \freq, freq,
					\gate, gate, \t_gate, gate] ++ args);
			}, {
				isReleasing.if({
					bundle = this.releaseMsg(-1.02);	// quick release
				});
					// triggerMsg() sets the synth instance var
				bundle = bundle ++ this.triggerMsg(freq, gate, args ++ [\freqlag, voicer.portaTime]);
				NodeWatcher.register(synth);
					// when the synth node dies, I need to set my flags
				Updater(synth, { |syn, msg|
					(msg == \n_end).if({
							// synth may have changed
						(syn == synth).if({
							isPlaying = isReleasing = false;
							synth = nil;
						});
						syn.releaseDependants;
					});
				});
			});
			target.server.listSendBundle(myLastLatency = latency, bundle);
			frequency = freq;	// save frequency for Voicer.release
			voicer.lastFreqs.add(freq);
			lastTrigger = thisThread.seconds;  // clock.beats2secs(thisThread.clock.beats);
			isPlaying = true;
			isReleasing = false;
		} {
			reserved = false;
		}
	}

	// triggerByEvent { |freq, gate(1), args, latency, length|
	// 	var result = this.trigger(freq, gate, args, latency);
	// 	if(length.isNumber and: { length < inf }) {
	// 		lastScheduledRelease = lastTrigger + (length / thisThread.clock.tempo);
	// 	} {
	// 		lastScheduledRelease = 0
	// 	};
	// 	^result
	// }
	//
	// shouldSteal {
	// 	^super.shouldSteal and: { isReleasing.not }
	// }

	shouldSteal {
		^steal and: {
			isPlaying or: {
				synth.notNil and: { synth.isPlaying }
			}
			// this condition seems wrong for mono voicers...???
			// or: { Main.elapsedTime - lastTrigger < (myLastLatency ? 0) }
			and: { isReleasing.not }
		}
	}

	release { |gate = 0, latency, freq|
		voicer.lastFreqs.remove(freq ?? { frequency });
		super.release(gate, latency, freq);
	}

	// releaseByEvent { |gate(0), latency, freq|
	// 	if(thisThread.seconds >= lastScheduledRelease) {
	// 		^this.release(gate, latency, freq);
	// 	} {
	// 		voicer.lastFreqs.remove(freq ?? { frequency });
	// 	}
	// }
}


// method defs are repeated between these 2 classes because of no multiple inheritance

MonoPortaInstrVoicerNode : InstrVoicerNode {
	var lastScheduledRelease;  // for event usage

	trigger { arg freq, gate = 1, args, latency;
		var bundle;

		if(freq.isNumber) {
			this.shouldSteal.if({
				bundle = this.setMsg([\freqlag, voicer.portaTime, \freq, freq,
					\gate, gate, \t_gate, gate] ++ args);
			}, {
				isReleasing.if({
					bundle = this.releaseMsg(-1.02);	// quick release
				});
				bundle = bundle ++ this.triggerMsg(freq, gate, args ++ [\freqlag, voicer.portaTime]);
				NodeWatcher.register(synth);
					// when the synth node dies, I need to set my flags
				Updater(synth, { |syn, msg|
					(msg == \n_end).if({
							// synth may have changed
						(syn == synth).if({
							isPlaying = isReleasing = false;
							synth = nil;
						});
						syn.releaseDependants;
					});
				});
			});

			target.server.listSendBundle(myLastLatency = latency, bundle);

			frequency = freq;
			voicer.lastFreqs.add(freq);
			lastTrigger = thisThread.seconds;
			isPlaying = true;
			isReleasing = false;
		} {
			reserved = false;
		}
	}

	// triggerByEvent { |freq, gate(1), args, latency, length|
	// 	var result = this.trigger(freq, gate, args, latency);
	// 	if(length.isNumber and: { length < inf }) {
	// 		lastScheduledRelease = lastTrigger + (length / thisThread.clock.tempo);
	// 	} {
	// 		lastScheduledRelease = 0
	// 	};
	// 	^result
	// }
	//
	// shouldSteal {
	// 	^super.shouldSteal and: { isReleasing.not }
	// }

	shouldSteal {
		^steal and: {
			isPlaying or: {
				synth.notNil and: { synth.isPlaying }
			}
			// this condition seems wrong for mono voicers...???
			// or: { Main.elapsedTime - lastTrigger < (myLastLatency ? 0) }
			and: { isReleasing.not }
		}
	}

	release { |gate = 0, latency, freq|
		voicer.lastFreqs.remove(freq ?? { frequency });
		super.release(gate, latency, freq);
	}

	// releaseByEvent { |gate(0), latency, freq|
	// 	[thisThread.seconds, lastScheduledRelease].debug("releaseByEvent");
	// 	if(thisThread.seconds >= lastScheduledRelease) {
	// 		^this.release(gate, latency, freq);
	// 	} {
	// 		voicer.lastFreqs.remove(freq ?? { frequency });
	// 	}
	// }
}
