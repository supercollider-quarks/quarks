/**
2006  Till Bovermann (IEM)


NOTES
-----
Imagine a Faderbox.
In general there are a fixed number of controls which behave in different ways.
there are e.g.
	Fader				(1 DOF)
	Knob					(1 DOF)
	Pad					(1 DOF)
	2D Fader (joystick)	(2 DOF)
	tangible rigid object	(6 DOF)
	pen on graphics-tablet	(5 DOF)
	...
this class is a general representation.
Subclasses implement the dedicated semantics.

	@todo values on server are currently set to be in [-1, 1]

	@ what about trigger buttons only sending that something's triggered?


*/
JInTController {
	var <numDOF;			/// number of available degrees of freedom
	var <description;		/// a String describing the functionality
	var <>short;			/// a shortcut, e.g. for a fader \f1
	var <>action;			/// a function which will be evaluated on a value change. param: this
	var <>beginAction;		/// a function evaluated on start of a continuous action. param: this
	var <>endAction;		/// a function evaluated on stop  of a continuous action. param: this
	
	var isRunning;
	var <nodeProxy;
	var <server;
	
	/** array of arrays containing symbols for each DOF:
	
			[values]
		\discrete		[discrete values]
		\continuous	[continuous values]

			[what happens if user lets go controller?]
		\snapBack		[snaps back into a predefined state]
		\sticky		[remains in last state]
		
		\autoOff		[automatically snaps back into default state after a given time]

			[are the values in a ring or not?]
		\range		[m .. n]
		\ring		[modulo ring]
		
			[active or passive?]
		\passive
		\settable
	*/
	var <semantics;	
	var <specs;			/// for each dimension one ControlSpec. nil: [0,1,'linear'].asSpec
	/**	
	 * array of raw controller values. 
	 * No specs assigned. 
	 * One for each dimension.
	 */
	var <rawVals;

	*new{|desc, server|
		^super.new.initJInTC(desc, server);
	}
	initJInTC {|desc, argServer|
		numDOF 		= 0;
		description 	= desc;
		semantics 	= [];
		specs 		= [];
		rawVals 		= [0];
		server 		= argServer ? Server.default;
		isRunning		= false;
	}
	/**
	 * @ToDo support arrays of indices as args...
	 */
	value{|i|
		i.notNil.if({
			^specs[i].unmap(rawVals[i]);
		}, {
			^specs.collect{|spec, i| 
				spec.unmap(rawVals[i])
			};
		})
	}
/* NOTES
		// init NodeProxies
		faderProxies = Array.fill(4, {
			NodeProxy(server).set(0, 0).source_(\control -> 0);
		});
*/
	set {|which = 0, val = 0|
		rawVals[which] = val;
		// set values to be in [-1, 1]
		action.value(this);
		nodeProxy !? {nodeProxy.set(which, this.value(which)*2-1);};
	}
	setWithoutProxy {|which = 0, val = 0|
		rawVals[which] = val;
		// set values to be in [-1, 1]
		action.value(this);
	}
	
	setAll {|vals|
		rawVals = vals;
		// set values to be in [-1, 1]
		action.value(this);
		nodeProxy !? {vals.do{|val, which| nodeProxy.set(which, this.value(which)*2-1)};};
	}
	beginCont {|which = 0, val = 0|
		beginAction.value(this);
		this.set(which, val);		
	}
	endCont {|which = 0, val = 0|
		endAction.value(this);
		this.set(which, val);		
	}
	start {
		this.initNodeProxy;
		isRunning = true;
	}
	stop {
		isRunning = false;
	}
	initNodeProxy {
		var n;
		nodeProxy = nodeProxy ?? {
			n = NodeProxy.control(server, numDOF);
			n.source_(\control -> (0!numDOF));
		};
	}
	/**
	 * 
	 */
	kr {|numChans|
		^nodeProxy.kr(numChans);
	}
	ar {|numChans|
		^nodeProxy.ar(numChans);
	}

}

JInTC_nState : JInTController{
	*new {|desc, server, numStates = 2|
		^super.new(desc ? format("simple one-DOF controller with % states", server, numStates)).initNState(numStates);
	}
	initNState{|numStates|
		numDOF 		= 1;
		semantics 	= [[\discrete]];
		specs 		= [[0, numStates-1, \linear, 1].asSpec];
		rawVals 		= [specs.first.default];
	}
}


