
+ MultiPageLayout {
	recursiveResize {
		this.checkNotClosed.if({
			this.view.recursiveResize;
			this.resizeToFit;
		});
	}
}

// despite "SC" prefix, SCViewHolder is not a cocoa view! Thus not osx-specific
+ SCViewHolder {
	findRightBottom { 
		^view.findRightBottom;
	}
}

+ FlowView {
	resizeToFitContents {
			// need bounds relative to parent's bounds
		var new, maxpt, mybounds, used;
		mybounds = this.bounds;
		maxpt = Point(0, 0);
		this.children.do({ arg c;
			maxpt = maxpt.max(c.findRightBottom);
		});
		new = mybounds.resizeTo(maxpt.x + this.decorator.margin.x,
			maxpt.y + this.decorator.margin.y);
		this.bounds_(new, reflow: false);	// don't reflow unless asked
		^new
	}

	recursiveResize {
		this.children.do({ arg c;
			c.recursiveResize;
		});
		this.tryPerform(\reflowAll);
		this.tryPerform(\resizeToFitContents).isNil.if({
			this.tryPerform(\resizeToFit);
		});
	}
}

+ Object {
		// non-views should reply with false
	isActive { ^false }
	isView { ^false }
}


+ Point {
	max { arg that;
		^Point(this.x.max(that.x), this.y.max(that.y))
	}
	
	min { arg that;
		^Point(this.x.min(that.x), this.y.min(that.y))
	}
}

// for debugging

+ Integer {
	reptChar { arg c = $\t;
		^(c ! this).as(String);
	}
}
