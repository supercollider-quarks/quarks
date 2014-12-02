
TempoClockGui : ObjectGui {
		// displays current status of a tempoclock; updates every beat
	classvar 		namewidth = 100, nameheight = 18,
				numheight = 40, numwidth = 70,
				height = 50, width = 600,
				spec;

	var	<w, <name, <namev, <bars, <beats, updater;	// counter guis
	var	<mainFlow, <tempoEditor, <tempoFlow, <mainLayout, <tempoEditGui;
	var	<serverMenu, <serverNames, <currentServer, <metroButton, <levelSlider, <>metroLevel,
		<>latency = 0,
		<metro;
	
	*initClass {
		StartUp.add({ spec = ControlSpec(0, 0.74, \amp) });
	}
	
	gui { arg lay, bounds ... args;	// must do some things felix doesn't
		var layout;
		mainLayout = layout = this.guify(lay,bounds);	// like save my mainLayout (the MPLayout)
		layout.flow({ arg layout;
			view = layout;
			this.writeName(layout);
			this.performList(\guiBody,[layout] ++ args);
		},bounds).background_(this.background);
		//if you created it, front it
		if(lay.isNil,{ layout.front });
	}

	guify { arg lay, bounds, title;
		
		lay.isNil.if({	// if no window given...
				// use the previously opened TempoClock window or make a new one if needed
			lay = w ?? { w = ResizeFlowWindow.new("TempoClock", 
				Rect(0, 20, width + 5, height))
			};
		}, {
			mainLayout = lay;
			lay = lay.asPageLayout(title,bounds);
		});
		lay.removeOnClose(this);
		^lay
	}

	guiBody { arg lay, n;
		
		namev.isNil.if({	// only make views if we don't already have them
			name = n ? name ? "";

			mainFlow = FlowView(lay, Rect.new(0, 0, width, height), margin: 2@2);
			tempoFlow = FlowView(mainFlow, Rect(0, 0, 350, height), margin: 2@2);

			namev = GUI.staticText.new(tempoFlow, Rect.new(0, 0, namewidth, nameheight))
				.align_(\center);

			tempoEditor = NumberEditor.new(model.tempo*60, [20, 320, \linear, 1]);
			tempoEditGui = tempoEditor.gui(tempoFlow);
			tempoEditor.action = { arg v; model.tempo_(v/60) };

			tempoFlow.startRow;

			serverNames = Server.named.keys.asArray;
			this.currentServer = Server.default;
			serverMenu = GUI.popUpMenu.new(tempoFlow, Rect(0, 0, namewidth, nameheight))
				.items_(serverNames)
				.action_({ |view|
					this.currentServer = Server.named[serverNames[view.value]];
					this.latency = currentServer.latency;
				})
				.value_(serverNames.indexOfEqual(currentServer.name));
			metroButton = GUI.button.new(tempoFlow, Rect(0, 0, 40, nameheight))
				.states_([["takt"], ["shh"]])
				.action_({ |view|
					this.runMetronome(view.value > 0);
				});
			metroLevel = spec.map(1);
			levelSlider = GUI.slider.new(tempoFlow, Rect(0, 0, 100, nameheight))
				.value_(metroLevel)
				.background_(tempoEditGui.background)
				.action_({ |view|
					metroLevel = spec.map(view.value);
					if(metro.notNil) {
						metro.amp = metroLevel;
					};
				});
			
			tempoFlow.resizeToFit(false, false);
			
			this.makeCounter;	// make the bars and beats views
		});

			// fix window
		mainLayout.recursiveResize;
		this.update;	// set initial display value
		namev.string_(name);

		updater.isNil.if({
			updater = Routine.new({ 	// routine to update every beat
				var	waitTime;
				{ model.isRunning }.while({
					this.updateCounter;
					if(metro.notNil and: { metro.synth.notNil }) {
						currentServer.listSendBundle(latency, [
							#[error, -1],	// prevent node not found when metro stops
							metro.synth.setMsg(\t_trig, metroLevel)
						]);
					};
					if((waitTime = min(1, model.nextBar - model.beats)) == 0) {
						min(1, model.beatsPerBar).wait
					} {
						waitTime.wait;
					};
				});
			});
				// start it running on the next beat
			model.schedAbs(model.elapsedBeats.ceil, updater);
		});
	}
	
	makeCounter {
		var	font = GUI.font.new("Helvetica", 24).boldVariant;
		bars = GUI.numberBox.new(mainFlow, Rect.new(0, 0, numwidth, numheight))
			.font_(font)
			.align_(\right)
			.stringColor_(Color.new255(157, 63, 145));
		beats = GUI.numberBox.new(mainFlow, Rect.new(0, 0, numwidth, numheight))
			.font_(font)
			.align_(\right)
			.stringColor_(Color.new255(157, 63, 145));
	}		
	
	update { arg obj, changer;
		(changer.isView).if({
			model.tempo_(tempoEditor.value / 60);
		}, {
			switch(changer)
				{ \tempo } { tempoEditor.value_(model.tempo*60).changed }
				{ \stop } { this.remove }
				{ this.updateCounter }
		});
	}
	
	updateCounter {
		var	barnum;
		(bars.notNil).if({
			{	if(model.isRunning and: { view.notClosed }) {
					bars.value = (barnum = model.bar);
					beats.value = (model.beats - model.bars2beats(barnum)).trunc;
				};
			}.defer(latency);
		});
	}
	
	remove {
		updater.stop;
		model.removeDependant(this);
		this.runMetronome(false);

		view.notClosed.if({
			view.remove;
			mainLayout.recursiveResize;
		});

		namev = bars = beats = updater = nil;
	}
	
	currentServer_ { |server|
		var	metroRunning = (metro.notNil);
		if(server !== currentServer) {
			this.runMetronome(false);
			currentServer = server;
			latency = server.latency;
			if(metroRunning) { this.runMetronome(true) };
		}
	}
	
	runMetronome { |on = true|
		if(on and: { metro.isNil }) {
			if(currentServer.serverRunning) {
				metro = DDWMetronome(model, currentServer, spec.map(metroLevel),
					run: false);
				defer { metroButton.tryPerform(\value_, 1) };
			} {
				"Server % is not running - can't start metronome.".format(currentServer.name).warn;
				{ metroButton.tryPerform(\value_, 0) }.defer;
			};
		} {
			metro.free;
			metro = nil;
			defer { metroButton.tryPerform(\value_, 0) };
		};
	}
}

