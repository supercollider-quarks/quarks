
	// use this class (not an instance) as the defaultDest for VoicerMIDIController
	// if you want MIDI controls not assigned to a VoicerGC or proxy NOT to die on
	// MIDIPort.update
VoicerGCDummy {
	*new { this.shouldNotImplement(\new) }

	*set {}

	*active { ^true }	// if this is the VoicerMIDIController's dest, it should not go away
					// on MIDIPort cleanup
	*midiControl_ {}

	*displayNameSet {}

	*spec { ^\unipolar.asSpec }
}

// AbstractFunction? Yeah... I'd like this to respond to math messages
GlobalControlBase : AbstractFunction {
	classvar	<indexForSorting = 0,
			<>defaultLag = nil;	// if you're using a global control in a Patch
	var	<name, <bus, <server, <spec, <value,	// internal to gc
		<allowGUI = true,			// some controls should not appear in the window
		<proxy,					// connections to other objects
		<parentProxy,				// should be a voicerproxy
		<voicerIndex,				// for sorting
		<midiControl,			// I need to know my midi controller for display
		<>lag,
		<autoSynth,
		watchCount = 0;    // more than one object might request that I watch for server changes

	*new { arg name, bus, value, spec, allowGUI = true ... extraArgs;
		^super.new.init(name, bus, value, spec, allowGUI, *extraArgs);
	}

	init { arg n, b, val, sp, guiOK;
		server = b.tryPerform(\server) ? Server.default;
		if(server.serverRunning.not) {
			MethodError("Server % must be running before using global controls."
				.format(server), this).throw;
		};
		allowGUI = guiOK;
		voicerIndex = indexForSorting = indexForSorting + 1;
		spec = sp.asSpec;
		name = n.asSymbol;
		bus = b ?? { BusDict.control(server, 1, name ++ " control") };
		this.set(val ? spec.default);
		lag = defaultLag;
		this.makeGUI;		// if gui isn't opened, this does nothing
	}

	set { arg val, updateGUI = true, latency, resync = true;
			// take action only if value is changing
			// because of dependencies, .set may be called multiple times with the same value
		(value != val).if({
			this.stopAuto;
			value = val;
			bus.server.sendBundle(latency, this.setMsg(value));
			this.changed((what: \value, updateGUI: updateGUI, resync: resync, updateBus: false));
		});
	}

	setMsg { |val|
		^[\c_set, bus.index, value]
	}

	value_ { arg value;
		this.set(value);
	}

	silentValue_ { |val|
		value = val;
	}

	unmappedValue { ^spec.unmap(value) }

	free { arg updateGUI = true;
		var	oldAutoNode = autoSynth, updater,
			freeBusFunc = {
				// problem: global control could be freed before receiving n_end for autosynth
				if(server.notNil) {
					if(BusDict.at(\control, server, bus.index).notNil) {
						BusDict.free(bus);		// free the bus
					} {
						bus.free
					};
				};
				bus = nil;
				server = nil;
			};
		server !? {
			this.stopWatching.stopAuto;
			// sync problem: bus could be freed, and reallocated,
			// before autoSynth *really* went away
			// so, if we were automating and getting server notifications,
			// wait until n_end comes back before freeing the bus number
			if(server.notified and: { oldAutoNode.notNil }) {
				updater = Updater(oldAutoNode, { |node, what|
					if(what == \n_end) {
						updater.remove;
						freeBusFunc.value;
					};
				})
			} {
				freeBusFunc.value
			};
			this.changed((what: \modelWasFreed, resync: true));
		}
	}

	spec_ { |newSpec, updateGUI = true|
		newSpec = newSpec.tryPerform(\asSpec);
		newSpec.notNil.if({
			spec = newSpec;
			this.changed((what: \spec, updateGUI: updateGUI));
		});
	}

		// automation
	play { |thing, args, target, addAction = \addToTail|
		^thing.playOnGlobalControl(this, args, target, addAction)
	}
	automate { |thing, args, target, addAction = \addToTail|
		var	targetServer = target.asTarget.server, updater;
		if(targetServer !== server) {
			MethodError("Target is not on the same server as the GlobalControl.", this).throw;
		};
		// autoSynth_ takes care of important bookkeeping
		this.autoSynth = this.play(thing, args, target, addAction);
		^autoSynth
	}
	stopAuto {
		if(autoSynth.notNil and: { autoSynth.tryPerform(\asNodeID).notNil }) {
			server.listSendBundle(nil, [
				[error: -1],	// suppress potential node not found error
				[n_free: autoSynth.asNodeID],
				[error: -2]
			]);
		} {
			// necessary: autoSynth could be a Patch, which doesn't answer asNodeID
			// we still have to dispose of it
			autoSynth.free;  // nil.free is a no-op
		};
		autoSynth = nil;
	}
	autoSynth_ { |node|
		var updater;
		autoSynth !? { this.stopAuto };
		autoSynth = node;
		// I would like to use respondsTo instead of tryPerform,
		// but 'nil' responds to asNodeID!
		// So I have to check the result :-|
		if(server.notified and: { autoSynth.tryPerform(\asNodeID).notNil }) {
			autoSynth.register;
			updater = Updater(autoSynth, { |node, what|
				if(what == \n_end) {
					updater.remove;
					if(node === autoSynth) {
						autoSynth = nil;
					};
				};
			});
		};
	}

		// mapping and UGen support
	asMap { ^("c" ++ bus.index).asSymbol }
	asMapArg { ^this.asMap }
	asNodeArg { ^this.asMap }
	asUGenInput {
		if(lag.notNil) {
			^Lag.kr(In.kr(bus.index, 1), lag)
		} {
			^In.kr(bus.index, 1)
		}
	}
	asControlInput { ^this.asMap }

	active { ^bus.notNil }

		// using KrBusWatcher to update gui
	asBus { ^bus }
	index { ^bus.index }

		// pass in updateBus == true to make the update on the server as well
		// normally this method is meant to get a value from the server,
		// so updateBus is NOT wanted then
	update { |bus, msg, updateBus(false)|
		value = msg[0];
		this.changed((what: \value, updateGUI: true, resync: true, updateBus: updateBus));
	}

	watch { |count = 0|
		if(watchCount == 0) {
			bus.notNil.if({
				KrBusWatcher.register(bus);
				bus.addDependant(this);
			});
		};
		watchCount = watchCount + max(1, count);
	}

	stopWatching { |count = 0|
		if(count == 0) {
			watchCount = 0;  // default, assume to stop watching no matter who else said to
		} {
			watchCount = max(0, watchCount - count);
		};
		if(watchCount == 0 and: { bus.notNil }) {
			KrBusWatcher.unregister(bus);
			bus.removeDependant(this);
		};
	}

		// math support
	composeUnaryOp { arg aSelector;
		^value.perform(aSelector)
	}
	composeBinaryOp { arg aSelector, something, adverb;
		^value.perform(aSelector, something, adverb);
	}
	reverseComposeBinaryOp { arg aSelector, something, adverb;
		^something.perform(aSelector, value, adverb);
	}
	composeNAryOp { arg aSelector, anArgList;
		^value.performList(aSelector, anArgList)
	}

		// pattern support
	asPattern { ^Pfunc({ this.value }) }
	asStream { ^this.asPattern.asStream }

		// patch support: use like KrNumberEditor
	instrArgFromControl { |control|
		^lag.notNil.if({ Lag.kr(In.kr(control, 1), lag) },
			{ In.kr(control, 1) });
	}
	addToSynthDef { |synthDef, name|
		synthDef.addIr(name, bus.index)
	}
		// protect against the bus number being changed (not sure if it really works)
	rate { ^\scalar }
	synthArg { ^this.index }
}


