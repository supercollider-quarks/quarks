//redFrik

RedGUICVButton {
	classvar /*<defaultWidth= 40,*/ <defaultHeight= 14;
	var <>cv, <button, <now;
	*new {|parent, bounds, cv ...str|
		^super.new.init(parent, bounds, cv, str);
	}
	init {|parent, bounds, argCV, str|
		cv= argCV;
		this.prMake(parent, bounds, str);
		cv.connect(button);
	}
	value {^cv.input}
	value_ {|val| cv.input= val}
	save {now= button.value}
	interp {|val, target| cv.input= now.blend(target, val)}
	
	//--private
	prMake {|parent, bounds, str|
		/*
		if(bounds.isNil, {
			bounds= Rect(0, 0, defaultWidth, defaultHeight);
		}, {
			bounds= bounds.asRect;
		});
		*/
		button= RedButton(parent, bounds, *str);
	}
}

RedGUICVButtonMirror : RedGUICVButton {
	init {|parent, bounds, argCV, str|
		cv= argCV;
		this.prMake(parent, bounds, str);
		button.value= cv.input;
	}
	value {^button.value}
	value_ {|val| button.valueAction= val}
	save {}
	interp {}
	sync {button.value= cv.input}
}

/*
(
w= Window("test", Rect(100, 200, 300, 400));
w.front;
w.view.decorator= FlowLayout(w.view.bounds);
a= CV.new.spec_(ControlSpec(0, 1, \lin, 1, 0));
b= RedGUICVButton(w.view, nil, a, "test", "asdf");
RedGUICVButton(w.view, nil, a, "test", "asdf");
w.view.decorator.nextLine;
c= RedGUICVButtonMirror(w.view, nil, a, "test", "asdf");
RedGUICVButtonMirror(w.view, nil, a, "test", "asdf");
)
a.input= 2.rand
c.sync
*/
