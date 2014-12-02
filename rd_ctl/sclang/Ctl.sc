/* Ctl.sc - (c) rohan drape, 2004-2007 */

Ctl {
	
	var s,
	<index, <name,
	<internal, <spec,
	<depth, <state, <stateMap,
	recv,
	doSend;
	
	*new {
		arg s = nil,
		    index = 0, name = \none,
		    internal = 0.0, spec = \unipolar,
		    depth = 1, state = 0,
		    doSend = true;
		^super.newCopyArgs(s,
			index, name.asSymbol,
			internal, spec.asSpec,
			depth, state, [0.0, 1.0],
			Set.new,
		    doSend);
	}

	name_ {
		arg n;
		name = n;
		this.onEdit;
	}

	onValueModified {
		if(depth>1, { state = (internal * (depth - 1)).round;});
		this.onEdit;
	}
		
	internal_ {
		arg n = 0;
		internal = n.clip(0.0,1.0);
		this.onValueModified;
	}

	value {
		^spec.map(internal);
	}

	value_ {
		arg n;
		internal = spec.unmap(n);
		this.onValueModified;
	}
	
	update {
		if(doSend, {
			s.sendMsg("/c_get", index);
		});
	}

	spec_ {
		arg n;
		spec = n.asSpec;
		this.onEdit;
	}

	depth_ {
		arg n;
		n = n.asInteger;
		if(state >= n, {state = 0;});
		depth = n;
		if(depth < 1, {depth = 1;});
		this.onEdit;
	}

	onStateModified {
		if(depth == 1,
			{internal = 0;},
			{internal = stateMap.blendAt((state/(depth-1))*(stateMap.size-1));});
		this.onEdit;
	}

	state_ {
		arg n = 0;
		state = n.mod(depth);
		this.onStateModified;
	}

	stateMap_ {
		arg map;
		if(map.isNil,
			{stateMap = [0.0, 1.0];},
			{stateMap = map.collect({arg e; spec.unmap(e);});});
		this.onStateModified;
	}

	increment {
		arg n = 1;
		state = (state+n).mod(depth);
		this.onStateModified;
	}

	addRecv {
		arg proc;
		recv.add(proc);
	}
	
	removeRecv {
		arg proc;
		recv.remove(proc);
	}

	clearRecv {
		recv.clear;
	}

	dispatchRecv {
		recv.do({
			arg proc;
			proc.value(index, spec, this.value, state);
		});
	}

	send {
		s.sendMsg("/c_set", index, this.value);
	}

	onEdit {
		this.dispatchRecv;
		this.send;
	}
	
	asUGenInput {
		^this.index
	}

	setup {
		arg newName = "", newSpec = nil, newValue = nil, newDepth = 1, newStateMap = nil;
		name = newName;
		internal = 0;
		depth = newDepth;
		state = 0;
		if(newStateMap.isNil,
			{ stateMap = [0.0, 1.0]; },
			{ stateMap = newStateMap });
		if(newSpec.isNil,
			{ spec = ControlSpec(0, 1, LinearWarp); },
			{ spec = newSpec; });
		if(newValue.isNil,
			{ this.onEdit; },
			{ this.value = newValue; });
	}

	displayName {
		if(depth == 1,
			{^name},
			{^name++":"++state.asString});
	}

    display {
		("Name        : "++(name)).postln;
		("DisplayName : "++(this.displayName)).postln;
		("Index       : "++(index)).postln;
		("Depth       : "++(depth.asString)).postln;
		("State       : "++(state.asString)).postln;
		("Value       : "++(this.value.asString)).postln;
		("Internal    : "++(internal.asString)).postln;
		("Recv's      : "++(recv.size.asString)).postln;
	}
}
