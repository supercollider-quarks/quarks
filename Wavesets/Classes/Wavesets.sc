// Analyses a soundfile's data into wavesets.
// keep all wavesets objects in global var all.
// only works for single channels.

/*****
SCDoc.indexAllDocuments;
to do:
		analyse turnaround points better:
		reverse-interpolate where between samples
		the actual turnaround point would be,
		and store them in vars <fracMaxima, <fracMinima.
*****/

Wavesets {

	classvar <>minLength = 10; 	// reasonable? => 4.4 kHz maxFreq.
	classvar <all, formatDict;
	classvar <>defaultInst = \wvst0;

	var 	<signal, <name, <numFrames, <sampleRate;
	var	<xings, <lengths, <fracXings, <fracLengths,
		<amps, <maxima, <minima;
	var 	<numXings, <minSet, <maxSet, <avgLength, <sqrAvgLength,
		<minAmp, <maxAmp, <avgAmp, <sqrAvgAmp;
	var 	<>path, <buffer;

	*prepareSynthDefs {
		SynthDef(\wvst0, { arg out = 0, buf = 0, start = 0, length = 441, playRate = 1, sustain = 1, amp=0.2, pan;
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * playRate, 0, length) + start;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor) * env;

			OffsetOut.ar(out, Pan2.ar(snd, pan));
		}, \ir.dup(8)).add;

		SynthDef(\wvst1gl, { arg out = 0, buf = 0, start = 0, length = 441, playRate = 1, playRate2 = 1, sustain = 1,
			amp=0.2, pan;
			var playRateEnv = Line.ar(playRate, playRate2, sustain);
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * playRateEnv, 0, length) + start;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor) * env;

			OffsetOut.ar(out, Pan2.ar(snd, pan));
		}, \ir.dup(8)).add;
	}

	*new { arg name, sig, sampleRate;
		^super.new.init(name, sig, sampleRate);
	}

	*from { arg path, name, toBuffer = true, server;
		var f, sig, ws;

		f = SoundFile.new;
		if (f.openRead(path).not) {
			("Wavesets.from: File" + path + "not found.").warn;
			^nil
		};
		if (f.numChannels > 1) {
			("File" + path + "has" + f.numChannels + "chans."
			"Wavesets only works on mono signals, so please ...").warn;
			// could also take first chan...
			^nil
		};
						// sampleFormat is not updated correctly in SoundFile.

		sig = (formatDict[f.sampleFormat] ? Signal).newClear(f.numFrames);
		name = name ?? { PathName(path).fileName.basename; };
		f.readData(sig);
		f.close;

		ws = this.new(name.asSymbol, sig, f.sampleRate);
		ws.path = path;
		if (toBuffer) { ws.toBuffer(server) };

		^ws
	}

	toBuffer { |server|
		server  = server ? Server.default;
		buffer = buffer ?? { Buffer(server) };
		buffer.allocRead(path, completionMessage: {|buf|["/b_query",buf.bufnum]});
	}

	init { arg argName, argSig, argSampleRate;

		if (all.at(argName).notNil and: { all.at(argName).signal.size == argSig.size },
			{
				("//	waveset" + argName + "seems identical to existing.\n"
				"// ignored.").postln;
				^all.at(argName);
			}
		);
		name = argName;
		signal = argSig;
		numFrames = argSig.size;
		sampleRate = argSampleRate ? Server.default.sampleRate;
		all.put(name, this);
		this.analyse;
	}

	*clear {  all = IdentityDictionary.new; }

	*initClass {
		this.clear;
		formatDict = (
			'int8': 	Int16Array,
			'int16': 	Int16Array,
			'mulaw': 	Int16Array,
			'alaw': 	Int16Array,
			'int24': 	Int32Array,
			'int32': 	Int32Array,
			'float':	Signal
		);
	}

	*at { |key| ^all[key] }

	analyse { arg finishFunc;
	//	var chunkSize = 400, pause = 0.01;	// not used yet

		xings = Array.new;
		amps = Array.new;
		lengths = Array.new;
		fracXings = Array.new;
		maxima = Array.new; 	// indices of possible turnaround points
		minima = Array.new; 	//
		( "Analysing" + name + "...").postcln;

		this.analyseFromTo;
		this.calcAverages;
		("Finished signal" + name + ":" + numXings + " xings.").postcln;
	}

	calcAverages { 		// useful statistics.
		// calculate maxAmp, minAmp, avgAmp, sqAvgAmp;
		// and maxSet, minSet, avgLength, sqAvgLength;

		numXings = xings.size;
		minSet = lengths.minItem;
		maxSet = lengths.maxItem;
		minAmp = amps.minItem;
		maxAmp = amps.maxItem;

		fracLengths = fracXings.drop(1) - fracXings.drop(-1);

		avgLength = xings.last - xings.first / numXings;
		sqrAvgLength = ( lengths.squared.sum / ( numXings - 1) ).sqrt;

		avgAmp = amps.sum / numXings;
		sqrAvgAmp = (amps.squared.sum / numXings).sqrt;

		^this;
	}

			// should eventually support analysis in blocks in realtime.

	analyseFromTo { arg startFrame = 0, endFrame;
		var lengthCount = 0, prevSample = 0.0,
			maxSamp = 0.0, minSamp = 0.0,
			maxAmpIndex, minAmpIndex,
			wavesetAmp, frac;

		// find xings, store indices, lengths, and amps.

		startFrame = max(0, startFrame);
		endFrame = (endFrame ? signal.size - 1).min(signal.size - 1);

		( startFrame to: endFrame ).do({ arg i;
			var thisSample;
			thisSample = signal.at(i);

						// if Xing from non-positive to positive:
			if (	(prevSample <= 0.0) and: (thisSample > 0.0) and: (lengthCount >= minLength),

				{

					if (xings.notEmpty, {
								// if we already have a first waveset,
								// keep results from analysis:
						wavesetAmp = max(maxSamp, minSamp.abs);
						amps = amps.add(wavesetAmp);
						lengths = lengths.add(lengthCount);
						maxima = maxima.add(maxAmpIndex);
						minima = minima.add(minAmpIndex);
					});

					xings = xings.add(i);

								// lin interpol for fractional crossings
					frac = prevSample / (prevSample - thisSample);
					fracXings = fracXings.add( i - 1 + frac );

								// reset vars for next waveset
					maxSamp = 0.0;
					minSamp = 0.0;
					lengthCount = 0;
				}
			);
			lengthCount = lengthCount + 1;
			if (thisSample > maxSamp) { maxSamp = thisSample; maxAmpIndex = i };
			if (thisSample < minSamp) { minSamp = thisSample; minAmpIndex = i };
			prevSample = thisSample;
		});
	}
				// convenience methods:
	plot { |startWs=0, length=1|
		var data = this.frameFor(startWs, length, false).postln;
		var segment = signal.copyRange(data[0], data[0] + data[1] - 1);
		var peak = max(segment.maxItem, segment.minItem.abs);
		segment.plot("Waveset" + name +
				": startWs" + startWs +
				", length" + length +
				", sustain" + data[2].round(0.000001),
				minval: peak.neg,
				maxval: peak);
	}

	eventFor { |startWs=0, numWs=5, repeats=3, playRate=1, useFrac = true|
		var start, length, sustain1;
		#start, length, sustain1 = this.frameFor(startWs, numWs, useFrac);
		^(start: start,
			length: length,
			sustain: sustain1 / playRate * repeats,
			playRate: playRate,
			buf: buffer,
			instrument: defaultInst,
			wsAmp: this.ampFor(startWs, numWs)
		)
	}

	ampFor { |startWs, length=1|
		^amps.copyRange(startWs, startWs + length - 1).maxItem;
	}

	frameFor { arg startWs, numWs = 1, useFrac = true;
		var whichXings = if (useFrac) { fracXings } { xings };
		var startFrame = whichXings.clipAt(startWs);
		var endFrame = whichXings.clipAt(startWs + numWs);
		var length = endFrame - startFrame;
		var sustain = length / sampleRate;

		^[startFrame, length, sustain]
	}

}
