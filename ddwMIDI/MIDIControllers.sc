
// various midi controllers

VoicerMIDIController : AbstractMIDIControl {
		// when you free a voicer, maybe you don't want unassigned VMC's to die
		// you can set this to a dummy class that pretends to be active
	classvar <>defaultDest;

	controlType { ^\knob }
	
	init { arg switch;
		var buttontry;
		{	destination.midiControl = this;
			destination.displayNameSet;
			spec = destination.tryPerform(\spec).asSpec;
		}.try;
	}
	
		// this is part of the "free" process so I won't use defaultDest here
	clear {
		{	destination.midiControl = nil;
			destination.displayNameSet;
			spec = nil.asSpec;
		}.try;
	}

	set { arg value, divisor;
		(this.active and: { destination.notNil and: { destination != defaultDest } }).if({
			destination.set(destination.spec.map(value/(divisor?127)), resync:false);
		});
	}
	
	destValue { ^destination.value }
	
	spec { ^destination.spec }
	
	active { ^destination.active }
	
	name { ^destination.name }
	
	destination_ { arg dest;
		{	destination.midiControl = nil;	// break old connection
			destination.displayNameSet;
		}.try;
		destination = dest ? defaultDest;
		{	
			destination.midiControl = this;
			destination.displayNameSet;
			spec = destination.tryPerform(\spec).asSpec;
		}.try { |error|
			error.reportError;	// but don't stop
		};
		this.resync;
	}
	
	draggedIntoVoicerGCGUI { |newDest|
			// ignore if I am already in the destination
		(newDest.model.midiControl !== this).if({
				// unmap from old thing
			{ newDest.model.midiControl.destination_(defaultDest); }.try;
			this.destination_(newDest.model);
		});
	}
}

MixerMIDIControl : AbstractMIDIControl {
	
	controlType { ^\slider }
	
	init { arg spc;
		var buttontry;
		spec = spc.asSpec;
		ccnum.notNil.if({
			buttontry = ccnum.tryPerform(\buttonnum);	// if your controller has buttons like mine
			buttontry.notNil.if({
				MixerMIDIMute(parent.channel, buttontry, destination);
			});
			destination.midiControl = this;
			spec = ControlSpec(0, 1, \amp);
			^this
		});
		^nil		// if no controller was available, don't fake it
	}
	
	destValue { ^destination.level }
	
	set { arg value, divisor;
		destination.setControl(\level, spec.map(value/(divisor?127)), resync:false);
	}
	
	name { ^destination.name }
	
}

MixerMIDIMute : AbstractMIDIControl {	
	set {
		destination.mute;	// auto toggles between muted and unmuted states
	}
}

SelectorMIDIControl : AbstractMIDIControl {
	var <divisor = 2, oldValue = 0, moved = 0;
		
	controlType { ^\encoder }

	init { arg div;
		divisor = div ? 2;	// value from encoder will be divided by this, for more coarse control
		spec = #[0, 127, \linear, 1].asSpec;
		ccnum.notNil.if({
			^this
		}, {
			^nil		// if no controller was available, don't fake it
		});
	}
	
	set { arg value;
		var addToViewValue = 0;
//		process.value = (value/divisor).asInteger;	// old, absolute value -> process

		moved = moved + ((value-oldValue).isStrictlyPositive.if(1,
			{ (value-oldValue).isNegative.if(-1, 0) }));

		oldValue = value;

		(moved.abs >= divisor).if({		// must move at least 'divisor' steps
			addToViewValue = moved.isPositive.binaryValue * 2 - 1;   // 1 -> 1, 0 -> -1
		});

		((addToViewValue != 0) /* and: { destination.view.notNil } */).if({  // if we changed, update gui
			moved = 0;	// start moved fresh
			{ destination.value = destination.value + addToViewValue }.defer;
		});
	}
	
	name { ^destination.voicer.name }
	
}

VPGMIDIControl : SelectorMIDIControl {

	init { arg div, button;
		var buttontry;
		super.init(div);
			// can we make a button control?
		buttontry = ccnum.tryPerform(\buttonnum);
		(button.notNil || buttontry.notNil).if({
			VPGButtonControl(parent.channel,
				button ??	{ ccnum.tryPerform(\buttonnum) },
				destination);
		});
	}

}

VPGButtonControl : VPGMIDIControl {

	init {
		spec = [0, 127, \linear, 1].asSpec;
		ccnum.notNil.if({
			^this
		}, {
			^nil		// if no controller was available, don't fake it
		});
	}
	
	set { arg value;
		destination.doAction;
	}
}

MIDIRecControl : AbstractMIDIControl {
		// activates and deactivates recording

	set {
		destination.recorder.isNil.if({		// if no recorder, time to start
			destination.initRecord;
		}, {
				// else, reset for next time
			destination.stopRecord;
		});
		destination.view.tryPerform(\refresh, this);
	}
}


BufManagerMIDIControl : SelectorMIDIControl {
	// allows you to record midi note streams into multiple buffers (client-side)
	// controllers are ignored at present, maybe I'll add that later
	// since this uses MIDI controllers for start/stop, it's in the MIDI hierarchy as a controller

	var	trigger;

	controlType { ^\encoder }
	
	init { arg div = 1;
		super.init(div);
			// must have a valid ccontrol with a button
		(ccnum.tryPerform(\buttonnum).notNil).if({
			trigger = MIDIRecControl(parent, ccnum.buttonnum, destination);
		}, {
			"MIDIBufManager-init: CControl is not valid. The CControl must have a button."
				.die(ccnum)
		});
	}
	
	clear {
		trigger.free;
		trigger = nil;
	}
	
	active { ^destination.active }
}
