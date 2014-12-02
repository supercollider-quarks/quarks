
// DEPRECATED. Chucklib is more powerful.

VoicerProcess {

		// a gui-able process that will be played by a voicer
		// the func should reference the voicer (or other object) by variable defined outside
		// unfortunately, there is no way to pass the voicer in as an argument
		// while it plays in real time
		
		// on creation, "task" may be an already-existing task or a function
		// if a func, it will be wrapped in a Task

	classvar <>defaultAction;

	var	<voicer,		// the voicer that will play this process
		<>action,		// this func gets executed on doAction
		<>tag,
		<>midiNote,	// midi note assigned by VPTrigSocket--already converted to string
		<foreground,
		<background,
		<parent,
		<>task;		// may be shared with other VoicerProcesses
					// (so that one can start and the other can pause, for instance)
	
	*initClass {
			// typically this is what you want, but you can override at creation
			// you shouldn't use this for "stop" processes
		defaultAction = { arg p, g; g.stopOthers(p); p.play(doReset:true) };
	}
	
	*new { arg voicer, tag, action, task, foreground, background, parent;
		^super.new.init(voicer, tag, action, task, foreground, background, parent)
	}
	
	init { arg v, t, a, tk, fg, bg, p;
		var tk2;
		voicer = v ? NullVoicer.new;
		action = a ? defaultAction;
		tag = t;
		parent = p;
		foreground = fg ? Color.black;
		background = bg ? Color.grey;

		tk2 = tk.isFunction.if({ tk.value(voicer) }, { tk });
		tk2.isFunction.if({
			task = QuantTask.new(tk2, voicer.clock);
		}, {
			task = tk2;
		});
		
	}
	
	free {
		this.stop;
		voicer = action = tag = parent = foreground = background = task = midiNote
			= nil;
	}
	
	doAction { arg group;
		action.value(this, group)
	}
	
	play { arg quant, clock, doReset = false;	// if nil, task fills in the voicer's clock
		var q, result;
//("VoicerProcess-play : " ++ tag).postln;
		clock = clock ?? { task.clock };
		q = this.getQuant(quant, voicer.latency);
//(["VoicerProcess-play", clock.elapsedBeats, q.nextTimeOnGrid(clock)]).postln;
			// QuantTasks have a different argument order than PauseStreams,
			// so use keyword addressing
		result = task.play(quant: q, argClock: clock, doReset: doReset ? false);  // nil if play failed
		this.playUpdateGui(result);
	}
	
	playUpdateGui { arg playResult;
		parent.view.notClosed.if({
			{ parent.view.background_(	// yellow if no play, green if it went ok
				playResult.isNil.if({ Color.new255(255, 248, 60) }, { Color.green })
			  ); }.defer;
		});
	}
	
	stop { arg quant;
//("VoicerProcess-stop : " ++ tag).postln;
		task.notNil.if({
			task.clock.schedAbs(this.getQuant(quant, 0.2+(voicer.latency ? 0))
				.asTimeSpec.nextTimeOnGrid(task.clock), { task.stop; nil });
		});
		this.stopUpdateGui;
	}
	
	stopUpdateGui {
		parent.view.notClosed.if({
			{ parent.view.background_(Color.clear); }.defer;
		});		
	}
	
	pause { arg q; this.stop(q) }
	
	resume { arg quant;
		task.notNil.if({
			task.clock.schedAbs(this.getQuant(quant, 0.2+(voicer.latency ? 0))
				.asTimeSpec.nextTimeOnGrid(task.clock), { task.resume; nil });
		});
		parent.view.notClosed.if({
			{ parent.view.background_(Color.green); }.defer;
		});
	}
	
	reset { arg quant;
		task.notNil.if({
			task.clock.schedAbs(this.getQuant(quant, 0.2+(voicer.latency ? 0))
				.asTimeSpec.nextTimeOnGrid(task.clock), { task.reset; nil });
		});
	}
	
	isPlaying {
		task.notNil.if({
			parent.view.notClosed.if({
				{ parent.view.background_(task.isPlaying.if
					({ Color.red }, { Color.clear })); }.defer;
			});
			^task.isPlaying 
		}, {
			^false
		});
	}
	
	clock {
		task.notNil.if({ ^task.clock }, { ^nil });
	}
	
	state {	// returns an array that can be used in an SCButton states array
		^[this.displayTag, foreground, background]
	}
	
	displayTag {
		^midiNote.notNil.if({ midiNote ++ ": " }) ++ tag
	}

	getQuant { arg quant, adjustment;
		voicer.editor.notNil.if({		// if a gui, schedule according to quantize setting
			quant = (quant ?? { voicer.editor.quant.string.interpret })
				.asTimeSpec.applyLatency(adjustment)
		}, {
			quant = quant ? QuantOffsetLatencyTimeSpec(1, 0, adjustment)
		});
//["getQuant", tag, quant.asTimeSpec.nextTimeOnGrid(this.clock)].postln;
//quant.asTimeSpec.dump;
		^quant
	}

}

////////////////////////////////////////////////////

