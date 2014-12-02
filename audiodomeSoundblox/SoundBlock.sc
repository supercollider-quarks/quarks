SoundBlock {
	var <>color, <>ids;
	
	// upFace 	returns the index of the face that is most probably shown, 
	// faceStates	contains the actual probabilities (counters, _not_ normalized) 
	// faceLag	is the number of seconds, it takes to switch from one face to another
	var <upFace = 0, <faceStates, <>faceLag = 2;
	var <lastUpdates, lastTick;
	var <>fUpThresh = 0.11;
	var <>visible = false;
	var <posX = 0, <posY = 0, <rot = 0;

	var <visualsAddr;

	// fiducial ids of blocks
	classvar <>allIds;

	classvar <all;
	classvar <idCounter;
	var <id;
	*initClass {
		idCounter = 0;
		allIds = (
			red:[
				[ 108, 109, 110, 111, 112, 113 ],
				[ 114, 115, 116, 117, 118, 119 ],
				[ 120, 121, 122, 123, 124, 125 ]
			],
			green:[
				[ 126, 127, 128, 129, 130, 131 ],
				[ 132, 133, 134, 135, 136, 137 ],
				[ 138, 139, 140, 141, 142, 143 ]
			],
			lightGreen:[
				[ 144, 145, 146, 147, 148, 149 ],
				[ 150, 151, 152, 153, 154, 155 ],
				[ 156, 157, 158, 159, 160, 161 ]
			],
			orange:[
				[ 162, 163, 164, 165, 166, 167 ],
				[ 168, 169, 170, 171, 172, 173 ],
				[ 174, 175, 176, 177, 178, 179 ]
			],
			blue:[
				[ 180, 181, 182, 183, 184, 185 ],
				[ 186, 187, 188, 189, 190, 191 ],
				[ 192, 193, 194, 195, 196, 197 ]
			],
			yellow:[
				[ 198, 199, 200, 201, 202, 203 ],
				[ 204, 205, 206, 207, 208, 209 ],
				[ 210, 211, 212, 213, 214, 215 ]
			]
		);
			
			
		all = IdentitySet[];

	}
		
	*basicNew{|color = \red, ids, visualsAddr|
		^super.new.init(color, ids, visualsAddr)
	}
	*new{|color = \red, number = 0, visualsAddr|
		^this.basicNew(color, allIds[color][number], visualsAddr)
	}
	init {|aColor, aIDs, aVisualsAddr|
		// register object
		all.add(this);
		id = idCounter;
		idCounter = idCounter + 1;
		color = aColor;
		ids   = aIDs;
		visualsAddr = aVisualsAddr;

		// 	
		faceStates = {0}!6;
		upFace = 0;
		
		lastUpdates = (SystemClock.seconds!6);
		lastTick = lastUpdates.first.copy;
	}
	
	printOn { | stream |
		if (stream.atLimit) { ^this };
		stream << this.class.name << "( " << this.color.asCompileString << ", " << this.ids << " )" ;
	}
	
	// call this if a face were seen
	// when called, this method increases its value in faceStates (with a maximum of faceLag).
	faceSeen{|id = 0, visible = true, x, y, r|
		var idx = ids.indexOf(id);
		var timeStamp = SystemClock.seconds;
		var dt = (timeStamp - lastUpdates[idx]);


		//[\dt, dt, \idx, idx, \states, faceStates].postln;
		visible.if({
			// direct
			faceStates[idx] = min(faceStates[idx] + (0.2*dt), faceLag);
			lastUpdates[idx] = timeStamp;
		}, {
			faceStates[idx] = max(fUpThresh*0.99, 0);
			lastUpdates[idx] = timeStamp;
		});

		this.tick(x, y, r);
	}
	
	
	faceSeenAs {|setObj|
		this.faceSeen(setObj.id, setObj.visible, setObj.pos[0], setObj.pos[1], setObj.rotEuler.first)
	}
		
	
	tick {|x, y, r|
		var timeStamp = SystemClock.seconds;
		var dt = (timeStamp - lastTick);
		var tmp;
		
		faceStates = max(faceStates * max(1-dt, 0), 0);
		// update face information
		
		tmp = faceStates.selectIndex{|v| v > fUpThresh};
				
		// only set to new face, if tmp does not include current face
		tmp.includes(upFace).not.if({
			tmp.isEmpty.if({
				// face does not change, but block is invisible. Information on the actual detected face is not updated. 
				//upFace = nil;
				visible = false;
				// block is invisible
				this.performInvisible
			}, {
				upFace = faceStates.maxIndex;
				visible = true;
				// face has changed
				posX = x;
				posY = y;
				rot = r;
				this.performFaceChange;
				this.performBlockUpdate;
			});
		}, {
			// face has not changed, now coordinates of the block should be set to current upFace.
			visible = true;
			posX = x;
			posY = y;
			rot = r;
			this.performBlockUpdate;
		});
				
		lastTick = timeStamp;
	}
	
	// implement these methods to add custom functionality
	performFaceChange {
		//"%: face changed to %".format(this.color, upFace).inform;
	}

	performInvisible {
		// "%: invisible".format(this.color).inform;
	}
	
	performBlockUpdate {
		//["/block", id, upFace, posX, posY, rot, visible.binaryValue].postln;
		visualsAddr.notNil.if{visualsAddr.sendMsg("/block", id, upFace, posX, posY, rot, visible.binaryValue);};
		//"%: update".format(this.color).inform;
	}
	
	// unregister object
	remove {
		all.remove(this);
	}	
}