/// simple one-DOF controller with on/off
JInTC_onoff : JInTC_nState{
	*new {|desc, server|
		^super.new(desc ? "simple one-DOF controller with on/off", 2);
	}
}

/// Button (on/off) with snap back ("Taster")
JInTC_Button : JInTC_onoff{
	*new {|desc, server, spec|
		^super.new(desc ? "Button (on/off)", server).initButton(spec);
	}
	initButton {|theSpec|
		semantics[0] = semantics[0].add(\snapBack);
		theSpec !? { specs = [theSpec];}
	}
}

/// Knocking on a surface
JInTC_Knock : JInTC_onoff{
	var <>audioInBus, <>sensitivity, <>dt;
	
	*new {|desc, server, spec, audioInBus = 0|
		^super.new(desc ? "Knocking on a surface (on; dt.wait; off)", server).initKnock(spec, audioInBus);
	}
	initKnock {|theSpec, argAudioInBus|
		audioInBus = argAudioInBus;
		semantics[0] = semantics[0].add(\snapBack);
		semantics[0] = semantics[0].add(\autoOff);
		theSpec !? { specs = [theSpec];}
	}
	initNodeProxy {
		var n;
		nodeProxy = nodeProxy ?? {
			n = NodeProxy(server);
			n.set(
				\audioInBus, audioInBus, 
				\sensitivity, sensitivity, 
				\dt, dt
			);
			n.source = {|audioInBus = 1, sensitivity = 0.025, dt = 0.1, trigID = 1000|
				var in, trig;
				in = AudioIn.ar(audioInBus).abs - sensitivity;
				//in.poll;
				trig = Trig.ar(
					Trig.ar(
						K2A.ar(in)>0, 
						dt
					), 
					(SampleRate.ir).reciprocal
				);
				SendTrig.ar(trig, trigID, trig);
				SendTrig.ar(DelayN.ar(trig, 1, dt), trigID, 0);
				trig!numDOF;
			};
			n;
		};
	}
}



/// simple one-DOF continuous controller
JInTC_linear : JInTController{
	*new {|desc, server, spec|
		^super.new(desc ? "simple one-DOF continuous controller", server).initLinear(spec);
	}
	initLinear{|spec|
		numDOF = 1;
		semantics = [[\continuous]];
		specs = spec.isNil.if({[[0, 1, \linear].asSpec]}, {[spec]});
		rawVals 		= [specs.first.default];
	}
}

/// simple one-DOF continuous controller, snapping back into default position
JInTC_linearSnapper : JInTC_linear{
	*new {|desc, server, spec|
		^super.new(desc ? "simple one-DOF continuous controller, snapping back into default position", server).initLinearSnapper(spec);
	}
	initLinearSnapper{|spec|
		semantics[0] = semantics[0] ++ [\snapBack];
		specs = spec.isNil.if({[ControlSpec(0, 1, default: 0.5)]}, {[spec]});
		rawVals 		= [specs.first.default];
	}
}


JInTC_PenPressure : JInTC_linearSnapper {
	*new {|desc, server, spec|
		^super.new(desc ? "pressure of a pen", server, spec ?? {ControlSpec(0, 1, default: 0)});
	}
}


/// simple one-DOF Fader
JInTC_Fader : JInTC_linear{
	*new {|desc, server, spec|
		^super.new(desc ? "simple one-DOF Fader", server, spec);
	}
}

/// simple knob to turn
JInTC_Knob : JInTC_linear{
	*new {|desc, server, spec| 
		^super.new(desc ? "simple knob to turn", server, spec);
	}
}

/// simple knob to turn
JInTC_EndlessKnob : JInTC_Knob{
	*new {|desc, server, spec| 
		^super.new(desc ? "simple knob to turn", server, spec).initEndlessKnob;
	}
	initEndlessKnob {
		semantics[0] = semantics[0].add(\ring);
	}
}

//////////////// n-DOF
/*
	I decided to not build trees of 1-DOF JInTController, since the definition of a controller 
	(see above) says, that a controller is something stick together, where it is near to 
	impossible to change one dimension without changing the others.
	Therefore it is not intendet to use one (of many) DOF for a single parameter.
*/

