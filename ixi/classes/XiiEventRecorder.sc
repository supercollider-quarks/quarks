


/*

w = Window.new("w", Rect(10,10, 600, 600)).front;
a = XiiEventRecorder(s, 1, w);

// next to do: instead of diskin, make an array of small buffers
// buffer recording starts when onset detected and stopped when off. then put into array.



SynthDef(\xiiEventAnalyser, {arg out=122, fftbuf, inbus=8, prerectime=0.01, onsetsensitivity=1.2, trailsensitivity=0.09, sustaintime=0.26;
	var in, signal, chain, onsettrig, trigger, gate, env;
	//var a;

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

o.remove; o = OSCresponder(s.addr, '/onset', { |t, r, msg| msg.postln }).add;
//r.remove; r = OSCresponder(s.addr, '/trail', { |t, r, msg| msg.postln }).add;
q.remove; q = OSCresponder(s.addr, '/xiidone', { |t, r, msg| msg.postln }).add;


b = Buffer.alloc(s, 512);

a = Synth(\xiiEventAnalyser, [\fftbuf, b, \recbuf, c])

c.close;
c.free;


a.set(\onsetsensitivity, 1.2)
a.set(\trailsensitivity, 0.09)


XiiEventRecorder(s)

*/


XiiEventRecorder {	
	
	var channels;
	var <>xiigui;
	var <>win, params;
	
	*new { arg server, channels=1, setting=nil;
		^super.new.initXiiEventRecorder(server, channels, setting);
	}
		
	initXiiEventRecorder {arg server, ch, setting;
		var endFunc, onsetsens, trailsens;
		var analyser, recording;
		var endRecTimer, sustaintime;
		var prerec, postrec;
		var recbang, recButt, renderButt;
		var recsynth, inBusPop, outputFile;
		var group, filepath, buffer;
		var oscOnset, oscEnd;
		var inbus, onsetsensitivity, trailsensitivity, prerectime;
		var fftbuf, trackbuf;
		var startEventTime, eventsTimeList;
		var point, name;
		
		channels = 1; // ch I might use the ch argument if I decide to make a stereo option
		name = "- eventrecorder -";
		
		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		params = if(setting.isNil, { [8, 1.0, 0.08, 0.04, 0.33] }, {setting[2]});
		
		inbus =  params[0] ;
		recording = false;
		endRecTimer = 0 ;
		onsetsensitivity = params[1];
		trailsensitivity = params[2];
		prerectime = params[3];
		sustaintime = params[4];
		startEventTime = Main.elapsedTime;
		eventsTimeList = List.new.add(0);
		
		group = Group.new;
		filepath = "sounds/ixiquarks/EventRecorder.aif";
		
		buffer = Buffer.alloc(server, 65536, 1);
		fftbuf = Buffer.alloc(server, 512);
		//trackbuf = Buffer.alloc(server, 512);

		win = Window.new("EventRecorder", Rect(point.x, point.y, 200, 160), resizable:false);

		analyser = Synth(\xiiEventAnalyser, [											\inbus, inbus,
								\fftbuf, fftbuf,
								\onsetsensitivity, onsetsensitivity, 
								\trailsensitivity, trailsensitivity, 
								\prerectime, prerectime,
								\sustaintime, sustaintime], 
								target: group, addAction:\addToHead);
		
		recbang = Bang.new(win, Rect(10, 10, 60, 60))
				.setBackground_(Color.white)
				.setFillColor_(Color.yellow(alpha:0.5));
					
		recButt = Button.new(win, Rect(10, 77, 60, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.states_([["start rec",Color.black,Color.clear], 
						["stop rec",Color.black,Color.green(alpha:0.2)]])
				.action_({arg butt;
					
					if(butt.value == 1, {
						recbang.setFillColor_(Color.red(alpha:0.5));
						startEventTime = Main.elapsedTime;
						eventsTimeList = List.new.add(0);

						recording = true;
												
						recsynth = Synth(\xiiEventRecorder, 
										["bufnum", buffer.bufnum], 
										target:group,
										addAction:\addToTail);
						recsynth.run(false);
						buffer.write(filepath, "aiff", XQ.pref.bitDepth, 0, 0, true);
					}, {
						recbang.setFillColor_(Color.yellow(alpha:0.5));
						//analyser.free;
						recsynth.free;
						recbang.setState_(false);
						//sensibang.setState_(false);
						recording = false;
						buffer.close;// close the recording file
					});
				 });

		inBusPop = PopUpMenu.new(win, Rect(10, 100, 60, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_( XiiACDropDownChannels.getMonoChnList )
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup; var inbus;
					inbus = if(channels==2, { popup.value*2 }, { popup.value });
					analyser.set(\inbus, inbus);
					params[0] = inbus;
				})
				.value_( inbus/channels );

		
		onsetsens = OSCIISlider.new(win, Rect(80, 10, 100, 10), "- onset", 0.01, 3, params[1], 0.01, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.value_(onsetsensitivity)
			.action_({arg sl; 
				analyser.set(\onsetsensitivity, sl.value);
				onsetsensitivity = sl.value;
				params[1] = sl.value;
			});


		trailsens = OSCIISlider.new(win, Rect(80, 40, 100, 10), "- trail", 0.001, 0.4, params[2], 0.001, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.value_(trailsensitivity)
			.action_({arg sl; 
				analyser.set(\trailsensitivity, sl.value);
				trailsensitivity = sl.value;
				params[2] = sl.value;
			});

			
		prerec = OSCIISlider.new(win, Rect(80, 70, 100, 10), "- prerec", 0.001, 0.3, params[3], 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.value_(prerectime)
			.action_({arg sl; 
				prerectime = sl.value;
				analyser.set(\prerectime, sl.value);
				params[3] = sl.value;
			});
			
		postrec = OSCIISlider.new(win, Rect(80, 100, 100, 10), "- sustaintime", 0.1, 1, params[4], 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.value_(sustaintime)
			.action_({arg sl; 
				sustaintime = sl.value;
				analyser.set(\sustaintime, sl.value);
				params[4] = sustaintime;
			});

					
		renderButt = Button.new(win, Rect(80, 130, 102, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.canFocus_(false)
				.states_([["bounce to bufferpool",Color.black,Color.clear]])
				.action_({arg butt;
					var cond, filepatharray, filepath, x;
					cond = Condition.new;
					if(eventsTimeList.size < 2, {
						XiiAlertSheet(win, Rect(0, 0, 240, 120), "You need to record a few events first");
					}, {
						recButt.valueAction_(0);
						filepatharray = [];
						// check if the bounce folder exists
						if(File.exists("sounds/ixiquarks/EventRecorder").not, {
							"mkdir sounds/ixiquarks/EventRecorder".unixCmd;
						});
						// create the files;
						Routine.run({
							(eventsTimeList.size-1).do({ arg i; // the times of onsets
								var b;
								b = Buffer.read(server, "sounds/ixiquarks/EventRecorder.aif", 
											eventsTimeList[i]*server.sampleRate, 
											(eventsTimeList[i+1]-eventsTimeList[i])*server.sampleRate );
								server.sync(cond);
								filepath = "sounds/ixiquarks/EventRecorder/Event_"++(i+1)++".aif";
								filepatharray = filepatharray.add(filepath);
								b.write(filepath, "AIFF", XQ.pref.bitDepth);
								server.sync(cond);
								b.free;
							});
							server.sync(cond);
							
							{
								x = XiiBufferPool.new(server);
								XQ.globalWidgetList.add(x); // add the pool to the registry 
								x.loadBuffers(filepatharray)
							}.defer;
						});
					});
				 });

		// responder to trigger synth

		oscOnset = OSCresponderNode(server.addr, '/onset', { arg time, responder, msg;
				startEventTime = Main.elapsedTime;
				{recbang.setState_(true)}.defer;
				if(recording, { recsynth.run(true) });
		}).add;
	
		oscEnd = OSCresponderNode(server.addr, '/xiidone', { arg time, responder, msg;
			{recbang.setState_(false)}.defer; 
			if(recording, {
				recsynth.run(false);
				eventsTimeList.add(eventsTimeList.last+(Main.elapsedTime-startEventTime));
				eventsTimeList.postln;
				//"END of Sound Event -------------".postln;
			});
		}).add;

		endFunc = { 	
					recButt.valueAction_(0);
					recsynth.free; 
					analyser.free; 
					buffer.close; 
					buffer.free; 
					oscEnd.remove; 
					oscOnset.remove;
				};

		recButt.focus(true);
		
		win.onClose_({
			var t;
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
			endFunc.value;
		});
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params]; // channels, point, params
	}

	
}