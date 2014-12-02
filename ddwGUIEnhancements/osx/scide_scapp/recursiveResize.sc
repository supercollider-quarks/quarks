
// resize everything in a view's chain

+ SCContainerView {
	recursiveResize {
		children.do({ arg c;
			c.recursiveResize;
		});
	}
	
		// a containerview can find the lowest-right point occupied by its children
	findRightBottom {
		var origin = this.bounds.leftTop, maxpt;
		if(this.class.instVarNames.includes(\relativeOrigin).not or: {
			this.tryPerform(\relativeOrigin) ? false
		}) {
			maxpt = Point(0, 0);
		} {
			maxpt = origin;
		};
		children.do({ arg c;
			maxpt = maxpt.max(c.findRightBottom);
		});
		if(this.class.instVarNames.includes(\relativeOrigin).not or: {
			this.tryPerform(\relativeOrigin) ? false
		}) {
			maxpt = maxpt + origin;
		};
		if(decorator.notNil) {
			maxpt = maxpt + decorator.margin;
		};
		^maxpt
	}
}


+ SCView {
	recursiveResize { ^nil }	// the buck stops here
	
		// non-recursive: give result to caller
	findRightBottom {
		^this.bounds.rightBottom
	}

	isActive { ^dataptr.notNil }

	isView { ^true }
}
