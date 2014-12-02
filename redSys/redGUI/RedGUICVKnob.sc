//redFrik

RedGUICVKnob {
	classvar <defaultWidth= 48, <defaultHeight= 48;
	var <>cv, <knob, <now;
	*new {|parent, bounds, cv|
		^super.new.init(parent, bounds, cv);
	}
	init {|parent, bounds, argCV|
		cv= argCV;
		this.prMake(parent, bounds);
		cv.connect(knob);
	}
	value {^cv.input}
	value_ {|val| cv.input= val}
	save {now= knob.value}
	interp {|val, target| cv.input= now.blend(target, val)}
	
	//--private
	prMake {|parent, bounds|
		if(bounds.isNil, {
			 bounds= Rect(0, 0, defaultWidth, defaultHeight);
		}, {
			bounds= bounds.asRect;
		});
		knob= RedKnob(parent, bounds);
	}
}

RedGUICVKnobMirror : RedGUICVKnob {
	init {|parent, bounds, argCV|
		cv= argCV;
		this.prMake(parent, bounds);
		knob.value= cv.input;
	}
	value {^knob.value}
	value_ {|val| knob.valueAction= val}
	save {}
	interp {}
	sync {
		knob.value= cv.input;
	}
}

/*
(
w= Window("test", Rect(100, 200, 300, 400));
w.front;
w.view.decorator= FlowLayout(w.view.bounds);
a= CV.new.spec_(\freq.asSpec);
b= RedGUICVKnob(w.view, nil, a);
RedGUICVKnob(w.view, nil, a);
w.view.decorator.nextLine;
c= RedGUICVKnobMirror(w.view, nil, a);
RedGUICVKnobMirror(w.view, nil, a);
)
a.input= 1.0.rand
c.sync
*/
