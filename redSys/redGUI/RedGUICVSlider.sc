//redFrik

RedGUICVSlider {
	classvar <defaultWidth= 16, <defaultHeight= 70;
	var <>cv, <slider, <now;
	*new {|parent, bounds, cv|
		^super.new.init(parent, bounds, cv);
	}
	init {|parent, bounds, argCV|
		cv= argCV;
		this.prMake(parent, bounds);
		cv.connect(slider);
	}
	value {^cv.input}
	value_ {|val| cv.input= val}
	save {now= slider.value}
	interp {|val, target| cv.input= now.blend(target, val)}
	
	//--private
	prMake {|parent, bounds|
		if(bounds.isNil, {
			bounds= Rect(0, 0, defaultWidth, defaultHeight);
		}, {
			bounds= bounds.asRect;
		});
		slider= RedSlider(parent, bounds);
	}
}

RedGUICVSliderMirror : RedGUICVSlider {
	init {|parent, bounds, argCV|
		cv= argCV;
		this.prMake(parent, bounds);
		slider.value= cv.input;
	}
	value {^slider.value}
	value_ {|val| slider.valueAction= val}
	save {}
	interp {}
	sync {slider.value= cv.input}
}

/*
(
w= Window("test", Rect(100, 200, 300, 400));
w.front;
w.view.decorator= FlowLayout(w.view.bounds);
a= CV.new.spec_(\freq.asSpec);
b= RedGUICVSlider(w.view, nil, a);
RedGUICVSlider(w.view, nil, a);
w.view.decorator.nextLine;
c= RedGUICVSliderMirror(w.view, nil, a);
RedGUICVSliderMirror(w.view, nil, a);
)
a.input= 1.0.rand
c.sync
*/
