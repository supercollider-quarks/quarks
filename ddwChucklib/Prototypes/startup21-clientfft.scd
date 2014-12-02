
var	subtype = AbstractChuckArray.defaultSubType;
AbstractChuckArray.defaultSubType = \fftTransient;

Proto({
	~keysFromParent = #[checkMinPeak, minPeak, window, imag, cos, melfreqsize, melmap,
		fftsmooth, slopedist, slopesmooth, halffft];
	
		// note, parent must NOT be stored in this environment
		// it's used only to get the shared variables
	~prep = { |audio, index, parent|
		~audio = audio;
		~index = index;
		~keysFromParent.do({ |key| key.envirPut(parent[key]) });
		currentEnvironment
	};
	
		// buf will be used in the buffer-based version
	~getfft = { 
		var	srcfft, peak, melfft;

		~peak = ~audio.abs.maxItem;
		((~checkMinPeak and: { ~peak >= ~minPeak }) or:
				{ ~checkMinPeak.not and: { ~peak > 0 } }).if({
			~fhzfft = ~audio / ~peak * ~window;
			~fhzfft = ~fhzfft.as(Signal).fft(~imag, ~cos);
			~fhzfft = ~fhzfft.magnitude[..~halffft-1];
			~srcfft = Array(~melfreqsize);
			~melmap.doAdjacentPairs({ |a, b|
				~srcfft.add(~fhzfft[a..b-1].mean);
			});
			~srcfft = ~srcfft.avgsmooth(~fftsmooth);
			
			currentEnvironment
		});	// returns nil if peak condition not met
	};
	
	~fftPostProcessing = { 
		var	fft = ~srcfft,
			slopes = Array.new(fft.size - ~slopedist),
			peaks = List.new,	// stores index and magnitude of peak
			peakIndices = List.new;

			// (crudely) differentiate the fft magnitudes and smooth the curve
		(fft.size - ~slopedist).do({ |i|
			slopes.add(fft[i + ~slopedist-1] - fft[i]);
		});
		slopes = slopes.avgsmooth(~slopesmooth);

			// identify where slope changes from + to -
			// a peak will be roughly around that index + (slopediff+slopesmooth / 2)
			// if i == 0, compareData will return inf no matter what the comparand
		slopes.doAdjacentPairs({ |a, b, i|
			((a >= 0) and: { b < 0 and: { i > 0 } }).if({
				peaks.add((index: i, magnitude: fft[i .. i+~slopedist+~slopesmooth].maxItem));
				peakIndices.add(i);
			});
		});
		
			// save data in the right place
		~slopes = slopes;
		~peaks = peaks.array;
		~peakIndices = peakIndices.array;
		currentEnvironment
	};
	
	~absRatio = { |a, b|
		(a.abs > b.abs).if({ a / b }, { b / a });
	};

		// a replacement for nearestInList that does not permit repeated matches
		// returns elements in b that are closest to elements in a
		// a.size <= b.size or the method will fail
	~nearInListExclusive = { |a, b|
		var	out, index;
		(a.size <= b.size).if({
			out = Array.new(a.size);
			b = b.copy;	// array will be changed
			a.do({ |item|
				(index = b.indexIn(item)).notNil.if({
					out.add(b[index]);
					b.removeAt(index);
				}, {
					Error("nearInListExclusive failed, no match for % in %."
						.format(item, b)).throw;
				});
			});
			out
		}, {
			Error("nearInListExclusive failed, a must be shorter than b.").throw;
		});
	};

	~compareData = { |comparisonfft|
		var	data1, data2, matchedIndices, matchedPeaks, out = 0;

			// which is shorter?
		(~peaks.size <= comparisonfft.peaks.size).if({
			data1 = currentEnvironment;
			data2 = comparisonfft;
		}, {
			data1 = comparisonfft;
			data2 = currentEnvironment;
		});

			// peak indices in data2 that are closest to the indices in data1
		matchedIndices = ~nearInListExclusive.(data1.peakIndices, data2.peakIndices);
			// peak objects at those indices
		matchedPeaks = data2.peaks[matchedIndices.collectIndicesFromArray(data2.peakIndices)];

			// now some numeric hackery
			// divide larger by smaller and take average of quotients
			// weight by quotient of peak magnitudes
		data1.peaks.do({ |p1, i|
			var	ind = ~absRatio.(p1.index, matchedPeaks[i].index),
				mag = ~absRatio.(p1.magnitude, matchedPeaks[i].magnitude);
			out = out + (ind * mag * (data1.peaks.size - i));
		});
		
			// if no peaks, this will be 0 / 0, replace with inf
			// just return, parent's job to save into the matrix
		(out = (out / matchedPeaks.size * ~absRatio.(data1.peaks.size, data2.peaks.size)))
			.isNaN.if(inf, out);
	};
	
		// remove most expensive memory consumers
		// preserve data (peaks, peakIndices) needed for comparison
		// usually you should collect garbage UNLESS you are debugging
	~collectGarbage = { |doGC|
		(doGC ? true).if({
			~fhzfft = nil;
			~srcfft = nil;
			~slopes = nil;
			~audio = nil;
			~keysFromParent.do({ |key|
				currentEnvironment.removeAt(key);
			});
		});
	};

}) => PR(\fftDataProto);