JInTC_composite : JInTController {
	*new {|desc, server, bunchOfJiNTController|
		^super.new(
			desc ? 
			"a composite of several controllers. should not be used (abstract class)",
			server
		).initComposite(bunchOfJiNTController);
	}
	initComposite {|conts, desc|
		// join semantics, specs and compute numDOF
		numDOF = 0;
		semantics = [];
		specs = [];
		rawVals = [];
		conts.do{|control, i|
			numDOF 		= numDOF 		+   control.numDOF;
			semantics 	= semantics 	++ control.semantics;
			specs 		= specs 		++ control.specs;
			rawVals 		= rawVals 		++ control.specs.collect(_.default);
		}
	}
}

/** as found on a PowerMate */
JInTC_PowerKnob : JInTC_composite {
	*new {|desc, server, specs|
		specs = specs ? [
			ControlSpec(0, 95, default: 0), 
			ControlSpec(0, 95, default: 0), 
			ControlSpec(0, 1, default: 0),
			ControlSpec(0, 1, default: 0)
		];
		^super.new(desc ? 
			"Just a big volume knob",
			server, 
			[
				JInTC_EndlessKnob.new(spec: specs[0]), // turning
				JInTC_EndlessKnob.new(spec: specs[1]), // down->turning
				JInTC_Button.new(spec: specs[2]),         // short press
				JInTC_Button.new(spec: specs[2])         // long press
			]
		);
	}
}

/** as found on logitech dual action */
JInTC_ThumbStick : JInTC_composite {
	*new {|desc, server, specs|
		specs = specs ? [ControlSpec(0, 1, default: 0.5), ControlSpec(0, 1, default: 0.5), ControlSpec(0, 1, default: 0)];
		^super.new(desc ? 
			"small two DOF analog joystick with an additional knob-functionality by pressing it. Normally actuated by thumb.",
			server, 
			[
				JInTC_linearSnapper.new(spec: specs[0]), // x
				JInTC_linearSnapper.new(spec: specs[1]), // y
				JInTC_Button.new(spec: specs[2])         // button
			]
		);
	}
}

/** as found on nunchuck; same as JInTC_ThumbStick, except for having no button */
JInTC_SimpleThumbStick : JInTC_composite {
	*new {|desc, server, specs|
		specs = specs ? [ControlSpec(0, 1, default: 0.5), ControlSpec(0, 1, default: 0.5)];
		^super.new(desc ? 
			"small two DOF analog joystick. Snap-back.",
			server, 
			[
				JInTC_linearSnapper.new(spec: specs[0]), // x
				JInTC_linearSnapper.new(spec: specs[1]), // y
			]
		);
	}
}

/** acceleration of objects */
JInTC_Acceleration : JInTC_composite {
	*new {|desc, server, specs|
		specs = specs ? [ControlSpec(0, 1, default: 0.5), ControlSpec(0, 1, default: 0.5), ControlSpec(0, 1, default: 0.5)];
		^super.new(desc ? 
			"Acceleration of an object(-part)",
			server, 
			[
				JInTC_linear.new(spec: specs[0]), // x
				JInTC_linear.new(spec: specs[0]), // y
				JInTC_linear.new(spec: specs[0])  // z
			]
		);
	}
}


/** a pressure sensitive touch-pad like it is found on sampling machines (MPC) */
JInTC_PPad : JInTC_composite {
	*new {|desc, server, specs|
		specs = specs ? [ControlSpec(0, 1, default: 0), ControlSpec(0, 1, default: 0)];
		^super.new(desc ? 
			"a pressure sensitive touch-pad like it is found on sampling machines (MPC)",
			server, 
			[
				JInTC_linearSnapper.new(spec: specs[0]), // trigger
				JInTC_linearSnapper.new(spec: specs[1])  // aftertouch
			]
		);
	}
	
}

JInTC_PenPos : JInTC_composite {
	*new {|desc, server, specs|
		specs = specs ? [
			ControlSpec(0, 1, default: 0), // posX
			ControlSpec(0, 1, default: 0) // posX
		];
		^super.new(desc ? 
			"position of a pen.",
			server, 
			[
				JInTC_linear.new(spec: specs[0]), // posX
				JInTC_linear.new(spec: specs[1])  // posY
			]
		);
	}
	
}

JInTC_PenTilt : JInTC_composite {
	*new {|desc, server, specs|
		specs = specs ? [
			ControlSpec(-1, 1, default: 0), // tiltX
			ControlSpec(-1, 1, default: 0)  // tiltY
		];
		^super.new(desc ? 
			"Tilting of a pen.",
			server, 
			[
				JInTC_linear.new(spec: specs[0]),  // tiltX
				JInTC_linear.new(spec: specs[1])  // tiltY
			]
		);
	}
	
}

