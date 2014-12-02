

EventLoopGui : JITGui {

	classvar <defaultSpecs;

	var <taskGui;
	var <loopBut, <revBut;
	var <listText, <indexBox;
	// <invBut, <sclBut;
	// var <scalerSl, <shiftSl;

	*initClass {
		defaultSpecs = (
			\tempo: [0.1, 10, \exp],
			\lpStart: [0, 1],
			\range: [0, 1],
			\jitter: [0, 1, \amp]
		);
	}

	accepts { |obj| ^(obj.isNil) or: { obj.isKindOf(EventLoop) }; }

	setDefaults { |options|
		defPos = 10@260;
		minSize = 270 @ (skin.buttonHeight * 6 + 10);
		if (parent.notNil) { skin = skin.copy.put(\margin, 0@0) };
	//	"% - minSize: %\n".postf(this.class, minSize);
	}

	makeViews { |options|

		var height = skin.buttonHeight;
		var lineWidth = zone.bounds.width - (skin.margin.y * 4);
		var zoneMargin = if ( (numItems > 0) or:
			{ parent.isKindOf(Window.implClass) }) { skin.margin } { 0@0 };

		zone.decorator = FlowLayout(zone.bounds, zoneMargin, skin.gap);
		zone.resize_(2);

		this.makeTaskGui(lineWidth, height);

		this.makeLoopButtons(lineWidth, height);
		zone.decorator.shift(10, 0);
		this.makeListControls(lineWidth, height);

		this.checkUpdate;
	}

	object_ { |obj|
		super.object_(obj);
		if (obj == object) {
			if (object.notNil) {
				taskGui.object_(object.task);
				taskGui.name_(object.key);
				this.name_(object.key);
			} {
				taskGui.object_(nil);
			};
		};
		defer ({ this.checkUpdate }, 0.05);
	}

	makeTaskGui { |lineWidth, height|
		taskGui = TdefGui(nil, 4, zone, lineWidth@height, makeSkip: false);
		taskGui.envirGui.specs.putAll(defaultSpecs);
		taskGui.envirGui.showNewNames_(false);

		// repurpose two right buttons:
		taskGui.envBut.states_([
			["rec", Color.black, skin.offColor],
			["rec", Color.white, Color.red]
		]).action_({ object.toggleRec });
		taskGui.envBut.value_(0);

		taskGui.srcBut.states_([
			["info", Color.black, skin.offColor]
		]).action_({ "not done yet".postln });
	}

	makeListControls { |lineWidth, height|
		indexBox = EZNumber(zone, (lineWidth * 0.15)@height, \i,
			[0, 99, \lin, 1].asSpec, { |b| object.setList(b.value.asInteger) },
			0, labelWidth: 10);
		indexBox.labelView.align_(\center);
		listText = StaticText(zone, (lineWidth * 0.15)@height).align_(\center);
		this.setNumLists(0);
	}

	setNumLists { |num = 0|
		listText.string_("of" + num.asString);
		indexBox.controlSpec.maxval_(num - 1);
	}

	makeLoopButtons { |lineWidth, height|

		Button(zone, Rect(0,0, lineWidth * 0.2, height))
		    .font_(font)
			.states_([["resetLp", skin.fontColor, skin.offColor]])
			.action_({ |but| object.resetLoop });

			// Button(zone, Rect(0,0, lineWidth * 0.27 - 1, height))
			// .font_(font).resize_(3)
			// .states_([["rsScale", skin.fontColor, skin.offColor]])
			// .action_({ |but| object.resetLoop });

		zone.decorator.shift(5, 0);

		loopBut = Button(zone, Rect(0,0, lineWidth * 0.13, height))
			.font_(font)
			.states_([
				["once", skin.fontColor, skin.offColor],
				["loop", skin.fontColor, skin.onColor]
			])
			.action_({ |but| object.toggleLooped; });

		revBut = Button(zone, Rect(0,0, lineWidth * 0.13, height))
			.font_(font)
			.states_([
				["fwd", skin.fontColor, skin.offColor],
				["rev", skin.fontColor, skin.onColor]
			])
			.action_({ |but|
				object.flip;
				but.value_(object.isReversed.binaryValue);
			});
	}

	getState {
		if (object.isNil) {
			^(object: nil, name: " ", isPlaying: false, isRecording: false,
			reverse: false, inverse: false, rescaled: false);
		};

		^(
			object: object,
			name: object.key,
			looped: object.looped.binaryValue,
			isRecording: object.isRecording.binaryValue,
			isReversed: object.isReversed.binaryValue,
			numLists: object.lists.size,
			listIndex: object.lists.indexOf(object.list) ? 0;
		);
	}

	checkUpdate {
		var newState = this.getState;
		var playState;


		// if (newState == prevState) {
		// 	taskGui.checkUpdate;
		// 	prevState = newState;
		// 	"no change.".postln;
		// 	"prevState: %\n".postf(prevState);
		// 	"newState: %\n".postf(newState);
		// 	^this
		// };

		if (newState[\object].isNil) {
			//	"no object.".postln;
			prevState = newState;
			zone.visible_(false);
			^this;
		};

		if (newState[\object] != prevState[\object]) {
			zone.visible_(object.notNil);
		};

		if (newState[\name] != prevState[\name]) {
			zone.visible_(true);
			taskGui.name_(newState[\name]);
		//	this.parent.name_(object.asString);

			// taskproxygui should do the name buttons
			taskGui.nameBut.states_(
				taskGui.nameBut.states.collect(_.put(0, object.key.asString))
			).refresh;
		};

		taskGui.checkUpdate;

		// dont check, so we always overwrite taskGui button state
		taskGui.envBut.value_(newState[\isRecording]).refresh;

		if (newState[\looped] != prevState[\looped]) {
			loopBut.value_(newState[\looped]).refresh;
		};

		if (newState[\isReversed] != prevState[\isReversed]) {
			revBut.value_(newState[\isReversed]).refresh;
		};

		if (newState[\isInverse] != prevState[\isInverse]) {
			revBut.value_(newState[\isInverse]).refresh;
		};

		if (newState[\numLists] != prevState[\numLists]) {
			this.setNumLists(newState[\numLists] ? "-");
		};

		if (newState[\listIndex] != prevState[\listIndex]) {
			indexBox.value_(newState[\listIndex]);
		};

		prevState = newState.copy;
	}
}
