XiiSnd {
	
	var <>point, <>pitch, rect, stageRect;
	var fillcolor, strokecolor;
	var rot, predatorArray, preyArray;
	
	var xDistance, yDistance, destpoint, move, friction;
	var posNegX, posNegY;
	var xAway, yAway;
	
	var outbus, inbus, selected;
	
	// temp vars
	var myVol;
	
//	var ateFunc; // what happens when predator eats from the prey
//	var myBuffer, myBufNum, myBufferName, poolnum, biteFlag;
//	var poolnum, bufferindex, myVol;
//	var <>selected, sampleNameField, pitchSampleField, outbus;
//	var synthesis, fixedPitch, fixedPitchMode, poolname;
//	var inbus, pitchratio;
//	var env;
	
	*new { | point, stageRect, soundmode=0 | 
		^super.new.initXiiSnd(point, stageRect, soundmode);
	}
	
	initXiiSnd { |argpoint, argstageRect, soundmode|
		if(argstageRect == nil, {
			stageRect = argpoint;
			point = Point(stageRect.width.rand, stageRect.height.rand);
		},{
			stageRect = argstageRect;
			point = argpoint;
		});
		fillcolor = XiiColors.darkgreen;
		strokecolor = Color.black;
		//predatorArray = [];
		rot = pi.rand;
		rect = Rect(point.x-5, point.y-5, 10, 10);
				
		destpoint = Point(50.rand2, 50.rand2);
		move = false;
		friction = 17.9;
		xAway = 1.rand2 * (30 + (30.rand));
		yAway = 1.rand2 * (30 + (30.rand));
		
		//biteFlag = false;
		selected = false;
		//myBufNum = -1; // no buffer to start with
		//bufferindex = 0;
		//poolnum = 0;
		//myBufferName = "no sound";
		myVol = 0.4;
		inbus = 20;
		//pitchratio = 1.0; // for playing buffers and audiostream (this is the pitch)
		// levels, times, timescale
		//env = [[0]++[0.0, 1.0, 0.7, 0.7, 0.0], [0.0, 0.05, 0.25, 0.75, 1.0], 2];
		
		//pitch = stageRect.height - point.y;
		
		//fixedPitchMode = false;
		//fixedPitch = pitch;
		//this.setAteFunc_(soundmode);
		//this.setRandomBuffer; // get a random sample (if the pool is loaded)
	}
	
//	supplyPredatorArray {|argpredatorArray| // from above level when all preds are initialized
//		//predatorArray = argpredatorArray;
//	}
//	
//	supplyPreyArray {|argpreyArray| // from above level when all preds are initialized
//		//preyArray = argpreyArray;
//	}
//	
//	supplyTextFields {arg argfieldarray;
//		//sampleNameField = argfieldarray[0];
//		//pitchSampleField = argfieldarray[1];
//	}

	update {
		var dist, close;
		
//		predatorArray.do({ |predator|
//			if(rect.intersects(Rect(predator.point.x-10, predator.point.y-10, 20, 20)), {
//				predator.moveAway;
//				//this.predatorAteMe; // now done in predator
//			})
//		});
//		
//		close = 100;
//		predatorArray.do({ |predator|
//			var amp;
//			amp = 0;
//			dist = this.distanceFrom(predator);
//        		if(dist < 96.0, { // fix this:
//        			if(dist < close, {
//					rot = atan2(predator.point.y - point.y, predator.point.x - point.x);
//					close = dist;
//					fillcolor = Color.new255(159+(abs(dist)), abs(dist)*2.5, 0);
//					//amp = 1 - (dist/60);
//				})          			
//			});
//			//if(amp > 0, {
//			//	predator.synth.set(\amp, amp); 
//			//});
//		});

		this.draw;
	}
	
	draw {
		^{
			Pen.use({
				Color.black.set;
				Pen.translate(point.x, point.y);
				Pen.moveTo(0@0);

				if(selected, {
					Color.new(0,0,0,0.3).set;
					Pen.strokeOval(Rect(-10, -10, 20, 20));
					Color.black.set;
				});

				//Pen.rotate(rot);

		     	//Pen.line(0@0, 12@3);
		     	//Pen.line(0@0, 12@ -3);
		     	//Pen.stroke;
			XiiColors.darkgreen.set;
			Pen.fillOval(Rect(point.x-3, point.y-3, 6, 6));
			Color.black.set;
			Pen.strokeOval(Rect(point.x-3, point.y-3, 6, 6));
			Pen.addArc(point, 7, 80.7, 1.9);
			Pen.stroke;
			Pen.addArc(point, 12, 80.8, 1.8);
			Pen.stroke;
			Pen.addArc(point, 7, 84, 1.9);
			Pen.stroke;
			Pen.addArc(point, 12, 84, 1.8);
			Pen.stroke;

			});
    		}
	}
	
//	predatorAteMe {
//		//"oy".postln;
//		//[\synthesis, synthesis].postln;
//		if((myBufNum != -1) || (synthesis==true) , { 	// if there is a buffer
//			//"eating ************************************".postln;
//			ateFunc.value;	// then play it
//		});
//		/* - this code is now in the predator
//		if(biteFlag == false, {
//			"I had a bite".postln;
//			biteFlag = true;
//			ateFunc.value;
//			AppClock.sched(5, { biteFlag = false; nil;});
//		});
//		*/
//	}
	
//	setAteFunc_ {arg funcnr=0;
//		"setting ate function ".post; funcnr.postln;
//		if(funcnr > 0, {	
//			synthesis = true; 
//			try{pitchSampleField.string_("prey pitch :")};
//		}, {
//			synthesis = false;
//			try{pitchSampleField.string_("prey sample :")};
//		});
//		[\synthesis, synthesis].postln;
//		ateFunc = switch (funcnr,
//			0, { { var selStart, selEnd; // the sample player
//				//"triggering bufferplayer".postln;
//				//[\myBufNum, myBufNum].postln;
//				//[\bufferindex, bufferindex].postln;
//				//"triggering bufferplayer".postln;
//				//[\selection, ~globalBufferList[poolnum][1][bufferindex]].postln;
//				selStart = ~globalBufferDict.at(poolname)[1][bufferindex][0];
//				selEnd = selStart + ~globalBufferDict.at(poolname)[1][bufferindex][1]-1;
//				//[\selStart,selStart].postln;
//				//[\selEnd, selEnd].postln;
//
//				if(myBuffer.numChannels == 1, {
//					Synth(\xiiPrey1x2, [	\outbus, outbus,
//										\bufnum, myBufNum, 
//										\startPos, selStart, 
//										\endPos, selEnd,
//										\vol, myVol,
//										\timesc, env[2]
//										]).setn(
//											\levels, env[0],
//											\times, env[1]
//										);
//
//				},{
//					Synth(\xiiPrey2x2, [	\outbus, outbus,
//										\bufnum, myBufNum, 
//										\startPos, selStart, 
//										\endPos, selEnd,
//										\vol, myVol,
//										\timesc, env[2]
//										]).setn(
//											\levels, env[0],
//											\times, env[1]
//										);
//				});
//			} },
//			1, { {
//					Synth(\xiiSine, [		\outbus, outbus,
//										\freq, pitch,
//										\amp, myVol
//					])
//			} },
//			2, { {
//					Synth(\xiiBells, [		\outbus, outbus,
//										\freq, pitch,
//										\amp, myVol
//					])
//			} },
//			3, { {
//					Synth(\xiiSines, [		\outbus, outbus,
//										\freq, pitch,
//										\amp, myVol
//					])
//			} },
//			4, { {
//					Synth(\XiiSynth1, [	\outbus, outbus,
//										\freq, pitch,
//										\amp, myVol
//					])
//			} },
//			5, { {
//					Synth(\xiiKs_string, [	\outbus, outbus,
//										\note, pitch, 
//										\pan, 0.7.rand2, 
//										\rand, 0.1+0.1.rand, 
//										\delayTime, 2+1.0.rand,
//										\amp, myVol
//										]);
//			} },
//			6, { {
//					Synth(\xiiString, [	\outbus, outbus,
//										\freq, pitch, 
//										\pan, 0.7.rand2, 
//										\amp, myVol
//										]);
//			} },
//			7, { {
//					Synth(\xiiimpulse, [	\outbus, outbus,
//										\pan, 0.7.rand2,
//										\amp, myVol
//										]);
//			} },
//			8, { {
//					Synth(\xiiRingz, [		\outbus, outbus,
//										\freq, pitch, 
//										\pan, 0.7.rand2,
//										\amp, myVol
//										]);
//			} },
//			9, { {
//					Synth(\xiiKlanks, [	\outbus, outbus,
//										\freq, pitch, 
//										\pan, 0.7.rand2,
//										\amp, myVol
//										]);
//			} },
//			10, { { // the sc-code synthdef is compiled into the name xiiCode
//					Synth(\xiiCode,	 [	\outbus, outbus,
//										\freq, pitch, 
//										\pan, 0.7.rand2,
//										\amp, myVol
//										]);
//			} },
//			11, { { // the audio stream
//					"timescale is : ".post; env[2].postln;
//					Synth(\xiiAudioStream,[	\outbus, outbus,
//										\inbus, inbus,
//										\pitchratio, pitchratio, 
//										//\pan, 0.7.rand2,
//										\amp, myVol,
//										\timesc, env[2]
//										]).setn(
//											\levels, env[0],
//											\times, env[1]
//										);
//			} }
//			)
//		
//	}
	
//	setMyBuffer {arg argpoolnum, prey, loading = false;
//		poolnum = argpoolnum;
//		if(loading, { // if loading a bufferpool
//			if(myBufNum == -1, {  // if I don't have a buffer assigned then assign...
//				if(try {~globalBufferDict.at(poolname)[0] } != nil, {
//					" OOOOOOOOOOOOOOOOOOO    loading BufferPool".postln;
//					bufferindex = prey%~globalBufferDict.at(poolname)[0].size; // if buf < preys
//					myBuffer = ~globalBufferDict.at(poolname)[0].wrapAt(bufferindex);
//					myBufNum = myBuffer.bufnum;
//					myBufferName = myBuffer.path.basename;
//					sampleNameField.string_(myBufferName);
//				});
//			});
//		}, {
//			if(selected, { // if selected then assign a sound
//				" 0000000000000000000   Im SELECTED ".postln;
//				bufferindex = prey;
//				myBuffer = ~globalBufferDict.at(poolname)[0].wrapAt(bufferindex);
//				myBufNum = myBuffer.bufnum;
//				" and my BUFNUM is ".post; myBufNum.postln;
//				myBufferName = myBuffer.path.basename;
//				sampleNameField.string_(myBufferName);
//			});
//		});
//	}
	
//	setRandomBuffer {arg argpoolname;
//		poolname = argpoolname.asSymbol;
//		if(try {~globalBufferDict.at(poolname)[0] } != nil, {
//			bufferindex = ~globalBufferDict.at(poolname)[0].size.rand; // choose a random buffer
//			myBuffer = ~globalBufferDict.at(poolname)[0][bufferindex];
//			myBufNum = myBuffer.bufnum;
//			myBufferName = myBuffer.path.basename;
//			sampleNameField.string_(myBufferName);
//			"I set a random buffer".postln;
//		});
//	}
	
	setVolume_ {arg vol;
		myVol = vol;
	}
	
	
	setInBus_ {arg bus;
		inbus = bus;
	}
	
	/*
	getAngry {
		fillcolor = Color.red;
		//AppClock.sched(3, {this.getCalm;});
	}
	
	getCalm {
		fillcolor = Color.yellow;
	}
	*/
	
	/*
	moveNow {
		xAway = 1.rand2 * (30 + (30.rand));
		yAway = 1.rand2 * (30 + (30.rand));
		move = true;
	}
	*/
	
//	setPitchMode_ {arg mode;
//		if(mode == 1, {
//			fixedPitchMode = true;
//			pitch = fixedPitch;
//			pitchratio = 1.0;
//			sampleNameField.string_(pitch.round(0.01).asString ++ "       " ++ pitch.cpsmidi.midinote);
//		}, {
//			fixedPitchMode = false;
//			pitch = stageRect.height - point.y;
//			pitchratio = ((pitch/460)+0.5); // rate for buffers and audiostream
//			sampleNameField.string_(pitch.round(0.01).asString ++ "       ~" ++ pitch.cpsmidi.midinote);
//		});
//	}
	
//	setPitch_ {arg midinote; // called from MIDIkeyboard
//		if(selected, {
//			pitch = midinote.midicps;
//			fixedPitch = pitch;
//			sampleNameField.string_(pitch.round(0.01).asString ++ "       " ++ midinote.midinote);
//		});	
//	}
//	
//	setEnvelope_ {arg envelope; // called from MIDIkeyboard
//		if(selected, {
//			env = envelope;
//		});	
//	}
	
	//getEnv {
//		^env;
//	}
	
	setOutBus_ {arg bus;
		//outbus = bus;
	}
	
	setLoc_ {|x, y|
		point = Point(x,y);
	}
	
	getLoc {
		^point;
	}
	
	mouseDown { |x, y, func|
		if(rect.intersects(Rect(x, y, 1, 1)), {
			move = true;
			selected = true;
//			if(fixedPitchMode, {
//				pitch = fixedPitch;
//				pitchratio = 1.0; // rate for buffers and audiostream
//			}, {
//				pitch = stageRect.height - point.y;
//				pitchratio = ((pitch/460)+0.5); // rate for buffers and audiostream
//			});
//			if(synthesis == false, {
//				sampleNameField.string_(myBufferName);
//			},{
//				sampleNameField.string_(pitch.round(0.01).asString ++ "       " ++ pitch.cpsmidi.midinote);
//			});
			//AppClock.sched(2, {selected = false; nil});
			^true;
		}, {
			^false;
		});
	}
		
	mouseTrack { |x, y, func|
		if(move==true, {
			point = Point(x, y);
			rect = Rect(point.x-5, point.y-5, 10, 10);

		});
	}
		
	mouseUp { |x, y, func|
		if(rect.intersects(Rect(x,y,1, 1)), {
			move = false;			
		});
	}
	
	mouseOver { |x, y, func|
		if(rect.intersects(Rect(x,y,1, 1)), {
			"mouseup".postln;
		});
	}	
	
	distanceFrom { |other|
		^sqrt(([this.point.x, this.point.y] - [other.point.x, other.point.y]).squared.sum);
	}
}