// adds gui powers specifically for Voicer guis

GenericGlobalControl : GlobalControlBase {

	midiControl_ { |cc|
		this.removeDependant(midiControl);
		(midiControl = cc).notNil.if({
			this.addDependant(midiControl);
		});
		this.changed((what: \midi));
	}

		// because this isn't linked to a voicer, must pass in the destination
	proxify { |ptProxy|		// make a gcproxy and save it in the voicerproxy
				// if it's already proxied, return the old
		proxy.isNil.if({	// need a proxy?
			(parentProxy = ptProxy).notNil.if({	// if there's a voicerproxy,
					// note: if there's a gui, there must be a proxy
				this.proxy = parentProxy.getFreeControlProxy(this);  // can I reuse something?
			}, {
				this.proxy = VoicerGCProxy(this);	// if no voicerproxy, make a new proxy
			});
		});
		^proxy
	}

	proxy_ { |gcpr|
		this.removeDependant(proxy);
		(proxy = gcpr).notNil.if({ this.addDependant(proxy) });
	}

		// resizes can be postponed until many gc guis are made
	makeGUI { arg parentProxy, resizeNow = true;
			// gui must be permitted, and there must be somewhere to put it
		(allowGUI and: { parentProxy.tryPerform(\editor).notNil }).if({
			this.proxify(parentProxy).notNil.if({
				proxy.makeGUI(resizeNow);
			});
		});
	}

	displayNameSet {
		proxy.notNil.if({ proxy.displayNameSet });
	}

	draggedIntoVoicerGCGUI { |gui|
		gui.model.gc_(this);
	}

	bindClassName { ^\GenericGlobalControl }

		// hardcode into a synthdef
		// should be used only for quick and dirty testing! better to use .asMapArg
	kr {
		^if(lag.notNil) {
			Lag.kr(In.kr(this.index, 1), lag)
		} {
			In.kr(this.index, 1)
		}
	}

	guiClass { ^NumberEditorGui }
	activeValue_ { |v| this.set(v) }
}

