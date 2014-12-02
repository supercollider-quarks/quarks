
// implementation and concept by Alberto de Campo, Rainer Schütz, Julian Rohrhuber
// developed for the virtual gamelan graz 2007



// abstact superclock //

SoftClock : TempoClock {

	var <>dt = 0.1, <>verbose = false;
	var fadeTask, fading=false, isPlaying=true;
	var <rateOfChange = 1.0;
	var <>minTempo = 0.01, <>maxTempo = 100;

	classvar <>all;

	add {
		this.class.all = this.class.all.add(this);
	}

	*stopAll {
		this.all.do { |clock|
			clock.permanent = false;
			if(clock.isPlaying) { clock.stop };
		};
		this.all = nil;
	}

	stop {
		this.stopListen;
		isPlaying = false;
		super.stop;
		if(this.class.all.notNil) { this.class.all.take(this) };
		if(verbose) { ("stopped clock:" + this).postln };
	}

	tempo_ { arg newTempo;
		newTempo = max(newTempo, 1e-256); // zero not allowed.
		rateOfChange = newTempo / this.tempo;
		super.tempo = newTempo;
	}

	pause { arg dur = 0;
		this.fadeTempo(0.0, dur)
	}


	fadeTempo { arg newTempo, dur = 1.0, warp = \cos, clock;
		var start = this.tempo, interpol;
		warp = warp.asWarp;
		if (warp.isKindOf(ExponentialWarp)) { warp.spec.minval_(0.01) };
		if (fading) { fadeTask.stop };
		fadeTask = Task {
			fading = true;
			"fadeTempo starts. going from: % to: %\n".postf(
				start.round(0.001), newTempo.round(0.001));
			(1 .. (dur / dt + 1).asInteger).normalize.do { |val|
				interpol = blend(start, newTempo, warp.map(val));
				this.tempo = interpol;
				if (verbose) { "fadeTempo index: % tempo: %\n".postf(
					val.round(0.001), interpol.round(0.001)) };
				dt.value.wait;
			};
			fading = false;
			"fadeTempo done. tempo was: % new tempo is: %\n".postf(
				start.round(0.001), interpol.round(0.001));
		};
		clock = clock ? SystemClock;
		fadeTask.play(clock);
	}

	warpTempo { arg frac, beats = 1.0, warp = \cos;
		this.fadeTempo(frac * this.tempo, beats, warp, this)
	}

}



ListeningClock : SoftClock {

	var <listener;
	var <>empathy = 0.5, <>confidence=0.5;
	var <>others, <>weights;
	var <>phaseWrap, phaseOffset = 0.0;

	classvar <>all;

	adjust {
		var tempo = this.othersMeanTempo;
		var beats = this.othersMeanBeats;
		if(tempo.notNil) {
			this.prAdjust(beats - this.elapsedBeats, tempo)
		}
	}

	startListen {
		listener.stop;
		listener = this.makeTask({ others !? { this.adjust }; });
		listener.play;
	}

	stopListen {
		listener.stop;
	}

	isListening {
		^listener.isPlaying
	}

	makeTask { arg func;
		^if(this.allPermanent) {
			SkipJack({ if(isPlaying, func) }, { dt })
		} {
			Task { loop { dt.wait; if(isPlaying, func) } }
		}
	}

	allPermanent { ^if(others.isNil) { true } { others.every(_.permanent) && this.permanent } }

	addClock { arg clock, weight;
		others = others.add(clock);
		if(weight.notNil) {
			weights = weights.add(weight);
			// todo: make weights un-normalized!
		};
	}

	setClocks { arg clocks, argWeights, start=true;
		var listening = start or: { this.isListening };
		this.stopListen;
		if(clocks.isNil or: { clocks.isEmpty }) {
			^this
		};
		others = clocks;
		argWeights !? { weights = argWeights.normalizeSum };
		if(listening) { this.startListen };
	}


	othersMeanTempo {
		if(others.isNil or: { others.isEmpty }) { ^nil };
		^if(weights.isNil) {
			others.collect(_.tempo).mean
		} {
			others.collect(_.tempo).mean {|x, i| x * weights[i] } * weights.size
		}
	}

	othersMeanBeats {
		if(others.isNil or: { others.isEmpty }) { ^nil };
		^if(weights.isNil) {
			others.collect(_.elapsedBeats).mean
		} {
			others.collect(_.elapsedBeats).mean {|x, i| x * weights[i] } * weights.size
		}
	}

	// private implementation

	prWrapPhase { |beats|
		var wrapped;
		if(phaseWrap.isNil) { ^beats };
		wrapped = (beats + phaseOffset).wrap2(phaseWrap); // allow for both signs
		phaseOffset = beats - wrapped; // keep previous offset
		//if(verbose) { [\beats, beats, \wrapped, wrapped, \phaseOffset, phaseOffset].postln; };
		^wrapped.postln
	}

	prAdjust { |deltaBeats, argTempo|
		var phaseDiff = this.prWrapPhase(deltaBeats);
		var myTempo = this.tempo;
		var timeComp = (phaseDiff * this.empathy);
		var newTempo = (blend(argTempo, myTempo, this.confidence) + timeComp)
				.clip(minTempo, maxTempo);
		if (verbose
			and: { (phaseDiff.abs > 0.001)
			or: { (newTempo - myTempo).abs > 0.001 } })
		{
			"Clock - adjust - avgDeltaBeats: % 	avgTempo: % timeComp: % newTempo: %"
			.format(*[deltaBeats, argTempo, timeComp, newTempo].round(0.0001)).postln;
		};
		this.tempo_(newTempo);
	}

}