///////////////////////////////////////////////////


BufferBlock : SoundBlock {
	classvar <bus, <phase;

	var <>buffers;
	var <>synth;
	//var nodeProxy;
	var <synthName;
	var <synthParams;
	var <out;
	
	// helper
	var ampTrigs = #[
				[1, 0, 0, 0, 0, 0],
				[0, 1, 0, 0, 0, 0],
				[0, 0, 1, 0, 0, 0],
				[0, 0, 0, 1, 0, 0],
				[0, 0, 0, 0, 1, 0],
				[0, 0, 0, 0, 0, 1]
			];
	
	*new{|color=\red, number=0, visualsAddr, buffers, outChannel = 0|
		^super.new(color, number, visualsAddr).initBuffers(buffers, outChannel)
	}

	initBuffers {|aBuffers, aOut|
		buffers = aBuffers;
		out = aOut;
		synthParams = (
/*			amp0: [0], // used to trigger
			amp1: [0],
			amp2: [0],
			amp3: [0],
			amp4: [0],
			amp5: [0],
*/			
			masterAmp: [0.1],
			rates: 1!6,
			amp: [0.1],
			interpolation: [2],
			masterMute: [0],
			mute: [0],
			pitch: [1]
		);
		
		synthName = \bbSynth;
	}

	play {|server|
		server = server ? Server.default;
		
		synth.isNil.if({
			server.bind{
				synth = Synth(synthName, [\out, out, \phaseIn, BufferBlock.bus.index], target: server).setn(\bufnums, buffers.collect(_.bufnum));
				synthParams.keysValuesDo{|key, value|
					synth.setn(key, value)
				}
			}
		}, {
			"% already playing".format(color).inform;	
		})
	}

	stop {
		synth.free;
		synth = nil;	
	}

	out_{|val|
		out = val;
		synth.notNil.if{
			synth.set(\out, val);	
		}	
	}
	
	set {|what=\rates, val = 0, side=0|
		(what == \amps).if{
			//synth.setn(what, (ampTrigs[side] * val));
			synth.set((what ++ side).asSymbol, val);
			^this // break	
		};
		(what == \filterFreqs).if{
			//synth.setn(what, (ampTrigs[side] * val));
			synth.set((what ++ side).asSymbol, val);
			^this // break	
		};
		(what == \pitch).if{
			synthParams[what] = [val];
			synth.notNil.if{
				synth.set(what, val);
			}
			^this // break	
		};
		
		synthParams[what][side] = val;
		synth.notNil.if{
			synth.setn(what, synthParams[what]);
		}
	}

	setFace {|id, what=\amps, val=1|
		this.set(what, val, ids.indexOf(id))
	}