PR(\abstractProcess).v.clone({
	~event = (eventKey: \dummy);

		// can add stuff here
	~dataProto = \fftDataProto;

	~fftsmooth = 8;
	~slopedist = 3;
	~slopesmooth = 5;
	
	~minPeak = 0.1;
		// this is, checkMinPeak of each time point -- the peak of the whole buffer is always checked
	~checkMinPeak = false;
	
	~postProgress = false;	// post status updates?
	~garbageCollection = true;

		// getfft converts to mel frequencies for better analysis
	~freqToMel = { |fhz|
		1127.01048 * log(1 + (fhz/700))
	};
	
	~melToFreq = { |mel|
		exp(mel / 1127.01048) - 1 * 700
	};

	~fftwait = 0.001;
	~cmpwait = 0.0000001;
	~clock = AppClock;		// do not need musical precision
	~quant = NilTimeSpec.new;
	~reuseCleanup = true;
	
	~startAnalysis = { |path, pts, fftsize|
		~isPlaying.if({
			"BP(%) is still analyzing %. Cannot start a new file."
				.format(~collIndex.asCompileString, ~path);
		}, {
			~path = path;
			~pts = pts;
			~fftsize = fftsize ? 2048;
			BP(~collIndex).play(doReset: true);
		});
	};
	
	~asyncHang = { 
		~saveEventStreamPlayer = ~eventStreamPlayer;
		nil.yield;
	};
	
	~asyncUnhang = { 
		~eventStreamPlayer = ~saveEventStreamPlayer;
		~clock.sched(0, ~eventStreamPlayer.refresh);
		~isPlaying = true;
	};

		// SoundFile-based is synchronous but buffers might be async
		// getAudioData should send the messages to get the data, then call ~asyncHang
		// the following method is synchronous
	~getAudioData = { |time|
		var	result;
		~file.seek(time * ~sampleRate.value, 0);
		result = FloatArray.newClear(~fftsize);
		~file.readData(result);
		(result.size == ~fftsize).if({
			result
		}, {
			"Soundfile read failed, asked for % samples and got %."
				.format(~fftsize, result.size).warn;
			nil
		});
	};

	~initIndexPair = {
		~indexpair = Routine({
			(~pts.size - 1).do({ |i|
				(i+1 .. ~pts.size - 1).do({ |j|
					[i, j].yield;
				});
			});
		});
	};

	~makeWindow = {  Signal.hanningWindow(~fftsize) };

	~sampleRate = {  ~file.sampleRate };

	~preparePlay = { 
		var	resolution, melreso, mel, fhz, melNyquist;

		~path !? { ~file = SoundFile.openRead(~path) };

		~fftsize = nextPowerOfTwo(~fftsize);
		~halffft = ~fftsize div: 2;
		~imag = Signal.fill(~fftsize, 0);
		~cos = Signal.fftCosTable(~fftsize);
		
		resolution = ~sampleRate.value / ~fftsize;
		~melfreqsize = (~fftsize * 0.125).nextPowerOfTwo.asInteger;
		melreso = ~freqToMel.(~sampleRate.value * 0.5) / ~melfreqsize;
		
			// melmap[mel_bin_index] == first bin index into fft
		~melmap = Array.fill(~melfreqsize+1, { |i|
			fhz = ~melToFreq.(melreso * i);
			((fhz + (resolution * 0.5)) / resolution).asInteger
		});
		
		~window = ~makeWindow.value;
		
		~analysisComplete = false;
	};

	~initMatrix = { 
		~matrix = Array.fill(~pts.size, { Array.newClear(~pts.size) });
	};
	
	~addToMatrix = { |metric, refindex, cmpindex, matrix, ffts, pts|
		matrix = matrix ? ~matrix;
		ffts = ffts ? ~ffts;
		pts = pts ? ~pts;
		matrix[refindex][cmpindex] = (metric: metric, peak: ffts[cmpindex].peak,
			time: pts[cmpindex], refindex: refindex, index: cmpindex);
		matrix[cmpindex][refindex] = (metric: metric, peak: ffts[refindex].peak,
			time: pts[refindex], refindex: cmpindex, index: refindex);
	};		

		// by now matrix is mostly built, need to fill in the [i, i] diagonal
	~buildMatrix = { 
		~matrix.size.do({ |i|
			~matrix[i][i] = (metric: 1, index: i, refindex: i, peak: ~ffts[i].peak,
				time: ~pts[i]);
		});
		~matrix
	};

	~asPattern = { 
			// normally this should be a pattern but routines made from patterns
			// can't be hung and unhung for async use
			// Routine().asStream === Routine() so it should be OK
		Routine({
			var	audio, srcfft, pair, metric, failed = 0, lastPair0;

			~postProgress.if({
				"BP(%): Calculating ffts and peaks\n".postf(~collIndex.asCompileString);
			});
			~ffts = Array.new(~pts.size);
			~pts = ~pts.copy;
			~pts.copy.do({ |time, i|
				(play: 0, delta: ~fftwait).yield;
					// ~buffer is nil in the file version, but won't be in the buffer version
				audio = ~getAudioData.(time, ~buffer);
				audio.notNil.if({
					srcfft = PR(~dataProto).v.copy.prep(audio, i-failed, currentEnvironment);
						// nil indicates peak check for the clip failed
					srcfft.getfft.notNil.if({
						~ffts.add(srcfft.fftPostProcessing);
						srcfft.collectGarbage(~garbageCollection);
					}, {
						~postProgress.if({
							"BP(%): Timepoint % is not valid.\n"
								.postf(~collIndex.asCompileString, time)
						});
						~pts.removeAt(i - failed);
						failed = failed + 1;
					});
				}, {
					~postProgress.if({
						"BP(%): Timepoint % is not valid.\n"
							.postf(~collIndex.asCompileString, time)
					});
					~pts.removeAt(i - failed);
					failed = failed + 1;
				});
			});

			~postProgress.if({
				"BP(%): % onsets did not meet threshold.\n".postf(~collIndex.asCompileString,
					failed);
			});

			~postProgress.if({
				"BP(%): Comparing ffts\n".postf(~collIndex.asCompileString);
			});
			~initIndexPair.value;
			~initMatrix.value;
			{ (pair = ~indexpair.next).notNil }.while({
					// this func must store comparison in matrix
				(~postProgress and: { pair[0] != lastPair0 }).if({
					"BP(%): Comparing index %, % comparisons.\n".postf(
						~collIndex.asCompileString, pair[0], ~ffts.size - pair[0] - 1);
					lastPair0 = pair[0];
				});
				metric = ~ffts[pair[0]].compareData(~ffts[pair[1]]);
				~addToMatrix.(metric, pair[0], pair[1]);
				(play: 0, delta: ~cmpwait).yield;
			});

			~buildMatrix.value;
			~garbageCollection.value;	// since these run in a routine, they may .wait
			~analysisComplete = true;
				// pattern will stop, control passes to ~stopCleanup
		})
	};

	~userStop = { 
		~file.close;
	};

		// if this is called when analysisComplete is false, the thread is hanging,
		// waiting for async reply
	~stopCleanup = { |streamStopped|
		streamStopped.if({
			(~analysisComplete ? false).if({
				"Analysis complete. Executing action.".postln;
				~userStop.value;
				~clearAsyncResponder.value;
				~action.(~path, ~pts, ~matrix);
			});
		}, {
			"Analysis was aborted. Call .play to resume.".warn;
		});
	};
	
	~freeCleanup = { 
		~clearAsyncResponder.value;
		~userFree.value;
	};
	
		// be careful -- this is NOT threaded and will block other threads until complete
		// it's very fast, though, can write over 180,000 numbers in 1-2 seconds
		// (on a MacBook Pro :)
		// if matrix is size n, first n rows contain the metrics sequentially
		// 2 additional rows for time points and peak amplitudes

		// to restore, do:
		//		(PR(\transient_analysis_file) => BP(\reader)).v.readMatrixFromCSV(path);

	~saveMatrixToCSV = { |path|
		var	file = File(path = path.standardizePath, "w");
		file.isOpen.if({
			~pts.do({ |pt, i|
				file.write(pt.asCompileString);
				(i < (~matrix.size-1)).if({ file.write($,); });
			});
			file.write($\n);
			~matrix[0].do({ |col, i|
				file.write(col.peak.asCompileString);
				(i < (~matrix.size-1)).if({ file.write($,); });
			});
			file.write($\n);
			~matrix.do({ |row|
				row.do({ |col, i|
					file.write(col.metric.asCompileString);
					(i < (row.size-1)).if({ file.write($,); });
				});
				file.write($\n);
			});
			file.close;
		}, {
			Error("saveMatrixToCSV: Could not open % for writing.".format(path)).throw;
		});
	};

	~readMatrixFromCSV = { |path|
		var	raw = CSVFileReader.read(path.standardizePath, true, true, _.asFloat),
			peaks, pts;
		raw.notNil.if({
			~pts = raw[0];
			peaks = raw[1];
			~matrix = { Array.newClear(raw.size-2) } ! (raw.size-2);
			(2..raw.size-1).do({ |i, row|
				raw[i].do({ |col, j|
					~matrix[row][j] = (refindex: row, index: j, metric: col, peak: peaks[j],
						time: ~pts[j]);
				});
			});
			~matrix
		});
	};
	
	~readMatrixFromCSVOldFormat = { |path|
		var	raw = CSVFileReader.read(path.standardizePath, true, true, _.asFloat),
			peaks, pts;
		raw.notNil.if({
			peaks = raw.removeAt(raw.size-1);
			~pts = raw.removeAt(raw.size-1);
			~matrix = { Array.newClear(raw.size) } ! (raw.size);
			raw.do({ |row, i|
				row.do({ |col, j|
					~matrix[i][j] = (refindex: i, index: j, metric: col, peak: peaks[j],
						time: ~pts[j]);
				});
			});
		});
	};
	
	~readRowFromCSV = { |file|
		var	out = List.new,
			char, string = String.new;

		{	char = file.getChar;
			char.notNil and: { "\n\r".includes(char).not }
		}.while({
			case { char == $, } {
				(string.size > 0).if({
					out.add(string.asFloat);
					string = String.new;
				});
			}
			{ string = string.add(char) };
		});
		(string.size > 0).if({
			out.add(string.asFloat);
		});
		out.asArray
	};
	
	~asyncReadFromCSV = { |path, action|
		var file, raw, peaks, size;
		path = path.standardizePath;
		(file = File.new(path, "r")).isOpen.if({
			AppClock.sched(0, Routine({
				protect {
					~pts = ~readRowFromCSV.(file);
					0.01.yield;
					peaks = ~readRowFromCSV.(file);
					size = ~pts.size;
					~matrix = Array.new(size);
					size.do({ |i|
						0.01.yield;
						raw = ~readRowFromCSV.(file);
						~matrix.add(
							raw.collect({ |col, j|
								(refindex: i, index: j, metric: col, peak: peaks[j],
									time: ~pts[j])
							})
						);
					});
					action.value(~matrix);
				} {
					file.close;
				};
			}))
		}, {
			"% could not be opened.".format(path).error
		});
	};
}) => PR(\transient_analysis_file);

