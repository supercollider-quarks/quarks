XiiAutoloop {
	
	var <>xiigui, <>win;

	*new { arg server=Server.default, channels=2, setting = nil;
		^super.new.initXiiAutoloop(server, channels, setting);
	}

	initXiiAutoloop {arg server, channels, setting;

		var buffers, buffNum, buffLengths, players, recorders;

		var initAll, closeAll, flushBuffer, timeTask, nextStep;
		var lengthsIndex, buffIndex, frozenBuffers, skipLengths;
		//var outbus, inbus;
		var playButtons, panSliders, ampSliders, rateSliders, flushButtons, freezeButtons;
		var lengthSliders, skipButtons;

		var dirtyBuffers;

		var firstlineX, firstlineY, gapY;

		var stereoChList, monoChList;

		var win, canvas1, sampling, numChannels; //

		var style;
		
		var logo;


				// SND INPUT bus channels
		//*********** remove this line later when integrated into ixiQuarks*********************************
		//XiiACDropDownChannels.numChannels_( 20 ); // NUMBER OF AUDIO BUSSES
		// *************************************************************************************************

		  



		//PlayBuf.ar(numChannels, bufnum, rate, trigger, startPos, loop, doneAction)
		SynthDef(\playBufStereo, { arg amp=1, pan=0.5, out = 0, rate = 1, bufnum = 0;
			var left, right;
			#left, right = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum)*rate, loop:1);
			Out.ar(out, Balance2.ar(left*amp, right*amp, pan));
		}).load(server);


		SynthDef(\recBufStereo, { arg in=2, bufnum=0, run=0, recLevel=1.0, preLevel=0.0 ;
			var sig;
			sig = AudioIn.ar([in, in+1]);
			RecordBuf.ar(sig, bufnum, run:run, recLevel:recLevel, preLevel:preLevel, loop:1);
		}).load(server);


		SynthDef(\playBufMono, { arg amp=1, pan=0.5, out = 0, rate = 1, bufnum = 0;
			var sig;
			sig = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum)*rate, loop:1);
			Out.ar(out, Balance2.ar(sig*amp, sig*amp, pan));
		}).load(server);



		SynthDef(\recBufMono, { arg in=2, bufnum=0, run=0, recLevel=1.0, preLevel=0.0 ;
			var sig;
			sig = AudioIn.ar(in);
			RecordBuf.ar(sig, bufnum, run:run, recLevel:recLevel, preLevel:preLevel, loop:1);
		}).load(server);




		///////////////



		// INIT some VARS
		sampling = false;

		numChannels = channels; ///************** stereo or mono. this should be set from ixiQuarks main window on open

		buffNum = 8;
		buffers = Array.fill(buffNum, {0}); // buffers
		buffLengths = Array.fill(buffNum, {1});
		frozenBuffers = Array.fill(buffNum, {0});
		skipLengths = Array.fill(buffNum, {0}); 

		dirtyBuffers = Array.fill(buffNum, {0}); // non zero on buffers market to change length

		// widgets
		playButtons = Array.fill(buffNum, {0});
		panSliders = Array.fill(buffNum, {0});
		ampSliders = Array.fill(buffNum, {0});
		rateSliders = Array.fill(buffNum, {0});
		flushButtons = Array.fill(buffNum, {0});
		freezeButtons = Array.fill(buffNum, {0});
		lengthSliders = Array.fill(buffNum, {0});
		skipButtons = Array.fill(buffNum, {0});

		players = Array.fill(buffNum, {0});
		recorders = Array.fill(buffNum, {0});
		 
		lengthsIndex = 0;
		buffIndex = 0;

		firstlineX = 25;
		firstlineY = 120;
		gapY = 20; // inbetween lines of widgets

		//hel9 = Font("Helvetica", 9);
		style = XiiStyles(numChannels);




		// FUNCTIONS


		flushBuffer =
		{	arg i; var srate=44100;
			if(server.actualSampleRate!=nil, {srate = server.actualSampleRate});
			buffers[i].free;
			buffers[i] = Buffer.alloc(server,  srate * buffLengths[i], numChannels); 
			("flushing buffer "+i+" / new sample length is "+(srate * buffLengths[i])).postln;
		};



		initAll = 
		{ 	
			lengthsIndex = 0;
			buffIndex = 0;

			buffNum.do({ arg i;
				try {
					var srate = 44100;
					if(server.actualSampleRate!=nil, {srate = server.actualSampleRate});
					buffers.put(i, Buffer.alloc(server,  srate * buffLengths[i], numChannels));
					("allocating " + buffers[i]).postln; 
					if (numChannels == 1,
						{
							recorders.put(i, Synth(\recBufMono, [\bufnum, buffers[i].bufnum]));
							players.put(i, Synth(\playBufMono, [\bufnum , buffers[i].bufnum]));
						},
						{
							recorders.put(i, Synth(\recBufStereo, [\bufnum, buffers[i].bufnum]));
							players.put(i, Synth(\playBufStereo, [\bufnum , buffers[i].bufnum]));
						}
					);
					players[i].set(\rate, 0); // paused
				}{"server not init?".postln};
			});
		};




		closeAll = 
		{
			timeTask.stop; //

			buffNum.do({ arg i;
				buffers[i].free;
				players[i].free;
				recorders[i].free;
			});
			"freeing resources".postln;
		};




		// TASKS ///////////////

		timeTask = Task({
			inf.do({
				// avoid no buffer or no length available
				if( (skipLengths.find([0]) == nil) || (frozenBuffers.find([0]) == nil) ,
				{0.5.wait; "must have on buffer and one timer at least!".postln}, // wait 
				{
					// RECORD //
					while(	{ frozenBuffers[buffIndex] == 1 }, // skip frozen buffers
						{ 
							buffIndex = buffIndex + 1;
							if(buffIndex >= buffers.size, { buffIndex=0 });
						}
					);
			
					// if market to change size, flush
					if ( dirtyBuffers[buffIndex] == 1, 
							{ 
								flushBuffer.value(buffIndex);
								dirtyBuffers[buffIndex] = 0; //clean										
							} 
					);
			
					// START recording
					try { recorders[buffIndex].set(\run, 1) }; 
			
					// WAIT //
					while(	{ skipLengths[lengthsIndex] == 1 }, // skip unavailable timers. 1=skip !!
						{
							lengthsIndex = lengthsIndex + 1;
							if(lengthsIndex >= skipLengths.size, { lengthsIndex=0 });
						}
					);
					buffLengths[lengthsIndex].wait; // WAIT HERE
		
					// STOP RECORDING //
					try { recorders[buffIndex].set(\run, 0) };
					//players[buffIndex].set(\bufnum, buffers[buffIndex].bufnum); // update
			
					// READY FOR NEXT STEP
					lengthsIndex = lengthsIndex + 1;
					buffIndex = buffIndex + 1;
					if(lengthsIndex >= buffLengths.size, { lengthsIndex=0 });
					if(buffIndex >= buffers.size, { buffIndex=0 });

					// update canvas1
					canvas1.clearDrawing;
					canvas1.refresh;
				}
				);
			});
		});







		/// GUI //////

		// WIN
		win = Window("Autoloop", Rect(100,100, 930, 310), resizable: false);

		win.onClose_(closeAll);

		win.view.keyDownAction = 
		{ arg view, char, modifiers, unicode, keycode;  
			[char, keycode].postln;

			case 
			{ keycode==27 } { // ESC key quits 
								timeTask.stop;
								closeAll.value;
								win.close;
							}
		 	{ ( (keycode >= 49) && (keycode <= 56) )} // keys 1-8 plot buffers 0 to 7
							{ 
								buffers[keycode-49].plot;
							}
		};


		// draw window background //
		logo = [
		Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
		Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
		Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
		Point(24,43), Point(7,43), Point(1,36), Point(1,8)
		]; // ixi logo points


		win.drawHook = 
		{
			// frame around widgets
			Pen.color = Color.new255(200, 200, 200);
			Pen.width = 1;

			Pen.addRect(
				Rect(7, 65, 460, 225)
			);
			Pen.addRect(
				Rect(480, 65, 160, 225)
			);
			Pen.addRect(
				Rect(655, 65, 272, 225)
			);
			// lines
			Pen.addRect( Rect(7, 110, 460, 1) );
			Pen.addRect( Rect(480, 110, 160, 1) );
			Pen.addRect( Rect(655, 110, 210, 1) );	
			Pen.stroke; // draw everything
	
			// ixi logo //
			Pen.color = Color.new255(255, 100, 0);
			Pen.width = 3;
			Pen.translate(win.bounds.width-40, 10);
			Pen.scale(0.4,0.4);
			Pen.moveTo(1@7);
			logo.do({arg point;
				Pen.lineTo(point+0.5);
			});
			Pen.stroke; // draw it
			// end logo	//
		};

		//////////////




		//////
		canvas1 = GUI.userView.new(win, Rect(480, 65, 195, 225))
				.canFocus_(false)
				.drawFunc_({ |view|
					Pen.color = Color(1, 0.0, 0.0, 1);
					Pen.width = 2;
					Pen.addRect( Rect(4, 59+(gapY*buffIndex-1), 10, 10) ); //step buffer
					Pen.addRect( Rect(179, 59+(gapY*lengthsIndex-1), 10, 10) ); //step timer
					Pen.stroke;
				})
				.relativeOrigin_(true) // use this for the refresh
				.clearOnRefresh_(false); // no refresh when window is refreshed
		//////






		// general stuff //

		// LOAD synthdefs
		Button(win, Rect(25,15, 60, 25))
			.font_(style.normal)
			.states_([["start",Color.black,Color.clear],["stop",Color.white,Color.black]])
			.action_({ arg butt;
				if(butt.value == 1, initAll, closeAll)
			}); // end action

		// toogle recording
		Button(win, Rect(100,15, 60, 25))
			.font_(style.normal)
			.states_([["sample", Color.black,Color.clear], ["stop",Color.white,Color.red]])
			.action_({ arg butt;
				if( butt.value == 1, { timeTask.start(AppClock) }, { timeTask.stop } )
			}); 


		// recorders IN BUS
		StaticText(win, Rect(185, 6, 100, 15))
			.font_(style.normal)
			.string_("input");

		PopUpMenu(win , Rect(185, 23, style.dropDownWidth, 16))
			.font_(style.normal)
			.items_(if(channels==1, {XiiACDropDownChannels.getMonoChnList}, {XiiACDropDownChannels.getStereoChnList}))
			.background_(Color.new255(255, 255, 255))
			.value_(1)
			.action_({ arg pm; var inbus;
				inbus = pm.value * numChannels; 
				("inbus set to "+inbus).postln;
				try{
					recorders.do({ arg i; i.set(\in, inbus) })
					//recSynth.moveToTail;
					//recSynth.set(\inbus, inbus);
				};
			});


		// players OUT BUS
		StaticText(win, Rect(255, 6, 100, 15))
			.font_(style.normal)
			.string_("output");

		PopUpMenu(win , Rect(255, 23, style.dropDownWidth, 16))
			.font_(style.normal)
			.items_(if(channels==1, {XiiACDropDownChannels.getMonoChnList}, {XiiACDropDownChannels.getStereoChnList}))			
			//.items_(XiiACDropDownChannels.getStereoChnList)
			.background_(Color.new255(255, 255, 255))
			.value_(0)
			.action_({ arg pm; var outbus;
				outbus = pm.value * numChannels; 
				("oubus set to "+outbus).postln;
				try{
					players.do({ arg i;	
						i.set(\outbus, outbus);
					})
					//recSynth.moveToTail;
					//recSynth.set(\inbus, inbus);
				};
			});



		// REC level
		StaticText(win, Rect(325, 12, 100, 15))
			.font_(style.normal)
			.string_("recLevel");
		Slider(win, Rect(370, 12, 90, 20)) // pan
			.value_(1)
			.action_({arg sl; 	
				try {
					recorders.do({ arg i;
						i.set(\recLevel, sl.value)
					})
				}
			});

		// PRE level. previous level
		StaticText(win, Rect(325, 33, 100, 15))
			.font_(style.normal)
			.string_("preLevel");
		Slider(win, Rect(370, 33, 90, 20)) // pan
			.value_(0)
			.action_({arg sl; 	
				try {
					recorders.do({ arg i;
						i.set(\preLevel, sl.value)
					})
				}
			});

		/*
		// frequency range
		EZRanger(win, Rect(500, 10, 310, 15), "freq range", \freq,
			{	arg v;
				players.do({ var i;
			
				})
			},
			[-20, 20]
		);

		// timer length range

		EZRanger(win, Rect(500, 30, 310, 15), "length range", \freq,
			{	arg v;
				v.value.postln
			},
			[0.01, 20000]
		);
		*/


		// PLAYERS //

		// TOGGLE ALL
		Button(win, Rect(25, firstlineY-40, 60, 25))
			.font_(style.normal)
			.states_([["playAll", Color.black,Color.clear], ["pauseAll", Color.white,Color.black]])
			.action_({ arg butt; 
				players.size.do({ arg i; var rt;
					if(butt.value==1, { rt = rateSliders[i].value }, { rt = 0 });
					try { players[i].set(\rate, rt) } 
						{"synths not initialised".postln };
					playButtons[i].value = butt.value; // update its button
				});
			});

		// PAN ALL
		Slider(win, Rect(100, firstlineY-40, 60, 20)) // pan
			.value_(0.5)
			.action_({arg sl; var pan;		
				players.size.do({ arg i; var pan;
					pan = [0, 2].asSpec.map(sl.value) - 1; // range -1 / 1
					try { players[i].set(\pan,  pan) }  
						{ "synths not initialised".postln };		
					panSliders[i].value = sl.value; // update its slider
				});
			});

		StaticText(win, Rect(120, firstlineY-55, 100, 15))
			.font_(style.normal)
			.string_("pan");

		// AMP ALL
		Slider(win, Rect(160, firstlineY-40, 100, 20)) 
			.value_(1)
			.action_({arg sl; var pan;	
				players.size.do({ arg i; var amp;
					amp = [0, 1, \amp].asSpec.map(sl.value);
					try { players[i].set(\amp, amp) } 
						{ "synths not initialised".postln }; 
					ampSliders[i].value = sl.value; // update its slider	
				});
			});
		StaticText(win, Rect(200, firstlineY-55, 100, 15))
			.font_(style.normal)
			.string_("amp");

		// RATE ALL
		StaticText(win, Rect(330, firstlineY-55, 100, 15))
			.font_(style.normal)
			.string_("freq");

		EZSlider(win, Rect(210, firstlineY-40, 250, 20), "", ControlSpec(-5, 5, \lin),
			{ arg ez; 		
				players.size.do({ arg i; 
					try { players[i].set(\rate, ez.value) }  
						{ "synths not initialised".postln };
					rateSliders[i].value = ez.value; // update its slider			
				});
			},
			1
		);






		// individual controls // playButtons, panSliders, ampSliders, rateSliders
		players.size.do(
		{ arg i;
			// Label
			StaticText(win, Rect(10, firstlineY+(gapY*i), 20, 15))
				.font_(style.normal)
				.string_(i+1);
			//PLAY
			playButtons.put(i, 
				Button(win, Rect(25, firstlineY+(gapY*i), 60, 15))
					.font_(style.normal)
					.states_([["play",Color.black,Color.clear],["pause",Color.white,Color.black]])
					.action_({ arg butt; var rt;
						if(butt.value==1, { rt = rateSliders[i].value }, { rt = 0 });
						try { players[i].set(\rate, rt) } 
							{"synths not initialised".postln}
					});
			);
			// PAN
			panSliders.put(i,
				Slider(win, Rect(100, firstlineY+(gapY*i), 60, 20)) 
					.value_(0.5)
					.action_({arg sl;
						try { players[i].set(\pan, [0, 2].asSpec.map(sl.value) - 1); }	// range -1 / 1		
							{"synths not initialised".postln};
					});
			);
			// AMP
			ampSliders.put(i,
				Slider(win, Rect(160, firstlineY+(gapY*i), 100, 20)) 
					.value_(1)
					.action_({arg sl; 
						try { players[i].set(\amp,  [0, 1, \amp].asSpec.map(sl.value));	}
							{"synths not initialised".postln};
					});
			);
			// RATE
			rateSliders.put(i,
				EZSlider(win, Rect(210, firstlineY+(gapY*i), 250, 20), "", ControlSpec(-5, 5, \lin),
					{ arg ez; 
						try { players[i].set(\rate,  ez.value) } 
							{ "synths not initialised".postln }
					},
					1
				);
			);
		});


		// BUFFERS //

		// FLUSH ALL
		Button(win, Rect(500, firstlineY-40, 60, 25))
			.font_(style.normal)
			.states_([["flush all",Color.black,Color.clear]])
			.action_({ arg butt;
				buffNum.do({ arg i;	
					try { flushBuffer.value(i) } ;
					flushButtons[i].value = 1;
				}); // flush all buffers
			}); 
		// FREEZE ALL
		Button(win, Rect(570, firstlineY-40, 60, 25))
			.font_(style.normal)
			.states_([["freeze all",Color.black,Color.clear],["all frozen",Color.white,Color.black]])
			.action_({ arg butt;
				buffNum.do({ arg i;	
					frozenBuffers[i] = butt.value;
					freezeButtons[i].value = butt.value;// update individual buttons
				}); // flush all buffers
			}); 

		// individual setttings
		buffers.size.do(
		{ arg i;
			// Label
			StaticText(win, Rect(485, firstlineY+(gapY*i), 20, 15))
				.font_(style.normal)
				.string_(i+1);
			// flush
			flushButtons.put(i,
				Button(win, Rect(500, firstlineY+(gapY*i), 60, 15))
					.font_(style.normal)
					.states_([["flush",Color.black,Color.clear]])
					.action_({ arg butt; flushBuffer.value(i); });
			);
			// freeze
			freezeButtons.put(i,		
				Button(win, Rect(570, firstlineY+(gapY*i), 60, 15))
					.font_(style.normal)
					.states_([["freeze",Color.black,Color.clear],["frozen",Color.white,Color.black]])
					.action_({ arg butt; frozenBuffers[i] = butt.value; });
			);
		});



		/// TIMERS -> buffLengths ///
		StaticText(win, Rect(775, firstlineY-55, 100, 15))
			.font_(style.normal)
			.string_("length");

		// skip timer	
		Button(win, Rect(675, firstlineY-40, 60, 15))
			.font_(style.normal)
			.states_([["skip",Color.black,Color.clear],["restore",Color.white,Color.black]])
			.action_({ arg butt; 
				skipLengths.size.do({ arg i; 
					skipLengths[i] = butt.value; 
					skipButtons[i].value = butt.value; 
				})
			});

		// length slider
		EZSlider(win, Rect(675, firstlineY-40, 250, 20), "", ControlSpec(0.0001, 10, \lin),
			{ arg ez; 
				buffLengths.size.do({ arg i; 
					try {
						buffLengths[i] = ez.value;	
						dirtyBuffers[i] = 1; //mark to change length next time it is recorded
					}  {"synths not initialised".postln};
					lengthSliders[i].value = ez.value; //update slider		
				});		
			},
			1 // default value!
		);

		buffLengths.size.do(
		{ arg i;
			// Label
			StaticText(win, Rect(660, firstlineY+(gapY*i), 20, 15))
				.font_(style.normal)
				.string_(i+1);
			skipButtons.put(i,		
				Button(win, Rect(675, firstlineY+(gapY*i), 60, 15))
					.font_(style.normal)
					.states_([["skip",Color.black,Color.clear],["restore",Color.white,Color.black]])
					.action_({ arg butt; skipLengths[i] = butt.value });
			);

			// buffer lenght sliders
			lengthSliders.put(i,
				EZSlider(win, Rect(675, firstlineY+(gapY*i), 250, 20), "", ControlSpec(0.0001, 10, \lin),
					{ arg ez; 
						try { 
							buffLengths[i] = ez.value;	
							dirtyBuffers[i] = 1; //mark to change length next time it is recorded
						}  {"synths not initialised".postln};
					},
					1 // default value
				);
			);
		});



		win.front;
	}
}
