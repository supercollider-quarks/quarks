
RecordProxyMixer2 {
	var <>proxymixer, bounds;
	var <recorder, <recType=\mix;
	var <>recHeaderFormat, <>recSampleFormat, <preparedForRecording=false;
	var skipjack, <display;

	*initClass {
		Class.initClassTree(GUI);
		GUI.skins.put(\jitSmall, (
				fontSpecs: 	["Helvetica", 10],
				fontColor: 	Color.black,
				background: 	Color(0.8, 0.85, 0.7, 0.5),
				foreground:	Color.grey(0.95),
				onColor:		Color(0.5, 1, 0.5),
				offColor:		Color.grey(0.8, 0.5),
				gap:			0 @ 0,
				margin: 		2@2,
				buttonHeight:	16
			)
		);
	}

	*new { arg proxymixer, bounds;
		^super.newCopyArgs(proxymixer, bounds).initDefaults.makeWindow
	}
	initDefaults {
		recHeaderFormat = recHeaderFormat ?? { this.server.recHeaderFormat };
		recSampleFormat = recSampleFormat ?? { this.server.recSampleFormat };
	}

	prepareForRecord {
		var numChannels=0, path, proxies, func;
		proxies = this.selectedKeys.collect { arg key; this.at(key) };
		if(proxies.isEmpty) { "NodeProxyEditor: no proxies.".postln; ^nil };


		if(recType == \multichannel)  {
			proxies.do {  |el| numChannels = numChannels + el.numChannels };
			func = { proxies.collect(_.ar).flat }; // no vol for now!
		} {
			proxies.do {  |el| numChannels = max(numChannels, el.numChannels) };
			func = { var sum=0;
				proxies.do { |el| sum = sum +.s el.ar };
				sum
			}
		};

		path = thisProcess.platform.recordingsDir +/+ "SC_PX_" ++ numChannels ++ "_" ++ Date.localtime.stamp ++ "." ++ recHeaderFormat;
		recorder = RecNodeProxy.audio(this.server, numChannels);
		recorder.source = func;
		recorder.open(path, recHeaderFormat, recSampleFormat);
		preparedForRecording = true;
		^numChannels
	}

	font { ^GUI.font.new(*GUI.skins[\jitSmall].fontSpecs) }
	skin { ^GUI.skins[\jitSmall] }

	server { ^proxymixer.proxyspace.server }

	removeRecorder {
		recorder !? {
			recorder.close;
			recorder.clear;
			recorder = nil;
		};
		preparedForRecording = false;
	}

	record { arg paused=false;
		if(recorder.notNil) {recorder.record(paused) };
	}

	pauseRecorder { recorder !? {recorder.pause} }
	unpauseRecorder {
		recorder !? { if(recorder.isRecording) { recorder.unpause } { "not recording".postln } }
	}

	selectedKeys {  ^proxymixer.selectedKeys }
	at { arg key; ^proxymixer.proxyspace.envir.at(key) }
	selectedKeysValues {
		^this.selectedKeys.collect { |key|
					var proxy = this.at(key);
					[ key, proxy.numChannels ]
		}.flat;
	}

	makeWindow {
		var rw, recbut, pbut, numChannels, recTypeChoice;
		var font = this.font;

		rw = Window("recording:" + proxymixer.title, bounds);
		rw.view.decorator = FlowLayout(rw.view.bounds.insetBy(20, 20));
		rw.onClose = { this.removeRecorder; this.stopUpdate; };
		rw.view.background = this.skin.background;

		recbut = Button(rw, Rect(0, 0, 80, 20)).states_([
			["prepare rec", this.skin.fontcolor, this.skin.offColor],
			["record >", Color.red, Color.gray(0.1)],
			["stop []", this.skin.fontcolor, Color.red]
		]).action_({|b|
			var list;
			switch(b.value,
				1, {
					numChannels = this.prepareForRecord;
					if(numChannels.isNil) { b.value = 0; pbut.value = 0 }
				},
				2, { this.record(pbut.value == 1) },
				0, { this.removeRecorder  }
			);
			if(b.value == 1 and: { numChannels.notNil }) {
				list = this.selectedKeysValues;
				this.displayString = format("recording % channels: %", numChannels, list.join(" "));
			};

		}).font_(font);

		pbut = Button(rw, Rect(0, 0, 80, 20)).states_([
			["pause", this.skin.fontcolor, this.skin.offColor],
			[">", Color.red, Color.gray(0.1)]
		]).action_({|b|
			if(b.value == 1) {  this.pauseRecorder } { this.unpauseRecorder }

		}).font_(font);

		recTypeChoice = PopUpMenu(rw, Rect(0, 0, 110, 20))
				.items_([ \mix, \multichannel ])
				.action_({ arg view;
					recType = view.items[view.value];
					if(recbut.value != 0) { recbut.valueAction = 0 }
				})
				.font_(font);
		recTypeChoice.value = 1;
		Button(rw, Rect(0, 0, 60, 20))
				.states_([["cancel", this.skin.fontcolor, this.skin.offColor]])
				.action_({ if(recbut.value != 0) { recbut.valueAction = 0 } })
				.font_(font);

		Button(rw, Rect(0, 0, 60, 20))
				.states_([["open recDir", this.skin.fontcolor, this.skin.offColor]])
				.action_({
					unixCmd("open" + quote(thisProcess.platform.recordingsDir));
				})
				.font_(font);


		rw.view.decorator.nextLine;
		display = StaticText(rw, Rect(30, 40, 300, 20)).font_(font);
		rw.front;
		this.makeSkipJack;
		this.runUpdate;

		^rw
	}

	displayString_ { arg str;
		display.string = str;
	}

	updateZones {
		if(preparedForRecording.not) {
			this.displayString = format("proxies: %", this.selectedKeysValues.join(" "));
		}
	}

	title {  ^"recorder for" + proxymixer.title }

	runUpdate { skipjack.start; }
	stopUpdate { skipjack.stop }
	makeSkipJack {				// stoptest should check window.
		skipjack = SkipJack({ this.updateZones }, 0.5, name: this.title);
	}
}