PR(\transient_analysis_file).v.clone({
	~sampleRate = {  ~buffer.sampleRate };

	~startAnalysis = { |buffer, pts, fftsize|
		~isPlaying.if({
			"BP(%) is still analyzing %. Cannot start a new file."
				.format(~collIndex.asCompileString, ~buffer);
		}, {
			~buffer = buffer;
			~pts = pts;
			~fftsize = fftsize ? 2048;
			BP(~collIndex).play(doReset: true);
		});
	};

		// asynchronous version to read from a buffer
		// note, if getToFloatArray fails, the thread will stop with error
	~getAudioData = { |time, buffer|
		var	result, startFrame = (time * buffer.sampleRate).asInteger;
		(startFrame < (buffer.numFrames - ~fftsize)).if({
			buffer.getToFloatArray(startFrame, ~fftsize, action: e { |v|
				result = v;
				~asyncUnhang.value;	// getToFloatArray action is not environment-safe
			});
			~asyncHang.value;
			(result.size == ~fftsize).if({
				result
			}, {
				"Buffer read failed, asked for % samples and got %."
					.format(~fftsize, result.size).warn;
				nil
			});
		});
	};

	~userStop = 0;
}) => PR(\transient_analysis_buffer);

// an incremental version for live analysis
// simultaneously receives audio from file or mic, records to a buffer,
// runs a feature detector to get triggers, and queues fft and comparison analyses
// in theory, after recording stops, the matrix should be ready within moments
// used a lot of code from my track "Got an itch to scratch" for multi-buffer support
// "clients" for these buffers can register as dependents of the BP
// and get status updates on the bufs