VoicerGlobalControl : GenericGlobalControl {
		// holds name, bus & gui info
		// note: these should ONLY be created by a voicer -- private class

	var	<>voicer;		// specific to a voicer

	*new { arg name, bus, voicer, value, spec, allowGUI = true;
		^super.new(name, bus, value, spec, allowGUI, voicer)
	}

	init { arg n, b, val, sp, guiOK, v;
		voicer = v;
		server = voicer.target.server;
		if(server.serverRunning.not) {
			MethodError("Server % must be running before using global controls."
				.format(server), this).throw;
		};
		allowGUI = guiOK;
		spec = sp.asSpec;
		name = n.asSymbol;
		voicerIndex = voicer.maxControlNum + 1;
		parentProxy = voicer.proxy;
		bus = b ? BusDict.control(server, 1, name ++ " control");
		this.set(val ? spec.default);
			// do the mapping for loaded nodes
		voicer.nodes.do({ arg n;
			(n.isPlaying).if({
				n.map(name, bus.index);
			});
		});
		this.makeGUI;		// if gui isn't opened, this does nothing
	}

	free { |updateGUI = true|
			  // remove kr bus mapping from each active synth
		voicer.nodes.do({ arg n;
			(n.isPlaying).if({ n.synth.map(name, -1); });
		});
		super.free(updateGUI);
	}

	proxify { ^super.proxify(parentProxy) }

	makeGUI { arg resizeNow = true;	// resizes can be postponed until many gc guis are made
			// gui must be permitted, and there must be somewhere to put it
		(allowGUI and: { voicer.editor.notNil }).if({
			this.proxify.notNil.if({
				proxy.makeGUI(resizeNow);
			});
		});
	}
}

VoicerGCProxy {
		// switchable placeholder for a VoicerGlobalControl in the gui system
	var	<gc, <>gui, <parentProxy,	// connections
		<active,
		midiControl;	// a VoicerMIDIController can point to a proxy for easy reuse
					// setter provided here; getter (below) needs to return this variable
					// or midiControl from the model (gc)

	*new { arg gc, ptProxy;
		^super.newCopyArgs(gc, nil, ptProxy).init // initialization? check here for gui exists?
	}

	init {
		gc.tryPerform(\proxy_, this);	// so model knows how to find me
		active = true;
	}

	free {
		this.releaseDependants;
		active = false;
		gc.tryPerform(\proxy_, nil);
		gc = gui = nil;
	}

	gc_ { arg c;
		gc.notNil.if({ gc.proxy_(nil); });	// break old connection
		gc = c;
		gc.notNil.if({ gc.proxy_(this); });	// set new connection
		this.changed((what: \gc, resync:true));	// update gui and midicontrol
	}

	bindGenericGlobalControl { |inputControl|
		this.gc_(inputControl)
	}

	midiControl { ^(midiControl ?? { gc.tryPerform(\midiControl) }) }
	midiControl_ { |cc|
		this.removeDependant(midiControl);
		(midiControl = cc).notNil.if({ this.addDependant(midiControl) });
		this.changed((what: \midi));
	}

	set { arg value, updateGUI = true, latency, resync = true;
		gc.notNil.if({ gc.set(value, false, latency, resync) });
		this.changed((what: \value, updateGUI: updateGUI, resync: resync));
	}

	update { arg theChanger, args;
			// should only do something if it's the result of the slider being moved
		case { gc.notNil and: { theChanger.isKindOf(NumberEditor) } }
				{ gc.set(theChanger.value, false); }	// false = don't reupdate gui
			{ args.respondsTo(\keysValuesDo) }	// standard dictionary check
				{	(args[\what] == \modelWasFreed).if({
						this.modelWasFreed;
					}, {
						this.changed(args)	// pass to my dependants
					});
				}
	}

	allowGUI {
		^gc.tryPerform(\allowGUI) ? VoicerProxyGui.drawEmptyControlProxies
	}

	updateGUI { |updateBus = true|
		gui.notNil.if({ gui.updateView(updateBus) });
	}

	makeGUI { arg resizeNow = true, voicerGUI;
			// if I already have a gui, stick with it
		gui.isNil.if({
			gui = VoicerGCView(this, resizeNow, voicerGUI);
			this.addDependant(gui);
		});
	}

	removeGUI { arg resizeNow = true;
		this.removeDependant(gui);
		gui.remove(true, resizeNow);
		gui = nil;
	}

	modelWasFreed {
		gc.removeDependant(this);
		gc = nil;		// break the connection
		gui.notNil.if({ gui.updateStatus });	// gui should know how to display "inactive"
	}

	displayNameSet {
		gui.notNil.if({ gui.displayNameSet });
	}

	// getters and/or setters

	voicer { ^gc.tryPerform(\voicer) }

	value { gc.notNil.if({ ^gc.value }, { ^0 }); }

	value_ { arg v; this.set(v) }	// alias for .set

	spec { gc.notNil.if({ ^gc.spec }, { ^\unipolar.asSpec }); }

	spec_ { |newSpec| gc !? { gc.spec_(newSpec) } }

	name { gc.notNil.if({ ^gc.name }, { ^"inactive" }); }

	proxify { ^this }

}

	// this gives you a name and NumberEditorGui for a global control
	// these are swappable -- you can plug a different VoicerGlobalControl
	// into the proxy and the display will update
	// this is a private class -- do not call outside the context of gui-ing a voicer or proxy
