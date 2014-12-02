
CtLoopGui : JITGui { 
	var <nameBut, <playBut, <pauseBut, <recBut, <mapBut;
	var <tempoSl, <startSl, <lengthSl, <jitterSl;
	var <revBut, <invBut, <sclBut;
	var <scalerSl, <shiftSl;
	
	accepts { |obj| ^(obj.isNil) or: { obj.isKindOf(CtLoop) }; }

	setDefaults { |options|
		defPos = 10@260;
		minSize = 210 @ (skin.buttonHeight * 8 + 10);
		if (parent.notNil) { skin = skin.copy.put(\margin, 0@0) };
	//	"% - minSize: %\n".postf(this.class, minSize);
	}
	
	makeViews { |options|
		var height = skin.buttonHeight;
		var lineWidth = zone.bounds.width - (skin.margin.y * 2);
		var width = lineWidth * 0.62 / 4;
		var nameWidth = lineWidth * 0.38 - 1;
		var zoneMargin = if ( (numItems > 0) or: { parent.isKindOf(Window.implClass) }) { skin.margin } { 0@0 };
		
		zone.decorator = FlowLayout(zone.bounds, zoneMargin, skin.gap);

		nameBut = Button(zone, Rect(0,0, nameWidth, height))
			.font_(font)
			.resize_(2)
			.states_([
				[" ", skin.fontColor, skin.onColor]
			])
//			.keyDownAction_({ |btn, char| 
//				char.postcs;
//				if (char.ascii == 127) {
//					object.clear;
//					object.class.all.removeAt(btn.states.first.first.asSymbol);
//					 object = nil;
//				};
//			})
		;

		playBut = Button(zone, Rect(0,0, width, height))
			.font_(font)
			.resize_(3)
			.states_([
				[" >", skin.fontColor, skin.offColor],
				[" _", skin.fontColor, skin.onColor ],
				[" |", skin.fontColor, skin.offColor ]
			])
			.action_({ |but|
				[ { object.play }, { object.play }, { object.stop } ][but.value].value;
				this.checkUpdate;
			});

		pauseBut = Button(zone, Rect(0,0, width, height))
			.font_(font)
			.resize_(3)
			.states_([
				["paus", skin.fontColor, skin.onColor],
				["rsum", skin.fontColor, skin.offColor]
			])
			.action_({ |but| var string;
				[ { object.resume },{ object.pause } ][but.value].value;
				this.checkUpdate;
			});

		recBut = Button(zone, Rect(0,0, width, height))
			.font_(font)
			.resize_(3)
			.states_([
				["rec", skin.fontColor, skin.offColor],
				["stop", Color.white, Color.red]
			])
			.action_({ |but| 
				[ { object.stopRec }, { object.startRec } ][but.value].value;
			});

		mapBut = Button(zone, Rect(0,0, width, height))
			.font_(font)
			.resize_(3)
			.states_([
				["map", skin.fontColor, skin.offColor],
			])
			.action_({ |but| 
				EnvirGui(object.ctlMap).name_(object.key + "ctlMap");
			});
		
		tempoSl = EZSlider(zone, lineWidth@height, \tempo, [0.1, 10, \exp], 
			{ |sl| object.tempo = sl.value }, 1, labelWidth: 40);

		startSl = EZSlider(zone, lineWidth@height, \start, [0.0, 1.0], 
			{ |sl| object.start = sl.value }, 0, labelWidth: 40);

		lengthSl = EZSlider(zone, lineWidth@height, \length, [0.0, 1.0], 
			{ |sl| object.length = sl.value }, 1, labelWidth: 40);

		jitterSl = EZSlider(zone, lineWidth@height, \jitter, [0.0, 1, \amp], 
			{ |sl| object.jitter = sl.value }, 0, labelWidth: 40);
		

		zone.decorator.nextLine.shift(0, 5);

		Button(zone, Rect(0,0, lineWidth * 0.27 - 1, height))
			.font_(font).resize_(3)
			.states_([["rsLoop", skin.fontColor, skin.offColor]])
			.action_({ |but| object.resetLoop });

		Button(zone, Rect(0,0, lineWidth * 0.27 - 1, height))
			.font_(font).resize_(3)
			.states_([["rsScale", skin.fontColor, skin.offColor]])
			.action_({ |but| object.resetLoop });
				

		revBut = Button(zone, Rect(0,0, width, height))
			.font_(font)
			.resize_(3)
			.states_([
				["rev", skin.fontColor, skin.offColor],
				["fwd", skin.fontColor, skin.onColor]
			])
			.action_({ |but| 
				[ { object.reverse }, { object.forward } ][but.value].value;
			});

		invBut = Button(zone, Rect(0,0, width, height))
			.font_(font)
			.resize_(3)
			.states_([
				["inv", skin.fontColor, skin.offColor],
				["up", skin.fontColor, skin.onColor]
			])
			.action_({ |but| 
				[ { object.invert }, { object.up } ][but.value].value;
			});

		sclBut = Button(zone, Rect(0,0, width, height))
			.font_(font)
			.resize_(3)
			.states_([
				["rscl", skin.fontColor, skin.offColor],
				["norm", skin.fontColor, skin.onColor]
			])
			.action_({ |but| object.rescaled_(but.value > 0); });

		scalerSl = EZSlider(zone, lineWidth@height, \scale, [0.0, 4, \amp], 
			{ |sl| object.scaler = sl.value }, 1, labelWidth: 40);

		shiftSl = EZSlider(zone, lineWidth@height, \shift, [-0.5, 0.5], 
			{ |sl| object.shift = sl.value }, 0, labelWidth: 40);
		
	//	this.checkUpdate;
	}
	
	getState { 
		if (object.isNil) { 
			^(object: nil, name: " ", isPlaying: false, isRecording: false, 
			reverse: false, inverse: false, rescaled: false);
		};
		
		^(
			object: object, 
			name: object.key, 
			isPlaying: object.isPlaying.binaryValue,
			isActive: object.task.isActive.binaryValue,
			canPause: object.task.canPause.binaryValue,
			isPaused: object.task.isPaused.binaryValue,
			isRecording: object.isRecording.binaryValue, 
			
			isReversed: object.isReversed.binaryValue,
			isInverse: object.isInverse.binaryValue,
			rescaled: (object.scaler == 1).binaryValue, 
			
			tempo: object.tempo,
			start: object.start,
			length: object.length, 
			jitter: object.jitter,
			scaler: object.scaler,
			shift: object.shift
		);
	}
	checkUpdate {
		var newState = this.getState;
		var playState; 
		
		if (newState == prevState) { 
		//	"no change.".postln;
			^this 
		};

		if (newState[\object].isNil) {
		//	"no object.".postln;
			prevState = newState;
			zone.visible_(false);
			^this;
		};

		if (newState[\name] != prevState[\name]) {  // name
			zone.visible_(true);
			nameBut.states_(nameBut.states.collect(_.put(0, object.key.asString))).refresh;
		};

		playState = newState[\isPlaying] * 2 - newState[\isActive];
		newState.put(\playState, playState);

		if (playState != prevState[\playState]) {
				// stopped/playing/ended
				// 0 is stopped, 1 is active, 2 is playing but waiting:
			playBut.value_(playState).refresh;
		};

		if (newState[\canPause] != prevState[\canPause]) {
			pauseBut.visible_(newState[\canPause] > 0).refresh;
		};

		if (newState[\isPaused] != prevState[\isPaused]) {
			pauseBut.value_(newState[\isPaused]).refresh;
		};

		if (newState[\isRecording] != prevState[\isRecording]) {
			recBut.value_(newState[\isRecording]).refresh;
		};

		if (newState[\isReversed] != prevState[\isReversed]) {
			revBut.value_(newState[\isReversed]).refresh;
		};


		if (newState[\tempo] != prevState[\tempo]) {
			tempoSl.value_(newState[\tempo]);
		};

		if (newState[\start] != prevState[\start]) {
			startSl.value_(newState[\start]);
		};

		if (newState[\length] != prevState[\length]) {
			lengthSl.value_(newState[\length]);
		};

		if (newState[\jitter] != prevState[\jitter]) {
			jitterSl.value_(newState[\jitter]);
		};



		if (newState[\isInverse] != prevState[\isInverse]) {
			revBut.value_(newState[\isInverse]).refresh;
		};

		if (newState[\rescaled] != prevState[\rescaled]) {
			sclBut.value_(newState[\rescaled]).refresh;
		};


		if (newState[\scaler] != prevState[\scaler]) {
			scalerSl.value_(newState[\scaler]);
		};

		if (newState[\shift] != prevState[\shift]) {
			shiftSl.value_(newState[\shift]);
		};

		
		prevState = newState.copy;
	}
}

