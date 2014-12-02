XiiToshioAgent {
	
	var node, oldnode;
	var toshioArray, agentArray;
	
	var userview;
	var clock, timermode, playmode;
	var outbus, globalvol;
	var server, poolName, tempo;
	var silentBuf, toshioNodeSampleIndexArray;
	var loopmode;
	
	*new {arg node, toshioArray, userview, silentBuf;
		^super.new.initXiiToshioAgent(node, toshioArray, userview, silentBuf);
	}

	initXiiToshioAgent {arg anode, atoshioArray, auserview, aSilentBuf;
		node = anode;
		oldnode = node;
		toshioArray = atoshioArray;
		userview = auserview;
		timermode = \timer;
		playmode = \morph;
		outbus = 0;
		globalvol = 1;
		server = Server.default;
		poolName = "none";
		silentBuf = aSilentBuf;
		tempo = 1;
		loopmode = true;
	}
	
	jump {
		var index;
		oldnode = node;
		toshioArray[node[0]][node[1]].setWithAgent_(false);
		switch (toshioArray[node[0]][node[1]].dir) // direction of toshio
		{0} { node = node + [0, -1] }
		{1} { node = node + [1, -1] }
		{2} { node = node + [1, 0] }
		{3} { node = node + [1, 1] }
		{4} { node = node + [0, 1] }
		{5} { node = node + [-1, 1] }
		{6} { node = node + [-1, 0] }
		{7} { node = node + [-1, -1] };
		
		if( node.includes(-1) || node.includes(10), {
			// agent out of the map
			agentArray.do({arg agent, i; if(agent===this, {index = i;}) });
			agentArray.removeAt(index);
			this.stop;
		}, {
			if(toshioArray[node[0]][node[1]].arrow == true, {
				toshioArray[node[0]][node[1]].setWithAgent_(true);
			}, {
				//"no way out".postln;
				this.kill;
			});
		});
	}
	
	getNode {
		^node;
	}
	
	supplyAgentArray_ {arg agArray;
		agentArray = agArray;	
		//agentArray.postln;
	}
	
	supplyBufferIndexArray_ {arg array;
		toshioNodeSampleIndexArray = array;
	}
	
	supplyBufferPoolName_{arg name;
		poolName = name;
		//"supplied poolname".postln;
	}
	
	setToNode_ {arg anode;
		node = anode;
	}
	
	setTimerMode_{arg mode;
		timermode = mode;
		if(timermode == 0, {timermode = \timer});
		if(timermode == 1, {timermode = \sound});
		//"TIMERMODE IS : ".post; timermode.postln;
	}
	
	setPlayMode_{arg mode;
		playmode = mode;
	}

	setLoopMode_{arg mode;
		loopmode = mode;
	}
	
	setOutbus_{ arg bus;
		outbus = bus;
	}

	setVolume_{ arg v;
		globalvol = v;
	}
	
	setPoolName_ {arg argpn;
		poolName = argpn;
	}
	
	setTempo_{arg temp;
		//"setting tempo : ".post; tempo.postln;
		tempo = temp;
		try{ clock.tempo_(tempo) };
	}
	
	kill {
		
		//var index;
		/*
		if( node.includes(-1) || node.includes(10), {
			toshioArray[oldnode[0]][oldnode[1]].setWithAgent_(false);
		}, {
			toshioArray[node[0]][node[1]].setWithAgent_(false);
		});
		
		\deb1.postln;
		agentArray.do({arg agent, i; if(agent===this, {index = i;}) });
		\deb2.postln;
		agentArray.removeAt(index);
		\deb3.postln;
		*/
		
		//toshioArray[node[0]][node[1]].setWithAgent_(false);
		//\deb4.postln;
		this.stop;
		toshioArray[node[0]][node[1]].setWithAgent_(false);
		//\deb5.postln;
	}
	
	start {
		/* 
		I decided to have independent clocks for each agent as some will be
		running by time, others by sound sample length, i.e. independent wait for each agent
		*/
		
		"+++++++++++++++++++++++++ STARTING AGENT ++++++++++++++++++++++++++++".postln;
		//[\agentArray, agentArray, \toshioNodeSampleIndexArray, toshioNodeSampleIndexArray].postln;
		//[\tempo, tempo, \node, node, \silentBuf, silentBuf].postln;
		
		clock = TempoClock.new(tempo, 0, Main.elapsedTime.ceil)
			.schedAbs(0, { arg beat, q, sec;
				var soundDur, envDur, selStart1, selStart2, selEnd1, selEnd2;
				var oldsample, newsample, oldBuffer, newBuffer;
				var oldnode, newnode;
				//"---------------      ".post; agentArray.postln; 
				//if(agentArray.size == 0, { {startbutt.valueAction_(0)}.defer });
				//agentArray.do({arg agent; 

				oldnode = node;
				this.jump; // this can remove the agent from the array, but will continue in this sched
				newnode = node;
			
		//	[\newnode, newnode, \agent, agent, \agentarray, agentArray].postln;

				if( (newnode.includes(-1) || newnode.includes(10)).not, { // new node might be out of bounds
					//\clock______debug0.postln;
					//toshioNodeSampleIndexArray[newnode[0]][newnode[1]].postln;
					// play sound
					oldsample = toshioNodeSampleIndexArray[oldnode[0]][oldnode[1]];
					newsample = toshioNodeSampleIndexArray[newnode[0]][newnode[1]];
				//	\clock______debug1.postln;
				//	toshioNodeSampleIndexArray.postln;
				//	[\tempo, tempo, \node, node, \silentBuf, silentBuf].postln;
				//	[\newsample, newsample, \oldsample, oldsample].postln;
	
					//if(try{XQ.buffers(poolName)[newsample-1]} != nil, {
					switch(playmode)
					{\morph} {
					//if(playmode == \morph, { // if it's the morph mode
							
						oldBuffer = XQ.buffers(poolName)[newsample-1];
						if(oldBuffer.isNil, { 
							oldBuffer = silentBuf;
							selStart1 = 0;
							selEnd1 = 44100;
						 }, {
							selStart1 = XQ.selections(poolName)[newsample-1][0];
							selEnd1 = selStart1 + XQ.selections(poolName)[newsample-1][1];
						 });
						 
						newBuffer = XQ.buffers(poolName)[oldsample-1];
						if(newBuffer.isNil, {
							newBuffer = silentBuf;
							selStart2 = 0;
							selEnd2 = 44100;
						}, {
							selStart2 = XQ.selections(poolName)[oldsample-1][0];
							selEnd2 = selStart2 + XQ.selections(poolName)[oldsample-1][1];
						});
						
						soundDur = if((selEnd1-selStart1) > (selEnd2-selStart2), {
								(selEnd1-selStart1)/server.sampleRate;
							},{
								(selEnd2-selStart2)/server.sampleRate;
							});
//						[\soundDur, soundDur].postln;
//						[\timermode, timermode].postln;
//						[\loopmode, loopmode].postln;

						envDur = if(timermode == \timer, {
							if(loopmode, { // max time of loop is the timeframe
								tempo.reciprocal;
							}, { // if duration of sound is longer than timeframe...
								min(tempo.reciprocal, soundDur); 
							});
						}, {
							soundDur;
						});
//						[\envDur, envDur].postln;
						
						if(oldBuffer.numChannels == 1, {
							if(newBuffer.numChannels == 1, {
								"morph 11x2".postln;
								Synth(\xiiMorph11x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"morph 12x2".postln;
								Synth(\xiiMorph12x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						},{ 
							if(newBuffer.numChannels == 1, {
								"morph 21x2".postln;
								Synth(\xiiMorph21x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"morph 22x2".postln;
								Synth(\xiiMorph22x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						});
					}
					// }, { // else it's the playback mode
					{\playback} {	
						if(newsample != 0, { // 0 is the no sound (top of the popuplist)
							newBuffer = XQ.buffers(poolName)[newsample-1];
							if(newBuffer.isNil, {
								newBuffer = silentBuf;
								selStart1 = 0;
								selEnd1 = 44100;
							}, {
								selStart1 = XQ.selections(poolName)[newsample-1][0];
								selEnd1 = selStart1 + XQ.selections(poolName)[newsample-1][1];
							});
//							"SEL END : ".post; selEnd1.postln;
							soundDur = (selEnd1-selStart1)/server.sampleRate;
							envDur = if(timermode == \timer, {
								if(loopmode, {
//									"Loopmode is TRUE: using tempo.reciprocal".postln;
									tempo.reciprocal;
								}, {
//									[\soundDur, soundDur].postln;
//									"Loopmode is FALSE: using SoundDur".postln;
									soundDur;
								});
							}, {
								soundDur;
							});
							if(newBuffer.numChannels == 1, {
								Synth(\xiiSamplePlayer1x2, [
										\outbus, outbus,
										\bufnum, newBuffer.bufnum,                                   
										\startPos, selStart1, 
										\endPos, selEnd1,
										\dur, envDur,
										\vol, globalvol
								])
							},{
								Synth(\xiiSamplePlayer2x2, [
										\outbus, outbus,
										\bufnum, newBuffer.bufnum, 
										\startPos, selStart1, 
										\endPos, selEnd1,
										\dur, envDur,
										\vol, globalvol
								])
							});
						}, {
							envDur = 1;
						});
					} {\softwipe} {
						oldBuffer = XQ.buffers(poolName)[newsample-1];
						if(oldBuffer.isNil, { 
							oldBuffer = silentBuf;
							selStart1 = 0;
							selEnd1 = 44100;
						 }, {
							selStart1 = XQ.selections(poolName)[newsample-1][0];
							selEnd1 = selStart1 + XQ.selections(poolName)[newsample-1][1];
						 });
						 
						newBuffer = XQ.buffers(poolName)[oldsample-1];
						if(newBuffer.isNil, {
							newBuffer = silentBuf;
							selStart2 = 0;
							selEnd2 = 44100;
						}, {
							selStart2 = XQ.selections(poolName)[oldsample-1][0];
							selEnd2 = selStart2 + XQ.selections(poolName)[oldsample-1][1];
						});
						
						soundDur = if((selEnd1-selStart1) > (selEnd2-selStart2), {
								(selEnd1-selStart1)/server.sampleRate;
							},{
								(selEnd2-selStart2)/server.sampleRate;
							});
//						[\soundDur, soundDur].postln;
//						[\timermode, timermode].postln;
//						[\loopmode, loopmode].postln;

						envDur = if(timermode == \timer, {
							if(loopmode, { // max time of loop is the timeframe
								tempo.reciprocal;
							}, { // if duration of sound is longer than timeframe...
								min(tempo.reciprocal, soundDur); 
							});
						}, {
							soundDur;
						});
//						[\envDur, envDur].postln;
						
						if(oldBuffer.numChannels == 1, {
							if(newBuffer.numChannels == 1, {
								"SoftWipe 11x2".postln;
								Synth(\xiiSoftWipe11x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"SoftWipe 12x2".postln;
								Synth(\xiiSoftWipe12x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						},{ 
							if(newBuffer.numChannels == 1, {
								"SoftWipe 21x2".postln;
								Synth(\xiiSoftWipe21x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"SoftWipe 22x2".postln;
								Synth(\xiiSoftWipe22x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						});
					}
					{\copyphase} {
						oldBuffer = XQ.buffers(poolName)[newsample-1];
						if(oldBuffer.isNil, { 
							oldBuffer = silentBuf;
							selStart1 = 0;
							selEnd1 = 44100;
						 }, {
							selStart1 = XQ.selections(poolName)[newsample-1][0];
							selEnd1 = selStart1 + XQ.selections(poolName)[newsample-1][1];
						 });
						 
						newBuffer = XQ.buffers(poolName)[oldsample-1];
						if(newBuffer.isNil, {
							newBuffer = silentBuf;
							selStart2 = 0;
							selEnd2 = 44100;
						}, {
							selStart2 = XQ.selections(poolName)[oldsample-1][0];
							selEnd2 = selStart2 + XQ.selections(poolName)[oldsample-1][1];
						});
						
						soundDur = if((selEnd1-selStart1) > (selEnd2-selStart2), {
								(selEnd1-selStart1)/server.sampleRate;
							},{
								(selEnd2-selStart2)/server.sampleRate;
							});
//						[\soundDur, soundDur].postln;
//						[\timermode, timermode].postln;
//						[\loopmode, loopmode].postln;

						envDur = if(timermode == \timer, {
							if(loopmode, { // max time of loop is the timeframe
								tempo.reciprocal;
							}, { // if duration of sound is longer than timeframe...
								min(tempo.reciprocal, soundDur); 
							});
						}, {
							soundDur;
						});
//						[\envDur, envDur].postln;
						
						if(oldBuffer.numChannels == 1, {
							if(newBuffer.numChannels == 1, {
								"CopyPhase 11x2".postln;
								Synth(\xiiCopyPhase11x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"CopyPhase 12x2".postln;
								Synth(\xiiCopyPhase12x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						},{ 
							if(newBuffer.numChannels == 1, {
								"CopyPhase 21x2".postln;
								Synth(\xiiCopyPhase21x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"CopyPhase 22x2".postln;
								Synth(\xiiCopyPhase22x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						});
					}
					{\rectcomb} {
						oldBuffer = XQ.buffers(poolName)[newsample-1];
						if(oldBuffer.isNil, { 
							oldBuffer = silentBuf;
							selStart1 = 0;
							selEnd1 = 44100;
						 }, {
							selStart1 = XQ.selections(poolName)[newsample-1][0];
							selEnd1 = selStart1 + XQ.selections(poolName)[newsample-1][1];
						 });
						 
						newBuffer = XQ.buffers(poolName)[oldsample-1];
						if(newBuffer.isNil, {
							newBuffer = silentBuf;
							selStart2 = 0;
							selEnd2 = 44100;
						}, {
							selStart2 = XQ.selections(poolName)[oldsample-1][0];
							selEnd2 = selStart2 + XQ.selections(poolName)[oldsample-1][1];
						});
						
						soundDur = if((selEnd1-selStart1) > (selEnd2-selStart2), {
								(selEnd1-selStart1)/server.sampleRate;
							},{
								(selEnd2-selStart2)/server.sampleRate;
							});
//						[\soundDur, soundDur].postln;
//						[\timermode, timermode].postln;
//						[\loopmode, loopmode].postln;

						envDur = if(timermode == \timer, {
							if(loopmode, { // max time of loop is the timeframe
								tempo.reciprocal;
							}, { // if duration of sound is longer than timeframe...
								min(tempo.reciprocal, soundDur); 
							});
						}, {
							soundDur;
						});
//						[\envDur, envDur].postln;
						
						if(oldBuffer.numChannels == 1, {
							if(newBuffer.numChannels == 1, {
								"RectComb 11x2".postln;
								Synth(\xiiRectComb11x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"RectComb 12x2".postln;
								Synth(\xiiRectComb12x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						},{ 
							if(newBuffer.numChannels == 1, {
								"RectComb 21x2".postln;
								Synth(\xiiRectComb21x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"RectComb 22x2".postln;
								Synth(\xiiRectComb22x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						});
					}
					{\randwipe} {
						oldBuffer = XQ.buffers(poolName)[newsample-1];
						if(oldBuffer.isNil, { 
							oldBuffer = silentBuf;
							selStart1 = 0;
							selEnd1 = 44100;
						 }, {
							selStart1 = XQ.selections(poolName)[newsample-1][0];
							selEnd1 = selStart1 + XQ.selections(poolName)[newsample-1][1];
						 });
						 
						newBuffer = XQ.buffers(poolName)[oldsample-1];
						if(newBuffer.isNil, {
							newBuffer = silentBuf;
							selStart2 = 0;
							selEnd2 = 44100;
						}, {
							selStart2 = XQ.selections(poolName)[oldsample-1][0];
							selEnd2 = selStart2 + XQ.selections(poolName)[oldsample-1][1];
						});
						
						soundDur = if((selEnd1-selStart1) > (selEnd2-selStart2), {
								(selEnd1-selStart1)/server.sampleRate;
							},{
								(selEnd2-selStart2)/server.sampleRate;
							});
//						[\soundDur, soundDur].postln;
//						[\timermode, timermode].postln;
//						[\loopmode, loopmode].postln;

						envDur = if(timermode == \timer, {
							if(loopmode, { // max time of loop is the timeframe
								tempo.reciprocal;
							}, { // if duration of sound is longer than timeframe...
								min(tempo.reciprocal, soundDur); 
							});
						}, {
							soundDur;
						});
						[\envDur, envDur].postln;
						
						if(oldBuffer.numChannels == 1, {
							if(newBuffer.numChannels == 1, {
								"RectComb 11x2".postln;
								Synth(\xiiRandWipe11x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"RectComb 12x2".postln;
								Synth(\xiiRandWipe12x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						},{ 
							if(newBuffer.numChannels == 1, {
								"RectComb 21x2".postln;
								Synth(\xiiRandWipe21x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							},{
								"RectComb 22x2".postln;
								Synth(\xiiRandWipe22x2, [
										\outbus, outbus,
										\buffer1, oldBuffer.bufnum,                                   
										\buffer2, newBuffer.bufnum,
										\startPos1, selStart1, 
										\startPos2, selStart2, 
										\endPos1, selEnd1, 
										\endPos2, selEnd2, 
										\dur, envDur,
										\amp, globalvol
								])
							});
						});
					}	;

					//});
				});				
				{ userview.refresh }.defer;
				if(timermode == \timer, {"clockwait".postln; 1}, {"envDur".postln; envDur});
			});
	}
	
	stop {
		clock.stop;
	}
}
