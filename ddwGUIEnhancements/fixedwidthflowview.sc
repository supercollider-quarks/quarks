
	// a custom flowview that never changes its width upon resizeToFit

FixedWidthFlowView : FlowView {
	resizeToFitContents { arg thorough, level;
			// need bounds relative to parent's bounds
		var new, maxpt, comparept, mybounds, used;
		mybounds = this.bounds;
		maxpt = mybounds.leftTop;	// w/o this, maxpt could be above or left of top left-bad
		this.children.do({ arg c;
			comparept = c.findRightBottom;
			maxpt = maxpt.max(comparept);
		});
//		if(view.tryPerform(\relativeOrigin) ? false) {
			new = mybounds.resizeTo(mybounds.width, maxpt.y + this.decorator.margin.y);
//		} {
//			new = mybounds.resizeTo(mybounds.width,
//				maxpt.y - mybounds.top + this.decorator.margin.y);
//		};
		this.bounds_(new, reflow: false);	// don't reflow unless asked
		^new
	}

	findRightBottom {		// a containerview can find the lowest-right point occupied by
		var bottom;		// an actual displaying view (slider, button, etc)

			// this is different from SCContainerView-findRightBottom because
			// the right edge of the FixedWidthFlowView must not change, even
			// if the contents don't fill it

		bottom = this.bounds.bottom;
		this.children.do({ arg c;
			bottom = bottom.max(c.findRightBottom.y);
		});
		^Point(this.bounds.right, bottom)
	}

}

