
XiiSynthDefs {	

	var s;
	
	*new { arg server;
		^super.new.initXiiSynthDefs(server);
		}
		
	initXiiSynthDefs { arg server;
		s = server;
		
		// --- the SoundScratcher --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		// the lag time is good around 4 seconds
		SynthDef.writeOnce(\xiiScratch1x2, {arg outbus=0, bufnum, pos=0, vol=0, gate=1;
			var signal, env;
			env = EnvGen.kr(Env.adsr(0.2, 0.2, 1, 0.21, 1, -4), gate, doneAction:2);
			signal = BufRd.ar(1, bufnum, Lag.ar(K2A.ar(pos), 4), 1) * env * vol;
			Out.ar(outbus, signal!2);
		});
		
		/*
		// old synth for warp - TGrains proved to sound better
		SynthDef(\xiiWarp, {arg outbus=0, bufnum = 0, freq=1, pointer=0, vol=0, gate=1;
			var signal, env;
			env = EnvGen.kr(Env.adsr(0.2, 0.2, 1, 0.21, 1, -4), gate, doneAction:2);
			signal = Warp1.ar(1, bufnum, pointer, freq, 0.09, -1, 8, 0.2, 2) * env * vol;
			Out.ar(outbus, signal!2);
		}).load(s);
		*/

		SynthDef(\xiiWarp, {arg outbus=0, bufnum=0, trate=20, freq=1, pointer=0, vol=0, gate=1, dur=0.1;
			var signal, env;
			env = EnvGen.kr(Env.adsr(0.2, 0.2, 1, 0.21, 1, -4), gate, doneAction:2);
			signal = TGrains.ar(2, Impulse.ar(trate), bufnum, freq, pointer, dur, 0) * env * vol;
			Out.ar(outbus, signal!2);
		}).load(s);


		
		SynthDef.writeOnce(\xiiGrain, {arg outbus=0, bufnum, rate=1, pos=0, dur=0.05, vol=1, envType=0;
			var signal, env, sineenv, percenv;
			sineenv = EnvGen.kr(Env.sine(dur, vol), doneAction:2);
			percenv = EnvGen.kr(Env.perc(0.001, dur*2, vol), doneAction:2);
			env = Select.kr(envType, [sineenv, percenv]);
			signal = PlayBuf.ar(1, bufnum, rate, 1.0, pos) * env ;
			Out.ar(outbus, Pan2.ar(signal, Rand(-0.75, 0.75)));
		});
		
		/*
		// One day I'll use Josh's BufGrain
		SynthDef(\xiiGrain, {arg outbus=0, bufnum, t_trig=0, rate=1, pos=0, dur=0.05, vol=1, envType=0;
			var signal, env, sineenv, percenv;
			sineenv = EnvGen.kr(Env.sine(dur, vol));
			percenv = EnvGen.kr(Env.perc(0.001, dur*2, vol));
			env = Select.kr(envType, [sineenv, percenv]);
			signal = BufGrain.ar(t_trig, dur, bufnum, rate, pos) * env ;
			Out.ar(outbus, Pan2.ar(signal, Rand(-0.75, 0.75)));
		}).load(s);
		*/
		
		SynthDef.writeOnce(\xiiGrains, {arg 	outbus=0, bufnum, dur=0.3, trate=10, ratelow= 0.5, 
								ratehigh=1.5, left=0.3, right=0.4, vol=0.2, globalvol=1;
			var clk, pos, pan, rate;
			clk = Impulse.kr(trate);
			pos = TRand.kr(left, right, clk);
			rate = TRand.kr(ratelow, ratehigh, clk);
			pan = WhiteNoise.kr(0.6);
			Out.ar(outbus, TGrains.ar(2, clk, bufnum, rate, pos, dur, pan, vol)*globalvol);
		});
		
		SynthDef.writeOnce(\xiiGrainsSQ, {arg outbus=0, bufnum, dur=0.3, trate=10, rate, left=0.3, vol=0.2, globalvol=1;
			var clk, pos, pan;
			clk = Impulse.kr(trate);
			pan = WhiteNoise.kr(0.6);
			Out.ar(outbus, TGrains.ar(2, clk, bufnum, rate, left, dur, pan, vol) * globalvol);
		});
		

		// --- the XiiPlayer --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! DiskIn
		
		
		SynthDef.writeOnce(\xiiPlayer1, { arg outbus = 0, bufnum = 0, vol=0;
			Out.ar(outbus, (DiskIn.ar(1, bufnum) * vol).dup);
		});

		SynthDef.writeOnce(\xiiPlayer2, { arg outbus = 0, bufnum = 0, vol=0;
			Out.ar(outbus, DiskIn.ar(2, bufnum) * vol);
		});


		// --- the AudioIn tool  --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! DiskIn
	
		SynthDef.writeOnce(\xiiAudioIn, { arg out=0, volL, volR, panL, panR;
			var updateRate=40, ampl, ampr, left, right;
			left =  AudioIn.ar(1) * volL;
			right = AudioIn.ar(2) * volR;
			ampl = Amplitude.kr(left);
			ampr = Amplitude.kr(right);
			SendTrig.kr(Impulse.kr(updateRate), 800, ampl);
			SendTrig.kr(Impulse.kr(updateRate), 801, ampr);
			Out.ar(out, Pan2.ar(left, panL));
			Out.ar(out, Pan2.ar(right, panR));
		});


		// --- the bufferplayer --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		SynthDef.writeOnce(\xiiBufPlayerSTEREO, {arg trigID = 10, out=0, bufnum=0, vol=0.0, pan = 1, trig=0, pitch=1.0, startPos=0, endPos= 1000;
			var updateRate=40, playbuf, ampl, ampr, signal;
			playbuf = LoopBuf.ar(2, bufnum, pitch, trig, startPos, startPos, endPos) * vol;
			signal = Balance2.ar(playbuf[0], playbuf[1], pan);
			ampl = Amplitude.kr(signal.at(0));  
			ampr = Amplitude.kr(signal.at(1));
			SendTrig.kr(Impulse.kr(updateRate), trigID, ampl);
			SendTrig.kr(Impulse.kr(updateRate), trigID+1, ampr);
			Out.ar(out, signal);
		});
		
		SynthDef.writeOnce(\xiiBufPlayerMONO, {arg trigID = 10, out=0, bufnum=0, vol=0.0, pan = 0, trig=0, pitch=1.0, startPos=0, endPos= 1000;
			var updateRate=40, playbuf, ampl;
			playbuf = LoopBuf.ar(1, bufnum, pitch, trig, startPos, startPos, endPos) * vol;
			ampl = Amplitude.kr(playbuf);  
			SendTrig.kr(Impulse.kr(updateRate), trigID, ampl);
			SendTrig.kr(Impulse.kr(updateRate), trigID+1, ampl);
			Out.ar(out, Pan2.ar(playbuf, pan));
		});


		// --- the polymachine --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		SynthDef.writeOnce(\xiiPolyrhythm1x2, {arg outbus=0, bufnum=0, vol=1, pan = 1, 
								trig=0, pitch=1, startPos=0, endPos= -1;
			var playbuf, env;
			env = EnvGen.ar(Env.linen(0.00001, (endPos-startPos)/44100, 0.0001), doneAction:2);
			playbuf = LoopBuf.ar(1, bufnum, pitch, trig, startPos, startPos, endPos) * vol * env;
			Out.ar(outbus, playbuf!2);
		});
		
		SynthDef.writeOnce(\xiiPolyrhythm2x2, {arg outbus=0, bufnum=0, vol=1, pan = 1, 
								trig=0, pitch=1, startPos=0, endPos= -1;
			var playbuf, env;
			env = EnvGen.ar(Env.linen(0.00001, (endPos-startPos)/44100, 0.0001), doneAction:2);
			playbuf = LoopBuf.ar(2, bufnum, pitch, trig, startPos, startPos, endPos) * vol * env;
			Out.ar(outbus, playbuf);
		});
		
		SynthDef.writeOnce(\xiiPolyrhythm1x2Env, {arg outbus=0, bufnum=0, vol=1, pan = 1, trig=0, pitch=1, 
				startPos=0, endPos= -1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.1, 0.5, 1.0];
			var playbuf, env, killenv;
			env = EnvGen.kr(Env.new(levels, times)); // killenv kills because there is no loop:0 in the new LoopBuf
			killenv = EnvGen.kr(Env.linen(0.00001, (endPos-startPos)/44100, 0.0001), doneAction:2);
			//playbuf = LoopBuf.ar(1, bufnum, pitch, trig, startPos, endPos, loop:0) * vol * env; // old loopbuf
			playbuf = LoopBuf.ar(1, bufnum, pitch, trig, startPos, startPos, endPos) * vol * env * killenv;
			Out.ar(outbus, playbuf!2);
		});

		SynthDef.writeOnce(\xiiPolyrhythm2x2Env, {arg outbus=0, bufnum=0, vol=1, pan = 1, trig=0, pitch=1, 
				startPos=0, endPos= -1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.1, 0.5, 1.0];
			var playbuf, env, killenv;
			env = EnvGen.kr(Env.new(levels, times)); // killenv kills because there is no loop:0 in the new LoopBuf
			killenv = EnvGen.kr(Env.linen(0.00001, (endPos-startPos)/44100, 0.0001), doneAction:2);
			playbuf = LoopBuf.ar(2, bufnum, pitch, trig, startPos, startPos, endPos) * vol * env * killenv; 
			//playbuf = LoopBuf.ar(2, bufnum, pitch, trig, startPos, endPos, loop:0) * vol * env;/ / old loopbuf
			Out.ar(outbus, playbuf);
		});

		
		SynthDef.writeOnce(\xiiPolyrhythmAudioStream2x2Env, {arg inbus=20, outbus=0, vol=1, 
				levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.07, 0.1, 0.062];
			var in, env, killenv;
			env = EnvGen.kr(Env.new(levels, times), doneAction:2);
			in = InFeedback.ar(inbus, 2) * vol * env; 
			Out.ar(outbus, in);
		});

		// --- the grainbox --- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		
		SynthDef.writeOnce(\xiiGranny, {arg out=0, trigRate=1, freq=1, centerPos=0.5, dur=0.05, 
						pan=0.2, amp = 0.4, buffer=0, cntrPosRandWidth=0.1, cntrPosRandFreq=10,
						 durRandWidth=0.1,  durRandFreq=10, revVol=0.1, delayTime=4,
						 decayTime=6, aDelTime=1, aDecTime=1, rateRandWidth=0.01, 
						 rateRandFreq=10, vol = 1;
			var fc, granny, outSignal, revSignal;
		
		granny = 
		TGrains.ar(
			2, 					// num channels
			Impulse.ar(trigRate), 	// trigger
			buffer,				// buffer
			freq + 
			TRand.kr(-1*rateRandWidth, rateRandWidth, Impulse.kr(rateRandFreq)), // rate  
			// cntrpos
			centerPos + 
			TRand.kr(-1*cntrPosRandWidth, cntrPosRandWidth, Impulse.kr(cntrPosRandFreq)),
			// duration of the grain
			dur + TRand.kr(-1*durRandWidth, durRandWidth, Impulse.kr(durRandFreq)), 				WhiteNoise.kr(pan),  	// pan
				amp, 				// amplitude
				2); 	// interpolation : (1 = no interp. 2 = linear interp. 4 = cubic interpol.) 
			
			revSignal = Mix.ar(granny) * revVol;
			revSignal = Mix.ar(CombL.ar(revSignal, 0.1, 
				{0.04.rand2 + 0.05}.dup(4) * delayTime,  decayTime));
			4.do({ revSignal = AllpassN.ar(revSignal, 0.150, [0.050.rand,0.051.rand] 
				* aDelTime, aDecTime) });
			Out.ar(out, granny + LeakDC.ar(revSignal) * vol);
		});
		
		// ------ the predators ------- !!!!!!!!!!!!!!!!!!!!!!!!!!!

		// - the sample player
		SynthDef.writeOnce(\xiiPrey1x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0,
			vol=1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.17, 0.4, 0.162]; 
			var bufplay, env;
			bufplay = LoopBuf.ar(1, bufnum, rate, 1, startPos, startPos, endPos) * vol;
			//bufplay = PlayBuf.ar(1, bufnum, rate, 1, 1, startPos, 0) * vol;
			env = EnvGen.kr(Env.new(levels, times), timeScale: timesc, doneAction:2);
			Out.ar(outbus, (bufplay!2)*env);
		});
		
		SynthDef.writeOnce(\xiiPrey2x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0,
			vol=1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.17, 0.4, 0.162]; 
			var bufplay, env;
			bufplay = LoopBuf.ar(2, bufnum, rate, 1, startPos, startPos, endPos) * vol;
			//bufplay = LoopBuf.ar(2, bufnum, rate, 1, 1, startPos, 0) * vol;
			env = EnvGen.kr(Env.new(levels, times), timeScale: timesc, doneAction:2);
			Out.ar(outbus, bufplay*env);
		});
		
		SynthDef.writeOnce(\xiiAudioStream, {arg inbus=20, outbus=0, amp=1, pitchratio=1.0, timesc=1.0, 
				levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.17, 0.4, 0.162];
			var in, env, killenv;
			env = EnvGen.kr(Env.new(levels, times), timeScale: timesc, doneAction:2);
			in = InFeedback.ar(inbus, 2); 
			in = PitchShift.ar(in, 0.1, pitchratio, 0, 0.004) * amp * env;
			in = LPF.ar(in, 20000); // make sure not to get spectral mirroring
			Out.ar(outbus, in);
		});

		SynthDef.writeOnce(\xiiCode, {arg outbus=0, freq=440, pan=0, amp=1;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = SinOsc.ar(freq, 0, env*amp);
			Out.ar(outbus, Pan2.ar(sine, pan));
		});
		
		// - the bell synthesis
		SynthDef.writeOnce(\xiiBells, {arg outbus=0, freq=440, dur=1, amp=0.4, pan=0;
		        var x, in, env;
		        env = EnvGen.kr(Env.perc(0.01, Rand(333,666)/freq, amp), doneAction:2);
		        x = Mix.ar([SinOsc.ar(freq, 0, 0.3), SinOsc.ar(freq*2, 0, 0.2)] ++
		        				Array.fill(6, {SinOsc.ar(freq*Rand(-10,10), 0, Rand(0.08,0.2))}));
		        //x = BPF.ar(x, freq, 4.91);
		        in = LPF.ar(x, 20000); // make sure not to get spectral mirroring
		        in = Pan2.ar(in, pan);
		        Out.ar(outbus, x*env);
		});
		
		// - harmonic sines
		SynthDef.writeOnce(\xiiSines, {arg outbus=0, freq=440, dur=1, amp=0.4, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.01, 220/freq, amp), doneAction:2);
		        x = Mix.ar(Array.fill(8, {SinOsc.ar(freq*IRand(1,10),0, 0.12)}));
		        x = LPF.ar(x, 20000);
		        x = Pan2.ar(x,pan);
		        Out.ar(outbus, x*env);
		});
				
		// - synth1
		
		SynthDef.writeOnce(\xiiSynth1, {arg outbus, freq=440, dur=1, amp=0.4, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.01, 220/freq, amp), doneAction:2);
		        x = Mix.ar([FSinOsc.ar(freq, pi/2, 0.5), Pulse.ar(freq, Rand(0.3,0.5))]);
		        x = LPF.ar(x, 20000);
		        x = Pan2.ar(x,pan);
		        Out.ar(outbus, LeakDC.ar(x)*env);
		});
		
		SynthDef.writeOnce(\xiiKs_string, { arg outbus, note, pan, amp=1, rand, delayTime, noiseType=1;
			var x, y, env;
			env = Env.new(#[1, 1, 0],#[2, 0.001]);
			x = Decay.ar(Impulse.ar(0, 0, rand), 0.1+rand, WhiteNoise.ar); 
		 	x = CombL.ar(x, 0.05, note.reciprocal, delayTime, EnvGen.ar(env, doneAction:2)); 
		     x = LPF.ar(x, 20000) * amp;
			x = Pan2.ar(x, pan);
			Out.ar(outbus, LeakDC.ar(x));
		});
		
		SynthDef.writeOnce(\xiiImpulse, { arg outbus, pan, amp;
			var x, y, env, imp;
			env = Env.perc(0.0000001, 0.1);
			imp = Impulse.ar(1);
			x = Pan2.ar(imp * EnvGen.ar(env, doneAction:2), pan) * amp;
			Out.ar(outbus, LeakDC.ar(x));
		});
		
		
		SynthDef.writeOnce(\xiiRingz, {arg outbus, freq, pan, amp;
			var ring, trig;
			trig = (Impulse.ar(0.005, 180) * 0.01)
						* EnvGen.ar(Env.perc(0.001, 220/freq), doneAction:2);
			ring = Ringz.ar(trig, [freq, freq+2], 220/freq);
			ring = LPF.ar(ring, 20000);
			ring = Pan2.ar(ring, pan) * amp;
			Out.ar(outbus, LeakDC.ar(ring));
		});
		
		SynthDef.writeOnce(\xiiKlanks, { arg outbus=0, freq= 1.0, amp = 1, pan;
			var trig, klan, env;
			var  p, exc, x, s;
			trig = PinkNoise.ar( 0.11 );
			klan = Klank.ar(`[ Array.fill( 16, { Rand(1.0, 10.0) }), nil, 
							Array.fill( 16, { 0.1 + Rand(2.0)})], trig, freq );
			klan = (klan * amp).softclip;
			klan = LPF.ar(klan, 20000);
			env = EnvGen.ar(Env.perc(0.001, 340/freq), doneAction:2);
			Out.ar( outbus, Pan2.ar( LeakDC.ar(klan) * env, pan ));
		});
		
		// --------------- The Gridder ---------------------
		
		SynthDef.writeOnce(\xiiSine, {arg outbus=0, freq=440, phase=0, pan=0, amp=0.61;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = sum(SinOsc.ar([freq, freq+1], phase, 0.5*env*amp*AmpComp.kr(freq, 65)));
			Out.ar(outbus, Pan2.ar(sine, pan));
		});
		
		SynthDef.writeOnce(\xiiString, {arg outbus=0, freq=440, pan=0, amp=1;
			var pluck, period, string;
			pluck = PinkNoise.ar(Decay.kr(Impulse.kr(0.005), 0.05));
			period = freq.reciprocal;
			string = CombL.ar(pluck, period, period, 4);
			string = LeakDC.ar(LPF.ar(Pan2.ar(string, pan), 12000)) * amp;
			DetectSilence.ar(string, doneAction:2);
			Out.ar(outbus, string)
		});
				
		SynthDef.writeOnce(\xiiGridder, {arg outbus=0, freq=440, pan=0, amp=1;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = SinOsc.ar(freq, 0, env*amp);
			Out.ar(outbus, Pan2.ar(sine, pan));
		});
		
		
		// -------------- TrigRecorder ----------------------
		
		SynthDef.writeOnce(\xiiTrigRecAnalyser1x1, {arg out=120, inbus=8, prerectime=1, sensitivity=0.8;
			var in, onset, signal;
			in = InFeedback.ar(inbus, 1);
			in = Limiter.ar(in, 0.99, 0.01);
			
			onset = Onsets.kr(FFT(LocalBuf(512, 1), in), sensitivity, \rcomplex);
			SendTrig.kr(onset, 666);
			
			signal = DelayN.ar(in, 2, prerectime); 
			Out.ar(out, signal);
			
		});

		SynthDef.writeOnce(\xiiTrigRecAnalyser2x2, {arg out=120, inbus=8, prerectime=1, sensitivity=0.8;
			var onset, in, signal;
			in = InFeedback.ar(inbus, 2);
			in = Limiter.ar(in, 0.99, 0.01);
			
			onset = Onsets.kr(FFT(LocalBuf(512, 2), in), sensitivity, \rcomplex);
			SendTrig.kr(onset, 666);
			
			signal = DelayN.ar(in, 2, prerectime); 
			Out.ar(out, signal);
	
		});
		
		SynthDef.writeOnce(\xiiTrigRecorderRec1x1, {arg bufnum, inbus=120;
			DiskOut.ar(bufnum, In.ar(inbus, 1));
		});
		
		SynthDef.writeOnce(\xiiTrigRecorderRec2x2, {arg bufnum, inbus=120;
			DiskOut.ar(bufnum, In.ar(inbus, 2));
		});

				
		// -------------- Recorder vumeter ----------------------
		SynthDef.writeOnce(\xiiVuMeter, {arg inbus = 0, amp = 1.0, rate = 15, rel = 1;
			var signal, amplitude;
			signal = InFeedback.ar(inbus, 1) * amp;
			amplitude = Amplitude.ar(signal, 0.01, rel);
			SendTrig.kr(Impulse.kr(rate), 820, amplitude);
		});
		
				
		// -------------- StratoSampler Synths ----------------------
		SynthDef.writeOnce(\xiiStratoSamplerRec,{ arg  inbus=0, bufnum=0, reclevel=1.0, prelevel=0.0;
		ÊÊÊ var ain;
		ÊÊÊ ain = InFeedback.ar(inbus, 1);
			ain = Limiter.ar(ain, 0.99, 0.01);
		ÊÊÊ RecordBuf.ar(ain, bufnum, recLevel: reclevel, preLevel: prelevel);
		}, [0.2, 0.2 , 0.2, 0.2]);
	   
	   SynthDef.writeOnce(\xiiStratoSamplerPlay,{ arg outbus=0, bufnum,Ê endloop=1000, amp=1.0; 
			var signal;
			signal = LoopBuf.ar(1, bufnum, 1, 1, 0, 0, endLoop:endloop) * amp;
			Out.ar(outbus, signal!2);
		});
		
		
		// ----------------- Mushrooms --------------------
		
		// -------------- BufferOnsets Synth ----------------------
	   SynthDef.writeOnce(\xiiBufOnset,{ arg outbus=0, bufnum,Ê rate=1.0, endloop=1000, amp=1.0; 
			var signal;
			signal = LoopBuf.ar(1, bufnum, rate, 1, 0, 0, endLoop:endloop) * amp;
			Out.ar(outbus, signal!2);
		});
		
		// - the sample player
		SynthDef.writeOnce(\xiiMush1x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0,
			vol=1; 
			var bufplay, env;
			bufplay = LoopBuf.ar(1, bufnum, rate, 1, startPos, startPos, endPos) * vol;
			env = EnvGen.kr(Env.perc(0.001,0.2), timeScale: timesc, doneAction:2);
			Out.ar(outbus, (bufplay!2)*env);
		});
		
		SynthDef.writeOnce(\xiiMush2x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0,
			vol=1; 
			var bufplay, env;
			bufplay = LoopBuf.ar(2, bufnum, rate, 1, startPos, startPos, endPos) * vol;
			env = EnvGen.kr(Env.perc(0.001,0.2), timeScale: timesc, doneAction:2);
			Out.ar(outbus, bufplay*env);
		});
		
		SynthDef.writeOnce(\xiiMushTime,{ arg outbus=0, bufnum, rate=1.0, startloop=0, endloop=1000, amp=1.0; 
			var signal;	
			signal = LoopBuf.ar(1, bufnum, rate, 1, startloop, startloop, endLoop:endloop) * amp;
			Out.ar(outbus, signal!2);
		});

		SynthDef.writeOnce(\xiiMushFFT, {arg outbus=0, thresh, bufnum, fftbuf, trackbuf,rate=1.0, amp=1, 
								startloop=0, endloop=1000;
			var sig, onsets, pips, am;
			sig = LoopBuf.ar(1, bufnum, rate, 1, startloop, startloop, endLoop:endloop);
			am = Amplitude.kr(sig);
			onsets = XiiOnsets.kr(sig, fftbuf, trackbuf, thresh*8, \complex);
			6.do{ am = max(am, Delay1.kr(am))}; // get the max power over the last 6 control periods
			SendTrig.kr(onsets, 840, am);
			Out.ar(outbus, ((sig * amp)).dup);
		});
		
		// the default code synth of the  mushrooms
		SynthDef.writeOnce(\xiiMushroom, {arg outbus=0, freq=440, pan=0, amp=1;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = SinOsc.ar(freq, 0, env*amp);
			Out.ar(outbus, Pan2.ar(sine, pan));
		});
		
		// soundfileview synthdefs
		SynthDef.writeOnce(\xiiLoopBufXSndFileView1x1, { arg out=0, bufnum=0, start=0, end= -1, vol = 1;
			var z;
			z = Pan2.ar(LoopBuf.ar(1, bufnum, 1, 1, start, start, end), 0.0) * vol;
			Out.ar(out, z);
		});
		
		SynthDef.writeOnce(\xiiLoopBufXSndFileView2x2, { arg out=0, bufnum=0, start=0, end= -1, vol = 1;
			var z;
			z = LoopBuf.ar(2, bufnum, 1, 1, start, start, end) * vol;
			Out.ar(out, z);
		});

		SynthDef.writeOnce(\xiiPlayBufXSndFileView1x1, { arg out=0, bufnum=0, start=0, end= -1, vol = 1;
			var z;
			z = Pan2.ar(PlayBuf.ar(1, bufnum, 1, 1, start, start, end), 0.0) * vol;
			Out.ar(out, z);
		});
		
		SynthDef.writeOnce(\xiiPlayBufXSndFileView2x2, { arg out=0, bufnum=0, start=0, end= -1, vol = 1;
			var z;
			z = PlayBuf.ar(2, bufnum, 1, 1, start, start, end) * vol;
			Out.ar(out, z)
		});
		
		// theory synthdef
		SynthDef.writeOnce(\midikeyboardsine, {arg freq, amp = 0.25;
			Out.ar(0, (SinOsc.ar(freq,0,amp)*EnvGen.ar(Env.perc, doneAction:2)).dup)
		});
		
		// - the sounddrops synthdefs
		SynthDef.writeOnce(\xiiSounddrops1x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0, vol=1, amp=1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.17, 0.4, 0.162]; 
			var bufplay, env;
			bufplay = PlayBuf.ar(1, bufnum, rate, 1, startPos, 0) * vol * amp;
			env = EnvGen.kr(Env.new(levels, times), timeScale: timesc, doneAction:2);
			Out.ar(outbus, (bufplay!2)*env);
		});
		
		SynthDef.writeOnce(\xiiSounddrops2x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, timesc=1.0, vol=1, amp=1, levels = #[0, 0.0, 1.0, 0.8, 0.0], times = #[0.0, 0.17, 0.4, 0.162]; 
			var bufplay, env;
			bufplay = PlayBuf.ar(2, bufnum, rate, 1, startPos, 0) * vol * amp;
			env = EnvGen.kr(Env.new(levels, times), timeScale: timesc, doneAction:2);
			Out.ar(outbus, bufplay*env);
		});


		// -------------- quanoon makamat -----------------
		// try with a sound sample instead of noise as excitor
		SynthDef(\xiiQuanoon, {arg outbus=0, freq=440, pan=0, amp=1, dur=4;
			var pluck, period, string;
			pluck = PinkNoise.ar(Decay.kr(Impulse.kr(0.005), 0.05));
			period = freq.reciprocal;
			string = CombL.ar(pluck, period, period, dur);
			string = LeakDC.ar(LPF.ar(Pan2.ar(string, pan), 12000)) * amp;
			DetectSilence.ar(string, doneAction:2);
			Out.ar(outbus, string)
		}).load(s);

		// 110 Hz is the lowest frequency possible in this synthdef.
		// 2793 Hz is the highest frequency
		SynthDef(\xiiPluck, {arg outbus=0, t_trig=1, freq=440, pan=0, amp=1, dur=4;
			var pluck, period, string;
			pluck = Pluck.ar(WhiteNoise.ar(1), t_trig, 1/110, 1/freq, dur, coef:0); // ((freq/2793)-1).abs
			DetectSilence.ar(pluck, doneAction:2);
			Out.ar(outbus, pluck)
		}).load(s);



// ----------------- EventRecorder


		SynthDef(\xiiEventAnalyser, {arg out=122, fftbuf, inbus=8, prerectime=0.01,
						 onsetsensitivity=1.2, trailsensitivity=0.09, sustaintime=0.26;
			var in, signal, chain, onsettrig, trigger, gate, env;

			in = In.ar(inbus, 1);
			chain = FFT(fftbuf, in);
			onsettrig = Onsets.kr(chain, onsetsensitivity, \complex);
			
			trigger = Amplitude.kr(in) >= trailsensitivity;
			gate = EnvGen.kr(Env(#[0, 1, 0], #[0, 1], 0, 1), trigger+onsettrig, timeScale: sustaintime);
			env =  EnvGen.kr(Env.adsr(0.2, 0.1, 1, 0.1), gate, doneAction: 0);
		
			signal = DelayN.ar(in, 2, prerectime);
			
			Out.ar(out, signal);
			
			onsettrig = BinaryOpUGen('!=',  env, 0 ); // only return an onset trig if the env is at 0
		
			SendReply.kr(onsettrig, '/onset', onsettrig);
			//SendReply.kr(Amplitude.kr(in) >= trailsensitivity, '/trail', 1);
			SendReply.kr(Done.kr(env), '/xiidone', onsettrig);
		}).load(s);

		SynthDef(\xiiEventRecorder, {arg bufnum, inbus=122;
			DiskOut.ar(bufnum, In.ar(inbus, 1));
		}).load(s);



// ----------------- toshiomorpher

/*
		SynthDef(\xiiToshioAnalyser, {arg out=120, fftbuf, trackbuf, inbus=8, prerectime=1, sensitivity=0.8;
			var in, signal, onsettrig;
			in = InFeedback.ar(inbus, 1);
			//in = Limiter.ar(in, 0.99, 0.01);
			signal = DelayN.ar(in, 2, prerectime); 
			Out.ar(out, signal);
			onsettrig = XiiOnsets.kr(in, fftbuf, trackbuf, sensitivity, \complex);
			//SendTrig.kr(Amplitude.ar(in) >= sensitivity, 667);
			//SendTrig.kr(onsettrig, 667);
			//SendReply.kr(Trig1.kr(onsettrig), '/toshioonset', 667);
		}).load(s);
		
		(
d = Buffer.read(s,"sounds/digireedoo.aif");
e = Buffer.read(s,"sounds/holeMONO.aif");
f = Buffer.read(s, "sounds/birta.aif");
g = Buffer.read(s, "sounds/hola02.aif");
)

SynthDef(\xiiMorph1x2, { arg out=0, buffer1=2, buffer2=3, dur=2.5, amp=1;
	var inA, chainA, inB, chainB, chain, signal;
	inA = PlayBuf.ar(1, buffer1, 1, loop: 1);
	inB = PlayBuf.ar(2, buffer2, 1, loop: 1);
	chainA = FFT(LocalBuf(2048, 1), inA);
	chainB = FFT(LocalBuf(2048, 1), inB);
	chain = PV_Morph(chainA, chainB, Line.kr(0, 1, dur));
	signal = IFFT(chain).dup * EnvGen.ar(Env.linen(0.01, dur, 0.1, amp), doneAction:2);
	Out.ar(out,  signal);
}).play(s,[\out, 0, \buffer1, f.bufnum, \buffer2, g.bufnum]);


*/

		
		SynthDef(\xiiMorph11x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_Morph(chainA, chainB, Line.kr(0, 1, dur));
			signal = IFFT(chain).dup * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiMorph12x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
							startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_Morph(chainA, chainB, Line.kr(0, 1, dur));
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiMorph21x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_Morph(chainA, chainB, Line.kr(0, 1, dur));
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiMorph22x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_Morph(chainA, chainB, Line.kr(0, 1, dur));
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		SynthDef(\xiiSoftWipe11x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_SoftWipe(chainA, chainB, Line.kr(0, 1, dur));
			signal = IFFT(chain).dup * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiSoftWipe12x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
							startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_SoftWipe(chainA, chainB, Line.kr(0, 1, dur));
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiSoftWipe21x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_SoftWipe(chainA, chainB, Line.kr(0, 1, dur));
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiSoftWipe22x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_SoftWipe(chainA, chainB, Line.kr(0, 1, dur));
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);

		SynthDef(\xiiCopyPhase11x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_CopyPhase(chainA, chainB);
			signal = IFFT(chain).dup * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiCopyPhase12x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
							startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_CopyPhase(chainA, chainB);
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiCopyPhase21x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_CopyPhase(chainA, chainB);
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiCopyPhase22x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_CopyPhase(chainA, chainB);
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);


		SynthDef(\xiiRectComb11x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_RectComb2(chainA, chainB, 20, Line.kr(0, 1, dur), 0.3);
			signal = IFFT(chain).dup * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiRectComb12x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
							startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_RectComb2(chainA, chainB, 20, Line.kr(0, 1, dur), 0.3);
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiRectComb21x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_RectComb2(chainA, chainB, 20, Line.kr(0, 1, dur), 0.3);
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiRectComb22x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_RectComb2(chainA, chainB, 20, Line.kr(0, 1, dur), 0.3);
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);



		SynthDef(\xiiRandWipe11x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_RandWipe(chainA, chainB, Line.kr(0, 1, dur), 0.3);
			signal = IFFT(chain).dup * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiRandWipe12x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
							startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(1, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 1), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_RandWipe(chainA, chainB, Line.kr(0, 1, dur), 0.3);
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiRandWipe21x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(1, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 1), inB);
			chain = PV_RandWipe(chainA, chainB, Line.kr(0, 1, dur), 0.3);
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);
		
		
		SynthDef(\xiiRandWipe22x2, { arg outbus=0, buffer1=2, buffer2=3, startPos1=0, 
								startPos2=0, endPos1=1000, endPos2=1000, dur=0.5, amp=1;
			var inA, chainA, inB, chainB, chain, signal;
			inA = LoopBuf.ar(2, buffer1, 1, 1, startPos1, startPos1, endPos1);
			inB = LoopBuf.ar(2, buffer2, 1, 1, startPos2, startPos2, endPos2);
			chainA = FFT(LocalBuf(2048, 2), inA);
			chainB = FFT(LocalBuf(2048, 2), inB);
			chain = PV_RandWipe(chainA, chainB, Line.kr(0, 1, dur), 0.3);
			signal = IFFT(chain) * EnvGen.ar(Env.linen(0.1, dur-(0.2), 0.1, amp), doneAction:2);
			Out.ar(outbus,  signal);
		}).load(s);


		
		SynthDef(\xiiSamplePlayer1x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, dur=1.0, vol=1; 
			var bufplay, env;
			bufplay = LoopBuf.ar(1, bufnum, rate, 1, startPos, startPos, endPos, 4) * vol;
			env = EnvGen.ar(Env.linen(0.01, dur-(0.12), 0.1), doneAction:2);
			Out.ar(outbus, (bufplay!2)*env);
		}).load(s);
		
		SynthDef(\xiiSamplePlayer2x2, {arg outbus=0, bufnum, rate=1, startPos=0, endPos= -1, dur=1.0, vol=1; 
			var bufplay, env;
			bufplay = LoopBuf.ar(2, bufnum, rate, 1, startPos, startPos, endPos, 4) * vol;
			env = EnvGen.ar(Env.linen(0.01, dur-(0.12), 0.1), doneAction:2);
			Out.ar(outbus, bufplay*env);
		}).load(s);

		// sound scratcher synthdefs



SynthDef(\xiilpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, freqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, LPF.ar(In.ar(inbus, 1), freqenv))
}).load(s);


SynthDef(\xiilpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, freqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, LPF.ar(In.ar(inbus, 2), freqenv))
}).load(s);


SynthDef(\xiihpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, freqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, HPF.ar(In.ar(inbus, 1), freqenv))
}).load(s);


SynthDef(\xiihpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, freqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, HPF.ar(In.ar(inbus, 2), freqenv))
}).load(s);


SynthDef(\xiibpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, BPF.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiibpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, BPF.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);


SynthDef(\xiirlpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, RLPF.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiirlpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, RLPF.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);



SynthDef(\xiirhpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, RHPF.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiirhpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, RHPF.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);



SynthDef(\xiiresonant_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, Resonz.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiiresonant_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, Resonz.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);


SynthDef(\xiimoogff_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\gain]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, MoogFF.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiimoogff_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\gain]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, MoogFF.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);



SynthDef(\xiidelay_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, delays, feedbacks, delayenv, feedbackenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	delays = Control.names([\delay]).ir({1.0.rand}!72);
	feedbacks = Control.names([\feedback]).ir( {0.5}!72);
	delayenv = EnvGen.ar(Env.new( delays, times, 'sine' ), timeScale:dur);
	feedbackenv = EnvGen.ar(Env.new( feedbacks, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
		sig = In.ar(inbus, 1); 
		fx = sig + LocalIn.ar(1); 
		fx = DelayC.ar(fx, 2, delayenv); 
		LocalOut.ar(fx * feedbackenv); 
		//Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
	Out.ar(outbus, fx!2);
}).load(s);



SynthDef(\xiidelay_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, delays, feedbacks, delayenv, feedbackenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	delays = Control.names([\delay]).ir({1.0.rand}!72);
	feedbacks = Control.names([\feedback]).ir( {0.5}!72);
	delayenv = EnvGen.ar(Env.new( delays, times, 'sine' ), timeScale:dur);
	feedbackenv = EnvGen.ar(Env.new( feedbacks, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
		sig = In.ar(inbus, 2); 
		fx = sig + LocalIn.ar(1); 
		fx = DelayC.ar(fx, 2, delayenv); 
		LocalOut.ar(fx * feedbackenv); 

	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiifreeverb_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, room, damp, mix, roomenv, dampenv, mixenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	room = Control.names([\room]).ir({1.0.rand}!72);
	damp = Control.names([\damp]).ir( {0.5}!72);
	mix = Control.names([\mix]).ir( {0.5}!72);
	roomenv = EnvGen.ar(Env.new( room, times, 'sine' ), timeScale:dur);
	dampenv = EnvGen.ar(Env.new( damp, times, 'sine' ), timeScale:dur);
	mixenv = EnvGen.ar(Env.new( mix, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	sig = In.ar(inbus, 1); 
	fx = FreeVerb.ar(sig, mixenv, roomenv, dampenv); 
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiifreeverb_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, room, damp, mix, roomenv, dampenv, mixenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	room = Control.names([\room]).ir({1.0.rand}!72);
	damp = Control.names([\damp]).ir( {0.5}!72);
	mix = Control.names([\mix]).ir( {0.5}!72);
	roomenv = EnvGen.ar(Env.new( room, times, 'sine' ), timeScale:dur);
	dampenv = EnvGen.ar(Env.new( damp, times, 'sine' ), timeScale:dur);
	mixenv = EnvGen.ar(Env.new( mix, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	sig = In.ar(inbus, 2); 
	fx = FreeVerb.ar(sig, mixenv, roomenv, dampenv); 
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiidistortion_1x2, { arg inbus=20, outbus=0, dur=1;
	var sigtocomp, genv, times, pregain, postgain, pregainenv, postgainenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	pregain = Control.names([\pregain]).ir({1.0.rand}!72);
	postgain = Control.names([\postgain]).ir( {0.5}!72);
	pregainenv = EnvGen.ar(Env.new( pregain, times, 'sine' ), timeScale:dur);
	postgainenv = EnvGen.ar(Env.new( postgain, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1); 
	sigtocomp = ((sig * pregainenv).distort * postgainenv).distort;
	fx = Compander.ar(sigtocomp, sigtocomp, 1, 0, 1 );
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiidistortion_2x2, { arg inbus=20, outbus=0, dur=1;
	var sigtocomp, genv, times, pregain, postgain, pregainenv, postgainenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	pregain = Control.names([\pregain]).ir({1.0.rand}!72);
	postgain = Control.names([\postgain]).ir( {0.5}!72);
	pregainenv = EnvGen.ar(Env.new( pregain, times, 'sine' ), timeScale:dur);
	postgainenv = EnvGen.ar(Env.new( postgain, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2); 
	sigtocomp = ((sig * pregainenv).distort * postgainenv).distort;
	fx = Compander.ar(sigtocomp, sigtocomp, 1, 0, 1 );
	Out.ar(outbus, fx);
}).load(s);



SynthDef(\xiibitcrusher_1x2, { arg inbus=20, outbus=0, dur=1;
	var sigtocomp, genv, times, samplerate, bitsize, samplerateenv, bitsizeenv;
	var fx, sig, bitRedux, downsamp; 
	times = {71.reciprocal}!71;
	samplerate = Control.names([\samplerate]).ir({1.0.rand}!72);
	bitsize = Control.names([\bitsize]).ir( {0.5}!72);
	samplerateenv = EnvGen.ar(Env.new( samplerate, times, 'sine' ), timeScale:dur);
	bitsizeenv = EnvGen.ar(Env.new( bitsize, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1); 
	downsamp = Latch.ar(sig, Impulse.ar(samplerateenv*0.5));
	bitRedux = downsamp.round(0.5 ** bitsizeenv);
	Out.ar(outbus, bitRedux);
}).load(s);


SynthDef(\xiibitcrusher_2x2, { arg inbus=20, outbus=0, dur=1;
	var sigtocomp, genv, times, samplerate, bitsize, samplerateenv, bitsizeenv;
	var fx, sig, bitRedux, downsamp; 
	times = {71.reciprocal}!71;
	samplerate = Control.names([\samplerate]).ir({1.0.rand}!72);
	bitsize = Control.names([\bitsize]).ir( {0.5}!72);
	samplerateenv = EnvGen.ar(Env.new( samplerate, times, 'sine' ), timeScale:dur);
	bitsizeenv = EnvGen.ar(Env.new( bitsize, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2); 
	downsamp = Latch.ar(sig, Impulse.ar(samplerateenv*0.5));
	bitRedux = downsamp.round(0.5 ** bitsizeenv);
	Out.ar(outbus, bitRedux);
}).load(s);




SynthDef(\xiipitchshifter_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pitch, dispersion, pitchenv, dispersionenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	
	pitch = Control.names([\pitch]).ir({1.0.rand}!72);
	dispersion = Control.names([\dispersion]).ir( {0.5}!72);
	pitchenv = EnvGen.ar(Env.new( pitch, times, 'sine' ), timeScale:dur);
	dispersionenv = EnvGen.ar(Env.new( dispersion, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
    fx = PitchShift.ar(sig, 0.1, pitchenv, dispersionenv, 0.0001);
	Out.ar(outbus, fx);
}).load(s);


SynthDef(\xiipitchshifter_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pitch, dispersion, pitchenv, dispersionenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	
	pitch = Control.names([\pitch]).ir({1.0.rand}!72);
	dispersion = Control.names([\dispersion]).ir( {0.5}!72);
	pitchenv = EnvGen.ar(Env.new( pitch, times, 'sine' ), timeScale:dur);
	dispersionenv = EnvGen.ar(Env.new( dispersion, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2); 
    fx = PitchShift.ar(sig, 0.1, pitchenv, dispersionenv, 0.0001);
	Out.ar(outbus, fx);
}).load(s);



SynthDef(\xiipanamp_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pan, amp, panenv, ampenv;
	var sig; 
	times = {71.reciprocal}!71;
	
	pan = Control.names([\pan]).ir({1.0.rand}!72);
	amp = Control.names([\amp]).ir( {0.5}!72);
	panenv = EnvGen.ar(Env.new( pan, times, 'sine' ), timeScale:dur);
	ampenv = EnvGen.ar(Env.new( amp, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = Pan2.ar(In.ar(inbus, 1) * ampenv, panenv); 
	Out.ar(outbus, sig);
}).load(s);



SynthDef(\xiipanamp_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pan, amp, panenv, ampenv;
	var sig; 
	times = {71.reciprocal}!71;
	
	pan = Control.names([\pan]).ir({1.0.rand}!72);
	amp = Control.names([\amp]).ir( {0.5}!72);
	panenv = EnvGen.ar(Env.new( pan, times, 'sine' ), timeScale:dur);
	ampenv = EnvGen.ar(Env.new( amp, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = Pan2.ar(In.ar(inbus, 2).sum * ampenv, panenv); 
	Out.ar(outbus, sig);
}).load(s);




SynthDef(\xiicyberpunk_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pitch, zpch, pitchenv, zpchenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	
	pitch = Control.names([\pitch]).ir({1.0.rand}!72);
	zpch = Control.names([\zpch]).ir( {0.5}!72);
	pitchenv = EnvGen.ar(Env.new( pitch, times, 'sine' ), timeScale:dur);
	zpchenv = EnvGen.ar(Env.new( zpch, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
    fx = Squiz.ar(sig, pitchenv, zpchenv, 0.1);
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiicyberpunk_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pitch, zpch, pitchenv, zpchenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	
	pitch = Control.names([\pitch]).ir({1.0.rand}!72);
	zpch = Control.names([\zpch]).ir( {0.5}!72);
	pitchenv = EnvGen.ar(Env.new( pitch, times, 'sine' ), timeScale:dur);
	zpchenv = EnvGen.ar(Env.new( zpch, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
    fx = Squiz.ar(sig, pitchenv, zpchenv, 0.1);
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiimagaboveFFT_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, ceil, ceilenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	ceil = Control.names([\ceil]).ir({1.0.rand}!72);
	ceilenv = EnvGen.ar(Env.new( ceil, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
	chain = FFT(LocalBuf(2048, 1), sig);
	fx = PV_MagAbove(chain, ceilenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);


SynthDef(\xiimagaboveFFT_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, ceil, ceilenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	ceil = Control.names([\ceil]).ir({1.0.rand}!72);
	ceilenv = EnvGen.ar(Env.new( ceil, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
	chain = FFT(LocalBuf(2048, 2), sig);
	fx = PV_MagAbove(chain, ceilenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);



SynthDef(\xiibrickwallFFT_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, wipe, wipeenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	wipe = Control.names([\wipe]).ir({1.0.rand}!72);
	wipeenv = EnvGen.ar(Env.new( wipe, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
	chain = FFT(LocalBuf(2048, 1), sig);
	fx = PV_BrickWall(chain, wipeenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);


SynthDef(\xiibrickwallFFT_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, wipe, wipeenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	wipe = Control.names([\wipe]).ir({1.0.rand}!72);
	wipeenv = EnvGen.ar(Env.new( wipe, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
	chain = FFT(LocalBuf(2048, 2), sig);
	fx = PV_BrickWall(chain, wipeenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);




SynthDef(\xiirectcombFFT_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, teeth, phase, width, teethenv, phaseenv, widthenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	teeth = Control.names([\teeth]).ir({1.0.rand}!72);
	teethenv = EnvGen.ar(Env.new( teeth, times, 'sine' ), timeScale:dur);
	phase = Control.names([\phase]).ir({1.0.rand}!72);
	phaseenv = EnvGen.ar(Env.new( phase, times, 'sine' ), timeScale:dur);
	width = Control.names([\width]).ir({1.0.rand}!72);
	widthenv = EnvGen.ar(Env.new( width, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
	chain = FFT(LocalBuf(2048, 1), sig);
	fx = PV_RectComb(chain, teethenv, phaseenv, widthenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);



SynthDef(\xiirectcombFFT_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, teeth, phase, width, teethenv, phaseenv, widthenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	teeth = Control.names([\teeth]).ir({1.0.rand}!72);
	teethenv = EnvGen.ar(Env.new( teeth, times, 'sine' ), timeScale:dur);
	phase = Control.names([\phase]).ir({1.0.rand}!72);
	phaseenv = EnvGen.ar(Env.new( phase, times, 'sine' ), timeScale:dur);
	width = Control.names([\width]).ir({1.0.rand}!72);
	widthenv = EnvGen.ar(Env.new( width, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
	chain = FFT(LocalBuf(2048, 2), sig);
	fx = PV_RectComb(chain, teethenv, phaseenv, widthenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);




SynthDef(\xiimagsmearFFT_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, bins, binsenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	bins = Control.names([\bins]).ir({1.0.rand}!72);
	binsenv = EnvGen.ar(Env.new( bins, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
	chain = FFT(LocalBuf(2048, 1), sig);
	fx = PV_MagSmear(chain, binsenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);



SynthDef(\xiimagsmearFFT_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, bins, binsenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	bins = Control.names([\bins]).ir({1.0.rand}!72);
	binsenv = EnvGen.ar(Env.new( bins, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
	chain = FFT(LocalBuf(2048, 2), sig);
	fx = PV_MagSmear(chain, binsenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);

		// Slicer players
		
		SynthDef.writeOnce ( "xiiSlicerStereoPlayer" , 
			{ 
			arg outbus=0, buffer=0, amp=1, pan=0, mute=1, start=0, length=1, rate=1, fps=12, index=0; //, trig=1, resetpos=0;
				var len, st, offset, clock, env, sound, left, right, phasor;

				len = length * BufFrames.kr(buffer); // scale from range 0 - 1 to 0 - buflength in frames
				st = start * BufFrames.kr(buffer);	
				
				clock = LFPulse.kr(rate.abs / len, 0); // loop
				len = Latch.kr(len, clock);
				offset = Latch.kr(st, clock);
				env = EnvGen.kr( Env .new([0,1,1,0], [0.01, 0.98,0.01]), clock, timeScale:len /(rate.abs)) ;
				
				phasor = Phasor.ar( 0, rate, st, (start+length) * BufFrames.kr(buffer) );
				SendReply.kr(LFPulse.kr(fps, 0), '/ixi/slicer/playheads', phasor/BufFrames.kr(buffer), index);
				//SendTrig .kr( LFPulse.kr(fps, 0), index, phasor/BufFrames.kr(buffer)); //12 times per sec playhead. Normalised to 0-1 range
				#left, right = BufRd.ar( 2, buffer, phasor, 1 ) * env * amp * mute;
				Out .ar(outbus, Balance2.ar(left, right, pan));
		});

		
		SynthDef.writeOnce ( "xiiSlicerMonoPlayer" , 
			{ 
			arg outbus=0, buffer=0, amp=1, pan=0, mute=1, start=0, length=1, rate=1, fps=12, index=0; //, trig=1, resetpos=0;
				var len, st, offset, clock, env, sound, sig, phasor;

				len = length * BufFrames.kr(buffer); // scale from range 0 - 1 to 0 - buflength in frames
				st = start * BufFrames.kr(buffer);	
				
				clock = LFPulse.kr(rate.abs / len, 0); // loop
				len = Latch.kr(len, clock);
				offset = Latch.kr(st, clock);
				env = EnvGen.kr( Env .new([0,1,1,0], [0.01, 0.98,0.01]), clock, timeScale:len /(rate.abs)) ;
				
				phasor = Phasor.ar( 0, rate, st, (start+length) * BufFrames.kr(buffer) );
				SendReply.kr(LFPulse.kr(fps, 0), '/ixi/slicer/playheads', phasor/BufFrames.kr(buffer), index);
				//SendTrig .kr( LFPulse.kr(fps, 0), index, phasor/BufFrames.kr(buffer)); //12 times per sec playhead. Normalised to 0-1 range				
				sig = BufRd.ar( 1, buffer, phasor, 1 ) * env * amp * mute;
				Out .ar(outbus, Balance2.ar(sig, sig, pan));
		});
		//


	}
}	
   