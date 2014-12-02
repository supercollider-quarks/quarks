/*

+	make subclasses:

+	NPVoicerL - done.
  - limit maxVoices,
  + add sostenuto and sustain:
	- when sostenuto is called, hold all current notes until sost false
	- sustain holds all current plus new notes until sustain false

+	NPVoicerMono - monophonic, legato
      maxVoices is 1, if prev still playing,
      grab it and set its params from new note.

+   simple NPVoicerGui? This could show:
--- the proxy (NdefGui), indivParams disabled,
--- SynthDef name, hasGate, numPlayingVoices, top, bot
--- if present: maxVoices, sostenuto, sustain, stealMode
--- maybe also voiceHistory in an optional little plot;

*/

NPVoicer {

	var <proxy, <indivParams, <synCtl, <usesSpawn = false;
	var <synthDesc, <hasGate;

	*new { | proxy, indivParams |
		^super.newCopyArgs(proxy, indivParams ? []);
	}

	prime { |obj, useSpawn = false|
		proxy.prime(obj);
		synCtl = proxy.objects.first;
		usesSpawn = useSpawn ? usesSpawn;
		proxy.awake_(usesSpawn.not);
		if (usesSpawn.not) { proxy.put(0, nil) };
		synthDesc = SynthDescLib.global[synCtl.source];
		// know whether sounds will end by themselves
		if (synthDesc.notNil) {
			hasGate = synthDesc.hasGate;
		};
	}

	put { | key, args |
		if (proxy.awake.not) {
			warn("NPVoicer: proxy not awake; should be awake for sustained voicing.")
		};
		proxy.put(key, synCtl, extraArgs: args );
	}

	release { |key, fadeTime| proxy.removeAt(key, fadeTime) }

	releaseAll { | fadeTime | proxy.release(fadeTime) }

	spawn { |args|
		if (proxy.awake) {
			warn("NPVoicer: proxy is awake; should not be awake for spawning.") };
		proxy.spawn(args);
	}

	playingKeys { ^proxy.objects.indices }

		// the most basic messages for the proxy
	play { | out, numChannels, group, multi=false, vol, fadeTime, addAction |
		proxy.play(out, numChannels, group, multi, vol, fadeTime, addAction)
	}

	playN { | outs, amps, ins, vol, fadeTime, group, addAction |
		proxy.playN(outs, amps, ins, vol, fadeTime, group, addAction);
	}

	stop { | fadeTime = 0.1, reset = false | proxy.stop (fadeTime, reset) }

	end { | fadeTime = 0.1, reset = false | proxy.end(fadeTime, reset) }

	pause { proxy.pause }

	resume { proxy.resume }

	// protect keys that are intended for indivdual nodes
	// from accidental setting by proxy group
	filterIndivPairs { |argList|
		if (indivParams.size > 0) {
			argList = argList.clump(2).select { |pair|
				indivParams.every(_ != pair[0]);
			}.flatten(1);
		};
		^argList
	}

	// set global params: key, val, key, val, ...
	set { |...args|
		args = this.filterIndivPairs(args);
		proxy.set(*args);
	}

	unset { |...keys|
		keys = keys.removeAll(indivParams);
		proxy.unset(*keys);
	}

	map { |...args|
		args = this.filterIndivPairs(args);
		proxy.map(*args);
	}

	unmap { |...keys|
		keys = keys.removeAll(indivParams);
		proxy.map(*keys);
	}

		// set params individually per node
	setAt { |key ... args| proxy.setAt(key, *args); }
	unsetAt { |key ... keys| proxy.setAt(key, *keys); }

}