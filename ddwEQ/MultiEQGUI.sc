
MultiEQGUI {

	var	<w,			// the window
		addButton,	// command buttons
		printButton,
		<eq,			// the DynMultiEQ object I'm editing
		<bandguis,	// array of EQBandGUI's
		caller;		// who called me?

	*new { arg multieq, name = "EQ editor", caller;
		^super.new.init(multieq, name, caller);
	}

	init { arg multieq, name, call;
		eq = multieq;
		eq.editor = this;
		caller = call;	// nil if instantiated by .new, \edit if called from DynMultiEQ-edit

		w.isNil.if({
			w = GUI.window.new(name, Rect.new(10, 10, 10, 10)).onClose_(this.closeAction);
			this.makeViews;
			this.refresh;		// resizes and updates
			w.front;
		});
	}

	makeViews {
		var origin, newgui;
		addButton = GUI.button.new(w, Rect.new(200, 20, 100, 20))
			.states_([["add band", Color.black, Color.grey]])
			.font_(GUI.font.new("Helvetica", 10))
			.action_(this.addAction);
		printButton = GUI.button.new(w, Rect.new(350, 20, 100, 20))
			.states_([["print specs", Color.black, Color.grey]])
			.font_(GUI.font.new("Helvetica", 10))
			.action_({ eq.postln });
		origin = Point(10, 60);
		bandguis = eq.spec.collect({
			arg sp;
			newgui = EQBandGUI.new(origin, sp, this);
			origin = origin + Point(0, EQBandGUI.bandSize.y + 20);
			newgui.update
		});
		this.resizeWindow
	}

	resizeWindow {
		w.bounds = Rect(w.bounds.left, w.bounds.top, EQBandGUI.bandSize.x + 40,
			80 + (eq.spec.size * (EQBandGUI.bandSize.y + 20))
		)
	}

	refresh {
		var origin;
		this.resizeWindow;
		origin = Point(10, 60);
		bandguis.do({ arg g;
			g.origin_(origin).update;
			origin = origin + Point(0, EQBandGUI.bandSize.y + 20);
		});
	}

	addAction {
		^{
			eq.add(\eq, 400, 1, 1);		// make a new band
			bandguis = bandguis.add(EQBandGUI.new(Point(0,0),
				eq.spec.last, this));
			this.refresh;
		}
	}

	closeAction {
		^{	eq.postln;
			eq.spec.do({ arg sp;	// let each band know it's no longer gui'ed
				sp.gui = nil;
			});
			bandguis = nil;	// garbage bandguis
			eq.editor = nil;
			(caller == \edit).if({	// editor should clean itself up
				eq.free;
			});
		}
	}

}

