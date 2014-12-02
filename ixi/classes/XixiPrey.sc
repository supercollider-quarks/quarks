XixiPrey {
	
	var <>point, <>pitch, rect, stageRect;
	var fillcolor, strokecolor;
	var rot, predatorArray, preyArray;
	
	var xDistance, yDistance, destpoint, move, friction;
	var posNegX, posNegY;
	var xAway, yAway;
	var ateFunc; // what happens when predator eats from the prey
	var myBuffer, myBufNum, myBufferName, poolnum, biteFlag;
	var poolnum, bufferindex, myVol;
	var <>selected, sampleNameField, pitchSampleField, outbus;
	var synthesis, fixedPitch, fixedPitchMode, poolname;
	var inbus, pitchratio;
	var env;
	
	*new { | point, stageRect, soundmode=1 | 
		^super.new.initXixiPrey(point, stageRect, soundmode);
	}
	
	initXixiPrey { |argpoint, argstageRect, soundmode|
		if(argstageRect == nil, {
			stageRect = argpoint;
			point = Point(stageRect.width.rand, stageRect.height.rand);
		},{
			stageRect = argstageRect;
			point = argpoint;
		});
		fillcolor = Color.yellow;
		strokecolor = Color.black;
		predatorArray = [];
		rot = pi.rand;
		rect = Rect(point.x, point.y, 10, 10);
				
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
		
		pitch = stageRect.height - point.y;
		fixedPitchMode = false;
		fixedPitch = pitch;
		this.setAteFunc_(soundmode);
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
		predatorArray.do({ |predator|
			if(rect.intersects(Rect(predator.point.x-10, predator.point.y-10, 20, 20)), {
				predator.moveAway;
			})
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
					fillcolor = Color.new255(159+(abs(dist)), abs(dist)*2.5, 0);
				})          			
			});
		});
	}
	
	draw {
		^{
			GUI.pen.use({
				if ( GUI.id == \cocoa, {  Pen.setShadow(1@2, 5, Color.black) });

				GUI.pen.color = Color.black;
				GUI.pen.width = 1;
				GUI.pen.translate(point.x, point.y);
				GUI.pen.moveTo(0@0);
				if(selected, {
					GUI.pen.color = Color.new(0,0,0,0.3);
					GUI.pen.strokeOval(Rect(-10, -10, 20, 20));
					GUI.pen.color = Color.black;
				});
				GUI.pen.rotate(rot);
		     	GUI.pen.line(0@0, 12@3);
		     	GUI.pen.line(0@0, 12@ -3);
		     	GUI.pen.stroke;
		     	GUI.pen.color = fillcolor;
		    		GUI.pen.fillOval(Rect(-5, -5, 10, 10));
		     	GUI.pen.color = Color.black;
		     	GUI.pen.strokeOval(Rect(-5, -5, 10, 10));
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
	
	mouseDown { |x, y, func|
		[\x, x, \y, y].postln;
		if(Rect(point.x-5, point.y-5, 10, 10).intersects(Rect(x, y, 1, 1)), {
			preyArray.do({arg prey; prey.selected = false});
			move = true;
			selected = true;
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
			^true;
		}, {
			^false;
		});
	}
		
	mouseTrack { |x, y, func|
		if(move==true, {
			point = Point(x, y);
			rect = Rect(point.x, point.y, 10, 10);
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
		if(rect.intersects( Rect(x+7, y+5, 1, 1) ), {
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
