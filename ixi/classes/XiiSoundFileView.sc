/*
// test the view
a = XiiSoundFileView.new(XQ.buffers(XQ.poolNames[1])[0], 0, XQ.poolNames[0], [0,7777], nil)

XQ.globalBufferList[0][0][0] // here one gets the first buffer
XQ.globalBufferList[0][1][0] // and here the selection for that

		a = Synth(\playBufXSndFileView2x2, 
					[\bufnum, 0, 
					\start, XQ.globalBufferList[0][1][0][0], 
					\end, (XQ.globalBufferList[0][1][0][0]+XQ.globalBufferList[0][1][0][1]), 
					\loop, 1]);


*/


XiiSoundFileView {	

	var <>xiigui, <>win;
	var <filename;
	
	var sndfileview, soundfile;
	var point, fileNumInPool;
	var bgColor, foreColor;
	var chNum, synth, loop, loopButt, playButt,setSelButt, deSelButt, resolutionSlider, zoomSlider, volSlider, cropButt, normButt;
	var synthRunFlag, timeCursorTask, resetclock; //, stopclock;
	var bufferPoolNum, vol, outbus;
	var start, end, seltime, logo, normalization;
	var numFramesView, selStartView, selEndView;
	
	*new { arg buffer, num=0, poolname, selArray, caller, chnl=0;
		^super.new.initXiiSoundFileView(buffer, num, poolname, selArray, caller, chnl);
		}
		
	initXiiSoundFileView {arg buffer, numInPool, poolname, selArray, caller, chnl;
	
	filename = buffer.path.basename;
	
		logo = [
Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
Point(24,43), Point(7,43), Point(1,36), Point(1,8)
];

		
//		//bufferPoolNum = bPoolNum;
//		if(caller.isKindOf(XiiBufferPool), { // if the caller is bufferpool
			win = GUI.window.new(filename+"  -  bufferPool: "+poolname, 							Rect(200, 600, 800, 248), 
							resizable:false).front;	
//		}, { // else it is the bufferplayer
//			win = GUI.window.new(filename, 
//							Rect(200, 500, 800, 238), 
//							resizable:false).front;	
//		});

		win.drawHook = {
			GUI.pen.color = Color.new255(255, 100, 0);
			GUI.pen.width = 3;
			GUI.pen.translate(17,204);
			GUI.pen.scale(0.35,0.35);
			GUI.pen.moveTo(1@7);
			logo.do({arg point;
				GUI.pen.lineTo(point+0.5);
			});
			GUI.pen.stroke;
		};

		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		loop = false;
		synthRunFlag = false;
		fileNumInPool = numInPool;
		
		soundfile = SoundFile.new;
		soundfile.openRead(buffer.path);
		chNum = soundfile.numChannels;
		vol = 1;
		outbus = 0;
		normalization = 1;
		
		selArray = if(selArray.sum == soundfile.numFrames, {
					[0,0]
				}, {
					selArray
				});
		
		sndfileview = SoundFileView(win, Rect(10, 0, 780, 190))
			.soundfile_(soundfile)
			.read(0, soundfile.numFrames)
			.elasticMode_(true)
			.timeCursorOn_(true)
			.timeCursorColor_(Color.white)
			.timeCursorPosition_(0)
			.drawsWaveForm_(true)
			.gridOn_(true)
			.gridResolution_(1)
			.gridColor_(Color.white)
			.waveColors_([ foreColor, foreColor ])
			.background_(bgColor)
			.canFocus_(false)
			.setSelectionColor(0, Color.new255(105, 185, 125))
			.setSelectionStart(0, selArray[0])		
			.setSelectionSize(0, selArray[1]) // size in frames
			.mouseDownAction_({arg view;
				selArray = view.selections[view.currentSelection];
				numFramesView.string_("frames : " + (selArray[1]));
				selStartView.string_("selStart : " + selArray[0]);
				selEndView.string_("selEnd : " + (selArray[0]+selArray[1]));
			})
			.mouseMoveAction_({arg view;
				selArray = view.selections[view.currentSelection];
				numFramesView.string_("frames : " + (selArray[1]));
				selStartView.string_("selStart : " + selArray[0]);
				selEndView.string_("selEnd : " + (selArray[0]+selArray[1]));
				if(caller.isKindOf(XiiBufferPlayer), {
					caller.setLoopRange(chnl, selArray[0], selArray[1]);
				});
			})
			.mouseUpAction_({
				if(synthRunFlag == true, { // IT'S LOOPING
					start = sndfileview.selections[0][0]; // the start
					end = start + sndfileview.selections[0][1]; // end of the selection
					
					if(sndfileview.selectionSize(0) < 10, {
					 	end = soundfile.numFrames; // used to be -1 but that's a bug in LoopBuf
						seltime = (soundfile.numFrames-start)/44100;
					}, { 
						seltime = sndfileview.selections[0][1]/44100;
					});
					synth.free;
					if(loop == false, {
						if(chNum == 1, {
							synth = Synth(\xiiPlayBufXSndFileView1x1, 
										[\bufnum, buffer.bufnum, \start, start, 
										\end, end, \out, outbus, \vol, vol]);
						}, {
							synth = Synth(\xiiPlayBufXSndFileView2x2, 
										[\bufnum, buffer.bufnum, \start, start, 
										\end, end, \out, outbus, \vol, vol]);
						});
					}, {
						if(chNum == 1, {
							synth = Synth(\xiiLoopBufXSndFileView1x1, 
										[\bufnum, buffer.bufnum, \start, start, 
										\end, end, \out, outbus, \vol, vol]);
						}, {
							synth = Synth(\xiiLoopBufXSndFileView2x2, 
										[\bufnum, buffer.bufnum, \start, start, 
										\end, end, \out, outbus, \vol, vol]);
						});
					});
					
					this.startTimeCursor;
				});
			});
		
		soundfile.close;
		
		zoomSlider = GUI.slider2D.new(win, Rect(60, 195, 176, 40 )) // scroll w/zoom x - felix!
					.canFocus_(false)
					.action_({|sl| 
						var y;
						sndfileview.zoomToFrac(y = sl.y * 0.95 + 0.05);
						sndfileview.scrollTo(sl.x);
					})
					.y_(1).x_(0.5);

		resolutionSlider = OSCIISlider(win, Rect(248,196,90,8), "resolution", 0.1, 10, 1, 0.1)
					.canFocus_(false)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({arg slider; 
						sndfileview.gridResolution_(slider.value);
					});
		
		volSlider = OSCIISlider(win, Rect(248, 222, 90, 8), "vol", 0, 1, 1, 0.01)
					.canFocus_(false)
					.value_(1)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({arg slider; 
						vol = slider.value;
						if(synthRunFlag == true, {
							synth.set(\vol, vol);
						});
					});

		GUI.popUpMenu.new(win, Rect(348, 196, 44, 16)) // outbusses
			.items_(
				if(chNum==2, {
					XiiACDropDownChannels.getStereoChnList
					}, {
					XiiACDropDownChannels.getMonoChnList
				})
			)
			.value_(0)
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch; 
				if(chNum == 2, {
					outbus = ch.value * 2;
					synth.set(\out, outbus);
				}, {
					outbus = ch.value;
					synth.set(\out, outbus);
				});
			});
			

		deSelButt = GUI.button.new(win, Rect(400, 195, 60, 18))
			.states_([["deselect",Color.black,Color.clear]])
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({
				sndfileview.setSelectionStart(0, 0);
				sndfileview.setSelectionSize(0, 0);
				sndfileview.timeCursorPosition_(0);
				numFramesView.string_("frames : " + soundfile.numFrames);
				selStartView.string_("selStart : "+ 0);
				selEndView.string_("selEnd : "+ soundfile.numFrames);
				
				//XQ.globalBufferList[bufferPoolNum][1][fileNumInPool] = [0, soundfile.numFrames];
				XQ.globalBufferDict.at(poolname.asSymbol)[1][fileNumInPool] = [0, soundfile.numFrames];
				
				// for the timecursor
				start = sndfileview.selections[0][0]; // the start
				end = start + sndfileview.selections[0][1]; // end of the selection
				
				if(sndfileview.selectionSize(0) < 10, {
				 	end = soundfile.numFrames; // used to be -1 but that's a bug in LoopBuf
					seltime = (soundfile.numFrames-start)/44100;
				}, { 
					seltime = sndfileview.selections[0][1]/44100;
				});

				if(synthRunFlag, {	
					if(loop == true, {
						synth.free;
						if(chNum == 1, {
							synth = Synth(\xiiLoopBufXSndFileView1x1, 
								[\bufnum, buffer.bufnum, \start, 0, 
								\end, soundfile.numFrames, \out, outbus, \vol, vol]);
						}, {
							synth = Synth(\xiiLoopBufXSndFileView2x2, 
								[\bufnum, buffer.bufnum, \start, 0, 
								\end, soundfile.numFrames, \out, outbus, \vol, vol]);
						});
					});
				});

			});

		setSelButt = GUI.button.new(win, Rect(465, 195, 75, 18))
			.states_([["set selection",Color.black,Color.clear]])
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg butt; var start, end, seltime;
				start = sndfileview.selections[0][0]; // the start
				end = start + sndfileview.selections[0][1]; // end of the selection
				if( end == 0, { end = soundfile.numFrames; });
				if( start < 0, { start = 0 });
				if( end > soundfile.numFrames, { end = soundfile.numFrames });

				XQ.globalBufferDict.at(poolname.asSymbol)[1][fileNumInPool] = [start, sndfileview.selections[0][1]];
			});

		normButt = Button(win, Rect(575, 195, 65, 18))
			.states_([["normalize", Color.black, Color.clear]])
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg butt; 
				var cond, tempbuf;
				cond = Condition.new;
				Routine.run({
					tempbuf = XQ.buffers(poolname.asSymbol)[fileNumInPool];
					tempbuf.normalize(normalization);
					Server.default.sync(cond);
					tempbuf.write(tempbuf.path, "AIFF", XQ.pref.bitDepth);
					Server.default.sync(cond);
					{
						soundfile = SoundFile.new;
						soundfile.openRead(tempbuf.path);
						sndfileview.soundfile_(soundfile);
						sndfileview.read(0, soundfile.numFrames);
						win.refresh;
					}.defer;
				});
			});

		volSlider = OSCIISlider(win, Rect(575, 222, 65, 8), "norm", 0, 1, 1, 0.01)
					.canFocus_(false)
					.value_(1)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({arg slider; 
						normalization = slider.value;
					});

		cropButt = GUI.button.new(win, Rect(645, 195, 45, 18))
			.states_([["crop",Color.black,Color.clear]])
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg butt; 
			
				var startFrame, numSelFrames, soundfile, view;

				startFrame = sndfileview.selections[0][0]; // the start
				numSelFrames = sndfileview.selections[0][1]; // end of the selection
				// if sound is playing, stop before cropping
				if(caller.isKindOf(XiiBufferPlayer), {
					caller.startButtList[chnl].valueAction_(0);
					
					XQ.pref.bufferPlayerTracks.do({ |i|
					if(caller.sfdropDownList[i].value == caller.sfdropDownList[chnl].value, {
						caller.startButtList[i].valueAction_(0)
					});
					});
					caller.viewerList[chnl].win.close; 
				});
				
				{
				// crop the sound
				XQ.buffers(poolname.asSymbol)[fileNumInPool]
					.crop(startFrame, numSelFrames, false);
				XQ.selections(poolname.asSymbol)[fileNumInPool] = [0, numSelFrames];
				}.defer(0.2);
				
				{
				view = XiiSoundFileView.new(
							XQ.buffers(poolname.asSymbol)[fileNumInPool], 
							numInPool, // the index in the pool
							poolname, // the poolname
							[0,0],
							caller, chnl);
					win.close;
					[\caller, caller].postln;
					if(caller.isKindOf(XiiBufferPlayer), {
						caller.viewerList[chnl] = view;
						"caller viewer :".post; caller.viewerList[chnl].postln;
					});
					if(caller.isKindOf(XiiBufferPool), {
						"caller is bufferpool".postln;
						caller.refreshBuffers; // refresh the buffer name (adding _CR)
					});
				}.defer(0.5);
			});

		numFramesView = GUI.staticText.new(win, Rect(348, 222, 80, 12))
					.font_(GUI.font.new("Helvetica", 9))
					.string_("frames : "+ selArray[1] );

		selStartView = GUI.staticText.new(win, Rect(420, 222, 80, 12))
					.font_(GUI.font.new("Helvetica", 9))
					.string_("selStart : "+ selArray[0] );

		selEndView = GUI.staticText.new(win, Rect(495, 222, 80, 12))
					.font_(GUI.font.new("Helvetica", 9))
					.string_("selEnd : "+ selArray[1] );

							
		loopButt = OSCIIRadioButton(win, Rect(696,197,14,14), "loop")
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg val; 
				if(val==1, { loop = true }, { loop = false });
			});

		playButt = GUI.button.new(win, Rect(740, 195, 43, 18))
			.states_([["play",Color.black,Color.clear], ["stop",Color.black, XiiColors.onbutton]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg butt;
				start = sndfileview.selections[0][0]; // the start
				end = start + sndfileview.selectionSize(0); // end of the selection
				if(sndfileview.selectionSize(0) < 10, {
				 	end = soundfile.numFrames-1;
					seltime = (soundfile.numFrames-start)/44100;
				}, { 
					seltime = sndfileview.selections[0][1]/44100;
				});
 				if(butt.value == 1, {
 					synthRunFlag = true;
					if(loop == false, {
						if(chNum == 1, {
							synth = Synth(\xiiPlayBufXSndFileView1x1, 
								[\out, outbus, \bufnum, buffer.bufnum, \start, start, 
								 \end, end, \out, outbus, \vol, vol]);
						}, {
							synth = Synth(\xiiPlayBufXSndFileView2x2, 
								[\out, outbus, \bufnum, buffer.bufnum, \start, start, 
								 \end, end, \out, outbus, \vol, vol]);
						});
					}, {
						if(chNum == 1, {
							synth = Synth(\xiiLoopBufXSndFileView1x1, 
								[\out, outbus, \bufnum, buffer.bufnum, \start, start, 
								 \end, end, \out, outbus, \vol, vol]);
						}, {
							synth = Synth(\xiiLoopBufXSndFileView2x2, 
								[\out, outbus, \bufnum, buffer.bufnum, \start, start, 
								 \end, end, \out, outbus, \vol, vol]);
						});
					});
					this.startTimeCursor;
				}, {					
					timeCursorTask.stop;
					resetclock.clear;
					synth.free; 
					synthRunFlag=false;
				});
			})
			.focus(true);
			
		win.onClose_({ 
			// write window position to archive.sctxar
			this.close;
			//caller.postln;
			if(caller.isKindOf(XiiBufferPlayer), {
			//	"caller is bufferplayer".postln;
				caller.viewerList[chnl] = nil;
			});
			point = Point(win.bounds.left, win.bounds.top);
			Archive.global.at(\win_position).put(poolname.asSymbol, point);
		}); 		
	}	
	
	startTimeCursor {
		resetclock.clear;
		timeCursorTask.stop;
		
		timeCursorTask = Task({ var pos;		
			pos = 0;
			if(loop == false, { // not looping
				resetclock = SystemClock.sched(seltime, { 
							{playButt.value_(0)}.defer; 
							if(synthRunFlag, {synth.free}); 							synthRunFlag=false; 
							{sndfileview.timeCursorPosition_(start)}.defer;
							timeCursorTask.stop;
						nil});
				inf.do({
					{
					win.isClosed.not.if({ // if window is not closed, update...
						sndfileview.timeCursorPosition_(start+pos)
					})
					}.defer;
					pos = pos + (Server.default.sampleRate/10);
					0.1.wait;
				});
			}, {			// looping
				resetclock = SystemClock.sched(seltime, {
					{sndfileview.timeCursorPosition_(start)}.defer;
					timeCursorTask.reset;
					pos = 0;
					nil;
				});
				
				inf.do({
					{
					win.isClosed.not.if({ // if window is not closed, update...
						sndfileview.timeCursorPosition_(start+pos)
					})
					}.defer;

					pos = pos + (Server.default.sampleRate/10);
					//{sndfileview.timeCursorPosition_(start+pos)}.defer;
					0.1.wait;
				});
			})
		}).start;
	}
	
	close {	
		timeCursorTask.stop;	
		resetclock.clear;
		if(synthRunFlag, {synth.free});
		win.close;
	}
	
	setBufferPlayerSelections {arg startPos, numSelectedFrames;
		// " -- start, numSelectedFrames".post; [startPos, numSelectedFrames].postln;
		sndfileview
			.setSelectionStart(1, startPos)
			.setSelectionSize(1, numSelectedFrames) // size in frames

	}
	
}

