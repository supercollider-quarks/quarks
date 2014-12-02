/* CtlView.sc - (c) rohan drape, 2004-2007 */

CtlView {
	var ctl, parent, basicColor, updateProc,
	layoutView, nameView, dialView, valueView;
		
	*new {
		arg ctl, parent, basicColor;
		^super.new.init(ctl, parent, basicColor);
	}

	*height {
		var n = 3;
		^(n*16)+(n*4);
	}
		
	*width {
		^(80+4);
	}
	
	*font {
		^GUI.font.new(\Helvetica, 10);
	}
	
	setupNameView {
		nameView = GUI.button.new(layoutView, Rect(0, 0, 80, 16));
		nameView.font = CtlView.font;
		nameView.states = [[ctl.displayName, Color.black, basicColor]];
		nameView.action = {
			arg button, modifiers;
			var shiftMask, direction;
			shiftMask = 131074;
			direction = if(modifiers&shiftMask == shiftMask, {-1}, {1});
			ctl.increment(direction);
		};
	}
		
	updateNameView {
		nameView.states = [[ctl.displayName, Color.black, basicColor]];
		nameView.value = 0;
	}

	setupDialView {
		dialView = GUI.slider.new(layoutView, Rect(0, 0, 80, 16));
		dialView.knobColor_(basicColor);
		dialView.background_(basicColor.copy.alpha_(0.3));
		dialView.thumbSize = 4;
		dialView.resize = 2;
		dialView.action = {
			ctl.internal = dialView.value;
		};
		dialView.action.value;
	}
		
	updateDialView {
		dialView.value = ctl.internal;
	}

	setupValueView {
		valueView = GUI.button.new(layoutView, Rect(0, 0, 80, 16));
		valueView.font = CtlView.font;
		valueView.states = [[ctl.value.round(0.01).asString,
			Color.black, basicColor]];
		valueView.action = {
			arg button;
			ctl.update;
		};
	}

	updateValueView {
		valueView.states = [[ctl.value.round(0.01).asString,
			Color.black, basicColor]];
		valueView.value = 0;
	}

	update {
		this.updateNameView;
		this.updateDialView;
		this.updateValueView;
	}

	init {
		arg argCtl, argParent, argBasicColor;
		parent = argParent;
		ctl = argCtl;
		basicColor = argBasicColor;
		updateProc = {
			arg index, spec, value, state;
			{ this.update; }.defer;
		};
		layoutView = GUI.vLayoutView.new(parent,
			Rect(0, 0, CtlView.width, CtlView.height));
		layoutView.onClose = {
			ctl.removeRecv(updateProc);
		};
		layoutView.spacing = 0;
		this.setupNameView;
		this.setupDialView;
		this.setupValueView;
		ctl.addRecv(updateProc);
	}
}
