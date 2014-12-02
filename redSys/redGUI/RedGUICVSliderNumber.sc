//redFrik

RedGUICVSliderNumber {
	classvar <>defaultWidth= 40, <>defaultHeight= 128;
	var <>cv, <slider, <numb, <now;
	*new {|parent, bounds, cv|
		^super.new.init(parent, bounds, cv);
	}
	init {|parent, bounds, argCV|
		cv= argCV;
		this.prMake(parent, bounds);
		cv.connect(slider);
		cv.connect(numb);
	}
	value {^cv.input}
	value_ {|val| cv.input= val}
	save {now= slider.value}
	interp {|val, target| cv.input= now.blend(target, val)}
	
	//--private
	prMake {|parent, bounds|
		var cmp;
		if(bounds.isNil, {
			bounds= Rect(0, 0, defaultWidth, defaultHeight);
		}, {
			bounds= bounds.asRect;
		});
		cmp= CompositeView(parent, bounds);
		cmp.decorator= FlowLayout(bounds, 4@0, 4@0);
		numb= RedNumberBox(cmp, cmp.decorator.indentedRemaining.width@14);
		cmp.decorator.nextLine;
		slider= RedSlider(cmp, cmp.decorator.indentedRemaining.width@(bounds.height-14));
	}
}

RedGUICVSliderNumberMirror : RedGUICVSliderNumber {
	init {|parent, bounds, argCV|
		cv= argCV;
		this.prMake(parent, bounds);
		slider.value= cv.input;
		slider.action= {|view| numb.value= cv.spec.map(view.value)};
		numb.value= cv.value;
		numb.action= {|view| slider.value= cv.spec.unmap(view.value)};
	}
	value {^slider.value}
	value_ {|val| slider.valueAction= val}
	save {}
	interp {}
	sync {
		slider.value= cv.input;
		numb.value= cv.value;
	}
}
/*
(
w= Window("test", Rect(100, 200, 300, 400));
w.front;
w.view.decorator= FlowLayout(w.view.bounds);
a= CV.new.spec_(\freq.asSpec);
b= RedGUICVSliderNumber(w.view, nil, a, "testtest");
RedGUICVSliderNumber(w.view, nil, a, "testtest");
w.view.decorator.nextLine;
c= RedGUICVSliderNumberMirror(w.view, nil, a, "testtest");
RedGUICVSliderNumberMirror(w.view, nil, a, "testtest");
)
a.input= 1.0.rand
c.sync
*/