EQBandGUI {
	// graphic controls for one EQ band
	// these should only be embedded in a MultiEQGUI

	classvar	<bandSize,	// size of 1 band altogether
			<typeBounds,	// bounds relative to origin
			<freqSlBounds,	// freqSlider
			<freqTxBounds,
			<kSlBounds, <kTxBounds,
			<rqSlBounds, <rqTxBounds,
			<bypassBounds,
			<removeBounds,
			<fontSpecs,
			<typesArray,
			<freqSpec, <kSpec, <rqSpec;

	var		<origin,		// supplied by MultiEQGUI
			<typeMenu,		// menu for eq type
			<freqSlider, <freqText,	// sliders & text displays for parameters
			<kSlider, <kText,
			<rqSlider, <rqText,
			<bypassButton,
			<removeButton,
			<band, <parent;	// points to EQBand and MultiEQGUI

	*initClass {
		bandSize = Point(745, 50);
		typeBounds = Rect(0, 0, 100, 20);
		freqSlBounds = Rect(120, 0, 420, 20);
		freqTxBounds = Rect(550, 0, 75, 20);
		kSlBounds = Rect(120, 30, 155, 20);
		kTxBounds = Rect(285, 30, 75, 20);
		rqSlBounds = Rect(385, 30, 155, 20);
		rqTxBounds = Rect(550, 30, 75, 20);
		bypassBounds = Rect(640, 0, 25, 20);
		removeBounds = Rect(675, 0, 50, 20);
		Class.initClassTree(StaticEQ);
		typesArray = StaticEQ.eqFuncs.keys.asArray.sort;
		Class.initClassTree(Spec);
		freqSpec = [20, 20000, \exponential].asSpec;
		kSpec = [0.1, 10, \exponential].asSpec;
		rqSpec = [0.01, 2, \linear].asSpec;
		fontSpecs = #["Helvetica", 10];
	}

	*new { arg org, bnd, par;
		^super.new.init(org, bnd, par)
	}

	init { arg org, bnd, par;
		band = bnd.gui_(this);
		parent = par;
		{ typeMenu = GUI.popUpMenu.new(parent.w, typeBounds).items_(typesArray)
			.font_(GUI.font.new(*fontSpecs))
			.action_(this.typeMenuAction);
		freqSlider = GUI.slider.new(parent.w, freqSlBounds)
			.action_(this.freqSlAction);
		freqText = GUI.staticText.new(parent.w, freqTxBounds);
		kSlider = GUI.slider.new(parent.w, kSlBounds)
			.action_(this.kSlAction);
		kText = GUI.staticText.new(parent.w, kTxBounds).align_(\right);
		rqSlider = GUI.slider.new(parent.w, rqSlBounds)
			.action_(this.rqSlAction);
		rqText = GUI.staticText.new(parent.w, rqTxBounds);
		bypassButton = GUI.button.new(parent.w, bypassBounds)
			.font_(GUI.font.new(*fontSpecs))
			.states_([["on", Color.black, Color.grey], ["off", Color.black, Color.grey]])
			.action_(this.bypassAction);
		removeButton = GUI.button.new(parent.w, removeBounds)
			.font_(GUI.font.new(*fontSpecs))
			.states_([["remove", Color.black, Color.grey]])
			.action_(this.removeAction);
		nil }.defer;
	}

	origin_ { arg org;
		{ origin = org;
		typeMenu.bounds = typeBounds.moveBy(origin.x, origin.y);
		freqSlider.bounds = freqSlBounds.moveBy(origin.x, origin.y);
		freqText.bounds = freqTxBounds.moveBy(origin.x, origin.y);
		kSlider.bounds = kSlBounds.moveBy(origin.x, origin.y);
		kText.bounds = kTxBounds.moveBy(origin.x, origin.y);
		rqSlider.bounds = rqSlBounds.moveBy(origin.x, origin.y);
		rqText.bounds = rqTxBounds.moveBy(origin.x, origin.y);
		bypassButton.bounds = bypassBounds.moveBy(origin.x, origin.y);
		removeButton.bounds = removeBounds.moveBy(origin.x, origin.y);
		nil }.defer;
	}

	free { arg freeIt = true;
		freeIt.if({ parent.eq.remove(band, false) });

		{	[typeMenu, freqSlider, freqText, kSlider, kText, rqSlider, rqText,
			bypassButton, removeButton].do(_.remove);
		nil }.defer
	}

	update {	// update view values
		{
			typeMenu.value = typesArray.indexOf(band.type);
			freqSlider.value = freqSpec.unmap(band.freq);
			freqText.string_(band.freq.trunc(0.01));
			kSlider.value = kSpec.unmap(band.k);
			kText.string_(
				(band.k >= 1).if({"+"}, {""}) ++ band.k.ampdb.trunc(0.01) ++ " dB"
			);
			rqSlider.value = rqSpec.unmap(band.rq);
			rqText.string_(band.rq.trunc(0.01));
		nil }.defer;
	}

	typeMenuAction {
		^{ 	band.type_(typesArray.at(typeMenu.value), false);
			this.update;
		 }
	}

	freqSlAction {
		^{ 	band.freq_(freqSpec.map(freqSlider.value), false);
			this.update;
		 }
	}

	kSlAction {
		^{ 	band.k_(kSpec.map(kSlider.value), false);
			this.update;
		 }
	}

	rqSlAction {
		^{ 	band.rq_(rqSpec.map(rqSlider.value), false);
			this.update;
		 }
	}

	bypassAction {
		^{	band.run(bypassButton.value == 0)
		}
	}

	removeAction {
		^{ 	this.free;
			parent.bandguis.remove(this);
			parent.refresh;
		}
	}

}