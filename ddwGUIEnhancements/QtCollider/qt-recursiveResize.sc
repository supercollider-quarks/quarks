
// resize everything in a view's chain

+ QView {
	recursiveResize {
		var owner;
		if((owner = this.decorator.tryPerform(\owner)).notNil) {
			owner.recursiveResize;
		};
	}
	
	findRightBottom {
		var maxpt;
		maxpt = this.bounds.rightBottom;
		if(decorator.notNil) {
			maxpt = maxpt + decorator.margin;
		};
		^maxpt
	}

	isActive { ^this.isClosed.not }

	isView { ^true }
}

// + JSCView {
// 	recursiveResize { ^nil }	// the buck stops here
	
// 	findRightBottom { ^this.bounds.rightBottom }	// non-recursive: give result to caller

// 	isActive { ^dataptr.notNil }

// 	isView { ^true }
// }

+ QDragView {
	silentObject_ { |obj| object = obj }
}
