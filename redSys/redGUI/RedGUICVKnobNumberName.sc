//redFrik

RedGUICVKnobNumberName {
	classvar <defaultWidth= 60, <defaultHeight= 70;	//bounds are hardcoded for this view
	var <>cv, <knob, <numb, <>name, <now;
	*new {|parent, bounds, cv, name|
		^super.new.init(parent, bounds, cv, name);
	}
	init {|parent, bounds, argCV, argName|
		cv= argCV;
		name= argName;
		this.prMake(parent, bounds, name);
		cv.connect(knob);
		cv.connect(numb);
	}
	value {^cv.input}
	value_ {|val| cv.input= val}
	save {now= knob.value}
	interp {|val, target| cv.input= now.blend(target, val)}
	
	//--private
	prMake {|parent, bounds, name|
		var cmp, width1, height1;
		if(bounds.isNil, {
			bounds= Rect(0, 0, defaultWidth, defaultHeight);
		}, {
			bounds= bounds.asRect;
		});
		cmp= CompositeView(parent, bounds);
		cmp.decorator= FlowLayout(bounds, 4@0, 4@0);
		width1= bounds.width*0.8;
		height1= 14;
		knob= RedKnob(cmp, width1@width1);
		cmp.decorator.nextLine;
		numb= RedNumberBox(cmp, width1@height1);
		cmp.decorator.nextLine;
		RedStaticText(cmp, name.asString);
	}
}

RedGUICVKnobNumberNameMirror : RedGUICVKnobNumberName {
	init {|parent, bounds, argCV, argName|
		cv= argCV;
		name= argName;
		this.prMake(parent, bounds, name);
		knob.value= cv.input;
		knob.action= {|view| numb.value= cv.spec.map(view.value)};
		numb.value= cv.value;
		numb.action= {|view| knob.value= cv.spec.unmap(view.value)};
	}
	value {^knob.value}
	value_ {|val| knob.valueAction= val}
	save {}
	interp {}
	sync {
		knob.value= cv.input;
		numb.value= cv.value;
	}
}

/*
(
w= Window("test", Rect(100, 200, 300, 400));
w.front;
w.view.decorator= FlowLayout(w.view.bounds);
a= CV.new.spec_(\freq.asSpec);
b= RedGUICVKnobNumberName(w.view, nil, a, "testtest");
RedGUICVKnobNumberName(w.view, nil, a, "testtest");
w.view.decorator.nextLine;
c= RedGUICVKnobNumberNameMirror(w.view, nil, a, "testtest");
RedGUICVKnobNumberNameMirror(w.view, nil, a, "testtest");
)
a.input= 1.0.rand
c.sync
*/
