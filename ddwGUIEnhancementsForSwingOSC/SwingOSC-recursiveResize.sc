
// resize everything in a view's chain

+ JSCContainerView {
	recursiveResize {
		children.do({ arg c;
			c.recursiveResize;
		});
	}
	
	findRightBottom {
		var origin = this.bounds.leftTop, maxpt;
		if(relativeOrigin ? false) {
			maxpt = Point(0, 0);
		} {
			maxpt = origin;
		};
		children.do({ arg c;
			maxpt = maxpt.max(c.findRightBottom);
		});
		if(relativeOrigin ? false) {
			maxpt = maxpt + origin;
		};
		if(decorator.notNil) {
			maxpt = maxpt + decorator.margin;
		};
		^maxpt
	}
}

+ JSCView {
	recursiveResize { ^nil }	// the buck stops here
	
	findRightBottom { ^this.bounds.rightBottom }	// non-recursive: give result to caller

	isActive { ^dataptr.notNil }

	isView { ^true }
}

+ JSCDragView {
	silentObject_ { |obj| object = obj }
}
