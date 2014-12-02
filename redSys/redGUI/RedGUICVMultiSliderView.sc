//redFrik

RedGUICVMultiSliderView {
	classvar <defaultWidth= 25,<defaultHeight= 25;
	var <>cv, <multiSlider, <now;
	*new {|parent, bounds, cv|
		^super.new.init(parent, bounds, cv);
	}
	init {|parent, bounds, argCV|
		cv= argCV;
		this.prMake(parent, bounds);
		cv.connect(multiSlider);
		this.prMake2;
	}
	value {^cv.input}
	value_ {|val| cv.input= val}
	save {now= multiSlider.value}
	interp {|val, target| cv.input= now.blend(target, val)}
	
	//--private
	prMake {|parent, bounds|
		if(bounds.isNil, {
			bounds= Rect(0, 0, defaultWidth, defaultHeight*cv.value.size);
		}, {
			bounds= bounds.asRect;
		});
		multiSlider= RedMultiSliderView(parent, bounds);
	}
	prMake2 {
		multiSlider.indexIsHorizontal= false;
		multiSlider.indexThumbSize= multiSlider.bounds.height/cv.value.size-4;
		multiSlider.valueThumbSize= 0;
		multiSlider.gap= 4;
		multiSlider.isFilled= true;
		//multiSlider.mouseMoveAction= {|view| view.value.postln};
	}
}

RedGUICVMultiSliderViewMirror : RedGUICVMultiSliderView {
	init {|parent, bounds, argCV|
		cv= argCV;
		this.prMake(parent, bounds);
		multiSlider.value= cv.input;
		this.prMake2;
	}
	value {^multiSlider.value}
	value_ {|val| multiSlider.valueAction= val}
	save {}
	interp {}
	sync {multiSlider.value= cv.input}
}

/*
(
w= Window("test", Rect(100, 200, 300, 400));
w.front;
w.view.decorator= FlowLayout(w.view.bounds);
a= CV.new.sp({1.0.rand}.dup(8), 0, 1, 0, \lin);
b= RedGUICVMultiSliderView(w.view, nil, a);
RedGUICVMultiSliderView(w.view, nil, a);
w.view.decorator.nextLine;
c= RedGUICVMultiSliderViewMirror(w.view, nil, a);
RedGUICVMultiSliderViewMirror(w.view, nil, a);
)
a.input= {1.0.rand}.dup(8)
c.sync
*/
