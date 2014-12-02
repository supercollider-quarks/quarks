// maybe some of this should go into NPVoicer.

NPVoicerL : NPVoicer {

	var <>limitVoices = true, <>maxVoices = 16, <voiceHistory, <>stealMode = \oldest;
	var <defParamValues;
	var <sustainedKeysToKeep, <sustained = false;
	var <sostenutoKeysToKeep, <sostenutoed = false;

	// in NPVoicer:prime, check whether synthdef hasGate or not;
	// if not, schedule removal of by sustain
	// if yes, release hould remove it

	prime { |obj, useSpawn|
		super.prime(obj, useSpawn);
		defParamValues = ();
		synthDesc.controlDict.keysValuesDo { |parName, control|
			defParamValues.put(parName, control.defaultValue);
		};
		voiceHistory = List[];
		sustainedKeysToKeep = ();
		sostenutoKeysToKeep = ();
	}

	sustain { |flag = true|
		var keysPlayingNow;
		if (sustained == flag) {^this };

		sustained = flag;

		if (flag) {
			sustainedKeysToKeep.clear;
			this.playingKeys.do{|key|
				sustainedKeysToKeep.put(key, true);
			};
		} {

			keysPlayingNow = this.playingKeys;
			sustainedKeysToKeep.keysValuesDo { |key, toKeep|
				if (keysPlayingNow.includes(key) and: toKeep.not) {
					this.release(key)
				};
			};
			sustainedKeysToKeep.clear;
		};
	}

	sustainedReleaseCheck { |key|

		if (sustained.not) { ^true };

		// note that that key was released,
		// so sustain will let it go when it goes off.
		sustainedKeysToKeep.put(key, false);
		^false;
	}

	sostenuto { |flag = true|
		var keysPlayingNow;
		if (sostenutoed == flag) {^this };

		sostenutoed = flag;

		if (flag) {
			sostenutoKeysToKeep.clear;
			this.playingKeys.do{|key|
				sostenutoKeysToKeep.put(key, true);
			};
		} {
			keysPlayingNow = this.playingKeys;
			sostenutoKeysToKeep.keysValuesDo { |key, toKeep|
				if (keysPlayingNow.includes(key) and: toKeep.not) {
					this.release(key)
				};
			};
			sostenutoKeysToKeep.clear;
		};
	}

	sostenutoReleaseCheck { |key|
		var foundKey;
		if (sostenutoed.not
			or: {sostenutoKeysToKeep[key].isNil }) {
			^true
		};

		// note that key was released, so sustenuto
		// will let it go when it goes off.
		sostenutoKeysToKeep.put(key, false);
		^false;
	}

	put { |key, args|
		super.put(key, args);
		this.removeVoiceAt(key); // super releases earlier voice under that key
		if (sustained) { sustainedKeysToKeep.put(key, true); };
		this.checkLimit(key, args);
		this.trackVoice(key, args);
	}

	release {|key, force = false|
		var sustainedReleaseNow = this.sustainedReleaseCheck(key);
		var sostenutoReleaseNow = this.sostenutoReleaseCheck(key);
		if (force or: { sustainedReleaseNow and: sostenutoReleaseNow }) {
			super.release(key);
			this.removeVoiceAt(key);
		};
	}

	removeVoiceAt { |key|
		var voiceHistIndex;
		voiceHistIndex = voiceHistory.detectIndex { |ev| ev[0] == key };
		voiceHistIndex !? { voiceHistory.removeAt(voiceHistIndex); };
	}

	releaseAll {
		super.releaseAll;
		voiceHistory.clear;
	}

	cmdPeriod { voiceHistory.clear.postln; }

	postHist { voiceHistory.printAll; }

	trackVoice {|key, args|
		var susDefault, susArgIndex, susFromArg, soundingTime;
		voiceHistory.add([key, args]);
		// why use a voicer when notes end by themselves?
		// maybe ask proxy who is still playing every now and then
		if (hasGate.not) {
			// figure how to estimate time synth will live
			susDefault = defParamValues[\sustain];
			susArgIndex = args.indexOf(\sustain);
			susFromArg = susArgIndex !? { args[susArgIndex + 1] };
			soundingTime = susFromArg ? susDefault ? 1;
			defer ({ this.release(key) }, soundingTime);
		};
	}

	findSoftestIndex {
		var minAmp = 1000, minIndex = nil;
		var defAmp = defParamValues[\amp];

		if (defAmp.isNil) { ^nil };

		voiceHistory.do { |ev, evi|
			var ampSymi, ampVali, ampVal = defAmp;
			ampSymi = ev[1].indexOf(\amp);

			if (ampSymi.notNil) {
				ampVali = ampSymi + 1;
				ampVal = ev[1][ampVali]
			};
			if (ampVal < minAmp) {
				minAmp = ampVal;
				minIndex = evi
			};
		};
		"findSoftest: minAmp: %, minIndex: %\n".postf(minAmp, minIndex);

		^minIndex
	}

	checkLimit {
		var keys, index;
		// check before adding the new voice,
		// so it can never be killed
		if (proxy.objects.size <= maxVoices) { ^this };

		stealMode.switch(
			\oldest, { this.release(voiceHistory[0][0]) },
			\lowest, { this.release(proxy.objects.indices[0]) },
			// maybe top and bottom voices will be less dispensable?
			\middle, {
				keys = proxy.objects.indices;
				this.release(keys[keys.size div: 2]);
			},
			\softest, {
				index = this.findSoftestIndex ? 0;
				this.release(voiceHistory[index][0]);
			},
			{ this.release(voiceHistory[0][0]) }
		);
	}
}