VoicerProcessGroup {

		// a group of voicer processes
		// states is not exactly like SCButton.states
		// here you give
		// [["tag", { action }, { task } or nil, fgcolor, bgcolor] ... ]
		
		// the action func gets passed this VoicerProcess and VoicerProcessGroup as an arg
		// the task func gets passed the voicer as an arg and it should return a function:
		// { arg v; { loop({ play stuff on v }) } }
		
		// if task is nil, it uses the same task as the previous VoicerProcess
		// to start a process on one item and stop it on the next, use:
		
		// [["Stopped", { arg proc, group; proc.play }, { arg v; { /* the player routine */ } }],
		//  ["Playing", { arg proc, group; proc.stop }, nil]]

	var	<voicer,
		<processes,		// array of VoicerProcess
		<view,
		<value = 0;			// current process to hit on .doAction
	
	*new { arg voicer, states;
		^super.new.init(voicer, states)
	}
	
	init { arg v, states;
		voicer = v;
		processes = Array.new;
		states.do({ arg st, i; this.add(st, i == 0) });
		value = processes.size * 10;	// allow room to go backward and forward w/midi control
		voicer.editor.notNil.if({ 
			this.makeGUI;
			voicer.editor.sizeWindow(0, 1);
		});
	}
	
	states {		// returns states as applicable to SCButton
		^processes.collect({ arg p; p.state });
	}
	
	items {
		^processes.collect({ arg p; p.displayTag })
	}
	
	value_ { arg v, updateGUI = true;
		value = v;
		(updateGUI && { view.notClosed }).if({
			{ view.value_(v) }.defer;
		});
	}
	
	stopAll {
		processes.do({ arg p; p.stop });
	}
	
	stopOthers { arg p;
			// only do this if the clock is before the time for scheduling of current task
			// if you trigger a change too late, the previously running thing shouldn't die
		var quant;
		quant = p.getQuant;
		(voicer.clock.elapsedBeats < quant.nextTimeOnGrid(p.task.clock ? TempoClock.default)).if({
			processes.do({ arg pp;
				(pp.task != p.task).if({
					pp.stop
				}, {
				});
			});
		});
	}
	
	doAction { arg p;
//["VoicerProcessGroup-doAction", p, value].postln;
		processes.wrapAt(p ? value).doAction(this);
	}
			
	makeGUI {
		(view.isNil and: { voicer.editor.notNil and: { voicer.editor.processView.notNil } }).if({
			view = GUI.popUpMenu.new(voicer.editor.processView, Rect(0, 0, 120, 20))
				.items_(this.items)
				.action_({ arg v;
					this.value_(v.value, false);	// set value but don't update gui
					processes.at(v.value).doAction(this)
				});
		});
	}
	
	removeGUI {	
		(view.notNil && voicer.editor.notNil).if({
			view.remove;
			view = nil;
			voicer.editor.refresh;
		});
	}
	
	updateView {
		view.notNil.if({
			{ view.items_(this.items);
			view.refresh; }.defer;
		});
	}
	
	add { arg st, first = false;
		{ st.size < 5 }.while({
			st = st.add(nil);
		});
		(st.at(2).isNil && first.not).if({ 
			st.put(2, processes.last.task);
		});
		processes = processes.add(VoicerProcess.performList(\new, [voicer] ++ st ++ [this]
//			st.at(0),				// tag
//			st.at(1),				// action
//			st.at(2),				// task
//			st.at(3),				// fg
//			st.at(4),				// bg
//			processes.size		// index
		));
		this.updateView;
	}
	
	removeAt { arg i;
		var p;
		p = processes.removeAt(i);
		p.isPlaying.if({ p.stop });
		(value >= processes.size).if({ this.value_((processes.size - 1).max(0)) });
		this.updateView;
//		view.doAction;
		^p
	}
	
	free {
		processes.do({ arg p; p.stop });		// stop all processes
		processes = Array.new;
		(view.notNil && voicer.editor.notNil).if({ 
			view.remove;
			voicer.editor.refresh;
		});
		view = nil;
	}
	
	at { |i| ^processes[i] }	// for easier syntax to get to VoicerProcesses

	active { ^voicer.active }
	
	clearMIDINotes {	// upon clearing VPTrigSocket pointing to me, I have to drop midi assg's
		processes.do({ arg p; p.midiNote = nil });
		this.updateView;
	}
	
}

ProcessToggleGroup : VoicerProcessGroup {
	
		// a simpler version: takes only one task IN VoicerProcessGroup SYNTAX
		// and cycles thru button states
		// states here is [["tag", { arg p; p.play or other }, task, foreground, background] ... ]
		// tasks after the first array will be ignored
	
	init { arg v, states;
		voicer = v;
		processes = Array.new;
		
		states.do({ arg st, i;
			this.add(st, i == 0)
		});

		voicer.editor.notNil.if({
			this.makeGUI;
			voicer.editor.sizeWindow(0, 1);
		});
	}
	
	makeGUI {
		(view.isNil and: { voicer.editor.notNil and: { voicer.editor.processView.notNil } }).if({
			view = GUI.button.new(voicer.editor.processView, Rect(0, 0, 120, 20))
				.states_(this.states)
				.action_({ arg v;
					processes.at(v.value).doAction
				});
		});
	}

	add { arg st, first;
		{ st.size < 3 }.while({
			st = st.add(nil);
		});
			// all processes in a toggle group must share the same task
		first.not.if({ st.put(2, nil); });
		super.add(st, first);
	}

	updateView {
		view.notNil.if({
			view.states_(this.states);
			view.refresh;
		});
	}
	
}

