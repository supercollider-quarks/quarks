XixiSampleCircle {
	
	var <>point, <>pitch, rect, stageRect;
	var fillcolor, strokecolor;
	var rot, predatorArray, preyArray;
	var mousePoint;
	
	var xDistance, yDistance, destpoint, move, friction;
	var posNegX, posNegY;
	var xAway, yAway;
	var ateFunc; // what happens when predator eats from the prey
	var myBuffer, myBufNum, myBufferName, poolnum, biteFlag;
	var poolnum, bufferindex, myVol;
	var <>selected, sampleNameField, pitchSampleField, outbus;
	var synthesis, fixedPitch, fixedPitchMode, poolname;
	var inbus, pitchratio;
	var env, synth;
	var synthPlaying;
	// new vars
	var radius, rangeradius;
	var amp, alpha, drawer;
	
	*new { | point, stageRect, drawer | 
		^super.new.initXixiSampleCircle(point, stageRect, drawer);
	}
	
	initXixiSampleCircle { |argpoint, argstageRect, argdrawer|
		if(argstageRect == nil, {
			stageRect = argpoint;
			point = Point(stageRect.width.rand, stageRect.height.rand);
		},{
			stageRect = argstageRect;
			point = argpoint;
		});
		drawer = argdrawer;
		alpha = 0.5;

		fillcolor = Color.rand.alpha_(alpha);
		strokecolor = Color.black;
		predatorArray = [];
		rot = pi.rand;
				
		destpoint = Point(50.rand2, 50.rand2);
		move = false;
		friction = 17.9;
		xAway = 1.rand2 * (30 + (30.rand));
		yAway = 1.rand2 * (30 + (30.rand));
		biteFlag = false;
		selected = false;
		myBufNum = -1; // no buffer to start with
		bufferindex = 0;
		poolnum = 0;
		myBufferName = "no sound";
		myVol = 0.4;
		inbus = 20;
		pitchratio = 1.0; // for playing buffers and audiostream (this is the pitch)
		// levels, times, timescale
		env = [[0]++[0.0, 1.0, 0.7, 0.7, 0.0], [0.0, 0.05, 0.25, 0.75, 1.0], 1];
		
		radius = 15;
		rangeradius = 30;
		rect = Rect(point.x-radius, point.y-radius, radius*2, radius*2);
		myBuffer = Buffer.read(Server.default, "sounds/a11wlk01-44_1.aiff"); // TEMP
		mousePoint = Point(0, 0);

		synthPlaying = false;
		pitch = stageRect.height - point.y;
		fixedPitchMode = false;
		fixedPitch = pitch;
//		this.setAteFunc_(soundmode);
		this.setRandomBuffer; // get a random sample (if the pool is loaded)
	}
	
	supplyPredatorArray {|argpredatorArray| 
		predatorArray = argpredatorArray;
	}
	
	supplyPreyArray {|argpreyArray|
		preyArray = argpreyArray;
	}
	
	supplyTextFields {arg argfieldarray;
		sampleNameField = argfieldarray[0];
		pitchSampleField = argfieldarray[1];
	}

	update {
		var dist, close;
		/*
		predatorArray.do({ |predator|
			if(rect.intersects(Rect(predator.point.x-10, predator.point.y-10, 20, 20)), {
				predator.moveAway;
			})
		});
		*/
		
		close = 100;
		dist = this.point.distanceFrom(mousePoint); // using the Point method, not the one in this class
		[\ampdist, (abs(dist)-100)/100].postln;
        	if(dist < 100, { // fix this:
        		if(synthPlaying.not, {
        			synth = Synth(\xiiWarp, [\bufnum, myBuffer, \vol, 1, \trate, 10, \dur, 1.reciprocal]);
        			synthPlaying = true;
        		});
			synth.set(\vol, (abs(dist)-100)/100);
		}, {
        		if(synthPlaying, {
        			synth.free;
        			synthPlaying = false;
        		});
		});
		
		
		close = 100;
		
		predatorArray.do({ |predator|
			var amp;
			amp = 0;
			dist = this.distanceFrom(predator);
        		if(dist < 96.0, { // fix this:
        			if(dist < close, {
					rot = atan2(predator.point.y - point.y, predator.point.x - point.x);
					close = dist;
					//fillcolor = Color.new255(159+(abs(dist)), abs(dist)*2.5, 0);
				//	[\dist, abs(dist)].postln;
					fillcolor = fillcolor.alpha_(alpha-((abs(dist)-100)/100));
				})          			
			});
		});
	}
	
	draw {
		^{
			GUI.pen.use({
				// if ( GUI.id == \cocoa, {  Pen.setShadow(1@2, 5, Color.black) });

				GUI.pen.color = Color.black;
				GUI.pen.width = 1;
				GUI.pen.translate(point.x, point.y);
				GUI.pen.moveTo(0@0);
				GUI.pen.rotate(rot);
		     	GUI.pen.line(0@0, 12@3);
		     	GUI.pen.line(0@0, 12@ -3);
		     	
		     	GUI.pen.color = Color.new(0,0,0,0.3);
		     	GUI.pen.strokeOval(Rect(-1*rangeradius, -1*rangeradius, rangeradius*2, rangeradius*2));

		     	GUI.pen.stroke;
		     	GUI.pen.color = fillcolor;
		    		GUI.pen.fillOval(Rect(-1*radius, -1*radius, radius*2, radius*2));
		     	GUI.pen.color = Color.black;
		     	GUI.pen.strokeOval(Rect(-1*radius, -1*radius, radius*2, radius*2));
				if(selected, {
					//GUI.pen.color = Color.new(0,0,0,0.3);
					GUI.pen.fillOval(Rect(-5, -5, 10, 10));
					//GUI.pen.color = Color.black;
				});
			});
    		}
	}
	
	predatorAteMe {
		if((myBufNum != -1) || (synthesis==true) , {
			ateFunc.value;
		});
	}
	
	setAteFunc_ {arg funcnr=0;
		if(funcnr > 0, {	
			synthesis = true; 
			try{pitchSampleField.string_("prey pitch :")};
		}, {
			synthesis = false;
			try{pitchSampleField.string_("prey sample :")};
		});
		ateFunc = switch (funcnr,
			0, { { var selStart, selEnd; // the sample player
				selStart = XQ.globalBufferDict.at(poolname)[1][bufferindex][0];
				selEnd = selStart + XQ.globalBufferDict.at(poolname)[1][bufferindex][1]-1;
				if(myBuffer.numChannels == 1, {
					Synth(\xiiPrey1x2, [	\outbus, outbus,
										\bufnum, myBufNum, 
										\startPos, selStart, 
										\endPos, selEnd,
										\vol, myVol,
										\timesc, env[2]
										]).setn(
											\levels, env[0],
											\times, env[1]
										);

				},{
					Synth(\xiiPrey2x2, [	\outbus, outbus,
										\bufnum, myBufNum, 
										\startPos, selStart, 
										\endPos, selEnd,
										\vol, myVol,
										\timesc, env[2]
										]).setn(
											\levels, env[0],
											\times, env[1]
										);
				});
			} },
			1, { {
					Synth(\xiiSine, [		\outbus, outbus,
										\freq, pitch,
										\amp, myVol
					])
			} },
			2, { {
					Synth(\xiiBells, [		\outbus, outbus,
										\freq, pitch,
										\amp, myVol
					])
			} },
			3, { {
					Synth(\xiiSines, [		\outbus, outbus,
										\freq, pitch,
										\amp, myVol
					])
			} },
			4, { {
					Synth(\xiiSynth1, [	\outbus, outbus,
										\freq, pitch,
										\amp, myVol
					])
			} },
			5, { {
					Synth(\xiiKs_string, [	\outbus, outbus,
										\note, pitch, 
										\pan, 0.7.rand2, 
										\rand, 0.1+0.1.rand, 
										\delayTime, 2+1.0.rand,
										\amp, myVol
										]);
			} },
			6, { {
					Synth(\xiiString, [	\outbus, outbus,
										\freq, pitch, 
										\pan, 0.7.rand2, 
										\amp, myVol
										]);
			} },
			7, { {
					Synth(\xiiImpulse, [	\outbus, outbus,
										\pan, 0.7.rand2,
										\amp, myVol
										]);
			} },
			8, { {
					Synth(\xiiRingz, [		\outbus, outbus,
										\freq, pitch, 
										\pan, 0.7.rand2,
										\amp, myVol
										]);
			} },
			9, { {
					Synth(\xiiKlanks, [	\outbus, outbus,
										\freq, pitch, 
										\pan, 0.7.rand2,
										\amp, myVol
										]);
			} },
			10, { { // the sc-code synthdef is compiled into the name xiiCode
					Synth(\xiiCode,	 [	\outbus, outbus,
										\freq, pitch, 
										\pan, 0.7.rand2,
										\amp, myVol
										]);
			} },
			11, { { // the audio stream
					Synth(\xiiAudioStream,[	\outbus, outbus,
										\inbus, inbus,
										\pitchratio, pitchratio, 
										\amp, myVol,
										\timesc, env[2]
										]).setn(
											\levels, env[0],
											\times, env[1]
										);
			} }
			)
		
	}
	
	setMyBuffer {arg argpoolname, prey, loading = false;
		poolname = argpoolname.asSymbol;
		if(loading, { // if loading a bufferpool
			if(myBufNum == -1, {  // if I don't have a buffer assigned then assign...
				if(try {XQ.globalBufferDict.at(poolname)[0] } != nil, {
					bufferindex = prey%XQ.globalBufferDict.at(poolname)[0].size; // if buf < preys
					myBuffer = XQ.globalBufferDict.at(poolname)[0].wrapAt(bufferindex);
					myBufNum = myBuffer.bufnum;
					myBufferName = myBuffer.path.basename;
					sampleNameField.string_(myBufferName);
				});
			});
		}, {
			if(selected, { // if selected then assign a sound
				bufferindex = prey;
				myBuffer = XQ.globalBufferDict.at(poolname)[0].wrapAt(bufferindex);
				myBufNum = myBuffer.bufnum;
				myBufferName = myBuffer.path.basename;
				sampleNameField.string_(myBufferName);
			});
		});
	}
	
	setRandomBuffer {arg argpoolname;
		poolname = argpoolname.asSymbol;
		if(try {XQ.globalBufferDict.at(poolname)[0] } != nil, {
			bufferindex = XQ.globalBufferDict.at(poolname)[0].size.rand; // choose a random buffer
			myBuffer = XQ.globalBufferDict.at(poolname)[0][bufferindex];
			myBufNum = myBuffer.bufnum;
			myBufferName = myBuffer.path.basename;
			sampleNameField.string_(myBufferName);
		});
	}
	
	setVolume_ {arg vol;
		myVol = vol;
	}
	
	
	setInBus_ {arg bus;
		inbus = bus;
	}
	
	supplyPredatorArray_ {arg predators;
		predatorArray = predators;
	}

	setPitchMode_ {arg mode;
		if(mode == 1, {
			fixedPitchMode = true;
			pitch = fixedPitch;
			pitchratio = 1.0;
			sampleNameField.string_(pitch.round(0.01).asString ++ "       " ++ pitch.cpsmidi.midinote);
		}, {
			fixedPitchMode = false;
			pitch = stageRect.height - point.y;
			pitchratio = ((pitch/460)+0.5); // rate for buffers and audiostream
			sampleNameField.string_(pitch.round(0.01).asString ++ "       ~" ++ pitch.cpsmidi.midinote);
		});
	}
	
	setPitch_ {arg midinote; // called from MIDIkeyboard
		if(selected, {
			pitch = midinote.midicps;
			fixedPitch = pitch;
			sampleNameField.string_(pitch.round(0.01).asString ++ "       " ++ midinote.midinote);
		});	
	}
	
	setEnvelope_ {arg envelope; // called from MIDIkeyboard
		if(selected, {
			env = envelope;
		});	
	}
	
	getEnv {
		^env;
	}
	
	setOutBus_ {arg bus;
		outbus = bus;
	}
		setLoc_ {|x, y|
		point = Point(x,y);
	}	
	getLoc {
		^point;
	}
	
	setSize_{arg size;
		if(selected, { radius = size/2; if(rangeradius<radius, { rangeradius = radius }) });
		drawer.refresh;
	}

	setRange_{arg range;
		if(selected, { rangeradius = range });
		drawer.refresh;
	}

	setAmp_{arg aamp;
		if(selected, { amp = aamp; alpha = aamp; fillcolor.alpha_(aamp) });
		drawer.refresh;
	}
	
	mouseDown { |x, y, func|
		mousePoint = Point(x, y);
		if(rect.intersects(Rect(x, y, 1, 1)), {
			preyArray.do({ arg prey; prey.selected = false });
			move = true;
			selected = true;
			
			// set circle to top
			block{|break| 
				preyArray.copy.do({ arg prey, i; 
					if(prey.selected == true, { 
						preyArray.swap(i, preyArray.size-1); 
//						[\predatorArray, predatorArray].postln;
						drawer.replaceDrawList(preyArray);
						drawer.addToDrawList(predatorArray);
						break.value 
					})
				})
			};

/*
			if(fixedPitchMode, {
				pitch = fixedPitch;
				pitchratio = 1.0; // rate for buffers and audiostream
			}, {
				pitch = stageRect.height - point.y;
				pitchratio = ((pitch/460)+0.5); // rate for buffers and audiostream
			});

			if(synthesis == false, {
				sampleNameField.string_(myBufferName);
			},{
				sampleNameField.string_(pitch.round(0.01).asString ++ "       " ++ pitch.cpsmidi.midinote);
			});
*/
			^true;
		}, {
			^false;
		});
	}
		
	mouseTrack { |x, y, func|
		mousePoint = Point(x, y);
		if(move==true, {
			point = Point(x, y);
			rect = Rect(point.x-radius, point.y-radius, radius*2, radius*2);
			if(fixedPitchMode, {
				pitch = fixedPitch;
				pitchratio = 1.0; // rate for buffers and audiostream
			}, {
				pitch = stageRect.height - point.y;
				pitchratio = ((pitch/460)+0.5); // rate for buffers and audiostream
			});
			if(synthesis == true, {
				sampleNameField.string_(pitch.round(0.01).asString ++ "       " ++ pitch.cpsmidi.midinote);
			});
		});
	}
		
	mouseUp { |x, y, func|
		if(rect.intersects( Rect(x, y, 1, 1) ), {
			move = false;			
		});
	}
	
	mouseOver { |x, y, func|
		if(rect.intersects(Rect(x,y,1, 1)), {
		});
	}	
	
	distanceFrom { |other|
		^sqrt(([this.point.x, this.point.y] - [other.point.x, other.point.y]).squared.sum);
	}
}