VoicerGCView {
	var	<editor, <editorGUI, <nameView,	// internal
		<midiDrag,	// make midi routings draggable
		<model,		// conn to other objects--model should be a VoicerGCProxy
		<parentGui;

	*new { arg gcproxy, doRefresh = true, voicerGUI;
		^super.new.init(gcproxy, doRefresh, voicerGUI)
	}

	init { arg gcproxy, doRefresh = true, voicerGUI;
			// only create an editor if there isn't one and a voicer editor is open
			// in what innocent circumstances will this test crash?
		model = gcproxy;
			// using ?? for conditional execution -- model.parentProxy.editor may fail,
			// but it shouldn't if voicerGUI was passed in as argument
		(voicerGUI = voicerGUI ?? { model.parentProxy.editor }).notNil.if({
			{
			editor = NumberEditor(model.value, model.spec);
			midiDrag = GUI.dragBoth.new(voicerGUI.controlView, Rect(0, 0, 30, 20))
				.align_(\center)
				.font_(GUI.font.new("Helvetica", 10))
				.action_({ |rec|
					rec.object.tryPerform(\draggedIntoVoicerGCGUI, this);
				})
				.beginDragAction_({ model.midiControl });
			nameView = GUI.staticText.new(voicerGUI.controlView, Rect(0, 0, 100, 20));
			editorGUI = editor.gui(voicerGUI.controlView, Rect(0, 0, 150, 20));
			editor.addDependant(model);
			voicerGUI.controlView.decorator.nextLine;  // insure correct formatting for next

			this.displayNameSet;		// displays with midi routing if any
			doRefresh.if({ voicerGUI.sizeWindow; });  // make sure it shows
			nil
			}.defer;
		});
	}

	remove { arg doRefresh = true, resizeNow = true;
		editor.removeDependant(model);
		this.releaseFromDependencies;
		doRefresh.if({  // doRefresh is true when removing a control, false when freeing whole gui
			this.removeViews(resizeNow);
		});
		editor = nil;
		editorGUI = nil;
	}

	removeViews { arg resizeNow = true;
		nameView.notNil.if({ nameView.remove });
		editorGUI.notNil.if({ editorGUI.removeView });
		nameView = editorGUI = nil;
		resizeNow.if({ model.parentProxy.editor.refresh; });
	}

	displayNameSet {
		var cc;
		var dispName, midiName = "";
		{
			(nameView.notNil and: { nameView.notClosed }).if({
				dispName = model.name.asString;
				cc = model.midiControl;  // do I have a controller num?
				// fix display name if it's midi-routed
				cc.notNil.if({
					{ midiName = cc.ccnum.shortName }.try({ |err|
						if(err.species == DoesNotUnderstandError and: { err.selector == \shortName }) {
							midiName = cc.ccnum.asString
						};
					})
				});
				nameView.string_(dispName);
				midiDrag.string_(midiName);
			});
		}.defer;
	}

	update { |theChanger, args|
		args.notNil.if({
			switch(args[\what])
				{ \value } { this.updateView(args[\updateBus] ? true) }
				{		// default action, update gui
					this.updateView(false);
					this.updateStatus;
				}
		});
	}

	updateView { |updateBus = true|
			// nameView.notClosed is an indirect check
			// if window has closed, nameView.notClosed will be false
			// and there is no need to update the views
		if(editor.notNil) {
			{
				if(nameView.notClosed) {
					editor.value_(model.value, false);
					updateBus.if({ editor.changed }, { editorGUI.update });
				};
				nil
			}.defer;
		};
	}

	updateStatus {
		this.displayNameSet;
			// if visible, update spec and value because
		// the proxy might be pointing to something else now
		{
			if(editor.notNil and: { nameView.notClosed }) {
				editor.removeDependant(model);	// changing editor must not affect model yet
				editor.spec_(model.spec).value_(model.value);
				editorGUI.update;
				editor.addDependant(model);
			};
		}.defer;
	}
}