/*	nodeProxy {|server|
		server = server ? Server.default;
		
		nodeProxy.isNil.if({
			nodeProxy = NodeProxy(server, \audio, 1);
			nodeProxy.source = \bbSynth;
			nodeProxy.setn(\bufnums, buffers.collect(_.bufnum).postln);
			synthParams.keysValuesDo{|key, value|
				nodeProxy.setn(key, value)
			}
		});
		^nodeProxy
	}

	play {
		this.nodeProxy.playN(out);
	}

	out_ {|val|
		out = val;
		nodeProxy.isPlaying.if({
			nodeProxy.playN(out)	
		})
	}

	stop {
		nodeProxy.stop;	
	}


	set {|what=\amps, val = 0, side=0|
		synthParams[what][side] = val;
		nodeProxy.notNil.if{
			nodeProxy.setn(what, synthParams[what]);
		}
	}
*/

	*startClock{|server|
		server = server ? Server.default;
		bus = bus ?? {
			Bus.audio(server);
		};
		phase = Synth(\phaseSynth, [\out, bus.index], 1, addAction: \addBefore);
	}

	*sendSynth {
		SynthDef(\phaseSynth, {|out, rate = 1|
			Out.ar(out, 
				Phasor.ar(
					0, rate,
					0, 
					162830
				)
			)
		}).memStore;
	
		SynthDef(\bbSynth, {|out = 0, amp = 0.1, mute = 0, masterAmp = 1, masterMute = 0, interpolation = 2, ampLagUp = 0.1, ampLagDown = 1, phaseIn, globOut = 6, pitch = 1|
			var bufnums = \bufnums.kr([1, 2, 3, 4, 5, 6]);

			var inAmps = [
				\amps0.tr(1),
				\amps1.tr(1),
				\amps2.tr(1),
				\amps3.tr(1),
				\amps4.tr(1),
				\amps5.tr(1)
			];
			var filterFreqs = [
				\filterFreqs0.tr(1000),
				\filterFreqs1.tr(1000),
				\filterFreqs2.tr(1000),
				\filterFreqs3.tr(1000),
				\filterFreqs4.tr(1000),
				\filterFreqs5.tr(1000)
			].lag(0.3);

			var rates = \rates.kr(
				[1, 1, 1, 1, 1, 1], 0.1
			);
			
			var amps = inAmps.collect{|amp, i| 
				EnvGen.kr(Env.perc(ampLagUp, ampLagDown), gate: amp-0.1, levelScale: amp);
				// amp.lagud(1, 1)
			};
			
			var src = BufRd.ar(
				1, 
				bufnums, 
				In.ar(phaseIn), 
				1, 
				interpolation
			);
			src = PitchShift.ar(src, 0.1, pitch);
			
			src = RLPF.ar(src, filterFreqs, 0.8);
			
			Out.ar(    out, (src * amps).sum * amp * masterAmp * (1 - masterMute) * (1-mute));
			Out.ar(globOut, (src * amps).sum * amp * masterAmp * (1 - masterMute) * (1-mute));
		}).memStore;
	}
}

/*AmpelBlock : BufferBlock {

	*new{|color=\red, number=0, visualsAddr, buffers, outChannel = 0|
		^super.new(color, number, visualsAddr, buffers, outChannel).initAmpel;
	}

	initAmpel {
		synthName = \ampel;	
	}

	*sendSynth{
		SynthDef(\ampel, {|klackBufnum = 0, mechaBufnum = 1, out = 0, freq = 0.5, inter = 4, mechaPitch = 1, mechaMaxDur = 1.1, isGreen = 0, greenAmp = 0.0125, greenPitch = 1|
		
			var klack, mecha, greenSignal, trig;
			
			trig = Impulse.ar(freq);
			//trig = DelayN.ar(trig, 1, freq.reciprocal * 0.99, -1); // add gate closing
			
			klack = BufRd.ar(
				2,
				klackBufnum, 
				EnvGen.ar(Env([0, BufFrames.ir(klackBufnum), 0], [BufSampleRate.kr(klackBufnum).reciprocal * (BufFrames.ir(klackBufnum)), 0]), gate: trig), 
				0, // no loop 
				inter
			);
		
			mecha = BufRd.ar(
				2,
				mechaBufnum, 
				Phasor.ar(trig, (BufRateScale.kr(mechaBufnum)) * mechaPitch, 1000, BufFrames.kr(mechaBufnum)),
				1, // loop 
				inter
			);
			
			mecha = mecha * EnvGen.ar(Env.linen(0, max(0, min(mechaMaxDur, freq.reciprocal - 0.05)), 0.01), gate: trig);
		
		
			greenSignal = greenAmp * (SinOsc.ar([1200, 2400] * greenPitch, 0, [3, 5]).tanh * [0.5, 1] * LFPulse.ar(8, 0, 0.25).lag(0.0125)).sum * isGreen;
		
			Out.ar(out, [klack.sum, mecha.sum, greenSignal].sum);	
		}).memStore
	}
}*/