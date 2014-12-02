
VoicerProxy {

	var	<voicer,		// the destination
		<controlProxies,	// proxies to voicer's global controls
		<processes,	// processes to be played on the destination
		<editor,		// my gui -- I'll probably have to write a custom setter
		<moreVoicers,	// voicer is the one that gets gui'ed;
					// items in this array are also recipients of play messages
					// todo: how to handle global controls
		<>moreVoicerProb = 1.0,	// probability of other voicers being triggered
		<>maxControlProxies;

	*new { arg v;
		^super.new.init(v)
	}
	
	init { arg v;
		controlProxies = Array.new;
		processes = Array.new;
		moreVoicers = Array.new;
		this.voicer_(v);
	}
	
	free {
		voicer.proxy_(nil);
		controlProxies.do({ |cp| cp.free });
		processes.do({ |pr| pr.free });
		editor.notNil.if({ editor.remove });
		this.releaseDependants;
	}	
	
	proxify { ^this }
	
	voicer_ { arg v;
			// to clear old voicer's proxy variable: voicer must not be nil
			// and voicer's current proxy must be this
			// if voicer is now pointing to another proxy (which is the case if
			// we're in the middle of reassigning voicers and proxies), I shouldn't change it
		(voicer.notNil and: { voicer.proxy == this }).if({
			voicer.proxy_(nil);	// break old connection
		});
		voicer = v ?? { NullVoicer.new };	// change voicer object
		voicer.proxy_(this);		// make new connection
		editor.notNil.if({ editor.updateStatus; });
	}
	
	draggedIntoVoicerGUI { arg dest;
		voicer.draggedIntoVoicerGUI(dest);
	}

	addControlProxy { arg gcproxy, addAlways = false;
		(this.canAddControlProxies and: {
			addAlways or: { controlProxies.includes(gcproxy).not } }).if({
				controlProxies = controlProxies.add(gcproxy
					?? { VoicerGCProxy(nil, this) });
		});
	}
	
	removeControlProxy { arg gc;	// gc may be symbolic name, gc, or gcproxy
		gc.isSymbol.if({
			gc = controlProxies.select({ |x| x.gc.name == gc }).at(0);
		});
		gc.isKindOf(VoicerGlobalControl).if({ gc = gc.proxy });
		controlProxies.remove(gc);
	}
	
	getFreeControlProxy { arg gcmodel;	// if I need to make a new proxy, this will be the model
		var gcproxy;
			// loop thru controlProxies array
			// gcproxy gets the first element of controlProxies whose model is nil

		maxControlProxies.isNil.if({ controlProxies }, { controlProxies[0..maxControlProxies-1] })
		.do({ |pr, i|
			(gcproxy.isNil && pr.tryPerform(\gc).isNil).if({ gcproxy = pr });
		});
			 // if none was free, it's my responsibility to make it and add it to me
		gcproxy.isNil.if({
				// but, we may do it only if there's no limit
			this.canAddControlProxies.if({
				gcproxy = VoicerGCProxy(gcmodel, this);
				this.addControlProxy(gcproxy);
			}, {
				"This VoicerProxy may have no more than % control proxies."
					.format(maxControlProxies).warn;
			});
		}, {
			gcproxy.gc_(gcmodel);	// make sure proxy knows who it's talking to
		});
		^gcproxy
	}
	
	switchControlProxies {		// my controlproxies should point to my voicer's controls
		var i;
			// take guiable proxies in order, sort in order of creation
		voicer.globalControlsByCreation.do({ |gc|
			this.getFreeControlProxy(gc);
		});
			// when this proxy receives a voicer with fewer controls than I have proxies,
			// remaining proxies must be set to nil
		i = voicer.globalControls.size;
		{ i < controlProxies.size }.while({
			controlProxies[i].gc_(nil);
			i = i+1;
		});
	}
	
	clearControlProxies {	// called when setting a voicer's proxy to nil
		controlProxies.do({ |gcpr|
			gcpr.gc_(nil);
		});
	}
	
	canAddControlProxies { ^controlProxies.size < (maxControlProxies ? inf) }
	
	modelWasFreed {
		this.voicer_(NullVoicer.new);
		editor.notNil.if({
			editor.updateStatus
		}, {
				// if voicer is gone and there is no gui, no need for this object to exist
			this.free;
		});
	}
	
	editor_ { |gui|
		editor = gui;
			// if editor dies and there is no voicer, again, kill the proxy
		(gui.isNil and: { this.active.not }).if({
			this.free;
		});
	}
	
		// change the voicer's target to the mixer in this gui
		// does not affect currently playing notes
		// if the gui is empty, the drag-n-drop will be ignored
	draggedIntoMixerGUI { |gui|
		voicer.notNil.if({
			voicer.draggedIntoMixerGUI(gui)
		});
	}

	clock {
		^voicer.clock;
	}
	
	addVoicer { arg v;
		moreVoicers = moreVoicers.add(v);
	}
	
	removeVoicer { arg v;
		^moreVoicers.remove(v);
	}

//////// things voicer used to manage, now managed by voicerproxy

	addProcess { arg states, type;	// adds a VoicerProcessGroup
		var class, process;
		class = (type == \toggle).if(ProcessToggleGroup, VoicerProcessGroup);
		process = class.new(this, states);
		processes = processes.add(process);
		^process
			// gui should update automatically if it exists--or should I tell it to?
	}
	
	removeProcess { arg p;
		var i;
		i = processes.indexOf(p);
		i.notNil.if({ ^this.removeProcessAt(i) }, { ^nil });
	}
	
	removeProcessAt { arg i;
		var process;
		process = processes.removeAt(i);
		process.free;
		^process
	}

//////// things Voicer normally does but which should be available thru the proxy as well

	mapGlobal { arg name, bus, value, spec, allowGUI = true;
		var gc, gcproxy;
		gc = voicer.mapGlobal(name, bus, value, spec, allowGUI);
		this.addControlProxy(gcproxy = VoicerGCProxy.new(gc, this));
		^gcproxy	// when you mapGlobal on a voicerproxy, you should get a voicergcproxy
				// so that guis and midi mappings are hot swappable
	}
	
	unmapGlobal { arg name;
		voicer.unmapGlobal(name);		
		// does not free the proxy, because proxies must be explicitly discarded
		// no need to update the gui here, b/c when gc gets freed, its proxy is updated
	}
	
//////// play methods sent directly on to voicer

	active {
		^voicer.active;
	}
	
	run { |bool = true|
		voicer.notNil.if({ voicer.run(bool) });
	}
	
	isRunning { voicer.notNil.if({ ^voicer.isRunning }, { ^true }); }
	
	panic {
		voicer.panic;
	}
	
	cleanup {
		voicer.cleanup;
	}
	
	set { arg args;
		voicer.set(args)
	}
	
	trigger1 { arg freq, gate = 1, args, lat = -1;
		moreVoicers.do({ |v|
			moreVoicerProb.coin.if({
				v.trigger1(freq, gate, args, lat)
			});
		});
		^voicer.trigger1(freq, gate, args, lat)
	}
	
	trigger { arg freq, gate = 1, args, lat = -1;
		moreVoicers.do({ |v|
			moreVoicerProb.coin.if({
				v.trigger(freq, gate, args, lat)
			});
		});
		^voicer.trigger(freq, gate, args, lat)
	}
	
	gate1 { arg freq, dur, gate = 1, args, lat = -1;
		moreVoicers.do({ |v|
			moreVoicerProb.coin.if({
				v.gate1(freq, dur, gate, args, lat)
			});
		});
		^voicer.gate1(freq, dur, gate, args, lat)
	}
	
	gate { arg freq, dur, gate = 1, args, lat = -1;
		moreVoicers.do({ |v|
			moreVoicerProb.coin.if({
				v.gate(freq, dur, gate, args, lat)
			});
		});
		^voicer.gate(freq, dur, gate, args, lat)
	}
	
	release1 { arg freq, lat = -1;
		moreVoicers.do({ |v| v.release1(freq, lat) });
		^voicer.release1(freq, lat)
	}
	
	release { arg freq, lat = -1;
		moreVoicers.do({ |v| v.release(freq, lat) });
		^voicer.release(freq, lat)
	}
	
	releaseAll {
		moreVoicers.do({ |v| v.releaseAll });
		^voicer.releaseAll;
	}
	
	releaseNow1 { arg freq, sec;
		moreVoicers.do({ |v| v.releaseNow1(freq, sec) });
		^voicer.releaseNow1(freq, sec)
	}
	
	releaseNow { arg freq, sec;
		moreVoicers.do({ |v| v.releaseNow(freq, sec) });
		^voicer.releaseNow(freq, sec)
	}
	
	// hmm... this will not work well for moreVoicers...
	releaseNode { |node, freq, releaseGate = 0, lat = -1|
		voicer.releaseNode(node, freq, releaseGate, lat);
	}

	prGetNodes { |numNodes = 1| ^voicer.prGetNodes(numNodes) }
	setArgsInEvent { |event| ^voicer.setArgsInEvent(event) }
	
	guiClass {
		^VoicerProxyGui
	}
	
	asString {
		^voicer.asString;
	}
	
//////// getters and setters

	latency {
		^voicer.latency;
	}
	
	latency_ { arg l;
		moreVoicers.do({ |v| v.latency_(l) });
		^voicer.latency_(l);
	}

	globalControls {
		^voicer.globalControls;
	}

	globalControlsByCreation {
		^voicer.globalControlsByCreation
	}
	
	target {
		^voicer.target;
	}

	bus {
		^voicer.bus;
	}

	susPedal { ^voicer.susPedal }
	sustainPedal { |sustain|
		moreVoicers.do(_.sustainPedal(sustain));
		voicer.sustainPedal(sustain);
	}
}