PR(\transient_analysis_buffer).v.clone({
	~errorRecovery = true;		// if an error occurs during comparisons, find and fix afterward
	~iMadeMixer = false;
	~numBufs = 5;
	~bufDur = 10;
	~recording = false;
	~quant = 1;
	~onsetRejectLimit = 0.15;		// minimum time between onsets
	~minPeak = 0.175;			// reject a recording if its peak <= this
	~recordActive = true;
	~audioThru = false;		// allow audio from another bus when not recording
							// (always plays thru during recording)
	~audioThruLevel = 1;		// level of audio allowed through (0 will suppress)
	~mixerOutChannels = 2;
	
	~midiTriggerChan = \omni;
	~midiTriggerCtlNum = 64;

	~bufStateProto = (status: \idle, dur: nil, peak: nil, recTime: 0, playCount: 0,
		actionDone: false)
		.parent_(
			(status_: { |thisBufState, status|
				thisBufState[\status] = status;
				thisBufState.changed(\status, status);  // dependents should know when status changes
			})
		);

//////// INITIALIZATION ////////
	
	~sampleRate = {  ~bufs[0].buf.sampleRate };

	~prep = { 
		~chan.isNil.if({
			~chan = MixerChannel(~collIndex, s, 1, ~mixerOutChannels, postSendReady: true,
				outbus: ~master);
			~iMadeMixer = true;
		});

		~bufs = { ~bufStateProto.copy
			.put(\buf, Buffer.alloc(s, (~bufDur ? 10) * s.sampleRate, 1))
			.put(\status, \idle) } ! ~numBufs;

			// LL tail is last item added to queue
			// items are processed in the order added by LL.popFirst
		~fftqueue = LinkedList.new;
		~cmpqueue = LinkedList.new;
		
		~fftbuf = Buffer.alloc(s, 256, 1);

		~watcher = NodeWatcher.newFrom(s);

		~resp = OSCresponderNode(s.addr, '/tr', e { |t, r, m|
			var	buf;
				// ignore if I don't know about the node
			buf = ~bufs.detect({ |buf| m[1] == buf.node.tryPerform(\nodeID) });
			buf.notNil.if({
				switch(m[2])	// switch is based on SendTrig id arg
					{ -1 } {
						buf.status = \record;
						buf.dur = nil;	// set flags so we know when all messages
						buf.peak = nil;	// have come in
						buf.ontimes = List.new;
						~doOnRecord.(buf);
					}
					{ 0 } {
						buf.status = \stopRec;
						buf.peak = m[3].max(0.01);
						buf.dur.notNil.if({ ~closeRecording.(buf) });
					}
					{ 1 } {
						buf.status = \stopRec;
						buf.dur = m[3];
						buf.recTime = Main.elapsedTime;
						buf.peak.notNil.if({ ~closeRecording.(buf) });
					}
			}, {
				buf = ~bufs.detect({ |buf| m[1] == buf.detectnode.tryPerform(\nodeID) });
				(buf.notNil and: { m[2] == 32 and: { buf.status == \record
						and: { m[3] > 0 } } }).if({
					buf.ontimes.add(m[3]);
					~queueOnsetTime.(buf, m[3], buf.ontimes.size-1);
				});
			});
		}).add;

		~featuredef = ~makeFeatureDetector.value.send(s);
		~inputdef = ~makeInputSource.value.send(s);

		~userprep.value;

		~initFFT.value;

		~triggerdevice = ~makeTrigger.value;
		~startRecord.(~recordActive);
		~lastTrigTime = 0;
	};

	~makeTrigger = { 
			// asClass is used here because you might not have installed the ddwMIDI quark
			// rather than make chucklib depend on the whole MIDI framework,
			// I will use this technique to avoid an error on recompile
		'BasicMIDIControl'.asClass.new(~midiTriggerChan, ~midiTriggerCtlNum, e { |value|
				// seems we need to trap very rapid triggers
			(value > 0 and: { (Main.elapsedTime - ~lastTrigTime) > 1.0 }).if({
				~sendTrigger.value;
			});
		});
	};
	
	~sendTrigger = { 
		s.makeBundle(nil, {
			~recNode !? { ~recNode.set(\t_trig, 1) };
			~detectNode !? { ~detectNode.set(\t_trig, 1) };
		});
		~lastTrigTime = Main.elapsedTime;
	};

	~makeFeatureDetector = {
		SynthDef(\pv_jensen, { |outbus, fftbuf, bufnum, t_trig, faststop = 0,
			propsc = 0.75581395348837, prophfe = 0.43023255813953, prophfc = 0.63953488372093,
				propsf = 0.54651162790698, threshold = 0.058139534883721,
				waittime = 0.15116279069767,
				i_fftwait = 0.05|  // can't do fft analysis until the whole frame is recorded

			var	pc = PulseCount.kr(t_trig),
				start = BinaryOpUGen.new1(\control, '==', pc, 1),
				stop = BinaryOpUGen.new1(\control, '==', pc, 2),
				started = (pc > 0),
				sig = In.ar(outbus, 1),
				fft = FFT(fftbuf, sig),
				dur = Phasor.ar(start, SampleDur.ir, 0, 1000),
				onsettrig;

				// event onset
			onsettrig = PV_JensenAndersen.ar(fft, propsc, prophfe, prophfc, propsf,
				threshold, waittime);
					// delayed to allow entire client fft frame to be recorded before analysis
					// BufDur.kr(fftbuf) * 0.5 is a correction for PV_Jensen's trigger lag
			SendTrig.kr(DelayN.kr(onsettrig, i_fftwait, i_fftwait), 32,
				dur - i_fftwait - (BufDur.kr(fftbuf) * 0.5));

			stop = stop + (started * A2K.kr(dur > BufDur.ir(bufnum))) + faststop;
			FreeSelf.kr(stop);
		});
	};
	
	~detectParms = { |buf| 
		[bufnum: buf.buf.bufnum, fftbuf: ~fftbuf.bufnum, 
			i_fftwait: ~fftsize / ~sampleRate.value, waittime: ~onsetRejectLimit]
	};
	
	~makeInputSource = {
		SynthDef(\trigrecfft, { |bufnum, t_trig, inbus, outbus,
				cmpgain = 1, cmpclamp = 0.02, cmprelax = 0.1, cmpthresh = 0.4,
				cmpratio = 1.0, cmpgateratio = 1.0, hardgate = 0.01,
				audiothru = 0, faststop = 0|
			var	pc = PulseCount.kr(t_trig),
				start = BinaryOpUGen.new1(\control, '==', pc, 1),
				stop = BinaryOpUGen.new1(\control, '==', pc, 2),
				started = (pc > 0),
				sig = In.ar(inbus, 1),
				dlysig,
				peak = Peak.ar(sig * started),
				dur;
			
			sig = sig * (Amplitude.kr(sig, cmpclamp, cmprelax) >= hardgate);
				// expand out CompanderD so I can control max delay time
			dlysig = DelayN.ar(sig, 0.15, cmpclamp);
			sig = Compander.ar(dlysig, sig, cmpthresh, cmpgateratio, cmpratio, cmpclamp, cmprelax,
				cmpgain);
			
				// trigger should ensure recording waits until trigger rec'd
			RecordBuf.ar(sig, bufnum, 0, 1, 0, started - stop, 0, 0);
		
			dur = Phasor.ar(start, SampleDur.ir, 0, 1000);
			stop = stop + (started * A2K.kr(dur > BufDur.ir(bufnum))) + faststop;
			
			SendTrig.kr(start, -1, 1);		// "started recording" message
			SendTrig.kr(stop, 0, peak);		// "stopped recording"
			SendTrig.kr(stop, 1, dur);		// "how long recording?"

				// delay the stop trigger in the EnvGen to avoid glitch
			Out.ar(outbus, sig
				* EnvGen.kr(Env.asr(0.05, 1, 0.05),
					DelayN.kr((started + audiothru) * (stop <= 0), cmpclamp, cmpclamp),
					doneAction:2)
			);
		});

	};
	
	~inputParms = [];

		// call from outside when a new node is using one of my buffers
		// watch for the node to stop and allow the buf to be reused when no one is using it
		// if node is nil, it's the user's responsibility to release the buffer (next method)
	~bufferPlayingNewNode = { |buffer, node|
		buffer.status = \play;
		buffer.playCount = buffer.playCount + 1;
		node.notNil.if({
			~watcher.register(node);
			Updater(node, e { |node, msg|
				(msg == \n_end).if({
					node.releaseDependants;
					~releaseBufferFromPlay.(buffer);
				});
			});
		});
	};

		// should be called from outside to release a buffer from play
	~releaseBufferFromPlay = { |buffer|
		buffer.playCount = buffer.playCount - 1;
		(buffer.playCount == 0).if({
			buffer.status = \ready;
			~startRecord.(~recordActive);	// if already recording, this will not fire
		});
	};
	
//////// RECORDING CONTROL ////////

	~inputBusIndex = s.options.numOutputBusChannels;

	~startRecord = { |recActive|
		var	buf = ~oldestBuffer.(#[idle, ready]),  // ready means not playing
			bundle;
		recActive = recActive ? true;	// if nil, start every time (if user calls directly)
		(recActive and: { ~recording.not and: { buf.notNil } }).if({
			buf.status = \recordPending;
			buf.actionDone = false;
			buf.ontimes = List.new;
			buf.ffts = List.new;
			buf.matrix = Array.new;
			bundle = s.makeBundle(false, {
				buf.node = ~chan.play(~inputdef.name, [\bufnum, buf.buf.bufnum,
					\inbus, ~inputBusIndex, \t_trig, 0, \audiothru, ~audioThru.binaryValue]
					++ ~inputParms.(buf));
				buf.detectnode = ~chan.playfx(~featuredef.name, [t_trig: 0]
					++ ~detectParms.(buf));
			});
			~recNode = buf.node;
			~detectNode = buf.detectnode;
			~chan.doWhenReady({
				s.listSendBundle(nil, bundle);
			});
			~recording = true;
			~recordActive = true;
			~currentBuf = buf;
			"\n\n\nRecording is paused.".postln;
			BP(~collIndex).changed(\bufRecord, buf);
		});
	};

	~oldestBuffer = { |status|
		~bufs.sort({ |a, b| a.recTime < b.recTime })
			.detect({ |buf| status.matchItem(buf.status) })
	};
	
	~closeRecording = { |buf, startRec|
			// update gui status if I'm in an MT
		~isPlaying = false;
		BP(~collIndex).changed(\stop);

			// if nothing was recorded on stopRecord, buf.peak is nil so guarantee failure with -1
		((buf.peak ? -1) >= ~minPeak).if({
			buf.status = switch(buf.status)
				{ \recordPending } { \idle }
				{ \record } { \ready }
				{ \stopRec } { \ready }
				{ \idle };
		}, {
			"Recording rejected, amplitude (%) didn't meet threshold (%)."
				.format(buf.peak, ~minPeak)
				.warn;
			buf.status = \idle;
			buf.recTime = -1;	// force next record to use this buffer
		});
		buf.node = nil;

			// kindly release the node -- buf.node is already nil so oscresp won't match
		(startRec ? true).not.if({
			~recNode.set(\faststop, 1);
		});
		~recNode = nil;
		~detectNode.free;	// detectNode should not be outputting audio, can kill brutally
		~detectNode = nil;
		~recording = false;
		~lastTrigTime = Main.elapsedTime;  // to avoid accidental record restart
		(startRec ? true).if({
			~startRecord.(~recordActive);
		});
		~cmpqueue.isEmpty.if({
			~doAction.(buf);
		});
	};

	~doOnRecord = { |buf|
		"\n\n\n>>>>> RECORDING IS ACTIVE. <<<<<".postln;
			// update gui status if I'm in an MT
		~isPlaying = true;
		BP(~collIndex).changed(\play);
	};
	
		// dummy action, replace to call another BP to use the buffer
	~doAction = { |buf|
		(buf.actionDone.not and: { buf.peak >= ~minPeak }).if({
			"Analysis complete. Last buffer stored in BP(%).v.lastBuf.\n"
				.postf(~collIndex.asCompileString);
			~lastBuf = buf;
			buf.actionDone = true;
			BP(~collIndex).changed(\bufReady, buf);
		});
		currentEnvironment
	};

//////// FFT ANALYSIS CONTROL ////////

	~fftThreadActive = false;
	~fftThreadWaiting = false;
	~cmpThreadActive = false;

	~queueOnsetTime = { |buf, time|
		~fftqueue.add((buf: buf, time: time, index: buf.ffts.size));
		~activateFFTThread.value;
	};
	
	~activateFFTThread = { 
		~fftThread.isNil.if({ ~makeFFTThread.value });
		~fftThreadActive.not.if({
			AppClock.sched(0, ~fftThread);
			~fftThreadActive = true;
		});
	};
	
	~makeFFTThread = { 
		~fftThread = HJHCleanupStream(
			Routine({
				var	audio, srcfft, spec;
				loop {
					spec = ~fftqueue.popFirst;
					spec.notNil.if({
							// spec.buf.ffts.size == index of next fft to add
							// spec has an index but if a previous fft failed, it may be wrong
						spec.index = spec.buf.ffts.size;
						audio = ~getAudioData.(spec.time, spec.buf.buf);
						audio.notNil.if({
							srcfft = PR(~dataProto).v.copy
								.prep(audio, spec.index, currentEnvironment);
							srcfft.getfft.notNil.if({
								~postProgress.if({
									"BP(%): got valid time % at index %\n".postf(
										~collIndex.asCompileString, spec.time, spec.index);
								});
								~ffts.add(srcfft.fftPostProcessing);
								srcfft.collectGarbage(~garbageCollection);
								spec.buf.ffts.add(srcfft);
								~queueComparisons.(spec);
							}, {
								spec.buf.ontimes.removeAt(spec.index);
							});
						}, {
							spec.buf.ontimes.removeAt(spec.index);
						});
						~fftwait.wait;
					}, {
						nil.yield;
					});
				}
			}),
			e { ~fftThreadWaiting.not.if({ ~fftThreadActive = false }); },
			true);
	};
	
	~queueComparisons = { |fftspec|
		var	matrix = fftspec.buf.matrix, index = fftspec.index;
			// make sure matrix is large enough to handle the comparisons
		(matrix.size < (index+1)).if({
			matrix = matrix.extend(index+1, Array.new(index+1));
			matrix.do({ |row, i|
				(row.size < (index+1)).if({ matrix[i] = row.extend(index+1); });
			});
		});
			// no comparison needed for diagonal
		matrix[index][index] = (metric: 1, refindex: index, index: index,
			peak: fftspec.buf.ffts[index].peak, time: fftspec.time);
		fftspec.buf.matrix = matrix;

		(index > 0).if({
			index.do({ |i|
				~cmpqueue.add((fftspec.copy.put(\cmpindex, i)));
			});
			~activateCmpThread.value;
		});
	};

	~activateCmpThread = { 
		~cmpThread.isNil.if({ ~makeCmpThread.value });
		~cmpThreadActive.not.if({
			AppClock.sched(0, ~cmpThread);
			~cmpThreadActive = true;
		});
	};
	
	~makeCmpThread = { 
		~cmpThread = HJHCleanupStream(
			Routine({
				var	spec, lastspec, errors = 0, done = 0, recoveredFrom;
				loop {
					spec = ~cmpqueue.popFirst;
					spec.notNil.if({
						lastspec = spec;
						try {
							~doComparison.(spec);
						} { |error|
							"\n\n\nERROR IN COMPARISON\n\n".postln;
							error.reportError;
							spec.postln;
							errors = errors + 1;
							"\n\nComparison error noted. % error% so far.\n"
								.postf(errors, (errors != 1).if($s, ""));
						};
						~cmpwait.wait;
					}, {
						(~errorRecovery and: { errors > 0 }).if({
							~postProgress.if({
								"BP(%): Recovering from % error%.\n"
									.postf(~collIndex.asCompileString, errors,
										(errors != 1).if($s, ""));
							});
							recoveredFrom = ~recoverErrors.(lastspec.buf);
							errors = errors - recoveredFrom;
						});
						(errors > 0).if({
							"BP(%): Did not recover from % error%.\n"
								.postf(~collIndex.asCompileString, errors,
									(errors != 1).if($s, ""));
						});
							// if recording is over and cmpqueue is empty,
							// pass to the play action
						(lastspec.buf.status == \ready).if({
							~doAction.(lastspec.buf);
						});
						nil.yield;
					});
				}
			}),
			e { ~cmpThreadActive = false; },
			true);
	};
	
	~recoveryTests = 100;	// number of tests to perform in one wake of the recovery thread
	
		// PRIVATE METHOD -- must be called only in the context of the comparison thread
		// can't think of a better way than brute force -- but, the nil check is very fast
	~recoverErrors = { |buf|
		var	success = 0, matrix, matrixsize, testsdone = 0, ok;
		(matrix = buf.tryPerform(\at, \matrix)).notNil.if({
			matrixsize = matrix.size;	// matrix may be extended while this is running
			(matrixsize - 1).do({ |row|
				for(row + 1, matrixsize - 1, { |col|
					matrix[row][col].tryPerform(\at, \metric).isNil.if({
						try {
							~doComparison.((buf: buf, index: row, cmpindex: col));
							success = success + 1;
						};	// if comparison fails again, second error is swallowed
						testsdone = 0;	// reset and yield
						~cmpwait.wait;
					}, {
						((testsdone = testsdone + 1) == ~recoveryTests).if({
							testsdone = 0;
							~cmpwait.wait;
						});
					});
				});
			});
		});
		success	// return value
	};

		// note, this should be called only within the fftThread
	~asyncHang = { 
		~saveFFTThread = ~fftThread;
		~fftThreadWaiting = true;
		nil.yield;
	};
	
	~asyncUnhang = { 
		~fftThread = ~saveFFTThread;
		~clock.sched(0, ~fftThread);
		~fftThreadWaiting = false;
	};

//////// FFT ANALYSIS ////////

	~initFFT = { 
		var	resolution, melreso, mel, fhz, melNyquist;

		~fftsize = nextPowerOfTwo(~fftsize ? 2048);
		~halffft = ~fftsize div: 2;
		~imag = Signal.fill(~fftsize, 0);
		~cos = Signal.fftCosTable(~fftsize);
		
		resolution = ~sampleRate.value / ~fftsize;
		~melfreqsize = (~fftsize * 0.125).nextPowerOfTwo.asInteger;
		melreso = ~freqToMel.(~sampleRate.value * 0.5) / ~melfreqsize;
		
			// melmap[mel_bin_index] == first bin index into fft
		~melmap = Array.fill(~melfreqsize+1, { |i|
			fhz = ~melToFreq.(melreso * i);
			((fhz + (resolution * 0.5)) / resolution).asInteger
		});
		
		~window = ~makeWindow.value;
	};

	~doComparison = { |cmpspec|
		var	metric;
		metric = cmpspec.buf.ffts[cmpspec.index]
			.compareData(cmpspec.buf.ffts[cmpspec.cmpindex]);
		~addToMatrix.(metric, cmpspec.index, cmpspec.cmpindex, cmpspec.buf.matrix,
			cmpspec.buf.ffts, cmpspec.buf.ontimes);
	};

//////// DESTRUCTOR ////////

	~freeCleanup = { 
		~clearAsyncResponder.value;
		~iMadeMixer.if({ ~chan.free });
		~userFree.value;
		~bufs.do({ |b| b.buf.free });
		[~fftbuf, ~triggerdevice].free;
		~resp.remove;
	};
}) => PR(\transient_analysis_incr);


AbstractChuckArray.defaultSubType = subtype;