DDWMetronome {
	var	<clock,
		<server, <group, <bus, <numChannels, <synthdef, <synth, <>amp,
		<aliveThread;
	*new { |clock, target, amp = 1, run = true|
		^super.new.init(clock, target, amp, run)
	}

	init { |clock, target, level, run|
		var	groupbus;
		if((groupbus = target.tryPerform(\groupBusInfo)).notNil) {
			#group, bus = groupbus;
			numChannels = target.inChannels;
		} {
			group = target.asTarget;
			bus = 0;
			numChannels = group.server.options.numOutputBusChannels;
		};
		server = group.server;
		fork {
			synthdef = this.makeSynthDef(numChannels).send(server);
			server.sync;
			amp = level;
			this.play(run ? true);
		};
	}

		// runthread may be false if the owner of this object will run its own thread
	play { |runThread = true|
		if(synth.isNil) {
			synth = Synth(synthdef.name, [\i_out, bus], target: group);
			if(runThread) {
				aliveThread = Routine({
					loop {
						server.sendBundle(server.latency, synth.setMsg(\t_trig, amp));
						1.0.wait;
					}
				}).play(clock, 1.0)
			}
		}
	}

	stop {
		aliveThread.stop;
		synth.free;
		synth = nil;
	}

	free {
		this.stop;
	}

	makeSynthDef { |numChannels = 2|
		^SynthDef("ddwmetro" ++ numChannels, { |t_trig = 0, i_out = 0|
			var	sig;
			sig = Klank.ar(`[ [ 403.06749750304, 2947.8690441738, 992.38941400907 ],
					  [ 0.15740193350554, 1, 0.63826208002513 ],
					  [ 0.017389599589788, 0.0032695745154834, 0.0098681694619578 ] ],
					K2A.ar(t_trig));
			Out.ar(i_out, sig ! numChannels)
		});
	}
}    
