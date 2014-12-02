
DualNumberEditor : NumberEditor {
		// a number editor with an integer part and a fraction part
	var <int, <fract, <intspec, <fractspec;
	
	init { arg val, sp;
		spec = sp.asSpec ?? {ControlSpec.new};
		fractspec = ControlSpec(0, 1-spec.step, \linear, spec.step);
		intspec = ControlSpec(spec.minval.asInteger, spec.maxval.asInteger, \linear, 1);
		this.value_(val);
	}
	
	value { ^(int + fract) }
	
	poll { ^(int + fract) }		// so the gui will work
	
	activeValue_ { arg val;
		int = val.asInteger;
		fract = (val-int).round(fractspec.step);
	}
	
	value_ { arg val;
		int = val.asInteger;
		fract = (val-int).round(fractspec.step);
	}
	
	guiClass { ^DualNumberEditorGUI }
}

